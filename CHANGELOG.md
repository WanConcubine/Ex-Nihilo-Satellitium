# 更新记录

## 1.5.0 — 2026-11-13（机器扩容升级）

> 两条线同版本、同功能：1.21.1 主线 + 1.20.1 线都加入扩容升级。

### 中文

- **扩容升级**（`capacity_upgrade`）：电动筛 / 电动锤的槽 4 新插件；每个物品增加 1 页
  （12 格）产物缓存，堆叠上限 8（基础 1 页 + 8 = 9 页 / 108 格）。
- **产物翻页**：输出 3×4 下方新增 `< 1/9 >`，只对多页机器显示；拆掉扩容后高页物品并回
  活动页或掉在机器上方，不会丢失。
- **IO 不分页**：漏斗 / 管道 / 自动输出看到的是全部活动页拼成的平铺输出缓存，不需要知道页。
- **合成**：铁锭 / 纸 / 铁锭 + 纸 / 任意箱子 / 纸 + 铁锭 / 纸 / 铁锭；箱子走
  `#exnihilosatellitium:chests`（箱子 / 陷阱箱 / 末影箱）。
- **修复**：机器输出在“先并进半组、后面没有空槽”时会把已并入的部分再整份掉出（复制）；
  现在只掉真正没放下的余量。
- **验证**：`build` + 纹理 / core / data / wiki 守卫通过；RCON 验证 9 页读写、自动输出与漏斗
  从第 9 页抽取、基础页满后继续往第 2 页产出、缩页回收与停机。**GUI 翻页和实际合成未目视。**

### English

- **Expansion Upgrade** (`capacity_upgrade`): a new slot-4 plugin for the electric sieve / hammer;
  each item adds one 12-slot output page, stack limit 8 (base page + 8 = 9 pages / 108 slots).
- **Output paging**: a `< 1/9 >` control appears under the 3x4 output grid when the machine has
  more than one page. Removing the upgrade merges the high pages back or drops the overflow
  above the machine instead of voiding it.
- **IO stays flat**: hoppers, pipes and auto output see every active page as one flat output
  buffer and never deal with page numbers.
- **Recipe**: iron ingot / paper / iron ingot, paper / any chest / paper, iron ingot / paper /
  iron ingot; "any chest" is `#exnihilosatellitium:chests` (chest, trapped chest, ender chest).
- **Fix**: machine output could duplicate items when a stack was partly merged into an existing
  stack and then ran out of empty slots; only the true remainder is dropped now.
- **Verified**: `build` and the texture / core / data / wiki guards pass; RCON covers 9-page
  read/write, auto output and hopper extraction from page 9, production into page 2 while the
  base page is full, and page-removal recovery / shutdown. **The GUI paging and the actual
  crafting were not visually checked.**

## 1.4.2 — 2026-11-11（黏液草 / 匠魂树苗 / 连锁图标）

> **1.21.1 线本版没有代码改动**，只同步版本号；改动在 1.20.1 线的黏液链与连锁图标。

### 中文

#### 新增（1.20.1）

- **大地黏液草种子**：`earth_slime_grass_seeds`（装匠魂才有），泥土 / 草方块各 **2%** 筛出；
  右键泥土 / 草方块 → `tconstruct:earth_vanilla_slime_grass`。
- **黏液泥土筛表**：`sieve/{earth,sky,ender}_slime_dirt.json`，10% 出同种匠魂树苗、
  25% 出同种黏液球（大地那档是原版 `minecraft:slime_ball`）。

#### 变更（1.20.1）

- **黏液草额外掉黏液泥土**：种子仍然种 `*_vanilla_slime_grass`（原途径不变）；
  `LootTableLoadEvent` 给它的掉落表**追加**一份同种 `<type>_slime_dirt`，
  **原有 `minecraft:dirt` 保留**，匠魂文件不改。空岛第一次有黏液泥土来源。

#### 修复（1.20.1）

- **连锁强化图标**：补 `tinkering/modifier_icons.json` + 16×16 图标，工作台 / JEI 不再空图标。

#### 版本对齐

- **1.21.1 线**：代码、数据表、贴图都没改，本版只为两条线版本号一致（同 1.1.2 的做法）。

#### 验证

- 1.20.1：`build`、纹理守卫（315 模型 / 213 纹理）、data 守卫通过；`-PtechMods -PdeMods`
  服务端 `[sieve] 10 inputs, 183 drops`、无未知物品；RCON 验证黏液草能放住并掉黏液泥土。
- **未做**：黏液泥土实际手筛、树苗生长、JEI 页面。

### English

#### New (1.20.1)

- **Earthslime grass seeds**: `earth_slime_grass_seeds` (only with Tinkers), **2%** from dirt /
  grass blocks; right-click dirt / grass to grow `tconstruct:earth_vanilla_slime_grass`.
