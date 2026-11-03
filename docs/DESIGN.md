# 设计文档：无中生有：众星捧月

> 状态：M1–M4 已实现（M3 收尾 + M4 待验收）/ 最后更新：2026-11-03

## 1. 独立实现原则

本项目是独立实现，不是严格的净室重写。以下规则贯穿始终：

1. **不复制**任何现有 Ex Nihilo 分支（原版、Adscensio、Omnia、Creatio、Sequentia、
   Ex Deorum、Fabrico、Fabricae）的代码、贴图、模型或音效。
2. 需求来自**公开玩法资料**：Wiki、MC百科词条、视频、玩家讨论、配方截图；另外对原版
   GPLv3 仓库做过**只读参考**，仅用于理解功能性思路（内缩桶身、桶内内容物渲染）。
3. 所有代码、贴图、模型、音效、文本由本项目独立编写；原版仓库放在 `reference/`
   （gitignore），不随本项目分发。
4. 不冒充官方：项目名带明确副标题，`mods.toml` 与 README 均声明非官方。
5. 玩法机制（规则、数值、流程）不受版权保护；受保护的是具体表达（代码、美术、文本、
   名称标识）。本项目只借鉴前者。

## 2. 目标与非目标

**定位**

- 本项目是 **1.7.10 版 Ex Nihilo 1 经典玩法的复刻**，并继承
  **Ex Astris（星辉生万物，LoveHoly / insaneau 的 Ex Nihilo 附属）** 的**自动化思路**：
  让当年那套“一棵树起步、一套完整资源链”的空岛体验，在 1.21.1 NeoForge 上重新成立。
- 之所以做：`无中生有-世间万物`（Omnia）曾经做到过这件事，但已停更；经典循环不该断在这里。
- **联动是本职，不是附加项**：Ex Nihilo 系本身不产矿，它的价值就是把别的模组的资源接进空岛。
  所以“给别的模组做支持”是一等公民需求，写法见 §6。
- 复刻的是**结构与流程**：方块种类、判定顺序、产出链条、交互手感、失败/返工回路。
  不是数值与美术的照抄（那条写进下面的非目标）。

**目标**

- Minecraft 1.21.1 / NeoForge 21.1.x / Java 21。
- 还原“空岛从无到有”的核心循环，并补齐 Ex Nihilo 系缺失的现代体验（数据驱动、JEI、配置）。
- **经典项全齐**：1.7.10 版有的方块与机制逐项落地，清单与完成度见 [parity.md](parity.md)。
- **自动化支柱**（承自 Ex Astris）：自动筛 / 自动锤 / 升级插件，能源走 FE；
  手动与自动共用同一套判定逻辑，机器只负责供能、计时与抽取（§6.2）。
- **联动框架优先**：第三方不改我方代码，只靠 JSON + tag 就能给筛子加产出、给木桶加堆肥、
  给坩埚加熔炼；硬绑定的东西一律降级为可选 compat 模块。
- 与「月光魔法」软联动（§6.3，首个联动实例）。

**非目标**

- **数值与美术的 1:1 照抄**：复刻结构，不复刻数字。堆肥分数、筛出概率按本项目平衡表定；
  贴图、模型、音效、文本一律自绘自写。
- 支持 1.21.1 以外的版本（后续再评估）。
- 兼容旧版 Ex Nihilo 的存档或配方格式。

## 3. 核心玩法循环

```
树 → 木头 → 工作台/木桶
木桶 + 有机物 → 堆肥 → 泥土
泥土 + 筛子 → 种子/矿粒/杂物
圆石 + 锤子 → 砂砾 → 沙子 → 尘土
砂砾/沙子 + 筛子（不同筛网）→ 矿粒、宝石、矿物
矿粒 ×4 → 粉碎矿 → 冶炼 → 锭
坩埚 + 热源 + 圆石 → 熔岩
水 + 熔岩 → 黑曜石/石头/圆石
树叶 + 钩子 → 树苗/苹果/蚕
蚕 + 树叶 → 侵染树叶 → 丝线
瓷土 → 瓷桶（可装熔岩）
水 + 有机物（发酵）→ 巫水
巫水 + 主世界方块 → 下界/末地资源

（自动化）手动筛/锤 → 供能 → 自动筛/自动锤 → 升级插件提速（M10）
（联动）其它模组的物品 → 数据包写进筛出物/堆肥/熔炼 → 空岛资源链延伸（§6.1）
```

## 4. 技术架构

| 层 | 方案 |
|---|---|
| 注册 | `DeferredRegister`（Blocks / Items / BlockEntities / RecipeTypes / Menus） |
| 方块实体 | `BlockEntity` + `IItemHandler` / `IFluidHandler` capabilities |
| 配方 | 1.21.1 自定义 `RecipeType` + `RecipeSerializer`（JSON，`RecipeInput` + `MapCodec`） |
| 网络 | `SimpleChannel`，仅同步必要的客户端状态（流体、进度） |
| 数据生成 | `DataGenerator`（blockstates / models / loot / tags / recipes） |
| 兼容 | JEI 插件、`mods.toml` optional dependency、事件隔离 |
| 配置 | `ModConfigSpec`（common）；core 只读 `ConfigValues` 门面 |

### 包结构

顶层只有四类：`registry/`（DeferredRegister）、`common/`（core 逻辑）、`client/`（渲染/界面）、
`compat/`（JEI / Jade / 月光魔法），外加每版重写的 `platform/`。core 与平台之间只隔四个窄门面：
`ConfigValues` / `Platform` / `MachineHooks` / `GuiLayers`。

具体文件清单见 [status.md](status.md) 的「文件地图」（唯一出处，别在本文件再抄一份）。

## 5. 里程碑

| 里程碑 | 内容 | 验收标准 |
|---|---|---|
| M0 | 骨架、元数据、Creative Tab、占位方块/物品 | `./gradlew build` 通过，游戏内能看到物品 |
| M1 | 木桶：堆肥 + 流体存储（1000 mB）+ 比较器 | 堆肥成泥土；桶能装/倒水与熔岩 |
| M1.5 | 桶内方块转化（水 + 熔岩 → 石/黑曜石） | 桶内两种流体按规则转化 |
| M2 | 筛子 + 筛网 + 数据驱动掉落 | 不同筛网筛不同材料，JSON 可改 |
| M3 | 锤子 + 钩子 | 锤碎圆石/砂砾，钩树叶出树苗/蚕 |
| M4 | 坩埚 + 热源 | 坩埚熔圆石为熔岩 |
| M5 | 矿粒 → 粉碎矿 → 锭 | 完整矿物链 |
| M6 | 瓷土 / 瓷桶 / 巫水 / 蚕 | 瓷桶可装熔岩，巫水转化方块 |
| M7 | JEI / 配置 / 数据生成 / 平衡 | JEI 显示全部配方 |
| M8 | 月光魔法联动 | 软依赖，独立运行不报错 |
| M9 | 文档、发布、CurseForge/Modrinth | 可公开测试 |
| M10 | Ex Astris 自动化：自动筛 / 自动锤 / 升级插件 | 供能后自动处理，与手动共用判定函数；见 [parity.md](parity.md) |

## 6. 联动与自动化

### 6.1 联动是本职

Ex Nihilo 系自身几乎不生成矿脉，玩家能拿到什么资源，完全取决于“别的模组的物品能不能进
筛子 / 木桶 / 坩埚”。所以兼容层不是彩蛋，是主循环的一部分。四条硬约束：

1. **机制归我们，条目归数据**：核心只暴露“输入 → 判定 → 输出”的通用注册表
   （筛出物、堆肥值、坩埚熔炼、热源），具体模组的条目写在 JSON 里；自定义表按 id 覆盖，不解析条件。
2. **数值走数据包，材料走表**：配方、概率、堆肥值这类**数值**用数据包就能覆盖；
   但**新增材料**（要注册新物品链）在 1.21.1 必须发生在注册期，只能改 jar 内材料表 + 重启
   （§6.4）。需要自定义判定或特殊工具的，才由我们写 `compat/` 模块。
3. **硬绑定必须可缺**：`compat/` 按模组分包，只按注册名查 `BuiltInRegistries`，
   **不 import 对方任何类**；没装该模组时类不加载、不报错（做法见 §6.3）。
4. **注册只认材料表，不做自动发现**（2026-10-30 修正）：内置材料表由我们手写
   （颜色、`tier`、`rarity` 人工定），表内材料**真的在游戏里存在**（整套矿砾/碎矿/粉末链，
   见 §6.4）；表外材料不自动扫描、不生成骨架行。新增材料要改 jar 内表、重跑生成脚本并重启。
   理由：概率必须是人调出来的手感，自动推导（tag 或 worldgen 密度）会飘、会打穿整合包
   进度设计；config 扩展层尚未实现（见 [tech-debt.md](tech-debt.md)）。

   自动发现/骨架行目前未实现；以后若要做，先补 schema 与资源生成，不能只写表。

### 6.2 自动化（承自 Ex Astris）

星辉生万物真正值得继承的是**自动化思路**：把“手动筛 / 手动锤”变成
“供能 → 自动处理 → 插件调节”，让空岛从单人手动升级到可规模化。三条设计约束：

1. **判定单一来源**：筛料、锤碎写成纯函数（材料 + 筛网 + 幸运 → 输出），
   手动方块与自动方块**调同一个函数**。机器只负责供能、计时、抽取，绝不复制一份掉落表。
2. **能源抽象成 FE**：用 `IEnergyStorage`，不直接依赖任何能源模组（1.7.10 的 RF 专有类不用）。
   没装能源模组时的解法（2026-11-01 拍板）：**不做“无电慢速”，而是做两档机器**——
   - **电动**：必须 FE，可装速度 / 时运插件，是后期规模化路线；
   - **内能**：烧原版熔炉燃料（`ItemStack#getBurnTime`），不需要任何能源模组，**没有插件槽**，
     是单人空岛 / 无能源整合包的前置路线。
3. **插件化**：速度 / 时运（产量）作为 upgrade 物品，只调判定函数的参数，不改判定本身；
   同类词条也可以挂在手动工具上（可选，视平衡）。

与 M2 的接口：M2 交付手动筛时就把判定抽成纯函数 + JSON 表，M10 的自动筛只接电与计时，不返工。
M10 落地时把这条真正兑现了：`SieveRolls` / `HammerRolls` 成为唯一判定入口，手动筛、手动锤、
四台机器全部调它们（见 §27）。

### 6.3 首个联动对象：月光魔法

**对接对象**（本机 `Projects/Minecraft-Mod-(Neo)Forge/1.20.1`；1.21.1 端口待确认）：

| 项 | 值 |
|---|---|
| mod id | `moonlight_magic` |
| 版本 | 1.2.0 |
| 包名 | `com.moonlight.magic` |
| 加载器 / MC | Forge 47.4.x / 1.20.1（旧资料） |
| 许可证 | MIT |
| 公开 API | **没有独立 `api` 包**（有 `event` / `ritual` / `spell` / `energy`，但都是内部实现） |

**策略**

- `mods.toml` 里声明 **optional** 依赖（`mandatory=false, ordering=AFTER`），
  不装月光魔法时本模组必须能独立启动。
- 代码侧统一走 `compat/MoonlightCompat`：只检查 `Platform.isModLoaded` + 按注册名查
  `BuiltInRegistries`，**不 import 对方任何类**，避免编译期耦合和版本脆断。
- 内容联动优先用**数据包**（配方 / 掉落 / 堆肥值）；自定义表按 id 覆盖，不解析条件。
  代码只负责少量运行时判断。
- 不碰对方内部类；如果将来对方提供正式 API，再替换 `MoonlightCompat` 的实现。

**候选联动点**（M8 落地，需和玩家确认数值）

| 方向 | 内容 |
|---|---|
| 月光 → 无中生有 | `moonlight_thread` 做「月光筛网」；`pool_dust` 作为稀有筛出物 |
| 月光 → 无中生有 | `moon_flax` / `rainbow_flower` 等植物进木桶堆肥 |
| 无中生有 → 月光 | 本模组矿粒 / 瓷土作为月光仪式或合成材料 |
| 双向 | 月光魔法素材 → 坩埚熔炼产物；本模组尘土 → 月光肥料 |

具体配方等 M2（数据驱动掉落）和 M6（瓷土/巫水）完成后再定，避免现在写死。

**联调测试注意**

月光魔法的 1.2.0 是**生产 jar（reobf / SRG 名）**，直接丢进本项目的 `run/mods/` 会在 dev
环境报 `NoSuchFieldError: f_279569_`（例如 `ModCreativeTabs.<clinit>`）——这不是双方代码
的问题，是 dev 用 official 名、生产 jar 用 SRG 名的映射错位。联调要用**正式 Forge 服务端 /
客户端**（两边都放 reobf jar），或者让月光魔法提供 deobf 依赖后再走 Gradle。

### 6.4 材料表与矿物链（自持）

**为什么自持**：别的模组说“我们有铜”，指的是它有铜矿石；无中生有说“我们有铜”，指的是
**我们注册了铜矿砾、破碎铜、粉碎铜、粉末铜，以及熔炼出的铜锭**。资源身份属于这个循环，
而不是把对方的物品贴到筛子上——贴上去的话，我们只是个改战利品表的补丁。

原版 1.7.10 就是这么做的（`OreRegistry.createOre`）：每种矿建 `gravelTable` /
`sandTable` / `dustTable` 三张方块表 + `brokenTable` / `crushedTable` / `powderedTable` /
`ingotTable` 四张物品表，**每种矿带一个 `Color`**，贴图在运行时由 Base+Template+Color 合成。

**链条**（每材料一套，四步）：

```
筛 砂砾 → 破碎矿    筛 沙子 → 粉碎矿    筛 尘土 → 粉末
破碎 ×4 → 矿砾方块    粉碎 ×4 → 矿砂方块    粉末 ×4 → 矿尘方块
锤 矿砾 → 粉碎矿      锤 矿砂 → 粉末      （对方模组的矿石方块 → 破碎矿）
熔炼 矿砾 / 矿砂 / 矿尘 → 锭（可复用对方已有的锭，避免重复定义金属）
```

**两层来源**：

| 层 | 位置 | 谁写 | 默认 |
|---|---|---|---|
| 内置材料表 | jar 内 `exnihilosatellitium/materials.json` | 我们 | 启用 |
| 数据包 | `data/*/...` | 任何人 | 覆盖配方与概率；**不能新增材料** |

