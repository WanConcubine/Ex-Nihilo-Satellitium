# KubeJS：用数据包通道 + 脚本

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1

KubeJS 不是一个“七张表的脚本 API”。它在本模组里能做的事分两类：

| 你想做的 | KubeJS 怎么做 | 说明 |
|---|---|---|
| 改筛子 / 锤子 / 钩子 / 桶 / 堆肥 / 坩埚 | **把同样的 JSON 放进 `kubejs/data/`** | KubeJS 会把它当数据包加载；不需要写脚本 |
| 加 / 删 / 改原版合成、熔炼 | `ServerEvents.recipes` | 操作 vanilla `RecipeManager` |
| 改物品标签 | `ServerEvents.tags` | 例如给堆肥补 `compostable` |
| 注册自己的物品 / 方块 | `StartupEvents.registry` | 只属于 KJS 内容，不进本模组材料表 |
| 给七张表写脚本循环 / 条件 | ❌ 没有 API | 用数据包 JSON；需要动态逻辑就生成 JSON |

> 版本：KubeJS 7（1.21.1 线，`2101.x`）；1.20.1 线用 `2001.x`。
> 本模组不依赖 KubeJS，装不装都能跑；下面的写法以 KubeJS 7 为例。

## 1. 七张表：走 `kubejs/data`

KubeJS 会把 `kubejs/data` 当数据包加载。目录结构和普通数据包一样：

```
kubejs/
├── data/
│   └── mypack/
│       ├── sieve/
│       │   └── sand_patch.json
│       ├── barrel/
│       │   └── water_sugar.json
│       └── compost/
│           └── patch.json
└── server_scripts/
    └── vanilla_recipes.js
```

**推荐用自己的命名空间 + `patch: true`**，不要依赖覆盖顺序：

`kubejs/data/mypack/sieve/sand_patch.json`

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"],
  "outputs": [
    { "item": "minecraft:diamond", "chance": 0.5 }
  ]
}
```

`kubejs/data/mypack/barrel/water_sugar.json`

```json
{
  "base": "minecraft:water",
  "added": { "item": "minecraft:sugar" },
  "result": "minecraft:dirt"
}
```

> 为什么不用 `data/exnihilosatellitium/sieve/sand.json`？
> 那会整文件替换内置 `sand.json`，需要把原条目抄全；而且不同 KJS 版本的包优先级
> 可能不同。用 `mypack` + `patch` 最稳。

七张表的字段、`#tag`、`mod` 门、`remove` 规则与[数据包页面](datapack.md)完全一致。

## 2. 原版配方：用脚本

```js
// kubejs/server_scripts/vanilla_recipes.js

ServerEvents.recipes(event => {
  // 删掉本模组的橡木桶配方（按 recipe id）
  event.remove({ id: 'exnihilosatellitium:barrel' })

  // 换成 3x3 原木
  event.shaped('exnihilosatellitium:barrel', [
    'LLL',
    'L L',
    'LLL'
  ], {
    L: 'minecraft:oak_log'
  })

  // 加一个熔炼
  event.smelting('minecraft:iron_ingot', 'minecraft:iron_ore')
})
```

- `event.remove({ id: '...' })` 也可以按输出删：`event.remove({ output: 'exnihilosatellitium:barrel' })`。
- 配方 id 在 `data/<ns>/recipe/`（1.20.1 是 `recipes/`）下，文件名就是 id 的 path。
- 想删某个模组的全部配方：`event.remove({ mod: 'exnihilosatellitium' })`。

## 3. 标签：`ServerEvents.tags`

堆肥需要 **JSON 分值 + `compostable` 标签** 两处。KJS 可以补标签这半：

```js
ServerEvents.tags('item', event => {
  event.add('exnihilosatellitium:compostable', 'minecraft:rotten_flesh')
  event.add('exnihilosatellitium:compostable', '#minecraft:flowers')
})
```

分值那半仍然要写 `kubejs/data/mypack/compost/*.json`：

```json
{
  "values": {
    "minecraft:rotten_flesh": 85,
    "#minecraft:flowers": 100
  }
}
```

## 4. 注册自己的物品（可选）

如果只是想加“本模组之外”的新物品，KJS 可以注册：

```js
// kubejs/startup_scripts/items.js
StartupEvents.registry('item', event => {
  event.create('mypack:tin_broken')
    .texture('mypack:item/tin_broken')
})
```

贴图放 `kubejs/assets/mypack/textures/item/tin_broken.png`；模型 KJS 会自动生成基础 item model。

但要清楚：

- 它不会出现在本模组的材料表、创造栏分组或 4→块→熔炼链里；
- 想让它参与筛子，仍然要在 `kubejs/data/mypack/sieve/*.json` 里写条目；
- **新增第 14 种材料不能靠 KJS**，见[首页](README.md#不能通过这三条路线做到的事)。

## 5. 重载与日志

- 服务器脚本 / `kubejs/data`：先 `/reload`；如果 `kubejs/data` 的文件没被重新读取
  （取决于 KJS 版本），退出重进世界或重启服务器。
- 脚本报错看 `logs/kubejs/server.log`；启动期注册看 `logs/kubejs/startup.log`。
- 本模组的表是否生效，仍然看 `/exnihilo tables` 和服务端日志里的
  `[sieve]` / `[hammer]` / … 行。

## 6. KubeJS 做不到的

- 没有 `SieveEvents.add(...)`、`BarrelEvents.add(...)` 之类的 API；
  七张表只能读 JSON。
- 不能用脚本直接调用 `SieveRegistry` / `CompostRegistry`。
- 不能改 `materials.json`。
- 不能把新物品“接进”本模组的材料链（自动 4→块、候选锭解析、JEI 分组都不会认）。
- 专用服客户端 JEI 仍然读不到服务端数据包（与数据包限制相同）。

## 未来

本页描述的是**当前版本**：KJS 没有七张表的脚本 API。三路径等价（KJS 直接
`SieveEvents.add(...)`）是路线图 P2 的目标，进度见
[`docs/customization-roadmap.md`](../docs/customization-roadmap.md)。

## 参考

- KubeJS 文档：<https://kubejs.com/>
- KubeJS 的 `data` 目录与普通数据包结构相同；本模组七张表的字段见[数据包页面](datapack.md)。