- **Slime dirt sifting**: `sieve/{earth,sky,ender}_slime_dirt.json` - 10% for the matching
  Tinkers sapling, 25% for the matching slime ball (earth uses vanilla slime balls).

#### Changed (1.20.1)

- **The slime grass drops slime dirt too**: the seeds still grow `*_vanilla_slime_grass`
  (the original path is untouched); a `LootTableLoadEvent` hook appends the matching
  `<type>_slime_dirt` to that block's table, so the original `minecraft:dirt` drop stays and
  TiC's files are untouched. This is the skyblock's first slime-soil source.

#### Fixes (1.20.1)

- **Chain modifier icon**: added `tinkering/modifier_icons.json` and a 16x16 icon, so the
  modifier worktable / JEI no longer show a blank icon.

#### Version alignment

- **The 1.21.1 line has no code changes** in this release; it only shares the version number.

#### Verification

- 1.20.1: `build`, texture guard (315 models / 213 textures) and data guard pass; the
  `-PtechMods -PdeMods` server logs `[sieve] 10 inputs, 183 drops` with no unknown items; RCON
  confirms the slime grass block stays and drops slime dirt.
- **Not done**: actually sifting slime dirt in game, sapling growth, the JEI page.

## 1.4.1 — 2026-11-11（匠魂碎矿锤配方 / 版本对齐）

> **1.21.1 线本版没有代码改动**，只同步版本号；真正的改动在 1.20.1 线的匠魂碎矿锤。
> 没有新增物品，也不改任何数据表结构。

### 中文

#### 变更

- **1.20.1：匠魂碎矿锤（`ex_nihilo_hammer`）不再和匠魂大锤同款**：装配站从
  「锤头 + 坚韧手柄 + 2 大板」改成 **1 锤头 + 1 坚韧手柄**；工具定义的部件列表、
  `default_materials` 与装配站输入槽一起减到两件。
- **1.20.1：修复挖掘速度**：砂砾 / 沙子 / 尘土这类 `mineable/shovel` 方块以前按空手速度挖，
  现在按锤头材料的挖掘速度（铁头 6.0）；去掉头部 0.5 缩放后耐久回到 1.4.0 的水平（铁头约 1000）。
- **1.20.1：连锁挖掘**：新增强化 `exnihilosatellitium:chain`（I–III，装配站 4 燧石 + 1 铁块），
  一次粉碎连带打碎相连的同类方块，每级多 16 块，锤头会多出两节锁链的 overlay；DE 锤新增
  **专属连锁模块** `chain_mining_module`（4 燧石 + 4 铁锭 + 1 module core），装上后一次连锁 32 块。
- **1.21.1 线**：代码、数据表、贴图都没改，本版只为两条线版本号一致。

#### 验证

- 两版 `./gradlew build --offline` 与 `check-textures.py` 通过。
- 1.20.1 服务端 `-PtinkersMods`：`Loaded 46 tool definitions` / `24 station slot layouts`、
  无解析错误；`exnihilosatellitium:chain` 强化注册与配方加载成功；FakePlayer 探针
  （临时命令，已删）铁头 + 木柄：6.0 速度 / 1000 耐久，连锁 17 / 33 块，夹一块沙子会停。
  `-PdeMods -PtinkersMods` + DE 探针：连锁模块进注册表、物品与合成加载、DE 锤宿主接受，
  `chainLimit` 0 → 32。
- **未做**：装配站应用强化的画面、锁链 overlay 外观、DE 实机装模块、JEI 强化配方页。

### English

#### Changed

- **1.20.1: the Tinkers' Construct crushing hammer (`ex_nihilo_hammer`) is no longer a clone of
  the sledge hammer**: the Tinker Station build went from *hammer head + tough handle + 2 large
  plates* to **1 hammer head + 1 tough handle**. The tool definition's part list,
  `default_materials` and the station layout's input slots all shrank to two parts.
- **1.20.1: mining speed fixed**: gravel / sand / dust (tags `mineable/shovel`) used to be mined
  at bare-hand speed; the hammer now uses its head material's mining speed (6.0 for iron).
  Dropping the head's 0.5 scale also brings durability back to the 1.4.0 level (~1000 for iron).
- **1.20.1: chain mining**: a new `exnihilosatellitium:chain` modifier (I–III, Tinker Station:
  4 flint + 1 iron block) makes one crush break connected blocks of the same kind, +16 per level,
  and adds a chain overlay to the hammer head; the Draconic Evolution hammer gets a dedicated
  chain module (`chain_mining_module`, 4 flint + 4 iron ingots + 1 module core) that chains 32
  blocks per crush.
- **The 1.21.1 line has no code changes** in this release; it only shares the version number.

#### Verification

