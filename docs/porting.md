# 移植与版本分层（1.20.1 / 1.21.1 / 未来版本）

> 目标：**1.21.1 NeoForge（`main`）+ 1.20.1 Forge（`1.20.1-forge` 分支）**同时维护，
> 以后再加 1.21.x / 1.22 时只改“平台层”，核心逻辑与数据格式不动。
> 本文件是移植的总清单；当前状态仍以 [status.md](status.md) 为准。

## 1. 现状（2026-11-01，main）

- `src/main/java` 共 **103 个文件**，其中 **40 个** import `net.neoforged`，63 个只依赖
  `net.minecraft` / `com.mojang` / `com.google.gson`。
- **判定/数据逻辑本来就干净**：`sieve` / `hammer` / `crook` / `compost` / `barrel` /
  `crucible` 六个包 **0 个 NeoForge import**；`machine` 包里的
  `SieveRolls` / `HammerRolls` / `MachineKind` / `UpgradeType` / `UpgradeItem` 也干净。
  已知漏点只有 4 个文件（见 §3 P0）。
- 版本耦合集中在：`registry/*`（8 个）、`client/*`（渲染/界面）、`common/event/*`、
  `common/network/*`、`common/config/*`、`common/item/*`、`common/block/*`、`common/blockentity/*`、
  `common/machine/blockentity/*`、`compat/*`。
- 数据：**145 个 recipe + 69 个 loot_table + 41 个 tag** 是 vanilla 类型，路径/格式随版本变
  （1.21 单数、`result.id`；1.20.1 复数、`result.item`）；自定义表（sieve/hammer/crook/compost/
  barrel/crucible_*）是我们自己的目录，**两版同构**。
- `DeferredHolder` 152 次，但只分布在 **12 个文件**（registry + `compat/jei` + `OreColors`）；
  其余代码只调 `.get()`。移植时改注册类即可，不需要全仓替换。
- `ModConfig.*` 只有 **10 个调用点**；`DeferredHolder` 与 config 都适合做薄门面，不适合过度抽象。

## 2. 分层约定

```
core/        纯逻辑：数据记录 + JSON 表解析 + 掷表/权重/状态机。
             只允许 net.minecraft / com.google.gson / java；不得 import net.neoforged。
platform/    版本胶水：注册、事件、能力、网络、配置 spec、BE 菜单/同步、渲染、compat。
data/        JSON 数据。自定义表两版共用；vanilla 类型（recipe/loot/tag）按目标版本转换。
scripts/     生成器按 --target 输出对应路径/格式；检查脚本。
docs/        本文件 + status/parity 的版本标注。
```

`CORE` 的边界用 `scripts/check-core-imports.py` 守住（新加的 NeoForge import 直接失败，
已知漏点列在脚本的 `KNOWN` 里，逐项清零）。

## 3. 重构顺序（按“移植收益 / 工作量”排序）

### P0 —— 守边界（本批次可做）

- [x] `scripts/check-core-imports.py`：六个逻辑包 + machine 纯类纳入守护；已知漏点列在
      脚本的 `KNOWN` 里（有 4 个），新增 NeoForge import 直接失败。
- [ ] P1 用 `OreMaterials.init(ModProbe)` 显式注入替代 `ModList`（`ModProbe` 是 core 里的
      一行接口，平台传 `ModList::isLoaded`），材料表就完全干净。
- [ ] `MachineEnergy` / `MachineItemHandler` / `MachineBlockEntity` 里的 `IItemHandler`/
      `EnergyStorage`/`Capabilities` 属于“机器平台层”，**不强行抽成 core**；在包里加注释说明，
      脚本把它们列为已知例外。

### P1 —— 薄门面（小、收益直接）

- [ ] `ConfigValues`：core 只读 `ConfigValues.crucibleCapacity()` 之类；`ModConfig`（NeoForge
      spec）作为实现。10 个调用点，1.20.1 只要换 spec 类。
- [ ] `Platform`：`isModLoaded` / `modVersion` / `dataDir` 这类最小接口。
- [ ] `MachineHooks`：把 `Capabilities.ItemHandler.BLOCK`、`ItemHandlerHelper`、`EnergyStorage`
      的查询收进一个平台类；BE 只调 `MachineHooks.push(...)` / `MachineHooks.energy(...)`。
- [ ] 客户端 `GuiLayers`：`MachineOutputScreen` 的压栈/出栈（1.21 用
      `Minecraft#pushGuiLayer`，1.20.1 Forge 没有）包成 `GuiLayers.push/pop`。

### P2 —— 数据管线（移植成本最大的一块）

- [ ] 生成器加 `--target 1.21.1|1.20.1`，统一处理：
      - 目录：`recipe/` ↔ `recipes/`、`loot_table/` ↔ `loot_tables/`、
        `tags/block` ↔ `tags/blocks`、`tags/item` ↔ `tags/items`；
      - 配方：`result.id` ↔ `result.item`；
      - 条件：`neoforge:mod_loaded` ↔ `forge:mod_loaded`（`conditions` 数组）。
