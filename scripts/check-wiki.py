#!/usr/bin/env python3
"""Guard the player wiki against structural rot.

Checks the cheap, objective failures so the human maintainer can spend time on
the semantic ones:

* required pages exist and every page has a "最后核对" header;
* every page has an English counterpart (X.en.md) with its own header, no
  leftover Chinese prose, and JSON examples identical to the Chinese page;
* README / sidebar link to every page;
* mkdocs.yml nav covers every page (local page with no entry is unreachable)
  and nav_translations covers every nav title (English sidebar stays English);
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
MKDOCS = ROOT / "mkdocs.yml"

CONTENT_PAGES = [
    "README.md",
    "datapack.md",
    "kubejs.md",
    "crafttweaker.md",
    "material-chain.md",
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
EN_HEADER = re.compile(r"^> Last checked: \d{4}-\d{2}-\d{2} · Applies to:", re.MULTILINE)
LINK = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")
NAV_ENTRY = re.compile(r"^\s+-\s+([^:]+):\s*(\S+\.md)\s*$")
FENCE = re.compile(r"```")
CJK = re.compile(r"[\u4e00-\u9fff]")
INLINE_CODE = re.compile(r"`[^`\n]*`")


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


def yaml_block(lines: list[str], key: str) -> list[str]:
    """Return the indented lines that follow a `key:` line at any depth."""
    try:
        start = next(i for i, line in enumerate(lines) if line.strip() == f"{key}:")
    except StopIteration:
        return []
    block: list[str] = []
    for line in lines[start + 1 :]:
        if line.strip() and not line[0].isspace():
            break
        block.append(line)
    return block


def check_site_nav(errors: list[str]) -> tuple[int, int]:
    """Every content page must be reachable from mkdocs.yml nav.

    MkDocs happily builds a page that no nav entry points at; the page then
    exists but nobody can reach it. Keep the site nav and the guard together.
    The English sidebar reuses those titles, so nav_translations must cover
    every one of them or the English site leaks Chinese labels.
    """
    if not MKDOCS.is_file():
        errors.append("missing mkdocs.yml (static site config)")
        return 0, 0

    lines = MKDOCS.read_text(encoding="utf-8").splitlines()
    entries = [m for m in (NAV_ENTRY.match(line) for line in yaml_block(lines, "nav")) if m]
    titles = [m.group(1).strip() for m in entries]
    targets = [m.group(2) for m in entries]

    if not targets:
        errors.append("mkdocs.yml: `nav:` has no page entries")
        return 0, 0

    for target in targets:
        if not (WIKI / target).is_file():
            errors.append(f"mkdocs.yml: nav entry points to missing wiki/{target}")
    for name in CONTENT_PAGES:
        if name not in targets:
            errors.append(f"mkdocs.yml: nav does not list wiki/{name} (page would be unreachable)")

    translations = yaml_block(lines, "nav_translations")
    if not translations:
        errors.append("mkdocs.yml: no `nav_translations:` block (English sidebar would stay Chinese)")
        return len(targets), 0

    translated = 0
    for title in titles:
        if any(re.match(rf"^\s+{re.escape(title)}:\s*\S", line) for line in translations):
            translated += 1
        else:
            errors.append(f"mkdocs.yml: nav_translations is missing {title!r}")
    return len(targets), translated


def json_blocks(text: str) -> list:
    blocks = []
    for raw in re.findall(r"```json\n(.*?)```", text, re.S):
        try:
            blocks.append(json.loads(raw))
        except json.JSONDecodeError:
            blocks.append(raw)
    return blocks


def check_translations(errors: list[str]) -> int:
    """Every page needs an English twin that is actually translated.

    Three things rot quietly on a bilingual site: a page with no translation,
    a translation that still contains Chinese paragraphs, and JSON examples
    that were edited on one side only. Check all three.
    """
    pairs = 0
    for name in CONTENT_PAGES:
        target = WIKI / f"{Path(name).stem}.en.md"
        if not target.is_file():
            errors.append(f"missing translation: wiki/{target.name}")
            continue
        pairs += 1
        text = target.read_text(encoding="utf-8")
        if not EN_HEADER.search(text):
            errors.append(
                f"wiki/{target.name}: missing '> Last checked: YYYY-MM-DD · Applies to: ...' header"
            )
        prose = INLINE_CODE.sub("", strip_fences(text))
        leftover = CJK.search(prose)
        if leftover:
            line = prose.count("\n", 0, leftover.start()) + 1
            errors.append(f"wiki/{target.name}: untranslated Chinese near line {line}")
        if json_blocks(text) != json_blocks((WIKI / name).read_text(encoding="utf-8")):
            errors.append(f"wiki/{target.name}: JSON examples drifted from wiki/{name}")
    return pairs



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
    for needle in ("craftingTable", "mods.exnihilosatellitium", "addJson"):
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
    json_examples = check_fences(errors)
    links = check_links(errors)
    check_index(errors)
    nav_pages, nav_translated = check_site_nav(errors)
    pairs = check_translations(errors)
    check_content(errors)

    if errors:
        print(f"wiki check failed ({len(errors)} problem(s)):", file=sys.stderr)
        for error in errors:
            print(f"  ERROR: {error}", file=sys.stderr)
        return 1

    print(
        f"wiki check passed ({len(CONTENT_PAGES)} pages, {pairs} translations, "
        f"{links} links, {json_examples} JSON blocks, "
        f"{nav_pages} nav entries ({nav_translated} translated))"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
