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
  让当年那套“一棵树起步、一套完整资源链”的空岛体验，在 1.20.1 Forge 上重新成立。
- 之所以做：`无中生有-世间万物`（Omnia）曾经做到过这件事，但已停更；经典循环不该断在这里。
- **联动是本职，不是附加项**：Ex Nihilo 系本身不产矿，它的价值就是把别的模组的资源接进空岛。
  所以“给别的模组做支持”是一等公民需求，写法见 §6。
- 复刻的是**结构与流程**：方块种类、判定顺序、产出链条、交互手感、失败/返工回路。
  不是数值与美术的照抄（那条写进下面的非目标）。

**目标**

- Minecraft 1.20.1 / Forge 47.x / Java 17。
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
- 支持 1.20.1 以外的版本（后续再评估）。
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
| 配方 | 1.20.1 自定义 `RecipeType` + `RecipeSerializer`（JSON，`Codec`） |
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
   （筛出物、堆肥值、坩埚熔炼、热源），具体模组的条目写在 JSON 里，配 `forge:mod_loaded` 条件。
2. **数值走数据包，材料走表**：配方、概率、堆肥值这类**数值**用数据包就能覆盖；
   但**新增材料**（要注册新物品链）在 1.20.1 必须发生在注册期，只能进扩展材料表 + 重启
   （§6.4）。需要自定义判定或特殊工具的，才由我们写 `compat/` 模块。
3. **硬绑定必须可缺**：`compat/` 按模组分包，只按注册名查 `ForgeRegistries`，
   **不 import 对方任何类**；没装该模组时类不加载、不报错（做法见 §6.3）。
4. **注册只认材料表，自动发现只负责补骨架**（2026-10-29 修订）：自动发现能力保留，但
   “扫到即生效”不存在。内置材料表由我们手写（颜色、`tier`、`rarity` 人工定），表内材料
   **真的在游戏里存在**（整套矿砾/碎矿/粉末链，见 §6.4）；表外材料只生成骨架行、
   **默认关闭**，由整合包作者或玩家填写数值后开启。理由：概率必须是人调出来的手感，
   自动推导（tag 或 worldgen 密度）会飘、会打穿整合包进度设计；但也不能让整合包作者
   为了一个冷门模组等我们发版。

   自动发现的合法用途只有两个：**① 给表外材料生成骨架行**（默认关闭）；
   **② 开发期覆盖自检**（列出“有材料、没条目”，提醒维护者补）。

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

**对接对象**（本机 `Projects/Minecraft-Mod-(Neo)Forge/1.20.1`）：

| 项 | 值 |
|---|---|
| mod id | `moonlight_magic` |
| 版本 | 1.2.0 |
| 包名 | `com.moonlight.magic` |
| 加载器 / MC | Forge 47.4.x / 1.20.1 |
| 许可证 | MIT |
| 公开 API | **没有独立 `api` 包**（有 `event` / `ritual` / `spell` / `energy`，但都是内部实现） |

**策略**

- `mods.toml` 里声明 **optional** 依赖（`mandatory=false, ordering=AFTER`），
  不装月光魔法时本模组必须能独立启动。
- 代码侧统一走 `compat/MoonlightCompat`：只检查 `ModList` + 按注册名查
  `ForgeRegistries`，**不 import 对方任何类**，避免编译期耦合和版本脆断。
- 内容联动优先用**数据包**（配方 / 掉落 / 堆肥值 + `forge:mod_loaded` 条件），
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

**三层来源**：

| 层 | 位置 | 谁写 | 默认 |
|---|---|---|---|
| 内置材料表 | jar 内 `exnihilosatellitium/materials.json` | 我们 | 启用 |
| 扩展材料表 | `config/exnihilosatellitium/materials.json` | 整合包作者 / 玩家 | 表外材料默认关闭，填值才开 |
| 数据包 | `data/*/...` | 任何人 | 覆盖配方与概率；**不能新增材料** |

**硬约束：新增材料必须重启。** 1.20.1 的物品/方块在注册期冻结，材料表必须在注册阶段读取，
所以它不能是数据包，只能是 jar 内资源 + config。数据包能改的是概率与配方，
不能“让一个新物品凭空出现”。

**贴图（两条线，都不手绘方块底图）**：

- **物品**（破碎 / 粉碎 / 粉末）：5 阶调色板手绘像素，已完成的 11 种见
  `art/items/ore_pieces/generate.py`；新金属 = 加一条 `PALETTES`。
- **方块**（矿砾 / 矿砂 / 矿尘）：**原版底 + 遮罩 + 纯色按 alpha 混合**，与原版/Omnia 同一套算法。
  底图引用 `minecraft:block/gravel`、`sand`、`light_gray_concrete_powder`（不复制、不打包）；
  我们出三张遮罩（白色 + alpha）：矿砾 21% 碎点 / alpha 178，矿砂 100% / 126，矿尘 100% / 110。
  罩色必须用材料表的**身份色**（`materials.json` 的 `color`），不能用物品调色板的中间明暗阶——
  那会让铁这类浅色金属发白（踩过）。
  模型：底 cube `tintindex -1`，遮罩面 `tintindex 0` 收 `BlockColor`，render layer 用 `translucent`。
