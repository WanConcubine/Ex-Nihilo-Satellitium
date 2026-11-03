# Ex Nihilo: Satellitium 自定义配方 Wiki

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1

面向玩家和整合包作者的「改配方」说明。这里不讲 Java 开发，只讲**装好模组之后怎么改数据**。

> 维护约定：本目录是玩家 wiki 的唯一出处；改代码里的表结构、字段名或加载顺序时，
> 同一提交里更新对应页面，并跑 `python3 scripts/check-wiki.py`。维护者职责和发布检查见
> [`MAINTAINING.md`](MAINTAINING.md)；三条路径的差距与阶段见
> [`docs/customization-roadmap.md`](../docs/customization-roadmap.md)。开发向的完整 schema 仍在
> [`docs/data-format.md`](../docs/data-format.md)。

## 三条路线怎么选

| 路线 | 能做什么 | 不能做什么 | 适合谁 |
|---|---|---|---|
| **数据包**（推荐） | 改筛子 / 锤子 / 钩子 / 桶 / 堆肥 / 坩埚七张表；加原版配方；加标签 | 不能新增物品、方块、材料；不能直接删原版配方 | 所有人，零代码 |
| **KubeJS** | 七张表脚本 API（1.21.1）+ `kubejs/data`；脚本改原版配方 / 标签；注册自己的物品 | 1.20.1 分支暂无脚本 API（用 `kubejs/data`）；不能改 `materials.json` | 整合包已经装了 KubeJS |
| **CraftTweaker** | 只改原版配方（合成、熔炼等） | **七张表完全碰不到**；不能加物品/方块/材料 | 已经装了 CT，且只改合成 |

**一句话：** 七张玩法表（筛子/锤子/钩子/桶/堆肥/坩埚）在 1.21.1 上可以用
**JSON 或 KubeJS 脚本**；1.20.1 分支暂时只认 JSON；CraftTweaker 两版都还不能改这七张表。

## 版本差异

本 wiki 同时覆盖两条发布线：

| | 1.21.1（`main`，NeoForge） | 1.20.1（`1.20.1-forge`，Forge） |
|---|---|---|
| 数据包 `pack_format` | `48` | `15` |
| 原版配方目录 | `data/<ns>/recipe/` | `data/<ns>/recipes/` |
| 原版标签目录 | `data/<ns>/tags/item/`、`tags/block/` | `tags/items/`、`tags/blocks/` |
| 通用标签命名空间 | `c:`（如 `#c:gravels`） | `forge:`（如 `#forge:gravel`） |
| 原版配方 `result` | `{"id": "...", "count": 1}` | `{"item": "...", "count": 1}` |
| 条件 | `neoforge:conditions` | `forge:conditions` |
| 七张表目录 | `sieve/`、`hammer/`…（两版相同） | 同左 |

七张表的**字段和目录名两版一致**，所以本 wiki 的 JSON 示例默认两版通用；
涉及标签命名空间、原版配方 JSON、`pack_format` 时会单独标注。
**KubeJS 脚本 API 目前只在 1.21.1 主线提供**；1.20.1 分支仍用 `kubejs/data`。

## 快速开始（数据包）

1. 在存档或服务器建目录：
   - 单人：`saves/<世界名>/datapacks/my_pack/`
   - 服务器：`<level-name>/datapacks/my_pack/`
2. 放 `pack.mcmeta`：

   ```json
   {
     "pack": {
       "pack_format": 48,
       "description": "我的空岛配方"
     }
   }
   ```

   1.20.1 把 `pack_format` 改成 `15`。
3. 按命名空间放文件，例如 `data/mypack/sieve/sand.json`：

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

4. 进游戏执行 `/reload`（或 `/datapack list` 确认启用）。
5. 用 `/exnihilo tables` 看条数；`/exnihilo sieve minecraft:sand` 查单条；
   `/exnihilo dump <table>` 看整张表（按行排序，方便对比）。

## 目录

- [数据包：完整表结构与示例](datapack.md)
- [KubeJS：能做什么、怎么写](kubejs.md)
- [CraftTweaker：边界与替代方案](crafttweaker.md)
- [常见任务 Cookbook](cookbook.md)
- [排错与日志](troubleshooting.md)
- [维护契约](MAINTAINING.md)
- [三路径等价路线图](../docs/customization-roadmap.md)

## 先记住这三条

1. **`chance` 是独立概率，不是权重。** 每条掉落单独掷 `0..1`，一次可能出多个。
2. **补丁要写 `"patch": true`。** 只写 `outputs` 不写 `patch` 会被当成“覆盖整条输入”，
   而不是追加。
3. **堆肥要改两处。** `compost/*.json` 的分值 + `#exnihilosatellitium:compostable`
   物品标签；只改一处会出现“有分值但不能堆”或“能堆但只给 1 点”。

## 不能通过这三条路线做到的事

- **新增材料**（第 14 种矿）：材料表在 jar 内的 `materials.json`，注册期读取；
  数据包/KJS/CT 都改不了。KJS 可以自己注册一套物品做“平行矿链”，但它不属于本模组的材料链。
- **新增本模组的物品/方块**：同上，注册期决定。
- **直接删除原版配方**：数据包没有“删除配方”的 JSON；用 KubeJS 或 CraftTweaker。

---

遇到问题先看 [排错与日志](troubleshooting.md)。