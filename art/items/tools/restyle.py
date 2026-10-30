#!/usr/bin/env python3
"""Restyle the Ex Nihilo: Creatio (MIT) textures to the 1.21 look.

Creatio's 1.12 textures are crisp but flat: the crooks have no directional
shading and the dust block is made of ~110 near-identical beige tones.
For the hammers this script keeps every adopted pixel position and only
changes colours; the silkworm is redrawn as an 8-frame animation.

* crooks       -> 1px bevel: dark rim on the lower/right edges, light on the
                  upper/left edges (same trick as vanilla bone / wooden sword);
* hammers      -> vanilla 1.21 tool colours: #181818 outlines for stone/iron,
                  warm gold for the golden hammer, netherite palette for the
                  netherite hammer (derived from the diamond sprite);
* silkworm     -> 8-frame wiggling loop, warm dark outline + muted cream/amber
                  body (no pure black, no saturated yellow);
* dust block   -> 4-tone quantisation; the noise pattern stays, the 110 tones
                  collapse to 4 (vanilla sand uses 6).

The pristine adopted PNGs live in art/items/tools/creatio/. Do not edit those;
edit this script and re-run it.

Usage:
    python3 art/items/tools/restyle.py
"""

from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[3]
BASE = ROOT / "art/items/tools/creatio"
ASSETS = ROOT / "src/main/resources/assets/exnihilosatellitium/textures"
PREVIEW = ROOT / "docs/images/m3-textures.png"

SOLID = 128  # alpha threshold: the bone crook has one a=26 stray pixel


# ---------------------------------------------------------------- helpers


def is_solid(im: Image.Image, x: int, y: int) -> bool:
    if not (0 <= x < 16 and 0 <= y < 16):
        return False
    return im.getpixel((x, y))[3] >= SOLID


def bevel(src: Image.Image, dark, base, light) -> Image.Image:
    """Directional bevel: dark on lower/right edges, light on upper/left."""
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if not is_solid(src, x, y):
                continue
            up = not is_solid(src, x, y - 1)
            left = not is_solid(src, x - 1, y)
            down = not is_solid(src, x, y + 1)
            right = not is_solid(src, x + 1, y)
            if down or right:
                colour = dark
            elif up or left:
                colour = light
            else:
                colour = base
            out.putpixel((x, y), colour + (255,))
    return out


def recolor(src: Image.Image, mapping: dict) -> Image.Image:
    out = src.copy()
    for y in range(16):
        for x in range(16):
            pixel = out.getpixel((x, y))
            if pixel[3] >= SOLID and pixel[:3] in mapping:
                out.putpixel((x, y), mapping[pixel[:3]] + (255,))
    return out


def quantise_dust(src: Image.Image, palette, weights=(0.25, 0.5, 0.75)) -> Image.Image:
    """Collapse the dust noise to len(palette) tones, keeping the pattern."""
    pixels = [src.getpixel((x, y)) for y in range(16) for x in range(16)]
    luma = sorted(0.2126 * r + 0.7152 * g + 0.0722 * b for r, g, b in pixels)
    cuts = [luma[int(len(luma) * w)] for w in weights]
    out = Image.new("RGB", (16, 16))
    for y in range(16):
        for x in range(16):
            r, g, b = src.getpixel((x, y))
            value = 0.2126 * r + 0.7152 * g + 0.0722 * b
            index = sum(1 for cut in cuts if value >= cut)
            out.putpixel((x, y), palette[index])
    return out


# ---------------------------------------------------------------- silkworm


WORM_OUTLINE = (0x33, 0x2A, 0x1B)
WORM_CREAM = (0xF6, 0xEA, 0xC8)
WORM_AMBER = (0xDD, 0xC5, 0x84)
WORM_EYE = (0x7F, 0x9D, 0x5D)
WORM_FRAMES = 4

