#!/usr/bin/env python3
"""Guard the player wiki against structural rot.

Checks the cheap, objective failures so the human maintainer can spend time on
the semantic ones:

* required pages exist and every page has a "最后核对" header;
* README / sidebar link to every page;
* local Markdown links resolve;
* fenced code blocks are balanced and every ```json block parses;
* the datapack page mentions all seven table directories;
* the README still carries both pack formats and both tag namespaces;
* the KJS / CT pages still state their boundaries;
* the roadmap still has the P1/P2/P3 phases.

    python3 scripts/check-wiki.py

Exit code 1 on the first failed category (all errors are printed).
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WIKI = ROOT / "wiki"
SIDEBAR = WIKI / "_sidebar.md"

CONTENT_PAGES = [
    "README.md",
    "datapack.md",
    "kubejs.md",
    "crafttweaker.md",
    "cookbook.md",
    "troubleshooting.md",
    "MAINTAINING.md",
]
REQUIRED_PAGES = CONTENT_PAGES + ["_sidebar.md"]

TABLE_DIRS = [
    "sieve/",
    "hammer/",
    "crook/",
    "barrel/",
    "compost/",
    "crucible_melt/",
    "crucible_heat/",
]

HEADER = re.compile(r"^> 最后核对：\d{4}-\d{2}-\d{2} · 适用：", re.MULTILINE)
LINK = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")
FENCE = re.compile(r"```")


def strip_fences(text: str) -> str:
    return re.sub(r"```.*?```", "", text, flags=re.S)


def check_required(errors: list[str]) -> None:
    for name in REQUIRED_PAGES:
        if not (WIKI / name).is_file():
            errors.append(f"missing page: wiki/{name}")


def check_headers(errors: list[str]) -> None:
    for name in CONTENT_PAGES:
        path = WIKI / name
        if not path.is_file():
            continue
        if not HEADER.search(path.read_text(encoding="utf-8")):
            errors.append(f"wiki/{name}: missing '> 最后核对：YYYY-MM-DD · 适用：...' header")


def check_fences(errors: list[str]) -> int:
    blocks = 0
    for path in sorted(WIKI.glob("*.md")):
        text = path.read_text(encoding="utf-8")
        if len(FENCE.findall(text)) % 2 != 0:
            errors.append(f"wiki/{path.name}: unbalanced ``` fences")
        for index, block in enumerate(re.findall(r"```json\n(.*?)```", text, re.S), 1):
            blocks += 1
            try:
                json.loads(block)
            except json.JSONDecodeError as exception:
                errors.append(f"wiki/{path.name}: invalid JSON block #{index}: {exception}")
    return blocks


def check_links(errors: list[str]) -> int:
    count = 0
    for path in sorted(WIKI.glob("*.md")):
        text = strip_fences(path.read_text(encoding="utf-8"))
        for label, target in LINK.findall(text):
            count += 1
            target = target.strip().split()[0]
            if target.startswith(("http://", "https://", "mailto:", "#")):
                continue
            relative = target.split("#", 1)[0]
            if not relative:
                continue
            resolved = (path.parent / relative).resolve()
            if not resolved.exists():
                errors.append(f"wiki/{path.name}: broken link [{label}]({target})")
    return count


def check_index(errors: list[str]) -> None:
    readme = (WIKI / "README.md").read_text(encoding="utf-8") if (WIKI / "README.md").is_file() else ""
    sidebar = SIDEBAR.read_text(encoding="utf-8") if SIDEBAR.is_file() else ""
    for name in CONTENT_PAGES:
        if name != "README.md" and f"]({name})" not in readme:
            errors.append(f"wiki/README.md: index does not link to {name}")
        if f"]({name})" not in sidebar:
            errors.append(f"wiki/_sidebar.md: sidebar does not link to {name}")


def check_content(errors: list[str]) -> None:
    datapack = (WIKI / "datapack.md").read_text(encoding="utf-8")
    for directory in TABLE_DIRS:
        if directory not in datapack:
            errors.append(f"wiki/datapack.md: table directory {directory} is not documented")

    readme = (WIKI / "README.md").read_text(encoding="utf-8")
    for needle in ("pack_format", "48", "15", "`c:`", "`forge:`"):
        if needle not in readme:
            errors.append(f"wiki/README.md: version matrix is missing {needle!r}")

    kubejs = (WIKI / "kubejs.md").read_text(encoding="utf-8")
    for needle in ("kubejs/data", "SieveEvents", "materials.json"):
        if needle not in kubejs:
            errors.append(f"wiki/kubejs.md: KJS boundary note is missing {needle!r}")

    crafttweaker = (WIKI / "crafttweaker.md").read_text(encoding="utf-8")
    for needle in ("craftingTable", "改不了", "addJsonRecipe"):
        if needle not in crafttweaker:
            errors.append(f"wiki/crafttweaker.md: CT boundary note is missing {needle!r}")

    roadmap_path = ROOT / "docs" / "customization-roadmap.md"
    if not roadmap_path.is_file():
        errors.append("missing docs/customization-roadmap.md")
        return
    roadmap = roadmap_path.read_text(encoding="utf-8")
    for needle in ("P1", "P2", "P3", "KubeJS", "CraftTweaker", "验收"):
        if needle not in roadmap:
            errors.append(f"docs/customization-roadmap.md: missing phase marker {needle!r}")

    maintaining = (WIKI / "MAINTAINING.md").read_text(encoding="utf-8")
    for needle in ("check-wiki.py", "Owner", "Definition of Done"):
        if needle not in maintaining:
            errors.append(f"wiki/MAINTAINING.md: missing {needle!r}")


def main() -> int:
    errors: list[str] = []
    if not WIKI.is_dir():
        print("wiki check failed: wiki/ is missing", file=sys.stderr)
        return 1

    check_required(errors)
    check_headers(errors)
    json_blocks = check_fences(errors)
    links = check_links(errors)
    check_index(errors)
    check_content(errors)

    if errors:
        print(f"wiki check failed ({len(errors)} problem(s)):", file=sys.stderr)
        for error in errors:
            print(f"  ERROR: {error}", file=sys.stderr)
        return 1

    print(f"wiki check passed ({len(CONTENT_PAGES)} pages, {links} links, {json_blocks} JSON blocks)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
