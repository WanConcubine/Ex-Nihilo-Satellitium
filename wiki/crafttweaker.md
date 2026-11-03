# CraftTweaker：只改原版配方

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1

先说结论：**CraftTweaker 改不了本模组的七张玩法表。**

原因不是“没写文档”，而是结构上的：

- 七张表（`sieve/`、`hammer/`、`crook/`、`barrel/`、`compost/`、`crucible_melt/`、
  `crucible_heat/`）是**自定义 JSON 注册表**，不是 vanilla `RecipeManager` 里的 `Recipe`。
- 本模组**没有注册 CT recipe type**，所以不存在
  `<recipetype:exnihilosatellitium:sieve>` 这种写法。
- CT 的 `craftingTable` / `furnace` / `recipes` 只看得见 vanilla 配方。

CT 能做的是：**删/改/加原版合成与熔炼**。如果你的目标只是“把木桶合成改简单点”，
CT 够用；如果目标是“改筛子掉落/桶转化/堆肥值”，请用
[数据包](datapack.md) 或 [KubeJS 的 data 通道](kubejs.md)。

## 1. CT 能做什么

| 目标 | 能否 |
|---|---|
| 删掉本模组的木桶/筛子/坩埚合成 | ✅ |
| 加自己的合成配方 | ✅ |
| 改熔炼（矿石 → 锭） | ✅ |
| 改筛子掉落、锤子产物、桶转化、堆肥值、坩埚熔点/热源 | ❌ |
| 新增物品/方块 | ❌ |
| 新增材料（`materials.json`） | ❌ |
| 给堆肥加 `compostable` 标签 | 用数据包/KJS 更稳 |

## 2. 删配方

1.20.1 / 1.21.1 的 CT 用 `craftingTable`（不是 1.12 教程里的 `recipes.addShaped`）：

```zenscript
// scripts/remove_barrel.zs

// 按输出删：所有产出木桶的合成配方
craftingTable.remove(<item:exnihilosatellitium:barrel>);
```

也可以按配方 id 删（id 就是 `data/<ns>/recipe/<name>.json` 的 `<name>`）：

```zenscript
craftingTable.removeByName("exnihilosatellitium:barrel");
```

> 不同 CT 版本的 `removeByName` 可能叫法不同；不确定时用按输出删。

## 3. 加合成

### 有序合成

```zenscript
craftingTable.addShaped("barrel_from_logs", <item:exnihilosatellitium:barrel>, [
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:air>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>]
]);
```

- 空格写 `<item:minecraft:air>`（CT 1.20.1/1.21.1 文档的写法）；不要用 `null`。
- 配方名（第一个参数）自己取，避免和别的脚本重名。

### 无序合成

```zenscript
craftingTable.addShapeless("barrel_from_planks", <item:exnihilosatellitium:barrel>, [
  <item:minecraft:oak_planks>,
  <item:minecraft:oak_planks>,
  <item:minecraft:oak_planks>,
  <item:minecraft:oak_planks>
]);
```

### 熔炼

```zenscript
furnace.addRecipe("iron_from_dust", <item:minecraft:iron_ingot>, <item:exnihilosatellitium:iron_ore_dust>, 0.7, 200);
```

参数顺序：配方名、输出、输入、经验、时间（tick）。

## 4. 为什么不能写 `<recipetype:...>`？

CT 的自定义配方类型需要模组在 Java 侧注册：

- 一个 `RecipeType` / serializer；
- 一个 CT 的 `IRecipeManager` 或脚本注册器；
- JEI 侧对应的 category。

本模组这七张表走的是自己的 reload listener，**没有进 `RecipeManager`**，
所以 CT 的 `recipes` 集合里根本没有它们。即使某些 CT 版本提供
`addJsonRecipe` 之类的通用入口，它最终也只往 vanilla `RecipeManager` 里塞，
**不会**让筛子表多一条；本模组也没有可供 CT 引用的 recipe type。

## 5. 替代方案

| 需求 | 用什么 |
|---|---|
| 改筛子/锤子/钩子/桶/堆肥/坩埚 | [数据包](datapack.md)，或 KJS 的 `kubejs/data` |
| 删/改原版合成、熔炼 | CT（本页）或 KJS `ServerEvents.recipes` |
| 加完全属于自己的物品 | KJS `StartupEvents.registry`；CT 做不到 |
| 加第 14 种材料 | 只能改模组 jar / 等材料表支持外部配置 |

如果整合包同时有 CT 和 KJS，**同一个配方不要两边都改**，否则谁后加载谁覆盖，
排查会很痛苦。

## 6. 重载与日志

- CT 脚本改动后按你的 CT 版本文档重载（通常是 `/reload`，部分版本是 `/ct reload`）。
- 报错看 `logs/crafttweaker.log`；ZenScript 语法错误会带行号。
- 不确定配方 id 时，用 CT 的 `/ct dump` 系列命令（取决于版本）或看
  `data/<ns>/recipe/` 文件名。
- CT 只影响 vanilla 配方；**七张表是否生效，用 `/exnihilo tables` 查，CT 的日志里不会出现它们**。

## 7. 什么时候需要“CT 支持”

如果你确实希望在 CT 里写：

```zenscript
// 目前不存在
sieve.add(<item:minecraft:sand>, <item:minecraft:diamond>, 0.01);
```

那需要模组新增 Java 集成：注册自定义 recipe type / CT 脚本注册器，并把条目写进
`SieveRegistry`。这不在当前版本范围内；目前请用数据包或 KJS。

## 未来

本页的“只能改原版配方”是**当前版本**的边界。三路径等价（CT 直接
`mods.exnihilosatellitium.sieve.add(...)`）是路线图 P3 的目标；在那之前，
CT 用户请用数据包或 KJS 的 `kubejs/data`。进度见
[`docs/customization-roadmap.md`](../docs/customization-roadmap.md)。

## 参考

- [CraftingTable（1.21.1）](https://docs.blamejared.com/1.21.1/en/tutorial/Recipes/Crafting/crafting_table/)
- [Recipe Managers（1.21.1）](https://docs.blamejared.com/1.21.1/en/tutorial/Recipes/RecipeManagers/)
- 1.20.1 的文档把版本号换成 `1.20.1` 即可；`craftingTable` / `furnace` 这类全局变量两版相同。