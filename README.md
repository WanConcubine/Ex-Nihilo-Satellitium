# 无中生有：众星捧月 / Ex Nihilo: Satellitium

> 面向 Minecraft **1.21.1 NeoForge** 的空岛资源获取模组，从零重写（另有 **1.20.1 Forge**
> 分支，玩法内容相同）。
> **非官方作品**，与 Ex Nihilo 系列及其作者没有隶属关系。

“众星捧月”取“群星环绕核心循环”的意思：木桶、筛子、锤子、坩埚都围绕同一条空岛资源链。
英文 *Satellitium* 是拉丁语“随从/卫星群”，对应“众星捧月”，也保留了“环绕、辅助”的意味。

## 这是什么

一个独立实现的 Ex Nihilo 风格模组：从一棵树、一桶水、一格熔岩开始，
通过木桶、筛子、锤子、钩子、坩埚等工具，把空岛一步步变成可发展的基地。

**没有复制任何 GPLv3 分支的代码、贴图、模型或音效。** 设计参考公开玩法资料
（Wiki、MC百科、视频）与 GPLv3 仓库中的功能性思路（内缩桶身、内容物渲染）；
具体实现为本项目原创。部分贴图/模型明确采用 **MIT 许可的 Ex Nihilo: Creatio**
（锤/钩/蚕/尘土/坩埚），逐项登记在 [THIRD-PARTY.md](THIRD-PARTY.md)。

## 下载与安装

| 版本 | 加载器 | 文件 |
|---|---|---|
| 1.21.1 | NeoForge 21.1.x | `exnihilosatellitium-1.21.1-1.2.0.jar`（`./gradlew build` 产物在 `build/libs/`） |
| 1.20.1 | Forge 47.4.x | `exnihilosatellitium-1.20.1-1.2.0.jar`（由 `1.20.1-forge` 分支构建） |

1. 装好对应加载器（NeoForge 21.1.x / Forge 47.4.x，Java 21 / Java 17）。
2. 把 jar 放进 `mods/`。
3. 可选：**JEI**（配方查询）、**Jade**（方块信息）、**Create / Mekanism / Thermal**
   （金属锭与矿石块的互通）、**Applied Energistics 2 + GuideME**（筛沙出赛特斯石英）、
   **Extreme Reactors + ZeroCore**（黄铀 / 铅矾 / 蓝锥矿的矿链）；
   1.20.1 还可选 **BambooMod**（树苗 / 竹笋 / 作物 / 宝石的筛源）；
   1.20.1 还可选 **Immersive Engineering**、**Tinkers' Construct 3 + Mantle** 与
   **工匠巧思（Tinkers' Thinking）**（金属锭/匠魂熔炼/两种矿石）。
   所有联动都是可选的，没装就回退自有物品。

1.2.0 的完整内容与已知限制见 [CHANGELOG.md](CHANGELOG.md)。

## 技术栈

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.265 |
| Java | 21 |
| Gradle | 8.8（Wrapper） |
| 构建插件 | ModDevGradle 2.0.148 |

## 当前进度

- [x] Gradle / NeoForge 骨架，可编译
- [x] 模组元数据、Creative Tab、中英文本地化
- [x] 基础内容：`porcelain`（瓷土，自绘贴图）、`barrel`（橡木木桶）
- [x] M1 木桶：堆肥→泥土（1000 分制，数值以 Omnia 1.10.2 为主、原版 1.7.10 校验；桶保留，右键取出）、1000 mB 流体、比较器、14×16 内缩模型、vanilla-sampled 贴图
- [x] M1 材质变体：云杉/白桦/丛林/金合欢/深色橡木木桶 + 石桶，共享 BE 与行为
- [x] M1.5 桶内转化：水+熔岩→圆石、熔岩+水→黑曜石、木桶拒热（石桶可装熔岩）、接雨水、熔岩掺红石/荧石（巫水/人偶/粘土未做）
- [x] M2 筛子 + 筛网 + 数据驱动掉落（JEI 展示输入/掉落/概率；4 碎块 → 矿方块 → 熔炼成锭）
- [x] 桶的 JEI：桶内转化（`barrel/*.json`）+ 堆肥（`compost/*.json`）两个分类，七种桶都可查
- [x] M3 锤子 + 钩子（6 锤 / 2 钩 / 尘土 / 蚕动画 / 熟蚕 / 粘土 / 锤钩 JEI；待验收）
- [x] M4 坩埚 + 热源（19 熔点 / 9 热源、容量/熔料/热源倍率可配、Creatio 模型、熔料/流体显示、Jade、JEI 两页；待验收）
- [x] M5 矿粒 / 粉碎矿 / 冶炼（熔炼成锭已通；锤子降档已接，待验收）
- [x] M6 瓷桶 / 巫水 / 蚕丝 / 娃娃（服务端已验证，画面待验收）
- [x] M10 自动化：电动 / 内能 × 自动筛 / 自动锤 + 速度/时运插件（服务端已验证，GUI 待验收）
- [x] M7 配置 / 数据生成（JEI 七个分类 + config 已齐；datagen 接管标签 / 掉落表 / 143 条配方 + 74 blockstate + 201 模型；语言仍手写）
- [ ] M8 与「月光魔法」联动
- [x] M9 发布（1.2.0 已打 tag；CurseForge / Modrinth 上传待所有者）

