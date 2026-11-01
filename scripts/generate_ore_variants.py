#!/usr/bin/env python3
"""Generate ore-chain assets, recipes and tags from materials.json + art/items.

Outputs (all under src/main/resources):
  assets/exnihilosatellitium/textures/item/<m>_<stage>.png     (copied from art)
  assets/exnihilosatellitium/textures/item/<m>_ingot.png       (own ingots only)
  assets/exnihilosatellitium/models/block/ore_<kind>_block.json (3 shared models)
  assets/exnihilosatellitium/blockstates/<m>_ore_<kind>.json
  assets/exnihilosatellitium/models/item/<m>_ore_<kind>.json
  assets/exnihilosatellitium/models/item/<m>_<stage>.json
  assets/exnihilosatellitium/models/item/<m>_ingot.json
  data/exnihilosatellitium/loot_tables/blocks/<m>_ore_<kind>.json
  data/exnihilosatellitium/recipes/<m>_<stage>_to_ore_<kind>.json  (4 pieces -> block)
  data/exnihilosatellitium/recipes/<m>_ore_<kind>_smelting.json    (block -> ingot)
  data/forge/tags/items/ingots/<m>.json                        (own ingots)
  data/forge/tags/blocks|items/ores/<m>.json                   (ore blocks)
  data/minecraft/tags/blocks/mineable/shovel.json
  lang entries merged into en_us.json / zh_cn.json

`materials.json` `ingot` may name an existing item (iron/gold/copper use the
vanilla ingots). Without one (or with a `#tag` value, which is not supported at
registration time) the material gets its own `<id>_ingot` item, so the chain
always ends somewhere.

    python3 scripts/generate_ore_variants.py
"""

import importlib.util
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/exnihilosatellitium"
MOD_ID = "exnihilosatellitium"
ART_ITEMS = ROOT / "art/items/ore_pieces"
ART_INGOTS = ROOT / "art/items/ingots"
DATA = RES / "data"

MATERIALS = json.load(open(RES / f"{MOD_ID}/materials.json", encoding="utf-8"))["materials"]
STAGES = ("broken", "crushed", "powdered")
KINDS = {"gravel": "gravel", "sand": "sand", "dust": "gray_concrete_powder"}
STAGE_TO_KIND = {"broken": "gravel", "crushed": "sand", "powdered": "dust"}
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


def ensure_ingot_texture(mid):
    src = ART_INGOTS / f"{mid}_ingot.png"
    dst = ASSETS / "textures/item" / f"{mid}_ingot.png"
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dst)


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
        "entries": [{"type": "minecraft:item", "name": f"{MOD_ID}:{bid}"}],
    }]}


def tag(values):
    return {"replace": False, "values": sorted(values)}


def ingot_for(material):
    """The ingot item this material smelts into; None means "our own"."""
    value = material.get("ingot")
    if not value or value.startswith("#"):
        return None
    return value


def craft_pieces_to_block(mid, stage):
    kind = STAGE_TO_KIND[stage]
    return {
        "type": "minecraft:crafting_shaped",
        "pattern": ["xx", "xx"],
        "key": {"x": {"item": f"{MOD_ID}:{mid}_{stage}"}},
        "result": {"item": f"{MOD_ID}:{mid}_ore_{kind}"},
    }


def smelt_block_to_ingot(mid, kind, ingot):
    return {
        "type": "minecraft:smelting",
        "ingredient": {"item": f"{MOD_ID}:{mid}_ore_{kind}"},
        "result": {"item": ingot},
        "experience": 0.1,
        "cookingtime": 200,
    }