- 不接受另外两条路：① 按材料生成整套方块底图（16×16 读成小方块/竖条）；
  ② 在底图上画清晰矿斑（原版是纯色罩色，不是碎块，会变成另一种美术语言）。两条都试过，已废弃。

材料表因此必须带颜色；没有颜色的材料不给注册（宁可报错，也不要一堆灰方块）。

**JEI**：筛子是自定义 `RecipeType`，JEI 直接读 `RecipeManager`，动态注册的配方会自动出现在
JEI 里，不需要额外写分类。



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
  `data/<namespace>/compost/*.json` 提供数值（默认 65 条）；满 1000 后 600 tick（30 秒）进入 DIRT 状态，
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
  **2026-11-04（1.1.1）改成原版泥土方块模型 + 按投入物加权混色**，`barrel_compost.png`
  已删除，见 §49。
- **物品栏**：block model 补了标准 `display` 变换（gui/ground/手持）；`models/item/barrel.json`
  直接继承开口 block model，物品栏/手持是**空心**的 3D 桶。
- **贴图**：木桶不再有自有 PNG（直接引用 vanilla block 贴图）；当时保留 `barrel_compost.png`
  和 `porcelain.png` 两张自有 16×16（1.1.1 起 `barrel_compost.png` 不再使用）。`scripts/check-textures.py` 禁止 vanilla **item**
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

- `COMPOST_MAX = 1000`、`COMPOST_TICKS = 600`（30 秒；1.1.1 从原版的 50 秒缩短，见 §49）。
- `default.json` 改为 **65 条**（12 个标签 + 53 个具体物品），`compostable.json` 同步；
  肉类按 1.20.1 的十种生/熟肉显式列出，鱼用 `#minecraft:fishes`，种子/作物/蛋/线用
  `#forge:*` 标签。
- 老存档迁移：`saveAdditional` 写 `CompostScale:1000`；`load()` 发现没有这个键时把
  `Compost` ×10、`CompostTicks` 按 600→1000 缩放，旧测试桶不会变成 1/10 进度。
- 150 的瓜类 7 个是 1050，桶会截到 1000，和 Omnia “多塞一点会溢出但不会浪费”一致。

**验证**

- `./gradlew build` 通过；`python3 scripts/check-textures.py` 通过。
- 服务端 `/reload` 日志：`[compost] loaded 65 entries`，无 `unknown item` / `invalid tag`。
- RCON：`data merge {Compost:1000,CompostTicks:1}` → 2 秒后仍是桶、`DirtReady=1b`。
- 未验证：逐个物品右键塞桶的点数（需要玩家）；JSON 与标签的键已用脚本对齐。

## 12. 数据包 v2 移植（2026-11-03）

主线 2026-11-03 的数据包 v2 整套搬到 1.20.1：

1. **同一套 loader**：`common/datapack/DataPackJson` + 七张表的 `rebuild()`。id / 标签解析
   改用 `BuiltInRegistries`（1.20.1 里可用，且 modded 条目也在其中），不再依赖
   `ForgeRegistries`；Forge 的 `BuiltInRegistries` 弃用警告不影响运行。
2. **Forge 事件**：`net.minecraftforge.event.TagsUpdatedEvent`，`SERVER_DATA_LOAD` 重建服务端表，
   `CLIENT_PACKET_RECEIVED` 且 `shouldUpdateStaticData()` 时重建客户端表。
   `load()` 只建具体条目，标签在事件里展开，避免 `TagManager` 尚未 bind 时读到空集。
3. **版本差异**：
   - 通用标签 `forge:`（`#forge:gravel`），不是主线的 `c:`；
   - `base` / `added.fluid` 只保留 still 流体（`Fluid#isSource(defaultFluidState())`），
     因为 `#minecraft:water` 同时含 still/flowing，而桶里只存 still；
   - 示例包 `pack_format 15`。
4. **验证**：Forge 47.4.28 dev server + 测试包，tag 展开后 `[sieve] 45/115`、`[hammer] 91`、
   `[crook] 32`、`[crucible] 11 heat`、`[barrel] 11`；RCON 查 sand / oak_log / end_stone /
   bread 均符合预期；`/reload` 增删补丁文件能恢复。`build` 与纹理守卫通过。

坑：1.20.1 分支没有 `check-core-imports.py` / `check-data-paths.py`（P1/P2 未回移），
这两项守卫只能在主线跑；后续回移 P2 时再补。

## 13. 末影蛋糕（2026-11-03）

补 `parity.md` 的 End Cake 缺口，行为参考 Omnia 1.10.2 `BlockEndCake`：可放置蛋糕，
放置 1 片，末影之眼右键补 1 片（`bites-1`），空手右键吃一片：主世界传送到末地、末地传回主世界
（床/世界出生点）；+2 饥饿 / 0.1 饱和度；下界不传送。

1.20.1 实现：

- `EndCakeBlock extends CakeBlock`；`getStateForPlacement` 返回 `bites=6`。
- `use(...)`：末影之眼且 `bites>0` → `bites-1`、`stack.shrink(1)`（创造不消耗）、
  `ENDER_EYE_LAUNCH`；空手且可进食 → 吃、`bites+1` 或移除、按维度传送（主世界→末地，末地→主世界）。
