# 1.4.1 发布材料（1.20.1 线）

> 状态：**已构建；tag `v1.4.1-1.20.1` 与 CurseForge / Modrinth 上传待所有者。**
> 版本号：`1.4.1`；本文件是 **1.20.1 / Forge 47.4** 线，1.21.1 线这一版没有代码改动。
> 性质：**配方调整**——匠魂碎矿锤改成 1 锤头 + 1 坚韧手柄，不再照抄大锤。
> 上一版：1.4.0（2026-11-10）。
> tag：`v1.4.1-1.20.1`（待打）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.1.jar` | 982 KB | `17287ed41b6be74d49feb62122c6c6e92db1cf80d4f1300357a8fa984a05e4b9` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png`、`META-INF/mods.toml`，以及可选的
`compat/tconstruct/*`（匠魂 source set）；`mods.toml` 里的 `version="1.4.1"` 已核对。
产物构建于 2026-11-11（Java 17 / ForgeGradle 6）。

## 本版内容（相对 1.4.0）

### 变更

- **匠魂碎矿锤（`ex_nihilo_hammer`）不再和匠魂大锤同款**：装配站从「锤头 + 坚韧手柄 + 2 大板」
  改成 **1 锤头 + 1 坚韧手柄**。工具定义的部件列表、`default_materials` 与装配站输入槽
  一起减到两件（两块 `large_plate` 删掉）。
- 材料属性 / 特性 / 强化槽 / 掉落表不变；耐久倍率也没调，所以同材料锤子的基础耐久只剩头和手柄，
  会比四件配方低（大板原本各按 0.25 计入基础耐久）。

### 数据

- `tinkering/tool_definitions/ex_nihilo_hammer.json`：`part_stats.parts` 4 → 2，
  `default_materials` 4 → 2。
- `tinkering/station_layouts/ex_nihilo_hammer.json`：输入槽 4 → 2（锤头 → 手柄，顺序与定义一致）。
- 其余数据表未动：`src/main/resources` 200 recipes / 10 tags；generated 198 recipes /
  75 loot tables / 71 tags。

## 验证

- `./gradlew build --offline` 通过（Java 17）。
- `check-textures.py` 通过：313 模型 / 116 blockstate / 209 纹理。
- 主线 `check-data-paths.py --target 1.20.1`：两个资源根都通过。
- 服务端 `runServer -PtinkersMods` 启动：`Loaded 46 tool definitions` /
  `24 station slot layouts`，无解析错误（同一份数据在上一轮也过了一次 `/reload`）；
  日志里 `exnihilosatellitium (version 1.4.0 -> 1.4.1)`，`mods.toml` 的 `version="1.4.1"` 已核对。
- **未做**：装配站两槽的实机画面、造出来的耐久复测、JEI 工具页。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.1 调整：
> - 匠魂碎矿锤的装配站配方改成**1 锤头 + 1 坚韧手柄**（之前和匠魂大锤一样，还要 2 块大板）。
> - 材料属性 / 特性 / 强化槽 / 掉落都不变；因为去掉了两块大板，同材料耐久会比以前低一些。

**English**：

> 1.4.1 changes:
> - The Tinkers' Construct crushing hammer now needs **1 hammer head + 1 tough handle** at the
>   Tinker Station (it used to copy the sledge hammer and also take 2 large plates).
> - Material stats, traits, modifier slots and drops are unchanged; with the plates gone, the
>   same materials give somewhat less durability.

## 发布前检查

- [x] `mod_version=1.4.1`
- [x] `CHANGELOG.md` 的 1.4.1 节（中英双语）
- [x] README 下载表版本号
- [x] `build` + `check-textures.py` + `check-data-paths.py`
- [ ] tag `v1.4.1-1.20.1`（所有者）
- [ ] CurseForge / Modrinth 上传（所有者）
