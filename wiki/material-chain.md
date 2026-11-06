# 材料链与联动

> 最后核对：2026-11-06 · 适用：1.21.1 / 1.20.1

这一页解释“模组的矿为什么能/不能进入本模组的矿链”，以及三条自定义路径的边界。
字段级说明看[数据包页面](datapack.md)；这里只讲整合包作者最常问的问题。

## 1. 矿链长什么样

每种材料有自己的一套六件：

| 阶段 | 方块 | 碎料物品 |
|---|---|---|
| 砂砾 | `<id>_ore_gravel` | `<id>_broken` |
| 沙子 | `<id>_ore_sand` | `<id>_crushed` |
| 粉尘 | `<id>_ore_dust` | `<id>_powdered` |

- 筛砂砾 → 破碎；筛沙子 → 粉碎；筛尘土（`exnihilosatellitium:dust`）→ 粉末；
- 4 个碎料 → 1 个对应矿块（2×2）；
- 锤矿砾 → 粉碎（4–6 个），锤矿砂 → 粉末（4–6 个）；矿尘块不再往下锤；
- 熔炼三种矿块 → 该材料的产物（锭/宝石/晶体，由材料决定）。

资源身份属于这条链：不是把别的模组的物品直接贴到筛子上，而是我们注册自己的矿砾/碎料/粉末。

## 2. 当前有哪些材料

当前 26 种：14 种走 `ingot`/`ingots`，12 种走 `gems`。权威列表在
[`materials.json`](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/src/main/resources/exnihilosatellitium/materials.json)；
下面只列“产物由别的模组提供”的部分。

| 来源模组 | 提供的候选产物 | 备注 |
|---|---|---|
| 原版 | 铁、金、铜 | 直接复用原版锭 |
| Mekanism | 锡、铅、锇、铀；氟石 | 两版都接 |
| Thermal 系列 | 锡、铅、银、镍；磷灰石、朱砂、硝石、硫磺、红宝石、蓝宝石 | 当前仅 1.20.1 |
| Create | 锌 | 装了才生效 |
| Immersive Engineering | 铅、银、镍、铝、铀 | 装了才生效 |
| Tinkers' Construct | 钴 | 装了才生效 |
| Tinkers' Thinking | 阿迪特、叶绿素 | 装了才生效 |
| Extreme Reactors | `yellorium`、`anglesite`、`benitoite` | `gems` 门控 |
| Draconic Evolution | `draconium` | 单源：粉碎末地石 1/8 |

- 候选是**有序列表**：多个候选模组同时在场时，按 `materials.json` 的顺序取第一个；
- **`gems` 材料**（氟石、磷灰石、`yellorium`、`draconium` 等；schema 里用 `gems` 字段，
  不一定是宝石）只在候选模组存在时**整条注册**：没装时连矿砾/碎料/粉末都不会出现；
- 用 `ingot`/`ingots` 的材料在候选模组都不在时注册自有锭（`<id>_ingot`），保证无模组也能闭环；
  `gems` 没有自有兜底；
- 换模组会改变“哪些锭存在”：已有世界里的自有锭可能变成未知物品；开世界前定好模组列表。

## 3. 为什么三条路径不能新增材料

物品/方块在**注册期**一次性注册，之后冻结；`materials.json` 在构造期读取，正是因为注册期要决定
“注册哪些物品”。数据包 / KubeJS / CraftTweaker 能改七张表、配方和标签，但**不能凭空创建
本模组的物品 id**。

新增材料需要：改 jar 内 `materials.json` → 跑贴图/锤表/lang 生成器与 `runData` → 重新构建并重启。

当前**没有**自动发现/配置扩展层：表外材料不会因为装了某个模组就自动出现。

## 4. 作者能做什么

### A. 用已有材料（推荐）

如果只是要让某条流程接入本模组已有的物品，不需要改材料表：

- 给自己的筛子输入加 `exnihilosatellitium:<id>_broken` / `_crushed` / `_powdered`；
- 用数据包覆盖熔炼配方，把某个材料的产物换成你想要的锭/宝石（同一材料可能有多条带
  `mod_loaded` 条件的变体，要一起处理）；
- 给本模组的方块/物品补标签；
- 用 KubeJS / CT 的七张表 API 做同样的 add / remove / replace。

可直接复制的示例包：
[examples/datapack](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/tree/main/examples/datapack)。

### B. 新模组有同类产物

如果新模组只是提供了另一种锡锭，最省事的是**让整合包改熔炼结果**，而不是新增材料。
这样物品 id、筛表、JEI 都不变。

注意：本模组的矿链**不消费其他模组的矿石方块**；它消费的是自己的矿砾/沙子/粉尘和碎料，
所以“把某模组的矿石方块映射成我们的材料”不是一个支持的入口。

### C. 确实需要新矿种

把下面信息开 issue / PR：

- 模组 id、矿石方块 id、`c:ores/<id>`（1.20.1 用 `forge:ores/<id>`）标签；
- 产物 id 和它的 `c:ingots/<id>` / `c:gems/<id>` 标签；
- 颜色 `RRGGBB`、tier、rarity、中英文名；
- 是否单源（只从某张筛表或某个方块来）。

我们会加一条 `materials.json` + 贴图/生成器 + 数据生成；作者侧只需要重启。

### D. KubeJS 平行链

KubeJS 可以注册自己的物品做“平行矿链”，但它不属于本模组的材料链：没有六件套、没有
`c:ores/<id>` 标签，也不会自动出现在筛表里；你可以手动把它写进筛表，但那只是普通掉落。
适合完全自定义的整合包，不适合“接入本模组”。

## 5. 常见误解

| 误解 | 实际 |
|---|---|
| 装了 Mekanism，所有 Mek 矿都会进筛子 | 只有 `materials.json` 里列出的材料才会 |
| 数据包能加表外材料 | 不能，注册期已冻结 |
| `gems` 材料没装模组也能筛 | 不能，整条材料不注册 |
| 换模组后旧锭变成未知物品 = bug | 经典行为；开世界前定好模组列表 |
| JEI 里没有 = 没生效 | 专用服客户端读不到服务端数据包，JEI 只显示客户端本地资源；以服务端 `/exnihilo dump` 为准 |

## 6. 相关页面

- [数据包：七张表字段](datapack.md)
- [KubeJS：脚本 API](kubejs.md)
- [CraftTweaker：脚本 API](crafttweaker.md)
- [常见任务](cookbook.md)
- 开发向 schema：[docs/data-format.md §8](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/data-format.md)
- 设计背景：[docs/DESIGN.md §6.4](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/DESIGN.md)
