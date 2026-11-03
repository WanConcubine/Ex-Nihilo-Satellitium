# 当前状态（交接卡）

> 最后更新：2026-11-03
> 先看这 20 行，细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**M1 木桶 + 七种材质变体已完成**；**M1.5 桶内方块转化已铺好**（2026-11-01：水+熔岩→圆石、
熔岩+水→黑曜石、熔岩+红石→下界岩、熔岩+荧石→末地石，木桶限温 + 接雨水）；
**M2 筛子 + 矿链已铺好**（三种筛网、6 张筛料表、20 种材料 4 碎块→方块→产物；无科技模组时 13 种）；
**M3 锤/钩 + 尘土方块已铺好**（6 锤 / 2 钩、73 条锤表、10 张钩表、沙→尘土）；
**M4 坩埚已铺好**（2026-11-01：瓷土 → 未烧制 → 烧制、39 条熔点 / 9 热源、
熔料+流体双储、JEI 两页 + Jade）。**M6 五刀已铺完**（2026-11-01：巫水 + 古代孢子、瓷桶、
木坩埚 11 种、木桶补齐 12 种、粉碎下界岩/末地石 + 草种子、蚕/侵染树叶、5 种人偶）；
**M10 自动化也已铺完**（四台机器 + 6 个插件 + 自动输出 + GUI/JEI/BER）；
`crucible.*` 与 `machine.*` config 全部就位。**桶配方已按原版改正**（4 板 + 台阶，见下）。
**科技模组锭接入**（2026-11-02）：tin/lead → Mekanism → Thermal，silver/nickel → Thermal，
zinc → Create，再补 Immersive Engineering（silver/lead/nickel/aluminum/uranium）与
Tinkers' Construct 钴（`tconstruct:cobalt_ingot`）；运行时取第一个已加载的模组，自有锭只作兜底。
**科技模组矿物（2026-11-03）**：Mekanism 氟石 + Thermal 磷灰石/朱砂/硝石/硫磺/红宝石/蓝宝石走
`gems` 三档矿链，**只在对应模组加载时注册**；无模组时物品/方块/碎料/筛出/配方都不出现。

## 当前批次（2026-11-03，科技模组矿物链：Mek 氟石 + Thermal 六种）

- **范围**：Mekanism 氟石 + Thermal 磷灰石 / 朱砂 / 硝石 / 硫磺 / 红宝石 / 蓝宝石。Create
  只有锌，已有完整链，本轮只做同服回归。与主线共用同一份 `materials.json` 与候选顺序。
- **形态（按所有者修正）**：新增 7 种 `gems` 材料，走现有三档矿链（筛 → 碎料 → 4 合 1 矿块 →
  熔炼）；**不注册自有磷灰石/氟石等兜底物品，也不生成 `forge:gems` 标签**。**整条材料只在
  候选 mod 已加载时注册**：没装时物品、方块、三档碎料、筛出物、锤表、配方和标签都不出现；
  装模组时矿砾熔成对方物品。矿块仍进 `forge:ores/<id>`（标签 `required: false`），Thermal
  磨粉机 / Mek 富集机可按标签处理；矿块没有 loot table，由 `SelfDropBlock` 代码掉自身。
- **数据**：21 条手写 `*_smelting_<mod>` 变体（`conditions` 裸键 + `forge:mod_loaded`）；
  `gems` **没有 base 配方**（不再有“无模组时熔成自有矿物”）。7 种矿物不写匠魂熔炼。
- **匠魂熔炼产量修正**：78 条 `tconstruct:melting` 的碎块类（broken/crushed/powdered）改为
  **45 mB = 1/2 锭**，矿块类（ore_gravel/ore_sand/ore_dust）改为 **180 mB = 2 锭**，时间按
  0.75 缩放；7 种 `gems` 矿物没有对应熔融液体，仍不写匠魂熔炼。
- **末影蛋糕图标修正**：物品模型改为 `item/generated` + 16×16 透明
  `exnihilosatellitium:item/end_cake`；`ModItemModels` 跳过 `end_cake`，不再用方块模型当
  物品图标，也不再生成重复的 `models/item/end_cake.json`。
- **开发服**：`build.gradle` 的 `-PtechMods` 增加 Mekanism 1.20.1 条目（libs/ 有 jar 才加载），
  方便一次启动 Mek + Thermal + Create + IE + Tinkers。
- **验证**（2026-11-03，Forge 47.4.28 / Java 17）：
  - 同服装 Mekanism 10.4.16 + Thermal Foundation 11.0.6 + CoFH Core + Create 6.0.8 + IE +
    Tinkers：日志 7 条 `[materials] ... smelts into ...` 全部命中（fluorite → `mekanism:fluorite_gem`，
    其余 6 种 → `thermal:*`）；`[materials] loaded 20 entries`。
  - RCON 熔炉逐个烧 `*_ore_gravel`：7/7 产出对应模组物品；`data get block` 与
    `execute if block` 都通过。
  - `/exnihilo dump sieve` 113 行、`dump hammer` 73 行；新条目概率与 main 一致。
  - 无科技模组：13 entries、筛 90 / 锤 52，`ruby_broken` unknown，无缺失物品/掉落表报错。
  - `./gradlew build`、`check-textures.py` 通过；generated 树**不再有** 7 种矿物的 fallback
    物品模型/标签/base 熔炼配方。
  - 计数：生成配方 164、手写 157（66 条候选变体 + 12 条 `create:crushing` + 78 条匠魂熔炼 +
    1 条末影蛋糕），合计 321；标签 58、掉落表 72（7 种 `gems` 矿块无 loot table，代码掉自身）。
  - **未验**：Thermal 磨粉机 / Mek 富集机实际处理新矿块（只核对 `forge:ores/<m>` 标签）；
    JEI/客户端（本轮会开 1.20.1 客户端）。

## 当前批次（2026-11-03，末影蛋糕移植）

- **新方块/物品 `exnihilosatellitium:end_cake`**：可放置蛋糕，吃一片**主世界 ↔ 末地**双向传送
  （主世界 → 末地中央岛；末地 → 玩家床/世界出生点）；下界不传送。放置时 1 片（`bites=6`），
  末影之眼右键补 1 片，最多 7 片；每次吃 +2 饥饿 / 0.1 饱和度，始终可吃（不要求饥饿值）。
