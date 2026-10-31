#!/usr/bin/env python3
"""Crushed netherrack / end stone block textures (M6).

The vanilla texture is kept as-is and a fine 2x2 checker of brightness is
imprinted on top, plus a little deterministic grain. That reads as a crushed
mosaic without the long seams that made the earlier 4x4 version look like
planks. Source: vanilla block/netherrack and block/end_stone, read out of the
ModDevGradle client jar (nothing stored in the repo).

    <python-with-pillow> art/blocks/crushed_stone/generate.py
"""

from __future__ import annotations

import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ARTIFACTS = ROOT / "build/moddev/artifacts"
OUT = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/block"

# 2x2 checker cells: tight enough to read as crushed stone, not as planks.
CELLS = 2
CHECKER = 0.9    # every other 2x2 cell is 10% darker
NOISE = 6        # deterministic +/- per channel so the cells are not flat


def read_vanilla(path: str) -> Image.Image:
    jars = sorted(ARTIFACTS.glob("*client-extra*jar"))
    if not jars:
        raise SystemExit(f"vanilla assets not found under {ARTIFACTS}; run ./gradlew build once")
    with zipfile.ZipFile(jars[0]) as jar:
        with jar.open(path) as handle:
            return Image.open(handle).convert("RGBA")


def crushed(source: Image.Image) -> Image.Image:
    """Keep the vanilla pixels, imprint a fine 2x2 checker and a little grain."""
    image = source.copy()
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            red, green, blue, alpha = pixels[x, y]
            if alpha == 0:
                continue
            scale = CHECKER if ((x // CELLS) + (y // CELLS)) % 2 == 0 else 1.0
            grain = ((x * 7 + y * 13) % (NOISE * 2 + 1)) - NOISE
            pixels[x, y] = (
                max(0, min(255, int(red * scale) + grain)),
                max(0, min(255, int(green * scale) + grain)),
                max(0, min(255, int(blue * scale) + grain)),
                255,
            )
    return image


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
