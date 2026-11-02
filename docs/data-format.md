# 数据格式：堆肥、筛子、NBT、命令

> 适用：M1 木桶 + M2 筛子（NeoForge 1.21.1）。代码位置：
> `common/compost/`、`common/sieve/`、`common/blockentity/`、`compat/jei/`。
>
> 十二种桶（`barrel` = 橡木、`spruce_barrel`、`birch_barrel`、`jungle_barrel`、
> `acacia_barrel`、`dark_oak_barrel`、`stone_barrel`）共用同一个 BE 类型和同一套
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
- 填满后计时 `COMPOST_TICKS = 1000`（50 秒），然后进入 DIRT。
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
| 125 | 8 | 树苗、树叶、面包 |
| 120 | 9 | 鱼（含熟鱼） |
| 100 | 10 | 花、蘑菇、苹果、仙人掌、甜浆果、发光浆果、海带、竹子、孢子花、滴水叶、红树根、菌类、高草（双格）等 |
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
- 默认 JSON 有 **65 条**（12 个标签 + 53 个具体物品）；`#minecraft:crops` / `#minecraft:seeds`
  不是 1.21.1 物品标签，别用；NeoForge 1.21.1 用 `#c:crops/*` / `#c:seeds` / `#c:eggs` /
  `#c:strings`。`#forge:*` 是旧命名空间，本环境里写了也不匹配。

## 2. 方块实体 NBT

木桶 BE 的 NBT（**NeoForge 21.1 的 `FluidTank` 是嵌套 `Fluid`**，不是旧版的顶层
`FluidName` / `Amount`）：

| 键 | 类型 | 含义 |
|---|---|---|
| `Fluid` | compound | `{id:"minecraft:water", amount:1000}`；空桶没有这个键 |
| `Compost` | int | 堆肥进度，0–1000 |
| `CompostTicks` | int | 剩余计时 tick；>0 表示正在腐熟 |
| `DirtReady` | boolean | `1b` 表示泥土已就绪，右键可取 |
| `CompostScale` | int | 进度刻度；新存档固定 `1000`，老存档没有这个键 |
| `Solid` | compound | 凝固/掺料产物（`ItemStack`，如圆石/黑曜石/下界岩/末地石）；空时没有这个键 |
| `Spore` | int | 巫水发酵进度（0 = 未开始）；满 `1000` 时整桶水变巫水 |

- 容量固定 `CAPACITY = 1000` mB，不依赖 NBT。
- **老存档迁移**：没有 `CompostScale` 的桶按 0..100 旧刻度读取，进度 ×10、计时按 600→1000 缩放；新存档写入 `CompostScale:1000`。
- 桶里有流体时不能堆肥；堆肥进度 > 0 时不能灌流体（互斥）。
- DIRT / `Solid` 状态下右键**任意手持物**都会取出产物并重置（原版 `ExtractMode.Always` 风格）。

### 侵染树叶 NBT（M6）

| 键 | 类型 | 含义 |
|---|---|---|
| `Mimic` | compound | 原树叶方块状态（`NbtUtils.writeBlockState`） |
| `Progress` | float | 0..1，0.0005/tick；>0.6 开始扩散 |
| `SpreadTimer` | int | 扩散计时，满 100 尝试一次 |

### 桶内反应（M1.5）

| 桶里 | 加入 | 结果 |
|---|---|---|
| 1000 mB 水 | 熔岩（桶倒，或上方熔岩**源/流动**） | 圆石（`Solid`，右键取出） |
| 1000 mB 熔岩 | 水（桶倒，或上方水**源/流动**） | 黑曜石 |
| 1000 mB 熔岩 | 红石（右键） | 下界岩 |
| 1000 mB 熔岩 | 荧石粉（右键） | 末地石 |

- **温度**：木桶只能存 `≤433 K` 的流体；水 300 K 可以，熔岩 1300 K 不行 → 用石桶。
  往满水木桶倒熔岩是允许的，因为熔岩会立刻变成圆石、不会留在桶里。
- **雨水**：露天的桶（`isRainingAt(上方)`）按 biome rainfall 积水；0.5 降雨约 100 秒满。
- 这些反应现在是数据驱动的：`data/<ns>/barrel/*.json`，表结构见 §10；BE 只保留“满桶”规则。

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

# 灌流体（NeoForge 21.1：嵌套 Fluid，不是顶层 FluidName/Amount）
/data merge block ~ ~ ~ {Fluid:{id:"minecraft:lava",amount:1000}}
/data get block ~ ~ ~ Fluid

