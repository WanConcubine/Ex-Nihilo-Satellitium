"""Hand-authored 16x16 ore-stage textures.

Style anchor (from the owner): a dark stone chunk with bright metal flecks for
broken ore, and scattered small metal fragments for crushed ore.  Powdered ore
stays a low heap.  All pixels are redrawn here; no GPLv3 PNGs are copied.

  broken  -> ore gravel
  crushed -> ore sand
  powdered-> ore dust

Outputs are preview-only (art/items/ore_pieces/); promote to assets/ when the
M5 ore items exist.

    python3 generate.py
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

# 0=outline 1=dark 2=base 3=light 4=highlight M=metal light m=metal base
BROKEN = """
................
......0000......
....00111100....
...0111222110...
..011122222110..
..01122333M2110.
.011223332m1110.
.01122332211M10.
.01M22222111100.
.01122221111m0..
..011111111100..
..00111111100...
...0001111000...
.....00000......
................
................
"""

# Each stamp is a small hand-shaped fragment: light top, base middle, dark base.
CRUSHED_FRAGMENTS = (
    (2, 2, ("344", "332", "211")),
    (8, 1, ("34", "33", "21")),
    (12, 4, ("44", "21")),
    (4, 6, ("344", "211")),
    (9, 7, ("344", "332", "211")),
    (1, 10, ("44", "21")),
    (13, 10, ("34", "33", "21")),
    (6, 12, ("344", "211")),
    (10, 13, ("44", "21")),
    (7, 4, ("3",)),
    (14, 2, ("2",)),
    (3, 14, ("2",)),
    (15, 8, ("3",)),
)

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

# Lumpy powder highlights; applied only to interior pixels.
POWDER_CLUMPS = (
    (5, 8, ("44", "32")),
    (9, 9, ("34", "21")),
    (12, 8, ("44", "32")),
    (7, 11, ("1",)),
)

# Dark stone body for broken ore; the metal only shows in the flecks.
STONE = ("#141414", "#2A2A2A", "#444444", "#666666", "#8C8C8C")

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


def broken(metal):
    stone = [hex_rgb(c) for c in STONE]
    metal_palette = [hex_rgb(c) for c in PALETTES[metal]]
    codes = {str(i): stone[i] for i in range(5)}
    codes["M"] = metal_palette[3]
    codes["m"] = metal_palette[2]

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(parse_template(BROKEN)):
        for x, code in enumerate(row):
            if code in codes:
                img.putpixel((x, y), codes[code] + (255,))
    return img


def crushed(metal):
    palette = [hex_rgb(c) for c in PALETTES[metal]]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x0, y0, stamp in CRUSHED_FRAGMENTS:
        for dy, row in enumerate(stamp):
            for dx, code in enumerate(row):
                if code.isdigit():
                    img.putpixel((x0 + dx, y0 + dy), palette[int(code)] + (255,))
    return img


def powdered(metal):
    palette = [hex_rgb(c) for c in PALETTES[metal]]
    codes = {str(i): palette[i] for i in range(5)}
    grid = [list(row) for row in parse_template(POWDERED)]
    for x0, y0, stamp in POWDER_CLUMPS:
        for dy, row in enumerate(stamp):
            for dx, code in enumerate(row):
                x, y = x0 + dx, y0 + dy
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
        images[(metal, "broken")] = broken(metal)
        images[(metal, "crushed")] = crushed(metal)
        images[(metal, "powdered")] = powdered(metal)
        for stage in STAGES:
            images[(metal, stage)].save(out / f"{metal}_{stage}.png")

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
