# 当前状态（交接卡）

> 最后更新：2026-11-02
> 先看这 20 行；细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**双版本目标**：主线 **Minecraft 1.21.1 / NeoForge 21.1.x**（`main`）+ **1.20.1 / Forge 47.4**
（`1.20.1-forge` 分支，当前停在 M10，M1.5–M10 已全部铺完）。原来的 `legacy/1.20.1-forge` tag
已于 2026-11-01 按所有者要求删除，1.20.1 以分支为准（见
[incidents/2026-11-01-unauthorized-archive.md](incidents/2026-11-01-unauthorized-archive.md)）。
M1–M4、M6 已实现；**M10 自动化已完成并由作者验收**（电动 / 内能 × 自动筛 / 自动锤 + 6 个插件，
GUI、32×32 模型、工作动画、JEI、管道都过了目视/手感），**自动输出配置也已实现**（服务端 RCON 验过，
GUI 待目视）。**supported mods 框架 + Mekanism 已接**（1.21.1）。**P1 薄门面已完成**：core 只通过
`ConfigValues` / `Platform` / `MachineHooks` / `GuiLayers` 读平台，版本胶水收进顶层 `platform/`，
`check-core-imports.py` 的 known 清零。**P2 数据管线已完成**：生成器 `--target 1.21.1|1.20.1`、
`scripts/port_data.py`、`scripts/check-data-paths.py`。**1.20.1 分支已补到 M10**（M1.5 桶转化 + M2 筛子/矿链 + M3 锤/钩/尘土 + M4 坩埚 +
M6 巫水/孢子/瓷桶/木坩埚/12 种桶/蚕/人偶 + M10 四台机器/插件/自动输出 +
`crucible.*`/`machine.*` config；分支提交 `6a733e8`）。
顺带按原版修正了分支的木桶合成（4 板 + 台阶），并补上 `dust`/`crucible`/`unfired_crucible`
缺失的掉落表。1.20.1 侧只剩画面/手感验收；主线下一步是 P3 状态机拆分。
**M7 datagen 已验证两批**（2026-11-02）：第一批 `runData` 生成方块/物品标签 + 方块掉落表，
第二批生成 **133 条配方**（`ModRecipes`，从注册表 + `materials.json` 推导，不生成 advancement），
输出到 `src/generated/resources`，旧的手写 tag/loot/recipe JSON 已删除；只剩 12 条
`*_smelting_mekanism` 手写（datagen 引用不了未注册的模组物品）。模型/blockstate 与语言文件
仍在 `src/main/resources`（下一批）。M8 月光魔法、M9 发布仍未开始。

## 当前批次（2026-11-02，M7 datagen 第二批：配方）

- **`ModRecipes extends RecipeProvider`**：9 材料 × （3 打包 + 3 熔炼）、12 桶、11 木坩埚、
  筛子 + 3 网、6 锤（含下界合金锻造）/2 钩、4 机器、6 插件、8 人偶、瓷土/坩埚/瓷桶、熟蚕 3 条
  = **133 条**；id 与旧手写 JSON 完全一致。
- **不生成 advancement**：`withoutAdvancements(...)` 包一层 `RecipeOutput`，丢掉
  `AdvancementHolder`（本项目从来没发过 `advancement/recipes/**`，多 133 个会改变配方书解锁）。
- **烹饪直接构造 `SmeltingRecipe`/`SmokingRecipe`/`CampfireCookingRecipe` + `MISC` 书类别**，
  等同旧 JSON 缺省值；合成统一 `RecipeCategory.MISC`。
- **12 条 `*_smelting_mekanism` 保留手写**：产物是 `mekanism:ingot_*`，不在 datagen classpath
  上，`ItemStack` 构造不出来；生成的是带 `not(mod_loaded(mekanism))` 的兜底配方。
- **`OreMaterial` 加 `ingotCandidates`**（原始有序候选）：`OreMaterials` 拆成
  `readIngotCandidates` + `resolveIngot`，`ingot` 仍是运行时解析结果，注册/标签逻辑不变。
- **语义对比脚本** `scripts/compare-recipes.py`：shaped 拆成去空行空列的格子、shapeless 多重集、
  烹饪比六字段；结果 `145 old, 133 generated, 12 old-only (0 unexpected), 0 new-only, 0 different`。
- **验证**：`runData` 幂等（删旧文件后重跑 `written: 0`）；`build` + 三个守卫全过
  （`check-data-paths` 仍是 145 recipes / 69 loot / 41 tags）；`runServer` `Loaded 1423 recipes`、
  `our recipes=133`、无解析错误。
- **下一批**：模型/blockstate（施工单见 [datagen.md](datagen.md) §4），语言保持 JSON。

## 上一批次（2026-11-01，1.20.1 M10）

- **分支**：`1.20.1-forge`（worktree `../Ex-Nihilo-Satellitium-1.20.1`）；客户端
  `./gradlew runClient -PdevMods`，服务端 `./gradlew runServer`。
- **M6 五刀**：巫水 + 古代孢子（自定义流体/液方块/桶、菌丝发酵、两条转化、沙筛 1/128）→
  瓷桶（Forge `FluidHandlerItemStack`，1000 mB）→ 木坩埚 11 种（被动 0.5 mB/t、只收水、
  433 K 上限）+ 木桶补齐 12 种 → 蚕/侵染树叶（BE 记原树叶、BER 变白、扩散、掉线、Jade）→
  5 种人偶 + 8 条桶规则（`entity` + `peaceful_drop`，右键立即生效）。
- **config（小尾巴）**：`ExConfig`（`ForgeConfigSpec`）补上 `crucible.capacity` /
  `meltCapacity` / `heatSpeedMultiplier`，与主线键名一致；坩埚 tick 里动态裁剪容量。
- **桶配方错误修正**：七种桶合成回到原版 `x x / x x / xyx`（4 板 + 台阶；石桶
  `stone` + `smooth_stone_slab`），此前误用 8 块板。
- **顺手补坑**：`dust` / `crucible` / `unfired_crucible` 没有掉落表，挖掉不掉，已补。
- **验证**：`./gradlew build --offline` + `check-textures.py` 通过；RCON 验了巫水发酵/液方块、
  巫水+沙→灵魂沙、熔岩+愤怒人偶→烈焰人、和平守卫者人偶→海晶砂粒、木坩埚投料、瓷桶↔木桶
  流体、蚕右键/扩散、草种子/孢子、config 热改；`[barrel] 11 transforms` /
  `[sieve] 6 inputs, 72 drops` / `[hammer] 49`。
- **M10 自动化**：四台机器（电动/内能 × 筛/锤）、6 个插件、自动输出（内嵌左侧抽屉）、
  GUI/BER/JEI、`machine.*` config；1.20.1 用 `ForgeHooks.getBurnTime` 取燃料值
  （`ItemStack#getBurnTime` 在 Forge 47 返回 -1，坑已记进分支 status）。
- **未做**：1.20.1 只剩画面/手感验收（机器 GUI、抽屉、BER 动画、插件槽拖拽、M6 美术）；
  M5 的矿链在 M2 已铺完。

## 上一批次（2026-11-01，1.20.1 M3）

- **分支**：`1.20.1-forge` @ `eaf5f6d`（worktree `../Ex-Nihilo-Satellitium-1.20.1`）。
  开发客户端：`./gradlew runClient -PdevMods`（JEI 15.59 / Jade 11.13.2 / JECh 4.6.11
  从 `libs/` deobf 加载；`-PdevMods` 只给客户端，JECh 在专用服会崩）。
- **M1.5**（`67029d6`）：`BarrelRecipes` 4 条（水+熔岩→圆石、熔岩+水→黑曜石、
  熔岩+红石→下界岩、熔岩+荧石→末地石）、木桶 433 K 限温、满桶上方倒另一种流体固化、
  右键取固体、接雨水、渲染桶内固体；RCON 验过两条流体反应。
- **M3**（`eaf5f6d`）：6 锤 / 2 钩、47 条锤表 / 10 张钩表、`HammerRolls` 共用判定、
  尘土方块（沙→尘土，第 4 张筛料表启用，4 输入 / 62 掉落）。1.20.1 没有
  `BlockDropsEvent`：锤子接管 `BlockEvent.BreakEvent`（取消 + 手动移除 + 掉表 + 扣耐久），
  钩子保留原版树叶掉落、只追加。
- **验证**：build / 纹理守卫通过；服务端 `[barrel] 4`、`[sieve] 4 inputs 62 drops`、
  `[hammer] 47`、`[crook] 10`；锭/锤/钩/尘土均可 summon。左键敲方块、右键取固体、
  筛料手感需要玩家实测（客户端已启动）。
- **JEI 筛子轮播（2026-11-01，主线 + 分支）**：`SieveRecipeCategory` 不再把产物截断到
  21 条；多出来的产物按槽位轮播（JEI 自动切换多物品槽），概率 tooltip 跟随当前显示项。
  锤子的「粉尘→自身」no-op 规则继续对 JEI 隐藏（47 条里展示 35 条）。
- **未做**：M4 坩埚、M6 蚕/人偶/巫水。

## 上一批次（2026-11-01，1.20.1 M2）

- **分支**：`1.20.1-forge` @ `9b92322`（worktree：`../Ex-Nihilo-Satellitium-1.20.1`），
  主线 `main` 不受影响。构建用 Temurin 17（ForgeGradle 47.4.28）。
