# 当前状态（交接卡）

> 最后更新：2026-10-31
> 先看这 20 行；细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**主线是 Minecraft 1.21.1 / NeoForge 21.1.x**（1.20.1/Forge 版归档在 tag `legacy/1.20.1-forge`）。
桶（七材质）、筛子、矿物六态（12 材料 × 3 方块 + 3 物品 + 9 锭）、M3（6 锤 / 2 钩 / 尘土 / 蚕动画）、
M4（坩埚 + 热源 + JEI + 配置）都已实现，已提交 `c1899d0`；
**M4 的服务端逻辑已用 RCON 验收，剩下画面/手感要作者进游戏看**。
**M6 进行中**：巫水 + 孢子已完成（服务端验过、待画面/操作验收）；瓷桶 / 蚕丝 / 娃娃还没做。

## 当前批次（2026-10-31 提交）

- **M3 收尾**：满水 + 尘土 → 粘土块（桶转化 5 条）；8 种砂岩 → 沙/红沙（锤表 47 条）；
  熟蚕（食物 2/0.6，熔炉/烟熏/营火）；锤/钩 JEI 分类（35 / 10 条）。
- **M4 坩埚**：`unfired_crucible` / `crucible` + BE + 流体 capability + 比较器；
  Creatio 模型/贴图现代化（`art/blocks/crucible/restyle.py`）；
  **19 条熔点**、**9 条热源**（含篝火 0.25 / 灵魂篝火 0.35 / 灵魂火 0.4）；
  配置 `capacity` 8000 / `meltCapacity` 2000 / `heatSpeedMultiplier` 1.0；
  熔料 > 流体时内腔显示熔料方块贴图，反之显示流体；Jade 显示熔料剩余 + 热源速度；
  JEI `crucible` / `crucible_heat` 两页。
- **数据包**：筛子/锤子/钩子/桶/堆肥/坩埚熔点/热源全部走 `data/<ns>/*.json`，可覆盖可新增；
  单人 JEI 优先读服务端表（包含数据包），专用服客户端回退内置镜像。详见 data-format §14。
- **提交**：本批（M3 收尾 + M4 + JEI + 文档整理）见 `git log`；上一批为 `797c110`（M3 主体）、
  `92b441d`（status 记录）。

## 功能现状

### 桶 / 堆肥 / 筛子

- 七种桶共享一个 BE；木桶 ≤433 K，石桶不限温；堆肥 1000 分制、50 秒成土；
  接雨水、比较器、破坏返还、管道状态互斥都已实现。
- 桶转化 5 条（水+熔岩、熔岩+水、熔岩+红石、熔岩+荧石、水+尘土→粘土）；
  筛子 4 输入 / 52 掉落（泥土 → 种子/作物，沙 → 仙人掌/甘蔗），三档筛网只影响耐久。
- 矿物链：4 碎块 → 矿方块 → 熔炼成锭；锤矿砾/矿砂掉 4–6、粉尘掉自身。

### 锤 / 钩

- 6 档锤（木/石/铁/金/钻石/下界合金），耐久 64/128/256/32/1561/2031；金/钻石工作台，
  下界合金锻造台。
- **只有左键破坏**（无右键）；`HarvestCheck` + `BreakSpeed` 6.0 让锤表方块可收获且不慢；
  创造模式用 `BlockEvent.BreakEvent` 补掉落；耐久扣真实主手栈。
- 木/骨钩；骨钩叶表概率 ×1.5；蚕是 M6 占位。

### 坩埚（M4）

- 瓷土（粘土球+骨粉）→ 7 瓷土合未烧制坩埚 → 熔炉烧成坩埚。
- 19 条熔点：圆石/石头/砂砾/花岗岩/闪长岩/安山岩（含磨制、深板岩、凝灰岩、方解石）250 mB，
  下界岩 1000 mB，雪块 500 mB 水，冰 1000 mB 水，沙子/红沙/尘土 100 mB。
- 熔料体积 = 产出量（1:1）；默认熔料上限 2000 mB（8 块圆石）。
- 热源必须在正下方；`lit=true` 区分点燃/熄灭的熔炉、篝火。

### 巫水（M6，2026-10-31）

- 自定义流体 `exnihilosatellitium:witch_water`（still/flowing + `FluidType` + 桶 + 液方块），
  紫色贴图由 `art/blocks/witch_water/generate.py` 生成。
- **孢子**：沙筛 1/128；右键泥土 → 菌丝；右键牛 → 哞菇（`common/item/SporeItem`）。
- **发酵**：满水桶 + 3×3×3 内有菌丝，每 tick 进度 `+1 + 菌丝数/2`，满 1000 tick（1 个菌丝 50 秒）
  整桶变巫水；直接递蘑菇煲可无菌丝起酵（同样 50 秒，退回碗）。
- **巫水转化**两条：+沙子 → 灵魂沙；+橡树苗 → 深色橡树苗（`barrel/witch_water_*.json`）。

### 配置与 JEI

- `config/exnihilosatellitium-common.toml`：`crucible.capacity`、`crucible.meltCapacity`、
  `crucible.heatSpeedMultiplier`，运行时读取。
- JEI 分类：`sieve`、`barrel`、`barrel_compost`、`hammer`、`crook`、`crucible`、`crucible_heat`；
  锤子页隐藏“粉尘→自身”的 no-op 规则（玩法 47，展示 35）。

## 交接清单（下次会话先做）