# 水 + 上方熔岩 -> 圆石（Solid）
/data merge block ~ ~ ~ {Fluid:{id:"minecraft:water",amount:1000}}
/setblock ~ ~1 ~ minecraft:lava
/data get block ~ ~ ~ Solid
```

`data merge` 会绕过正常交互和标签判断，**只用于测试**；正常玩法请右键塞堆肥/用桶装流体。

## 4. 比较器

- DIRT / `Solid` 状态：固定 `15`。
- 其他状态：`max(流体量 × 15 / 1000, Compost × 15 / 1000)`。

## 5. Jade tooltip 键

`assets/exnihilosatellitium/lang/{en_us,zh_cn}.json`：

| 键 | 用途 |
|---|---|
| `jade.exnihilosatellitium.barrel.ready` | 泥土已就绪 |
| `jade.exnihilosatellitium.barrel.solid` | `内含：圆石（右键取出）` |
| `jade.exnihilosatellitium.barrel.compost` | `堆肥：45%（450 / 1000）` —— 百分比 + 原始值 |
| `jade.exnihilosatellitium.barrel.timer` | `成型中：还需 n 秒` —— 堆肥满后倒计时 |
| `jade.exnihilosatellitium.barrel.fluid` | `流体：x / 1000 mB` |

Provider：`compat/jade/BarrelComponentProvider.java`；只读已同步的 BE，不发额外网络包。
服务端在倒计时期间每 10 tick 同步一次（`serverTick` 里的 `sync()`），所以百分比和秒数是活的。

显示顺序：流体 → 泥土就绪（有就返回）→ 堆肥百分比 → 成型倒计时。

## 6. 相关代码

- `common/compost/CompostRegistry.java`：解析 JSON、按物品/标签查值。
- `common/compost/CompostReloadListener.java`：资源重载入口。
- `common/tag/ModTags.java`：`#exnihilosatellitium:compostable`。
- `common/blockentity/BarrelBlockEntity.java`：状态机、NBT、比较器、粒子/音效。
- `client/BarrelRenderer.java`：液面 / 堆肥 box / DIRT 泥土 box。

## 7. 材质变体数据

12 种桶（11 木 + 石）的 `blockstates/`、`models/block/`、`models/item/`、`recipe/`、掉落表和
`mineable/{axe,pickaxe}` 标签现在**全部由 datagen 生成**（`datagen/WoodTypes` 一张表 +
`ModBlockStates` / `ModItemModels` / `ModRecipes` / `ModBlockTags` / `ModLootTables`）。
几何只在手写的 `models/block/barrel_base.json` 里维护，子模型只覆盖 `textures`。

| block id | 桶身贴图 | 内壁/桶底 | 配方键 | 工具 |
|---|---|---|---|---|
| `barrel` | `oak_planks` | `stripped_oak_log` | `oak_planks` | 斧 |
| `spruce_barrel` | `spruce_planks` | `stripped_spruce_log` | `spruce_planks` | 斧 |
| `birch_barrel` | `birch_planks` | `stripped_birch_log` | `birch_planks` | 斧 |
| `jungle_barrel` | `jungle_planks` | `stripped_jungle_log` | `jungle_planks` | 斧 |
| `acacia_barrel` | `acacia_planks` | `stripped_acacia_log` | `acacia_planks` | 斧 |
| `dark_oak_barrel` | `dark_oak_planks` | `stripped_dark_oak_log` | `dark_oak_planks` | 斧 |
| `mangrove_barrel` | `mangrove_planks` | `stripped_mangrove_log` | `mangrove_planks` | 斧 |
| `cherry_barrel` | `cherry_planks` | `stripped_cherry_log` | `cherry_planks` | 斧 |
| `bamboo_barrel` | `bamboo_planks` | `stripped_bamboo_block` | `bamboo_planks` | 斧 |
| `crimson_barrel` | `crimson_planks` | `stripped_crimson_stem` | `crimson_planks` | 斧 |
| `warped_barrel` | `warped_planks` | `stripped_warped_stem` | `warped_planks` | 斧 |
| `stone_barrel` | `stone` | `stone` | `stone` | 镐 |

- 所有桶配方都是 `x x / x x / xyx`：`x` 是木种木板（石桶用 `stone`），`y` 是对应台阶
  （石桶用 `smooth_stone_slab`）；不再接受 `#minecraft:planks`，避免和具体木种配方冲突。
- 石桶复制 `Blocks.STONE` 属性（需要镐、石质音效），木桶复制对应木板属性。
- 行为、NBT、Jade tooltip 十二种桶基本一致；**唯一的差异化是石桶能存熔岩、木桶不能**
  （M1.5 起，按 433 K 温度阈值判定，见 §2「桶内反应」）。

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
| 数据包 | `data/<ns>/...` | 覆盖概率/配方，**不能新增材料** |

**当前没有 config 扩展表**：表外材料不会自动发现，也不会生成骨架行。新增材料要改 jar 内表、
重跑 `scripts/generate_ore_variants.py`（贴图 + 锤表 + lang）和 `./gradlew runData`
（模型 / 配方 / 掉落 / 标签），需要的话再跑 `generate_sieve_assets.py`（筛表），然后重启。
config/自动发现留给后续 schema 修订（见 [tech-debt.md](tech-debt.md)）。

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
      "ingot": "minecraft:copper_ingot",
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
| `rarity` | ✅ | 三种筛料共用的 `1/N`，必须 > 0。**当前只支持整数**；对象写法未实现（读 `gravel` 并打警告） |
| `ingot` | ❌ | 无条件复用：写**具体物品 id**（`minecraft:iron_ingot`），熔炼总是产出它，不注册自有锭。**暂不支持标签**（`#c:ingots/tin`），写标签会打警告并按自有锭处理 |
| `ingots` | ❌ | **受支持模组的候选锭**（有序数组）：`[{"item": "mekanism:ingot_tin", "mod": "mekanism"}]`。启动时取第一个 `mod` 已加载（或没写 `mod`）的候选；都不匹配时注册自有 `exnihilosatellitium:<id>_ingot`。datagen 会给 base 配方加 `neoforge:not(...)` 兜底；每个候选的 `*_smelting_<mod>.json` 仍是手写（datagen 引用不了未注册的物品），见下 |
| `owner_mod` | ❌ | 主要来源模组；当前不解析，仅保留字段 |
| `source` | ❌ | `classic`（照 1.7.10 对照）/ `estimate`（我们估的）；省略视为 `estimate` |
| `enabled` | ❌ | 默认 `true`；设为 `false` 时该条跳过（当前没有自动骨架行） |

