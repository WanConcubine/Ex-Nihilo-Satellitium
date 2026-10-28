"""Hand-authored 16x16 ore-stage textures in a modern vanilla style.

The original Ex Nihilo/Omnia progression has three distinct item stages:

  broken ore  ->  ore gravel block
  crushed ore ->  ore sand block
  powdered ore->  ore dust block

They used a tinted base/template pair; we redraw the three silhouettes with
per-metal palettes instead, keeping the project MIT and the stages readable
at 16x16.

Outputs are preview-only (art/items/ore_pieces/); nothing here is shipped
until the M5 ore items exist.

    python3 generate.py
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

# --- silhouettes -----------------------------------------------------------
# A single large chunk plus a small chip.  Broken ore should read as "chunk".
BROKEN_MAIN = {
    3: (6, 10),
    4: (5, 11),
    5: (4, 12),
    6: (4, 12),
    7: (4, 12),
    8: (4, 11),
    9: (4, 11),
    10: (5, 11),
    11: (6, 10),
    12: (7, 9),
}
BROKEN_CHIP = {
    4: (13, 14),
    5: (13, 15),
    6: (14, 15),
}

# Scattered angular fragments.  Crushed ore should read as "many small bits".
CRUSHED_CHUNKS = (
    (2, 3, 3, 3),
    (8, 2, 3, 3),
    (5, 6, 3, 3),
    (11, 7, 3, 3),
    (3, 9, 3, 3),
    (9, 10, 3, 3),
    (13, 4, 2, 2),
)

# A wide, low heap.  Powdered ore should read as "pile", not a rock.
POWDER_MOUND = {
    4: (7, 9),
    5: (6, 10),
    6: (5, 11),
    7: (4, 12),
    8: (3, 13),
    9: (2, 14),
    10: (2, 14),
    11: (1, 15),
    12: (2, 14),
    13: (3, 13),
    14: (4, 12),
}

STAGES = ("broken", "crushed", "powdered")

# name -> (outline, shadow, base, light, highlight)
PALETTES = {
    "iron": ("#3A3A3A", "#6F6F6F", "#B0B0B0", "#D4D4D4", "#F2F2F2"),
    "gold": ("#4A3600", "#947000", "#D6A400", "#F2CE44", "#FFF2A0"),
    "copper": ("#4A2418", "#86422E", "#BE6546", "#E08A62", "#F6B78E"),
    "tin": ("#344A44", "#6E8880", "#A2B8B0", "#C6D8D0", "#EAF4EE"),
    "lead": ("#2C2C36", "#545462", "#808090", "#A4A4B4", "#C6C6D6"),
    "silver": ("#36444C", "#82949C", "#BCC8CE", "#DEE8EC", "#F6FCFE"),
    "nickel": ("#3A362C", "#746E5E", "#A29A86", "#C4BCA4", "#E2DAC6"),
    "aluminum": ("#36414A", "#74828C", "#AAB4BE", "#CED8E0", "#ECF4FA"),
    "platinum": ("#30424C", "#6C8490", "#A2BAC6", "#C6DEE8", "#EAF8FE"),
    "uranium": ("#1E3A10", "#3E7A20", "#6FBF3A", "#A6E36A", "#D8F8A0"),
    "zinc": ("#34414A", "#6C7C86", "#9EACB4", "#C2D0D8", "#E2F0F6"),
}


def hex_rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def inside(mask, x, y):
    row = mask.get(y)
    return row is not None and row[0] <= x <= row[1]


def add_outline(px, mask, outline):
    for y in range(16):
        for x in range(16):
            if px[y][x] is not None:
                continue
            if (inside(mask, x - 1, y) or inside(mask, x + 1, y)
                    or inside(mask, x, y - 1) or inside(mask, x, y + 1)):
                px[y][x] = outline


def draw_mask(px, mask, palette, facets=None):
    outline, shadow, base, light, highlight = (hex_rgb(c) for c in palette)
    ys = list(mask)
    y0, y1 = min(ys), max(ys)

    for y in range(16):
        for x in range(16):
            if not inside(mask, x, y):
                continue
            left, right = mask[y]
            if x <= left + 1 and y <= y0 + 1:
                colour = light if (x + y) % 3 else highlight
            elif x >= right - 1 or y >= y1 - 1:
                colour = shadow
            else:
                colour = base
            px[y][x] = colour

    if facets:
        for x, y, colour in facets:
            if inside(mask, x, y):
                px[y][x] = {"s": shadow, "b": base, "l": light, "h": highlight}[colour]

    add_outline(px, mask, outline)


def draw_small_chunk(px, x0, y0, width, height, palette):
    outline, shadow, base, light, _highlight = (hex_rgb(c) for c in palette)
    mask = {}
    for y in range(height):
        row_y = y0 + y
        if y == 0 or y == height - 1:
            mask[row_y] = (x0 + 1, x0 + width - 2)
        else:
            mask[row_y] = (x0, x0 + width - 1)
    if width <= 2:
        mask = {y0 + y: (x0, x0 + width - 1) for y in range(height)}

    for y, (left, right) in mask.items():
        for x in range(left, right + 1):
            if x == left or y == min(mask) or x == right or y == max(mask):
                px[y][x] = light if (x == left and y == min(mask)) else shadow
            else:
                px[y][x] = base
    add_outline(px, mask, outline)


def texture(metal, stage):
    palette = PALETTES[metal]
    px = [[None] * 16 for _ in range(16)]

    if stage == "broken":
        draw_mask(px, BROKEN_MAIN, palette, facets=((6, 4, "h"), (7, 5, "l"),
                                                   (8, 8, "s"), (9, 9, "s")))
        # chip first so the main chunk's outline wins where they are close
        draw_mask(px, BROKEN_CHIP, palette)
    elif stage == "crushed":
        for chunk in CRUSHED_CHUNKS:
            draw_small_chunk(px, *chunk, palette)
    elif stage == "powdered":
        draw_mask(px, POWDER_MOUND, palette)
        # speckles make the heap read as loose powder rather than stone
        outline, shadow, _base, light, highlight = (hex_rgb(c) for c in palette)
        for x, y, colour in ((5, 6, highlight), (7, 5, light), (9, 5, shadow),
                             (11, 7, highlight), (4, 8, light), (8, 8, shadow),
                             (12, 8, light), (3, 10, highlight), (6, 10, shadow),
                             (10, 10, light), (13, 10, highlight), (5, 12, light),
                             (9, 12, shadow), (12, 12, highlight), (7, 13, light)):
            if px[y][x] not in (None, outline):
                px[y][x] = colour
    else:
        raise ValueError(stage)

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


def paste_scaled(canvas, img, x, y, scale):
    canvas.alpha_composite(img.resize((16 * scale, 16 * scale), Image.Resampling.NEAREST),
                           (x, y))


def main():
    out = Path(__file__).resolve().parent
    for old in list(out.glob("*_piece.png")) + [out / "preview.png"]:
        old.unlink(missing_ok=True)

    images = {}
    for metal in PALETTES:
        for stage in STAGES:
            img = to_image(texture(metal, stage))
            img.save(out / f"{metal}_{stage}.png")
            images[(metal, stage)] = img

    # Clear stage comparison for four representative metals.
    showcase = ("iron", "copper", "gold", "uranium")
    scale = 8
    cell = 16 * scale
    width = 20 + len(STAGES) * (cell + 24)
    height = 52 + len(showcase) * (cell + 36)
    preview = Image.new("RGBA", (width, height), (24, 24, 28, 255))
    draw = ImageDraw.Draw(preview)
    draw.text((16, 14), "ore stages (16x16, redrawn, MIT)",
              fill=(232, 232, 232), font=font(16))
    for col, stage in enumerate(STAGES):
        draw.text((20 + col * (cell + 24), 34), stage, fill=(195, 195, 195), font=font(14))
    for row, metal in enumerate(showcase):
        y = 58 + row * (cell + 36)
        draw.text((16, y + cell // 2), metal, fill=(195, 195, 195), font=font(14))
        for col, stage in enumerate(STAGES):
            paste_scaled(preview, images[(metal, stage)],
                         20 + col * (cell + 24) + 54, y, scale)
    preview.convert("RGB").save(out / "preview_stages.png")

    # Full 11-metal contact sheet at 3x.
    scale = 3
    cell = 16 * scale
    width = 20 + len(STAGES) * (cell + 20) + 60
    height = 48 + len(PALETTES) * (cell + 14)
    sheet = Image.new("RGBA", (width, height), (24, 24, 28, 255))
    draw = ImageDraw.Draw(sheet)
    draw.text((16, 12), "all ore stages - 11 metals (16x16, 3x)",
              fill=(230, 230, 230), font=font(14))
    for col, stage in enumerate(STAGES):
        draw.text((70 + col * (cell + 20), 30), stage, fill=(190, 190, 190), font=font(11))
    for row, metal in enumerate(PALETTES):
        y = 46 + row * (cell + 14)
        draw.text((12, y + cell // 2 - 4), metal, fill=(195, 195, 195), font=font(10))
        for col, stage in enumerate(STAGES):
            paste_scaled(sheet, images[(metal, stage)],
                         70 + col * (cell + 20), y, scale)
    sheet.convert("RGB").save(out / "preview_all.png")

    print(f"wrote {len(images)} stage PNGs, preview_stages.png, preview_all.png")


if __name__ == "__main__":
    main()