- **配方/资源**：8 末影珍珠 + 蛋糕；4 张 16×16 贴图（Creatio 原图经 restyle），模型只写 `parent: minecraft:block/cake*`
  + 覆盖贴图；无掉落表（和 vanilla 蛋糕一致）。资源在主线和 1.20.1 是同一套。
- **1.20.1 实现差异**：`EndCakeBlock` 用 1.20.1 的 `use(...)`（没有 `useItemOn` /
  `useWithoutItem`），`stack.shrink(1)` 代替 `ItemStack.consume`，`InteractionResult.sidedSuccess`。
- **验证**（2026-11-03）：
  - `./gradlew build` / `compileJava`、`check-textures.py` 通过；
  - 服务端 `/setblock ... end_cake[bites=6]` 并通过 `execute if block` 检查；
  - **未验**：进游戏右键吃/传送、末影之眼补片、模型画面；`/setblock` 默认是 `bites=0`，
    物品放置才是 `bites=6`。

## 当前批次（2026-11-03，P3：CraftTweaker）

- **入口**：`@ZenRegister` 的七个类
  `mods.exnihilosatellitium.Sieve` / `.Hammer` / `.Crook` / `.Barrel` / `.Compost` /
  `.CrucibleMelt` / `.CrucibleHeat`；常用 `add` / `remove` / `replace` +
  `addJson` / `removeJson` / `replaceJson`。
- **依赖**：`compileOnly` CraftTweaker 14.0.60（不随 jar 分发）。BlameJared maven 的
  Gradle TLS 问题用 `downloadCraftTweaker` 下载到 `libs/crafttweaker/`；Forge 开发服
  用 `./gradlew runServer -PctMods` 把 `fg.deobf` 后的 CT 放进运行时。
- **语义**：source `crafttweaker`、priority 200（在数据包和 KJS 之后）；`#tag` / `mod` /
  校验共用；`onAddReloadListenerEvent` 在 CT 脚本前清旧贡献。
- **验证**（2026-11-03，CT 14.0.60 / Forge 47.4.28 / Java 17）：
  - 七个入口全部生效，七张表条数正确；
  - 改脚本 `/reload`：`cleared 7 stale crafttweaker op(s)`，旧条目消失、compost 300→150；
  - CT 与 datapack fixture 的七张表 dump 逐字节一致；
  - 不带 `-PctMods`（无 CT）正常启动；旧世界提示 `Missing data pack mod:crafttweaker` 属正常。
- **未做**：CT recipe type / JEI 分类（后置）；P4 客户端 JEI 同步。

## 当前批次（2026-11-03，P2：KubeJS 插件）

- **KubeJS 2001 插件**：`compileOnly` KubeJS `2001.6.5-build.26` + Rhino
  `2001.2.3-build.10`（不随 jar 分发）；`kubejs.plugins.txt` 注册
  `ExNihiloKubeJSPlugin extends KubeJSPlugin`。
- **与 1.21.1 的差异**：2001 的插件是类、绑定是 `BindingsEvent`（`getType()` + `add`），
  没有 `beforeScriptsLoaded`；在 `registerBindings`（server 脚本批次前）清旧 `kubejs` 贡献。
  七个绑定对象（`SieveEvents` 等）和脚本写法与主线完全相同。
- **开发服运行**：KJS/Rhino/Architectury 的发布 jar 是 SRG 名，直接放 `run/mods` 会
  mixin 报 `m_7038_` 找不到；用 `./gradlew runServer -PkjsMods` 让 ForgeGradle
  deobf + 进运行时。普通 `runServer` 不带 KJS。
- **验证**（2026-11-03，KubeJS 2001.6.5 / Rhino 2001.2.3 / Architectury 9.2.14）：
  - 七个绑定各一条：`[sieve] 90→91 drops`、hammer 52→53、crook 22→23、barrel 12→13、
    compost 67→68、melt 39→40、heat 9→10；`/exnihilo dump` 出现预期条目。
  - 改脚本（删 sieve、compost 300→150）后 `/reload`：`dropped 7 old op(s)`，
    sieve emerald 消失、compost=150，overlay 6 条。
  - 同一 fixture 用 datapack（`pack_format 15`）跑，七张表 dump 与 KJS 结果逐字节一致。
  - 不带 `-PkjsMods`（无 KJS）正常启动，无 `NoClassDefFoundError`。
- **未做**：P4 客户端 JEI 同步；P3 CraftTweaker。

## 当前批次（2026-11-03，P1.5：overlay 边角）

- **与主线同步**：overlay 显式 `priority`（升序应用，高的后应用所以赢）、
  `replaced` / `removed` / `add ignored` 冲突日志（带旧 source）、同 source 重复操作去重、
  操作上限 4096、空 payload 拒绝、server stop 清空、`/exnihilo dump all`。
- **验证**（2026-11-03，Forge 47.4.28 / Java 17）：
  - `build`、`check-textures.py` 通过；
  - `dump all` 298 行 / 7 个表头；
  - p10 先提交、p5 后提交，最终 p10 赢；`add ignored`、`replaced` 日志可见；
  - 同一 sieve add 提交两次，overlay 仍 1 条；空 payload 被拒；
  - 停止服务器时 `cleared 4 overlay op(s) on server stop`。
- **未做**：KJS / CT 入口（P2/P3）；客户端同步（P4）。

## 当前批次（2026-11-03，三路径 P1 移植）

- **与主线同步 P1 基座**：`common/table/`（`TableId` / `TableBuilder` /
  `CustomizationOverlay` / `Tables` / `TableDump`）、七张表统一走 builder、
  `/exnihilo dump <table>`、`/exnihilo customize <table> add|remove|replace <json>`、
  `list` / `clear [source]`。
- **1.20.1 差异**：命令与表 API 两版相同；`BuiltInRegistries` 的弃用警告照旧；
  本分支没有 `check-core-imports.py` / `check-data-paths.py`，P1 只跑 build + 纹理守卫。
- **验证**（2026-11-03，Forge 47.4.28 / Java 17）：
  - `./gradlew build`、`check-textures.py` 通过；
  - 七张表 add 各 +1 行；replace 后目标行正确（sand 只剩 diamond 1.0、cobblestone → flint、
    oak_leaves 只剩 stick、water+sugar → glass、emerald=50、end_stone → water 100、
    furnace[lit=true]=0.9）；
  - 连续两次 `/reload` 后七张表 dump 与 reload 前**逐字节一致**；
  - `clear` 掉 7 条后七张表 dump 与启动基线**逐字节一致**。
