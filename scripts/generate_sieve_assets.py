#!/usr/bin/env python3
"""Sieve textures + the hand-written sift tables.

Datagen owns the blockstate (multipart), the frame/mesh models and the item
models (`./gradlew runData`). This script writes the rest:

  textures/block/sieve_mesh_<name>.png   16x16 own pixels
  textures/item/<name>_mesh.png          icon derived from the mesh texture
  data/<ns>/sieve/*.json                 sift tables merged from materials.json
  lang entries merges

    python3 scripts/generate_sieve_assets.py [--out DIR]

`--out` writes a preview tree; the sift tables merge on top of whatever is there
(so preview on an empty dir produces only the generated drops).
"""
import argparse
import json
import pathlib

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[1]
# Read-only inputs. Writes go to RES; --out redirects them.
SOURCE = ROOT / "src/main/resources"
RES = SOURCE
ASSETS = RES / "assets/exnihilosatellitium"
MOD_ID = "exnihilosatellitium"

MATS = [m for m in json.load(open(SOURCE / f"{MOD_ID}/materials.json", encoding="utf-8"))["materials"]
        if m.get("enabled", True)]

MESH_COLORS = {"wood": (122, 88, 52), "silk": (232, 232, 228), "iron": (188, 190, 196)}
# 档位不是换色：编织密度不同。pitch=网格间距，strand=线宽
# 三档同结构（斜向编织），只换色；铁网没有单独贴图，按木网配色推
MESH_PITCH, MESH_STRAND = 6, 2


def rarity_of(material):
    rarity = material["rarity"]
    if isinstance(rarity, int):
        return rarity
    print(f"warning: {material['id']}: per-kind rarity object is not implemented; using rarity.gravel")
    return rarity["gravel"]


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
    out = ASSETS / f"textures/block/sieve_mesh_{name}.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out)


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def merge_table(path, input_id, generated, suffix, material_ids):
    """Merge generated drops into an existing table.

    Hand-written drops (the M2/M6 extras) stay where they are; a generated item
    keeps its position but takes the new chance; material pieces whose material
    disappeared from materials.json are pruned. Re-running therefore produces
    no diff on a tree that was already generated.
    """
    existing = []
    if path.exists():
        existing = json.loads(path.read_text(encoding="utf-8")).get("outputs", [])
    by_item = {entry["item"]: entry for entry in generated}
    output, seen = [], set()
    for entry in existing:
        item = entry.get("item", "")
        if item in by_item:
            output.append(by_item[item])
            seen.add(item)
            continue
        if item.startswith(f"{MOD_ID}:") and item.endswith(f"_{suffix}"):
            base = item[len(MOD_ID) + 1:-(len(suffix) + 1)]
            if base not in material_ids:
                continue
        output.append(entry)
    for item, entry in by_item.items():
        if item not in seen:
            output.append(entry)
    write_json(path, {"input": input_id, "outputs": output})


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=pathlib.Path, default=None,
                        help="write the resource tree here instead of src/main/resources")
    args = parser.parse_args(argv)

    global RES, ASSETS
    if args.out is not None:
        RES = args.out
        ASSETS = RES / "assets/exnihilosatellitium"

    for name in MESH_COLORS:
        mesh_texture(name)
        Image.open(ASSETS / f"textures/block/sieve_mesh_{name}.png").save(
            ASSETS / f"textures/item/{name}_mesh.png")

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
        merge_table(RES / f"data/{MOD_ID}/sieve/{kind}.json", inputs[kind], outputs,
                    suffix_name, {m["id"] for m in MATS})
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
        # Merge on top of the source language file so --out previews keep the
        # hand-written entries.
        src = SOURCE / f"assets/{MOD_ID}/lang/{lang}.json"
        d = json.loads(src.read_text(encoding="utf-8"))
        # Language files are hand-edited copy: only fill missing keys.
        for key, value in entries.items():
            d.setdefault(key, value)
        p = ASSETS / f"lang/{lang}.json"
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(dict(sorted(d.items())), ensure_ascii=False, indent=2) + "\n",
                     encoding="utf-8")

    print(f"sieve assets written; drop tables: {len(inputs)} inputs, {count} drops")


if __name__ == "__main__":
    main()
