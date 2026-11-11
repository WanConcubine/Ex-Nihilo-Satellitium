#!/usr/bin/env python3
"""Generate small modifier overlays for the Tinkers' crushing hammer.

TiC renders a modifier only if the tool model's modifier map points at a
sprite. The overlays are anchored to the head of our 16x16 hammer icon (head
box x 7-15, y 0-8), so they read as rivets, gems, sparks and flames on the
head instead of the sledge-hammer layout.

Each entry is a list of ``(x, y, #RRGGBB)`` pixels on an otherwise transparent
16x16 canvas. Keep the art small and hard-edged; it is drawn at item scale.

Usage:
    python3 art/items/tools/crushing_hammer_overlays.py
    python3 art/items/tools/crushing_hammer_overlays.py --out /tmp/overlays
"""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TARGET = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item/ex_nihilo_hammer/modifiers"

#: name -> [(x, y, colour), ...]
OVERLAYS: dict[str, list[tuple[int, int, str]]] = {
    # speed lines at the top-left of the head
    "haste": [
        (7, 2, "#7FE7FF"), (8, 2, "#A9F1FF"), (7, 3, "#7FE7FF"),
        (8, 4, "#7FE7FF"), (9, 5, "#A9F1FF"), (12, 1, "#7FE7FF"),
    ],
    # warm glow along the top edge
    "glowing": [
        (9, 0, "#FFF2A8"), (10, 0, "#FFF2A8"), (8, 1, "#FFE873"),
        (11, 1, "#FFF2A8"), (12, 2, "#FFE873"), (13, 3, "#FFE873"),
        (14, 4, "#FFE873"),
    ],
    # purple rivet in the head centre
    "reinforced": [
        (10, 3, "#2A163F"), (11, 3, "#4B2A7A"), (12, 3, "#2A163F"),
        (10, 4, "#4B2A7A"), (11, 4, "#8E63C9"), (12, 4, "#4B2A7A"),
        (10, 5, "#2A163F"), (11, 5, "#4B2A7A"), (12, 5, "#2A163F"),
    ],
    # turquoise gem
    "diamond": [
        (11, 3, "#B8FFFF"), (10, 4, "#59E0E0"), (11, 4, "#B8FFFF"),
        (12, 4, "#59E0E0"), (11, 5, "#2E8B8B"),
    ],
    # emerald gem
    "emerald": [
        (11, 3, "#B8FFC8"), (10, 4, "#4CD964"), (11, 4, "#B8FFC8"),
        (12, 4, "#4CD964"), (11, 5, "#237A33"),
    ],
    # dark trim on the lower-right edge
    "netherite": [
        (13, 4, "#2B2B2B"), (14, 5, "#2B2B2B"), (15, 5, "#4A4A4A"),
        (12, 6, "#2B2B2B"), (13, 6, "#4A4A4A"), (14, 7, "#2B2B2B"),
        (12, 8, "#2B2B2B"), (13, 8, "#4A4A4A"),
    ],
    # flames under the head
    "fiery": [
        (10, 6, "#FF7A1A"), (11, 6, "#FFD24A"), (12, 6, "#FF7A1A"),
        (11, 7, "#FFB02E"), (12, 7, "#FF7A1A"), (13, 7, "#FFD24A"),
        (12, 8, "#FF7A1A"),
    ],
    # pale cross
    "smite": [
        (10, 2, "#FFF7C2"), (9, 3, "#FFE873"), (10, 3, "#FFFFFF"),
        (11, 3, "#FFE873"), (10, 4, "#FFF7C2"),
    ],
    # orange sparks on the striking face
    "blasting": [
        (12, 1, "#FFB02E"), (11, 2, "#FF7A1A"), (13, 2, "#FF7A1A"),
        (12, 2, "#FFE873"), (11, 3, "#FFB02E"), (13, 3, "#FFB02E"),
    ],
    # green clover
    "luck": [
        (9, 4, "#2F9E44"), (10, 3, "#4CD964"), (11, 4, "#2F9E44"),
        (10, 5, "#4CD964"), (10, 4, "#B8FFC8"),
    ],
    # white thread along the top
    "silky": [
        (9, 1, "#FFFFFF"), (10, 1, "#D8F0FF"), (11, 2, "#FFFFFF"),
        (12, 2, "#D8F0FF"), (13, 3, "#FFFFFF"),
    ],
    # xp orbs
    "experienced": [
        (8, 5, "#C8FF6B"), (9, 5, "#6BBF1A"), (12, 6, "#C8FF6B"),
        (13, 6, "#6BBF1A"),
    ],
    # violet edge glow
    "unbreakable": [
        (7, 2, "#8E63C9"), (8, 1, "#B991E8"), (14, 5, "#8E63C9"),
        (13, 6, "#B991E8"), (15, 4, "#B991E8"),
    ],
    # red slash
    "severing": [
        (12, 3, "#C92A2A"), (11, 4, "#FF6B6B"), (10, 5, "#C92A2A"),
        (13, 2, "#FF6B6B"),
    ],
    # red/blue magnet
    "magnetic": [
        (12, 3, "#FF6B6B"), (13, 3, "#FF6B6B"),
        (12, 4, "#4DABF7"), (13, 4, "#4DABF7"),
        (12, 5, "#2B8A3E"), (13, 5, "#2B8A3E"),
    ],
    # two chain links across the head (the exnihilosatellitium:chain upgrade)
    "chain": [
        (8, 1, "#C9D6E3"), (9, 1, "#8FA3B8"), (10, 1, "#C9D6E3"),
        (8, 2, "#8FA3B8"), (10, 2, "#8FA3B8"),
        (8, 3, "#C9D6E3"), (9, 3, "#8FA3B8"), (10, 3, "#C9D6E3"),
        (11, 3, "#FFC94A"), (12, 3, "#FFE08A"), (13, 3, "#FFC94A"),
        (11, 4, "#FFE08A"), (13, 4, "#FFE08A"),
        (11, 5, "#FFC94A"), (12, 5, "#FFE08A"), (13, 5, "#FFC94A"),
    ],
    # dark green mark
    "necrotic": [
        (10, 4, "#2B8A3E"), (9, 5, "#1E5E2A"), (11, 5, "#1E5E2A"),
        (10, 6, "#2B8A3E"),
    ],
}


def build(pixels: list[tuple[int, int, str]]) -> Image.Image:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x, y, colour in pixels:
        rgb = tuple(int(colour[i:i + 2], 16) for i in (1, 3, 5))
        image.putpixel((x, y), (*rgb, 255))
    return image


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path, default=TARGET)
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)
    for name, pixels in OVERLAYS.items():
        build(pixels).save(args.out / f"{name}.png")
    print(f"wrote {len(OVERLAYS)} overlays to {args.out}")


if __name__ == "__main__":
    main()
