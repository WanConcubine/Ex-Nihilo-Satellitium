#!/usr/bin/env python3
"""Restyle Creatio's doll icon for this project (MIT, see THIRD-PARTY.md).

Source: `art/items/dolls/creatio/doll.png` (128x128). The doll icons ship at
**64x64** (the author asked for the clearer variant), so the source is downscaled
to 64 with Lanczos, quantised to a 4-step palette by luminance and given a 1 px
outline. `scripts/check-textures.py` allows 64x64 for `textures/item/` folders.
`doll`, `angry_doll` and `creepy_doll` share the silhouette and only differ in
palette.

    <python-with-pillow> art/items/dolls/restyle.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
SRC = Path(__file__).resolve().parent / "creatio" / "doll.png"
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"
SIZE = 64

# outline, shadow, base, light
PALETTES = {
    "doll": [(74, 86, 100), (140, 158, 175), (198, 212, 226), (236, 244, 250)],
    "angry_doll": [(64, 26, 24), (146, 52, 44), (204, 96, 72), (246, 168, 118)],
    "creepy_doll": [(46, 30, 72), (104, 74, 150), (158, 122, 208), (214, 190, 244)],
    "shulker_doll": [(70, 40, 80), (150, 92, 150), (202, 142, 196), (242, 208, 236)],
    "guardian_doll": [(26, 74, 74), (62, 138, 136), (127, 198, 192), (201, 239, 232)],
    # Thermal mobs: blitz is a storm/lightning elemental (gold), blizz is ice (pale blue).
    "blitz_doll": [(66, 48, 10), (150, 116, 30), (214, 176, 66), (252, 232, 148)],
    "blizz_doll": [(26, 52, 82), (74, 128, 178), (146, 204, 236), (222, 246, 255)],
    # Villager doll: emerald ring, zombie-villager green silhouette.
    "villager_doll": [(22, 60, 34), (54, 128, 74), (104, 190, 122), (178, 238, 186)],
}


def main() -> None:
    source = Image.open(SRC).convert("RGBA")
    sprite = source.resize((SIZE, SIZE), Image.LANCZOS)
    pixels = sprite.load()

    # Silhouette mask and per-pixel luminance (0..1).
    solid: set[tuple[int, int]] = set()
    level: dict[tuple[int, int], float] = {}
    for y in range(SIZE):
        for x in range(SIZE):
            red, green, blue, alpha = pixels[x, y]
            if alpha < 40:
                continue
            solid.add((x, y))
            level[(x, y)] = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0

    # Normalise luminance to the sprite's own range.
    if level:
        low, high = min(level.values()), max(level.values())
        span = max(1e-6, high - low)
        level = {key: (value - low) / span for key, value in level.items()}

    for name, palette in PALETTES.items():
        palette_outline, palette_shadow, palette_base, palette_light = palette
        image = Image.new("RGBA", (SIZE, SIZE))
        out = image.load()

        for x, y in solid:
            value = level[(x, y)]
            if value > 0.66:
                colour = palette_light
            elif value > 0.33:
                colour = palette_base
            else:
                colour = palette_shadow
            out[x, y] = (*colour, 255)

        # 1 px outline around the silhouette.
        for x, y in solid:
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    px, py = x + dx, y + dy
                    if 0 <= px < SIZE and 0 <= py < SIZE and (px, py) not in solid:
                        out[px, py] = (*palette_outline, 255)

        image.save(TEX / f"{name}.png")

    preview = Image.new("RGBA", (SIZE * len(PALETTES) + 8 * (len(PALETTES) - 1), SIZE), (24, 24, 24, 255))
    for index, name in enumerate(PALETTES):
        preview.paste(Image.open(TEX / f"{name}.png"), (index * (SIZE + 8), 0))
    preview.resize((preview.width * 4, preview.height * 4), Image.NEAREST).save(
        Path(__file__).with_name("preview.png"))
    print("wrote " + ", ".join(PALETTES))


if __name__ == "__main__":
    main()
