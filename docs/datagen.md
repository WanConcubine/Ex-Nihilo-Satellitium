# 数据生成（DataGen）与第二批迁移

> 2026-11-02。第一批（标签 + 方块掉落表）、第二批（配方）、第三批（blockstate/model）
> 已在主线完成并提交。
> 本文是**施工单 + 现状记录**：新会话从这里开始，不要重做已完成的批次。
> 1.20.1 分支只做了第一批；第二批配方还没移植过去（见 §5）。

## 1. 现状

- Gradle 任务：`./gradlew runData`（1.20.1：`--offline -Dnet.minecraftforge.gradle.check.certs=false`）
- 输出：`src/generated/resources`（已加入主资源集，提交进仓库；`.cache/` 已 gitignore）
- 参数写死在 `build.gradle` 的 `data` run 里：`--output src/generated/resources --existing src/main/resources`
- Provider（都在 `datagen/`，两版同名类）：

| Provider | 产出 | 批次 |
|---|---|---|
| `ModDataGen` | `GatherDataEvent` 入口 | 一 |
| `ModBlockTags` | 方块标签 | 一 |
| `ModItemTags` | 物品标签（矿石/锭/堆肥） | 一 |
| `ModLootTables` | 69 张自掉表 | 一 |
| `ModRecipes` | 133 条配方 | 二 |
| `ModBlockStates` | 71 个 blockstate + 29 个机械方块模型 | 三 |
| `ModItemModels` | 144 个物品模型 | 三 |

- 旧的手写 `data/**/tags/**`、`data/**/loot_table(s)/**`、133 条手写配方、244 个
  blockstate/model JSON 已删除。
- `scripts/check-data-paths.py` 会同时扫 `src/main/resources` 和 `src/generated/resources`，
  当前输出 `145 recipes, 69 loot tables, 41 tags`（145 = 133 生成 + 12 手写，见 §3）。

## 2. 范围与状态

| 类别 | 迁移前来源 | 数量 | 状态 |
|---|---|---|---|
| 配方 | 手写 JSON | 145 | ✅ 133 条进 `RecipeProvider`；12 条 `*_mekanism` 仍手写 |
| 模型 / blockstate | Python 生成器 | 244 | ✅ 244 条进 provider；13 个手绘几何模型 + 2 个物品几何模型留手写 |
| 语言 | 手写 `en_us` / `zh_cn` | 265 键 | ⬜ 建议**保持 JSON**（双语手维护），除非所有者要求迁 |
| 自定义玩法表 | JSON + reload listener | sieve/hammer/crook/compost/barrel/crucible_* | 不属于原版 datagen，暂不迁 |

## 3. 配方（第二批，已完成 2026-11-02）

### 3.1 覆盖

`ModRecipes extends RecipeProvider`，`buildRecipes(RecipeOutput)` 里按内容分节，全部从
**注册表 + `materials.json`** 推导，不另存一份 id 表：

- `oreChain`：9 种材料 × 3 条 2×2 打包 + 3 条熔炼（共 81 条）。
- `barrels`：12 种桶（11 木 + 石）；`woodenCrucibles`：11 种木坩埚。
- `sieveAndMeshes`：筛子 + 木/线/铁三张网。
- `tools`：5 把锤（形状相同、头部不同）+ 下界合金锤（`SmithingTransformRecipeBuilder`）+ 2 把钩。
- `machines`：四台机器；`upgrades`：速度/时运各三级。
- `dolls`：2 条瓷人偶 + 2 种下界人偶（各含镜像摆法）+ 漂浮/守卫者人偶。
- `porcelain`：瓷土（无序）、未烧制坩埚/瓷桶、两条烧制。
- `silkwormCooking`：熟蚕的熔炉/烟熏/营火三条。

### 3.2 两个刻意的决定

1. **不生成 advancement**。`RecipeBuilder#save` 默认会写
   `data/<ns>/advancement/recipes/**`；本项目从来没发过这些文件，突然多出 133 个会改变
   玩家的配方书解锁行为。`withoutAdvancements(...)` 包一层 `RecipeOutput`，把
   `AdvancementHolder` 丢掉（builder 仍然需要 `unlockedBy(...)`，否则它自己的 `ensureValid` 不通过）。
