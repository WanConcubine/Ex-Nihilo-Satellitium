# 更新记录

## 1.4.1 — 2026-11-11（匠魂碎矿锤：配方 / 速度 / 连锁）

> 只动匠魂碎矿锤与两把联动锤：没有新增物品，也不改任何玩法数据表结构。1.21.1 线本版无代码改动。

### 中文

#### 变更

- **装配站配方**：`ex_nihilo_hammer` 从「锤头 + 坚韧手柄 + 2 大板」改成
  **1 锤头 + 1 坚韧手柄**（不再和匠魂大锤同款）；工具定义部件、`default_materials` 与装配站
  输入槽一起减到两件。
- **挖掘速度**：锤砂砾 / 沙子 / 尘土这类 `mineable/shovel` 方块时，之前会回退到空手速度
  （工具在这些方块上不算“有效”）。现在 `HammerTool#breakSpeed` 对锤表方块返回**头部材料
  自己的挖掘速度**（铁头 6.0），不再掉到 1.0；同时去掉头部部件 0.5 的缩放，耐久也回到
  1.4.0 四件配方的水平（铁头约 1000）。
- **连锁挖掘**：
  - **匠魂锤**：新增强化 `exnihilosatellitium:chain`（连锁 I–III），在装配站用 **4 燧石 +
    1 铁块**应用（占 1 个升级槽）；每级让一次粉碎多打 **16 块**相连的同类方块。
    锤身加了**外形变化**：装了连锁后锤头上会出现两节锁链的 overlay。
  - **龙之进化锤**：新增**专属连锁模块** `exnihilosatellitium:chain_mining_module`
    （DE 的 `draconicevolution:modules` 注册表、`MINING_TOOL` 分类），装到我们的 DE 锤上
    后一次粉碎连锁 **32 块**。合成：4 燧石 + 4 铁锭 + 1 个 DE `module_core`。
  - 规则：只连锁**同一种方块**、六面相连，每块照常扣 1 点耐久 / 一次能量；搜索上限 1024 格。

#### 验证

- `./gradlew build --offline`、`check-textures.py`、`check-data-paths.py --target 1.20.1` 通过。
- 服务端 `runServer -PtinkersMods`：`Loaded 46 tool definitions` / `24 station slot layouts`、
  无解析错误；`exnihilosatellitium:chain` 静态强化注册成功、`tconstruct:modifier` 配方加载成功。
- 服务端 FakePlayer 探针（临时命令，已删除）：铁头 + 木柄碎矿锤，挖掘速度 **6.0**、
  最大耐久 **1000**；连锁 0 级 1 块 / 1 点耐久，I 级 17 块 / 17 点，II 级 33 块 / 33 点；
  中间夹一块沙子时在沙子前停下。
- DE 探针（临时命令，已删除）：模块进入 DE 注册表、物品与配方都加载、DE 锤的模块宿主接受
  该模块；装上后 `chainLimit` 从 0 变成 **32**。
- **未做**：装配站里应用强化的画面、连锁 overlay 的实机外观、JEI 强化配方页。

### English

#### Changed

- **Tinker Station recipe**: `ex_nihilo_hammer` went from *hammer head + tough handle + 2 large
  plates* to **1 hammer head + 1 tough handle** (no longer a clone of the sledge hammer); the
  tool definition's part list, `default_materials` and the station layout's input slots all
  shrank to two parts.
- **Mining speed**: crushing gravel / sand / dust used to fall back to bare-hand speed because
  the tool is not "effective" on `mineable/shovel` blocks. `HammerTool#breakSpeed` now returns the
  **head material's own mining speed** (6.0 for iron) for hammer-table blocks, and the head part
  no longer carries the 0.5 scale, so durability is back at the 1.4.0 four-part level (~1000 for
  an iron head).
