#!/usr/bin/env python3
"""Generate the per-wood wooden crucible assets (1.21.1 data paths).

Geometry is shared with the porcelain crucible (`models/block/crucible_base.json`);
each wood variant is a child model that only overrides the texture slots with
vanilla-sampled log textures. Recipes use that wood's own log plus any wooden
slab (`#minecraft:wooden_slabs`).

    python3 scripts/generate_wooden_crucible_variants.py

This writes into `src/main/resources`; review the diff before committing.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "exnihilosatellitium"
DATA = ROOT / "src" / "main" / "resources" / "data"
MOD_ID = "exnihilosatellitium"

# (block id, log item, side texture, top texture, inside texture, zh, en).
# `wooden_crucible` keeps the plain id but is named as the oak variant.
WOODS = (
    ("wooden_crucible", "minecraft:oak_log", "oak_log", "oak_log_top",
     "stripped_oak_log", "橡木坩埚", "Oak Crucible"),
    ("spruce_wooden_crucible", "minecraft:spruce_log", "spruce_log", "spruce_log_top",
     "stripped_spruce_log", "云杉木坩埚", "Spruce Crucible"),
    ("birch_wooden_crucible", "minecraft:birch_log", "birch_log", "birch_log_top",
     "stripped_birch_log", "白桦木坩埚", "Birch Crucible"),
    ("jungle_wooden_crucible", "minecraft:jungle_log", "jungle_log", "jungle_log_top",
     "stripped_jungle_log", "丛林木坩埚", "Jungle Crucible"),
    ("acacia_wooden_crucible", "minecraft:acacia_log", "acacia_log", "acacia_log_top",
     "stripped_acacia_log", "金合欢木坩埚", "Acacia Crucible"),
    ("dark_oak_wooden_crucible", "minecraft:dark_oak_log", "dark_oak_log", "dark_oak_log_top",
     "stripped_dark_oak_log", "深色橡木坩埚", "Dark Oak Crucible"),
    # 1.20+ woods.
    ("mangrove_wooden_crucible", "minecraft:mangrove_log", "mangrove_log", "mangrove_log_top",
     "stripped_mangrove_log", "红树木坩埚", "Mangrove Crucible"),
    ("cherry_wooden_crucible", "minecraft:cherry_log", "cherry_log", "cherry_log_top",
     "stripped_cherry_log", "樱花木坩埚", "Cherry Crucible"),
    ("bamboo_wooden_crucible", "minecraft:bamboo_block", "bamboo_block", "bamboo_block_top",
     "stripped_bamboo_block", "竹坩埚", "Bamboo Crucible"),
    # Nether stems (1.16).
    ("crimson_wooden_crucible", "minecraft:crimson_stem", "crimson_stem", "crimson_stem_top",
     "stripped_crimson_stem", "绯红木坩埚", "Crimson Crucible"),
    ("warped_wooden_crucible", "minecraft:warped_stem", "warped_stem", "warped_stem_top",
     "stripped_warped_stem", "诡异木坩埚", "Warped Crucible"),
)


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def merge_tag(path: Path, values: list[str]) -> None:
    existing: list[str] = []
    if path.exists():
        existing = [v for v in json.loads(path.read_text(encoding="utf-8")).get("values", [])
                    if isinstance(v, str)]
    for value in values:
        if value not in existing:
            existing.append(value)
    write_json(path, {"replace": False, "values": existing})


def textures(side: str, top: str, inside: str) -> dict[str, str]:
    return {
        "particle": f"minecraft:block/{side}",
        "side": f"minecraft:block/{side}",
        "top": f"minecraft:block/{top}",
        "bottom": f"minecraft:block/{side}",
        "inside": f"minecraft:block/{inside}",
    }


def update_lang() -> None:
    for lang in ("en_us", "zh_cn"):
        path = ASSETS / "lang" / f"{lang}.json"
        data = json.loads(path.read_text(encoding="utf-8"))
        for block_id, _, _, _, _, zh, en in WOODS:
            name = zh if lang == "zh_cn" else en
            data[f"block.{MOD_ID}.{block_id}"] = name
            data[f"item.{MOD_ID}.{block_id}"] = name
        path.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n",
                        encoding="utf-8")


def main() -> None:
    ids = []
    for block_id, log_item, side, top, inside, _, _ in WOODS:
        ids.append(f"{MOD_ID}:{block_id}")

        write_json(ASSETS / "blockstates" / f"{block_id}.json", {
            "variants": {"": {"model": f"{MOD_ID}:block/{block_id}"}}
        })
        write_json(ASSETS / "models" / "block" / f"{block_id}.json", {
            "parent": f"{MOD_ID}:block/crucible_base",
            "textures": textures(side, top, inside),
        })
        write_json(ASSETS / "models" / "item" / f"{block_id}.json",
                   {"parent": f"{MOD_ID}:block/{block_id}"})

        write_json(DATA / MOD_ID / "loot_table" / "blocks" / f"{block_id}.json", {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1.0,
                "entries": [{"type": "minecraft:item", "name": f"{MOD_ID}:{block_id}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        })

        write_json(DATA / MOD_ID / "recipe" / f"{block_id}.json", {
            "type": "minecraft:crafting_shaped",
            "pattern": ["x x", "x x", "xyx"],
            "key": {
                "x": {"item": log_item},
                "y": {"tag": "minecraft:wooden_slabs"},
            },
            "result": {"id": f"{MOD_ID}:{block_id}"},
        })

    update_lang()
    write_json(DATA / MOD_ID / "tags" / "block" / "wooden_crucibles.json",
               {"replace": False, "values": ids})
    merge_tag(DATA / "minecraft" / "tags" / "block" / "mineable" / "axe.json", ids)
    print(f"generated {len(WOODS)} wooden crucibles")


if __name__ == "__main__":
    main()
