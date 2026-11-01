# 当前状态（交接卡）

> 最后更新：2026-11-01
> 先看这 20 行；细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**主线是 Minecraft 1.21.1 / NeoForge 21.1.x**（1.20.1/Forge 版归档在 tag `legacy/1.20.1-forge`）。
M1–M4、M6 已实现；**M10 自动化也已落地**（电动 / 内能 × 自动筛 / 自动锤 + 6 个插件），
服务端逻辑用 RCON 验过。
**下一步是进游戏验收**：先看 M10 的 GUI / 贴图 / 手感，再补 M4/M6 的画面与右键。
M5/M7 余项（config 扩展、datagen）、M8 月光魔法、M9 发布仍未开始。

## 当前批次（2026-11-01，M10 自动化）

- **四台机器**：`electric_sieve` / `electric_hammer`（FE，4 个插件槽）与
  `fuel_sieve` / `fuel_hammer`（烧原版熔炉燃料，无插件槽）；共享一个 `MachineBlock` + 两个 BE 类型。
- **判定单一来源**：新增 `SieveRolls` / `HammerRolls`；手动筛（`SieveBlockEntity`）、
  手动锤（`ToolEvents` 普通/创造路径）已全部改调它们，机器不再有第二份掉落逻辑。
- **数值**：基础 80 tick（4 秒）/ 件；电动 20 FE/t；内能 200 燃烧值/件（1 煤 = 8 件）；
  速度 ×1.5/×2.5/×4，时运 = 额外 1/2/3 次整表 roll。全部走 `machine.*` config。
- **GUI**：两个 Menu + 两个 Screen，进度/能量/火焰三块仪表改用贴图渐变（进度是箭头形），
  空槽有物品虚影（筛网/锤、原料、燃料、速度/时运），标题右侧实时状态
  （运行中/缺少工具/原料/能量/燃料/输出已满）；176×186，玩家物品栏下移，`物品栏` 不再压住插件槽。
  贴图由 `scripts/generate_machine_assets.py` 生成。
- **管道**：`MachineItemHandler` 只允许从输出 12 格抽取，避免漏斗抽走筛网 / 锤 / 插件；
  电动机器只进不出的能量能力。
- **JEI**：四台机器注册为筛/锤配方催化剂；点击 GUI 进度条用当前输入打开 JEI（空输入则打开分类）。
- **RCON 验证**：内能筛 / 电动筛 / 电动锤（速度 III + 时运 III）/ 内能锤 / 上下漏斗全部通过（见验证表）。
- **未验**：GUI 打开与外观、贴图观感、合成配方、玩家手感。

## 上一批次（2026-11-01 提交，M6）

- **巫水**：自定义流体 + 桶 + 液方块；满水桶 + 菌丝（或蘑菇煲）发酵，`Spore` 进度存 NBT。
- **古代孢子**：沙筛 1/128；右键泥土 → 菌丝、右键牛 → 哞菇。
- **瓷桶**：3 瓷土按原版桶形 → 未烧制瓷桶 → 烧制 → 通用 1000 mB 容器（`fluid_contents` 组件 + `IFluidHandlerItem`）。
- **木坩埚**：11 种木材（含 1.16 菌柄 / 1.20 红树·樱花·竹），0.5 mB/t 被动熔树叶/树苗 → 水，只收水类熔料。
- **木桶**：补齐到 12 种（11 木材 + 石桶），删掉两道外凸箍。
- **蚕**：`silkworm` 右键树叶 → 侵染树叶（BE 记住原树叶、按进度变白、60% 后扩散、破坏掉线），Jade 显示进度。
- **人偶**：5 种（人偶 / 愤怒 / 诡异 / 漂浮 / 守卫者），8 条配方；满桶右键**立即**转换：
  和平掉战利品（烈焰棒 / 末影珍珠 / 潜影壳 / 海晶碎片或海晶砂粒随机），非和平直接生成生物。
- **筛出物**：泥土 → 草种子/甜浆果；沙 → 可可豆/海带/5 种珊瑚/海晶砂粒/海晶碎片；
  砂砾 → 海晶碎片；锤下界岩 → 粉碎下界岩方块（筛石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙）、
  锤末地石 → 粉碎末地石方块（筛紫颂果/末影珍珠）。
- **标签**：删掉 `c:raw_materials`，只保留 `c:ores`（矿砾/矿砂/矿尘方块）。
- **贴图**：人偶采用 Creatio（MIT）底稿重制为 64×64；粉碎方块从原版 netherrack/end_stone 派生。

