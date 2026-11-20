# 历史批次（2026-11-01 ～ 2026-11-13）

> 从 [status.md](status.md) 搬出来的批次叙事：只记「当时做了什么、怎么验的」。
> **当前状态 / 表尺 / 验证表 / 下一步只看 status.md**；这里只追加、不改写，用于追溯。
> 顺序：新 → 旧。

## 1.4.2（2026-11-11）

- **1.21.1 线：无代码改动**。本版只为和 1.20.1 线版本号一致。
- **1.20.1 线（改在 `1.20.1-forge` 分支）**：补**大地黏液草种子**；种子照旧种
  `*_vanilla_slime_grass`，但通过 `LootTableLoadEvent` 给它的掉落表**追加**一份
  `<type>_slime_dirt`（原泥土掉落保留）；新增 `sieve/{earth,sky,ender}_slime_dirt.json`
  （10% 树苗 / 25% 黏液球）；补连锁强化图标。
- **验证**：1.20.1 `build` + 纹理守卫（315 模型 / 213 纹理）+ data 守卫通过；
  `-PtechMods -PdeMods` 服务端 `[sieve] 10 inputs, 183 drops`、无未知物品；RCON 验证
  `sky_vanilla_slime_grass` 放得住且掉落 `sky_slime_dirt`。手筛 / 生长 / JEI 未看。

## 1.4.1（2026-11-11）

- **1.21.1 线：无代码改动**。本版只为和 1.20.1 线版本号一致；jar 与 1.4.0 只有版本号不同。
- **1.20.1 线（改在 `1.20.1-forge` 分支）**：匠魂碎矿锤装配站从「锤头 + 坚韧手柄 + 2 大板」
  改成 **1 锤头 + 1 坚韧手柄**；砂砾 / 沙子 / 尘土改按锤头材料速度挖（铁头 6.0，耐久回到
  1000）；新增强化 `exnihilosatellitium:chain`（I–III，每级 16 块，锤头有锁链 overlay），
  DE 锤用专属模块 `chain_mining_module` 一次连锁 32 块。
- **验证**：两版 `./gradlew build --offline` + `check-textures.py`（主线 307 模型 / 116
  blockstate / 190 纹理）通过；1.20.1 服务端 `-PtinkersMods` 46 tool definitions /
  24 station layouts、`chain` 强化注册与配方加载成功；FakePlayer 探针 6.0 速度 / 1000 耐久、
  连锁 17 / 33 块；DE 探针：模块进注册表、宿主接受、装上后 32 块。装配站应用强化、锁链
  overlay 外观与 DE 实机装模块未复测。

## 1.4.0（2026-11-10）

- **9 把金属锤（两版都做）**：锡 / 铝 / 锌 / 铜 256，银 320、铅 384、镍 448、锇 512、铂 1920。
  耐久以 64 为基数；**钻石 1561 仍是普通材质上限**，铂 1920 接近下界合金 2031。
- **3 把 Bamboo 宝石锤（仅 1.20.1）**：樱 / 枫 / 银杏统一 **2048**，按 `bamboomod` 条件注册。
  BambooMod 0.6.2 本身没有宝石锤，这三把是我们用它三种宝石做的自有锤。
- **3 把龙之进化锤**：`wyvern_hammer` / `draconic_hammer` / `chaotic_hammer`，装 DE 时注册；
  走 DE 聚合装置升级链（钻石锤 → 双足飞龙 → 神龙 → 混沌，8M / 32M / 128M OP），
  继承 DE 的模块网格 / 能量 / 速度 / AOE 模块与模块 GUI；不消耗耐久，走 DE 能量。
- **DE 锤模型**：不是平面图标，而是 3D cube 模型（左右锤头 + 中间发光核心 + 长柄 + 柄尾），
  每个面用 `textures/item/de_hammer/*` 分区贴图，自定义 GUI / 手持 display。
- **DE 锤能量**：`HammerTool.energyPerUse()` 统一接口；DE 锤返回 `EquipCfg.energyHarvest`
  （DE 配置的 -99 哨兵回退到 256）。手锤和自动锤都扣能量；空电时手锤不触发锤表、机器停机。
  服务端扣完必须 `broadcastChanges()`，否则客户端能量条/提示不刷新。
