#!/usr/bin/env python3
"""Render the End Cake inventory icon from the real block model.

Reuses the barrel preview renderer (same face shading / item transform) but
loads the vanilla cake parent model from the client jar for the geometry.
"""

from __future__ import annotations

import importlib.util
import json
import zipfile
from pathlib import Path

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
# Our model only overrides textures; geometry comes from the vanilla parent.
faces = renderer.build_faces(merged)
icon = renderer.render_item(faces, size=512)
icon.save(HERE / "inventory.png")
print("wrote", HERE / "inventory.png")
