#!/usr/bin/env python3
"""Adopt the Ex Nihilo: Creatio (MIT) crucible model + textures for 1.21.

Creatio ships a 13-element cauldron model and two 16x16 block textures. This
script keeps the geometry (fixing the cullfaces that point into the cavity,
which would cull the floor/legs when a block sits above) and quantises the
noisy textures to a small modern palette.

Pristine sources: art/blocks/crucible/creatio/. Do not edit those.

Usage:
    python3 art/blocks/crucible/restyle.py
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[3]
BASE = ROOT / "art/blocks/crucible/creatio"
ASSETS = ROOT / "src/main/resources/assets/exnihilosatellitium"
MODEL_OUT = ASSETS / "models/block/crucible_base.json"

# Light -> dark. The fired crucible is warm porcelain; the unfired one is clay.
FIRED = [(0xE7, 0xE3, 0xD7), (0xDF, 0xDA, 0xCD), (0xD6, 0xD0, 0xC3),
         (0xCB, 0xC5, 0xB7), (0xBF, 0xB9, 0xAB)]
UNFIRED = [(0xDE, 0xD7, 0xC8), (0xD4, 0xCC, 0xBC), (0xC9, 0xC1, 0xB0),
           (0xBD, 0xB4, 0xA2), (0xB0, 0xA7, 0x94)]

AXIS = {"down": (1, "from"), "up": (1, "to"), "north": (2, "from"),
        "south": (2, "to"), "west": (0, "from"), "east": (0, "to")}


def quantise(src: Image.Image, palette) -> Image.Image:
    """Map each pixel to the nearest palette entry by luminance percentile."""
    # A light blur turns the 1.12 high-frequency speckles into soft patches,
    # which is closer to modern vanilla surfaces.
    rgb = src.convert("RGB").filter(ImageFilter.GaussianBlur(0.8))
    pixels = list(rgb.getdata())
    luma = sorted(0.2126 * r + 0.7152 * g + 0.0722 * b for r, g, b in pixels)
    cuts = [luma[int(len(luma) * w)] for w in (0.2, 0.4, 0.6, 0.8)]
    out = Image.new("RGB", (16, 16))
    for y in range(16):
        for x in range(16):
            r, g, b = rgb.getpixel((x, y))
            value = 0.2126 * r + 0.7152 * g + 0.0722 * b
            index = sum(1 for cut in cuts if value >= cut)
            out.putpixel((x, y), palette[index])
    return out


def build_model() -> dict:
    model = json.loads((BASE / "block_crucible_big.json").read_text(encoding="utf-8"))
    for element in model["elements"]:
        for face, spec in element["faces"].items():
            if "cullface" not in spec:
                continue
            axis, side = AXIS[face]
            if element[side][axis] not in (0, 16):
                # Inset face: culling it would remove the floor/legs whenever a
                # block sits above the crucible.
                del spec["cullface"]
    return model


def main() -> None:
    fired = quantise(Image.open(BASE / "blockcrucible.png"), FIRED)
    unfired = quantise(Image.open(BASE / "blockcrucibleunfired.png"), UNFIRED)
    for image, relative in ((fired, "textures/block/crucible.png"),
                            (unfired, "textures/block/unfired_crucible.png")):
        target = ASSETS / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        image.save(target)
        print(f"wrote {target.relative_to(ROOT)}")

    model = build_model()
    MODEL_OUT.parent.mkdir(parents=True, exist_ok=True)
    MODEL_OUT.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {MODEL_OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