- **修复：木钩 / 骨钩恢复耐久**：`ToolEvents#wearMainHand` 曾被限制为只处理 `HammerItem`，
  1.21.1 的木钩 / 骨钩打树叶时耐久永远是满的。现在任何可损坏的手持工具都会扣耐久；
  能量锤仍走 `consumeEnergy`。服务端 FakePlayer 探针：木钩 / 骨钩 / 木锤各 `damage=1`，
  创造模式 `0`。
- **不做**：钴 / 阿迪特转匠魂工具路线；铀因放射性不做锤。三者在 `materials.json` 里的矿链保留。
- **实现**：`ModItems.METAL_HAMMERS` 从 `materials.json` 生成（材料 disabled 时锤子一起消失）；
  配方走 `c:ingots/<id>`（1.20.1 用 `forge:ingots/<id>`），铜用原版铜锭；
  宝石锤配方手写 `forge:mod_loaded(bamboomod)`；DE 锤在可选的 `src/de/java` source set 里继承
  `ModularPickaxe`，由反射在 DE 在场时注册/接能力，配方是 DE `fusion_crafting`（带 `mod_loaded` 门）；
  贴图由 `art/items/tools/hammer_variants.py` 从铁/钻石锤底图换色；创造栏与 JEI catalyst 遍历 `HAMMERS`。
- **数字**：主线无 DE 15 把锤（6 基础 + 9 金属）、装 DE 18 把；307 模型 / 190 纹理 / 297 配方；
  1.20.1 18 把锤（+3 宝石），数字见分支验证。
- **验证**：两版 `build` + 守卫通过；主线真装 DE 后 3 把 DE 锤注册、模块 GUI / 能量模块可用，
  手锤与自动锤均按 256/次扣电、空电停机，**作者实机确认通过**；聚合实际合成 / AOE / JEI 仍未目视。

## 1.3.2（2026-11-05，木桶修复）

- **满桶右键堆肥不再放方块**：可堆肥物品始终被视为「已处理」——`canUse` 对任意可堆肥物品返回
  true；满桶 / 有流体时 `use()` 返回 `CONSUME`，客户端预测也按已处理走。树叶不再被放到桶顶。
- **管道（物品）**：新增 `BarrelItemHandler`：槽 0 堆肥输入（插入即计分）、槽 1 泥土 / 固体产物输出。
  1.21.1 在 `CapabilityEvents` 注册 `Capabilities.ItemHandler.BLOCK`；1.20.1 在 BE
  `getCapability` 返回 `ForgeCapabilities.ITEM_HANDLER`。
- **管道（流体）**：1.21.1 原有 `FluidHandler.BLOCK` 保留；1.20.1 补
  `ForgeCapabilities.FLUID_HANDLER`（tank）。堆肥进行中 / 有固体 / 有泥土时仍拒绝输入，这是状态互斥。
- **验证**：两版 `build` 通过；**未进游戏**测右键拦截 / 管道（没有装管道模组），JEI 未目视。

## 1.3.1（2026-11-05，深板岩 / 末影接口起步）

- **深板岩石子**（无条件）：`sieve/dirt.json` 泥土 **50% × 3-5** 个；4 个 → `minecraft:cobbled_deepslate`
  （2×2），熔炉烧成 `minecraft:deepslate`。草方块表**不加**（`generate_sieve_assets.py` 明确过滤）。
  用途：1.20.1 的「原始合金炉」要 6 个深板岩；1.21.1 的深板岩火焰合成也要它。
- **末影接口（Ender IO）起步**：`sieve/dust_enderio.json`（根级 `mod: enderio` + `patch`）——
  尘土 **2%** 出 `enderio:grains_of_infinity`（1.20.1/1.21.1 的「无限粉」；1.12.2 的
  「基岩粉 / 基岩粉块」在 6.x / 8.x 已不存在）。**不新增基岩粉物品、不写「粉 → 基岩」配方**；
  该物品在 Ender IO 自己的虚空框架 / 电容 / 无限杆配方里使用。
- **脑白金提示**：`BrainPlatinumItem#appendHoverText` + `item.exnihilosatellitium.brain_platinum.tooltip`
  （中英双语），文字「对傻子村民使用使其变为普通村民」；1.3.1 发布内容。
- **数字**：基表 118 → **119**（1.20.1 115 → **116**）；装 Ender IO 各 **+1**（120 / 117）。
  纹理/模型 **295 / 171**（分支 **297 / 167**），配方 **285**；`check-data-paths` 285 recipes /
  75 loot / 75 tags。
