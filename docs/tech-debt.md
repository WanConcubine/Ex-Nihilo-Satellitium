# 技术债清单（2026-10-30 审计，2026-10-31 更新）

> 范围：`src/main/java`、`src/main/resources`、`scripts/`、`build.gradle`、CI、文档与元数据。
> 方法：通读代码 + 数据/资源一致性脚本 + 构建/服务端日志对照。本文件同时记录审计与修复状态（见下方修复记录）。
> 状态：`P0` 会直接坏功能/坏数据；`P1` 高概率/结构性风险；`P2` 工具链与文档；`P3` 清理与路线图。

## 修复记录（2026-10-30）

**已修**：
- P0 #1 桶生成器（1.21 路径、`result.id`、实际配方、mineable 标签合并）；
- P0 #2 玩家破坏返还（筛网/筛内材料/`Solid`/泥土；爆炸仍会丢）；
- P0 #3 `fill` 状态互斥（DIRT/`Solid`/堆肥中拒绝灌流体）；
- P0 #4 `grass`→`short_grass`，53+12 标签对齐；Sieve/Compost/Moonlight 用 `getOptional`；
- P0 #5 文档改为“只有 jar 内表”，不再承诺 config/自动发现/骨架行；
- P0 #6 材料表逐条容错；P0 #7 创造标签图标固定用桶；
- P1 #8 服务端 gameplay 表与客户端 JEI 快照分离（2026-10-31 七个表全部完成；JEI 优先服务端表，
  专用服客户端回退 `assets/` 镜像）；
- P1 #9–#11、#14、#18、#19、#22、#23（`enabled` 过滤）已修；
- P2 #25 加了纹理检查步骤（仍无客户端/JEI/数据测试）；#29 `mod_description`；#30 关键过期文档；
  #32 删了 `OreDebugScreen`/`OreDebugScreenshot` 与 `OreColors` debug 分支。

**未修/部分**：P1 #12 只加了截断警告；#15/#16 性能；#17 `Fluid` 精确匹配；#20 `rarity` 对象仍未实现
（只警告并读 gravel）；#21 `ingot` 存在性；#23 旧文件清理；#24 生成器陈旧文件；#25 客户端/JEI/数据测试；
#30 历史 1.20.1 记录仍保留；#32 `CommonEvents` 的 `[debug]` 日志；P2 #26–#28/#31/#33–#36；P3 全部。

## P0：确认会坏功能或坏数据

