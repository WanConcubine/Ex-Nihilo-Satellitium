# 无中生有：众星捧月 / Ex Nihilo: Satellitium

> 面向 Minecraft **1.21.1 NeoForge** 的空岛资源获取模组，从零重写。
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
- [ ] M6 瓷桶 / 巫水 / 蚕丝 / 娃娃（瓷土与熟蚕已在 M3 完成）
- [ ] M7 配置 / 数据生成（JEI 七个分类已完成；config 目前只有坩埚三项，datagen 未做）
- [ ] M8 与「月光魔法」联动

## 文档

- [docs/status.md](docs/status.md) —— 当前状态、验证表、下一步（交接先看这个）
- [docs/tech-debt.md](docs/tech-debt.md) —— 技术债/潜在风险审计（P0–P3）
- [docs/DESIGN.md](docs/DESIGN.md) —— 原则、架构、里程碑、M1–M4 实现记录
- [docs/data-format.md](docs/data-format.md) —— 堆肥/筛子 JSON、材料表、NBT、命令、JEI
- [docs/textures.md](docs/textures.md) —— 纹理路线、引用规则、检查脚本
- [THIRD-PARTY.md](THIRD-PARTY.md) —— 第三方/参考来源
- [AGENTS.md](AGENTS.md) —— 仓库规则（给协作者/AI）

## 构建

```bash
./gradlew build        # 产物在 build/libs/
./gradlew runClient    # 开发客户端
./gradlew runServer    # 开发服务端
python3 scripts/check-textures.py   # 确保没有原版贴图占位
python3 scripts/generate_barrel_variants.py   # 从材质表重生成七种桶的 JSON
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
