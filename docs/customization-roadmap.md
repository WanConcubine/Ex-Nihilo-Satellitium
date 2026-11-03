# 自定义三路径路线图

> 状态：草案 / 决策记录 · 最后核对：2026-11-03
> 目标：**数据包 / KubeJS / CraftTweaker 三条路径最终等价**，而不是“数据包能用，脚本只能改原版配方”。

## 1. 原则

- 自定义是本模组的产品能力，不是附属功能。
- **数据包是规范来源**：任何新表、新字段先有 JSON 形态，再谈脚本入口。
- **脚本不是二等公民**：KJS / CT 一旦声明支持某张表，就必须能完成和数据包相同的
  add / remove / replace / 条件判断，并且共用同一套校验和 reload 语义。
- **没有测试就不算等价**：三条路径用同一份 fixture，reload 后快照必须一致。
- **文档是 Definition of Done**：见 [`wiki/MAINTAINING.md`](../wiki/MAINTAINING.md)。

## 2. 当前能力矩阵（2026-11-03）

| 能力 | 数据包 | KubeJS | CraftTweaker |
|---|---|---|---|
| 筛子 add / remove / replace | ✅ | 只能写 `kubejs/data` JSON | ❌ |
| 锤子 / 钩子 | ✅ | 只能写 JSON | ❌ |
| 桶转化 | ✅ | 只能写 JSON | ❌ |
| 堆肥 | ✅ | JSON + `ServerEvents.tags` | ❌ |
| 坩埚熔点 / 热源 | ✅ | 只能写 JSON | ❌ |
| `#tag` 输入 | ✅ | 只能写 JSON | ❌ |
| `mod` 条件 | ✅ | 只能写 JSON | ❌ |
| 原版合成 / 熔炼 | 只能加，不能删 | ✅ add / remove | ✅ add / remove |
| 注册新物品 / 方块 | ❌ | ✅（KJS 自己的内容） | ❌ |
| 新增材料（`materials.json`） | ❌ | ❌ | ❌ |
| 专用服客户端 JEI 同步 | ❌ | ❌ | ❌ |

**结论：** 目前只有数据包是完整路径；KJS 是“数据包通道 + 原版配方脚本”，
CT 只覆盖原版配方。这是当前事实，wiki 不得写成“三条都支持”。

## 3. 目标矩阵

| 能力 | 数据包 | KubeJS | CraftTweaker |
|---|---|---|---|
| 七张表 add / remove / replace | ✅ | ✅ 脚本 API | ✅ 脚本 API |
| `#tag` / `mod` / patch 语义 | ✅ | ✅ 同一套 | ✅ 同一套 |
| 原版配方 add / remove | 加 ✅ / 删 ❌ | ✅ | ✅ |
| 校验、日志、`/exnihilo` 诊断 | ✅ | ✅ | ✅ |
| 专用服 JEI | ✅（网络同步后） | ✅ | ✅ |
| 新物品 | 不支持（保持） | KJS 自有物品（保持） | 不支持（保持） |
| 新材料 | 不支持（保持） | 不支持（保持） | 不支持（保持） |

“等价”的定义：**同一份 fixture 分别用 JSON / KJS / CT 表达，`/reload` 后
`/exnihilo dump` 的输出逐条相同**（顺序可归一化）。

## 4. 架构方向

### 4.1 一张规范表，三个入口

现在七张表的 loader 各自解析 JSON，直接生成运行时表。为了接脚本，需要抽一层：

```
JSON 文件 ─┐
KJS 事件  ─┼─> TableBuilder（add/remove/replace/validate）─> 快照 ─> 原子替换
CT 脚本   ─┘
```

- `TableBuilder` 放在 `common/table/`，core 层，不 import NeoForge/Forge。
- JSON loader 和脚本入口都只调用 `TableBuilder`，校验逻辑只写一份。
- 快照类型保持现在的 `SieveDrop` / `Rule` / `Recipe` / `Entry` 等不可变结构。
- reload 分三步：读 JSON → 应用脚本 overlay → 原子 swap；失败保留旧快照并打日志。

### 4.2 脚本 overlay 的生命周期

- 脚本贡献不是“直接改全局 map”，而是注册到一个 `CustomizationOverlay`，
  在每次 reload 后重新应用，避免 `/reload` 丢条目或重复叠加。
- overlay 必须可清空（脚本重载时先清空自己的贡献，再重新注册）。
- 冲突策略：后应用者覆盖，并在 `[customize]` 日志里打印 `source + entry + winner`；
  不静默吞掉。

### 4.3 KubeJS

- 一个 `@KubeJSPlugin`：
  - `registerEvents` 注册 server 事件；
  - `registerBindings` 暴露 `SieveEvents` / `BarrelEvents` / `CrucibleEvents` 等。
- 脚本 API 示例（目标形态）：

  ```js
  SieveEvents.add('minecraft:sand', 'minecraft:diamond', 0.02)
  SieveEvents.remove('minecraft:sand', 'minecraft:cactus')
  BarrelEvents.add('minecraft:water', { item: 'minecraft:sugar' }, 'minecraft:dirt')
  ```

- 同时保留 `kubejs/data` JSON 通道；两条通道共用 `TableBuilder`。
- KJS 版本分线（1.21.1 的 `2101.x` / 1.20.1 的 `2001.x`）要在构建里做 compileOnly
  或反射隔离，不能把 KJS 变成硬依赖。

### 4.4 CraftTweaker