| # | 问题 | 证据 | 影响 | 建议 |
|---|---|---|---|---|
| 1 | ~~**桶生成器仍在写 1.20.1 路径**：`recipes/`、`loot_tables/blocks/`、`tags/blocks/mineable/`，配方结果还是 `result.item`~~（已修） | `scripts/generate_barrel_variants.py:85,100,125`；对照 `docs/data-format.md:183-185` | 有人按 README/status/textures 的说明跑它，新桶的配方/掉落/工具标签不会加载（1.21 只认单数路径） | 改成 `recipe/`、`loot_table/blocks/`、`tags/block/mineable/`、`result.id`；跑一遍并 diff |
| 2 | ~~**筛子/桶被破坏时内容物直接消失**~~（玩家破坏已修；爆炸仍丢） | `src/main/resources/data/exnihilosatellitium/loot_table/blocks/sieve.json`、`barrel.json`；`SieveBlock`/`BarrelBlock` 无对应钩子 | 筛网（含耐久）、筛内材料；桶内 `Solid`/DIRT/流体在挖掉时全丢，玩家会认为是吞物品 | 给 BE/Block 加 `preRemoveSideEffects` 或把内容写进掉落；至少筛网+筛内物品必须返还 |
| 3 | ~~**流体 capability 绕过堆肥/固体状态**：`FluidTank.fill` 只查温度和凝固，没查 `dirtReady`/`hasSolid`/`compostProgress`~~（已修） | `BarrelBlockEntity.java:71-93`；`setSolid` 里会清掉 `dirtReady`（`:269-276`） | 管道/漏斗给“泥土已就绪”的桶灌流体，会把 DIRT 状态覆盖成 `Solid`；堆肥中途也能被灌入流体，出现“半堆肥半流体” | `fill` 开头拒绝 `dirtReady`/`hasSolid`/`compostProgress > 0`；或在灌流体时明确清空并提示 |
| 4 | ~~**堆肥数据里 `minecraft:grass` 不存在**~~（已修）（1.21.1 改为 `short_grass`），且 `CompostRegistry` 用 defaulted registry，`get()` 返回 AIR 而不是 null | `data/.../compost/default.json:39`；`data/.../tags/item/compostable.json:40`；`CompostRegistry.java:80` | 该条变成空物品：JEI 会出现一个空输入条目；`short_grass` 因为标签有、JSON 没有，只能按 1 点堆肥 | JSON 改为 `minecraft:short_grass: 80`；把 `get()` 换成 `getOptional()`；同时把 `#c:strings` 与标签里的 `minecraft:string` 统一 |
| 5 | ~~**材料表的“扩展表/自动发现/骨架行”只写在文档里**~~（已按文档对齐，未实现）：代码只读 jar 内 `materials.json` | `OreMaterials.java:29,44`；`docs/data-format.md:219-221,268-270`；`docs/DESIGN.md:223-227` | 整合包作者按文档放 `config/exnihilosatellitium/materials.json` 不会生效；文档承诺的三层来源是假的 | 要么实现 config 读取与合并，要么把文档改成“当前只有 jar 内一份，重启生效” |
| 6 | ~~**`OreMaterials` 一条坏数据会中断整个循环**~~（已修）：`try` 包住整个 `for`，不是逐条捕获 | `OreMaterials.java:43-84` | 缺 `name`/`tier`/`rarity.gravel` 的条目会让它后面的材料全部不注册；文档说“校验失败只跳过该条” | 把单条解析放进独立 try，缺字段给默认/报错后 `continue` |
| 7 | ~~**材料表为空时创造标签直接崩溃**~~（已修）：图标取 `byMaterial().values().iterator().next()` | `ModCreativeTabs.java:25` | `materials.json` 缺失/全坏/全 `enabled:false` 时，创建创造标签抛 `NoSuchElementException`，启动失败 | 图标用固定桶（`ModItems.BARREL`）或做空表回退 |

## P1：高概率或结构性风险