- **筛子**：`SieveBlock` / `SieveBlockEntity` / `SieveRenderer` 按 1.20.1 API 重写
  （`Block#use` → `InteractionResult`、`saveAdditional(CompoundTag)`、`load()`、
  `BlockRenderDispatcher#renderSingleBlock`、`ForgeRegistries` 查表）；`MESH` 状态、
  进度式做工、破坏返还、三种筛网（10/64/256 耐久）。
- **数据**：`sieve/{dirt,sand,gravel}.json`（3 输入 / 48 掉落）；第 4 张 `dust` 表需要 M3 的
  尘土方块，先不装。83 条配方（7 桶 + 36 碎块→方块 + 36 熔炼 + 筛子/三网）。
- **矿链**：9 种材料注册自有锭（tin/silver/lead/nickel/platinum/aluminum/osmium/zinc/uranium），
  铜改成 `minecraft:copper_ingot`；`forge:ores` / `forge:ingots` 标签齐全。
- **M1 资产同步**（`d138cf9`）：矿方块单层染色 + 暖铁碎块 + 木桶去箍/去 `rotation:90`，
  与主线一致。
- **未做**：`dust` 筛料表、JEI（分支没有 JEI 依赖）、M1.5 桶内转化、M3 锤/钩。
- **验证**：`./gradlew build` 通过；`check-textures.py` 通过；`runServer` 起来后 RCON 验过
  筛子方块/`mesh` 状态/`Mesh`+`Contents` NBT、锭物品存在、`[sieve] loaded 3 inputs, 48 drops`。
  **右键筛料、耐久、掉落需要玩家**——客户端已重新启动，等作者操作验收。

## 更早（2026-11-01，P2 数据管线）

- **`scripts/data_target.py`**：`DataTarget` 一处管住两版的路径与字段——`recipe/`↔`recipes/`、
  `loot_table/`↔`loot_tables/`、`tags/block|item`↔`tags/blocks|items`、`result.id`↔`result.item`、
  `neoforge:*`↔`forge:*`、`c:`↔`forge:`（含 `"tag": "c:rods/wooden"` 与 `#c:` 值）。
- **生成器加 `--target` / `--out`**：`generate_ore_variants` / `generate_barrel_variants` /
  `generate_wooden_crucible_variants` / `generate_sieve_assets`；默认仍写
  `src/main/resources`，`--out` 用于预览/验证。
- **顺手修了两个“重跑会吃手改”的坑**（以前生成器直接覆盖）：
  - lang（三个生成器）改成**只补缺失键**，作者改过的 `木筛网`/`线筛网`/`String Mesh` 不会被冲掉；
  - 筛表（`sieve/*.json`）改成**合并**：保留 M2/M6 手加掉落、生成项按材料权重更新、
    删掉已从 `materials.json` 移除的材料的残留条目。
  现在四个生成器对当前树重跑是 **零 diff**（以前会删掉 13 条手写筛出物：沙 12 + 砂砾 1）。
- **`scripts/port_data.py`**：1.21.1 → 1.20.1 数据（只改路径/字段/命名空间，数值原样）；
  默认输出 `build/port-1.20.1`，非空目录要 `--force`，拒绝写回源树/仓库根。
- **`scripts/check-data-paths.py`**：按目标版本检查目录名、`result.id/item`、条件命名空间、
  `c:`/`forge:` 标签；没有塞进 `check-textures.py`（两类问题分开报更清楚）。
- **验证**：
  - 1.21.1 生成器重跑 → `diff -r` 与仓库逐字节一致（含 lang、筛表）；
  - `port_data.py` → 145 recipe / 69 loot / 41 tag / 125 其他文件，`check-data-paths --target 1.20.1` 通过；
  - 生成器 `--target 1.20.1 --out` 产物（107 recipe / 60 loot / 39 tag）同样通过守卫，
    并与 port 产物在 **240 个文件**上逐字节一致（差异只有合并型 `minecraft:tags/blocks/mineable/*`
    和 3 张筛表——空树没有手写条目，属预期）。
- **未做**：1.20.1 分支本身还没铺数据、没补 M2；生成器产物里的 `render_type` 等**资产**差异不在
  本批范围（P2 只管 `data/`）。

## 更早（2026-11-01，P1 薄门面）

- **分层落地**：新增顶层 `platform/` 包，只放每版要重写的胶水
  （`NeoForgeModProbe`、`config/ModConfig`、`machine/{MachineEnergy,MachineItemHandler,MachineHooks}`）。
  core 里新增窄门面 `common/config/{ConfigSource,ConfigValues}`、`common/platform/{ModProbe,Platform}`；
  判断标准：**1.20.1 会重写的文件才进 `platform/`**。
- **ConfigValues**：`platform/config/ModConfig` 实现 `ConfigSource`（仍然每次实时读 spec），
  构造期 `install` 进门面。坩埚/机器 10 个调用点改读 `ConfigValues.*`，热改行为不变。
- **Platform / ModProbe**：`OreMaterials` 不再 import `ModList`，改为 `OreMaterials.init()`
  （mod 构造期、注册之前）+ `Platform.isModLoaded`；`MoonlightCompat` 与机器 GUI 的 JEI 探测也走 `Platform`。
- **机器能量拆两半**：`MachineEnergyBuffer`（core，纯整数缓冲数学）+
  `platform/machine/MachineEnergy`（`EnergyStorage` 适配器，创建时绑定 buffer）；BE 只碰 buffer。
- **机器能力门面**：自动输出改调 `MachineHooks.insertIntoNeighbour(...)`；
  `MachineItemHandler` / `MachineEnergy` 移到 `platform/machine`，`CapabilityEvents` 从新位置注册。
- **GuiLayers**：`client/GuiLayers.push/pop` 包住 `Minecraft#pushGuiLayer`，自动输出抽屉不再直接调平台 API。
- **守卫清零**：`check-core-imports.py` 的 `KNOWN` 4 → 0；新增 `common/config`、`common/platform`
  两个受检目录，`common/machine/blockentity/MachineBlockEntity.java` 仍单独受检。顶层 `platform/`
  按约定不扫描（它就是每版重写的层）。
- **服务端 RCON 已验证**：电动筛 217 tick 从 20000 FE 掉到 15660（正好 20 FE/t，配置门面生效）、
  产物 1 秒内推进下方箱子（`MachineHooks`）、下方漏斗只抽到输出格（`MachineItemHandler`，
  筛网/沙/输入都留在机器里）；`[materials] loaded 12 entries`，无 ERROR。
- **未验**：纯结构重构，画面/手感预期不变，但**客户端没进游戏看**（自动输出抽屉仍走同一个
  `pushGuiLayer`，只是多包了一层）。

## 更早（2026-11-01，双版本 / supported mods / 移植审计）

- **supported mods**：`materials.json` 的 `ingots` 有序候选（`{"item": ..., "mod": ...}`），
  启动时取第一个已加载模组的锭，都没装就回退自有锭；熔炼配方按 `neoforge:mod_loaded` 生成，
  base 配方用 `neoforge:not(...)` 兜底。首个接入 **Mekanism 1.21.1**（tin / lead / osmium / uranium）。
  服务端已验证：有 Mek 时这 4 种自有锭不注册、熔炉产出 `mekanism:ingot_*`；无 Mek 时回退自有锭。
- **双版本恢复**：`1.20.1-forge` 分支从 `e0bc177` 恢复并推送；`legacy/1.20.1-forge` tag
  本地+远端删除；AGENTS/status/README/parity/DESIGN 全部改成双版本（复盘见
  [incidents/2026-11-01-unauthorized-archive.md](incidents/2026-11-01-unauthorized-archive.md)）。
- **移植审计**：[porting.md](porting.md) 记录分层契约、1.20.1↔1.21.1 API 差异、P0–P4 顺序；
  `scripts/check-core-imports.py` 守住 core（4 个已知漏点，新增 NeoForge import 直接失败）。
- **下一批从 P1 开始**（见「下一步」）；画面验收（M4/M6/M10 + 自动输出 GUI）仍然待做。

## 更早（2026-11-01，M10 自动输出配置）

- **自动输出**：机器新增 `AutoOutput`（总开关，默认关）+ `OutputMask`（6 个面各一位，默认只推底面）。
  每完成一次操作立刻推一次，另有 20 tick 重试；邻居拒收的余量留在输出格。
- **只管自动弹出**：掩码不影响 `MachineItemHandler`，漏斗/管道仍可从任意面抽输出格，
  已有的「下方漏斗抽货 / 上方漏斗补料」两套摆法不用改配置。
- **配置 GUI**：机器面板**左侧突出的标签页**（22×28，16×16 三帧图标：常态/悬停/开启）→ 点开后
  配置面板从面板左边缘向**左伸出**（132×168，齐平、带淡阴影、不铺全屏黑底）。
  6 个面按钮按**玩家相对方向**标注：上/下/前/后/左/右（两列三行成对排列），世界方向只放 tooltip；
  映射在打开时用 `player.getDirection()` 冻结。至少点亮一个面，取消最后一个面会自动关总开关。
- **筛料渲染修复**：BER 料层原来按 0.875 整块铺（1..15 px），穿过托盘内壁并与盘壁外侧面
  z-fighting；改为 x/z 0.6（3.2..12.8）、底面 10.5（网格 10.35），四周留缝。
