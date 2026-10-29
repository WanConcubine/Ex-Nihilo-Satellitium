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
MATS = json.load(open(RES / "exnihilosatellitium/materials.json", encoding="utf-8"))["materials"]

MESH_COLORS = {"wood": (122, 88, 52), "silk": (232, 232, 228), "iron": (188, 190, 196)}
MESH_NO = {"wood": 1, "silk": 2, "iron": 3}


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def mesh_texture(name):
    colour = MESH_COLORS[name]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for i in range(0, 16, 3):
        d.line([(i, 0), (i, 15)], fill=colour + (255,))
        d.line([(0, i), (15, i)], fill=tuple(max(0, c - 30) for c in colour) + (255,))
    img.save(ASSETS / f"textures/block/sieve_mesh_{name}.png")


def frame_model():
    """Flat frame only: four 2x3px bars on the rim, no floor (a sieve, not a crate)."""
    plank = "minecraft:block/oak_planks"
    face = {f: {"texture": "#wood"} for f in ("up", "down", "north", "south", "east", "west")}
    return {
        "parent": "minecraft:block/block",
        "textures": {"particle": plank, "wood": plank},
        "elements": [
            {"from": [0, 0, 0], "to": [16, 3, 2], "faces": face},
            {"from": [0, 0, 14], "to": [16, 3, 16], "faces": face},
            {"from": [0, 0, 2], "to": [2, 3, 14], "faces": face},
            {"from": [14, 0, 2], "to": [16, 3, 14], "faces": face},
        ],
    }


def mesh_model(name):
    tex = f"exnihilosatellitium:block/sieve_mesh_{name}"
    return {
        "render_type": "minecraft:cutout",
        "textures": {"particle": tex, "mesh": tex},
        "elements": [{
            "from": [2, 2, 2], "to": [14, 2.02, 14],
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
        "dust": [],
    }
    suffix = {"gravel": "broken", "sand": "crushed"}  # dust 输入等通用尘土方块；粉末暂由锤子产出
    inputs = {"gravel": "minecraft:gravel", "sand": "minecraft:sand"}
    count = 0
    for kind, suffix_name in suffix.items():
        outputs = [{"item": f"exnihilosatellitium:{m['id']}_{suffix_name}",
                    "chance": round(1.0 / m["rarity"], 5)} for m in MATS]
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

    print(f"sieve assets written; drop tables: 3 inputs, {count} drops")


if __name__ == "__main__":
    main()