- **未做**：KJS / CT 入口（等主线 P2/P3）；overlay 不持久化；专用服客户端 JEI 同步（P4）。

## 当前批次（2026-11-03，数据包 v2 移植）

- **1.20.1 已对齐主线的数据包 v2**：`#tag` 输入、根级 `"mod"` 门、`"patch": true` /
  `"remove"`、具体 > 标签的加载顺序、`/exnihilo` 诊断命令全部搬过来了。
- **FORGE 侧时序**：`TagManager` 同样在所有 listener 之后才 bind，所以 `load()` 只建具体条目，
  Forge 的 `TagsUpdatedEvent`（`net.minecraftforge.event.TagsUpdatedEvent`）里再 `rebuild()`。
  日志先 `waiting for tags`、后完整条数，属正常。
- **1.20.1 特有**：
  - 通用标签是 `forge:`（`#forge:gravel`），vanilla 标签仍是 `#minecraft:logs` 等。
  - `base` / `added.fluid` 的流体标签只展开 still（source）流体；`#minecraft:water` 的
    still/flowing 两个变体里 flowing 规则会被丢掉（桶里只可能存 still）。
  - 代码从 `ForgeRegistries` 改成 `BuiltInRegistries` 做 id/标签解析，Forge 的
    `BuiltInRegistries` 弃用警告只是警告，功能正常。
- **示例包**：`examples/datapack/`（`pack_format 15`，`#forge:gravel`）。
- **验证**（2026-11-03，Forge 47.4.28 / Java 17）：
  - `./gradlew build`、`check-textures.py` 通过；本分支没有 `check-core-imports.py` /
    `check-data-paths.py`（P1/P2 未回移），这两项没跑。
  - dev server + 测试包：启动 `[sieve] 6 inputs, 90 drops` → 展开后 **45 inputs / 115 drops**；
    `[hammer] 52 → 91`；`[crook] 22 → 32 drops`；`[crucible] 9 → 11 heat`
    （`#minecraft:campfires` 展开 2 个）；`[barrel] 12 → 11`（水+牛奶规则被补丁删除）。
  - RCON：`sieve minecraft:sand` 已无 cactus、有 0.5 钻石；`sieve minecraft:oak_log` 有 1% 钻石；
    `hammer minecraft:oak_log` → 木棍 1–2；`hammer minecraft:end_stone` → no rule；
    `compost minecraft:bread` → 100；tag-targeted `remove: true` 把原木锤规则 91 → 51，
    `"mod": "minecraft"` 正例让桶 +1；删掉测试文件 `/reload` 后恢复 91 / 11。
- **未做**：专用服客户端 JEI 同步；本分支没有 core/data 守卫脚本；KubeJS / CraftTweaker
  仍无集成；`materials.json` 仍只认 jar 内那份。
- **三路径等价**：主线的 `docs/customization-roadmap.md` 是总计划；**P1（TableBuilder +
  overlay）已同步到本分支**，之后 KJS/CT 入口仍以主线 P2/P3 为准，不在分支单独维护路线图。

## 0.2.0 发布准备（2026-11-02）

- `mod_version=0.2.0`；`CHANGELOG.md` / `README.md` 已更新；主线的
  [release-0.2.0.md](../../Ex-Nihilo-Satellitium/docs/release-0.2.0.md) 含商店文案与上传清单。
- 本分支 jar：`build/libs/exnihilosatellitium-0.2.0.jar`（663 KB，
  sha256 `e579e3a1…cc1ff`）；`mods.toml` 版本号、LICENSE / THIRD-PARTY / icon 已核对。
- **待所有者确认**：tag `v0.2.0-1.20.1`、CurseForge / Modrinth 上传。

## 当前批次（2026-11-02 晚，Tinkers' Construct 3 接入）

- **碧空 / 末影黏液草种子**：只在 `tconstruct` 在场时注册；右键草方块或泥土 →
  `tconstruct:sky_vanilla_slime_grass` / `ender_vanilla_slime_grass`；泥土筛 2% / 1%，
  条目带 `"mod": "tconstruct"`，无匠魂不加载、不刷 unknown item。模型/贴图手写。
- **钴**（第 13 种材料）：候选 `tconstruct:cobalt_ingot`，无匠魂回退自有钴锭；
  碎下界岩 1% 出 `cobalt_broken`；broken 贴图用下界岩红棕基底。
- **下界合金碎片**：碎下界岩 0.4%。
- **匠魂熔炼**：13 材料 × 6 形态 = 78 条 `tconstruct:melting`；物品 45 mB（1/2 锭）、方块 180 mB（2 锭）；
  流体 `forge:molten_<m>`，温度取匠魂锭配方；条件 `mod_loaded(tconstruct)`。
- **阿迪特暂不做**：Tinkers 3.12.1 已移除，等「工匠巧思」拓展。
- **验证**：
  - 只装 Tinkers 3.12.1 + Mantle 1.11.117：`[materials] cobalt → tconstruct:cobalt_ingot`；
    `[sieve] 92 drops`；RCON 熔炉 `cobalt_ore_gravel → tconstruct:cobalt_ingot`；
    两个种子 `summon` 正常；目标黏液草方块 `setblock` 成功。
  - 不装 Tinkers：`[sieve] 90 drops`，无 unknown recipe/item 报错。
  - **右键草方块/泥土、匠魂炉实际熔化、JEI 页未进游戏**。
- **配方计数**：143 generated + 135 手写 = **278**；手写含 45 候选变体 + 12 crushing +
  78 匠魂熔炼。

## 上一批次（2026-11-02，Immersive Engineering 接入）

- silver/nickel → Thermal → IE；lead → Mek/Thermal → IE；aluminum → IE；uranium → Mek → IE；
  15 条变体；IE 粉碎机走 `forge:ores` 标签。只装 IE 与 Thermal+Create+IE 两种组合都已验。

## 更早批次（2026-11-02，树的起点：树苗 / 竹子 / 下界菌岩孢子）

- 泥土筛 9 种树苗 + 竹子；碎下界岩筛菌岩孢子各 5% + 骨粉链；当时 88 掉落 / 67 compost。

## 更早批次（2026-11-02，datagen 第二、三批移植）

- **配方（第二批）**：`datagen/ModRecipes` 生成 **143 条**（133 条 base + 4 条石子→石头 + 6 条钴），
  手写只剩 54 条（42 条 `*_smelting_<mod>` 候选变体 + 12 条 `create:crushing`），
  原来的 137 个手写文件已删除。
