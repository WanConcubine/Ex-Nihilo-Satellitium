# 1.4.1 发布材料（1.20.1 线）

> 状态：**已构建；tag `v1.4.1-1.20.1` 与 CurseForge / Modrinth 上传待所有者。**
> 版本号：`1.4.1`；本文件是 **1.20.1 / Forge 47.4** 线，1.21.1 线这一版没有代码改动。
> 性质：**匠魂碎矿锤调整**——装配站改成 1 锤头 + 1 坚韧手柄、修复砂砾/沙子挖掘速度、
> 新增匠魂「连锁」强化并让 DE 范围模块也能连锁碎矿方块。
> 上一版：1.4.0（2026-11-10）。
> tag：`v1.4.1-1.20.1`（待打）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.1.jar` | 995 KB | `cc4964208e4504820c39ac81fa277f7ded1f0d36a219096a0e9ee9884004a486` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png`、`META-INF/mods.toml`，以及可选的
`compat/tconstruct/*`（匠魂 source set）；`mods.toml` 里的 `version="1.4.1"` 已核对。
产物构建于 2026-11-11（Java 17 / ForgeGradle 6）。jar 的 sha256 取自本次最终构建；
ForgeGradle 重新打包会写入新的时间戳，重建出来的 sha 不会相同。

## 本版内容（相对 1.4.0）

### 变更

- **匠魂碎矿锤（`ex_nihilo_hammer`）不再和匠魂大锤同款**：装配站从「锤头 + 坚韧手柄 + 2 大板」
  改成 **1 锤头 + 1 坚韧手柄**。工具定义的部件列表、`default_materials` 与装配站输入槽
  一起减到两件（两块 `large_plate` 删掉）。
- **挖掘速度**：砂砾 / 沙子 / 尘土这些 `mineable/shovel` 方块以前按空手速度挖（工具不算“有效”）。
  `HammerTool#breakSpeed` 现在对锤表方块返回头部材料的挖掘速度（铁头 6.0）；头部部件的
  0.5 缩放也去掉，耐久回到 1.4.0 四件时的水平（铁头约 1000）。
- **连锁挖掘**：
  - 匠魂锤：新增强化 `exnihilosatellitium:chain`（I–III），装配站用 **4 燧石 + 1 铁块** 应用
    （1 个升级槽），每级多连锁 16 块；装了连锁后**锤头会多出两节锁链的 overlay**。
  - 龙之进化锤：新增**专属连锁模块** `exnihilosatellitium:chain_mining_module`，注册进 DE 的
    `draconicevolution:modules` 注册表（`MINING_TOOL` 分类），装上后一次粉碎连锁 **32 块**；
    合成：4 燧石 + 4 铁锭 + 1 个 DE `module_core`。
  - 只连锁同一种方块、六面相连；每块扣 1 点耐久 / 一次能量；搜索上限 1024 格。

### 数据

- `tinkering/tool_definitions/ex_nihilo_hammer.json`：`part_stats.parts` 4 → 2，
  `default_materials` 4 → 2，头部去掉 `scale`，`tool_actions` 增加 `shovel_dig`。
- `tinkering/station_layouts/ex_nihilo_hammer.json`：输入槽 4 → 2（锤头 → 手柄，顺序与定义一致）。
- 新增 `recipes/tools/modifiers/chain.json`（`tconstruct:modifier`，`forge:mod_loaded` 条件）。
- 其余数据表未动：`src/main/resources` 202 recipes / 10 tags；generated 198 recipes /
  75 loot tables / 71 tags。

## 验证

- `./gradlew build --offline` 通过（Java 17）。
- `check-textures.py` 通过：314 模型 / 116 blockstate / 211 纹理。
- 主线 `check-data-paths.py --target 1.20.1`：两个资源根都通过（202 / 198 / 75 / 71）。
- 服务端 `runServer -PtinkersMods` 启动 + `/reload`：`Loaded 46 tool definitions` /
  `24 station slot layouts`，无解析错误。
- 服务端 FakePlayer 探针（临时命令，已删除）：铁头 + 木柄碎矿锤速度 **6.0**、最大耐久
  **1000**；连锁 0 级 1 块 / 1 耐久，I 级 17 块 / 17 耐久，II 级 33 块 / 33 耐久；
  中间夹一块沙子时停在沙子前。
- DE 探针（临时命令，已删除）：模块在 DE 注册表中、物品与配方加载、DE 锤模块宿主接受该模块，
  装上后 `chainLimit` 0 → **32**。
- **未做**：装配站应用强化的画面、锁链 overlay 的实机外观、JEI 强化配方页。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.1 调整（仅 1.20.1 有实际改动；1.21.1 只同步版本号）：
> - 匠魂碎矿锤的装配站配方改成**1 锤头 + 1 坚韧手柄**（之前和匠魂大锤一样，还要 2 块大板）。
> - 修复碎矿锤挖砂砾 / 沙子 / 尘土时按空手速度的问题，现在按锤头材料的挖掘速度走。
> - 新增强化**连锁 I–III**（装配站：4 燧石 + 1 铁块），一次粉碎连带打碎相连的同类方块，
>   每级多 16 块；装上后锤头会多出锁链外观。
> - 龙之进化锤新增**专属连锁模块**（4 燧石 + 4 铁锭 + 1 个 module core），装上后一次连锁 32 块。
> - 材料属性 / 特性 / 掉落不变；1.21.1 线与 1.4.0 相同。

**English**：

> 1.4.1 changes (1.20.1 only; the 1.21.1 build just shares the version number):
> - The Tinkers' Construct crushing hammer now needs **1 hammer head + 1 tough handle** at the
>   Tinker Station (it used to copy the sledge hammer and also take 2 large plates).
> - Fixed gravel / sand / dust being mined at bare-hand speed; the hammer now uses its head
>   material's mining speed.
> - New **Chain I–III** modifier (Tinker Station: 4 flint + 1 iron block): one crush also breaks
>   connected blocks of the same kind, +16 blocks per level; the hammer head also gains a chain
>   overlay.
> - The Draconic Evolution hammer gets a dedicated chain module (4 flint + 4 iron ingots +
>   1 module core) that chains 32 blocks per crush.
> - Material stats, traits and drops are unchanged; the 1.21.1 line matches 1.4.0.

## 发布前检查

- [x] `mod_version=1.4.1`
- [x] `CHANGELOG.md` 的 1.4.1 节（中英双语）
- [x] README 下载表版本号
- [x] `build` + `check-textures.py` + `check-data-paths.py`
- [ ] tag `v1.4.1-1.20.1`（所有者）
- [ ] CurseForge / Modrinth 上传（所有者）