**硬约束：新增材料必须重启。** 1.21.1 的物品/方块在注册期冻结，材料表必须在注册阶段读取，
所以它不能是数据包，只能是 jar 内资源。数据包能改的是概率与配方，
不能“让一个新物品凭空出现”。

**supported mods（2026-11-01）**：`materials.json` 的 `ingots` 是**有序候选列表**
（`{"item": "mekanism:ingot_tin", "mod": "mekanism"}`）。启动时按顺序取第一个已加载模组的锭；
都不在就注册自有锭（经典 Ex Nihilo 的 “create the ingot if no other mod supplies one”）。
生成脚本把每个候选写成带 `neoforge:mod_loaded` 条件的熔炼配方，base 配方再用
`neoforge:not(...)` 兜底到自有锭——JSON 条件与 Java 解析走同一条顺序，加载时永远只有一个
配方生效，不需要运行时注入配方。

- 首个接入：**Mekanism 1.21.1**（`mekanism:ingot_tin` / `lead` / `osmium` / `uranium`），
  覆盖我们材料表里的 tin / lead / osmium / uranium。
- **Thermal 系列只有 1.20.1**：接在 **1.20.1 分支**（项目双版本目标，见 §7）；1.21.1 侧等它出
  1.21.1 版后加一行 item id 即可，不用改代码。
- 换模组会改变“哪些锭存在”：已有世界里已经拿到的自有锭，在装上受支持模组后会变成未知物品
  （日志 `Unknown registry key`）。**开世界前定好模组列表**；这是经典行为，不做运行时迁移。

**贴图（两条线，都不手绘方块底图）**：

- **物品**（破碎 / 粉碎 / 粉末）：5 阶调色板手绘像素，已完成的 11 种见
  `art/items/ore_pieces/generate.py`；新金属 = 加一条 `PALETTES`。
- **方块**（矿砾 / 矿砂 / 矿尘）：**单层 tint**。一个完整 cube，六面 `tintindex: 0`，
  底图引用 `minecraft:block/gravel`、`sand`、`gray_concrete_powder`（不复制、不打包），
  颜色由 `BlockColor` 给。
  早先试过“底图 + 叠加遮罩”，在物品栏图标里叠加层不出现、内缩底块还会留缝，已废弃。
  代价是整表面 100% 染色，原版 21% 斑块的观感丢了；要恢复得做运行期合成贴图。
材料表因此必须带颜色；没有颜色的材料不给注册（宁可报错，也不要一堆灰方块）。

**JEI（2026-10-29 修正）**：筛料表**不是** vanilla `RecipeType`，所以 JEI 不会自动读到。
我们写了一个可选分类 `exnihilosatellitium:sieve`，直接读 `SieveRegistry`（与结算共用同一张表），
输入在左、全部掉落 + 独立概率在右；筛子方块与三档筛网注册为 catalyst。JEI 只 `compileOnly`，
不打包；没装 JEI 时插件类不加载。客户端需要自己的 `SieveReloadListener`（见 §12）。
桶的转化/堆肥在 2026-10-30 也接了 JEI：`BarrelRecipes` + `CompostRegistry` 直接喂给
`barrel` / `barrel_compost` 两个分类，见 §14。



## 7. 待定问题

- 模组英文名最终定为 **Ex Nihilo: Satellitium**，中文名「无中生有：众星捧月」；
  *Satellitium* 是拉丁语“随从/卫星群”，对应“众星捧月”。
- 是否需要 Skyblock 世界类型（Ex Deorum 有，工作量不小）？
- 是否提供 1.20.1 之外的版本分支？
- **自动筛 / 自动锤在没装能源模组时的行为**：慢速无电运行，还是干脆不启动？
  （倾向：无电可用但极慢，保证单人空岛也能起步，待 M10 定。）
- **匠魂 3 的金属表已改**（阿迪特移除）：Ex Astris 那条“碎下界岩过筛出钴/阿迪特，约 1%”
  要不要改写成只出钴？条目归属见 [parity.md](parity.md) §2.3。
- ~~ARR 是否长期保留~~ → **已转 MIT（2026-10-27）**，代码与自绘贴图均按 MIT 发布。

## 8. M1 实现记录（2026-10-27，含内缩模型 + MIT）

**已完成**

- `BarrelBlock` + `BarrelBlockEntity`：1000 mB `FluidTank`、`IFluidHandler` capability、NBT 持久化、客户端同步。
- 堆肥：`#exnihilosatellitium:compostable` 物品标签负责客户端交互预测；
  `data/<namespace>/compost/*.json` 提供数值（默认 65 条）；满 1000 后 1000 tick 进入 DIRT 状态，
  **木桶保留**；右键取出 1 个泥土并重置，桶可继续用。
- 比较器输出：DIRT 状态固定 15；否则 `max(流体比例, 堆肥比例)` 映射到 0–15。
- **模型**：从 16×16 满方块改为 **14×16×14 内缩桶身**，`getShape` / `getCollisionShape`
  同步内缩；采用 **vanilla-sampled**：桶身直接引用 `oak_planks` / `spruce_planks`（侧面
  `rotation: 90`）。2026-10-31 按作者要求删掉了两道外凸箍（原来 8 个 0.5px 体素）。
  2026-11-01：前后墙的 **1×15 端面**（外侧四条竖棱）去掉了 `rotation: 90` —— 1 格宽的
  UV 矩形再旋转会把木纹压成 15:1 的细条纹，看起来像棱上有一排乱线；不旋转后是干净的
  1px 木纹边（作者反馈，离线渲染确认过）。
- **物品模型**：恢复为开口 block model（移除封闭盖），靠 `display` 变换在物品栏/手持
  显示 3D 空心桶。
- **客户端 `BarrelRenderer`**：液面 + 堆肥/泥土内容 box，按 fill 高度上升；堆肥计时期间
  颜色向泥土过渡，完成时播放 `COMPOSTER_READY` + 堆肥粒子；流体走
  `IClientFluidTypeExtensions`，堆肥走 `barrel_compost.png`，DIRT 状态走原版泥土贴图。
- **物品栏**：block model 补了标准 `display` 变换（gui/ground/手持）；`models/item/barrel.json`
  直接继承开口 block model，物品栏/手持是**空心**的 3D 桶。
- **贴图**：木桶不再有自有 PNG（直接引用 vanilla block 贴图）；只保留 `barrel_compost.png`
  和 `porcelain.png` 两张自有 16×16。`scripts/check-textures.py` 禁止 vanilla **item**
  贴图，允许 vanilla **block** 贴图。
- **许可证**：ARR → **MIT**；`gradle.properties`、`LICENSE`、`THIRD-PARTY.md` 同步。

**验证**

- `./gradlew build` 通过（无警告）；jar 内 `license="MIT"`、贴图与模型均打包。
- `python3 scripts/check-textures.py` 通过，assets 下已无 `minecraft:` 贴图引用。
- 开发服 RCON 实测：
  - `setblock ... exnihilosatellitium:barrel` + `data merge {Compost:1000,CompostTicks:1}`
    → 2 秒后**仍是 barrel**，`DirtReady=1`、`Compost=1000`（`execute if block` 返回 Test passed）。
  - `data merge {FluidName:"minecraft:lava",Amount:1000}` 后 `data get` 读回
    `minecraft:lava` / `1000`。
  - 标签加载无 `TagLoader` 报错，日志 `[compost] loaded 30 entries`。

**与原版的关系**

原版 Ex Nihilo 是 1.7.10 的 TESR + Techne 模型：约 12.8×16 的桶身，`setBlockBounds(0.1..0.9)`，
内容物由 `ModelBarrelInternal` 在液面高度画 quad，贴图是 128×128 图集。
本项目只借鉴了“内缩桶身 + 内容物表面”这一功能性思路，使用 1.20.1 JSON 模型、自有代码和
vanilla-sampled 贴图（堆肥面/瓷土为自有 16×16）；没有复制原版代码、模型或贴图。
原版仓库仅本地只读参考（`reference/`，gitignore）。

**踩过的坑**

- `FluidTank` 在 Forge 47.4.28 的 NBT 是**顶层** `FluidName` / `Amount`，不是
  `{Fluid:{...}}`；写 datapack/命令时别套错。
- `#minecraft:crops` / `#minecraft:seeds` 不是 1.20.1 的物品标签，会整条标签加载失败；
  改用具体物品 id。`minecraft:carrots` / `minecraft:potatoes` 是方块 id，物品是
  `minecraft:carrot` / `minecraft:potato`。
- Forge 47.4.x 把 `BlockBehaviour#use` 和比较器方法标了 `@Deprecated`，但 1.20.1
  没有替代；`BarrelBlock` 用 `@SuppressWarnings("deprecation")` 抑制并注明原因。
- 内缩模型的外侧面**不能加 `cullface`**：墙在 z=1/x=1，不在方块边界，加了会在相邻方块
  旁边被整面剔除，露出空洞。

**未验证 / 下一步**

- 液面、堆肥/泥土 box、内缩模型与材质贴图**没有进游戏目视**（headless 只能保证编译、
  JSON、贴图引用和逻辑）；需要一次客户端截图验收。
- 右键取出泥土没有自动化测试（需要真实玩家）；逻辑已按原版 `ExtractMode.Always` 实现。
- 比较器没有实测（RCON `setblock` 出来的比较器读不到 BE，属于测试方式问题）。
- 桶内方块转化（水 + 熔岩 → 石/黑曜石）未做，留到 M1.5。
- 切角八边形还没做；外凸箍已按反馈删除，当前就是 14×16 方桶身。

## 9. Jade（玉）集成（2026-10-27）

- 依赖：`compileOnly "maven.modrinth:jade:11.13.3+forge"`（Modrinth Maven）。
  **不打包、不随我们的 jar 分发**，只用于编译插件。
- `mods.toml`：`jade` optional dependency，`[11.0.0,)`，`ordering=AFTER`，`side=CLIENT`。
- `compat/jade/BarrelJadePlugin`（`@WailaPlugin`）+ `BarrelComponentProvider`：
  只读已经同步到客户端的 `BarrelBlockEntity`，显示：
  - `堆肥：x / 1000`
  - `还需 n 秒`
  - `泥土已就绪（右键取出）`
  - 流体：`水：x / 1000 mB`
- 没装 Jade 时：`@WailaPlugin` 类不会被 Jade 扫描/加载，模组独立运行。
- 验证：编译通过；无 Jade 的服务端启动正常；jar 内只有我们的插件类，没有 Jade 类。
- 未验证：真实 Jade 客户端里的 tooltip 渲染（需要装 Jade 进游戏看）。

## 10. M1 材质变体（2026-10-28）

**决策**

- 沿用 **vanilla-sampled**，不把 `art/barrel/*` 的早期自绘图塞进 release。
  木桶外观由原版木板/去皮原木/石头组合；这样所有材质共用一套几何，也不复制 GPLv3 PNG。
- 几何与 `display` 抽到 `models/block/barrel_base.json`；每种材质是只覆盖 `textures`
  的**子模型**，避免七份 560 行 JSON 漂移。
- **七种材质**：橡木（保留 `barrel` id）、云杉、白桦、丛林、金合欢、深色橡木、石头。
  1.20 新增的 mangrove / cherry / bamboo 暂不做，避免一次把变体表铺得太开。
- 七种桶共享 **一个 `BlockEntityType` + 一个 `BarrelBlock` 类**；行为、NBT、比较器、
  Jade tooltip 完全一致。石桶复制 `Blocks.STONE` 属性（镐、石音效），木桶复制对应木板属性。
- 配方从“任意木板 → 橡木桶”改为**每种木材一个具体配方**；否则 `#minecraft:planks`
  会和具体木种配方同时匹配，产物不可预测。石桶用 `stone`。
- 数据文件最初由 `scripts/generate_barrel_variants.py` 生成：blockstate、block/item model、
  shaped recipe、loot table、`mineable/{axe,pickaxe}` 标签都来自同一张材质表。
  **2026-11-02 起改由 datagen 接管**（`datagen/WoodTypes` 一张表 + `ModBlockStates` /
  `ModItemModels` / `ModRecipes` / `ModBlockTags` / `ModLootTables`），该脚本已删除。
- `art/barrel/render_current.py` 改为解析 parent 链，一次渲染七种材质的物品图标和
  3/4 视图，输出 `showcase_variants*.png`；早期自绘脚本保留但不再代表游戏内模型。

**验证**

- `./gradlew build` 通过。
- `python3 scripts/check-textures.py` 通过（七个子模型只引用 vanilla **block** 贴图）。
- 服务端 RCON 逐种 `setblock` + `data merge` 验证 BE 共享、NBT 读写和方块保留。
- 未验证：进游戏目视七种桶的比例、箍的位置、物品栏图标；预览图只能保证模型/贴图解析正确。

**坑**

- 子模型只写 `parent` + `textures` 时，纹理检查脚本仍会遍历到；`barrel_base` 本身没有
  `textures`，不要把它当可直接引用的方块模型。
- 共享 BE 类型后，`BlockEntityType.Builder.of` 必须把七种方块全部列进去；漏一个会在
  放置时 `BlockEntity` 创建失败或 `getTicker` 返回 null。
- 木种配方如果保留旧的 `#minecraft:planks` 版本，会和具体木种配方冲突；已删除。

## 11. 堆肥数值校正（2026-10-28）

**问题**

最初的 `default.json` 是拍脑袋的一版：树苗 8、树叶 4、苹果 4、面包 6、干草块 18……按 100 分制
换算后，树苗要 13 个才满一桶，和原版 Ex Nihilo 的 8 个明显对不上；肉类、鱼、南瓜派、
西瓜块、蜘蛛眼等经典条目也缺失。

**来源与取舍**

- **原版 Ex Nihilo 1.7.10**：本地只读参考 `reference/ex-nihilo-original` 的
  `CompostRegistry.load()`。桶容量是 0..1 float（树苗/树叶 0.125、花/蘑菇 0.10、
  小麦 0.08、面包 0.16、肉 0.20、鱼 0.15、瓜块 1/6）。
