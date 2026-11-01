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


def hammer_anvil_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(62, 68, 74))
    draw.rectangle((1, 1, 14, 14), fill=(88, 96, 103))
    draw.rectangle((2, 2, 13, 6), fill=(120, 129, 136))
    draw.line((2, 7, 13, 7), fill=(48, 53, 58))
    return img


def hammer_head_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(74, 81, 88))
    draw.rectangle((1, 1, 14, 14), fill=(168, 177, 184))
    draw.rectangle((2, 2, 13, 6), fill=(210, 218, 224))
    draw.line((2, 13, 13, 13), fill=(120, 128, 135))
    draw.line((2, 7, 13, 7), fill=(140, 149, 156))
    return img


def hammer_top_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(55, 61, 66))
    draw.rectangle((1, 1, 14, 14), fill=(150, 159, 166))
    draw.rectangle((3, 3, 12, 12), fill=(126, 134, 141))
    draw.line((1, 1, 14, 1), fill=(203, 210, 215))
    draw.point((2, 2), fill=(220, 226, 230))
    draw.point((13, 2), fill=(220, 226, 230))
    draw.point((2, 13), fill=(220, 226, 230))
    draw.point((13, 13), fill=(220, 226, 230))
    return img


def hammer_shaft_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(96, 104, 111))
    for y in (2, 6, 10, 14):
        draw.line((0, y, 15, y), fill=(70, 77, 83))
        draw.line((0, y + 1, 15, y + 1), fill=(140, 149, 156))
    return img


def sieve_inner_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 15, 15), fill=(118, 126, 133))
    for x in range(1, 16, 4):
        draw.line((x, 0, x, 15), fill=(102, 110, 117))
    draw.line((0, 0, 15, 0), fill=(150, 158, 165))
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

    side_uv = [0, 0, 16, 16]
    rim_uv = [0, 0, 16, 6]
    elements = []
    # thin 1x1 legs (0..10), inset 1 px so adjacent machines never share a plane
    for x, z in ((1, 1), (14, 1), (1, 14), (14, 14)):
        elements.append({
            "from": [x, 0, z], "to": [x + 1, 8.95, z + 1],
            "faces": {f: face("#leg") for f in ("down", "up", "north", "south", "west", "east")},
        })
    # thin tray bottom 9..10
    elements.append({
        "from": [1, 9, 1], "to": [15, 10, 15],
        "faces": {
            "down": face("#side"), "up": face("#inner"),
            "north": face("#side"), "south": face("#side"),
            "west": face("#side"), "east": face("#side"),
        },
    })
    # thin 4 px tray walls 10..14, mesh at the tray bottom
    walls = (
        ("north", [1, 10, 1, 15, 14, 3]),
        ("south", [1, 10, 13, 15, 14, 15]),
        ("west", [1, 10, 3, 3, 14, 13]),
        ("east", [13, 10, 3, 15, 14, 13]),
    )
    for outer, box in walls:
        faces = {f: face("#side") for f in ("down", "up", "north", "south", "west", "east")}
        faces["up"] = face("#top")
        faces[outer] = face("#side", side_uv)
        inner = {"north": "south", "south": "north", "west": "east", "east": "west"}[outer]
        faces[inner] = face("#inner")
        elements.append({"from": box[:3], "to": box[3:], "faces": faces})
    elements.append({
        "from": [3, 10.2, 3], "to": [13, 10.35, 13],
        "faces": {f: face("#mesh") for f in ("down", "up", "north", "south", "west", "east")},
    })
    return {"parent": "minecraft:block/block", "render_type": "minecraft:cutout",
            "textures": tex, "elements": elements}


