#!/usr/bin/env python3
"""Render the End Cake inventory icon from the real block model.

Produces a 16x16 RGBA icon with a transparent background (the item model is
``item/generated``, not the block model).  A 512x512 ``preview_inventory.png``
is written for eyeballing; it is not shipped.

The geometry comes from the vanilla cake parent model, the textures from our
block model overrides.  Background removal uses two renders (black + white) to
recover per-pixel alpha, so the icon has no slot frame baked in.
"""

from __future__ import annotations

import importlib.util
import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
VANILLA_JAR = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"
MODEL = ROOT / "src/main/resources/assets/exnihilosatellitium/models/block/end_cake.json"

spec = importlib.util.spec_from_file_location(
    "barrel_render", ROOT / "art/barrel/render_current.py")
renderer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(renderer)

with zipfile.ZipFile(VANILLA_JAR) as jar:
    vanilla = json.loads(jar.read("assets/minecraft/models/block/cake.json"))
model = json.loads(MODEL.read_text(encoding="utf-8"))
merged = dict(vanilla)
merged["textures"] = {**vanilla.get("textures", {}), **model.get("textures", {})}
faces = renderer.build_faces(merged)


def project_at(size, ss):
    width = height = size * ss
    # Keep the object-to-canvas ratio constant when rendering smaller previews.
    px = 32.0 * ss * (size / 512.0)
    cx = width * 0.5
    cy = height * 0.52
    ax = math.radians(-30.0)
    ay = math.radians(225.0)

    def project(p):
        x, y, z = p[0] - 8.0, p[1] - 8.0, p[2] - 8.0
        x1 = x * math.cos(ay) + z * math.sin(ay)
        z1 = -x * math.sin(ay) + z * math.cos(ay)
        y2 = y * math.cos(ax) - z1 * math.sin(ax)
        z2 = y * math.sin(ax) + z1 * math.cos(ax)
        sx = cx + x1 * 0.625 * px
        sy = cy - y2 * 0.625 * px
        return sx, sy, z2

    return width, height, project


def render_rgba(size=512, ss=2):
    width, height, project = project_at(size, ss)
    black = np.zeros((height, width, 3), dtype=np.float32)
    white = np.ones((height, width, 3), dtype=np.float32)
    c_black = renderer.rasterize(faces, project, width, height, black, z_sign=-1.0)
    c_white = renderer.rasterize(faces, project, width, height, white, z_sign=-1.0)
    # C = alpha * F + (1 - alpha) * B, so alpha = 1 - (C_white - C_black).
    alpha = np.clip(1.0 - (c_white - c_black).mean(axis=2), 0.0, 1.0)
    rgba = np.dstack([np.clip(c_black, 0.0, 1.0), alpha])
    return Image.fromarray((rgba * 255.0).astype(np.uint8), "RGBA")


def main() -> None:
    icon = render_rgba(512, 2)
    # LANCZOS keeps the silhouette smooth at 16x16; the source is not pixel art.
    icon.resize((512, 512), Image.Resampling.LANCZOS).save(HERE / "preview_inventory.png")
    icon.resize((16, 16), Image.Resampling.LANCZOS).save(HERE / "inventory.png")
    print("wrote", HERE / "inventory.png", "and preview_inventory.png")


if __name__ == "__main__":
    main()
