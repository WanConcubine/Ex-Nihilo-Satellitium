#!/usr/bin/env python3
"""Sugar water / concentrated honey bucket icons.

Same trick as the witch water bucket: read the vanilla water bucket from the
client-extra jar, recolour only the blue water pixels along a per-fluid ramp,
and leave the metal bucket untouched. No Mojang asset is stored in the repo.

    <python-with-pillow> art/items/barrel_fluid_buckets/generate.py
"""

from __future__ import annotations

import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ARTIFACTS = ROOT / "build/moddev/artifacts"
OUT_DIR = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

# Per-fluid ramp (dark -> light); vanilla water pixels pick an entry by their
# brightness, so the original shading structure is preserved.
RAMPS = {
    "sugar_water_bucket": [
        (146, 106, 46),
        (186, 146, 76),
        (222, 186, 116),
        (248, 226, 168),
    ],
    "concentrated_honey_bucket": [
        (110, 62, 8),
        (166, 101, 22),
        (208, 143, 44),
        (240, 184, 78),
    ],
}


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


def recolour(bucket: Image.Image, ramp: list[tuple[int, int, int]]) -> Image.Image:
    out = []
    for pixel in bucket.getdata():
        if not is_water(pixel):
            out.append(pixel)
            continue
        red, green, blue, _ = pixel
        level = (red + green + blue) / 3.0
        t = max(0.0, min(1.0, (level - 100.0) / 60.0)) * (len(ramp) - 1)
        low = int(t)
        high = min(low + 1, len(ramp) - 1)
        frac = t - low
        colour = tuple(round(ramp[low][i] + (ramp[high][i] - ramp[low][i]) * frac) for i in range(3))
        out.append((*colour, 255))
    result = Image.new("RGBA", bucket.size)
    result.putdata(out)
    return result


def main() -> None:
    bucket = read_vanilla("assets/minecraft/textures/item/water_bucket.png")
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, ramp in RAMPS.items():
        result = recolour(bucket, ramp)
        result.save(OUT_DIR / f"{name}.png")
        result.resize((256, 256), Image.NEAREST).save(
            Path(__file__).with_name(f"{name}_preview.png"))
        print(f"wrote {name}.png")


if __name__ == "__main__":
    main()