## 功能现状

### 桶 / 堆肥 / 筛子

- 十二种桶（11 种木材 + 石桶）共享一个 BE；木桶 ≤433 K，石桶不限温；堆肥 1000 分制、50 秒成土；
  接雨水、比较器、破坏返还、管道状态互斥都已实现。
- 桶转化 5 条（水+熔岩、熔岩+水、熔岩+红石、熔岩+荧石、水+尘土→粘土）；
  筛子 4 输入 / 52 掉落（泥土 → 种子/作物，沙 → 仙人掌/甘蔗），三档筛网只影响耐久。
- 矿物链：4 碎块 → 矿方块 → 熔炼成锭；锤矿砾/矿砂掉 4–6、粉尘掉自身。
- **M6 补的筛出物**：泥土 → 草种子（右键泥土变草方块）/甜浆果；沙 → 可可豆/海带/5 种珊瑚/
  海晶砂粒/海晶碎片；砂砾 → 海晶碎片；锤下界岩 → **粉碎下界岩方块**（筛石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙）、
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

### 配置与 JEI

- `config/exnihilosatellitium-common.toml`：`crucible.capacity`、`crucible.meltCapacity`、
  `crucible.heatSpeedMultiplier`，运行时读取。
- `machine.ticksPerOperation` / `energyPerTick` / `energyCapacity` / `burnPerOperation`（M10）。
- JEI 分类：`sieve`、`barrel`、`barrel_compost`、`hammer`、`crook`、`crucible`、`crucible_heat`；
  锤子页隐藏“粉尘→自身”的 no-op 规则（玩法 47，展示 35）。

## 交接清单（下次会话先做）

- **先跑** `./gradlew build` + `python3 scripts/check-textures.py`。
- 进游戏验收（服务端逻辑都已验过，剩下“看得见/摸得着”的）：
  - **M4 坩埚**：1 块圆石只占 12.5%、8 块满 2000、第 9 块被拒；沙子/尘土 100、花岗岩 250；
    桶/瓶抽流体；熔料↔流体显示切换；Jade 熔料剩余 + 热源速度；三个配置键热改；JEI 两页。
  - **M6 人偶**：5 种配方（含镜像摆法）；满桶右键立即出结果（和平掉战利品 / 非和平生成生物）；桶变空。
  - **M6 木坩埚**：11 种变种的外观与合成；树叶/树苗 → 水的手感（0.5 mB/t）；熔岩被拒的提示。
  - **M6 蚕**：右键树叶 → 侵染；变白进度；Jade 百分比；扩散；破坏掉线；创造模式不掉落。
  - **M6 瓷桶**：未烧制 → 烧制；舀水/熔岩/巫水；倒进木桶/坩埚。
  - **M3**：6 锤/2 钩、左键链、右键无反应、矿砾/矿砂 4–6、锤钩 JEI 35/10、蚕动画。
  - **M2/M6 筛子**：6 个输入、74 掉落；JEI 筛子页；粉碎方块平铺料层。
  - **M10 机器**：四台机器右键开 GUI；能量条/火焰条/进度条动；插件槽只收插件、燃料槽只收燃料；
    筛网/锤在 GUI 里显示耐久；破坏机器掉出全部内容；贴图观感；上下漏斗在游戏里再走一遍。
- **未验证**：上面这些画面/交互，以及 P0/P1 修复的玩家实测、桶/筛旧内容目视。
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
| M10 | 电动/内能 × 自动筛/自动锤 + 速度/时运插件；手动与机器共用判定函数 | 服务端✅ / GUI🚧（[DESIGN §27](DESIGN.md)） |

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-31：`BUILD SUCCESSFUL`；JEI 依赖走 `maven.blamejared.com` |
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
| 筛子：M6 新资源 | ✅ | 服务端 `[sieve] 6 inputs, 74 drops`、`[hammer] 49 recipes`；粉碎下界岩/末地石可放置并掉自己 |
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
| M10 电动筛 | ✅ | RCON：`Energy:20000`，101 tick 后 17980（20/tick 对得上）、输入 63、产出铝碎块/海晶结晶 |
| M10 电动锤 + 速度 III + 时运 III | ✅ | RCON：5 秒 5 次（20 tick/次）、输入 64→59、锤 damage 5、20 砂砾（1×4 roll×5） |
| M10 内能锤 | ✅ | RCON：80 tick 一次、输入 63、锤 damage 1、`BurnHalfTicks` 2700、1 砂砾 |
| M10 管道 | ✅ | RCON：下方漏斗只抽到砂砾，锤/插件仍在；上方漏斗把圆石补进输入槽 |
| M10 内能筛（存档实证） | ✅ | 2026-11-01 存档 `新的世界`：`(198,72,-489)` 丝筛网 damage 12、粉碎末地石 52、煤 62，
  产出末地石×5 / 紫颂果×1 / 末影珍珠×2，`BurnHalfTicks` 1470——机器实际已处理 12 次 |
