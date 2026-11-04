# 1.20.1 分支历史批次（2026-11-01 ～ 2026-11-03）

> 从 [status.md](status.md) 搬出来的批次叙事与日期流水：只记「当时做了什么、怎么验的」。
> **当前状态、验证表、下一步只看 status.md**；这里不再维护，只作追溯用。

## 2026-11-03 晚（工匠巧思 + AE2 赛特斯石英）

- **对象**：TiT `0.2.4`（只有 1.20.1）+ AE2 `15.4.12`（前置 GuideME `20.1.15`）。两者都可选，
  不写 `mods.toml`；AE2/GuideME 的生产 jar 放 `libs/`，由 `-PtechMods` 走 `fg.deobf`。
- **TiT 阿迪特（`ardite`，金属）**：`ingots` 候选 `tinkers_thinking:ardite_ingot`，没装时回退
  自有 `ardite_ingot`；**单源**碎下界岩 1% → `ardite_broken`，走三档链 → 矿块 → 熔炼；
  6 条 `tconstruct:melting`（碎矿 45 mB / 矿块 180 mB，950°C，`forge:molten_ardite`）。
- **TiT 叶绿素（`chlorophyll`，`gems`）**：只在 TiT 在场时注册；**单源**草方块 1% →
  `chlorophyll_broken`（草方块表 = 泥土 24 条 + 这一条），矿块熔成
  `tinkers_thinking:chlorophyte_compound`。没装 TiT 时整条不出现。
- **AE2 赛特斯**：`sieve/sand_ae2.json` 对沙追加 `ae2:certus_quartz_crystal` **1/6**、
  `ae2:certus_quartz_dust` **1/12**；不注册自有物品；充能赛特斯走 AE2 自己的充电器，
  天石粉/陨石不做；缺 AE2 时整文件跳过。
- **验证**：`-PtechMods` 全装 `22 entries` / `141 drops` / `79` 锤；RCON 熔炉阿迪特与叶绿素
  通过；去掉 AE2 时 139、无 unknown；手筛 / JEI / 匠魂炉未目视。

## 2026-11-03（数据包 v2 / 末影蛋糕 / 三路径 / 科技矿物链移植）

### 数据包 v2 移植

- 六张玩法表的 `#tag` 输入、根级 `"mod"` 门、`"patch"` / `"remove"` 补丁语义全部移植。
- 1.20.1 的配方条件键是**裸 `conditions`**（不是 `forge:conditions`）；写错会被 Forge 忽略，
  然后按无条件解析，炸出 missing item。`check-data-paths.py` 已加负向测试。
- 诊断命令 `/exnihilo tables|sieve|hammer|compost`、`/exnihilo customize` 与主线同一份代码。
- 验证：测试包 `[sieve] 45 inputs / 115 drops` 等；`/reload` 回基线；删补丁后恢复。

### 末影蛋糕移植

- 可放置蛋糕，主世界 ↔ 末地双向传送；1 片起（`bites=6`），末影之眼补片；8 珍珠 + 蛋糕。
- 复用 vanilla cake 父模型 + 4 张 16×16 贴图；无掉落表；`/setblock ... [bites=6]` 验过。

### 三路径 P1 → P3（分支）

- P1 `common/table/` 整体移植：七张表 add/remove/replace、`/exnihilo dump`、overlay 基座。
- P1.5：priority、冲突日志、去重、server stop 清理。
- P2 KubeJS 2001：`KubeJSPlugin` 类 + `BindingsEvent`，七张表绑定 + JSON 逃生口。
- P3 CraftTweaker 14.0.60：七个 `@ZenRegister` 类，source priority 200，reload 清旧贡献。
- 验证：七张表 dump 与 datapack fixture 逐字节一致；无 KJS/CT 正常启动。

### Mek 氟石 + Thermal 六种矿物链

- 与主线共用 `materials.json` 的 `gems` schema：纯模组产物、无自有兜底、整条只在候选 mod
  在场时注册；21 条 `*_smelting_<mod>` 变体，条件键裸 `conditions`。
- 验证：Thermal Foundation 11.0.6 + CoFH Core：7 种矿砾 RCON 熔炼 7/7 通过；
  无科技模组时 13 entries、无 unknown item。

## 2026-11-02（发布 / datagen 移植 / Tinkers / IE / 树起点）

