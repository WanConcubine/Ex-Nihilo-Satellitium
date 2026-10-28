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
    "minecraft:carrot": 100,
    "minecraft:bread": 160
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

原版 Ex Nihilo 用 0..1 的 float（树苗/树叶 0.125、花/蘑菇 0.10、小麦 0.08、肉 0.20、鱼 0.15）。
本项目改为 **1000 分制**，乘 1000 后全是整数：

| 类别 | 原版值 | 本模组值 | 单独塞满 1000 最少要 |
|---|---:|---:|---:|
| 树苗、树叶 | 0.125 | 125 | 8 |
| 花、蘑菇、苹果、腐肉、仙人掌、藤蔓、海带等 | 0.10 | 100 | 10 |
| 鱼 | 0.15 | 150 | 7 |
| 生/熟肉 | 0.20 | 200 | 5 |
| 小麦、种子、鸡蛋、甘蔗、胡萝卜/土豆/甜菜根、烤土豆/毒土豆、高草 | 0.08 | 80 | 13 |
| 面包、南瓜派 | 0.16 | 160 | 7 |
| 西瓜/南瓜块、雕刻南瓜、南瓜灯 | 1/6 | 166 | 7 |
| 西瓜片、线 | 0.04 | 40 | 25 |

经典物品严格按原版 1.7.10 的比例；1.20 新物品（海带、浆果、竹子、滴水叶、孢子花等）
按 Sequentia 的 100/80 分类值补。Sequentia 把胡萝卜/土豆/甜菜根/烤土豆/毒土豆/高草
提高到 100/150/200，本项目保留原版的 **80**。完整表在 `compost/default.json`。

### 标签必须同步

`data/exnihiloimitatio/tags/items/compostable.json`：

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
- 默认 JSON 有 **60 条**（12 个标签 + 48 个具体物品）；`#minecraft:crops` / `#minecraft:seeds`
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
/setblock ~ ~ ~ exnihiloimitatio:barrel

# 其他材质：spruce_barrel / birch_barrel / jungle_barrel /
# acacia_barrel / dark_oak_barrel / stone_barrel
/setblock ~ ~ ~ exnihiloimitatio:stone_barrel

# 直接写满堆肥，1 tick 后完成
/data merge block ~ ~ ~ {Compost:1000,CompostTicks:1}

# 看状态
/data get block ~ ~ ~ DirtReady
/data get block ~ ~ ~ Compost
/data get block ~ ~ ~ CompostTicks

# 确认还是桶（不是泥土）
/execute if block ~ ~ ~ exnihiloimitatio:barrel

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

`assets/exnihiloimitatio/lang/{en_us,zh_cn}.json`：

| 键 | 用途 |
|---|---|
| `jade.exnihiloimitatio.barrel.ready` | 泥土已就绪 |
| `jade.exnihiloimitatio.barrel.compost` | `堆肥：x / 1000` |
| `jade.exnihiloimitatio.barrel.timer` | `还需 n 秒` |
| `jade.exnihiloimitatio.barrel.fluid` | `流体：x / 1000 mB` |

Provider：`compat/jade/BarrelComponentProvider.java`；只读已同步的 BE，不发额外网络包。

## 6. 相关代码

- `common/compost/CompostRegistry.java`：解析 JSON、按物品/标签查值。
- `common/compost/CompostReloadListener.java`：资源重载入口。
- `common/tag/ModTags.java`：`#exnihiloimitatio:compostable`。
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