| # | 问题 | 证据 | 影响 | 建议 |
|---|---|---|---|---|
| 8 | ~~**客户端 reload 与服务端共用同一张静态表**~~（已修） | `SieveRegistry` / `BarrelRecipes` / `CompostRegistry` / `HammerRecipes` / `CrookDrops` / `CrucibleRecipes` / `CrucibleHeat` 都有 server/client 两份；`ClientEvents` 只写 client 副本 | 已消除 F3+T 覆盖服务端规则的问题 | 保持 `clientView` 分离；JEI 读服务端表（为空时回退 client） |
| 9 | **defaulted registry 让“未知 id”检查失效**：`BuiltInRegistries.ITEM.get` 对未知 id 返回 AIR | `SieveRegistry.java:58,68`；`CompostRegistry.java:80`；`MoonlightCompat.java:52` | 未知筛输入/掉落、未知堆肥物品、月光物品都会静默变成空气；日志里的 `unknown ...` 永远不打；坏数据被隐藏 | 统一改用 `getOptional()`；对 `air` 结果额外拒绝 |
| 10 | **筛网耐久可以重置**：安装筛网时 `new ItemStack(stack.getItem())` 新建满耐久栈 | `SieveBlock.java:103`；`SieveBlockEntity.java:54-59` | 玩家把用过的筛网取回再放回，耐久回满，等于无限筛网 | 用 `stack.copyWithCount(1)` 安装，保留 damage |
| 11 | **筛网可堆叠**：`Item.Properties().durability(...)` 没配 `stacksTo(1)` | `ModItems.java:45-50` | 一叠 64 个筛网，安装/取回语义混乱；JEI/创造标签也显示成叠 | 三档筛网都加 `.stacksTo(1)` |
| 12 | **JEI 筛子页最多显示 21 个掉落**，超出静默截断 | `SieveRecipeCategory.java:63-67` | 现在每个输入都 <21，但以后加尘土/新表，多出的掉落不会显示，玩家以为没有 | 超限时加第二页/滚动，至少打 `[jei]` 警告 |
| 13 | **筛子渲染只支持 BlockItem 输入** | `SieveRenderer.java:30-32` | 数据表允许任意物品输入（例如以后用物品形态的尘土），但筛内看不到料层 | 非方块输入给一个默认平面/图标兜底，或文档明确只支持方块 |
| 14 | **筛子每次 sift 前不检查当前表是否还有该输入** | `SieveBlockEntity.java:147-164` | `/reload`/数据包换掉筛表后，已经放进去的材料会“空转”消耗掉且只扣筛网耐久 | 结算时若 `drops.isEmpty()`，把 contents 退回给玩家或保留不消耗 |
| 15 | **筛子每 2 work tick 同步一次**（25 次/秒/台） | `SieveBlockEntity.java:114-117` | 大量筛子时 `sendBlockUpdated` + 邻居更新开销明显；这是为平滑渲染付出的网络成本 | 评估 4–5 tick 同步或用更轻的客户端进度包；先做大数量基准 |
| 16 | **桶每 tick 做降雨/上方流体检查** | `BarrelBlockEntity.java:316-374` | 大量桶/雨天时 `isRainingAt` + biome 查询每 tick 跑；当前无基准 | 用 `level.getGameTime() % N` 降低检查频率，或只在有玩家附近时跑 |
| 17 | **桶转化按 `Fluid ==` 精确匹配** | `BarrelRecipes.java:57-78` | 只支持 still 水/熔岩；管道/模组流体若是 flowing 变体不会匹配；不同组件也被忽略 | 改用 `FluidStack.isSameFluid`/tag 匹配，并在文档写清 still-only |
| 18 | **转化表重复规则无检测**：同一 `base+added` 多条时先加载者赢 | `BarrelRecipes.java:80-143` | 数据包/内置表重复时行为取决于文件顺序，难排查 | 加载时用 `Set` 检测重复并打 `[barrel]` 警告 |
| 19 | **筛表重复输入无检测**：`built.put(input, ...)` 后者覆盖前者 | `SieveRegistry.java:76` | 两个数据包都写 `gravel.json` 时，前者静默失效 | 检测重复 key 并警告 |
| 20 | **`OreMaterials` 不校验 `tier`，`rarity` 对象只读 `gravel`** | `OreMaterials.java:54-57`；`docs/data-format.md:250-264` | 文档说 tier 与 rarity 区间联动校验，实际任何 tier 字符串都能过；写 `{"gravel":..,"sand":..,"dust":..}` 的沙/尘值被忽略 | 校验 tier 枚举 + rarity 区间；`OreMaterial` 改成三个 rarity 或明确只支持单值 |
| 21 | **`ingot` 只做 id 语法校验，不查物品是否存在** | `OreMaterials.java:98-115` | 指向缺失模组的锭时，不注册自有锭，熔炼配方结果为空/未知物品，链条断掉 | 在 registry 可用后校验；缺失则回退自有锭并打警告 |
| 22 | **`generate_sieve_assets.py` 把 `rarity` 当数字** | `scripts/generate_sieve_assets.py:129-130` | 一旦按文档写 rarity 对象，脚本直接 `1.0 / dict` 崩溃 | 与材料表 schema 同步，或先只允许数字并在脚本里报错 |
| 23 | **生成脚本不跳过 `enabled:false`** | `scripts/generate_ore_variants.py:146`；`scripts/generate_sieve_assets.py:128` | 被禁用的材料仍生成模型/配方/筛出物，但 Java 不注册物品 → 配方指向缺失物品 | 两个脚本都 `if not m.get('enabled', True): continue`；同时清理旧文件 |
| 24 | **生成脚本不清理被删除材料的旧文件** | `scripts/generate_ore_variants.py` 只清“切换锭”的三类文件 | 删材料/改 id 后，旧 blockstate/model/loot/recipe 留在仓库，产生孤儿资源/未知配方 | 维护一个 manifest，或生成前按当前材料集合删除旧产物 |