- **木桶竖棱修复**：前后墙 1×15 的外侧端面原来带 `rotation: 90`，1 格宽 UV 矩形被旋转后
  木纹压成 15:1 细条纹（作者反馈「柱面的棱怪怪的」）；去掉这 4 个面的 rotation，
  现在是干净的 1px 木纹边（12 种桶 + 石桶共用 `barrel_base`）。
- **机器可镐挖**：四台机器补进 `#minecraft:mineable/pickaxe` + `#minecraft:needs_stone_tool`
  （原来只有 IRON_BLOCK 的 `requiresCorrectToolForDrops`，不在镐标签里 → 镐挖不掉方块）。
- **同步**：`ContainerData` 新增 `DATA_AUTO_OUTPUT(8)` / `DATA_OUTPUT_MASK(9)`（总 10 项）；
  改动走 C2S `exnihilosatellitium:set_machine_output`（绝对值，服务端 `stillValid` 8 格校验）。
- **RCON 已验证**：电动筛向下推、关掉后产物留槽、改推上方、无邻居时放箱子 20 tick 内补推、
  内能筛同配置——5 项都通过（见验证表）。**配置 GUI 第二版的画面/点击未目视**
  （作者看过首版：面按钮别用东南西北、入口别放内部、筛料穿模）。
- **supported mods 框架（2026-11-01）**：`materials.json` 新增 `ingots` 有序候选
  （`{"item": ..., "mod": ...}`）。启动时取第一个已加载模组的锭，都不在就注册自有锭；
  生成脚本给每个候选写 `neoforge:mod_loaded` 条件的熔炼配方，base 配方用
  `neoforge:not(...)` 兜底。首个接入 **Mekanism 1.21.1**：tin / lead / osmium / uranium
  熔成 `mekanism:ingot_*`（自有锭在那 4 种里不再注册）。Thermal 只有 1.20.1，接在 1.20.1 分支。
  图标三帧预览：[m10-output-button.png](images/m10-output-button.png)。

## 更早（2026-11-01，M10 自动化）

- **四台机器**：`electric_sieve` / `electric_hammer`（FE，4 个插件槽）与
  `fuel_sieve` / `fuel_hammer`（烧原版熔炉燃料，无插件槽）；共享一个 `MachineBlock` + 两个 BE 类型。
- **判定单一来源**：新增 `SieveRolls` / `HammerRolls`；手动筛（`SieveBlockEntity`）、
  手动锤（`ToolEvents` 普通/创造路径）已全部改调它们，机器不再有第二份掉落逻辑。
- **数值**：基础 100 tick（5 秒）/ 件；电动基础 20 FE/t、缓冲 48000；内能 200 燃烧值/件（1 煤 = 8 件）；
  速度 ×1.5/×2.5/×4，时运 = 额外 1/2/3 次整表 roll；插件按**物品数量**叠加（每个物品堆叠上限 8，速度/时运各自一个专用槽；**自动锤时运槽上限 1**）；速度每多一个效果衰减 20%，时运每多一个生效几率减少 20%（100/80/64%…），每个物品 +25% FE/t。全部走 `machine.*` config。
- **贴图/粒子**：机器方块升级到 32×32；工作时筛子冒尘、锤子冒火花/烟（电动蓝火花、内能火焰）。
- **GUI**：两个 Menu + 两个 Screen，进度/能量/火焰三块仪表改用贴图渐变（进度是箭头形），
  空槽有物品虚影（筛网/锤、原料、燃料、速度/时运），标题右侧实时状态
  （运行中/缺少工具/原料/能量/燃料/输出已满）；176×186，玩家物品栏下移，`物品栏` 不再压住插件槽。
  贴图由 `scripts/generate_machine_assets.py` 生成。
- **管道**：`MachineItemHandler` 只允许从输出 12 格抽取，避免漏斗抽走筛网 / 锤 / 插件；
  电动机器只进不出的能量能力。
- **JEI**：四台机器注册为筛/锤配方催化剂；点击 GUI 进度条用当前输入打开 JEI（空输入则打开分类）。
- **RCON 验证**：内能筛 / 电动筛 / 电动锤（速度 III + 时运 III）/ 内能锤 / 上下漏斗全部通过（见验证表）。
- **已验收（作者，2026-11-01）**：四台机器 GUI/仪表/虚影/状态行、筛子与锤子的 32×32 模型、
  工作动画（锤头下压、筛料薄层）、粒子、JEI 点击、shift-click 槽位规则。
- **未验**：机器配方未实际合成；自动输出配置 GUI 的画面/点击（服务端行为已 RCON 验过，见当前批次）。

## 更早（2026-11-01 提交，M6）

- **巫水**：自定义流体 + 桶 + 液方块；满水桶 + 菌丝（或蘑菇煲）发酵，`Spore` 进度存 NBT。
- **古代孢子**：沙筛 1/128；右键泥土 → 菌丝、右键牛 → 哞菇。
- **瓷桶**：3 瓷土按原版桶形 → 未烧制瓷桶 → 烧制 → 通用 1000 mB 容器（`fluid_contents` 组件 + `IFluidHandlerItem`）。
- **木坩埚**：11 种木材（含 1.16 菌柄 / 1.20 红树·樱花·竹），0.5 mB/t 被动熔树叶/树苗 → 水，只收水类熔料。
- **木桶**：补齐到 12 种（11 木材 + 石桶），删掉两道外凸箍。
- **蚕**：`silkworm` 右键树叶 → 侵染树叶（BE 记住原树叶、按进度变白、60% 后扩散、破坏掉线），Jade 显示进度。
- **人偶**：5 种（人偶 / 愤怒 / 诡异 / 漂浮 / 守卫者），8 条配方；满桶右键**立即**转换：
  和平掉战利品（烈焰棒 / 末影珍珠 / 潜影壳 / 海晶碎片或海晶砂粒随机），非和平直接生成生物。
- **筛出物**：泥土 → 草种子/甜浆果；沙 → 可可豆/海带/5 种珊瑚/海晶砂粒/海晶碎片；锤下界岩 → 粉碎下界岩方块（筛石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙）、
  锤末地石 → 粉碎末地石方块（筛紫颂果/末影珍珠）。
- **标签**：删掉 `c:raw_materials`，只保留 `c:ores`（矿砾/矿砂/矿尘方块）。
- **贴图**：人偶采用 Creatio（MIT）底稿重制为 64×64；粉碎方块从原版 netherrack/end_stone 派生。

## 功能现状

### 桶 / 堆肥 / 筛子

- 十二种桶（11 种木材 + 石桶）共享一个 BE；木桶 ≤433 K，石桶不限温；堆肥 1000 分制、50 秒成土；
  接雨水、比较器、破坏返还、管道状态互斥都已实现。
- 桶转化 5 条（水+熔岩、熔岩+水、熔岩+红石、熔岩+荧石、水+尘土→粘土）；
  筛子 4 输入 / 51 掉落（泥土 → 种子/作物，沙 → 仙人掌/甘蔗），三档筛网只影响耐久。
- 矿物链：4 碎块 → 矿方块 → 熔炼成锭；锤矿砾/矿砂掉 4–6、粉尘掉自身。
- **M6 补的筛出物**：泥土 → 草种子（右键泥土变草方块）/甜浆果；沙 → 可可豆/海带/5 种珊瑚/
  海晶砂粒/海晶碎片；锤下界岩 → **粉碎下界岩方块**（筛石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙）、
  锤末地石 → **粉碎末地石方块**（筛紫颂果/末影珍珠）。两者都是方块类筛料，走原来的方块料层渲染；
  `SieveRenderer` 仍保留了非方块输入的图标兜底。

### 锤 / 钩

- 6 档锤（木/石/铁/金/钻石/下界合金），耐久 64/128/256/32/1561/2031；金/钻石工作台，
  下界合金锻造台。
- **只有左键破坏**（无右键）；`HarvestCheck` + `BreakSpeed` 6.0 让锤表方块可收获且不慢；
  创造模式用 `BlockEvent.BreakEvent` 补掉落；耐久扣真实主手栈。
- 木/骨钩；骨钩叶表概率 ×1.5；蚕已接 M6（右键树叶 → 侵染树叶 → 线）。

### 坩埚（M4）

- 瓷土（粘土球+骨粉）→ 7 瓷土合未烧制坩埚 → 熔炉烧成坩埚。
- 19 条熔点：圆石/石头/砂砾/花岗岩/闪长岩/安山岩（含磨制、深板岩、凝灰岩、方解石）250 mB，
  下界岩 1000 mB，雪块 500 mB 水，冰 1000 mB 水，沙子/红沙/尘土 100 mB。
- 熔料体积 = 产出量（1:1）；默认熔料上限 2000 mB（8 块圆石）。
- 热源必须在正下方；`lit=true` 区分点燃/熄灭的熔炉、篝火。

### 巫水（M6，2026-10-31）

- 自定义流体 `exnihilosatellitium:witch_water`（still/flowing + `FluidType` + 桶 + 液方块），
  紫色贴图由 `art/blocks/witch_water/generate.py` 生成。
- **古代孢子**（`ancient_spores`，紫色）：沙筛 1/128；右键泥土 → 菌丝；右键牛 → 哞菇（`common/item/AncientSporesItem`）。
- **发酵**：满水桶 + 3×3×3 内有菌丝，每 tick 进度 `+1 + 菌丝数/2`，满 1000 tick（1 个菌丝 50 秒）
  整桶变巫水；直接递蘑菇煲可无菌丝起酵（同样 50 秒，退回碗）。
