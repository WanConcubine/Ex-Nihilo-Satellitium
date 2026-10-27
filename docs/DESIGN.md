# 设计文档：无中生有：东施效颦

> 状态：M0（骨架）／最后更新：2026-10-27

## 1. Clean-room 原则

本项目是净室重写，以下规则贯穿始终：

1. **不阅读、不复制**任何现有 Ex Nihilo 分支（原版、Adscensio、Omnia、Creatio、
   Sequentia、Ex Deorum、Fabrico、Fabricae）的源代码。
2. 需求只来自**公开的玩法描述**：Wiki、MC百科词条、视频、玩家讨论、配方截图。
3. 所有代码、贴图、模型、音效、文本自行创作；不使用上游的任何资源文件。
4. 不冒充官方：项目名带明确副标题，`mods.toml` 与 README 均声明非官方。
5. 玩法机制（规则、数值、流程）不受版权保护，可以作为设计参考；受保护的是
   具体表达（代码、美术、文本、名称标识）。

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
| M1 | 木桶：堆肥、流体存储、方块转化 | 木桶能堆肥成泥土，能装水/熔岩 |
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
- ARR 是否长期保留，还是稳定后转 MIT？
