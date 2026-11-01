#!/usr/bin/env python3
"""Version-specific data-pack layout and field conversion.

The gameplay tables (`sieve/`, `hammer/`, `barrel/`, ...) are our own format and
are identical on both versions. The *vanilla-typed* JSON is not:

| item            | 1.21.1 (NeoForge)           | 1.20.1 (Forge)              |
|-----------------|-----------------------------|-----------------------------|
| recipe dir      | `recipe/`                   | `recipes/`                  |
| loot dir        | `loot_table/`               | `loot_tables/`              |
| tag dirs        | `tags/block`, `tags/item`   | `tags/blocks`, `tags/items` |
| recipe result   | `result.id`                 | `result.item`               |
| conditions      | `neoforge:conditions` etc.  | `forge:conditions` etc.     |
| common tags     | `c:`                        | `forge:`                    |

`DataTarget` is imported by the asset/data generators (one script can emit
either layout) and by `check-data-paths.py`; `scripts/port_data.py` additionally
uses `to_legacy_recipe` / `to_legacy_tag` to convert an existing 1.21.1 tree.

    python3 scripts/generate_ore_variants.py --target 1.20.1 --out /tmp/port
"""

from __future__ import annotations

from dataclasses import dataclass

TARGETS = ("1.21.1", "1.20.1")
DEFAULT = "1.21.1"

# "Common" tags: 1.21.1 NeoForge uses the Fabric-style `c:` convention, 1.20.1
# Forge only knows `forge:`.
COMMON_NAMESPACE = {"1.21.1": "c", "1.20.1": "forge"}
OTHER_NAMESPACE = {"1.21.1": "forge", "1.20.1": "c"}


@dataclass(frozen=True)
class DataTarget:
    """Paths and JSON field names for one Minecraft version."""

    name: str

    def __post_init__(self) -> None:
        if self.name not in TARGETS:
            raise ValueError(f"unknown target {self.name!r}; pick one of {TARGETS}")

    @property
    def legacy(self) -> bool:
        """True for 1.20.1 (Forge)."""
        return self.name == "1.20.1"

    # ------------------------------------------------------------------ paths

    @property
    def recipe_dir(self) -> str:
        return "recipes" if self.legacy else "recipe"

    @property
    def loot_dir(self) -> str:
        return "loot_tables" if self.legacy else "loot_table"

    def tag_dir(self, kind: str) -> str:
        if kind not in ("block", "item"):
            raise ValueError(f"tag kind must be block/item, got {kind!r}")
        folder = {"block": "blocks", "item": "items"}[kind] if self.legacy else kind
        return f"tags/{folder}"

    def tag_namespace(self, namespace: str) -> str:
        """Map the other version's common namespace onto this target's."""
        if namespace == OTHER_NAMESPACE[self.name]:
            return COMMON_NAMESPACE[self.name]
        return namespace

    def common_ref(self, path: str) -> str:
        """`ores/tin` -> `c:ores/tin` (1.21.1) / `forge:ores/tin` (1.20.1)."""
        return f"{COMMON_NAMESPACE[self.name]}:{path}"

    # `data/...` paths, relative to the resource root.
    def recipe_path(self, namespace: str, sub: str) -> str:
        return f"data/{namespace}/{self.recipe_dir}/{sub}.json"

    def loot_path(self, namespace: str, sub: str) -> str:
        return f"data/{namespace}/{self.loot_dir}/{sub}.json"

    def tag_path(self, namespace: str, kind: str, sub: str) -> str:
        return f"data/{self.tag_namespace(namespace)}/{self.tag_dir(kind)}/{sub}.json"

    # ------------------------------------------------------- field names

    @property
    def result_key(self) -> str:
        """Recipe result: `id` on 1.21.1, `item` on 1.20.1."""
        return "item" if self.legacy else "id"

    @property
    def condition_key(self) -> str:
        return "forge:conditions" if self.legacy else "neoforge:conditions"

    def mod_loaded(self, modid: str) -> dict:
        prefix = "forge" if self.legacy else "neoforge"
        return {"type": f"{prefix}:mod_loaded", "modid": modid}

    def negate(self, condition: dict) -> dict:
        prefix = "forge" if self.legacy else "neoforge"
        return {"type": f"{prefix}:not", "value": condition}


def get(name: str) -> DataTarget:
    return DataTarget(name)


# --------------------------------------------------------------- conversion


def to_legacy_recipe(recipe: dict) -> dict:
    """Convert a 1.21.1 recipe object to the 1.20.1 field names."""
    return _to_legacy(recipe)


def to_legacy_tag(tag: dict) -> dict:
    """Convert a 1.21.1 tag object to the 1.20.1 namespaces."""
    return _to_legacy(tag)


def _to_legacy(node):
    if isinstance(node, dict):
        out = {}
        for key, value in node.items():
            if key == "neoforge:conditions":
                key = "forge:conditions"
            if key == "result" and isinstance(value, dict) and "id" in value:
                value = {("item" if k == "id" else k): v for k, v in value.items()}
            out[key] = _to_legacy(value)
        return out
    if isinstance(node, list):
        return [_to_legacy(value) for value in node]
    if isinstance(node, str):
        if node.startswith("neoforge:"):
            return "forge:" + node[len("neoforge:"):]
        if node.startswith("#c:"):
            return "#forge:" + node[len("#c:"):]
        if node.startswith("c:"):
            return "forge:" + node[len("c:"):]
        return node
    return node
