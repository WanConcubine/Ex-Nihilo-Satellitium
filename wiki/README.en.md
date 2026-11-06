# Ex Nihilo: Satellitium Custom Recipe Wiki

> Chinese: [Chinese version](https://wanconcubine.github.io/Ex-Nihilo-Satellitium/)

> Last checked: 2026-11-06 · Applies to: 1.21.1 / 1.20.1

A "recipe editing" guide for players and modpack authors.

> Maintenance convention: this directory is the single source of truth for the player wiki. When you change table structures, field names, or load order in code,
> update the corresponding pages in the same commit and run `python3 scripts/check-wiki.py`. For maintainer duties and release checks see
> [`MAINTAINING.md`](MAINTAINING.md); for the gaps and stages between the three paths see
> [`docs/customization-roadmap.md`](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/customization-roadmap.md). The full developer-facing schema still lives in
> [`docs/data-format.md`](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/data-format.md).

## Choosing Between the Three Approaches

| Approach | What it can do | What it cannot do | Who it is for |
|---|---|---|---|
| **Datapack** (recommended) | Change the seven tables — sieve / hammer / crook / barrel / compost / crucible; add vanilla recipes; add tags | Cannot add items, blocks, or materials; cannot delete vanilla recipes directly | Everyone, no code |
| **KubeJS** | Script API for the seven tables (1.21.1 / 1.20.1) + `kubejs/data`; change vanilla recipes / tags from scripts; register your own items | Cannot change `materials.json` | Modpacks that already ship KubeJS |
| **CraftTweaker** | Script API for the seven tables (1.21.1 / 1.20.1) + vanilla recipes (crafting, smelting, etc.) | Cannot add items/blocks/materials; no CT recipe type / JEI category | Packs that already ship CT |

**Summary:** all three approaches can drive the seven gameplay tables (sieve/hammer/crook/barrel/compost/crucible):
**JSON, a KubeJS script, or a CraftTweaker script**; CT can additionally change vanilla recipes.

> Not sure whether a modded ore can be sifted? Start with [Material Chain & Mod Integration](material-chain.md).

## Version Differences

This wiki covers both release lines:

| | 1.21.1 (`main`, NeoForge) | 1.20.1 (`1.20.1-forge`, Forge) |
|---|---|---|
| Datapack `pack_format` | `48` | `15` |
| Vanilla recipe directory | `data/<ns>/recipe/` | `data/<ns>/recipes/` |
| Vanilla tag directory | `data/<ns>/tags/item/`, `tags/block/` | `tags/items/`, `tags/blocks/` |
| Common tag namespace | `c:` (e.g. `#c:gravels`) | `forge:` (e.g. `#forge:gravel`) |
| Vanilla recipe `result` | `{"id": "...", "count": 1}` | `{"item": "...", "count": 1}` |
| Conditions | `neoforge:conditions` | `forge:conditions` |
| Seven table directories | `sieve/`, `hammer/`… (identical on both versions) | Same as left |

The seven tables use **the same fields and directory names on both versions**, so the JSON examples in this wiki apply to both by default;
where tag namespaces, vanilla recipe JSON, or `pack_format` differ, they are noted separately.

## Getting Started (Datapack)

1. Create a directory in the save or on the server:
    - Single-player: `saves/<world name>/datapacks/my_pack/`
    - Server: `<level-name>/datapacks/my_pack/`
2. Add `pack.mcmeta`:

    ```json
    {
      "pack": {
        "pack_format": 48,
        "description": "我的空岛配方"
      }
    }
    ```

    1.20.1 changes `pack_format` to `15`.
3. Place files by namespace, e.g. `data/mypack/sieve/sand.json`:

    ```json
    {
      "input": "minecraft:sand",
      "patch": true,
      "remove": ["minecraft:cactus"],
      "outputs": [
        { "item": "minecraft:diamond", "chance": 0.5 }
      ]
    }
    ```

4. In game, run `/reload` (or `/datapack list` to confirm it is enabled).
5. Use `/exnihilo tables` to see entry counts; `/exnihilo sieve minecraft:sand` to query a single entry;
    `/exnihilo dump <table>` to see the whole table (sorted by line, easy to compare).

> Ready-to-copy example pack:
> [examples/datapack](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/tree/main/examples/datapack).

## Contents

- [Datapack: full table structure and examples](datapack.md)
- [KubeJS: what it can do, how to write it](kubejs.md)
- [CraftTweaker: limits and alternatives](crafttweaker.md)
- [Material chain & mod integration: which ores can enter, what authors can do](material-chain.md)
- [Cookbook for common tasks](cookbook.md)
- [Troubleshooting and logs](troubleshooting.md)
- [Maintenance contract](MAINTAINING.md)
- [Three-path equivalence roadmap](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/customization-roadmap.md)

## Three Things to Remember First

1. **`chance` is an independent probability, not a weight.** Each drop rolls `0..1` separately, so one pass can produce several items.
2. **A patch must set `"patch": true`.** Supplying only `outputs` without `patch` is read as "replacing the whole input entry",
   not appending.
3. **Compost needs two edits.** The value in `compost/*.json` plus the `#exnihilosatellitium:compostable`
   item tag; change only one and you get either "has a value but cannot be composted" or "can be composted but only gives 1 point".

## What These Three Paths Cannot Do

- **Add materials** (an ore outside the table): the material table lives in `materials.json` inside the jar and is read at registry time;
  datapacks/KJS/CT cannot change it. KJS can register its own item set as a "parallel ore chain", but it is not part of this mod's material chain.
  See [Material Chain & Mod Integration](material-chain.md).
- **Add items/blocks for this mod**: same as above — decided at registry time.
- **Delete vanilla recipes directly**: datapacks have no "delete recipe" JSON; use KubeJS or CraftTweaker.

---

If you run into problems, start with [Troubleshooting and logs](troubleshooting.md).
