#!/usr/bin/env python3
"""Guard the `data/` tree against the Minecraft version it targets.

Catches the expensive mistakes: a 1.20.1-style directory in a 1.21.1 tree (or
the other way round), `result.id` on 1.20.1, `neoforge:*` conditions in Forge
data, and `c:` tags that only exist on 1.21.1 (`forge:` on 1.20.1).

    python3 scripts/check-data-paths.py
    python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1

Exit code 1 when a file is in the wrong layout.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

import data_target

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources"

FORBIDDEN_DIRS = {
    "1.21.1": (("recipes",), ("loot_tables",), ("tags", "blocks"), ("tags", "items")),
    "1.20.1": (("recipe",), ("loot_table",), ("tags", "block"), ("tags", "item")),
}


def check_namespace(namespace: Path, target: data_target.DataTarget, errors: list[str]) -> None:
    name = namespace.name
    if name == data_target.OTHER_NAMESPACE[target.name]:
        errors.append(f"{namespace}: `{name}:` namespace belongs to the other version")
    for parts in FORBIDDEN_DIRS[target.name]:
        if (namespace.joinpath(*parts)).is_dir():
            errors.append(f"{namespace.joinpath(*parts)}: wrong directory name for {target.name}")

    recipe_dir = namespace / target.recipe_dir
    if recipe_dir.is_dir():
        for path in sorted(recipe_dir.rglob("*.json")):
            errors.extend(_check_recipe(path, target))

    tag_dir = namespace / "tags"
    if tag_dir.is_dir():
        for path in sorted(tag_dir.rglob("*.json")):
            if path.parts[-3] not in ("blocks", "block", "items", "item"):
                continue
            errors.extend(_check_tag(path, target))


def _check_recipe(path: Path, target: data_target.DataTarget) -> list[str]:
    problems: list[str] = []
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exception:
        return [f"{path}: invalid JSON ({exception})"]

    result = data.get("result")
    if isinstance(result, dict):
        expected = target.result_key
        wrong = "item" if expected == "id" else "id"
        if expected not in result:
            problems.append(f"{path}: recipe result has no `{expected}` key")
        if wrong in result:
            problems.append(f"{path}: recipe result still uses `{wrong}` (expected `{expected}`)")

    foreign = "neoforge" if target.legacy else "forge"
    if f"{foreign}:conditions" in data:
        problems.append(f"{path}: use `{target.condition_key}`, not `{foreign}:conditions`")
    for text in _walk_strings(data):
        if text.startswith(f"{foreign}:"):
            problems.append(f"{path}: `{text}` is not a {target.name} condition id")
        if text.startswith("#"):
            continue
        if target.legacy and text.startswith("c:"):
            problems.append(f"{path}: tag `{text}` is 1.21.1-only; use `forge:`")
        if not target.legacy and text.startswith("forge:"):
            problems.append(f"{path}: tag `{text}` is 1.20.1-only; use `c:`")
    return problems


def _check_tag(path: Path, target: data_target.DataTarget) -> list[str]:
    problems: list[str] = []
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exception:
        return [f"{path}: invalid JSON ({exception})"]

    for text in _walk_strings(data):
        if target.legacy and text.startswith("#c:"):
            problems.append(f"{path}: `{text}` is a 1.21.1 common tag; use `#forge:`")
        if not target.legacy and text.startswith("#forge:"):
            problems.append(f"{path}: `{text}` is a 1.20.1 common tag; use `#c:`")
    return problems


def _walk_strings(node):
    if isinstance(node, dict):
        for value in node.values():
            yield from _walk_strings(value)
    elif isinstance(node, list):
        for value in node:
            yield from _walk_strings(value)
    elif isinstance(node, str):
        yield node


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--root", type=Path, default=SOURCE,
                        help="resource root to check (default: src/main/resources)")
    parser.add_argument("--target", choices=data_target.TARGETS, default=data_target.DEFAULT)
    args = parser.parse_args(argv)

    target = data_target.get(args.target)
    data = args.root / "data"
    if not data.is_dir():
        print(f"no data/ directory under {args.root}")
        return 1

    errors: list[str] = []
    recipes = loots = tags = 0
    for namespace in sorted(entry for entry in data.iterdir() if entry.is_dir()):
        check_namespace(namespace, target, errors)
        recipes += len(list((namespace / target.recipe_dir).rglob("*.json"))) \
            if (namespace / target.recipe_dir).is_dir() else 0
        loots += len(list((namespace / target.loot_dir).rglob("*.json"))) \
            if (namespace / target.loot_dir).is_dir() else 0
        if (namespace / "tags").is_dir():
            tags += len(list((namespace / "tags").rglob("*.json")))

    if errors:
        print(f"data layout check failed for {target.name}:")
        for entry in errors[:20]:
            print(f"  - {entry}")
        if len(errors) > 20:
            print(f"  ... and {len(errors) - 20} more")
        return 1

    print(f"data layout check passed for {target.name} "
          f"({recipes} recipes, {loots} loot tables, {tags} tags)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