`tier` 与 `rarity` 的对应（校验用，越界即报错跳过）：

| tier | rarity |
|---|---|
| `common` | ≤ 24 |
| `uncommon` | 25 – 48 |
| `rare` | 49 – 96 |
| `epic` | ≥ 97 |

### 规则

- **只有 jar 内这一份表**：同一 `id` 重复时后者跳过；没有 config/自动发现层。
- **表外材料不注册**：只处理表内条目，不会自动扫描物品注册表。
- **校验失败只跳过该条**（不拖垮启动）：`id` 非法、颜色格式错、`tier` 非法、`rarity` 越界、重复 `id`。
  日志用 `[materials] ...` 前缀，和 `[compost]` 保持一致的风格。
- **注册名不可变**：`id` 改了就等于换了材料，旧存档里的物品会变成缺失——发布后不要改已用的 `id`。
- **宝石不走材料链**：钻石、绿宝石、青金石、石英、红石、下界石英这类没有“锭”的东西，
  直接写进筛子掉落表（`1/N`），不占 `materials` 的条目——它们的链条在 1.7.10 里也不存在。
- **维度变体暂缺**：1.7.10 有 overworld / nether / end 三套矿砾（碎下界岩、末地砾）。
  当前 schema 只有一件事（筛砂砾/沙子/尘土），下界与末地变体留到 M2 实现时再扩字段，
  现在不要在表里硬塞。

### 生成的配方与标签

以下文件都由 `./gradlew runData` 从材料表生成（`ModRecipes` / `ModItemTags` / `ModBlockTags` /
`ModLootTables` / `ModBlockStates` / `ModItemModels`）；只有 `*_smelting_<mod>.json` 是手写：

| 文件 | 内容 |
|---|---|
| `data/exnihilosatellitium/recipe/<id>_<stage>_to_ore_<kind>.json` | 2×2：4 个破碎/粉碎/粉末 → 对应矿砾/矿砂/矿尘方块 |
| `data/exnihilosatellitium/recipe/<id>_ore_<kind>_smelting.json` | 熔炼：矿砾/矿砂/矿尘 → 锭（0.1 xp，200 tick） |
| `data/c/tags/item/ingots/<id>.json` | 自有锭加入 `c:ingots/<id>`，方便别的模组用标签接受它 |
| `data/c/tags/block/ores/<id>.json` | 矿砾/矿砂/矿尘方块 → `c:ores/<id>`（方块标签） |
| `data/c/tags/item/ores/<id>.json` | 同名方块物品 → `c:ores/<id>`（物品标签） |
| ~~`data/c/tags/item/raw_materials/<id>.json`~~ | **已删除（2026-10-31）**：破碎/粉碎/粉末不是原矿，不能进 `c:raw_materials`，否则别的模组会把它们当原矿处理，绕过「4 块 → 方块 → 熔炼」链条 |
| `data/c/tags/{block,item}/ores.json` | 把本项目材料的 `#c:ores/<id>` 子标签挂到根标签（只含矿砾/矿砂/矿尘方块，对应 1.7.10 把矿方块注册给 TE/TC 的做法） |

- **受支持模组（`ingots` 候选）**：有序数组，**第一个已加载模组的锭胜出**。数据侧是
  「一条 base + 每条候选一个变体」：
  - base `<m>_ore_<kind>_smelting.json` → 自有锭，条件 `not(or(候选 mod...))`；
  - 变体 `<m>_ore_<kind>_smelting_<mod>.json` → 候选的锭，条件
    `mod_loaded(本模组)` **加上** 对该候选之前所有 mod 的 `not(mod_loaded(...))`。
    没有这串否定，两个模组同时在场时同一输入会匹配两条熔炼配方。
  - 运行时 `OreMaterials` 用同一个顺序解析，命中的候选模组在场时**不注册自有锭**，
    `c:ingots/<id>` 里的自有锭条目写成 `required: false`（物品不存在时标签跳过）。

  | 材料 | 候选（按优先级） |
  |---|---|
  | tin / lead | `mekanism` → `thermal_foundation` |
  | silver / nickel | `thermal_foundation` |
  | zinc | `create` |
  | osmium / uranium | `mekanism` |
  | platinum / aluminum | 无候选（永远自有锭） |

  - **mod id ≠ 物品命名空间**：Thermal 的模组 id 是 `thermal_foundation`，锭却注册在
    `thermal:` 里。条件写 mod id，物品写命名空间，两边都要对。
  - **两个版本的配方条件键不同**：1.21.1 用 `neoforge:conditions`，1.20.1 用**裸 `conditions`**
    （Forge 的 `RecipeManager` 只认这个；写 `forge:conditions` 会被静默忽略，然后按无条件解析，
    结果是「未知物品」报错或条件失效）。两者都是**解析前**判定，所以未安装模组的变体可以
    安全地指向不存在的物品。
  - **Thermal 只在 1.20.1 存在**：主线的材料表同样带着它的候选（两条线共用一个
    `materials.json`），在 1.21.1 上这些候选永远不命中，对应的 15 条变体配方也永远不会加载。
  - 加一个模组 = 在材料里加一行候选 + 手写 `<m>_ore_<kind>_smelting_<mod>.json` +
    重跑 `./gradlew runData`（base 配方会自动重算条件）；id 必须按对方 jar 核对。
  - **换模组会改变物品存在性**：已有存档里拿到的自有锭，装上受支持模组后会变成未知物品
    （日志报错并消失）。开世界前定好模组列表。
