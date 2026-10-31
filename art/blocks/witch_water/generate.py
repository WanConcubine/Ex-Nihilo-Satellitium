#!/usr/bin/env python3
"""Witch water textures (M6): still, flowing, and the bucket item.

Reproducible pixel art per docs/textures.md: 16x16, 4-6 shades, no anti-aliasing.
Run with a Pillow-enabled Python:

    <python-with-pillow> art/blocks/witch_water/generate.py
"""

from __future__ import annotations

import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures"

# 5-step purple palette: outline, shadow, base, light, highlight.
PALETTE = [
    (46, 27, 71),     # #2E1B47
    (74, 44, 107),    # #4A2C6B
    (108, 74, 156),   # #6C4A9C
    (142, 104, 196),  # #8E68C4
    (179, 149, 224),  # #B395E0
]


def still_texture() -> Image.Image:
    """Soft swirl: two crossing sine waves pick the shade."""
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y in range(16):
        for x in range(16):
            wave = math.sin((x + y * 0.55) * 0.85) + math.sin((x * 0.7 - y) * 0.6)
            # -2..2 -> 1..4, biased towards the base colour.
            index = int(round(2.2 + wave * 0.85))
            index = max(1, min(4, index))
            pixels[x, y] = (*PALETTE[index], 255)
    # A couple of fixed sparkles so the surface is not perfectly uniform.
    for x, y in ((3, 4), (10, 11), (12, 3)):
        pixels[x, y] = (*PALETTE[4], 255)
    return image


def flow_texture() -> Image.Image:
    """Diagonal streaks for the flowing variant."""
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y in range(16):
        for x in range(16):
            band = (x - y * 2) % 8
            index = 1 if band < 2 else (2 if band < 5 else 3)
            if (x + y) % 9 == 0:
                index = 4
            pixels[x, y] = (*PALETTE[index], 255)
    return image


# Bucket: '#' outline, 'i' iron, 'j' iron shadow, 'w' witch water, 'W' water highlight.
BUCKET = [
    "................",
    "....########....",
    "...#........#...",
    "..#..........#..",
    "..#jjjjjjjjjj#..",
    "..#iiiiiiiiii#..",
    "..#iwwwwwwwwi#..",
    "...#iwwwwwwi#...",
    "...#iwwWwwwi#...",
    "....#iwwwwi#....",
    "....#iwwwwi#....",
    ".....#iwwi#.....",
    ".....#iwwi#.....",
    "......####......",
    "................",
    "................",
]

BUCKET_COLORS = {
    "#": (63, 63, 63, 255),
    "i": (198, 198, 198, 255),
    "j": (138, 138, 138, 255),
    "w": (*PALETTE[2], 255),
    "W": (*PALETTE[4], 255),
    ".": (0, 0, 0, 0),
}


def bucket_texture() -> Image.Image:
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y, row in enumerate(BUCKET):
        for x, key in enumerate(row):
            pixels[x, y] = BUCKET_COLORS[key]
    return image


def main() -> None:
    (TEX / "block").mkdir(parents=True, exist_ok=True)
    (TEX / "item").mkdir(parents=True, exist_ok=True)

    still = still_texture()
    flow = flow_texture()
    bucket = bucket_texture()

    still.save(TEX / "block/witch_water_still.png")
    flow.save(TEX / "block/witch_water_flow.png")
    bucket.save(TEX / "item/witch_water_bucket.png")

    preview = Image.new("RGBA", (16 * 3 + 8, 16), (24, 24, 24, 255))
    preview.paste(still, (0, 0))
    preview.paste(flow, (20, 0))
    preview.paste(bucket, (40, 0))
    preview.resize((preview.width * 8, preview.height * 8), Image.NEAREST).save(
        Path(__file__).with_name("preview.png")
    )
    print("wrote witch_water_still / witch_water_flow / witch_water_bucket")


if __name__ == "__main__":
    main()
