# 当前状态（交接卡）

> 最后更新：2026-10-30
> 先看这 20 行；细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**主线是 Minecraft 1.21.1 / NeoForge 21.1.x**（1.20.1/Forge 版归档在 tag `legacy/1.20.1-forge`）。
桶（七材质）、筛子、矿物六态（12 材料 × 3 方块 + 3 物品 + 9 锭）已迁移并编译通过；
**M1.5 桶内转化已数据驱动并接入 JEI**，**M2 的筛子 + JEI + 矿物链前段已接上**；
**M3 锤子/钩子、巫水、尘土筛料还没开始**。

## 现在停在哪（2026-10-30）

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
- 本批（自 10-29 起的 JEI / 桶内转化 / 桶 JEI / 技术债修复 + 配方/命名修正）已于
  2026-10-30 提交，工作树干净；提交说明见 `git log`。
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

- 本批已提交（见 `git log`），工作树干净；后续改动仍按“下一步 0”先验收，再提交，
  不要提交 `build/`、`run/`。
- 开发服/客户端已停，临时方块/force-load 已清；下一次先跑
  `./gradlew build` + `python3 scripts/check-textures.py`。
- 设计/数据细节看 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)；
  未修项和优先级看 [tech-debt.md](tech-debt.md)。
- 当前“改完但没实测”的路径：破坏返还、管道灌桶、F3+T 分离、红石/荧石掺料。

## 已完成的里程碑

| 里程碑 | 内容 | 记录 |
|---|---|---|
| M1 | 木桶：堆肥 1000 分制 → 泥土、1000 mB 流体、比较器、七材质、内缩模型 | [DESIGN §8/§10](DESIGN.md) |
| M1.5 | 水/熔岩凝固、木桶限温 433 K、接雨水、熔岩掺红石/荧石；转化表已 JSON 化 | [DESIGN §13/§14](DESIGN.md) |
| M2 | 筛子 + 三档筛网 + 数据驱动掉落 + JEI；4 碎块 → 矿方块 → 熔炼成锭 | [DESIGN §12](DESIGN.md) |
| 桶 JEI | `barrel` / `barrel_compost` 两个分类，读同一张 `BarrelRecipes` / `CompostRegistry` | [DESIGN §14](DESIGN.md) |

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-30：`BUILD SUCCESSFUL`；JEI 依赖走 `maven.blamejared.com` |
| `python3 scripts/check-textures.py` | ✅ | 七种子模型只引用 vanilla **block** 贴图；9 张自有锭贴图均为 16×16 |
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
| 矿物链配方 | 🚧 | 4 块 → 矿方块 → 熔炼成锭已通；锤子降档（矿砾→粉碎、矿砂→粉末）等 M3 |
| 铁筛网配方 | ⚠️ | 3×3 铁栏杆；JSON 已加载，未玩家实测合成格 |
| 矿物标签 | ⚠️ | 12 材料 × `c:ores` block/item + `c:raw_materials`；未用其它模组机器实测 |
| 改名完整性 | ✅ | `git grep` 无旧 id/旧类名；jar 内 `mods.toml` 为 `exnihilosatellitium` / `Ex Nihilo: Satellitium` |

## 下一步（按价值排序）

0. **补 P0/P1 修复的实测**：玩家破坏筛/桶的返还、管道灌桶状态互斥、F3+T 后 JEI/服务端表
   分离、右键红石/荧石；剩余 P1（同步频率、`Fluid` 精确匹配、生成器旧文件清理）见
   [tech-debt.md](tech-debt.md)。
1. **进游戏补 5 分钟验收**（最便宜）：
   - 放七种桶，看颜色/箍/内壁是否可区分，比例是否一致；物品栏图标是否是空心 3D 桶；
   - 塞树苗看堆肥 box、粒子、50 秒颜色过渡；DIRT 后右键取泥土；
   - 看木桶/筛子的 Jade tooltip；
   - 用 4 个破碎矿合成一次矿砾方块，确认合成格与 JEI 显示；
   - 用任意木板 + 任意木台阶 + 任意木棍合成筛子，确认 JEI 里筛子配方没有筛网；
   - 看橡木桶物品名是否为「橡木木桶」，不是原版「木桶」；
   - JEI 搜「桶」：确认「桶内转化」的 tooltip / 红石掺料提示 / 「桶：堆肥」分值；
     再让玩家右键满熔岩桶放红石、荧石，确认地狱岩/末地石。
2. **P1 硬化（半天）**：`Fluid` 匹配（still/flowing）、`ingot` 存在性校验、生成器旧文件清理、
   CI 客户端 smoke；对应 [tech-debt.md](tech-debt.md) #17/#21/#24/#25。
3. **M3 锤子 + 钩子**：锤碎圆石→砂砾→沙子→尘土，锤矿砾→粉碎、矿砂→粉末，
   接上尘土筛料；钩树叶出树苗/蚕。
4. **M1.5 补完**：巫水（水 + 下方菌丝）→ 灵魂沙/末影人；生物人偶；粘土（等 M3 尘土）；
   以及玩家实测木桶拒热、红石/荧石掺料。
5. **M2 余项**：给 `dust` 一个通用尘土方块，补沙子的宝石/杂物掉落。

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