- 传送用 `ServerPlayer.teleportTo(ServerLevel, ...)`：主世界 → 末地中央岛
  （`Heightmap.MOTION_BLOCKING_NO_LEAVES`）；末地 → 主世界床/重生点或世界出生点。
  不创建 1.10 的末地平台。
- 配方 8 末影珍珠 + 蛋糕；4 张 16×16 贴图（Creatio 原图经 `art/blocks/end_cake/restyle.py` 裁边/放大/描边），
  模型只写 `parent: minecraft:block/cake*` + `textures`；无掉落表。

回程是对 Omnia “不能回传”的有意偏离（用户要求）。
未验：右键吃/传送、末影之眼补片、模型画面；`/setblock` 默认 `bites=0`，物品放置才是 `bites=6`。

## 14. 科技模组矿物链：氟石 + Thermal 六种（2026-11-03）

- **范围**：Mekanism 氟石，Thermal 磷灰石/朱砂/硝石/硫磺/红宝石/蓝宝石；Create 只有锌，已有链。
- **`gems` schema**：`materials.json` 新增与 `ingots` 同构的 `gems` 数组。按所有者要求，
  `gems` 是**纯模组产物**：不注册自有兜底物品、不生成 `forge:gems` 标签、不生成 base 熔炼
  配方；**整条材料只在候选 mod 已加载时注册**（没装时物品/方块/碎料/筛出/锤表/配方/标签都
  不出现），矿块仍进 `forge:ores/<id>`（`required: false`），无 loot table、由 `SelfDropBlock`
  掉自身。`gems` 与 `ingot`/`ingots` 不能混用。
- **不直接掉成品**：7 种矿物都走三档矿链（筛碎料 → 4 合 1 矿块 → 熔炼），与金属同一套
  `SieveRolls` / `HammerRolls` / JEI 数据源；没有第二份掉落逻辑。
- **数据**：`gems` 没有 base；21 条变体手写（`conditions` 裸键 + `forge:mod_loaded`）。7 种矿物不写匠魂熔炼，因为 Tinkers 没有对应熔融液体。
- **验证**：同服装 Mek + Thermal + Create + IE + Tinkers，7 种矿砾 RCON 熔炼 7/7 产出
  `mekanism:fluorite_gem` / `thermal:apatite|cinnabar|niter|sulfur|ruby|sapphire`；筛表 113、锤表 73。
  无科技模组：13 entries、筛 90 / 锤 52，`ruby_broken` unknown，无缺失物品/掉落表报错。
未验：Thermal 机器吃新矿块（只核对标签）、JEI/客户端。
- **末影蛋糕图标**：物品模型改 `item/generated` + 16×16 透明 `item/end_cake`，`ModItemModels`
  跳过 `end_cake`，不再用方块模型当物品图标。

## 15. 应用能源 2：赛特斯石英（2026-11-03，两版数据与真装）

AE2 是空岛最典型的「没有筛子就永远拿不到」的科技模组：赛特斯石英只从陨石 / 晶簇来，
空岛没有陨石。经典 Ex Nihilo 也是筛沙出赛特斯，所以沿用「直接掉对方成品」的接入原则。

1. **只做两条**：沙 → `ae2:certus_quartz_crystal`（1/6）与 `ae2:certus_quartz_dust`（1/12）。
   不注册自有物品，不复制对方贴图 / JSON / 代码。
2. **不做充能赛特斯 / 天石粉**：充能赛特斯由 AE2 自己的 `ae2:charger` 充能（配方只要
   铁 / 铜 / 赛特斯，空岛能凑齐）；天石粉来自陨石天石，不属于空岛缺口。
3. **门控与形态**：`sieve/sand_ae2.json` 是独立 patch，根级 `"mod": "ae2"` + `"patch": true`；
   没装时在物品查找之前整文件跳过，基表不变、不刷 unknown item。两版 JSON 逐字相同。
4. **数值依据**：水晶 1/6 对齐经典 Ex Nihilo（Omnia 是 5%）；粉取水晶的一半。
5. **1.20.1 dev 坑**：AE2 生产 jar 直接丢 `run/mods` 会因 SRG mixin `@Shadow f_129762_`
   找不到而崩服；必须放 `libs/`，由 `-PtechMods` 做 `fg.deobf`（`build.gradle` 已加入口）。
6. **验证**：`-PtechMods` 全装 + AE2 15.4.12 + GuideME 20.1.15：`[sieve] 7 inputs, 141 drops`
   （基表 139 + 2），RCON sand 含 `certus_quartz_crystal 0.16667` / `dust 0.08333`；
   去掉 AE2 时 139、无 unknown item。手筛与 JEI 页面未目视。

## 47. 热力人偶：狂风 / 暴雪（2026-11-04）

- **来源**：所有者给的两张 JEI 合成图（4 × 硝石粉 / 雪球 + 红石 + 2 × 荧石粉 + 人偶 + 下界疣），
  参考 Ex Nihilo: Sequentia 的 Thermal 附属玩法：熔岩 → 狂风人、水 → 暴雪人。只借数值，不复制代码 / 资源。
  中文名按所有者指定用「狂风人偶 / 暴雪人偶」，不用参考图的「劲风 / 冻结」。
