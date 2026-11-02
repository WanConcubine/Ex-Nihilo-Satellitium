#!/usr/bin/env python3
"""Semantic comparison of two recipe directories (old hand-written vs datagen).

Textual diffs are useless here: the recipe codec always writes defaults
("group": "", "category": "misc", "count": 1, "show_notification": true) and may
re-letter a shaped pattern. This script canonicalises each recipe — shaped
patterns become a trimmed grid of ingredient ids, shapeless recipes a sorted
multiset, cooking recipes a fixed tuple — and reports real behavioural
differences only.

Usage:
    python3 scripts/compare-recipes.py \
        --old src/main/resources/data/exnihilosatellitium/recipe \
        --new src/generated/resources/data/exnihilosatellitium/recipe \
        --expect-old-only '*_mekanism.json'

Exit code 0 means every old recipe has an identical generated twin and the only
old-only files are the ones matched by --expect-old-only.
"""

from __future__ import annotations

import argparse
import fnmatch
import json
import sys
from pathlib import Path


def canon_ingredient(value) -> str:
    """One ingredient as a comparable string."""
    if isinstance(value, list):
        return "[" + ",".join(sorted(canon_ingredient(v) for v in value)) + "]"
    if isinstance(value, dict):
        if "item" in value:
            return "item:" + value["item"]
        if "tag" in value:
            return "tag:" + value["tag"]
        # Anything else (custom ingredient types) falls back to canonical JSON.
        return "json:" + canonical_json(value)
    return "raw:" + canonical_json(value)


def canon_result(value) -> str:
    if not isinstance(value, dict):
        return canonical_json(value)
    count = value.get("count", 1)
    # 1.21.1 uses `id`, 1.20.1 uses `item`.
    return f"{value.get('id') or value.get('item')}x{count}"


def canonical_json(value) -> str:
    return json.dumps(value, sort_keys=True, separators=(",", ":"))


def canon_conditions(recipe) -> str:
    conditions = recipe.get("neoforge:conditions")
    if conditions is None:
        return ""
    return canonical_json(conditions)


def canon_shaped(recipe) -> dict:
    key = recipe.get("key", {})
    pattern = recipe.get("pattern", [])
    grid = []
    for row in pattern:
        grid.append([None if ch == " " else canon_ingredient(key[ch]) for ch in row])
    # Trim empty rows and columns: a 2x2 pattern and a 3x3 pattern with an empty
    # border describe the same recipe.
    while grid and all(cell is None for cell in grid[-1]):
        grid.pop()
    width = max((len(row) for row in grid), default=0)
    for row in grid:
        while len(row) > 0 and row[-1] is None:
            row.pop()
    while width > 0 and all(len(row) <= width - 1 or row[width - 1] is None for row in grid):
        width -= 1
    trimmed = [[row[i] if i < len(row) else None for i in range(width)] for row in grid]
    return {
        "type": recipe.get("type"),
        "grid": trimmed,
        "result": canon_result(recipe.get("result", {})),
        "conditions": canon_conditions(recipe),
    }


def canon_shapeless(recipe) -> dict:
    ingredients = sorted(canon_ingredient(v) for v in recipe.get("ingredients", []))
    return {
        "type": recipe.get("type"),
        "ingredients": ingredients,
        "result": canon_result(recipe.get("result", {})),
        "conditions": canon_conditions(recipe),
    }


def canon_cooking(recipe) -> dict:
    return {
        "type": recipe.get("type"),
        "ingredient": canon_ingredient(recipe.get("ingredient")),
        "result": canon_result(recipe.get("result", {})),
        # Missing category in 1.21 means MISC; the codec materialises the default.
        "category": recipe.get("category", "misc"),
        "experience": float(recipe.get("experience", 0)),
        "cookingtime": int(recipe.get("cookingtime", 0)),
        "conditions": canon_conditions(recipe),
    }


def canon_smithing(recipe) -> dict:
    return {
        "type": recipe.get("type"),
        "template": canon_ingredient(recipe.get("template")),
        "base": canon_ingredient(recipe.get("base")),
        "addition": canon_ingredient(recipe.get("addition")),
        "result": canon_result(recipe.get("result", {})),
        "conditions": canon_conditions(recipe),
    }


CANONICALISERS = {
    "minecraft:crafting_shaped": canon_shaped,
    "minecraft:crafting_shapeless": canon_shapeless,
    "minecraft:smelting": canon_cooking,
    "minecraft:smoking": canon_cooking,
    "minecraft:campfire_cooking": canon_cooking,
    "minecraft:smithing_transform": canon_smithing,
}


def canonicalise(recipe: dict, path: Path) -> dict:
    recipe_type = recipe.get("type")
    canonicaliser = CANONICALISERS.get(recipe_type)
    if canonicaliser is None:
        print(f"[warn] {path}: no canonicaliser for type {recipe_type!r}", file=sys.stderr)
        return {k: v for k, v in sorted(recipe.items())}
    return canonicaliser(recipe)


def load(directory: Path) -> dict[str, dict]:
    recipes = {}
    for path in sorted(directory.glob("*.json")):
        try:
            recipes[path.stem] = canonicalise(json.loads(path.read_text(encoding="utf-8")), path)
        except Exception as exc:  # noqa: BLE001 - report and keep going
            print(f"[error] {path}: {exc}", file=sys.stderr)
            recipes[path.stem] = {"__error__": str(exc)}
    return recipes


def describe(value) -> str:
    return json.dumps(value, indent=2, sort_keys=True)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--old", type=Path,
                        default=Path("src/main/resources/data/exnihilosatellitium/recipe"))
    parser.add_argument("--new", type=Path,
                        default=Path("src/generated/resources/data/exnihilosatellitium/recipe"))
    parser.add_argument("--expect-old-only", action="append", default=[],
                        help="glob of old files that are intentionally not generated (repeatable)")
    args = parser.parse_args()

    if not args.old.is_dir():
        print(f"[error] missing old recipe directory: {args.old}", file=sys.stderr)
        return 2
    if not args.new.is_dir():
        print(f"[error] missing generated recipe directory: {args.new}", file=sys.stderr)
        return 2

    old = load(args.old)
    new = load(args.new)

    missing = sorted(set(old) - set(new))
    unexpected_missing = [name for name in missing
                          if not any(fnmatch.fnmatch(name + ".json", pattern)
                                     for pattern in args.expect_old_only)]
    extra = sorted(set(new) - set(old))
    changed = sorted(name for name in set(old) & set(new) if old[name] != new[name])

    if missing:
        print(f"[info] {len(missing)} old recipe(s) not generated:")
        for name in missing:
            marker = "expected" if name not in unexpected_missing else "UNEXPECTED"
            print(f"  - {name}.json ({marker})")
    if extra:
        print(f"[error] {len(extra)} generated recipe(s) have no old counterpart:")
        for name in extra:
            print(f"  - {name}.json")

    for name in changed:
        print(f"[error] {name}.json differs")
        print("  old: " + describe(old[name]).replace("\n", "\n       "))
        print("  new: " + describe(new[name]).replace("\n", "\n       "))

    ok = not unexpected_missing and not extra and not changed
    print()
    print(f"{len(old)} old, {len(new)} generated, "
          f"{len(missing)} old-only ({len(unexpected_missing)} unexpected), "
          f"{len(extra)} new-only, {len(changed)} different")
    print("MATCH" if ok else "MISMATCH")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