- **Chain mining**:
  - **Tinkers' hammer**: a new `exnihilosatellitium:chain` modifier (Chain I–III), applied at the
    Tinker Station from **4 flint + 1 iron block** (one upgrade slot); each level adds
    **16 blocks** of chain to one crush. The tool model also changes: with the modifier installed
    the hammer head shows a two-link chain overlay.
  - **Draconic Evolution hammer**: a dedicated chain module,
    `exnihilosatellitium:chain_mining_module` (registered in DE's `draconicevolution:modules`
    registry, `MINING_TOOL` category). With it installed on our DE hammer one crush chains
    **32 blocks**. Crafted from 4 flint + 4 iron ingots + 1 DE `module_core`.
  - Rules: only the **same block** connected by its six faces; every block still costs one
    durability point / one energy charge; the search is capped at 1024 positions.

#### Verification

- `./gradlew build --offline`, `check-textures.py` and `check-data-paths.py --target 1.20.1`
  pass.
- Dedicated server with `-PtinkersMods`: `Loaded 46 tool definitions` /
  `24 station slot layouts`, no parse errors; the `exnihilosatellitium:chain` static modifier
  registered and the `tconstruct:modifier` recipe loaded.
- Server-side FakePlayer probe (temporary command, since removed): iron head + wood handle
  hammer, mining speed **6.0**, max durability **1000**; level 0 takes 1 block / 1 durability,
  level I takes 17 blocks / 17 durability, level II takes 33 blocks / 33 durability, and a sand
  block in the middle stops the chain.
- DE probe (temporary command, since removed): the module is present in DE's registry, the item
  and recipe load, and the DE hammer's module host accepts it; once installed `chainLimit` goes
  from 0 to **32**.
- **Not done**: the in-game station screen for applying the modifier, the chain overlay in game,
  and the JEI page for the modifier recipe.

## 1.4.0 — 2026-11-10（材质锤 / 匠魂碎矿锤 / 修复）

### 中文

#### 新增

- **9 把金属材质锤**：锡 / 铝 / 锌 / 铜 **256**、银 **320**、铅 **384**、镍 **448**、锇 **512**、
  铂 **1920**。以 64 为基数，钻石 1561 仍是普通材质上限；配方沿用斜形（2 锭 + 2 木棍），
  head 走 `forge:ingots/<id>`，铜用原版铜锭。
- **3 把 Bamboo 宝石锤**：樱 / 枫 / 银杏统一 **2048**，仅在装了 BambooMod 0.6.2 时注册，
  配方带 `forge:mod_loaded` 条件。
- **3 把龙之进化锤**（装 DE 时）：`wyvern_hammer` / `draconic_hammer` / `chaotic_hammer`
  继承 DE 的 `ModularPickaxe`，走聚合装置升级链（钻石锤 → 双足飞龙 → 神龙 → 混沌，
  8M / 32M / 128M OP），可用 DE 的速度 / AOE / 能量模块；不消耗耐久，走 DE 能量（256/次）。
  3D cube 模型（左右锤头 + 发光核心 + 长柄）。
- **匠魂碎矿锤（`ex_nihilo_hammer`）**：真正的匠魂 3 可改造工具。装配站用 **锤头 + 坚韧手柄 +
  2 大板** 合成；材料属性 / 材料特性 / 强化槽 / 耐久 / 破损动画全部走匠魂，钴 / 阿迪特 /
  玛玉灵等都能做头。命中锤表方块时掉落与自有锤一致，其余方块正常挖掘；单方块判定，不登记
  `aoe`。自带 16 个强化 overlay（急速 / 发光 / 火焰 / 强化 / 钻石 / 绿宝石 / 下界合金 /
  亡灵杀手 / 自动烧炼 / 幸运 / 丝触 / 经验 / 不毁 / 斩首 / 磁力 / 坏死）。
- **叶绿素碎矿直接熔炼**：破碎 / 粉碎 / 粉末叶绿素矿各 **45 mB** 熔成工匠巧思的
  `tinkers_thinking:molten_chlorophyte`（600°C，`time: 26`），不用先压成矿块再熔 compound。

#### 修复

- **非 `HammerItem` 的锤不再白嫖耐久**：`ToolEvents#onBreak` 取消原版破坏后统一走
  `HammerTool#wear`；匠魂锤覆写为 `ToolDamageUtil.damageAnimated`（强化 / 粘液覆层 /
  破损动画都生效）。自动锤机器对非能量锤也改为扣耐久。
