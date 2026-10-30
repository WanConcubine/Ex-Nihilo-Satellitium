# 当前状态（交接卡）

> 最后更新：2026-10-30
> 先看这 20 行；细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**主线是 Minecraft 1.21.1 / NeoForge 21.1.x**（1.20.1/Forge 版归档在 tag `legacy/1.20.1-forge`）。
桶（七材质）、筛子、矿物六态（12 材料 × 3 方块 + 3 物品 + 9 锭）已迁移并编译通过；
**M1.5 桶内转化已数据驱动并接入 JEI**，**M2 的筛子 + JEI + 矿物链前段已接上**；
**M3（6 档锤/钩子/尘土/蚕动画）已实现，待进游戏验收**；巫水/蚕丝仍是后续。

## 现在停在哪（2026-10-30）

- **2026-10-30 M3 首批**：2 档钩（木/骨）、`dust` 方块、
  `silkworm`（M6 占位）；10 张 crook 叶表、3 输入 / 44 掉落筛表。
- **2026-10-30 M3 第二批（数据已验证，玩法未验）**：锤子扩到 6 档（木/石/铁/金/钻石/下界合金，
  耐久 64/128/256/32/1561/2031）；**删掉锤子右键**，只保留左键破坏路径；
  锤表扩到 **39 条**：矿砾→粉碎、矿砂→粉末各掉 **4–6 个**，矿物粉尘→方块自身；
  蚕改成 **4 帧蠕动动画**（`silkworm.png` 16×64 + `.mcmeta`，frametime 6；保留 Creatio 原图轮廓，只移动头/中/尾 1px）；
  新增金/钻石/下界合金锤贴图（金/钻石采用 Creatio，下界合金由钻石锤换色）。
  `art/items/tools/restyle.py` 生成全部贴图与预览。
  客户端启动日志已确认 `[hammer] loaded 39 recipes`、`[crook] loaded 10 leaves tables`，
  无 missing model/texture（2026-10-30 23:11）。
- **2026-10-30 M3 贴图返工**：首版自绘验收不通过 → 改用 **Ex Nihilo: Creatio
  1.12.2（MIT）** 的 16×16 PNG（木/石/铁/金/钻石锤、木/骨钩、尘土），
  再按 1.21 风格微调：钩子加 1px 方向光、石/铁锤描边收成
  `#181818`、金锤改暖金、蚕基于原图做 4 帧位移动画、尘土 110 色 → 4 色。原始采用图只读在
  `art/items/tools/creatio/`。尘土方块模型不再引用 `minecraft:block/gray_concrete_powder`；
  6 个工具模型改 `item/handheld`。许可证/来源登记在 [THIRD-PARTY.md](../THIRD-PARTY.md)。
  Sequentia（传承）1.21 是 CC BY-NC-SA 4.0，**不能**采用，只作风格对照。
- **2026-10-30 配方/命名修正**：筛子配方改为任意木板/木台阶/木棍（`#minecraft:planks` + `#minecraft:wooden_slabs` + `#c:rods/wooden`），不再用木网；
  橡木桶显示名改为「橡木木桶」/「Oak Barrel」，避免和原版「木桶 / Barrel」撞名。
- 桶的“能产什么”不再硬编码：`data/exnihilosatellitium/barrel/*.json` 4 条，
  `BarrelRecipes` 同时喂 `BarrelBlockEntity` 和 JEI。
- JEI 现有三个分类：`sieve`（筛子）、`barrel`（桶内转化）、`barrel_compost`（桶：堆肥，65 条）。
  七种桶都是后两个分类的 catalyst；`barrel/`、`compost/` 也镜像到 `assets/` 供客户端读取。
- 顺带修：`compost/default.json` 的 8 条 `#forge:*` 改成 `#c:*`，并将 `minecraft:grass`
  改为 1.21.1 的 `short_grass`；标签/JSON 现在 53 个物品 + 12 个标签完全对齐。
