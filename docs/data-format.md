# 数据格式：堆肥、NBT、命令

> 适用：M1 木桶 + 七种材质变体（Forge 1.20.1）。代码位置：
> `common/compost/`、`common/blockentity/BarrelBlockEntity.java`。
>
> 十二种桶（11 种木材 + 石桶）共用同一个 BE 类型和同一套
> NBT/行为；区别只有方块属性、模型贴图和配方。`barrel` 保持原名是为了兼容旧存档。

## 1. 堆肥 JSON

**路径**：`data/<namespace>/compost/*.json`（可以拆多个文件）

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

规则：

- `values` 是对象；key 可以是**物品 id** 或 **`#标签`**。
- value 是**正整数**；小于等于 0 的条目会被忽略。
- 同一个 stack 命中多条时，取**最大值**（不是相加）。
- 总和到 `1000` 填满；`COMPOST_MAX = 1000`。
- 填满后计时 `COMPOST_TICKS = 600`（30 秒），然后进入 DIRT。
- 服务端在资源重载时读取；改完执行 `/reload`（或重启）生效。
- **客户端不读这个 JSON**；客户端只用物品标签做交互预测。

### 1000 分制怎么来的

- 原版 Ex Nihilo 1.7.10 用 0..1 的 float（树苗/树叶 0.125、花/蘑菇 0.10、小麦 0.08、肉 0.20、鱼 0.15）。
- Ex Nihilo Omnia 1.10.2 直接按 1000 mB 写（默认 125），数值是原版比例在 1.10 的延续/微调。
- Ex Nihilo: Sequentia 1.20.1 也用 1000 分制，但重新平衡过；本项目只用它的**刻度**，不采用它的数值。

所以本项目以 **Omnia 1.10.2 的 1000 分制表为骨架**，用原版 1.7.10 校验比例；
1.20 新物品按 Omnia 的类别补。换算关系就是 `Omnia 值 = 原版比例 × 1000`：

| 堆肥值 | 单独塞满 1000 最少要 | 物品 |
|---:|---:|---|
| 200 | 5 | 金苹果 |
| 175 | 6 | 生/熟猪牛鸡肉、羊肉、兔肉；南瓜派 |
| 150 | 7 | 西瓜块、南瓜、雕刻南瓜、南瓜灯 |
| 125 | 8 | 树苗（含杜鹃 / 盛开的杜鹃）、树叶、面包 |
| 120 | 9 | 鱼（含熟鱼） |
| 100 | 10 | 花、蘑菇、苹果、仙人掌、甜浆果、发光浆果、海带、竹子、孢子花、滴水叶、红树根、菌类、高草（双格）、**绯红 / 诡异菌岩孢子**等 |
| 85 | 12 | 小麦、腐肉、蜘蛛眼、发酵蜘蛛眼 |
| 80 | 13 | 种子、鸡蛋、甘蔗、胡萝卜、土豆、甜菜根、烤土豆、毒土豆、睡莲、藤蔓、下界疣、草（单格）、蕨 |
| 40 | 25 | 西瓜片、线 |
| 5 | 200 | 恶魂之泪 |

原版 1.7.10 与 Omnia 1.10.2 的差异（例如原版腐肉 100 / 面包 160 / 肉 200 / 鱼 150 / 瓜块 166，
Omnia 是 85 / 125 / 175 / 120 / 150）按 Omnia 1.10.2 取；如果你要严格 1.7.10 的旧值，
直接把对应条目改回原版 ×1000 即可。完整表在 `compost/default.json`。

### 标签必须同步

`data/exnihilosatellitium/tags/items/compostable.json`：

```json
{
  "replace": false,
  "values": [
    "#minecraft:leaves",
    "#minecraft:saplings",
    "minecraft:carrot",
    "minecraft:bread"
  ]
}
```

- 服务端 `use()` **先查标签**，再查 JSON：
  - 标签有、JSON 没有 → 按 **1 点** 处理（`Math.max(1, value)`）。
  - JSON 有、标签没有 → **不能堆肥**（服务端也会拒绝）。
