# 数据包：七张玩法表

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1

数据包是**推荐方式**：不写代码、`/reload` 生效、服务端为准。

> 1.21.1 的 `pack_format` 是 `48`，1.20.1 是 `15`；下面的表目录名两版相同。
> 标签命名空间：1.21.1 用 `c:`，1.20.1 用 `forge:`。

## 1. 安装与生效

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

`pack.mcmeta`：

```json
{
  "pack": {
    "pack_format": 48,
    "description": "我的空岛配方"
  }
}
```

- 文件夹和 `.zip` 都可以；推荐文件夹，改完直接 `/reload`。
- `/datapack list` 能看到 `file/my_pack` 才算加载。
- 七张表会扫描**所有命名空间**，所以 `mypack` 不需要等于 `exnihilosatellitium`。
- 改完 `/reload`；日志里先出现一次 `waiting for tags`，再出现完整条数，**以第二次为准**。

## 2. 通用字段

### 2.1 `#tag` 输入

所有 `input` / `block` / `base` / `added` 字段都可以写 `#标签`：

```json
{ "input": "#c:gravels", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

- 1.21.1：`#c:gravels`
- 1.20.1：`#forge:gravel`
- vanilla 标签两版都是 `#minecraft:logs` / `#minecraft:leaves` / `#minecraft:campfires` 等。

展开规则：

| 表 | 标签行为 |
|---|---|
| 筛子 / 钩子 | 给每个匹配物品**追加**条目；完全相同的掉落只留一份 |
| 锤子 / 熔点 / 热源 | **具体条目优先**；标签不会覆盖已有规则 |
| 桶 | `base` / `added` 展开后做叉积；`base`/`added.fluid` 只保留 **still（source）** 流体 |
| 堆肥 | 原生支持，按标签匹配 |

> 掉落产物（`item` / `result`）必须是**具体 id**，不支持 `#tag`。要“随机掉一种锭”，写多条掉落。

### 2.2 `mod` 门

文件根写：

```json
{ "mod": "tconstruct", "input": "minecraft:magma_block", "fluid": "minecraft:lava", "amount": 500 }
```

模组不在时**整个文件静默跳过**，不会解析里面的物品，也不会刷 unknown id 警告。
筛子/钩子的**单条掉落**还可以各自带 `mod`：

```json
{ "item": "tconstruct:sky_slime_grass_seeds", "chance": 0.02, "mod": "tconstruct" }
```

### 2.3 补丁与删除

| 表 | `"patch": true` | `"remove": true` | `"remove": ["id", "#tag"]` |
|---|---|---|---|
| 筛子 | 追加 `outputs` | 删除该 `input` 整条 | 删除匹配的掉落 |
| 钩子 | 追加 `drops` | 删除该 `input` 整条 | 删除匹配的掉落 |
| 锤子 / 熔点 / 热源 | 替换该输入的规则 | 删除该输入 | — |
| 桶 | 替换该 `base + added` 规则 | 删除该规则 | — |
| 堆肥 | 追加 `values` | — | 删除匹配的条目（可再用 `values` 加回新值） |

- 只写 `outputs`/`drops` 而**不写** `patch`：会被当成普通文件。同一输入已有条目时按各表规则处理：
  筛子覆盖并打 `duplicate` 警告；锤子/钩子/熔点后到的条目跳过。
- 出现 `"remove"` 会自动按补丁处理；`patch: true` 用于“只追加”的补丁。
- 补丁在普通文件之后应用，按资源 id 排序；多个补丁改同一条目时，后面的看到前面的结果。

## 3. 筛子 `sieve/`

**一个文件 = 一个输入物品 + 掉落列表。**

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

| 字段 | 必填 | 说明 |
|---|---|---|
| `input` | ✅ | 物品 id 或 `#物品标签` |
| `outputs` | ✅ | 掉落数组 |
| `outputs[].item` | ✅ | 产物物品 id |
| `outputs[].chance` | ✅ | `0 < chance <= 1`，**独立概率**，不是权重 |
| `outputs[].min` / `max` | ❌ | 成功掷出后给几件，默认 1；闭区间 |
| `outputs[].mod` | ❌ | 该条仅在某模组存在时参与 |

