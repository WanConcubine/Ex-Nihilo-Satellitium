# CraftTweaker: script API for the seven tables + vanilla recipes

> Last checked: 2026-11-03 · Applies to: 1.21.1 / 1.20.1
> CT version: CraftTweaker 21.x for 1.21.1; 14.x for 1.20.1; the entry point names are the same in both versions

CraftTweaker now has two ways of being used:

| What you want to do | How |
|---|---|
| Change sieve / hammer / crook / barrel / compost / crucible | **ZenScript API** (`mods.exnihilosatellitium.*`, see §1) |
| Add / remove / change vanilla crafting, smelting | `craftingTable` / `furnace` (§3) |
| Change item tags | datapack / KJS is more reliable |
| Add new mod items / materials | ❌ not possible |

> The seven tables are not vanilla `RecipeManager` recipes, so `<recipetype:exnihilosatellitium:sieve>` does not
> appear in CT's recipe browser; what is provided here are **plain ZenScript methods** —
> they can change the tables, but they do not register a recipe type. JEI still uses the mod's own categories.

## 1. The ZenScript API for the seven tables

Put the script in `scripts/`, for example `scripts/exnihilo_tables.zs`:

```zenscript
// 筛子：沙子里加 0.02 的钻石；一次成功掉 1 个
mods.exnihilosatellitium.Sieve.add("minecraft:sand", "minecraft:diamond", 0.02);

// 第 4/5 个参数是 min/max（省略时都是 1）
mods.exnihilosatellitium.Sieve.add("minecraft:dirt", "minecraft:oak_sapling", 0.08, 1, 2);

// 删掉落 / 删整个输入 / 整条替换
mods.exnihilosatellitium.Sieve.remove("minecraft:sand", "minecraft:cactus");
mods.exnihilosatellitium.Sieve.removeInput("minecraft:gravel");
mods.exnihilosatellitium.Sieve.replace("minecraft:sand", "minecraft:emerald", 1.0);

// 锤子 / 钩子
mods.exnihilosatellitium.Hammer.add("minecraft:cobblestone", "minecraft:gravel");
mods.exnihilosatellitium.Hammer.add("minecraft:oak_log", "minecraft:stick", 1, 2);
mods.exnihilosatellitium.Hammer.remove("minecraft:cobblestone");

mods.exnihilosatellitium.Crook.add("minecraft:oak_leaves", "minecraft:stick", 0.1);

// 桶：物品 / 流体两种 added
mods.exnihilosatellitium.Barrel.addItem("minecraft:water", "minecraft:sugar", "minecraft:dirt");
mods.exnihilosatellitium.Barrel.addFluid("minecraft:water", "minecraft:milk_bucket", "minecraft:slime_block");

// 堆肥 / 坩埚
mods.exnihilosatellitium.Compost.add("minecraft:bread", 100);
mods.exnihilosatellitium.Compost.add("#minecraft:flowers", 100);
mods.exnihilosatellitium.CrucibleMelt.add("minecraft:cobblestone", "minecraft:lava", 250);
mods.exnihilosatellitium.CrucibleHeat.add("minecraft:torch", 0.1);
```

### Semantics

- All ids can be written as `#tag` (`#minecraft:logs`, `#forge:gravel` on 1.20.1).
- `add` = the semantics of a normal datapack file: sieve/crook **merge drops**; hammer/melt point/heat
  source/barrel are "one rule per key", first one wins, and `add` **cannot override** an existing rule —
  use `replace` instead.
- `remove` = delete the whole rule; for sieve/crook you can also use `remove(input, output)` to delete
  a single drop.
- CT's priority is fixed at **200**: applied after datapacks (0) and KubeJS (100), so scripts are the last
  layer of override.
- Run `/reload` after changing things; the mod clears old `crafttweaker` contributions before the script
  batch, so nothing stacks up twice.
- Diagnostics: `/exnihilo tables`, `/exnihilo dump <table>`, the `[customize]` log.

### JSON escape hatch

For complex structures, write the full datapack JSON directly:

```zenscript
mods.exnihilosatellitium.Sieve.addJson('{"input":"minecraft:sand","outputs":[{"item":"minecraft:diamond","chance":0.02}]}');
mods.exnihilosatellitium.Sieve.removeJson('{"input":"minecraft:sand","remove":["minecraft:cactus"]}');
mods.exnihilosatellitium.Sieve.replaceJson('{"input":"minecraft:sand","outputs":[]}');

mods.exnihilosatellitium.Barrel.addJson('{"base":"minecraft:witch_water","added":{"item":"exnihilosatellitium:creepy_doll"},"entity":"minecraft:enderman","peaceful_drop":"minecraft:ender_pearl","cook_ticks":600}');
```

All seven tables have `addJson` / `removeJson` / `replaceJson`; the fields match the
[datapack page](datapack.md).

### Common pitfalls

- **Sieve: same item with different chances = two independent drops**: both `add(sand, diamond, 0.02)`
  and `add(sand, diamond, 0.5)` remain, which in practice is two independent rolls.
- **Hammer `add` does not override an existing rule**: while the built-in `cobblestone → gravel` is still
  there, `add(cobblestone, flint)` is ignored and logs `add ignored`; use `replace` to change it.
- **Do not write the same rule with both CT and KJS**: CT priority 200 comes after KJS 100, so CT usually
  wins; having both makes troubleshooting harder.
- The mod does not register a CT recipe type yet, so **these entries do not appear on JEI's CT recipe
  pages**; verify with `/exnihilo dump`.

## 2. Vanilla recipes: `craftingTable` / `furnace`

```zenscript
// 按输出删：所有产出木桶的合成配方
craftingTable.remove(<item:exnihilosatellitium:barrel>);

// 按配方 id 删
craftingTable.removeByName("exnihilosatellitium:barrel");

// 加有序合成
craftingTable.addShaped("barrel_from_logs", <item:exnihilosatellitium:barrel>, [
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:air>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>]
]);

// 加熔炼：配方名、输出、输入、经验、时间（tick）
furnace.addRecipe("iron_from_dust", <item:minecraft:iron_ingot>, <item:exnihilosatellitium:iron_ore_dust>, 0.7, 200);
```

- Use `<item:minecraft:air>` for empty slots, not `null`.
- Pick your own recipe names to avoid collisions with other scripts.
- `removeByName` may be named differently in different CT versions; when unsure, remove by output.

## 3. Why is there no `<recipetype:...>`?

A CT custom recipe type requires the mod to register a `RecipeType` / serializer / `IRecipeManager` and a
JEI category; the mod's seven tables go through their own reload listener and never enter `RecipeManager`.
P3 chose to provide ZenScript methods first, prioritising "being able to change the tables"; recipe
type / JEI display comes later. So scripts call `mods.exnihilosatellitium.Sieve.add(...)`, not
`recipes.addJsonRecipe` or `<recipetype:exnihilosatellitium:sieve>`.

## 4. Reload and logs

- `/reload` after changing CT scripts (or whatever reload command your CT version has).
- The mod's CT contributions are cleared before the script batch, so editing scripts leaves no stale entries.
- For errors see `logs/crafttweaker.log`; for the mod's side see `/exnihilo tables`, `/exnihilo dump` and
  the `[customize]` log.

## 5. What CT cannot do

- It cannot change `materials.json`, nor make CT items enter the mod's material chain.
- It cannot register the mod's blocks/items.
- There is no CT recipe type, so JEI shows no CT recipe pages for the seven tables.
- On dedicated servers the client-side JEI still cannot read the server tables (P4 client sync).

## Reference

- [CraftingTable (1.21.1)](https://docs.blamejared.com/1.21.1/en/tutorial/Recipes/Crafting/crafting_table/)
- [Recipe Managers (1.21.1)](https://docs.blamejared.com/1.21.1/en/tutorial/Recipes/RecipeManagers/)
- For 1.20.1, swap the docs version number to `1.20.1`; globals such as `craftingTable` / `furnace` are the same in both versions.