- **巫水转化**两条：+沙子 → 灵魂沙；+橡树苗 → 深色橡树苗（`barrel/witch_water_*.json`）。
- **木坩埚**（Creatio 的 Wooden Crucible）：**11 种木材变种**（橡木 `wooden_crucible`、云杉、白桦、丛林、
  金合欢、深色橡木、红树、樱花、竹，以及下界的绯红/诡异菌柄），各用**自己的原木** + **任意木台阶** 合成；
  **不需要热源**，按 0.5 mB/t 被动熔化（一块树叶 ~10 秒），只收水类熔料（树叶 100 mB、树苗 250 mB）；
  底下有没有热源都一样，装熔岩会被拒（433 K 上限，和木桶一致）。
- **蚕 / 侵染树叶**（M6）：`silkworm` 右键树叶 → `infested_leaves`（BE 记住原树叶外观）；
  进度 0.0005/tick（100 秒满），超过 60% 后每 100 tick 向 3×3×3 内的树叶扩散一次；
  破坏时按进度掷 `线`（progress 概率 + 1/4 再次判定；**创造模式不掉落**）。渲染 = BER 画回原树叶，
  用 `WhiteningVertexConsumer` 把顶点色按进度混向白色（保留树叶纹理，不再盖白盒）+ 白灰粒子；
  **Jade** 显示原树叶名与侵染百分比。物品形态 `infested_leaves` 带 `infestation` 组件：
  创造栏里 0% 是「侵染中的树叶」、100% 是「被侵染的树叶」，放下即按组件设进度。
- **人偶**（M6）：`doll`（7 瓷土 + 钻石/绿宝石）→ `angry_doll`（烈焰粉×4 + 下界疣 + 荧石×2 + 红石）、
  `creepy_doll`（墨囊×4 + 下界疣 + 青金石×2 + 红石），两者都有「红石↔下界疣」互换的镜像配方；
  `shulker_doll` 漂浮人偶（紫染料×4 + 末地石 + 荧石×2 + 末影珍珠）、
  `guardian_doll` 守卫者人偶（海晶砂粒×4 + 红石 + 荧石×2 + 生鲑鱼）。
  桶规则：熔岩+愤怒→烈焰人、巫水+诡异→末影人、**巫水+漂浮→潜影贝**、**水+守卫者→守卫者**，
  **右键立即生效**（不再有 30 秒烹饪）：非和平直接生成生物，和平掉烈焰棒 / 末影珍珠 / 潜影壳 /
  海晶碎片或海晶砂粒（随机），桶恢复空桶。
- **瓷桶**（`porcelain_bucket`，物品）：通用 1000 mB 容器，能装熔岩/巫水/牛奶；
  内容存 `exnihilosatellitium:fluid_contents` 组件，`IFluidHandlerItem` 已接。
  3 瓷土按原版桶形（`x x` / ` x `）→ 未烧制瓷桶 → 熔炉烧 200 tick → 可用瓷桶。

### 自动化（M10，2026-11-01）

- 四台机器共用 `MachineBlock`（kind = 筛/锤，power = 电动/内能），BE 分 `ElectricMachineBlockEntity` /
  `FuelMachineBlockEntity`，GUI 分 `ElectricMachineMenu` / `FuelMachineMenu`。
- 槽位：`0` 工具、`1` 输入、输出 12 格（3×4）；电动另有 `2..5` 插件，内能 `2` 燃料。
- 工具消耗：筛网每件 −1 耐久、锤每件 −1 耐久，坏掉即消失；输出满时停机，剩余产物掉在机器上方。
- 电动无电 / 内能无燃料时停机，`Work` 归零，不消耗输入。
- 管道：下方漏斗只抽输出格；上方漏斗可补料。电动机器暴露只进不出的能量能力，内能机器没有能量能力。
- 内能燃料接受任何 `getBurnTime(RecipeType.SMELTING) > 0` 的物品；熔岩桶退回空桶。
- **自动输出**：`AutoOutput` + `OutputMask`（默认关、默认只推底面）；完成一件立刻推，20 tick 重试；
  配置入口是机器面板左侧突出的标签页，点开的配置面板也向左伸出（`MachineOutputScreen` 叠加层），
  不改菜单；面按钮用玩家相对方向（上下前后左右）。掩码只管自动弹出，管道/漏斗仍可从任意面抽输出格。

### 配置与 JEI

- `config/exnihilosatellitium-common.toml`：`crucible.capacity`、`crucible.meltCapacity`、
  `crucible.heatSpeedMultiplier`，运行时读取。
- `machine.ticksPerOperation` / `energyPerTick` / `energyCapacity` / `burnPerOperation`（M10）。
- JEI 分类：`sieve`、`barrel`、`barrel_compost`、`hammer`、`crook`、`crucible`、`crucible_heat`；
  锤子页隐藏“粉尘→自身”的 no-op 规则（玩法 47，展示 35）。

## 交接清单（下次会话先做）

- **1.20.1 分支**：`/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium-1.20.1`
  （`1.20.1-forge` @ `6a733e8`，含一次性验收清单），已到 **M10**（含 `crucible.*` + `machine.*` config），
  下一步只剩一次性画面/手感验收。
  分支的 `docs/status.md` 里有当前批次、验证表、构建/客户端命令（`runClient -PdevMods`）
  和固定套路。新会话在分支工作区开工即可。

- **先读** [porting.md](porting.md) §3 的 P2 记录；本批从 **1.20.1 分支补 M2** 开始（清单见「下一步」）。
- **先跑** `./gradlew build` + `python3 scripts/check-textures.py` + `python3 scripts/check-core-imports.py`
  + `python3 scripts/check-data-paths.py`（后两条必须 `0 known leak(s)` / `passed for 1.21.1`）。
- **datagen 自检**：`./gradlew runData --offline` 后 `git status` 应为空（已迁移的两批稳定）；
  配方要重新比对时用 `python3 scripts/compare-recipes.py --expect-old-only '*_mekanism.json'`。
- **数据管线验收（不需要进游戏）**：
  `python3 scripts/port_data.py --out build/port-1.20.1 --force` →
  `python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1`；
  四个生成器重跑不应产生 diff（`git status --short` 应为空）。
- 进游戏验收（服务端逻辑都已验过，剩下“看得见/摸得着”的）：
  - **M4 坩埚**：1 块圆石只占 12.5%、8 块满 2000、第 9 块被拒；沙子/尘土 100、花岗岩 250；
    桶/瓶抽流体；熔料↔流体显示切换；Jade 熔料剩余 + 热源速度；三个配置键热改；JEI 两页。
  - **M6 人偶**：5 种配方（含镜像摆法）；满桶右键立即出结果（和平掉战利品 / 非和平生成生物）；桶变空。
  - **M6 木坩埚**：11 种变种的外观与合成；树叶/树苗 → 水的手感（0.5 mB/t）；熔岩被拒的提示。
  - **M6 蚕**：右键树叶 → 侵染；变白进度；Jade 百分比；扩散；破坏掉线；创造模式不掉落。
  - **M6 瓷桶**：未烧制 → 烧制；舀水/熔岩/巫水；倒进木桶/坩埚。
  - **M3**：6 锤/2 钩、左键链、右键无反应、矿砾/矿砂 4–6、锤钩 JEI 35/10、蚕动画。
  - **M2/M6 筛子**：6 个输入、72 掉落；JEI 筛子页；粉碎方块平铺料层。
  - **M10 机器**：四台机器右键开 GUI；能量条/火焰条/进度条动；插件槽只收插件、燃料槽只收燃料；
    筛网/锤在 GUI 里显示耐久；破坏机器掉出全部内容；贴图观感；上下漏斗在游戏里再走一遍。
  - **M10 自动输出 GUI（第二版）**：左侧标签页的观感/悬停/变绿；配置面板从左边伸出、与面板齐平、
    不挡机器 GUI；6 个面按钮（上/下/前/后/左/右）与 tooltip 世界方向对得上；绿色边框选中态；
    关掉最后一个面会自动关总开关；Esc/E 返回；开/关后状态下发与 `data get` 对得上。
  - **筛料渲染**：新料层不再穿过盘壁、不再和盘边交替闪烁；变薄动画照旧（三种筛料各看一遍）。
  - **木桶竖棱**：木桶/石桶四条竖棱不再是细条纹；俯视/平视都看一遍。
  - **机器镐挖掘**：木镐不掉落、石镐及以上正常掉机器（含内容物掉落逻辑不变）。
- **未验证**：上面这些画面/交互，以及 P1 重构后的客户端实测（服务端已 RCON 验过）、桶/筛旧内容目视。
- 不要提交 `build/`、`run/`、`reference/`、`libs/`。

## 已完成的里程碑

