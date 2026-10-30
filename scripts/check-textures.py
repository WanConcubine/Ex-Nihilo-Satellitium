#!/usr/bin/env python3
"""Fail the build if shipped assets still depend on vanilla textures.

Checks:
  - every model texture reference to this mod resolves to an existing PNG
  - no model texture reference points at minecraft:item/* (vanilla block
    textures are allowed for the vanilla-sampled style)
  - every block/item texture is 16x16 (PNG header only, no Pillow needed)
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
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "exnihilosatellitium"
MODELS = ASSETS / "models"
TEXTURES = ASSETS / "textures"

errors: list[str] = []


def png_size(path: Path) -> tuple[int, int]:
    with path.open("rb") as handle:
        header = handle.read(24)
    if header[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a PNG")
    return struct.unpack(">II", header[16:24])


def texture_path(reference: str) -> Path | None:
    if reference.startswith("exnihilosatellitium:"):
        rel = reference.split(":", 1)[1]
        return TEXTURES / f"{rel}.png"
    return None


def walk_textures(node: object, model: Path) -> None:
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
                    path = texture_path(reference)
                    if path is not None and not path.exists():
                        errors.append(f"{model}: layer '{layer}' missing texture {path}")
            else:
                walk_textures(value, model)
    elif isinstance(node, list):
        for item in node:
            walk_textures(item, model)


def main() -> int:
    if not MODELS.exists():
        print("no models directory; nothing to check")
        return 0

    for model in sorted(MODELS.rglob("*.json")):
        try:
            data = json.loads(model.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exception:
            errors.append(f"{model}: invalid JSON ({exception})")
            continue
        walk_textures(data, model)

    for png in sorted(TEXTURES.rglob("*.png")):
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
        elif (width, height) != (16, 16):
            errors.append(f"{png}: expected 16x16, got {width}x{height}")

    if errors:
        print("texture check failed:")
        for error in errors:
            print("  -", error)
        return 1

    print("texture check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
