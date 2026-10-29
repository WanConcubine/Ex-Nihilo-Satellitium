# 数据格式：堆肥、NBT、命令

> 适用：M1 木桶 + 七种材质变体（Forge 1.20.1）。代码位置：
> `common/compost/`、`common/blockentity/BarrelBlockEntity.java`。
>
> 七种桶（`barrel` = 橡木、`spruce_barrel`、`birch_barrel`、`jungle_barrel`、
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
  不是 1.20.1 物品标签，别用；用 `#forge:crops/*` / `#forge:seeds`。

## 2. 方块实体 NBT

木桶 BE 的 NBT 是**顶层键**（不是 `{Fluid:{...}}`）：

| 键 | 类型 | 含义 |
|---|---|---|
| `FluidName` | string | 流体 id，空桶是 `minecraft:empty` |
| `Amount` | int | 流体量，0–1000 mB |
| `Compost` | int | 堆肥进度，0–1000 |
| `CompostTicks` | int | 剩余计时 tick；>0 表示正在腐熟 |
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
      "rarity": { "gravel": 18, "sand": 32, "dust": 48 },
      "ingot": "forge:ingots/copper",
      "owner_mod": "mymod",
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
| `tier` | ✅ | `common` / `uncommon` / `rare` / `epic`，只用于校验数值区间与文档 |
| `rarity` | ✅ | 三种筛料的 `1/N` 的 N，必须 > 0；人工调过手感才允许提交 |
| `ingot` | ❌ | 已有锭的注册名；填了就熔炼到它，不重复造锭 |
| `owner_mod` | ❌ | 主要来源模组，只用于自检与文档 |
| `enabled` | ❌ | 默认 `true`；表外骨架行默认 `false` |

### 规则

- **同一 `id` 只能有一个生效来源**：扩展表覆盖内置表，数据包再覆盖两者。
- **表外材料不注册**：启动时只处理表内条目；发现表外材料只写骨架行（`enabled: false`）。
- **校验失败即跳过该条**（不拖垮启动）：`id` 非法、颜色格式错、`rarity <= 0`、重复 `id`。
  日志用 `[materials] ...` 前缀，和 `[compost]` 保持一致的风格。
- **注册名不可变**：`id` 改了就等于换了材料，旧存档里的物品会变成缺失——发布后不要改已用的 `id`。
