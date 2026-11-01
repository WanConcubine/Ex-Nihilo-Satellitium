# 移植与版本分层（1.20.1 / 1.21.1 / 未来版本）

> 目标：**1.21.1 NeoForge（`main`）+ 1.20.1 Forge（`1.20.1-forge` 分支）**同时维护，
> 以后再加 1.21.x / 1.22 时只改“平台层”，核心逻辑与数据格式不动。
> 本文件是移植的总清单；当前状态仍以 [status.md](status.md) 为准。

## 1. 现状（2026-11-01，P1 之后，main）

- `src/main/java` 共 **111 个文件**，其中 **36 个** import `net.neoforged`。
- **core 已经干净**：`sieve` / `hammer` / `crook` / `compost` / `barrel` / `crucible` /
  `material` / `config` / `platform`（core 门面）以及 `machine` 顶层类 **0 个 NeoForge import**；
  `MachineBlockEntity` 也只经 `MachineHooks` 调能力。`check-core-imports.py` 的 known 已从 4 清零。
- 版本耦合集中在：顶层 `platform/`（5 个文件：`NeoForgeModProbe`、`config/ModConfig`、
  `machine/{MachineEnergy,MachineItemHandler,MachineHooks}`）、`registry/*`（8 个）、
  `client/*`（渲染/界面）、`common/event/*`、`common/network/*`、`common/item/*`、
  `common/block/*`、`common/blockentity/*`、`compat/*`。
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
             需要平台能力时只走窄门面：ConfigValues / Platform / MachineHooks / GuiLayers。
platform/    版本胶水：注册、事件、能力、网络、配置 spec、BE 菜单/同步、渲染、compat。
             包名就是 com.kaifengcrt.exnihilosatellitium.platform（与 core 的
             common/config、common/platform 门面不是一回事：那两个是接口，platform/ 是实现）。
data/        JSON 数据。自定义表两版共用；vanilla 类型（recipe/loot/tag）按目标版本转换。
scripts/     生成器按 --target 输出对应路径/格式；检查脚本。
docs/        本文件 + status/parity 的版本标注。
```

`CORE` 的边界用 `scripts/check-core-imports.py` 守住（新加的 NeoForge import 直接失败，
已知漏点列在脚本的 `KNOWN` 里，逐项清零）。

## 3. 重构顺序（按“移植收益 / 工作量”排序）

### P0 —— 守边界（本批次可做）

- [x] `scripts/check-core-imports.py`：逻辑包 + machine 纯类纳入守护，新增 NeoForge import
      直接失败；P1 之后 `KNOWN` 已清空（4 → 0），`common/config`、`common/platform` 也在受检范围。
- [x] `OreMaterials` 不再 import `ModList`：改为 `OreMaterials.init()`（mod 构造期、注册之前）
      + `Platform.isModLoaded`；`ModProbe` 是 core 的一行接口，`platform/NeoForgeModProbe`
      用 `ModList` 实现，构造期 `Platform.install(...)` 注入。
- [x] `MachineEnergy` / `MachineItemHandler` 移到 `platform/machine/`（它们本来就是每版重写的胶水）；
      缓冲数学抽成 core 的 `MachineEnergyBuffer`，`MachineBlockEntity` 只碰 buffer 和
      `MachineHooks.insertIntoNeighbour(...)`。脚本不再把它们列为已知例外。

### P1 —— 薄门面（小、收益直接）—— ✅ 2026-11-01 完成

- [x] `ConfigValues`（`common/config`）：`ConfigSource` 是 core 接口（7 个 getter），
      `ConfigValues` 是静态门面，`platform/config/ModConfig` 用 `ModConfigSpec` 实现并在构造期
      `install`。10 个调用点改完；门面是**委托读**，所以配置热改/`/reload` 行为不变。
- [x] `Platform` + `ModProbe`（`common/platform`）：`isLoaded` / `version` 两个最小方法；
      `platform/NeoForgeModProbe` 包住 `ModList`，`OreMaterials` / `MoonlightCompat` /
      机器 GUI 的 JEI 探测都走它。`dataDir` 等到 P2 真需要时再加，避免空接口。
- [x] `MachineHooks`（`platform/machine`）：自动输出只调
      `MachineHooks.insertIntoNeighbour(level, pos, face, stack)`；能力查询/`ItemHandlerHelper`
      都留在平台层。能量侧是 `MachineEnergyBuffer`（core）+ `MachineEnergy`（`EnergyStorage` 适配器）。
- [x] `GuiLayers`（`client`）：`push/pop` 包住 `Minecraft#pushGuiLayer`，自动输出抽屉只调门面。
      1.20.1 没有 GUI layer 系统，移植时在这里换成自绘/重新打开菜单。
- 验证：`./gradlew build` 通过；`check-core-imports.py` → `0 known leak(s)`；`runServer` 起来后
  RCON 验过 20 FE/t、自动输出进箱子、漏斗只抽输出格（细节在 [status.md](status.md) 验证表）。

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
