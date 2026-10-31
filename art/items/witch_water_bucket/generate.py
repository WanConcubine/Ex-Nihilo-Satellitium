#!/usr/bin/env python3
"""Witch water bucket icon: the vanilla water bucket with the water recoloured.

The metal part is kept exactly as vanilla; only the blue water pixels are
hue-rotated to purple, so the icon reads as "the bucket you already know, filled
with witch water". Nothing is drawn by hand.

The vanilla texture is read from the NeoForge client-extra jar that ModDevGradle
already downloads, so no Mojang asset is stored in the repository.

    <python-with-pillow> art/items/witch_water_bucket/generate.py
"""

from __future__ import annotations

import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ARTIFACTS = ROOT / "build/moddev/artifacts"
OUT = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item/witch_water_bucket.png"

# Witch water ramp (dark -> light); the vanilla water pixels pick an entry by
# their own brightness so the bucket keeps the original shading structure.
RAMP = [
    (74, 44, 120),    # #4A2C78
    (107, 68, 168),   # #6B44A8
    (142, 99, 201),   # #8E63C9
    (176, 143, 224),  # #B08FE0
]


def read_vanilla(path: str) -> Image.Image:
    jars = sorted(ARTIFACTS.glob("*client-extra*jar"))
    if not jars:
        raise SystemExit(f"vanilla assets not found under {ARTIFACTS}; run ./gradlew build once")
    with zipfile.ZipFile(jars[0]) as jar:
        with jar.open(path) as handle:
            return Image.open(handle).convert("RGBA")


def is_water(pixel: tuple[int, int, int, int]) -> bool:
    red, green, blue, _ = pixel
    return blue > red + 20 and blue > green + 20


def main() -> None:
    bucket = read_vanilla("assets/minecraft/textures/item/water_bucket.png")
    pixels = list(bucket.getdata())

    out = []
    for pixel in pixels:
        if not is_water(pixel):
            out.append(pixel)
            continue
        red, green, blue, _ = pixel
        # Vanilla water pixels run from dark (35,79,204) to light (90,130,243).
        level = (red + green + blue) / 3.0
        t = max(0.0, min(1.0, (level - 100.0) / 60.0)) * (len(RAMP) - 1)
        low = int(t)
        high = min(low + 1, len(RAMP) - 1)
        frac = t - low
        colour = tuple(
            round(RAMP[low][i] + (RAMP[high][i] - RAMP[low][i]) * frac) for i in range(3)
        )
        out.append((*colour, 255))

    result = Image.new("RGBA", bucket.size)
    result.putdata(out)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    result.save(OUT)
    result.resize((256, 256), Image.NEAREST).save(Path(__file__).with_name("preview.png"))
    print(f"wrote {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