- 每次 sift 对每条掉落独立判定，可能同时出多个。
- 筛网档位只影响耐久，不影响掉落表。
- 机器筛和手动筛读同一张表，改表机器自动跟随。

**补丁示例：删仙人掌、加 50% 钻石**

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"],
  "outputs": [ { "item": "minecraft:diamond", "chance": 0.5 } ]
}
```

## 4. 锤子 `hammer/`

**一个文件 = 一个方块 → 一个产物 + 数量区间。**

```json
{
  "input": "minecraft:cobblestone",
  "result": "minecraft:gravel",
  "min": 1,
  "max": 2
}
```

| 字段 | 必填 | 说明 |
|---|---|---|
| `input` | ✅ | 方块 id 或 `#方块标签` |
| `result` | ✅ | 产物物品 id |
| `min` / `max` | ❌ | 1..64，默认 1 |

- 一个方块只能有一条锤规则；标签不会覆盖已有具体规则。
- 要改已有规则，用 `"patch": true`；要删，用 `"remove": true`。

## 5. 钩子 `crook/`

**一个文件 = 一种树叶方块 → 追加掉落。**

```json
{
  "input": "minecraft:oak_leaves",
  "drops": [
    { "item": "minecraft:stick", "chance": 0.1 },
    { "item": "minecraft:apple", "chance": 0.02 }
  ]
}
```

| 字段 | 必填 | 说明 |
|---|---|---|
| `input` | ✅ | 方块 id 或 `#方块标签` |
| `drops[].item` | ✅ | 物品 id |
| `drops[].chance` | ✅ | `0 < chance <= 1` |
| `drops[].mod` | ❌ | 软依赖门 |

钩子只**追加**掉落，原版树叶掉落不受影响。

## 6. 桶转化 `barrel/`

**一个文件 = 一种 `base + added` 规则。**

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

| 字段 | 必填 | 说明 |
|---|---|---|
| `base` | ✅ | 桶内必须**装满 1000 mB** 的流体（`drain` 规则除外）；可写 `#流体标签` |
| `added` | ✅ | 恰好一个 `fluid` 或 `item`；都可写标签 |
| `result` | ✅* | 方块/物品 id（写方块物品的 id）；与 `entity` / `result_fluid` 三选一 |
| `entity` | ✅* | 生成实体 id；与 `result` / `result_fluid` 三选一 |
| `result_fluid` | ✅* | 把整桶换成该流体；与 `result` / `entity` 三选一 |
| `delay_ticks` | ❌ | 仅 `result_fluid`：等待 tick（600 = 30 秒） |
| `drain` | ❌ | 仅 `added.item` + `result`：桶内至少这么多 mB 就能右键，扣流体给物品 |
| `peaceful_drop` | 实体规则必填 | 和平模式掉落，字符串或数组 |
| `cook_ticks` | ❌ | 旧字段，仅为旧存档兼容保留；新规则右键立即生效 |

- `base` / `added.fluid` 只展开 **still** 流体；`#minecraft:water` 的 flowing 变体
  会被丢掉（桶里只可能存 still）。
- **物品规则优先于流体容器路径**：牛奶桶既是物品又是流体容器，先匹配 `added.item`；
  消耗后把 `getCraftingRemainingItem()` 还给玩家（牛奶桶 → 空桶）。
- 生成实体规则：

  ```json
  { "base": "minecraft:lava", "added": { "item": "exnihilosatellitium:angry_doll" },
    "entity": "minecraft:blaze", "peaceful_drop": "minecraft:blaze_rod" }
  ```
- 流体产出规则（糖水 / 浓缩蜂蜜汁）：
  ```json
  { "base": "minecraft:water", "added": { "item": "minecraft:sugar" },
    "result_fluid": "exnihilosatellitium:sugar_water" }
  ```
  ```json
  { "base": "exnihilosatellitium:sugar_water", "added": { "item": "minecraft:yellow_dye" },
    "result_fluid": "exnihilosatellitium:concentrated_honey", "delay_ticks": 600 }
  ```
- 部分抽取规则（玻璃瓶装浓缩蜂蜜汁）：
  ```json
  { "base": "exnihilosatellitium:concentrated_honey",
    "added": { "item": "minecraft:glass_bottle" }, "drain": 250, "result": "minecraft:honey_bottle" }
  ```