## P2：工具链、CI 与文档

| # | 问题 | 证据 | 影响 | 建议 |
|---|---|---|---|---|
| 25 | **CI 不跑纹理检查、不跑客户端/JEI、不跑任何测试** | `.github/workflows/neo-spike.yml:25-51` | 最容易坏的渲染/JEI/资源问题全部漏检；CI 只证明“能编译 + 服务端能启动” | 加 `check-textures.py`；至少跑一次 `runClient` 到主菜单/进世界的日志断言；加数据 JSON 校验 |
| 26 | **CI 有 `contents: write`，失败时强推 `ci-logs` 分支** | `neo-spike.yml:8-9,53-63` | 工作流失败会改远端分支；权限过大；`ci-logs` 会不断堆积 | 日志用 artifact 上传；权限收窄到 `contents: read` |
| 27 | **CI 硬编码 `[materials] loaded 12 entries`** | `neo-spike.yml:50-51` | 加一个材料就红；测试与数据耦合 | 改成断言 `loaded [1-9][0-9]* entries` 或从表计算 |
| 28 | **CI 的 `pkill -f net.minecraft.server` 可能杀不到 NeoForge dev 进程** | `neo-spike.yml:47-48` | 失败时服务端挂到 75 分钟超时 | 用进程组/`timeout` 包住 `runServer`，或记录子 PID |
| 29 | **`mod_description` 还写着 1.20.1** | `gradle.properties:62` | 模组列表/Modrinth 元数据显示错误版本 | 改成 1.21.1 / NeoForge |
| 30 | **文档仍写 1.20.1 路径和 `forge:mod_loaded`** | `docs/data-format.md:183-185`；`docs/DESIGN.md:129-131,227`；`README.md:82,86`；`BarrelBlock.java:34-36`；`ModItems.java:15-18` | 新贡献者按旧路径生成/写条件，直接踩迁移坑 | 全仓扫 `1.20.1`、`recipes/`、`loot_tables/`、`tags/blocks/`、`forge:mod_loaded`，按 1.21.1 名称改 |
| 31 | **状态表把堆肥标签写成“已对齐”** | `docs/status.md` 堆肥相关行 | 与 P0 #4 的 `grass`/`short_grass` 差异冲突，交接会误判 | 修数据或改成 ⚠️ 并链接本文件 |
| 32 | ~~**调试代码留在生产源**~~（debug 类/OreColors 分支已删；`CommonEvents` 的 `[debug]` 日志仍留） | `OreColors.java:60-89`；`OreDebugScreen.java`；`OreDebugScreenshot.java`；`CommonEvents.java:35-37` | 生产 jar 多三个 debug 类和一个系统属性钩子；日志噪音 | 移到 test/dev 源集，或用 `@EventBusSubscriber` 的 dev-only 条件；至少删掉截图钩子 |
| 33 | **`MoonlightCompat` 实际未被调用，且 `item()` 返回 AIR 而不是 null** | `MoonlightCompat.java:48-58`；全仓无 `logStatus`/`item` 调用 | M8 之前是死代码；一旦使用 `hasItem()` 永远为 true | 先修 `getOptional`；在 M8 实现前保留但标注“未接线” |
| 34 | **生成脚本与文档命令不一致**：`generate_sieve_assets.py` 不在常用命令里，`generate_barrel_variants.py` 却是坏的 | `docs/status.md` 常用命令；`docs/data-format.md:183` | 维护者不知道哪个脚本该跑，容易跑到旧脚本 | 统一脚本清单；给每个脚本加 `--check`（只校验不写） |
| 35 | **`check-textures.py` 只查 models/textures，不查 blockstates 引用和 data JSON** | `scripts/check-textures.py:40-52` | 生成器写错 blockstate 路径/模型名时，纹理检查仍通过 | 增加 blockstates → models、loot/recipe id 的基础存在性检查 |
| 36 | **`processResources` 把 data 表复制进 assets，jar 内有两份** | `build.gradle` 的 `processResources` | 包体增大；专用服客户端读不到服务端数据包，JEI 只能显示内置镜像 | 已接受并在 data-format §14 写清；单人/局域网 JEI 优先服务端表，专用服客户端需同装数据包 |

