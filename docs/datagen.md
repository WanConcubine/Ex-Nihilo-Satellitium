# 数据生成（DataGen）与第二批迁移

> 2026-11-02。第一批（标签 + 方块掉落表）已在主线和 `1.20.1-forge` 上完成并提交。
> 本文是**第二批**的施工单；新会话从这里开始，不要重做第一批。

## 1. 现状（第一批已完成）

- Gradle 任务：`./gradlew runData`（1.20.1：`--offline -Dnet.minecraftforge.gradle.check.certs=false`）
- 输出：`src/generated/resources`（已加入主资源集，提交进仓库；`.cache/` 已 gitignore）
- Provider（两版同名类）：
  - `datagen/ModDataGen` — `GatherDataEvent` 入口
  - `datagen/ModBlockTags` — 方块标签
  - `datagen/ModItemTags` — 物品标签（矿石/锭/堆肥）
  - `datagen/ModLootTables` — 69 张自掉表
- 旧的手写 `data/**/tags/**` 与 `data/**/loot_table(s)/**` 已删除。
- `scripts/check-data-paths.py`（主线）会同时扫 `src/main/resources` 和
  `src/generated/resources`，当前输出 `145 recipes, 69 loot tables, 41 tags`。
- 提交：main `8fe7314`，分支 `0b7df76`。

## 2. 第二批范围

| 类别 | 当前来源 | 数量 | 目标 |
|---|---|---|---|
| 配方 | 手写 JSON | 145 | `RecipeProvider` 生成 |
| 模型 / blockstate | Python 生成器 | 约 100+ JSON | `BlockStateProvider` / `ItemModelProvider` |
| 语言 | 手写 `en_us` / `zh_cn` | 265 键 | 建议**保持 JSON**（双语手维护），除非所有者要求迁 |
| 自定义玩法表 | JSON + reload listener | sieve/hammer/crook/compost/barrel/crucible_* | 不属于原版 datagen，暂不迁 |

**推荐顺序**：配方 → 模型/blockstate → 语言结论。每批都要先对比生成结果，再删旧文件。

## 3. 配方迁移（第二批主项）

### 3.1 分类与来源

主资源目录：`src/main/resources/data/exnihilosatellitium/recipe/`（1.20.1 是 `recipes/`）。

- 矿链（117 条中的大部分）：
  - 36 条 `*_<stage>_to_ore_<kind>`：`broken→gravel`、`crushed→sand`、`powdered→dust`，2×2。
  - 36 条 `*_ore_<kind>_smelting`：矿块 → 锭，exp 0.1 / 200 tick。
  - 12 条 `*_ore_<kind>_smelting_mekanism`（仅主线）：`neoforge:mod_loaded=mekanism`；
    base 配方带 `neoforge:not(mod_loaded(mekanism))`。
- 12 条桶配方、11 条木坩埚、4 台机器、6 个插件、6 锤、2 钩、筛子/三网、9 人偶、
  瓷土/瓷桶/坩埚、熟蚕 3 条等（约 28 条手写形状）。
- 总计 145 条：36+36+12+12+11+4+6+6+2+1+3+9+7 ≈ 145（以目录为准）。

### 3.2 写法

- `ModRecipes extends RecipeProvider`：
  - 主线 1.21.1 构造器 `(PackOutput, CompletableFuture<HolderLookup.Provider>)`，
    `buildRecipes(RecipeOutput out)`；
  - 1.20.1 构造器 `(PackOutput)`，`buildRecipes(Consumer<FinishedRecipe> out)`。
- 用 `ShapedRecipeBuilder` / `ShapelessRecipeBuilder` / `SimpleCookingRecipeBuilder`
  （1.20.1 是 `SimpleCookingRecipeBuilder`，1.21 同样可用）并显式
  `save(out, new ResourceLocation(MOD_ID, name))`，保证 id 与旧 JSON 一致。
- 条件：
  - 主线：`out.withConditions(new ModLoadedCondition("mekanism"))`；base 用
    `out.withConditions(new NotCondition(new ModLoadedCondition("mekanism")))`。
  - 1.20.1：`IConditionBuilder` 的 `modLoaded(...)` / `not(...)`，
    用 `ConditionalRecipe.builder()` 包一层（见 `net.minecraftforge.common.crafting.conditions`）。
- 建议先把矿链/桶/坩埚用循环写掉（占 117 条），手写配方逐个照 JSON 抄。
- `RecipeBuilder#save` 会生成 `advancement/recipes/...`；如果不想新增 advancement，
  需要自己构造 `Recipe` 并 `output.accept(id, recipe, null)`，先跑一次看输出再决定。