**删除内置规则：**

```json
{ "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" }, "remove": true }
```

## 7. 堆肥 `compost/`

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

- key 是物品 id 或 `#物品标签`；value 是正整数。
- 同一个 stack 命中多条时取**最大值**，不叠加。
- 总和到 `1000` 填满，然后 30 秒成土。
- **必须同时改物品标签** `#exnihilosatellitium:compostable`，否则不能堆肥；
  标签有、JSON 没有 → 只给 1 点。

标签文件（1.21.1）：

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

1.20.1 路径是 `data/exnihilosatellitium/tags/items/compostable.json`。

**补丁：把面包从 125 改成 100**

```json
{
  "remove": ["minecraft:bread"],
  "values": { "minecraft:bread": 100 }
}
```

## 8. 坩埚熔点 `crucible_melt/`

```json
{
  "input": "minecraft:cobblestone",
  "fluid": "minecraft:lava",
  "amount": 250,
  "solid": 2000
}
```

| 字段 | 必填 | 说明 |
|---|---|---|
| `input` | ✅ | 方块 id 或 `#方块标签` |
| `fluid` | ✅ | 流体 id |
| `amount` | ✅ | 1..64000 mB |
| `solid` | ❌ | 经典固体量记录；默认等于 `amount`，上限 64000 |

## 9. 坩埚热源 `crucible_heat/`

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

| 字段 | 必填 | 说明 |
|---|---|---|
| `block` | ✅ | 方块 id 或 `#方块标签` |
| `properties` | ❌ | 全部匹配才算；用于区分点燃/未点燃 |
| `speed` | ✅ | mB/tick，`0 < speed <= 4` |

热源补丁带 `properties` 时只删/替换同状态的规则；不带则删该方块全部规则。

## 10. 原版配方（合成 / 熔炼）

七张表之外，合成/熔炼仍然是 vanilla `RecipeManager`，可以直接加数据包 JSON。

### 1.21.1 有序合成

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

### 1.20.1 同一配方

路径改成 `data/mypack/recipes/`，`result` 改成：

```text
"result": { "item": "exnihilosatellitium:barrel", "count": 1 }
```

### 熔炼

1.21.1：

```json
{
  "type": "minecraft:smelting",
  "ingredient": { "item": "minecraft:iron_ore" },
  "result": { "id": "minecraft:iron_ingot", "count": 1 },
  "experience": 0.7,
  "cookingtime": 200
}
```

1.20.1 把 `result` 改成 `{ "item": "minecraft:iron_ingot", "count": 1 }`。

> **数据包不能直接删除原版配方。** 需要删配方时用 [KubeJS](kubejs.md) 或
> [CraftTweaker](crafttweaker.md)。

## 11. 自检

```mcfunction
/reload
/exnihilo tables
/exnihilo dump sieve
/exnihilo dump all
/exnihilo sieve minecraft:sand
/exnihilo hammer minecraft:cobblestone
/exnihilo compost minecraft:bread
```

- `/exnihilo dump <table>` 输出整张表的排序文本（`sieve` / `hammer` / `crook` / `barrel` /
  `compost` / `crucible_melt` / `crucible_heat`），用来确认补丁到底改了哪几行。
- `/exnihilo customize ...` 是 P1 的内部调试入口（直接向运行期 overlay 排队，不写文件、
  重启即清空），不是玩家功能；KJS / CT 的正式入口在 roadmap 的 P2 / P3。

日志前缀：

- `[sieve]` / `[hammer]` / `[crook]` / `[barrel]` / `[compost]` / `[crucible]`
- `empty or unknown tag`：标签拼错或没装对应模组
- `unknown input/result`：id 拼错，或模组不在
- `duplicate input`：同一输入被多个文件命中；筛子是后者覆盖，锤子/钩子/熔点是后者跳过，都看日志确认

## 12. 客户端 JEI

- 单人 / 局域网：JEI 读服务端表，数据包改动会显示。
- **专用服**：客户端读不到服务端数据包，JEI 仍显示 jar 内置值；实际掉落/合成以服务端为准。
  这是当前限制，不是数据包没生效。