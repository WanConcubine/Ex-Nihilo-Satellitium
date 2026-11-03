# Datapack: seven gameplay tables

> Last checked: 2026-11-03 · Applies to: 1.21.1 / 1.20.1

The datapack is the **recommended way**: no code, takes effect on `/reload`, and the server is the authority.

> In 1.21.1 `pack_format` is `48`, in 1.20.1 it is `15`; the table directory names below are the same in both versions.
> Tag namespace: `c:` in 1.21.1, `forge:` in 1.20.1.

## 1. Installing and activating

```
saves/<世界名>/datapacks/my_pack/
├── pack.mcmeta
└── data/
    └── mypack/
        ├── sieve/
        ├── hammer/
        ├── crook/
        ├── barrel/
        ├── compost/
        ├── crucible_melt/
        └── crucible_heat/
```

`pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 48,
    "description": "我的空岛配方"
  }
}
```

- Folders and `.zip` files both work; folders are recommended, since you can edit and `/reload` directly.
- It only counts as loaded once `/datapack list` shows `file/my_pack`.
- The seven tables scan **all namespaces**, so `mypack` does not have to equal `exnihilosatellitium`.
- After editing, `/reload`; the log first shows `waiting for tags`, then the full entry counts — **the second one is authoritative**.

## 2. Common fields

### 2.1 `#tag` inputs

All `input` / `block` / `base` / `added` fields accept `#tag`:

```json
{ "input": "#c:gravels", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

- 1.21.1: `#c:gravels`
- 1.20.1: `#forge:gravel`
- Vanilla tags are the same in both versions: `#minecraft:logs` / `#minecraft:leaves` / `#minecraft:campfires`, etc.

Expansion rules:

| Table | Tag behavior |
|---|---|
| Sieve / crook | **Append** an entry for every matching item; identical drops are kept only once |
| Hammer / melting point / heat source | **Specific entries win**; tags do not override existing rules |
| Barrel | `base` / `added` are expanded, then cross-producted; `base`/`added.fluid` keep only **still (source)** fluids |
| Compost | Natively supported, matched by tag |

> Drop products (`item` / `result`) must be **concrete ids**; `#tag` is not supported. To "randomly drop one kind of ingot", write several drop entries.

### 2.2 `mod` gate

Write it at the file root:

```json
{ "mod": "tconstruct", "input": "minecraft:magma_block", "fluid": "minecraft:lava", "amount": 500 }
```

When the mod is absent the **whole file is skipped silently**: the items inside are not parsed, and no unknown id warnings are printed.
**Individual drops** of the sieve / crook can also carry their own `mod`:

```json
{ "item": "tconstruct:sky_slime_grass_seeds", "chance": 0.02, "mod": "tconstruct" }
```

### 2.3 Patches and removal

| Table | `"patch": true` | `"remove": true` | `"remove": ["id", "#tag"]` |
|---|---|---|---|
| Sieve | Append to `outputs` | Delete the whole `input` entry | Delete matching drops |
| Crook | Append to `drops` | Delete the whole `input` entry | Delete matching drops |
| Hammer / melting point / heat source | Replace the rule for that input | Delete that input | — |
| Barrel | Replace that `base + added` rule | Delete that rule | — |
| Compost | Append to `values` | — | Delete matching entries (new values can be added back with `values`) |

- Writing only `outputs`/`drops` **without** `patch` treats the file as an ordinary file. When an entry for the same input already exists, each table follows its own rule: the sieve overrides it and logs a `duplicate` warning; for the hammer / crook / melting point the later entry is skipped.
- The presence of `"remove"` automatically treats the file as a patch; `patch: true` is for "append-only" patches.
- Patches are applied after ordinary files, sorted by resource id; when several patches change the same entry, later ones see the earlier results.

## 3. Sieve `sieve/`

**One file = one input item + a drop list.**

```json
{
  "input": "minecraft:sand",
  "outputs": [
    { "item": "exnihilosatellitium:iron_crushed", "chance": 0.2 },
    { "item": "minecraft:diamond", "chance": 0.02 },
    { "item": "exnihilosatellitium:cobblestone_pebble", "chance": 1.0, "min": 4, "max": 6 }
  ]
}
```

