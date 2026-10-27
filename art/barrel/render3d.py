"""Software 3D preview of the barrel block.

Renders the actual block model (five cuboids) with the hand-authored 16x16
textures, using an isometric camera and a z-buffer. No external 3D library.

    python3 render3d.py
"""

import math

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

# --- canvas -----------------------------------------------------------------
SS = 2                       # supersampling
W, H = 1000, 1000
RW, RH = W * SS, H * SS

S = 26.0 * SS                # pixels per block unit
COS30 = math.cos(math.radians(30.0))
SIN30 = 0.5
CX = RW * 0.5
CY = RH * 0.50

# camera at (+inf, +inf, +inf), looking at the origin; larger depth is closer
def project(x, y, z):
    sx = (x - z) * COS30 * S + CX
    sy = (x + z) * SIN30 * S - y * S + CY
    return sx, sy


def depth(x, y, z):
    return x + y + z


def load_tex(name):
    img = Image.open(name).convert("RGB")
    return np.asarray(img, dtype=np.float32) / 255.0


TEX_SIDE = load_tex("barrel_side.png")
TEX_TOP = load_tex("barrel_top.png")
TEX_INNER = load_tex("barrel_inner.png")
TEX_BOTTOM = load_tex("barrel_bottom.png")

FACES = []


def add_quad(p0, p1, p2, p3, tex, shade):
    # UV (0,0) is the top-left of the texture; V grows downwards.
    FACES.append((
        np.array([p0, p1, p2, p3], dtype=np.float64),
        np.array([(0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0)], dtype=np.float64),
        tex,
        shade,
    ))


def add_box(x0, y0, z0, x1, y1, z1, tex, shades):
    # north / south / west / east / up / down
    add_quad((x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0), tex["north"], shades.get("north", 0.7))
    add_quad((x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1), tex["south"], shades.get("south", 0.8))
    add_quad((x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0), tex["west"], shades.get("west", 0.65))
    add_quad((x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0), tex["east"], shades.get("east", 0.85))
    add_quad((x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1), tex["up"], shades.get("up", 1.0))
    add_quad((x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1), tex["down"], shades.get("down", 0.45))


# --- the actual model: five cuboids of the open-top barrel ------------------
# bottom slab
add_box(1, 0, 1, 15, 1, 15,
        {"north": TEX_SIDE, "south": TEX_SIDE, "west": TEX_SIDE, "east": TEX_SIDE,
         "up": TEX_INNER, "down": TEX_BOTTOM},
        {"up": 0.5, "down": 0.35})

# north wall (outer face north, inner face south)
add_box(1, 1, 1, 15, 16, 2,
        {"north": TEX_SIDE, "south": TEX_INNER, "west": TEX_SIDE, "east": TEX_SIDE,
         "up": TEX_TOP, "down": TEX_SIDE},
        {"north": 0.68, "south": 0.5, "west": 0.58, "east": 0.58, "up": 1.0, "down": 0.35})

# south wall (outer face south, inner face north)
add_box(1, 1, 14, 15, 16, 15,
        {"north": TEX_INNER, "south": TEX_SIDE, "west": TEX_SIDE, "east": TEX_SIDE,
         "up": TEX_TOP, "down": TEX_SIDE},
        {"north": 0.5, "south": 0.88, "west": 0.58, "east": 0.58, "up": 1.0, "down": 0.35})

# west wall (outer face west, inner face east)
add_box(1, 1, 2, 2, 16, 14,
        {"north": TEX_SIDE, "south": TEX_SIDE, "west": TEX_SIDE, "east": TEX_INNER,
         "up": TEX_TOP, "down": TEX_SIDE},
        {"north": 0.55, "south": 0.55, "west": 0.72, "east": 0.5, "up": 1.0, "down": 0.35})

# east wall (outer face east, inner face west)
add_box(14, 1, 2, 15, 16, 14,
        {"north": TEX_SIDE, "south": TEX_SIDE, "west": TEX_INNER, "east": TEX_SIDE,
         "up": TEX_TOP, "down": TEX_SIDE},
        {"north": 0.55, "south": 0.55, "west": 0.5, "east": 0.92, "up": 1.0, "down": 0.35})