- 所以新增堆肥物品时，**两处都要加**。
- 默认 JSON 有 **67 条**（12 个标签 + 55 个具体物品）；`#minecraft:crops` / `#minecraft:seeds`
  不是 1.20.1 物品标签，别用；用 `#forge:crops/*` / `#forge:seeds`。

## 2. 方块实体 NBT

木桶 BE 的 NBT 是**顶层键**（不是 `{Fluid:{...}}`）：

| 键 | 类型 | 含义 |
|---|---|---|
| `FluidName` | string | 流体 id，空桶是 `minecraft:empty` |
| `Amount` | int | 流体量，0–1000 mB |
| `Compost` | int | 堆肥进度，0–1000 |
| `CompostTicks` | int | 剩余计时 tick；>0 表示正在腐熟 |
| `CompostItems` | list | 堆肥投入物 `[{id:"minecraft:rotten_flesh", points:500}, ...]`，按点数加权给客户端混色；旧存档没有该键 |
| `DirtReady` | boolean | `1b` 表示泥土已就绪，右键可取 |
| `CompostScale` | int | 进度刻度；新存档固定 `1000`，老存档没有这个键 |

- 容量固定 `CAPACITY = 1000` mB，不依赖 NBT。
- **老存档迁移**：没有 `CompostScale` 的桶按 0..100 旧刻度读取，进度 ×10、计时按 600→1000 缩放；新存档写入 `CompostScale:1000`。
- 桶里有流体时不能堆肥；堆肥进度 > 0 时不能灌流体（互斥）。
- DIRT 状态下右键**任意手持物**都会取出 1 个泥土并重置（原版 `ExtractMode.Always` 风格）。

## 3. 测试命令

```mcfunction
# 放一个桶（橡木）
/setblock ~ ~ ~ exnihilosatellitium:barrel

# 其他材质：spruce_barrel / birch_barrel / jungle_barrel /
# acacia_barrel / dark_oak_barrel / stone_barrel
/setblock ~ ~ ~ exnihilosatellitium:stone_barrel

# 直接写满堆肥，1 tick 后完成
/data merge block ~ ~ ~ {Compost:1000,CompostTicks:1}

# 看状态
/data get block ~ ~ ~ DirtReady
/data get block ~ ~ ~ Compost
/data get block ~ ~ ~ CompostTicks

# 确认还是桶（不是泥土）
/execute if block ~ ~ ~ exnihilosatellitium:barrel

# 灌流体（顶层 FluidName / Amount）
/data merge block ~ ~ ~ {FluidName:"minecraft:lava",Amount:1000}
/data get block ~ ~ ~ FluidName
/data get block ~ ~ ~ Amount
```

`data merge` 会绕过正常交互和标签判断，**只用于测试**；正常玩法请右键塞堆肥/用桶装流体。

## 4. 比较器

- DIRT 状态：固定 `15`。
- 其他状态：`max(流体量 × 15 / 1000, Compost × 15 / 1000)`。

## 5. Jade tooltip 键

`assets/exnihilosatellitium/lang/{en_us,zh_cn}.json`：

| 键 | 用途 |
|---|---|
| `jade.exnihilosatellitium.barrel.ready` | 泥土已就绪 |
| `jade.exnihilosatellitium.barrel.compost` | `堆肥：x / 1000` |
| `jade.exnihilosatellitium.barrel.timer` | `还需 n 秒` |
| `jade.exnihilosatellitium.barrel.fluid` | `流体：x / 1000 mB` |

Provider：`compat/jade/BarrelComponentProvider.java`；只读已同步的 BE，不发额外网络包。

## 6. 相关代码

- `common/compost/CompostRegistry.java`：解析 JSON、按物品/标签查值。
- `common/compost/CompostReloadListener.java`：资源重载入口。
- `common/tag/ModTags.java`：`#exnihilosatellitium:compostable`。
- `common/blockentity/BarrelBlockEntity.java`：状态机、NBT、比较器、粒子/音效。
- `client/BarrelRenderer.java`：液面 / 堆肥 box / DIRT 泥土 box。