- **验证**：两版 `./gradlew build --offline` + `check-textures.py` 通过；主线
  `check-core-imports.py` / `check-data-paths.py` 通过。**未进游戏筛过**、未看 tooltip，JEI 未目视。

## 2026-11-04 深夜（龙之进化，1.2.0 之后）

- **材料**：`materials.json` 新增 `draconium`（`gems`，`draconicevolution` 门控，tier `rare`、
  rarity 64、颜色 `8A5CF5`）；DE 不在场时整条不注册，基表 118 / 115 不变。
- **入口**：`sieve/crushed_end_stone_draconicevolution.json`（根级 `mod` + `patch`）——
  **粉碎末地石 1/8** 出 `draconium_broken`；`generate_sieve_assets.py` 把它加进 `SINGLE_SOURCE`，
  重跑不会漏进砂砾 / 沙 / 尘土三张常规表。
- **链条**：4 破碎 → `draconium_ore_gravel` → DE 的 `c:ores/draconium`
  （1.20.1 `forge:ores/draconium`）熔成 `draconicevolution:draconium_ingot`；
  锤表 3 条（矿砾→粉碎 4–6、矿砂→粉末 4–6、矿尘→自身）全带 `mod` 门。
  **不写** `*_smelting_draconicevolution.json`，避免同一输入在 JEI 里出现两条相同配方。
- **不碰终局**：觉醒龙（4 龙块 + 6 龙核 + 龙心）与混沌碎片（混沌守卫）保持 DE 原样。
- **贴图基底**：破碎龙矿的石壳走 `STONE_PALETTES["draconium"] = END_STONE`（末地石淡黄），
  不是默认灰石——单源材料必须按来源方块换基底。
- **数字**：主线 AE2+ER+DE `[sieve] 132`（+1）、`[hammer] 67`（+3）、`[materials] 18`；
  1.20.1 全装 + Bamboo + DE `[sieve] 174`（+1）、`[hammer] 91`（+3）、`[materials] 26`。
- **验证**：两版真装 DE + Brandons Core + CodeChicken Lib；RCON 熔炉把
  `draconium_ore_sand` 熔成 `draconicevolution:draconium_ingot`（主线两轮、分支一轮）；
  手筛与 JEI 未目视。

## 1.2.0（2026-11-04）

### 骨粉 / 烈焰粉

- **骨粉**：`dust` 新增 `minecraft:bone_meal` 20%（经典 Ex Nihilo 尘土 1/5），
  瓷土与菌岩催菌不再只靠骷髅掉落。
- **烈焰粉**：`sand` 新增 `minecraft:blaze_powder` 3%；碎下界岩 6% 保留为更高产的第二来源，
  酿造 / 末影之眼 / 愤怒人偶多一条来源。
- **数字**：基表 116 → 118（1.20.1 113 → 115）；AE2 120 → 122 / 117 → 119；
  主线 Mek+Thermal 137 → 139；1.20.1 全装 +TiT 139 → 141、+AE2 143 → 145。
- **验证**：两版 JSON 解析 / `build` / 纹理守卫通过，分支数据用主线
  `check-data-paths.py --target 1.20.1` 验过；未进游戏手筛，JEI 未目视。

### Extreme Reactors（两版）

- **三条材料**：`yellorium`（1/8，common）、`anglesite` / `benitoite`（各 1/64，rare）。
  三条都走 `gems` 门控：只在 `bigreactors` 在场时注册，没装 ER 时基表/物品/方块完全不出现；
  装 ER 时筛表 +9、锤表 +9。
- **获取**：三档矿链（碎/粉/砂 → 4 合 1 矿块 → 熔炉）熔成 `bigreactors:yellorium_ingot` /
  `anglesite_crystal` / `benitoite_crystal`；石墨仍由煤烧，cyanite/blutonium 等继续由反应堆产出，
  不做筛源。
- **匠魂（1.20.1）**：yellorium 碎/粉/砂 45 mB、矿块 180 mB、ER 锭 90 / 块 810，
  铸造回 ER 锭/块；用 ER 自带的 `forge:yellorium` 流体，共 10 条，不新增流体。
- **矿石识别（补齐）**：ER 的 `yellorite_ore` 同时登记在 `c:ores/yellorite` 和 `c:ores/uranium`；
  我们的 yellorium 矿块现在同样进这两个 tag（块 + 物品），`yellorium_broken` 进
  `c:raw_materials/yellorium`（及根 tag）。Mekanism 的 `enriching`、IE 的 crusher 等按
  `c:ores/uranium`（1.21.1）/ `forge:ores/uranium`（1.20.1）取矿的机器因此能磨我们的矿，
  输出与 ER 原矿一致（对方的铀粉，不是 yellorium）。