# One-pixel see-saw: (head dy, middle dy, tail dy). Frame 0 is the untouched
# Creatio sprite, so the icon still looks exactly like the adopted artwork;
# the other frames only shift the head, the middle and the tail by 1 px.
WORM_POSES = ((0, 0, 0), (1, 0, 0), (0, -1, 0), (0, 0, 1))


def silkworm_strip() -> Image.Image:
    """Animate the adopted Creatio silkworm with a 4-frame crawl.

    The base PNG is recoloured exactly like the other adopted textures, then
    each frame shifts three segments (head / middle / tail) by one pixel.
    Keeps the original silhouette and eye instead of redrawing the worm.
    """
    source = recolor(
        Image.open(BASE / "silkworm.png").convert("RGBA"),
        {
            (0x00, 0x00, 0x00): WORM_OUTLINE,
            (0xFE, 0xDF, 0xAE): WORM_CREAM,
            (0xFE, 0xE1, 0x81): WORM_AMBER,
            (0x7E, 0xC3, 0x74): WORM_EYE,
        },
    )
    frames = []
    for head_dy, middle_dy, tail_dy in WORM_POSES:
        frame = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for x in range(16):
            dy = head_dy if x <= 5 else (middle_dy if x <= 9 else tail_dy)
            for y in range(16):
                pixel = source.getpixel((x, y))
                if pixel[3] == 0:
                    continue
                if 0 <= y + dy < 16:
                    frame.putpixel((x, y + dy), pixel)
        # Close the 1 px seams left between segments with the nearest colour.
        for x in range(16):
            solid = [y for y in range(16) if frame.getpixel((x, y))[3]]
            if not solid:
                continue
            for y in range(min(solid), max(solid) + 1):
                if frame.getpixel((x, y))[3] == 0:
                    above = frame.getpixel((x, y - 1)) if y > 0 else (0, 0, 0, 0)
                    below = frame.getpixel((x, y + 1)) if y < 15 else (0, 0, 0, 0)
                    frame.putpixel((x, y), above if above[3] else below)
        frames.append(frame)

    strip = Image.new("RGBA", (16, 16 * WORM_FRAMES), (0, 0, 0, 0))
    for index, frame in enumerate(frames):
        strip.alpha_composite(frame, (0, 16 * index))
    return strip


# ---------------------------------------------------------------- recipes


