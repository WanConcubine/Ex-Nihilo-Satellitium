"""Hand-authored 16x16 ore-stage textures in a modern vanilla style.

The original Ex Nihilo/Omnia progression has three distinct item stages:

  broken ore  ->  ore gravel block   (4 pieces -> 1 block)
  crushed ore ->  ore sand block     (4 pieces -> 1 block)
  powdered ore->  ore dust block     (4 pieces -> 1 block)

The old mod tinted a base/template pair at runtime.  We redraw three clean
ASCII silhouettes and tint them per metal: deliberate pixels, no noise, and
no GPLv3 PNGs.

Outputs are preview-only (art/items/ore_pieces/); nothing here is shipped
until the M5 ore items exist.

    python3 generate.py
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

# o=outline  s=shadow  b=base  l=light  h=highlight  .=transparent
BROKEN = """
................
......0000......
....00333300....
...0333333110...
..033333332110..
..0333333222110.
.03333322221110.
.03332222211110.
.01222222111100.
.0112222111100..
..011111111100..
..00111111100...
...0001111000...
.....00000......
................
................
"""

CRUSHED = """
................
................
..434....434....
.3321...3321....
.211....211.....
................
.....434........
.4..3321........
.32..211........
.21......4......
.32..434.3321...
.....3321.211...
......211.......
.......11.332...
................
................
"""

POWDERED = """
................
................
.......00.......
......0330......
.....033330.....
....03333330....
...0333333330...
..033333333320..
..133333333320..
..133333333320..
...1333333320...
....11333320....
......1111......
................
................
................
"""

STAGE_FACETS = {
    "broken": ((6, 4, "4"), (7, 4, "4"), (5, 5, "4"), (8, 7, "1"), (8, 8, "1")),
    "crushed": ((3, 3, "4"), (12, 2, "4"), (4, 11, "4")),
    "powdered": ((5, 8, "4"), (8, 7, "4"), (10, 9, "1"), (12, 8, "4"), (7, 10, "1")),
}

STAGE_TEMPLATES = {
    "broken": BROKEN,
    "crushed": CRUSHED,
    "powdered": POWDERED,
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


def parse_template(text):
    rows = [row for row in text.strip().splitlines() if row]
    if len(rows) != 16 or any(len(row) != 16 for row in rows):
        raise ValueError(f"template must be 16x16, got {len(rows)} rows")
    return rows


def texture(metal, stage):
    palette = PALETTES[metal]
    codes = {str(index): hex_rgb(colour) for index, colour in enumerate(palette)}
    grid = [list(row) for row in parse_template(STAGE_TEMPLATES[stage])]
    for x, y, code in STAGE_FACETS[stage]:
        if grid[y][x] not in ".0":
            grid[y][x] = code

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        for x, code in enumerate(row):
            if code in codes:
                img.putpixel((x, y), codes[code] + (255,))
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


def paste(canvas, img, x, y, scale):
    canvas.alpha_composite(img.resize((16 * scale, 16 * scale), Image.Resampling.NEAREST),
                           (x, y))


def main():
    out = Path(__file__).resolve().parent
    images = {}
    for metal in PALETTES:
        for stage in STAGES:
            img = texture(metal, stage)
            img.save(out / f"{metal}_{stage}.png")
            images[(metal, stage)] = img

    # Four representative metals across the three stages.
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
            paste(preview, images[(metal, stage)],
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
            paste(sheet, images[(metal, stage)],
                  70 + col * (cell + 20), y, scale)
    sheet.convert("RGB").save(out / "preview_all.png")

    print(f"wrote {len(images)} stage PNGs, preview_stages.png, preview_all.png")


if __name__ == "__main__":
    main()