- 已验证：build、纹理检查、服务端启动 + `/reload`（`[barrel] loaded 4 transforms`、
  `[compost] loaded 65 entries`）、RCON 水+熔岩→圆石 / 熔岩+水→黑曜石、
  客户端进世界 `[jei] registered 4 barrel transforms, 65 compost entries`；
  截图目视「桶内转化」1/2 页与「桶：堆肥」1/22 页。
- 未验证：满熔岩桶右键红石/荧石（需要真实玩家）、两个分类的 tooltip 逐条、
  七种桶各自作为 catalyst 逐个点开、木桶拒热手感；玩家破坏返还、管道灌桶、F3+T 分离
  只有代码路径，未玩家实测。
- `b952550`（JEI / 桶内转化 / 技术债修复 + 配方/命名）已推送；
  **M3 全部内容已在 `797c110` 提交并推送（2026-10-30）**。
- 开发服 `run/server.properties` 的 `online-mode` / `enforce-secure-profile` 为 `true`；
  spawn 仍是 `0, 72, 0`；测试用的临时方块、force-load 已清掉，开发服/客户端已停。
- **2026-10-30 技术债 P0/P1 修复**（本轮）：
  - 桶生成器改回 1.21 路径/`result.id`/实际配方，并合并 mineable 标签（不再吞掉筛子）；
  - `grass`→`short_grass`；Sieve/Compost/Moonlight 统一 `getOptional`，坏 id 不再变 AIR；
  - `BarrelBlockEntity.fill` 拒绝 DIRT/`Solid`/堆肥中的桶，管道不能覆盖状态；
  - 筛子/桶被玩家破坏时返还筛网（含耐久）/筛内材料/`Solid`/泥土；
  - 材料表逐条容错、`tier`/rarity 区间校验、空表创意标签回退；文档不再承诺未实现的
    config/自动发现层；
  - 服务端 gameplay 表与客户端 JEI 快照分离（F3+T 不会覆盖服务端规则）；
  - 筛网保留耐久、不可堆叠；筛表变更时已放入材料返还；JEI 超限打警告；CI 加纹理检查。
  - 技术债清单/剩余项：[tech-debt.md](tech-debt.md)。

  **待实测**：玩家破坏返还、管道灌桶、F3+T 分离、右键红石/荧石。

## 交接清单（下次会话先做）

- M3 已在 `797c110` 提交并推送；下一步是进游戏验收“下一步 0”，发现问题再开新提交，
  不要提交 `build/`、`run/`。
- 开发服/客户端已停，临时方块/force-load 已清；下一次先跑
  `./gradlew build` + `python3 scripts/check-textures.py`。
- 设计/数据细节看 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)；
  未修项和优先级看 [tech-debt.md](tech-debt.md)。
- 当前“改完但没实测”的路径：M3 六档锤/钩/尘土、蚕动画、破坏返还、管道灌桶、
  F3+T 分离、红石/荧石掺料。
- `797c110` 已包含：`common/hammer/`、`common/crook/`、`HammerItem`、`CrookItem`、
  `ToolEvents`、`dust` 方块/物品/语言/掉落、`hammer/*.json`（39 条）、`crook/*.json`、
  `sieve/dust.json`、6 种锤 + 2 种钩的合成（金/钻石是工作台，下界合金是锻造台）、
  9 张 Creatio 采用贴图 + 1 张换色下界合金锤 + 蚕动画 + mcmeta、
  `art/items/tools/creatio/` 与 `art/items/tools/restyle.py`、`docs/images/m3-textures.png`。
  改贴图风格请改 `restyle.py` 后重跑，不要直接手改 assets 里的 PNG。
- 已知限制：**右键不再有任何锤击行为**；锤表内方块通过 `HarvestCheck`/`BreakSpeed`
  标记为可收获并给 6.0 速度（不再是镐但也不慢），锤表外方块仍是徒手速度；
  **创造模式破坏也会掉锤表结果**（`BlockEvent.BreakEvent`，方便测试，和经典行为一致）；
  蚕是 M6 占位；锤/钩暂时没有独立 JEI 分类，但它们的合成配方会出现在原版
  crafting / smithing 分类。

## 已完成的里程碑

