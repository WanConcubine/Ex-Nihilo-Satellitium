# 示例数据包

把整个 `datapack/` 目录复制到存档的 `datapacks/` 下（或服务器的
`world/datapacks/`），然后在游戏里执行 `/reload`。用 `/exnihilo tables` 看条数，
再看日志里的 `[sieve]` / `[hammer]` / `[barrel]` / `[compost]` 行确认生效。

- 目标版本：Minecraft 1.20.1 / Forge 47.x（`pack_format: 15`）。
  1.21.1 主线用 `pack_format: 48`；表目录名不变，但 vanilla 的
  `recipes/` / `loot_tables/` / `tags/` 命名空间按 1.20.1 写。
- 示例只是演示语法，不是平衡建议；上线前按自己的整合包改数值。

| 文件 | 演示 |
|---|---|
| `data/example/sieve/gravel_tag.json` | 标签输入：所有 `#forge:gravel` 物品额外 1% 钻石 |
| `data/example/sieve/patch_dirt.json` | 补丁：删掉橡树苗掉落，换成 8% 樱花树苗（不用抄 dirt.json） |
| `data/example/hammer/logs.json` | 标签输入：所有原木可锤成 1–2 木棍 |
| `data/example/crook/leaves.json` | 标签输入：所有树叶额外 10% 木棍 |
| `data/example/crucible_melt/saplings.json` | 标签输入：所有树苗熔成 100 mB 水 |
| `data/example/crucible_heat/campfires.json` | 标签热源：所有营火 0.2 mB/tick |
| `data/example/barrel/mod_gate.json` | `mod` 门：只有装 Tinkers 时才注册（1.21.1 上不会生效） |
| `data/example/compost/patch.json` | 堆肥补丁：把面包从 125 改成 100 |

语法和优先级见 [`docs/data-format.md`](../../docs/data-format.md) §14。