- **0.1.0 发布**：tag `v0.1.0-1.20.1`（`35d98d4`），jar 587 KB；已推 origin，商店上传待所有者。
- **0.2.0 准备 → 1.0.0 发布**：原定 0.2.0，验收后直接定为 **1.0.0**；tag `v1.0.0-1.20.1`（2026-11-04）。
- **datagen 第二/三批移植**：配方 143 条（133 base + 4 石子→石头 + 6 钴）、71 blockstate +
  29 方块模型 + 157 物品模型；`compare-models.py` 248 identical；`written: 0` 幂等。
- **Tinkers 3**：两个黏液草种子条件注册；钴 → `tconstruct:cobalt_ingot`；78 条匠魂熔炼；
  下界合金碎片 0.4%。
- **Immersive Engineering**：silver/lead/nickel/aluminum/uranium 候选（排在 Thermal/Mek 后）；
  IE 粉碎机走 `forge:ores/<m>`。
- **树的起点**：泥土筛 9 种树苗 + 竹子 5%；碎下界岩筛菌岩孢子各 5%。
- **科技模组锭**：Thermal / Mek / Create 候选；无模组回退自有锭。
- **1.20.1 datagen 差异**：`runData` 生成到 `recipes/`、`loot_tables/`、`tags/blocks|items/`。

## 上一批次（2026-11-01，M6 + M10 + config + 桶配方修正）

- **桶配方修正**：七种木桶/石桶的合成回到原版式样 `x x / x x / xyx`
  （x=对应木板或 `stone`，y=对应台阶 / `smooth_stone_slab`）；之前是 8 块板的
  `PPP/P P/PPP`，与主线 85cc392、原版都对不上。补齐红树/樱花/竹/绯红/诡异 5 种桶，
  木桶总数 12（11 木材 + 石桶），合成/掉落/贴图/模型/`mineable/axe` 同步。
- **流动熔岩热源**：按所有者决定，源方块与流动熔岩都按 **0.2**，不再区分 `level`。
- **config（小尾巴）**：新增 `common/config/ExConfig`（`ForgeConfigSpec`，`COMMON`），
  键与主线一致：`crucible.capacity` / `crucible.meltCapacity` / `crucible.heatSpeedMultiplier`。
  `CrucibleBlockEntity` 改为动态容量：tick 里发现配置变化就 `setCapacity` 并裁掉多余流体，
  熔料上限/热源倍率实时读。生成的 `run/config/exnihilosatellitium-common.toml` 已核对。
- **巫水 + 古代孢子**：`ModFluids`（`ForgeFlowingFluid` + 自定义 `WitchWaterFluidType`，
  1.20.1 用 `initializeClient` 注册紫色水贴图）、`witch_water` 液方块 + 桶、
  `barrel/*.json` 两条转化；满水桶 + 3×3×3 菌丝（或蘑菇煲起酵）按
  `1 + 菌丝数/2` 累计 1000 tick 变巫水；沙筛 1/128 古代孢子，右键泥土→菌丝、右键牛→哞菇。
- **瓷桶**：`PorcelainBucketItem` + Forge `FluidHandlerItemStack`（1000 mB，NBT 顶层
  `Fluid: {FluidName, Amount}`），3 瓷土 → 未烧制 → 烧制；模型 predicate
  `exnihilosatellitium:filled` + 流体层染色；能装熔岩/巫水，也能倒进木桶/坩埚。
- **木坩埚 11 种**：`wooden_crucibles` 方块标签 + 同一 `CrucibleBlockEntity`；
  `isWooden()` 走标签，被动 0.5 mB/t、只收水类熔料、`fill` 拒 >433 K。
- **粉碎方块 + 草种子**：锤下界岩/末地石 → `crushed_netherrack` / `crushed_end_stone`
  （锤表 49 条），两张筛料表进入 `sieve/`（6 输入 / 72 掉落）；泥土筛草种子。
- **蚕 / 侵染树叶**：`silkworm` 右键树叶 → `infested_leaves`（BE 记 `Mimic`、0.0005/tick、
  60% 后扩散、破坏按进度掉线）；BER 用 `WhiteningVertexConsumer` 按进度把原树叶混向白色；
  物品形态用 `BlockEntityTag.Progress` 区分 0%/成熟，Jade 显示原树叶名与百分比。
  1.20.1 没有 data component，因此走 NBT（这是与主线唯一的实现差异）。
