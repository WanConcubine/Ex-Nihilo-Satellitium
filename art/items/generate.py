"""Hand-authored 16x16 porcelain clay item texture.

    python3 generate.py
"""

from PIL import Image, ImageDraw, ImageFont

OUTLINE = (78, 72, 62)
SHADOW = (156, 148, 134)
BASE = (214, 206, 192)
LIGHT = (236, 230, 219)
HIGHLIGHT = (250, 247, 240)
FLECK = (188, 178, 160)

# hand-shaped lump, one row per x-range
MASK = {
    4: (5, 10),
    5: (4, 11),
    6: (4, 12),
    7: (3, 12),
    8: (3, 12),
    9: (3, 12),
    10: (3, 12),
    11: (4, 12),
    12: (4, 11),
    13: (5, 10),
}


def inside(x, y):
    r = MASK.get(y)
    return r is not None and r[0] <= x <= r[1]


def texture():
    px = [[None] * 16 for _ in range(16)]

    for y in range(16):
        for x in range(16):
            if inside(x, y):
                if x <= 5 and y <= 7:
                    colour = LIGHT if (x + y) % 3 else HIGHLIGHT
                elif x >= 10 or y >= 11:
                    colour = SHADOW
                else:
                    colour = BASE

                if (x, y) == (5, 6):
                    colour = HIGHLIGHT

                px[y][x] = colour
            elif (inside(x - 1, y) or inside(x + 1, y)
                  or inside(x, y - 1) or inside(x, y + 1)):
                px[y][x] = OUTLINE

    for x, y in ((9, 9), (10, 8), (8, 12)):
        if inside(x, y):
            px[y][x] = FLECK

    return px


def to_image(px):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if px[y][x] is not None:
                img.putpixel((x, y), px[y][x] + (255,))
    return img


def main():
    img = to_image(texture())
    img.save("porcelain.png")
    img.resize((256, 256), Image.Resampling.NEAREST).save("porcelain_16x.png")

    preview = Image.new("RGBA", (320, 320), (24, 24, 24, 255))
    preview.alpha_composite(img.resize((256, 256), Image.Resampling.NEAREST), (32, 48))
    draw = ImageDraw.Draw(preview)
    try:
        font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 16)
    except OSError:
        font = ImageFont.load_default()
    draw.text((32, 16), "porcelain clay (16x16)", fill=(220, 220, 220), font=font)
    preview.save("preview.png")
    print("wrote porcelain.png, porcelain_16x.png, preview.png")


if __name__ == "__main__":
    main()
