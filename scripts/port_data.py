#!/usr/bin/env python3
"""Port the `data/` tree from the 1.21.1 layout to the 1.20.1 layout.

Only paths and field names change; every number (chances, counts, values) is
copied verbatim:

  data/<ns>/recipe/...            -> data/<ns>/recipes/...
  data/<ns>/loot_table/...        -> data/<ns>/loot_tables/...
  data/<ns>/tags/block/...        -> data/<ns>/tags/blocks/...
  data/<ns>/tags/item/...         -> data/<ns>/tags/items/...
  data/c/...                      -> data/forge/...
  {"result": {"id": ...}}         -> {"result": {"item": ...}}
  "neoforge:conditions" etc.      -> "forge:conditions" etc.
  "#c:ores/tin", "c:rods/wooden"  -> "#forge:ores/tin", "forge:rods/wooden"

Our own gameplay tables (`sieve/`, `hammer/`, `barrel/`, ...) are copied
unchanged. The output is a data tree, not a full resource pack: assets are
version-neutral and are written by the generators (`--target ... --out ...`).

    python3 scripts/port_data.py --out build/port-1.20.1
    python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1
"""

from __future__ import annotations

import argparse
import json
import shutil
import sys
from pathlib import Path

import data_target

ROOT = Path(__file__).resolve().parents[1]
# Hand-written resources and the datagen output are one logical tree; recipes,
# loot tables and tags now live in the generated half (datagen batches 1 + 2),
# while the gameplay tables and assets stay hand-written.
SOURCE = ROOT / "src/main/resources"
GENERATED = ROOT / "src/generated/resources"

# Tag kinds whose directory name differs between the versions.
TAG_KINDS = ("block", "item")


def map_path(relative: Path, target: data_target.DataTarget) -> Path | None:
    """Map one path under `data/` to the target layout; None to skip."""
    parts = relative.parts
    if len(parts) < 2:
        return None
    namespace, kind = parts[0], parts[1]
    rest = Path(*parts[2:])

    if kind == target.recipe_dir or kind in ("recipe", "recipes"):
        return Path("data", namespace, target.recipe_dir) / rest
    if kind == target.loot_dir or kind in ("loot_table", "loot_tables"):
        return Path("data", namespace, target.loot_dir) / rest
    if kind == "tags" and len(parts) >= 3 and parts[2] in ("block", "blocks", "item", "items"):
        tag_kind = "block" if parts[2] in ("block", "blocks") else "item"
        folder = target.tag_dir(tag_kind)
        return Path("data", target.tag_namespace(namespace), folder) / Path(*parts[3:])
    return Path("data") / relative


def detect_source_target(root: Path) -> str:
    """Guess the source layout from the first namespace that has a data dir."""
    data = root / "data"
    if not data.is_dir():
        raise SystemExit(f"no data/ directory under {root}")
    for namespace in data.iterdir():
        if (namespace / "recipes").is_dir() or (namespace / "loot_tables").is_dir():
            return "1.20.1"
        if (namespace / "recipe").is_dir() or (namespace / "loot_table").is_dir():
            return "1.21.1"
    raise SystemExit(f"cannot tell the layout of {data} (no recipe/ or recipes/ directory)")


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--source", type=Path, nargs="+", default=[SOURCE, GENERATED],
                        help="resource root(s) to read; later roots win on duplicate paths "
                             "(default: src/main/resources src/generated/resources)")
    parser.add_argument("--target", choices=data_target.TARGETS, default="1.20.1",
                        help="target layout (only 1.21.1 -> 1.20.1 is implemented)")
    parser.add_argument("--out", type=Path, default=ROOT / "build" / "port-1.20.1",
                        help="output root (default: build/port-1.20.1)")
    parser.add_argument("--force", action="store_true",
                        help="replace a non-empty output directory")
    args = parser.parse_args(argv)

    if args.target != "1.20.1":
        raise SystemExit("only the 1.21.1 -> 1.20.1 direction is implemented "
                         "(the 1.20.1 branch is still at M1)")
    target = data_target.get(args.target)

    sources: list[Path] = args.source
    for source in sources:
        if not (source / "data").is_dir():
            raise SystemExit(f"no data/ directory under {source}")
    source_layout = detect_source_target(sources[0])
    if source_layout == args.target:
        raise SystemExit(f"{sources[0]} is already in the {args.target} layout")

    out: Path = args.out
    forbidden = {ROOT.resolve(), (ROOT / "src").resolve(),
                 *(source.resolve() for source in sources)}
    if out.resolve() in forbidden:
        raise SystemExit(f"refusing to write the port into {out}")
    if out.exists() and any(out.iterdir()):
        if not args.force:
            raise SystemExit(f"{out} is not empty; pass --force to replace it")
        shutil.rmtree(out)
    out.mkdir(parents=True, exist_ok=True)

    # Merge the roots into one relative-path -> file map; a later root overrides
    # an earlier one, which is the same precedence the resource pack uses.
    files: dict[Path, Path] = {}
    for source in sources:
        for path in (source / "data").rglob("*"):
            if path.is_file():
                files[path.relative_to(source / "data")] = path

    counts = {"recipe": 0, "loot": 0, "tag": 0, "copied": 0}
    for relative, path in sorted(files.items()):
        destination = out / map_path(relative, target)
        destination.parent.mkdir(parents=True, exist_ok=True)

        if path.suffix != ".json":
            shutil.copyfile(path, destination)
            counts["copied"] += 1
            continue

        parts = relative.parts
        kind = parts[1] if len(parts) > 1 else ""
        if kind in ("recipe", "recipes"):
            recipe = json.loads(path.read_text(encoding="utf-8"))
            destination.write_text(_dump(data_target.to_legacy_recipe(recipe)),
                                   encoding="utf-8")
            counts["recipe"] += 1
        elif kind == "tags" and len(parts) >= 3:
            tag = json.loads(path.read_text(encoding="utf-8"))
            destination.write_text(_dump(data_target.to_legacy_tag(tag)), encoding="utf-8")
            counts["tag"] += 1
        elif kind in ("loot_table", "loot_tables"):
            shutil.copyfile(path, destination)
            counts["loot"] += 1
        else:
            shutil.copyfile(path, destination)
            counts["copied"] += 1

    print(f"{source_layout} -> {args.target}: "
          f"{counts['recipe']} recipes, {counts['loot']} loot tables, "
          f"{counts['tag']} tags, {counts['copied']} other files")
    print(f"written to {out}")
    return 0


def _dump(data) -> str:
    return json.dumps(data, ensure_ascii=False, indent=2) + "\n"


if __name__ == "__main__":
    sys.exit(main())
