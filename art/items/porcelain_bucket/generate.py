#!/usr/bin/env python3
"""Porcelain bucket icon (M6): vanilla bucket shape in porcelain white, plus a
white fluid mask that the game tints with the stored fluid colour.

Reads the vanilla water bucket texture from the ModDevGradle client jar, so no
Mojang PNG is stored in the repository (same approach as witch_water_bucket).

    <python-with-pillow> art/items/porcelain_bucket/generate.py
"""

from __future__ import annotations

import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
ARTIFACTS = ROOT / "build/moddev/artifacts"
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

# Luminance -> fired porcelain ramp. The vanilla bucket silhouette is kept as-is.
PORCELAIN = {
    0: (110, 106, 97),    # outline
    1: (168, 162, 150),   # shadow
    2: (201, 195, 184),   # base
    3: (221, 216, 207),   # light
    4: (242, 239, 233),   # highlight
}
# Unfired clay ramp, same silhouette.
CLAY = {
    0: (94, 79, 63),
    1: (150, 128, 106),
    2: (201, 174, 146),
    3: (222, 201, 174),
    4: (239, 224, 201),
}
# Vanilla bucket greys, ordered by brightness.
STEPS = [53, 114, 150, 168, 216]


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


def porcelain_index(value: int) -> int:
    for index, step in enumerate(STEPS):
        if value <= step + 8:
            return index
    return len(STEPS) - 1


def main() -> None:
    # The empty bucket keeps the rim/interior drawn; the water bucket only
    # contributes the fluid silhouette. Using the water bucket for the body would
    # punch a hole through the opening (the "hollow top" bug).
    body_source = read_vanilla("assets/minecraft/textures/item/bucket.png")
    water_source = read_vanilla("assets/minecraft/textures/item/water_bucket.png")

    body = Image.new("RGBA", body_source.size)
    unfired = Image.new("RGBA", body_source.size)
    mask = Image.new("RGBA", body_source.size)
    body_px = body.load()
    unfired_px = unfired.load()
    mask_px = mask.load()

    for y in range(body_source.height):
        for x in range(body_source.width):
            pixel = body_source.getpixel((x, y))
            if pixel[3] == 0:
                body_px[x, y] = (0, 0, 0, 0)
                unfired_px[x, y] = (0, 0, 0, 0)
            else:
                red, green, blue, _ = pixel
                level = (red + green + blue) // 3
                index = porcelain_index(level)
                body_px[x, y] = (*PORCELAIN[index], 255)
                unfired_px[x, y] = (*CLAY[index], 255)

            water = water_source.getpixel((x, y))
            if water[3] > 0 and is_water(water):
                mask_px[x, y] = (255, 255, 255, 255)
            else:
                mask_px[x, y] = (0, 0, 0, 0)

    TEX.mkdir(parents=True, exist_ok=True)
    body.save(TEX / "porcelain_bucket.png")
    unfired.save(TEX / "unfired_porcelain_bucket.png")
    mask.save(TEX / "porcelain_bucket_fluid.png")

    preview = Image.new("RGBA", (60, 16), (24, 24, 24, 255))
    preview.paste(unfired, (0, 0))
    preview.paste(body, (20, 0))
    tinted = mask.copy()
    tint_px = tinted.load()
    for y in range(tinted.height):
        for x in range(tinted.width):
            if tint_px[x, y][3] > 0:
                tint_px[x, y] = (107, 68, 168, 255)  # witch water purple for the preview
    preview.alpha_composite(tinted, (20, 0))
    preview.paste(mask, (44, 0))
    preview.resize((preview.width * 8, preview.height * 8), Image.NEAREST).save(
        Path(__file__).with_name("preview.png")
    )
    print("wrote unfired_porcelain_bucket.png / porcelain_bucket.png / porcelain_bucket_fluid.png")


if __name__ == "__main__":
    main()