- **物品**：`blitz_doll` / `blizz_doll`，`stacksTo(16)`，模型 `item/generated`，64×64 贴图
  （金黄 / 冰蓝）。`registerThermalDoll` 在 `ModList` 没有 `thermal_foundation` 时返回 `null`，
  创造栏 null 检查——条件物品的既有做法（同 `registerTinkersGrassSeeds`）。
- **配方**：两张手写 shaped JSON（`xyx / zwz / xvx`），根级 `conditions` =
  `forge:mod_loaded` + `thermal_foundation`。不能交给 datagen：datagen 里物品不注册，
  `thermal:niter_dust` / `thermal:blitz_rod` 也不在 classpath。
- **桶规则**：`lava_blitz_doll_blitz.json` / `water_blizz_doll_blizz.json`，根级
  `"mod": "thermal_foundation"` + `entity` + `peaceful_drop`；右键立即生效，和平掉 `thermal:*_rod`。
- **验证**：`-PtechMods` 下 `[barrel] 14 transforms (2 fluid, 12 item)`；
  `/exnihilo dump barrel` 两条规则带 `entity:thermal:blitz` / `thermal:blizz` 与
  `peaceful:[thermal:blitz_rod]` / `[thermal:blizz_rod]`；`/summon item` 两物品显示
  Gale / Blizzard Doll；不带 `-PtechMods` 时桶 12、物品不存在；配方经 `/reload` 解析探测。
  条件物品的模型手写，`ModItemModels.HAND_FLAT_MODELS` 按名字跳过（临时强制注册后 datagen
  不会写出同名模型）；字段 `@Nullable`，创造栏 null 检查。
  **未验**：工作台摆放、右键桶、JEI、贴图亮度。

## 48. 村民 / 脑白金 / 蜂蜜链 / 蜜蜂 / 蜂箱 / AE2 陨石粉（2026-11-04）

- **物品**：`villager_doll`（8 绿宝石围人偶；巫水桶 → 僵尸村民，和平掉腐肉）、
  `brain_platinum`（蛋 4 + 糖 4 + 绿宝石；右键傻子村民 → 普通无职业村民）。
  脑白金另接 `PlayerInteractEvent.EntityInteract`：原版村民会先开交易界面，事件在
  `Entity#interact` 之前取消，普通右键即可生效（不必 Shift）。
- **桶表**：`BarrelRecipes` 增加 `resultFluid` / `delayTicks` / `drain`；`result_fluid` 与
  `result`/`entity` 互斥，`drain` 只配 `added.item` 且不要求满桶。
- **BE**：`PendingFluidFrom/To/Ticks` 保存 30 秒换液；到点且仍是原流体才替换，每 20 tick 同步；
  玻璃瓶抽取 250 mB → 蜂蜜瓶；浓缩蜂蜜桶每 100 tick 1/6 概率在 6 格内生成 1 只蜜蜂（上限 5）。
- **流体**：`sugar_water` / `concentrated_honey`（+ flowing）注册 still/flowing、`TintedWaterFluidType`
  （`initializeClient` 挂 `TintedWaterClientExtensions`，复用原版水贴图 + tint）、source block 和桶；
  桶内规则不变，桶只是运输/放置途径。
  **tint 不能存实例字段**：`FluidType` 构造器在子类字段赋值前调用 `initializeClient`，
  会读到 0（黑色 tint）；按 `descriptionId` 映射常量。
- **JEI / Jade / dump**：JEI 流体结果槽 + 延迟/抽取提示；Jade「转化中」；dump 输出
  `fluid:` / `delay:` / `drain:`。
- **AE2**：沙、砂砾各 1/16 `ae2:sky_dust`；**蜂箱**：3 木板 + 3 蜂蜜瓶 + 3 木板 → 原版蜂箱。
- **验证**：`-PtechMods` 下 `[barrel] 18 transforms (2 fluid, 16 item)`、`[sieve] 143 drops`；
  data merge 验证换液与养蜂；`/summon` 新物品正常；sand/gravel dump 含 `sky_dust 0.0625`。
  **未验**：真实右键链条、脑白金右键、JEI、流体亮度。

## 49. 1.1.1 视觉修正：堆肥混色 + 木坩埚树叶模型（2026-11-04）

**问题**：木桶堆肥只有一个固定的 `barrel_compost.png` 贴图，投腐肉/树叶颜色不变，远看是
纯色方块；木坩埚只熔树叶/树苗→水，水量超过固体后按经典规则切成蓝色水盒，玩家要求
“堆肥按投入物比例混色”“木坩埚堆水显示树叶模型”。

**实现**

- `BarrelBlockEntity` 新增 `CompostIngredient(item, points)` 列表，NBT `CompostItems`
  （`getUpdateTag` 同步）；部分填满时只记剩余空间，权重和 = 1000。
- `client/ItemTint` 采样物品图标平均色（方块物品走 block particle icon，普通物品走
  baked item model），乘 `ItemColors` 的 tint（**树叶/草贴图是灰度的**，不乘植被色会得到
  灰白）；按 sprite 实例缓存，资源重载失效。