# --- rasterizer -------------------------------------------------------------
COLOR = np.zeros((RH, RW, 3), dtype=np.float32)
ZBUFFER = np.full((RH, RW), -1e9, dtype=np.float32)


def raster_triangle(scr, uvs, depths, tex, shade):
    (x0, y0), (x1, y1), (x2, y2) = scr
    xmin = max(int(math.floor(min(x0, x1, x2))), 0)
    xmax = min(int(math.ceil(max(x0, x1, x2))), RW - 1)
    ymin = max(int(math.floor(min(y0, y1, y2))), 0)
    ymax = min(int(math.ceil(max(y0, y1, y2))), RH - 1)
    if xmin > xmax or ymin > ymax:
        return

    xs = np.arange(xmin, xmax + 1, dtype=np.float32)
    ys = np.arange(ymin, ymax + 1, dtype=np.float32)
    xx, yy = np.meshgrid(xs, ys)

    denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
    if abs(denominator) < 1e-9:
        return

    w0 = ((y1 - y2) * (xx - x2) + (x2 - x1) * (yy - y2)) / denominator
    w1 = ((y2 - y0) * (xx - x2) + (x0 - x2) * (yy - y2)) / denominator
    w2 = 1.0 - w0 - w1

    inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
    if not inside.any():
        return

    u = w0 * uvs[0][0] + w1 * uvs[1][0] + w2 * uvs[2][0]
    v = w0 * uvs[0][1] + w1 * uvs[1][1] + w2 * uvs[2][1]
    d = w0 * depths[0] + w1 * depths[1] + w2 * depths[2]

    tx = np.clip((u * 16.0).astype(np.int32), 0, 15)
    ty = np.clip((v * 16.0).astype(np.int32), 0, 15)
    sampled = tex[ty, tx] * shade

    z_sub = ZBUFFER[ymin:ymax + 1, xmin:xmax + 1]
    c_sub = COLOR[ymin:ymax + 1, xmin:xmax + 1]
    mask = inside & (d > z_sub)
    if not mask.any():
        return

    z_sub[mask] = d[mask]
    c_sub[mask] = sampled[mask]


def render():
    for points, uvs, tex, shade in FACES:
        scr = np.array([project(*p) for p in points], dtype=np.float32)
        depths = [depth(*p) for p in points]

        for tri in ((0, 1, 2), (0, 2, 3)):
            raster_triangle(
                scr[list(tri)],
                uvs[list(tri)],
                [depths[i] for i in tri],
                tex,
                shade,
            )


def background():
    top = np.array([38, 42, 50], dtype=np.float32) / 255.0
    bottom = np.array([18, 20, 24], dtype=np.float32) / 255.0
    ramp = np.linspace(0.0, 1.0, RH, dtype=np.float32)[:, None, None]
    return (top * (1.0 - ramp) + bottom * ramp) * np.ones((1, RW, 1), dtype=np.float32)


def main():
    global COLOR
    COLOR = background()

    # soft shadow under the block
    shadow = Image.new("L", (RW, RH), 0)
    draw = ImageDraw.Draw(shadow)
    cx, cy = project(8, 0, 8)
    draw.ellipse((cx - 15 * S, cy - 4.5 * S, cx + 15 * S, cy + 4.5 * S), fill=110)
    shadow = shadow.filter(ImageFilter.GaussianBlur(18 * SS))
    shadow_np = np.asarray(shadow, dtype=np.float32)[:, :, None] / 255.0
    COLOR *= (1.0 - shadow_np * 0.75)

    render()

    img = Image.fromarray((np.clip(COLOR, 0.0, 1.0) * 255.0).astype(np.uint8), "RGB")
    img = img.resize((W, H), Image.Resampling.LANCZOS)
    img.save("render_3d.png")
    print("wrote render_3d.png")


if __name__ == "__main__":
    main()
