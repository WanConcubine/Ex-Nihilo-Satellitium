"""Render the current barrel model with the real vanilla textures.

Outputs:
  render_current_3d.png    - in-world isometric view
  render_current_item.png  - GUI/inventory icon using display.gui
  showcase_current.png     - both, labelled

    python3 render_current.py
"""

import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
VANILLA_CACHE = OUT / ".vanilla_textures"
VANILLA_JAR = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"
NEEDED_TEXTURES = ("oak_planks", "spruce_planks", "gray_concrete")

for name in NEEDED_TEXTURES:
    target = VANILLA_CACHE / "assets/minecraft/textures/block" / f"{name}.png"
    if target.exists():
        continue
    with zipfile.ZipFile(VANILLA_JAR) as jar:
        jar.extract(f"assets/minecraft/textures/block/{name}.png", VANILLA_CACHE)

MODEL_PATH = str(ROOT / "src/main/resources/assets/exnihiloimitatio/models/block/barrel.json")
VANILLA_TEXTURES = str(VANILLA_CACHE / "assets/minecraft/textures")
MOD_TEXTURES = str(ROOT / "src/main/resources/assets/exnihiloimitatio/textures")

COS30 = math.cos(math.radians(30.0))
SIN30 = 0.5

SHADE = {
    "up": 1.0,
    "down": 0.45,
    "north": 0.68,
    "south": 0.86,
    "west": 0.74,
    "east": 0.92,
}

FACE_ORDER = ("up", "down", "north", "south", "west", "east")

MODEL = json.load(open(MODEL_PATH))
TEXTURES = MODEL["textures"]

_texture_cache = {}


def load_texture(reference: str, rotation: int = 0) -> np.ndarray:
    key = (reference, rotation)
    if key in _texture_cache:
        return _texture_cache[key]

    if reference.startswith("#"):
        reference = TEXTURES[reference[1:]]

    namespace, path = reference.split(":", 1)
    if namespace == "minecraft":
        filename = f"{VANILLA_TEXTURES}/{path}.png"
    elif namespace == "exnihiloimitatio":
        filename = f"{MOD_TEXTURES}/{path}.png"
    else:
        raise ValueError(f"unsupported texture {reference}")

    image = Image.open(filename).convert("RGB")
    if rotation:
        # Minecraft's face rotation is clockwise; PIL's positive angle is CCW.
        image = image.rotate(-rotation, resample=Image.Resampling.NEAREST, expand=False)
    data = np.asarray(image, dtype=np.float32) / 255.0
    _texture_cache[key] = data
    return data


def face_vertices(x0, y0, z0, x1, y1, z1, name):
    if name == "up":
        return [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)]
    if name == "down":
        return [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)]
    if name == "north":
        return [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)]
    if name == "south":
        return [(x1, y0, z1), (x0, y0, z1), (x0, y1, z1), (x1, y1, z1)]
    if name == "west":
        return [(x0, y0, z0), (x0, y0, z1), (x0, y1, z1), (x0, y1, z0)]
    if name == "east":
        return [(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)]
    raise ValueError(name)


def build_faces():
    faces = []
    for element in MODEL["elements"]:
        x0, y0, z0 = element["from"]
        x1, y1, z1 = element["to"]
        for name, data in element["faces"].items():
            texture = load_texture(data["texture"], data.get("rotation", 0))
            vertices = face_vertices(x0, y0, z0, x1, y1, z1, name)
            uvs = [(0.0, 1.0), (1.0, 1.0), (1.0, 0.0), (0.0, 0.0)]
            faces.append((vertices, uvs, texture, SHADE[name]))
    return faces


FACES = build_faces()


def rasterize(faces, project, width, height, background, z_sign=1.0):
    colour = background.copy()
    zbuffer = np.full((height, width), -1e9, dtype=np.float32)

    for vertices, uvs, texture, shade in faces:
        projected = [project(v) for v in vertices]
        for tri in ((0, 1, 2), (0, 2, 3)):
            idx = list(tri)
            scr = [projected[i] for i in idx]
            uv = [uvs[i] for i in idx]

            (x0, y0, d0), (x1, y1, d1), (x2, y2, d2) = scr
            xmin = max(int(math.floor(min(x0, x1, x2))), 0)
            xmax = min(int(math.ceil(max(x0, x1, x2))), width - 1)
            ymin = max(int(math.floor(min(y0, y1, y2))), 0)
            ymax = min(int(math.ceil(max(y0, y1, y2))), height - 1)
            if xmin > xmax or ymin > ymax:
                continue

            denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
            if abs(denominator) < 1e-9:
                continue

            xs = np.arange(xmin, xmax + 1, dtype=np.float32)
            ys = np.arange(ymin, ymax + 1, dtype=np.float32)
            xx, yy = np.meshgrid(xs, ys)

            w0 = ((y1 - y2) * (xx - x2) + (x2 - x1) * (yy - y2)) / denominator
            w1 = ((y2 - y0) * (xx - x2) + (x0 - x2) * (yy - y2)) / denominator
            w2 = 1.0 - w0 - w1
            inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
            if not inside.any():
                continue

            u = w0 * uv[0][0] + w1 * uv[1][0] + w2 * uv[2][0]
            v = w0 * uv[0][1] + w1 * uv[1][1] + w2 * uv[2][1]
            depth = (w0 * d0 + w1 * d1 + w2 * d2) * z_sign

            tx = np.clip((u * 16.0).astype(np.int32), 0, 15)
            ty = np.clip((v * 16.0).astype(np.int32), 0, 15)
            sampled = texture[ty, tx] * shade

            z_sub = zbuffer[ymin:ymax + 1, xmin:xmax + 1]
            c_sub = colour[ymin:ymax + 1, xmin:xmax + 1]
            mask = inside & (depth > z_sub)
            if mask.any():
                z_sub[mask] = depth[mask]
                c_sub[mask] = sampled[mask]

    return colour