- 铁/金/铜的 `ingot` 指向原版锭，所以 36 个熔炼配方里它们熔到 `minecraft:*_ingot`；
  其余 9 种熔到自有锭。
- 锭贴图由 `art/items/ingots/generate.py` 用同一套 5 阶色板生成，16×16、自有像素。
- **改了材料表就要重跑生成脚本**；否则会出现「注册了自有锭但没有熔炼配方」的错位。
- 粉碎/粉末现在有两条通路：4 块合成 + M3 锤子降档（矿砾→粉碎、矿砂→粉末）。

### 第三方机器：矿块能不能进别人家的机器

判据是**标签**，不是配方——除了少数「对方把自己家的矿写成具体物品」的情况。

| 材料 | Thermal 磨粉机（`thermal:pulverizer`） | Create 粉碎（`create:crushing`） |
|---|---|---|
| iron / gold / copper | ✅ 走 `forge:ores/<m>` 标签 | ❌ 对方用具体物品（`minecraft:iron_ore` 等）→ **我们补 3 条/材料** |
| tin / silver / lead / nickel | ✅ 走标签 | ✅ 走 `forge:ores/<m>` 标签 |
| platinum / aluminum / osmium / uranium | —（Thermal 没有这些金属） | ✅ 走标签 |
| zinc | —（Thermal 没有锌） | ❌ 对方用 `create:zinc_ore` → **我们补 3 条** |

要补的只有 **Create 侧的铁/金/铜/锌**，共 12 条 `*_ore_{gravel,sand,dust}_crushing.json`：
产物与 Create 自己的同名矿石配方一致（铁 1+0.75、铜 5+0.25、金 1+0.75+2 经验粒、锌 1+0.75，
都带 0.125 圆石），条件是 `create` 已加载。其余 8 种材料本来就躺在 `forge:ores/<m>` /
`c:ores/<m>` 标签里，对方的标签配方直接吃。

两个版本的 `create:crushing` 字段名不同，**不能共用一份 JSON**：

| 版本 | 结果数组 | 时间字段 |
|---|---|---|
| 1.21.1 | `"results": [{"id": "create:crushed_raw_iron", "count": 1, "chance": 0.75}]` | `processing_time` |
| 1.20.1 | `"results": [{"item": "create:crushed_raw_iron", "chance": 0.75}]` | `processingTime` |

## 9. 筛子 JSON（M2）

**路径**：`data/<namespace>/sieve/*.json`，一个输入一个文件。

```json
{
  "input": "minecraft:gravel",
  "outputs": [
    { "item": "exnihilosatellitium:iron_broken", "chance": 0.2 },
    { "item": "minecraft:flint", "chance": 0.25 },
    { "item": "exnihilosatellitium:cobblestone_pebble", "chance": 1.0, "min": 4, "max": 6 }
  ]
}
```

- `input` 是物品 id；筛子一次只装 1 个该物品。
- `outputs` 每条独立掷 `chance`（0..1），不是权重；一次结算可能同时出多个产物。
- `min` / `max` 可选，默认 `1`：一次**成功**掷出几件（闭区间，`min <= max`）。
  目前只有泥土的四种石子用 `4..6`；机器筛和手动筛走同一个 `SieveRolls`，JEI 会在概率下面多一行掉落数量。
- `chance <= 0` 或物品 id 解析不到：该条跳过并打 `[sieve]` 警告。
- 服务端与客户端各读一次：服务端用于结算，客户端用于 JEI。客户端的资源管理器只暴露
  `assets/`，所以 `processResources` 会把本目录镜像到 `assets/exnihilosatellitium/sieve/`；
  **仓库里只改 `data/` 这一份**，构建时自动复制。
- 当前有 6 个输入、76 掉落：`gravel` / `sand` / `dirt` / `dust`，以及 M6 加的
  **方块** `crushed_netherrack` / `crushed_end_stone`（锤下界岩 / 末地石得到）；`dust` 输入是 M3 的通用尘土方块，
  `dirt` 是空岛农业起点（种子/作物 + **草种子** + 甜浆果 + **四种石子各 4-6 个、100% 出**
  （圆石/安山岩/花岗岩/闪长岩，四个石子合成一块对应石头；一次泥土 ≈ 4-6 块石头）），`sand` 额外给仙人掌、甘蔗、可可豆、海带、
  管珊瑚、海晶砂粒与**古代孢子**（`ancient_spores`，1/128，M6 巫水链起点）；
  `crushed_netherrack` 出石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙，`crushed_end_stone` 出紫颂果/末影珍珠（2026-11-01 删掉“筛回末地石”，避免原地循环）；
  `sand` 另有 5 种珊瑚（管/脑/气泡/火/角）各 1% 与海晶碎片 2%（海晶碎片只从沙出，砂砾不出）。
- 筛网档位不改变掉落表，只决定寿命（木 10 / 丝 64 / 铁 256）。

## 10. 桶转化 JSON（2026-10-30）

**路径**：`data/<namespace>/barrel/*.json`，一个转化一个文件。

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

- `base`：桶内必须**装满 1000 mB** 的流体；表只描述 `base + added -> result`，
  “必须满桶”这条规则仍在 `BarrelBlockEntity` 里（BE 与 JEI 共用 `BarrelRecipes`）。
