# 当前状态（交接卡）

> 最后更新：2026-11-04
> 先看这 25 行；批次叙事在 [history.md](history.md)，架构/取舍在 [DESIGN.md](DESIGN.md)。

## 一句话

**双版本目标**：主线 **Minecraft 1.21.1 / NeoForge 21.1.x**（`main`）+ **1.20.1 / Forge 47.4**
（`1.20.1-forge` 分支）；M1–M10、M7 datagen、P1/P1.5/P2/P3、数据包 v2 都已完成。
**0.1.0 已发布**（两条线都有 tag）；**0.2.0 内容已增加 TiT/AE2，旧 jar 需重打**，
 tag / 商店上传待所有者确认。
**最新一批（2026-11-03 晚）**：工匠巧思（TiT）阿迪特 / 叶绿素 + 应用能源 2（AE2）赛特斯石英，
两版数据与 dev 环境都已接好，已提交并推送（main `1417924` / 1.20.1 `10454db`）。
**联动**：22 种材料（14 金属 + 8 矿物）；Mekanism / Thermal / Create / IE / Tinkers / TiT / AE2
全部可选，缺失即降级；核心门面 P1 与三路径（数据包 / KubeJS / CraftTweaker）已完成。
**当前表尺**：筛子 7 输入 / 116 掉落（无科技模组；1.20.1 113，缺 3 条钴历史差异）；
+AE2 118 / 115；1.20.1 全装 +TiT 139、再加 AE2 141；主线 Mek+Thermal 137、+AE2 139。
锤表 55 基础 / 79 全装；桶 12、堆肥 67、坩埚 39 熔点 / 9 热源。
**下一步**：0.2.0 发布 → 画面/手感验收 → P4 JEI 同步 → P3 状态机拆分（见文末「下一步」）。

## 当前批次（2026-11-03 晚，TiT 两种矿 + AE2 赛特斯石英）

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
- **验证**：
  - `./gradlew build`（主线 21 / 分支 17）+ `check-textures.py` + `check-data-paths.py` +
    `check-core-imports.py` + `check-wiki.py` 全绿；两棵树提交后 `git status` 干净。
  - 1.21.1（AE2 19.2.18 + GuideME 21.1.22）：`[materials] 14 entries`、`[sieve] 7 inputs,
    116 → 118 drops`、`[hammer] 55`；`/exnihilo sieve minecraft:sand` 含
    `certus_quartz_crystal 0.16667` / `certus_quartz_dust 0.08333`；不装 AE2 时 116、无 unknown。
  - 1.20.1（`-PtechMods` 全装 Mek+Thermal+Create+IE+Tinkers+TiT+AE2）：`[materials] 22 entries`、
    `[sieve] 7 inputs, 141 drops`（基表 139 + AE2 2）、`[hammer] 79`；RCON 熔炉
    `ardite_ore_gravel → tinkers_thinking:ardite_ingot`、`chlorophyll_ore_gravel →
    tinkers_thinking:chlorophyte_compound`；去掉 AE2 时 139、sand 无 AE2 条目。
  - **未验**：真实手筛 / 机器筛筛出 TiT/AE2 物品、JEI 页面、匠魂炉实际熔化。
- **坑**：TiT 的两种原矿都要钻石镐，阿迪特没有熔炉配方（高炉/冶炼炉）；AE2 的 1.20.1
  生产 jar 不能在 FG dev 直丢 `run/mods`（SRG mixin 崩服），见「踩过的坑」。

## 历史批次（详细见 [history.md](history.md)）

- **2026-11-03**：数据包 v2（标签/mod 门/补丁）、末影蛋糕、三路径 P1/P1.5/P2/P3、
  Mekanism 氟石 + Thermal 六种矿物链。
- **2026-11-02**：M7 datagen 第二/三批、科技模组锭接入、泥土筛石子 + Create 粉碎桥。
- **2026-11-01**：1.20.1 分支 M2–M10、P1 薄门面、P2 数据管线、M6 六刀、M10 自动化。

## 功能现状

### 桶 / 堆肥 / 筛子