- `./gradlew build --offline` and `check-textures.py` pass on both lines.
- 1.20.1 server with `-PtinkersMods`: `Loaded 46 tool definitions` /
  `24 station slot layouts`, no parse errors; the `chain` modifier registered and its recipe
  loaded; a FakePlayer probe (temporary command, since removed) with an iron head + wood handle
  measured 6.0 mining speed / 1000 durability and 17 / 33 chained blocks, stopping at a sand
  block.
- **Not done**: the in-game station screen for applying the modifier, chain mining with the DE
  hammer in game, and the JEI page for the modifier recipe.

## 1.4.0 — 2026-11-10（材质锤 / 龙之进化锤 / 修复）

### 中文

#### 新增

- **9 把金属材质锤**：锡 / 铝 / 锌 / 铜 **256**、银 **320**、铅 **384**、镍 **448**、锇 **512**、
  铂 **1920**。以 64 为基数，钻石 1561 仍是普通材质上限；配方沿用斜形（2 锭 + 2 木棍），
  head 走 `c:ingots/<id>`（1.20.1 为 `forge:ingots/<id>`），铜用原版铜锭。
- **3 把龙之进化锤**（装 DE 时）：`wyvern_hammer` / `draconic_hammer` / `chaotic_hammer`
  继承 DE 的 `ModularPickaxe`，走聚合装置升级链（钻石锤 → 双足飞龙 → 神龙 → 混沌，
  8M / 32M / 128M OP），可用 DE 的速度 / AOE / 能量模块；不消耗耐久，走 DE 能量（256/次）。
  3D cube 模型（左右锤头 + 发光核心 + 长柄）。

#### 修复

- **木钩 / 骨钩恢复消耗耐久**：`ToolEvents#wearMainHand` 曾被限制为只处理 `HammerItem`，
  导致 1.21.1 的木钩 / 骨钩打树叶时耐久永远是满的。现在任何可损坏的手持工具都会扣耐久；
  能量锤仍走 `consumeEnergy`。服务端 FakePlayer 探针（木钩 / 骨钩 / 木锤各 `damage=1`，
  创造模式 `0`）已通过。

#### 说明

- **钴 / 阿迪特不做锤**：转匠魂工具路线；**铀不做锤**：放射性。三者的矿链 / 筛表保留。
- **普通材质锤只加耐久**：`tier` 仍只是排序元数据，不改变锤表与挖掘速度；创造栏 / JEI
  catalyst 遍历 `HAMMERS`。DE 锤额外走能量与模块。
- 装 DE 时共 **18 把锤**（6 基础 + 9 金属 + 3 DE），不装 DE **15 把**。
- Bamboo 宝石锤与匠魂碎矿锤只在 1.20.1 线（BambooMod / Tinkers 3 没有官方 1.21.1 构建）。

### English

#### New

- **9 metal hammers**: tin / aluminium / zinc / copper **256**, silver **320**, lead **384**,
  nickel **448**, osmium **512**, platinum **1920** durability. Durability is based on 64;
  diamond (1561) remains the normal-material cap. Recipes keep the diagonal shape
  (2 ingots + 2 sticks) with the head on `c:ingots/<id>` (`forge:ingots/<id>` on 1.20.1);
  copper uses vanilla copper.
- **3 Draconic Evolution hammers** (with DE installed): `wyvern_hammer` / `draconic_hammer` /
  `chaotic_hammer` extend DE's `ModularPickaxe` and follow the fusion-crafting upgrade chain
  (diamond → wyvern → draconic → chaotic, 8M / 32M / 128M OP). They accept DE speed / AOE /
  energy modules, do not consume durability and run on DE energy (256 per use); models are 3D
  cubes (two heads, glowing core, long handle).

#### Fixes

- **Wooden / bone crooks lose durability again**: `ToolEvents#wearMainHand` had been restricted to
  `HammerItem`s, so on 1.21.1 crooks never wore out when breaking leaves. Any damageable held tool
  now takes wear; energy hammers still go through `consumeEnergy`. Verified on the dev server with
  a FakePlayer probe (wood crook / bone crook / wooden hammer each `damage=1`, creative `0`).

#### Notes

- **No cobalt / ardite hammers**: they go through the Tinkers route. **No uranium hammer**:
  radioactive. Their ore chains and sieve entries stay.
- **Metal hammers only add durability**: `tier` is still ordering metadata and does not change the
  hammer table or mining speed; the creative tab / JEI catalyst iterate `HAMMERS`. DE hammers
  additionally use energy and modules.
- With DE installed there are **18 hammers** (6 base + 9 metal + 3 DE), **15** without DE.
- Bamboo gem hammers and the Tinkers crushing hammer are 1.20.1-only (no official 1.21.1 builds of
  BambooMod / Tinkers 3).

## 1.3.2 — 2026-11-05（木桶修复）

### 修复

- **满桶右键堆肥不再放方块**：可堆肥物品现在始终被木桶视为「已处理」；满桶 / 有流体时
  `BarrelBlockEntity#use` 返回 `CONSUME`，客户端预测也按已处理走，树叶不会再被放到桶顶。
