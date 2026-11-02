#!/usr/bin/env python3
"""Icon textures for the extra sieve resources:

  grass_seeds.png           - green seeds that turn dirt into grass
  crimson_nylium_spores.png - red spores that turn netherrack into crimson nylium
  warped_nylium_spores.png  - teal spores that turn netherrack into warped nylium

    <python-with-pillow> art/items/sieve_extras/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ASSETS = ROOT / "src/main/resources/assets/exnihilosatellitium/textures"

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
    "crimson_nylium_spores": (
        ["................",
         "..........#.....",
         ".........#l#....",
         "....#....#l#....",
         "...#l#....b.....",
         "....b...........",
         "................",
         "......####......",
         ".....#lppl#.....",
         "....#lppppl#....",
         "....#lppppl#....",
         ".....#lppl#.....",
         "......####......",
         "..b...b..b...b..",
         "................",
         "................"],
        [(58, 18, 20), (105, 30, 32), (158, 53, 47), (208, 96, 80)],
    ),
    "warped_nylium_spores": (
        ["................",
         "....#...........",
         "...#l#..........",
         "....b.....#.....",
         ".........#l#....",
         "..........b.....",
         "................",
         "......####......",
         ".....#lppl#.....",
         "....#lppppl#....",
         "....#lppppl#....",
         ".....#lppl#.....",
         "......####......",
         "..b...b..b...b..",
         "................",
         "................"],
        [(17, 42, 48), (27, 82, 88), (43, 127, 124), (86, 173, 158)],
    ),
}

CHARS = {"#": 0, "b": 1, "l": 2, "p": 3}
BLOCK_TEXTURES: set[str] = set()


def main() -> None:
    for name, (rows, palette) in SPRITES.items():
        assert len(rows) == 16 and all(len(row) == 16 for row in rows), name
        folder = "block" if name in BLOCK_TEXTURES else "item"
        TEX = ASSETS / folder
        TEX.mkdir(parents=True, exist_ok=True)
        image = Image.new("RGBA", (16, 16))
        pixels = image.load()
        for y, row in enumerate(rows):
            for x, key in enumerate(row):
                if key == ".":
                    continue
                pixels[x, y] = (*palette[CHARS[key]], 255)
        image.save(TEX / f"{name}.png")
        print(f"wrote {folder}/{name}.png")


if __name__ == "__main__":
    main()
