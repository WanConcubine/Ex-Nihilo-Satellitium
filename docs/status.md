# 当前状态（交接卡）

> 最后更新：2026-11-01
> 先看这 20 行，细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**M1 木桶 + 七种材质变体已完成**；**M1.5 桶内方块转化已铺好**（2026-11-01：水+熔岩→圆石、
熔岩+水→黑曜石、熔岩+红石→下界岩、熔岩+荧石→末地石，木桶限温 + 接雨水）；
**M2 筛子 + 矿链已铺好**（三种筛网、4 张筛料表、12 种材料 4 碎块→方块→锭）；
**M3 锤/钩 + 尘土方块已铺好**（6 锤 / 2 钩、47 条锤表、10 张钩表、沙→尘土）；
**JEI 五个分类页 + Jade 筛子 tooltip 已补**（2026-11-01）。**M4 坩埚仍未开始**。

## 现在停在哪

- 工作区：`/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium-1.20.1`（`1.20.1-forge` 分支的
  git worktree，主工作区仍在 `main`）。
- **2026-11-01 M1 资产与主线同步**（`d138cf9`）：矿物方块改成主线那套**单层染色** cube
  （去掉双层遮罩 / `ore_mask_*`）、铁碎块换**暖铁色**、木桶去掉两道外凸箍与侧面
  `rotation: 90`；`scripts/generate_ore_variants.py` 跟着改，重跑不会变回去。
- **2026-11-01 M2 铺到分支**：
  - 筛子：`SieveBlock` + `SieveBlockEntity` + `SieveRenderer`（1.20.1 的 `use` / NBT /
    `renderSingleBlock` 签名），`MESH` 方块状态、进度条式做工、破坏返还、右键装网/下料；
  - 三种筛网：木 10 / 丝 64 / 铁 256 耐久，只在创意栏与合成里出现；
  - 数据：`sieve/{dirt,sand,gravel}.json`（3 输入 / 48 掉落），`forge:ores`、
    `forge:ingots` 标签，`recipes/` 下 83 条配方（7 桶 + 36 碎块→方块 + 36 熔炼 + 筛子/网）；
  - 矿链：12 种材料 × 4 碎块 → 矿方块 → 熔炼成锭；9 种没有现成锭的材料（tin/silver/lead/
    nickel/platinum/aluminum/osmium/zinc/uranium）注册自有 `<m>_ingot`，铜改成原版铜锭。
  - **没有做**：`dust` 筛料表（需要 M3 的尘土方块）、JEI（这条分支还没引入 JEI 依赖）。
- **2026-11-01 JEI + Jade**：JEI 15.59 的 compileOnly API（`maven.blamejared.com`）+
  `-PdevMods` 才加载完整 JEI；移植五个分类页（筛子 / 桶转化 / 堆肥 / 锤 / 钩），
  数据和 block entity 共用同一张表。Jade 补 `SieveComponentProvider`（筛网名称+耐久、
  筛选进度）和 `config.jade.plugin_exnihilosatellitium.sieve` 翻译（缺了 Jade 会崩客户端）。
  冒烟测试（`--quickPlaySingleplayer` 进存档）：`[jei] registered 35 hammer / 4 sieve /
  4 barrel + 65 compost / 10 crook`，无异常。
- **2026-11-01 M3 锤/钩 + 尘土**：`HammerItem` / `CrookItem` / `HammerRecipes` /
  `CrookDrops` + 两个 reload listener、`HammerRolls` 共用判定；6 锤（100 木/石/铁/金/钻/下界合金，
  耐久 64/128/256/32/1561/2031）+ 2 钩（木 64 / 骨 128，骨钩概率 ×1.5）；47 条锤表（跳过
  M6 的 `crushed_*` 两条）、10 张钩表（去掉 M6 蚕掉落）；沙 → 尘土、尘土方块可筛
  （第 4 张筛料表现在启用：4 输入 / 62 掉落）。**1.20.1 没有 `BlockDropsEvent`**：锤子走
  `BlockEvent.BreakEvent` 自己接管破坏（取消事件 + 手动移除 + 掉锤表产物 + 扣耐久），
  钩子保留原版树叶掉落、只追加钩表产物。