def main():
    for kind in KINDS:
        write_json(ASSETS / f"models/block/ore_{kind}_block.json", block_model(kind))

    shovel = []
    own_ingots = []
    lang_en = json.load(open(ASSETS / "lang/en_us.json", encoding="utf-8"))
    lang_zh = json.load(open(ASSETS / "lang/zh_cn.json", encoding="utf-8"))

    for m in MATERIALS:
        mid, en, zh = m["id"], m["name"]["en_us"], m["name"]["zh_cn"]
        explicit = ingot_for(m)
        ingot = explicit or f"{MOD_ID}:{mid}_ingot"
        if explicit is None:
            own_ingots.append(mid)
            ensure_ingot_texture(mid)
            write_json(ASSETS / f"models/item/{mid}_ingot.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"{MOD_ID}:item/{mid}_ingot"}})
            write_json(DATA / f"forge/tags/items/ingots/{mid}.json",
                       tag([f"{MOD_ID}:{mid}_ingot"]))
            lang_en[f"item.{MOD_ID}.{mid}_ingot"] = f"{en} Ingot"
            lang_zh[f"item.{MOD_ID}.{mid}_ingot"] = f"{zh}锭"

        for stage in STAGES:
            ensure_item_texture(mid, stage)
            iid = f"{mid}_{stage}"
            write_json(ASSETS / f"models/item/{iid}.json",
                       {"parent": "minecraft:item/generated",
                        "textures": {"layer0": f"{MOD_ID}:item/{iid}"}})
            write_json(DATA / f"{MOD_ID}/recipes/{iid}_to_ore_{STAGE_TO_KIND[stage]}.json",
                       craft_pieces_to_block(mid, stage))
            lang_en[f"item.{MOD_ID}.{iid}"] = f"{en} {stage.capitalize()} Ore"
            lang_zh[f"item.{MOD_ID}.{iid}"] = f"{STAGE_ZH[stage]}{zh}矿"

        ore_blocks = []
        for kind, _ in KINDS.items():
            bid = f"{mid}_ore_{kind}"
            ore_blocks.append(f"{MOD_ID}:{bid}")
            shovel.append(f"{MOD_ID}:{bid}")
            write_json(ASSETS / f"blockstates/{bid}.json",
                       {"variants": {"": {"model": f"{MOD_ID}:block/ore_{kind}_block"}}})
            write_json(ASSETS / f"models/item/{bid}.json",
                       {"parent": f"{MOD_ID}:block/ore_{kind}_block"})
            write_json(DATA / f"{MOD_ID}/loot_tables/blocks/{bid}.json", loot_table(mid, kind))
            write_json(DATA / f"{MOD_ID}/recipes/{bid}_smelting.json",
                       smelt_block_to_ingot(mid, kind, ingot))
            lang_en[f"block.{MOD_ID}.{bid}"] = f"{en} Ore {kind.capitalize()}"
            lang_zh[f"block.{MOD_ID}.{bid}"] = f"{zh}矿{KIND_ZH[kind]}"

        write_json(DATA / f"forge/tags/blocks/ores/{mid}.json", tag(ore_blocks))
        write_json(DATA / f"forge/tags/items/ores/{mid}.json", tag(ore_blocks))

    # Ore blocks only: the ore pieces are intermediates and must not be tagged as
    # raw materials, or other mods would bypass the 4-pieces -> block -> smelt chain.
    subtags = [f"#forge:ores/{m['id']}" for m in MATERIALS]
    write_json(DATA / "forge/tags/blocks/ores.json", tag(subtags))
    write_json(DATA / "forge/tags/items/ores.json", tag(subtags))

    write_json(DATA / "minecraft/tags/blocks/mineable/shovel.json",
               {"replace": False, "values": sorted(shovel)})
    write_json(ASSETS / "lang/en_us.json", dict(sorted(lang_en.items())))
    write_json(ASSETS / "lang/zh_cn.json", dict(sorted(lang_zh.items())))
    print(f"generated assets for {len(MATERIALS)} materials "
          f"({len(MATERIALS) * 3} items, {len(MATERIALS) * 3} blocks, "
          f"{len(own_ingots)} own ingots)")


if __name__ == "__main__":
    main()