- **木桶接管道（物品）**：新增 `BarrelItemHandler`（槽 0 堆肥输入、槽 1 泥土 / 固体产物输出）；
  1.21.1 在 `CapabilityEvents` 注册 `Capabilities.ItemHandler.BLOCK`，1.20.1 在 BE 的
  `getCapability` 返回 `ForgeCapabilities.ITEM_HANDLER`。
- **木桶接管道（流体）**：1.21.1 原有 `FluidHandler.BLOCK` 保留；1.20.1 补上
  `ForgeCapabilities.FLUID_HANDLER`（`getCapability` 返回 tank）。

## 1.3.1 — 2026-11-05（深板岩 / 末影接口起步 + 脑白金提示）

### 新增

- **深板岩石子**（无门控）：泥土筛 **50% × 3-5** 个；4 个 → `minecraft:cobbled_deepslate`
  （2×2），熔炉烧成 `minecraft:deepslate`。草方块表不加。供给 1.20.1 的原始合金炉（6 深板岩）
  与 1.21.1 的深板岩火焰合成。
- **末影接口（Ender IO）起步**：`sieve/dust_enderio.json`（`mod: enderio` + `patch`）——
  尘土 **2%** 出 `enderio:grains_of_infinity`（1.20.1/1.21.1 的无限粉）。不新增基岩粉物品、
  不写「粉 → 基岩」；1.12.2 的基岩粉 / 基岩粉块在 6.x / 8.x 已不存在。

### 改进

- **脑白金提示**：物品介绍新增「对傻子村民使用使其变为普通村民」；中英双语 tooltip。

### 数据

- 筛表基表 118 → **119**（1.20.1 115 → **116**）；装 Ender IO 各 +1（120 / 117）。
- 纹理/模型 295 / 171（分支 297 / 167）；配方 285。

## 1.3.0 — 2026-11-05（龙之进化联动）

> 新增 **Draconic Evolution（龙之进化）** 的 `draconium` 材料：粉碎末地石 1/8 单源 →
> 破碎龙矿 → 4 合 1 矿块 → 熔成龙锭。DE 不在场时整条不注册，基表不变。

### 新增

- **龙尘（`draconium`）**：`gems` 门控材料（`draconicevolution`），tier `rare`、rarity 64。
  工作台 2×2 把 4 个破碎龙矿压成矿砾，矿砂 / 矿尘同理；锤矿砾→粉碎、矿砂→粉末各 4–6。
- **唯一入口**：粉碎末地石 **1/8** 出 `draconium_broken`（`sieve/crushed_end_stone_draconicevolution.json`，
  根级 `mod` + `patch`），末地石本身由熔岩 + 荧石获得。
- **熔炼**：矿砾 / 矿砂 / 矿尘进 `c:ores/draconium`（1.20.1 `forge:ores/draconium`），
  直接复用 DE 的标签熔炼，出 `draconicevolution:draconium_ingot`；不写重复的自有变体。
- **不碰终局**：觉醒龙（4 龙块 + 6 龙核 + 龙心）与混沌碎片（混沌守卫）保持 DE 原流程。

### 修复

- **破碎龙矿基底**：`broken` 阶段的石壳改为**末地石**色（`STONE_PALETTES["draconium"]`），
  不再落回默认灰石；`textures.md` 补了“单源材料必须配来源基底”的硬规则。

### 数据

- 材料 25 → **26** 种（14 金属 + 12 矿物）。
- 筛表：基表 118 / 115 不变；装 DE 各 +1（主线 AE2+ER 131 → 132，1.20.1 全装 173 → 174）。
- 锤表：装 DE 各 +3（主线 AE2+ER 64 → 67，1.20.1 全装 88 → 91）。

## 1.2.0 — 2026-11-04（内容更新）

> 本版新增 **Extreme Reactors**（两版）与 **BambooMod**（仅 1.20.1）联动，补骨粉 / 烈焰粉两条筛出物，
> 并修掉“我们的 yellorium 矿块不被 Mekanism / IE / Create 识别”的问题。
> BambooMod 与匠魂只存在于 1.20.1 线；1.21.1 线只有 ER 与两条筛出物。

### 新增

- **骨粉**：尘土筛 20% 出 `minecraft:bone_meal`（对齐经典 Ex Nihilo 的 1/5），
  瓷土与菌岩催菌不再只靠骷髅掉落。
- **烈焰粉**：沙子筛 3% 出 `minecraft:blaze_powder`；碎下界岩 6% 保留为更高产的第二来源。
- **Extreme Reactors（两版）**：新增 `yellorium`（1/8）、`anglesite` / `benitoite`（各 1/64）
  三条 `gems` 门控材料；三档矿链 → 熔成 `bigreactors:yellorium_ingot` /
  `anglesite_crystal` / `benitoite_crystal`。没装 ER 时整条不注册，基表 118 / 115 不变；
  装 ER 时筛表 +9、锤表 +9。