| 里程碑 | 内容 | 记录 |
|---|---|---|
| M1 | 木桶：堆肥 1000 分制 → 泥土、1000 mB 流体、比较器、七材质、内缩模型 | [DESIGN §8/§10](DESIGN.md) |
| M1.5 | 水/熔岩凝固、木桶限温 433 K、接雨水、熔岩掺红石/荧石；转化表 JSON 化 | [DESIGN §13/§14](DESIGN.md) |
| M2 | 筛子 + 三档筛网 + 数据驱动掉落 + JEI；4 碎块 → 矿方块 → 熔炼成锭 | [DESIGN §12](DESIGN.md) |
| 桶 JEI | `barrel` / `barrel_compost` 两个分类，读同一张 `BarrelRecipes` / `CompostRegistry` | [DESIGN §14](DESIGN.md) |
| M3 | 6 锤 / 2 钩 / `dust` / 蚕动画 / 熟蚕 / 粘土 / 47 条锤表 / 锤钩 JEI | 🚧 待验收（[DESIGN §15–16、§18](DESIGN.md)） |
| M4 | 坩埚 + 热源；19 熔点 / 9 热源；容量可配；Creatio 模型；熔料/流体显示；Jade；JEI 两页 | 服务端✅ / 画面🚧（[DESIGN §17](DESIGN.md)） |
| M6 | 巫水 / 古代孢子 / 瓷桶 / 11 种木坩埚 / 12 种木桶 / 蚕 / 5 种人偶 / 筛出物扩充 | 服务端✅ / 画面🚧（[DESIGN §21–26](DESIGN.md)） |
| M10 | 电动/内能 × 自动筛/自动锤 + 速度/时运插件 + 自动输出配置；手动与机器共用判定函数 | ✅（GUI/模型/动画作者已验收；自动输出 GUI 待目视）（[DESIGN §27](DESIGN.md)） |

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-31：`BUILD SUCCESSFUL`；JEI 依赖走 `maven.blamejared.com` |
| `python3 scripts/check-core-imports.py` | ✅ | core 层无 NeoForge import；4 个已知漏点（OreMaterials + 3 个机器平台文件）以警告列出 |
| `python3 scripts/check-textures.py` | ✅ | 方块/物品 16×16、蚕动画 16×64 + mcmeta、坩埚贴图存在；无 `minecraft:item/*` 引用 |
| 数据表 JSON | ✅ | 47 hammer / 10 crook / 19 melt / 9 heat / 5 barrel / 65 compost / 4 sieve；全部可解析 |
| 客户端启动（最后一次） | ✅ | 2026-10-31 00:16：`[hammer] 47`、`[crook] 10`、`[crucible] 6`、`[jei] 47/6/5/3/10`；无 missing model/texture |
| 客户端启动（当前代码） | ✅ | 2026-10-31 09:26 客户端日志：`[jei] 35 hammer` / `19 meltables, 9 heat sources` / `5 barrel, 65 compost` / `4 sieve` / `10 crook`；`[sieve] 4 inputs, 52 drops`；无 missing model/texture |
| 桶：堆肥→泥土 | ✅ | 2026-10-29：苹果 +100 / 仙人掌 +100 / 树叶 +125，满 1000 后 `dirt=true` |
| 桶：流体装填 | ✅ | 2026-10-29：岩浆桶倒入成功；石桶 NBT 读回 `minecraft:lava` / 1000 |
| 桶：水+熔岩 / 熔岩+水 | ✅ | RCON：圆石 / 黑曜石两条 Solid 反应通过 |
| 桶：接雨水 | ✅ | `/weather rain` 12 秒积水 80 mB |
| 桶：木桶拒热 | ⚠️ | `tank.fill` 按 433 K 拒绝；消息/手感未实测 |
| 桶：红石/荧石掺料 | ⚠️ | 已进 `barrel/*.json`；未玩家实测 |
| 桶：流动水 → 黑曜石 | ✅ | RCON：满熔岩桶，上方放水源、桶顶格是 `water[level=8]` 流动水，1.5 秒后 `Solid=obsidian`（`FlowingFluid#getSource()` 归一化） |
| 桶：水+尘土→粘土 | ⚠️ | JSON 已写；未实测 |
| 桶：水+牛奶→史莱姆/粘液球 | ✅ | RCON 2026-11-02：满水 + 牛奶桶 → 桶清空、空桶返还；Normal 生成史莱姆、Peaceful 掉 `slime_ball` |
| 巫水：满水 + 菌丝发酵 | ✅ | RCON：满水木桶 + 2 菌丝，约 25 秒变 `exnihilosatellitium:witch_water`，`Spore` 归零 |
| 巫水：蘑菇煲起酵 | ✅ | RCON：只写 `Spore:1` 无菌丝，约 50 秒后变巫水 |
| 巫水：流体方块 | ✅ | RCON `setblock ... exnihilosatellitium:witch_water` 成功并可被 `execute if block` 命中 |
| 巫水：桶装/倒 | ⚠️ | `witch_water_bucket` 已注册；未玩家实测 |
| 巫水：两条转化 | ⚠️ | JSON 已加载（`[barrel] 7 transforms`）；未玩家实测 |
| 古代孢子：沙筛 | ⚠️ | `sieve/sand.json` 1/128；未实测 |
| 古代孢子：泥土→菌丝 / 牛→哞菇 | ⚠️ | 逻辑已写；未实测 |
| 巫水：画面/桶纹理 | ⚠️ | 流体用原版 water_still/flow + 紫色 tint；桶图标由原版水桶重着色；未进游戏目视 |
| 木坩埚：被动熔化 | ✅ | RCON：放树叶 10 秒 100 → 1.0 固体 / 99 mB 水（≈0.5 mB/t）；底下加篝火速率不变 |
| 木坩埚：11 种木材变种 | ✅ | RCON：11 个变种 `setblock` 后 `execute if block` 全部命中；生成脚本 `scripts/generate_wooden_crucible_variants.py` |
| 木坩埚：水类限定 | ⚠️ | `canAccept` 拒非水熔料、`fill` 拒 >433 K；未用熔岩桶实测 |
| 瓷桶：物品/组件 | ✅ | RCON：`summon item` 带 `fluid_contents` 可读回熔岩 1000 / 巫水 1000 |
| 瓷桶：画面/舀水/倒液体 | ⚠️ | 模型 predicate、tint、右键舀水、倒进木桶/坩埚均未玩家实测 |
| 瓷桶：合成/烧制 | ⚠️ | 3 瓷土（`x x` / ` x `）→ 未烧制瓷桶 → 烧制；未实测 |
| 桶 JEI | ✅ | 转化 1/2 页、堆肥 1/22 页已目视；[截图](images/jei-barrel-transform.png) |
| 筛子（1.21.1） | ✅ | 作者已实际筛出产物；2026-10-31 服务端日志 `[sieve] loaded 4 inputs, 52 drops`（泥土/沙两档新增未实测） |
| 筛子 JEI | ✅ | 7×3 + 166×60 已目视；[截图](images/jei-sieve.png)（还是 3 输入时的样子） |
| 筛子：泥土 → 种子/作物 | ⚠️ | `sieve/dirt.json` 6 条（小麦 15% / 甜菜·土豆·胡萝卜 6% / 西瓜·南瓜 5%）；服务端已加载，未实测 |
| 筛子：沙 → 仙人掌/甘蔗 | ⚠️ | `sieve/sand.json` 补 2 条（1/32、1/24）；未实测 |
| 筛子：M6 新资源 | ✅ | 服务端 `[sieve] 6 inputs, 72 drops`、`[hammer] 49 recipes`；粉碎下界岩/末地石可放置并掉自己 |
| 蚕：侵染树叶 / 扩散 | ✅ | RCON：进度 0.65 → 0.849（20 秒，0.0005/tick 对得上）；20 秒内 4/8 邻近树叶变侵染 |
| 蚕：右键转化 / 掉线 | ⚠️ | 逻辑已写；需玩家右键树叶实测、破坏看掉落 |
| 蚕：渲染 / 变白 | ⚠️ | BER 画回原树叶 + 白色遮罩（alpha 随进度）+ 白灰粒子；未目视 |
| 人偶：烹饪生成 | ✅ | RCON：写入 `CookEntity` 后非和平模式刷出烈焰人；和平模式掉烈焰棒 |
| 人偶：配方 / 桶规则 | ⚠️ | 8 条配方 + 4 条桶规则已加载（`[barrel] 11 transforms`）；未玩家实测 |
| 人偶：潜影贝 / 守卫者 | ✅ | RCON：Normal 难度刷出潜影贝/守卫者；和平掉落列表随机取一个（实测海晶砂粒/海晶碎片各一次） |
| 人偶：右键链路 | ✅ | 用 FakePlayer 走真实 `use()`：满水桶 + 守卫者人偶 → `use=SUCCESS`、`CookTicks=600`、`CookEntity=guardian`；30 秒后和平模式掉海晶碎片 |
| 人偶：即时转换 | ⚠️ | 现在任何难度都是右键立即生效（和平掉落 / 非和平生成）；`cook_ticks`/Jade 倒计时只给旧存档兜底，未再实测 |
| 蚕：Jade | ⚠️ | 显示 `原树叶：…` + `侵染：xx%`（成熟后显示「已成熟」）；未目视 |
| 粉碎下界岩/末地石：外观/筛 | ⚠️ | 方块已注册；未进游戏看贴图、未实际筛 |
| 草种子 | ⚠️ | `grass_seeds` 右键泥土 → 草方块；未实测 |
| 矿物链 | ⚠️ | 4 块→方块→熔炼已 RCON 通过；锤降档未玩家实测；9 锭未目视 |
| M3 锤/钩玩法 | ⚠️ | 左键链路/耐久/右键无反应/4–6 掉落未实测 |
| M3 贴图 | ✅ | 9 张 Creatio 采用 + 1 张换色 + 蚕 4 帧动画；[预览](images/m3-textures.png) |
| M3 收尾 | ⚠️ | 粘土/砂岩/熟蚕 JSON 已写，未实测 |
| M4 坩埚方块 | ⚠️ | 两个方块 + BE + 流体 capability + 比较器；仅右键投放/抽流体未实测 |
| M4 熔点/热源表 | ✅ | 19 + 9 条 JSON；服务端启动日志 `[crucible] loaded 19 meltables` / `9 heat sources` |
| M4 熔化速率 | ✅ | RCON 实测：篝火 0.25、点燃熔炉 0.15、熄灭熔炉 0（`lit=false` 不熔）；240 tick 内实测值与 `speed × tick` 差 ≤1 mB |
| M4 容量钳制 | ✅ | RCON：熔料 250 + 流体 7900 → 熔到 150/8000 停住；流体 8000 时熔料不动 |
| M4 比较器 | ✅ | RCON：7604/8000 → 信号 14，红石灯亮；100/8000 → 信号 0，灯灭 |
| M4 熔料/流体显示 | ⚠️ | 固体 > 流体显示熔料方块，反之流体；未目视 |
| M4 Jade | ⚠️ | 熔料剩余 + 热源速度；未目视 |
| M4 配置 | ✅ | 热改已验：`capacity` 8000→3000 下一秒内把 8000 mB 压到 3000；`heatSpeedMultiplier` 2.0 → 实测 0.5 mB/t |
| M4 JEI | ⚠️ | 坩埚/热源两页；未目视 |
| 瓷土/坩埚配方 | ⚠️ | 瓷土、未烧制坩埚、烧制；未实测 |
| 锤/钩 JEI | ⚠️ | 35 / 10 条 + catalyst；未目视 |
| M10 内能筛 | ✅ | RCON：120 tick 后输入 64→63、筛网 damage 1、`BurnHalfTicks` 3200→2600、产出燧石 |
| M10 电动筛 | ✅ | RCON（旧 4 秒档）：`Energy:20000`，101 tick 后 17980（20/tick 对得上）、输入 63、产出铝碎块/海晶结晶；5 秒/48000 档待重测 |
| M10 电动锤 + 速度 III + 时运 III | ✅ | RCON：5 秒 5 次（20 tick/次）、输入 64→59、锤 damage 5、20 砂砾（1×4 roll×5） |
| M10 内能锤 | ✅ | RCON：80 tick 一次、输入 63、锤 damage 1、`BurnHalfTicks` 2700、1 砂砾 |
| M10 管道 | ✅ | RCON：下方漏斗只抽到砂砾，锤/插件仍在；上方漏斗把圆石补进输入槽 |
| M10 内能筛（存档实证） | ✅ | 2026-11-01 存档 `新的世界`：`(198,72,-489)` 丝筛网 damage 12、粉碎末地石 52、煤 62，
  产出末地石×5 / 紫颂果×1 / 末影珍珠×2，`BurnHalfTicks` 1470——机器实际已处理 12 次 |
