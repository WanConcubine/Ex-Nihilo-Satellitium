# 数据格式：堆肥、NBT、命令

> 适用：M1 木桶（Forge 1.20.1）。代码位置：
> `common/compost/`、`common/blockentity/BarrelBlockEntity.java`。

## 1. 堆肥 JSON

**路径**：`data/<namespace>/compost/*.json`（可以拆多个文件）

```json
{
  "values": {
    "#minecraft:leaves": 4,
    "#minecraft:saplings": 8,
    "minecraft:carrot": 4,
    "minecraft:hay_block": 18
  }
}
```

规则：

- `values` 是对象；key 可以是**物品 id** 或 **`#标签`**。
- value 是**正整数**；小于等于 0 的条目会被忽略。
- 同一个 stack 命中多条时，取**最大值**（不是相加）。
- 总和到 `100` 填满；`COMPOST_MAX = 100`。
- 填满后计时 `COMPOST_TICKS = 600`（30 秒），然后进入 DIRT。
- 服务端在资源重载时读取；改完执行 `/reload`（或重启）生效。
- **客户端不读这个 JSON**；客户端只用物品标签做交互预测。

### 标签必须同步

`data/exnihiloimitatio/tags/items/compostable.json`：

```json
{
  "replace": false,
  "values": [
    "#minecraft:leaves",
    "#minecraft:saplings",
    "minecraft:carrot",
    "minecraft:hay_block"
  ]
}
```

- 服务端 `use()` **先查标签**，再查 JSON：
  - 标签有、JSON 没有 → 按 **1 点** 处理（`Math.max(1, value)`）。
  - JSON 有、标签没有 → **不能堆肥**（服务端也会拒绝）。
- 所以新增堆肥物品时，**两处都要加**。
- 默认 JSON 有 30 条；`#minecraft:crops` / `#minecraft:seeds` 不是 1.20.1 物品标签，别用。

## 2. 方块实体 NBT

木桶 BE 的 NBT 是**顶层键**（不是 `{Fluid:{...}}`）：

| 键 | 类型 | 含义 |
|---|---|---|
| `FluidName` | string | 流体 id，空桶是 `minecraft:empty` |
| `Amount` | int | 流体量，0–1000 mB |
| `Compost` | int | 堆肥进度，0–100 |
| `CompostTicks` | int | 剩余计时 tick；>0 表示正在腐熟 |
| `DirtReady` | boolean | `1b` 表示泥土已就绪，右键可取 |

- 容量固定 `CAPACITY = 1000` mB，不依赖 NBT。
- 桶里有流体时不能堆肥；堆肥进度 > 0 时不能灌流体（互斥）。
- DIRT 状态下右键**任意手持物**都会取出 1 个泥土并重置（原版 `ExtractMode.Always` 风格）。

## 3. 测试命令

```mcfunction
# 放一个桶
/setblock ~ ~ ~ exnihiloimitatio:barrel

# 直接写满堆肥，1 tick 后完成
/data merge block ~ ~ ~ {Compost:100,CompostTicks:1}

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
- 其他状态：`max(流体量 × 15 / 1000, Compost × 15 / 100)`。

## 5. Jade tooltip 键

`assets/exnihiloimitatio/lang/{en_us,zh_cn}.json`：

| 键 | 用途 |
|---|---|
| `jade.exnihiloimitatio.barrel.ready` | 泥土已就绪 |
| `jade.exnihiloimitatio.barrel.compost` | `堆肥：x / 100` |
| `jade.exnihiloimitatio.barrel.timer` | `还需 n 秒` |
| `jade.exnihiloimitatio.barrel.fluid` | `流体：x / 1000 mB` |

Provider：`compat/jade/BarrelComponentProvider.java`；只读已同步的 BE，不发额外网络包。

## 6. 相关代码

- `common/compost/CompostRegistry.java`：解析 JSON、按物品/标签查值。
- `common/compost/CompostReloadListener.java`：资源重载入口。
- `common/tag/ModTags.java`：`#exnihiloimitatio:compostable`。
- `common/blockentity/BarrelBlockEntity.java`：状态机、NBT、比较器、粒子/音效。
- `client/BarrelRenderer.java`：液面 / 堆肥 box / DIRT 泥土 box。