| 里程碑 | 内容 | 记录 |
|---|---|---|
| M1 | 木桶：堆肥 1000 分制 → 泥土、1000 mB 流体、比较器、七材质、内缩模型 | [DESIGN §8/§10](DESIGN.md) |
| M1.5 | 水/熔岩凝固、木桶限温 433 K、接雨水、熔岩掺红石/荧石；转化表已 JSON 化 | [DESIGN §13/§14](DESIGN.md) |
| M2 | 筛子 + 三档筛网 + 数据驱动掉落 + JEI；4 碎块 → 矿方块 → 熔炼成锭 | [DESIGN §12](DESIGN.md) |
| 桶 JEI | `barrel` / `barrel_compost` 两个分类，读同一张 `BarrelRecipes` / `CompostRegistry` | [DESIGN §14](DESIGN.md) |
| M3 | 6 锤 / 2 钩 / `dust` / 蚕动画 / 10 张叶表 / 39 条锤表 / 尘土筛料 | 🚧 待验收（[DESIGN §15](DESIGN.md)） |

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-30：`BUILD SUCCESSFUL`；JEI 依赖走 `maven.blamejared.com` |
| `python3 scripts/check-textures.py` | ✅ | 七种子模型只引用 vanilla **block** 贴图；9 张自有锭贴图均为 16×16；蚕动画按 16×128 + mcmeta 放行 |
| 服务端启动 / `/reload` | ✅ | `[barrel] loaded 4 transforms (2 fluid, 2 item)`、`[compost] loaded 65 entries`；`/reload` 后一致 |
| 七种桶 BE | ✅ | RCON：桶堆肥→DIRT、`DirtReady=1b`；七种材质共享同一 BE 注册表 |
| 堆肥容量/刻度 | ✅ | `data get` 读回 `Compost=1000`、`CompostScale=1000`；满后 1000 tick → DIRT |
| 堆肥数值表 | ✅ | `[compost] loaded 65 entries`；53 物品 + 12 标签与 `compostable` 完全对齐（grass→short_grass） |
| 石桶流体 NBT | ✅ | RCON 写 `{Fluid:{id:"minecraft:lava",amount:1000}}`，读回 `minecraft:lava` / `1000` |
| 掉落表 | ✅ | `/loot spawn ... exnihilosatellitium:blocks/{spruce,stone}_barrel` 返回对应物品 |
| 资源重载 | ✅ | `[debug] our recipes=83`、`[sieve] loaded 2 inputs, 29 drops`、`[barrel] loaded 4 transforms` |
| 客户端资源加载 | ✅ | 进单人世界无 missing model / missing texture；`[jei] registered 4 barrel transforms, 65 compost entries` |
| 实际进游戏目视 | ⚠️ | 2026-10-30 截了 JEI 的桶内转化/堆肥页；桶/筛子比例、箍、物品栏图标仍未验收 |
| 右键取泥土 | ⚠️ | 逻辑未改，仍未自动化（需要真实玩家） |
| 逐项右键堆肥 | ⚠️ | JSON/标签已对齐，但没有玩家逐项右键验证点数 |
| 比较器 | ⚠️ | 逻辑未改，仍未实测 |
| Jade tooltip | ⚠️ | Jade 15 已加载；熔炉正常，**木桶/筛子 tooltip 仍未目视** |
| 合成配方 | ⚠️ | 83 个 JSON 全部加载（含 36 个 2×2 与 36 个熔炼）；没有玩家实测合成格 |
| 筛子配方 | ✅ | 2026-10-30 作者验收可提交；`x x / xyx / z z`，`x=#minecraft:planks`、`y=#minecraft:wooden_slabs`、`z=#c:rods/wooden` |
| 橡木桶显示名 | ✅ | 2026-10-30 作者验收可提交；`block.exnihilosatellitium.barrel` =「橡木木桶」/「Oak Barrel」 |
| 桶：堆肥→泥土 | ✅ | 2026-10-29 实测：苹果 +100 / 仙人掌 +100 / 树叶 +125，满 1000 后 `dirt=true` |
| 桶：流体装填 | ✅ | 2026-10-29 实测：岩浆桶倒入成功 |
| 桶：水 + 熔岩 → 圆石 | ✅ | RCON（先 forceload）：水桶上方放熔岩源，BE 变 `Solid: minecraft:cobblestone`、流体清空 |
| 桶：熔岩 + 水 → 黑曜石 | ✅ | RCON：石桶装熔岩、上方放水，BE 变 `Solid: minecraft:obsidian` |
| 桶：接雨水 | ✅ | `/weather rain` 下 12 秒积水 80 mB（降雨 0.5，20 mB/2 秒） |
| 桶：木桶拒热 | ⚠️ | `tank.fill` 按 433 K 拒绝；消息/手感未玩家实测（dispenser 不走 capability） |
| 桶：熔岩掺红石/荧石 | ⚠️ | 已进 `barrel/*.json`（红石→地狱岩、荧石→末地石）；未玩家实测 |
| 桶：转化表 JSON | ✅ | 4 条加载；BE 与 JEI 共用 `BarrelRecipes`；RCON 两条流体反应通过 |
| 桶 JEI：转化分类 | ✅ | `[jei] registered 4 barrel transforms`；截图目视 1/2 页（3 条/页），流体槽/箭头正常；[自截图](images/jei-barrel-transform.png) |
| 桶 JEI：堆肥分类 | ✅ | 65 条堆肥 → 泥土；截图目视 1/22 页；空条目已修 |
| 桶 JEI：catalyst | ⚠️ | 七种桶都注册；左侧能看到多个桶 tab，**未逐个点开验证** |
| 筛子方块渲染 | ✅ | 作者确认框体可见（修 render shape 之后） |
| 料层/进度渲染 | ⚠️ | 已修位置（网面 0.70 起）与方向（越筛越薄），未见作者确认 |
| 筛子 Jade | ⚠️ | provider 已注册并加载，显示内容待作者确认 |
| 筛子（1.21.1） | ✅ | 方块 + 三档网 + 数据表；作者已实际筛出产物；`[sieve] loaded 2 inputs, 29 drops` |
| 筛子 JEI 分类 | ✅ | 7×3 + 166×60，两条筛料 1/1；日志 `[jei] registered 2 sieve recipes`；[自截图](images/jei-sieve.png) |
| 矿物 4 块 → 矿方块 | ⚠️ | 36 个 2×2 配方 JSON 加载（`our recipes=83`）；未玩家实测合成格 |
| 矿方块 → 锭（熔炼） | ✅ | RCON 熔炉：`iron_ore_gravel → minecraft:iron_ingot`、`tin_ore_gravel → tin_ingot` |
| 自有锭（9 种） | ⚠️ | `tin_ingot` 等已注册、可熔炼、创意标签可查；未目视贴图/未玩家实测 |
| 矿物六态注册 | ✅ | 客户端 `/give` 与创意标签可见 72 条；`[materials] loaded 12 entries` |
| 矿物六态纹理 | ✅ | 作者目视通过：原版底 + 遮罩 + 罩色，物品栏为 3D 方块 |
| 矿物链配方 | 🚧 | 4 块 → 矿方块 → 熔炼成锭已通；M3 锤表已接入矿砾→粉碎（4–6）、矿砂→粉末（4–6）、粉尘→自身，待玩家实测 |
| M3 数据表 | ✅ | 39 条 hammer JSON（3 基础 + 24 条 4–6 + 12 条粉尘自身）、10 张 crook、3 输入 / 44 掉落筛表；客户端启动日志 `[hammer] loaded 39 recipes`、`[crook] loaded 10 leaves tables`（2026-10-30 23:11），JSON/ID 也已脚本校验 |
| 锤子（6 档） | ⚠️ | 木/石/铁/金/钻石/下界合金 + 39 条 crush 表；**无右键**；耐久 64/128/256/32/1561/2031；未玩家实测 |
| 锤子左键路径 | ⚠️ | `HarvestCheck` 标记可收获 + `BreakSpeed` 6.0；生存走 `BlockDropsEvent` 并扣 1 耐久，创造走 `BlockEvent.BreakEvent` 补掉落；**未实测** |
| 金/钻石锤配方 | ⚠️ | 3 金锭 / 3 钻石 + 2 木棍；JSON 已写，未玩家实测合成格 |
| 下界合金锤 | ⚠️ | 锻造台：模板 + 钻石锤 + 下界合金锭；JSON 已写，未玩家实测 |
| 钩子（2 档） | ⚠️ | 木/骨钩 + 10 张叶表；骨钩概率 ×1.5；贴图已换 Creatio，未玩家实测 |
| 尘土筛料 | ⚠️ | `sieve/dust.json` 15 条（12 粉末 + 红石/荧石/火药）；JEI `3 sieve recipes`，未玩家筛过 |
| 尘土方块 | ⚠️ | `dust` 方块/物品/掉落/铲标签已注册；贴图改用 `block/dust.png`（Creatio），未进游戏目视 |
| 蚕 | ⚠️ | `silkworm` 物品/掉落已注册；贴图是 4 帧蠕动动画（16×64 + mcmeta，原图 + 3 个位移帧）；**M6 才接丝线链** |
| M3 贴图来源 | ✅ | 9 张 Creatio（MIT）采用图 + 1 张换色下界合金锤 + 蚕原图的 4 帧位移动画；`check-textures.py` 通过（含动画尺寸检查）；[预览](images/m3-textures.png)；尚待进游戏 4× 目视 |
| 铁筛网配方 | ⚠️ | 3×3 铁栏杆；JSON 已加载，未玩家实测合成格 |
| 矿物标签 | ⚠️ | 12 材料 × `c:ores` block/item + `c:raw_materials`；未用其它模组机器实测 |
| 改名完整性 | ✅ | `git grep` 无旧 id/旧类名；jar 内 `mods.toml` 为 `exnihilosatellitium` / `Ex Nihilo: Satellitium` |