- 十二种桶（11 种木材 + 石桶）共享一个 BE；木桶 ≤433 K，石桶不限温；堆肥 1000 分制、50 秒成土；
  接雨水、比较器、破坏返还、管道状态互斥都已实现。
- 桶转化 12 条：水+熔岩→圆石、熔岩+水→黑曜石、熔岩+红石→下界岩、熔岩+荧石→末地石、
  水+尘土→粘土、水+牛奶→史莱姆；巫水+沙→灵魂沙、巫水+树苗→深色橡树苗；
  4 条人偶规则（熔岩+愤怒→烈焰人、巫水+诡异→末影人、巫水+漂浮→潜影贝、水+守卫者→守卫者）。
- 筛子 7 输入 / 116 掉落（无科技模组：砂砾 / 沙 / 泥土 / 尘土 / 草方块 / 粉碎下界岩 / 粉碎末地石）；
  装模组后 1.20.1 全装 +TiT 139、再加 AE2 141；主线 Mek+Thermal 137、+AE2 139。
  三档筛网只影响耐久。
- 矿物链：22 种材料（14 金属 + 8 矿物）；4 碎块 → 矿方块 → 熔炼成锭/对方矿物；
  锤矿砾/矿砂掉 4–6、粉尘掉自身。`gems` 材料只在对应模组在场时注册。
- **泥土的石头来源**：四种石子（圆石/安山岩/花岗岩/闪长岩）每次 4-6 个、100% 出，
  四个合成一块对应石头。
- **泥土的树苗来源**：橡木 8% / 白桦 4% / 云杉 4% / 丛林 3% / 金合欢 3% +
  **樱花 3% / 红树 3% / 杜鹃 2% / 盛开的杜鹃 1%**（共 9 种、合计 31%；经典 Ex Nihilo 的树种子位）。
- **竹子来源**：泥土筛 5% 出 `minecraft:bamboo`（原版竹子物品就是竹笋）；竹桶、竹坩埚、
  脚手架和竹子堆肥从此有第一个来源。
- **下界树起点**：粉碎下界岩筛出**绯红 / 诡异菌岩孢子**各 5%，右键下界岩变成对应菌岩；
  之后用原版骨粉催菌、再催巨菌，得到绯红 / 诡异菌柄（木桶、木坩埚的来源）。
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

### 联动（supported mods）

- 材料表 `materials.json`：14 金属（铁/金/铜/锡/银/铅/镍/铂/铝/锇/锌/钴/铀/阿迪特）
  + 8 矿物（`gems`：氟石/磷灰石/朱砂/硝石/硫磺/红宝石/蓝宝石/叶绿素）。
- 锭候选按数组顺序取第一个已加载模组：Mekanism / Thermal / Create / IE / Tinkers / TiT；
  没有候选时回退自有锭。`gems` 没有自有兜底，模组不在就整条不注册。
- 直接掉落：TiT 阿迪特（碎下界岩 1%）、TiT 叶绿素（草方块 1%）、AE2 赛特斯石英
  （沙 1/6 水晶 + 1/12 粉）。三条都带 `mod` 门，缺失时在查物品之前跳过。
- 所有联动都是可选的；`mods.toml` 不声明硬依赖，发布 jar 不打包对方代码/资源。

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

- **工作树**：主线 `/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium`；分支
  `/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium-1.20.1`（`1.20.1-forge` @ `10454db`）。
  两条线都已推到 `origin`，`git status` 应为空。
- **先跑**：`./gradlew build` + `python3 scripts/check-textures.py` +
  `python3 scripts/check-core-imports.py` + `python3 scripts/check-data-paths.py` +
  `python3 scripts/check-wiki.py`。分支没有后三个脚本，只跑 `build` + 纹理。
- **datagen 自检**：`./gradlew runData --offline` 后 `git status` 应为空；配方比对用
  `python3 scripts/compare-recipes.py`，模型比对用 `python3 scripts/compare-models.py`。
- **dev 环境**：主线 `run/mods` 有 AE2 + GuideME（默认 118 drops）；分支 `libs/` 有
  Mek/Thermal/Create/IE/Tinkers/TiT + AE2/GuideME，用 `-PtechMods` 启动。删掉即回基表。
