#!/usr/bin/env python3
"""Generate ore-chain assets and recipes from materials.json + art/items.

Outputs (all under src/main/resources):
  assets/exnihilosatellitium/textures/item/<m>_<stage>.png     (copied from art)
  assets/exnihilosatellitium/textures/item/<m>_ingot.png       (own ingots only)
  assets/exnihilosatellitium/models/block/ore_<kind>_block.json (3 shared models)
  assets/exnihilosatellitium/blockstates/<m>_ore_<kind>.json
  assets/exnihilosatellitium/models/item/<m>_ore_<kind>.json
  assets/exnihilosatellitium/models/item/<m>_<stage>.json
  assets/exnihilosatellitium/models/item/<m>_ingot.json
  data/exnihilosatellitium/loot_table/blocks/<m>_ore_<kind>.json
  data/exnihilosatellitium/recipe/<m>_<stage>_to_ore_<kind>.json   (4 pieces -> block)
  data/exnihilosatellitium/recipe/<m>_ore_<kind>_smelting.json     (block -> ingot)
  data/c/tags/item/ingots/<m>.json                              (own ingots)
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
ART_INGOTS = ROOT / "art/items/ingots"
ART_BLOCKS = ROOT / "art/blocks/ore_overlay"

MATERIALS = [m for m in json.load(open(RES / "exnihilosatellitium/materials.json", encoding="utf-8"))["materials"]
             if m.get("enabled", True)]
STAGES = ("broken", "crushed", "powdered")
KINDS = {"gravel": "gravel", "sand": "sand", "dust": "gray_concrete_powder"}
STAGE_ZH = {"broken": "破碎", "crushed": "粉碎", "powdered": "粉末"}
KIND_ZH = {"gravel": "沙砾", "sand": "沙子", "dust": "粉尘"}
STAGE_TO_KIND = {"broken": "gravel", "crushed": "sand", "powdered": "dust"}

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


def tag(values):
    return {"replace": False, "values": sorted(values)}


def own_ingot_id(mid):
    return f"exnihilosatellitium:{mid}_ingot"


def crafting_pieces_to_block(mid, stage):
    """Classic 2x2: four broken/crushed/powdered pieces make one ore block."""
    kind = STAGE_TO_KIND[stage]
    return {
        "type": "minecraft:crafting_shaped",
        "pattern": ["xx", "xx"],
        "key": {"x": {"item": f"exnihilosatellitium:{mid}_{stage}"}},
        "result": {"id": f"exnihilosatellitium:{mid}_ore_{kind}"},
    }


def smelting_block_to_ingot(mid, kind, ingot_id):
    return {
        "type": "minecraft:smelting",
        "ingredient": {"item": f"exnihilosatellitium:{mid}_ore_{kind}"},
        "result": {"id": ingot_id},
        "experience": 0.1,
        "cookingtime": 200,
    }


def ensure_ingot_texture(mid):
    src = ART_INGOTS / f"{mid}_ingot.png"
    dst = ASSETS / "textures/item" / f"{mid}_ingot.png"
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dst)


def clear_own_ingot_assets(mid):
    """A material switched to an existing ingot: drop the now-unused assets."""
    for path in (
        ASSETS / f"textures/item/{mid}_ingot.png",
        ASSETS / f"models/item/{mid}_ingot.json",
        RES / f"data/c/tags/item/ingots/{mid}.json",
    ):
        path.unlink(missing_ok=True)


def main():
    for kind in KINDS:
        write_json(ASSETS / f"models/block/ore_{kind}_block.json", block_model(kind))

    shovel = ["exnihilosatellitium:dust"]
    lang_en = json.load(open(ASSETS / "lang/en_us.json", encoding="utf-8"))
    lang_zh = json.load(open(ASSETS / "lang/zh_cn.json", encoding="utf-8"))

    for m in MATERIALS:
        mid, en = m["id"], m["name"]["en_us"]
        zh = m["name"].get("zh_cn", en)
        existing_ingot = m.get("ingot")
        if existing_ingot and existing_ingot.startswith("#"):
            print(f"warning: {mid}: tag ingot {existing_ingot} is not supported; using own ingot")
            existing_ingot = None
        for stage in STAGES:
            ensure_item_texture(mid, stage)
            iid = f"{mid}_{stage}"
            write_json(ASSETS / f"models/item/{iid}.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"exnihilosatellitium:item/{iid}"}})
            lang_en[f"item.exnihilosatellitium.{iid}"] = f"{en} {stage.capitalize()} Ore"
            lang_zh[f"item.exnihilosatellitium.{iid}"] = f"{STAGE_ZH[stage]}{zh}矿"
            write_json(RES / f"data/exnihilosatellitium/recipe/{iid}_to_ore_{STAGE_TO_KIND[stage]}.json",
                       crafting_pieces_to_block(mid, stage))

        if existing_ingot:
            ingot_id = existing_ingot
            clear_own_ingot_assets(mid)
            lang_en.pop(f"item.exnihilosatellitium.{mid}_ingot", None)
            lang_zh.pop(f"item.exnihilosatellitium.{mid}_ingot", None)
        else:
            ingot_id = own_ingot_id(mid)
            ensure_ingot_texture(mid)
            iid = f"{mid}_ingot"
            write_json(ASSETS / f"models/item/{iid}.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"exnihilosatellitium:item/{iid}"}})
            write_json(RES / f"data/c/tags/item/ingots/{mid}.json",
                       {"replace": False, "values": [own_ingot_id(mid)]})
            lang_en[f"item.exnihilosatellitium.{iid}"] = f"{en} Ingot"
            lang_zh[f"item.exnihilosatellitium.{iid}"] = f"{zh}锭"

        ore_blocks = []
        for kind, _ in KINDS.items():
            bid = f"{mid}_ore_{kind}"
            ore_blocks.append(f"exnihilosatellitium:{bid}")
            shovel.append(f"exnihilosatellitium:{bid}")
            write_json(ASSETS / f"blockstates/{bid}.json",
                       {"variants": {"": {"model": f"exnihilosatellitium:block/ore_{kind}_block"}}})
            write_json(ASSETS / f"models/item/{bid}.json",
                       {"parent": f"exnihilosatellitium:block/ore_{kind}_block"})
            write_json(RES / f"data/exnihilosatellitium/loot_table/blocks/{bid}.json",
                       loot_table(mid, kind))
            write_json(RES / f"data/exnihilosatellitium/recipe/{bid}_smelting.json",
                       smelting_block_to_ingot(mid, kind, ingot_id))
            if kind == "gravel":
                write_json(RES / f"data/exnihilosatellitium/hammer/{mid}_ore_gravel.json",
                           {"input": f"exnihilosatellitium:{mid}_ore_gravel",
                            "result": f"exnihilosatellitium:{mid}_crushed",
                            "min": 4, "max": 6})
            elif kind == "sand":
                write_json(RES / f"data/exnihilosatellitium/hammer/{mid}_ore_sand.json",
                           {"input": f"exnihilosatellitium:{mid}_ore_sand",
                            "result": f"exnihilosatellitium:{mid}_powdered",
                            "min": 4, "max": 6})
            elif kind == "dust":
                # Hammering ore dust does not refine it further: the block
                # simply drops itself instead of being lost to a wrong tool.
                write_json(RES / f"data/exnihilosatellitium/hammer/{mid}_ore_dust.json",
                           {"input": f"exnihilosatellitium:{mid}_ore_dust",
                            "result": f"exnihilosatellitium:{mid}_ore_dust"})
            lang_en[f"block.exnihilosatellitium.{bid}"] = f"{en} Ore {kind.capitalize()}"
            lang_zh[f"block.exnihilosatellitium.{bid}"] = f"{zh}矿{KIND_ZH[kind]}"

        # Ore blocks only: 1.7.10 Ex Nihilo registers its gravel/sand/dust ore
        # blocks with TE/TC, so `c:ores` is faithful. `c:raw_materials` is the
        # 1.17+ "raw ore" concept and must NOT contain our broken/crushed/powdered
        # intermediates — other mods would treat them as raw ore and bypass the
        # 4-pieces -> block -> smelt chain.
        write_json(RES / f"data/c/tags/block/ores/{mid}.json", tag(ore_blocks))
        write_json(RES / f"data/c/tags/item/ores/{mid}.json", tag(ore_blocks))

    write_json(RES / "data/minecraft/tags/block/mineable/shovel.json",
               {"replace": False, "values": sorted(shovel)})

    # Root tags: NeoForge's own root only lists vanilla metals, so append our
    # per-material subtags for mods that look up the whole tag.
    subtags = [f"#c:ores/{m['id']}" for m in MATERIALS]
    write_json(RES / "data/c/tags/block/ores.json", tag(subtags))
    write_json(RES / "data/c/tags/item/ores.json", tag(subtags))

    # Drop tags from older generator versions: raw_materials is NeoForge's root
    # tag and our pieces do not belong in it.
    shutil.rmtree(RES / "data/c/tags/item/raw_materials", ignore_errors=True)
    raw_root = RES / "data/c/tags/item/raw_materials.json"
    if raw_root.exists():
        raw_root.unlink()
    write_json(ASSETS / "lang/en_us.json", dict(sorted(lang_en.items())))
    write_json(ASSETS / "lang/zh_cn.json", dict(sorted(lang_zh.items())))
    print(f"generated assets for {len(MATERIALS)} materials "
          f"({len(MATERIALS) * 3} ore pieces, {len(MATERIALS) * 3} blocks) "
          f"+ {len(MATERIALS) * 6} recipes")


if __name__ == "__main__":
    main()
