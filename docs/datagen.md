# 数据生成（DataGen）——1.20.1 分支现状

> 2026-11-02。**三批都已完成**：标签 + 掉落表、配方、blockstate/模型。
> 现在这条线和主线一样，vanilla 类型的数据全部由 `runData` 生成。

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
| `ModRecipes` | 配方（矿石链、桶/坩埚、筛/网、锤/钩、机器/插件、人偶、瓷、石子） | 137 |
| `ModBlockStates` | blockstate + 机械方块模型 | 71 + 29 |
| `ModItemModels` | 物品模型（从注册表推导） | 148 |

- 当前整个 `data/` 树：**176 recipe / 69 loot_tables / 41 tags**（137 条生成 + 39 条手写）。
- 手写保留：27 条 `*_smelting_<mod>` 候选变体（产物是对方模组的物品）、12 条 `create:crushing`、
  13 个手绘方块模型、2 个手绘物品模型（`electric_hammer` / `fuel_hammer`）。

## 2. 1.20.1 侧的三个 API 坑（移植时踩过）

1. **`RecipeBuilder.save(out, String)` 的 id 必须带命名空间**：传 `"tin_broken_to_ore_gravel"` 会被
   解析成 `minecraft:...`，文件落到 `data/minecraft/recipes/`。而且 id 若等于「产物默认 id」
   （产物物品的注册名）会直接抛 `should remove its 'save' argument`。所以统一走
   `ModRecipes.save(out, name, builder)` 助手：相等用 `save(out)`，否则传 `id(name).toString()`。
2. **配方条件用裸 `conditions` 键**（不是 `forge:conditions`，写错会被静默忽略）；
   `datagen/DatagenRecipe` 负责挂条件数组，同时把 advancement 丢掉。
3. **`runData` 不能装科技模组**：Thermal/Create 挂在 `-PtechMods` 后面；否则 `OreMaterials`
   会解析到对方锭（自有锭不注册 → 生成直接抛异常），而且对方自己的 datagen provider 会把文件
   写进我们的输出树。

## 3. 命令与冒烟

```bash
JAVA_HOME=.../temurin-17.jdk/Contents/Home ./gradlew runData --offline \
  -Dnet.minecraftforge.gradle.check.certs=false
JAVA_HOME=.../temurin-17.jdk/Contents/Home ./gradlew build --offline \
  -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
```

- `runData` 后 `git status` 应干净（`.cache` 被忽略，generated JSON 显式提交）。
- 服务端冒烟看：`[barrel] 12 transforms`、`[sieve] 6 inputs, 87 drops`、`[hammer] 49 recipes`、
  `[crucible] 39 meltables / 9 heat sources`、`[compost] 67 entries`，无 missing tag/loot/recipe。
- 日志里的 `Loaded N recipes` 是**配方类型数**（1.20.1 的 `RecipeManager` 就这么打印），不是配方条数。

## 4. 新会话开场清单

1. 读 `docs/status.md` 的「下一步」与本文。
2. `git status --short` 干净；`./gradlew build` 先绿。
3. 跑一次 `runData`（**不带** `-PtechMods`），确认 `git status` 零 diff。
4. 改配方/模型：优先改 provider，再 `runData`；语义对比脚本 `compare-recipes.py` /
   `compare-models.py` 已在本仓库 `scripts/`。
5. 联调科技模组：`./gradlew runServer -PtechMods` / `runClient -PdevMods -PtechMods`。
