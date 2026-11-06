# KubeJS：数据包通道 + 七张表脚本 API

> 最后核对：2026-11-06 · 适用：1.21.1 / 1.20.1
> KubeJS 版本：1.21.1 用 `2101.x`，1.20.1 用 `2001.x`；绑定名和调用写法相同

KubeJS 现在有两条通道：

| 你想做的 | 怎么做 |
|---|---|
| 改筛子 / 锤子 / 钩子 / 桶 / 堆肥 / 坩埚 | **脚本 API**（`SieveEvents` 等，见 §1）或 `kubejs/data` JSON |
| 加 / 删 / 改原版合成、熔炼 | `ServerEvents.recipes` |
| 改物品标签 | `ServerEvents.tags` |
| 注册自己的物品 / 方块 | `StartupEvents.registry`（只属于 KJS 内容） |
| 新增本模组材料（`materials.json`） | ❌ 做不到 |

## 1. 七张表的脚本 API（两版）

把脚本放到 `kubejs/server_scripts/`，例如 `exnihilo_tables.js`：

```js
// 筛子：沙子里加 0.02 的钻石；一次成功掉 1 个
SieveEvents.add('minecraft:sand', 'minecraft:diamond', 0.02)

// 第 4 个参数是 min，第 5 个是 max（省略时都是 1）
SieveEvents.add('minecraft:dirt', 'minecraft:oak_sapling', 0.08, 1, 2)

// 删掉落 / 删整个输入 / 整条替换
SieveEvents.remove('minecraft:sand', 'minecraft:cactus')
SieveEvents.removeInput('minecraft:gravel')
SieveEvents.replace('minecraft:sand', 'minecraft:emerald', 1.0)

// 锤子 / 钩子
HammerEvents.add('minecraft:cobblestone', 'minecraft:gravel')
HammerEvents.add('minecraft:oak_log', 'minecraft:stick', 1, 2)
HammerEvents.remove('minecraft:cobblestone')

CrookEvents.add('minecraft:oak_leaves', 'minecraft:stick', 0.1)

// 桶：物品 / 流体两种 added
BarrelEvents.addItem('minecraft:water', 'minecraft:sugar', 'minecraft:dirt')
BarrelEvents.addFluid('minecraft:water', 'minecraft:milk_bucket', 'minecraft:slime_block')

// 堆肥 / 坩埚
CompostEvents.add('minecraft:bread', 100)
CompostEvents.add('#minecraft:flowers', 100)       // 标签也可以
CrucibleMeltEvents.add('minecraft:cobblestone', 'minecraft:lava', 250)
CrucibleHeatEvents.add('minecraft:torch', 0.1)
```

### 语义

- 所有 id 都可以写 `#tag`（例如 `#minecraft:logs`、`#forge:gravel`）。
- `add` = 数据包普通文件语义：筛子/钩子**合并掉落**；锤子/熔点/热源/桶是“一条规则一个 key”，
  先到者赢，脚本 `add` **覆盖不了**已有规则；要改已有规则用 `replace`。
- `remove` = 删整条；筛子/钩子也可以用 `remove(input, output)` 只删一条掉落。
- 脚本操作在数据包和补丁**之后**应用（脚本是最后一层覆盖），优先级固定为 `100`。
- 脚本重载时旧贡献会先被清掉，再按新脚本重新提交；`/reload` 不会重复叠加。
- 改完 `/reload`；`/exnihilo tables` 看条数，`/exnihilo dump <table>` 看整表，
  `[customize]` 日志看谁覆盖了谁。
- 1.20.1 的 KJS 2001 API 与 1.21.1 的 2101 不同，但本模组暴露的绑定对象和写法完全一样；
  两个版本都验证过同一份脚本。

### JSON 逃生口

复杂结构（例如带实体/和平掉落的桶规则）可以直接写完整 JSON：

```js
SieveEvents.addJson('{"input":"minecraft:sand","outputs":[{"item":"minecraft:diamond","chance":0.02}]}')
SieveEvents.removeJson('{"input":"minecraft:sand","remove":["minecraft:cactus"]}')
SieveEvents.replaceJson('{"input":"minecraft:sand","outputs":[]}')

BarrelEvents.addJson('{"base":"minecraft:witch_water","added":{"item":"exnihilosatellitium:creepy_doll"},"entity":"minecraft:enderman","peaceful_drop":"minecraft:ender_pearl","cook_ticks":600}')
```

七张表都有 `addJson` / `removeJson` / `replaceJson`。JSON 字段与[数据包页面](datapack.md)完全一致；
桶的新字段 `result_fluid` / `delay_ticks` / `drain` 同样通过 `addJson` 生效（见数据包页 §6）。
`dump` 输出也可以直接拿来当 fixture。

### 容易踩的坑

- **筛子同物品不同概率 = 两条独立掉落**：`add(sand, diamond, 0.02)` 和
  `add(sand, diamond, 0.5)` 都会留下，实际是两次独立判定。
- **锤子 `add` 不会覆盖已有规则**：内置 `cobblestone → gravel` 还在时，`add(cobblestone, flint)`
  会被忽略并打 `add ignored`；要改请用 `replace`。
- **不要同时用脚本和 `kubejs/data` 写同一条**：脚本在文件之后应用，通常脚本赢；
  但两边都写会让排查变复杂。选一个来源。
- **材料表不参与**：脚本不能新增 `materials.json` 里的材料，也不能让 KJS 物品进入
  4→块→熔炼链。

## 2. `kubejs/data` 通道（两版都能用）

KubeJS 会把 `kubejs/data` 当数据包加载，目录结构和普通数据包一样：

```
kubejs/data/mypack/sieve/sand_patch.json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"],
  "outputs": [{ "item": "minecraft:diamond", "chance": 0.5 }]
}
```

- 推荐自己的命名空间 + `"patch": true`，不要依赖覆盖顺序。
- 七张表的字段、`#tag`、`mod` 门、`remove` 规则与[数据包页面](datapack.md)一致。
- 没装 KJS / 不想写脚本时，这条通道两版都能用。

## 3. 原版配方：用脚本

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'exnihilosatellitium:barrel' })
  event.shaped('exnihilosatellitium:barrel', ['LLL', 'L L', 'LLL'], { L: 'minecraft:oak_log' })
  event.smelting('minecraft:iron_ingot', 'minecraft:iron_ore')
})
```

## 4. 标签：`ServerEvents.tags`

堆肥需要 **JSON 分值 + `compostable` 标签** 两处；脚本 API 的 `CompostEvents.add` 只写分值，
标签仍要补：

```js
ServerEvents.tags('item', event => {
  event.add('exnihilosatellitium:compostable', 'minecraft:rotten_flesh')
})
```

## 5. 重载与日志

- 脚本 / `kubejs/data` 改完先 `/reload`；脚本在服务端重载时重新执行。
- 本模组的脚本贡献在每次脚本重载前清空，所以改脚本不会留下旧条目。
- 看 `logs/kubejs/server.log`；本模组侧看 `/exnihilo tables`、`/exnihilo dump` 和
  `[customize]` 日志。

## 6. KJS 做不到的

- 不能改 `materials.json`，也不能让 KJS 物品进入本模组材料链；边界和替代方案见[材料链与联动](material-chain.md)。
- 不能注册本模组的方块/物品（KJS 自己的内容可以）。
- 专用服客户端 JEI 仍读不到服务端表（P4 客户端同步）。

## 参考

- KubeJS 文档：<https://kubejs.com/>
- 七张表字段：[数据包页面](datapack.md)；三路径进度：[roadmap](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/customization-roadmap.md)。