## 7. 材质变体数据

`scripts/generate_barrel_variants.py` 从一张表生成七种桶的
`blockstates/`、`models/block/`、`models/item/`、`recipes/`、`loot_tables/blocks/`
和 `data/minecraft/tags/blocks/mineable/{axe,pickaxe}.json`。几何只在
`models/block/barrel_base.json` 里维护，子模型只覆盖 `textures`。

| block id | 桶身贴图 | 内壁/桶底 | 箍 | 配方键 | 工具 |
|---|---|---|---|---|---|
| `barrel` | `oak_planks` | `stripped_oak_log` | `gray_concrete` | `oak_planks` | 斧 |
| `spruce_barrel` | `spruce_planks` | `stripped_spruce_log` | `gray_concrete` | `spruce_planks` | 斧 |
| `birch_barrel` | `birch_planks` | `stripped_birch_log` | `gray_concrete` | `birch_planks` | 斧 |
| `jungle_barrel` | `jungle_planks` | `stripped_jungle_log` | `gray_concrete` | `jungle_planks` | 斧 |
| `acacia_barrel` | `acacia_planks` | `stripped_acacia_log` | `gray_concrete` | `acacia_planks` | 斧 |
| `dark_oak_barrel` | `dark_oak_planks` | `stripped_dark_oak_log` | `gray_concrete` | `dark_oak_planks` | 斧 |
| `stone_barrel` | `stone` | `stone` | `deepslate_tiles` | `stone` | 镐 |

- 所有桶配方都是 `PPP / P P / PPP`，只换 `P` 的物品；橡木配方不再接受 `#minecraft:planks`，
  避免和具体木种配方冲突。
- 石桶复制 `Blocks.STONE` 属性（需要镐、石质音效），木桶复制对应木板属性。
- 行为、NBT、Jade tooltip 七种桶完全一致；石桶暂不做“只装熔岩”等差异化，留到 M1.5。

## 8. 材料表（矿物链，M2 起用）

矿物不是“别的模组给什么就产出什么”，而是**每种材料注册一套我们自己的物品链**（见
[DESIGN.md](DESIGN.md) §6.4）：

```
筛 砂砾 → 破碎矿   筛 沙子 → 粉碎矿   筛 尘土 → 粉末
破碎 ×4 → 矿砾方块  粉碎 ×4 → 矿砂方块  粉末 ×4 → 矿尘方块
锤 矿砾 → 粉碎矿    锤 矿砂 → 粉末    熔炼 矿砾/矿砂/矿尘 → 锭
```

### 位置

| 层 | 路径 | 说明 |
|---|---|---|
| 内置 | `src/main/resources/exnihilosatellitium/materials.json` | 我们手写，随 jar 发布 |
| 扩展 | `config/exnihilosatellitium/materials.json` | 整合包作者/玩家补表外材料 |
| 数据包 | `data/<ns>/...` | 只能覆盖概率/配方，**不能新增材料** |

**改材料表需要重启游戏**：物品与方块在注册期冻结，无法在 `/reload` 时新增。

### Schema

```json
{
  "materials": [
    {
      "id": "copper",
      "color": "FF4D00",
      "name": { "en_us": "Copper", "zh_cn": "铜" },
      "tier": "common",
      "rarity": 18,
      "ingot": "forge:ingots/copper",
      "owner_mod": "mymod",
      "source": "classic",
      "enabled": true
    }
  ]
}
```