| Field | Required | Description |
|---|---|---|
| `input` | ✅ | Item id or `#item tag` |
| `outputs` | ✅ | Drop array |
| `outputs[].item` | ✅ | Output item id |
| `outputs[].chance` | ✅ | `0 < chance <= 1`, an **independent chance**, not a weight |
| `outputs[].min` / `max` | ❌ | How many items after a successful roll, default 1; inclusive range |
| `outputs[].mod` | ❌ | This entry only participates while the given mod is present |

- Every sift rolls each drop independently, so several can succeed at once.
- The sieve mesh tier only affects durability, not the drop table.
- The machine sieve and the manual sieve read the same table, so table edits propagate to the machine automatically.

**Patch example: remove cactus, add a 50% diamond chance**

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"],
  "outputs": [ { "item": "minecraft:diamond", "chance": 0.5 } ]
}
```

## 4. Hammer `hammer/`

**One file = one block → one output + a count range.**

```json
{
  "input": "minecraft:cobblestone",
  "result": "minecraft:gravel",
  "min": 1,
  "max": 2
}
```

| Field | Required | Description |
|---|---|---|
| `input` | ✅ | Block id or `#block tag` |
| `result` | ✅ | Output item id |
| `min` / `max` | ❌ | 1..64, default 1 |

- A block can have only one hammer rule; tags do not override an existing specific rule.
- To change an existing rule use `"patch": true`; to delete one use `"remove": true`.

## 5. Crook `crook/`

**One file = one leaves block → appended drops.**

```json
{
  "input": "minecraft:oak_leaves",
  "drops": [
    { "item": "minecraft:stick", "chance": 0.1 },
    { "item": "minecraft:apple", "chance": 0.02 }
  ]
}
```

| Field | Required | Description |
|---|---|---|
| `input` | ✅ | Block id or `#block tag` |
| `drops[].item` | ✅ | Item id |
| `drops[].chance` | ✅ | `0 < chance <= 1` |
| `drops[].mod` | ❌ | Soft-dependency gate |

The crook only **appends** drops; vanilla leaves drops are unaffected.

## 6. Barrel transformations `barrel/`

**One file = one `base + added` rule.**

```json
{
  "base": "minecraft:water",
  "added": { "fluid": "minecraft:lava" },
  "result": "minecraft:cobblestone"
}
```

```json
{
  "base": "minecraft:lava",
  "added": { "item": "minecraft:redstone" },
  "result": "minecraft:netherrack"
}
```

