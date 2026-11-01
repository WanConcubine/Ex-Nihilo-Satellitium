# 当前状态（交接卡）

> 最后更新：2026-11-01
> 先看这 20 行，细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**M1 木桶 + 七种材质变体已完成**；**M1.5 桶内方块转化已铺好**（2026-11-01：水+熔岩→圆石、
熔岩+水→黑曜石、熔岩+红石→下界岩、熔岩+荧石→末地石，木桶限温 + 接雨水）；
**M2 筛子 + 矿链已铺好**（三种筛网、4 张筛料表、12 种材料 4 碎块→方块→锭）；
**M3 锤/钩 + 尘土方块已铺好**（6 锤 / 2 钩、47 条锤表、10 张钩表、沙→尘土）；
**M4 坩埚已铺好**（2026-11-01：瓷土 → 未烧制 → 烧制、39 条熔点 / 9 热源、
熔料+流体双储、JEI 两页 + Jade）。**M6 五刀已铺完**（2026-11-01：巫水 + 古代孢子、瓷桶、
木坩埚 11 种、木桶补齐 12 种、粉碎下界岩/末地石 + 草种子、蚕/侵染树叶、5 种人偶）；
**M10 自动化也已铺完**（四台机器 + 6 个插件 + 自动输出 + GUI/JEI/BER）；
`crucible.*` 与 `machine.*` config 全部就位。**桶配方已按原版改正**（4 板 + 台阶，见下）。

## 当前批次（2026-11-01，M6 + M10 + config + 桶配方修正）

- **桶配方修正**：七种木桶/石桶的合成回到原版式样 `x x / x x / xyx`
  （x=对应木板或 `stone`，y=对应台阶 / `smooth_stone_slab`）；之前是 8 块板的
  `PPP/P P/PPP`，与主线 85cc392、原版都对不上。补齐红树/樱花/竹/绯红/诡异 5 种桶，
  木桶总数 12（11 木材 + 石桶），合成/掉落/贴图/模型/`mineable/axe` 同步。
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

## 当前批次（2026-11-01，M10 自动化）

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

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-28：`--offline` 下 `BUILD SUCCESSFUL in 8s`；直连 TLS 失败时用缓存 |
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
| M10 GUI/动画 | ⚠️ | 画面、抽屉、BER 动画、槽位拖拽待作者目视 |

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
3. **M7 余项**：datagen（主线也未开始）；配置已齐。

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
├── ExNihiloSatellitium.java           # 入口
├── registry/                       # Block / Item / BlockEntity / CreativeTab
├── common/
│   ├── block/BarrelBlock.java
│   ├── blockentity/BarrelBlockEntity.java
│   ├── compost/                    # CompostRegistry + reload listener
│   ├── event/CommonEvents.java
│   └── tag/ModTags.java
├── client/                         # BarrelRenderer + ClientEvents
└── compat/
    ├── MoonlightCompat.java
    └── jade/                       # 可选 HUD
```

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑”，data-format 写“JSON/NBT/命令”，textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录。
- 不要在本文件抄大段设计；链接过去。