def scale(image, size):
    return image.resize((size, size), Image.Resampling.LANCZOS)


def render_isometric(size=1000, ss=2):
    width = height = size * ss
    s = 24.0 * ss
    cx = width * 0.5
    cy = height * 0.52

    def project(p):
        x, y, z = p
        sx = (x - z) * COS30 * s + cx
        sy = (x + z) * SIN30 * s - y * s + cy
        return sx, sy, x + y + z

    top = np.array([38, 42, 50], dtype=np.float32) / 255.0
    bottom = np.array([18, 20, 24], dtype=np.float32) / 255.0
    ramp = np.linspace(0.0, 1.0, height, dtype=np.float32)[:, None, None]
    background = (top * (1.0 - ramp) + bottom * ramp) * np.ones((1, width, 1), dtype=np.float32)

    shadow = Image.new("L", (width, height), 0)
    draw = ImageDraw.Draw(shadow)
    sx, sy, _ = project((8, 0, 8))
    draw.ellipse((sx - 9.5 * s, sy - 3.2 * s, sx + 9.5 * s, sy + 3.2 * s), fill=110)
    shadow = shadow.filter(ImageFilter.GaussianBlur(18 * ss))
    shadow_np = np.asarray(shadow, dtype=np.float32)[:, :, None] / 255.0
    background *= (1.0 - shadow_np * 0.75)

    colour = rasterize(FACES, project, width, height, background, z_sign=1.0)
    image = Image.fromarray((np.clip(colour, 0, 1) * 255).astype(np.uint8), "RGB")
    return scale(image, size)


def render_item(size=512, ss=2):
    width = height = size * ss
    px = 22.0 * ss
    cx = width * 0.5
    cy = height * 0.52

    ax = math.radians(-30.0)
    ay = math.radians(225.0)

    def project(p):
        x, y, z = p[0] - 8.0, p[1] - 8.0, p[2] - 8.0
        # Minecraft applies Z, then Y, then X. Here Z is zero.
        x1 = x * math.cos(ay) + z * math.sin(ay)
        z1 = -x * math.sin(ay) + z * math.cos(ay)
        y2 = y * math.cos(ax) - z1 * math.sin(ax)
        z2 = y * math.sin(ax) + z1 * math.cos(ax)
        sx = cx + x1 * 0.625 * px
        sy = cy - y2 * 0.625 * px
        return sx, sy, z2

    background = np.zeros((height, width, 3), dtype=np.float32)
    background[:] = np.array([24, 24, 28], dtype=np.float32) / 255.0

    slot = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(slot)
    x0, y0, x1, y1 = 64 * ss, 64 * ss, width - 64 * ss, height - 64 * ss
    draw.rectangle((x0, y0, x1, y1), fill=(139, 139, 139, 255))
    draw.rectangle((x0, y0, x1, y0 + 4 * ss), fill=(55, 55, 55, 255))
    draw.rectangle((x0, y0, x0 + 4 * ss, y1), fill=(55, 55, 55, 255))
    draw.rectangle((x0, y1 - 4 * ss, x1, y1), fill=(255, 255, 255, 255))
    draw.rectangle((x1 - 4 * ss, y0, x1, y1), fill=(255, 255, 255, 255))
    draw.rectangle((x0 + 6 * ss, y0 + 6 * ss, x1 - 6 * ss, y1 - 6 * ss), fill=(139, 139, 139, 255))

    slot_np = np.asarray(slot, dtype=np.float32) / 255.0
    alpha = slot_np[:, :, 3:4]
    background = background * (1.0 - alpha) + slot_np[:, :, :3] * alpha

    colour = rasterize(FACES, project, width, height, background, z_sign=-1.0)
    image = Image.fromarray((np.clip(colour, 0, 1) * 255).astype(np.uint8), "RGB")
    return scale(image, size)


def font(size):
    for path in (
        "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
    ):
        try:
            return ImageFont.truetype(path, size)
        except OSError:
            continue
    return ImageFont.load_default()


def main():
    view = render_isometric()
    view.save(OUT / "render_current_3d.png")

    item = render_item()
    item.save(OUT / "render_current_item.png")

    canvas = Image.new("RGB", (1200, 700), (18, 19, 22))
    canvas.paste(scale(view, 640), (20, 40))
    canvas.paste(scale(item, 400), (760, 150))
    draw = ImageDraw.Draw(canvas)
    draw.text((20, 14), "current model - in-world 3/4 view", fill=(225, 225, 225), font=font(20))
    draw.text((760, 14), "current model - inventory icon (display.gui)", fill=(225, 225, 225), font=font(20))
    draw.text((760, 580), "open top + vanilla oak/spruce + dark hoop geometry", fill=(150, 150, 150), font=font(14))
    canvas.save(OUT / "showcase_current.png")
    print("wrote render_current_3d.png, render_current_item.png, showcase_current.png")


if __name__ == "__main__":
    main()