- **blockstate / 模型（第三批）**：`ModBlockStates` 生成 71 个 blockstate + 29 个机械模型，
  `ModItemModels` 生成 150 个物品模型；13 个手绘方块模型 + 2 个物品模型保留。
- **1.20.1 的两个 API 坑**（都写进 `datagen.md`）：
  - `RecipeBuilder.save(out, String)` 的 id **必须带命名空间**，否则落到 `data/minecraft/recipes/`；
    而且 id 等于「产物默认 id」时会抛异常，所以统一走 `save(out, name, builder)` 助手
    （相等时用 `save(out)`，否则传完整 id）。
  - 配方条件用**裸 `conditions`**；新增 `datagen/DatagenRecipe` 负责挂条件 + 丢掉 advancement。
- **datagen 期间不能装科技模组**：Thermal/Create 现在挂在 `-PtechMods` 后面，
  `runData` 不带这个参数（否则 `OreMaterials` 解析到对方锭、且对方自己的 datagen provider
  会往我们的输出树里写文件）。
- **验证**：`compare-recipes.py` → `176 old, 137 generated, 39 old-only (0 unexpected), 0 different`；
  `compare-models.py` → `263 old, 248 generated, 248 identical`（15 个 old-only 全是手绘）；
  删旧文件后 `runData` `written: 0`；`build` + `check-textures`（已换成扫两个资源根的版本）通过；
  服务端 0 条解析错误、`[sieve] 6 inputs, 76 drops`；客户端启动 0 条 missing model/texture。
- **未验**：进游戏看模型/配方书（作者验收）；`-PtechMods` 下的联调还要再跑一次。

## 更早批次（2026-11-02，科技模组锭接入：Thermal / Mekanism / Create）

- **材料表换成候选制**：`materials.json` 里 9 个自有锭材料原本写的是 `#forge:ingots/<m>` 标签
  （读表时标签还没加载 → 全部打警告并按自有锭处理）。现在改成有序 `ingots` 候选：
  tin/lead → `mekanism` → `thermal_foundation`；silver/nickel → `thermal_foundation`；
  zinc → `create`；osmium/uranium → `mekanism`；platinum/aluminum 保持自有锭。
  与主线**共用同一份表**（Thermal 只在 1.20.1 存在，主线上的候选永远不命中）。
- **`OreMaterial` / `OreMaterials`**：加了 `ingotCandidates`（原始有序候选），解析拆成
  `readIngotCandidates` + `resolveIngot`；模组探测走分支自己的 `ModList`（这里还没有
  `Platform` 门面）。
- **配方**：21 条 base 重新生成（带 `not(or(...))` / `not(...)` 条件）+ 27 条候选变体
  （tin/lead 各 2 个候选、silver/nickel/zinc/osmium/uranium 各 1 个）。
- **踩到的真坑**：Forge 1.20.1 的配方条件键是**裸 `conditions`**，不是 `forge:conditions`。
  写错不报错——`RecipeManager` 直接忽略，然后按无条件解析，未安装模组的 `thermal:`/`create:`
  物品炸出 27 条 `Unknown item`（而且 `Loaded 7 recipes` 里的 7 其实是**配方类型数**，
  不是配方数，别被骗）。改成 `conditions` 后 0 条解析错误。
- **验证（无科技模组）**：27 条变体全部按条件跳过、`tin_ore_gravel` 熔成自有 `tin_ingot`、
  `[materials] loaded 12 entries` 且不再刷「tag ingot 不支持」。
- **验证（真装 Thermal Foundation 11.0.6.70 + Thermal Expansion + Create 6.0.8）**：
  服务端日志 `tin/silver/lead/nickel smelts into thermal:*_ingot from thermal_foundation`、
  `zinc smelts into create:zinc_ingot from create`；RCON 熔炉实测
  `tin_ore_gravel → thermal:tin_ingot`、`silver_ore_sand → thermal:silver_ingot`、
  `zinc_ore_dust → create:zinc_ingot`；自有 tin/silver/zinc 锭全部未注册（`summon` 得 Air）；
  `Unknown item` 0 条（只装 Foundation 时它会为自己的压印模具报 64 条，装上 Expansion 后消失）。
  dev 侧用 `fg.deobf` 把生产 jar 重映射进 `run`，Create 的 JiJ（flywheel/ponder/registrate）正常加载。
- **泥土筛石子（2026-11-02 追加）**：四种石子（圆石/安山岩/花岗岩/闪长岩），筛泥土每次必出
  四种、每种 4-6 个（100%），4 个合成一块对应石头；筛表新增 `min`/`max`（默认 1），手动筛/机器筛共用 `SieveRolls`，
  JEI 多一行数量。贴图 `art/items/pebbles/generate.py`（16×16），模型与配方手写。
- **矿块接 Create 粉碎**：Thermal 磨粉机全走 `forge:ores/<m>` 标签（我们 13 种都能进）；
  Create 只有铁/金/铜/锌用具体物品，其余走标签。补 **12 条** `create:crushing`
  （铁/金/铜/锌 × 砾/砂/尘），条件 `create`，字段用 1.20.1 的 `results[].item` + `processingTime`。
- **验证（追加）**：机器筛 7 次 → 圆石 34 / 安山岩 38 / 花岗岩 32 / 闪长岩 34（每种每次 4-6，100%），
  `[sieve] 6 inputs, 76 drops`；装 Thermal + Create 时 0 条解析错误（12 条 crushing 全被接受）。**未验**：手动筛右键、JEI 数量行、粉碎轮实际输出。
- **0.1.0 已发布（2026-11-02）**：tag `v0.1.0-1.20.1` → `35d98d4`，已推到 `origin`；主线同时发了
  `v0.1.0`。产物 `build/libs/exnihilosatellitium-0.1.0.jar`（1.20.1 / Forge 47.4）。打包剔除
  datagen 的 `.cache/`，带 `LICENSE` + `THIRD-PARTY.md` + `icon.png`；商店页上传仍需所有者操作。
- **未验**：Mek 与 Thermal 同时在场时的优先级（本机没有 1.20.1 的 Mekanism；主线已验证 Mek 单独在）。
- **datagen 目前只有第一批**（标签 + 掉落表）；配方（160 条）与模型在这条线仍是手写，
  移植计划与 1.20.1 API 差异见 [datagen.md](datagen.md)。

