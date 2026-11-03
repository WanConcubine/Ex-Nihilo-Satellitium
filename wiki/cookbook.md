# Cookbook：常见改法

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1

> 数据包和 KubeJS 的七张表写法**完全一样**；KJS 只是把文件放到 `kubejs/data/`。
> 下面用 `data/mypack/...` 表示数据包路径，KJS 换成 `kubejs/data/mypack/...` 即可。

## 1. 给沙子加 2% 钻石

`data/mypack/sieve/sand.json`

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "outputs": [
    { "item": "minecraft:diamond", "chance": 0.02 }
  ]
}
```

## 2. 删掉沙子的仙人掌

```json
{
  "input": "minecraft:sand",
  "patch": true,
  "remove": ["minecraft:cactus"]
}
```

## 3. 给所有砂砾加 1% 钻石

1.21.1：

```json
{ "input": "#c:gravels", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

1.20.1：

```json
{ "input": "#forge:gravel", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

## 4. 删掉整个输入（例如尘土）

```json
{ "input": "exnihilosatellitium:dust", "remove": true }
```

## 5. 改堆肥：面包 125 → 100

`data/mypack/compost/bread.json`

```json
{
  "remove": ["minecraft:bread"],
  "values": { "minecraft:bread": 100 }
}
```

如果面包原本不在表里，只写 `values` 即可；同时确认
`#exnihilosatellitium:compostable` 包含面包。

## 6. 加桶转化：水 + 糖 → 泥土

```json
{
  "base": "minecraft:water",
  "added": { "item": "minecraft:sugar" },
  "result": "minecraft:dirt"
}
```

删内置规则：

```json
{ "base": "minecraft:water", "added": { "item": "minecraft:milk_bucket" }, "remove": true }
```

## 7. 改锤子：圆石 → 2 个砂砾

```json
{
  "input": "minecraft:cobblestone",
  "patch": true,
  "result": "minecraft:gravel",
  "min": 2,
  "max": 2
}
```

## 8. 加坩埚热源：营火 0.2 mB/t

1.21.1 / 1.20.1 都有 `#minecraft:campfires`：

```json
{ "block": "#minecraft:campfires", "speed": 0.2 }
```

## 9. 加坩埚熔点：树苗 → 100 mB 水

```json
{ "input": "#minecraft:saplings", "fluid": "minecraft:water", "amount": 100 }
```

## 10. 改木桶合成（原版配方）

数据包只能**加**，不能删；要替换内置配方用 KJS 或 CT。

**KJS：**

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'exnihilosatellitium:barrel' })
  event.shaped('exnihilosatellitium:barrel', [
    'LLL',
    'L L',
    'LLL'
  ], { L: 'minecraft:oak_log' })
})
```

**CraftTweaker：**

```zenscript
craftingTable.remove(<item:exnihilosatellitium:barrel>);
craftingTable.addShaped("barrel_from_logs", <item:exnihilosatellitium:barrel>, [
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:air>, <item:minecraft:oak_log>],
  [<item:minecraft:oak_log>, <item:minecraft:oak_log>, <item:minecraft:oak_log>]
]);
```

## 11. 删掉某条配方

- **KJS**：`event.remove({ id: '...' })` 或 `event.remove({ output: '...' })`
- **CT**：`craftingTable.remove(<item:...>);`
- **数据包**：做不到“删”，只能同路径覆盖成另一条配方（不推荐）。

## 12. 加一个自己的物品

只有 KJS 能做：

```js
// kubejs/startup_scripts/items.js
StartupEvents.registry('item', event => {
  event.create('mypack:tin_broken').texture('mypack:item/tin_broken')
})
```

然后要在 `kubejs/data/mypack/sieve/*.json` 里把它写进掉落，或写合成配方。
它**不会**进入本模组的材料链/创造栏分组。

## 13. 加第 21 种材料

三条路线都做不到。材料表在 jar 内 `materials.json`，注册期读取；
新增材料还要贴图、模型、配方、标签、JEI 分组。需要改模组源码或等后续 schema 支持。

## 14. 一次改多张表

一个数据包可以同时放多个目录：

```
data/mypack/
├── sieve/
├── hammer/
├── barrel/
└── compost/
```

`/reload` 一次全部生效；用 `/exnihilo tables` 看汇总。

## 15. 暂时关掉自己写的一批条目

给文件根加一个不存在的模组门：

```json
{ "mod": "mypack_disabled", "input": "#c:gravels", "outputs": [ { "item": "minecraft:diamond", "chance": 0.01 } ] }
```

这只会关掉**这个文件**，不会关掉 jar 内置表；内置条目要用 `remove` 补丁逐条删。