## 文档

- [docs/status.md](docs/status.md) —— 当前状态、验证表、下一步（交接先看这个）
- [docs/tech-debt.md](docs/tech-debt.md) —— 技术债/潜在风险审计（P0–P3）
- [docs/DESIGN.md](docs/DESIGN.md) —— 原则、架构、里程碑、M1–M10 实现记录
- [docs/data-format.md](docs/data-format.md) —— 堆肥/筛子 JSON、材料表、NBT、命令、JEI
- [wiki/](wiki/README.md) —— 玩家向：数据包 / KubeJS / CraftTweaker 自定义配方
- [docs/customization-roadmap.md](docs/customization-roadmap.md) —— 三路径等价路线图（P1–P4、验收标准）
- [docs/textures.md](docs/textures.md) —— 纹理路线、引用规则、检查脚本
- [THIRD-PARTY.md](THIRD-PARTY.md) —— 第三方/参考来源
- [AGENTS.md](AGENTS.md) —— 仓库规则（给协作者/AI）

## 构建

```bash
./gradlew build        # 产物在 build/libs/
./gradlew runClient    # 开发客户端
./gradlew runServer    # 开发服务端
./gradlew runData      # 重生成标签 / 掉落表 / 配方 / blockstate+模型
python3 scripts/check-textures.py   # 确保没有原版贴图占位
```

首次构建会下载 Gradle、NeoForge、Minecraft 依赖并反编译，耗时较长（约 5–15 分钟），
之后会走缓存。

如果构建卡在下载 `maven.neoforged.net`（国内网络），给 Gradle 配代理即可；
本机用的是 `~/.gradle/gradle.properties` 里的 `systemProp.https.proxyHost/Port`，不进仓库。

## 纹理

风格、色板、命名和 AI 政策见 [docs/textures.md](docs/textures.md)。
木桶走 **vanilla-sampled**：桶身用对应木种的木板、内壁/桶底用去皮原木，石桶用石头 +
深板岩瓦；铁箍是外凸几何体。七种材质共用 `barrel_base.json` 的几何，子模型只覆盖贴图。
瓷土和堆肥面是自有 16×16 像素。
`scripts/check-textures.py` 会阻止模型引用原版 **item** 贴图，但允许原版 **block** 贴图。

## 联动

对「月光魔法」（`moonlight_magic`）做**软依赖**占位（实际联动内容在 M8）：

- `mods.toml` 里是 optional dependency（`mandatory=false`），没装月光魔法也能独立运行。
- 代码走 `compat/MoonlightCompat`，只通过 mod id + 注册名查找，不直接引用对方类。
- 联动内容用数据包实现（配方 / 掉落 / 堆肥值；自定义表按 id 覆盖，不解析条件）。

候选内容：月光线做筛网、池尘做稀有筛出物、月光植物进堆肥、本模组矿粒作为月光仪式材料。
详见 [docs/DESIGN.md](docs/DESIGN.md) 第 6 节。

### 支持的模组（supported mods）

材料表的 `ingots` / `gems` 是**有序候选列表**：装了对应模组就用它的锭/矿物熔炼。金属
`ingots` 没装时回退自有锭；`gems` 是**纯模组产物**，只有装了对应模组才注册——没装时
没有自有磷灰石/氟石，也没有矿砾/矿砂/矿尘、碎料、筛出物或熔炼配方。

