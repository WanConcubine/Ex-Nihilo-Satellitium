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

# (block id, wood type, Chinese name). The public `wooden_crucible` stays oak.
WOODS = (
    ("wooden_crucible", "oak", "木坩埚"),
    ("spruce_wooden_crucible", "spruce", "云杉木坩埚"),
    ("birch_wooden_crucible", "birch", "白桦木坩埚"),
    ("jungle_wooden_crucible", "jungle", "丛林木坩埚"),
    ("acacia_wooden_crucible", "acacia", "金合欢木坩埚"),
    ("dark_oak_wooden_crucible", "dark_oak", "深色橡木坩埚"),
)

EN = {
    "oak": "Wooden Crucible",
    "spruce": "Spruce Crucible",
    "birch": "Birch Crucible",
    "jungle": "Jungle Crucible",
    "acacia": "Acacia Crucible",
    "dark_oak": "Dark Oak Crucible",
}


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


def textures(wood: str) -> dict[str, str]:
    return {
        "particle": f"minecraft:block/{wood}_log",
        "side": f"minecraft:block/{wood}_log",
        "top": f"minecraft:block/{wood}_log_top",
        "bottom": f"minecraft:block/{wood}_log",
        "inside": f"minecraft:block/stripped_{wood}_log",
    }


def update_lang(entries: dict[str, str]) -> None:
    for lang in ("en_us", "zh_cn"):
        path = ASSETS / "lang" / f"{lang}.json"
        data = json.loads(path.read_text(encoding="utf-8"))
        data.update(entries if lang == "zh_cn" else {})
        if lang == "en_us":
            for block_id, wood, _ in WOODS:
                data[f"block.{MOD_ID}.{block_id}"] = EN[wood]
                data[f"item.{MOD_ID}.{block_id}"] = EN[wood]
        path.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n",
                        encoding="utf-8")


def main() -> None:
    ids = []
    zh: dict[str, str] = {}
    for block_id, wood, name in WOODS:
        ids.append(f"{MOD_ID}:{block_id}")
        zh[f"block.{MOD_ID}.{block_id}"] = name
        zh[f"item.{MOD_ID}.{block_id}"] = name

        write_json(ASSETS / "blockstates" / f"{block_id}.json", {
            "variants": {"": {"model": f"{MOD_ID}:block/{block_id}"}}
        })
        write_json(ASSETS / "models" / "block" / f"{block_id}.json", {
            "parent": f"{MOD_ID}:block/crucible_base",
            "textures": textures(wood),
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
                "x": {"item": f"minecraft:{wood}_log"},
                "y": {"tag": "minecraft:wooden_slabs"},
            },
            "result": {"id": f"{MOD_ID}:{block_id}"},
        })

    update_lang(zh)
    write_json(DATA / MOD_ID / "tags" / "block" / "wooden_crucibles.json",
               {"replace": False, "values": ids})
    merge_tag(DATA / "minecraft" / "tags" / "block" / "mineable" / "axe.json", ids)
    print(f"generated {len(WOODS)} wooden crucibles")


if __name__ == "__main__":
    main()
