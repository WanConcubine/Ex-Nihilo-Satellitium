# 1.4.1 发布材料

> 状态：**已构建；tag `v1.4.1` / `v1.4.1-1.20.1` 与 CurseForge / Modrinth 上传待所有者。**
> 版本号：`1.4.1`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> **性质：配方调整 + 版本对齐**——真正的改动只有 1.20.1 的匠魂碎矿锤装配站配方；
> 1.21.1 线没有代码 / 数据 / 贴图改动，只同步版本号。
> 上一版：1.4.0（2026-11-10）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.4.1.jar` | 948 KB | `b0b9530ff266593db17f716c368ce455e8eb1c8f61910004a1fdf5d05a7fe9bc` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.1.jar` | 982 KB | `17287ed41b6be74d49feb62122c6c6e92db1cf80d4f1300357a8fa984a05e4b9` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.4.1"` 已核对。产物构建于 2026-11-11（主线 Java 21、
`1.20.1-forge` Java 17；主线用 ModDevGradle，1.20.1 用 ForgeGradle 6）。

jar 文件名仍带 MC 版本（`<mod_id>-<mc_version>-<mod_version>.jar`），两条线同时上传不会撞名。

## 本版内容（相对 1.4.0）

### 1.20.1 线（`1.20.1-forge` 分支）

- **匠魂碎矿锤（`ex_nihilo_hammer`）不再和匠魂大锤同款**：装配站从
  「锤头 + 坚韧手柄 + 2 大板」改成 **1 锤头 + 1 坚韧手柄**。工具定义的部件列表、
  `default_materials` 与装配站输入槽一起减到两件（两块 `large_plate` 删掉）。
- 材料属性 / 特性 / 强化槽 / 掉落表不变；耐久倍率也没调，所以同材料锤子的基础耐久只剩头和手柄，
  会比四件配方低（大板原本各按 0.25 计入基础耐久）。
- 其余数据表未动：`src/main/resources` 200 recipes / 10 tags；generated 198 recipes /
  75 loot tables / 71 tags。

### 1.21.1 线（`main`）

- **无改动**。这一版只把 `mod_version` 改成 `1.4.1`，jar 与 1.4.0 只有版本号不同；
  这样两条线的版本号继续对齐（同 1.1.2 的做法）。

## 验证

- `./gradlew build --offline`：两版都通过（主线 Java 21 / 1.20.1 Java 17）。
- `check-textures.py`：主线 307 模型 / 116 blockstate / 190 纹理；1.20.1 313 模型 /
  116 blockstate / 209 纹理。
- `check-data-paths.py --target 1.20.1`（主线脚本跑 1.20.1 资源根）：两个根都通过。
- 1.20.1 服务端 `runServer -PtinkersMods` 启动：`Loaded 46 tool definitions` /
  `24 station slot layouts`，无解析错误；日志 `exnihilosatellitium (version 1.4.0 -> 1.4.1)`。
- **未做**：装配站两槽的实机画面、造出来的耐久复测、JEI 工具页。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.1 调整（仅 1.20.1 有实际改动；1.21.1 只同步版本号）：
> - 匠魂碎矿锤的装配站配方改成**1 锤头 + 1 坚韧手柄**（之前和匠魂大锤一样，还要 2 块大板）。
> - 材料属性 / 特性 / 强化槽 / 掉落都不变；因为去掉了两块大板，同材料耐久会比以前低一些。
> - 1.21.1 线没有内容改动，与 1.4.0 相同。

**English**：

> 1.4.1 changes (1.20.1 only; the 1.21.1 build just shares the version number):
> - The Tinkers' Construct crushing hammer now needs **1 hammer head + 1 tough handle** at the
>   Tinker Station (it used to copy the sledge hammer and also take 2 large plates).
> - Material stats, traits, modifier slots and drops are unchanged; with the plates gone, the
>   same materials give somewhat less durability.
> - The 1.21.1 line has no content changes and is identical to 1.4.0 apart from the version.

## 发布前检查

- [x] 两版 `mod_version=1.4.1`
- [x] `CHANGELOG.md` 的 1.4.1 节（中英双语）
- [x] README 下载表版本号
- [x] 两版 `build --offline` + `check-textures.py`
- [ ] tag `v1.4.1` / `v1.4.1-1.20.1`（所有者）
- [ ] CurseForge / Modrinth 上传（所有者）