- `added`：恰好一个 `fluid`（从上方倒入，或倒进满桶）或 `item`（满桶时右键）。
- **生成实体规则**（人偶）：用 `entity` 代替 `result`，并给 `peaceful_drop`，可选 `cook_ticks`（默认 600）：
  ```json
  { "base": "minecraft:lava", "added": { "item": "exnihilosatellitium:angry_doll" },
    "entity": "minecraft:blaze", "peaceful_drop": "minecraft:blaze_rod", "cook_ticks": 600 }
  ```
  `peaceful_drop` 也可以是数组（守卫者人偶）：`["minecraft:prismarine_shard", "minecraft:prismarine_crystals"]`，
  结束时随机取一个。`cook_ticks` 现在**不再使用**（右键立即生效），仅为旧数据包兼容保留解析。
  满桶 + 人偶 → 清空流体、倒计时；结束时非和平模式在桶上方生成实体，和平模式掉 `peaceful_drop`，
  桶恢复空桶。JEI 里显示 `peaceful_drop` 作为结果。
  两个都有 / 都没有 → 跳过该文件并打 `[barrel]` 警告。
  **物品规则优先于流体容器路径**：牛奶桶既是物品、又可能是流体容器（Forge 的牛奶流体），
  先匹配 `added.item` 再走 `FluidUtil`；规则消耗物品后把 `getCraftingRemainingItem()` 还给玩家
  （牛奶桶 → 空桶）。
  ```json
  { "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" },
    "entity": "minecraft:slime", "peaceful_drop": "minecraft:slime_ball" }
  ```
- 流体按 registry id 精确匹配（still 水 / 熔岩）；未知 base / added / result 只跳过该条，
  不影响其它文件。
- 客户端 JEI 读镜像后的 `assets/.../barrel/`；`/reload` 或重启后生效，不需要改 Java。
- 当前 12 条：水+熔岩→圆石、熔岩+水→黑曜石、熔岩+红石→下界岩、
  熔岩+荧石粉→末地石、水+尘土→粘土块（`water_dust_clay.json`）、
  水+牛奶桶→史莱姆/粘液球（`water_milk_slime.json`）；
  巫水+沙子→灵魂沙（`witch_water_sand_soul_sand.json`）、
  巫水+橡树苗→深色橡树苗（`witch_water_sapling_dark_oak.json`）；
  人偶 4 条：熔岩+愤怒→烈焰人、巫水+诡异→末影人、巫水+漂浮→潜影贝、水+守卫者→守卫者。
- **删掉 JSON 桶就不反应**：不会退回硬编码。

## 11. JEI 分类

- 插件：`compat/jei/SieveJeiPlugin`（筛子）、`compat/jei/BarrelJeiPlugin`（桶）、
  `compat/jei/CrucibleJeiPlugin`（坩埚），都是 `@JeiPlugin`；JEI 不装时类不加载，也不打包。
- 分类 id：
  - `exnihilosatellitium:sieve`，标题键 `jei.exnihilosatellitium.sieve`；
  - `exnihilosatellitium:barrel`，标题键 `jei.exnihilosatellitium.barrel`；
  - `exnihilosatellitium:barrel_compost`，标题键 `jei.exnihilosatellitium.barrel_compost`；
  - `exnihilosatellitium:crucible`，标题键 `jei.exnihilosatellitium.crucible`；
  - `exnihilosatellitium:crucible_heat`，标题键 `jei.exnihilosatellitium.crucible_heat`；
  - `exnihilosatellitium:hammer`，标题键 `jei.exnihilosatellitium.hammer`；
  - `exnihilosatellitium:crook`，标题键 `jei.exnihilosatellitium.crook`。
- **筛子页**：左上材料、左下筛网、右边 7 列 × 3 行掉落槽；槽位整体居中（输入/筛网 x=6，
  输出 x=34..160），用 JEI 标准槽位背景；砂砾/沙子/尘土三条由 JEI 自动分页。
  三档筛网放进同一个输入槽轮换显示（筛网不影响掉落）。
- **桶内转化页**：116×26，三个标准槽——满桶流体（x=4）、加入的流体或物品（x=42）、
  产物（x=90）；中间画 JEI 原生的加号和箭头。流体槽用
  `setFluidRenderer(1000, false, 16, 16)` + `addFluidStack(fluid, 1000)`，
  所以显示的是流体而不是桶物品。
- **桶堆肥页**：74×26，输入槽（标签展开成物品列表）+ 箭头 + 泥土输出。
  每个条目一页，tooltip 是 `jei...barrel_compost.value`（`堆肥值：+125 / 1000`）；
  展开后为空的标签条目会跳过并打 `[jei]` 警告，避免空槽。
- 每个掉落槽的 tooltip 追加 `jei.exnihilosatellitium.sieve.chance`（`概率：20%`）；
  材料槽 tooltip 是 `jei...sieve.hint`（每筛一个材料，每个产物独立判定）；
  筛网槽 tooltip 是 `jei...sieve.mesh_hint`（只影响耐久，不改变掉落）。
  **不要再用底部 `addText` 提示**：JEI 会把窄宽度下的文字竖排到槽位上（2026-10-29 修）。
- 筛子方块和三档筛网注册为筛子分类 catalyst；**十二种桶**注册为两个桶分类的 catalyst；
  `crucible` 同时是两个坩埚分类的 catalyst。