2. **烹饪配方直接构造 `SmeltingRecipe`/`SmokingRecipe`/`CampfireCookingRecipe`，不用
   `SimpleCookingRecipeBuilder`**。builder 会按产物推导 `food`/`blocks` 书类别，而旧 JSON 没有
   `category` 字段（默认 `misc`）；用 `CookingBookCategory.MISC` 保持行为一致。同理所有合成
   配方用 `RecipeCategory.MISC`（对应 `crafting_book_category: misc`）。

### 3.3 为什么 12 条 `*_mekanism` 还是手写

`tin/lead/osmium/uranium` 的 `*_smelting_mekanism` 产物是 `mekanism:ingot_*`。datagen 里
`ItemStack` 必须指向**已注册**的 `Item`，而 Mekanism 不在 `runData` 的 classpath 上（它是可选
软依赖），无法构造。所以：

- 生成的是**兜底**配方：产物 `exnihilosatellitium:<id>_ingot`，带
  `neoforge:conditions: [{type: neoforge:not, value: {type: neoforge:mod_loaded, modid: mekanism}}]`；
- 12 条 `<id>_ore_{gravel,sand,dust}_smelting_mekanism.json` 留在
  `src/main/resources/data/exnihilosatellitium/recipe/`，条件相反。

`ModRecipes` 的 `smeltingTarget(...)` 在候选存在时**总是**指向自有锭，与 datagen 机器上装了
哪些模组无关；若候选材料的自有锭没注册（即 runData 时该模组在 classpath 上），直接抛异常，
避免静默少生成配方。将来接第二个 supported mod 时，照这个规则再加手写变体。

为此 `OreMaterial` 多了 `ingotCandidates`（原始有序候选列表），`OreMaterials` 把解析拆成
`readIngotCandidates` + `resolveIngot`；`ingot` 仍是**运行时已解析**的结果，注册/标签逻辑不变。
1.20.1 分支移植这批配方时要带上同一个 record 改动。

### 3.4 验收（已做）

1. `./gradlew runData --offline` → `Recipes finished after 34 ms`，133 个文件进
   `src/generated/resources/data/exnihilosatellitium/recipe/`，**没有** `advancement/` 目录。
2. `python3 scripts/compare-recipes.py --expect-old-only '*_mekanism.json'` →
   `145 old, 133 generated, 12 old-only (0 unexpected), 0 new-only, 0 different` / `MATCH`。
   比较的是**语义**：shaped 拆成“去掉空行空列的格子 + 结果 + 条件”，shapeless 排成多重集，
   烹饪比 `ingredient/result/category/experience/cookingtime`，条件按 JSON 规范化；
   `group:""`、`count:1`、`category:"misc"`、`show_notification:true` 这些 codec 默认值不算差异。
3. 删掉 133 条手写 JSON 后再跑 `runData`：`written: 0`（幂等，没有复活旧文件）。
4. `./gradlew build --offline` 通过；`check-textures.py` / `check-core-imports.py`（0 leak）/
   `check-data-paths.py`（145 recipes, 69 loot tables, 41 tags）全过。
5. `runServer`：`Loaded 1423 recipes`（与迁移前一致）、`[debug] our recipes=133`
   （有 Mek 时才会多出 12 条条件配方），无 recipe 解析错误。

`compare-recipes.py` 是可复用的：改配方后先跑 datagen，再跑它；`--old` 指向旧目录即可
（迁移已完成，日常用它对比“生成结果 vs 仓库当前生成结果”意义不大，但换分支/换版本时有用）。
脚本对未知 `type` 会打印 warning 并按原 JSON 比较，不做静默跳过。

## 4. 第三批：模型 / blockstate（已完成 2026-11-02）

### 4.1 覆盖