- **2026-11-01 M1.5 桶内转化**：`BarrelRecipes` + `BarrelRecipeReloadListener`
  （`barrel/*.json`，4 条规则：2 流体 + 2 物品）、木桶 433 K 限温、石桶不限、
  满桶上方倒另一种流体自动固化、右键取出固体、接雨水、比较器/破坏返还；
  渲染补了桶内固体（用方块粒子图标画一个立方体）。`water_dust_clay` 需要 M3 的
  `dust` 方块，先没放。
- 开发服：`run/server.properties` 现在是测试用（`level-name=m2-test`、RCON `eni`、
  `online-mode=false`）；要和主线一样正式联调时记得改回 `true`。
- **开发时加载生产 mod（2026-11-01）**：1.20.1 的 dev 运行时用 official 名，生产 jar 里是
  SRG 名，**直接丢 `run/mods` 会 `NoSuchMethodError` / mixin refmap 失效**。现在的做法：
  jar 放 `libs/`（gitignored）→ `build.gradle` 里 `flatDir` + `fg.deobf(...)` 挂成
  `compileOnly`/`runtimeOnly`，并给 run config 加
  `mixin.env.remapRefMap=true` + `mixin.env.refMapRemappingFile=build/createSrgToMcp/output.srg`。
  依赖块用 `file(...).exists()` 守卫，没放 jar 的干净 clone 仍能 `build`。
  当前接的是 **JEI 15.59.0.212 / Jade 11.13.2 / Just Enough Characters 4.6.11**。

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-28：`--offline` 下 `BUILD SUCCESSFUL in 8s`；直连 TLS 失败时用缓存 |
| `python3 scripts/check-textures.py` | ✅ | 七个子模型只引用 vanilla **block** 贴图 |
| 服务端启动 | ✅ | 无 Jade 也能启动；`Done (3.5s)` |
| 七种桶 BE | ✅ | 改名后 RCON：`exnihilosatellitium:barrel` 堆肥→DIRT、`DirtReady=1b`；七种材质共享同一 BE 注册表 |
| 堆肥容量/刻度 | ✅ | `data get` 读回 `Compost=1000`、`CompostScale=1000`；满后 1000 tick → DIRT |
| 堆肥数值表 | ✅ | `/reload` 日志 `[compost] loaded 65 entries`，无 unknown item / invalid tag |
| 石桶流体 NBT | ✅ | RCON 写 `FluidName`/`Amount`，读回 `minecraft:lava` / `1000` |
| 掉落表 | ✅ | `/loot spawn ... exnihilosatellitium:blocks/{spruce,stone}_barrel` 返回对应物品 |
| 资源重载 | ✅ | `/reload` 后 `Loaded 7 recipes`（正好七种桶配方）、`[compost] loaded 65 entries` |
| 客户端资源加载 | ✅ | 改名后客户端到主菜单；`Reloading ResourceManager` 无 missing model / missing texture |
| 开发测试 mod | ✅ | JEI 15.59.0.212 / Jade 11.13.2 / JECh 4.6.11 按 deobf 方式加载，客户端到主菜单不崩 |
| 实际进游戏目视 | ❌ | 只跑了客户端资源加载；没有截图验收比例、箍位置、物品栏图标 |
| 右键取泥土 | ⚠️ | 逻辑未改，仍未自动化（需要真实玩家） |
| 逐项右键堆肥 | ⚠️ | JSON/标签已对齐，但没有玩家逐项右键验证点数 |
| 比较器 | ⚠️ | 逻辑未改，仍未实测 |
| Jade tooltip | ⚠️ | 编译通过、无 Jade 启动通过；**未装 Jade 实测** |
| 合成配方 | ⚠️ | 7 个 JSON 都加载成功；没有玩家实测合成格 |
| 矿物六态注册 | ✅ | 客户端 `/give` 与创意标签可见 72 条；`[materials] loaded 12 entries` |
| 矿物六态纹理 | ✅ | 作者目视通过：原版底 + 遮罩 + 罩色，物品栏为 3D 方块 |
| 矿物链配方 | ✅ | 83 条 recipe JSON 在 mod 数据包里；36 条 4 碎块→方块、36 条方块→锭（RCON 验证锭物品存在） |
| M2 筛子方块/ BE | ✅ | RCON：`setblock ... sieve`、`[mesh=0/3]` 状态命中、`data merge` 的 `Mesh`/`Contents` 读回 |
| M2 筛料表 | ✅ | `/reload` 日志 `[sieve] loaded 3 inputs, 48 drops`；`dust` 表按计划未装 |
| M2 筛子玩法（右键/掉落/耐久） | ⚠️ | 需要玩家右键；客户端目视待作者验收 |
| M1.5 转化表加载 | ✅ | `[barrel] loaded 4 transforms (2 fluid, 2 item)` |
| M1.5 固化 | ✅ | RCON：木桶满水 + 上方熔岩 → `Solid=cobblestone`、水清空；石桶满熔岩 + 上方水 → `Solid=obsidian` |
| M1.5 限温/取固体/雨水 | ⚠️ | 逻辑已写；限温提示、右键取固体、雨水需要玩家实测 |
| M3 表加载 | ✅ | `[hammer] loaded 47 recipes`、`[crook] loaded 10 leaves tables` |
| M3 物品/方块 | ✅ | RCON：Wooden Hammer / Netherite Hammer / Bone Crook / Dust 均能 summon，`dust` 方块可放置 |
| M3 锤/钩手感 | ⚠️ | 左键敲方块、耐久、创造模式、骨钩 ×1.5 需要玩家实测 |
| JEI 分类页 | ✅ | `[jei] registered 35 hammer / 4 sieve / 4 barrel + 65 compost / 10 crook`（进存档冒烟） |
| Jade 筛子 | ✅ | `SieveComponentProvider` 已注册 + `config.jade.plugin_...sieve` 翻译（缺 key 会崩） |
| JEI 画面 | ⚠️ | 分类页/布局未目视（冒烟只验证了注册与不崩） |
| 筛子产物轮播 | ✅ | 超过 21 条时按槽位轮播（JEI 自动切换多物品槽），tooltip 跟随当前显示项；沙筛 23 条不再截断 |
| M2 第 4 张筛料表 | ✅ | `[sieve] loaded 4 inputs, 62 drops`（尘土已可筛） |
| 27/28 资源模型 | ✅ | 客户端资源加载无 missing model/texture（筛子 frame/mesh、9 种锭、矿方块） |
| 1.20.1 配方计数 | ℹ️ | 1.20.1 的 `Loaded 7 recipes` 是**配方类型数**，不是配方条数；别拿它当进度 |
| 改名完整性 | ✅ | `git grep` 无旧 id/旧类名；jar 内 `mods.toml` 为 `exnihilosatellitium` / `Ex Nihilo: Satellitium` |