| M10 GUI / 仪表 / JEI | ✅ | 作者已进游戏确认：虚影/状态行/三块仪表/点箭头开 JEI/时运槽规则 |
| M10 模型 / 贴图 | ✅ | 作者确认：筛子薄托盘 + 锤子四柱压锤，32×32 贴图；预览图见 `docs/images/m10-*.png` |
| M10 工作动画 / 粒子 | ✅ | 作者确认：锤头下压铁砧、筛料薄层变薄、电动蓝火花/内能火焰烟 |
| M10 5 秒 / 48k / 叠加 | ✅ | RCON：无升级 124 tick → 1 次 + Work24、20 FE/t；2 个速度 III（同槽 count2）→ 7 次 + Work12（16 tick/件）、25 FE/t |
| M10 配方 | ⚠️ | 四台机器 + 6 个插件配方已注册，未实际合成 |
| M10 电动筛配方 | ⚠️ | 2026-11-01 按作者要求换成 `IRI / RBR / ISI`（铁锭角 + 红石 ×3 + **铁块** + 筛子底），替换原来 `IRI / RSI / IRI`；服务端 1423 条配方加载无报错，JEI 未目视 |
| supported mods（Mek 在场） | ✅ | 服务端加 Mekanism 10.7.19.85：日志 `tin/lead/osmium/uranium smelts into mekanism:ingot_*`；`summon` 自有 `tin_ingot` 得到 Air（未注册），`mekanism:ingot_tin` 正常；1423 → 3001 条配方（Mek 自带 + 我们的 12 条变体） |
| supported mods（Mek 不在） | ✅ | 无 Mek：1423 条配方照旧；熔炉烧 `tin_ore_gravel` 产出 `exnihilosatellitium:tin_ingot`（fallback 条件生效） |
| supported mods（Mek 在场熔炼） | ✅ | 有 Mek：熔炉烧同一块 `tin_ore_gravel` 产出 `mekanism:ingot_tin` |
| supported mods（JEI/客户端） | ⚠️ | 服务端已验；JEI 里的熔炼页与创造栏未在客户端目视 |
| M10 自动输出（下推） | ✅ | RCON 2026-11-01：`AutoOutput=1/OutputMask=1` + 下方箱子，一件后箱子拿到 `iron_crushed`、输出格清空 |
| M10 自动输出（关闭） | ✅ | RCON：`AutoOutput=0` 后产物留在槽 6..10，下方箱子不再增长 |
| M10 自动输出（换面） | ✅ | RCON：`OutputMask=2`（只推上）→ 缓冲 5 件 + 新 1 件全进上方箱子，下方不变 |
| M10 自动输出（20 tick 重试） | ✅ | RCON：无邻居时 3 燧石留槽；放箱子后**无新操作**，20 tick 内推进箱子 |
| M10 自动输出（内能筛） | ✅ | RCON：`fuel_sieve` 同配置 4 次产出 3 件进下方箱子（1 次 roll 空手） |
| M10 自动输出 GUI | ⚠️ | 首版（内嵌按钮 + 东南西北）作者反馈：面按钮方向不直观、入口位置不对；已改为左侧标签页 + 左侧抽屉 + 相对方向，**待目视** |
| 筛料渲染溢出 | ⚠️ | 作者截图确认料层与盘壁交替渲染；已改为 0.6 宽 + 抬到 10.5 px，**待目视** |
| 木桶/石桶竖棱 | ⚠️ | 作者反馈棱上渲染怪；已去掉 4 个 1×15 端面的 `rotation:90`（离线渲染对比确认条纹消失），**待目视** |
| 机器镐挖掘 | ⚠️ | 已加 `mineable/pickaxe` + `needs_stone_tool`（石镐及以上掉方块），**待游戏内挖一次确认** |
| 1.20.1 M2 build | ✅ | `1.20.1-forge` @ `9b92322`：`./gradlew build`（Temurin 17）通过 |
| 1.20.1 M2 筛子结构 | ✅ | RCON：`setblock ... sieve`、`[mesh=0/3]` 命中、`data merge` 的 `Mesh`/`Contents` 读回 |
| 1.20.1 M2 数据 | ✅ | `[sieve] loaded 3 inputs, 48 drops`；`[compost] loaded 65`；83 个 recipe JSON |
| 1.20.1 M2 锭物品 | ✅ | RCON `summon item ... tin_ingot` → `Tin Ingot`（9 种自有锭） |
| 1.20.1 M2 右键筛料 | ⚠️ | 需要玩家右键（客户端已启动）；未由 agent 实测 |
| 1.20.1 M3 表加载 | ✅ | `[hammer] 47`、`[crook] 10`、`[sieve] 4 inputs 62 drops` |
| 1.20.1 M3 物品 | ✅ | RCON summon 锤/钩/尘土均成功，`dust` 方块可放置 |
| 1.20.1 M3 手感 | ⚠️ | 锤左键链路/耐久、骨钩 ×1.5 待玩家 |
| 1.20.1 M4 坩埚 | ✅ | 表加载 39/9；RCON 篝火 0.25、熄灭停、火把 0.1；JEI 两页注册 |
| 1.20.1 M4 交互 | ⚠️ | 右键投料/取流体、Jade 画面待玩家 |
| P2 数据守卫 | ✅ | `python3 scripts/check-data-paths.py` → `passed for 1.21.1 (145 recipes, 69 loot tables, 41 tags)` |
| P2 生成器幂等 | ✅ | 1.21.1 四个生成器写入仓库副本后 `diff -r` 与仓库逐字节一致（含 lang/筛表） |
| P2 移植数据 | ✅ | `port_data.py`：145 recipe / 69 loot / 41 tag / 125 其他；`check-data-paths --target 1.20.1` 通过 |
| P2 生成器 1.20.1 | ✅ | `--target 1.20.1 --out`：107 recipe / 60 loot / 39 tag 通过守卫；与 port 产物 240 文件逐字节一致 |
| P2 1.20.1 分支 | ⚠️ | 分支本身还没铺数据、没补 M2 |
| M7 datagen 第二批（配方） | ✅ | 2026-11-02：`ModRecipes` 生成 133 条；`compare-recipes.py` → `145 old, 133 generated, 12 old-only (0 unexpected), 0 new-only, 0 different` |
| M7 datagen 幂等 | ✅ | 删掉 133 条手写配方后再跑 `runData`：`HashCache ... written: 0`，没有旧文件复活 |
| M7 datagen 第二批（服务端） | ✅ | `runServer`：`Loaded 1423 recipes`（与迁移前同）、`[debug] our recipes=133`、无 recipe 解析错误 |
| M7 配方语义 | ✅ | 生成物只多出 codec 默认字段（`count:1` / `category:"misc"`）；没有 advancement 文件 |
| M7 datagen 移植链路 | ✅ | `port_data.py` 现在合并 main + generated：145 recipe / 69 loot / 41 tag / 126 other，`check-data-paths --target 1.20.1` 通过（只读 main 的旧默认会漏 133 配方） |
| P1 core 守卫 | ✅ | `python3 scripts/check-core-imports.py` → `core purity check passed (0 known leak(s))` |
| P1 服务端启动 | ✅ | 2026-11-01 `runServer`：`[materials] loaded 12 entries`，`our recipes=133`，无 ERROR |
| P1 ConfigValues | ✅ | RCON：电动筛 217 tick，`Energy` 20000→15660（正好 20 FE/t） |
| P1 MachineHooks（自动输出） | ✅ | RCON：`AutoOutput=1/OutputMask=1` + 下方箱子，产物 1 秒内进箱子 |
| P1 MachineItemHandler（漏斗） | ✅ | RCON：下方漏斗只抽走 3 燧石，铁筛网/沙/输入仍留在机器 |
| P1 客户端 | ⚠️ | 只编译与跑服务端；画面未进游戏看（P1 不改渲染） |
| P0/P1 修复 | ⚠️ | 破坏返还、管道灌桶、F3+T 分离、红石/荧石未玩家实测 |
| 旧内容目视 | ⚠️ | 桶/筛比例、Jade tooltip 等仍待作者确认；桶的两道外凸箍已按反馈删除（2026-10-31） |

