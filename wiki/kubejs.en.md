# KubeJS: Datapack Channel + Script API for Seven Tables

> Last checked: 2026-11-03 · Applies to: 1.21.1 / 1.20.1
> KubeJS versions: 1.21.1 uses `2101.x`, 1.20.1 uses `2001.x`; binding names and call syntax are the same

KubeJS now has two channels:

| What you want to do | How |
|---|---|
| Change sieve / hammer / crook / barrel / compost / crucible | **script API** (`SieveEvents` etc., see §1) or `kubejs/data` JSON |
| Add / remove / change vanilla crafting, smelting | `ServerEvents.recipes` |
| Change item tags | `ServerEvents.tags` |
| Register your own items / blocks | `StartupEvents.registry` (KJS content only) |
| Add new materials for this mod (`materials.json`) | ❌ Not possible |

## 1. Script API for the seven tables (both versions)

Put the script in `kubejs/server_scripts/`, e.g. `exnihilo_tables.js`:

```js
// 筛子：沙子里加 0.02 的钻石；一次成功掉 1 个
SieveEvents.add('minecraft:sand', 'minecraft:diamond', 0.02)

// 第 4 个参数是 min，第 5 个是 max（省略时都是 1）
SieveEvents.add('minecraft:dirt', 'minecraft:oak_sapling', 0.08, 1, 2)

// 删掉落 / 删整个输入 / 整条替换
SieveEvents.remove('minecraft:sand', 'minecraft:cactus')
SieveEvents.removeInput('minecraft:gravel')
SieveEvents.replace('minecraft:sand', 'minecraft:emerald', 1.0)

// 锤子 / 钩子
HammerEvents.add('minecraft:cobblestone', 'minecraft:gravel')
HammerEvents.add('minecraft:oak_log', 'minecraft:stick', 1, 2)
HammerEvents.remove('minecraft:cobblestone')

CrookEvents.add('minecraft:oak_leaves', 'minecraft:stick', 0.1)

// 桶：物品 / 流体两种 added
BarrelEvents.addItem('minecraft:water', 'minecraft:sugar', 'minecraft:dirt')
BarrelEvents.addFluid('minecraft:water', 'minecraft:milk_bucket', 'minecraft:slime_block')

// 堆肥 / 坩埚
CompostEvents.add('minecraft:bread', 100)
CompostEvents.add('#minecraft:flowers', 100)       // 标签也可以
CrucibleMeltEvents.add('minecraft:cobblestone', 'minecraft:lava', 250)
CrucibleHeatEvents.add('minecraft:torch', 0.1)
```

### Semantics

- All ids can be written as `#tag` (e.g. `#minecraft:logs`, `#forge:gravel`).
- `add` = normal datapack file semantics: sieve/crook **merge drops**; hammer/melt/heat source/barrel are "one rule per key",
  first one wins, and script `add` **cannot** override existing rules; to change an existing rule use `replace`.
- `remove` = delete the whole rule; for sieve/crook you can also use `remove(input, output)` to delete just one drop.
- Script operations are applied **after** the datapack and patches (the script is the last override layer), with a fixed priority of `100`.
- On script reload old contributions are cleared first, then re-submitted per the new scripts; `/reload` does not stack duplicates.
- After changes run `/reload`; `/exnihilo tables` shows counts, `/exnihilo dump <table>` shows the whole table, and the
  `[customize]` log shows who overrode what.
- The KJS 2001 API in 1.20.1 differs from 2101 in 1.21.1, but the binding objects and syntax this mod exposes are exactly the same;
  the same script has been verified on both versions.

### JSON escape hatch

For complex structures (e.g. barrel rules with entity / peaceful drops) you can write the full JSON directly:

```js
SieveEvents.addJson('{"input":"minecraft:sand","outputs":[{"item":"minecraft:diamond","chance":0.02}]}')
SieveEvents.removeJson('{"input":"minecraft:sand","remove":["minecraft:cactus"]}')
SieveEvents.replaceJson('{"input":"minecraft:sand","outputs":[]}')

BarrelEvents.addJson('{"base":"minecraft:witch_water","added":{"item":"exnihilosatellitium:creepy_doll"},"entity":"minecraft:enderman","peaceful_drop":"minecraft:ender_pearl","cook_ticks":600}')
```

All seven tables have `addJson` / `removeJson` / `replaceJson`. The JSON fields are exactly the same as the [datapack page](datapack.md);
the new barrel fields `result_fluid` / `delay_ticks` / `drain` also work through `addJson` (see §6 there).
`dump` output can also be used directly as a fixture.

### Common pitfalls

- **Same item on the sieve with different chances = two independent drops**: `add(sand, diamond, 0.02)` and
  `add(sand, diamond, 0.5)` both stay, and are in fact two separate rolls.
- **Hammer `add` does not override an existing rule**: while the built-in `cobblestone → gravel` is still there, `add(cobblestone, flint)`
  is ignored and logs `add ignored`; use `replace` to change it.
- **Do not write the same rule with both scripts and `kubejs/data`**: scripts are applied after files, so scripts usually win;
  but writing both makes troubleshooting harder. Pick one source.
- **The material table is not involved**: scripts cannot add materials to `materials.json`, nor make KJS items enter the
  4→block→smelting chain.

## 2. The `kubejs/data` channel (works on both versions)

KubeJS loads `kubejs/data` as a datapack, with the same directory structure as a normal datapack:

```
kubejs/data/mypack/sieve/sand_patch.json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"],
  "outputs": [{ "item": "minecraft:diamond", "chance": 0.5 }]
}
```

- Prefer your own namespace + `"patch": true`; do not rely on override order.
- The fields, `#tag`, `mod` gate, and `remove` rules for the seven tables match the [datapack page](datapack.md).
- If KJS is not installed / you do not want to write scripts, this channel works on both versions.

## 3. Vanilla recipes: use scripts

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'exnihilosatellitium:barrel' })
  event.shaped('exnihilosatellitium:barrel', ['LLL', 'L L', 'LLL'], { L: 'minecraft:oak_log' })
  event.smelting('minecraft:iron_ingot', 'minecraft:iron_ore')
})
```

## 4. Tags: `ServerEvents.tags`

Compost needs **both a JSON value and the `compostable` tag**; the script API's `CompostEvents.add` only writes the value,
so the tag still has to be added:

```js
ServerEvents.tags('item', event => {
  event.add('exnihilosatellitium:compostable', 'minecraft:rotten_flesh')
})
```

## 5. Reload and logs

- After changing scripts / `kubejs/data`, run `/reload` first; scripts are re-executed on server reload.
- This mod's script contributions are cleared before every script reload, so changing scripts leaves no old entries.
- See `logs/kubejs/server.log`; on this mod's side see `/exnihilo tables`, `/exnihilo dump`, and the
  `[customize]` log.

## 6. What KJS cannot do

- Cannot change `materials.json`, nor make KJS items enter this mod's material chain.
- Cannot register this mod's blocks/items (KJS's own content is fine).
- On dedicated servers the client JEI still cannot read server tables (P4 client sync).

## Reference

- KubeJS docs: <https://kubejs.com/>
- Seven-table fields: [datapack page](datapack.md); three-path progress: [roadmap](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/customization-roadmap.md).
