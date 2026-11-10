# 1.4.0 发布材料（1.21.1 线）

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.4.0`；本文件是 **1.21.1 / NeoForge 21.1** 线，1.20.1 线的同版本说明在
> `1.20.1-forge` 分支。
> 性质：**内容更新**——材质锤 / 龙之进化锤 + 木钩耐久修复。
> 上一版：1.3.2（2026-11-05）。
> tag：`v1.4.0`（2026-11-10）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.4.0.jar` | 948 KB | `1c3e976316f244336b14dd9049a0426b9e7a5934c91cd3ef2fa72ac267726cbf` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png`、`META-INF/neoforge.mods.toml`，以及可选的
`src/de/java`（龙之进化 source set）编译产物；`mods.toml` 里的 `version="1.4.0"` 已核对。
产物构建于 2026-11-10（Java 21 / ModDevGradle）。

## 本版内容（相对 1.3.2）

### 新增

- **9 把金属材质锤**：锡 / 铝 / 锌 / 铜 **256**、银 **320**、铅 **384**、镍 **448**、锇 **512**、
  铂 **1920**；配方沿用斜形（2 锭 + 2 木棍），head 走 `c:ingots/<id>`，铜用原版铜锭。
- **3 把龙之进化锤（装 DE 时）**：`wyvern_hammer` / `draconic_hammer` / `chaotic_hammer`
  继承 DE 的 `ModularPickaxe`，走聚合装置升级链（钻石锤 → 双足飞龙 → 神龙 → 混沌，
  8M / 32M / 128M OP），可用 DE 的速度 / AOE / 能量模块；不消耗耐久，走 DE 能量（256/次）。
  3D cube 模型（左右锤头 + 发光核心 + 长柄），每个面用 `textures/item/de_hammer/*` 分区贴图。

### 修复

- **木钩 / 骨钩恢复消耗耐久**：`ToolEvents#wearMainHand` 曾被限制为只处理 `HammerItem`，
  1.21.1 的木钩 / 骨钩打树叶时耐久永远是满的。现在任何可损坏的手持工具都会扣耐久；
  能量锤仍走 `consumeEnergy`。服务端 FakePlayer 探针：木钩 / 骨钩 / 木锤各 `damage=1`，
  创造模式 `0`。

### 工程 / CI

- `check-core-imports.py` / `check-data-paths.py` 进 CI，PR 也触发；CI 用只读 token、
  删掉 `ci-logs` 强推，服务端日志作为 artifact 上传。

### 数据

- 锤：无 DE **15 把**（6 基础 + 9 金属），装 DE **18 把**。
- 资产：**307 模型 / 116 blockstate / 190 纹理**；**297 配方 / 75 loot / 75 tags**。
- Bamboo 宝石锤、匠魂碎矿锤、叶绿素熔炼只在 1.20.1 线，主线不注册。

## 验证

- `./gradlew build --offline` 通过（Java 21）。
- `check-textures.py` 通过：307 模型 / 116 blockstate / 190 纹理。
- `check-data-paths.py` 通过：297 recipes / 75 loot / 75 tags。
- `check-core-imports.py` 通过：0 个 core 层泄漏。
- 木钩 / 骨钩耐久：服务端 FakePlayer 探针通过（2026-11-09）。
- DE 锤：真装 Draconic Evolution 3.1.4.633 + Brandons Core / CodeChicken Lib 后 3 把锤注册，
  模块 GUI / 能量模块可用，手锤与自动锤 256/次扣电、空电停机；**作者实机确认通过**
  （聚合实际合成 / AOE 未单独目视）。
- **未做**：9 把金属锤的合成 / 耐久条 / JEI 目视，聚合合成 / AOE。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.0 新增：
> - 9 把金属锤（锡 / 铝 / 锌 / 铜 256，银 320，铅 384，镍 448，锇 512，铂 1920）。
> - 3 把龙之进化锤（双足飞龙 / 神龙 / 混沌；走聚合装置升级链，用 DE 能量与模块）。
> 1.4.0 修复：
> - 木钩 / 骨钩重新消耗耐久（此前 1.21.1 上打树叶不掉耐久）。

**English**：

> 1.4.0 adds:
> - 9 metal hammers (tin / aluminium / zinc / copper 256, silver 320, lead 384, nickel 448,
>   osmium 512, platinum 1920).
> - 3 Draconic Evolution hammers (wyvern / draconic / chaotic; fusion-crafting upgrade chain,
>   run on DE energy and accept DE modules).
> 1.4.0 fixes:
> - Wooden and bone crooks consume durability again (they had stopped wearing on 1.21.1).

## 发布前检查

- [x] `mod_version=1.4.0`
- [x] `CHANGELOG.md` 的 1.4.0 节（中英双语）
- [x] README 下载表版本号
- [x] `build` + `check-textures.py` + `check-data-paths.py` + `check-core-imports.py`
- [ ] CurseForge / Modrinth 上传（所有者）