- **验证**：两版真装 ER + ZeroCore；1.20.1 全装 `[materials] loaded 25`、`[sieve] 154 drops`，
  主线 AE2+ER `[sieve] 131 drops`；RCON 熔炉三种产物均出，
  `RecipesUsed` 指向 `*_ore_*_smelting_bigreactors`；手筛与 JEI 未目视。
- **版本**：1.20.1 用 ER 2.0.84 + ZeroCore 2.1.45；1.21.1 用 ER 2.4.9 + ZeroCore 2.4.9。

### BambooMod（仅 1.20.1）

- BambooMod 0.6.2 只有 1.20.1 Forge 构建，接在 `1.20.1-forge` 分支；主线不加载。
- `sieve/dirt.json` / `grass_block.json` 各 +8（4 树苗 2%、竹笋 4%、米种 5%、豆 4%、番茄 4%），
  `sieve/gravel.json` +3 宝石（银杏/枫/樱各 1/64）；全部 `mod: bamboomod` 门控，
  没装时整条跳过，基表 118/115 不变；1.20.1 全装 `[sieve] 173`。
- 堆肥不新增 JSON：它的树叶/树苗/竹笋已在 `#minecraft:leaves` / `#minecraft:saplings`，
  现有标签 + `compost/default.json` 已按 125 分处理。
- 验证：分支真装 0.6.2，`[sieve] 173`、`[hammer] 88`；RCON 泥土 32 / 砂砾 31 含新条目，
  树苗/竹笋 `/setblock` 成功；手筛、树苗生长与 JEI 未目视。

## 1.1.2（2026-11-04，Bug 修复）

- **1.20.1 木钩 / 骨钩不掉耐久**：`common/event/ToolEvents#onBreak` 的 crook 分支只添加
  钩子表掉落，漏了 `hurtAndBreak`。现在非创造模式、且方块确实有钩子表时，对实际手持的
  钩子扣 1 点；耐久耗尽走正常损坏回调。
- **1.21.1 无需改代码**：`BlockDropsEvent` → `wearMainHand` 一直正常；服务端实测
  `damage=1`，本版只是同步版本号。
- **验证**：1.20.1 服务端 FakePlayer + `gameMode.destroyBlock`，木/骨钩各 `damage=1`
  （修复前 0）；两版 `build` + 纹理守卫通过。
- **性质**：纯 bug 修复，没有新增玩法、数据表或贴图改动。

## 1.1.1（2026-11-04，视觉修正）

- **木桶堆肥**：`BarrelBlockEntity` 记录 `CompostItems`（物品 + 实际计入点数）；
  客户端 `ItemTint` 采样物品图标平均色（树叶等灰度贴图乘 `ItemColors` 植被色），
  `BarrelRenderer` 用原版泥土方块模型 + `TintVertexConsumer` 按点数加权混色；
  腐熟计时中 tint 淡回纯白，成土后显示原版泥土色。
- **坩埚熔料**：`CrucibleRenderer` 用 `renderSingleBlock` 画被熔方块的真实模型；
  木坩埚（`isWooden()`）在固体未熔完时始终显示熔料，不再中途切成水盒。
- **成土时间**：`COMPOST_TICKS` 1000 → 600（30 秒），JEI 提示同步。
- **证据**：两条线 dev client 截图（[1.1.1-compost-crucible.png](images/1.1.1-compost-crucible.png)）：
  腐肉红褐 / 树叶绿 / 50-50 黄褐 / 成土原版泥土色；木坩埚显示树叶方块。
- **清理**：删除不再使用的 `textures/block/barrel_compost.png`。

## 2026-11-04 下午（村民 / 脑白金 / 蜂蜜 / 蜜蜂）

- **村民人偶**（`villager_doll`）：8 个绿宝石围一圈普通人偶；巫水满桶 + 右键 → 僵尸村民，
  和平模式掉腐肉，桶恢复空桶。是第 8 只人偶。
- **脑白金**（`brain_platinum`）：鸡蛋 4 + 糖 4 + 绿宝石 1（`ese / sMs / ese`）；
  右键**傻子村民**转成普通（无职业）村民，消耗 1 个，成功音效 + 绿色粒子。
