#!/usr/bin/env python3
"""Restyle Ex Nihilo: Creatio's End Cake textures for 1.21.

Creatio's 1.12.2 textures are MIT (see THIRD-PARTY.md).  The originals are kept
read-only in ``creatio/``.  This script:

* crops the transparent padding (14x14 top, 14x8 side/inner),
* scales the used region to the full 16x16 face the vanilla cake model expects,
* applies a small saturation/contrast lift for the 1.21 look,
* adds a 1px dark border so seams don't read as holes.

Run with Pillow:
    python3 art/blocks/end_cake/restyle.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance

HERE = Path(__file__).resolve().parent
SRC = HERE / "creatio"
OUT = HERE.parents[2] / "src/main/resources/assets/exnihilosatellitium/textures/block"
PREVIEW = HERE / "preview.png"
OUTLINE = (12, 18, 14, 255)


def face(name: str, box: tuple[int, int, int, int]) -> Image.Image:
    image = Image.open(SRC / f"end_cake_{name}.png").convert("RGBA")
    image = image.crop(box).resize((16, 16), Image.Resampling.NEAREST)
    # Keep Creatio's eye/detail; just lift the flat 1.12 palette a little.
    image = ImageEnhance.Color(image).enhance(1.15)
    image = ImageEnhance.Contrast(image).enhance(1.08)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 15, 15), outline=OUTLINE)
    return image


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    sheets = {
        "top": face("top", (1, 1, 15, 15)),
        "side": face("side", (1, 8, 15, 16)),
        "bottom": face("bottom", (0, 0, 16, 16)),
        "inner": face("inner", (1, 8, 15, 16)),
    }
    for name, image in sheets.items():
        image.save(OUT / f"end_cake_{name}.png")

    preview = Image.new("RGBA", (64 * 2 + 8, 64 * 2 + 8), (0, 0, 0, 0))
    for index, (_, image) in enumerate(sheets.items()):
        preview.alpha_composite(image.resize((64, 64), Image.Resampling.NEAREST),
                                ((index % 2) * 68, (index // 2) * 72))
    preview.save(PREVIEW)
    print(f"restyled 4 textures from {SRC}")


if __name__ == "__main__":
    main()