| Provider | 产出 | 说明 |
|---|---|---|
| `ModBlockStates` | 71 个 `blockstates/*.json` | 70 个单 variant；筛子是 multipart（frame + 三档 mesh） |
| `ModBlockStates` | 29 个 `models/block/*.json` | 12 桶 + 12 木坩埚 + 火/未烧制坩埚 + dust/crushed×2 + 侵染树叶 |
| `ModItemModels` | 144 个 `models/item/*.json` | 70 个方块物品（parent 指向方块模型）+ 66 个 `item/generated` + 8 个 `item/handheld` |

为什么是 144 而不是 146：`electric_hammer` / `fuel_hammer` 的物品模型带完整 `elements`
（图标要有静态锤头），属于手绘几何，保留普通文件。

### 4.2 手绘文件（不生成，别删）

- `models/block/`：`barrel_base`、`crucible_base`、`ore_{gravel,sand,dust}_block`、
  `sieve_frame`、`sieve_mesh_{wood,silk,iron}`、四台机器（`electric_*` / `fuel_*`）。
- `models/item/`：`electric_hammer`、`fuel_hammer`。
- `textures/**` 全部仍由 Python/art 流程出图，datagen 不碰 PNG。

### 4.3 实现要点

- 木头变种只有一张表：`datagen/WoodTypes`（名字、planks/slab/log、log 贴图名）。
  `ModRecipes` 的桶/坩埚配方与 `ModBlockStates` 的模型都从它派生，改一处两边同步。
- 物品模型**从注册表推导**：遍历 `ModBlocks.BLOCKS` 出方块物品（`sieve` → `block/sieve_frame`、
  矿块 → 共享的 `block/ore_<kind>_block`、`witch_water` 无物品跳过），遍历
  `ModItems.ITEMS` + `ModOreItems.ITEMS` 出平面物品（锤/钩走 `item/handheld`，
  瓷桶单独写两层 + `filled` predicate）。新增物品不再需要手写模型。
- 顺序要求：`ModBlockStates` 必须注册在 `ModItemModels` 之前——物品模型用
  `getExistingFile` 校验方块模型父级，`ExistingFileHelper` 靠前一个 provider 的
  `trackGenerated` 才知道它们存在。
- `witch_water` 的 blockstate 直接引用 `minecraft:block/water`（颜色由 FluidType 给），
  datagen 的 ExistingFileHelper 能看到原版资源。

### 4.4 验收（已做）

1. `./gradlew runData --offline` → 244 个新文件，`Block States` / `Item Models` 两个 provider 各 ~25 ms。
2. `python3 scripts/compare-models.py` →
   `259 old, 244 generated, 244 identical` / `MATCH`，旧-only 的 15 个正是 §4.2 的手绘文件。
   blockstate 按「state → 模型映射」比较（variant 键序、multipart 段序都不算差异），
   模型按 parent/textures/overrides 比较。
3. 删掉 244 个手写文件后重跑 `runData`：`written: 0`（幂等）。
4. `check-textures.py` 升级为**同时扫两个资源根**，并新增两条结构性检查：
   blockstate 引用的模型必须存在、模型的 `parent`（本命名空间）必须存在；
   负向测试（故意写坏 `dust` 的 parent 与 blockstate）两条都报错。
5. `build` 通过；`check-core-imports` 0 泄漏；`check-data-paths` 仍是 145/69/41。
6. 三个 Python 生成器改成只写 PNG + 手写表（见 §4.5），原地重跑 `git status` 无资产 diff。

### 4.5 Python 生成器的收尾

- `generate_barrel_variants.py`、`generate_wooden_crucible_variants.py`：**删除**。它们只写 JSON
  （桶/坩埚的 blockstate、model、配方、掉落、标签、lang），现在全归 datagen；lang 键已在
  `lang/*.json` 里，新增木种记得手加两行。
- `generate_ore_variants.py`：只留**纹理拷贝/生成 + 锤表 + lang**；方块/物品模型、blockstate、
  配方、掉落、标签全部删掉。
