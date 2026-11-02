#!/usr/bin/env python3
"""Mod icon (128x128): night sky, crescent moon, wooden sieve.

Drawn at 512x512 with supersampling and downscaled, so the edges stay smooth at
icon size. Output goes both to the jar root (`icon.png`, referenced by the mod
metadata) and to `art/icon/icon_512.png` for store listings.

    python3 art/icon/generate.py
"""

from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

SIZE = 128
SUPERSAMPLE = 4
CANVAS = SIZE * SUPERSAMPLE

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/icon.png"
PREVIEW = Path(__file__).parent / "icon_512.png"

STARS = (
    (58, 30, 1.2), (150, 44, 1.0), (196, 96, 1.4), (214, 42, 0.9), (52, 118, 1.1),
    (112, 150, 0.8), (248, 140, 1.0), (30, 176, 1.2), (176, 196, 0.9), (222, 214, 1.1),
    (70, 226, 0.8), (130, 252, 1.0), (250, 262, 0.9), (24, 286, 1.0), (190, 300, 0.8),
    (96, 316, 1.1), (238, 340, 0.7), (44, 350, 0.9), (160, 366, 0.8), (286, 108, 0.9),
    (300, 200, 0.7), (16, 92, 0.7), (268, 296, 0.9), (118, 396, 0.8),
)


def backplate() -> Image.Image:
    img = Image.new("RGBA", (CANVAS, CANVAS), (11, 16, 32, 255))
    px = img.load()
    for y in range(CANVAS):
        t = y / (CANVAS - 1)
        r = int(11 + 12 * t)
        g = int(16 + 16 * t)
        b = int(32 + 26 * t)
        for x in range(CANVAS):
            px[x, y] = (r, g, b, 255)

    # Soft moonlight glow behind the crescent.
    glow = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gx, gy, gr = int(CANVAS * 0.66), int(CANVAS * 0.30), int(CANVAS * 0.34)
    for step in range(18):
        radius = gr - step * (gr // 22)
        alpha = 6 + step * 2
        gdraw.ellipse([gx - radius, gy - radius, gx + radius, gy + radius],
                      fill=(246, 231, 184, alpha))
    glow = glow.filter(ImageFilter.GaussianBlur(CANVAS // 40))
    img = Image.alpha_composite(img, glow)

    draw = ImageDraw.Draw(img)
    for x, y, size in STARS:
        cx, cy = int(x * SUPERSAMPLE), int(y * SUPERSAMPLE)
        r = max(1, int(size * SUPERSAMPLE * 0.8))
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(226, 235, 255, 210))
    return img


def crescent(img: Image.Image) -> Image.Image:
    cx, cy, radius = int(CANVAS * 0.66), int(CANVAS * 0.30), int(CANVAS * 0.19)
    moon = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    draw = ImageDraw.Draw(moon)
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
                 fill=(246, 233, 190, 255))
    # Bite out the lit disc with the sky colour, then keep only the crescent.
    mask = Image.new("L", (CANVAS, CANVAS), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], fill=255)
    mdraw.ellipse([cx - radius + int(radius * 0.46), cy - radius - int(radius * 0.18),
                   cx + radius + int(radius * 0.46), cy + radius - int(radius * 0.18)],
                  fill=0)
    moon.putalpha(mask)

    halo = moon.filter(ImageFilter.GaussianBlur(CANVAS // 48))
    img = Image.alpha_composite(img, halo)
    return Image.alpha_composite(img, moon)


def sieve(img: Image.Image) -> Image.Image:
    draw = ImageDraw.Draw(img)
    # Frame: a square ring, slightly inset from the icon edge.
    left, top = int(CANVAS * 0.20), int(CANVAS * 0.52)
    right, bottom = int(CANVAS * 0.80), int(CANVAS * 0.90)
    depth = int(CANVAS * 0.035)

    draw.rectangle([left, top, right, bottom], fill=(74, 44, 22, 255))
    draw.rectangle([left + depth, top + depth, right - depth, bottom - depth],
                   fill=(58, 34, 17, 255))

    # Mesh: two families of diagonal lines inside the frame.
    inner = (left + depth * 2, top + depth * 2, right - depth * 2, bottom - depth * 2)
    mesh = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    mdraw = ImageDraw.Draw(mesh)
    step = int(CANVAS * 0.045)
    width = max(1, int(CANVAS * 0.008))
    for offset in range(-CANVAS, CANVAS, step):
        mdraw.line([inner[0] + offset, inner[1], inner[0] + offset + (inner[3] - inner[1]),
                    inner[3]], fill=(214, 182, 133, 235), width=width)
        mdraw.line([inner[2] - offset, inner[1], inner[2] - offset - (inner[3] - inner[1]),
                    inner[3]], fill=(214, 182, 133, 235), width=width)
    clip = Image.new("L", (CANVAS, CANVAS), 0)
    ImageDraw.Draw(clip).rectangle(inner, fill=255)
    mesh.putalpha(Image.composite(mesh.getchannel("A"), Image.new("L", (CANVAS, CANVAS), 0), clip))

    # Wooden frame highlight on the top/left edges.
    draw.rectangle([left, top, right, top + depth], fill=(138, 90, 44, 255))
    draw.rectangle([left, top, left + depth, bottom], fill=(120, 76, 36, 255))

    img = Image.alpha_composite(img, mesh)

    # A couple of pebbles resting in the mesh.
    for cx, cy, r, colour in ((0.40, 0.70, 0.030, (138, 138, 138)),
                              (0.52, 0.74, 0.036, (176, 176, 176)),
                              (0.46, 0.66, 0.024, (196, 140, 118))):
        x, y, radius = int(CANVAS * cx), int(CANVAS * cy), int(CANVAS * r)
        ImageDraw.Draw(img).ellipse([x - radius, y - radius, x + radius, y + radius],
                                    fill=colour + (255,), outline=(40, 28, 18, 255),
                                    width=max(1, radius // 3))
    return img


def main() -> None:
    img = backplate()
    img = crescent(img)
    img = sieve(img)

    icon = img.resize((SIZE, SIZE), Image.LANCZOS)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    icon.save(OUT)
    img.resize((512, 512), Image.LANCZOS).save(PREVIEW)
    print(f"wrote {OUT} and {PREVIEW}")


if __name__ == "__main__":
    main()