- **人偶 5 种**：`doll` / `angry_doll` / `creepy_doll` / `shulker_doll` / `guardian_doll`，
  8 条合成（含两条镜像）；桶规则扩展 `entity` + `peaceful_drop`，右键立即生成生物 /
  和平掉战利品（随机列表），旧存档的 `CookEntity` 倒计时仍可继续。
- **顺手补的坑**：`dust`、`crucible`、`unfired_crucible` 三个方块此前**没有掉落表**
  （挖掉不掉），本批一并补上；`check-textures.py` 同步主线的 64×64 人偶 / 16×64 蚕动画放行。

## 上一批次（2026-11-01，M10 自动化）

- **四台机器**：`electric_sieve` / `electric_hammer` / `fuel_sieve` / `fuel_hammer`，
  共用 `MachineBlock` + `MachineBlockEntity`，BE 分电动/内能两种；4 个插件槽（速度/时运，
  每种一个专槽，锤子的时运槽上限 1），内能机器只烧原版熔炉燃料。
- **判定单一来源**：机器每完成一次操作都调 `SieveRolls` / `HammerRolls`，与手动筛/锤完全同一张表。
- **GUI**：`ElectricMachineScreen` / `FuelMachineScreen`（176×186，能量/火焰条、箭头进度、
  空槽虚影、状态行）；1.20.1 没有 `pushGuiLayer`，自动输出改成**同一屏幕内嵌的左侧抽屉**
  （22×28 标签页 + 132×168 面板，6 个玩家相对方向按钮），不再新开 Screen。
- **网络/能力**：`MachineOutputMessage` 走 Forge `SimpleChannel`；菜单用
  `IForgeMenuType` + `NetworkHooks.openScreen` 传方块坐标。物品能力
  `MachineItemHandler` 只允许从输出 12 格抽取；电动暴露只进不出的 `MachineEnergy`。
- **config**：新增 `machine.ticksPerOperation` / `energyPerTick` / `energyCapacity` /
  `burnPerOperation`（键名与主线一致）。
- **数据/资产**：四台机器的 blockstate/model/32×32 贴图、6 个插件物品、合成配方、
  掉落表、`mineable/pickaxe` + `needs_stone_tool`、JEI 催化剂、GUI 贴图全部同步主线。
- **踩坑**：1.20.1 的 `ItemStack#getBurnTime` 走 `IForgeItem` 返回 **-1**，燃料槽必须用
  `ForgeHooks.getBurnTime(stack, RecipeType.SMELTING)`；否则煤会被当成非法物品弹出来。
- **验证**：`./gradlew build --offline` + `check-textures.py` 通过；RCON 验了电动筛耗电
  （20 FE/t）、电动锤速度 III + 时运 III（15 秒 23 次、92 砂砾）、内能筛烧煤
  （`BurnHalfTicks` 3200→2712 / 6 秒）、自动输出进下方箱子、下方漏斗只抽输出格、
  上方漏斗补料；客户端冒烟进存档，JEI `37 hammer / 11 barrel / 6 sieve / 10 crook /
  39 meltables`，无 missing model / texture。
- **未验**：机器 GUI 画面/点击、自动输出抽屉观感、BER 工作动画、插件槽/燃料槽的拖拽规则
  （逻辑已编译且服务端行为验过，需要作者进游戏看一眼）。

## 现在停在哪

- 工作区：`/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium-1.20.1`（`1.20.1-forge` 分支的
  git worktree，主工作区仍在 `main`）。
- **2026-11-01 M1 资产与主线同步**（`d138cf9`）：矿物方块改成主线那套**单层染色** cube
  （去掉双层遮罩 / `ore_mask_*`）、铁碎块换**暖铁色**、木桶去掉两道外凸箍与侧面
  `rotation: 90`；`scripts/generate_ore_variants.py` 跟着改，重跑不会变回去。
- **2026-11-01 M2 铺到分支**：
  - 筛子：`SieveBlock` + `SieveBlockEntity` + `SieveRenderer`（1.20.1 的 `use` / NBT /
    `renderSingleBlock` 签名），`MESH` 方块状态、进度条式做工、破坏返还、右键装网/下料；
  - 三种筛网：木 10 / 丝 64 / 铁 256 耐久，只在创意栏与合成里出现；
  - 数据：`sieve/{dirt,sand,gravel}.json`（3 输入 / 48 掉落），`forge:ores`、
    `forge:ingots` 标签，`recipes/` 下 83 条配方（7 桶 + 36 碎块→方块 + 36 熔炼 + 筛子/网）；
  - 矿链：12 种材料 × 4 碎块 → 矿方块 → 熔炼成锭；9 种没有现成锭的材料（tin/silver/lead/
    nickel/platinum/aluminum/osmium/zinc/uranium）注册自有 `<m>_ingot`，铜改成原版铜锭。
  - **没有做**：`dust` 筛料表（需要 M3 的尘土方块）、JEI（这条分支还没引入 JEI 依赖）。