- **不强行做 RecipeType**：七张表不是 vanilla `RecipeManager` 的 recipe，
  做 recipe type 成本和版本风险都高。
- 优先做 ZenScript API：`mods.exnihilosatellitium.sieve.add(...)` 这类
  `@ZenRegister` 入口，内部仍然调用 `TableBuilder`。
- 如果以后要 JEI 里显示成 CT recipe type，再单独做 recipe type；
  先保证“能改表”，再保证“能在 CT 的 recipe 浏览器里看到”。
- 目标形态：

  ```zenscript
  mods.exnihilosatellitium.sieve.add(<item:minecraft:sand>, <item:minecraft:diamond>, 0.02);
  mods.exnihilosatellitium.sieve.remove(<item:minecraft:sand>, <item:minecraft:cactus>);
  ```

### 4.5 客户端同步

- 专用服客户端 JEI 现在读不到服务端表；要让三条路径“看起来也等价”，
  需要一个 login/reload 后下发表快照的包，客户端 `clientTable` 用它。
- 表很小（百条级别），可以整表同步；注意分页/版本校验。
- 这是 P4，不是 P1–P3 的前置条件。

### 4.6 文档与守卫

- `scripts/check-wiki.py`：结构守卫（链接、JSON、必需页面、版本矩阵、日期头）。
- P1 之后加 `scripts/check-customization.py`：
  - 用同一份 fixture 分别跑 JSON / KJS / CT（需要测试 harness）；
  - 比较 `/exnihilo dump` 快照。
- 字段稳定后再考虑从 schema 生成 API 参考页；玩家解释仍手写。

## 5. 阶段与验收

### P0 — 数据包 v2（已完成，2026-11-03）

- 七张表支持 `#tag` / `mod` / `patch` / `remove`；
- `TagsUpdatedEvent` 两阶段 reload；
- `/exnihilo` 诊断；
- 示例包 + wiki + 本路线图。

**验收：** 两版 dev server 实测；build + 守卫绿；wiki 结构守卫绿。

### P1 — 规范表 + 脚本 overlay 基座

- 抽 `TableBuilder` / `CustomizationOverlay`；
- JSON loader 改为走 builder；
- 加 `/exnihilo dump <table>`（稳定、可比较的输出）；
- 先用一个测试入口（命令/测试代码）验证 overlay 的 add/remove/replace；
- 不引入 KJS/CT 依赖。

**验收：** JSON + 程序化 overlay 对七张表都能 add/remove/replace；
`/reload` 两次结果一致；失败不破坏旧快照。

### P2 — KubeJS 插件

- `compileOnly` KJS 依赖 + `@KubeJSPlugin`；
- 暴露七张表的 add/remove/replace；
- `kubejs/data` JSON 通道继续可用；
- 文档 `wiki/kubejs.md` 增加脚本示例与能力矩阵更新。

**验收：** 同一 fixture 用 KJS 脚本和 JSON 各跑一遍，`/exnihilo dump` 逐条一致；
KJS 不在时模组正常启动。

### P3 — CraftTweaker API

- `@ZenRegister` 的 ZenScript 入口；
- 七张表 add/remove/replace；
- 不要求 recipe type；JEI 显示可以后置；
- 文档 `wiki/crafttweaker.md` 从“只能原版配方”改为“两条入口”。

**验收：** 同一 fixture 用 CT 脚本和 JSON 各跑一遍，快照一致；
CT 不在时模组正常启动。

### P4 — 客户端同步 + 自动守卫

- 表快照网络包（login + reload）；
- 专用服 JEI 读同步表；
- `check-customization.py` fixture harness；
- 文档生成/校验接入 CI。

**验收：** 专用服客户端 JEI 与服务端 `/exnihilo dump` 一致；
三路径 fixture 在 CI 里跑。

### P5（可选）— 材料表外置

- `materials.json` 支持外部文件 / config；
- 注册期读取，仍然是重启生效，不是 `/reload`；
- 数据包/脚本都不能在运行期新增材料，这一条不为了“等价”而破坏注册期约束。

## 6. 非目标

- 让数据包在运行期注册新物品/方块。
- 让 CT/KJS 直接改 `materials.json` 并热重载。
- 把七张表塞进 vanilla `RecipeManager` 来“兼容”所有配方查看器。
- 为脚本路径牺牲服务端权威性（客户端永远不能决定掉落）。

## 7. 风险

| 风险 | 处理 |
|---|---|
| KJS / CT 版本 API 漂移 | compileOnly + 反射隔离；CI 只测声明支持的版本 |
| 三路径 overlay 冲突 | 统一 `TableBuilder` + 冲突日志 + fixture 对比 |
| CT recipe type 成本高 | 先做 ZenScript API，recipe type 后置 |
| 客户端同步包过大/版本不一致 | 表快照加 schema 版本，登录时校验；必要时分页 |
| 文档再次过时 | `check-wiki.py` + 发布前检查 + 维护契约 |

## 8. 待定问题

1. KJS 和 CT 同时改同一条目时，优先级按加载顺序还是显式 `priority`？
2. `remove` 的粒度：只按产物删，还是支持按 chance/条件删？
3. `/exnihilo dump` 的输出格式要不要稳定成可 diff 的 JSON？
4. CT 是否需要 recipe type 才能在 JEI 里显示，还是接受“脚本 API + 无 JEI 分类”？
5. 客户端同步是否也覆盖 `compost` 的标签交互预测？

这些问题在 P1/P2 开始前由 Owner 定；先记在这里，避免实现时临时拍脑袋。