## 历史批次（详细见 [history.md](history.md)）

- **2026-11-01**：M1 资产同步、M2 筛子/矿链、M3 锤钩/尘土、M4 坩埚、M6 巫水/瓷桶/木坩埚/蚕/人偶、
  M10 自动化与自动输出配置、桶配方修正、config。
- **2026-11-02**：datagen 第一批（标签 + 掉落表）、科技模组锭接入（Thermal / Mekanism / Create）、
  泥土筛石子、0.1.0 发布。

## 验证状态
| datagen 配方移植 | ✅ | `ModRecipes` 生成 143 条（当批 compare 为 137）；0 different |
| datagen 模型移植 | ✅ | 71 blockstate + 29 方块模型 + 157 物品模型；`compare-models.py` → 248 identical（当批） |
| datagen 幂等 | ✅ | 删旧文件后重跑 `runData`：`written: 0` |
| datagen 服务端 | ✅ | 0 条 recipe 解析错误；`[sieve] 76 drops` / `[barrel] 12` / `[hammer] 49` |
| datagen 客户端 | ✅ | 启动到菜单，0 条 missing model/texture |
| 泥土筛树苗 / 菌岩孢子 | ✅ | 2026-11-02 `runServer`：`[sieve] 6 inputs, 88 drops`、`[compost] 67 entries`；RCON `summon` 两个孢子物品名字正常 |
| 泥土筛竹子 | ✅ | 泥土加 `minecraft:bamboo` 5%；88 drops 加载无警告；竹子堆肥/竹桶/竹坩埚已有，只补来源 |
| 菌岩孢子右键 | ⚠️ | `NyliumSporesItem` 右键下界岩 → 菌岩；未进游戏右键；骨粉链沿用原版 |
| 孢子贴图 / 模型 | ✅ | 2 张 16×16 自有像素（`art/items/sieve_extras/generate.py`）+ datagen 模型；`check-textures` 通过 |
| IE 锭候选（仅 IE） | ✅ | 只装 IE 10.2.0-183：`[materials]` 的 silver/lead/nickel/aluminum/uranium 全部 → `immersiveengineering:ingot_*` |
| IE 熔炼 RCON | ✅ | 熔炉烧 `aluminum_ore_gravel` → `ingot_aluminum`，烧 `silver_ore_sand` → `ingot_silver` |
| IE 优先级（Thermal+Create+IE） | ✅ | `[materials]`：tin/silver/lead/nickel → Thermal、zinc → Create、aluminum/uranium → IE；0 条 `Unknown item` |
| IE 粉碎机（标签） | ⚠️ | 13 种矿块在 `forge:ores/<m>`，IE crusher 输入也是该标签；多方块本身未跑 |
| 匠魂种子（条件物品） | ✅ | 只装 Tinkers：`summon` 两个种子成功；不装：`[sieve] 90` vs 92，无 unknown item |
| 匠魂钴 | ✅ | `[materials] cobalt → tconstruct:cobalt_ingot`；RCON 熔炉 `cobalt_ore_gravel → tconstruct:cobalt_ingot` |
| 匠魂熔炼配方 | ⚠️ | 78 条 `tconstruct:melting` 格式对齐，加载无报错；匠魂炉实际熔化未跑 |
| 下界合金碎片 | ✅ | 碎下界岩 0.4%；`[sieve] 90`（无匠魂）/ 92（有匠魂） |
| 匠魂草种子右键 | ⚠️ | `TinkersGrassSeedItem` 右键草方块或泥土 → `*_vanilla_slime_grass`；需玩家实测 |

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| P1 TableBuilder / overlay（1.20.1） | ✅ | 2026-11-03：七张表 add/replace；`/reload` 前后 dump 逐字节一致；clear 7 条回基线 |
| P1 `/exnihilo dump`（1.20.1） | ✅ | 七张表排序行输出；命令与主线同一份代码 |
| P1 守卫（1.20.1） | ⚠️ | 只跑 build + `check-textures.py`；本分支没有 core/data 守卫脚本 |
| `./gradlew build` | ✅ | 2026-11-02：`--offline` 下 `BUILD SUCCESSFUL`；直连 TLS 失败时用缓存 |
| `python3 scripts/check-textures.py` | ✅ | 七个子模型只引用 vanilla **block** 贴图 |
| 服务端启动 | ✅ | 无 Jade 也能启动；`Done (3.5s)` |
| 七种桶 BE | ✅ | 改名后 RCON：`exnihilosatellitium:barrel` 堆肥→DIRT、`DirtReady=1b`；七种材质共享同一 BE 注册表 |
| 堆肥容量/刻度 | ✅ | `data get` 读回 `Compost=1000`、`CompostScale=1000`；满后 1000 tick → DIRT |
| 堆肥数值表 | ✅ | `/reload` 日志 `[compost] loaded 65 entries`，无 unknown item / invalid tag |
| 石桶流体 NBT | ✅ | RCON 写 `FluidName`/`Amount`，读回 `minecraft:lava` / `1000` |
| 掉落表 | ✅ | `/loot spawn ... exnihilosatellitium:blocks/{spruce,stone}_barrel` 返回对应物品 |
| 资源重载 | ✅ | `/reload` 后 `Loaded 7 recipes`（正好七种桶配方）、`[compost] loaded 65 entries` |
| 客户端资源加载 | ✅ | 改名后客户端到主菜单；`Reloading ResourceManager` 无 missing model / missing texture |
| 开发测试 mod | ✅ | JEI 15.59.0.212 / Jade 11.13.2 / JECh 4.6.11 按 deobf 方式加载，客户端到主菜单不崩 |
| 实际进游戏目视 | ❌ | 只跑了客户端资源加载；没有截图验收比例、箍位置、物品栏图标 |
| 右键取泥土 | ⚠️ | 逻辑未改，仍未自动化（需要真实玩家） |
| 逐项右键堆肥 | ⚠️ | JSON/标签已对齐，但没有玩家逐项右键验证点数 |
| 比较器 | ⚠️ | 逻辑未改，仍未实测 |
| Jade tooltip | ⚠️ | 编译通过、无 Jade 启动通过；**未装 Jade 实测** |
| 合成配方 | ⚠️ | 7 个 JSON 都加载成功；没有玩家实测合成格 |
| 矿物六态注册 | ✅ | 客户端 `/give` 与创意标签可见 72 条；`[materials] loaded 12 entries` |
| 矿物六态纹理 | ✅ | 作者目视通过：原版底 + 遮罩 + 罩色，物品栏为 3D 方块 |
| 矿物链配方 | ✅ | 83 条 recipe JSON 在 mod 数据包里；36 条 4 碎块→方块、36 条方块→锭（RCON 验证锭物品存在） |
| M2 筛子方块/ BE | ✅ | RCON：`setblock ... sieve`、`[mesh=0/3]` 状态命中、`data merge` 的 `Mesh`/`Contents` 读回 |
| M2 筛料表 | ✅ | `/reload` 日志 `[sieve] loaded 3 inputs, 48 drops`；`dust` 表按计划未装 |
| M2 筛子玩法（右键/掉落/耐久） | ⚠️ | 需要玩家右键；客户端目视待作者验收 |
| M1.5 转化表加载 | ✅ | `[barrel] loaded 4 transforms (2 fluid, 2 item)` |
| M1.5 固化 | ✅ | RCON：木桶满水 + 上方熔岩 → `Solid=cobblestone`、水清空；石桶满熔岩 + 上方水 → `Solid=obsidian` |
| M1.5 限温/取固体/雨水 | ⚠️ | 逻辑已写；限温提示、右键取固体、雨水需要玩家实测 |
| M3 表加载 | ✅ | `[hammer] loaded 47 recipes`、`[crook] loaded 10 leaves tables` |
| M3 物品/方块 | ✅ | RCON：Wooden Hammer / Netherite Hammer / Bone Crook / Dust 均能 summon，`dust` 方块可放置 |
| M3 锤/钩手感 | ⚠️ | 左键敲方块、耐久、创造模式、骨钩 ×1.5 需要玩家实测 |
| JEI 分类页 | ✅ | `[jei] registered 35 hammer / 4 sieve / 4 barrel + 65 compost / 10 crook`（进存档冒烟） |
| Jade 筛子 | ✅ | `SieveComponentProvider` 已注册 + `config.jade.plugin_...sieve` 翻译（缺 key 会崩） |
| JEI 画面 | ⚠️ | 分类页/布局未目视（冒烟只验证了注册与不崩） |
| M4 坩埚表 | ✅ | `[crucible] loaded 39 meltables` / `9 heat sources`；JEI `39 meltables, 9 heat sources` |
| M4 熔化 | ✅ | RCON：篝火 0.25 mB/t（3 秒 250→234.75、15 mB 熔岩）；熄灭篝火停；火把 0.1 mB/t |
| M4 玩家交互 | ⚠️ | 右键投料/取流体、木坩埚差异化（M6）、Jade 画面待玩家 |
| 筛子产物轮播 | ✅ | 超过 21 条时按槽位轮播（JEI 自动切换多物品槽），tooltip 跟随当前显示项；沙筛 23 条不再截断 |
| M2 第 4 张筛料表 | ✅ | `[sieve] loaded 4 inputs, 62 drops`（尘土已可筛） |
| 27/28 资源模型 | ✅ | 客户端资源加载无 missing model/texture（筛子 frame/mesh、9 种锭、矿方块） |
| 1.20.1 配方计数 | ℹ️ | 1.20.1 的 `Loaded 7 recipes` 是**配方类型数**，不是配方条数；别拿它当进度 |
| 改名完整性 | ✅ | `git grep` 无旧 id/旧类名；jar 内 `mods.toml` 为 `exnihilosatellitium` / `Ex Nihilo: Satellitium` |