- `generate_sieve_assets.py`：只留**筛网纹理 + 物品图标 + 筛表合并 + lang**；blockstate/模型删掉。
- `generate_machine_assets.py`：只留 **PNG**（32×32 方块贴图、GUI、图标）；模型/blockstate 删掉。
- 三个脚本都不再需要 `--target`（它们不再写版本相关的 `data/` 路径）。

### 4.6 下一批：语言（建议不动）

265 个键、中英双语、含 Jade/JEI 配置键。`LanguageProvider` 会把它们搬进 Java，维护成本高于收益。
建议保留 `assets/exnihilosatellitium/lang/*.json`，只在新增内容时手加；除非所有者明确要求。

## 5. 1.20.1 分支

- 只做了第一批（`0b7df76`）：标签 + 掉落表。配方、模型仍是手写。
- **`port_data.py` 已改成合并读**：默认 `--source src/main/resources src/generated/resources`
  （后者覆盖前者）。第一批把标签/掉落表、第二批把配方搬进 generated 之后，只读 main 的旧默认
  会漏掉 133 条配方 / 69 张掉落表 / 41 个标签；现在跑出来是 145 recipe / 69 loot / 41 tag / 126 other，
  `check-data-paths.py --target 1.20.1` 通过。
- 移植第二批时注意：
  - 1.20.1 没有 `RecipeOutput`：`buildRecipes(Consumer<FinishedRecipe> out)`，
    `RecipeBuilder#save` 第二参数是 `String` id，`ResourceLocation` 要自己拼；
  - 条件用 `IConditionBuilder` 的 `modLoaded(...)` / `not(...)` + `ConditionalRecipe.builder()`；
  - 目录是 `recipes/`（复数）、`result.item`；`data_target.py` 已能改这些，但 Java provider 要手写一遍；
  - `OreMaterial` 的 `ingotCandidates` 改动要一起带过去。

## 6. 环境与命令

```bash
# 主线
./gradlew runData --offline
./gradlew build --offline
python3 scripts/compare-recipes.py --expect-old-only '*_mekanism.json'
python3 scripts/compare-models.py --expect-old-only 'models/block:barrel_base' ...  # 见 §4.4
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
- **HashCache 坑**：`.cache/` 记的是「provider 产物的哈希」，不是文件内容。手改
  `src/generated/**` 后重跑 `runData` **不会改回来**（哈希与缓存一致 → `written: 0`）；
  要恢复就删掉那个文件（或整个 `.cache/`）再跑。`processResources` 还会因为
  `src/main` 与 `src/generated` 同名资源直接失败，这也是删旧文件必须一次删干净的原因。
- 服务端冒烟：`runServer`，日志看 `Loaded ... recipes`、`[barrel] 12 transforms`、
  `[sieve] 6 inputs, 72 drops`，无 missing tag/loot/recipe。
- 两条线都**未 push**（2026-11-02：main ahead 8，分支 ahead 8）。

## 7. 新会话开场清单

1. 读 `docs/status.md` 的「下一步」与本文。
2. `git status --short` 确认干净；`./gradlew build --offline` 先绿。
3. 跑一次 `runData`，确认 `git status` **零 diff**（证明已迁移的批次稳定）。
4. 已迁移的批次（标签/掉落/配方/模型）都在 §1 的表里；新增内容时优先加进 provider。
5. 语言默认不动（§4.6）：只有所有者明确要求才迁 `LanguageProvider`。

## 8. 版本差异速查（主线 1.21.1）

- 包：`net.neoforged.neoforge.*`；`GatherDataEvent` 在
  `net.neoforged.neoforge.data.event.GatherDataEvent`。
- 标签目录：`data/<ns>/tags/block|item`（单数）；命名空间 `c:`。
- 条件：`neoforge:mod_loaded` / `neoforge:not`；`IConditionBuilder` 在
  `net.neoforged.neoforge.common.conditions`。
- 资源路径：`recipe/`、`loot_table/`、`advancement/`（单数）。
- `RecipeOutput` = `accept(id, recipe, advancement, ICondition...)`；生成条件用
  `out.withConditions(...)`（内部包 `ConditionalRecipeOutput`）。
