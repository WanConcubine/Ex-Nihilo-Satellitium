# 排错与日志

> 最后核对：2026-11-06 · 适用：1.21.1 / 1.20.1

## 1. 先做这 4 步

1. `/reload`
2. `/datapack list` → 你的包在列表里且是 enabled
3. `/exnihilo tables` → 条数有没有变化；`/exnihilo dump <table>` → 整表内容
4. 看服务端日志里对应的 `[sieve]` / `[hammer]` / `[barrel]` / `[compost]` / `[crucible]` 行

如果第 3 步条数没变，基本是**文件没被加载**；如果条数变了但行为不对，基本是**字段/概率写错**。

## 2. 常见“没生效”

| 现象 | 常见原因 |
|---|---|
| `/datapack list` 没有你的包 | 目录层级错了，或没有 `pack.mcmeta` |
| 包显示 disabled | `/datapack enable "file/my_pack"` |
| 日志 `empty or unknown tag` | 标签拼错；1.20.1 用了 `c:`（应为 `forge:`），或反过来 |
| 日志 `unknown input/result` | 物品 id 拼错，或依赖的模组没装（可加 `"mod"` 门） |
| 日志 `duplicate input` | 两个文件写了同一个输入；筛子后者覆盖，锤子/钩子/熔点后者跳过 |
| 只改了 `outputs`，结果原掉落全没了 | 少了 `"patch": true`，被当成覆盖 |
| `remove` 写了没反应 | 没写 `patch: true` 或 `remove` 不是补丁字段；或目标 id 不对 |
| 1.21.1 的包放到 1.20.1 | `pack_format` 应为 `15`；`recipe/` vs `recipes/` 等路径不同 |
| `/reload` 后第一次条数少 | 正常：先 `waiting for tags`，`TagsUpdatedEvent` 后才是完整条数 |
| 改完不知道整表长什么样 | `/exnihilo dump <table>`；按行排序，可直接和另一份表 diff |

## 3. `waiting for tags` 是什么

标签在所有 reload listener 之后才绑定，所以每次 reload 会打印两次：

```
[sieve] loaded 6 inputs, 90 drops (0 tag files, 2 patches, waiting for tags)
[sieve] loaded 45 inputs, 115 drops (1 tag files, 2 patches)
```

**以第二次为准。** 第一次只是具体条目；`#tag` 和补丁还没展开。

## 4. 堆肥“有分值但不能堆”

堆肥需要两处同时存在：

1. `data/<ns>/compost/*.json` 里的 `values`
2. 物品标签 `#exnihilosatellitium:compostable`

用 `/exnihilo compost <item>` 查当前分值：

```mcfunction
/exnihilo compost minecraft:bread
```

- 分值 0：JSON 没写或没加载
- 分值 > 0 但游戏里不能堆：标签里没有这个物品
- 能堆但只给 1 点：标签有、JSON 没有（默认 1 点）

## 5. JEI 和实际不一致

- 单人 / 局域网：JEI 读服务端表，应该一致。
- **专用服**：客户端读不到服务端数据包，JEI 只显示客户端本地资源（jar 内置值或客户端自己的数据包）；
  实际以服务端为准。这不是数据包没生效。
- 改完 `/reload` 后 JEI 没刷新：重开 JEI 界面或重进世界。

## 6. KubeJS 专属

| 现象 | 检查 |
|---|---|
| `kubejs/data` 的 JSON 不生效 | 路径必须是 `kubejs/data/<ns>/sieve/...`，不是 `kubejs/sieve/...` |
| 脚本改了配方但表没变 | 脚本没在 `/reload` 后重跑；用 `add` 改已有 key 会被忽略（改用 `replace`）；或绑定名写错 |
| 启动脚本报错 | 看 `logs/kubejs/startup.log` |
| 服务器脚本报错 | 看 `logs/kubejs/server.log` |
| `/reload` 后 `kubejs/data` 没重读 | 退出重进世界 / 重启服务器（取决于 KJS 版本） |
| KJS 注册的物品不在材料链 | 正常；它只是 KJS 内容，不是本模组材料 |

## 7. CraftTweaker 专属

| 现象 | 检查 |
|---|---|
| 想改筛子/桶/堆肥 | 用 `mods.exnihilosatellitium.*`（Sieve / Barrel / Compost 等）；本模组没有 CT recipe type，不要写 `<recipetype:...>` |
| `<recipetype:exnihilosatellitium:sieve>` 报错 | 本模组没有注册 CT recipe type |
| 脚本语法错误 | 看 `logs/crafttweaker.log`，ZenScript 会带行号 |
| 配方删不掉 | 用输出删 `craftingTable.remove(<item:...>)`；确认 CT 版本用 `craftingTable` 而不是 1.12 的 `recipes` |
| 和 KJS 冲突 | 同一配方不要两边都改；先禁用一边再测 |

## 8. 版本差异速查

| | 1.21.1 | 1.20.1 |
|---|---|---|
| `pack_format` | 48 | 15 |
| 原版配方 | `data/<ns>/recipe/` | `data/<ns>/recipes/` |
| 原版标签 | `tags/item`、`tags/block` | `tags/items`、`tags/blocks` |
| 通用标签 | `c:` | `forge:` |
| 原版 `result` | `{"id": ...}` | `{"item": ...}` |
| 七张表 | 同左 | 同左 |

## 9. 还是不行时，带上这些信息

- 游戏版本 + 模组版本（1.21.1 / 1.20.1）
- 数据包目录树（`/datapack list` 的截图或文字）
- `/exnihilo tables` 输出
- 服务端日志里所有 `[sieve]` / `[hammer]` / `[barrel]` / `[compost]` / `[crucible]` 行
- 出问题的那个 JSON 文件全文

这样可以一眼看出是“没加载”“字段错”还是“版本路径错”。