- **先跑** `./gradlew build` + `python3 scripts/check-textures.py`。
- 进游戏验收（当前优先级，服务端逻辑已验过，剩下都是“看得见/摸得着”的）：
  - M4：坩埚放 1 块圆石只占 12.5%，放 8 块才满 2000（第 9 块应被拒）；沙子/尘土 100、花岗岩 250；
    桶/瓶抽流体与倒流体；熔料↔流体显示切换；Jade 熔料剩余/热源速度；改三个配置键看效果；
    JEI 坩埚/热源页。
  - M3：6 锤耐久与合成、左键锤链、右键无反应、矿砾/矿砂 4–6、粉尘掉自身；钩叶表；
    蚕动画；粘土/砂岩/熟蚕；锤/钩 JEI 35/10。
  - 农业链：泥土过筛（种子/土豆/胡萝卜/西瓜/南瓜/甜菜，6 条）与沙筛新增的仙人掌/甘蔗；
    JEI 筛子页应变成 4 个输入。
  - M6 巫水：沙筛孢子 → 泥土变菌丝（或牛变哞菇）→ 满水桶靠近菌丝 50 秒 → 巫水；
    用桶装巫水、倒进另一个桶、+沙子看是否变灵魂沙、+树苗看是否变深色橡树；
    观察巫水贴图/流动、桶图标、JEI 桶转化页多出的两条。
- **未验证**：M4 投放/交互/显示/Jade/JEI、JEI 35 条过滤、服务端优先 JEI、P0/P1 修复、桶/筛旧内容目视、
  泥土筛/沙筛新条目、锤/钩新配方形状、坩埚满时右键、**M6 巫水全部画面/交互**。
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
| 桶：水+尘土→粘土 | ⚠️ | JSON 已写；未实测 |
| 巫水：满水 + 菌丝发酵 | ✅ | RCON：满水木桶 + 2 菌丝，约 25 秒变 `exnihilosatellitium:witch_water`，`Spore` 归零 |
| 巫水：蘑菇煲起酵 | ✅ | RCON：只写 `Spore:1` 无菌丝，约 50 秒后变巫水 |
| 巫水：流体方块 | ✅ | RCON `setblock ... exnihilosatellitium:witch_water` 成功并可被 `execute if block` 命中 |
| 巫水：桶装/倒 | ⚠️ | `witch_water_bucket` 已注册；未玩家实测 |
| 巫水：两条转化 | ⚠️ | JSON 已加载（`[barrel] 7 transforms`）；未玩家实测 |
| 孢子：沙筛 | ⚠️ | `sieve/sand.json` 1/128；未实测 |
| 孢子：泥土→菌丝 / 牛→哞菇 | ⚠️ | 逻辑已写；未实测 |
| 巫水：画面/桶纹理 | ⚠️ | 贴图生成 + 纹理检查通过；未进游戏目视 |
| 桶 JEI | ✅ | 转化 1/2 页、堆肥 1/22 页已目视；[截图](images/jei-barrel-transform.png) |
| 筛子（1.21.1） | ✅ | 作者已实际筛出产物；2026-10-31 服务端日志 `[sieve] loaded 4 inputs, 52 drops`（泥土/沙两档新增未实测） |
| 筛子 JEI | ✅ | 7×3 + 166×60 已目视；[截图](images/jei-sieve.png)（还是 3 输入时的样子） |
| 筛子：泥土 → 种子/作物 | ⚠️ | `sieve/dirt.json` 6 条（小麦 15% / 甜菜·土豆·胡萝卜 6% / 西瓜·南瓜 5%）；服务端已加载，未实测 |
| 筛子：沙 → 仙人掌/甘蔗 | ⚠️ | `sieve/sand.json` 补 2 条（1/32、1/24）；未实测 |
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
| P0/P1 修复 | ⚠️ | 破坏返还、管道灌桶、F3+T 分离、红石/荧石未玩家实测 |
| 旧内容目视 | ⚠️ | 桶/筛比例、箍、Jade tooltip 等仍待作者确认 |

## 下一步（按价值排序）

0. **M4 画面验收（当前优先级）**：进游戏放坩埚，试 1 块 vs 8 块圆石、沙子/尘土/花岗岩、
   熔料↔流体切换、桶抽流体、Jade、JEI 两页——**数值/热源/容量/比较器/配置热改已经 RCON 验过**。
1. **M3 验收**：6 锤/2 钩/右键无反应/4–6 掉落/粘土/砂岩/熟蚕/蚕动画/锤钩 JEI。
2. **补 P0/P1 实测**：玩家破坏返还、管道灌桶、F3+T 后 JEI/服务端表分离、红石/荧石。
3. **M6（进行中）**：~~巫水 + 孢子~~ 已完成待验收；接着瓷桶（可装熔岩）、蚕（侵染树叶→线）、
   娃娃（愤怒/诡异 → 烈焰人/末影人）。
4. **M7**：JEI 已补全，剩余 config 扩展与 datagen。
5. **M2 余项**：已完成（泥土筛 + 沙筛杂物，2026-10-31）；数值待试玩再调。

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
python3 scripts/generate_barrel_variants.py          # 七种桶 JSON
python3 scripts/generate_ore_variants.py             # 矿物链资源/配方/锭
python3 art/items/tools/restyle.py                   # 锤/钩/蚕/熟蚕/尘土贴图 + 预览
python3 art/blocks/crucible/restyle.py               # 坩埚模型/贴图（Creatio 现代化）
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
│   ├── config/ModConfig.java          # 坩埚三项配置
│   ├── material/                      # materials.json 读取（注册期）
│   ├── event/{Common,Capability,Tool}Events.java
│   └── tag/ModTags.java
├── client/                            # BarrelRenderer / SieveRenderer / CrucibleRenderer / ClientEvents
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
