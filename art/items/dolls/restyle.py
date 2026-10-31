#!/usr/bin/env python3
"""Restyle Creatio's doll icon for this project (MIT, see THIRD-PARTY.md).

Source: `art/items/dolls/creatio/doll.png` (128x128 = the 16x16 icon at 8x).
The pipeline mirrors `art/blocks/crucible/restyle.py`: drop back to 16x16, add a
1 px outline, and quantise to a 4-step palette so it matches the 1.21 item style.
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

# outline, shadow, base, light
PALETTES = {
    "doll": [(74, 86, 100), (140, 158, 175), (198, 212, 226), (236, 244, 250)],
    "angry_doll": [(64, 26, 24), (146, 52, 44), (204, 96, 72), (246, 168, 118)],
    "creepy_doll": [(46, 30, 72), (104, 74, 150), (158, 122, 208), (214, 190, 244)],
}


def main() -> None:
    source = Image.open(SRC).convert("RGBA")
    sprite = source.resize((16, 16), Image.NEAREST)
    pixels = sprite.load()

    # Silhouette mask and per-pixel luminance (0..1).
    solid: set[tuple[int, int]] = set()
    level: dict[tuple[int, int], float] = {}
    for y in range(16):
        for x in range(16):
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
        image = Image.new("RGBA", (16, 16))
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
                    if 0 <= px < 16 and 0 <= py < 16 and (px, py) not in solid:
                        out[px, py] = (*palette_outline, 255)

        image.save(TEX / f"{name}.png")

    preview = Image.new("RGBA", (60, 16), (24, 24, 24, 255))
    for index, name in enumerate(("doll", "angry_doll", "creepy_doll")):
        preview.paste(Image.open(TEX / f"{name}.png"), (index * 20, 0))
    preview.resize((preview.width * 8, preview.height * 8), Image.NEAREST).save(
        Path(__file__).with_name("preview.png"))
    print("wrote doll / angry_doll / creepy_doll")


if __name__ == "__main__":
    main()