def machine_side_32(*, fuel: bool) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    edge = RUST_DARK if fuel else EDGE
    plate = RUST if fuel else PLATE
    band = (176, 58, 46) if not fuel else (176, 90, 30)
    draw.rectangle((0, 0, 31, 31), fill=edge)
    draw.rectangle((2, 2, 29, 29), fill=plate)
    draw.line((2, 2, 29, 2), fill=(190, 198, 204))
    draw.line((2, 2, 2, 29), fill=(190, 198, 204))
    draw.line((29, 3, 29, 29), fill=(70, 77, 83))
    draw.line((3, 29, 29, 29), fill=(70, 77, 83))
    # panel seam
    draw.rectangle((5, 5, 26, 21), outline=(88, 96, 103))
    draw.rectangle((6, 6, 25, 20), outline=(150, 159, 166))
    # corner bolts
    for x, y in ((4, 4), (27, 4), (4, 27), (27, 27)):
        draw.rectangle((x, y, x + 1, y + 1), fill=(210, 218, 224))
        draw.point((x + 1, y + 1), fill=(70, 77, 83))
    # cooling vents in the upper panel
    for y in (9, 14, 19):
        draw.rectangle((8, y, 23, y + 2), fill=(58, 64, 70))
        draw.line((8, y, 23, y), fill=(36, 40, 44))
        draw.line((8, y + 2, 23, y + 2), fill=(150, 159, 166))
    # bottom band with hazard dashes
    draw.rectangle((0, 26, 31, 30), fill=band)
    draw.line((0, 26, 31, 26), fill=(255, 196, 170) if not fuel else (255, 214, 150))
    draw.line((0, 30, 31, 30), fill=(60, 20, 16))
    for x in range(2, 32, 8):
        draw.line((x, 28, x + 3, 28), fill=(255, 226, 170))
    return img


def machine_top_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(55, 61, 66))
    draw.rectangle((2, 2, 29, 29), fill=(160, 169, 176))
    draw.rectangle((5, 5, 26, 26), fill=(128, 137, 144))
    draw.line((2, 2, 29, 2), fill=(210, 218, 224))
    # raised center plate with diagonal brace
    draw.rectangle((10, 10, 21, 21), fill=(96, 104, 111))
    draw.rectangle((11, 11, 20, 20), fill=(140, 149, 156))
    draw.line((11, 11, 20, 20), fill=(78, 85, 91))
    draw.line((11, 20, 20, 11), fill=(78, 85, 91))
    for x, y in ((3, 3), (27, 3), (3, 27), (27, 27)):
        draw.rectangle((x, y, x + 1, y + 1), fill=(228, 234, 238))
    return img


def machine_leg_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(46, 51, 56))
    for x in (3, 11, 19, 27):
        draw.line((x, 0, x, 31), fill=(34, 38, 42))
        draw.line((x + 1, 0, x + 1, 31), fill=(72, 79, 85))
    return img


def machine_mesh_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for x in range(0, 32, 4):
        draw.line((x, 0, x, 31), fill=(202, 210, 216, 255))
    for y in range(0, 32, 4):
        draw.line((0, y, 31, y), fill=(178, 187, 194, 255))
    return img


def machine_inner_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(118, 126, 133))
    for x in range(2, 32, 6):
        draw.line((x, 1, x, 30), fill=(100, 108, 115))
    draw.line((0, 0, 31, 0), fill=(150, 158, 165))
    return img


def hammer_anvil_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(62, 68, 74))
    draw.rectangle((2, 2, 29, 29), fill=(88, 96, 103))
    draw.rectangle((4, 4, 27, 12), fill=(120, 129, 136))
    draw.line((4, 14, 27, 14), fill=(48, 53, 58))
    draw.line((4, 4, 27, 4), fill=(160, 169, 176))
    return img


def hammer_head_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(74, 81, 88))
    draw.rectangle((2, 2, 29, 29), fill=(168, 177, 184))
    draw.rectangle((4, 4, 27, 12), fill=(210, 218, 224))
    draw.rectangle((4, 16, 27, 20), fill=(96, 104, 111))
    draw.line((4, 14, 27, 14), fill=(120, 128, 135))
    draw.line((4, 26, 27, 26), fill=(120, 128, 135))
    draw.point((5, 27), fill=(74, 81, 88))
    draw.point((26, 5), fill=(232, 240, 246))
    return img


def hammer_shaft_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(96, 104, 111))
    for y in (4, 12, 20, 28):
        draw.line((0, y, 31, y), fill=(70, 77, 83))
        draw.line((0, y + 1, 31, y + 1), fill=(140, 149, 156))
    return img


