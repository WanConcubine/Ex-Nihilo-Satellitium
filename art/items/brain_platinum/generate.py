#!/usr/bin/env python3
"""Brain Platinum item icon (16x16): a blue gift box with a gold ribbon.

Own pixel art for the right-click villager item; no reference textures are
copied. The map is explicit so an edit is a one-line change.

    <python-with-pillow> art/items/brain_platinum/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

# outline, box shadow, box, box light, ribbon shadow, ribbon
PALETTE = {
    "O": (24, 34, 66),
    "b": (37, 68, 136),
    "B": (56, 98, 186),
    "L": (92, 140, 224),
    "y": (176, 128, 28),
    "Y": (238, 192, 64),
}

MAP = [
    "................",
    "................",
    ".....OO..OO.....",
    "....OYYOOYYO....",
    "...OOYYYYYYOO...",
    "...OBBBYYBBBO...",
    "...OBBLYYBBBO...",
    "...OBBLYYBBBO...",
    "...OYYYYYYYYO...",
    "...OyyyyyyyyO...",
    "...OBBLYYBBBO...",
    "...OBBLYYBBBO...",
    "...OBBLYYBBBO...",
    "...OBBLYYBBBO...",
    "...OOOOOOOOOO...",
    "....OOOOOOOO....",
]


def main() -> None:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pixels = image.load()
    assert len(MAP) == 16 and all(len(row) == 16 for row in MAP)
    for y, row in enumerate(MAP):
        for x, char in enumerate(row):
            if char == ".":
                continue
            pixels[x, y] = (*PALETTE[char], 255)
    TEX.mkdir(parents=True, exist_ok=True)
    image.save(TEX / "brain_platinum.png")
    print("wrote brain_platinum.png")


if __name__ == "__main__":
    main()
