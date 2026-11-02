# 数据生成（DataGen）——1.20.1 分支现状

> 2026-11-02。**本分支只做了第一批**：方块/物品标签 + 方块掉落表。
> 配方、blockstate、模型在这条线**仍是手写 JSON**；主线已完成第二、三批，待移植。

## 1. 本分支现在有什么

- Gradle 任务：`./gradlew runData --offline -Dnet.minecraftforge.gradle.check.certs=false`
- 输出：`src/generated/resources`（已加入主资源集，提交进仓库；`.cache/` 已 gitignore）
- Provider（4 个，包 `datagen/`）：

| Provider | 产出 | 文件数 |
|---|---|---|
| `ModDataGen` | `GatherDataEvent` 入口 | — |
| `ModBlockTags` | 方块标签：`forge:ores/<m>`、`mineable/*`、`#exnihilosatellitium:wooden_crucibles` | 18 |
| `ModItemTags` | 物品标签：`forge:ores|ingots/<m>`、`#exnihilosatellitium:compostable` | 23 |
| `ModLootTables` | 方块自掉表 | 69 |

- 当前整个 `data/` 树：**160 recipe / 69 loot_tables / 41 tags**（160 条配方是手写）。
- 旧的手写 `data/**/tags/**` 与 `data/**/loot_tables/**` 已删除（提交 `0b7df76`）。

## 2. 主线已经做完、这里还没做的

| 批次 | 主线实现 | 本分支状态 |
|---|---|---|
| 第二批：配方（133 条 base 生成 + 27 条候选变体手写） | `datagen/ModRecipes`（+ `datagen/WoodTypes`） | ⬜ 160 条全是手写 JSON |
| 第三批：blockstate + 模型（71 + 244） | `datagen/ModBlockStates`、`datagen/ModItemModels` | ⬜ 全部手写 JSON |
| 语言 | 保持手写 JSON（已拍板） | 同 |

移植时注意 1.20.1 的 API 差异：

- 没有 `RecipeOutput`：`buildRecipes(Consumer<FinishedRecipe> out)`，`RecipeBuilder#save` 的第二参数是
  `String` id；路径是 `recipes/`、结果字段是 `result.item`。
- 配方条件键是**裸 `conditions`**（不是 `forge:conditions`，写错会被 Forge 静默忽略）；
  条件类型是 `forge:mod_loaded` / `forge:not` / `forge:or`。
- `BlockStateProvider` / `ItemModelProvider` 在 `net.minecraftforge.client.model.generators`，
  构造器与 NeoForge 版略有差别；`ExistingFileHelper` 同样需要 `--existing`。
- `materials.json` 的 `ingots` 候选与 `OreMaterial.ingotCandidates` 已与主线一致。
- `runData` 的 `data()` 已在 `build.gradle` 里配好输出与 `--existing`。

## 3. 命令与冒烟

```bash
JAVA_HOME=.../temurin-17.jdk/Contents/Home ./gradlew runData --offline \
  -Dnet.minecraftforge.gradle.check.certs=false
JAVA_HOME=.../temurin-17.jdk/Contents/Home ./gradlew build --offline \
  -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
```

- `runData` 后 `git status` 应干净（`.cache` 被忽略，generated JSON 显式提交）。
- 服务端冒烟看：`[barrel] 12 transforms`、`[sieve] 6 inputs, 76 drops`、`[hammer] 49 recipes`、
  `[crucible] 39 meltables / 9 heat sources`、`[compost] 65 entries`，无 missing tag/loot/recipe。
- 日志里的 `Loaded N recipes` 是**配方类型数**（1.20.1 的 `RecipeManager` 就这么打印），不是配方条数。

## 4. 新会话开场清单

1. 读 `docs/status.md` 的「下一步」与本文。
2. `git status --short` 干净；`./gradlew build` 先绿。
3. 跑一次 `runData`，确认 `git status` 零 diff。
4. 要移植配方/模型时，先在主线拿到 `ModRecipes` / `ModBlockStates` / `ModItemModels` 的最终版，
   再按第 2 节的差异改写；语义对比脚本（`compare-recipes.py` / `compare-models.py`）在主线仓库。