- **ER 矿石识别**：yellorium 矿块同时进 `forge:ores/yellorite` 与 `forge:ores/uranium`
  （1.21.1 为 `c:` 别名），`yellorium_broken` 进 `forge:raw_materials/yellorium`。
  这样 Mekanism `enriching`、IE crusher、Create crushing 能像处理 ER 原矿一样处理我们的矿块
  （输出对方的铀粉 / 粉碎铀，不是 yellorium）。
- **BambooMod（仅 1.20.1）**：泥土/草方块各 8 条（4 树苗 2%、竹笋 4%、米种 5%、豆 4%、
  番茄 4%），砂砾 3 条宝石（银杏/枫/樱各 1/64）；全部 `mod: bamboomod` 门控。
  它的树叶/树苗/竹笋已在 `#minecraft:leaves` / `#minecraft:saplings`，堆肥直接按 125 分。
- **匠魂（仅 1.20.1）**：yellorium 碎/粉/砂 45 mB、矿块 180 mB、ER 锭 90 / 块 810；
  再加锭/块 2 条铸造。复用 ER 自带的 `forge:yellorium` 流体，不新增流体。

### 修复

- **yellorium 矿块不被其他模组识别**：我们的矿块原先只在 `forge:ores/yellorium`，
  而 ER 的 yellorite 同时是 `forge:ores/yellorite` 与 `forge:ores/uranium`，机器按后者取矿。
  现在两个别名都补上，`yellorium_broken` 也进 raw 标签。

### 数据

- 材料 22 → **25** 种（14 金属 + 11 矿物；ER 三条走 `gems` 门控）。
- 筛表：基表 118 / 1.20.1 115 不变；装 ER 各 +9；1.20.1 装 BambooMod +19
  （泥土 8 / 草 8 / 砂砾 3），全装 173。
- 匠魂：92 条熔炼 + 2 条铸造（原 84 + ER 10）。

### 验证

- 两版 `build`、`check-textures.py`、`check-data-paths.py`、`check-wiki.py` 通过。
- 两版真装 ER + ZeroCore：`[materials] 25`（1.20.1 全装）、`[sieve] 154`、RCON 熔炉三种 ER 产物通过。
- 1.20.1 真装 BambooMod：`[sieve] 173`、`[hammer] 88`、`/setblock` 树苗/竹笋通过。
- 匠魂配方随服务端加载无解析错误；实机熔炉熔化尚未逐项点过。
- 未做：手动右键筛、树苗生长、JEI 页面目视。

### 已知限制

- **BambooMod 只有 1.20.1**；1.21.1 线不加载。
- **anglesite / benitoite 没有匠魂熔炼**：它们没有对应熔融流体；要支持需新增 molten 流体。
- **Thermal 磨粉机没有 yellorium / uranium 配方**；Mekanism / IE / Create 按 `forge:ores/uranium` 工作。
- 1.20.1 分支筛表仍比主线少 3 条钴（历史差异）。

## 1.1.2 — 2026-11-04（Bug 修复版）

> 这是一个**纯 bug 修复版**，没有新增玩法内容，也不改任何数据表。

### 修复

- **1.20.1：木钩 / 骨钩不掉耐久。** `ToolEvents#onBreak` 的 crook 分支只添加了钩子表的
  额外掉落，漏掉了工具磨损；玩家用木钩/骨钩打树叶时，耐久始终是满的。
  现在按锤子同款逻辑处理：非创造模式、破坏的方块确实有钩子表时，对**实际手持的**
  钩子扣 1 点耐久；耐久耗尽时正常触发损坏回调。
- **1.21.1 线同一逻辑本来就正确**（`BlockDropsEvent` → `wearMainHand`），本版只同步
  版本号，没有代码改动。

### 验证

- 1.20.1：服务端 FakePlayer + `gameMode.destroyBlock` 破坏橡树树叶，木钩 / 骨钩各
  `damage=1`（修复前为 `0`）。
- 1.21.1：同一路径实测 `damage=1`，代码未改。
- 两版 `build` 通过，`check-textures.py` 通过。

## 1.1.1 — 2026-11-04

面向 **Minecraft 1.21.1 / NeoForge 21.1.x** 与 **1.20.1 / Forge 47.4.x** 的同步视觉修正。
本版不新增玩法内容；主要修掉“堆肥/熔料看起来像纯色方块”的问题，并把成土时间缩短到 30 秒。

### 修复

- **木桶堆肥外观**：堆肥内容改为**原版泥土方块模型**，顶点色按投入物品的颜色加权混色：
  腐肉偏红褐、树叶偏绿，混投后取两者的比例色；计时中逐渐淡回泥土本色，成土后显示
  原版泥土的本来颜色。此前只有一张固定的棕色贴图，远看就是纯色方块。