## 下一步（按价值排序）

1. **1.20.1 一次性验收 M1.5–M10**：服务端逻辑都已 RCON 验过，剩下机器 GUI/自动输出抽屉/
   BER 动画、M6 美术与右键手感；之后接 Thermal（它只有 1.20.1）。
2. **补 M4 / M6 / M10 的画面与右键验收**（含自动输出配置 GUI、筛料渲染、木桶竖棱、镐挖机器）：
   清单见交接清单。
3. **P3 状态机拆分**（P2 之后才有意义：`BarrelState` 等纯类 + 第一批 JUnit）。
4. **M7 余项**：datagen 第三批 = 模型/blockstate（施工单见 [datagen.md](datagen.md) §4）；
   语言默认保持 JSON。配方（第二批）已完成。config 与 JEI 已齐。
5. **工程收尾**：advancement 树、mod 图标、CI、零测试（tech-debt P3 #37）。
6. **M8 月光魔法联动** / **M9 发布**。

## 已拍板的决策

- **项目名 / 许可证**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`；
  MIT（Forge MDK 仍 LGPL-2.1），见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**：注册只认 jar 内 `materials.json`；表内材料有完整自有链；表外材料不自动发现。
- **方块贴图**：一般 vanilla-sampled；**坩埚例外**：采用 Creatio（MIT）模型/贴图并做模糊 + 5 色现代化，
  原始文件在 `art/blocks/crucible/creatio/`。
- **物品贴图**：自有或采用 Creatio（MIT）；Sequentia 是 CC BY-NC-SA，只作风格对照，不采用。
- **锤/钩**：6 锤 / 2 钩，左键唯一路径；粉粉尘→自身是安全兜底，不作为 JEI 配方展示。
- **坩埚**：熔料与流体分开记，熔料 1:1 产出流体；固体 > 流体显示熔料，反之流体；
  容量/熔料/热源倍率走 config，热源与熔点走数据包。**流动熔岩与源方块都按 0.2**
  （2026-11-02 所有者决定；`crucible_heat` 只按方块 id 匹配，不加 `level` 范围）。
- **坩埚被破坏时内容物一起消失**（只掉方块本体），不做返还：忠于 1.7.10 原版，
  也避免“先熔满再挖走”当免费流体袋。
- **数据包优先**：所有玩法表可覆盖/新增；单人 JEI 优先服务端表，专用服客户端回退内置镜像。
- **Jade 是可选依赖**：`compileOnly`，不打包；每个 provider 都要有 `config.jade.plugin_<ns>.<uid>` 翻译。
- **月光魔法是可选软依赖**：目前只占位。
- **分层契约（P1）**：`common/` 是 core（两版共用，可整包复制），顶层 `platform/` 是每版重写的胶水；
  core 只通过 `ConfigValues` / `Platform` / `MachineHooks` / `GuiLayers` 这几个窄门面碰平台，
  不用 `ModList` / `ModConfigSpec` / capability 类型。

## 踩过的坑

- **Jade provider 缺配置翻译会炸资源包**：`config.jade.plugin_exnihilosatellitium.<uid>` 必须有，
  否则 `AssertionError` 会导致 "removing all selected resourcepacks"。
- **`BlockDropsEvent#getTool()` 是复制栈**：扣耐久要扣 `player.getMainHandItem()`，扣事件里的工具无效。
- **`BlockDropsEvent` 创造模式不触发**：另挂 `BlockEvent.BreakEvent` 补掉落。
- **锤子不是镐**：不加 `HarvestCheck`/`BreakSpeed` 时 `canHarvestBlock` 会让方块直接掉空。
- **本地构建需要访问 `maven.neoforged.net`**：国内网络要挂代理（见 [migration-neoforge.md](migration-neoforge.md)）。
- **JEI/界面类改动先上原生控件**：筛子页曾因自绘背景返工；复盘见
  [incidents/2026-10-29-jei-layout.md](incidents/2026-10-29-jei-layout.md)。
- **直连 TLS 失败（libraries.minecraft.net / repo.maven）**：缓存已齐时用 `--offline`。
- **NeoForge 21.1 `FluidTank` NBT 是嵌套 `{Fluid:{id,amount}}`**，不是 1.20.1 的顶层 `FluidName`/`Amount`。
- **1.21.1 没有 `#forge:*` 物品标签**：用 `#c:*`；`#minecraft:crops/seeds` 也不是物品标签。
- **堆肥标签和 JSON 必须同步**：标签有、JSON 没有 = 按 1 点处理。
- **内缩模型的外侧面不能加 `cullface`**：只保留在方块边界；坩埚模型已按此规则清洗。
- **`Properties.ofFullCopy(Blocks.FURNACE)` 会带上 `lit` 光照函数**：新方块没有 `lit` 状态，
  注册期就崩（`Cannot get property BooleanProperty{name=lit}`）。机器方块用 `IRON_BLOCK` 复制 + 手调强度。
- **`EnergyStorage(maxExtract = 0)` 的 `extractEnergy` 永远返回 0**：机器内部扣电要走自己的
  `MachineEnergy#drain`；同时 `receiveEnergy` 受 `maxReceive` 限制，读档要用 `setEnergyStored` 直写。
- **没有玩家时普通区块的 BE 不 tick**：RCON 测机器先 `forceload add`，否则会看到“摆放后只动了几十 tick 就停”。
- **漏斗会把机器里所有槽位当可抽**：`InvWrapper` 全开会让漏斗抽走筛网/插件，必须用
  `MachineItemHandler` 限制抽取范围。
- **`ContainerData` 客户端不能直接挂 BE**：BE 的 `set` 是空实现，菜单同步过来的数据写不进去，
  进度/能量/火焰条会永远冻在初始值。客户端菜单要用 `SimpleContainerData`，服务端才用 BE。
- **`renderBg` 里全局 pose 还没有平移到 `leftPos/topPos`**：自绘虚影/图标要自己加偏移，
  否则全部画到屏幕左上角。
- **1.21.1 的 `Slot#mayPlace` 默认恒为 `true`**，不会调用 `Container#canPlaceItem`；只用容器校验
  挡不住 GUI 拖拽/shift-click（插件会进筛网格）。必须给槽位写 `MachineSlot#mayPlace` 显式转发
  `canPlaceInSlot`（2026-11-01 修）。
- **`Active` 不写 NBT 就不会同步**：`getUpdateTag` 走 `saveAdditional`，漏了 `Active` 会让 BER/粒子
  永远判定停机；必须持久化并在翻转时 `sendBlockUpdated`（2026-11-01 修）。
- **`ambientocclusion: false` 会把机器渲染成纯黑**：别为了提亮凹槽关 AO，改用浅色内壁贴图。
- **相邻机器共面会 z-fighting**：模型整体内缩 1px（x/z 1..15），尤其是实心底座/四壁。
- **BER 头会遮住方块模型**：锤头交给 BER 后，方块模型要去掉静态头，否则会重影；物品模型要单独
  留一个带头版本，不然图标是空架子。
- **机器贴图 32×32 要放行检查脚本**：`check-textures.py` 只对 `machine_*` 允许 16/32，其他方块仍 16。
- **BER 里整块渲染输入物会穿模**：`renderSingleBlock` 铺 0.875 时方块占 1..15 px，正好穿过托盘内壁
  并与盘壁外侧面共面，表现为「料层和盘边交替闪烁」；缩到 0.6（3.2..12.8）并抬高 0.15 px 才干净。
- **1px 宽的端面不能带 `rotation: 90`**：面只有 1 px 宽、UV 矩形是 1×15，旋转后木纹被压进
  1 px → 棱上出现一排细条纹；端面用默认 UV（不旋转）才干净（木桶 2026-11-01 修）。
