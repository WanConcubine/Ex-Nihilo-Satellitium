# 1.4.0 发布材料（1.20.1 线）

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.4.0`；本文件是 **1.20.1 / Forge 47.4** 线，1.21.1 线的同版本说明在 `main` 分支。
> 性质：**内容更新**——材质锤 / 匠魂碎矿锤 / 龙之进化锤，叶绿素碎矿熔炼，两处修复。
> 上一版：1.3.2（2026-11-05）。
> tag：`v1.4.0-1.20.1`（2026-11-10）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.0.jar` | 982 KB | `ad28d811f39daeacad6b07b68548a7cf0c2d4c9d2968a153eda4ff51e46d808f` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png`、`META-INF/mods.toml`，以及可选的
`compat/tconstruct/*`（匠魂 source set）；`mods.toml` 里的 `version="1.4.0"` 已核对。
产物构建于 2026-11-10（Java 17 / ForgeGradle 6）。

## 本版内容（相对 1.3.2）

### 新增

- **9 把金属材质锤**：锡 / 铝 / 锌 / 铜 **256**、银 **320**、铅 **384**、镍 **448**、锇 **512**、
  铂 **1920**；配方 head 走 `forge:ingots/<id>`，铜用原版铜锭。
- **3 把 Bamboo 宝石锤**：樱 / 枫 / 银杏统一 **2048**，按 `bamboomod` 条件注册，配方带
  `forge:mod_loaded`；BambooMod 本身没有宝石锤，这是用它三种宝石做的自有锤。
- **3 把龙之进化锤（装 DE 时）**：`wyvern_hammer` / `draconic_hammer` / `chaotic_hammer`
  继承 DE 的 `ModularPickaxe`，走聚合装置升级链（钻石锤 → 双足飞龙 → 神龙 → 混沌，
  8M / 32M / 128M OP），可用 DE 模块；不消耗耐久，走 DE 能量（256/次）。3D cube 模型。
- **匠魂碎矿锤（`ex_nihilo_hammer`，装 TConstruct 时）**：真正的匠魂 3 可改造工具。
  装配站用 **锤头 + 坚韧手柄 + 2×大板** 合成，材料属性 / 特性 / 强化槽 / 耐久 / 破损动画
  全走匠魂；命中锤表方块时掉落与自有锤一致，其余方块正常挖掘；单方块判定，不登记 `aoe`。
  自带 16 个强化 overlay。
- **叶绿素碎矿熔炼**：破碎 / 粉碎 / 粉末叶绿素矿各 **45 mB**、600°C、`time: 26`，
  直接熔成工匠巧思的 `tinkers_thinking:molten_chlorophyte`，不必先压成矿块再熔 compound。

### 修复

- **机器 GUI 没有压暗**：1.20.1 的 `AbstractContainerScreen#render` 不会自动画背景，
  机器界面只在 `renderBg` 里贴面板图，打开时直接盖在世界上。现在先调
  `renderBackground(graphics)` 再画面板，世界按原版渐变压暗。
- **非 `HammerItem` 的锤白嫖耐久**：`ToolEvents#onBreak` 取消原版破坏后统一走
  `HammerTool#wear`；匠魂锤覆写为 `ToolDamageUtil.damageAnimated`（强化 / 粘液覆层 /
  破损动画都生效）。自动锤机器对非能量锤也扣耐久。

### 数据

- 锤：装 DE **21 把**（6 基础 + 9 金属 + 3 宝石 + 3 DE），不装 DE **18 把**；装匠魂再 +1。
- 匠魂配方：**95 条熔炼 + 2 条铸造**（含阿迪特 6 条、ER yellorium 8 条、叶绿素 3 条）。
- 资产：**313 模型 / 116 blockstate / 209 纹理**；手写 + datagen 配方 200 个文件
  （`src/main/resources`），generated 目录 198 recipes / 75 loot / 71 tags。

## 验证

- `./gradlew build --offline` 通过（Java 17）。
- `check-textures.py` 通过：313 模型 / 116 blockstate / 209 纹理。
- 主线 `check-data-paths.py --target 1.20.1`：`src/main/resources` 200 recipes / 10 tags；
  `src/generated/resources` 198 recipes / 75 loot / 71 tags。
- 全量 dev client（`-PtechMods -PdeMods -PdevMods -PctMods -PkjsMods`）加载了 Mekanism、
  Thermal ×3、Create（+Flywheel/Ponder）、IE、TConstruct + Mantle + TiT、AE2 + GuideME、
  ER + ZeroCore、BambooMod、DE（+CCL/BrandonsCore）、JEI、Jade、Just Enough Characters、
  CraftTweaker、KubeJS + Rhino + Architectury；**机器 GUI 压暗由作者实机确认**。
- 服务端 `-PtechMods`（含匠魂 3 + Mantle + TiT）：`[materials] loaded 25 entries`，叶绿素注册；
  58 tool definitions / 28 station layouts；无配方解析错误。
- **未做**：叶绿素实际熔化的画面/手感、JEI 工具页与配方页、聚合合成 / AOE、BambooMod 合成。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.0 新增：
> - 9 把金属锤（锡 / 铝 / 锌 / 铜 256，银 320，铅 384，镍 448，锇 512，铂 1920）。
> - 3 把 Bamboo 宝石锤（樱 / 枫 / 银杏，2048；仅装 BambooMod 时出现）。
> - 3 把龙之进化锤（双足飞龙 / 神龙 / 混沌；走聚合装置升级链，用 DE 能量与模块）。
> - 匠魂碎矿锤：装配站用锤头 + 坚韧手柄 + 2 大板合成，材料特性 / 强化槽 / 耐久全走匠魂，
>   掉落仍走我们的锤表。
> - 叶绿素三种碎矿可以直接在匠魂熔化炉里熔成熔融叶绿。
> 1.4.0 修复：
> - 打开机器界面时世界会正确压暗，不再直接盖在画面上。
> - 非自有锤（含匠魂锤）不再跳过耐久消耗；自动锤机器也会扣耐久。

**English**：

> 1.4.0 adds:
> - 9 metal hammers (tin / aluminium / zinc / copper 256, silver 320, lead 384, nickel 448,
>   osmium 512, platinum 1920).
> - 3 Bamboo gem hammers (sakura / maple / ginkgo, 2048; only with BambooMod installed).
> - 3 Draconic Evolution hammers (wyvern / draconic / chaotic; fusion-crafting upgrade chain,
>   run on DE energy and accept DE modules).
> - Tinkers' Construct crushing hammer: built at the Tinkers station from a hammer head, a tough
>   handle and 2 large plates; material traits, modifier slots and durability all come from
>   Tinkers, while drops still use our hammer table.
> - Chlorophyll ore pieces (broken / crushed / powdered) now melt directly into molten chlorophyll.
> 1.4.0 fixes:
> - Machine GUIs now dim the world behind them instead of sitting on top of it.
> - Hammers that are not our own HammerItem (including the Tinkers hammer) no longer skip
>   durability; the automatic hammer machine damages them too.

## 发布前检查

- [x] `mod_version=1.4.0`
- [x] `CHANGELOG.md` 的 1.4.0 节（中英双语）
- [x] README 下载表版本号
- [x] `build` + `check-textures.py` + `check-data-paths.py`
- [ ] CurseForge / Modrinth 上传（所有者）
