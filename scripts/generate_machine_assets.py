#!/usr/bin/env python3
"""Generate M10 machine textures, models and blockstates.

Run with the bundled Pillow environment:

    /path/to/python3 scripts/generate_machine_assets.py

Outputs (all overwritten, deterministic):
  assets/exnihilosatellitium/textures/block/{electric_sieve,electric_hammer,
      fuel_sieve,fuel_hammer}.png            16x16
  assets/exnihilosatellitium/textures/item/{speed,fortune}_upgrade_{1,2,3}.png
  assets/exnihilosatellitium/textures/gui/{electric_machine,fuel_machine}.png
  blockstates/*.json, models/block/*.json, models/item/*.json
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "exnihilosatellitium"

# ---------------------------------------------------------------- palette
EDGE = (44, 49, 54)
PLATE = (126, 134, 141)
PLATE_DARK = (94, 101, 108)
PLATE_LIGHT = (171, 179, 186)
BOLT = (203, 210, 215)
MESH_DARK = (38, 43, 48)
MESH_WIRE = (150, 159, 166)
CYAN = (86, 206, 236)
CYAN_LIGHT = (170, 236, 252)
RUST = (124, 84, 58)
RUST_DARK = (88, 58, 40)
FIRE = (232, 120, 36)
FIRE_LIGHT = (255, 198, 98)
BROWN = (116, 76, 42)
GEM = (54, 132, 86)
GEM_LIGHT = (150, 236, 184)
GEM_DARK = (30, 78, 50)
GOLD = (236, 196, 92)


def metal_base(img: Image.Image, plate: tuple[int, int, int],
               edge: tuple[int, int, int]) -> None:
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=edge)
    draw.rectangle((1, 1, 14, 14), fill=plate)
    draw.line((1, 1, 14, 1), fill=PLATE_LIGHT)
    draw.line((1, 1, 1, 14), fill=PLATE_LIGHT)
    draw.line((14, 2, 14, 14), fill=PLATE_DARK)
    draw.line((2, 14, 14, 14), fill=PLATE_DARK)
    for x, y in ((2, 2), (12, 2), (2, 12), (12, 12)):
        draw.point((x, y), fill=BOLT)
        draw.point((x + 1, y), fill=PLATE_DARK)


def mesh_window(img: Image.Image, x0: int, y0: int, x1: int, y1: int) -> None:
    draw = ImageDraw.Draw(img)
    draw.rectangle((x0 - 1, y0 - 1, x1 + 1, y1 + 1), fill=PLATE_LIGHT)
    draw.rectangle((x0, y0, x1, y1), fill=MESH_DARK)
    for x in range(x0 + 1, x1, 2):
        draw.line((x, y0, x, y1), fill=MESH_WIRE)
    for y in range(y0 + 1, y1, 2):
        draw.line((x0, y, x1, y), fill=MESH_WIRE)


def hammer_glyph(img: Image.Image) -> None:
    draw = ImageDraw.Draw(img)
    draw.rectangle((4, 3, 11, 6), fill=PLATE_DARK)
    draw.rectangle((4, 4, 11, 5), fill=PLATE_LIGHT)
    draw.line((4, 3, 11, 3), fill=BOLT)
    draw.rectangle((7, 7, 8, 12), fill=BROWN)
    draw.point((7, 12), fill=RUST_DARK)


def fire_vents(img: Image.Image) -> None:
    draw = ImageDraw.Draw(img)
    for x in (3, 7, 11):
        draw.line((x, 13, x + 1, 13), fill=FIRE)
        draw.point((x, 12), fill=FIRE_LIGHT)


def block_texture(name: str, *, fuel: bool, sieve: bool) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    metal_base(img, RUST if fuel else PLATE, RUST_DARK if fuel else EDGE)
    draw = ImageDraw.Draw(img)
    if sieve:
        mesh_window(img, 4, 4, 11, 11)
    else:
        hammer_glyph(img)
    if fuel:
        fire_vents(img)
    else:
        # status LED: electric machines get a small cyan lamp
        draw.rectangle((12, 10, 13, 11), fill=CYAN)
        draw.point((12, 10), fill=CYAN_LIGHT)
    return img


def pips(draw: ImageDraw.ImageDraw, tier: int, color: tuple[int, int, int]) -> None:
    xs = {1: [8], 2: [6, 10], 3: [5, 8, 11]}[tier]
    for x in xs:
        draw.point((x, 14), fill=color)


def upgrade_texture(kind: str, tier: int) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    if kind == "speed":
        # rounded badge
        draw.ellipse((2, 2, 13, 13), fill=(30, 52, 68, 255), outline=CYAN)
        draw.ellipse((3, 3, 12, 12), outline=(58, 96, 118, 255))
        # chevrons: one per tier, shifted
        for index in range(tier):
            x = 4 + index * 3
            draw.line((x, 5, x + 2, 8), fill=CYAN_LIGHT)
            draw.line((x + 2, 8, x, 11), fill=CYAN_LIGHT)
        pips(draw, tier, GOLD)
    else:
        draw.polygon(((8, 1), (14, 8), (8, 15), (2, 8)), fill=GEM, outline=GEM_DARK)
        draw.polygon(((8, 3), (12, 8), (8, 13), (4, 8)), fill=GEM_DARK)
        draw.polygon(((8, 5), (10, 8), (8, 11), (6, 8)), fill=GEM_LIGHT)
        for index in range(tier):
            # extra sparkle dots scale with the tier
            draw.point((3 + index * 2, 3), fill=GOLD)
        pips(draw, tier, GOLD)
    return img


def gui_texture(*, electric: bool) -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    width, height = 176, 186
    draw.rectangle((0, 0, width - 1, height - 1), fill=(198, 198, 198))
    draw.line((0, 0, width - 1, 0), fill=(255, 255, 255))
    draw.line((0, 0, 0, height - 1), fill=(255, 255, 255))
    draw.line((width - 1, 1, width - 1, height - 1), fill=(85, 85, 85))
    draw.line((1, height - 1, width - 1, height - 1), fill=(85, 85, 85))
    draw.line((1, 1, width - 2, 1), fill=(232, 232, 232))

    slots = [(26, 20), (26, 44)]
    if electric:
        slots += [(26 + i * 18, 68) for i in range(4)]
    else:
        slots += [(26, 68)]
    slots += [(98 + column * 18, 20 + row * 18) for row in range(3) for column in range(3)]
    slots += [(8 + column * 18, 104 + row * 18) for row in range(3) for column in range(9)]
    slots += [(8 + column * 18, 162) for column in range(9)]

    # divider between the machine area and the player inventory
    draw.line((8, 89, 168, 89), fill=(85, 85, 85))
    draw.line((8, 90, 168, 90), fill=(232, 232, 232))

    for x, y in slots:
        draw.rectangle((x - 1, y - 1, x + 16, y + 16), fill=(139, 139, 139))
        draw.line((x - 1, y - 1, x + 16, y - 1), fill=(55, 55, 55))
        draw.line((x - 1, y - 1, x - 1, y + 16), fill=(55, 55, 55))
        draw.line((x + 16, y, x + 16, y + 16), fill=(255, 255, 255))
        draw.line((x, y + 16, x + 16, y + 16), fill=(255, 255, 255))
    return img


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def machine_assets(name: str) -> None:
    write_json(ASSETS / "blockstates" / f"{name}.json",
               {"variants": {"": {"model": f"exnihilosatellitium:block/{name}"}}})
    write_json(ASSETS / "models" / "block" / f"{name}.json",
               {"parent": "minecraft:block/cube_all",
                "textures": {"all": f"exnihilosatellitium:block/{name}"}})
    write_json(ASSETS / "models" / "item" / f"{name}.json",
               {"parent": f"exnihilosatellitium:block/{name}"})


def main() -> None:
    for name, fuel, sieve in (
            ("electric_sieve", False, True),
            ("electric_hammer", False, False),
            ("fuel_sieve", True, True),
            ("fuel_hammer", True, False)):
        block_texture(name, fuel=fuel, sieve=sieve).save(
            ASSETS / "textures" / "block" / f"{name}.png")
        machine_assets(name)

    for kind in ("speed", "fortune"):
        for tier in (1, 2, 3):
            name = f"{kind}_upgrade_{tier}"
            upgrade_texture(kind, tier).save(
                ASSETS / "textures" / "item" / f"{name}.png")
            write_json(ASSETS / "models" / "item" / f"{name}.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"exnihilosatellitium:item/{name}"}})

    gui_texture(electric=True).save(
        ASSETS / "textures" / "gui" / "electric_machine.png")
    gui_texture(electric=False).save(
        ASSETS / "textures" / "gui" / "fuel_machine.png")
    print("machine assets written")


if __name__ == "__main__":
    main()
