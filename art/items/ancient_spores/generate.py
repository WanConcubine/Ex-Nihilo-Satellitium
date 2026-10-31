#!/usr/bin/env python3
"""Ancient Spores item texture (古代孢子, M6): purple puffball plus loose spores.

Purple keeps it visually tied to witch water; the original Ex Nihilo spore icon
is purple too (we only match the hue, the pixels are our own).

    <python-with-pillow> art/items/ancient_spores/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

OUTLINE = (58, 36, 86, 255)
SHADOW = (85, 52, 128, 255)
BASE = (122, 85, 184, 255)
LIGHT = (164, 127, 217, 255)
PALE = (201, 174, 239, 255)

# '#' outline, 's' shadow, 'b' base, 'l' light, 'p' pale highlight.
SPRITE = [
    "................",
    "...........p....",
    "...p............",
    ".........s......",
    "....####........",
    "...##llb#....b..",
    "..##llbbb#......",
    "..#llbbbb#..b...",
    "..#lbbbb##...l..",
    "..#sbbbs#..b....",
    "...######.......",
    "......s.....s...",
    "..b.........b...",
    ".......b........",
    "..........s.....",
    "................",
]

COLORS = {"#": OUTLINE, "s": SHADOW, "b": BASE, "l": LIGHT, "p": PALE, ".": (0, 0, 0, 0)}


def main() -> None:
    TEX.mkdir(parents=True, exist_ok=True)
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y, row in enumerate(SPRITE):
        for x, key in enumerate(row):
            pixels[x, y] = COLORS[key]
    image.save(TEX / "ancient_spores.png")
    image.resize((256, 256), Image.NEAREST).save(Path(__file__).with_name("preview.png"))
    print("wrote ancient_spores.png")


if __name__ == "__main__":
    main()