- **机器 GUI 没有压暗**：1.20.1 的 `AbstractContainerScreen#render` 不会自动画背景，
  机器界面只在 `renderBg` 里贴面板图，打开时直接盖在世界上。现在先调
  `renderBackground(graphics)` 再画面板，世界按原版渐变压暗。

#### 说明

- **钴 / 阿迪特不做锤**：转匠魂工具路线；**铀不做锤**：放射性。三者的矿链 / 筛表保留。
- **普通材质锤只加耐久**：`tier` 仍只是排序元数据，不改变锤表与挖掘速度；创造栏 / JEI
  catalyst 遍历 `HAMMERS`。DE 锤额外走能量与模块。
- 装 DE 时共 **21 把锤**（6 基础 + 9 金属 + 3 宝石 + 3 DE），不装 DE **18 把**；
  装了匠魂再 +1（碎矿锤）。匠魂配方合计 **95 条熔炼 + 2 条铸造**。
- 仅 1.20.1：BambooMod 与匠魂 3 都没有官方 1.21.1 构建，主线不加这两组物品。

### English

#### New

- **9 metal hammers**: tin / aluminium / zinc / copper **256**, silver **320**, lead **384**,
  nickel **448**, osmium **512**, platinum **1920** durability. Durability is based on 64;
  diamond (1561) remains the normal-material cap. Recipes keep the diagonal shape
  (2 ingots + 2 sticks) with the head on `forge:ingots/<id>`; copper uses vanilla copper.
- **3 Bamboo gem hammers**: sakura / maple / ginkgo at **2048** durability, registered only
  when BambooMod 0.6.2 is installed, with a `forge:mod_loaded` condition.
- **3 Draconic Evolution hammers** (with DE installed): `wyvern_hammer` / `draconic_hammer` /
  `chaotic_hammer` extend DE's `ModularPickaxe` and follow the fusion-crafting upgrade chain
  (diamond → wyvern → draconic → chaotic, 8M / 32M / 128M OP). They accept DE speed / AOE /
  energy modules, do not consume durability and run on DE energy (256 per use); models are 3D
  cubes (two heads, glowing core, long handle).
- **Tinkers' Construct crushing hammer (`ex_nihilo_hammer`)**: a real Tinkers 3 modifiable tool,
  built at the Tinkers station from **hammer head + tough handle + 2 large plates**. Material
  stats, traits, modifier slots, durability and the broken animation all come from Tinkers;
  cobalt / ardite / manyullyn heads all work. Hitting a hammer-table block drops exactly what our
  own hammers drop; everything else mines normally. It is single-block (no `aoe` tag) and ships
  16 modifier overlays (haste, glowing, fiery, reinforced, diamond, emerald, netherite, smite,
  auto-smelt, fortune, silk touch, experienced, unbreakable, severing, magnetic, necrotic).
- **Chlorophyll ore pieces melt directly**: broken / crushed / powdered chlorophyll each yield
  **45 mB** of Tinkers' Thinking's `tinkers_thinking:molten_chlorophyte` (600 °C, `time: 26`),
  so you no longer have to press them into ore blocks and smelt them into compound first.

#### Fixes

- **Hammers that are not `HammerItem`s no longer skip durability**: `ToolEvents#onBreak` now routes
  every blocked vanilla break through `HammerTool#wear`; the Tinkers hammer overrides it with
  `ToolDamageUtil.damageAnimated`, so modifiers, overslime and the broken animation all apply.
  The automatic hammer machine also damages non-energy hammers now.
- **Machine GUIs had no screen dim**: 1.20.1's `AbstractContainerScreen#render` does not draw the
  background itself, and the machine screen only blitted its panel, so it sat directly on the
  world. It now calls `renderBackground(graphics)` before the panel, giving the vanilla gradient.

#### Notes

- **No cobalt / ardite hammers**: they go through the Tinkers route. **No uranium hammer**:
  radioactive. Their ore chains and sieve entries stay.
- **Metal hammers only add durability**: `tier` is still ordering metadata and does not change the
  hammer table or mining speed; the creative tab / JEI catalyst iterate `HAMMERS`. DE hammers
  additionally use energy and modules.
