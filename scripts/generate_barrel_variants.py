#!/usr/bin/env python3
"""Generate the per-material barrel assets.

The barrel geometry lives in `models/block/barrel_base.json`. Every material is
a child model that only overrides the texture slots, so the cuboids and the
`display` transforms have exactly one source of truth. Blockstates, item models,
shaped recipes, loot tables and the vanilla mineable tags all come from the same
table below; adding a material is one line.

    python3 scripts/generate_barrel_variants.py

This writes into `src/main/resources`; review the diff before committing.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "exnihiloimitatio"
DATA = ROOT / "src" / "main" / "resources" / "data"
MOD_ID = "exnihiloimitatio"

# (block id, wood type). The public `barrel` id stays oak for save compatibility.
WOOD_BARRELS = (
    ("barrel", "oak"),
    ("spruce_barrel", "spruce"),
    ("birch_barrel", "birch"),
    ("jungle_barrel", "jungle"),
    ("acacia_barrel", "acacia"),
    ("dark_oak_barrel", "dark_oak"),
)

STONE_BARREL = "stone_barrel"


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def wood_textures(wood: str) -> dict[str, str]:
    # Side/top are the vanilla-sampled planks; the inside and the bottom use the
    # stripped log, which keeps a vertical grain and reads darker than the sides.
    return {
        "particle": f"minecraft:block/{wood}_planks",
        "side": f"minecraft:block/{wood}_planks",
        "top": f"minecraft:block/{wood}_planks",
        "inner": f"minecraft:block/stripped_{wood}_log",
        "bottom": f"minecraft:block/stripped_{wood}_log",
        "hoop": "minecraft:block/gray_concrete",
    }


def stone_textures() -> dict[str, str]:
    # A stone barrel is carved from stone, not masonry. The smooth-stone rim and
    # the dark bands keep it readable next to the hooped wooden barrels.
    return {
        "particle": "minecraft:block/stone",
        "side": "minecraft:block/stone",
        "top": "minecraft:block/smooth_stone",
        "inner": "minecraft:block/stone",
        "bottom": "minecraft:block/stone",
        "hoop": "minecraft:block/deepslate_tiles",
    }


def generate(name: str, textures: dict[str, str], ingredient: str) -> None:
    write_json(ASSETS / "blockstates" / f"{name}.json", {
        "variants": {
            "": {"model": f"{MOD_ID}:block/{name}"},
        },
    })

    write_json(ASSETS / "models" / "block" / f"{name}.json", {
        "parent": f"{MOD_ID}:block/barrel_base",
        "textures": textures,
    })

    write_json(ASSETS / "models" / "item" / f"{name}.json", {
        "parent": f"{MOD_ID}:block/{name}",
    })

    write_json(DATA / MOD_ID / "recipes" / f"{name}.json", {
        "type": "minecraft:crafting_shaped",
        "pattern": [
            "PPP",
            "P P",
            "PPP",
        ],
        "key": {
            "P": {"item": ingredient},
        },
        "result": {
            "item": f"{MOD_ID}:{name}",
        },
    })

    write_json(DATA / MOD_ID / "loot_tables" / "blocks" / f"{name}.json", {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1.0,
                "entries": [
                    {"type": "minecraft:item", "name": f"{MOD_ID}:{name}"},
                ],
                "conditions": [
                    {"condition": "minecraft:survives_explosion"},
                ],
            },
        ],
    })


def main() -> None:
    base = ASSETS / "models" / "block" / "barrel_base.json"
    if not base.exists():
        raise SystemExit(f"missing shared geometry: {base}")

    for name, wood in WOOD_BARRELS:
        generate(name, wood_textures(wood), f"minecraft:{wood}_planks")
    generate(STONE_BARREL, stone_textures(), "minecraft:stone")

    write_json(DATA / "minecraft" / "tags" / "blocks" / "mineable" / "axe.json", {
        "replace": False,
        "values": [f"{MOD_ID}:{name}" for name, _ in WOOD_BARRELS],
    })
    write_json(DATA / "minecraft" / "tags" / "blocks" / "mineable" / "pickaxe.json", {
        "replace": False,
        "values": [f"{MOD_ID}:{STONE_BARREL}"],
    })

    print(f"generated {len(WOOD_BARRELS)} wood barrels + 1 stone barrel")


if __name__ == "__main__":
    main()