- **糖水 → 浓缩蜂蜜汁**（两个可放置流体，有 source block + 桶）：
  - 满水桶 + 糖 → 糖水；满糖水桶 + 蒲公英黄 → 30 秒（600 tick）后变浓缩蜂蜜汁；
  - 满/不满的浓缩蜂蜜汁桶 + 玻璃瓶 → 扣 250 mB、给蜂蜜瓶（`drain` 规则，不要求满桶）；
  - 桶里有任意数量浓缩蜂蜜汁时，每 100 tick 有 1/6 概率在 6 格内生成 1 只蜜蜂，附近上限 5 只。
- **蜂箱**：3 木板 / 3 蜂蜜瓶 / 3 木板 → 原版 `minecraft:beehive`（工作台）。
- **AE2 陨石粉**：`ae2:sky_dust` 加入沙（1/16）与砂砾（1/16）补丁；AE2 掉落 118 → 120。
- **实现要点**：`BarrelRecipes.Recipe` 增加 `resultFluid` / `delayTicks` / `drain`；
  `BarrelBlockEntity` 增加延迟换液状态机（NBT `PendingFluidFrom/To/Ticks`）、
  玻璃瓶抽取和蜂蜜养蜂；JEI 用流体槽显示流体产出并标注延迟/抽取量，Jade 显示「转化中」。
- **贴图**：村民人偶用现有 64×64 轮廓（翠绿）；脑白金是 16×16 蓝色礼盒 + 金丝带。
- **验证**：见 [status.md](status.md) 的验证表。

## 2026-11-04（热力人偶：狂风 / 暴雪）

- **对象**：热力（Thermal Foundation 11.0.x，含 Thermal Core；本地可用的发行版是 1.20.1）。
  新增两个**条件物品** `blitz_doll`（狂风人偶）/ `blizz_doll`（暴雪人偶），中文名按所有者指定，
  **不沿用参考图的「劲风 / 冻结」**。
- **配方**（照所有者给的两张图）：`xyx / zwz / xvx`，中心是普通人偶，四角 4 个
  `thermal:niter_dust`（硝石粉）或 `minecraft:snowball`，上中红石、两侧荧石粉、下中下界疣。
  两张都是工作台配方，和愤怒 / 诡异 / 漂浮 / 守卫者人偶同形。**手写 JSON**，不进 datagen。
- **桶规则**（右键立即生效，和平模式掉材料，桶恢复空桶）：
  - 满熔岩 + `blitz_doll` → `thermal:blitz`（狂风人），和平掉 `thermal:blitz_rod`；
  - 满水 + `blizz_doll` → `thermal:blizz`（暴雪人），和平掉 `thermal:blizz_rod`。
- **门控三处一致**：物品注册（主线 `Platform.isModLoaded("thermal_foundation")` / 分支 `ModList`）、
  配方条件（1.21.1 `neoforge:conditions` / 1.20.1 根级 `conditions`）、桶规则根级
  `"mod": "thermal_foundation"`。没装 Thermal 时物品、配方、桶规则一起消失。
- **datagen 防撞**：`ModItemModels.HAND_FLAT_MODELS` 按名字跳过手写模型；字段 `@Nullable`。
  临时强制注册跑 `runData` 不会写出同名模型。
- **验证**：1.20.1 `[barrel] 14`、dump 两条规则、`/summon` 两个物品；主线无 Thermal 时桶 12、
  物品不存在；配方经 `/reload` 解析探测。合成 / 右键 / JEI 未实测。

## 2026-11-03 晚（工匠巧思 + AE2 赛特斯石英）

- **对象**：工匠巧思（`tinkers_thinking` 0.2.4，只有 1.20.1）与 AE2（1.21.1 `19.2.18` /
  1.20.1 `15.4.12`，前置 GuideME `21.1.22` / `20.1.15`）。两者都是可选依赖，不写 `mods.toml`。
- **TiT 阿迪特（`ardite`，金属）**：`ingots` 候选 `tinkers_thinking:ardite_ingot`，没装时回退
  自有 `ardite_ingot`。**单源**：碎下界岩 1% → `ardite_broken`，再走三档链（碎矿 ×4 → 矿块 →
  熔炼）；砂砾/沙子/尘土不出。1.20.1 另有 6 条 `tconstruct:melting`（碎矿 45 mB / 矿块 180 mB，
  950°C，`forge:molten_ardite`）。
