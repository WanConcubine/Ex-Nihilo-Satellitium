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

### 桶内反应（M1.5）

| 桶里 | 加入 | 结果 |
|---|---|---|
| 1000 mB 水 | 熔岩（桶倒或上方熔岩源） | 圆石（`Solid`，右键取出） |
| 1000 mB 熔岩 | 水（桶倒或上方水源） | 黑曜石 |
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

`scripts/generate_barrel_variants.py` 从一张表生成 11 种木桶 + 石桶的
`blockstates/`、`models/block/`、`models/item/`、`recipe/`、`loot_table/blocks/`
和 `data/minecraft/tags/block/mineable/{axe,pickaxe}.json`。几何只在
`models/block/barrel_base.json` 里维护，子模型只覆盖 `textures`。

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
重跑 `scripts/generate_ore_variants.py`（以及需要的 `generate_sieve_assets.py`），然后重启。
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
| `ingot` | ❌ | 已有锭：写**具体物品 id**（`minecraft:iron_ingot`），熔炼就产出它，不重复造锭。省略时注册自有 `exnihilosatellitium:<id>_ingot`（9 种非原版金属走这条）。**暂不支持标签**（`#c:ingots/tin`）：材料表在标签加载前读取，写标签会打警告并按自有锭处理 |
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

`scripts/generate_ore_variants.py` 读材料表，除了方块/物品资源，还生成：

| 文件 | 内容 |
|---|---|
| `data/exnihilosatellitium/recipe/<id>_<stage>_to_ore_<kind>.json` | 2×2：4 个破碎/粉碎/粉末 → 对应矿砾/矿砂/矿尘方块 |
| `data/exnihilosatellitium/recipe/<id>_ore_<kind>_smelting.json` | 熔炼：矿砾/矿砂/矿尘 → 锭（0.1 xp，200 tick） |
| `data/c/tags/item/ingots/<id>.json` | 自有锭加入 `c:ingots/<id>`，方便别的模组用标签接受它 |
| `data/c/tags/block/ores/<id>.json` | 矿砾/矿砂/矿尘方块 → `c:ores/<id>`（方块标签） |
| `data/c/tags/item/ores/<id>.json` | 同名方块物品 → `c:ores/<id>`（物品标签） |
| ~~`data/c/tags/item/raw_materials/<id>.json`~~ | **已删除（2026-10-31）**：破碎/粉碎/粉末不是原矿，不能进 `c:raw_materials`，否则别的模组会把它们当原矿处理，绕过「4 块 → 方块 → 熔炼」链条 |
| `data/c/tags/{block,item}/ores.json` | 把本项目材料的 `#c:ores/<id>` 子标签挂到根标签（只含矿砾/矿砂/矿尘方块，对应 1.7.10 把矿方块注册给 TE/TC 的做法） |

- 铁/金/铜的 `ingot` 指向原版锭，所以 36 个熔炼配方里它们熔到 `minecraft:*_ingot`；
  其余 9 种熔到自有锭。
- 锭贴图由 `art/items/ingots/generate.py` 用同一套 5 阶色板生成，16×16、自有像素。
- **改了材料表就要重跑生成脚本**；否则会出现「注册了自有锭但没有熔炼配方」的错位。
- 粉碎/粉末现在有两条通路：4 块合成 + M3 锤子降档（矿砾→粉碎、矿砂→粉末）。

## 9. 筛子 JSON（M2）

**路径**：`data/<namespace>/sieve/*.json`，一个输入一个文件。

```json
{
  "input": "minecraft:gravel",
  "outputs": [
    { "item": "exnihilosatellitium:iron_broken", "chance": 0.2 },
    { "item": "minecraft:flint", "chance": 0.25 }
  ]
}
```

- `input` 是物品 id；筛子一次只装 1 个该物品。
- `outputs` 每条独立掷 `chance`（0..1），不是权重；一次结算可能同时出多个产物。
- `chance <= 0` 或物品 id 解析不到：该条跳过并打 `[sieve]` 警告。
- 服务端与客户端各读一次：服务端用于结算，客户端用于 JEI。客户端的资源管理器只暴露
  `assets/`，所以 `processResources` 会把本目录镜像到 `assets/exnihilosatellitium/sieve/`；
  **仓库里只改 `data/` 这一份**，构建时自动复制。
- 当前有 6 个输入、74 掉落：`gravel` / `sand` / `dirt` / `dust`，以及 M6 加的
  **方块** `crushed_netherrack` / `crushed_end_stone`（锤下界岩 / 末地石得到）；`dust` 输入是 M3 的通用尘土方块，
  `dirt` 是空岛农业起点（种子/作物 + **草种子** + 甜浆果），`sand` 额外给仙人掌、甘蔗、可可豆、海带、
  管珊瑚、海晶砂粒与**古代孢子**（`ancient_spores`，1/128，M6 巫水链起点），`gravel` 额外给海晶碎片；
  `crushed_netherrack` 出石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙，`crushed_end_stone` 出末地石/紫颂果/末影珍珠；
  `sand` 另有 5 种珊瑚（管/脑/气泡/火/角）各 1% 与海晶碎片 2%，`gravel` 也有海晶碎片 2%。
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
  两个都有 / 都没有 → 跳过该文件并打 `[barrel]` 警告。
- 流体按 registry id 精确匹配（still 水 / 熔岩）；未知 base / added / result 只跳过该条，
  不影响其它文件。
- 客户端 JEI 读镜像后的 `assets/.../barrel/`；`/reload` 或重启后生效，不需要改 Java。
- 当前 7 条：水+熔岩→圆石、熔岩+水→黑曜石、熔岩+红石→下界岩、
  熔岩+荧石粉→末地石、水+尘土→粘土块（`water_dust_clay.json`）；
  M6 巫水两条：巫水+沙子→灵魂沙（`witch_water_sand_soul_sand.json`）、
  巫水+橡树苗→深色橡树苗（`witch_water_sapling_dark_oak.json`）。
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
