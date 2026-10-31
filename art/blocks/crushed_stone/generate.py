#!/usr/bin/env python3
"""Crushed netherrack / end stone block textures (M6).

Sequentia's crushed blocks read as the vanilla material broken into coarse
chunks: the vanilla texture is averaged down to a 4x4 grid and scaled back up
with nearest-neighbour, so every 4x4 pixel cell is one flat colour. We keep the
vanilla source (read from the ModDevGradle client jar, nothing stored here),
apply that down/up-sampling, then slightly darken and add a thin seam between
chunks.

    <python-with-pillow> art/blocks/crushed_stone/generate.py
"""

from __future__ import annotations

import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ARTIFACTS = ROOT / "build/moddev/artifacts"
OUT = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/block"

# Cells per axis in the crushed grid, and how the grid is expressed.
CELLS = 4
SEAM = 0.88      # darker right/bottom edge of every cell
CHECKER = 0.93   # alternate chunks read slightly darker
NOISE = 6        # deterministic +/- per channel so the chunks are not flat


def read_vanilla(path: str) -> Image.Image:
    jars = sorted(ARTIFACTS.glob("*client-extra*jar"))
    if not jars:
        raise SystemExit(f"vanilla assets not found under {ARTIFACTS}; run ./gradlew build once")
    with zipfile.ZipFile(jars[0]) as jar:
        with jar.open(path) as handle:
            return Image.open(handle).convert("RGBA")


def crushed(source: Image.Image) -> Image.Image:
    """Crush the vanilla material into a 4x4 grid of chunks.

    Average the 16x16 texture down to 4x4 and scale it back up, so each chunk is
    one flat colour taken from the vanilla pixels; then add a thin seam and a
    little deterministic grain so the result is not a perfect checkerboard.
    """
    size = source.width // CELLS
    chunks = source.resize((size, size), Image.BOX).resize(
        (source.width, source.height), Image.NEAREST)

    pixels = chunks.load()
    for y in range(chunks.height):
        for x in range(chunks.width):
            red, green, blue, alpha = pixels[x, y]
            if alpha == 0:
                continue
            cell_x, cell_y = x // CELLS, y // CELLS
            scale = CHECKER if (cell_x + cell_y) % 2 == 0 else 1.0
            if x % CELLS == CELLS - 1 or y % CELLS == CELLS - 1:
                scale *= SEAM
            grain = ((x * 7 + y * 13) % (NOISE * 2 + 1)) - NOISE
            pixels[x, y] = (
                max(0, min(255, int(red * scale) + grain)),
                max(0, min(255, int(green * scale) + grain)),
                max(0, min(255, int(blue * scale) + grain)),
                255,
            )
    return chunks


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    pairs = {
        "crushed_netherrack": "assets/minecraft/textures/block/netherrack.png",
        "crushed_end_stone": "assets/minecraft/textures/block/end_stone.png",
    }
    preview = Image.new("RGBA", (16 * 2 + 4, 16), (24, 24, 24, 255))
    for index, (name, path) in enumerate(pairs.items()):
        image = crushed(read_vanilla(path))
        image.save(OUT / f"{name}.png")
        preview.paste(image, (index * 20, 0))
        print(f"wrote block/{name}.png")
    preview.resize((preview.width * 8, preview.height * 8), Image.NEAREST).save(
        Path(__file__).with_name("preview.png")
    )


if __name__ == "__main__":
    main()