- With DE installed there are **21 hammers** (6 base + 9 metal + 3 gem + 3 DE), **18** without DE;
  with Tinkers installed, one more (the crushing hammer). Tinkers recipes total
  **95 melting + 2 casting**.
- 1.20.1 only: neither BambooMod nor Tinkers 3 has an official 1.21.1 build, so the mainline does
  not ship these two groups.

## 1.3.2 — 2026-11-05（木桶修复）

### 修复

- **满桶右键堆肥不再放方块**：可堆肥物品现在始终被木桶视为「已处理」；满桶 / 有流体时
  `BarrelBlockEntity#use` 返回 `CONSUME`，客户端预测也按已处理走，树叶不会再被放到桶顶。
- **木桶接管道（物品）**：新增 `BarrelItemHandler`（槽 0 堆肥输入、槽 1 泥土 / 固体产物输出）；
  本线在 BE 的 `getCapability` 返回 `ForgeCapabilities.ITEM_HANDLER`。
- **木桶接管道（流体）**：本线在 BE 的 `getCapability` 返回
  `ForgeCapabilities.FLUID_HANDLER`（tank）。

## 1.3.1 — 2026-11-05（深板岩 / 末影接口起步 + 脑白金提示）

### 新增

- **深板岩石子**（无门控）：泥土筛 **50% × 3-5**；4 个 → `minecraft:cobbled_deepslate`，
  熔炉烧成 `minecraft:deepslate`。草方块表不加。1.20.1 的 Primitive Alloy Smelter 要 6 深板岩。
- **末影接口起步**：`sieve/dust_enderio.json`（`mod: enderio` + `patch`）——尘土 **2%** 出
  `enderio:grains_of_infinity`（1.20.1/1.21.1 的无限粉）；不新增基岩粉物品/基岩合成。

### 改进

- **脑白金提示**：物品介绍新增「对傻子村民使用使其变为普通村民」；中英双语 tooltip。

### 数据

- 筛表基表 115 → **116**；装 Ender IO +1（117）；纹理/模型 297 / 167。

## 1.3.0 — 2026-11-05（龙之进化联动）

> 新增 **Draconic Evolution（龙之进化）** 的 `draconium` 材料：粉碎末地石 1/8 单源 →
> 破碎龙矿 → 4 合 1 矿块 → 熔成龙锭。DE 不在场时整条不注册，基表不变。

### 新增

- **龙尘（`draconium`）**：`gems` 门控材料（`draconicevolution`），唯一入口是
  粉碎末地石 **1/8**（`sieve/crushed_end_stone_draconicevolution.json`）；4 破碎 → 矿砾，
  三条锤表（矿砾→粉碎 / 矿砂→粉末 4–6、矿尘→自身）都带 `mod` 门。
- **熔炼**：矿块进 `forge:ores/draconium`，直接复用 DE 的标签熔炼，出
  `draconicevolution:draconium_ingot`；不写重复的自有变体。
- **不碰终局**：觉醒龙（4 龙块 + 6 龙核 + 龙心）与混沌碎片（混沌守卫）保持 DE 原流程。

### 修复

- **破碎龙矿基底**：`broken` 阶段改用末地石色（`STONE_PALETTES["draconium"]`），不再退回默认灰石。

### 数据

- 材料 25 → **26** 种（14 金属 + 12 矿物）。
- 筛表：基表 115 不变；装 DE +1（全装 173 → 174）。
- 锤表：装 DE +3（全装 88 → 91）。

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

1.20.1 / Forge 47.4.x 与主线同步的视觉修正 + 堆肥时间调整。

### 修复

- **木桶堆肥外观**：改为**原版泥土方块模型**，顶点色按投入物品的颜色加权混色
  （腐肉偏红褐、树叶偏绿，混投取比例色）；计时中淡回泥土本色，成土后显示原版泥土色。
- **熔料外观**：坩埚（含 11 种木坩埚）用被熔方块的真实模型渲染；木坩埚里的树叶显示为
  树叶方块，且固体没熔完时不再中途切成水盒。

### 调整

- **堆肥成土时间** 50 秒 → **30 秒**（`COMPOST_TICKS` 1000 → 600），JEI 提示同步。

