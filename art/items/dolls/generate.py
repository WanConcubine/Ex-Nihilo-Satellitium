#!/usr/bin/env python3
"""Doll item textures (M6): doll / angry doll / creepy doll.

The shape follows Ex Nihilo Creatio's doll — head, arms raised diagonally and
legs spread, i.e. a straw voodoo doll — redrawn with the project's modern
1.21 shading: 4-step palette, 1 px outline, light from the top-left.

    <python-with-pillow> art/items/dolls/generate.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEX = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"

SIZE = 16

# outline, shadow, base, light
PALETTES = {
    "doll": [(105, 118, 130), (174, 188, 201), (220, 230, 238), (244, 249, 253)],
    "angry_doll": [(70, 30, 30), (150, 62, 58), (206, 104, 88), (246, 170, 128)],
    "creepy_doll": [(52, 34, 78), (110, 78, 158), (160, 122, 214), (214, 190, 246)],
}


def line(points: list[tuple[int, int]], x0: int, y0: int, x1: int, y1: int) -> None:
    """Bresenham line, appended to `points`."""
    dx, dy = abs(x1 - x0), abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    err = dx - dy
    while True:
        points.append((x0, y0))
        if x0 == x1 and y0 == y1:
            return
        e2 = 2 * err
        if e2 > -dy:
            err -= dy
            x0 += sx
        if e2 < dx:
            err += dx
            y0 += sy


def shape() -> set[tuple[int, int]]:
    pixels: set[tuple[int, int]] = set()
    body: list[tuple[int, int]] = []
    # Head: 4x4 blob centred at x=7..10, y=1..4.
    for y in range(1, 5):
        for x in range(6, 10):
            body.append((x, y))
    # Torso.
    line(body, 7, 5, 7, 9)
    line(body, 8, 5, 8, 9)
    # Arms raised diagonally.
    line(body, 6, 6, 2, 2)
    line(body, 7, 6, 3, 2)
    line(body, 9, 6, 13, 2)
    line(body, 10, 6, 14, 2)
    # Legs spread.
    line(body, 6, 9, 3, 13)
    line(body, 7, 9, 4, 13)
    line(body, 9, 9, 12, 13)
    line(body, 10, 9, 13, 13)
    for x, y in body:
        if 0 <= x < SIZE and 0 <= y < SIZE:
            pixels.add((x, y))
    return pixels


def render(palette: list[tuple[int, int, int]]) -> Image.Image:
    outline, shadow, base, light = palette
    body = shape()
    image = Image.new("RGBA", (SIZE, SIZE))
    pixels = image.load()

    # 1 px outline around the silhouette.
    for x, y in body:
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                px, py = x + dx, y + dy
                if 0 <= px < SIZE and 0 <= py < SIZE and (px, py) not in body:
                    pixels[px, py] = (*outline, 255)

    # Shaded fill: light on the top-left half, shadow on the bottom-right.
    for x, y in body:
        if (x + y) % 7 == 0:
            pixels[x, y] = (*light, 255)
        elif (x - y) > 3 or y >= 11:
            pixels[x, y] = (*shadow, 255)
        else:
            pixels[x, y] = (*base, 255)
    return image


def main() -> None:
    TEX.mkdir(parents=True, exist_ok=True)
    for name, palette in PALETTES.items():
        render(palette).save(TEX / f"{name}.png")
        print(f"wrote {name}.png")


if __name__ == "__main__":
    main()
