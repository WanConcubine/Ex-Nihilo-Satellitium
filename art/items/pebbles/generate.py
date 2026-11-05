#!/usr/bin/env python3
"""Hand-authored 16x16 pebble textures (dirt sifting).

Five kinds, one silhouette, five palettes: cobblestone (mid grey), andesite
(light grey), diorite (near white), granite (pink-brown) and deepslate (blue-grey,
the 2026-11-05 Ender IO source). The shape is a small lump with the light coming
from the top-left, matching the stone blocks they craft into.

    python3 art/items/pebbles/generate.py
    python3 art/items/pebbles/generate.py --out path/to/assets/exnihilosatellitium/textures/item
"""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image

# 0=outline 1=dark 2=base 3=light 4=highlight
PEBBLE = """
................
................
................
.......00.......
......0440......
.....044330.....
....04433310....
...0443333210...
...0433332210...
...0433222210...
....03222210....
.....032210.....
......0110......
.......00.......
................
................
"""

# name -> (outline, dark, base, light, highlight)
PALETTES = {
    "cobblestone_pebble": ("#3F3F3F", "#6A6A6A", "#8A8A8A", "#A8A8A8", "#C6C6C6"),
    "andesite_pebble": ("#4A4A4A", "#767676", "#9A9A9A", "#B6B6B6", "#D2D2D2"),
    "diorite_pebble": ("#6B6B6B", "#A8A8A8", "#CCCCCC", "#E4E4E4", "#F6F6F6"),
    "granite_pebble": ("#4A2E26", "#7A4A3C", "#9C6A5A", "#BC8A78", "#D8A896"),
    "deepslate_pebble": ("#26262C", "#3C3C44", "#52525C", "#6A6A74", "#888892"),
}

ROOT = Path(__file__).resolve().parents[3]
DEFAULT_OUT = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"


def hex_rgb(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def parse_template(text: str) -> list[str]:
    rows = [row for row in text.strip().splitlines() if row]
    if len(rows) != 16 or any(len(row) != 16 for row in rows):
        raise ValueError(f"template must be 16x16, got {len(rows)} rows")
    return rows


def pebble(palette: tuple[str, ...]) -> Image.Image:
    colours = [hex_rgb(colour) for colour in palette]
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(parse_template(PEBBLE)):
        for x, cell in enumerate(row):
            if cell == ".":
                continue
            image.putpixel((x, y), colours[int(cell)] + (255,))
    return image


def main(argv=None) -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT,
                        help="texture directory to write into")
    args = parser.parse_args(argv)

    args.out.mkdir(parents=True, exist_ok=True)
    for name, palette in PALETTES.items():
        image = pebble(palette)
        image.save(args.out / f"{name}.png")
        # 4x preview next to the script, handy when tweaking the palette
        image.resize((64, 64), Image.NEAREST).save(
            Path(__file__).parent / f"{name}_preview.png")
        print(f"wrote {args.out / f'{name}.png'}")


if __name__ == "__main__":
    main()