### 数据与兼容

- 木桶 NBT 新增 `CompostItems`（`[{id, points}]`），只用于客户端混色；旧存档按纯泥土显示。
- 删除不再使用的 `textures/block/barrel_compost.png`。

### 验证

- 1.20.1 dev client 截图确认腐肉红褐 / 树叶绿 / 50-50 黄褐 / 成土泥土色，木坩埚显示树叶；
  `build` 与 `check-textures.py` 通过。

## 1.1.0 — 2026-11-04

面向 **Minecraft 1.20.1 / Forge 47.4.x** 的同步更新（与 1.21.1 主线玩法一致）。
本版新增热力人偶、村民人偶、脑白金、糖水/浓缩蜂蜜汁、蜜蜂、蜂箱配方与 AE2 陨石粉；
桶表新增 `result_fluid` / `delay_ticks` / `drain` 规则。

### 新增内容

- **热力人偶（Thermal）**：狂风人偶（`blitz_doll`）与暴雪人偶（`blizz_doll`）。
  工作台配方分别是 4 硝石粉 / 雪球 + 红石 + 2 荧石粉 + 人偶 + 下界疣；
  满熔岩桶 + 狂风人偶生成 `thermal:blitz`（狂风人），满水桶 + 暴雪人偶生成 `thermal:blizz`
  （暴雪人）；和平模式掉 `thermal:blitz_rod` / `thermal:blizz_rod`。
  两个物品只在 Thermal Foundation 加载时注册，缺 Thermal 时配方 / 桶规则一起消失。
- **贴图**：狂风人偶金黄、暴雪人偶冰蓝，沿用 64×64 人偶轮廓。
- **村民人偶 / 脑白金**：村民人偶 = 8 绿宝石围普通人偶，巫水桶右键生成僵尸村民（和平掉腐肉）；
  脑白金 = 蛋 4 + 糖 4 + 绿宝石，右键傻子村民转成普通无职业村民。
- **糖水 / 浓缩蜂蜜汁**：满水桶 + 糖 → 糖水；满糖水桶 + 蒲公英黄 → 30 秒后浓缩蜂蜜汁；
  玻璃瓶右键浓缩蜂蜜汁桶扣 250 mB 得蜂蜜瓶；浓缩蜂蜜汁桶每 100 tick 1/6 概率在 6 格内养
  1 只蜜蜂（上限 5 只）。两个流体都有 source block 与桶，可舀取/放置。
- **蜂箱配方**：3 木板 + 3 蜂蜜瓶 + 3 木板 → 原版蜂箱。
- **AE2 陨石粉**：沙、砂砾各 1/16 出 `ae2:sky_dust`；AE2 总掉落 141 → 143。

### 数据与兼容

- **桶 18 条**（缺 Thermal 16）：新增糖水 1、浓缩蜂蜜 2、村民人偶 1，另有热力狂风/暴雪 2。
- **筛子**：7 输入 / 113 掉落；+AE2 117；全装 +TiT 139、+AE2 143。
- **流体**：糖水与浓缩蜂蜜汁都有 source block 和桶装物，可放置/舀取；桶内转化保持不变。
- **未验**：村民人偶 / 脑白金 / 糖水链 / 玻璃瓶抽取 / 新 JEI 页的实际玩家操作与画面；
  已知画面问题（黑色流体）已在本版修复，末次客户端验收仍待补。

## 1.0.0 — 2026-11-04（首个正式版）

面向 **Minecraft 1.20.1 / Forge 47.4.x** 的空岛资源获取模组；与 1.21.1 / NeoForge 主线
玩法内容相同。这是继 0.1.0 之后的首个正式版（原 0.2.0 准备线直接定为 1.0.0）。

### 新增内容

- **泥土筛的树苗与竹子**：9 种树苗（橡木 8% / 白桦 4% / 云杉 4% / 丛林 3% / 金合欢 3% /
  樱花 3% / 红树 3% / 杜鹃 2% / 盛开的杜鹃 1%，合计 31%/次）；竹子 5%。
