#!/usr/bin/env python3
"""Icon textures for the extra sieve resources (M6):

  grass_seeds.png          - green seeds that turn dirt into grass
  crushed_netherrack.png   - hammered netherrack, sieved for nether loot
  crushed_end_stone.png    - hammered end stone, sieved for end loot

    <python-with-pillow> art/items/sieve_extras/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

# Per sprite: 16 rows of 16 chars, plus a 4-step palette (outline, shadow, base, light).
SPRITES = {
    "grass_seeds": (
        ["................",
         "................",
         "....##..........",
         "...##b#.........",
         "...#bb##....##..",
         "....###....##b#.",
         "............#b#.",
         "..##............",
         ".#bb#.....##....",
         ".#bb#....#bb#...",
         "..##.....#bb#...",
         "..........##....",
         "................",
         ".....####.......",
         "....#bllb#......",
         ".....####......."],
        [(47, 74, 34), (78, 122, 53), (127, 168, 78), (181, 209, 131)],
    ),
    "crushed_netherrack": (
        ["................",
         "................",
         "....####........",
         "...##b##....##..",
         "..##bbbb#..##b#.",
         "..#bllbb#..#bb#.",
         "..##bbbb#..##b#.",
         "...##b##....##..",
         "......##........",
         ".....####.......",
         "....##bb##......",
         "...#bbllb#......",
         "...#bbbbb#......",
         "....#####.......",
         "................",
         "................"],
        [(58, 20, 20), (92, 31, 31), (126, 43, 43), (163, 68, 68)],
    ),
    "crushed_end_stone": (
        ["................",
         "................",
         "....####........",
         "...##bb##...##..",
         "..#bbllbb#..#b#.",
         "..#bllllb#..##..",
         "..#bbbbbb#......",
         "...######.......",
         ".........##.....",
         ".....##..##b#...",
         "....#bb#.#bb#...",
         "...#bllb#.##....",
         "...#bbbb#.......",
         "....####........",
         "................",
         "................"],
        [(120, 116, 90), (168, 164, 132), (214, 210, 168), (240, 237, 218)],
    ),
}

CHARS = {"#": 0, "b": 1, "l": 2, "p": 3}


def main() -> None:
    TEX.mkdir(parents=True, exist_ok=True)
    for name, (rows, palette) in SPRITES.items():
        image = Image.new("RGBA", (16, 16))
        pixels = image.load()
        for y, row in enumerate(rows):
            for x, key in enumerate(row):
                if key == ".":
                    continue
                pixels[x, y] = (*palette[CHARS[key]], 255)
        image.save(TEX / f"{name}.png")
        print(f"wrote {name}.png")


if __name__ == "__main__":
    main()