def build() -> dict[str, Image.Image]:
    wooden_crook = bevel(
        Image.open(BASE / "wooden_crook.png").convert("RGBA"),
        dark=(0x28, 0x1E, 0x0B),
        base=(0x49, 0x36, 0x15),
        light=(0x68, 0x4E, 0x1E),
    )
    bone_crook = bevel(
        Image.open(BASE / "bone_crook.png").convert("RGBA"),
        dark=(0xCB, 0xC6, 0xA5),
        base=(0xF3, 0xF0, 0xDF),
        light=(0xFF, 0xFF, 0xFD),
    )
    wooden_hammer = recolor(
        Image.open(BASE / "wooden_hammer.png").convert("RGBA"),
        {(0x37, 0x29, 0x10): (0x20, 0x18, 0x0A)},
    )
    stone_hammer = recolor(
        Image.open(BASE / "stone_hammer.png").convert("RGBA"),
        {
            (0x6C, 0x6C, 0x6C): (0x18, 0x18, 0x18),
            (0x5A, 0x5A, 0x5A): (0x2E, 0x2E, 0x2E),
        },
    )
    iron_hammer = recolor(
        Image.open(BASE / "iron_hammer.png").convert("RGBA"),
        {(0x28, 0x28, 0x28): (0x18, 0x18, 0x18)},
    )
    golden_hammer = recolor(
        Image.open(BASE / "hammergold.png").convert("RGBA"),
        {
            (0xEA, 0xEE, 0x57): (0xFD, 0xFF, 0x76),  # highlight
            (0xCD, 0xD0, 0x50): (0xEA, 0xEE, 0x57),  # light
            (0xBC, 0xBF, 0x4D): (0xE9, 0xB1, 0x15),  # mid
            (0x9C, 0x9E, 0x45): (0xC9, 0x9A, 0x12),  # mid-dark
            (0x7C, 0x7E, 0x3E): (0x82, 0x5D, 0x16),  # rim
            (0x3D, 0x3D, 0x24): (0x3F, 0x2E, 0x0E),  # dark edge
        },
    )
    diamond_hammer = Image.open(BASE / "hammerdiamond.png").convert("RGBA")
    netherite_hammer = recolor(
        diamond_hammer,
        {
            (0x33, 0xEB, 0xCB): (0x86, 0x7B, 0x86),
            (0x2B, 0xC7, 0xAC): (0x70, 0x67, 0x70),
            (0x27, 0xB2, 0x9A): (0x5D, 0x56, 0x5D),
            (0x1E, 0x8A, 0x77): (0x4A, 0x29, 0x40),
            (0x0E, 0x3F, 0x36): (0x2F, 0x21, 0x22),
            (0x08, 0x25, 0x20): (0x23, 0x10, 0x12),
        },
    )
    silkworm = silkworm_strip()
    cooked_silkworm = recolor(
        Image.open(BASE / "silkwormcooked.png").convert("RGBA"),
        {
            (0x00, 0x00, 0x00): WORM_OUTLINE,
            (0x91, 0x73, 0x67): (0xA8, 0x84, 0x5F),
            (0x90, 0x6A, 0x4E): (0x8D, 0x6A, 0x4A),
            (0x57, 0x70, 0x46): (0x7F, 0x9D, 0x5D),
        },
    )
    dust = quantise_dust(
        Image.open(BASE / "dust.png").convert("RGB"),
        palette=[
            (0xCF, 0xCC, 0xB0),
            (0xD6, 0xD3, 0xB7),
            (0xDD, 0xDA, 0xBE),
            (0xE4, 0xE1, 0xC5),
        ],
    )
    return {
        "item/wooden_crook.png": wooden_crook,
        "item/bone_crook.png": bone_crook,
        "item/wooden_hammer.png": wooden_hammer,
        "item/stone_hammer.png": stone_hammer,
        "item/iron_hammer.png": iron_hammer,
        "item/golden_hammer.png": golden_hammer,
        "item/diamond_hammer.png": diamond_hammer,
        "item/netherite_hammer.png": netherite_hammer,
        "item/silkworm.png": silkworm,
        "item/cooked_silkworm.png": cooked_silkworm,
        "block/dust.png": dust,
    }


# ---------------------------------------------------------------- preview


def iso_cube(texture: Image.Image, size: int = 96) -> Image.Image:
    tex = texture.convert("RGBA").resize((size, size), Image.NEAREST)
    width, height = size, size + size // 2
    half = size // 2
    out = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    faces = (
        ([(half, 0), (size, half), (half, size), (0, half)], 1.0),      # top
        ([(0, half), (half, size), (half, size + half), (0, half + half)], 0.72),  # left
        ([(half, size), (size, half), (size, half + half), (half, size + half)], 0.55),  # right
    )
    for points, shade in faces:
        p0, p1, p3 = points[0], points[1], points[3]
        a, b = (p1[0] - p0[0]) / size, (p3[0] - p0[0]) / size
        c, d = (p1[1] - p0[1]) / size, (p3[1] - p0[1]) / size
        det = a * d - b * c
        ia, ib, ic, id_ = d / det, -b / det, -c / det, a / det
        e = -(ia * p0[0] + ib * p0[1])
        f = -(ic * p0[0] + id_ * p0[1])
        face = tex.transform((width, height), Image.AFFINE,
                             (ia, ib, e, ic, id_, f), resample=Image.NEAREST)
        px = face.load()
        for y in range(height):
            for x in range(width):
                r, g, bl, al = px[x, y]
                if al:
                    px[x, y] = (int(r * shade), int(g * shade), int(bl * shade), al)
        out.alpha_composite(face)
    return out


