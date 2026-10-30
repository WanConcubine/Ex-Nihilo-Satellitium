# 设计文档：无中生有：众星捧月

> 状态：M1（木桶 + 材质变体）/ 最后更新：2026-10-28

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
| 配置 | `ForgeConfigSpec`（common / client / server） |

### 包结构

```
com.kaifengcrt.exnihilosatellitium
├── ExNihiloSatellitium.java        # 入口
├── registry/                    # DeferredRegister
│   ├── ModBlocks.java
│   ├── ModItems.java
│   └── ModCreativeTabs.java
├── common/                      # 与加载器无关的逻辑
│   ├── block/
│   ├── blockentity/
│   ├── item/
│   ├── recipe/
│   └── menu/
├── data/                        # 数据生成
├── client/                      # 客户端渲染/屏幕
├── compat/                      # JEI、月光魔法等
└── config/
```

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
   没装能源模组时的行为待定：慢速无电运行，或干脆不启动。
3. **插件化**：速度 / 幸运（产量）作为 upgrade 物品，只调判定函数的参数，不改判定本身；
   同类词条也可以挂在手动工具上（可选，视平衡）。

与 M2 的接口：M2 交付手动筛时就把判定抽成纯函数 + JSON 表，M10 的自动筛只接电与计时，不返工。

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
- 代码侧统一走 `compat/MoonlightCompat`：只检查 `ModList` + 按注册名查
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
  `rotation: 90`），两道铁箍是 8 个外凸 0.5px 的 `gray_concrete` 体素。
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
- 切角八边形还没做；外凸箍已做（8 个 0.5px 体素），当前是方桶身 + 外凸箍的折中。

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
- 数据文件由 `scripts/generate_barrel_variants.py` 生成：blockstate、block/item model、
  shaped recipe、loot table、`mineable/{axe,pickaxe}` 标签都来自同一张材质表。
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
4. **熔岩掺料**：满熔岩 + 红石 → 地狱岩；+ 荧石粉 → 末地石。
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