- **熔料外观**：坩埚（含 11 种木坩埚）里正在熔的方块改用其**真实方块模型**渲染，
  木坩埚里的树叶显示为树叶方块，不再是纯蓝水盒；木坩埚在固体没熔完时始终显示
  树叶/树苗，不再因为水量超过固体而中途切换成水。

### 调整

- **堆肥成土时间**：50 秒 → **30 秒**（`COMPOST_TICKS` 1000 → 600），JEI 提示同步。

### 数据与兼容

- 木桶 NBT 新增 `CompostItems`（`[{id, points}]`），只用于客户端混色；旧存档没有该键时
  按纯泥土显示，玩法不变。`CompostScale` 仍为 `1000`。
- 删除不再使用的 `textures/block/barrel_compost.png`（堆肥表面改用 `minecraft:block/dirt`）。

### 验证

- 两条线的 dev client 截图核对：腐肉 = 红褐、树叶 = 绿、50/50 = 黄褐、成土 = 原版泥土色；
  木坩埚（固体 60 mB / 水 50 mB）显示绿色树叶方块。
  截图见 [docs/images/1.1.1-compost-crucible.png](docs/images/1.1.1-compost-crucible.png)。
- 两条线 `build` 通过；`check-textures.py` / `check-core-imports.py` / `check-data-paths.py` 通过。

## 1.1.0 — 2026-11-04

面向 **Minecraft 1.21.1 / NeoForge 21.1.x** 与 **1.20.1 / Forge 47.4.x** 的同步更新。
本版新增热力人偶、村民人偶、脑白金、糖水/浓缩蜂蜜汁、蜜蜂、蜂箱配方与 AE2 陨石粉；
桶表新增 `result_fluid` / `delay_ticks` / `drain` 规则。

### 新增内容

- **热力人偶（1.20.1 / Thermal）**：狂风人偶（`blitz_doll`）与暴雪人偶（`blizz_doll`）。
  工作台配方分别是 4 硝石粉 / 雪球 + 红石 + 2 荧石粉 + 人偶 + 下界疣；
  满熔岩桶 + 狂风人偶生成 `thermal:blitz`（狂风人），满水桶 + 暴雪人偶生成 `thermal:blizz`
  （暴雪人）；和平模式掉 `thermal:blitz_rod` / `thermal:blizz_rod`。
  两个物品只在 Thermal Foundation 加载时注册，缺 Thermal 时配方 / 桶规则一起消失。
- **贴图**：狂风人偶金黄、暴雪人偶冰蓝，沿用 64×64 人偶轮廓。
- **村民人偶 / 脑白金（两版）**：村民人偶 = 8 绿宝石围普通人偶，巫水桶右键生成僵尸村民
  （和平掉腐肉）；脑白金 = 蛋 4 + 糖 4 + 绿宝石，右键傻子村民转成普通无职业村民。
- **糖水 / 浓缩蜂蜜汁（两版）**：满水桶 + 糖 → 糖水；满糖水桶 + 蒲公英黄 → 30 秒后浓缩蜂蜜汁；
  玻璃瓶右键浓缩蜂蜜汁桶扣 250 mB 得蜂蜜瓶；浓缩蜂蜜汁桶每 100 tick 1/6 概率在 6 格内养
  1 只蜜蜂（上限 5 只）。两个流体都有 source block 与桶，可舀取/放置。
- **蜂箱配方**：3 木板 + 3 蜂蜜瓶 + 3 木板 → 原版蜂箱。
- **AE2 陨石粉**：沙、砂砾各 1/16 出 `ae2:sky_dust`；AE2 总掉落 +2（主 120 / 分支 143）。

### 数据与兼容

- **桶 18 条**（缺 Thermal 16）：新增糖水 1、浓缩蜂蜜 2、村民人偶 1，另有热力狂风/暴雪 2。
- **筛子**：7 输入 / 116 掉落；AE2 陨石粉加入后 +AE2 120（1.20.1 117）；
  Mek+Thermal 137 / +AE2 141；1.20.1 全装 +TiT 139、+AE2 143。
- **流体**：糖水与浓缩蜂蜜汁都有 source block 和桶装物，可放置/舀取；桶内转化保持不变。
- **未验**：村民人偶 / 脑白金 / 糖水链 / 玻璃瓶抽取 / 新 JEI 页的实际玩家操作与画面；
  已知画面问题（黑色流体）已在本版修复，末次客户端验收仍待补。

## 1.0.0 — 2026-11-04（首个正式版）

面向 **Minecraft 1.21.1 / NeoForge 21.1.x** 的空岛资源获取模组；1.20.1 / Forge 47.4.x
在 `1.20.1-forge` 分支同步。这是继 0.1.0 之后的首个正式版（原 0.2.0 准备线直接定为 1.0.0）。

### 新增内容