- **TiT 叶绿素（`chlorophyll`，`gems`）**：只在 TiT 在场时注册；**单源**：草方块 1% →
  `chlorophyll_broken`（草方块表 = 泥土 24 条 + 这一条，由生成器从 `dirt.json` 生成），
  矿块熔成 `tinkers_thinking:chlorophyte_compound`（原矿 1A+1B 的等价物）。没装 TiT 时整条
  （物品/方块/碎料/筛出/锤表/配方/标签）都不出现。
- **AE2 赛特斯**：独立补丁 `sieve/sand_ae2.json`（根级 `"mod": "ae2"` + `"patch": true`），
  沙 → `ae2:certus_quartz_crystal` **1/6**、`ae2:certus_quartz_dust` **1/12**。
  不注册自有物品；充能赛特斯走 AE2 自己的 `ae2:charger`（配方只要铁/铜/赛特斯），
  天石粉/陨石不做。缺 AE2 时整文件在查物品之前跳过。
- **为什么形态不同**：TiT 的两种矿本身没有「空岛可得的成品链」，所以走本模组三档材料链；
  AE2 的赛特斯是基础资源且 AE2 自带种子/充能/氟石/ME 全链，直接掉成品不会多出对不上的中间物。
- **验证**：`./gradlew build`（主线 21 / 分支 17）+ 五个守卫脚本全绿；1.21.1（AE2）`[materials]
  14 entries`、`116 → 118 drops`；1.20.1（全装）`22 entries`、`141 drops`；RCON 熔炉阿迪特 /
  叶绿素两条通过；未验手筛 / JEI / 匠魂炉实际熔化。
- **坑**：TiT 的两种原矿都要钻石镐，阿迪特没有熔炉配方；AE2 的 1.20.1 生产 jar 不能在 FG dev
  直丢 `run/mods`（SRG mixin 崩服）。

## 2026-11-03（数据包 v2 / 末影蛋糕 / 三路径 / 科技矿物链）

### 数据包 v2：标签 / mod 门 / 补丁

- 筛 / 锤 / 钩 / 桶 / 熔点 / 热源六张表的 id 字段接受 `#tag`，加载时展开；`TagManager` 只在
  reload 里存结果、标签在所有 listener 之后才绑定，所以 `load()` 先建具体条目，
  `TagsUpdatedEvent` 再 `rebuild()`（日志先 `waiting for tags` 是正常的）。
- 所有表支持根级 `"mod"`，筛 / 钩单条掉落也支持；`"patch": true`（或出现 `"remove"`）
  在所有普通文件之后按资源 id 应用，`remove: true` 删整条、`remove: [...]` 删列表条目。
- 诊断命令 `/exnihilo tables`、`/exnihilo sieve|hammer|compost`；示例包 `examples/datapack/`
  （`pack_format 48`）。玩家 wiki 按数据包 / KubeJS / CraftTweaker 三路线写，中英双语 + 静态站。
- 验证：测试包下 `[sieve] 45 inputs, 115 drops`、`[hammer] 91`、`[crook] 32`、
  `[crucible] 49 heat`、`[barrel] 11`；RCON 验证标签展开与 patch 删除；删补丁后 `/reload` 回基线。

### 末影蛋糕（End Cake）

- 可放置蛋糕：主世界吃一片 → 末地中央岛；末地吃 → 玩家床 / 世界出生点；下界不传送。
- 放置时只有 1 片（`bites=6`），末影之眼右键补 1 片、最多 7 片；每次 +2 饥饿 / 0.1 饱和度，
  始终可吃；8 末影珍珠 + 1 蛋糕。
- 复用 vanilla `block/cake` 父模型，只覆盖 4 张 16×16 贴图；无掉落表；1.20.1 同步。
- 验证：`/setblock ... [bites=0/1/5/6]` 全部命中；右键吃 / 传送 / 补片未目视。

### 三路径 P1 → P3（两版）

- **P1 基座**：`common/table/`（`TableId` / `TableBuilder` / `CustomizationOverlay` / `Tables` /
  `TableDump`），七张表统一 rebuild；命令 `/exnihilo customize <table> add|remove|replace`
  与 `/exnihilo dump`；失败隔离（坏 overlay 只 warning，坏数据包保留旧快照）。
- **P1.5**：显式 priority、冲突日志（replaced / removed / add ignored）、同 source 去重、
  4096 操作上限、server stop 清空 overlay。