| M6 巫水发酵 | ✅ | RCON：满水桶 + 菌丝，约 50 秒后 `FluidName=exnihilosatellitium:witch_water`、`Amount=1000` |
| M6 巫水方块 | ✅ | RCON `setblock ... witch_water` 后 `execute if block` 命中 |
| M6 桶规则（物品） | ✅ | RCON + FakePlayer `use()`：巫水 + 沙子 → `Solid: soul_sand`、桶清空 |
| 桶：水+牛奶→史莱姆/粘液球 | ✅ | RCON 2026-11-02：满水 + 牛奶桶 → 桶清空、空桶返还；Normal 生成史莱姆、Peaceful 掉 `slime_ball` |
| 流动熔岩热源 | ✅ | 2026-11-02 拍板：流动熔岩与源方块都按 0.2（`crucible_heat/lava.json` 只匹配方块 id） |
| M6 人偶（非和平） | ✅ | RCON：熔岩 + 愤怒人偶 → 桶清空、附近生成烈焰人 |
| M6 人偶（和平） | ✅ | RCON：水 + 守卫者人偶 → 桶清空、掉 `prismarine_crystals` |
| M6 木坩埚 | ✅ | RCON：右键投橡树叶 → `MeltFluid=water`、0.5 mB/t 熔化；`wooden_crucibles` 标签生效 |
| M6 瓷桶 capability | ✅ | RCON：1000 mB 巫水瓷桶右键木桶 → 桶内 1000 巫水；木桶满水右键瓷桶 → 桶满 |
| M6 蚕 | ✅ | RCON：右键橡树叶 → `infested_leaves` + `Mimic` 保留原方块；进度按 0.0005/tick 增长 |
| M6 侵染扩散 | ✅ | RCON：0.65 起 12 秒内相邻橡树叶变 `infested_leaves` |
| M6 草种子/孢子 | ✅ | RCON：右键泥土 → `grass_block` / `mycelium` |
| config 热改 | ✅ | 改 `capacity=3000` 后 8000 mB 坩埚被裁到 3000；改回 8000 不重新灌满（预期） |
| 1.20.1 M6 表加载 | ✅ | `[barrel] 11 transforms (2 fluid, 9 item)`、`[sieve] 6 inputs, 72 drops`、`[hammer] 49` |
| 1.20.1 build（M6） | ✅ | `./gradlew build --offline` 通过；`scripts/check-textures.py` 通过 |