- **坩埚页**：76×26，方块输入槽 + 箭头 + 流体输出槽（`setFluidRenderer(产物量, ...)`）；
  **坩埚热源页**：92×26，热源物品槽 + 右侧速度文字；`fire`/`lava` 没有方块物品，
  用打火石/熔岩桶作为图标。
- **锤子页**：76×26，方块输入 + 箭头 + 产物；tooltip 写单次数量或 4–6 区间。
  玩法表 47 条，JEI 只展示 35 条（12 条“矿物粉尘→自身”是安全兜底，不作为配方展示）。
  6 种锤都注册为 catalyst。
- **钩子页**：104×26，树叶输入 + 箭头 + 最多 3 个追加掉落槽，每个 tooltip 写概率
  （骨钩 ×1.5 只写在 tooltip 里）；木/骨钩作 catalyst。
- 锤/钩的客户端表来自镜像后的 `assets/.../hammer`、`assets/.../crook`；服务端玩法表
  不会被客户端 `/reload` 覆盖（`HammerRecipes.ruleFor` / `CrookDrops.dropsFor` 只读服务端表）。
- 数据直接读 `SieveRegistry` / `BarrelRecipes` / `CompostRegistry` / `CrucibleRecipes` /
  `CrucibleHeat`（不是 `RecipeManager`），所以客户端必须注册对应 reload listener
  （`ClientEvents` 里）才能在连服务器时显示。
- JEI 的表**优先服务端**（单人/局域网主机会包含数据包覆盖），服务端表为空时回退到
  `assets/` 里的客户端镜像；专用服客户端读不到服务端数据包，只能显示内置版。
- 加/改 `sieve/*.json`、`barrel/*.json` 后 `/reload` 或重启即可；`compost` 走
  `SimpleJsonResourceReloadListener`，同样不需要改 JEI 代码。

## 12. M3：锤子 / 钩子 / 尘土（2026-10-30）

### 锤子表

**路径**：`data/<namespace>/hammer/*.json`，一个方块一条。

```json
{
  "input": "minecraft:cobblestone",
  "result": "minecraft:gravel"
}
```

```json
{
  "input": "exnihilosatellitium:iron_ore_gravel",
  "result": "exnihilosatellitium:iron_crushed",
  "min": 4,
  "max": 6
}
```

- `input` 是方块 id；`result` 是物品 id；原方块不保留。
- `min` / `max`（可选，默认都是 1）是**包含端点**的掉落数量区间，均匀随机。
  上限 64；`min < 1` 或 `max < min` 的文件会警告并跳过。
- **锤子只有左键（正常破坏）一条路径**，没有右键快捷方式（经典 Ex Nihilo 也没有）。
- 当前 47 条：
  - 圆石→砂砾、砂砾→沙子、沙子→`exnihilosatellitium:dust`（各 1 个）；
  - 8 种砂岩（含红砂岩）→沙子 / 红沙（各 1 个）；
  - 12 种材料的矿砾→粉碎、矿砂→粉末（各 **4–6 个**）；
  - 12 种材料的矿物粉尘→**它自己**（不再降档，1 个；防止锤子把粉尘方块敲没）。
- 未知/重复输入、非法数量区间打 `[hammer]` 警告并跳过。

### 钩子表

**路径**：`data/<namespace>/crook/*.json`，一个树叶方块一条。

```json
{
  "input": "minecraft:oak_leaves",
  "drops": [
    { "item": "minecraft:oak_sapling", "chance": 0.25 },
    { "item": "minecraft:apple", "chance": 0.05 },
    { "item": "exnihilosatellitium:silkworm", "chance": 0.02 }
  ]
}
```

- 原版树叶掉落保留；钩子只**追加**表里的条目。
- `chance` 是 0..1；骨钩把概率 ×1.5（封顶 1.0）。
- 服务端专用；当前 10 张表（橡木/深色橡木/云杉/白桦/丛林/金合欢/红树/樱花/杜鹃/盛开的杜鹃）。
- 蚕（`silkworm`）是 M6 丝线链的占位，暂时没有用途。

### 尘土

- 新方块 `exnihilosatellitium:dust`，自有 `block/dust.png` 贴图（Creatio 噪点 4 色量化）；
  沙子用锤子砸出，放进筛子就是 `sieve/dust.json` 的输入。
- 尘土表 15 条：12 种 `<material>_powdered` + 红石 / 荧石粉 / 火药。
- 尘土方块有独立 loot table、`mineable/shovel` 标签和创造标签条目。

## 13. 坩埚表与配置（M4，2026-10-30）

### 熔点表

**路径**：`data/<namespace>/crucible_melt/*.json`，一个方块一条。

```json
{
  "input": "minecraft:cobblestone",
  "fluid": "minecraft:lava",
  "amount": 250
}
```

- `input` 方块 → `fluid` 流体，`amount` 是**一块方块产出多少 mB**，同时也是它的熔料体积
  （当前全部 1:1）；`solid` 可选，默认等于 `amount`。坩埚熔料上限 **2000 mB**：
  8 块圆石/石头/砂砾，或 2 块下界岩，或 4 块雪块。
- 渲染时熔料体积 > 流体体积就显示熔料方块贴图，反之显示流体。
- `amount` / `solid` 必须在 1..64000；未知方块/流体、重复输入会打 `[crucible]` 警告并跳过。
- 当前 19 条：
  - 圆石 / 石头 / 砂砾 / 花岗岩 / 闪长岩 / 安山岩（含磨制、深板岩、凝灰岩、方解石）
    → 250 mB 熔岩；
  - 下界岩 → 1000 mB 熔岩；
  - 雪块 → 500 mB 水；冰 → 1000 mB 水；
  - **沙子 / 红沙 / 尘土 → 100 mB 熔岩**；
  - **M6 木坩埚用**：10 种树叶 → 100 mB 水、10 种树苗 → 250 mB 水（共 20 条）。
