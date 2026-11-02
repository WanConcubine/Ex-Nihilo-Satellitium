#!/usr/bin/env python3
"""Fail the build if shipped assets still depend on vanilla textures.

Scans both resource roots (hand-written `src/main/resources` and the datagen
output `src/generated/resources`); a file in the generated tree wins.

Checks:
  - every model texture reference to this mod resolves to an existing PNG
  - no model texture reference points at minecraft:item/* (vanilla block
    textures are allowed for the vanilla-sampled style)
  - every blockstate reference to this mod resolves to an existing model
  - every model `parent` reference to this mod resolves to an existing model
  - every block texture is 16x16; item textures may be 16x16 or 64x64
    (the dolls ship at 64x64; PNG header only, no Pillow needed)
  - animated textures (a sibling .mcmeta with an "animation" block) are
    16x(16*n) vertical sprite sheets
  - GUI textures under textures/gui/ are exempt (JEI backgrounds etc.)

Usage:
    python3 scripts/check-textures.py
"""

from __future__ import annotations

import json
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NAMESPACE = "exnihilosatellitium"
# Later roots win, matching the resource-pack precedence of the Gradle source set.
ASSET_ROOTS = [
    ROOT / "src" / "main" / "resources" / "assets" / NAMESPACE,
    ROOT / "src" / "generated" / "resources" / "assets" / NAMESPACE,
]

errors: list[str] = []


def merged(subdirectory: str, suffix: str) -> dict[str, Path]:
    """Relative path (without the subdirectory) -> file, across both roots."""
    files: dict[str, Path] = {}
    for root in ASSET_ROOTS:
        base = root / subdirectory
        if not base.is_dir():
            continue
        for path in base.rglob(f"*{suffix}"):
            files[str(path.relative_to(base))[:-len(suffix)]] = path
    return files


def allowed_sizes(path: Path) -> set[tuple[int, int]]:
    """Item icons may ship at 64x64 (dolls); block textures stay 16x16."""
    if "textures" in path.parts and "item" in path.parts:
        return {(16, 16), (64, 64)}
    if path.name.startswith("machine_"):
        return {(16, 16), (32, 32)}
    return {(16, 16)}


def png_size(path: Path) -> tuple[int, int]:
    with path.open("rb") as handle:
        header = handle.read(24)
    if header[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a PNG")
    return struct.unpack(">II", header[16:24])


def our_reference(reference: str) -> str | None:
    """`exnihilosatellitium:block/foo` -> `block/foo`; other namespaces -> None."""
    prefix, _, path = reference.partition(":")
    if prefix != NAMESPACE or not path:
        return None
    return path


def walk_textures(node: object, model: str, textures: dict[str, Path]) -> None:
    if isinstance(node, dict):
        for key, value in node.items():
            if key == "textures" and isinstance(value, dict):
                for layer, reference in value.items():
                    if not isinstance(reference, str):
                        continue
                    if reference.startswith("minecraft:item/"):
                        errors.append(f"{model}: layer '{layer}' uses a vanilla item texture {reference}")
                        continue
                    if reference.startswith("minecraft:block/"):
                        # Intentional vanilla-sampled block texture; not redistributed.
                        continue
                    if reference.startswith("minecraft:"):
                        errors.append(f"{model}: layer '{layer}' points at unsupported vanilla resource {reference}")
                        continue
                    rel = our_reference(reference)
                    if rel is not None and rel not in textures:
                        errors.append(f"{model}: layer '{layer}' missing texture {rel}.png")
            else:
                walk_textures(value, model, textures)


def check_blockstate_references(name: str, data: dict, models: dict[str, Path]) -> None:
    references: list[str] = []
    for entry in data.get("variants", {}).values():
        for model in entry if isinstance(entry, list) else [entry]:
            if isinstance(model, dict) and isinstance(model.get("model"), str):
                references.append(model["model"])
    for part in data.get("multipart", []):
        apply = part.get("apply")
        for model in apply if isinstance(apply, list) else [apply]:
            if isinstance(model, dict) and isinstance(model.get("model"), str):
                references.append(model["model"])
    for reference in references:
        rel = our_reference(reference)
        if rel is not None and rel not in models:
            errors.append(f"blockstates/{name}.json: missing model {rel}")


def check_model_parent(name: str, data: dict, models: dict[str, Path]) -> None:
    parent = data.get("parent")
    if not isinstance(parent, str):
        return
    rel = our_reference(parent)
    if rel is not None and rel not in models:
        errors.append(f"models/{name}.json: missing parent model {rel}")


def main() -> int:
    models = merged("models", ".json")
    blockstates = merged("blockstates", ".json")
    textures = merged("textures", ".png")

    for name in sorted(models):
        try:
            data = json.loads(models[name].read_text(encoding="utf-8"))
        except json.JSONDecodeError as exception:
            errors.append(f"models/{name}.json: invalid JSON ({exception})")
            continue
        walk_textures(data, f"models/{name}.json", textures)
        check_model_parent(name, data, models)

    for name in sorted(blockstates):
        try:
            data = json.loads(blockstates[name].read_text(encoding="utf-8"))
        except json.JSONDecodeError as exception:
            errors.append(f"blockstates/{name}.json: invalid JSON ({exception})")
            continue
        check_blockstate_references(name, data, models)

    for name in sorted(textures):
        png = textures[name]
        try:
            width, height = png_size(png)
        except ValueError as exception:
            errors.append(f"{png}: {exception}")
            continue
        if png.parent.name == "gui":
            continue
        meta = png.with_name(png.name + ".mcmeta")
        if meta.exists():
            # Animated sprite sheet: 16 wide, a vertical strip of 16 px frames.
            if width != 16 or height % 16 != 0 or height == 0:
                errors.append(f"{png}: animated texture must be 16x(16*n), got {width}x{height}")
            try:
                data = json.loads(meta.read_text(encoding="utf-8"))
            except json.JSONDecodeError as exception:
                errors.append(f"{meta}: invalid JSON ({exception})")
                continue
            if "animation" not in data:
                errors.append(f"{meta}: missing \"animation\" block")
        elif (width, height) not in allowed_sizes(png):
            errors.append(f"{png}: expected {sorted(allowed_sizes(png))[0]}x..."
                          f" got {width}x{height}")

    if errors:
        print("texture check failed:")
        for error in errors:
            print("  -", error)
        return 1

    print(f"texture check passed ({len(models)} models, {len(blockstates)} blockstates, "
          f"{len(textures)} textures)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
