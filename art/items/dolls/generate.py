#!/usr/bin/env python3
"""Doll item textures (M6): doll / angry doll / creepy doll.

Hand-drawn 16x16 pixel art in the project palette (porcelain body, then a darker
angry variant and a witch-water purple creepy variant).

    <python-with-pillow> art/items/dolls/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

# 16x16 doll silhouette: head, body, two arms, two legs.
DOLL = [
    "................",
    ".....######.....",
    "....#bbbbbb#....",
    "...#bllbbllb#...",
    "...#bl#bb#lb#...",
    "...#bbbbbbbb#...",
    "....#bbbbbb#....",
    ".....#bbbb#.....",
    "...##bbbbbb##...",
    "..#bb#bbbb#bb#..",
    "..#b#.#####.#b#.",
    "..###..#b#..###.",
    ".......#b#......",
    ".......#b#......",
    "......##b##.....",
    "................",
]

PALETTES = {
    # outline, shadow, base, light
    "doll": [(78, 72, 62), (156, 148, 134), (214, 206, 192), (246, 242, 234)],
    "angry_doll": [(46, 20, 20), (120, 42, 42), (188, 78, 62), (240, 150, 110)],
    "creepy_doll": [(38, 24, 60), (84, 56, 128), (128, 92, 184), (196, 168, 236)],
}

CHARS = {"#": 0, "b": 1, "l": 2, "p": 3}


def main() -> None:
    TEX.mkdir(parents=True, exist_ok=True)
    for name, palette in PALETTES.items():
        image = Image.new("RGBA", (16, 16))
        pixels = image.load()
        for y, row in enumerate(DOLL):
            for x, key in enumerate(row):
                if key == ".":
                    continue
                pixels[x, y] = (*palette[CHARS[key]], 255)
        image.save(TEX / f"{name}.png")
        print(f"wrote {name}.png")


if __name__ == "__main__":
    main()
