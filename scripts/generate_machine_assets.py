#!/usr/bin/env python3
"""Generate M10 machine textures, models and blockstates.

Run with the bundled Pillow environment:

    /path/to/python3 scripts/generate_machine_assets.py

Outputs (all overwritten, deterministic):
  assets/exnihilosatellitium/textures/block/{electric_sieve,electric_hammer,
      fuel_sieve,fuel_hammer}.png            16x16
  assets/exnihilosatellitium/textures/item/{speed,fortune}_upgrade_{1,2,3}.png
  assets/exnihilosatellitium/textures/gui/{electric_machine,fuel_machine,
      energy_fill,fuel_fill,energy_icon,fuel_icon,arrow_track,arrow_fill}.png
  blockstates/*.json, models/block/*.json, models/item/*.json

GUI layout (176x186): machine area 0..88, divider 89/90, inventory label 93,
player inventory 104..158, hotbar 162..178.
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

# GUI geometry (inclusive coordinates)
GAUGE_FRAME = (7, 18, 18, 63)       # 12 x 46
GAUGE_INNER = (8, 19, 17, 62)       # 10 x 44
ARROW_X, ARROW_Y, ARROW_W, ARROW_H = 52, 26, 24, 16
ICON_X, ICON_Y, ICON_SIZE = 9, 36, 8


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
        draw.ellipse((2, 2, 13, 13), fill=(30, 52, 68, 255), outline=CYAN)
        draw.ellipse((3, 3, 12, 12), outline=(58, 96, 118, 255))
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
            draw.point((3 + index * 2, 3), fill=GOLD)
        pips(draw, tier, GOLD)
    return img


def arrow_points() -> list[tuple[int, int]]:
    return [(0, 5), (13, 5), (13, 0), (23, 8), (13, 15), (13, 10), (0, 10)]


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
    slots += [(99 + column * 18, 20 + row * 18) for row in range(3) for column in range(4)]
    slots += [(8 + column * 18, 104 + row * 18) for row in range(3) for column in range(9)]
    slots += [(8 + column * 18, 162) for column in range(9)]

    draw.line((8, 89, 168, 89), fill=(85, 85, 85))
    draw.line((8, 90, 168, 90), fill=(232, 232, 232))

    # energy / fuel gauge: dark bevel, black well; the fill + icon are blitted by the screen
    x0, y0, x1, y1 = GAUGE_FRAME
    draw.rectangle(GAUGE_FRAME, fill=(55, 55, 55))
    draw.line((x0, y0, x1, y0), fill=(255, 255, 255))
    draw.line((x0, y0, x0, y1), fill=(255, 255, 255))
    draw.line((x0 + 1, y1, x1, y1), fill=(85, 85, 85))
    draw.rectangle(GAUGE_INNER, fill=(22, 22, 22))

    # vanilla-style arrow: gray track is a sprite, the white fill is cropped by the screen

    for x, y in slots:
        draw.rectangle((x - 1, y - 1, x + 16, y + 16), fill=(139, 139, 139))
        draw.line((x - 1, y - 1, x + 16, y - 1), fill=(55, 55, 55))
        draw.line((x - 1, y - 1, x - 1, y + 16), fill=(55, 55, 55))
        draw.line((x + 16, y, x + 16, y + 16), fill=(255, 255, 255))
        draw.line((x, y + 16, x + 16, y + 16), fill=(255, 255, 255))
    return img


def gauge_fill_sprite(top: tuple[int, int, int],
                      bottom: tuple[int, int, int]) -> Image.Image:
    """10x44 vertical bar: gradient + subtle scanlines + edge light/shade."""
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for y in range(44):
        t = y / 43.0
        color = (int(top[0] * (1 - t) + bottom[0] * t),
                 int(top[1] * (1 - t) + bottom[1] * t),
                 int(top[2] * (1 - t) + bottom[2] * t), 255)
        draw.line((0, y, 9, y), fill=color)
    draw.line((0, 0, 0, 43), fill=(255, 255, 255, 90))
    draw.line((9, 0, 9, 43), fill=(0, 0, 0, 90))
    for y in range(43, -1, -6):
        draw.line((0, y, 9, y), fill=(0, 0, 0, 26))
    return img


def bolt_icon() -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    points = [(5, 0), (8, 0), (5, 4), (8, 4), (2, 8), (4, 4), (1, 4)]
    draw.polygon(points, fill=(20, 20, 20, 210))
    inner = [(4, 0), (6, 0), (4, 3), (6, 3), (3, 6), (4, 3), (2, 3)]
    draw.polygon(inner, fill=(255, 255, 255, 255))
    return img


def flame_icon() -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    outer = [(4, 0), (6, 2), (7, 4), (7, 6), (5, 8), (2, 8), (1, 5), (2, 3), (3, 4), (4, 2)]
    draw.polygon(outer, fill=(20, 20, 20, 210))
    inner = [(4, 2), (5, 3), (6, 5), (6, 6), (4, 7), (3, 7), (2, 5), (3, 4), (4, 4)]
    draw.polygon(inner, fill=(255, 255, 255, 255))
    return img


def arrow_track() -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon(arrow_points(), fill=(55, 55, 55, 255))
    inner = [(1, 6), (13, 6), (13, 2), (21, 8), (13, 14), (13, 9), (1, 9)]
    draw.polygon(inner, fill=(139, 139, 139, 255))
    draw.line((1, 6, 12, 6), fill=(198, 198, 198, 255))
    return img


def arrow_fill() -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon(arrow_points(), fill=(255, 255, 255, 255))
    inner = [(1, 6), (13, 6), (13, 2), (21, 8), (13, 14), (13, 9), (1, 9)]
    draw.polygon(inner, fill=(244, 244, 244, 255))
    draw.line((1, 7, 12, 7), fill=(255, 255, 255, 255))
    return img


def sieve_side_texture(*, fuel: bool) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    metal_base(img, RUST if fuel else PLATE, RUST_DARK if fuel else EDGE)
    draw = ImageDraw.Draw(img)
    band = (176, 58, 46) if not fuel else (176, 90, 30)
    draw.rectangle((0, 12, 15, 14), fill=band)
    draw.line((0, 12, 15, 12), fill=(255, 190, 170) if not fuel else (255, 214, 150))
    draw.line((0, 14, 15, 14), fill=(60, 20, 16))
    draw.point((2, 2), fill=BOLT)
    draw.point((13, 2), fill=BOLT)
    return img


def sieve_top_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(55, 61, 66))
    draw.rectangle((1, 1, 14, 14), fill=(150, 159, 166))
    draw.rectangle((3, 3, 12, 12), fill=(74, 81, 87))
    draw.line((1, 1, 14, 1), fill=(203, 210, 215))
    draw.line((1, 1, 1, 14), fill=(203, 210, 215))
    return img


def sieve_leg_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(46, 51, 56))
    for x in (3, 7, 11):
        draw.line((x, 0, x, 15), fill=(34, 38, 42))
        draw.line((x + 1, 0, x + 1, 15), fill=(66, 73, 79))
    return img


def sieve_mesh_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for x in range(0, 16, 2):
        draw.line((x, 0, x, 15), fill=(202, 210, 216, 255))
    for y in range(0, 16, 2):
        draw.line((0, y, 15, y), fill=(178, 187, 194, 255))
    return img


def sieve_inner_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(40, 45, 50))
    for x in range(1, 16, 4):
        draw.line((x, 0, x, 15), fill=(33, 37, 41))
    return img


def sieve_model(*, fuel: bool) -> dict:
    side = f"exnihilosatellitium:block/machine_frame_side_{'fuel' if fuel else 'electric'}"
    tex = {
        "particle": side,
        "side": side,
        "top": "exnihilosatellitium:block/machine_frame_top",
        "leg": "exnihilosatellitium:block/machine_leg",
        "mesh": "exnihilosatellitium:block/machine_mesh",
        "inner": "exnihilosatellitium:block/machine_inner",
    }

    def face(texture, uv=None):
        out = {"texture": texture}
        if uv:
            out["uv"] = uv
        return out

    side_uv = [0, 6, 16, 16]
    elements = []
    # four 1x1 legs, exactly like the manual sieve
    for x, z in ((1, 1), (14, 1), (1, 14), (14, 14)):
        elements.append({
            "from": [x, 0, z], "to": [x + 1, 11, z + 1],
            "faces": {f: face("#leg") for f in ("down", "up", "north", "south", "west", "east")},
        })
    # 1 px thick rim at y=10..14, matching the manual sieve frame
    walls = (
        ("north", [0, 10, 0, 16, 14, 1]),
        ("south", [0, 10, 15, 16, 14, 16]),
        ("west", [0, 10, 1, 1, 14, 15]),
        ("east", [15, 10, 1, 16, 14, 15]),
    )
    for outer, box in walls:
        faces = {f: face("#side") for f in ("down", "up", "north", "south", "west", "east")}
        faces["up"] = face("#top")
        faces[outer] = face("#side", side_uv)
        inner = {"north": "south", "south": "north", "west": "east", "east": "west"}[outer]
        faces[inner] = face("#inner")
        elements.append({"from": box[:3], "to": box[3:], "faces": faces})
    # dark shelf under the mesh, then the cutout mesh surface
    elements.append({
        "from": [1, 10, 1], "to": [15, 10.1, 15],
        "faces": {"up": face("#inner"), "down": face("#inner")},
    })
    elements.append({
        "from": [1, 10.25, 1], "to": [15, 10.4, 15],
        "faces": {f: face("#mesh") for f in ("down", "up", "north", "south", "west", "east")},
    })
    return {"parent": "minecraft:block/block", "render_type": "minecraft:cutout",
            "textures": tex, "elements": elements}


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def machine_assets(name: str, *, sieve: bool, fuel: bool) -> None:
    write_json(ASSETS / "blockstates" / f"{name}.json",
               {"variants": {"": {"model": f"exnihilosatellitium:block/{name}"}}})
    if sieve:
        model = sieve_model(fuel=fuel)
    else:
        model = {"parent": "minecraft:block/cube_all",
                 "textures": {"all": f"exnihilosatellitium:block/{name}"}}
    write_json(ASSETS / "models" / "block" / f"{name}.json", model)
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
        machine_assets(name, sieve=sieve, fuel=fuel)

    for kind in ("speed", "fortune"):
        for tier in (1, 2, 3):
            name = f"{kind}_upgrade_{tier}"
            upgrade_texture(kind, tier).save(
                ASSETS / "textures" / "item" / f"{name}.png")
            write_json(ASSETS / "models" / "item" / f"{name}.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"exnihilosatellitium:item/{name}"}})

    sieve_side_texture(fuel=False).save(
        ASSETS / "textures" / "block" / "machine_frame_side_electric.png")
    sieve_side_texture(fuel=True).save(
        ASSETS / "textures" / "block" / "machine_frame_side_fuel.png")
    sieve_top_texture().save(ASSETS / "textures" / "block" / "machine_frame_top.png")
    sieve_leg_texture().save(ASSETS / "textures" / "block" / "machine_leg.png")
    sieve_mesh_texture().save(ASSETS / "textures" / "block" / "machine_mesh.png")
    sieve_inner_texture().save(ASSETS / "textures" / "block" / "machine_inner.png")

    gui_texture(electric=True).save(
        ASSETS / "textures" / "gui" / "electric_machine.png")
    gui_texture(electric=False).save(
        ASSETS / "textures" / "gui" / "fuel_machine.png")
    gauge_fill_sprite((79, 195, 247), (21, 101, 192)).save(
        ASSETS / "textures" / "gui" / "energy_fill.png")
    gauge_fill_sprite((255, 213, 79), (230, 81, 0)).save(
        ASSETS / "textures" / "gui" / "fuel_fill.png")
    bolt_icon().save(ASSETS / "textures" / "gui" / "energy_icon.png")
    flame_icon().save(ASSETS / "textures" / "gui" / "fuel_icon.png")
    arrow_track().save(ASSETS / "textures" / "gui" / "arrow_track.png")
    arrow_fill().save(ASSETS / "textures" / "gui" / "arrow_fill.png")
    print("machine assets written")


if __name__ == "__main__":
    main()