| M10 电动筛 | ✅ | RCON：Electric 20000 → 12460（20 FE/t），15 秒产 3 件、沙 64→61、筛网 Damage 3 |
| M10 电动锤 + 速度/时运 | ✅ | RCON：速度 III ×2 + 时运 III，15 秒 23 次、铁锤 Damage 23、92 砂砾 |
| M10 内能筛 | ✅ | RCON：煤燃烧 `BurnHalfTicks` 3200→2712（6 秒），Work 22，产出矿碎块 |
| M10 自动输出 | ✅ | RCON：`AutoOutput=1/OutputMask=1` + 下方箱子，产出进箱子；内能筛同样生效 |
| M10 漏斗抽取 | ✅ | RCON：下方漏斗只拿到 `osmium_crushed` / `prismarine_shard`，筛网/沙留在机器 |
| M10 漏斗补料 | ✅ | RCON：上方漏斗把箱子里的沙补进输入槽（机器沙 28、箱子剩 33） |
| M10 config | ✅ | `run/config/exnihilosatellitium-common.toml` 生成 `crucible.*` + `machine.*` |
| M10 客户端冒烟 | ✅ | 进存档无 missing model/texture；JEI 37 hammer / 11 barrel / 6 sieve / 10 crook |
| M10 JEI 箭头 | ✅ | 2026-11-02 实测：点进度箭头后 `screen = mezz.jei.gui.recipes.RecipesGui`（空输入打开分类）；修复前是 `super.mouseClicked` 先吞掉点击 |
| M10 GUI/动画 | ⚠️ | 抽屉、BER 动画、槽位拖拽、有输入时的 JEI 聚焦待作者目视 |

## 一次性验收清单（作者进游戏）

> 服务端逻辑都已 RCON 验过；下面只列“要看一眼/摸一下”的项。建议创造模式 + JEI。

- [ ] **桶**：12 种桶合成（4 板 + 台阶）；水+熔岩→圆石、熔岩+水→黑曜石；堆肥成土；
      木桶拒绝熔岩的提示；满桶上方倒流体固化。
- [ ] **筛/锤/钩**：4 张筛料表（泥土/沙/砂砾/尘土），三种筛网耐久；6 锤左键、2 钩；
      粉碎下界岩/末地石能筛。
- [ ] **坩埚**：瓷土→未烧制→烧制；投圆石/沙子；篝火/火把热源；熔料与流体显示切换；
      11 种木坩埚被动熔树叶/树苗、拒绝熔岩。
- [ ] **巫水线**：满水桶 + 菌丝发酵；巫水桶装/倒；巫水+沙→灵魂沙、巫水+树苗→深色橡树苗；
      沙筛古代孢子 → 泥土变菌丝、牛变哞菇。
- [ ] **瓷桶**：未烧制→烧制；舀水/熔岩/巫水；倒进木桶/坩埚；图标流体层染色。
- [ ] **蚕/人偶**：蚕右键树叶→侵染、变白、扩散、掉线、Jade 百分比；5 种人偶配方与
      桶规则（和平掉战利品 / 非和平生成生物）。
- [ ] **四台机器**：合成；右键开 GUI；能量/火焰/进度条；空槽虚影与状态行；
      插件槽只收对应插件（锤子时运槽上限 1）；燃料槽只收燃料；JEI 箭头点击。
- [ ] **自动输出**：左侧标签页 → 面板；6 个面按钮（上/下/前/后/左/右）与 tooltip 世界方向；
      开启后产物弹出；关掉最后一个面自动关总开关；Esc/E 返回。
- [ ] **机器动画**：锤头下压、筛料薄层变薄、电动蓝火花/内能火焰烟；破坏机器掉全部内容。
- [ ] **管道**：下方漏斗只抽输出格；上方漏斗补料；电动只进不出能量。
- [ ] **配置**：`run/config/exnihilosatellitium-common.toml` 的 `crucible.*` / `machine.*`
      热改生效（坩埚容量、机器速度/耗电）。

## 交接清单（新会话先看）

- 工作区：**`/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium-1.20.1`**（`1.20.1-forge` 的
  worktree）；主工作区 `/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium` 是 `main`。
- 构建：`JAVA_HOME=/Users/kaifengcrt/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./gradlew build`。
- 客户端：`./gradlew runClient -PdevMods`（JEI 走 maven、Jade/JECh 走 `libs/` deobf；
  `-PdevMods` 只给客户端，JECh 在专用服会崩）。专用服：`./gradlew runServer`（不带 `-PdevMods`）。
- 冒烟技巧：临时在 `runs` 里加 `client { args '--quickPlaySingleplayer', '<世界名>' }`
  可无点击进存档，JEI/Jade 的注册日志会打印；验完把这段删掉。
- 新增方块/物品的固定套路（M4 已验证）：注册 → BE → 数据表 + reload listener →
  渲染 → Jade（**`config.jade.plugin_<ns>.<uid>` 翻译必须有，缺了 Jade 直接崩客户端**）→
  JEI 分类（参考已移植的 `compat/jei/` 五页 + 坩埚两页）→ `check-textures.py`。
- config 已有：`common/config/ExConfig`（`run/config/exnihilosatellitium-common.toml`），
  键是 `crucible.*` 与 `machine.*`；热改由 `CrucibleBlockEntity` 每 tick 读取并裁剪。

## 下一步（按价值排序）

1. **一次性进游戏验收 M1.5–M10**：桶/筛/锤/钩/坩埚/巫水/瓷桶/蚕/人偶，以及四台机器、
   自动输出抽屉、BER 动画、JEI 页面（服务端逻辑都已 RCON 验过，剩下画面/手感）。
2. **M5**：矿链（4 碎块→方块→锭）在 M2 已铺完，无需再补；标记为完成。
3. **M7 余项**：本分支 datagen **只有第一批**（标签 + 掉落表，`0b7df76`），配方与模型仍是手写；
   主线已完成第二批（配方）与第三批（blockstate/模型），移植差异见 [datagen.md](datagen.md) §2。
   语言保持手写 JSON（已拍板）。

## 已拍板的决策

