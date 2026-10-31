#!/usr/bin/env python3
"""Spores item texture (M6): a small puffball cluster plus loose spores.

    <python-with-pillow> art/items/spores/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

OUTLINE = (44, 58, 34, 255)
SHADOW = (78, 107, 56, 255)
BASE = (110, 145, 82, 255)
LIGHT = (150, 180, 120, 255)
PALE = (190, 210, 160, 255)

# 16x16 sprite: '#' outline, 's' shadow, 'b' base, 'l' light, 'p' pale highlight.
SPRITE = [
    "................",
    "................",
    "....p...........",
    ".........s......",
    "....####........",
    "...##llb#.......",
    "..##llbbb#..b...",
    "..#llbbbb#......",
    "..#lbbbb##..l...",
    "..#sbbbs#.......",
    "...######..b....",
    "......s.........",
    "..b.......s.....",
    ".......b........",
    "................",
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
    image.save(TEX / "spores.png")
    image.resize((256, 256), Image.NEAREST).save(Path(__file__).with_name("preview.png"))
    print("wrote spores.png")


if __name__ == "__main__":
    main()