| 字段 | 必填 | 说明 |
|---|---|---|
| `id` | ✅ | 材料名，只允许 `[a-z0-9_]`；决定注册名（`copper_broken` 等） |
| `color` | ✅ | `RRGGBB`，用于物品/方块染色；缺失则该条报错跳过 |
| `name` | ✅ | 显示名，至少给 `en_us`；缺 `zh_cn` 时回退 `en_us` |
| `tier` | ✅ | `common` / `uncommon` / `rare` / `epic`，校验数值区间用（见下） |
| `rarity` | ✅ | 三种筛料共用的 `1/N`，必须 > 0。需要分筛料微调时写成对象 `{"gravel":18,"sand":32,"dust":48}` |
| `ingot` | ❌ | **无条件**复用某个锭：写具体物品名（`minecraft:iron_ingot`）。填了就熔炼到它，不注册自有锭。标签（`#forge:ingots/copper`）**不支持**：读表时标签还没加载，会打警告并按自有锭处理 |
| `ingots` | ❌ | **受支持模组的有序候选**：`[{"item": "thermal:tin_ingot", "mod": "thermal_foundation"}]`。运行时取第一个 `mod` 已加载的候选；都不在就注册自有 `<id>_ingot`。数据侧 base 配方 `not(or(...))`、第 i 条变体 `mod_loaded(i) + not(之前所有候选)`。**注意 mod id 与物品命名空间可能不同**（Thermal：`thermal_foundation` / `thermal:`） |
| `gems` | ❌ | **受支持模组的候选矿物/模组专属材料**（与 `ingots` 同构）：氟石、磷灰石、朱砂、硝石、硫磺、红宝石、蓝宝石；也用于只在模组在场时注册的锭类材料（ER 的 `yellorium`、DE 的 `draconium`）——共同点是产物由对方提供、没有自有兜底。第一个已加载候选胜出；**不注册自有兜底物品、不生成 `forge:gems` 标签、也不生成 base 熔炼配方**。**整条材料只在候选 mod 已加载时注册**：没装时物品、方块、三档碎料、筛出物、锤表、配方和标签都不出现。装模组时矿砾熔成对方物品。**不能与 `ingot`/`ingots` 混用**，混用整条跳过 |
| `owner_mod` | ❌ | 主要来源模组，只用于自检与文档 |
| `source` | ❌ | `classic`（照 1.7.10 对照）/ `estimate`（我们估的）；省略视为 `estimate` |
| `enabled` | ❌ | 默认 `true`；表外骨架行默认 `false` |

`tier` 与 `rarity` 的对应（校验用，越界即报错跳过）：

| tier | rarity |
|---|---|
| `common` | ≤ 24 |
| `uncommon` | 25 – 48 |
| `rare` | 49 – 96 |
| `epic` | ≥ 97 |

### 规则

- **同一 `id` 只能有一个生效来源**：扩展表覆盖内置表，数据包再覆盖两者。
- **表外材料不注册**：启动时只处理表内条目；发现表外材料只写骨架行（`enabled: false`）。
- **校验失败即跳过该条**（不拖垮启动）：`id` 非法、颜色格式错、`rarity <= 0`、重复 `id`。
  日志用 `[materials] ...` 前缀，和 `[compost]` 保持一致的风格。
- **注册名不可变**：`id` 改了就等于换了材料，旧存档里的物品会变成缺失——发布后不要改已用的 `id`。
- **宝石不走材料链**：钻石、绿宝石、青金石、石英、红石、下界石英这类没有“锭”的东西，
  直接写进筛子掉落表（`1/N`），不占 `materials` 的条目——它们的链条在 1.7.10 里也不存在。
- **维度变体暂缺**：1.7.10 有 overworld / nether / end 三套矿砾（碎下界岩、末地砾）。
  当前 schema 只有一件事（筛砂砾/沙子/尘土），下界与末地变体留到 M2 实现时再扩字段，
  现在不要在表里硬塞。
- **当前候选（2026-11-02）**：tin → Mekanism → Thermal；lead → Mekanism → Thermal → IE；
  silver/nickel → Thermal → IE；zinc → Create；**cobalt → Tinkers**
  （`tconstruct:cobalt_ingot`；无匠魂时自有钴锭）；osmium → Mekanism；uranium → Mekanism → IE；
  aluminum → IE；platinum 无候选。IE 只在 1.20.1 有版本，锭 id 是 `ingot_aluminum` 这类；
  它的 crusher 输入走 `forge:ores/<m>`，我们的矿块已在标签里。