- **生成器不能无条件覆盖手写数据**：`lang` 只补缺失键、筛表按 item 合并；否则重跑
  `generate_sieve_assets.py` 会删掉 M2/M6 手加的 13 条沙筛掉落（P2 修）。
  新写生成器时，凡是作者可能手改的 JSON，都要走“合并”而不是整文件重写。
- **`port_data.py` 只改路径/字段/命名空间，不改数值**：数值漂移要回生成器或手写源改，
  别在移植产物上打补丁（改完下一次 port 就丢）。
- **core 门面必须在注册之前安装**：`Platform.install()` / `ConfigValues.install()` /
  `OreMaterials.init()` 都在 mod 构造函数最前面；漏装会抛带说明的 `IllegalStateException`
  （`OreMaterials.all()` 同理），而不是到世界加载时才 NPE。配置门面是**委托读 spec**，
  所以 `/reload` 或改配置文件不需要重启。
- **`platform/` 不进 core 守卫**：判断标准是“1.20.1 要重写它吗”；要重写的才放顶层 `platform/`。
  core 门面（`common/config`、`common/platform`、`MachineHooks` 的调用方）永远不带 NeoForge import。
- **`ofFullCopy(IRON_BLOCK)` 不会把方块放进镐标签**：只继承 `requiresCorrectToolForDrops`，
  不加 `#minecraft:mineable/pickaxe` 时镐挖不掉方块；石镐门槛还要 `#minecraft:needs_stone_tool`。
- **配置层别用 `setScreen` 开**：那会把机器菜单关掉（`Screen#removed` → 菜单失效）。1.21.1 用
  `Minecraft#pushGuiLayer`（NeoForge `IMinecraftExtension` 的默认方法）+ 免写 `onClose`
  （`Screen#onClose` 已改成 `popGuiLayer`）；背景层会保留并自动按叠加顺序渲染（2026-11-01）。
- **`ContainerData` 的原版同步包是 short，NeoForge 客户端是 int**：`AdvancedContainerSetDataPayload`
  只有 NeoForge 客户端才走；48000 FE 在 NeoForge 客户端没问题，但新增字段别假设原版客户端能读。
- **`IContainerFactory` 菜单只调 `openMenu(provider)` 会崩客户端**：原版 `handleOpenScreen` 走
  `MenuType.create(id, inv)`，额外数据 buffer 为 `null`，客户端工厂 `readBlockPos()` 直接 NPE。
  必须 `openMenu(provider, buf -> buf.writeBlockPos(pos))`，让 NeoForge 发 `AdvancedOpenScreenPayload`
  （2026-11-01 修，`fff3535`）。
- **共享 BE 类型要列全七种方块**：漏一个放置后 BE 不创建。
- **子模型不要直接当方块模型用**：`barrel_base.json` 没有 textures，只作 parent。
- **JEI / 月光魔法生产 jar 不能直接丢 `run/mods`**：SRG 名会 `NoSuchFieldError`。
- **JEI 的 Modrinth maven 会拿到 Fabric jar**：用 `maven.blamejared.com` 的 neoforge 坐标。
- **数据表客户端只认 `assets/`**：`processResources` 把 `barrel/`、`compost/`、`sieve/`、`hammer/`、
  `crook/`、`crucible_melt/`、`crucible_heat/` 镜像到 `assets/`；data 仍是唯一源。
- **RCON 测桶前先 `forceload add`**；测熔炉要自己写 `CookTimeTotal`。
- **`/tick step` 仍按 20 TPS 走**：`step 250` 约 12.5 秒。
- **原版的锤/钩配方是斜的**：锤 `" x "/" yx"/"y  "`、钩 `"xx "/" x "/" x "`；写成十字/对角棍会被玩家一眼看出不对
  （2026-10-31 修正）。改配方前先翻 `reference/ex-nihilo-original/.../Recipes.java` 对形状。
- **方块交互别随便 `PASS_TO_DEFAULT_BLOCK_INTERACTION`**：坩埚满了以后右键熔料会落到默认交互上，
  在坩埚顶放一个方块（2026-10-31 修）。熔料/流体容器被拒时返回 `ItemInteractionResult.CONSUME`；潜行仍能放置。
- **`ItemInteractionResult.FAIL` 拦不住放置**：`InteractionResult.FAIL` 不 `consumesAction()`，
  `ServerPlayerGameMode#useItemOn` 会继续走 `ItemStack#useOn`。要“点了但什么都不做”只能用 `CONSUME`
  或 `SUCCESS`。
- **`/tick freeze` + `step` 不驱动强制加载区块里的方块实体**（没有玩家时）：坩埚/桶这类要验就得
  `tick unfreeze` 后按真实时间等，用 `/time query gametime` 前后差值算 tick 数再对照速率。
- **RCON 放比较器/中继器要先垫实心方块**（下方必须可支撑），否则 `setblock` 报成功但方块立刻掉落；
  比较器 `facing` 指向**输入**侧，输出在反方向。
- **不要用 `data merge` 往 `FluidTank` 写 `{id:"minecraft:air",amount:0}`**：日志会刷
  `Tried to load invalid fluid`（测试副作用，不影响存档）；清空直接不写 `Fluid` 键。

## 常用命令

```bash
./gradlew build
python3 scripts/check-textures.py
python3 scripts/check-core-imports.py                # core 层不得出现 NeoForge/Forge import
python3 scripts/check-data-paths.py                  # 数据目录/字段/命名空间与目标版本一致

# 数据管线（P2）
python3 scripts/port_data.py --out build/port-1.20.1 --force     # 1.21.1 -> 1.20.1 数据
python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1
python3 scripts/generate_ore_variants.py --target 1.20.1 --out build/gen-1.20.1   # 生成器同理

# 推送（SakuraCat 代理；SSH 22 会被代理拒，走 ssh.github.com:443）
GIT_SSH_COMMAND='ssh -i ~/.ssh/id_ed25519_github -o IdentitiesOnly=yes \
  -o ProxyCommand="nc -X connect -x 127.0.0.1:12450 %h %p"' \
  git -c url."ssh://git@ssh.github.com:443/WanConcubine/Ex-Nihilo-Satellitium.git".insteadOf="git@github.com:WanConcubine/Ex-Nihilo-Satellitium.git" push origin main
python3 scripts/generate_barrel_variants.py          # 11 种木桶 + 石桶 JSON
python3 scripts/generate_ore_variants.py             # 矿物链资源/配方/锭
python3 art/items/tools/restyle.py                   # 锤/钩/蚕/熟蚕/尘土贴图 + 预览
python3 art/blocks/crucible/restyle.py               # 坩埚模型/贴图（Creatio 现代化）
python3 scripts/generate_machine_assets.py           # M10 机器方块/插件/GUI 贴图与模型
./gradlew runServer   # RCON：localhost:25575 / eni
./gradlew runClient
```

> `generate_ore_variants.py`、两个 restyle 脚本都依赖 Pillow；系统 Python 没有时用 DSH 自带 Python 跑。

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java           # 入口 + config 注册
├── registry/                          # Block / Item / BlockEntity / CreativeTab
├── platform/                          # 版本胶水（1.20.1 只重写这一层）
│   ├── NeoForgeModProbe.java          # ModProbe 的 NeoForge 实现（ModList）
│   ├── config/ModConfig.java          # ModConfigSpec + ConfigSource 实现
│   └── machine/                       # MachineEnergy / MachineItemHandler / MachineHooks
├── common/
│   ├── block/{Barrel,Sieve,Crucible,UnfiredCrucible}Block.java
│   ├── blockentity/{Barrel,Sieve,Crucible}BlockEntity.java
│   ├── config/                        # ConfigSource + ConfigValues 门面（core）
│   ├── platform/                      # ModProbe + Platform 门面（core）
│   ├── barrel/                        # BarrelRecipes + reload
│   ├── compost/                       # CompostRegistry + reload
│   ├── sieve/                         # SieveRegistry + reload + SieveDrop
│   ├── hammer/                        # HammerRecipes + reload
│   ├── crook/                         # CrookDrops + reload
│   ├── crucible/                      # CrucibleRecipes / CrucibleHeat + reload
│   ├── machine/                       # M10：SieveRolls / HammerRolls / UpgradeItem
│   │   ├── block/MachineBlock.java    # 四台机器共用一个方块类
│   │   ├── blockentity/               # MachineBlockEntity + 电动/内能子类 + MachineInventory
│   │   └── menu/                      # ElectricMachineMenu / FuelMachineMenu
│   ├── material/                      # materials.json 读取（注册期）
│   ├── event/{Common,Capability,Tool}Events.java
│   └── tag/ModTags.java
├── client/                            # BarrelRenderer / SieveRenderer / CrucibleRenderer / ClientEvents
│                                      # + screen/{Electric,Fuel}MachineScreen
└── compat/
    ├── MoonlightCompat.java
    ├── jade/                          # 可选 HUD：桶/筛/坩埚
    └── jei/                           # 筛/桶/堆肥/坩埚/锤/钩 分类
```

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。
JEI 截图与贴图预览在 `docs/images/`。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑/实现记录”，data-format 写“JSON/NBT/命令”，
  textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录；不要把同一份进度抄到多处。
- 新的“本轮”记录先加在 [DESIGN.md](DESIGN.md) 对应章节，本文件只留一行摘要。
