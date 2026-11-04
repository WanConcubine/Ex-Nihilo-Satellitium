# CraftTweaker：七张表脚本 API + 原版配方

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1
> CT 版本：1.21.1 用 CraftTweaker 21.x；1.20.1 用 14.x；两版入口命名相同

CraftTweaker 现在有两条用法：

| 你想做的 | 怎么做 |
|---|---|
| 改筛子 / 锤子 / 钩子 / 桶 / 堆肥 / 坩埚 | **ZenScript API**（`mods.exnihilosatellitium.*`，见 §1） |
| 加 / 删 / 改原版合成、熔炼 | `craftingTable` / `furnace`（§3） |
| 改物品标签 | 用数据包 / KJS 更稳 |
| 新增本模组物品 / 材料 | ❌ 做不到 |

> 七张表不是 vanilla `RecipeManager` 的 recipe，所以 CT 的配方浏览器里不会出现
> `<recipetype:exnihilosatellitium:sieve>`；这里提供的是**普通 ZenScript 方法**，
> 能改表，但不注册 recipe type。JEI 仍看本模组自己的分类。

## 1. 七张表的 ZenScript API

把脚本放在 `scripts/`，例如 `scripts/exnihilo_tables.zs`：

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

### 语义

- 所有 id 都可以写 `#tag`（`#minecraft:logs`、1.20.1 的 `#forge:gravel`）。
- `add` = 数据包普通文件语义：筛子/钩子**合并掉落**；锤子/熔点/热源/桶是
  “一条规则一个 key”，先到者赢，`add` **覆盖不了**已有规则，要改用 `replace`。
- `remove` = 删整条；筛子/钩子也可以用 `remove(input, output)` 只删一条掉落。
- CT 的 priority 固定为 **200**：数据包（0）和 KubeJS（100）之后应用，所以脚本是最后一层覆盖。
- 改完 `/reload`；本模组会在脚本批次前清掉旧的 `crafttweaker` 贡献，不会重复叠加。
- 诊断：`/exnihilo tables`、`/exnihilo dump <table>`、`[customize]` 日志。

### JSON 逃生口

复杂结构直接写完整 datapack JSON：

```zenscript
mods.exnihilosatellitium.Sieve.addJson('{"input":"minecraft:sand","outputs":[{"item":"minecraft:diamond","chance":0.02}]}');
mods.exnihilosatellitium.Sieve.removeJson('{"input":"minecraft:sand","remove":["minecraft:cactus"]}');
mods.exnihilosatellitium.Sieve.replaceJson('{"input":"minecraft:sand","outputs":[]}');

mods.exnihilosatellitium.Barrel.addJson('{"base":"minecraft:witch_water","added":{"item":"exnihilosatellitium:creepy_doll"},"entity":"minecraft:enderman","peaceful_drop":"minecraft:ender_pearl","cook_ticks":600}');
```

七张表都有 `addJson` / `removeJson` / `replaceJson`；字段与[数据包页面](datapack.md)一致。
桶的新字段 `result_fluid` / `delay_ticks` / `drain` 同样通过 `addJson` 生效（见数据包页 §6）。

### 容易踩的坑

- **筛子同物品不同概率 = 两条独立掉落**：`add(sand, diamond, 0.02)` 和
  `add(sand, diamond, 0.5)` 都会留下，实际是两次独立判定。
- **锤子 `add` 不会覆盖已有规则**：内置 `cobblestone → gravel` 还在时，
  `add(cobblestone, flint)` 会被忽略并打 `add ignored`；要改请用 `replace`。
- **不要同时用 CT 和 KJS 写同一条**：CT priority 200 在 KJS 100 之后，通常 CT 赢；
  两边都写会让排查变复杂。
- 本模组还没注册 CT recipe type，所以 **JEI 的 CT 配方页看不到这些条目**；
  用 `/exnihilo dump` 验证。

## 2. 原版配方：`craftingTable` / `furnace`

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

- 空格写 `<item:minecraft:air>`，不要用 `null`。
- 配方名自己取，避免和别的脚本重名。
- 不同 CT 版本的 `removeByName` 可能叫法不同；不确定时用按输出删。

## 3. 为什么没有 `<recipetype:...>`？

CT 的自定义 recipe type 需要模组注册 `RecipeType` / serializer / `IRecipeManager` 和 JEI category；
本模组七张表走自己的 reload listener，没有进 `RecipeManager`。P3 选择先提供 ZenScript 方法，
“能改表”优先；recipe type / JEI 展示后置。所以脚本写 `mods.exnihilosatellitium.Sieve.add(...)`，
而不是 `recipes.addJsonRecipe` 或 `<recipetype:exnihilosatellitium:sieve>`。

## 4. 重载与日志

- CT 脚本改动后 `/reload`（或你的 CT 版本对应的 reload 命令）。
- 本模组的 CT 贡献在脚本批次前清空，改脚本不会残留旧条目。
- 报错看 `logs/crafttweaker.log`；本模组侧看 `/exnihilo tables`、`/exnihilo dump` 和
  `[customize]` 日志。

## 5. CT 做不到的

- 不能改 `materials.json`，也不能让 CT 物品进入本模组材料链。
- 不能注册本模组的方块/物品。
- 没有 CT recipe type，JEI 不显示七张表的 CT 配方页。
- 专用服客户端 JEI 仍读不到服务端表（P4 客户端同步）。

## 参考

- [CraftingTable（1.21.1）](https://docs.blamejared.com/1.21.1/en/tutorial/Recipes/Crafting/crafting_table/)
- [Recipe Managers（1.21.1）](https://docs.blamejared.com/1.21.1/en/tutorial/Recipes/RecipeManagers/)
- 1.20.1 把文档版本号换成 `1.20.1`；`craftingTable` / `furnace` 这类全局变量两版相同。