- **2026-11-01 M4 坩埚**：`CrucibleBlock` / `UnfiredCrucibleBlock` / `CrucibleBlockEntity`、
  `CrucibleRecipes`（39 条熔点）+ `CrucibleHeat`（9 热源）+ reload listener；瓷土 → 未烧制
  坩埚 → 烧制；熔料与流体分开记，热源按 `crucible_heat/*.json` 的 mB/t 熔化；比较器、
  破坏返还、Jade 熔料/热源、JEI 熔点/热源两页。1.20.1 分支没有 config，容量/倍率先用常量
  （8000 / 2000 / 1.0）。RCON 验证：篝火 0.25 mB/t、熄灭篝火停、火把 0.1 mB/t。
- **2026-11-01 JEI + Jade**：JEI 15.59 的 compileOnly API（`maven.blamejared.com`）+
  `-PdevMods` 才加载完整 JEI；移植五个分类页（筛子 / 桶转化 / 堆肥 / 锤 / 钩），
  数据和 block entity 共用同一张表。Jade 补 `SieveComponentProvider`（筛网名称+耐久、
  筛选进度）和 `config.jade.plugin_exnihilosatellitium.sieve` 翻译（缺了 Jade 会崩客户端）。
  冒烟测试（`--quickPlaySingleplayer` 进存档）：`[jei] registered 35 hammer / 4 sieve /
  4 barrel + 65 compost / 10 crook`，无异常。
- **2026-11-01 M3 锤/钩 + 尘土**：`HammerItem` / `CrookItem` / `HammerRecipes` /
  `CrookDrops` + 两个 reload listener、`HammerRolls` 共用判定；6 锤（100 木/石/铁/金/钻/下界合金，
  耐久 64/128/256/32/1561/2031）+ 2 钩（木 64 / 骨 128，骨钩概率 ×1.5）；47 条锤表（跳过
  M6 的 `crushed_*` 两条）、10 张钩表（去掉 M6 蚕掉落）；沙 → 尘土、尘土方块可筛
  （第 4 张筛料表现在启用：4 输入 / 62 掉落）。**1.20.1 没有 `BlockDropsEvent`**：锤子走
  `BlockEvent.BreakEvent` 自己接管破坏（取消事件 + 手动移除 + 掉锤表产物 + 扣耐久），
  钩子保留原版树叶掉落、只追加钩表产物。
- **2026-11-01 M1.5 桶内转化**：`BarrelRecipes` + `BarrelRecipeReloadListener`
  （`barrel/*.json`，4 条规则：2 流体 + 2 物品）、木桶 433 K 限温、石桶不限、
  满桶上方倒另一种流体自动固化、右键取出固体、接雨水、比较器/破坏返还；
  渲染补了桶内固体（用方块粒子图标画一个立方体）。`water_dust_clay` 需要 M3 的
  `dust` 方块，先没放。
- 开发服：`run/server.properties` 现在是测试用（`level-name=m2-test`、RCON `eni`、
  `online-mode=false`）；要和主线一样正式联调时记得改回 `true`。
- **开发时加载生产 mod（2026-11-01）**：1.20.1 的 dev 运行时用 official 名，生产 jar 里是
  SRG 名，**直接丢 `run/mods` 会 `NoSuchMethodError` / mixin refmap 失效**。现在的做法：
  jar 放 `libs/`（gitignored）→ `build.gradle` 里 `flatDir` + `fg.deobf(...)` 挂成
  `compileOnly`/`runtimeOnly`，并给 run config 加
  `mixin.env.remapRefMap=true` + `mixin.env.refMapRemappingFile=build/createSrgToMcp/output.srg`。
  依赖块用 `file(...).exists()` 守卫，没放 jar 的干净 clone 仍能 `build`。
  当前接的是 **JEI 15.59.0.212 / Jade 11.13.2 / Just Enough Characters 4.6.11**。
