# 技术债清单（2026-11-09 第二轮审计）

> 范围：`src/main/java`（156 个文件 / 16.9k 行）、`src/main/resources`、`src/generated`、
> `scripts/`、`build.gradle`、`.github/workflows`、两个分支的 `common/` 差异、文档数字。
> 方法：通读状态机/表加载器/注册/渲染/脚本，跑守卫与构建，做跨分支 diff；**没有**重跑
> `runData`、没有进游戏。文中的行号以本轮 `main` 工作树为准。
>
> 状态：`P0` 已坏功能/数据；`P1` 结构性风险，下个批次必须处理；`P2` 性能/工具链；
> `P3` 重构机会与清理。首轮（2026-10-30）的编号保留，已关闭的折叠到文末，新发现从 #47 起。

## 本轮结论

- **1 个 P0（已修并实测通过）**：`1.21.1` 的木钩/骨钩在 2026-11-08 的 DE 锤提交（`16c78bb`）
  后不再掉耐久；2026-11-09 恢复 `wearMainHand` 的通用可损坏物品判断，并在开发服上
  用 FakePlayer + `gameMode.destroyBlock` 实测：木钩/骨钩/木锤各 `damage=1`，创造钩 0（见 #47）。
- **3 个 P1 流程问题（#48–#50 已修，待首次 GitHub 运行确认）**：CI 的
  `[materials] loaded 12 entries` 断言按当前材料表必红（#48）；CI 不跑 core/data 守卫、
  也不跑 PR（#49）；1.20.1 分支与 `main` 的 `common/` 已实质漂移，且分支没有守卫（#51）。
- **最大重构点**：6 份几乎同构的表加载器（~2.2k 行，含 6 个空壳 reload listener）与
  `BarrelBlockEntity` 单类 1097 行的多状态机（#66/#67）。
- **最大性能点**：机器每 tick `setChanged` + 全槽扫描（#54）、升级查询在 BER 每帧分配/排序
  （#55）、桶/侵染树叶的高频同步（#57）。

## 本轮实际跑过的证据

| 命令 | 结果 |
|---|---|
| `./gradlew build --offline` | ✅ BUILD SUCCESSFUL（2026-11-09） |
| `./gradlew test --offline` | ⚠️ `compileTestJava NO-SOURCE` / `test NO-SOURCE`（零测试） |
| CI 工作流本地校验 | ✅ Ruby YAML 解析 + `bash -n` + Python `py_compile`；materials 断言用假日志验过 14 通过 / 12 失败；**未在 GitHub 首跑** |
| 服务端钩子耐久探针（临时命令，已删） | ✅ `FakePlayer` + `gameMode.destroyBlock`：木钩 / 骨钩 / 木锤各 `damage=1`，创造模式钩 `damage=0`；探针类已从源码删除，jar 内无残留 |
| `python3 scripts/check-textures.py` | ✅ 307 models / 116 blockstates / 190 textures |
| `python3 scripts/check-core-imports.py` | ✅ 0 known leak(s) |
| `python3 scripts/check-data-paths.py` | ✅ 297 recipes / 75 loot tables / 75 tags |
| `python3 scripts/check-wiki.py` | ✅ 8 pages / 8 translations / 91 links / 74 JSON / 8 nav |
| `diff -rq` 两分支 `common/` | 39 个文件不同（含预期 API 差异与真实逻辑漂移） |
| `materials.json` 解析 | 26 条：14 金属无条件 + 12 `gems` 需模组 |

## P0：已坏功能（#47 已修并实测）

| # | 问题 | 证据 | 影响 | 建议 |
|---|---|---|---|---|
| 47 | **`1.21.1` 木钩/骨钩不掉耐久**（`16c78bb` 引入的回归）— **2026-11-09 已修并实测通过** | `common/event/ToolEvents.java:112-123` 的 crook 分支调用 `wearMainHand`；回归版本 `:161-169` 的 `wearMainHand` 只在 `held.getItem() instanceof HammerItem` 时扣耐久；`CrookItem extends Item`（`common/item/CrookItem.java:10`），不是 `HammerItem`。1.20.1 分支自己写 `tool.hurtAndBreak`，所以只有主线坏。现已改为 `held.isDamageableItem()`（见下方代码） | 回归期间钩子无限使用（M3 进度平衡失效）；修复后开发服实测 `damage=1`，`status.md` 已改回 ✅ | 已完成：代码改一行 + 服务端 FakePlayer 实测（木钩/骨钩/木锤各 1，创造钩 0，临时探针已删）；后续由单测/CI 防回归（#52） |