- **泥土筛的树苗与竹子**：9 种树苗（橡木 8% / 白桦 4% / 云杉 4% / 丛林 3% / 金合欢 3% /
  樱花 3% / 红树 3% / 杜鹃 2% / 盛开的杜鹃 1%，合计 31%/次）；竹子 5%。
- **下界树**：粉碎下界岩筛出绯红 / 诡异菌岩孢子各 5%，右键下界岩变成对应菌岩；
  之后用原版骨粉催菌、催巨菌，绯红 / 诡异木桶与木坩埚有了来源。
- **下界合金碎片**：粉碎下界岩 0.4%，两条线都有。
- **钴**：新增钴材料；粉碎下界岩 1% 出 `cobalt_broken`，broken 贴图使用下界岩红棕基底。
  1.20.1 装 Tinkers 时熔成 `tconstruct:cobalt_ingot`，否则使用自有钴锭。
- **Immersive Engineering（1.20.1）**：silver / lead / nickel / aluminum / uranium 的锭候选
  （Thermal / Mekanism 之后），15 条熔炼变体；IE 粉碎机通过 `forge:ores/<m>` 标签直接接受矿块。
- **Tinkers' Construct 3（1.20.1）**：条件注册的碧空 / 末影黏液草种子（右键草方块或泥土），
  84 条 `tconstruct:melting`（碎块/粉碎/粉末 45 mB、矿砾/矿砂/矿尘方块 180 mB）。
- **工匠巧思（Tinkers' Thinking，1.20.1）**：新增 `ardite`（碎下界岩 1%，没装 TiT 熔成自有
  阿迪特锭，装了熔成 `tinkers_thinking:ardite_ingot`）和 `chlorophyll`（草方块 1%，走 `gems`：
  只装 TiT 时注册，矿块熔成 `tinkers_thinking:chlorophyte_compound`）两种材料，都走本模组的
  三档碎矿链；1.20.1 另有 6 条匠魂熔炼（`forge:molten_ardite`）。
- **应用能源 2（两版）**：筛沙 1/6 出 `ae2:certus_quartz_crystal`、1/12 出
  `ae2:certus_quartz_dust`；独立 `sieve/sand_ae2.json` 带根级 `"mod": "ae2"` + patch，
  没装 AE2 时整文件静默跳过。充能赛特斯与天石粉不做（充能走 AE2 自己的充电器）。
- **1.20.1 datagen 补齐**：0.1.0 时分支尚缺的配方、blockstate/模型已全部补上；
  木桶合成按原版修正为 4 板 + 台阶。

### 数据与兼容

- **筛子**：7 个输入 / 116 掉落（无科技模组；1.20.1 113）；1.20.1 全装 + 工匠巧思 139、
  主线 Mek+Thermal 137，装 **AE2** 各 +2（141 / 139）；**堆肥** 67 条；
  **锤表** 55 条（全装 79）/ 钩表 10 条。
- **配方**：主线 143 条生成 + 57 条手写 = 200；1.20.1 分支 143 + 135 = 278
  （其中 84 条匠魂熔炼）。
- **材料**：22 种（14 金属 + 8 矿物；其中 8 种 `gems` 只在对应模组在场时注册）；
  铁/金/铜熔到原版锭，其余按已加载模组的候选锭或自有锭。
- **支持模组**：Mekanism（1.21.1 / 1.20.1）、Thermal Foundation + Expansion（1.20.1）、
  Create（两版）、Immersive Engineering（1.20.1）、Tinkers' Construct 3（1.20.1）、
  Tinkers' Thinking（1.20.1）、Applied Energistics 2（两版）。
  所有联动都是**可选**的：没装就回退自有物品，不硬依赖。
- **JEI / Jade**：仍是可选客户端依赖；JEI 提供筛子/桶/堆肥/锤/钩/坩埚/机器页面，
  Jade 提供桶/坩埚/机器/侵染树叶提示。

### 已知限制

- 部分画面与手感仍待作者逐一验收（机器 GUI/动画、自动输出抽屉、筛料层、木桶竖棱、
  手动筛右键、匠魂种子右键、匠魂炉实际熔化等）；玩法逻辑本身大多已用服务端 RCON 验过。
- **阿迪特 / 叶绿素**：Tinkers 3.12.1 已移除阿迪特，1.20.1 由工匠巧思补回；我们筛的是**自己的**
  阿迪特 / 叶绿素碎矿，走本模组三档链。阿迪特有自有锭兜底；叶绿素是 `gems`，只在 TiT 在场时注册。
- IE 与 Tinkers 都只有 1.20.1 版本；1.21.1 主线只保留它们的候选/变体（不会命中）。
- AE2 的筛出物只验过服务端表与 RCON 查询，手筛 / 机器筛实际出货与 JEI 页面未目视。
- 没有 advancement 进度树、没有自动化测试与 CI；与「月光魔法」仍只有软依赖占位。