- **矿物候选（2026-11-03）**：`fluorite → Mekanism`（`mekanism:fluorite_gem`）；
  `apatite / cinnabar / niter / sulfur / ruby / sapphire → Thermal`（`thermal:apatite` / `thermal:ruby` 等）。走 `gems`，
  **没有自有兜底物品**；无模组时没有熔炼终点。
- **Extreme Reactors（2026-11-04，两版）**：`yellorium → bigreactors:yellorium_ingot`、
  `anglesite → anglesite_crystal`、`benitoite → benitoite_crystal`，都走 `gems` 门控；
  mod id 与命名空间都是 `bigreactors`；1.20.1 用 2.0.84 + ZeroCore 2.1.45；
  同样没有自有兜底物品，无模组时没有熔炼终点。
- **龙之进化（2026-11-04，两版）**：`draconium → draconicevolution:draconium_ingot`，`gems` 门控；
  唯一入口是**粉碎末地石 1/8**（`sieve/crushed_end_stone_draconicevolution.json`，根级 `mod` + patch）；
  矿块进 `forge:ores/draconium`，直接靠 DE 自己的标签熔炼，**不要**再写
  `*_smelting_draconicevolution.json`（同一输入会匹配两条配方）。觉醒龙 / 混沌碎片不补。

### 匠魂熔炼（Tinkers' Construct 3，1.20.1）

**路径**：`data/exnihilosatellitium/recipes/smeltery/melting/<material>/<stage>.json`，
14 种金属材料 × 6 形态 = 84 条（含阿迪特）。7 种模组矿物（氟石/磷灰石/朱砂/硝石/硫磺/红宝石/蓝宝石）不写匠魂熔炼，
因为 Tinkers 没有对应熔融液体。
**例外：ER 的 yellorium** 用 ER 自带的 `forge:yellorium` 流体（不新增流体）：
碎/粉/砂 45 mB、矿块 180 mB、ER 锭 90 mB、ER 块 810 mB（8 条熔炼），再加锭/块 2 条铸造
（结果指向 `forge:ingots/yellorium` / `forge:storage_blocks/yellorium`）。10 条都带
`tconstruct` + `bigreactors` 条件；anglesite / benitoite 仍不写（没有对应流体）。合计 92 条熔炼 + 2 条铸造（94 条匠魂配方）。

```json
{
  "type": "tconstruct:melting",
  "conditions": [{ "type": "forge:mod_loaded", "modid": "tconstruct" }],
  "ingredient": { "item": "exnihilosatellitium:cobalt_broken" },
  "result": { "amount": 60, "tag": "forge:molten_cobalt" },
  "temperature": 950,
  "time": 43
}
```

- 物品（broken / crushed / powdered）→ 45 mB（1/2 锭）；方块（ore_gravel / ore_sand / ore_dust）→ 180 mB（2 锭）。
- result 是流体（`tag` + `amount`，没有 item/id）；`check-data-paths.py` 只对 item/id result
  检查版本键。
- 温度取 Tinkers 自己的锭配方，时间按 90 mB 锭等比缩放；没匠魂时整批条件跳过。
- 阿迪特：Tinkers 3.12.1 本体已移除，改由**工匠巧思**（`tinkers_thinking`，只有 1.20.1）提供
  `forge:molten_ardite`，所以 84 条熔炼里包含阿迪特（碎矿 45 mB / 矿块 180 mB、950°C）。
  我们筛的是**自己的** `ardite_broken`（碎下界岩 1%，砂砾/沙子/尘土不出），矿块熔成 TiT 的锭。
  叶绿素是 `gems`，没有熔融液体，不写熔炼。
- 应用能源 2（`ae2`，两版）：不新增材料，直接给筛沙补丁 `sieve/sand_ae2.json`
  （根级 `"mod": "ae2"` + `"patch": true`）：`ae2:certus_quartz_crystal` **1/6**、
  `ae2:certus_quartz_dust` **1/12**；没装 AE2 时整文件跳过，基表仍是 116 条。
  充能赛特斯 / 天石粉不做：前者用 AE2 自己的 `ae2:charger` 充能，后者来自陨石天石。