## P3：清理与路线图（潜在债）

| # | 问题 | 说明 |
|---|---|---|
| 37 | **零自动化测试** | `src/test` 不存在；`build` 的 `test` 是 NO-SOURCE。最值得补：`SieveRegistry`/`BarrelRecipes`/`CompostRegistry` 的解析、重复/坏 id、`BarrelBlockEntity.fill` 状态机、`SieveBlockEntity` 结算 |
| 38 | **数据格式没有 schema/版本** | `sieve`/`barrel`/`compost`/`materials` 都是 de facto API；字段改名/新增无版本号、无迁移、无 `--check`。建议给每个格式写 JSON Schema 或 Java 校验器 |
| 39 | **无 datagen / 通用 config** | M7 未做；材料表/筛/锤等仍靠 Python 脚本 + 手写 JSON；目前只有坩埚三项 config（`capacity` / `meltCapacity` / `heatSpeedMultiplier`） |
| 40 | **BE NBT 没有 `DataVersion`** | 桶有 `CompostScale` 迁移，筛子只有 `Work`/`Contents`/`Mesh`；未来改字段只能猜。建议加 `DataVersion` 或 `SieveVersion` |
| 41 | **`BarrelBlockEntity` 单类 489 行，职责过多** | 流体/堆肥/降雨/固体/比较器/NBT 都在一个类；拆出 `BarrelTank`/`CompostState`/`SolidState` 后，P0 #3 这类状态互斥更容易守住 |
| 42 | **`getSolid()`/`getMesh()`/`getContents()` 直接返回内部 ItemStack** | `BarrelBlockEntity:132`、`SieveBlockEntity:46,61`；渲染器只读所以现在没事，但外部一改就穿透 BE 状态 | 返回 `copy()` 或只读视图 |
| 43 | **`SieveBlockEntity` 公共 `setMesh` 不校验/MeshItem 的 `tier` 无上限** | 只被方块调用；未来 API 化时容易写坏 `MESH` 状态 | 内部化或加校验 |
| 44 | **`Barrel` 的堆肥产物硬编码 `Items.DIRT`** | `BarrelBlockEntity:297-305`；`compost` JSON 只有分值 | 想做“堆肥 → 其它产物”的联动要改代码；classic 只有泥土，可接受，但要写进扩展边界 |
| 45 | **无 EMI/REI、无 item handler/自动化接口、无配方条件** | JEI/Jade 已有；M7 之后再看；当前 `forge:mod_loaded` 文档是旧写法 | 路线图 |
| 46 | **`materials` 的 `source`/`owner_mod` 不参与任何逻辑** | 文档说 `owner_mod` 只用于自检/文档；目前连校验都没有 | 低优先级；要么删字段，要么在加载时校验 |

## 建议的修复顺序（最小改动优先）

1. **P0 #1** 修 `generate_barrel_variants.py`（避免下一次跑脚本产生坏数据）。
2. **P0 #4** 修 `grass`/`short_grass` + `getOptional`（一处数据 + 三个 registry 的校验习惯）。
3. **P0 #3** 给 `BarrelBlockEntity.fill` 加状态互斥（管道/自动化前必须做）。
4. **P0 #2** 补筛子/桶的破坏返还（玩家最容易被吞物品）。
5. **P0 #5/#6/#7** 材料表：先修正文档与逐条校验，再决定是否实现 config 层。
6. **P1 #8** 拆分服务端表与客户端 JEI 视图（F3+T 场景）。
7. **P1 #9/#12/#14** 统一 `getOptional`、JEI 超限警告、筛表变更兜底。
8. **P2 #25/#29/#30** CI + 元数据 + 文档过期项，成本低、收益高。
