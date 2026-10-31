#!/usr/bin/env python3
"""Generate the per-material barrel assets (1.21.1 data paths).

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
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "exnihilosatellitium"
DATA = ROOT / "src" / "main" / "resources" / "data"
MOD_ID = "exnihilosatellitium"

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

# 1.16/1.20 woods. Bamboo has no stripped log and the nether woods use stems,
# so each entry carries its own stripped texture.
EXTRA_BARRELS = (
    ("mangrove_barrel", "mangrove", "stripped_mangrove_log", "红树木桶", "Mangrove Barrel"),
    ("cherry_barrel", "cherry", "stripped_cherry_log", "樱花木桶", "Cherry Barrel"),
    ("bamboo_barrel", "bamboo", "stripped_bamboo_block", "竹桶", "Bamboo Barrel"),
    ("crimson_barrel", "crimson", "stripped_crimson_stem", "绯红木桶", "Crimson Barrel"),
    ("warped_barrel", "warped", "stripped_warped_stem", "诡异木桶", "Warped Barrel"),
)


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def merge_tag(path: Path, values: list[str]) -> None:
    """Keep tags written by other generators (e.g. the sieve's axe entry)."""
    existing: list[str] = []
    if path.exists():
        data = json.loads(path.read_text(encoding="utf-8"))
        existing = [value for value in data.get("values", []) if isinstance(value, str)]
    for value in values:
        if value not in existing:
            existing.append(value)
    write_json(path, {"replace": False, "values": existing})


def wood_textures(wood: str, stripped: str | None = None) -> dict[str, str]:
    # Side/top are the vanilla-sampled planks; the inside and the bottom use the
    # stripped log, which keeps a vertical grain and reads darker than the sides.
    inner = stripped or f"stripped_{wood}_log"
    return {
        "particle": f"minecraft:block/{wood}_planks",
        "side": f"minecraft:block/{wood}_planks",
        "top": f"minecraft:block/{wood}_planks",
        "inner": f"minecraft:block/{inner}",
        "bottom": f"minecraft:block/{inner}",
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
    }


def generate(name: str, textures: dict[str, str], planks: str, slab: str) -> None:
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

    # Classic barrel: four corner planks plus a slab floor.
    write_json(DATA / MOD_ID / "recipe" / f"{name}.json", {
        "type": "minecraft:crafting_shaped",
        "pattern": [
            "x x",
            "x x",
            "xyx",
        ],
        "key": {
            "x": {"item": planks},
            "y": {"item": slab},
        },
        "result": {
            "id": f"{MOD_ID}:{name}",
        },
    })

    write_json(DATA / MOD_ID / "loot_table" / "blocks" / f"{name}.json", {
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
        generate(
            name,
            wood_textures(wood),
            f"minecraft:{wood}_planks",
            f"minecraft:{wood}_slab",
        )
    for name, wood, stripped, _, _ in EXTRA_BARRELS:
        generate(name, wood_textures(wood, stripped),
                 f"minecraft:{wood}_planks", f"minecraft:{wood}_slab")
    generate(STONE_BARREL, stone_textures(), "minecraft:stone", "minecraft:smooth_stone_slab")

    merge_tag(
        DATA / "minecraft" / "tags" / "block" / "mineable" / "axe.json",
        [f"{MOD_ID}:{name}" for name, _ in WOOD_BARRELS]
        + [f"{MOD_ID}:{name}" for name, _, _, _, _ in EXTRA_BARRELS],
    )
    merge_tag(
        DATA / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json",
        [f"{MOD_ID}:{STONE_BARREL}"],
    )

    print(f"generated {len(WOOD_BARRELS) + len(EXTRA_BARRELS)} wood barrels + 1 stone barrel")


if __name__ == "__main__":
    main()