- 熔化速度由**下方热源**决定（见下）；**木坩埚例外**：不看热源，固定 0.5 mB/t 被动熔化，
  只接受 `fluid = minecraft:water` 的熔料，且 `fill` 会拒绝温度 > 433 K 的流体。
- 瓷坩埚也能熔树叶/树苗（同一张表），但需要有热源，且速度按热源算。

### 热源表

**路径**：`data/<namespace>/crucible_heat/*.json`，一个热源一条。

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

- `speed` 是 mB/tick（0 < speed ≤ 4）。
- `properties` 可选；列出的每个状态属性都必须匹配（大小写不敏感），
  所以 `lit=true` 可以区分点燃/熄灭的熔炉、篝火。
- 当前 9 条：火把 0.1、熔岩 0.2、火 0.3、燃烧的熔炉/高炉/烟熏炉 0.15、
  篝火 0.25、灵魂篝火 0.35、灵魂火 0.4。
- 热源必须在坩埚**正下方**（`pos.below()`）。

### 配置

- `config/exnihilosatellitium-common.toml`：

  | 键 | 默认 | 范围 | 作用 |
  |---|---|---|---|
  | `crucible.capacity` | 8000 | 1000–64000 | 流体容量 mB |
  | `crucible.meltCapacity` | 2000 | 250–64000 | 熔料（固体）容量 mB |
  | `crucible.heatSpeedMultiplier` | 1.0 | 0.1–10.0 | 全局缩放所有热源速度 |

- 运行时读取；改配置后 BE 下一 tick 生效，不需要重启。
- 客户端 JEI 读 `capacity` 画流体槽。

### 方块与配方

- `unfired_crucible`：7 个 `porcelain`（`x x` / `x x` / `xxx`）→ 烧制成 `crucible`。
- `porcelain`：粘土球 + 骨粉（无序合成）——这是 M4 才补上的前置链。
- 两个方块都进 `mineable/pickaxe`，掉落自身。

## 14. 数据包魔改指南（筛子 / 锤子 / 钩子 / 桶 / 堆肥 / 坩埚）

所有玩法表都是**数据包资源**，路径固定：

| 表 | 路径 | 一条 JSON 表示 |
|---|---|---|
| 筛子 | `data/<namespace>/sieve/*.json` | 一个输入 + 掉落列表 |
| 锤子 | `data/<namespace>/hammer/*.json` | 一个方块 + 产物 + 数量区间 |
| 钩子 | `data/<namespace>/crook/*.json` | 一种树叶 + 追加掉落 |
| 桶转化 | `data/<namespace>/barrel/*.json` | base + added → 方块 |
| 堆肥 | `data/<namespace>/compost/*.json` | 物品/标签 → 分值 |
| 坩埚熔点 | `data/<namespace>/crucible_melt/*.json` | 方块 → 流体 + 量 |
| 坩埚热源 | `data/<namespace>/crucible_heat/*.json` | 方块（可带状态）→ 速度 |

**两种用法**：

1. **覆盖内置条目**：在数据包里用同一路径，例如
   `data/exnihilosatellitium/sieve/sand.json` 会替换 jar 内的 `sand.json`
   （数据包优先级更高，同名资源只保留高优先级那份）。
2. **新增条目**：用自己的命名空间，例如
   `data/mypack/sieve/soul_sand.json`；加载器会扫描所有命名空间下的 `sieve/`、
   `hammer/`、`crook/`、`barrel/`、`compost/`、`crucible_melt/`、`crucible_heat/`。
   同一输入重复时按加载顺序后者覆盖并打警告。

示例（给沙子加 2% 钻石，覆盖内置 sand.json 时要把原有条目一起抄全）：

```json
{
  "input": "minecraft:sand",
  "outputs": [
    { "item": "exnihilosatellitium:iron_crushed", "chance": 0.2 },
    { "item": "minecraft:diamond", "chance": 0.02 }
  ]
}
```

热源可带状态属性：

```json
{
  "block": "minecraft:furnace",
  "properties": { "lit": "true" },
  "speed": 0.15
}
```

**生效方式**：服务端 `/reload` 或重启；单人世界 JEI 现在优先读服务端表，
所以覆盖会同步到 JEI。**专用服客户端**读不到服务端数据包，JEI 仍显示 jar 内置版
（`assets/exnihilosatellitium/` 镜像），这是客户端限制，可在整合包里给客户端也装同一数据包。

## 15. M10 自动化机器（2026-11-01）

四台机器**没有自己的 JSON 表**：它们直接读 `sieve/*.json` 与 `hammer/*.json`，判定函数是
`SieveRolls` / `HammerRolls`。因此改掉落表 = 改原有 JSON，机器自动跟随。

### NBT 键