def render_preview(textures: dict[str, Image.Image]) -> None:
    silkworm_first = textures["item/silkworm.png"].crop((0, 0, 16, 16))
    items = [
        ("Wooden Hammer", textures["item/wooden_hammer.png"]),
        ("Stone Hammer", textures["item/stone_hammer.png"]),
        ("Iron Hammer", textures["item/iron_hammer.png"]),
        ("Golden Hammer", textures["item/golden_hammer.png"]),
        ("Diamond Hammer", textures["item/diamond_hammer.png"]),
        ("Netherite Hammer", textures["item/netherite_hammer.png"]),
        ("Wooden Crook", textures["item/wooden_crook.png"]),
        ("Bone Crook", textures["item/bone_crook.png"]),
        ("Silkworm (4 frames)", silkworm_first),
        ("Cooked Silkworm", textures["item/cooked_silkworm.png"]),
    ]
    scale, pad, label_h, cols = 8, 18, 22, 4
    cell = scale * 16
    cube = iso_cube(textures["block/dust.png"], 96)
    width = pad + cols * (cell + pad)
    rows = 3
    height = pad + 20 + rows * (cell + label_h + pad) + cube.height + 40
    sheet = Image.new("RGBA", (width, height), (250, 250, 250, 255))
    draw = ImageDraw.Draw(sheet)
    draw.text((pad, 8), "M3 textures (adopted from Ex Nihilo: Creatio, MIT; restyled)",
              fill=(30, 30, 30, 255))
    for index, (name, image) in enumerate(items):
        x = pad + (index % cols) * (cell + pad)
        y = pad + 20 + (index // cols) * (cell + label_h + pad)
        draw.rectangle([x - 2, y - 2, x + cell + 2, y + cell + 2], outline=(200, 200, 200, 255))
        sheet.alpha_composite(image.convert("RGBA").resize((cell, cell), Image.NEAREST), (x, y))
        draw.text((x, y + cell + 4), name, fill=(20, 20, 20, 255))
    y = pad + 20 + rows * (cell + label_h + pad)
    draw.text((pad, y), "Dust block (isometric preview)", fill=(20, 20, 20, 255))
    sheet.alpha_composite(cube, (pad, y + 18))
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)


def render_worm_gif(strip: Image.Image) -> None:
    """Looping preview of the silkworm animation for the docs."""
    frames = []
    for index in range(strip.height // 16):
        frame = strip.crop((0, 16 * index, 16, 16 * index + 16))
        frame = frame.resize((128, 128), Image.NEAREST)
        background = Image.new("RGBA", (128, 128), (0x8B, 0x8B, 0x8B, 255))
        background.alpha_composite(frame)
        frames.append(background.convert("P", palette=Image.ADAPTIVE, colors=32))
    target = ROOT / "docs/images/silkworm-animation.gif"
    frames[0].save(target, save_all=True, append_images=frames[1:],
                   duration=150, loop=0, optimize=False)
    print(f"wrote {target.relative_to(ROOT)}")


def main() -> None:
    textures = build()
    for relative, image in textures.items():
        target = ASSETS / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        mode = "RGB" if image.mode == "RGB" else "RGBA"
        image.convert(mode).save(target)
        print(f"wrote {target.relative_to(ROOT)}")
    meta = ASSETS / "item/silkworm.png.mcmeta"
    meta.write_text('{\n  "animation": {\n    "frametime": 6\n  }\n}\n', encoding="utf-8")
    print(f"wrote {meta.relative_to(ROOT)}")
    render_preview(textures)
    print(f"wrote {PREVIEW.relative_to(ROOT)}")
    render_worm_gif(textures["item/silkworm.png"])


if __name__ == "__main__":
    main()