- **P2 KubeJS**：2101 插件接口 / 2001 类 + `BindingsEvent`，七张表 add/remove/replace +
  `addJson/removeJson/replaceJson`；source `kubejs` priority 100。
- **P3 CraftTweaker**：21.0.39 / 14.0.60，`@ZenRegister` 七个类；source `crafttweaker` priority 200；
  每次 reload 清旧贡献。没有注册 CT recipe type（JEI 不出现七张表的 CT 页）。
- 验证：两版七张表与 datapack fixture 的 `dump` 逐字节一致；改脚本 `/reload` 替换而非叠加；
  无 KJS / CT 正常启动。

### 科技模组矿物链：Mek 氟石 + Thermal 六种

- `materials.json` 新增可选 `gems`（与 `ingots` 同构，**不能混用**）：纯模组产物，
  **不注册自有物品、不生成 gems 标签、没有 base 熔炼**；整条材料只在候选 mod 加载时注册。
- 7 种：fluorite 1/8、apatite 1/18、cinnabar 1/32、niter 1/24、sulfur 1/24、ruby/sapphire 1/64；
  三档筛料共用同一 `rarity`，21 条手写 `*_smelting_<mod>` 变体。
- 矿块进 `c:ores/<id>` / `forge:ores/<id>`（`required:false`），Mek 富集机 / Thermal 磨粉机
  可按标签处理；矿块无 loot table，由 `SelfDropBlock` 掉自身。
- 验证：main + Mek：氟石矿砾 → `mekanism:fluorite_gem`；1.20.1 + Thermal：6 种矿砾
  → `thermal:*` 6/6 通过；无科技模组时整条静默跳过、无 unknown item。

## 2026-11-02 发布记录

- **0.1.0 已发**：main tag `v0.1.0`（`d4a6dc2`）、分支 `v0.1.0-1.20.1`（`35d98d4`），
  jar 649 KB / 587 KB（sha256 `2f3d1af5…` / `1168ca77…`）；已推 origin。
  CurseForge / Modrinth 上传需要所有者操作。
- **0.2.0 准备 → 1.0.0 发布**：两条线原定 0.2.0，验收后直接定为 **1.0.0**；tag `v1.0.0` /
  `v1.0.0-1.20.1`（2026-11-04）。打包剔除 datagen `.cache/`，带 LICENSE / THIRD-PARTY.md / icon.png。

## 2026-11-02（Tinkers' Construct 3 接入）

- 碧空 / 末影黏液草种子：只在 `tconstruct` 在场时注册；右键草方块 / 泥土 →
  `tconstruct:sky/ender_vanilla_slime_grass`；泥土筛 2% / 1%，条目带 `mod`；模型/贴图手写。
- 钴（第 13 种材料）：候选 `tconstruct:cobalt_ingot`，无匠魂回退自有钴锭；碎下界岩 1% 出
  `cobalt_broken`，broken 贴图用下界岩红棕基底。
- 下界合金碎片：碎下界岩 0.4%，双线都加。
- 匠魂熔炼：78 条 `tconstruct:melting`（碎块 45 mB / 矿块 180 mB，`forge:molten_<m>`）。
- 验证：只装 Tinkers 时 `cobalt → tconstruct:cobalt_ingot`；RCON 熔炉通过；不装时 90 drops。

## 2026-11-02（Immersive Engineering 接入）

- silver / nickel → Thermal → IE；lead → Mek / Thermal → IE；aluminum → IE；uranium → Mek → IE；
  15 条熔炼变体；IE 粉碎机走 `forge:ores/<m>` 标签，不补配方。
- 只装 IE 时 5 材料全部解析到 IE；Thermal+Create+IE 同装时优先级正确；主线无 IE 回退自有铝锭。

## 2026-11-02（树的起点：树苗 / 竹子 / 下界菌岩孢子）

- 泥土筛 9 种树苗（合计 31%/次）+ 竹子 5%；碎下界岩筛菌岩孢子各 5% + 骨粉催菌/巨菌；
  新物品贴图 / 模型 / 堆肥 100。当时 `[sieve] 88 drops` / `[compost] 67 entries`。

## 上一批次（2026-11-02，科技模组锭接入：Mekanism / Thermal / Create）

- **目标**：只保证「空岛上拿不到的基础资源」有来源；合金交给科技模组自己做。所以这轮只做
  **锭接入**，不加新金属、不加合金、不碰粉尘标签。