- **末影接口（`enderio`，beta）**：不新增材料，独立补丁 `sieve/dust_enderio.json`
  （根级 `"mod": "enderio"` + `"patch": true`）：尘土 **2%** 出 `enderio:grains_of_infinity`
  （无限粉）。没装时整文件跳过；**不新增基岩粉物品、不写「粉 → 基岩」**。
- **深板岩石子**（无门控）：`sieve/dirt.json` 泥土 50% × 3-5；4 个 → `cobbled_deepslate`，
  熔炉 → `deepslate`。草方块表不加。
- **ER 矿石识别（例外）**：ER 的 `yellorite_ore` 同时登记在 `forge:ores/yellorite` 和
  `forge:ores/uranium`；我们的 yellorium 矿块也进这两个 tag（块 + 物品），`yellorium_broken`
  进 `forge:raw_materials/yellorium`（及根 tag）。这样 Mekanism `enriching` / IE crusher / Create crushing
  这类按 `forge:ores/uranium` 取矿的机器能磨我们的矿，输出对方的铀粉（与 ER 原矿一致）。
  这是「碎料不进 raw_materials」规则的唯一例外。
- BambooMod（`bamboomod`，仅 1.20.1）：不新增材料。`sieve/dirt.json` / `sieve/grass_block.json`
  各加 8 条（4 树苗 2%、竹笋 4%、米种 5%、豆 4%、番茄 4%），`sieve/gravel.json` 加 3 条宝石
  （银杏/枫/樱各 1/64）；每条都带 `"mod": "bamboomod"`，没装时在查物品前跳过。
  它的树叶/树苗/竹笋已进 `#minecraft:leaves` / `#minecraft:saplings`，堆肥由现有标签覆盖。

### 匠魂可改造锤（Tinkers' Construct 3，1.20.1）

不是我们的玩法表，而是匠魂自己的数据文件；改它们要按匠魂的格式来，且**只在装匠魂时生效**。

| 文件 | 作用 |
|---|---|
| `data/exnihilosatellitium/tinkering/tool_definitions/ex_nihilo_hammer.json` | 工具定义：部件 `hammer_head`(0.5) + `tough_handle` + 2×`large_plate`(0.25)、`base_stats` / `multiply_stats`、强化槽、`smite` 1、`pickaxe_dig`、有效方块 tag |
| `data/exnihilosatellitium/tinkering/station_layouts/ex_nihilo_hammer.json` | 装配站 4 个输入槽的位置与图标；`conditions` 带 `forge:mod_loaded(tconstruct)` |
| `data/exnihilosatellitium/recipes/tools/building/ex_nihilo_hammer.json` | `tconstruct:tool_building` 配方，`result` 指向本物品 |
| `data/tconstruct/tags/items/modifiable/**` | 6 个追加文件，把物品接入 `harvest` / `multipart` / `durability` / `melee/primary` / `broad`（不登记 `aoe`，见下） |
| `assets/exnihilosatellitium/tinkering/modifiers/sprites/ex_nihilo_hammer.json` | 强化外观：16 个 modifier id → overlay 贴图；模型里的 `modifier_maps` 指向它 |

- 工具定义 JSON 没有 `conditions`（匠魂的 loader 只读已注册的定义）；不装匠魂时核心不会注册
  这个定义，整个 JSON 被忽略。配方与布局有 `forge:mod_loaded` 条件，不会报 unknown item。
- 强化外观同样是匠魂的数据：`modifier_maps` 指向我们的 sprite map，再指向
  `assets/exnihilosatellitium/textures/item/ex_nihilo_hammer/modifiers/*.png`。没列进的
  强化不会显示 overlay，也不会报错；要加就是 map 一条 + 一张 16×16 图。