- **已知不一致**：1.20.1 分支筛表少 3 条钴（沙/砂砾/尘土各 1/64），基表 113 vs 主线 116；
  分支生成器缺失（没有 `generate_sieve_assets.py`），下次要么补脚本要么手工对齐。
- **进游戏验收（剩余）**：M1.5–M10 的画面/右键手感（机器 GUI/自动输出抽屉/BER/筛料渲染/
  木桶竖棱/镐挖机器）、TiT 与 AE2 的实际筛出与 JEI 页、匠魂炉实际熔化。
- 不要提交 `build/`、`run/`、`reference/`、`libs/`、`.gradle/`。

## 已完成的里程碑

| 里程碑 | 内容 | 记录 |
|---|---|---|
| M1 | 木桶：堆肥 1000 分制 → 泥土、1000 mB 流体、比较器、七材质、内缩模型 | [DESIGN §8/§10](DESIGN.md) |
| M1.5 | 水/熔岩凝固、木桶限温 433 K、接雨水、熔岩掺红石/荧石；转化表 JSON 化 | [DESIGN §13/§14](DESIGN.md) |
| M2 | 筛子 + 三档筛网 + 数据驱动掉落 + JEI；4 碎块 → 矿方块 → 熔炼成锭 | [DESIGN §12](DESIGN.md) |
| 桶 JEI | `barrel` / `barrel_compost` 两个分类，读同一张 `BarrelRecipes` / `CompostRegistry` | [DESIGN §14](DESIGN.md) |
| M3 | 6 锤 / 2 钩 / `dust` / 蚕动画 / 熟蚕 / 粘土 / 55 条基础锤表（全装 79）/ 锤钩 JEI | 🚧 待验收（[DESIGN §15–16、§18](DESIGN.md)） |
| M4 | 坩埚 + 热源；19 熔点 / 9 热源；容量可配；Creatio 模型；熔料/流体显示；Jade；JEI 两页 | 服务端✅ / 画面🚧（[DESIGN §17](DESIGN.md)） |
| M6 | 巫水 / 古代孢子 / 瓷桶 / 11 种木坩埚 / 12 种木桶 / 蚕 / 5 种人偶 / 筛出物扩充 | 服务端✅ / 画面🚧（[DESIGN §21–26](DESIGN.md)） |
| M10 | 电动/内能 × 自动筛/自动锤 + 速度/时运插件 + 自动输出配置；手动与机器共用判定函数 | ✅（GUI/模型/动画作者已验收；自动输出 GUI 待目视）（[DESIGN §27](DESIGN.md)） |

## 验证状态