| 模组 | 覆盖材料 | 说明 |
|---|---|---|
| [Mekanism](https://modrinth.com/mod/mekanism) 1.21.1 | 锡 / 铅 / 锇 / 铀 / 氟石 | 矿砾/矿砂/矿尘熔成 `mekanism:ingot_*` / `mekanism:fluorite_gem`；这些材料不再注册自有兜底 |
| Thermal 系列 | 锡 / 银 / 铅 / 镍 / 磷灰石 / 朱砂 / 硝石 / 硫磺 / 红宝石 / 蓝宝石 | **接在 1.20.1 线**（Thermal 只有 1.20.1）；六种矿物走三档矿链，熔成 `thermal:apatite` / `thermal:ruby` 等 |
| Create | 锌 | 矿砾/矿砂/矿尘熔成 `create:zinc_ingot`；1.20.1 另有 `create:crushing` 桥 |
| Immersive Engineering（1.20.1） | 银 / 铅 / 镍 / 铝 / 铀 | 排在 Mek/Thermal 之后；IE 粉碎机走 `forge:ores/<m>` 标签 |
| Tinkers' Construct（1.20.1） | 钴 | `tconstruct:cobalt_ingot`；只有金属有匠魂熔炼，氟石等矿物不写 |
| [Tinkers' Thinking](https://modrinth.com/mod/tinkers-thinking)（工匠巧思，1.20.1） | 阿迪特 / 叶绿素 | 碎下界岩 1% → `ardite_broken`（没装 TiT 熔成自有阿迪特锭），草方块 1% → `chlorophyll_broken`（熔成 `tinkers_thinking:chlorophyte_compound`）；走本模组三档矿链 |
| [Applied Energistics 2](https://modrinth.com/mod/ae2)（两版） | 赛特斯石英 | 筛沙 1/6 → `ae2:certus_quartz_crystal`、1/12 → `ae2:certus_quartz_dust`；独立 `sieve/sand_ae2.json` 带 `mod` 门补丁，没装时整文件跳过，不注册自有物品 |
| [BambooMod](https://www.mcmod.cn/class/109.html)（仅 1.20.1） | 树苗 / 竹笋 / 作物 / 宝石 | 泥土与草方块各 8 条（4 树苗 2%、竹笋 4%、米种 5%、豆 4%、番茄 4%），砂砾 3 宝石各 1/64；全部 `mod` 门控；堆肥由 leaves/saplings 标签覆盖 |
| [Extreme Reactors](https://modrinth.com/mod/extreme-reactors) + [ZeroCore](https://modrinth.com/mod/zerocore)（两版） | 黄铀 / 铅矾 / 蓝锥矿 | 三条 `gems` 门控材料：三档矿链 → 熔成 `bigreactors:yellorium_ingot` / `anglesite_crystal` / `benitoite_crystal`；没装 ER 时整条不注册（基表不变） |

加模组支持 = 在 `materials.json` 对应材料里加一条 `{"item": "...", "mod": "..."}`，
再跑 `python3 scripts/generate_ore_variants.py` 和 `./gradlew runData`。**对方物品本身就是基础资源时**
（如 AE2 的赛特斯石英），用独立 patch 文件（根级 `"mod"` + `"patch": true`）给筛表追加掉落即可，
见 `sieve/sand_ae2.json`。**开世界前定好模组
列表**：加/减受支持模组会改变哪些兜底物品存在，旧存档里的自有锭/矿物会变成未知物品。

## Jade（玉）

安装 [Jade](https://modrinth.com/mod/jade) 后，准星对准木桶会显示：

- `堆肥：420 / 1000`
- `还需 18 秒`（堆肥计时中）
- `泥土已就绪（右键取出）`（DIRT 状态）
- `水：1000 / 1000 mB`（有流体时）

Jade 是**可选依赖**；没装时插件类不会被加载。API 只用于编译（`compileOnly`），
不会打进我们的 jar。

## JEI

安装 [JEI](https://modrinth.com/mod/jei) 后，会多出三个分类：

- **筛子**：左边是输入（砂砾 / 沙子），右边是全部可能的产物；
  每个产物槽的 tooltip 显示独立概率（例如 `概率：5.56%`）；
  查筛子方块或任意一档筛网都能打开这个分类。
- **桶内转化**：满桶流体 + 加入的流体/物品 → 桶内方块
  （水+熔岩→圆石、熔岩+水→黑曜石、熔岩+红石→地狱岩、熔岩+荧石→末地石）；
  查任意一种桶都能打开这个分类。
- **桶：堆肥**：每个可堆肥物品 → 泥土，tooltip 显示堆肥值（如 `+125 / 1000`）。

JEI 同样是**可选依赖**，只用于编译（`compileOnly`），不会打包；
没装 JEI 时模组独立运行。数据与结算共用 `data/<ns>/sieve/*.json`、
`data/<ns>/barrel/*.json` 和 `data/<ns>/compost/*.json`，改表即改 JEI。

## 数据包

筛子 / 锤子 / 钩子 / 桶转化 / 堆肥 / 坩埚熔点和热源都可以用数据包改：

- 输入支持 `#tag`（如 `"input": "#c:gravels"`），加载时展开成当前注册表里的物品/方块；
- 根级 `"mod": "tconstruct"` 让整张表只在装了某模组时生效；
- `"patch": true` + `"remove"` 可以只改一两条，不用抄全内置文件；
- 改完 `/reload`；`/exnihilo tables`（OP）看当前条数，`/exnihilo sieve <item>` 查单条。

可直接用的示例在 [`examples/datapack/`](examples/datapack/README.md)，
完整语法和优先级见 [`docs/data-format.md`](docs/data-format.md) §14。

面向玩家的完整 wiki（数据包 / KubeJS / CraftTweaker 三条路线）：[`wiki/`](wiki/README.md)。

## 许可证

- **本项目的代码与美术资源：MIT**，详见 [LICENSE](LICENSE)。
- 第三方声明、原版参考说明：见 [THIRD-PARTY.md](THIRD-PARTY.md)。
- Forge MDK 模板部分（构建脚本等）遵循 **LGPL-2.1**，全文见
  [third-party/LGPL-2.1-Forge-MDK.txt](third-party/LGPL-2.1-Forge-MDK.txt)。
- Minecraft / Forge 本身不包含在本仓库中，其版权归各自所有者。

## 致谢

感谢 Ex Nihilo 系列（Erasmus_Crowley、insaneau、Jozufozu、BloodWorkXGaming、SirLyle、
NovaMachina、thedarkcolour、LordDeatHunter 等）为社区提供的玩法范式。本项目仅借鉴
公开的玩法概念，不代表、也不包含他们的实现。
