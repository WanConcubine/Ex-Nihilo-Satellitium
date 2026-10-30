# Third-party notices

本项目（Ex Nihilo: Satellitium / 无中生有：众星捧月）的代码与美术资源采用 MIT，
见 [LICENSE](LICENSE)。以下是第三方组件与参考来源。

## Forge MDK

`build.gradle`、Gradle Wrapper、`settings.gradle` 以及它们派生自的示例源码，
来自 Forge MDK，按 **LGPL-2.1** 授权。全文见
[third-party/LGPL-2.1-Forge-MDK.txt](third-party/LGPL-2.1-Forge-MDK.txt)。

## Minecraft / Forge

Minecraft 是 Mojang Studios 的商标。Minecraft 与 Forge 不随本仓库分发；
使用本项目需要合法的 Minecraft 与 Forge 安装。

木桶等方块模型会在运行时引用原版方块贴图（`oak_planks`、`spruce_planks`、
`gray_concrete`）；这些贴图不随本项目的 jar 分发。

## Ex Nihilo 系列（参考，不含代码/资源）

本项目是独立实现，**没有复制**任何现有 Ex Nihilo 分支的代码、贴图、模型或音效。

设计阶段以只读方式参考了原版 Ex Nihilo（Erasmus_Crowley，GPLv3）的公开仓库，
用于理解桶类方块的**功能性思路**（例如内缩的桶身轮廓、在桶内渲染内容物）。
没有复制它的 PNG；木桶采用同样的“采样自原版”思路，直接引用 vanilla block 贴图。
这些属于玩法/功能层面的参考；具体代码、模型几何与像素图均由本项目独立编写。
原版仓库仅作本地只读参考，不随本项目分发。

原版地址：<https://bitbucket.org/Erasmus_Crowley/ex-nihilo>

堆肥数值以原版 1.7.10 的 `CompostRegistry` 和
[Ex Nihilo Omnia 1.10.2](https://github.com/Jozufozu/ExNihiloOmnia/tree/1.10)
的 `CompostRegistry.registerVanillaRecipes()` 为主表（Omnia 1.12 的 `registries/composting/*.json`
用于交叉核对），[Ex Nihilo: Sequentia 1.20.1](https://github.com/NovaMachina-Mods/ExNihiloSequentia)
只用于确认 1000 分制刻度和 1.20 新物品的分类（只读数据参考，未复制代码）。
数值属于玩法数据，本项目按 1000 分制重新写成 `compost/default.json`；
代码、JSON 结构和贴图均为自写。

**许可证注意**：原版 Ex Nihilo 和 Ex Nihilo Omnia（CurseForge/mcmod 标注 **GPLv3**）都是
GPLv3。本项目只把它们当作**玩法数值/行为**的只读参考，没有复制代码、JSON 文件、贴图、
模型或音效，因此本项目仍可按 MIT 发布。如果将来要复制 Omnia/原版的任何代码或美术资源，
就必须把相应部分及其衍生作品按 GPLv3 处理（通常意味着整个项目改为 GPLv3 并随附源码），
不能继续只标 MIT。`reference/` 和本地 Omnia jar 都在 `.gitignore` 里，不提交、不随包分发。

具体到 GPLv3 条文：**§5c** 要求基于该作品的整个作品按 GPLv3 授权给任何拿到副本的人；
**§6** 要求分发 object code 时同时提供 Corresponding Source；**§4/§5** 要求保留许可证/版权声明，
修改过的版本要注明修改和日期。MIT 代码可以并入 GPLv3 作品，但反过来不行——所以“MIT 项目里
混入 GPLv3 代码，再整体只标 MIT”是走不通的。只有将来真的引入 GPLv3 组件或决定转协议时，
才需要把 GPLv3 全文放进仓库并逐项满足这些条件。

## 美术

`src/main/resources/assets/exnihilosatellitium/textures/` 下的贴图由本项目的
像素绘制脚本（`art/barrel/generate.py`、`art/items/ore_pieces/generate.py`、
`art/items/ingots/generate.py`）逐像素生成，按 MIT 授权。
没有使用 AI 生图，也没有直接引用原版或其他模组的贴图。

## 名称

“Ex Nihilo”是拉丁语短语；“无中生有”是中文成语。本项目名称仅用于说明玩法渊源，
不代表、也不隶属于 Ex Nihilo 系列或其作者。

## Jade（可选，不打包）

Jade 是可选依赖，**不随本项目分发**。本项目只以 `compileOnly` 方式引用其 API
（`maven.modrinth:jade:15.10.6+neoforge`）来编译 HUD 插件；用户需要自行安装 Jade。
Jade 的许可证与版权归其作者所有，见其项目页。

## JEI（可选，不打包）

JEI 是可选依赖，**不随本项目分发**。本项目只以 `compileOnly` 方式引用其 API
（`mezz.jei:jei-1.21.1-neoforge:19.27.0.340`，来自 `maven.blamejared.com`）来编译
筛子分类插件；用户需要自行安装 JEI。JEI 的许可证与版权归其作者所有，见其项目页。
构建时不会把 JEI 的类或资源打进本项目的 jar。