| M10 GUI 引导 | ⚠️ | 虚影 / 悬停提示 / 状态行 / 加高布局已实现；未目视 |
| M10 GUI / 贴图 / 合成 | ⚠️ | Menu/Screen/配方已注册，`build` 通过；贴图未目视、配方未实际合成 |
| P0/P1 修复 | ⚠️ | 破坏返还、管道灌桶、F3+T 分离、红石/荧石未玩家实测 |
| 旧内容目视 | ⚠️ | 桶/筛比例、Jade tooltip 等仍待作者确认；桶的两道外凸箍已按反馈删除（2026-10-31） |

## 下一步（按价值排序）

0. **进游戏验收 M10 + M4 + M6**（当前优先级）：先看 M10 的 GUI / 贴图 / 漏斗手感，
   再补 M4/M6 的画面与右键；清单见上一节。这一环没过就不算完。
1. **M7 余项**：config 扩展、datagen；JEI 已补全。
2. **工程收尾**：advancement 树、mod 图标（`neoforge.mods.toml` 还没有 `logoFile`）、CI 加纹理检查、
   零测试（tech-debt P3 #37）。
3. **M8 月光魔法联动**：目前只有 `MoonlightCompat` 占位。
4. **M9 发布**：等验收与 M7/M8 收尾后再做。

## 已拍板的决策

- **项目名 / 许可证**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`；
  MIT（Forge MDK 仍 LGPL-2.1），见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**：注册只认 jar 内 `materials.json`；表内材料有完整自有链；表外材料不自动发现。
- **方块贴图**：一般 vanilla-sampled；**坩埚例外**：采用 Creatio（MIT）模型/贴图并做模糊 + 5 色现代化，
  原始文件在 `art/blocks/crucible/creatio/`。
- **物品贴图**：自有或采用 Creatio（MIT）；Sequentia 是 CC BY-NC-SA，只作风格对照，不采用。
- **锤/钩**：6 锤 / 2 钩，左键唯一路径；粉粉尘→自身是安全兜底，不作为 JEI 配方展示。
- **坩埚**：熔料与流体分开记，熔料 1:1 产出流体；固体 > 流体显示熔料，反之流体；
  容量/熔料/热源倍率走 config，热源与熔点走数据包。
- **坩埚被破坏时内容物一起消失**（只掉方块本体），不做返还：忠于 1.7.10 原版，
  也避免“先熔满再挖走”当免费流体袋。
- **数据包优先**：所有玩法表可覆盖/新增；单人 JEI 优先服务端表，专用服客户端回退内置镜像。
- **Jade 是可选依赖**：`compileOnly`，不打包；每个 provider 都要有 `config.jade.plugin_<ns>.<uid>` 翻译。
- **月光魔法是可选软依赖**：目前只占位。

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
├── common/
│   ├── block/{Barrel,Sieve,Crucible,UnfiredCrucible}Block.java
│   ├── blockentity/{Barrel,Sieve,Crucible}BlockEntity.java
│   ├── barrel/                        # BarrelRecipes + reload
│   ├── compost/                       # CompostRegistry + reload
│   ├── sieve/                         # SieveRegistry + reload + SieveDrop
│   ├── hammer/                        # HammerRecipes + reload
│   ├── crook/                         # CrookDrops + reload
│   ├── crucible/                      # CrucibleRecipes / CrucibleHeat + reload
│   ├── machine/                       # M10：SieveRolls / HammerRolls / UpgradeItem
│   │   ├── block/MachineBlock.java    # 四台机器共用一个方块类
│   │   ├── blockentity/               # MachineBlockEntity + 电动/内能子类 + MachineItemHandler
│   │   └── menu/                      # ElectricMachineMenu / FuelMachineMenu
│   ├── config/ModConfig.java          # 坩埚/机器配置
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