- `BarrelRenderer` 用 `renderSingleBlock` 画原版泥土方块模型（只缩放 Y），
  `TintVertexConsumer` 按点数加权混色；腐熟计时中 tint 淡回纯白，DIRT 显示泥土本色。
- `CrucibleRenderer` 用 `renderSingleBlock` 画被熔方块的真实模型（0.70 缩放居中）；
  木坩埚在 `solidVolume > 0` 时始终显示熔料，不再中途切成水盒。
- `COMPOST_TICKS` 1000 → 600（50 秒 → 30 秒），两版同步；JEI 提示同步。

**验证**：1.20.1 dev client 临时截图（脚本已删）确认腐肉红褐 / 树叶绿 / 50-50 黄褐 /
成土原版泥土色，木坩埚（solid 60 / water 50）显示树叶方块；`build` 与资源守卫通过。
主线的同一场景截图见主仓库 `docs/images/1.1.1-compost-crucible.png`。

## 50. 1.1.2 Bug 修复：木钩 / 骨钩耐久（2026-11-04）

**问题**：1.20.1 的 `ToolEvents#onBreak` 里，crook 分支只调用 `CrookDrops` 追加掉落，
没有调用 `hurtAndBreak`；木钩/骨钩打树叶时耐久永远满格。1.21.1 走 `BlockDropsEvent` →
`wearMainHand`，一直正常。

**修复**：crook 分支在非创造模式、且方块确实有钩子表时，对 `player.getMainHandItem()`
调用 `hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(MAIN_HAND))`，与 hammer
分支同款。`BreakEvent` 在方块移除前触发，每次有效破坏只扣一次。

**验证**：服务端 FakePlayer + `gameMode.destroyBlock` 破坏橡树树叶：木钩、骨钩各
`damage=1`（修复前 0）；`build` + `check-textures.py` 通过。

## 51. 筛表补骨粉 / 烈焰粉（2026-11-04，两版）

**为什么**：骨粉卡着瓷土与菌岩催菌，却只有骷髅掉落；烈焰粉只从碎下界岩（熔岩 + 红石）出，
酿造 / 末影之眼 / 愤怒人偶被卡在中后期。按所有者要求补进筛表。

**数值**：`sieve/dust.json` 加 `minecraft:bone_meal` **20%**（经典尘土 1/5）；
`sieve/sand.json` 加 `minecraft:blaze_powder` **3%**；碎下界岩 6% 保留。两版同步，
`merge_table` 保留手写条目。

**验证**：基表 113 → 115（全装 +TiT 139 → 141、+AE2 143 → 145）；
`build`（Java 17 / `--offline`）、`check-textures.py`、主线 `check-data-paths.py --target 1.20.1`
通过；未进游戏手筛。

## 52. Extreme Reactors：yellorium / anglesite / benitoite（2026-11-04）

**为什么**：反应堆燃料棒/控制器吃 `forge:ingots/yellorium`，石墨是烧煤，铁/红石/玻璃都有；
空岛缺的只有 ER 的矿源。cyanite/blutonium/ludicrite 是反应堆与再处理器产物，不做筛源。
anglesite/benitoite 是 2.x 后期晶体（充能器、insanite），一并补上，避免后期断头。

**为什么用 `gems` 门控**：yellorium 没装 ER 时没有用途；`gems` 的“产物由对方提供、无自有兜底、
只在候选模组在场时注册”正好符合。没装 ER 时物品/方块/筛表补丁不出现，基表 115 不变。

**数值**：yellorium **1/8**（common，约 32 砂砾/锭，反应堆持续烧）；anglesite/benitoite **1/64**
（rare，后期晶体）。三档矿链全进，4 碎块 → 矿块 → 熔炉 200 tick。

**实现**：`materials.json` 三条 `gems`；碎料贴图/锤表/筛表补丁由生成器写；
每版 9 条 `<m>_ore_<kind>_smelting_bigreactors.json`（1.20.1 用裸 `conditions` +
`forge:mod_loaded`）。ER 2.0.84 + ZeroCore 2.1.45 放入 `libs/`，`-PtechMods` 加载。

**匠魂（1.20.1）**

- yellorium 碎/粉/砂 45 mB、矿块 180 mB、ER 锭 90 / 块 810；再加锭/块 2 条铸造回
  `forge:ingots/yellorium` / `forge:storage_blocks/yellorium`。用 ER 自带的
  `forge:yellorium` 流体，不新增流体；10 条都带 `tconstruct` + `bigreactors` 条件。
- 这是「gems 不写匠魂熔炼」的例外：不是所有 gems 都能熔，只有 ER 提供流体标签的 yellorium 能。

**矿石识别**：ER 的 `yellorite_ore` 同时是 `forge:ores/yellorite` 与 `forge:ores/uranium`，
Mek/IE/Create 的铀矿处理因此能磨它。我们的 yellorium 矿块原先只在 `forge:ores/yellorium`，机器看不到；
现在矿块（块 + 物品）同时进两个 tag，`yellorium_broken` 进 `forge:raw_materials/yellorium`
（及根 tag），输出对方的铀粉，和 ER 原矿一致。这是「碎料不进 raw_materials」的唯一例外。