## 下一步（按价值排序）

0. **M3 验收（当前优先级）**：
   - 6 档锤合成 + 耐久（金锤 32 应最快坏；下界合金锻造台配方）；
   - **左键**锤圆石→砂砾→沙子→尘土；确认**右键已无反应**；
   - 锤矿砾→粉碎、矿砂→粉末（各掉 4–6 个）；锤矿物粉尘→掉方块自身；
   - 把尘土放进筛子，确认 `dust` 输入和 15 条掉落；
   - 木/骨钩合成；钩树叶看树苗/苹果/蚕追加掉落；
   - 看尘土方块纹理/掉落、蚕物品是否可拾取、**蚕动画是否在物品栏里动**；
   - **新贴图目视**：进游戏 4× 看 6 种锤/2 种钩手持角度、尘土方块接缝。
1. **补上一批 P0/P1 修复的实测**：玩家破坏筛/桶返还、管道灌桶状态互斥、F3+T 后
   JEI/服务端表分离、右键红石/荧石；剩余 P1 见 [tech-debt.md](tech-debt.md)。
2. **进游戏补 5 分钟旧内容验收**：七种桶外观/堆肥/Jade、矿砾 4 块合成、筛子配方与木桶命名。
3. **P1 硬化（半天）**：`Fluid` 匹配（still/flowing）、`ingot` 存在性校验、生成器旧文件清理、
   CI 客户端 smoke；对应 [tech-debt.md](tech-debt.md) #17/#21/#24/#25。
