# 1.4.1 发布材料

> 状态：**已构建；tag `v1.4.1` / `v1.4.1-1.20.1` 与 CurseForge / Modrinth 上传待所有者。**
> 版本号：`1.4.1`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> **性质：配方调整 + 连锁 + 版本对齐**——真正的改动都在 1.20.1 的匠魂碎矿锤（装配站配方、
> 挖掘速度、连锁强化，DE 锤用 AOE 模块连锁）；
> 1.21.1 线没有代码 / 数据 / 贴图改动，只同步版本号。
> 上一版：1.4.0（2026-11-10）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.4.1.jar` | 948 KB | `b0b9530ff266593db17f716c368ce455e8eb1c8f61910004a1fdf5d05a7fe9bc` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.1.jar` | 995 KB | `cc4964208e4504820c39ac81fa277f7ded1f0d36a219096a0e9ee9884004a486` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.4.1"` 已核对。产物构建于 2026-11-11（主线 Java 21、
`1.20.1-forge` Java 17；主线用 ModDevGradle，1.20.1 用 ForgeGradle 6）。

jar 文件名仍带 MC 版本（`<mod_id>-<mc_version>-<mod_version>.jar`），两条线同时上传不会撞名。

## 本版内容（相对 1.4.0）

### 1.20.1 线（`1.20.1-forge` 分支）

- **匠魂碎矿锤（`ex_nihilo_hammer`）不再和匠魂大锤同款**：装配站从
  「锤头 + 坚韧手柄 + 2 大板」改成 **1 锤头 + 1 坚韧手柄**。工具定义的部件列表、
  `default_materials` 与装配站输入槽一起减到两件（两块 `large_plate` 删掉）。
- **挖掘速度**：砂砾 / 沙子 / 尘土这类 `mineable/shovel` 方块以前按空手速度挖；现在锤表方块
  按锤头材料的挖掘速度（铁头 6.0）。头部去掉 0.5 缩放后耐久也回到 1.4.0 的四件水平
  （铁头约 1000）。
- **连锁挖掘**：新增强化 `exnihilosatellitium:chain`（I–III，装配站 **4 燧石 + 1 铁块**，
  占 1 个升级槽），一次粉碎连带打碎相连的同类方块，每级多 16 块；装了以后锤头多出两节锁链的
  overlay。DE 锤新增**专属连锁模块** `chain_mining_module`（4 燧石 + 4 铁锭 + 1 module core），
  装上后一次连锁 32 块。每块照常扣 1 点耐久 / 一次能量。
- 其余数据表未动：`src/main/resources` 202 recipes / 10 tags；generated 198 recipes /
  75 loot tables / 71 tags。

### 1.21.1 线（`main`）

- **无改动**。这一版只把 `mod_version` 改成 `1.4.1`，jar 与 1.4.0 只有版本号不同；
  这样两条线的版本号继续对齐（同 1.1.2 的做法）。

## 验证

- `./gradlew build --offline`：两版都通过（主线 Java 21 / 1.20.1 Java 17）。
- `check-textures.py`：主线 307 模型 / 116 blockstate / 190 纹理；1.20.1 314 模型 /
  116 blockstate / 211 纹理。
- `check-data-paths.py --target 1.20.1`（主线脚本跑 1.20.1 资源根）：两个根都通过。
- 1.20.1 服务端 `runServer -PtinkersMods` 启动：`Loaded 46 tool definitions` /
  `24 station slot layouts`，无解析错误；日志 `exnihilosatellitium (version 1.4.0 -> 1.4.1)`；
  `exnihilosatellitium:chain` 静态强化注册成功、`tconstruct:modifier` 配方加载成功。
- 服务端 FakePlayer 探针（临时命令，已删除）：铁头 + 木柄 6.0 速度 / 1000 耐久；连锁 0 级
  1 块、I 级 17 块、II 级 33 块，中间夹一块沙子会停在沙子前。
- DE 探针（临时命令，已删除）：模块在 DE 注册表、物品与合成加载、DE 锤宿主接受，装上后
  `chainLimit` 0 → 32。
- **未做**：装配站应用强化的画面、锁链 overlay 外观、DE 实机装模块、JEI 强化配方页。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.1 调整（仅 1.20.1 有实际改动；1.21.1 只同步版本号）：
> - 匠魂碎矿锤的装配站配方改成**1 锤头 + 1 坚韧手柄**（之前和匠魂大锤一样，还要 2 块大板）。
> - 修复碎矿锤挖砂砾 / 沙子 / 尘土时按空手速度的问题，现在按锤头材料的挖掘速度走。
> - 新增强化**连锁 I–III**（装配站：4 燧石 + 1 铁块），一次粉碎连带打碎相连的同类方块，
>   每级多 16 块；装上后锤头会多出锁链外观。
> - 龙之进化锤新增**专属连锁模块**（4 燧石 + 4 铁锭 + 1 个 module core），装上后一次连锁 32 块。
> - 材料属性 / 特性 / 掉落不变；1.21.1 线没有内容改动，与 1.4.0 相同。

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
> - Material stats, traits and drops are unchanged; the 1.21.1 line has no content changes and
>   is identical to 1.4.0 apart from the version.

## 发布前检查

- [x] 两版 `mod_version=1.4.1`
- [x] `CHANGELOG.md` 的 1.4.1 节（中英双语）
- [x] README 下载表版本号
- [x] 两版 `build --offline` + `check-textures.py`
- [ ] tag `v1.4.1` / `v1.4.1-1.20.1`（所有者）
- [ ] CurseForge / Modrinth 上传（所有者）