**验证**：`[materials] 25`、`[sieve] 154`、`[hammer] 88`；RCON 熔炉三种矿块分别出
`bigreactors:yellorium_ingot` / `anglesite_crystal` / `benitoite_crystal`，`RecipesUsed` 指向
`*_smelting_bigreactors`；手筛/JEI 未目视。

## 53. BambooMod 资源入口（2026-11-04，仅 1.20.1）

**为什么**：BambooMod 0.6.2 只有 1.20.1 Forge；它把原版竹子也放进 `forge:bamboo`，所以普通
竹子配方本来就通，但空岛缺**第一棵**：4 种树苗、竹笋、米种/豆/番茄、3 种宝石都只在世界生成
或村庄箱子里。只补这些起步资源，不写它的机器/食物/村庄配方。

**入口**：`sieve/dirt.json` / `grass_block.json` 各 +8（4 树苗 2%、竹笋 4%、米种 5%、
豆 4%、番茄 4%），`sieve/gravel.json` +3 宝石（银杏/枫/樱各 1/64）。全部
`"mod": "bamboomod"`，没装时在查物品前跳过，基表 115 不变；草方块由泥土生成，自动同款。
宝石没有配方用途（全 data 只在 loot 里出现），作为收藏补 1/64。

**堆肥**：它的树叶/树苗/竹笋已进 `#minecraft:leaves` / `#minecraft:saplings`，
现有 `#exnihilosatellitium:compostable` + `compost/default.json` 的 125 分直接覆盖，
不新增 JSON。

**验证**：BambooMod 0.6.2 真装（`libs/` + `-PtechMods`）：`[sieve] 173`（+19：泥土 8 /
草 8 / 砂砾 3）、`[hammer] 88`；RCON 泥土 32 / 砂砾 31 条含新条目；`/setblock` 树苗与竹笋成功；
未进游戏手筛、树苗生长与 JEI 未测。

## 54. 龙之进化：龙尘 / 龙锭（2026-11-04，两版）

**为什么**：DE（`draconicevolution`，1.20.1 3.1.2.621）的龙矿只在世界生成，空岛拿不到；
它的矿掉 2–4 龙尘，龙尘 1:1 熔成龙锭，且熔炼按 `forge:ores/draconium` 标签取矿——
这正是 datagen 已经会生成的同名标签。

**入口**：`draconium` 是 `gems` 材料（DE 不在场整条不注册），唯一来源
`sieve/crushed_end_stone_draconicevolution.json`（根级 `"mod"` + `patch`）：
**粉碎末地石 1/8 → `draconium_broken`**。分支没有 `generate_sieve_assets.py`，所以这是
手写补丁文件；4 破碎 → 矿砾，三条锤表（`mod` 门）手写补齐。

**终点**：矿块进 `forge:ores/draconium`，直接吃 DE 自己的熔炼配方，出
`draconicevolution:draconium_ingot`；不要在 datagen 之外再写 `*_smelting_draconicevolution.json`。

**不碰终局**：觉醒龙（4 龙块 + 6 龙核 + 龙心）与混沌碎片（混沌守卫）保持 DE 原样。

**贴图**：破碎阶段的石壳必须跟来源方块——龙尘来自粉碎末地石，所以
`STONE_PALETTES["draconium"] = END_STONE`（末地石淡黄），不是默认灰石。

**验证**：真装 DE 3.1.2.621 + Brandons Core 3.2.1.302 + CodeChicken Lib 4.4.0.528
（`libs/` + `-PdeMods`）；全装叠加 `-PtechMods`：`[materials] 26`、`[sieve] 174`、
`[hammer] 91`；RCON 熔炉把 `draconium_ore_dust` 熔成
`draconicevolution:draconium_ingot`；手筛 / JEI 未目视。

## 材质锤：9 金属 + 3 Bamboo 宝石（2026-11-08）

**为什么**：六档基础锤之外，空岛已经注册了 14 种金属，但铜/锡/银/铅/镍/铂/铝/锇/锌
没有对应的手动锤；BambooMod 的樱/枫/银杏宝石也没有消耗口。

**耐久（所有者 2026-11-08 拍板）**：锡/铝/锌/铜 **256**、银 **320**、铅 **384**、
镍 **448**、锇 **512**、铂 **1920**；Bamboo 樱/枫/银杏统一 **2048**。
以 64 为基数，钻石 1561 仍是普通材质上限，宝石锤略高于下界合金。
**当时不做**：钴/阿迪特自有锤——改走匠魂路线；2026-11-09 由匠魂可改造锤承接（见文末）。
铀因放射性不做锤。只加耐久，不改锤表/速度。

**注册**：`ModItems.METAL_HAMMERS` 从 `materials.json` 生成；材料 disabled 时锤子一起消失。
Bamboo 三把只在 1.20.1 分支、`bamboomod` 在场（或 datagen JVM）时注册。

**配方/贴图**：金属锤 head 走 `forge:ingots/<id>`，铜用原版铜锭；宝石锤手写
`forge:mod_loaded(bamboomod)` 配方。贴图由主线 `art/items/tools/hammer_variants.py`
从铁/钻石锤底图换色，金属复用矿链五阶调色板。