> 只保留当前仍有效的证据；2026-11-02 之前的逐项记录见 [history.md](history.md)。
> **测试绿 ≠ 画面/手感验过**；没进游戏看过的一律标 ⚠️。

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| 三路径 P1–P3 | ✅ | TableBuilder/overlay、KubeJS 2101+2001、CraftTweaker 21+14；两版七张表 add/remove/replace；`/reload` 前后 dump 逐字节一致 |
| P1 生命周期/失败隔离 | ✅ | 重复提交去重、空 payload 拒绝、server stop 清 overlay；坏 overlay 只 warning、坏数据包保留旧快照 |
| P1 core 守卫 | ✅ | `check-core-imports.py` → `0 known leak(s)`，`common/table` 已纳入扫描 |
| `./gradlew build` | ✅ | 2026-11-03 提交后两版 `BUILD SUCCESSFUL`（`--offline --no-daemon`） |
| `check-textures.py` | ✅ | 主线 264 模型 / 102 blockstate / 153 纹理；分支 266 / 102 / 149；无 `minecraft:item/*` |
| `check-data-paths.py` | ✅ | 1.21.1：258 recipes / 75 loot / 63 tags；1.20.1 分支未见报错 |
| `check-wiki.py` | ✅ | 7 页 / 7 译文 / 52 链接 / 68 JSON / 7 nav |
| 数据表 JSON | ✅ | 7 输入筛表 116（基表）、55 锤、10 钩、12 桶、67 堆肥、39 熔点 / 9 热源；全部可解析 |
| 桶：堆肥 / 流体 / 固化 | ✅ | RCON：苹果+100、树叶+125，满 1000 成土；熔岩/水装填与圆石/黑曜石固化通过 |
| 桶：牛奶 / 巫水 / 人偶 | ✅ | RCON：牛奶→史莱姆（和平掉粘液球）；巫水发酵与 4 条转化；人偶右键立即生成/掉落 |
| 巫水 / 古代孢子 | ✅ | 满水+菌丝约 25 秒变巫水；蘑菇煲起酵；流体方块可放置；沙筛 1/128 |
| 坩埚：熔点/热源/容量 | ✅ | RCON：19+9 条加载；篝火 0.25 / 熔炉 0.15 / 熄灭 0；容量 8000→3000 热改生效 |
| 坩埚：显示 / Jade / JEI | ⚠️ | 熔料↔流体切换、Jade 剩余、两页 JEI 未目视 |
| 筛子：基础表 | ✅ | 2026-11-04 审计：`[sieve] 7 inputs, 116 drops`（无科技模组），含树苗 9 种、竹子、海晶、菌岩孢子、下界合金碎片 |
| 筛子：矿物/联动 | ✅ | Mek+Thermal 137；1.20.1 +TiT 139；+AE2 141；RCON 概率与 JSON 一致，无 unknown item |
| 筛子：草方块 / 泥土 | ✅ | 草方块 = 泥土 24 条 + 叶绿素碎矿 1%；四种石子 4-6 个 100% 出 |
| 筛子：手筛 / 机器 / JEI | ⚠️ | 实际出货手感、JEI 筛子页未目视（尤其 TiT/AE2 条目） |
| 锤 / 钩 | ⚠️ | 6 锤 / 2 钩、左键唯一路径、耐久与 4–6 掉落逻辑就位；手感与 JEI 未验 |
| 矿物链 | ✅ | 4 碎块→矿块→熔炼 RCON 通过；14 金属 + 8 矿物按候选解析；`gems` 缺模组整条不注册 |
| 蚕 / 侵染树叶 | ⚠️ | 进度/扩散 RCON 验过；右键、变白渲染、破坏掉线未目视 |
| 瓷桶 / 木坩埚 / 人偶 | ⚠️ | 组件/熔化/生成 RCON 验过；舀水/倒液体/11 变种外观未目视 |
| M10 机器 | ✅ | 电动/内能 × 筛/锤、插件、管道、自动输出（下推/关闭/换面/20 tick 重试）RCON 全过 |
| M10 GUI / 自动输出面板 | ⚠️ | 机器 GUI/仪表作者已验收；自动输出左侧抽屉与面按钮待目视 |
| supported mods：Mek | ✅ | 1.21.1 + 1.20.1：`tin/lead/osmium/uranium` 熔成 `mekanism:ingot_*`；氟石矿砾 → `mekanism:fluorite_gem` |
| supported mods：Thermal | ✅ | 1.20.1：磷灰石/朱砂/硝石/硫磺/红宝石/蓝宝石 6/6 熔成 `thermal:*` |
| supported mods：Create / IE | ✅ | Create 锌锭 + 12 条 crushing；IE 五锭候选与 `forge:ores` 标签；多方块未跑 |
| supported mods：Tinkers / TiT | ✅ | 钴 → `tconstruct:cobalt_ingot`；阿迪特/叶绿素 22 entries、RCON 熔炉两条都通过 |
| supported mods：AE2 | ✅ | 1.21.1 116→118 / 1.20.1 139→141；RCON sand 两条；缺 AE2 静默跳过、无 unknown |
| 客户端画面 | ⚠️ | 最近一次客户端验收停在 2026-10-31；TiT/AE2 物品与 JEI 页未进游戏看过 |
| 旧内容目视 | ⚠️ | 桶/筛比例、木桶竖棱、筛料层、Jade tooltip 等仍待作者确认 |

## 下一步（按价值排序）

> 自定义三路径：P1 + P1.5 + P2（KubeJS）+ P3（CraftTweaker）两版都已完成；
> 下一步是 [roadmap](customization-roadmap.md) 的 **P4 客户端 JEI 同步 + fixture CI**。