- 工具的物品类在可选 `src/tinkers/java` source set，**不装匠魂的客户端不加载任何 TiC 类**；
  贴图/模型始终在 jar 里：模型用我们自己的 `exnihilosatellitium:tool` loader，装匠魂时委托
  TiC 的 `tconstruct:tool` 做部件材料染色，没装匠魂时回退成 `forge:item_layers` 分层模型，
  不会报未知 loader。
- 改数值只动 tool definition 的 `multiply_stats` / `base_stats`；改部件只动 `parts` 与
  station layout 的 `input_slots`，两边必须一起改。

## 9. 桶转化 JSON（M1.5/M6）

**路径**：`data/<namespace>/barrel/*.json`，一个转化一个文件。

```json
{ "base": "minecraft:water", "added": { "fluid": "minecraft:lava" },
  "result": "minecraft:cobblestone" }
```

- `base`：桶内必须装满 1000 mB；`added` 恰好一个 `fluid` 或 `item`。
- 方块结果用 `result`；生成实体用 `entity` + `peaceful_drop`（人偶、牛奶）：
  ```json
  { "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" },
    "entity": "minecraft:slime", "peaceful_drop": "minecraft:slime_ball" }
  ```
  非和平在桶上方生成实体，和平掉 `peaceful_drop`；`peaceful_drop` 可为数组。
- **流体结果**用 `result_fluid` 代替 `result`/`entity`，可选 `delay_ticks`：
  ```json
  { "base": "minecraft:water", "added": { "item": "minecraft:sugar" },
    "result_fluid": "exnihilosatellitium:sugar_water" }
  ```
  ```json
  { "base": "exnihilosatellitium:sugar_water", "added": { "item": "minecraft:yellow_dye" },
    "result_fluid": "exnihilosatellitium:concentrated_honey", "delay_ticks": 600 }
  ```
  桶必须满；延迟期间保留原流体，到点整桶换液（600 tick = 30 秒）。
- **部分抽取**用 `drain` + `result`（只配 `added.item`），不要求满桶：
  ```json
  { "base": "exnihilosatellitium:concentrated_honey",
    "added": { "item": "minecraft:glass_bottle" }, "drain": 250, "result": "minecraft:honey_bottle" }
  ```
- **物品规则优先于流体容器路径**：牛奶桶同时是物品和流体容器，先匹配 `added.item`；
  消耗后把 `getCraftingRemainingItem()`（牛奶桶 → 空桶）还给玩家。
- 糖水/浓缩蜂蜜汁都有 source block 与桶（`sugar_water_bucket` / `concentrated_honey_bucket`），
  可放置/舀取；桶内规则不变。
- 当前 18 条（缺 Thermal 时 16 条）：基础 5、牛奶 1、巫水 2、人偶 7（含村民人偶；
  热力 2 条：熔岩+狂风人偶→`thermal:blitz`、水+暴雪人偶→`thermal:blizz`，根级
  `"mod": "thermal_foundation"`，和平掉 `thermal:*_rod`）、糖水/蜂蜜 3。
  浓缩蜂蜜桶每 100 tick 1/6 概率在 6 格内养 1 只蜜蜂（上限 5）。
- 未知 base/added/result/entity 只跳过该条并打 `[barrel]` 警告；删掉 JSON 就不反应。

## 10. 数据包魔改指南（七张玩法表，2026-11-03）

七张表都是数据包资源，加载器扫描**所有命名空间**：

| 表 | 路径 | 一条 JSON | 输入可用 `#tag` |
|---|---|---|---|
| 筛子 | `data/<ns>/sieve/*.json` | 一个输入物品 + 掉落列表 | ✅ 物品标签 |
| 锤子 | `data/<ns>/hammer/*.json` | 一个方块 + 产物 + 数量区间 | ✅ 方块标签 |
| 钩子 | `data/<ns>/crook/*.json` | 一种树叶方块 + 追加掉落 | ✅ 方块标签 |
| 桶转化 | `data/<ns>/barrel/*.json` | base 流体 + added → 方块/实体 | ✅ 流体/物品标签 |
| 堆肥 | `data/<ns>/compost/*.json` | 物品/标签 → 分值 | ✅ 原生 |
| 坩埚熔点 | `data/<ns>/crucible_melt/*.json` | 方块 → 流体 + 量 | ✅ 方块标签 |
| 坩埚热源 | `data/<ns>/crucible_heat/*.json` | 方块（可带状态）→ 速度 | ✅ 方块标签 |

