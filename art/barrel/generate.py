"""Hand-authored 16x16 barrel textures (modern vanilla style).

Deterministic pixel art, written pixel by pixel. Not a generated image.

    python3 generate.py
"""

from PIL import Image, ImageDraw, ImageFont

# --- palette (warm weathered oak + dark iron) -------------------------------
W0 = (52, 35, 22)      # seams / outline
W1 = (84, 58, 36)      # shadow
W2 = (112, 80, 50)     # base
W3 = (134, 99, 64)     # light
W4 = (156, 119, 78)    # highlight

I0 = (34, 34, 34)      # iron outline
I1 = (55, 55, 55)      # iron shadow
I2 = (80, 80, 80)      # iron base
I3 = (110, 110, 110)   # iron highlight

SEAMS = {3, 7, 11, 15}
RIVETS = {2, 8, 14}

# stave ramp: W1 -> W4. stave index picks the middle, row shade darkens it.
RAMP = [W1, W2, W3, W4]
STAVE_BASE = [1, 2, 1, 1]


def stave_colour(x, y):
    if x in SEAMS:
        return W0

    stave = x // 4
    offset = x % 4
    base = STAVE_BASE[stave]

    if offset == 0:
        return RAMP[min(3, base + 2)]
    if offset == 1:
        return RAMP[min(3, base + 1)]
    if (y * 3 + stave * 7) % 13 == 0:
        return W1
    return RAMP[base]


def side_texture():
    px = [[None] * 16 for _ in range(16)]

    for y in range(16):
        for x in range(16):
            if y == 0:
                colour = W0 if x in SEAMS else W4
            elif y == 1:
                colour = W0 if x in SEAMS else W3
            elif y == 2:
                colour = I3 if x in RIVETS else I2
            elif y == 3:
                colour = I0 if x in RIVETS else I1
            elif 4 <= y <= 11:
                colour = stave_colour(x, y)
            elif y == 12:
                colour = I3 if x in RIVETS else I2
            elif y == 13:
                colour = I0 if x in RIVETS else I1
            elif y == 14:
                colour = W0 if x in SEAMS else W2
            else:
                colour = W0

            px[y][x] = colour

    return px


def top_texture():
    """Squashed onto the 1px-wide rim of the open box, so keep it mostly even."""
    px = [[W4] * 16 for _ in range(16)]
    for x in range(16):
        if x in SEAMS:
            for y in range(16):
                px[y][x] = W3
    for x in range(16):
        px[15][x] = W2
    return px


def inner_texture():
    px = [[W2] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if y % 4 == 0:
                colour = W0
            elif y % 4 == 1:
                colour = W3
            else:
                colour = W2

            if x % 7 == 0 and y % 4 == 2:
                colour = W1
            if x in (0, 15) or y in (0, 15):
                colour = W0

            px[y][x] = colour
    return px


def bottom_texture():
    px = [[W1] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if x in SEAMS:
                colour = W0
            elif x % 4 == 0:
                colour = W2
            else:
                colour = W1
            if y in (0, 15) or x in (0, 15):
                colour = W0
            px[y][x] = colour
    return px


def compost_texture():
    c0 = (42, 30, 18)
    c1 = (62, 46, 28)
    c2 = (86, 66, 40)
    c3 = (108, 84, 52)
    c4 = (128, 102, 64)
    green = (58, 70, 32)
    palette = [c1, c2, c3, c4]

    px = [[c1] * 16 for _ in range(16)]
    for by in range(4):
        for bx in range(4):
            h = (bx * 7 + by * 13 + bx * by * 3) % 11
            base = palette[h % 4]
            for y in range(by * 4, (by + 1) * 4):
                for x in range(bx * 4, (bx + 1) * 4):
                    n = (x * 5 + y * 11 + x * y * 3) % 17
                    if n < 2:
                        colour = c0
                    elif n < 4:
                        colour = green
                    elif n < 6:
                        colour = c4
                    elif n < 8:
                        colour = palette[max(0, h % 4 - 1)]
                    else:
                        colour = base
                    px[y][x] = colour

    for cx, cy in ((3, 4), (9, 8), (6, 12), (12, 3)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            px[(cy + dy) % 16][(cx + dx) % 16] = c0

    return px


def to_image(px):
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), px[y][x] + (255,))
    return img


def scaled(img, scale):
    return img.resize((16 * scale, 16 * scale), Image.Resampling.NEAREST)


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
    side = to_image(side_texture())
    top = to_image(top_texture())
    inner = to_image(inner_texture())
    bottom = to_image(bottom_texture())

    side.save("barrel_side.png")
    top.save("barrel_top.png")
    inner.save("barrel_inner.png")
    bottom.save("barrel_bottom.png")

    compost = to_image(compost_texture())
    compost.save("barrel_compost.png")

    side_8x = scaled(side, 8)
    side_8x.save("barrel_side_8x.png")

    tiled = Image.new("RGBA", (32 * 8, 32 * 8))
    for ty in range(2):
        for tx in range(2):
            tiled.alpha_composite(side_8x, (tx * 128, ty * 128))

    preview = Image.new("RGBA", (760, 560), (24, 24, 24, 255))
    draw = ImageDraw.Draw(preview)
    f16 = font(16)
    f14 = font(14)
    f12 = font(12)

    draw.text((20, 12), "Ex Nihilo: Satellitium - barrel test (16x16, modern vanilla)", fill=(230, 230, 230), font=f16)
    draw.text((20, 38), "hand-authored pixels; no vanilla or AI textures", fill=(150, 150, 150), font=f12)

    draw.text((20, 66), "side", fill=(210, 210, 210), font=f14)
    preview.alpha_composite(side_8x, (20, 88))

    draw.text((20, 232), "2x2 tiling", fill=(210, 210, 210), font=f14)
    preview.alpha_composite(tiled, (20, 254))

    draw.text((300, 66), "top rim", fill=(210, 210, 210), font=f14)
    preview.alpha_composite(scaled(top, 8), (300, 88))
    draw.text((450, 66), "inner", fill=(210, 210, 210), font=f14)
    preview.alpha_composite(scaled(inner, 8), (450, 88))
    draw.text((600, 66), "bottom", fill=(210, 210, 210), font=f14)
    preview.alpha_composite(scaled(bottom, 8), (600, 88))

    draw.text((450, 240), "compost", fill=(210, 210, 210), font=f14)
    preview.alpha_composite(scaled(compost, 8), (450, 262))

    draw.text((300, 240), "game scale", fill=(210, 210, 210), font=f14)
    draw.text((300, 266), "1x", fill=(150, 150, 150), font=f12)
    draw.text((330, 266), "2x", fill=(150, 150, 150), font=f12)
    draw.text((370, 266), "4x", fill=(150, 150, 150), font=f12)
    preview.alpha_composite(side, (300, 286))
    preview.alpha_composite(scaled(side, 2), (330, 286))
    preview.alpha_composite(scaled(side, 4), (370, 286))

    draw.rectangle((19, 87, 19 + 128, 87 + 128), outline=(60, 60, 60))
    draw.rectangle((19, 253, 19 + 256, 253 + 256), outline=(60, 60, 60))

    preview.save("preview.png")
    print("wrote barrel_side.png, barrel_top.png, barrel_inner.png, barrel_bottom.png, preview.png")


if __name__ == "__main__":
    main()
