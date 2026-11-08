#!/usr/bin/env python3
"""Generate the material hammer icons from the adopted Creatio hammer base.

The six vanilla hammers are hand-restyled by ``restyle.py``. This script keeps
that artwork as the silhouette and only swaps the head colours, so every new
material hammer shares the same 1.21-style 16x16 icon:

* metals (tin/aluminium/zinc/copper/silver/lead/nickel/osmium/platinum)
  recolour ``iron_hammer.png`` with the five-tone palette already used by
  ``art/items/ore_pieces/generate.py``;
* Bamboo gems (sakura/maple/ginkgo) recolour ``diamond_hammer.png`` with the
  six-tone palettes sampled from the BambooMod gem sprites (colours only, no
  BambooMod pixels are copied).

The wooden handle keeps its original colours in both cases.

Usage:
    python3 art/items/tools/hammer_variants.py
    python3 art/items/tools/hammer_variants.py --ids sakura,maple,ginkgo --out /tmp/hammers
"""

from __future__ import annotations

import argparse
import importlib.util
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
TEXTURES = ROOT / "src/main/resources/assets/exnihilosatellitium/textures/item"
ORE_PALETTES = ROOT / "art/items/ore_pieces/generate.py"

METALS = ("tin", "aluminum", "zinc", "copper", "silver", "lead", "nickel", "osmium", "platinum")
GEMS = ("sakura", "maple", "ginkgo")
#: Optional Draconic Evolution hammers (only present when the DE jars are on the build).
DE = ("wyvern", "draconic", "chaotic")

#: Head colours in the restyled iron hammer -> tone slot.
IRON_MAP = {
    "#181818": "outline",
    "#444444": "dark",
    "#A0A0A0": "shadow",
    "#C1C1C1": "base",
    "#D8D8D8": "light",
    "#FFFFFF": "highlight",
}

#: Head colours in the restyled diamond hammer -> tone slot.
DIAMOND_MAP = {
    "#082520": "outline",
    "#0E3F36": "dark",
    "#1E8A77": "shadow",
    "#27B29A": "base",
    "#2BC7AC": "light",
    "#33EBCB": "highlight",
}

#: Six-tone Bamboo gem palettes (outline, dark, shadow, base, light, highlight).
GEM_PALETTES = {
    "sakura": {
        "outline": (0x5A, 0x1A, 0x26),
        "dark": (0x9A, 0x30, 0x48),
        "shadow": (0xD4, 0x6A, 0x86),
        "base": (0xF4, 0x9A, 0xB2),
        "light": (0xFF, 0xC6, 0xD6),
        "highlight": (0xFF, 0xF0, 0xF4),
    },
    "maple": {
        "outline": (0x3A, 0x05, 0x03),
        "dark": (0x6E, 0x0A, 0x05),
        "shadow": (0xA3, 0x14, 0x0A),
        "base": (0xD9, 0x2A, 0x16),
        "light": (0xF4, 0x5C, 0x3C),
        "highlight": (0xFF, 0xB0, 0x9A),
    },
    "ginkgo": {
        "outline": (0x4A, 0x36, 0x06),
        "dark": (0x7A, 0x5E, 0x0E),
        "shadow": (0xA8, 0x82, 0x1E),
        "base": (0xD8, 0xB2, 0x2A),
        "light": (0xF2, 0xD4, 0x4E),
        "highlight": (0xFF, 0xF4, 0xB0),
    },
}

#: Draconic Evolution tiers (colours sampled from DE's tool sprites, no DE pixels copied).
DE_PALETTES = {
    "wyvern": {
        "outline": (0x1E, 0x0A, 0x33),
        "dark": (0x3A, 0x0F, 0x66),
        "shadow": (0x57, 0x27, 0x91),
        "base": (0x7A, 0x3A, 0xC8),
        "light": (0x9B, 0x3B, 0xFF),
        "highlight": (0xD8, 0xB0, 0xFF),
    },
    "draconic": {
        "outline": (0x1A, 0x00, 0x00),
        "dark": (0x4A, 0x00, 0x00),
        "shadow": (0x8A, 0x00, 0x00),
        "base": (0xC2, 0x3A, 0x00),
        "light": (0xFF, 0x76, 0x00),
        "highlight": (0xFF, 0xC0, 0x80),
    },
    "chaotic": {
        "outline": (0x05, 0x00, 0x00),
        "dark": (0x15, 0x00, 0x00),
        "shadow": (0x2A, 0x00, 0x00),
        "base": (0x4F, 0x00, 0x00),
        "light": (0x8A, 0x10, 0x10),
        "highlight": (0xFF, 0x40, 0x40),
    },
}


def hex_rgb(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))  # type: ignore[return-value]


def mix(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))  # type: ignore[return-value]


def six_tone(palette: tuple[str, ...]) -> dict[str, tuple[int, int, int]]:
    """Expand the five-tone ore palette to the six shades the hammer uses."""
    outline, dark, base, light, highlight = (hex_rgb(c) for c in palette)
    return {
        "outline": outline,
        "dark": dark,
        "shadow": mix(dark, base, 0.5),
        "base": base,
        "light": light,
        "highlight": highlight,
    }


def load_metal_palettes() -> dict[str, tuple[str, ...]]:
    spec = importlib.util.spec_from_file_location("ore_pieces", ORE_PALETTES)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {ORE_PALETTES}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    palettes = dict(module.PALETTES)
    # generate_ore_variants.py adds osmium as a local default; keep the same values.
    palettes.setdefault("osmium", ("#26313D", "#3F5B78", "#608FC4", "#8FB4DC", "#C4DCF4"))
    return palettes


def recolor(base: Image.Image, tones: dict[str, tuple[int, int, int]],
            mapping: dict[str, str]) -> Image.Image:
    out = base.copy()
    for y in range(16):
        for x in range(16):
            r, g, b, a = out.getpixel((x, y))
            if a < 128:
                continue
            key = "#%02X%02X%02X" % (r, g, b)
            slot = mapping.get(key)
            if slot is not None:
                out.putpixel((x, y), tones[slot] + (255,))
    return out


def build(ids: list[str], metal_palettes: dict[str, tuple[str, ...]]) -> dict[str, Image.Image]:
    iron = Image.open(TEXTURES / "iron_hammer.png").convert("RGBA")
    diamond = Image.open(TEXTURES / "diamond_hammer.png").convert("RGBA")
    out: dict[str, Image.Image] = {}
    for material in ids:
        if material in GEMS:
            out[material] = recolor(diamond, GEM_PALETTES[material], DIAMOND_MAP)
        elif material in DE:
            out[material] = recolor(iron, DE_PALETTES[material], IRON_MAP)
        else:
            out[material] = recolor(iron, six_tone(metal_palettes[material]), IRON_MAP)
    return out


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path, default=TEXTURES,
                        help="texture directory (default: the mod's item textures)")
    parser.add_argument("--ids", default=",".join(METALS + GEMS + DE),
                        help="comma-separated material ids")
    args = parser.parse_args()

    ids = [value.strip() for value in args.ids.split(",") if value.strip()]
    palettes = load_metal_palettes()
    unknown = [value for value in ids if value not in METALS and value not in GEMS and value not in DE]
    if unknown:
        raise SystemExit(f"unknown material(s): {', '.join(unknown)}")

    args.out.mkdir(parents=True, exist_ok=True)
    for material, image in build(ids, palettes).items():
        target = args.out / f"{material}_hammer.png"
        image.save(target)
        print(f"wrote {target}")


if __name__ == "__main__":
    main()