**DE 锤（2026-11-08 追加）**：三把 DE 锤使用 3D cube 模型（与主线同源），
`HammerTool.energyPerUse()` 返回 256（DE 配置 `-99` 哨兵回退），手锤 / 自动锤都扣电，
空电停机；模块 host / 能量 capability 走 1.20.1 的 `initCapabilities`。

**验证**：本线 `build` + `check-textures.py` 通过（312 模型 / 189 纹理）；
`runData` 生成 12 个模型与 9 条金属配方；`-PtechMods` 真装 BambooMod 后 RCON
`summon item` 樱/枫/银杏 + 金属锤全部注册；3D 模型 / 能量已在主线实机确认，
本线未单独复验聚合合成 / 模块 GUI / JEI。

## 龙之进化锤：双足飞龙 / 神龙 / 混沌（2026-11-08）

**目标**：三把 DE 主题锤，沿用 DE 聚合装置升级链，并使用 DE 模块。DE 3.1.x 没有锤子物品，
所以这三把是自造的 `ModularPickaxe` 子类。

**升级链**：`diamond_hammer` → `wyvern_hammer`（8M OP）→ `draconic_hammer`（32M）→
`chaotic_hammer`（128M）；材料沿用 DE 镐的龙核/龙锭/中继水晶/能量核心/下界合金/觉醒龙/混沌核心。
配方手写 `forge:mod_loaded(draconicevolution)` 条件。

**模块/能量**：`DEHammerItem extends ModularPickaxe implements HammerTool`，继承 DE 模块网格、
`createHost` / `createOPStorage` 与 1.20.1 的 `initCapabilities`；`ToolEvents` 对 DE 锤跳过
`hurtAndBreak`，走 DE 能量。DE 类放在可选 `src/de/java` source set，只有 `libs/` 三个 DE jar
都在时才编译并打进 jar；核心用反射查找 `DEHammers`。

**验证**：本线 `build` + 纹理守卫通过（312 模型 / 182 纹理）；`-PtechMods -PdeMods` 真装
BambooMod + DE 后 RCON 3 把 DE 锤均注册，聚合配方无解析错误；**未实机聚合**，模块 GUI /
AOE / 能量未目视。

## 匠魂可改造锤：碎矿锤（2026-11-09，仅 1.20.1）

**目标**：不是再造一把自有锤，而是让“钴 / 阿迪特走匠魂路线”真正落地——用匠魂 3 的部件、
材料、特性与强化系统做一把锤，同时保留我们的粉碎表。

**做法**：`ExNihiloHammerItem extends ModifiableItem implements HammerTool`。
- `ModifiableItem` 负责匠魂的一切：材料属性、材料特性、强化槽、耐久、Tool Stack NBT、
  装配站合成、强化台、破损动画。
- `HammerTool` 只桥接我们的判定：1.20.1 的 `ToolEvents#onBreak` 已经接管锤表方块的破坏，
  所以“是不是锤”只取决于这个接口，不需要第二套判定。
- **耐久**：1.20.1 取消 `BlockEvent.BreakEvent` 后，TiC 的 `ToolHarvestLogic` 不会跑，
  因此 `HammerTool#wear` 负责扣耐久；匠魂锤覆写为 `ToolDamageUtil.damageAnimated`，
  让强化 / 粘液覆层 / 耐久倍率 / 破损广播都生效。普通锤走默认的 `hurtAndBreak`。

**工具定义**（`data/exnihilosatellitium/tinkering/tool_definitions/ex_nihilo_hammer.json`）：
- 部件：`hammer_head` + `tough_handle`，**只有两件**——2026-11-11 所有者指出装配站配方和
  匠魂大锤一模一样，要求改成 1 坚韧手柄 + 1 锤头，于是去掉两块 `large_plate`；
  `default_materials` 与装配站布局的输入槽同步减到 2 个。
- 头部不再带 `scale`（原为 0.5）：TiC 的 `MaterialStatsModule` 会把 `scale` 乘进耐久 / 攻击 /
  挖掘速度，0.5 会让铁头只剩 3.0 速度和 500 耐久；去掉后铁头 6.0 速度、1000 耐久，
  与 1.4.0 四件配方持平。`base_stats` / `multiply_stats` 没动（耐久 ×4.0、攻击 1.35 倍）。
- 去掉了大锤的 `mining_speed ×0.4`：本工具没有基础 3×3，单方块速度按头部材料算。
- 1 能力 + 2 升级槽，自带 `tconstruct:smite` 1；`pickaxe_dig` + `shovel_dig`；有效谓词仍是
  `mineable/pickaxe`；不加 `box_aoe`、不登记 `modifiable/aoe`（锤表方块的破坏由我们接管）。

**挖掘速度（2026-11-11）**：砂砾 / 沙子 / 尘土是 `mineable/shovel` 方块，而工具的有效谓词是
`mineable/pickaxe`，TiC 的 `getDestroySpeed` 在这些方块上回退到 1.0，手感就是空手。
`HammerTool#breakSpeed` 现在对锤表方块返回头部材料的 `MINING_SPEED`，`ToolEvents#onBreakSpeed`
取和当前速度的较大值；普通锤与 DE 锤仍是 6.0 的固定下限。

