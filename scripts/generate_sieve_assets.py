#!/usr/bin/env python3
"""Sieve assets + sift tables from materials.json.

Writes: blockstate/multipart, frame + mesh models, mesh item models, mesh
textures (16x16 own pixels), lang entries, and data/<ns>/sieve/*.json.
"""
import json
import pathlib
from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/exnihilosatellitium"
MATS = [m for m in json.load(open(RES / "exnihilosatellitium/materials.json", encoding="utf-8"))["materials"]
        if m.get("enabled", True)]

MESH_COLORS = {"wood": (122, 88, 52), "silk": (232, 232, 228), "iron": (188, 190, 196)}
MESH_NO = {"wood": 1, "silk": 2, "iron": 3}
# 档位不是换色：编织密度不同。pitch=网格间距，strand=线宽
# 三档同结构（斜向编织），只换色；铁网没有单独贴图，按木网配色推
MESH_PITCH, MESH_STRAND = 6, 2


def rarity_of(material):
    rarity = material["rarity"]
    if isinstance(rarity, int):
        return rarity
    print(f"warning: {material['id']}: per-kind rarity object is not implemented; using rarity.gravel")
    return rarity["gravel"]


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def mesh_texture(name):
    """Diagonal woven lattice: strands along both diagonals, diamond holes between."""
    colour = MESH_COLORS[name]
    dark = tuple(max(0, c - 50) for c in colour)
    mid = tuple(max(0, c - 18) for c in colour)
    light = tuple(min(255, c + 24) for c in colour)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            a = (x + y) % MESH_PITCH
            b = (x - y) % MESH_PITCH
            on_a = a < MESH_STRAND
            on_b = b < MESH_STRAND
            if not (on_a or on_b):
                continue
            if on_a and on_b:
                c = light
            elif on_a:
                c = dark if a == MESH_STRAND - 1 else colour
            else:
                c = dark if b == MESH_STRAND - 1 else colour
            img.putpixel((x, y), c + (255,))
    img.save(ASSETS / f"textures/block/sieve_mesh_{name}.png")


def frame_model():
    """Matched to the original ModelSieve: four 1x11x1 legs, a 4px rim at y 10..14."""
    plank = "minecraft:block/oak_planks"
    face = {f: {"texture": "#wood"} for f in ("up", "down", "north", "south", "west", "east")}
    legs = [
        ([1, 0, 1], [2, 11, 2]), ([14, 0, 1], [15, 11, 2]),
        ([1, 0, 14], [2, 11, 15]), ([14, 0, 14], [15, 11, 15]),
    ]
    rim = [
        ([0, 10, 0], [16, 14, 1]), ([0, 10, 15], [16, 14, 16]),
        ([0, 10, 1], [1, 14, 15]), ([15, 10, 1], [16, 14, 15]),
    ]
    elements = [{"from": f, "to": t, "faces": face} for f, t in legs + rim]
    return {
        "parent": "minecraft:block/block",
        "textures": {"particle": plank, "wood": plank},
        "elements": elements,
    }


def mesh_model(name):
    tex = f"exnihilosatellitium:block/sieve_mesh_{name}"
    return {
        "render_type": "minecraft:cutout",
        "textures": {"particle": tex, "mesh": tex},
        "elements": [{
            "from": [1, 11.0, 1], "to": [15, 11.02, 15],
            "faces": {"up": {"texture": "#mesh"}, "down": {"texture": "#mesh"}},
        }],
    }


def main():
    for name in MESH_COLORS:
        mesh_texture(name)
        write_json(ASSETS / f"models/block/sieve_mesh_{name}.json", mesh_model(name))
    write_json(ASSETS / "models/block/sieve_frame.json", frame_model())
    write_json(ASSETS / "models/item/sieve.json", {"parent": "exnihilosatellitium:block/sieve_frame"})
    write_json(ASSETS / "blockstates/sieve.json", {"multipart": [
        {"apply": {"model": "exnihilosatellitium:block/sieve_frame"}},
        {"when": {"mesh": "1"}, "apply": {"model": "exnihilosatellitium:block/sieve_mesh_wood"}},
        {"when": {"mesh": "2"}, "apply": {"model": "exnihilosatellitium:block/sieve_mesh_silk"}},
        {"when": {"mesh": "3"}, "apply": {"model": "exnihilosatellitium:block/sieve_mesh_iron"}},
    ]})

    for name in MESH_COLORS:
        write_json(ASSETS / f"models/item/{name}_mesh.json",
                   {"parent": "minecraft:item/generated",
                    "textures": {"layer0": f"exnihilosatellitium:item/{name}_mesh"}})
        Image.open(ASSETS / f"textures/block/sieve_mesh_{name}.png").save(
            ASSETS / f"textures/item/{name}_mesh.png")

    # loot table + axe tag
    write_json(RES / "data/exnihilosatellitium/loot_table/blocks/sieve.json", {
        "type": "minecraft:block", "pools": [{
            "rolls": 1.0, "bonus_rolls": 0.0,
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": "exnihilosatellitium:sieve"}],
        }]})
    axe_path = RES / "data/minecraft/tags/block/mineable/axe.json"
    axe = json.loads(axe_path.read_text(encoding="utf-8"))
    if "exnihilosatellitium:sieve" not in axe["values"]:
        axe["values"].append("exnihilosatellitium:sieve")
        write_json(axe_path, axe)

    # sift tables
    vanilla = {
        "gravel": [("minecraft:flint", 4), ("minecraft:coal", 8), ("minecraft:lapis_lazuli", 20),
                   ("minecraft:diamond", 128), ("minecraft:emerald", 150)],
        "sand": [],
        "dust": [("minecraft:redstone", 8), ("minecraft:glowstone_dust", 12),
                 ("minecraft:gunpowder", 16)],
    }
    suffix = {"gravel": "broken", "sand": "crushed", "dust": "powdered"}
    inputs = {"gravel": "minecraft:gravel", "sand": "minecraft:sand",
              "dust": "exnihilosatellitium:dust"}
    count = 0
    for kind, suffix_name in suffix.items():
        outputs = [{"item": f"exnihilosatellitium:{m['id']}_{suffix_name}",
                    "chance": round(1.0 / rarity_of(m), 5)} for m in MATS]
        outputs += [{"item": item, "chance": round(1.0 / n, 5)} for item, n in vanilla[kind]]
        write_json(RES / f"data/exnihilosatellitium/sieve/{kind}.json",
                   {"input": inputs[kind], "outputs": outputs})
        count += len(outputs)

    # lang
    for lang, entries in {
        "zh_cn": {"block.exnihilosatellitium.sieve": "筛子",
                  "item.exnihilosatellitium.wood_mesh": "木网",
                  "item.exnihilosatellitium.silk_mesh": "丝网",
                  "item.exnihilosatellitium.iron_mesh": "铁筛网",
                  "message.exnihilosatellitium.need_mesh": "需要先放入筛网"},
        "en_us": {"block.exnihilosatellitium.sieve": "Sieve",
                  "item.exnihilosatellitium.wood_mesh": "Wooden Mesh",
                  "item.exnihilosatellitium.silk_mesh": "Silk Mesh",
                  "item.exnihilosatellitium.iron_mesh": "Iron Mesh",
                  "message.exnihilosatellitium.need_mesh": "Needs a mesh"},
    }.items():
        p = ASSETS / f"lang/{lang}.json"
        d = json.loads(p.read_text(encoding="utf-8"))
        d.update(entries)
        p.write_text(json.dumps(dict(sorted(d.items())), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(f"sieve assets written; drop tables: {len(inputs)} inputs, {count} drops")


if __name__ == "__main__":
    main()
