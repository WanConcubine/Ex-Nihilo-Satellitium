"""Hand-authored 16x16 ore-piece textures in a modern vanilla style.

These are *our* pixels, not the old Ex Nihilo PNGs: a shared three-nugget
silhouette is tinted with a per-metal palette. The old mod used a tinted
base/template pair; redrawing gives us the same readable ore-piece shape
without copying GPLv3 art.

Outputs are preview-only (art/items/ore_pieces/); nothing here is shipped
until the M5 ore items exist.

    python3 generate.py
"""

from PIL import Image, ImageDraw, ImageFont

# y -> (x0, x1) inclusive; three separate nuggets so the cluster reads at 16x16.
LUMPS = (
    {
        3: (5, 9),
        4: (4, 11),
        5: (3, 11),
        6: (3, 11),
        7: (4, 11),
        8: (4, 10),
        9: (5, 9),
    },
    {
        9: (11, 13),
        10: (10, 14),
        11: (10, 14),
        12: (11, 13),
    },
    {
        11: (3, 5),
        12: (2, 5),
        13: (3, 5),
    },
)

# name -> (outline, shadow, base, light, highlight)
PALETTES = {
    "iron": ("#3A3A3A", "#777777", "#B4B4B4", "#D6D6D6", "#F2F2F2"),
    "gold": ("#4A3600", "#9A7400", "#D9A800", "#F4D24A", "#FFF3A6"),
    "copper": ("#4A2418", "#8A4632", "#C26A4A", "#E28E66", "#F6B78E"),
    "tin": ("#384448", "#76868C", "#A6B4BA", "#C8D4D8", "#EAF2F4"),
    "lead": ("#2C2C36", "#585866", "#848494", "#A6A6B6", "#C6C6D6"),
    "silver": ("#384448", "#84949A", "#BCC8CC", "#DCE6E8", "#F4FAFC"),
    "nickel": ("#3A362C", "#787262", "#A69E8A", "#C6BEA6", "#E4DCC8"),
    "aluminum": ("#384048", "#78848C", "#AEB6BE", "#D0D8DE", "#EEF4F8"),
    "platinum": ("#324048", "#70848C", "#A6BAC2", "#C8DCE4", "#EAF4F8"),
    "uranium": ("#1E3A10", "#3E7A20", "#6FBF3A", "#A6E36A", "#D8F8A0"),
    "zinc": ("#364048", "#707C84", "#A2AEB4", "#C4D0D6", "#E4F0F4"),
}


def hex_rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def inside(lump, x, y):
    row = lump.get(y)
    return row is not None and row[0] <= x <= row[1]


def draw_lump(px, lump, palette):
    outline, shadow, base, light, highlight = (hex_rgb(c) for c in palette)
    ys = list(lump)
    y0, y1 = min(ys), max(ys)

    for y in range(16):
        for x in range(16):
            if inside(lump, x, y):
                if x <= lump[y][0] + 1 and y <= y0 + 1:
                    colour = light if (x + y) % 3 else highlight
                elif x >= lump[y][1] - 1 or y >= y1 - 1:
                    colour = shadow
                else:
                    colour = base
                px[y][x] = colour
            elif (inside(lump, x - 1, y) or inside(lump, x + 1, y)
                  or inside(lump, x, y - 1) or inside(lump, x, y + 1)):
                px[y][x] = outline


def texture(palette):
    px = [[None] * 16 for _ in range(16)]
    for lump in LUMPS:
        draw_lump(px, lump, palette)

    # A couple of facets keep the cluster from looking like three flat blobs.
    outline, shadow, base, light, highlight = (hex_rgb(c) for c in palette)
    for x, y in ((6, 5), (12, 10), (4, 12)):
        if px[y][x] not in (None, outline):
            px[y][x] = highlight
    for x, y in ((8, 7), (13, 11)):
        if px[y][x] not in (None, outline):
            px[y][x] = shadow
    return px


def to_image(px):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if px[y][x] is not None:
                img.putpixel((x, y), px[y][x] + (255,))
    return img


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
    images = {}
    for name, palette in PALETTES.items():
        img = to_image(texture(palette))
        img.save(f"{name}_piece.png")
        images[name] = img

    scale = 8
    cell = 16 * scale
    cols = 4
    rows = (len(images) + cols - 1) // cols
    width = 24 + cols * (cell + 24)
    height = rows * (cell + 38) + 40
    preview = Image.new("RGB", (width, height), (24, 24, 28))
    draw = ImageDraw.Draw(preview)
    draw.text((16, 14), "ore pieces - 16x16, modern vanilla palette (redrawn, MIT)",
              fill=(230, 230, 230), font=font(18))

    for index, (name, img) in enumerate(images.items()):
        col = index % cols
        row = index // cols
        x = 16 + col * (cell + 24)
        y = 48 + row * (cell + 38)
        preview.paste(img.resize((cell, cell), Image.Resampling.NEAREST), (x, y))
        draw.text((x, y + cell + 6), name, fill=(205, 205, 205), font=font(14))

    preview.save("preview.png")
    print(f"wrote {len(images)} ore piece PNGs and preview.png")


if __name__ == "__main__":
    main()
