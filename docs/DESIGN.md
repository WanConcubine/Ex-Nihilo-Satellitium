# 设计文档：无中生有：东施效颦

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

**目标**

- Minecraft 1.20.1 / Forge 47.x / Java 17。
- 还原“空岛从无到有”的核心循环，并补齐 Ex Nihilo 系缺失的现代体验（数据驱动、JEI、配置）。
- 对整合包作者友好：配方、筛网掉落、堆肥、熔炼、热源全部可数据包覆盖。
- 与「月光魔法」软联动。

**非目标**

- 1:1 复刻某个具体分支的数值与贴图。
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
com.kaifengcrt.exnihiloimitatio
├── ExNihiloImitatio.java        # 入口
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

## 6. 与「月光魔法」的联动

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



## 7. 待定问题

- 模组英文名最终用 `Ex Nihilo: Imitatio` 还是 `Imitatio: Out of Nothing`？
- 是否需要 Skyblock 世界类型（Ex Deorum 有，工作量不小）？
- 是否提供 1.20.1 之外的版本分支？
- ~~ARR 是否长期保留~~ → **已转 MIT（2026-10-27）**，代码与自绘贴图均按 MIT 发布。

## 8. M1 实现记录（2026-10-27，含内缩模型 + MIT）

**已完成**

- `BarrelBlock` + `BarrelBlockEntity`：1000 mB `FluidTank`、`IFluidHandler` capability、NBT 持久化、客户端同步。
- 堆肥：`#exnihiloimitatio:compostable` 物品标签负责客户端交互预测；
  `data/<namespace>/compost/*.json` 提供数值（默认 30 条）；满 100 后 600 tick 进入 DIRT 状态，
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
  - `setblock ... exnihiloimitatio:barrel` + `data merge {Compost:100,CompostTicks:1}`
    → 2 秒后**仍是 barrel**，`DirtReady=1`、`Compost=100`（`execute if block` 返回 Test passed）。
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
  - `堆肥：x / 100`
  - `还需 n 秒`
  - `泥土已就绪（右键取出）`
  - 流体：`水：x / 1000 mB`
- 没装 Jade 时：`@WailaPlugin` 类不会被 Jade 扫描/加载，模组独立运行。
- 验证：编译通过；无 Jade 的服务端启动正常；jar 内只有我们的插件类，没有 Jade 类。
- 未验证：真实 Jade 客户端里的 tooltip 渲染（需要装 Jade 进游戏看）。

## 10. M1 材质变体（2026-10-28）

**决策**

- 沿用 **vanilla-sampled**，不把 `art/barrel/*` 的早期自绘图塞进 release。
  木桶外观由原版木板/去皮原木/石砖组合；这样所有材质共用一套几何，也不复制 GPLv3 PNG。
- 几何与 `display` 抽到 `models/block/barrel_base.json`；每种材质是只覆盖 `textures`
  的**子模型**，避免七份 560 行 JSON 漂移。
- **七种材质**：橡木（保留 `barrel` id）、云杉、白桦、丛林、金合欢、深色橡木、石头。
  1.20 新增的 mangrove / cherry / bamboo 暂不做，避免一次把变体表铺得太开。
- 七种桶共享 **一个 `BlockEntityType` + 一个 `BarrelBlock` 类**；行为、NBT、比较器、
  Jade tooltip 完全一致。石桶复制 `Blocks.STONE` 属性（镐、石音效），木桶复制对应木板属性。
- 配方从“任意木板 → 橡木桶”改为**每种木材一个具体配方**；否则 `#minecraft:planks`
  会和具体木种配方同时匹配，产物不可预测。石桶用 `stone_bricks`。
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