1. **0.2.0 发布收尾**：版本号/日期确认、tag（`v0.2.0` / `v0.2.0-1.20.1`）、
   CurseForge / Modrinth 上传、README 下载链接（需要所有者操作）。
2. **一次性画面/手感验收 M1.5–M10**：机器 GUI/自动输出抽屉、BER 动画、筛料渲染、
   木桶竖棱、镐挖机器；清单见「交接清单」。
3. **P4 客户端 JEI 同步 + fixture CI**：专用服客户端 JEI 目前只读本地镜像/数据包。
4. **P3 状态机拆分**：`BarrelState` 等纯类 + 第一批 JUnit（P2 之后才有意义）。
5. **工程收尾**：advancement 树、CI、零测试（tech-debt P3 #37）；M8 月光魔法联动占位。
6. **更多 supported mods**：AE2 充能赛特斯 / 天石粉（若所有者要）、TiT 其它矿（锆/锆石）、
   IE 1.21.1、其它科技模组；按 [data-format.md](data-format.md) §8 加候选/补丁（只接基础资源）。
7. **修 1.20.1 分支筛表钴差异**：沙/砂砾/尘土各缺 1 条 1/64（基表 113 vs 116），
   决定是补生成器还是手工对齐。

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
- **`#minecraft:saplings` 包含杜鹃 / 盛开的杜鹃**（1.21.1 与 1.20.1 都是），它们会按
  树苗标签的 125 堆肥；别把它们当成标签外物品再补 100。菌岩孢子不在任何原版标签里，
  才需要 `ModItemTags` 与 `compost/default.json` 两处都加。
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
- **`art/items/sieve_extras/generate.py` 曾缺 `CHARS` 和 `SPRITES` 的收尾**（`20de823` 删块时带走），
  重跑会 `NameError`；本次补回并把菌岩孢子并进同一脚本（重跑能生成同样像素）。
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
- **IE 1.20.1 的锭 id 是 `ingot_aluminum`**（不是 `aluminum_ingot`），模组 id 是
  `immersiveengineering`；它的 crusher 输入只认 `forge:ores/<m>`（1.20.1），不吃 `c:ores`。
  IE 没有 1.21.1 版，主线只保留候选 / 变体。
- **IE 在专用服会打 `DISTXFORM` 报错**：那是 IE 自己尝试加载 client 类，server 仍然 `Done`，
  不要当成本项目的错误。
- **Tinkers 3 已经移除阿迪特**：3.12.1 本体没有 `molten_ardite` / `ardite_ingot`（只有残留贴图），
  阿迪特由 **工匠巧思（`tinkers_thinking`，只有 1.20.1）** 提供；我们筛**自己**的 `ardite_broken`
  走三档链，再熔成 TiT 的锭（或没装时熔成自有锭）。不要在 Tinkers 3 上自己造阿迪特。
- **条件物品不能交给 datagen**：`sky/ender_slime_grass_seeds` 只在 `tconstruct` 在场时注册，
  `runData` 没有匠魂 → 不会生成模型；这两个模型/贴图必须手写。条件物品的创造栏也要 null 检查。
- **匠魂熔炼的 `result` 是流体**（`{tag, amount}`，没有 `item`/`id`）：`check-data-paths.py`
  现在只对带 item/id 的 result 检查版本键，否则 78 条熔炼会被误报。
- **筛表 `mod` 字段**：`"mod": "tconstruct"` 的条目在模组缺失时**在查物品之前**跳过，
  这样条件物品不会刷 unknown item；没有这个字段的条目行为不变。
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
- **AE2 1.20.1 dev 必须 deobf**：生产 jar 直丢 `run/mods` 会在 ForgeGradle dev 里报
  `@Shadow field f_129762_ was not located`（SRG mixin），服务端起不来；放 `libs/` 由
  `-PtechMods` 走 `fg.deobf`。1.21.1 NeoForge dev 用官方名，生产 jar 可直接进 `run/mods`。
- **AE2 是可选依赖**：不写 `mods.toml`；`sieve/sand_ae2.json` 根级 `mod` 门保证缺模组时
  在查物品之前整文件跳过。发布 jar 不带 AE2 / GuideME。
