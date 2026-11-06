# Cookbook: Common Changes

> Last checked: 2026-11-06 · Applies to: 1.21.1 / 1.20.1

> The seven tables are written **exactly the same way** for datapacks and KubeJS; KJS just puts the files under `kubejs/data/`.
> Below, `data/mypack/...` stands for the datapack path; for KJS, swap in `kubejs/data/mypack/...`.

## 1. Add 2% diamond to sand

`data/mypack/sieve/sand.json`

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "outputs": [
    { "item": "minecraft:diamond", "chance": 0.02 }
  ]
}
```

## 2. Remove cactus from sand

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"]
}
```

## 3. Add 1% diamond to all gravel

1.21.1:

```json
{ "input": "#c:gravels", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

1.20.1:

```json
{ "input": "#forge:gravel", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

## 4. Remove an entire input (e.g. dust)

```json
{ "input": "exnihilosatellitium:dust", "remove": true }
```

## 5. Change compost: bread 125 → 100

`data/mypack/compost/bread.json`

```json
{
  "remove": ["minecraft:bread"],
  "values": { "minecraft:bread": 100 }
}
```

If bread isn't in the table to begin with, just write `values`; also make sure
`#exnihilosatellitium:compostable` contains bread.

## 6. Add a barrel transformation: water + sugar → dirt

```json
{
  "base": "minecraft:water",
  "added": { "item": "minecraft:sugar" },
  "result": "minecraft:dirt"
}
```

Remove a built-in rule:

```json
{ "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" }, "remove": true }
```

## 7. Change the hammer: cobblestone → 2 gravel

```json
{
  "input": "minecraft:cobblestone",
  "patch": true,
  "result": "minecraft:gravel",
  "min": 2,
  "max": 2
}
```

## 8. Add a crucible heat source: campfire 0.2 mB/t

Both 1.21.1 / 1.20.1 have `#minecraft:campfires`:

```json
{ "block": "#minecraft:campfires", "speed": 0.2 }
```

## 9. Add a crucible melting point: sapling → 100 mB water

```json
{ "input": "#minecraft:saplings", "fluid": "minecraft:water", "amount": 100 }
```

## 10. Change the barrel recipe (vanilla recipe)

A datapack can only **add**, not remove; to replace a built-in recipe use KJS or CT.

**KJS:**

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'exnihilosatellitium:barrel' })
  event.shaped('exnihilosatellitium:barrel', [
    'LLL',
    'L L',
    'LLL'
  ], { L: 'minecraft:oak_log' })
})
```

**CraftTweaker:**

```zenscript
craftingTable.remove(<item:exnihilosatellitium:barrel>);
craftingTable.addShaped("barrel_from_logs", <item:exnihilosatellitium:barrel>, [
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:air>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>]
]);
```

## 11. Remove a recipe

- **KJS**: `event.remove({ id: '...' })` or `event.remove({ output: '...' })`
- **CT**: `craftingTable.remove(<item:...>);`
- **Datapack**: you can't "delete"; you can only overwrite the same path with another recipe (not recommended).

## 12. Add an item of your own

Only KJS can do this:

```js
// kubejs/startup_scripts/items.js
StartupEvents.registry('item', event => {
  event.create('mypack:tin_broken').texture('mypack:item/tin_broken')
})
```

Then write it into the drops in `kubejs/data/mypack/sieve/*.json`, or write a crafting recipe for it.
It will **not** enter this mod's material chain / creative tab group.

## 13. Add a new material

None of the three routes can do this. The material table is `materials.json` inside the jar and is read at registration time;
a new material also needs a texture, model, recipe, tag, and JEI group. You'd have to change the mod source, or open an
issue/PR following [Material Chain & Mod Integration](material-chain.md).

## 14. Change several tables at once

One datapack can hold several directories at once:

```
data/mypack/
├── sieve/
├── hammer/
├── barrel/
└── compost/
```

`/reload` applies it all at once; use `/exnihilo tables` to see the summary.

## 15. Temporarily disable a batch of your own entries

Add a nonexistent mod gate at the file root:

```json
{ "mod": "mypack_disabled", "input": "#c:gravels", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

This only disables **this file**, not the jar's built-in tables; built-in entries must be removed one by one with `remove` patches.