- [ ] 写一个 `scripts/port_data.py`：从当前 1.21 数据生成 1.20.1 版本的数据（只做路径/字段/条件
      转换，不改数值），放到 1.20.1 分支用。
- [ ] `check-textures.py` 增加一个“数据路径与目标版本一致”的检查（防 tech-debt #1 重演）。

### P3 —— 状态机拆分（为移植也为测试）

- [ ] `BarrelBlockEntity`（717 行）拆成 `BarrelState`（堆肥/烹饪/孢子/雨 tick、固体/流体读写）
      + BE 胶水（NBT/菜单/能力/音效）；`BarrelState` 不 import NeoForge。
- [ ] `SieveBlockEntity` / `CrucibleBlockEntity` / `MachineBlockEntity` 同样把“每 tick 的状态推进”
      与“MC 对象操作”分开；`MachineBlockEntity` 的能力部分走 P1 的 `MachineHooks`。
- [ ] 拆完后可以在无 MC 环境里给状态机写纯 JUnit（tech-debt P3 #37 的零测试）。

### P4 —— 物品状态编码（1.20.1 的真实障碍）

- [ ] 1.21 的 `DataComponents`（瓷桶 `fluid_contents`、侵染树叶 `infestation`）在 1.20.1 是 NBT。
      抽一个 `ItemStateCodec<T>`（`read(ItemStack)` / `write(ItemStack, T)`），
      1.21 实现走组件，1.20.1 实现走 `getOrCreateTag()`；调用方不直接碰组件 API。

## 4. 1.20.1 ↔ 1.21.1 API 差异速查（按我们代码的实际用法）

| 主题 | 1.21.1 NeoForge（现在） | 1.20.1 Forge（移植时） | 我们哪里用到 |
|---|---|---|---|
| 注册项类型 | `DeferredHolder` | `RegistryObject` | 12 个文件，`.get()` 调用点不用改 |
| 注册 / 事件包 | `net.neoforged.neoforge.*` | `net.minecraftforge.*` | registry/event/client |
| 事件订阅 | `@EventBusSubscriber(bus=MOD)` | `@Mod.EventBusSubscriber` + `Bus` | 6 个类 |
| 方块交互 | `useItemOn` / `useWithoutItem` | `Block#use`（已弃用）+ `useItemOn` 不存在 | `BarrelBlock` / `SieveBlock` / `CrucibleBlock` / `MachineBlock` |
| BE 存档 | `saveAdditional(CompoundTag, HolderLookup.Provider)` / `getUpdateTag(Provider)` | `saveAdditional(CompoundTag)` / `getUpdateTag()` | 10 处 save/load |
| 物品比较 | `ItemStack.isSameItemSameComponents` | `isSameItemSameTags` | 2 处 |
| ID | `ResourceLocation.parse` / `tryParse` | `new ResourceLocation` / `tryParse` | 22 处 |
| 能力 | `Capabilities.ItemHandler.BLOCK`、`registerBlockEntity` | `ForgeCapabilities.ITEM_HANDLER` + `LazyOptional`、`AttachCapabilitiesEvent` | `CapabilityEvents`、机器、瓷桶 |
| 能量 | `net.neoforged.neoforge.energy.EnergyStorage` | `net.minecraftforge.energy.EnergyStorage`（同名不同包） | `MachineEnergy` |
| 网络 | `CustomPacketPayload` + `RegisterPayloadHandlersEvent` | `SimpleChannel` | `ModNetwork` |
| 配置 | `ModConfigSpec` | `ForgeConfigSpec` | `ModConfig` |
| 客户端叠加层 | `Minecraft#pushGuiLayer` / `Screen#onClose` | 无 GUI layer 系统，需要自己渲染父界面或用 `setScreen` | `MachineOutputScreen` |
| 物品状态 | `DataComponents`（组件） | NBT（`getOrCreateTag`） | 瓷桶、侵染树叶 |
| FluidTank NBT | `{Fluid:{id,amount}}` | `{FluidName, Amount}` | 桶、坩埚、瓷桶 |
| 数据路径 | `recipe/` `loot_table/` `tags/block` | `recipes/` `loot_tables/` `tags/blocks` | 255 个 JSON |
| 配方结果 | `{"id": ..., "count": n}` | `{"item": ..., "count": n}` | 145 个 recipe |
| 条件 | `neoforge:mod_loaded` | `forge:mod_loaded` | `ingots` 候选、压缩块联动 |

## 5. 两条不要做的事

1. **不要为了“共用”把 registry/能力/网络抽成复杂框架**：这些本来就是每版重写一次的薄层，
   12 个注册文件、1 个网络类、1 个能力类，重写的成本低于抽象的成本。
2. **不要在两个分支里各写一份数据生成器**：P2 做完后，数据只有一份输入 + 两个 target 的输出，
   避免 `recipes/` ↔ `recipe/` 这种漂移（tech-debt #1 已经踩过一次）。