4. **M1.5 补完**：巫水（水 + 下方菌丝）→ 灵魂沙/末影人；生物人偶；粘土（等 M3 尘土）；
   以及玩家实测木桶拒热、红石/荧石掺料。
5. **M2 余项**：补沙子的宝石/杂物掉落；`dust` 通用方块已由 M3 接上。

## 已拍板的决策

- **项目名**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`，
  Java 包 `com.kaifengcrt.exnihilosatellitium`，资源命名空间 `exnihilosatellitium`（2026-10-28）。
- **许可证 MIT**（2026-10-27 从 ARR 转）；Forge MDK 仍 LGPL-2.1，见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**（2026-10-30 修正）：注册只认 jar 内材料表；表内材料有完整自有链
  （矿砾 → 破碎 → 粉碎 → 粉末 → 锭）；表外材料不自动发现、不生成骨架行；新增材料要改表、
  重跑生成脚本并重启。详见 [DESIGN.md](DESIGN.md) §6.1 第 4 条、§6.4；
  表结构见 [data-format.md](data-format.md) §8。
- **方块走 vanilla-sampled**：模型直接引用 `minecraft:block/*`，不复制/不打包原版贴图。
  七种桶共用 `barrel_base.json` 几何；木桶侧重木种木板 + 去皮原木内壁 + `gray_concrete` 深灰箍，
  石桶用 `stone` + `smooth_stone` + `deepslate_tiles`。
- **物品与特殊表面自有像素**：瓷土、堆肥面；不把 AI 生图放进 release。
- **原版 Ex Nihilo 与 Omnia 都是 GPLv3**：只对照玩法数值/行为，不复制代码、JSON、贴图、模型、
  音效；`reference/` 和本地 Omnia jar 只读、gitignore、不随包分发。复制 GPL 资源就必须改 GPLv3。
- **木桶 14×16×14 内缩**；物品模型用开口 block model + `display` 变换（没有封闭盖）。
- **七种桶共享一个 BE 类型**；行为/NBT/Jade 基本一致。唯一差异化：**石桶不限温**，木桶上限 433 K。
- **堆肥用 1000 分制**：以 **Omnia 1.10.2** 的表为骨架，原版 1.7.10 校验比例；
  Sequentia 只借 1000 刻度，不采用它的重平衡。树苗/树叶 125、面包 125、花/蘑菇 100、
  腐肉/蜘蛛眼/小麦 85、肉/南瓜派 175、鱼 120、瓜块 150、胡萝卜等 80。
  满 1000 后 1000 tick（50 秒）变 DIRT。老存档按 `CompostScale` 缺失自动 ×10 迁移。
- **桶转化走 JSON**（2026-10-30）：`base`（满桶流体）+ `added`（流体或物品）+ `result`；
  BE 只保留“满桶”规则，不再写 if。JEI 读同一张表，避免结算/展示漂移。
- **Jade 是可选依赖**：`compileOnly` API，不打包；没装时插件类不加载。
- **月光魔法是可选软依赖**：`moonlight_magic`，目前只占位，没有实际联动内容。

## 踩过的坑（改代码前先看）

- **本地构建需要访问 `maven.neoforged.net`**：国内网络要挂代理（见 [migration-neoforge.md](migration-neoforge.md)）。
- **JEI/界面类改动先上原生控件**：筛子页曾因为自绘背景 + 反复调高度返工一整晚；
  正确基线是 JEI 原生 `setStandardSlotBackground()`，复盘见
  [incidents/2026-10-29-jei-layout.md](incidents/2026-10-29-jei-layout.md)。
- **直连 TLS 失败（libraries.minecraft.net / repo.maven TLS handshake）**：缓存已齐时用 `--offline`。
- **NeoForge 21.1 的 `FluidTank` NBT 是嵌套 `{Fluid:{id,amount}}`**，不是旧 1.20.1 的
  顶层 `FluidName` / `Amount`。RCON 测试/数据包写错格式会被静默忽略（BE 看起来是空桶）。
- **1.21.1 没有 `#forge:*` 物品标签**：NeoForge 用 `#c:crops/*`、`#c:seeds`、`#c:eggs`、
  `#c:strings`。`#minecraft:crops` / `#minecraft:seeds` 也不是物品标签；
  `minecraft:carrots/potatoes` 是方块 id，物品是 `carrot` / `potato`。
- **堆肥标签和 JSON 必须同步**：服务端也先查 `#exnihilosatellitium:compostable`；
  标签有、JSON 没有 = 按 1 点处理（`#forge:*` 这次就是这么坏的）。
- **内缩模型的外侧面不能加 `cullface`**：墙在 z=1/x=1，不在方块边界，加了会整面被剔除。
- **堆肥刻度从 100 改成 1000**：老存档没有 `CompostScale`，`load()` 会 ×10 并缩放计时。
- **共享 BE 类型要列全七种方块**：`BlockEntityType.Builder.of` 漏一个，放置后 BE 不创建 / ticker 为 null。
- **旧配方 `#minecraft:planks` 必须删掉**：它会和具体木种配方同时匹配，产物不可预测。
- **子模型不要直接当方块模型用**：`barrel_base.json` 没有 `textures`，只作为 parent。
- **Jade / 月光魔法生产 jar 不能直接丢 `run/mods` 做 dev 测试**：SRG 名会 `NoSuchFieldError`；
  联调要正式服务端/客户端，或用 deobf 依赖。
- **JEI 的 Modrinth maven 会拿到 Fabric jar**：改用 `maven.blamejared.com` 的
  `mezz.jei:jei-1.21.1-neoforge`；JEI/Jade 都是 `compileOnly`，不打包。
- **数据表要在客户端也加载，而且客户端只认 `assets/`**：`AddReloadListenerEvent` 只覆盖服务端；
  `RegisterClientReloadListenersEvent` 的 `ResourceManager` 是 CLIENT_RESOURCES，
  `listResources("barrel", ...)` 只会找 `assets/<ns>/barrel`。所以 `processResources`
  把 `barrel/`、`compost/`、`sieve/` 镜像到 `assets/`；仓库里仍只有 data 一份。
- **JEI 小分类会自动打包**：116×26 的桶页一页放 3 条，所以 4 条转化是 1/2 页而不是 4 页；
  改高度/槽位时先按这个预期验收。
- **RCON 测桶前先 `forceload add`**：专用服空服时区块可能不 tick，`data merge` 看起来写进去了
  但 `Solid` / `DirtReady` 永远不出现。测完 `forceload remove`。
- **RCON 测熔炉要自己写 `CookTimeTotal`**：`data merge` 绕过 `setItem()`，
  `cookingTotalTime` 会是 0（进度永远等不到）。
- **`/tick step` 仍按 20 TPS 走**：`step 250` 大约要 12.5 秒；中途 `/tick unfreeze` 会取消剩余步进。

## 常用命令

```bash
./gradlew build
python3 scripts/check-textures.py
python3 scripts/generate_barrel_variants.py          # 从材质表重生成七种桶的 JSON
python3 scripts/generate_ore_variants.py             # 矿物链资源 + 2×2 合成 + 熔炼配方 + 自有锭
./gradlew runServer   # RCON：localhost:25575 / eni
# 注意：run/ 被 gitignore；fresh clone 需要在 run/server.properties 自己开 RCON
./gradlew runClient
```

> `generate_ore_variants.py` 依赖 Pillow；本机系统 Python 没有时用 DSH 自带 Python 跑。

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java           # 入口
├── registry/                       # Block / Item / BlockEntity / CreativeTab
├── common/
│   ├── block/BarrelBlock.java
│   ├── block/SieveBlock.java
│   ├── blockentity/BarrelBlockEntity.java
│   ├── blockentity/SieveBlockEntity.java
│   ├── barrel/                     # BarrelRecipes + reload listener（barrel/*.json）
│   ├── compost/                    # CompostRegistry + reload listener
│   ├── sieve/                      # SieveRegistry + reload listener + SieveDrop
│   ├── material/                   # materials.json 读取（注册期）
│   ├── event/CommonEvents.java
│   └── tag/ModTags.java
├── client/                         # BarrelRenderer / SieveRenderer / ClientEvents
└── compat/
    ├── MoonlightCompat.java
    ├── jade/                       # 可选 HUD
    └── jei/                        # 可选 JEI：Sieve / Barrel / Compost 分类
```

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。
JEI 截图在 `docs/images/`（`jei-sieve.png`、`jei-barrel-transform.png`）。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑/实现记录”，data-format 写“JSON/NBT/命令”，
  textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录；不要把同一份进度抄到多处。
- 新的“本轮”记录先加在 [DESIGN.md](DESIGN.md) 对应章节，本文件只留一行摘要。