- **项目名**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`，
  Java 包 `com.kaifengcrt.exnihilosatellitium`，资源命名空间 `exnihilosatellitium`（2026-10-28）。
- **许可证 MIT**（2026-10-27 从 ARR 转）；Forge MDK 仍 LGPL-2.1，见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**（2026-10-29）：自动发现保留，但注册只认材料表。表内材料由我们
  手写颜色 / `tier` / `rarity`，并在游戏里拥有完整自有链（矿砾 → 破碎 → 粉碎 → 粉末 → 锭）；
  表外材料只生成骨架行、默认关闭，作者填值并重启后开启。不做“扫到即生效”。
  详见 [DESIGN.md](DESIGN.md) §6.1 第 4 条、§6.4；表结构见 [data-format.md](data-format.md) §8。
- **方块走 vanilla-sampled**：模型直接引用 `minecraft:block/*`，不复制/不打包原版贴图。
  七种桶共用 `barrel_base.json` 几何；木桶侧重木种木板 + 去皮原木内壁 + `gray_concrete` 深灰箍，
  石桶用 `stone` + `smooth_stone` + `deepslate_tiles`。
- **物品与特殊表面自有像素**：瓷土、堆肥面；不把 AI 生图放进 release。
- **原版 Ex Nihilo 与 Omnia 都是 GPLv3**：只对照玩法数值/行为，不复制代码、JSON、贴图、模型、
  音效；`reference/` 和本地 Omnia jar 只读、gitignore、不随包分发。复制 GPL 资源就必须改 GPLv3。
- **木桶 14×16×14 内缩**；物品模型用开口 block model + `display` 变换（没有封闭盖）。
- **七种桶共享一个 BE 类型**；行为/NBT/Jade 完全一致，石桶暂不做差异化。
- **堆肥用 1000 分制**：以 **Omnia 1.10.2** 的表为骨架，原版 1.7.10 校验比例；
  Sequentia 只借 1000 刻度，不采用它的重平衡。树苗/树叶 125、面包 125、花/蘑菇 100、
  腐肉/蜘蛛眼/小麦 85、肉/南瓜派 175、鱼 120、瓜块 150、胡萝卜等 80。
  满 1000 后 1000 tick（50 秒）变 DIRT。老存档按 `CompostScale` 缺失自动 ×10 迁移。
- **Jade 是可选依赖**：`compileOnly` API，不打包；没装时插件类不加载。
- **月光魔法是可选软依赖**：`moonlight_magic`，目前只占位，没有实际联动内容。

## 踩过的坑（改代码前先看）

- **ForgeGradle 卡 `Checking server connections`**：加 `-Dnet.minecraftforge.gradle.check.certs=false`。
- **直连 TLS 失败（libraries.minecraft.net / repo.maven TLS handshake）**：缓存已齐时用 `--offline` 构建/启动。
- **`FluidTank` NBT 是顶层** `FluidName` / `Amount`，不是 `{Fluid:{...}}`。
- **`#minecraft:crops` / `#minecraft:seeds` 不是 1.20.1 物品标签**；用 `#forge:*` 或具体 id。
  `minecraft:carrots/potatoes` 是方块 id，物品是 `carrot` / `potato`。
- **内缩模型的外侧面不能加 `cullface`**：墙在 z=1/x=1，不在方块边界，加了会整面被剔除。
- **堆肥标签和 JSON 必须同步**：服务端也先查 `#exnihilosatellitium:compostable`，JSON 里有但标签没有 = 不能堆肥。
- **堆肥刻度从 100 改成 1000**：老存档没有 `CompostScale`，`load()` 会 ×10 并缩放计时；新存档写 `CompostScale:1000`。
- **共享 BE 类型要列全七种方块**：`BlockEntityType.Builder.of` 漏一个，放置后 BE 不创建 / ticker 为 null。
- **旧配方 `#minecraft:planks` 必须删掉**：它会和具体木种配方同时匹配，产物不可预测。
- **子模型不要直接当方块模型用**：`barrel_base.json` 没有 `textures`，只作为 parent。
- **Jade / 月光魔法生产 jar 不能直接丢 `run/mods` 做 dev 测试**：SRG 名会 `NoSuchFieldError`；
  联调要正式服务端/客户端，或用 deobf 依赖。本分支已改成 `libs/` + `fg.deobf` +
  `mixin.env.remapRefMap`（见「现在停在哪」），新加 mod 照这个套路放。
- **Jade provider 缺 `config.jade.plugin_<ns>.<uid>` 翻译会直接崩客户端**：jade 11 在
  `JadeClient.onGui` 里 `AssertionError`（`Missing config translation`），不是可选提示。
  分支的木桶插件 uid 是 `barrel`，`config.jade.plugin_exnihilosatellitium.barrel` 已补。

## 常用命令

```bash
./gradlew build -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
python3 scripts/generate_barrel_variants.py          # 从材质表重生成七种桶的 JSON
./gradlew runServer -Dnet.minecraftforge.gradle.check.certs=false   # RCON：localhost:25575 / eni
# 注意：run/ 被 gitignore；fresh clone 需要在 run/server.properties 自己开 RCON
./gradlew runClient -Dnet.minecraftforge.gradle.check.certs=false
```

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java        # 入口（Forge 的 FMLJavaModLoadingContext）
├── registry/                       # Block / Item / BlockEntity / MenuType / CreativeTab
├── common/
│   ├── block/                      # Barrel / Sieve / Crucible / UnfiredCrucible / Machine / Liquid
│   ├── blockentity/                # 桶 / 筛 / 坩埚 / 侵染树叶的状态机、NBT、比较器
│   ├── fluid/                      # 巫水（ForgeFlowingFluid + FluidType）
│   ├── barrel/ compost/            # 转化表 / 堆肥表 + reload listener
│   ├── sieve/ hammer/ crook/       # 筛表 / 锤表 / 叶表 + reload listener
│   ├── crucible/                   # 熔点 + 热源表 + reload listener
│   ├── machine/                    # 四台机器：SieveRolls / HammerRolls / UpgradeItem
│   │   ├── block/ blockentity/ menu/
│   ├── material/                   # materials.json 读取（注册期，ingots 候选）
│   ├── config/                     # ExConfig（ForgeConfigSpec）
│   ├── network/                    # 机器输出配置用的 C2S 包
│   ├── event/                      # CommonEvents（reload）、能力注册
│   └── tag/ModTags.java
├── client/                         # BER（桶/筛/坩埚/机器/蚕）、GUI 屏、ClientEvents
├── compat/                         # MoonlightCompat、jade、jei（可选软依赖）
├── datagen/                        # 第一批：标签 + 掉落表（配方/模型仍手写）
└── platform/                       # 本版的机器能量/物品能力适配
```

数据在 `src/main/resources/data/exnihilosatellitium/`（`recipes/`、`loot_tables/`、`tags/`），
格式见 [data-format.md](data-format.md)；datagen 现状见 [datagen.md](datagen.md)。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑”，data-format 写“JSON/NBT/命令”，textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录。
- 不要在本文件抄大段设计；链接过去。
- **批次叙事只留当前批次**；上一批/更早的搬到 [history.md](history.md)（2026-11-02 整理时搬走过约 100 行）。
