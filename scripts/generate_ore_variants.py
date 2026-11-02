#!/usr/bin/env python3
"""Generate the ore-chain textures that datagen cannot express.

Datagen owns every vanilla-typed file (`./gradlew runData`): blockstates, block and
item models, recipes, loot tables and tags. This script only writes what is left:

  assets/exnihilosatellitium/textures/item/<m>_<stage>.png   (copied from art/items)
  assets/exnihilosatellitium/textures/item/<m>_ingot.png     (own ingots only)
  data/exnihilosatellitium/hammer/<m>_ore_<kind>.json        (hammer crush table)
  lang entries merged into en_us.json / zh_cn.json

`materials.json` stays the single input; adding a material still needs a restart
(the table decides registration too).

    python3 scripts/generate_ore_variants.py [--out DIR]
"""

import argparse
import importlib.util
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
# Read-only inputs (materials, art). Writes go to RES; --out redirects them.
SOURCE = ROOT / "src/main/resources"
RES = SOURCE
ASSETS = RES / "assets/exnihilosatellitium"
ART_ITEMS = ROOT / "art/items/ore_pieces"
ART_INGOTS = ROOT / "art/items/ingots"
MOD_ID = "exnihilosatellitium"

MATERIALS = [m for m in json.load(open(SOURCE / f"{MOD_ID}/materials.json", encoding="utf-8"))["materials"]
             if m.get("enabled", True)]
STAGES = ("broken", "crushed", "powdered")
KINDS = ("gravel", "sand", "dust")
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


def ensure_ingot_texture(mid):
    src = ART_INGOTS / f"{mid}_ingot.png"
    dst = ASSETS / "textures/item" / f"{mid}_ingot.png"
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dst)


def clear_own_ingot_assets(mid):
    """A material switched to an existing ingot: drop the now-unused icon."""
    (ASSETS / f"textures/item/{mid}_ingot.png").unlink(missing_ok=True)


def own_ingot_reachable(material):
    """Whether our own ``<id>_ingot`` item can ever be the smelting result.

    An explicit ``ingot`` means we never register one; an unconditional ``ingots``
    candidate always wins, so the fallback is unreachable there too.
    """
    explicit = material.get("ingot")
    if explicit and not explicit.startswith("#"):
        return False
    for candidate in material.get("ingots", []):
        if isinstance(candidate, dict) and candidate.get("item") and candidate.get("mod") is None:
            return False
    return True


def hammer_table(mid, kind):
    """The hand-written hammer crush rule: gravel/sand drop 4-6 pieces, dust itself."""
    if kind == "gravel":
        return {"input": f"{MOD_ID}:{mid}_ore_gravel",
                "result": f"{MOD_ID}:{mid}_crushed", "min": 4, "max": 6}
    if kind == "sand":
        return {"input": f"{MOD_ID}:{mid}_ore_sand",
                "result": f"{MOD_ID}:{mid}_powdered", "min": 4, "max": 6}
    # Hammering ore dust does not refine it further: the block simply drops
    # itself instead of being lost to a wrong tool.
    return {"input": f"{MOD_ID}:{mid}_ore_dust", "result": f"{MOD_ID}:{mid}_ore_dust"}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=Path, default=None,
                        help="write the resource tree here instead of src/main/resources")
    args = parser.parse_args(argv)

    global RES, ASSETS
    if args.out is not None:
        RES = args.out
        ASSETS = RES / "assets/exnihilosatellitium"

    # Language files are hand-edited copy: merge on top of the source file.
    lang_dir = SOURCE / "assets" / MOD_ID / "lang"
    lang_en = json.load(open(lang_dir / "en_us.json", encoding="utf-8"))
    lang_zh = json.load(open(lang_dir / "zh_cn.json", encoding="utf-8"))

    for material in MATERIALS:
        mid, en = material["id"], material["name"]["en_us"]
        zh = material["name"].get("zh_cn", en)
        for stage in STAGES:
            ensure_item_texture(mid, stage)
            lang_en.setdefault(f"item.exnihilosatellitium.{mid}_{stage}", f"{en} {stage.capitalize()} Ore")
            lang_zh.setdefault(f"item.exnihilosatellitium.{mid}_{stage}", f"{STAGE_ZH[stage]}{zh}矿")

        if own_ingot_reachable(material):
            ensure_ingot_texture(mid)
            lang_en.setdefault(f"item.exnihilosatellitium.{mid}_ingot", f"{en} Ingot")
            lang_zh.setdefault(f"item.exnihilosatellitium.{mid}_ingot", f"{zh}锭")
        else:
            clear_own_ingot_assets(mid)
            lang_en.pop(f"item.exnihilosatellitium.{mid}_ingot", None)
            lang_zh.pop(f"item.exnihilosatellitium.{mid}_ingot", None)

        for kind in KINDS:
            write_json(RES / f"data/{MOD_ID}/hammer/{mid}_ore_{kind}.json", hammer_table(mid, kind))
            lang_en.setdefault(f"block.exnihilosatellitium.{mid}_ore_{kind}",
                               f"{en} Ore {kind.capitalize()}")
            lang_zh.setdefault(f"block.exnihilosatellitium.{mid}_ore_{kind}",
                               f"{zh}矿{KIND_ZH[kind]}")

    write_json(ASSETS / "lang/en_us.json", dict(sorted(lang_en.items())))
    write_json(ASSETS / "lang/zh_cn.json", dict(sorted(lang_zh.items())))
    print(f"generated textures, hammer tables and lang for {len(MATERIALS)} materials")


if __name__ == "__main__":
    main()