- **TiT 阿迪特 / 叶绿素是单源**：`scripts/generate_sieve_assets.py` 的 `SINGLE_SOURCE`
  把两种材料从砂砾/沙子/尘土（以及 gems 的 patch 文件）排除，只保留碎下界岩 / 草方块入口；
  重跑生成器不会把旧的多源条目加回来。
## 常用命令

```bash
./gradlew build
python3 scripts/check-textures.py
python3 scripts/check-core-imports.py                # core 层不得出现 NeoForge/Forge import
python3 scripts/check-data-paths.py                  # 数据目录/字段/命名空间与目标版本一致

# 数据管线（P2）
python3 scripts/port_data.py --out build/port-1.20.1 --force     # 1.21.1 -> 1.20.1 数据
python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1

# datagen（标签 / 掉落 / 配方 / blockstate+model）
./gradlew runData --offline
python3 scripts/compare-recipes.py --expect-old-only '*_mekanism.json'
python3 scripts/compare-models.py --expect-old-only 'models/block:barrel_base'  # 完整清单见 datagen.md §4.4

# 推送（SakuraCat 代理；代理开着时走 ssh.github.com:443；代理没开时直连 22 也可，2026-11-04 实测）
export GIT_SSH_COMMAND='ssh -i ~/.ssh/id_ed25519_github -o IdentitiesOnly=yes -o ProxyCommand="nc -X connect -x 127.0.0.1:12450 %h %p"'
git -c url."ssh://git@ssh.github.com:443/WanConcubine/Ex-Nihilo-Satellitium.git".insteadOf="git@github.com:WanConcubine/Ex-Nihilo-Satellitium.git" push origin main
git -c url."ssh://git@ssh.github.com:443/WanConcubine/Ex-Nihilo-Satellitium.git".insteadOf="git@github.com:WanConcubine/Ex-Nihilo-Satellitium.git" push origin 1.20.1-forge

# 分支（worktree ../Ex-Nihilo-Satellitium-1.20.1）
./gradlew runServer -PtechMods      # 全装 Mek/Thermal/Create/IE/Tinkers/TiT + AE2/GuideME（libs/ 有 jar 才加载）
./gradlew runClient -PdevMods -PtechMods
python3 scripts/generate_ore_variants.py             # 矿物链物品贴图 + 锤表 + lang（JSON 归 datagen）
python3 art/items/tools/restyle.py                   # 锤/钩/蚕/熟蚕/尘土贴图 + 预览
python3 art/blocks/crucible/restyle.py               # 坩埚模型/贴图（Creatio 现代化）
python3 scripts/generate_machine_assets.py           # M10 机器方块/插件/GUI 贴图（模型归 datagen）
./gradlew runServer   # RCON：localhost:25575 / eni（AE2/GuideME 在 run/mods 时 118 drops）
./gradlew runClient
```

> `generate_ore_variants.py`、`generate_sieve_assets.py`、`generate_machine_assets.py`、
> 两个 restyle 脚本都依赖 Pillow；系统 Python 没有时用 DSH 自带 Python 跑。
> 生成器现在**只写 PNG + 手写玩法表**：blockstate / 模型 / 配方 / 掉落 / 标签都归 `runData`。

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
    ├── jei/                           # 筛/桶/堆肥/坩埚/锤/钩/机器 分类
    ├── kubejs/                        # P2：七张表的 KubeJS 绑定
    └── crafttweaker/                  # P3：七张表的 ZenScript 入口
└── datagen/                           # runData：标签、掉落、配方、blockstate+模型
                                       # ModRecipes / ModBlockStates / ModItemModels / WoodTypes
```

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。
JEI 截图与贴图预览在 `docs/images/`。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑/实现记录”，data-format 写“JSON/NBT/命令”，
  textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录；不要把同一份进度抄到多处。
- 新的“本轮”记录先加在 [DESIGN.md](DESIGN.md) 对应章节，本文件只留一行摘要。
- **批次叙事不要堆在这里**：只留「当前批次」；上一批/更早的搬运到 [history.md](history.md)，
  本文件只留一节「历史批次」摘要 + 链接（2026-11-02 整理时搬走过约 250 行）。