| 键 | 类型 | 含义 |
|---|---|---|
| `Items` | list | 全部槽位；每项 `{Slot: byte, id, count, components?}`（3×4 输出后电动 18 格 / 内能 15 格） |
| `Work` | int | 当前这一件的进度（tick）；满 `ticksPerOperation` 归零并产出 |
| `Active` | boolean | 是否正在工作；变化时服务端 `sendBlockUpdated`，客户端 BER/粒子用 |
| `Energy` | int | 电动版内部 FE；内能版没有这个键 |
| `BurnHalfTicks` | int | 内能版剩余燃料，单位是**半个**燃烧 tick（煤 1600 tick = 3200） |
| `BurnHalfTotal` | int | 当前这根燃料的总量，用于 GUI 火焰条 |
| `AutoOutput` | boolean | 自动输出总开关；默认 `false`（缺键 = 关） |
| `OutputMask` | int | 自动输出面：每个面一位，位号 = `Direction.get3DDataValue()`（下 0 / 上 1 / 北 2 / 南 3 / 西 4 / 东 5）；缺键时默认 1（底面） |

- 槽位：`0` 工具（筛网 / 锤）、`1` 输入；电动 `2..5` 插件、`6..14` 输出；内能 `2` 燃料、`3..11` 输出。
- 机器记录的 `Work` 在断电/断料时归零（不做半程存档）。

### 自动输出

- `AutoOutput` 打开后，每完成一次操作立刻把输出 12 格推进 `OutputMask` 点亮的面；
  另有 20 tick 的重试，邻居满了再清空也能补推。邻居拒收的余量留在输出格。
- 掩码只影响自动弹出；`ItemHandler.BLOCK` 不受影响，漏斗/管道仍可从任意面抽输出格。
- `AutoOutput=true` 且 `OutputMask=0`：GUI 提交（`setOutputConfig`）会回退为底面，
  直接 `/data merge` 写 0 不会推送（没有目标面）。
- GUI 入口：机器面板左侧突出的标签页（22×28）→ `MachineOutputScreen`（向左伸出的配置面板，不关菜单）；
  面按钮用玩家相对方向（上/下/前/后/左/右），世界方向在 tooltip。
- 网络包 `exnihilosatellitium:set_machine_output`（C2S）：`{BlockPos pos, boolean autoOutput, int mask}`，
  服务端用 `stillValid`（8 格）校验，只写绝对值，不做切换。

### 配置

`config/exnihilosatellitium-common.toml`：

```toml
[machine]
    # 一次处理的基础耗时（tick）；速度插件按 ×1.5/×2.5/×4 缩短（叠加递减）
    ticksPerOperation = 100
    # 电动机器每 tick 抽的 FE；0 = 免费
    energyPerTick = 20
    # 电动机器内部缓冲
    energyCapacity = 48000
    # 内能机器每完成一次消耗的燃烧值（煤 1600 = 8 次）
    burnPerOperation = 200
```

### 测试命令

```mcfunction
forceload add 0 0
setblock 0 120 0 exnihilosatellitium:electric_sieve
data merge block 0 120 0 {Energy:48000, Items:[
  {Slot:0b, id:"exnihilosatellitium:silk_mesh", count:1},
  {Slot:1b, id:"minecraft:sand", count:64}]}
# 等 5 秒（约 100 tick）后
data get block 0 120 0
```

- 能量必须用 `setEnergyStored` 直接写；`/data merge` 的 `Energy` 会走 BE 的 `loadAdditional`。
- 内能版的 `BurnHalfTicks` 是半 tick 单位：写 3200 = 一根煤。
- **必须在 `forceload` 的区块里测**（没有玩家时，普通区块的 BE 不 tick）。
- 管道验证：机器下方放漏斗只会抽到输出格；上方漏斗可以把料补进输入格。

自动输出（RCON）：

```mcfunction
setblock 0 119 0 minecraft:chest
data merge block 0 120 0 {AutoOutput:1b, OutputMask:1}
# 等一件产出后
data get block 0 119 0
data merge block 0 120 0 {AutoOutput:0b}
```

- `OutputMask` 用位或组合：`1` 下 / `2` 上 / `4` 北 / `8` 南 / `16` 西 / `32` 东（例如 `3` = 上下都推）。
- `/data merge` 会走 `loadAdditional`，所以可以直接在 RCON 里开关自动输出。

### 能力

| 能力 | 电动 | 内能 |
|---|---|---|
| `ItemHandler.BLOCK` | 输入/工具/插件格可插，输出 12 格只抽不放 | 同左（输出 12 格只抽不放） |
| `EnergyStorage.BLOCK` | `MachineEnergy`（只进不出） | `null`（没有能力） |

## 16. 版本相关的数据目录（P2，2026-11-01）

自定义表（`sieve/` `hammer/` `crook/` `barrel/` `compost/` `crucible_melt/` `crucible_heat/`）
两版同构，目录名不变。**vanilla 类型**的数据随版本换：

| 1.21.1（NeoForge） | 1.20.1（Forge） |
|---|---|
| `data/<ns>/recipe/` | `data/<ns>/recipes/` |
| `data/<ns>/loot_table/` | `data/<ns>/loot_tables/` |
| `data/<ns>/tags/block/`、`tags/item/` | `tags/blocks/`、`tags/items/` |
| `result: {"id": ..., "count": n}` | `result: {"item": ..., "count": n}` |
| `neoforge:conditions` / `neoforge:mod_loaded` / `neoforge:not` | `forge:*` |
| `c:` 标签（含 `#c:`、`"tag": "c:..."`） | `forge:` |

工具链：`scripts/data_target.py`（唯一真相）、`scripts/port_data.py`（整体移植数据）、
`scripts/check-data-paths.py`（守卫）；生成器用 `--target 1.21.1|1.20.1 --out DIR`。
细节见 [porting.md](porting.md) §3 的 P2。