- **材料表**（两条线共用一份 `materials.json`）：tin/lead → `mekanism` → `thermal_foundation`；
  silver/nickel → `thermal_foundation`；zinc → `create`；osmium/uranium → `mekanism`；
  platinum/aluminum 保持自有锭。优先级 = 数组顺序，运行时取第一个已加载的。
- **数据**：每个「材料 × 候选」一条变体配方，条件是本模组在场 **且** 它之前的候选都不在
  （否则两个模组同时装会匹配两条）。base 配方条件由 `ModRecipes` 自动算成
  `not(or(...))`（1.21.1）／手写在 1.20.1 分支。
- **踩到的真坑**：Forge 1.20.1 的配方条件键是**裸 `conditions`**，不是 `forge:conditions`。
  写错不会报「未知条件」——Forge 直接忽略，然后按无条件解析，于是未安装模组的
  `thermal:`/`create:` 物品炸出 27 条 `Unknown item`，整个配方表只剩 7 个类型。
  `port_data.py` 之前把 `neoforge:conditions` 转成 `forge:conditions`，正是这个错误；
  已改成 `conditions`，`check-data-paths.py` 也加了「键必须对」的检查（负向测试能红）。
- **验证（主 1.21.1，装 Mekanism 10.7.19.85）**：`[materials] tin/lead/osmium/uranium
  smelts into mekanism:ingot_*`；熔炉烧 `tin_ore_gravel` 产出 `mekanism:ingot_tin`；
  `exnihilosatellitium:tin_ingot` 未注册（`summon` 得到 Air）；烧 `zinc_ore_gravel`
  产出自有 `zinc_ingot`（Create 不在场，兜底分支生效）。
- **验证（1.20.1 分支，无科技模组）**：0 条 recipe 解析错误，27 条变体按条件跳过，
  `tin_ore_gravel → tin_ingot` 正常。
- **未验**：真装 Thermal / Create 的路径（本机没有这两个 jar）；它们与 Mek 走同一套
  条件机制，静态等价。

## 上一批次（2026-11-02，M7 datagen 第三批：blockstate / model）

- **`ModBlockStates`**：71 个 blockstate（70 个单 variant + 筛子 multipart：frame + 三档 mesh）
  + 29 个机械方块模型（12 桶 + 12 木坩埚 + 火/未烧制坩埚 + dust/crushed×2 + 侵染树叶）。
- **`ModItemModels`**：144 个物品模型，**从注册表推导**——方块物品 parent 指向方块模型
  （`sieve` → `sieve_frame`、矿块 → 共享的 `ore_<kind>_block`），其余 `item/generated`，
  锤/钩 `item/handheld`，瓷桶两层 + `filled` predicate。
- **共享木种表** `datagen/WoodTypes`：`ModRecipes` 的桶/坩埚配方与模型都从它派生。
- **保留手绘**：`barrel_base`/`crucible_base`/3 个矿块/筛框与三张网/四台机器（13 个方块模型）
  + `electric_hammer`/`fuel_hammer` 两个带 elements 的物品模型；PNG 全部仍由 Python/art 出。
- **provider 顺序**：blockstates 必须先于 item models（物品模型用 `getExistingFile` 校验方块模型父级，
  依赖前一个 provider 的 `trackGenerated`）。
- **Python 生成器收尾**：删掉 `generate_barrel_variants.py` / `generate_wooden_crucible_variants.py`
  （只写 JSON）；`generate_ore_variants.py` 只留贴图 + 锤表 + lang；`generate_sieve_assets.py`
  只留筛网纹理 + 筛表 + lang；`generate_machine_assets.py` 只留 PNG。三者原地重跑零 diff。
- **`check-textures.py` 升级**：同时扫 `src/main` + `src/generated`，新增 blockstate→模型、
  模型 parent 两条引用检查（负向测试通过）。
- **验证**：`compare-models.py` → `259 old, 244 generated, 244 identical` / `MATCH`（15 个 old-only
  都是手绘文件）；删旧文件后 `runData` `written: 0`；`build` + 三个守卫全过；客户端启动无
  missing model/texture。**画面仍待作者目视**（桶/坩埚/筛子/机器/矿块）。
- **下一批**：语言默认不动；M7 只剩这个待定项。

## 上一批次（2026-11-02，M7 datagen 第二批：配方）

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
  本地+远端删除；status/README/parity/DESIGN 全部改成双版本（复盘见
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