- **下界树**：粉碎下界岩筛出绯红 / 诡异菌岩孢子各 5%，右键下界岩变成对应菌岩；
  之后用原版骨粉催菌、催巨菌，绯红 / 诡异木桶与木坩埚有了来源。
- **下界合金碎片**：粉碎下界岩 0.4%，两条线都有。
- **钴**：第 13 种材料；粉碎下界岩 1% 出 `cobalt_broken`，broken 贴图使用下界岩红棕基底。
  1.20.1 装 Tinkers 时熔成 `tconstruct:cobalt_ingot`，否则使用自有钴锭。
- **Immersive Engineering（1.20.1）**：silver / lead / nickel / aluminum / uranium 的锭候选
  （Thermal / Mekanism 之后），15 条熔炼变体；IE 粉碎机通过 `forge:ores/<m>` 标签直接接受矿块。
- **Tinkers' Construct 3（1.20.1）**：条件注册的碧空 / 末影黏液草种子（右键草方块或泥土），
  84 条 `tconstruct:melting`（碎块/粉碎/粉末 45 mB、矿砾/矿砂/矿尘方块 180 mB）。
- **工匠巧思（Tinkers' Thinking，1.20.1）**：新增 `ardite`（粉碎下界岩 1%，没装 TiT 熔自有
  阿迪特锭、装了熔 `tinkers_thinking:ardite_ingot`）和 `chlorophyll`（草方块 1%，`gems`：仅 TiT
  在场时注册，矿块熔成 `tinkers_thinking:chlorophyte_compound`）两种材料，走本模组三档碎矿链；
  另有 6 条匠魂熔炼（`forge:molten_ardite`）。
- **应用能源 2（两版）**：筛沙 1/6 出 `ae2:certus_quartz_crystal`、1/12 出
  `ae2:certus_quartz_dust`；独立 `sieve/sand_ae2.json` 带根级 `"mod": "ae2"` + patch，
  没装 AE2 时整文件静默跳过。充能赛特斯与天石粉不做（充能走 AE2 自己的充电器）。
- **1.20.1 datagen 补齐**：0.1.0 时分支尚缺的配方、blockstate/模型已全部补上；
  木桶合成按原版修正为 4 板 + 台阶。

### 数据与兼容

- **筛子**：7 个输入 / 139 掉落（全装 + 工匠巧思；无 techMods 113；装 **AE2** 时 +2，全装 141、
  无科技模组 115）；**堆肥** 67 条；
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
- **阿迪特 / 叶绿素走工匠巧思的材料链**：Tinkers 3.12.1 已移除阿迪特，1.20.1 由 TiT 补回；
  我们注册自有阿迪特锭兜底，叶绿素只在 TiT 在场时注册。
- IE 与 Tinkers 都只有 1.20.1 版本；1.21.1 主线只保留它们的候选/变体（不会命中）。
- AE2 的筛出物只验过服务端表与 RCON 查询，手筛 / 机器筛实际出货与 JEI 页面未目视。
- 没有 advancement 进度树、没有自动化测试与 CI；与「月光魔法」仍只有软依赖占位。

### 安装

1. 安装 **Forge 47.4.x**（Java 17）。
2. 把 `exnihilosatellitium-1.20.1-1.0.0.jar` 放进 `mods/`。
3. 可选：JEI（配方查询）、Jade（方块信息）、Create / Mekanism / Thermal / IE / Tinkers /
   TiT / AE2（资源与机器互通；IE / Tinkers / TiT 仅 1.20.1）。

## 0.1.0 — 2026-11-02（首个公开版本）

面向 **Minecraft 1.20.1 / Forge 47.4.x** 的空岛资源获取模组，从零重写；与 1.21.1 / NeoForge
主线（`main`）玩法内容相同。

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
- 1.20.1 分支的标签、掉落、配方、blockstate/模型也全部由 datagen 生成（v0.1.0 之后）。

### 安装

1. 安装 **NeoForge 21.1.x**（1.20.1 版用 **Forge 47.4.x**）。
2. 把 `exnihilosatellitium-0.1.0.jar` 放进 `mods/`。
3. 可选：JEI（配方查询）、Jade（方块信息）、Create / Mekanism / Thermal（资源互通）。