### 3.3 验收（必须做）

1. `runData` 生成到 `src/generated/resources/data/.../recipe(s)`。
2. 用语义对比（忽略 key 顺序、`replace`、`bonus_rolls`、`random_sequence`）把生成文件与
   旧 JSON 逐条比较；**只有 145 条全部一致/等价**才删除旧文件。
3. `git diff` 应该只有“删旧 + 加 generated”；不要出现配方 id 变化。
4. `./gradlew build` + 服务端启动（`Loaded 1423 recipes` 级别，且无 missing recipe）。
5. 主线跑 `check-data-paths.py`；两条线跑 `check-textures.py`。

## 4. 模型 / blockstate（配方之后）

- 现有 Python：`generate_ore_variants.py`、`generate_barrel_variants.py`、
  `generate_wooden_crucible_variants.py`、`generate_sieve_assets.py`、
  `generate_machine_assets.py`。
- 这些脚本**同时生成 PNG**（物品图标、机器 GUI/32×32 方块贴图）。datagen 只接管 JSON
  （blockstate / block model / item model），PNG 仍留在 Python/art 流程。
- Java provider：
  - `BlockStateProvider`（`registerStatesAndModels`）— barrel/crucible/ore/sieve/machine；
  - `ItemModelProvider`（`registerModels`）— 物品图标模型；
  - `ExistingFileHelper` 已在 `GatherDataEvent` 里，需要 `--existing src/main/resources`。
- 迁移后从 Python 生成器里删掉“写 JSON”的部分，只留 PNG；否则重跑会复活旧文件。
- 视觉风险高：生成后要对比 blockstate/model 引用，最好进客户端看一眼。

## 5. 语言（建议不迁）

- 265 个键、中英双语、含 Jade/JEI 配置键；`LanguageProvider` 会把它们搬进 Java，
  维护成本高于收益。
- 建议保留 `assets/exnihilosatellitium/lang/*.json`，只在新增内容时手加。
- 如果所有者明确要求，再单开一批。

## 6. 环境与命令

```bash
# 主线
./gradlew runData --offline
./gradlew build --offline
python3 scripts/check-textures.py
python3 scripts/check-core-imports.py
python3 scripts/check-data-paths.py

# 1.20.1 worktree
cd ../Ex-Nihilo-Satellitium-1.20.1
JAVA_HOME=.../temurin-17.jdk/Contents/Home ./gradlew runData --offline -Dnet.minecraftforge.gradle.check.certs=false
./gradlew build --offline -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
```

- `runData` 后检查 `git status`：`.cache` 应被忽略，generated JSON 应显式提交。
- 服务端冒烟：`runServer`，日志看 `Loaded ... recipes`、`[barrel] 12 transforms`、
  `[sieve] 6 inputs, 72 drops`，无 missing tag/loot/recipe。
- 两条线都**未 push**（2026-11-02：main ahead 6，分支 ahead 8）。

## 7. 新会话开场清单

1. 读 `docs/status.md` 的「下一步」与本文。
2. `git status --short` 确认干净；`./gradlew build --offline` 先绿。
3. 跑一次 `runData`，确认 `git status` **零 diff**（证明第一批稳定）。
4. 按 §3 做配方：先写 provider，再对比，再删旧 JSON。
5. 配方稳定后再动模型/blockstate；语言默认不动。

## 8. 版本差异速查（1.20.1 Forge）

- 包：`net.minecraftforge.*`；`GatherDataEvent` 在
  `net.minecraftforge.data.event.GatherDataEvent`，入口类用 `@Mod.EventBusSubscriber`。
- 标签目录：`data/<ns>/tags/blocks|items`（复数）；命名空间 `forge:`。
- 条件：`forge:mod_loaded` / `forge:not`；`IConditionBuilder` 在
  `net.minecraftforge.common.crafting.conditions`。
- 资源路径：`recipes/`、`loot_tables/`、`advancements/`（复数）。
- `runData` 配置在 `build.gradle` 的 `data` run（已有，不要重复添加）。
- `LootTableProvider` 构造器没有 `HolderLookup.Provider` 参数；
  `BlockLootSubProvider` 是 `(Set<Item>, FeatureFlagSet)`。
- `RecipeProvider` 构造器只有 `PackOutput`，`buildRecipes(Consumer<FinishedRecipe>)`。
