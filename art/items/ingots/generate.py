"""Hand-authored 16x16 ingot textures for the self-owned metals.

Every material that has no `ingot` entry in `materials.json` gets one of these,
so the ore chain ends in a usable item even without another mod.  The shape and
shading are ours; colours come from the same 5-shade palettes as the ore pieces.

    python3 generate.py            # writes PNGs into this directory
    python3 generate.py --preview  # also writes a zoomed contact sheet
"""

from __future__ import annotations

import importlib.util
import json
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
MATERIALS = ROOT / "src/main/resources/exnihilosatellitium/materials.json"
PIECES = HERE.parent / "ore_pieces/generate.py"

# Two visible faces of one bar, in 16x16 pixel space.  Long axis runs
# lower-left -> upper-right, which reads as an ingot instead of a blob.
TOP_FACE = ((2.5, 8.0), (11.0, 4.0), (15.0, 7.0), (6.5, 11.0))
FRONT_FACE = ((6.5, 11.0), (15.0, 7.0), (15.0, 9.5), (6.5, 13.5))
LEFT_FACE = ((2.5, 8.0), (6.5, 11.0), (6.5, 13.5), (2.5, 10.5))


def load_tools():
    spec = importlib.util.spec_from_file_location("ore_pieces", PIECES)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    module.PALETTES.setdefault("osmium", ("#26313D", "#3F5B78", "#608FC4", "#8FB4DC", "#C4DCF4"))
    return module


def inside(poly, x, y):
    px, py = x + 0.5, y + 0.5
    hit = False
    for i in range(len(poly)):
        x1, y1 = poly[i]
        x2, y2 = poly[(i + 1) % len(poly)]
        if (y1 > py) != (y2 > py):
            at_x = x1 + (py - y1) * (x2 - x1) / (y2 - y1)
            if px < at_x:
                hit = not hit
    return hit


def render(palette):
    """palette = (outline, shadow, base, light, highlight) as RGB tuples."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    top = [[inside(TOP_FACE, x, y) for x in range(16)] for y in range(16)]
    front = [[inside(FRONT_FACE, x, y) for x in range(16)] for y in range(16)]
    left = [[inside(LEFT_FACE, x, y) for x in range(16)] for y in range(16)]
    filled = [[top[y][x] or front[y][x] or left[y][x] for x in range(16)] for y in range(16)]

    def surrounded(mask, x, y):
        """True when all four orthogonal neighbours are inside the mask."""
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = x + dx, y + dy
            if not (0 <= nx < 16 and 0 <= ny < 16) or not mask[ny][nx]:
                return False
        return True

    for y in range(16):
        for x in range(16):
            if not filled[y][x]:
                continue
            edge = not surrounded(filled, x, y)
            # a dark crease between the lit top and the two side faces
            crease = top[y][x] and (
                (x > 0 and filled[y][x - 1] and not top[y][x - 1])
                or (x < 15 and filled[y][x + 1] and not top[y][x + 1])
                or (y > 0 and filled[y - 1][x] and not top[y - 1][x])
                or (y < 15 and filled[y + 1][x] and not top[y + 1][x]))
            if edge:
                # keep the lit top rim light: a full black outline reads as a
                # stone clump, not a metal bar
                if top[y][x]:
                    colour = palette[2]
                elif left[y][x]:
                    colour = palette[0]
                else:
                    colour = palette[1] if y < 9 else palette[0]
            elif crease:
                colour = palette[1]
            elif top[y][x]:
                if not surrounded(top, x, y):
                    colour = palette[4]
                elif (x + y) % 5 == 0:
                    colour = palette[4]
                elif (x * 3 + y) % 7 == 0:
                    colour = palette[2]
                else:
                    colour = palette[3]
            elif left[y][x]:
                colour = palette[1] if (x + y) % 5 else palette[2]
            else:
                if (x + y) % 6 == 0:
                    colour = palette[1]
                elif y >= 11:
                    colour = palette[1]
                else:
                    colour = palette[2]
            img.putpixel((x, y), colour + (255,))
    return img


def main() -> None:
    tools = load_tools()
    materials = json.loads(MATERIALS.read_text(encoding="utf-8"))["materials"]
    rendered = {}
    for material in materials:
        if material.get("enabled") is False or "ingot" in material:
            continue
        mid = material["id"]
        palette = [tools.hex_rgb(c) for c in tools.PALETTES[mid]]
        img = render(palette)
        img.save(HERE / f"{mid}_ingot.png")
        rendered[mid] = img

    scale = 8
    width = max(1, len(rendered)) * 18 * scale
    sheet = Image.new("RGBA", (width, 18 * scale), (24, 24, 28, 255))
    for index, (mid, img) in enumerate(rendered.items()):
        sheet.paste(img.resize((16 * scale, 16 * scale), Image.NEAREST),
                    (index * 18 * scale + scale, scale))
    sheet.save(HERE / "preview_ingots.png")
    print(f"wrote {len(rendered)} ingot textures + preview_ingots.png")


if __name__ == "__main__":
    main()