- **Ex Nihilo Omnia 1.10.2**：`CompostRegistry.registerVanillaRecipes()` 直接用 1000 mB
  写值（默认 125），是原版比例在 1.10 的延续/微调。本项目以它为**主表**：
  树苗/树叶 125、花/蘑菇 100、小麦/腐肉/蜘蛛眼 85、面包 125、南瓜派 175、鸡蛋 80、
  生熟肉 175、鱼 120、苹果 100、金苹果 200、西瓜片 40、西瓜/南瓜块 150、仙人掌 100、
  胡萝卜/土豆 80、睡莲/藤蔓/下界疣/甘蔗 80、线 40、恶魂之泪 5。
- **Ex Nihilo: Sequentia 1.20.1**：只用它的 **1000 分制刻度**，不采用它的重平衡
  （它把肉调到 200、鱼 150、面包 160、瓜块 166，和 Omnia 1.10.2 不一致）。

**高版本适配**

- 1.20 新植物（海带、甜浆果、发光浆果、竹子、孢子花、大/小滴水叶、海草、红树根、
  绯红/诡异菌与根、垂根）按 Omnia 的“植物/菌类”类别取 **100**。
- 1.20 新种子（火把花种子、瓶子草荚）按种子取 **80**；甜菜根按根茎作物取 **80**。
- 藤蔓类（藤蔓、垂泪藤、缠怨藤）取 **80**；单格草/蕨取 **80**，双格高草/大蕨取 **100**。
- 不能映射的 Omnia 条目（灰烬、蚕、蚕蛹等）等对应物品做出来后再加。

**落地**

- `COMPOST_MAX = 1000`、`COMPOST_TICKS = 1000`（50 秒，对齐原版 `MAX_COMPOSTING_TIME`）。
- `default.json` 改为 **65 条**（12 个标签 + 53 个具体物品），`compostable.json` 同步；
  肉类按 1.20.1 的十种生/熟肉显式列出，鱼用 `#minecraft:fishes`，种子/作物/蛋/线用
  `#c:*` 标签（2026-10-30 从 `#forge:*` 修正：1.21.1 的 NeoForge 只提供 `c:` 命名空间，
  旧写法与标签文件不一致，导致这些条目此前只按 1 点堆肥）。
- 老存档迁移：`saveAdditional` 写 `CompostScale:1000`；`load()` 发现没有这个键时把
  `Compost` ×10、`CompostTicks` 按 600→1000 缩放，旧测试桶不会变成 1/10 进度。
- 150 的瓜类 7 个是 1050，桶会截到 1000，和 Omnia “多塞一点会溢出但不会浪费”一致。

**验证**

- `./gradlew build` 通过；`python3 scripts/check-textures.py` 通过。
- 服务端 `/reload` 日志：`[compost] loaded 65 entries`，无 `unknown item` / `invalid tag`。
- RCON：`data merge {Compost:1000,CompostTicks:1}` → 2 秒后仍是桶、`DirtReady=1b`。
- 未验证：逐个物品右键塞桶的点数（需要玩家）；JSON 与标签的键已用脚本对齐。

## 12. M2 收尾：JEI 与筛出物的去向（2026-10-29）

**问题**

M2 的筛子能出货，但两个缺口让循环看起来断掉：JEI 里搜不到“砂砾能筛出什么”；
筛出来的破碎/粉碎/粉末没有任何配方，等于死物品。

**决策**

1. **JEI 不假装成 vanilla 配方**。筛料表是 `SieveRegistry` 里的 JSON，不是 `RecipeManager`
   的 `RecipeType`；硬塞一个假的 `RecipeType` 会多一份要同步的数据。做法是写可选分类
   `exnihilosatellitium:sieve`，直接读同一张表——结算与展示只有一个来源。
   - 左上材料、左下筛网、右侧 7 列掉落；槽位在 166×60 页面里居中（x=6/34），两条筛料叠成一页。
     概率放在每个产物槽的 tooltip 里（`概率：5.56%`），
     因为 17 个产物同时铺在页面上放不下文字。
   - 筛子方块 + 三档筛网都注册为 catalyst；查筛网也能看到分类。
   - JEI 只 `compileOnly`；没装时 `@JeiPlugin` 类不会被加载。
2. **客户端也要读筛表**。`AddReloadListenerEvent` 只覆盖服务端资源；单人时集成服会加载，
   但连专用服时客户端不会读服务端数据包，JEI 会是空的。`ClientEvents` 在
   `RegisterClientReloadListenersEvent` 里再挂一个 `SieveReloadListener`。客户端的
   `ResourceManager` 是 `CLIENT_RESOURCES`，`listResources` 只找 `assets/`，所以
   `processResources` 把 `data/.../sieve` 镜像到 `assets/.../sieve`（仓库里仍只有 data 一份）。
   代价：专用服的**数据包覆盖**不会同步到客户端 JEI（显示内置表）；内置 JSON 永远一致。
3. **筛出物接回矿物链**（复刻经典结构，不抄数值）：
   - 4 个破碎/粉碎/粉末 → 对应矿砾/矿砂/矿尘方块（2×2）。
   - 矿砾/矿砂/矿尘方块 → 熔炼 → 1 锭（0.1 xp，200 tick）。
   - 铁/金/铜复用原版锭；其余材料在表里省略 `ingot` 时注册自有 `<id>_ingot`。
     这是原版 `createOre(..., existingIngot, ...)` 的行为：有别的模组的锭就复用，
     没有就自己造一个，保证单人空岛也有终点。
   - 锭贴图 16×16、自有像素（`art/items/ingots/generate.py`，与矿粒同一套 5 阶色板）。
4. **潜行空手**：有材料先倒出材料，没材料才取回筛网。原实现先判 `hasMesh()`，
   `ejectContents` 永远走不到。

**验证**

- `./gradlew build` 通过；`check-textures.py` 通过（9 张锭贴图 16×16）。
- 专用服启动：`Loaded 1373 recipes`、`[debug] our recipes=83`（11 旧 + 72 新）、
  `[sieve] loaded 2 inputs, 29 drops`。
- RCON 熔炉：`iron_ore_gravel → minecraft:iron_ingot`、`tin_ore_gravel → tin_ingot`，
  `RecipesUsed` 命中本项目配方；原版铁矿石对照也正常。
- 客户端：进单人世界，JEI 启动无异常，日志 `[jei] registered 2 sieve recipes`。
- 未验证：JEI 页面实际排版/概率 tooltip 目视；2×2 合成由玩家实测。

**坑**

- Modrinth 的 maven 在“同版本号多 loader”时会返回 Fabric jar；JEI 必须用
  `maven.blamejared.com` 的 `mezz.jei:jei-1.21.1-neoforge`。
- RCON 用 `data merge` 往熔炉塞物品会绕过 `setItem()`，`CookTimeTotal` 停在 0，
  进度永远不完成；测试时要手动写 `CookTimeTotal:200`。
- `/tick step N` 仍按 20 TPS 跑，中间 `/tick unfreeze` 会取消剩余步进。

## 13. M1.5 桶内转化（2026-10-29）

