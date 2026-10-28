"""Hand-authored 16x16 ore-stage textures.

Style anchors supplied by the owner:
  - broken: a dark stone chunk with bright metal flecks;
  - crushed: loose, irregular metal fragments.

The silhouettes below are redrawn from those references; colours and the
rendering are ours.  No GPLv3 PNGs are copied.

  broken  -> ore gravel
  crushed -> ore sand
  powdered-> ore dust

Outputs are preview-only (art/items/ore_pieces/); promote to assets/ when the
M5 ore items exist.

    python3 generate.py
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

# 1=stone body 2=stone light O=metal fleck M=metal highlight
BROKEN = """
................
.......111......
......11111.....
.....1111111....
.....11112111...
.....O122O1111..
....OO1222O1111.
...121O1111OO11.
...O112O11111OO.
..111111O1121OO.
..1121211O1111..
..1111111O1111..
...1111111O11...
....11111O1O....
.....111........
................
"""

# O=metal base 3=metal light 2=metal shadow 1=metal dark
CRUSHED = """
................
................
.....OO...OOO...
...3OOOO...O3...
...3OOOO...32...
....3OO.........
.........OO3....
..O..OO.3OO2....
..OO.1...33.....
..OO...O....O...
..32.3OOO.OOO...
.....3OOO.OO1...
......33..3O....
.......1O.333...
................
................
"""

POWDER_CLUMPS = (
    (5, 8, ("44", "32")),
    (9, 9, ("34", "21")),
    (12, 8, ("44", "32")),
    (7, 11, ("1",)),
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

STONE = ("#141414", "#2E2E2E", "#4A4A4A", "#6A6A6A", "#8A8A8A")

BROKEN_LIGHTS = ((6, 4), (7, 4), (6, 5))

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


def inside(grid, x, y):
    return 0 <= x < 16 and 0 <= y < 16 and grid[y][x] != "."


def broken(metal):
    stone = [hex_rgb(c) for c in STONE]
    metal_palette = [hex_rgb(c) for c in PALETTES[metal]]
    grid = [list(row) for row in parse_template(BROKEN)]
    for x, y in BROKEN_LIGHTS:
        if grid[y][x] == "1":
            grid[y][x] = "2"
    grid = ["".join(row) for row in grid]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    for y in range(16):
        for x in range(16):
            code = grid[y][x]
            if code == "1":
                dark_edge = ((y + 1 < 16 and grid[y + 1][x] == ".")
                             or (x + 1 < 16 and grid[y][x + 1] == "."))
                colour = stone[0] if dark_edge else stone[1]
            elif code == "2":
                colour = stone[3] if (x + y) % 3 else stone[2]
            elif code == "O":
                colour = metal_palette[2] if (x + y) % 3 == 0 else metal_palette[3]
            elif code == "M":
                colour = metal_palette[4]
            else:
                continue
            img.putpixel((x, y), colour + (255,))
    return img


def crushed(metal):
    palette = [hex_rgb(c) for c in PALETTES[metal]]
    grid = parse_template(CRUSHED)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    for y in range(16):
        for x in range(16):
            code = grid[y][x]
            if code == "O":
                edge = ((y + 1 < 16 and grid[y + 1][x] == ".")
                        or (x + 1 < 16 and grid[y][x + 1] == "."))
                colour = palette[1] if edge else palette[2]
            elif code == "3":
                colour = palette[3]
            elif code == "2":
                colour = palette[1]
            elif code == "1":
                colour = palette[0]
            else:
                continue
            img.putpixel((x, y), colour + (255,))
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