def hammer_top_32() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 31, 31), fill=(55, 61, 66))
    draw.rectangle((2, 2, 29, 29), fill=(150, 159, 166))
    draw.rectangle((6, 6, 25, 25), fill=(126, 134, 141))
    draw.line((2, 2, 29, 2), fill=(203, 210, 215))
    for x, y in ((4, 4), (27, 4), (4, 27), (27, 27)):
        draw.rectangle((x, y, x + 1, y + 1), fill=(228, 234, 238))
    return img


def hammer_model(*, fuel: bool, head: bool = True) -> dict:
    side = f"exnihilosatellitium:block/machine_frame_side_{'fuel' if fuel else 'electric'}"
    tex = {
        "particle": side,
        "side": side,
        "top": "exnihilosatellitium:block/machine_hammer_top",
        "leg": "exnihilosatellitium:block/machine_leg",
        "anvil": "exnihilosatellitium:block/machine_hammer_anvil",
        "head": "exnihilosatellitium:block/machine_hammer_head",
        "shaft": "exnihilosatellitium:block/machine_hammer_shaft",
        "inner": "exnihilosatellitium:block/machine_inner",
    }

    def face(texture):
        return {"texture": texture}

    def box(frm, to, texture, top=None):
        return {"from": frm, "to": to,
                "faces": {f: face(texture) for f in ("down", "up", "north", "south", "west", "east")}}

    elements = [
        box([1, 0, 1], [15, 4, 15], "#side"),
        box([2, 4, 2], [4, 12, 4], "#leg"),
        box([12, 4, 2], [14, 12, 4], "#leg"),
        box([2, 4, 12], [4, 12, 14], "#leg"),
        box([12, 4, 12], [14, 12, 14], "#leg"),
        box([2, 12, 2], [14, 14, 14], "#top"),
        box([5, 4, 5], [11, 6, 11], "#anvil"),
        box([7, 10, 7], [9, 12, 9], "#shaft"),
    ]
    if head:
        elements.append(box([5, 7, 5], [11, 10, 11], "#head"))
    # top plate should use top texture on the up face only
    elements[5]["faces"]["up"] = face("#top")
    return {"parent": "minecraft:block/block", "textures": tex, "elements": elements}


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def machine_assets(name: str, *, sieve: bool, fuel: bool) -> None:
    write_json(ASSETS / "blockstates" / f"{name}.json",
               {"variants": {"": {"model": f"exnihilosatellitium:block/{name}"}}})
    if sieve:
        write_json(ASSETS / "models" / "block" / f"{name}.json", sieve_model(fuel=fuel))
        write_json(ASSETS / "models" / "item" / f"{name}.json",
                   {"parent": f"exnihilosatellitium:block/{name}"})
    else:
        # the block model has no head (the BER renders the moving head), but the
        # item model keeps a static head so the icon is not an empty press
        write_json(ASSETS / "models" / "block" / f"{name}.json",
                   hammer_model(fuel=fuel, head=False))
        write_json(ASSETS / "models" / "item" / f"{name}.json",
                   hammer_model(fuel=fuel, head=True))


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

    machine_side_32(fuel=False).save(
        ASSETS / "textures" / "block" / "machine_frame_side_electric.png")
    machine_side_32(fuel=True).save(
        ASSETS / "textures" / "block" / "machine_frame_side_fuel.png")
    machine_top_32().save(ASSETS / "textures" / "block" / "machine_frame_top.png")
    machine_leg_32().save(ASSETS / "textures" / "block" / "machine_leg.png")
    machine_mesh_32().save(ASSETS / "textures" / "block" / "machine_mesh.png")
    machine_inner_32().save(ASSETS / "textures" / "block" / "machine_inner.png")
    hammer_anvil_32().save(ASSETS / "textures" / "block" / "machine_hammer_anvil.png")
    hammer_head_32().save(ASSETS / "textures" / "block" / "machine_hammer_head.png")
    hammer_shaft_32().save(ASSETS / "textures" / "block" / "machine_hammer_shaft.png")
    hammer_top_32().save(ASSETS / "textures" / "block" / "machine_hammer_top.png")

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