**来源**：[MC百科 · 木桶（Ex Nihilo: Sequentia）](https://www.mcmod.cn/item/465510.html)
补齐了 [ex-nihilo-basics.md](ex-nihilo-basics.md) 里的待核项：巫水来源、木/石桶温差、
凝固与掺料表、雨水。数值仍用本项目的表，不搬 Sequentia 的重平衡。

**决策**

1. **木桶限温 433 K**：水 300 K、熔岩 1300 K。判定放在 `FluidTank.fill` 里，
   所以桶、漏斗/管道、桶倒全走同一条路；消息在 `use` 里提前提示，不静默失败。
   石桶是唯一不限温的变体（七种桶里只有它）。
2. **凝固用 `Solid` 状态**：满水 + 熔岩 → 圆石；满熔岩 + 水 → 黑曜石。两种触发都支持：
   - 玩家用桶倒（`FluidUtil` → `tank.fill`，满且异种流体时直接结算）；
   - 桶上方放着熔岩/水源（`serverTick` 检查 `pos.above()`，对应经典玩法）。
   产物存成 `ItemStack`，右键取出；不替换方块、不额外做 GUI。
   往满水木桶倒熔岩是允许的——熔岩立刻变圆石，不会“存”在木桶里。
3. **接雨水**：露天的桶（`isRainingAt`）按 biome rainfall 累积；每 20 mB 同步一次，
   避免每 tick 发包。0.5 降雨约 100 秒满。
4. **熔岩掺料**：满熔岩 + 红石 → 下界岩；+ 荧石粉 → 末地石。
   水 + 尘土 → 粘土块、巫水 + 沙子 → 灵魂沙、巫水 + 蘑菇 → 粘液块都**没做**
   （尘土/巫水本身还没做），代码里不硬塞空配方。
5. **Jade / 渲染 / 比较器**：`Solid` 用内容方块贴图画在桶内；Jade 显示
   `内含：圆石（右键取出）`；比较器在 `Solid` 状态固定 15。

**验证**

- `./gradlew build` / `check-textures.py` 通过。
- RCON：水桶上方放熔岩源 → `Solid: minecraft:cobblestone`、流体清空；
  石桶装熔岩、上方放水 → `Solid: minecraft:obsidian`。
- RCON：`/weather rain` 下 12 秒积水 80 mB（降雨 0.5）。
- **未验证**：木桶拒热与右键掺料需要真实玩家（dispenser 不走流体 capability，测不了）。

**坑**

- **NeoForge 21.1 的 `FluidTank` NBT 是嵌套 `{Fluid:{id,amount}}`**。旧 1.20.1 文档里的
  顶层 `FluidName` / `Amount` 在这里会被静默忽略；RCON 测试必须用嵌套格式。
- `FluidUtil.getFluidContained` 要在 `FluidUtil.interactWithFluidHandler` 之前判断，
  否则木桶拒热只能表现为“没反应”，玩家不知道要用石桶。
- 熔岩凝固必须“满 + 异种流体”才结算；半桶时 `FluidTank` 默认不接受异种流体，正好。
- 雨水用批量（20 mB）而不是每 tick `fill`，否则 `onContentsChanged` 会每 tick 同步。

## 14. 桶转化数据驱动 + JEI（2026-10-30）

**问题**

桶的“能产什么”写在 `BarrelBlockEntity.solidResult` / `itemMixResult` 的两个 `if` 里：
JEI 看不到，加一条转化要改 Java；`compost` 虽然数据驱动，但 JSON 里 8 条 `#forge:*`
标签在 1.21.1 不存在，标签文件却写着 `#c:*`，于是这些物品只按 1 点堆肥。

**决策**

1. 建 `common/barrel/BarrelRecipes`，从 `data/<ns>/barrel/*.json` 读：
   `base`（必须满桶的流体）+ `added`（fluid 或 item）+ `result`。
   BE 只保留“满桶”规则，流体/物品判定都问这张表；结算与 JEI 共用一个快照。
2. JEI 不再为桶写独立的“假配方”：`BarrelJeiPlugin` 直接读 `BarrelRecipes` 与
   `CompostRegistry`，分两个分类 `barrel` / `barrel_compost`。
   - 转化页三槽（满桶流体 / 加入物 / 产物），流体用 JEI 原生 fluid renderer；
   - 堆肥页一个条目一页，标签展开成物品列表，tooltip 显示分值；
   - 七种桶都是 catalyst（查任意桶都能看到）。
3. 客户端镜像沿用筛子的做法：`processResources` 把 `barrel/`、`compost/` 复制到
   `assets/`，`ClientEvents` 注册 `BarrelRecipeReloadListener` / `CompostReloadListener`。
4. 堆肥标签改成 `#c:*` 与标签文件对齐；JEI 堆肥页注册 65 条（无空槽）反过来证明
   12 个标签都能展开。

**验证**

- 服务端 `[barrel] loaded 4 transforms (2 fluid, 2 item)`；RCON（先 forceload）
  水+熔岩→`cobblestone`、熔岩+水→`obsidian`。
- 客户端进单人世界：`[jei] registered 4 barrel transforms, 65 compost entries`；
  截图目视「桶内转化」1/2 页（3 条/页，流体槽/加号/箭头正常）与「桶：堆肥」1/22 页。
- 未验证：右键红石/荧石掺料（需要真实玩家）；tooltip 逐条目视。

**坑**

- 转化表的流体按 `Fluid` 实例精确匹配；JSON 里写 `minecraft:water` 解析到 still 水，
  上方源方块也是 still，所以能匹配。写 flowing 不会匹配。
- JEI 会把小分类按页面可用高度打包：116×26 的一页会放 3 条，所以 4 条是 1/2 页，
  不是“少画了一条”。
- 桶的 JEI 分类在空服/无世界时不会注册（JEI 进世界时才 `addRecipes`）；看日志要进世界。
- 专用服空服时区块不 tick：RCON 测桶前先 `forceload add`，否则 `data merge` 写进去也
  不会出现 `Solid` / `DirtReady`。

## 15. M3 首批：锤子 / 钩子 / 尘土（2026-10-30）

**目标**

把 M2 的“筛矿”接成完整的手动链：圆石 → 砂砾 → 沙子 → 尘土；尘土进筛出粉末；
矿砾/矿砂用锤子降档；树叶用钩子多出树苗/蚕。数值仍用本项目的表，不搬 GPL 代码/资源。

**决策**

1. **锤子六档**：木 / 石 / 铁 / 金 / 钻石 / 下界合金，耐久
   64 / 128 / 256 / 32 / 1561 / 2031（金最脆，钻石与下界合金最长）；档位只排序，不改变 crush 表。
   **只有左键正常破坏**一条路径（`BlockDropsEvent` 替换掉落），**没有右键快捷方式**——
   经典 Ex Nihilo 同样只走破坏方块；早期实现里的 `HammerItem.useOn` 已删除。
2. **锤表数据驱动**：`data/<ns>/hammer/*.json`（`input` 方块 → `result` 物品 + 可选
   `min`/`max` 数量区间），共 39 条（§16 再补 8 条砂岩，当前 47）：圆石→砂砾、砂砾→沙子、沙子→尘土；
   12 种材料的矿砾→粉碎、矿砂→粉末（各 **4–6 个**）；12 种矿物粉尘→**它自己**。
   服务端专用；没有装 JEI 也能用。
3. **尘土**：新增 `exnihilosatellitium:dust` 方块（`cube_all`，自有 `block/dust.png` 贴图）+ 物品；
   由沙子锤出，可放进筛子；`sieve/dust.json` 出 12 种粉末 + 红石/荧石粉/火药。
4. **钩子两档**：木钩 / 骨钩，耐久 64 / 128；骨钩把叶表概率 ×1.5（封顶 100%）。
   `data/<ns>/crook/*.json` 按树叶方块列出额外掉落（树苗/苹果/蚕），10 张表；
   原版树叶掉落保留，钩子只追加。蚕物品是 M6 丝线链的占位。
5. **蚕/丝线拆分**：M3 只给 `silkworm` 物品和掉落，不接丝线/羊毛；M6 再补。
   蚕贴图是 **4 帧蠕动动画**（16×64，`frametime 6`）：以 Creatio 原图为基础，只把头/中/尾各移动 1px。
6. **贴图改用 Creatio（MIT）+ 1.21 风格微调**：首版 5 个工具图标 + 蚕 + 尘土是自绘占位，
   作者验收不通过；改为采用 **Ex Nihilo: Creatio 1.12.2** 的 16×16 PNG
   （木/石/铁/金/钻石锤、木/骨钩、尘土）。
   Creatio 是 MIT，可复制再分发；来源与版权登记在 [THIRD-PARTY.md](../THIRD-PARTY.md)。
   随后按 1.21 原版工具/骨头的观感做微调（`art/items/tools/restyle.py`，原始图只读放在
   `art/items/tools/creatio/`）：钩子加 1px 方向光斜切、石/铁锤描边收成近黑 `#181818`、
   金锤改成 1.21 的暖金色、下界合金锤由钻石锤换色而来、蚕保留原图做 4 帧位移动画、
   尘土从 110 色收敛到 4 色。Sequentia（传承）1.21 是 CC BY-NC-SA 4.0，**不能**采用，
   只作风格对照。旧的 `art/items/tools/generate.py`（全自绘）已删除。

**验证**

- 服务端：`[hammer] loaded 39 recipes`（§16 补砂岩后应为 47）、`[crook] loaded 10 leaves tables`、
  `[sieve] loaded 3 inputs, 44 drops`、`our recipes=91`（原 88 + 3 锤；熟蚕 3 条后应为 94）。
- 客户端：JEI `registered 3 sieve recipes`，无 missing model/texture。
- **未验证**：玩家实际锤/钩/筛尘土、六档锤与两档钩合成、尘土纹理/掉落手感。

**坑**

- `BlockDropsEvent` 是**服务端**事件；钩子/锤子的掉落不能在客户端改。
- 锤子/钩子是普通 `Item`，没有 `DataComponents.TOOL`，原版**不会**在破坏方块时扣耐久；
  耐久由 `ToolEvents` 手动扣 1。**`BlockDropsEvent#getTool()` 是 `ServerPlayerGameMode`
  复制出来的栈**，直接扣它会丢失，必须扣 `player.getMainHandItem()`（并校验 item 一致）。
- 锤子不是镐/锹：锤表内方块靠 `PlayerEvent.HarvestCheck` 标记为可收获、
  `BreakSpeed` 给 6.0 速度（接近经典锤的 0.75× 效率），否则 `playerDestroy` 的
  `canHarvestBlock` 闸门会让方块直接掉空。锤表外方块不加速。
- **创造模式不经过 `BlockDropsEvent`**（`ServerPlayerGameMode.destroyBlock` 走
  `removeBlock` 直接返回），所以另挂 `BlockEvent.BreakEvent` 给创造模式补掉落，
  方便在创造测试世界里敲方块。生存模式仍只走 `BlockDropsEvent`，不会双掉。
- 尘土方块贴图用更浅的灰米色；原版 `gray_concrete_powder` 会和沙子视觉混淆。

## 16. M3 收尾：粘土 / 砂岩 / 熟蚕（2026-10-30）

**决策**

1. **粘土走经典木桶**：满水 + 尘土 → `minecraft:clay`，一条
   `barrel/water_dust_clay.json`。尘土由 M3 的“沙子→尘土”提供，至此经典 M1.5 的粘土链闭合。
2. **砂岩锤规则**：普通 / 红砂岩的 4 种变体（原版、雕纹、切制、平滑）各一条，
   共 8 条，普通 → 沙子、红 → 红沙，各 1 个。砂岩需要镐才能正常掉落，
   锤子靠已有的 `HarvestCheck` / `BreakSpeed` 走同一条破坏路径。
3. **熟蚕**：`cooked_silkworm`，食物 2 饥饿 / 0.6 饱和度；熔炉（200 t）、烟熏（100 t）、
   营火（600 t）三种烤法。贴图沿用 Creatio `silkwormcooked.png`，只做和生蚕同款调色。

**验证**

- `[hammer] loaded 47 recipes`、`[barrel] loaded 5 transforms`（待启动日志确认；
  客户端资源加载时 `[barrel] loaded 4 transforms` 的旧记录是加粘土之前的）。
- `./gradlew build` + `check-textures.py` 通过；三种烤制配方 JSON 已解析校验。
- **未验证**：粘土在桶里能否正常取出、砂岩锤击、熟蚕食用/烤制。

## 17. M4 坩埚（2026-10-30）

**目标**

坩埚 + 正下方热源，把圆石 / 石头 / 砂砾熔成熔岩；下界岩产出更多，雪/冰熔成水。

**决策**

1. **两个方块**：`unfired_crucible`（未烧制）+ `crucible`（带 BE）。
   瓷土 = 粘土球 + 骨粉，7 瓷土 → 未烧制坩埚，熔炉烧成坩埚；这同时补上了
   `porcelain` 物品从 M1 起的“有物品没用途”。
   模型和贴图采用 Creatio（MIT）的 `block_crucible_big` + 两块 16×16，
   `art/blocks/crucible/restyle.py` 做模糊/5 色量化，并把非边界面的 `cullface` 去掉。
2. **容量可配置**：`common` 配置有 `crucible.capacity`（默认 8000，1000–64000）、
   `crucible.meltCapacity`（默认 2000，250–64000）、`crucible.heatSpeedMultiplier`
   （默认 1.0，0.1–10.0）。BE 每 tick 读配置并夹紧 tank；不需要重启。
3. **热源数据表** `data/<ns>/crucible_heat/*.json`：火把 0.1、熔岩 0.2、火 0.3、
   燃烧的熔炉/高炉/烟熏炉 0.15、**篝火 0.25、灵魂篝火 0.35、灵魂火 0.4**
   （后三者是本项目新增，1.7.10 原版没有）。支持 `properties` 精确匹配
   （例如 `lit=true`，未点燃的篝火不算热源）。
4. **熔点表** `data/<ns>/crucible_melt/*.json`：圆石/石头/砂砾/花岗岩/闪长岩/安山岩
   （含磨制、深板岩、凝灰岩、方解石）250 mB 熔岩；下界岩 1000 mB；雪块 500 mB 水；
   冰 1000 mB 水；**沙子/红沙/尘土各 100 mB 熔岩**。`amount` 同时是熔料体积（1:1）。
   熔化速度 = 热源 mB/tick × `heatSpeedMultiplier`；熔料上限可配（默认 2000 = 8 块圆石）。
5. **交互**：右键放入可熔方块；右键桶/容器抽流体；管道走 `IFluidHandler`；
   比较器输出液位；BE 每 10 tick 同步一次客户端用于渲染。
6. **JEI 两个分类**：`crucible`（方块 → 流体 + 产量）和 `crucible_heat`
   （热源 → 速度），都使用 JEI 原生槽位/流体槽，不自绘背景。
7. **熔料可视**：BE 同时维护 `solidVolume`（熔料，默认上限 **2000 mB**，可配）和流体量；
   **固体体积 > 流体体积**时，BER 用该方块的贴图填充整个内腔；反之渲染流体。
   BE 存 `PendingStack` 并同步；Jade 显示 `熔料：<方块> 剩余 N mB` 和下方热源速度。
8. **数据包**：`crucible_melt/` 与 `crucible_heat/` 和其他玩法表一样可覆盖/新增；
   单人 JEI 优先读服务端表（包含数据包），专用服客户端回退 `assets/` 镜像。

**验证**

- 启动日志应出现 `[crucible] loaded 19 meltables`、`[crucible] loaded 9 heat sources`、
  `[jei] registered 19 crucible meltables, 9 heat sources`。
- `./gradlew build` + `check-textures.py` 通过；19 个熔点 / 9 个热源 JSON 已解析校验。
- **未验证**：进游戏放方块/加热/抽流体、熔料比例、配置改容量、Jade、JEI 两页目视。

## 18. M3 补 JEI：锤子 / 钩子（2026-10-30）

**决策**

- 锤子/钩子的数据表保留**客户端镜像表**（`HammerRecipes.clientRecipes` /
  `CrookDrops.clientTable`），客户端 reload listener 用 `clientView=true` 读
  `assets/.../hammer`、`assets/.../crook`；游戏逻辑（`ruleFor`/`dropsFor`）只读服务端表，
  避免 F3+T 把服务端规则换掉。JEI 优先读服务端表（单人/局域网会包含数据包），
  服务端表为空时回退客户端镜像。
- JEI 新增两个分类：
  - `hammer`：76×26，方块 → 产物；tooltip 写 `掉落 N` 或 `掉落 4–6`；6 种锤作 catalyst。
  - `crook`：104×26，树叶 → 最多 3 个追加掉落；每个 tooltip 写概率，骨钩 ×1.5 只写提示。
- 两个分类都使用 JEI 原生槽位/箭头，不自绘背景。

**验证**

- 启动日志应出现 `[jei] registered 35 hammer recipes`（玩法 47 条，12 条粉尘自掉不进 JEI）、
  `[jei] registered 10 crook recipes`。
- build + 纹理检查通过；**未进游戏目视**。

## 19. M2 余项：泥土筛与沙筛杂物（2026-10-31）

**问题**

空岛跑完 M1–M4 之后仍然**没有任何种子来源**：`wheat_seeds` 只出现在堆肥表的输入里，
筛表只有 `gravel` / `sand` / `dust` 三档。结果是「堆肥 → 泥土」这条链没有出口，农业、
繁殖、纸/书、绿染料全部断在起点。

**做法**

- 纯数据，不动 Java：新增 `data/exnihilosatellitium/sieve/dirt.json`，输入 `minecraft:dirt`，
  6 条独立判定：
  - 小麦种子 0.15、甜菜种子 0.06、土豆 0.06、胡萝卜 0.06、西瓜种子 0.05、南瓜种子 0.05。
- `sieve/sand.json` 补两条杂物：仙人掌 1/32（绿染料/仙人掌绿）、甘蔗 1/24（纸、书、附魔台）。
- 为什么放在泥土：泥土是堆肥产物，本身就消耗有机物；让「堆肥 → 泥土 → 过筛」形成闭环，
  而不是让种子从石头/沙子里凭空冒出来。甘蔗/仙人掌放在沙，是给“沙子除了矿没有别的”补出口。

**数值口径**

- 概率是本项目自己定的，不抄 1.7.10 / Omnia 的具体值；按“期望 3–4 次筛出第一颗小麦种子”起步。
- 沙筛仍全部独立判定，一次结算可能同时出矿粉和杂物；`JEI` 最多 21 槽，沙筛 14 条、泥土 6 条，没触顶。

**验证**

- `./gradlew build` + `check-textures.py` 通过；服务端日志 `[sieve] loaded 4 inputs, 52 drops`。
- **未进游戏实测**：`dirt` 能不能放进筛子、种子掉率手感、JEI 第 4 个输入页。

## 20. 修正：锤/钩配方形状 + 坩埚满时的右键（2026-10-31）

**问题（作者试玩反馈）**

1. 木钩、骨钩、六档锤的合成配方与原版不一致：我们写成了正十字（`"xxx"/" y "/" y "`）
   与单列对角棍（`"  x"/" x "/"x  "`），看起来像普通工具。
2. 坩埚装满后再按住右键递熔料，因为 `useItemOn` 返回 `PASS_TO_DEFAULT_BLOCK_INTERACTION`，
   交互落到默认放置，**在坩埚顶上放了一个方块**；原版是直接无视。

**修正**

- 配方按原版斜向形状重写（对照 `reference/` 的 `Recipes.java`，只取形状，不复制代码/资源）：
  - 锤：`" x "` / `" yx"` / `"y  "`，`x` = 材质（木板/圆石/铁锭/金锭/钻石），`y` = `#c:rods/wooden`。
  - 钩：`"xx "` / `" x "` / `" x "`，木钩用木棍、骨钩用骨头。
  - 下界合金锤仍是锻造台升级（1.7.10 没有下界合金，这是本项目扩展）。
- `CrucibleBlock#useItemOn`：先判 `meltable`；`crucible.use()` 没消耗动作时返回
  **`ItemInteractionResult.CONSUME`**，把右键吃掉而不是落到放置方块。潜行放置仍然有效
  （潜行会跳过 `useItemOn`）。非熔料方块（泥土、机器等）不受影响。
- **坑**：`ItemInteractionResult.FAIL` 的 `InteractionResult.FAIL.consumesAction()` 是 `false`，
  所以 `ServerPlayerGameMode#useItemOn` 会继续走到 `ItemStack#useOn` 把方块放下去——
  想“拒绝但不做事”必须返回 `CONSUME`。流体容器同理（否则满罐时拿桶右键会把流体倒出来）。

**验证**

- `./gradlew build` + 纹理检查通过；服务端 `[debug] our recipes=97`，无 recipe 解析错误。
- **未进游戏复测**：新形状能不能合成、JEI 是否显示斜向配方、坩埚满时右键是否彻底无反应。

## 21. M6 第一刀：巫水 + 古代孢子（2026-10-31）

**问题**

巫水是 M6 的起点，但空岛没有菌丝来源：原版靠**沙筛出古代孢子**（1/128）→ 右键泥土变菌丝 /
右键牛变哞菇，再用满水桶挨着菌丝发酵。少了这一环，巫水在空岛里永远做不出来。

**做法**

- **流体**：`exnihilosatellitium:witch_water` still/flowing 一对 + `FluidType`
  （density 1100 / viscosity 1500 / 300 K / 不可乘船 / 可灭火）+ `LiquidBlock` + 桶。
  流动参数比水懒：`slopeFindDistance 3`、`levelDecreasePerBlock 2`、`tickRate 15`，
  避免手滑一桶把岛淹了。客户端外观在 `WitchWaterClientExtensions`（紫色 still/flow）。
- **古代孢子**（`common/item/AncientSporesItem`，`ancient_spores`）：紫色，右键泥土 → 菌丝；
  右键牛 → 哞菇（保留血量/名字/幼年）。沙筛 1/128。
- **发酵状态机**（`BarrelBlockEntity`）：满 1000 mB 水 + 3×3×3 内有菌丝时，
  每 tick `Spore += 1 + 菌丝数/2`，满 1000 变巫水并清零。
  - 1 个菌丝 ≈ 50 秒，2 个 ≈ 25 秒（和原版 `MAX_COMPOSTING_TIME` 同量级）。
  - 递**蘑菇煲**可以无菌丝起酵（消耗煲、退回碗），同样 50 秒。
  - 发酵进度存 NBT `Spore`；巫水本身不会再被发酵成别的东西。
- **转化**走既有 `BarrelRecipes`，不写 Java：巫水+沙子→灵魂沙、巫水+橡树苗→深色橡树苗。

**美术**

- **不再自绘流体**：客户端直接用原版 `minecraft:block/water_still` / `water_flow`
  （灰度）乘紫色 tint `0xFF6B44A8`，动画/流动与原版水一致；自绘的 still/flow 已删。
- **巫水桶**：`art/items/witch_water_bucket/generate.py` 读原版 `water_bucket.png`，
  按亮度把蓝色水体映射到紫色色阶，桶身保持原版；脚本从 ModDevGradle 的
  client-extra jar 里取图，仓库不存 Mojang PNG。
- 古代孢子：`art/items/ancient_spores/generate.py` 逐像素生成（紫色，与巫水同一套色板）。

**验证**

- 服务端：`[barrel] loaded 7 transforms (2 fluid, 5 item)`、`[sieve] 4 inputs, 53 drops`。
- RCON：满水 + 2 菌丝 25 秒变巫水、`Spore` 归零；只写 `Spore:1` 无菌丝 50 秒后变巫水；
  `setblock` 巫水方块可命中。
- **未验**：客户端流体/桶纹理、桶装倒、两条转化、古代孢子右键（要玩家）。

**下一步**

- 瓷桶（可装熔岩）→ 蚕（侵染树叶 → 线）→ 娃娃（烈焰人/末影人）。
- 娃娃和「牛奶→粘液球」需要给 `BarrelRecipes` 加**产出物品/生成实体**的规则类型。

## 22. M6 第二刀：瓷桶（2026-10-31，重做）

**做错了一次**

第一版把「瓷桶」做成了 **`porcelain_barrel` 方块**（复用 BarrelBlock）。作者指出：Omnia 的瓷桶是
**物品桶**（`ItemBucketPorcelain` / `UniversalPorcelainBucket`，lang 里是 Unfired Bucket →
%s Bucket），不是方块。已把方块整套删掉，改成物品。

**现在的实现**

- `porcelain_bucket`：通用 1000 mB 容器，能装任何流体（熔岩、巫水、水、奶）。
  - 内容存 data component `exnihilosatellitium:fluid_contents`（`{id, amount}`，纯 `ResourceLocation`
    + int codec，不依赖 registry ops）。
  - `PorcelainBucketFluidHandler` 实现 `IFluidHandlerItem`（1 罐 / 1000 mB / 一次只能一种流体），
    在 `CapabilityEvents` 里注册；因此桶可以给桶、桶给坩埚/木桶倒液体，木桶的“拒热”检查也认它。
  - 右键流体源可以舀满（`Item#use` + `BucketItem#getPlayerPOVHitResult`）。
  - tooltip 显示内含流体与 mB 数。
- 合成：**3 瓷土**按原版桶的形状（`x x` / ` x `）→ `unfired_porcelain_bucket`，再熔炉烧成
  `porcelain_bucket`（200 tick / 0.3 经验）。和坩埚的 7 瓷土配方不冲突，也照 Omnia 的
  Unfired Bucket → 烧制 两步走。未烧制的桶没有流体 capability，不能装液体。
- 客户端：模型用 predicate `exnihilosatellitium:filled` 在空桶/满桶模型之间切换；满桶模型的
  layer1 是白色液体遮罩，`RegisterColorHandlersEvent.Item` 按流体上色（水 0xFF3F76E4、
  熔岩 0xFFFF8A2A、巫水用流体的 tint）。
- 贴图 `art/items/porcelain_bucket/generate.py`：桶身读原版 **`bucket.png`**（空桶，带桶沿/内壁），
  按亮度映射到瓷白 / 陶土色阶；液体遮罩从 `water_bucket.png` 的水体像素提取成白色。
  第一版拿 `water_bucket` 当桶身，把水体像素挖空后桶口是镂空的——已修正。仓库不存原版 PNG。

**验证**

- 服务端：`[debug] our recipes=98`；`summon item` 带组件可读回
  `{"exnihilosatellitium:fluid_contents": {amount: 1000, id: "minecraft:lava"}}`，巫水同样通过。
- **未验**：进游戏看空桶/满桶模型与颜色、右键舀水、倒进木桶/坩埚、合成。

## 23. M6 第三刀：木坩埚（2026-10-31）

**来源**

Creatio 的 **Wooden Crucible**（mcmod 条目 71289）：「坩埚的木质版。往里放入树叶/树苗可缓慢变为水，
不需要热源，减少等雨的麻烦。」配方 6 橡木原木 + 1 橡木台阶。

**实现**

- 11 种木材变种（橡木 + 5 种经典木 + 红树/樱花/竹 + 绯红/诡异菌柄）：
  `scripts/generate_wooden_crucible_variants.py` 生成 blockstate/模型/掉落/配方/lang/标签，
  配方 = 该木材原木 ×6 + `#minecraft:wooden_slabs` ×1（台阶任意木材）；竹用 `bamboo_block`、
  下界两种用 `crimson_stem` / `warped_stem`。
- 方块复用 `CrucibleBlock` + `CrucibleBlockEntity`（同一 BE 类型，所以 `CrucibleRenderer`、
  Jade、比较器都直接可用），`isWooden()` 查方块标签 `#exnihilosatellitium:wooden_crucibles`。
- 行为差异都由 `CrucibleBlockEntity#isWooden()` 分支：
  - 熔化速度固定 `WOODEN_PASSIVE_SPEED = 0.5 mB/t`，**不看下方热源**（有篝火也一样快）；
  - `canAccept` 只收 `fluid == water` 的熔料，熔岩类配方直接被拒；
  - `fill` 拒绝 > 433 K 的流体（和木桶同一条温度线），右键倒熔岩会提示
    `message.exnihilosatellitium.crucible.hot_fluid`。
- 数据：新增 20 条水熔料（10 树叶 100 mB、10 树苗 250 mB），走既有 `crucible_melt` 表；
  瓷坩埚也能用，只是要热源。
- 模型 vanilla-sampled：`oak_log` 桶身 / `oak_log_top` 顶沿 / `stripped_oak_log` 内壁，
  不新增 PNG；斧挖，掉落自身；JEI 坩埚分类多一个 catalyst。

**验证**

- 服务端：`[crucible] loaded 39 meltables`（19 + 20）。
- RCON：木坩埚放树叶，6 秒 100 → 76.6 固体 / 23 mB 水，速率 0.2 mB/t；底下换成篝火后速率不变。
- **未验**：合成、进游戏看模型/液体显示、熔岩桶被拒的提示、JEI 新条目。

## 24. M6 第四刀：筛出物补齐（2026-10-31）

**问题**

作者指出筛表缺了一批高版本资源：草种子、海晶碎片/砂粒、可可豆、甜浆果、海带、珊瑚，
以及「粉碎下界岩 / 粉碎末地石」这两条下界/末地资源链。

**做法**

- 新物品：
  - `grass_seeds`（草种子）：右键泥土 → 草方块（`GrassSeedsItem`），空岛不靠流浪商人也能量产草；
  - `crushed_netherrack` / `crushed_end_stone`：**方块**（不是物品），锤下界岩 / 末地石得到
    （`hammer/netherrack.json`、`hammer/end_stone.json`，1–2 个），再作为筛料。铲子挖掘、掉落自身。
- 筛表（全部独立判定，数值是本项目自己的）：
  - 泥土 + 草种子 6%、甜浆果 5%；
  - 沙 + 可可豆 3%、海带 4%、5 种珊瑚各 1%、海晶砂粒 2%、海晶碎片 2%；
  - 砂砾 + 海晶碎片 2%（2026-11-01 按作者反馈调整：海晶碎片只从沙出，砂砾不再掉）；
  - 粉碎下界岩 → 石英 20%、荧石粉 12%、下界疣 10%、烈焰粉 6%、岩浆膏 4%、灵魂沙 2%；
  - 粉碎末地石 → 末地石 20%、紫颂果 6%、末影珍珠 5%。
- 粉碎下界岩/末地石是方块类筛料，走原来的方块堆渲染；`SieveRenderer` 仍然保留了
  非方块输入的图标兜底（`ItemRenderer.renderStatic` 平铺），以后再有纯物品筛料也看得到。
- 图标：草种子还是 `art/items/sieve_extras/generate.py` 手绘；**粉碎下界岩/末地石的方块贴图**由
  `art/blocks/crushed_stone/generate.py` 从原版 `netherrack` / `end_stone` 派生：保留原版像素，
  叠 2×2 细棋盘（交替 0.9 亮度）+ ±6 确定性噪点。试过 4×4 大色块 + 连续接缝，被指出像木板，已废弃。
  不在仓库里存原版 PNG。

**验证**

- 服务端：`[sieve] loaded 6 inputs, 69 drops`、`[hammer] loaded 49 recipes`；修掉了
  `minecraft:nether_quartz` 这个不存在的 id（1.21 是 `minecraft:quartz`），修完无 `bad drop` 警告。
- **未验**：草种子右键、粉碎物平铺渲染、掉落手感（要玩家）。

## 25. M6 第五刀：蚕与侵染树叶（2026-10-31）

**做什么**

- `infested_leaves` 方块：`RenderShape.ENTITYBLOCK_ANIMATED`，本体不画，由
  `InfestedLeavesRenderer` 用 BER 把**被替换的树叶方块**画回来，所以橡木/云杉/樱花…各自保持外观；
  文件是 `common/block/InfestedLeavesBlock` + `common/blockentity/InfestedLeavesBlockEntity`。
- `silkworm` 从普通物品换成 `SilkwormItem`：右键 `#minecraft:leaves` → 换成侵染树叶并写入 `Mimic`。
- 进度 0.0005/tick（2000 tick = 100 秒），`Progress > 0.6` 后每 100 tick 扩散一次；
  扩散只在 3×3×3 里**真实存在的树叶**中随机挑一个（一开始用 3×3×3 随机偏移，大部分落在空气上，
  20 秒只长 1 个，改成先收集候选再挑，20 秒能长 4 个）。
- 破坏时按 `Progress` 掷 `线`：`rand < progress` 掉一个，再 `rand < progress * 0.25` 可能再掉一个。
- 视觉：BER 画回原树叶，用 `WhiteningVertexConsumer` 把顶点色按 `min(0.85, progress * 0.9)`
  混向白色——保留树叶纹理，不会出现白盒子（第一版用 `DEBUG_QUADS` 画白方块，被指出渲染异常）；
  另有每 tick 1/24 概率的白灰粒子。`Progress` 同步给客户端（update tag）。
- **创造模式**：`playerWillDestroy` 里跳过掉线；补了 `infested_leaves` 物品形态 +
  `infestation` 组件，创造栏给两个堆叠（0% / 100%），放下时由 `InfestedLeavesBlock#setPlacedBy` 设进度。
- **Jade（桶）**：烹饪期间显示 `生成中：还需 N 秒`（`jade.exnihilosatellitium.barrel.cooking`），
  之前作者以为「右键没反应」，其实是没看到 600 tick 的倒计时。
- **Jade**：`InfestedLeavesComponentProvider` 显示 `原树叶：<方块名>` 和 `侵染：xx%`，
  成熟（progress = 1）时改成「已成熟（会向周围扩散）」；配置键
  `config.jade.plugin_exnihilosatellitium.infested_leaves` 已补。

**验证**

- 服务端：`setblock infested_leaves` + `data merge` 写入 `Mimic/Progress` 后，20 秒 `0.65 → 0.849`，
  8 个相邻树叶里 4 个变侵染。
- **未验**：右键树叶转化、破坏掉线、游戏内渲染。

## 26. M6 第六刀：人偶（2026-10-31）

**来源**

1.7.10 `Recipes.java`：人偶 = 7 瓷土 + 钻石/绿宝石（`xyx / x / x x`）；愤怒/诡异人偶用同一张
`xyx / zwz / xvx`，中心是普通人偶，四周换材料，并且各有一个「红石 ↔ 下界疣」互换的镜像配方。
作者确认：叫「人偶」、保留双配方与镜像、和平模式改为掉烈焰棒/末影珍珠、生成后桶恢复空桶。

**实现**

- 三个物品 `doll` / `angry_doll` / `creepy_doll`：形状采用 Creatio（MIT）的 `doll.png`，
  `art/items/dolls/restyle.py` 缩到 **64×64**（作者要求更高清）、量化到 4 阶 + 1px 描边；
  愤怒/诡异换色板。（先试过手绘 X 形人偶，被否掉；16×16 也被嫌不够清楚。）
  `check-textures.py` 因此放宽：物品贴图允许 16 或 64，方块仍是 16。
- 6 条合成配方（钻石、绿宝石、愤怒正/反、诡异正/反）。
- **`BarrelRecipes` 新规则类型**：`entity` + `peaceful_drop` + 可选 `cook_ticks`（默认 600）。
  - 命中后 `BarrelBlockEntity#startCooking` 清空流体、记录 `pendingEntity`，600 tick 后
    非和平在桶上方生成实体，和平模式掉 `peaceful_drop`，然后清字段（空的桶）。
  - NBT：`CookEntity` / `PeacefulDrop` / `CookTicks`；烹饪期间比较器 15。
  - JEI 的桶分类把 `peaceful_drop` 当作结果展示（`result` 字段镜像它）。
- 四条桶规则：熔岩 + 愤怒人偶 → 烈焰人；巫水 + 诡异人偶 → 末影人；
  **巫水 + 漂浮人偶 → 潜影贝**；**水 + 守卫者人偶 → 守卫者**（按作者决定不做海水，直接用普通水）。
  守卫者的和平掉落是数组（海晶碎片 / 海晶砂粒随机）。

**验证**

- 服务端：`[barrel] loaded 9 transforms (2 fluid, 7 item)`。
- RCON：写入 `CookEntity=blaze` 后，Normal 难度刷出烈焰人；Peaceful 难度掉烈焰棒。
- **未验**：6 条配方的摆放、玩家右键人偶、JEI 展示。

## 27. M10 自动化：电动 / 内能四台机器（2026-11-01）

**范围**

| 机器 | id | 供能 | 插件 | 工具槽 |
|---|---|---|---|---|
| 电动自动筛 | `electric_sieve` | FE | 4 格 | 筛网 |
| 电动自动锤 | `electric_hammer` | FE | 4 格 | 锤 |
| 内能自动筛 | `fuel_sieve` | 原版熔炉燃料 | 无 | 筛网 |
| 内能自动锤 | `fuel_hammer` | 原版熔炉燃料 | 无 | 锤 |

**判定单一来源（M2 欠账兑现）**

- 新增 `common/machine/SieveRolls`、`common/machine/HammerRolls`：机器与手动路径都走它们。
  `SieveBlockEntity#completeSift` 和 `ToolEvents`（普通破坏、创造破坏）已改为调用同一函数，
  判定逻辑不再有第二份。
- 时运不是“提高概率”，而是**整表再掷一次**：时运 I/II/III = 额外 1/2/3 次独立 roll。
  锤子的多次 roll 合并成一摞（例如矿砾 4–6 在时运 II 下是 12–18）。

**槽位与布局（176×166 GUI）**

- 通用：`0` 工具、`1` 输入、输出 **12 格（3 行 × 4 列）**。
- 自动筛子的方块模型**忠于 Ex Astris 的四腿托盘**（MIT 思路，自建）：1×1 细腿（0..10）+
  薄托盘底（9..10）+ 4px 薄壁（10..14）+ 槽底网格（10.2），高度 14 与手动筛子一致。
  整个模型向内缩 **1px**（x/z 1..15），避免两台机器并排时相邻面共面产生 z-fighting；
  碰撞/`getShape` 也是 1..15 × 0..14。
- 自动锤子是**四柱压锤**：底板（0..4，色带区分电动/内能）+ 四条立柱（4..12）+
  顶板（12..14）+ 中间铁砧（4..6）/ 锤头（7..10）/ 连杆（10..12），整体也内缩 1px、
  高度 14；碰撞 `getShape` 同为 1..15 × 0..14。
- **工作动画**（`client/MachineRenderer`，BER）：锤头是一个独立方块实体渲染的 6×3×6 金属头，
  工作周期内从上方 4px 压到铁砧；输入物以小模型摆在铁砧上。筛子把输入物压成薄层摆在网格上，
  随进度变薄（不再左右抖动）。
  **料层必须留在托盘内壁以内**：整块渲染到 0.875 会穿过盘壁（1..15）并与盘壁外侧面
  z-fighting，表现为「料层和边缘交替闪烁」；现在 x/z 缩到 0.6（3.2..12.8 px）、
  底面抬到 10.5 px（网格顶面 10.35），四周留 ~0.2 px 缝（2026-11-01 修）。
  `Active` 必须写进 `saveAdditional`（否则 `getUpdateTag` 同步不到客户端，BER 永远当停机）。
  方块模型里不再放静态锤头（物品模型单独保留静态头，图标不会空）。
- **工作时粒子**（`MachineBlock#animateTick`，服务端 `active` 变化时同步 BE）：筛子在托盘上方
  冒 `CRIT`，电动加 `ELECTRIC_SPARK`、内能加 `SMOKE`；锤子在铁砧/锤头处冒 `CRIT`，
  电动 `ELECTRIC_SPARK`、内能 `FLAME`/`SMOKE`。
  贴图：`machine_frame_side_{electric,fuel}`（底部色带）、`machine_frame_top`、`machine_leg`、
  `machine_mesh`（cutout 镂空网格）、`machine_inner`。
- 电动：`2..5` 插件、输出从 `6` 开始；能量条在左上。
- 内能：`2` 燃料、输出从 `3` 开始；火焰条在左下。
- 破坏方块时 `Containers.dropContents` 掉出全部内容（含筛网/锤/插件），不做返还例外。

**数值（全部进 config）**

| 键 | 默认 | 含义 |
|---|---|---|
| `machine.ticksPerOperation` | 100（5 秒） | 一次处理的基础耗时；速度插件按 ×1.5 / ×2.5 / ×4 缩短，叠加递减 |
| `machine.energyPerTick` | 20 | 电动机器工作时每 tick 抽 FE；无电即停机 |
| `machine.energyCapacity` | 48000 | 内部 FE 缓冲（电动筛/锤共用） |
| `machine.burnPerOperation` | 200 | 内能机器每完成一次消耗的燃料值；煤 1600 → 8 次 |

- 内能机器没有速度插件，`ticksPerOperation` 固定走 config；燃烧按“每 tick 消耗
  `burnPerOperation × 2 / ticksPerOperation` 个半 tick”精确记账（内部用半 tick 整数，避免浮点漂移）。
- **插件叠加（2026-11-01 改）**：不再“只取最高档”。**按物品数量计算（含堆叠数量）**，单个物品
  堆叠上限 **8**；**每个槽位只收一种类型**：槽 2 = 速度、槽 3 = 时运，槽 4/5 留给以后的新插件
  （所以速度/时运各自最多 8 个，不能靠多槽位堆到 2×8）。同类型按档位从高到低排序，
  第 1 个全额、之后每个权重乘 0.8：
  - 速度倍率 = `1 + Σ (tier 倍率 - 1) × 0.8^(名次-1)`；1/2/3/4 个速度 III =
    ×4 / ×6.4 / ×8.32 / ×9.86（25 / 16 / 12 / 10 tick/件）。
  - 时运按**生效几率**叠加：第 1 个 100% 生效，之后每个的生效率乘 0.8（100/80/64/51.2%…）；
    生效的插件把自己的档位作为额外 roll（时运 III = +3 次）。
  - 每多装一个插件，`FE/t` 增加 25%（4 个 = 1.75×基础），内能机器不受影响。
  - **自动锤的时运槽最多只收 1 个**（槽位 `getMaxStackSize`/管道 `getSlotLimit` 双重限制），
    筛子仍是 8 个。
- 燃料槽接受任何 `getBurnTime(RecipeType.SMELTING) > 0` 的物品；熔岩桶会退回空桶。

**能力 / 管道**

- `Capabilities.ItemHandler.BLOCK`：四台机器都暴露 `MachineItemHandler`，
  **只允许从输出 12 格抽取**，插入走与 GUI 相同的 `canPlaceInSlot`；避免漏斗把筛网/锤/插件抽走。
- `Capabilities.EnergyStorage.BLOCK`：只有电动机器返回 `MachineEnergy`（只进不出，`maxExtract = 0`）；
  内能机器返回 `null`，没有能量能力。
- `MachineEnergy.Drain` 是机器内部扣电入口：NeoForge 的 `extractEnergy` 在 `maxExtract = 0` 时
  直接返回 0，不能拿来扣自己的电量（踩坑记录见 status.md）。

**GUI**

- 标题直接取方块名（`getBlockState().getBlock().getName()`），所以是「电动自动筛子 / 内能自动筛子 /
  电动自动锤 / 内能自动锤」，不按电源类型写死。
- 槽位用自定义 `MachineSlot`：1.21.1 的 `Slot#mayPlace` 默认 `true`，必须显式转发
  `canPlaceInSlot`，否则 GUI 拖拽/shift-click 能把插件塞进筛网格（2026-11-01 修）。
- 两个 `MenuType`（`electric_machine` / `fuel_machine`）、两个 `Screen`，尺寸 **176×186**；
  玩家物品栏下移到 `y=104/162`，`物品栏` 标签在分隔线下方，不再和插件槽重叠。
- 输出 3×4（x=99..169），电动 18 格 / 内能 15 格。
- 空槽虚影：工具槽 = 筛网/锤、输入槽 = 沙/圆石、燃料槽 = 煤、
  电动槽 2 显示速度插件、槽 3 显示时运插件（槽 4/5 留给未来类型，不显示虚影）；
  空槽铺深蓝底、物品用 `setShaderColor` 压暗
  （**不要用 `fill` 盖物品**：`fill` 走 `gui` 渲染批次，会画在物品模型下面），悬停显示槽位说明。
- 标题右侧状态行（服务端算、`ContainerData` 下发）：运行中 / 缺少工具 / 缺少原料 /
  缺少能量 / 缺少燃料 / 输出已满（`DATA_STATUS`）。
- 仪表：能量条用 `energy_fill.png`（8×42 竖向渐变 + 分段线）、进度条用 `progress_fill.png`
  （27×6 箭头形，底槽是暗绿箭头）、火焰用 `flame.png`（14×14，从下往上长）；三者都是贴图，
  不再用纯色 `fill`。
- JEI：电动/内能筛与锤注册为 `sieve` / `hammer` 分类的催化剂；点击进度条按当前输入打开 JEI
  （输入为空则用 `showTypes` 打开整个分类）。运行时句柄在 `MachineJeiPlugin`。
- `ContainerData` 8 项：`work / ticksPerOperation / energy / energyCapacity / burnHalfTicks /
  burnHalfTotal / active / status`。**客户端用 `SimpleContainerData`** 接收同步值；
  直接用 BE 会把 `set` 吃掉，进度条冻住（踩坑记录见 status.md）。
- 贴图：机器方块用 **32×32** 的 `machine_*` 贴图（2026-11-01 由 16×16 升级），
  `scripts/generate_machine_assets.py` 生成（Pillow）；`check-textures.py` 对 `machine_*` 允许 16/32。

**自动输出（2026-11-01 追加）**

- 机器可以自动把输出 12 格推进相邻容器，默认**关闭**（不改变已有漏斗/管道摆法）。
  状态是 `AutoOutput` + `OutputMask` 两个字段：前者总开关，后者按 `Direction.get3DDataValue()`
  每个面一位；默认掩码只有底面（`FACE_DOWN`），因为箱子通常放在机器下面。
- 语义只覆盖**自动弹出**：掩码不限制 `MachineItemHandler`，漏斗/管道仍可从任意面抽取输出格
  （已验证的「下方漏斗抽输出 / 上方漏斗补料」两套摆法都不用改配置）。
- 触发时机：每完成一次操作立刻推一次；另外每 20 tick 重试一次，把「邻居满了 / 后来才清空」
  的情况也补上。邻居满时余量留在输出格，不丢物品。
- 配置 GUI（`MachineOutputScreen`）用 `Minecraft#pushGuiLayer` 叠在机器 GUI 上，
  **不新开菜单**：机台菜单保持打开，改配置不会关界面或吞掉手上的物品。
  入口是机器面板**左侧突出的标签页**（代码绘制的 22×28 标签 + 16×16 三帧图标），
  点开后配置面板从机器面板左边缘向**左伸出**（132×168，与 GUI 左边界齐平），
  不铺全屏黑底，只带一层淡阴影；空间不够时 `max(2, guiLeft - 132)` 兜底。
- 面按钮按**玩家相对方向**标注：上 / 下 / 前 / 后 / 左 / 右（两列三行，成对排列），
  映射在打开面板时用 `player.getDirection()` 冻结（容器界面里相机不能转）；
  世界方向（东南西北）只放在 tooltip（`自动输出到前（东）`）。
- 6 个面按钮 + 总开关，改动以绝对值 payload 发到服务端，再由 `ContainerData`
  （`DATA_AUTO_OUTPUT` / `DATA_OUTPUT_MASK`）回读，客户端不维护第二份权威状态。
- 自动输出至少要有一个面：`setOutputConfig`（GUI/网络包路径）把空掩码回退到底面，
  GUI 在取消最后一个面时自动关掉总开关；直接改 NBT 不走这条校验。
- 图标三帧（常态/悬停/开启）由 `generate_machine_assets.py` 生成；开启时图标变绿，
  不进配置页也能看出来。预览：[m10-output-button.png](images/m10-output-button.png)。
- 数据包 `exnihilosatellitium:set_machine_output`（C2S）只接受 8 格以内的玩家，
  `stillValid` 校验与方法同 GUI 距离。

**验证（2026-11-01，RCON / forceload 0 0）**

- 内能筛：写入木筛网 + 砂砾 64 + 煤 1，120 tick 后输入 63、筛网 damage 1、`BurnHalfTicks` 3200→2600、
  产出燧石（走 `sieve/gravel.json`）。
- 电动筛：`Energy:20000` + 丝筛网 + 沙 64，101 tick 后能量 17980（20/tick 对上）、输入 63、产出铝碎块/海晶结晶。
- 电动锤 + 速度 III + 时运 III：5 秒完成 5 次（20 tick/次），输入 64→59、锤 damage 5、
  圆石规则 1 个 × 4 roll × 5 次 = 20 砂砾（时运生效）。
- 内能锤：80 tick 一次，输入 63、锤 damage 1、`BurnHalfTicks` 2700、产出 1 砂砾。
- 漏斗在机器下方只抽到砂砾（输出），机器内的锤/插件仍在；漏斗在上方成功把圆石补进输入槽。
- 自动输出（2026-11-01 追加）：电动筛 `AutoOutput=1/OutputMask=1` + 下方箱子，跑完一件后
  箱子拿到 `iron_crushed`，机器输出格为空；关掉 `AutoOutput` 后产物留在槽 6..10、箱子不变。
- 自动输出换面：`OutputMask=2`（只推上方）→ 缓冲区 5 件 + 新产出的 1 件全部进上方箱子，
  下方箱子不变。
- 自动输出重试：无邻居时 3 个燧石留在槽 6；放上箱子后**没有新操作**，20 tick 内自动推进箱子。
- 内能筛同配置同样生效（4 次产出 3 件进下方箱子，1 次 roll 空手）。
- 存档实证：内能筛在作者世界里已处理 12 次（筛网 damage 12、末地石×5、紫颂果×1、末影珍珠×2）。
- 放错槽位的物品会被弹到机器上方，不会把机器永久卡死。
- **未验**：虚影/状态行/贴图的观感；配方未实际合成。

## 28. P1 薄门面：core / platform 分层（2026-11-01）

目标不是抽象，而是**让 1.20.1 的改动面收敛到 `platform/` 一层**（[porting.md](porting.md) §3）。
判断标准一句话：**1.20.1 要重写的文件才进顶层 `platform/`**。

- 新顶层包 `platform/`：`NeoForgeModProbe`、`config/ModConfig`（`ModConfigSpec` 实现 core 的
  `ConfigSource`）、`machine/{MachineEnergy,MachineItemHandler,MachineHooks}`。
- core 侧四个窄门面：
  - `common/config/{ConfigSource,ConfigValues}`：**委托读** spec，所以配置热改/`/reload` 行为不变；
    没装 source 时抛带说明的 `IllegalStateException`。
  - `common/platform/{ModProbe,Platform}`：`isLoaded` / `version`；`ModProbe.NONE` 是“都没装”的空实现。
  - `platform/machine/MachineHooks`：BE 唯一的能力入口 `insertIntoNeighbour(...)`。
  - `client/GuiLayers`：`Minecraft#pushGuiLayer` 的版本适配点。
- 机器能量拆两半：core 的 `MachineEnergyBuffer`（纯整数：容量、接收 1024 上限、内部 drain、直接 set）
  + 平台的 `MachineEnergy`（`EnergyStorage` 适配器，`maxExtract=0`）。BE 存档键仍是 `Energy`，
  旧存档不需要迁移。
- `OreMaterials` 从“静态初始化时读 `ModList`”改成显式 `init()`（构造期、注册之前）+
  `Platform.isModLoaded`；漏调会立刻抛异常，而不是到世界加载才炸。
- 守卫脚本：`KNOWN` 清空，`common/config`、`common/platform` 加入受检目录；
  `MachineBlockEntity` 仍单独受检（core 状态机，能力只走门面）。
- 验证：`./gradlew build` 通过、守卫 0 泄漏、`runServer` 起来后 RCON 抽验 20 FE/t、
  自动输出进下方箱子、漏斗只抽输出格（明细见 [status.md](status.md) 验证表）。

## 29. P2 数据管线：一份输入、两个 target（2026-11-01）

问题：vanilla 类型 JSON（recipe / loot / tag）的目录名、`result` 字段、条件命名空间、
common 标签命名空间随版本变；1.20.1 分支的数据是手抄的，已经漂过一次（tech-debt #1）。
P2 把“版本差异”收进一个模块，让数据只维护一份。

- `scripts/data_target.py`：`DataTarget` 是唯一知道 1.21.1 / 1.20.1 差异的地方。
  生成器只用 `TARGET.recipe_path / loot_path / tag_path`、`TARGET.result_key`、
  `TARGET.condition_key`、`TARGET.mod_loaded / negate`、`TARGET.common_ref`，
  不写死目录名和 `neoforge:`。
- 四个数据生成器接受 `--target`（选版本）和 `--out`（选输出树，默认仓库）；流程仍是
  “生成 → 人 review diff”。
- `scripts/port_data.py`：从 1.21.1 树整体生成 1.20.1 数据（路径/字段/命名空间），数值原样；
  用于给 1.20.1 分支铺底。之后两边**数值改动只能在生成器或手写源里做**，不要在移植产物上补丁。
- `scripts/check-data-paths.py`：按 target 反查目录名、`result` 键、条件前缀、标签命名空间。
  单独成脚本而不是塞进 `check-textures.py`：报错要能直接指到“哪个文件哪个字段错了”。
- **合并而不是覆盖**：`lang` 用 setdefault（人工文案优先）；筛表按 item 合并、按材料权重更新、
  清理已移除材料的残留。之前直接重跑会把 M2/M6 手加的 13 条筛出物（沙 12 + 砂砾 1）删掉。
- 验收：生成器对当前树重跑零 diff；port 产物过守卫；生成器产物与 port 产物在 240 个文件上
  逐字节一致（只剩合并型标签与筛表的上下文差异）。

## 30. M7 datagen 第二批：配方（2026-11-02）

问题：145 条配方是手写 JSON，id、形状、条件都要和 `materials.json`、注册表人工对齐；
新增材料时最容易被漏掉。第一批已经把标签/掉落表收进 `runData`，这批收配方。

- `datagen/ModRecipes` 从**注册表 + 材料表**推导：矿链 81 条一条循环出，桶/坩埚按木种表，
  机器/插件/人偶按内容分节手写，共 **133 条**；id 与旧 JSON 逐一相同。
- **不生成 advancement**：`RecipeBuilder#save` 默认会写 `advancement/recipes/**`，本项目从没发过，
  多 133 个会改变配方书解锁。用一层 `RecipeOutput` 包装丢掉 `AdvancementHolder`，
  但保留 builder 的 `unlockedBy(...)`（它自己的 `ensureValid` 要非空 criteria）。
- **烹饪不用 `SimpleCookingRecipeBuilder`**：builder 按产物推导 `food`/`blocks` 书类别，旧 JSON 缺省
  是 `misc`。直接构造 `SmeltingRecipe`/`SmokingRecipe`/`CampfireCookingRecipe` +
  `CookingBookCategory.MISC`；合成配方统一 `RecipeCategory.MISC`。
- **12 条 `*_smelting_mekanism` 留下手写**：`ItemStack` 必须指向已注册的 `Item`，而 Mekanism 不在
  datagen classpath 上。生成的是带 `not(mod_loaded(mekanism))` 的兜底配方，候选材料在
  `smeltingTarget(...)` 里**总是**回退自有锭，不受 runData 环境的模组影响。
- 支撑改动：`OreMaterial` 增加 `ingotCandidates`（原始有序候选），`OreMaterials` 拆成
  `readIngotCandidates` + `resolveIngot`；`ingot` 仍是运行时解析结果，注册与标签不变。
- 验收：`scripts/compare-recipes.py`（shaped 去空行空列成格子、shapeless 多重集、烹饪六字段、
  条件规范化）对 145 旧 / 133 新给出 `0 different`；删旧文件后 `runData` `written: 0`；
  `runServer` `Loaded 1423 recipes` 与迁移前一致。细节见 [datagen.md](datagen.md)。

## 31. M7 datagen 第三批：blockstate 与模型（2026-11-02）

问题：244 个 blockstate/model JSON 是 Python 生成器写出来的，同一份信息在
「Java 注册表」「Python 表」「JSON 文件」三处各有一份；改一个木种要动三处，重跑脚本还会
把手工调整盖掉。配方批次之后，这批是最后一块「数据跟着表走」的资产。

- `datagen/ModBlockStates`：71 个 blockstate + 29 个模型。blockstate 全部是「一个 state → 一个模型」，
  只有筛子是 multipart（frame + 三档 mesh，`when: {mesh: n}`）。
- `datagen/ModItemModels`：144 个物品模型**从注册表推导**——方块物品的 parent 指向方块模型
  （`sieve` → `sieve_frame`，矿块 → 共享的 `ore_<kind>_block`），其余 `item/generated`，
  锤/钩 `item/handheld`，瓷桶两层 + `filled` predicate。新增物品不再需要手写模型。
- `datagen/WoodTypes`：11 个木种的唯一一张表，配方（`ModRecipes`）和模型（`ModBlockStates`）
  都从它派生；桶/坩埚的 id 规则（oak 保留无前缀 id）也在这里。
- **手绘几何不迁**：`barrel_base`/`crucible_base`/3 个矿块/筛框与三张网/四台机器，以及
  `electric_hammer`/`fuel_hammer` 两个带 elements 的物品模型。datagen 只写「parent + textures」
  这一层，几何仍是要人调的资产。
- **provider 顺序是有语义的**：`ModBlockStates` 先于 `ModItemModels` 注册，因为物品模型用
  `getExistingFile` 校验方块模型父级，而 `ExistingFileHelper` 要到前一个 provider 调
  `trackGenerated` 之后才认识它们；顺序反了会在 datagen 期就抛「model does not exist」。
- `check-textures.py` 同步升级：同时扫 `src/main` 与 `src/generated`（后者覆盖前者），并新增
  blockstate→模型、模型 `parent` 两条引用检查。以前它只看 `models/` 下的贴图引用，模型改名/
  blockstate 指错都查不出来。
- **Python 生成器的收尾**比迁移本身更重要：桶/坩埚两个只写 JSON 的脚本删除；矿/筛/机器三个改成
  只写 PNG + 手写玩法表（锤表、筛表、lang）。否则任何人重跑一次脚本，244 个旧 JSON 就会
  复活并与 generated 同名冲突（`processResources` 会直接失败）。
- 验收：`scripts/compare-models.py`（variant 键序、multipart 段序都按语义比较）对
  259 旧 / 244 新给出 `244 identical`；删旧文件后 `runData` `written: 0`；客户端启动无
  missing model/texture。细节见 [datagen.md](datagen.md) §4。

## 32. 科技模组锭接入：只接「空岛上拿不到的基础资源」（2026-11-02）

定位先说清楚：**我们负责让基础资源的来源在空岛上说得通，合金由科技模组自己做**。
所以这一轮只做「同一块矿砾熔成谁家的锭」，不加新金属、不加合金、不占 `c:dusts` 标签。

- **一个材料一条有序候选表**（`materials.json` 的 `ingots`）：tin → `mekanism` →
  `thermal_foundation`；lead → `mekanism` → `thermal_foundation` → `immersiveengineering`；
  silver/nickel → `thermal_foundation` → `immersiveengineering`；zinc → `create`；
  osmium → `mekanism`；uranium → `mekanism` → `immersiveengineering`；aluminum →
  `immersiveengineering`（无 IE 时自有锭）；platinum 保持自有锭。顺序 = 优先级，运行时
  `OreMaterials` 取第一个已加载的模组；都不在就注册并使用自有锭。
- **数据侧必须和运行时同构**：base 配方带 `not(or(候选 mod...))`，第 i 条的变体带
  `mod_loaded(i)` **加上** 对它之前所有候选的 `not(mod_loaded(...))`。少了后半截，两个
  科技模组同时装时同一输入会匹配两条熔炼配方，结果取决于加载顺序。
- **两条线的机制不同、键名也不同**：NeoForge 1.21.1 读 `neoforge:conditions`，Forge 1.20.1
  读**裸 `conditions`**（`RecipeManager` 在解析前判定）。写错键不会报错——Forge 直接忽略，
  然后按无条件解析，于是未安装模组的 `thermal:`/`create:` 物品变成 `Unknown item`，
  整个配方表只剩 7 个类型。这正是 P2 的 `port_data.py` 之前的转换错误（`forge:conditions`），
  现在改成 `conditions`，并给 `check-data-paths.py` 加了「键必须对」的检查。
- **为什么变体仍是手写**：产物是别的模组的物品，datagen 里的 `ItemStack` 必须指向已注册的
  `Item`（同 §30 的 Mekanism 结论）。base 由 `ModRecipes` 生成，变体手写，两边用同一张表。
- **两条线共用一份 `materials.json`**：Thermal / Create / IE 只在 1.20.1 有对应版本，主线的
  表里也留着它们的候选，在 1.21.1 上永远不命中、对应的 30 条变体也永远不加载——换来的是
  「一份表、两条线不漂移」。
- 验证：主线装 Mekanism 时熔炉烧 `tin_ore_gravel` 得到 `mekanism:ingot_tin`、自有 `tin_ingot`
  不注册；不装 Create 时 `zinc_ore_gravel` 回退自有锭；1.20.1 分支无模组时 0 条解析错误、
  42 条变体全部按条件跳过。Thermal/Create/IE 的真机验证见 §35 与
  [status.md](status.md) 验证表。

## 33. 泥土筛石子，以及「矿块被别人家机器当成矿」（2026-11-02）

两个来自实测的洞：

1. **空岛缺石头的来源**。空岛早期没有石头，但原版 Ex Nihilo 的泥土筛会出石子。这里补成
   四种（圆石 / 安山岩 / 花岗岩 / 闪长岩），**一次成功掷 4-6 个**，4 个合成一块对应石头。
   为此筛表从「每条产出 1 件」扩成可选的 `min`/`max`（默认 1），判定仍然是
   `SieveRolls` 这一处——手动筛、四台机器、JEI 都读同一份表，没有第二份掉落逻辑。
2. **矿块能不能进别人家的机器**，本质是**标签对不对**，不是配方。Thermal 的磨粉机全部走
   `forge:ores/<m>`，我们的 12 种矿块直接能吃；Create 分两种：8 种材料走标签（直接吃），
   **铁/金/铜/锌**走具体物品（`minecraft:iron_ore`、`create:zinc_ore`），标签对它无效——
   于是补 **12 条** `create:crushing` 当桥（4 材料 × 砾/砂/尘）。这也说明「支持的模组」不止是锭：
   **凡是我们替代掉的资源，都要在对方认的键上出现一次**。

副作用与约束：

- `create:crushing` 两个版本的 JSON 字段不同（1.21.1 用 `results[].id` + `processing_time`，
  1.20.1 用 `results[].item` + `processingTime`），P2 的 `port_data.py` 不覆盖这类第三方配方，
  两条线各写一份，文档里列成对照表。
- 石子掉率按所有者要求改成 **100%**：一次泥土必出四种、每种 4-6 个，即一次泥土 ≈ 4-6 块石头。
  这是单一数字，调平衡只改 `sieve/dirt.json` 里的 `chance`（`min`/`max` 管数量）。

## 34. 树的起点：泥土筛树苗 + 下界菌岩孢子（2026-11-02）

1. **泥土筛补树苗**。经典 Ex Nihilo 的泥土筛本来就有「树种子」，我们之前只有钩树叶一条来源，
   空岛一旦把第一棵树砍完就没有回头路。这次把泥土筛做成树的起点：5 种旧树苗（22%）+
   1.17–1.20 的樱花 / 红树 / 杜鹃 / 盛开的杜鹃（9%），合计 31%/次。深色橡木仍保留
   「巫水 + 橡树苗」的仪式感；苍白橡木是 1.21.4 的物品，两条目标线都没有，等升目标版本再加。
   - **杜鹃在 `#minecraft:saplings` 里**（1.20.1 也是）：筛表加它不会绕开任何标签，
     堆肥按树苗的 125 走；只有菌岩孢子这种新物品才要同时补 `ModItemTags` 与
     `compost/default.json`。
2. **下界树**。1.16 起绯红 / 诡异是菌而不是树，需要菌岩才能长成巨菌；空岛没有菌岩，
   所以不能只把菌柄塞进筛表。采用 Sequentia 的**孢子链路**（只参考链路，不复制它的资源）：
   - 碎下界岩过筛 → `crimson_nylium_spores` / `warped_nylium_spores`（各 5%）；
   - 孢子右键**下界岩** → 对应菌岩（`NyliumSporesItem`，和草种子 / 古代孢子同一种一次性转换）；
   - 之后完全交给原版：菌岩撒骨粉 → 菌 / 根；菌上再撒骨粉 → 巨菌 → 菌柄。
   这样不新增桶规则，也不把「绯红木桶 / 木坩埚」变成无来源物品。孢子做成物品而不是
   「巫水 + 下界岩 → 菌岩」，是为了让 JEI 的碎下界岩页直接展示下一步；桶路线会藏掉一个台阶。
3. **平衡**：孢子各 5%（一次碎下界岩 10%），和 Sequentia 同档；但我们的筛表不区分筛网，
   所以实际比它宽松——这是本项目「筛网只管耐久」的既定口径，不为下界树单开例外。
   菌岩孢子堆肥 100（花 / 蘑菇档），JSON 和标签同步；杜鹃按树苗标签的 125，不另写。
4. **竹子**：原版没有独立的「竹笋」物品，`minecraft:bamboo` 本身就是可种植的竹笋；所以
   不新增自定义物品/贴图，只在泥土筛加 5%（Sequentia 的泥土筛也用 `minecraft:bamboo`；
   概率按本项目自己的表定）。竹子已经在堆肥表里（100），竹桶 / 竹坩埚 / 脚手架也有配方，
   缺的只是第一个来源——加一个掉落就闭环。

## 35. Immersive Engineering：第二个科技模组锭接入（2026-11-02）

定位延续 §32：**只接基础锭，不接合金**；这一批用 IE 1.20.1 `10.2.0-183` 真装验证。

- 候选：silver / nickel → Thermal → IE；lead → Mekanism → Thermal → IE；
  aluminum → IE；uranium → Mekanism → IE。IE 的锭 id 是 `ingot_aluminum` 这类，
  **不是** `aluminum_ingot`；模组 id 是 `immersiveengineering`。
- 15 条变体（5 材料 × 3 筛料）按 §32 的「本模组 + 之前候选的否定」写；base 由 `runData`
  重算成 `not(or(...))`。两条线共用一份 `materials.json`，IE 没有 1.21.1 版，主线保留
  候选 / 变体，永不命中。
- 验证：IE-only 时 `[materials]` 的 5 条全部解析到 `immersiveengineering:ingot_*`，RCON 熔炉
  `aluminum_ore_gravel → ingot_aluminum`、`silver_ore_sand → ingot_silver`；Thermal + Create + IE
  同时装时优先级正确（Thermal 拿 tin/silver/lead/nickel、Create 拿 zinc、IE 拿 aluminum/uranium）；
  主线无 IE 时 `aluminum_ore_gravel` 回退自有 `aluminum_ingot`。
- **IE 粉碎机**输入走 `forge:ores/<m>` 标签，1.20.1 的 12 种矿块本来就在标签里，所以不需要
  补 IE 配方；产物是 IE 自己的 dust（我们的矿尘不占 `forge:dusts`，这是 §32 的既定边界）。
  多方块本身没跑，只核对了标签与配方输入。
- 坑：IE 在专用服会尝试加载 client 类并打 `DISTXFORM` 报错（IE 自己的事，server 仍然 Done），
  别当成本项目错误；它的 crusher 只认 `forge:ores`（1.20.1），不吃 `c:ores`。
- 合金（steel / electrum / constantan）不接，避免抢科技模组的中间产物。

## 36. Tinkers' Construct 3 联动（2026-11-02，1.20.1）

这一批只做 **1.20.1**（Tinkers 3.12.1 + Mantle 1.11.117 真装）。目标不是复刻匠魂，而是把
「空岛拿不到的资源」接到匠魂的炉子上。

1. **碧空 / 末影黏液草种子**。匠魂自己有这两个种子，但只认 `DirtType`（原版泥土 / 黏液泥土），
   **右键草方块不生效**。所以本项目注册自己的两个条件物品，右键草方块或泥土直接换成
   `tconstruct:sky_vanilla_slime_grass` / `ender_vanilla_slime_grass`：
   - 物品只在 `tconstruct` 在场时注册（`ModItems` 条件注册，缺失时字段 `null`）；
     创造栏 / JEI 都不会出现，筛表条目用 `"mod": "tconstruct"` 在物品解析前过滤。
   - datagen 在没有匠魂时看不到这两个物品，**模型/贴图必须手写**（`models/item/*_slime_grass_seeds.json`）；
     泥土筛 2% / 1%。
2. **钴**。`materials.json` 加第 13 种材料 `cobalt`，候选 `tconstruct:cobalt_ingot`；
   没匠魂时回退自有 `cobalt_ingot`。碎下界岩筛 `cobalt_broken` 1%，然后走普通
   4→矿砾块→熔炼链。**broken 贴图的下界岩红棕基底**由 `STONE_PALETTES` 覆盖，
   不再用其他矿碎块的煤灰底。
3. **下界合金碎片**。碎下界岩 0.4%，**双线都加**（用户明确要求）；不参与匠魂熔炼。
4. **匠魂熔炼**。13 材料 × 6 形态 = **78 条** `tconstruct:melting`：
   - 物品（broken / crushed / powdered）→ **60 mB**（1/2 粗金属）；
   - 方块（ore_gravel / ore_sand / ore_dust）→ **240 mB**（2× 粗金属）；
   - 流体用 `forge:molten_<m>` 标签，温度复制 Tinkers 自己的锭配方，时间按 90 mB 换算；
   - 全部带 `conditions: mod_loaded(tconstruct)`；没匠魂时整批跳过。
5. **阿迪特不做**。Tinkers 3.12.1 已移除阿迪特（没有 `molten_ardite` / `ardite_ingot`，
   只有残留贴图）；等所有者说的「工匠巧思」拓展重现 1/2 代阿迪特后再按同一套加。

验证：只装 Tinkers 时 `[materials] cobalt → tconstruct:cobalt_ingot`、`[sieve] 92 drops`；
RCON 熔炉 `cobalt_ore_gravel → tconstruct:cobalt_ingot`；两个种子 `summon` 正常；
不装 Tinkers 时 `[sieve] 90 drops`（种子条目被过滤）且没有 unknown item/recipe。
**右键草方块/泥土、匠魂炉实际熔化、JEI 页还没进游戏**。

坑：Tinkers 的熔炼 `result` 是流体（`tag` + `amount`），没有 `item`/`id`；`check-data-paths.py`
已改成只对 item/id result 检查版本键，否则 78 条熔炼会全部误报。

## 37. 数据包 v2：标签输入 / `mod` 门 / 补丁（2026-11-03）

目标：把「数据包能改」从“能覆盖 JSON”推进到“能当扩展 API 用”，先不引入 KubeJS /
CraftTweaker 的 Java 依赖。

1. **标签输入**。七张表的 id 字段都接受 `#tag`，在加载时展开成当前注册表对象，玩法代码
   仍是具体 id 查表。筛子 / 钩子对同一物品**合并**掉落（去重）；锤子 / 熔点 / 热源是
   一条规则一个方块，**具体条目优先**；桶做 base × added 叉积。
2. **`mod` 门**。文件根 `"mod": "x"` 在解析任何 id 之前判断，避免未装模组时刷 unknown id；
   筛子 / 钩子的单条掉落也可以各自带 `mod`。
3. **补丁**。`"patch": true` 或出现 `"remove"` 的文件进入第三遍：`remove: true` 删整条，
   `remove: ["id", "#tag"]` 删列表条目；补丁按资源 id 排序，所以多包改同一条目时顺序确定。
   `remove` 也接受标签，用来“删掉所有树叶的某条掉落”这类操作。
4. **时序坑**。`TagManager` 只在 reload 中存 `LoadResult`，真正的 `bindTags` 在
   `ReloadableServerResources.updateRegistryTags()`，也就是所有 listener 之后。第一版在
   `apply()` 里展开标签全是空集，日志 `[sieve] 5 inputs, 75 drops`。改成：
   - `load()` 只建具体条目，并缓存原始 `JsonObject`；
   - `TagsUpdatedEvent`（server / client 都会发）里再 `rebuild()`，展开标签并应用补丁；
   - 客户端打开 F3+T 时标签仍在，`load()` 允许直接展开，不会丢 JEI 的标签条目。
5. **诊断**。`/exnihilo tables` 看条数；`/exnihilo sieve|hammer|compost <id>` 直接查一条
   规则，用来确认补丁删/加是否生效。示例包 `examples/datapack/` 覆盖全部语法。

验证：见 `docs/status.md` 的「当前批次（2026-11-03）」；测试包下 tag 展开后
`[sieve] 45 inputs, 115 drops`、`[hammer] 91`、`[crook] 32 drops`、`[crucible] 49 heat`，
删补丁文件后 `/reload` 恢复 `hammer 91` / `barrel 11`；`sieve sand` 已无 cactus、
`hammer end_stone` 无规则、`compost bread` = 100。

没做：专用服客户端 JEI 同步（需要网络包）；1.20.1 分支移植；KubeJS / CT 的一等集成。

## 38. 决策：自定义三路径等价（2026-11-03）

**背景。** 玩家已经在问“能不能用 KJS/CT 改筛子/桶/堆肥”。当前数据包路径完整，
KJS 只能走 `kubejs/data`，CT 只能改原版配方；这与“尽可能支持高度自定义”的产品理念不符。

**决策。**

1. **数据包 / KubeJS / CraftTweaker 是三条目标等价路径**，不是“数据包 + 两个脚本补丁”。
   最终验收：同一份 fixture 用三种写法表达，`/reload` 后 `/exnihilo dump` 快照一致。
2. **数据包仍是规范来源。** 任何新表/新字段先有 JSON 形态、loader、校验和文档，再谈
   KJS/CT 入口；脚本入口不允许绕过校验。
3. **统一 TableBuilder + overlay。** JSON loader 和脚本贡献都写同一个 builder；
   脚本贡献在每次 reload 后重新应用，不能直接改全局 map，也不能在 `/reload` 后丢条目。
4. **KJS 用 plugin + 事件，CT 优先用 ZenScript API。** CT 不强行把七张表塞进
   `RecipeManager`；先保证“能改表”，recipe type/JEI 显示后置。
5. **文档是 Definition of Done。** 维护契约见 `wiki/MAINTAINING.md`；
   能力矩阵见 `wiki/README.md`；差距和阶段见 `docs/customization-roadmap.md`。
   任何页面不得把未支持的能力写成“支持”。
6. **注册期内容不参与等价化。** 新物品/方块/材料仍由注册期决定；KJS 可以拥有自己的物品，
   但不进 `materials.json` 链。数据包/CT/KJS 都不能在运行期新增材料。

**后果。**

- 七张表的 loader 要重构出 `TableBuilder` / `CustomizationOverlay`（roadmap P1），
  之后才接 KJS（P2）和 CT（P3）。
- 每次改表结构要同时改 wiki 和 roadmap 的能力矩阵，并跑 `scripts/check-wiki.py`。
- 专用服客户端 JEI 的同步（P4）是“看起来等价”的必要条件，但不阻塞 P1–P3。

**当前状态。** P0（数据包 v2）已完成并在两版验证；P1 尚未开始。

## 39. 末影蛋糕（2026-11-03）

补 `parity.md` 的 End Cake 缺口。原版参考是 Omnia 1.10.2 的 `BlockEndCake`：可放置蛋糕，
放置时只有 1 片，末影之眼右键把 `bites` 减 1（补一片），空手右键吃一片并
`changeDimension(1)`（仅主世界）；吃一片 +2 饥饿 / 0.1 饱和度；原版末地/下界不传送。
本实现额外补了“末地 → 主世界”的回程（用户要求）。

实现：

1. **`EndCakeBlock extends CakeBlock`**，复用 vanilla 的 `BITES`、形状、`canSurvive`、比较器。
   - `getStateForPlacement` 返回 `bites=6`（1 片），所以物品放置和 `/setblock` 的默认状态不同；
   - `useItemOn`：末影之眼且 `bites>0` → `bites-1`、消耗 1 个眼、播放 `ENDER_EYE_LAUNCH`；
   - `useWithoutItem`：空手且（可进食或创造）→ 吃、`bites+1` 或移除方块、按维度传送
     （主世界 → 末地，末地 → 主世界）。
2. **传送**：主世界吃 → `ServerPlayer.teleportTo(ServerLevel.END, 0.5, height+1, 0.5, ...)`，
   用 `Heightmap.MOTION_BLOCKING_NO_LEAVES` 找中央岛表面；末地吃 → 传回玩家在
   **主世界**的床/重生锚点，没有则世界出生点；下界不传送。不走 `changeDimension(1)` 的
   1.10 写法，也不创建 1.10 的末地平台。
3. **配方/资源**：8 末影珍珠 + 蛋糕；4 张 16×16 贴图（Creatio 原图经 `art/blocks/end_cake/restyle.py` 裁边/放大/描边），
   模型只写 `parent: minecraft:block/cake*` + `textures` 覆盖，不复制 vanilla 模型文件；
   无掉落表（和 vanilla 蛋糕一致）。
4. **1.20.1**：同一套资源/配方；`EndCakeBlock` 单独实现 `use(...)`（1.20.1 没有
   `useItemOn`/`useWithoutItem`），`stack.shrink(1)` 代替 `ItemStack.consume`，
   `InteractionResult.sidedSuccess`。

差异/坑：

- 原版 Omnia 有 `end_cake_hunger` 配置（满饥饿时能否吃）；我们没加配置，**始终可吃**，
  避免“在末地满饥饿导致回不了主世界”。
- 原版 `changeDimension(1)` 会把玩家丢到末地默认入口；我们直接落到中央岛表面，
  不需要先有末地平台。回程按玩家要求补成“末地吃 → 主世界床/出生点”，这是对
  Omnia 原版“不能回传”的有意偏离。
- `/setblock` 放出来是 `bites=0`（vanilla 默认状态），不是物品放置的 `bites=6`；
  这是命令调试的正常差异，不要当 bug。

