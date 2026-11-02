#!/usr/bin/env python3
"""Semantic comparison of two asset trees (old hand-written vs datagen output).

Covers the three directories the model datagen owns:

    blockstates/*.json     state -> model mapping (variants or multipart)
    models/block/*.json    parent + textures (+ elements for hand-written files)
    models/item/*.json     parent + textures + overrides

`blockstates` are compared as data, not text: variant key order is irrelevant,
multipart part order is irrelevant (a part list is compared as a multiset), and
the `apply`/`when` key order is normalised.

Usage:
    python3 scripts/compare-models.py \
        --old src/main/resources/assets/exnihilosatellitium \
        --new src/generated/resources/assets/exnihilosatellitium \
        --expect-old-only models/block:barrel_base \
        --expect-old-only models/block:'ore_*_block' ...

Exit code 0 means every old file has an identical generated twin and the only
old-only files are the ones matched by --expect-old-only.
"""

from __future__ import annotations

import argparse
import fnmatch
import json
import sys
from pathlib import Path

DIRECTORIES = ("blockstates", "models/block", "models/item")


def canonical_json(value) -> str:
    return json.dumps(value, sort_keys=True, separators=(",", ":"))


def canon_variant_entry(entry) -> str:
    """One `variants` value: a single model blob or a weighted list of them."""
    if isinstance(entry, list):
        return canonical_json(sorted(entry, key=canonical_json))
    return canonical_json(entry)


def canon_blockstate(data: dict) -> dict:
    if "multipart" in data:
        parts = []
        for part in data["multipart"]:
            apply = part.get("apply")
            apply = sorted(apply, key=canonical_json) if isinstance(apply, list) else apply
            parts.append(canonical_json({"apply": apply, "when": part.get("when")}))
        return {"multipart": sorted(parts)}
    variants = {state: canon_variant_entry(entry) for state, entry in data.get("variants", {}).items()}
    return {"variants": variants}


def canon_model(data: dict) -> dict:
    """Model JSON as-is; dict comparison already ignores key order."""
    return data


def load(directory: Path, canon) -> dict[str, dict]:
    found = {}
    for path in sorted(directory.glob("*.json")):
        try:
            found[path.stem] = canon(json.loads(path.read_text(encoding="utf-8")))
        except Exception as exc:  # noqa: BLE001 - report and keep going
            found[path.stem] = {"__error__": str(exc)}
    return found


def expected_old_only(section: str, name: str, patterns: list[tuple[str, str]]) -> bool:
    return any(directory == section and fnmatch.fnmatch(name + ".json", pattern)
               for directory, pattern in patterns)


def describe(value) -> str:
    return json.dumps(value, indent=2, sort_keys=True)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--old", type=Path,
                        default=Path("src/main/resources/assets/exnihilosatellitium"))
    parser.add_argument("--new", type=Path,
                        default=Path("src/generated/resources/assets/exnihilosatellitium"))
    parser.add_argument("--expect-old-only", action="append", default=[],
                        metavar="DIR:GLOB",
                        help="old file that is intentionally not generated, e.g. "
                             "models/block:barrel_base (repeatable)")
    args = parser.parse_args()

    patterns: list[tuple[str, str]] = []
    for spec in args.expect_old_only:
        directory, _, pattern = spec.partition(":")
        if not directory or not pattern:
            parser.error(f"--expect-old-only wants DIR:GLOB, got {spec!r}")
        if not pattern.endswith(".json"):
            pattern += ".json"
        patterns.append((directory, pattern))

    for side in (args.old, args.new):
        if not side.is_dir():
            print(f"[error] missing asset directory: {side}", file=sys.stderr)
            return 2

    ok = True
    total_old = total_new = total_same = 0
    for section in DIRECTORIES:
        canon = canon_blockstate if section == "blockstates" else canon_model
        old = load(args.old / section, canon) if (args.old / section).is_dir() else {}
        new = load(args.new / section, canon) if (args.new / section).is_dir() else {}

        missing = sorted(set(old) - set(new))
        unexpected = [name for name in missing
                      if not expected_old_only(section, name, patterns)]
        extra = sorted(set(new) - set(old))
        changed = sorted(name for name in set(old) & set(new) if old[name] != new[name])

        for name in unexpected:
            print(f"[error] {section}/{name}.json has no generated counterpart")
        for name in extra:
            print(f"[error] {section}/{name}.json is generated but has no old counterpart")
        for name in changed:
            print(f"[error] {section}/{name}.json differs")
            print("  old: " + describe(old[name]).replace("\n", "\n       "))
            print("  new: " + describe(new[name]).replace("\n", "\n       "))

        if missing:
            print(f"[info] {section}: {len(missing)} old file(s) not generated "
                  f"({len(missing) - len(unexpected)} expected)")

        if unexpected or extra or changed:
            ok = False
        total_old += len(old)
        total_new += len(new)
        total_same += len(set(old) & set(new)) - len(changed)

    print()
    print(f"{total_old} old, {total_new} generated, {total_same} identical")
    print("MATCH" if ok else "MISMATCH")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
