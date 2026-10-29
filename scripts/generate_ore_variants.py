#!/usr/bin/env python3
"""Generate ore-chain assets from materials.json + art/items/ore_pieces.

Outputs (all under src/main/resources):
  assets/exnihilosatellitium/textures/item/<m>_<stage>.png     (copied from art)
  assets/exnihilosatellitium/textures/block/ore_mask_<kind>.png
  assets/exnihilosatellitium/models/block/ore_<kind>_block.json (3 shared models)
  assets/exnihilosatellitium/blockstates/<m>_ore_<kind>.json
  assets/exnihilosatellitium/models/item/<m>_ore_<kind>.json
  assets/exnihilosatellitium/models/item/<m>_<stage>.json
  data/exnihilosatellitium/loot_table/blocks/<m>_ore_<kind>.json
  data/minecraft/tags/block/mineable/shovel.json
  lang entries merged into en_us.json / zh_cn.json

    python3 scripts/generate_ore_variants.py
"""

import importlib.util
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/exnihilosatellitium"
ART_ITEMS = ROOT / "art/items/ore_pieces"
ART_BLOCKS = ROOT / "art/blocks/ore_overlay"

MATERIALS = json.load(open(RES / "exnihilosatellitium/materials.json", encoding="utf-8"))["materials"]
STAGES = ("broken", "crushed", "powdered")
KINDS = {"gravel": "gravel", "sand": "sand", "dust": "gray_concrete_powder"}
STAGE_ZH = {"broken": "破碎", "crushed": "粉碎", "powdered": "粉末"}
KIND_ZH = {"gravel": "沙砾", "sand": "沙子", "dust": "粉尘"}

_spec = importlib.util.spec_from_file_location("ore_pieces", ART_ITEMS / "generate.py")
_pieces = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_pieces)
_pieces.PALETTES.setdefault("osmium", ("#26313D", "#3F5B78", "#608FC4", "#8FB4DC", "#C4DCF4"))


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def ensure_item_texture(mid, stage):
    src = ART_ITEMS / f"{mid}_{stage}.png"
    dst = ASSETS / "textures/item" / f"{mid}_{stage}.png"
    dst.parent.mkdir(parents=True, exist_ok=True)
    if src.exists():
        shutil.copyfile(src, dst)
    else:  # osmium 等在 art/ 里还没有的，用同一套面具现生成并落回 art/
        img = getattr(_pieces, stage)(mid)
        img.save(src)
        img.save(dst)


def block_model(kind):
    """单层染色：一个完整 cube，所有面 tintindex 0。

    之前的“底图 + 叠加层”双层模型在世界里能染色，但物品栏里叠加层不出现，
    且底块内缩会留缝。改用最标准的单层 tint（草/树叶同一条路径）。
    """
    base = f"minecraft:block/{KINDS[kind]}"
    return {
        "parent": "minecraft:block/block",
        "textures": {"particle": base, "all": base},
        "elements": [{
            "from": [0, 0, 0], "to": [16, 16, 16],
            "faces": {f: {"texture": "#all", "tintindex": 0, "cullface": f}
                      for f in ("down", "up", "north", "south", "west", "east")},
        }],
    }


def loot_table(mid, kind):
    bid = f"{mid}_ore_{kind}"
    return {"type": "minecraft:block", "pools": [{
        "rolls": 1.0,
        "bonus_rolls": 0.0,
        "conditions": [{"condition": "minecraft:survives_explosion"}],
        "entries": [{"type": "minecraft:item", "name": f"exnihilosatellitium:{bid}"}],
    }]}


def main():
    for kind in KINDS:
        write_json(ASSETS / f"models/block/ore_{kind}_block.json", block_model(kind))

    shovel = []
    lang_en = json.load(open(ASSETS / "lang/en_us.json", encoding="utf-8"))
    lang_zh = json.load(open(ASSETS / "lang/zh_cn.json", encoding="utf-8"))

    for m in MATERIALS:
        mid, en, zh = m["id"], m["name"]["en_us"], m["name"]["zh_cn"]
        for stage in STAGES:
            ensure_item_texture(mid, stage)
            iid = f"{mid}_{stage}"
            write_json(ASSETS / f"models/item/{iid}.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"exnihilosatellitium:item/{iid}"}})
            lang_en[f"item.exnihilosatellitium.{iid}"] = f"{en} {stage.capitalize()} Ore"
            lang_zh[f"item.exnihilosatellitium.{iid}"] = f"{STAGE_ZH[stage]}{zh}矿"
        for kind, _ in KINDS.items():
            bid = f"{mid}_ore_{kind}"
            shovel.append(f"exnihilosatellitium:{bid}")
            write_json(ASSETS / f"blockstates/{bid}.json",
                       {"variants": {"": {"model": f"exnihilosatellitium:block/ore_{kind}_block"}}})
            write_json(ASSETS / f"models/item/{bid}.json",
                       {"parent": f"exnihilosatellitium:block/ore_{kind}_block"})
            write_json(RES / f"data/exnihilosatellitium/loot_table/blocks/{bid}.json",
                       loot_table(mid, kind))
            lang_en[f"block.exnihilosatellitium.{bid}"] = f"{en} Ore {kind.capitalize()}"
            lang_zh[f"block.exnihilosatellitium.{bid}"] = f"{zh}矿{KIND_ZH[kind]}"

    write_json(RES / "data/minecraft/tags/block/mineable/shovel.json",
               {"replace": False, "values": sorted(shovel)})
    write_json(ASSETS / "lang/en_us.json", dict(sorted(lang_en.items())))
    write_json(ASSETS / "lang/zh_cn.json", dict(sorted(lang_zh.items())))
    print(f"generated assets for {len(MATERIALS)} materials "
          f"({len(MATERIALS) * 3} items, {len(MATERIALS) * 3} blocks)")


if __name__ == "__main__":
    main()