已应用的修复（保持锤/能量分支不动；`./gradlew build --offline` 通过）：

```java
private static void wearMainHand(final Player player, final ItemStack eventTool) {
    if (player.isCreative()) {
        return;
    }
    final ItemStack held = player.getMainHandItem();
    if (held.isDamageableItem() && held.getItem() == eventTool.getItem()) {
        held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
```

> 为什么不是「再给 crook 加一个分支」：`wearMainHand` 的职责本来就是「扣手上工具」，
> 类型过滤是 DE 锤引入能量分支时顺手加的；恢复通用判断可以同时覆盖未来的其它工具类型。

## P1：结构性风险

| # | 问题 | 证据 | 影响 | 建议 |
|---|---|---|---|---|
| 48 | **CI 的 materials 断言写死 12** — **2026-11-09 已修，待首次运行确认** | 旧 `.github/workflows/neo-spike.yml:57` 断言 `[materials] loaded 12 entries`；当前 `materials.json` 有 26 条，其中 12 条 `gems` 在无模组时被 `OreMaterials.java:109-113` 跳过 → CI 环境实际是 **14**。现在 smoke 步骤用 Python 从 `materials.json` 计算期望值（enabled 非 gem + 无条件 gem），再和日志里的 `[materials] loaded N entries` 比较 | 旧断言让 `build` 工作流在 smoke 步骤必红；失败后还会走 `ci-logs` 强推（#50） | 已按「从表计算」修；本地用假日志验过：14 通过、12 失败。后续新增/禁用材料不需要再改 CI |
| 49 | **CI 不跑 core/data 守卫，也没有 PR 触发** — **2026-11-09 已修，待首次运行确认** | 旧文件 `:29-32` 只跑 `check-textures` / `check-wiki`；`on:` 只有 `workflow_dispatch` + `push: main`（`:3-6`）。现已加 `check-core-imports.py` / `check-data-paths.py` 两步，并加 `pull_request`（`src/**`、`scripts/**`、`build.gradle`、`gradle**` 等路径） | AGENTS §2 要求的两道守卫现在会在 PR 上执行；PR 不再能把坏数据路径/核心 import 合进 main | 已修；`runServer` 改用 `setsid` 启到独立进程组，结束时 `kill -- -PID` 整组回收，`pkill` 只留作兜底（首轮 #28） |
| 50 | **CI 失败会强推 `ci-logs`，且权限过大** — **2026-11-09 已修，待首次运行确认** | 旧文件 `:8-9` `contents: write`；`:60-70` 失败时 `git push -f origin ci-logs`。现在 `permissions: contents: read`，删除 publish 步骤，失败日志改 `actions/upload-artifact@v4`（`server-log`） | 一次失败不再改写远端分支；`ci-logs` 不会继续堆积；工作流只读仓库 + 上传 artifact | 已修；远端既有的 `ci-logs` 分支需要所有者手动删除 |
| 51 | **1.20.1 分支与 main 的 core 已漂移，分支没有守卫** | `diff -rq` 两分支 `common/`：39 个文件不同。真实差异不止 API：分支 `common/material/OreMaterials.java` 直接 `import net.minecraftforge.fml.ModList`（绕过 `common/platform/Platform`）；分支 `common/table/CustomizationOverlay.java` 缺少 `DataPackJson.modLoaded(payload)` 过滤；分支 `scripts/` 没有 `check-core-imports.py` / `check-data-paths.py` / `check-wiki.py`；1.1.2 的钩子耐久修复在分支仍在（主线 #47 已另行修复） | 「core 可整包复制、只重写 platform」的契约已经名存实亡；每次修 bug 都要手工移植，且分支可以静默违反自己的架构 | ① 分支补上三个守卫（至少 core-imports）；② 加 `scripts/check-branch-sync.py`：对「可复制 core 文件清单」做哈希对比，允许白名单（platform 相关）并列出真实漂移；③ 发布检查单里跑一次 |
| 52 | **零自动化测试，状态机/表解析全靠人肉**（首轮 #37） | `src/test` 目录存在但为空；`build.gradle` 无 test 依赖；`./gradlew test` → `NO-SOURCE` | #47 正是单测能抓住的回归；7 张表的解析/patch/overlay 与桶状态机都是纯逻辑，却只能靠 RCON/进游戏 | 先加纯 Java JUnit 5（不启动 MC）：`TableBuilder` 的 add/remove/replace/conflict；把各 loader 的 `parseDrops`/`keysFor` 提成包内可见的静态函数后测边界；状态机拆分（#66）后补 `BarrelState` 迁移表。CI 跑 `./gradlew test` |
| 53 | **core 纯度守卫的覆盖范围与文档承诺不一致** | `scripts/check-core-imports.py` 的 `CORE_DIRS` 只含 11 个包 + `MachineBlockEntity.java`；`common/blockentity`、`common/block`、`common/item`、`common/event`、`common/network` 不在扫描内，而这些文件大量 import NeoForge：`BarrelBlockEntity.java:39-43`、`CrucibleBlock.java:26`、`PorcelainBucketItem.java:24`、`CapabilityEvents.java`。AGENTS §2 却写「core 层（数据表/掷表/**状态机**）不得 import NeoForge/Forge」 | 新贡献者会以为 BE/状态机是可移植 core，实际 1.20.1 重写了它们；守卫的「0 leak」只证明了一个更窄的事实 | 建议**收窄文档承诺**为「表/掷表/overlay 核心 + `MachineBlockEntity` 状态机」，在 `docs/porting.md` 写明 BE/block/item 属于每版重写层；若确实想覆盖 BE，需要先把 `FluidTank`/`FluidUtil` 包进 platform 门面，成本远大于收益 |

## P2：性能、正确性与工具链

| # | 问题 | 证据 | 影响 | 建议 |
|---|---|---|---|---|
| 54 | **机器每 tick 标记落盘 + 全槽扫描** | `MachineBlockEntity.serverTick` 末尾无条件 `setChanged()`（`:348`）；`relocateInvalidSlotItems()` 每 tick 扫 `0..outputSlotStart()`（`:321`、`:431-452`）；空闲时也会跑 | 每台工作机器每 tick 把 chunk 标脏，自动保存压力大；大量机器时扫描是纯浪费 | `setChanged()` 只在 `work/energy/burn` 真正变化时调用；`relocate` 改为「菜单关闭/加载/管道插入后」或 inventory 版本号触发；空闲早退 |
| 55 | **升级查询在渲染和 GUI 数据路径上分配 + 排序** | `MachineBlockEntity.upgradesOf`（`:163-183`）每次 new `ArrayList` + sort；`upgradeCount()` 调两次；`energyPerTick()` 又调 `upgradeCount()`；`MachineRenderer.render` 每帧调 `ticksPerOperation()`（`client/MachineRenderer.java:49`）→ `speedMultiplier()` → `upgradesOf` | 每台机器每帧 2 次列表分配/排序，GUI 打开时服务端每 tick 再多次；GC 抖动 | 在升级槽变化时缓存 `speedMultiplier`/`fortuneRolls`/`upgradeCount`（`MachineInventory.setChanged` 失效缓存），渲染只读缓存值 |
| 56 | **桶每 tick 做查表/降雨/群系查询**（首轮 #16） | `BarrelBlockEntity.serverTick:607-617` 满桶时每 tick 调 `BarrelRecipes.resultForFluid` 线性扫；`tickRain`（`:909-933`）每 tick `isRainingAt`，下雨时再 `getBiome(...).getModifiedClimateSettings()` | 雨天大量露天桶时每 tick 的 biome/heightmap 查询明显；满桶查表随数据包增大 | 雨水检查用 `gameTime % N` 或只在有玩家加载时跑；满桶的「上方流体」检查加 `gameTime % 5`；`resultForFluid` 改 `Map<String, Recipe>` 索引 |
| 57 | **高频 BE 同步** | 筛子每 2 work tick `sync()`（`SieveBlockEntity:115-117`，≈10 次/秒/台）；桶堆肥期间每 10 tick（`:579-582`）；**侵染树叶每 40 tick 永久同步**（`InfestedLeavesBlockEntity:83-86`），满进度、不再扩散后仍然发；每次 `getUpdateTag` 都是 `saveWithoutMetadata`（桶含 tank + 堆肥物品列表 + 所有计时器） | 大量筛子/桶/树叶时网络与序列化开销；满进度树叶是纯浪费 | 筛子改 4–5 tick 或独立进度包（首轮 #15）；桶把进度/颜色拆成更小的同步 tag（`CompostScale`/ingredients 只在变化时发）；侵染树叶在 `progress>=1 && spreadTimer==0` 时停止周期同步 |
| 58 | **`BarrelRenderer` 每帧分配** | `getCompostIngredients()` 返回 `List.copyOf`（`BarrelBlockEntity:178-180`），`BarrelRenderer.compostTint` 每帧调用；`vert()` 每个顶点 `new Vector3f`（`BarrelRenderer.java:226-228`），一个内容盒 ~24 顶点 | 大量桶时每帧几十~上百次小对象分配 | 把平均色缓存进 BE（3 个 float，随 `addCompostIngredient` 更新）；顶点变换改用 `matrix.transform` 到复用的 `Vector3f` 或手算，避免逐顶点 new |
| 59 | **`BarrelItemHandler.extractItem` 忽略 `amount`** | `platform/barrel/BarrelItemHandler.java:56-69`：`simulate` 分支按 `amount` 截断，`execute` 分支直接 `extractOutput(false)` 返回整栈 | 当前产物都是 1 个（泥土/固体），所以没暴露；一旦桶支持多产物或堆叠输出，管道会一次拿整栈，违反 `IItemHandler` 契约 | 执行路径也按 `min(amount, preview.getCount())` 返回，剩余留在桶里（需要给 `extractOutput` 加 amount 参数） |
| 60 | **关键查表是线性扫描** | `BarrelRecipes.resultForFluid`/`ruleForItem`（`:130-155`）扫 `List<Recipe>`；`CompostRegistry.valueOf`（`:244-256`）扫 67 条；`CrucibleHeat.speedFor`（`:97-104`）扫 9 条 | 当前规模无感；但 `MAX_RECIPES=4096`，满桶每 tick 的扫描会放大到不可忽略 | 桶表已有规范化 key（`key(recipe)`），直接存 `Map<String, Recipe>`；compost 可建 `Map<Item,Integer>` + tag 列表；heat 按 block 分桶 |
| 61 | **「锤子的时运槽只能放 1 个」规则重复三处** | `MachineBlockEntity.upgradesOf`（`:173-175`）、`AbstractMachineMenu.MachineSlot.getMaxStackSize`（`:62-68`）、`MachineItemHandler.getSlotLimit`（`:43-48`）各自判断 `kind()==HAMMER && slot==UPGRADE_SLOT_START+1` | 改槽位/规则时容易漏一处，GUI、管道、逻辑行为分叉 | 抽 `MachineBlockEntity.slotLimit(slot)` / `isUpgradeSlotValid` 单一入口，三处都调它 |
| 62 | **datagen 漂移没有自动检查** | `docs/status.md:347-348` 要求手工 `runData` 后看 `git status`；CI 没有 | 手写资源/生成资源会悄悄不一致，直到有人跑 datagen | CI 加一步 `./gradlew runData --offline` + `git diff --exit-code src/generated`（或单独的 datagen job） |
| 63 | **构建期联网下载 CraftTweaker** | `build.gradle:29-45`：`downloadCraftTweaker` 在 `compileJava` 前用 `URL.withInputStream` 抓 jar，无校验和，缓存到 `libs/`（gitignored）；失败/半截文件会留下坏缓存 | 离线/镜像环境构建不稳定；构建不可复现；供应链上没有完整性校验 | 优先修 BlameJared 仓库解析（Gradle 原生依赖）；不行就把 jar 放进带 sha256 的 `libs/` 并由脚本校验；至少校验文件大小/zip 完整性 |
| 64 | **`processResources` 手列 7 个数据目录** | `build.gradle:151-172` 逐目录 `from(...)`；新增第 8 张表要手工记得加 | 新表在专用服客户端 JEI 里静默缺失（数据在 `data/`，客户端只读 `assets/`） | 用循环遍历一个常量列表，或直接镜像 `data/exnihilosatellitium/{sieve,barrel,...}` 的父目录（注意排除 `recipe/loot_table/tags` 等 vanilla 目录） |
| 65 | **文档数字漂移** | `docs/status.md:396` 写「295 模型 / 171 纹理」「check-wiki 7 页 / 52 链接」，实际是 307/190 与 8 页/91 链接（同文件 `:62` 的当前批数字又与之矛盾）；`CrucibleRecipes.java:32-34` 说 `solid` 只用于文档/校验，实际 `CrucibleBlockEntity` 用它算容量与产出比（`docs/data-format.md:739-741` 才是对的） | 交接时按旧数字判断回归；读者被 javadoc 误导 | 每次守卫输出变化时同步 status 的「验证状态」；`solid` 的 javadoc 改成「熔料体积，参与容量与产出比」 |

## P3：重构机会与清理

| # | 问题 | 证据 | 说明 / 建议 |
|---|---|---|---|
| 66 | **`BarrelBlockEntity` 1097 行、5 套状态机交织**（首轮 #41，已从 489 涨到 1097） | 堆肥（progress/ticks/dirtReady/ingredients）、孢子（sporeProgress）、烹饪/人偶（pendingEntity/peacefulDrops/cookTicks）、延迟换液（pendingFluid*）、降雨（rainBuffer）都在一个类；`canUse`/`use`/`fill`/`tickSpores`/`canAcceptCompost` 各自重复互斥判断 | 先拆纯逻辑 `BarrelState`（无 MC 类型）：`EMPTY / COMPOSTING / CURING / DIRT_READY / SOLID / COOKING / CONVERTING` + 合法迁移表，让非法组合不可表达；再把 `BarrelTank`（温度/凝固/容量）与 `NbtCodec` 拆出。`cookTicks` 那套「旧存档兼容」在 `startCooking` 立刻 `finishCooking`（`:759-779`），可删或明确标注 only-for-legacy |
| 67 | **6 份几乎同构的表加载器** | `SieveRegistry`(382)/`CrookDrops`(382)/`HammerRecipes`(292)/`CrucibleRecipes`(279)/`CrucibleHeat`(368)/`BarrelRecipes`(552) 都是 load→rebuild→tagFiles→patchFiles→overlay→snapshot；`union`/`parseDrops`/`isEmptyArray`/`overlayLabel`/`overlaySource` 成对复制；Sieve 与 Crook 文本相似度 64%，方法体级别 `for`/`if` 直接重复 | 抽 `AbstractJsonTable<K,V>`（模板方法：parse/apply/merge），表只实现 4 个钩子。收益：约 600–800 行消失；patch/overlay 语义只有一份；`TableDump`/JEI 读取也统一。风险：7 张表的字段名不同，先做 Sieve+Crook（最像），再 Hammer/Crucible |
| 68 | **6 个空壳 reload listener，服务端/客户端各注册一次** | `common/{sieve,hammer,crook,barrel,crucible,compost}/*ReloadListener.java` 每个 ~25–30 行，`prepare` 全返回 null，`apply` 一行；`ClientEvents.registerReloadListeners` 又逐个注册 6 个 | 合并成 `TableReloadListener(TableId, clientView)` 一个类，内部 `Tables.load(...)`；文件数 -5，注册处 -10 行。Compost 的 `SimpleJsonResourceReloadListener` 与其它 `DataPackJson.readAll` 也可统一 |
| 69 | **KJS/CT 绑定层成对复制** | `compat/kubejs/KubeJSBridge.java` 与 `compat/crafttweaker/CraftTweakerBridge.java` 除 `SOURCE`/`PRIORITY` 外逐字相同；7 对 `SieveEvents`/`SieveCT`、`BarrelEvents`/`BarrelCT`… 也只是 `static` 与 `@ZenCodeType.Method` 的差异 | 抽一个 `ScriptPayloads`（`input/drop/target/recipe` 的 JsonObject 构造 + `parse`），两侧只保留 submit 与注解；减少「改一边忘另一边」的漂移（#51 已经在漂） |
| 70 | **电动/内燃机器 screen 约 90% 重复** | `ElectricMachineScreen` 与 `FuelMachineScreen` 各 138 行；`init`/`openOutputConfig`/`mouseClicked`/`render` 仅 gauge 数据源与纹理不同（方法体 diff 见本轮审计） | 抽 `AbstractMachineScreen<T extends AbstractMachineMenu>`，子类只提供 gauge 的 `(value,max,texture,fill,icon)`；顺带统一输出配置抽屉的点击区域 |
| 71 | **注册表/创造栏大量手写清单** | `ModBlocks` 11 个 barrel + 11 个 crucible 常量；`ModItems.BARREL_ITEMS`/`WOODEN_CRUCIBLES` 再抄一遍；`ModBlockEntities.CRUCIBLE` 再列 11 个；`ModCreativeTabs` 逐项 `output.accept`（~50 行） | 用 `WoodTypes.ALL` 生成 barrel/crucible 的 block/item 注册与 BE 列表；创造栏按注册表遍历 + 少量排除，避免新增物品忘记进栏。注意保持 `barrel`/`wooden_crucible` 的旧 id |
| 72 | **BE NBT 没有 `DataVersion`**（首轮 #40） | `BarrelBlockEntity` 只有 ad-hoc `CompostScale`（`:1023-1083`）；`Sieve`/`Crucible`/`InfestedLeaves`/`Machine` 完全没有版本字段 | #66 拆状态机会改 NBT 键；现在没有版本号只能猜。给每个 BE 加 `DataVersion`（或统一的 `ExNihiloData` 包装），迁移逻辑集中到一处 |
| 73 | **公共 getter 返回内部 `ItemStack`**（首轮 #42） | `BarrelBlockEntity.getSolid():187`、`SieveBlockEntity.getMesh():46` / `getContents():61`、`CrucibleBlockEntity.getPendingStack():117` | 渲染器只读所以现在没炸；拆分/测试后外部一改就穿透 BE 状态。返回 `copy()` 或只读视图（渲染热路径可用 `ItemStack` 的不可变快照缓存） |
| 74 | **死代码/调试残留**（首轮 #32/#33） | `compat/MoonlightCompat.java` 全仓无引用（M8 占位）；`build.gradle:199-206` `printDeClasspath` 调试任务；`CommonEvents.java:70` `[debug] our recipes=...`（每次开服遍历全部物品建 `ItemStack`）；`BarrelBlockEntity` 的 `cookTicks` 兼容机 | 删除或移到 dev 源集；`[debug]` 改成 `LOGGER.debug` 或删除；Moonlight 保留接口但标注 `@Deprecated`/未接线 |
| 75 | **数据格式没有 schema/版本**（首轮 #38） | 7 张表的字段是 de-facto API；坏字段只靠各 loader 的 `warn`；没有 JSON Schema、没有 `--check`、没有格式版本号 | 给每张表写一个最小 JSON Schema（或 Java validator），在 `check-data-paths.py` 里跑；字段改名时用 `format_version` 拒绝旧文件而不是静默降级 |
| 76 | **桶的堆肥产物硬编码泥土**（首轮 #44） | `BarrelBlockEntity.getOutputStack():322-328`、`extractDirt():548-552` 直接 `new ItemStack(Items.DIRT)`；`compost` JSON 只有分值 | classic 只有泥土，可接受；但要做「堆肥 → 其它产物」联动就得改代码。要么在文档写死边界，要么给 `compost` 文件加可选 `result` |
| 77 | **`check-textures.py` 不查 data JSON 引用的存在性**（首轮 #35 的剩余部分） | 脚本只查 models/blockstates/textures；recipe/loot 里写错 id 仍能过 | 可选：把 `check-data-paths.py` 扩成「引用的物品/方块必须存在」，但需要 registry 快照，成本较高；至少对 `sieve/hammer` 的 `result/item` 做静态 id 存在性检查 |
| 78 | **`SieveBlockEntity.setMesh` 公共 API 不校验 / MeshItem tier 无上限**（首轮 #43） | `:54-59` 只 `copyWithCount(1)`；`MeshItem` 的 tier 由常量决定，未来脚本/数据驱动时容易写出 `MESH=99` | 拆状态机时把 `setMesh`/`setContents` 收成包内可见，或加 `tier` 范围校验 |
| 79 | **「数据表镜像进 assets」是已接受的债**（首轮 #36） | `processResources` 复制 7 个目录；专用服客户端 JEI 只能读内置镜像（P4 路线） | 保持现状可以，但 #64 的目录列表要和新增表同步；P4 做客户端 JEI 同步时一并去掉镜像 |
| 80 | **桶的流体匹配仍是 `Fluid ==`**（首轮 #17 的剩余部分） | `BarrelBlockEntity.tank.fill` 的凝固判断（`:103-104`）与 `BarrelRecipes.resultForFluid`（`:130-137`）比较 `Fluid` 实例；`normalizeFluid` 只用在「桶上方流体」路径（`:607-617`），`tank.fill` 未归一 | 正常桶/管道只给 still 流体，所以当前不炸；若某个模组管道把 flowing 灌进空桶，后续凝固规则全部匹配不到，表现为「满桶但没反应」 | 在 `fill` 开头把 `FlowingFluid` 归一成 source（或统一用 `FluidStack.isSameFluid`），并给一条「flowing 灌入仍能凝固」的服务端测试 |

## 建议的修复/重构顺序

1. **#47（P0，已完成）**：代码已修、开发服实测通过（木钩/骨钩/木锤 `damage=1`，创造钩 0），
   `status.md` 已改回 ✅；后续用 #52 的单测防回归。
2. **#48/#49/#50（P1，已修待首跑）**：CI 断言已改为从 `materials.json` 计算、补了两个守卫、
   加了 PR 触发、日志改 artifact；下一步是首次 GitHub 运行确认（以及删除远端旧的 `ci-logs`）。
   这一步完成后 #47 这类回归至少能在 main 之前被看到。
3. **#51/#53（P1，1–2 天）**：分支补 core-imports 守卫 + `check-branch-sync.py`；
   把 AGENTS/porting 的「core」定义收窄到实际可复制的范围。
4. **#54/#55/#57（P2 性能，先基准）**：用 20 台机器/50 个桶/一片侵染树叶做服务端 tick 对比，
   再按「setChanged 去抖 → 升级缓存 → 同步降频」的顺序改；#58 用 F3 帧时间/GC 日志确认。
5. **#67 + #68（重构件，先 Sieve+Crook）**：抽泛型表 + 单 reload listener，跑 `runData` 与
   `compare-recipes/models` 确认字节/语义不变；这一步会顺手解决 #69/#70 的一部分。
6. **#66 + #72 + #52（状态机与测试）**：先 `BarrelState` 纯类 + JUnit，再搬 NBT；
   这是从「能跑」到「敢改」的分水岭。
7. 其余 P2/P3 按发布节奏穿插（#63/#62/#64 是工具链，适合单独一个 chore 批次）。

## 已关闭 / 不再成立的条目（相对 2026-10-30 首轮）

- **已修复并抽查确认**：#9（`getOptional` 统一，未再发现 defaulted `get`）；#10/#11（筛网
  `copyWithCount(1)` 保留 damage、三档 `stacksTo(1)`）；#12（JEI 筛子页改为 7×3 轮转，
  不再截断 21）；#13（非 BlockItem 输入有平铺图标兜底）；#14（结算时表为空退还材料）；
  #18/#19（重复规则/输入会 warn，
  `TableBuilder.logConflicts` 记录 overlay 冲突）；#22/#23（脚本已处理 object rarity 与
  `enabled:false`）；#29（`mod_description` 已是 1.21.1）；#39（datagen 接管 tags/loot/
  recipes/blockstates/models）；#31（status 的堆肥标签行已随数据更新）。
- **已过时**：#46（`materials.json` 现在用 `source` 字段，文档明确标 ❌ 不参与逻辑，不再算债）。
- **已接受、不再作为债**：#36（数据镜像进 assets，P4 再动）；#45 的「无 item handler」部分
  （`BarrelItemHandler`/`MachineItemHandler` 已接）。
- **首轮遗留、本轮合并进新编号**：#15/#16 → #57/#56；#17 → #80；#25/#26/#27/#28 → #49/#50；
  #30 → #65；#32/#33 → #74；#35 → #77；#37 → #52；#38 → #75；#40 → #72；
  #41 → #66；#42 → #73；#43 → #78；#44 → #76。

## 附：本轮看到但**没有**列成债的点

- `TableBuilder` 的「谁最后写了这个 key」、overlay 去重、`/exnihilo dump` 的确定性排序，
  设计是自洽的，是本仓库最扎实的一块。
- `DataPackJson.readAll` 的按 id 排序 + `tagsReady()` 重试，配合 `TagsUpdatedEvent` 的双阶段
  加载，能自愈；不建议为了「更快」改成单阶段。
- 七个表的「重复输入」语义（筛子后加载覆盖、锤/钩/熔点先到者赢、桶先到者赢）在
  `docs/data-format.md:813-819` 已写清，是**有意**的 API 差异，不算不一致；
  但作者仍容易踩，建议在 wiki 的 cookbook 里给一张对照表。