## 下一步（按价值排序）

1. **进游戏验收 M1.5 / M2 / M3 + JEI/Jade**：桶转化、筛四档料、锤/钩左键、JEI 五个分类页。
2. **M4 坩埚**：瓷土 → 未烧制坩埚 → 烧制；19 条熔点 / 9 热源、熔料 + 流体双储。
3. **M6**：蚕 / 人偶 / 巫水 / 瓷桶。

## 已拍板的决策

- **项目名**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`，
  Java 包 `com.kaifengcrt.exnihilosatellitium`，资源命名空间 `exnihilosatellitium`（2026-10-28）。
- **许可证 MIT**（2026-10-27 从 ARR 转）；Forge MDK 仍 LGPL-2.1，见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**（2026-10-29）：自动发现保留，但注册只认材料表。表内材料由我们
  手写颜色 / `tier` / `rarity`，并在游戏里拥有完整自有链（矿砾 → 破碎 → 粉碎 → 粉末 → 锭）；
  表外材料只生成骨架行、默认关闭，作者填值并重启后开启。不做“扫到即生效”。
  详见 [DESIGN.md](DESIGN.md) §6.1 第 4 条、§6.4；表结构见 [data-format.md](data-format.md) §8。
- **方块走 vanilla-sampled**：模型直接引用 `minecraft:block/*`，不复制/不打包原版贴图。
  七种桶共用 `barrel_base.json` 几何；木桶侧重木种木板 + 去皮原木内壁 + `gray_concrete` 深灰箍，
  石桶用 `stone` + `smooth_stone` + `deepslate_tiles`。
- **物品与特殊表面自有像素**：瓷土、堆肥面；不把 AI 生图放进 release。
- **原版 Ex Nihilo 与 Omnia 都是 GPLv3**：只对照玩法数值/行为，不复制代码、JSON、贴图、模型、
  音效；`reference/` 和本地 Omnia jar 只读、gitignore、不随包分发。复制 GPL 资源就必须改 GPLv3。
- **木桶 14×16×14 内缩**；物品模型用开口 block model + `display` 变换（没有封闭盖）。
- **七种桶共享一个 BE 类型**；行为/NBT/Jade 完全一致，石桶暂不做差异化。
- **堆肥用 1000 分制**：以 **Omnia 1.10.2** 的表为骨架，原版 1.7.10 校验比例；
  Sequentia 只借 1000 刻度，不采用它的重平衡。树苗/树叶 125、面包 125、花/蘑菇 100、
  腐肉/蜘蛛眼/小麦 85、肉/南瓜派 175、鱼 120、瓜块 150、胡萝卜等 80。
  满 1000 后 1000 tick（50 秒）变 DIRT。老存档按 `CompostScale` 缺失自动 ×10 迁移。
- **Jade 是可选依赖**：`compileOnly` API，不打包；没装时插件类不加载。
- **月光魔法是可选软依赖**：`moonlight_magic`，目前只占位，没有实际联动内容。

## 踩过的坑（改代码前先看）

- **ForgeGradle 卡 `Checking server connections`**：加 `-Dnet.minecraftforge.gradle.check.certs=false`。
- **直连 TLS 失败（libraries.minecraft.net / repo.maven TLS handshake）**：缓存已齐时用 `--offline` 构建/启动。
- **`FluidTank` NBT 是顶层** `FluidName` / `Amount`，不是 `{Fluid:{...}}`。
- **`#minecraft:crops` / `#minecraft:seeds` 不是 1.20.1 物品标签**；用 `#forge:*` 或具体 id。
  `minecraft:carrots/potatoes` 是方块 id，物品是 `carrot` / `potato`。
- **内缩模型的外侧面不能加 `cullface`**：墙在 z=1/x=1，不在方块边界，加了会整面被剔除。
- **堆肥标签和 JSON 必须同步**：服务端也先查 `#exnihilosatellitium:compostable`，JSON 里有但标签没有 = 不能堆肥。
- **堆肥刻度从 100 改成 1000**：老存档没有 `CompostScale`，`load()` 会 ×10 并缩放计时；新存档写 `CompostScale:1000`。
- **共享 BE 类型要列全七种方块**：`BlockEntityType.Builder.of` 漏一个，放置后 BE 不创建 / ticker 为 null。
- **旧配方 `#minecraft:planks` 必须删掉**：它会和具体木种配方同时匹配，产物不可预测。
- **子模型不要直接当方块模型用**：`barrel_base.json` 没有 `textures`，只作为 parent。
- **Jade / 月光魔法生产 jar 不能直接丢 `run/mods` 做 dev 测试**：SRG 名会 `NoSuchFieldError`；
  联调要正式服务端/客户端，或用 deobf 依赖。本分支已改成 `libs/` + `fg.deobf` +
  `mixin.env.remapRefMap`（见「现在停在哪」），新加 mod 照这个套路放。
- **Jade provider 缺 `config.jade.plugin_<ns>.<uid>` 翻译会直接崩客户端**：jade 11 在
  `JadeClient.onGui` 里 `AssertionError`（`Missing config translation`），不是可选提示。
  分支的木桶插件 uid 是 `barrel`，`config.jade.plugin_exnihilosatellitium.barrel` 已补。

## 常用命令

```bash
./gradlew build -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
python3 scripts/generate_barrel_variants.py          # 从材质表重生成七种桶的 JSON
./gradlew runServer -Dnet.minecraftforge.gradle.check.certs=false   # RCON：localhost:25575 / eni
# 注意：run/ 被 gitignore；fresh clone 需要在 run/server.properties 自己开 RCON
./gradlew runClient -Dnet.minecraftforge.gradle.check.certs=false
```

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java           # 入口
├── registry/                       # Block / Item / BlockEntity / CreativeTab
├── common/
│   ├── block/BarrelBlock.java
│   ├── blockentity/BarrelBlockEntity.java
│   ├── compost/                    # CompostRegistry + reload listener
│   ├── event/CommonEvents.java
│   └── tag/ModTags.java
├── client/                         # BarrelRenderer + ClientEvents
└── compat/
    ├── MoonlightCompat.java
    └── jade/                       # 可选 HUD
```

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑”，data-format 写“JSON/NBT/命令”，textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录。
- 不要在本文件抄大段设计；链接过去。