**连锁挖掘（2026-11-11）**：`HammerTool#chainLimit(stack)` 决定一次粉碎最多额外带几块；
`ChainMining.collect` 从被锤方块做六面 BFS，只收同一种、且仍有锤表规则的方块；`ToolEvents`
逐块 `crush`（掉落 / 破坏音效 / 1 点耐久或一次能量），工具损坏或没能量就停。
- **匠魂锤**：新强化 `exnihilosatellitium:chain`（I–III）。`ChainModifier` 只让强化存在，
  行为由 `chainLimit` 读 `ToolStack#getModifiers().getLevel` 决定，每级 16 块；注册走
  `ModifierDeferredRegister`（TiC 的正式第三方入口），配方是 `tconstruct:modifier`
  （4 燧石 + 1 铁锭，占 1 个升级槽），核心反射 `TinkersTools#registerModifiers`。
- **DE 锤**：注册了一个**专属模块** `exnihilosatellitium:chain_mining_module`——DE 自己的
  AOE（范围）模块只处理“镐子正确”的方块，砂砾 / 沙子会被 `isCorrectToolForDrops` 过滤掉，
  所以才单做一个。模块进 DE 的 `draconicevolution:modules` 注册表（DE 用
  `NewRegistryEvent` 建表，我们挂它自己的 `RegisterEvent`），分类 `MINING_TOOL`——DE 给所有
  `IModularMiningTool` 宿主自动加这个分类，所以我们的 DE 锤能装。`ChainData` 记录连锁块数
  （固定 32），`DEHammerItem#chainLimit` 通过 `DECapabilities.MODULE_HOST_CAPABILITY` 读它。
  合成 4 燧石 + 4 铁锭 + 1 个 `module_core`，不跟 DE 后期模块比价。
- **上限**：`MAX_VISITED = 1024`，防止坏数据包把一次点击变成全图扫描。

**构建**：TConstruct 没有官方 1.21.1，所以只有 1.20.1 分支。`src/tinkers/java` 是可选 source set，
和 DE 锤同一模式：`libs/` 里有 TConstruct + Mantle 才编译并打进 jar；核心
`ModItems#tinkersHammers` 反射加载 `TinkersTools`，运行时按 `tconstruct` 门控。
数据文件始终在 jar 里，但合成配方与装配站布局带 `forge:mod_loaded(tconstruct)` 条件；
没有匠魂时工具定义不会被注册，JSON 被忽略。

**模型**：loader 是 `exnihilosatellitium:tool`（我们注册，始终存在）。装匠魂时它委托 TiC 的
`tconstruct:tool`，用 `head` / `handle` 两张 16×16 灰度部件图，按每个部件的实际材料染色；
没装匠魂时回退到 `forge:item_layers`，所以孤儿模型不会刷 unknown loader。`TinkersClient`
只在“客户端 + 匠魂”时注册 `ToolModel.registerItemColors`，并用反射从核心调用——如果给它加
`@EventBusSubscriber`，没有匠魂的客户端会因为类里的 TiC 引用直接崩在 `common_setup`。
裸物品模型另补 `*_tconstruct_unknown.png` 别名，避免 `Missing textures`。

**强化外观**：`modifier_maps` 指向我们的 sprite map（`tinkering/modifiers/sprites/`），第一批
16 个常用强化各有 16×16 overlay；位置按锤头而不是大锤布局画，所以不会出现大锤的错位图案。
匠魂的染色 / 粘液覆层 / 流体储罐有各自专用模型，工具侧暂不接；要补新强化只需 map 一条 +
一张 overlay，不需要改代码。

**兼容**：自动锤机器接受它（`HammerTool`）；`consumeToolUse` 改为对非能量锤统一
`damageTool()`，否则匠魂锤会在机器里白嫖。机器只扣原始耐久，不触发 TiC 的
`damageAnimated`（无玩家上下文），这是有意的取舍。

**验证**：`-PtinkersMods` 启动 → 46 tool definitions / 24 station layouts、`tconstruct:modifier`
配方加载无错、`exnihilosatellitium:chain` 静态强化注册成功；临时 FakePlayer 探针
（铁头 + 木柄，两件配方）得到 **6.0 速度 / 1000 耐久**；连锁 0 级 1 块 / 1 耐久、
I 级 17 块 / 17 耐久、II 级 33 块 / 33 耐久，中间夹一块沙子会停在沙子前。
DE 侧用临时探针验证：模块在 DE 注册表、物品与配方加载、DE 锤宿主接受该模块，装上后
`chainLimit` 0 → 32；实机装模块的画面未看。客户端已开：装配站造出木锤（240 耐久 / 5.05 攻击），tooltip 的 `培植 IV` + `亡灵杀手 I` +
我们的说明行都正常，强化页可装自动烧炼；创造栏里不同材料的锤头/手柄颜色明显不同
（木、铁、钴、玛玉灵等）；无匠魂客户端也能正常启动（模型走 fallback，不报 unknown loader）。
强化 overlay 已接入 16 个（`smite` 默认就在，创造栏锤头应有淡黄十字），但未逐个强化目视。
JEI 工具页 / 左键手感未单独验。
