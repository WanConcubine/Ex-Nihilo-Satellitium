#!/usr/bin/env python3
"""Keep the version-agnostic core free of NeoForge/Forge imports.

The `core` layer (data tables, JSON parsing, roll math, state machines) must be
copyable between the 1.21.1 and 1.20.1 branches without touching it. This guard
fails when a new `net.neoforged` / `net.minecraftforge` reference sneaks into a
core package.

Version glue lives in `com.kaifengcrt.exnihilosatellitium.platform` (registries,
capabilities, config spec, machine capability adapters) and is deliberately not
scanned: that is exactly the layer a port rewrites. Core talks to it through the
small facades `ConfigValues`, `Platform` and `MachineHooks`.

`KNOWN` is empty since P1; add an entry only together with a plan to remove it.

    python3 scripts/check-core-imports.py
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PKG = ROOT / "src/main/java/com/kaifengcrt/exnihilosatellitium"

CORE_DIRS = (
    "common/sieve",
    "common/hammer",
    "common/crook",
    "common/compost",
    "common/barrel",
    "common/crucible",
    "common/material",
    "common/config",    # ConfigValues / ConfigSource facades
    "common/platform",  # Platform / ModProbe facades
    "common/machine",   # top-level roll math / enums / energy buffer
    "common/table",     # TableBuilder / CustomizationOverlay / dump
)

CORE_FILES = (
    # the machine state machine is pure; capabilities live in platform/machine
    "common/machine/blockentity/MachineBlockEntity.java",
)

# Platform glue in core-looking packages. Empty since P1.
KNOWN: dict[str, str] = {}

PATTERN = re.compile(r"net\.(?:neoforged|minecraftforge)\.")


def core_files() -> list[Path]:
    files: list[Path] = []
    for directory in CORE_DIRS:
        files.extend(sorted((PKG / directory).glob("*.java")))
    files.extend(PKG / path for path in CORE_FILES)
    # one entry per file even if a directory and an explicit file overlap
    return sorted({path for path in files if path.exists()})


def violations(path: Path) -> list[int]:
    hits: list[int] = []
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        stripped = line.strip()
        if stripped.startswith(("//", "*", "/*")):
            continue
        if PATTERN.search(line):
            hits.append(number)
    return hits


def main() -> int:
    known: list[str] = []
    unknown: list[str] = []
    for path in core_files():
        hits = violations(path)
        if not hits:
            continue
        rel = path.relative_to(PKG).as_posix()
        message = f"{rel}:{hits[0]} ({len(hits)} hit{'s' if len(hits) > 1 else ''})"
        if rel in KNOWN:
            known.append(f"{message} — {KNOWN[rel]}")
        else:
            unknown.append(message)

    for entry in known:
        print(f"  known leak: {entry}")
    if unknown:
        print("core purity check failed:")
        for entry in unknown:
            print(f"  - {entry}")
        print("Move the code to a platform package or wrap it behind a facade.")
        return 1
    print(f"core purity check passed ({len(known)} known leak(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
