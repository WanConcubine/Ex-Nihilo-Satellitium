#!/usr/bin/env python3
"""Split the shared hammer icon into Tinkers' Construct part sprites.

The TiC tool model tints every part by the material of the corresponding tool
part, so the sprites have to be *grayscale*: TiC's ``recolor_sprite`` /
``grey_to_color`` transformer maps each brightness bucket (000, 063, 102, ...)
to a colour from the material's palette.

This script keeps the iron hammer silhouette and writes two 16x16 sprites:

* ``head.png``  -> the metal head, mapped onto the metal palette buckets,
* ``handle.png`` -> the wooden handle, mapped onto TiC's wood palette buckets.

The resulting composite is the same hammer shape as before, but TiC can now
recolour the head and handle separately from the actual tool materials. No
TiC pixels are copied.

Usage:
    python3 art/items/tools/crushing_hammer_parts.py
    python3 art/items/tools/crushing_hammer_parts.py --out /tmp/parts
"""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
SOURCE = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item/iron_hammer.png"
TARGET = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item/ex_nihilo_hammer"

#: iron head grey -> metal palette bucket
HEAD = {
    (0x18, 0x18, 0x18): 0,
    (0x44, 0x44, 0x44): 63,
    (0xA0, 0xA0, 0xA0): 140,
    (0xC1, 0xC1, 0xC1): 178,
    (0xD8, 0xD8, 0xD8): 216,
    (0xFF, 0xFF, 0xFF): 255,
}

#: iron handle brown -> wood palette bucket (the source browns are exactly
#: TiC's wood palette values 102 / 178 / 255, so the handle keeps its shading)
HANDLE = {
    (0x28, 0x1E, 0x0B): 0,
    (0x49, 0x36, 0x15): 102,
    (0x68, 0x4E, 0x1E): 178,
    (0x89, 0x67, 0x27): 255,
}


def split() -> tuple[Image.Image, Image.Image]:
    source = Image.open(SOURCE).convert("RGBA")
    head = Image.new("RGBA", source.size, (0, 0, 0, 0))
    handle = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.load()
    dst_head = head.load()
    dst_handle = handle.load()
    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            key = (r, g, b)
            if key in HEAD:
                value = HEAD[key]
                dst_head[x, y] = (value, value, value, a)
            elif key in HANDLE:
                value = HANDLE[key]
                dst_handle[x, y] = (value, value, value, a)
    return head, handle


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path, default=TARGET)
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)
    head, handle = split()
    head.save(args.out / "head.png")
    handle.save(args.out / "handle.png")
    print(f"wrote {args.out / 'head.png'}")
    print(f"wrote {args.out / 'handle.png'}")


if __name__ == "__main__":
    main()