### 安装

1. 安装 **NeoForge 21.1.x**（1.20.1 版用 **Forge 47.4.x**）。
2. 把 `exnihilosatellitium-1.21.1-1.0.0.jar` 放进 `mods/`。
3. 可选：JEI（配方查询）、Jade（方块信息）、Create / Mekanism / Thermal / IE / Tinkers /
   TiT / AE2（资源与机器互通；IE / Tinkers / TiT 仅 1.20.1）。

## 0.1.0 — 2026-11-02（首个公开版本）

面向 **Minecraft 1.21.1 / NeoForge 21.1.x** 的空岛资源获取模组，从零重写；1.20.1 / Forge 47.4
同步提供一份（分支 `1.20.1-forge`），玩法内容相同。

### 核心循环

- **木桶**：12 种（11 木材 + 石桶）；堆肥 1000 分制出泥土、1000 mB 流体、比较器、破坏返还、
  接雨水；桶内转化 12 条（水+熔岩→圆石、熔岩+水→黑曜石、熔岩+红石/荧石、水+尘土→粘土、
  水+牛奶→史莱姆、人偶系列）。
- **筛子**：6 种筛料（砂砾 / 沙子 / 泥土 / 尘土 / 粉碎下界岩 / 粉碎末地石）、76 条掉落；
  三档筛网（木 10 / 丝 64 / 铁 256 耐久）只影响寿命、不改掉落。
- **锤子 / 钩子**：6 档锤 + 2 档钩；49 条锤表（矿砾→粉碎→粉末、石头降档）、10 张钩表；
  锤子左键唯一路径，自带可收获与挖掘速度。
- **坩埚**：19 条熔点 / 9 种热源；熔料与流体分开记，容量、熔料上限、热源倍率可配；
  Jade 显示熔料剩余与热源速度；净水、熔岩两块 JEI 页。
- **木坩埚**：11 种木材（含下界菌柄、红树/樱花/竹），不需热源，被动把树叶/树苗熔成水。
- **巫水**：自定义流体 + 液方块 + 桶；菌丝发酵或蘑菇煲起酵；沙→灵魂沙、树苗→深色橡树苗。
- **瓷桶**：3 瓷土 → 未烧制 → 烧制，通用 1000 mB 容器（水/熔岩/牛奶/巫水）。
- **蚕 / 侵染树叶**：右键树叶侵染、按进度变白、扩散、破坏掉线，Jade 显示百分比。
- **人偶**：5 种，满桶右键立即转化（和平掉战利品 / 非和平生成生物）。
- **自动化**：电动 / 内能 × 自动筛 / 自动锤四台机器，6 个速度/时运插件、12 格输出、
  自动输出（6 面掩码配置 GUI）、JEI 催化剂、漏斗/管道接口。
- **石子**：筛泥土必出四种石子（圆石/安山岩/花岗岩/闪长岩）各 4-6 个，4 个合成一块石头。

### 数据与兼容

- **数据驱动**：筛表、锤表、钩表、桶转化、堆肥值、坩埚熔点/热源、材料表全部是数据包 JSON，
  可覆盖可扩展；`/reload` 生效。
- **材料表**：12 种金属，自带完整的矿砾→矿砂→矿尘→锭链条；空岛上拿不到的金属由本模组补。
- **支持模组**：Mekanism（1.21.1 与 1.20.1）、Thermal Foundation（1.20.1）、Create（两版）。
  装了谁就熔成谁家的锭（`tin/lead → mekanism → thermal_foundation`、`silver/nickel →
  thermal_foundation`、`zinc → create`），矿石块也进对应的标签与粉碎配方。
- **数据生成**：方块/物品标签、方块掉落、133 条配方、71 个 blockstate + 173 个模型由
  `./gradlew runData` 生成；模型/贴图不覆盖手绘几何。
- **JEI / Jade**：可选客户端依赖，提供筛子/桶/堆肥/锤/钩/坩埚/机器七类页面与 HUD 提示。

### 已知限制

- 部分画面与手感仍待作者逐一验收（机器 GUI/动画、自动输出抽屉、筛料层、木桶竖棱、
  手动筛右键等），玩法逻辑本身已用服务端 RCON 验过。
- 没有 advancement 进度树、没有内置模组图标以外的美术资产、没有自动化测试与 CI。
- 与「月光魔法」只有软依赖占位，实际联动未做。
- 1.20.1 分支的 datagen 在 v0.1.0 之后补齐（配方、blockstate/模型），下一个版本带上。

### 安装

1. 安装 **NeoForge 21.1.x**（1.20.1 版用 **Forge 47.4.x**）。
2. 把 `exnihilosatellitium-0.1.0.jar` 放进 `mods/`。
3. 可选：JEI（配方查询）、Jade（方块信息）、Create / Mekanism / Thermal（资源互通）。