### 10.1 三种写法

1. **覆盖内置**：同命名空间同路径（例如 `data/exnihilosatellitium/sieve/sand.json`）
   会整文件替换 jar 内那份；要抄全原有条目。
2. **新增**：用自己的命名空间；同一输入重复时后加载的覆盖并打 `[sieve] duplicate input ...`。
3. **补丁**：`"patch": true` 或出现 `"remove"`，在所有普通文件之后按资源 id 排序应用，
   不用抄全，还能删条目。

### 10.2 标签输入

`input` / `block` / `base` / `added` 可以写 `#tag`，加载时展开：

```json
{ "input": "#forge:gravel", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

- 1.20.1 的通用标签是 `forge:` 命名空间（`#forge:gravel`、`#forge:ingots/...`）；
  vanilla 标签仍是 `#minecraft:logs` / `#minecraft:leaves` / `#minecraft:campfires`。
- 筛子 / 钩子：标签**合并**到每个匹配物品，完全相同的掉落只留一份。
- 锤子 / 熔点 / 热源：具体条目优先，标签不覆盖已有规则。
- 桶：`base` / `added.fluid` 只展开 **still（source）流体**；`#minecraft:water` 在 1.20.1
  的标签里同时含 still/flowing，flowing 规则永远匹配不到桶里的流体，所以加载时丢掉。
- 掉落产物（`item` / `result`）仍必须是具体 id；要随机掉一种锭请写多条。

### 10.3 `mod` 条件

文件根写 `"mod": "tconstruct"`，模组不在时整文件静默跳过（不解析物品、不刷 unknown id）。
筛子 / 钩子的单条掉落也可以各自带 `mod`。

### 10.4 补丁与删除

| 表 | `"patch": true` | `"remove": true` | `"remove": ["id", "#tag"]` |
|---|---|---|---|
| 筛子 | 追加 `outputs` | 删除该 `input` 整条 | 删除匹配的掉落 |
| 钩子 | 追加 `drops` | 删除该 `input` 整条 | 删除匹配的掉落 |
| 锤子 / 熔点 / 热源 | 替换该输入的规则 | 删除该输入 | — |
| 桶 | 替换该 `base + added` 规则 | 删除该规则 | — |
| 堆肥 | 追加 `values` | — | 删除匹配的条目（可再用 `values` 加回新值） |

```json
{ "input": "minecraft:sand", "patch": true,
  "remove": ["minecraft:cactus"],
  "outputs": [ { "item": "minecraft:diamond", "chance": 0.5 } ] }
```

```json
{ "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" }, "remove": true }
```

### 10.5 加载顺序与自检

```
普通具体条目（按资源 id 排序） → 普通标签条目 → 补丁（按资源 id 排序）
```

标签在所有 reload listener 之后才 bind，所以日志会先出现一次
`waiting for tags`（只有具体条目），紧接着 `TagsUpdatedEvent` 再打印完整条数；**以第二次为准**。
`/exnihilo tables`、`/exnihilo sieve <item>`、`/exnihilo hammer <block>`、
`/exnihilo compost <item>`（OP）可直接查当前生效规则。

### 10.6 示例包

`examples/datapack/`（`pack_format 15`）覆盖标签、补丁、`mod` 门、堆肥改值；
1.21.1 主线的同一示例在主线仓库的 `examples/datapack/`。

### 10.7 客户端

1.20.1 分支目前没有客户端表镜像 / 网络同步：单人 / 局域网 JEI 读服务端表；
专用服客户端看不到服务端数据包，JEI 只能显示内置内容（和主线同一边界）。
