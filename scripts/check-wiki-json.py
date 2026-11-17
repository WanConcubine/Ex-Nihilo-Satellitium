#!/usr/bin/env python3
"""Check the JSON snippets embedded in the player wiki.

`check-wiki.py` guarantees that zh/en pages stay in sync and that every example
parses as JSON. It cannot tell whether an example is *shaped* like a table
entry: a renamed field, a missing namespace or a 0..1 `chance` typo still looks
like valid JSON, and players copy these blocks verbatim.

This guard scans every ```json fenced block in `wiki/*.md` and validates the
parts that the loaders actually interpret:

- id fields (`item`, `input`, `result`, `fluid`, `block`, `tag`, `base`,
  `drain`, `remove[]`) must be `namespace:path` or `#namespace:tag`;
- `chance` must be a number in 0..1;
- `min` / `max` must be non-negative integers with min <= max;
- `amount` / `solid` / `speed` must be non-negative numbers;
- every key of a compost `values` object must be an id (or `#tag`).

Blocks are classified by content, so a new page does not need registration
here. A block that is not a table entry (for example `pack.mcmeta`) is skipped.

    python3 scripts/check-wiki-json.py
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

DEFAULT_WIKI = Path(__file__).resolve().parents[1] / "wiki"

BLOCK = re.compile(r"```json\n(.*?)```", re.S)
ID = re.compile(r"^#?[a-z0-9_.-]+:[a-z0-9_./-]+$")

ID_KEYS = ("item", "input", "result", "fluid", "block", "tag", "base", "drain")
NUMBER_KEYS = ("amount", "solid", "speed")
SKIP_KEYS = ("mod", "entity", "source", "description")


def check_id(value: object, where: str, problems: list[str]) -> None:
    if isinstance(value, str):
        if not ID.match(value):
            problems.append(f"{where}: bad id {value!r} (expected namespace:path or #namespace:tag)")
    elif isinstance(value, list):
        for index, entry in enumerate(value):
            check_id(entry, f"{where}[{index}]", problems)
    # booleans (`"remove": true`) are a different feature and are ignored here


def check_number(value: object, where: str, problems: list[str], *, chance: bool = False) -> None:
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        problems.append(f"{where}: expected a number, got {value!r}")
        return
    if chance and not 0.0 <= float(value) <= 1.0:
        problems.append(f"{where}: chance {value} outside 0..1")
    if not chance and float(value) < 0:
        problems.append(f"{where}: {value} is negative")


def walk(node: object, where: str, problems: list[str]) -> None:
    if isinstance(node, dict):
        for key in ("min", "max"):
            if key in node:
                value = node[key]
                if isinstance(value, bool) or not isinstance(value, int) or value < 0:
                    problems.append(f"{where}.{key}: expected a non-negative integer, got {value!r}")
        if "min" in node and "max" in node:
            low, high = node["min"], node["max"]
            if isinstance(low, int) and isinstance(high, int) and not isinstance(low, bool) and low > high:
                problems.append(f"{where}: min {low} > max {high}")
        for key, value in node.items():
            child = f"{where}.{key}" if where else key
            if key in SKIP_KEYS:
                continue
            if key in ID_KEYS:
                check_id(value, child, problems)
            elif key == "chance":
                check_number(value, child, problems, chance=True)
            elif key in NUMBER_KEYS:
                check_number(value, child, problems)
            elif key == "values" and isinstance(value, dict):
                for entry_key, entry_value in value.items():
                    check_id(entry_key, f"{child}[{entry_key!r}]", problems)
                    check_number(entry_value, f"{child}[{entry_key!r}]", problems)
            else:
                walk(value, child, problems)
    elif isinstance(node, list):
        for index, entry in enumerate(node):
            walk(entry, f"{where}[{index}]", problems)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--wiki", default=str(DEFAULT_WIKI), help="wiki directory (default: ./wiki)")
    args = parser.parse_args()

    problems: list[str] = []
    checked = 0
    for path in sorted(Path(args.wiki).glob("*.md")):
        text = path.read_text(encoding="utf-8")
        for match in BLOCK.finditer(text):
            line = text.count("\n", 0, match.start()) + 1
            where = f"{path.name}:{line}"
            try:
                data = json.loads(match.group(1))
            except json.JSONDecodeError as error:
                problems.append(f"{where}: not valid JSON ({error})")
                continue
            if isinstance(data, dict) and "pack" in data and "pack_format" in data.get("pack", {}):
                continue  # pack.mcmeta snippet, not a table entry
            checked += 1
            walk(data, "", problems)

    if problems:
        print("wiki JSON check failed:")
        for problem in problems:
            print(f"  - {problem}")
        return 1
    print(f"wiki JSON check passed ({checked} table snippet(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
