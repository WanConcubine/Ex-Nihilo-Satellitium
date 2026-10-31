#!/usr/bin/env python3
"""Crushed netherrack / end stone block textures (M6).

They are derived from the vanilla block textures (read out of the ModDevGradle
client jar, so no Mojang PNG is stored here): the material stays recognisable,
then get the "crushed" treatment — darkened, slightly desaturated and crossed by
a few short cracks.

    <python-with-pillow> art/blocks/crushed_stone/generate.py
"""

from __future__ import annotations

import colorsys
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ARTIFACTS = ROOT / "build/moddev/artifacts"
OUT = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/block"

# Deterministic crack pixels (x, y) — short diagonal gashes, 16x16.
CRACKS = {
    "crushed_netherrack": ((3, 4), (4, 5), (5, 6), (6, 6), (10, 3), (11, 4),
                           (6, 11), (7, 12), (8, 12), (12, 9), (13, 10)),
    "crushed_end_stone": ((2, 10), (3, 11), (4, 12), (9, 4), (10, 5), (10, 6),
                          (5, 5), (6, 6), (12, 12), (13, 13)),
}


def read_vanilla(path: str) -> Image.Image:
    jars = sorted(ARTIFACTS.glob("*client-extra*jar"))
    if not jars:
        raise SystemExit(f"vanilla assets not found under {ARTIFACTS}; run ./gradlew build once")
    with zipfile.ZipFile(jars[0]) as jar:
        with jar.open(path) as handle:
            return Image.open(handle).convert("RGBA")


def crushed(source: Image.Image, cracks: tuple[tuple[int, int], ...]) -> Image.Image:
    image = source.copy()
    pixels = image.load()
    for y in range(16):
        for x in range(16):
            red, green, blue, alpha = pixels[x, y]
            if alpha == 0:
                continue
            hue, saturation, value = colorsys.rgb_to_hsv(red / 255.0, green / 255.0, blue / 255.0)
            # Keep the material's hue, dim it and flatten the contrast a little.
            saturation *= 0.8
            value *= 0.78
            rgb = colorsys.hsv_to_rgb(hue, saturation, value)
            pixels[x, y] = (int(rgb[0] * 255), int(rgb[1] * 255), int(rgb[2] * 255), 255)

    # Cracks: pick the darkest variant of each crack pixel's own hue.
    for x, y in cracks:
        red, green, blue, _ = pixels[x, y]
        pixels[x, y] = (int(red * 0.45), int(green * 0.45), int(blue * 0.45), 255)
    return image


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    pairs = {
        "crushed_netherrack": "assets/minecraft/textures/block/netherrack.png",
        "crushed_end_stone": "assets/minecraft/textures/block/end_stone.png",
    }
    preview = Image.new("RGBA", (16 * 2 + 4, 16), (24, 24, 24, 255))
    for index, (name, path) in enumerate(pairs.items()):
        image = crushed(read_vanilla(path), CRACKS[name])
        image.save(OUT / f"{name}.png")
        preview.paste(image, (index * 20, 0))
        print(f"wrote block/{name}.png")
    preview.resize((preview.width * 8, preview.height * 8), Image.NEAREST).save(
        Path(__file__).with_name("preview.png")
    )


if __name__ == "__main__":
    main()