| Field | Required | Description |
|---|---|---|
| `base` | ✅ | Fluid the barrel must be **filled with 1000 mB** of; `#fluid tag` allowed |
| `added` | ✅ | Exactly one `fluid` or `item`; both accept tags |
| `result` | ✅* | Block/item id (use the id of the block's item) |
| `entity` | ✅* | Entity id to spawn; mutually exclusive with `result` |
| `peaceful_drop` | Required for entity rules | Peaceful-mode drop, a string or an array |
| `cook_ticks` | ❌ | Legacy field kept only for old-save compatibility; new rules take effect immediately on right-click |

- `base` / `added.fluid` expand only **still** fluids; the flowing variants of `#minecraft:water`
  are dropped (a barrel can only hold still).
- **Item rules take priority over the fluid-container path**: a milk bucket is both an item and a fluid container, so `added.item` is matched first;
  after consumption, `getCraftingRemainingItem()` is returned to the player (milk bucket → empty bucket).
- Entity-spawning rules:

  ```json
  { "base": "minecraft:lava", "added": { "item": "exnihilosatellitium:angry_doll" },
    "entity": "minecraft:blaze", "peaceful_drop": "minecraft:blaze_rod" }
  ```

**Deleting a built-in rule:**

```json
{ "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" }, "remove": true }
```

## 7. Compost `compost/`

```json
{
  "values": {
    "#minecraft:leaves": 125,
    "#minecraft:saplings": 125,
    "minecraft:carrot": 80,
    "minecraft:bread": 125
  }
}
```

- The key is an item id or `#item tag`; the value is a positive integer.
- When one stack matches several entries, the **maximum** is used, not the sum.
- The total reaching `1000` fills it, then it turns into dirt in 50 seconds.
- **You must also edit the item tag** `#exnihilosatellitium:compostable`, otherwise it cannot be composted;
  present in the tag but missing from the JSON → only 1 point.

Tag file (1.21.1):

`data/exnihilosatellitium/tags/item/compostable.json`

```json
{
  "replace": false,
  "values": [
    "minecraft:rotten_flesh",
    "minecraft:bread"
  ]
}
```

In 1.20.1 the path is `data/exnihilosatellitium/tags/items/compostable.json`.

**Patch: change bread from 125 to 100**

```json
{
  "remove": ["minecraft:bread"],
  "values": { "minecraft:bread": 100 }
}
```

## 8. Crucible melting point `crucible_melt/`

```json
{
  "input": "minecraft:cobblestone",
  "fluid": "minecraft:lava",
  "amount": 250,
  "solid": 2000
}
```

| Field | Required | Description |
|---|---|---|
| `input` | ✅ | Block id or `#block tag` |
| `fluid` | ✅ | Fluid id |
| `amount` | ✅ | 1..64000 mB |
| `solid` | ❌ | Classic solid-amount record; defaults to `amount`, capped at 64000 |

## 9. Crucible heat source `crucible_heat/`

```json
{ "block": "minecraft:torch", "speed": 0.1 }
```

```json
{
  "block": "minecraft:furnace",
  "properties": { "lit": "true" },
  "speed": 0.15
}
```

| Field | Required | Description |
|---|---|---|
| `block` | ✅ | Block id or `#block tag` |
| `properties` | ❌ | All must match; used to tell lit from unlit |
| `speed` | ✅ | mB/tick, `0 < speed <= 4` |

A heat source patch carrying `properties` deletes/replaces only the rules for the same state; without it, all rules for that block are deleted.

## 10. Vanilla recipes (crafting / smelting)

Beyond the seven tables, crafting/smelting is still vanilla `RecipeManager`, so datapack JSON can be added directly.

### 1.21.1 Shaped crafting

`data/mypack/recipe/barrel_from_logs.json`

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "LLL",
    "L L",
    "LLL"
  ],
  "key": {
    "L": { "item": "minecraft:oak_log" }
  },
  "result": { "id": "exnihilosatellitium:barrel", "count": 1 }
}
```

### 1.20.1 The same recipe

The path becomes `data/mypack/recipes/`, and `result` becomes:

```text
"result": { "item": "exnihilosatellitium:barrel", "count": 1 }
```

### Smelting

1.21.1:

```json
{
  "type": "minecraft:smelting",
  "ingredient": { "item": "minecraft:iron_ore" },
  "result": { "id": "minecraft:iron_ingot", "count": 1 },
  "experience": 0.7,
  "cookingtime": 200
}
```

In 1.20.1 change `result` to `{ "item": "minecraft:iron_ingot", "count": 1 }`.

> **A datapack cannot delete vanilla recipes directly.** When you need to remove a recipe, use [KubeJS](kubejs.md) or
> [CraftTweaker](crafttweaker.md).

## 11. Self-check

```mcfunction
/reload
/exnihilo tables
/exnihilo dump sieve
/exnihilo dump all
/exnihilo sieve minecraft:sand
/exnihilo hammer minecraft:cobblestone
/exnihilo compost minecraft:bread
```

- `/exnihilo dump <table>` prints the whole table as sorted text (`sieve` / `hammer` / `crook` / `barrel` /
  `compost` / `crucible_melt` / `crucible_heat`), which you can use to confirm exactly which rows a patch changed.
- `/exnihilo customize ...` is a P1 internal debug entry point (it queues directly into the runtime overlay, writes no files,
  and is cleared on restart); it is not a player feature. The official KJS / CT entry points are P2 / P3 in the roadmap.

Log prefixes:

- `[sieve]` / `[hammer]` / `[crook]` / `[barrel]` / `[compost]` / `[crucible]`
- `empty or unknown tag`: tag misspelled or the corresponding mod is not installed
- `unknown input/result`: id misspelled, or the mod is absent
- `duplicate input`: the same input was matched by several files; the sieve lets the later one override, the hammer / crook / melting point skip the later one — check the log in either case

## 12. Client-side JEI

- Single player / LAN: JEI reads the server tables, so datapack changes show up.
- **Dedicated server**: the client cannot read server datapacks, so JEI still shows the values built into the jar; actual drops/crafting follow the server.
  This is a current limitation, not a datapack that failed to load.
