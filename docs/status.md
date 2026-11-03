# 当前状态（交接卡）

> 最后更新：2026-11-03
> 先看这 20 行；细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**双版本目标**：主线 **Minecraft 1.21.1 / NeoForge 21.1.x**（`main`）+ **1.20.1 / Forge 47.4**
（`1.20.1-forge` 分支，当前停在 M10，M1.5–M10 已全部铺完）。原来的 `legacy/1.20.1-forge` tag
已于 2026-11-01 按所有者要求删除，1.20.1 以分支为准（见
[incidents/2026-11-01-unauthorized-archive.md](incidents/2026-11-01-unauthorized-archive.md)）。
M1–M4、M6 已实现；**M10 自动化已完成并由作者验收**（电动 / 内能 × 自动筛 / 自动锤 + 6 个插件，
GUI、32×32 模型、工作动画、JEI、管道都过了目视/手感），**自动输出配置也已实现**（服务端 RCON 验过，
GUI 待目视）。**supported mods 框架 + Mekanism 已接**（1.21.1）。**P1 薄门面已完成**：core 只通过
`ConfigValues` / `Platform` / `MachineHooks` / `GuiLayers` 读平台，版本胶水收进顶层 `platform/`，
`check-core-imports.py` 的 known 清零。**P2 数据管线已完成**：生成器 `--target 1.21.1|1.20.1`、
`scripts/port_data.py`、`scripts/check-data-paths.py`。**1.20.1 分支已补到 M10**（M1.5 桶转化 + M2 筛子/矿链 + M3 锤/钩/尘土 + M4 坩埚 +
M6 巫水/孢子/瓷桶/木坩埚/12 种桶/蚕/人偶 + M10 四台机器/插件/自动输出 +
`crucible.*`/`machine.*` config；分支提交 `6a733e8`）。
顺带按原版修正了分支的木桶合成（4 板 + 台阶），并补上 `dust`/`crucible`/`unfired_crucible`
缺失的掉落表。1.20.1 侧只剩画面/手感验收；主线下一步是 P3 状态机拆分。
**M7 datagen（两条线各三批）**：`runData` 生成标签 41 + 方块掉落表 69 + **配方 143**
（`ModRecipes`）+ **74 个 blockstate + 201 个模型**（`ModBlockStates` + `ModItemModels`），
输出到 `src/generated/resources`；仍手写 57 条配方（45 条 `*_smelting_<mod>` 候选变体 +
12 条 `create:crushing`，产物都是对方模组的物品）、15 个手绘模型、全部语言文件。
1.20.1 分支在 2026-11-02 晚补齐同样三批，并再加 78 条匠魂熔炼，见 [datagen.md](datagen.md) §5。
**科技模组锭接入（2026-11-02）**：tin/lead → Mekanism → Thermal，silver/nickel → Thermal，
zinc → Create；**1.20.1 再补 Immersive Engineering**（silver/lead/nickel/aluminum/uranium）
和 **Tinkers' Construct 钴**（`tconstruct:cobalt_ingot`），运行时取第一个已加载模组的锭，
自有锭只作兜底。
**M9 发布：0.1.0 已发（两条线都有 tag）；0.2.0 内容与 jar 已备，tag / 商店上传待确认；
M8 月光魔法联动仍未开始。三路径 P1 基座 + P2 KubeJS 插件已完成**（TableBuilder + CustomizationOverlay +
`/exnihilo dump`，见当前批次；KJS/CT 入口仍待 P2/P3）。

## 当前批次（2026-11-03，P3：CraftTweaker，两版）

- **依赖**：`compileOnly` CraftTweaker，不随 jar 分发。1.21.1 用 21.0.39，1.20.1 用
  14.0.60。BlameJared maven 在本机 Gradle 上有 TLS handshake 问题，构建脚本用
  `downloadCraftTweaker` 任务经构建 JVM 下载到 `libs/crafttweaker/`（gitignored），
  再作为本地依赖编译；1.20.1 开发服用 `-PctMods` 把 deobf 的 CT 放进运行时。
- **入口**：`@ZenRegister` + `@ZenCodeType.Name` 的七个类：
  `mods.exnihilosatellitium.Sieve` / `.Hammer` / `.Crook` / `.Barrel` / `.Compost` /
  `.CrucibleMelt` / `.CrucibleHeat`。每个类有常用 `add` / `remove` / `replace` 与
  `addJson` / `removeJson` / `replaceJson`，内部构造 datapack JSON 后提交 overlay。
- **语义**：CT source `crafttweaker`，priority 200（数据包 0、KJS 100 之后）；
  `#tag`、`mod` 门、校验、replace 先校验后删旧规则全部复用。没有注册 CT recipe type，
  JEI 不会出现七张表的 CT 配方页（P4/可选）。
- **生命周期**：`onAddReloadListenerEvent` 在每次 reload 的清点阶段清空旧
  `crafttweaker` 贡献；CT 的脚本执行在该事件之后，所以 `/reload` 是替换而不是叠加。
- **验证（1.21.1，CT 21.0.39）**：
  - 七个入口各写一条，七张表条数/条目正确（sieve 91 drops、hammer 53、crook 23、
    barrel 13、compost 68、melt 40、heat 10）。
  - 改脚本（删 sieve、compost 300→150）后 `/reload`：`cleared 6 stale crafttweaker op(s)`，
    旧条目消失、新条目生效，overlay 6。
  - 同一 fixture 用 datapack 跑，七张表 `dump` 与 CT 逐字节一致。
  - 移除 CT jar 后服务端正常启动，无 `NoClassDefFoundError`。
- **验证（1.20.1，CT 14.0.60）**：
  - 七个入口全部生效；改脚本 `/reload` 清 7 条旧贡献、应用 6 条新贡献；
  - CT 与 datapack fixture 的七张表 `dump` 逐字节一致；
  - 不带 `-PctMods`（无 CT）正常启动；旧世界会提示 `Missing data pack mod:crafttweaker`，
    这是移除模组后的正常警告。
- **未做**：CT recipe type / JEI 分类（可选，后置）；P4 客户端 JEI 同步。

## 当前批次（2026-11-03，P2：KubeJS 插件，两版）

- **依赖**：`compileOnly` KubeJS + Rhino，不随 jar 分发。1.21.1 用
  `2101.7.2-build.348` / Rhino `2101.2.7-build.81`；1.20.1 用 `2001.6.5-build.26` /
  Rhino `2001.2.3-build.10`，开发服另需 Architectury `9.2.14`（`runServer -PkjsMods`）。
- **插件发现**：`src/main/resources/kubejs.plugins.txt` 指向
  `com.kaifengcrt.exnihilosatellitium.compat.kubejs.ExNihiloKubeJSPlugin`；
  KJS 不在时该文件不被读取，插件类也不会被加载（已实测无 KJS 启动）。
- **七个绑定**（只注册到 server 脚本）：
  `SieveEvents` / `HammerEvents` / `CrookEvents` / `BarrelEvents` / `CompostEvents` /
  `CrucibleMeltEvents` / `CrucibleHeatEvents`。每个都有常用的
  `add` / `remove` / `replace` 签名，以及 `addJson` / `removeJson` / `replaceJson`
  直接喂 datapack JSON（复杂桶实体规则、未来新字段走这条）。
- **语义**：绑定只构造 payload 并提交 overlay（source `kubejs`，priority 100），
  解析/校验/`#tag`/根级 `mod` 与数据包完全共用；脚本贡献在数据包和补丁之后应用。
- **生命周期**：`beforeScriptsLoaded` 在每次 server 脚本批次前清空旧 `kubejs` 贡献；
  应用由 tags rebuild 和 `ServerStartedEvent` 触发；脚本改完 `/reload` 不会残留旧条目。
- **1.20.1 差异**：2001 的 `KubeJSPlugin` 是类、绑定是 `BindingsEvent`（不是 2101 的
  `KubeJSPlugin` 接口 / `BindingRegistry`）；插件单独实现，在 `registerBindings`
  （server 脚本批次前）清旧 `kubejs` 贡献。绑定对象和脚本写法与主线完全相同。
- **验证（1.21.1，KubeJS 2101.7.2 + Rhino，dev server）：**
  - 七个绑定各一条：`[sieve] 90→91 drops`、hammer 52→53、crook 22→23、barrel 12→13、
    compost 67→68、melt 39→40、heat 9→10；`/exnihilo dump` 分别出现预期条目。
  - 脚本删除 sieve 条目、compost 300→150 后 `/reload`：`dropped 6 old op(s)`，
    sieve emerald 消失、compost=150，overlay 6 条。
  - 同一 fixture 用命令 overlay（priority 100）重放，七张表 `dump` 与 KJS 结果逐字节一致。
  - 移除 KJS/Rhino 后服务端正常启动，无 `NoClassDefFoundError`。
- **验证（1.20.1，KubeJS 2001.6.5 + Rhino 2001.2.3 + Architectury 9.2.14，dev server）：**
  - 七个绑定全部生效，七张表条数与主线一致（sieve 91 drops、hammer 53、crook 23、barrel 13、
    compost 68、melt 40、heat 10）；
  - 改脚本后 `/reload`：`dropped 7 old op(s)`，旧 sieve 条目消失、compost 300→150；
  - KJS 脚本与 datapack fixture 的七张表 dump 逐字节一致；
  - 不带 `-PkjsMods`（无 KJS）正常启动，无 `NoClassDefFoundError`。
- **未做**：专用服客户端 JEI 同步（P4）；CraftTweaker（P3）。

## 当前批次（2026-11-03，P1.5：overlay 边角）

- **显式 priority**：`CustomizationOverlay` 的操作带 `priority`（默认 0）；应用顺序是
  **priority 升序，再按提交顺序**，高 priority 后应用所以覆盖低 priority。
  命令形态：`/exnihilo customize <table> add|remove|replace [priority] <json>`。
- **冲突可见**：`TableBuilder` 记录 overlay 的 `replaced` / `removed` / `add ignored`，
  rebuild 后打印一行一条，带旧 source：
  - `[customize] hammer replaced minecraft:cobblestone (was exnihilosatellitium:hammer/cobblestone.json)`
  - `[customize] hammer add ignored for minecraft:cobblestone (kept ...)`
  - `[customize] barrel removed ...` / `sieve removed ...`
- **生命周期**：服务器停止时清空 overlay（单人切世界不会泄漏到下一个世界）；同一 source
  重复提交完全相同的操作会去重；总操作数上限 4096；空 payload 直接拒绝。
- **诊断**：`/exnihilo dump all` 一次输出七张表（298 行，7 个表头）。
- **验证**（2026-11-03，dev server + RCON）：
  - priority：先提交 `p10 cobblestone→flint`、再提交 `p5 cobblestone→gravel`，
    最终 dump 是 flint（p10 后应用）；冲突日志显示 p5 覆盖文件、p10 覆盖 p5。
  - `add ignored`：对已有文件规则的锤子 add 被拒绝并记录保留的 source。
  - 去重：同一 sieve add 提交两次，overlay 仍是 1 条。
  - 空 payload：`/exnihilo customize sieve add {}` 返回 rejected，overlay 不变。
  - 连续两次 `/reload`：dump 一致，conflict 日志重复但最终表不变。
  - 停止服务器：日志 `cleared 3 overlay op(s) on server stop`，重启后 `overlay 0 ops`。
- **1.20.1**：代码已同步，build + 纹理守卫通过；P1.5 的 RCON 复验见分支 `docs/status.md`。

## 当前批次（2026-11-03，三路径 P1：TableBuilder + overlay 基座）

- **新包 `common/table/`**：
  - `TableId` —— 七张表的唯一 id（`sieve` / `hammer` / `crook` / `barrel` / `compost` /
    `crucible_melt` / `crucible_heat`），命令、dump、未来的脚本绑定共用；
  - `TableBuilder` —— JSON loader 与 overlay 共用的有序工作集，记录每个 key 的 last source；
  - `CustomizationOverlay` —— 脚本/命令贡献按 source 排队，每次 rebuild 在**文件 + 补丁之后**
    重放，可按 source 清空；不持久化，重启即空；
  - `Tables` / `TableDump` —— 统一 rebuild 入口与稳定行输出。
- **七张表**的 rebuild 全部改为“建 builder → 喂 JSON/补丁 → 喂 overlay → 发布不可变快照”；
  原有语义保持：筛子/钩子合并掉落、锤子/熔点/热源具体优先、桶按 base+added 覆盖、
  堆肥同 key 取 max。启动日志：`[sieve] 6 inputs/90 drops`、`[hammer] 52`、
  `[crook] 10/22`、`[barrel] 12`、`[compost] 67`、`[crucible] 39 melt / 9 heat`。
- **命令（OP 2）**：
  - `/exnihilo customize <table> add|remove|replace <json>`：payload 与 datapack 文件同字段，
    复用同一套解析/校验；`/exnihilo customize list`、`clear [source]`；
  - `/exnihilo dump <table>`：按行排序、可直接 diff；头行 `# <table> N lines`
    （overlay 数量看 `/exnihilo tables`）。
- **失败语义**：单条 overlay 解析异常只跳过该条并打 `[customize]` 警告；整表 rebuild 抛异常时
  保留上一份快照（`rebuild failed, keeping previous snapshot`）。
- **验证**（2026-11-03，dev server + RCON）：
  - `./gradlew build`、`check-core-imports.py`（`common/table` 已纳入扫描）、
    `check-textures.py`、`check-data-paths.py`、`check-wiki.py` 全绿。
  - 七张表各跑 add：dump 只多一行；remove 掉刚加的条目后回到基线；筛子额外删了 sand 的
    cactus 验证“删现有掉落”；replace 后目标只剩新规则（sand 只剩 diamond 1.0、
    cobblestone → flint x1-3、oak_leaves 只剩 stick、water+sugar → glass、
    emerald=50、end_stone → water 100、furnace[lit=true]=0.9）。
  - 连续两次 `/reload` 后七张表 dump 与 reload 前**逐字节一致**（overlay 只重放一次，不叠加）。
  - `/exnihilo customize clear` 清掉 21 条后，七张表 dump 与启动基线**逐字节一致**。
  - 坏 overlay（`chance` 写成字符串）只打 warning，表不变；坏数据包（同一错误）触发
    `[sieve] rebuild failed, keeping previous snapshot`，dump 仍是旧表。
  - `replace` 先校验再删旧规则：`chance` 越界 / `speed: 0` / 未知 `result` 的 replace
    都保留原条目（筛子/热源/桶各验一次）。
  - tag payload：hammer 的 `#minecraft:logs` 展开 52 → 92 条，`clear command` 回到 52。
- **1.20.1**：已移植并单独验证（`common/table/` + 七张表 + 命令整体带过去；
  build + 纹理守卫、七张表 add/replace、`/reload` 一致、clear 回基线），
  见分支 `docs/status.md`「三路径 P1 移植」。
- **未做**：KJS / CT 的脚本绑定（P2 / P3）；overlay 持久化；专用服客户端 JEI 同步（P4）。

## 当前批次（2026-11-03，末影蛋糕）

- **新方块/物品 `exnihilosatellitium:end_cake`**：可放置蛋糕，吃一片**主世界 ↔ 末地**双向传送
  （主世界 → 末地中央岛；末地 → 玩家床/世界出生点）；下界不传送。放置时只有 **1 片**
  （`bites=6`），末影之眼右键补 1 片，最多 7 片；每次吃 +2 饥饿 / 0.1 饱和度，
  **始终可吃**（不要求饥饿值，保证末地回程）。顶面是末影之眼标志（绿色 end-stone 底 +
  teal 虹膜 + 黑瞳）。
- **配方**：8 末影珍珠 + 1 蛋糕（3×3 环，蛋糕居中），1.20.1 同配方。
- **资源**：几何复用 vanilla `minecraft:block/cake` / `cake_slice1..6` 父模型，只覆盖 4 张
  16×16 贴图（Creatio 原图经 `art/blocks/end_cake/restyle.py` 裁边/放大/描边）；
  无掉落表（和 vanilla 蛋糕一样，打掉不返还）。
- **验证**（2026-11-03，main）：
  - `./gradlew build`、`check-textures.py`、`check-data-paths.py`、`check-wiki.py` 全绿。
  - dev server：`Loaded 1434 recipes` / `our recipes=144`（+1 条 End Cake）；
    `/setblock ... end_cake[bites=0/1/5/6]` 均可放置并通过 `execute if block` 检查。
  - **未验**：进游戏右键吃/传送、末影之眼补片、模型画面；`getStateForPlacement` 的
    `bites=6` 需要物品放置才能看到（命令 `/setblock` 走默认 `bites=0`）。
- **1.20.1**：已同步同一套方块/物品/贴图/模型/配方，`compileJava` + `check-textures.py` 通过；
  服务端 `/setblock ... [bites=6]` 验过。

## 当前批次（2026-11-03，数据包 v2：标签 / mod 门 / 补丁）

- **标签输入**：`sieve` / `hammer` / `crook` / `barrel` / `crucible_melt` / `crucible_heat` 的
  id 字段接受 `#tag`（物品 / 方块 / 流体标签），加载时展开；堆肥原本就支持 `#tag`。
- **时序坑（重要）**：`TagManager` 只在 reload 里存结果，标签在所有 listener 之后才绑定，
  所以 `load()` 只建具体条目，`TagsUpdatedEvent` 里再 `rebuild()` 展开标签。日志会先出现
  `waiting for tags`，随后出现完整条数，这是正常的，不是漏加载。
- **mod 门**：所有表支持根级 `"mod": "x"`；筛子 / 钩子的单条掉落也支持 `mod`。
- **补丁**：`"patch": true`（或出现 `"remove"`）在所有普通文件之后按资源 id 排序应用；
  `remove: true` 删整条规则，`remove: ["id", "#tag"]` 删列表条目（筛子 / 钩子 / 堆肥）。
  具体条目优先于标签；筛子 / 钩子的标签是合并，锤子 / 熔点 / 热源是具体优先。
- **诊断命令**（OP）：`/exnihilo tables`、`/exnihilo sieve <item>`、
  `/exnihilo hammer <block>`、`/exnihilo compost <item>`。
- **示例包**：`examples/datapack/`（`pack_format 48`），可直接丢进 `world/datapacks/`。
- **玩家 wiki**：`wiki/` 按「数据包 / KubeJS / CraftTweaker」三条路线写，覆盖 1.21.1 与
  1.20.1 的路径/标签差异；CT 明确写清只改原版配方、碰不到七张表。
- **维护契约**：`wiki/MAINTAINING.md` 定义 Owner / Definition of Done / 发布前检查；
  `scripts/check-wiki.py` 守卫页面、链接、JSON、版本矩阵和“最后核对”头。
- **三路径路线图**：`docs/customization-roadmap.md` 列出当前能力矩阵、P1（规范表 + overlay 基座）、
  P2（KJS 插件）、P3（CT API）、P4（客户端同步）的验收标准；**当前只有数据包是完整路径**。
- **验证**（2026-11-03，main）：
  - `./gradlew build`、`check-textures.py`、`check-core-imports.py`、`check-data-paths.py`、
    `check-wiki.py` 全绿。
  - dev server + 测试包（`#minecraft:logs` / `#minecraft:leaves` / `#minecraft:saplings` 标签、
    sand 补丁、end_stone 删除、水 + 牛奶删除、tconstruct mod 门、面包 125→100、tag 删树苗）：
    - 启动：先 `waiting for tags`，随后 **测试包下** `[sieve] 45 inputs, 115 drops`、
      `[hammer] 91`、`[crook] 32 drops`、`[crucible] 49 heat`、`[barrel] 11`；
      无 unknown item / empty tag 警告。
    - RCON：`sieve minecraft:sand` 已无 cactus、有 0.5 钻石；`sieve minecraft:oak_log` 有 1%
      钻石（tag）；`hammer minecraft:oak_log` → 木棍 1–2；`hammer minecraft:end_stone` → no rule；
      `compost minecraft:bread` → 100；删掉补丁文件后 `/reload` 恢复 `hammer 91`、`barrel 11`。
- **1.20.1**：已移植并单独验证，见分支的 `docs/status.md`「数据包 v2 移植」。
- **未做**：专用服客户端 JEI 仍只读本地镜像 / 本地数据包；KubeJS / CraftTweaker 仍无集成；
  下一步是 roadmap 的 **P1（TableBuilder / overlay 基座）**，之后才谈 KJS/CT 等价。

## 0.2.0 发布准备（2026-11-02）

- 两条线 `mod_version=0.2.0`；`CHANGELOG.md` / `README.md` / [release-0.2.0.md](release-0.2.0.md)
  已更新。
- 产物：主线 `build/libs/exnihilosatellitium-0.2.0.jar`（657 KB，
  sha256 `fff01fc6…2624cc7`）、1.20.1 分支同名 jar（663 KB，sha256 `e579e3a1…cc1ff`）。
- 两个 jar 的 `mods.toml` 版本号、`LICENSE` / `THIRD-PARTY.md` / `icon.png` 均已核对。
- **待所有者确认**：版本号 / 发布日期、tag（`v0.2.0` / `v0.2.0-1.20.1`）、
  CurseForge / Modrinth 上传、README 下载链接。

## 0.1.0 已发布（2026-11-02）

- **tag / 远端**：主线 `v0.1.0`（`d4a6dc2`）、1.20.1 分支 `v0.1.0-1.20.1`（`35d98d4`），
  两条线都已推到 `origin`（推送走 SakuraCat 代理：`ssh.github.com:443`，见下方常用命令）。
- **产物**：`build/libs/exnihilosatellitium-0.1.0.jar`（1.21.1 / 649 KB，
  sha256 `2f3d1af5…ecb49`）、分支同名 jar（1.20.1 / 587 KB，sha256 `1168ca77…3e1ba`）。
- **打包口径**：jar 里剔除 datagen 的 `.cache/`，带上 `LICENSE` + `THIRD-PARTY.md` 与
  `icon.png`（`art/icon/generate.py`，128×128）；与源码比对 0 缺失，只多出给 JEI 用的
  `assets/.../{barrel,sieve,...}` 镜像。
- **发布材料**：`CHANGELOG.md`（首版内容 / 兼容矩阵 / 已知限制 / 安装）、README「下载与安装」。
- **还差**：CurseForge / Modrinth 的商店页与文件上传需要所有者操作（本机没有 API key 与 `gh`），
  文案见 CHANGELOG；上传后再把下载链接补进 README。

## 当前批次（2026-11-02 晚，Tinkers' Construct 3 接入）

- **碧空 / 末影黏液草种子**：`sky_slime_grass_seeds` / `ender_slime_grass_seeds`，
  **只在 `tconstruct` 在场时注册**（`ModItems` 条件注册；缺失时字段为 `null`，创造栏/JEI 不出现）。
  右键**草方块或泥土** → `tconstruct:sky_vanilla_slime_grass` / `ender_vanilla_slime_grass`；
  泥土筛 2% / 1%，筛表条目带 `"mod": "tconstruct"`，没匠魂时整条不参与加载、也不刷 unknown item。
  贴图 / 模型手写，因为 datagen 在无匠魂时不会注册这两个物品。
- **钴**（第 13 种材料）：`materials.json` 加 `cobalt`，候选 `tconstruct:cobalt_ingot`；
  无匠魂时回退自有 `cobalt_ingot`。碎下界岩筛 `cobalt_broken` 1%，走普通 4→块→熔炼链；
  **broken 贴图改用下界岩红棕基底**（`STONE_PALETTES`），不再用其他矿碎块的煤炭灰底。
- **下界合金碎片**：碎下界岩 0.4%，**双线都加**。
- **匠魂熔炼**：13 材料 × 6 形态 = **78 条** `tconstruct:melting`（1.20.1 分支）：
  - 碎块 / 粉碎 / 粉末物品 → **60 mB**（1/2 粗金属）；
  - 对应矿砾 / 矿砂 / 矿尘方块 → **240 mB**（2× 粗金属）；
  - 流体用 `forge:molten_<m>`，温度取 Tinkers 自己的锭配方，时间按 90 mB 锭等比缩放；
  - 全部带 `conditions: mod_loaded(tconstruct)`，没匠魂时整批跳过。
- **阿迪特暂不做**：Tinkers 3.12.1 已移除阿迪特（无 `molten_ardite` / `ardite_ingot`），
  等「工匠巧思」拓展（重现 1/2 代阿迪特）再按同一套加。
- **验证**：
  - 1.20.1 只装 Tinkers 3.12.1 + Mantle 1.11.117：`[materials] cobalt → tconstruct:cobalt_ingot`；
    `[sieve] 6 inputs, 92 drops`；RCON 熔炉 `cobalt_ore_gravel → tconstruct:cobalt_ingot`；
    `summon` 两个种子物品名字正常；目标黏液草方块 `setblock` 成功。
  - 不装 Tinkers：`[sieve] 90 drops`（两个种子条目被 `mod` 过滤掉），无 unknown recipe/item 报错。
  - 主线 1.21.1：`[sieve] 90 drops`、`Loaded 1433 recipes`、`[compost] 67 entries`。
  - **右键草方块/泥土、匠魂炉实际熔化、JEI 页仍未进游戏**。
- **配方计数**：`runData` 生成 **143** 条（137 + cobalt 6）；主线手写 **57** 条
  （45 条候选变体 + 12 条 `create:crushing`）；1.20.1 再手写 78 条匠魂熔炼 → **135** 条。

## 上一批次（2026-11-02，Immersive Engineering 接入）

- silver / nickel → Thermal → IE；lead → Mek / Thermal → IE；aluminum → IE；uranium → Mek → IE；
  15 条熔炼变体，IE 粉碎机走 `forge:ores/<m>` 标签（不补配方）。
- 只装 IE 时 5 材料全部解析到 IE；Thermal+Create+IE 同时装时优先级正确；主线无 IE 回退自有铝锭。

## 更早批次（2026-11-02，树的起点：树苗 / 竹子 / 下界菌岩孢子）

- 泥土筛 9 种树苗（合计 31%/次）+ 竹子 5%；碎下界岩筛菌岩孢子各 5% + 骨粉催菌/巨菌；
  新物品贴图 / 模型 / 堆肥 100。当时 `[sieve] 88 drops` / `[compost] 67 entries`。

## 历史批次（详细见 [history.md](history.md)）

- **2026-11-02**：M7 datagen 第二批（配方 133 条）、第三批（blockstate + 模型 244 个）、
  科技模组锭接入（Mekanism / Thermal / Create）、泥土筛石子 + Create 粉碎桥。
- **2026-11-01**：1.20.1 分支 M2–M10（筛子/矿链、锤钩、坩埚、巫水/瓷桶/蚕/人偶、机器）、
  P1 薄门面、P2 数据管线、M6 六刀、M10 自动化与自动输出配置。

## 功能现状

### 桶 / 堆肥 / 筛子

- 十二种桶（11 种木材 + 石桶）共享一个 BE；木桶 ≤433 K，石桶不限温；堆肥 1000 分制、50 秒成土；
  接雨水、比较器、破坏返还、管道状态互斥都已实现。
- 桶转化 12 条：水+熔岩→圆石、熔岩+水→黑曜石、熔岩+红石→下界岩、熔岩+荧石→末地石、
  水+尘土→粘土、水+牛奶→史莱姆；巫水+沙→灵魂沙、巫水+树苗→深色橡树苗；
  5 条人偶规则（熔岩+愤怒→烈焰人、巫水+诡异→末影人、巫水+漂浮→潜影贝、水+守卫者→守卫者）。
- 筛子 6 输入 / 88 掉落（砂砾 / 沙 / 泥土 / 尘土 / 粉碎下界岩 / 粉碎末地石）；
  三档筛网只影响耐久。
- 矿物链：4 碎块 → 矿方块 → 熔炼成锭；锤矿砾/矿砂掉 4–6、粉尘掉自身。
- **泥土的石头来源**：四种石子（圆石/安山岩/花岗岩/闪长岩）每次 4-6 个、100% 出，
  四个合成一块对应石头。
- **泥土的树苗来源**：橡木 8% / 白桦 4% / 云杉 4% / 丛林 3% / 金合欢 3% +
  **樱花 3% / 红树 3% / 杜鹃 2% / 盛开的杜鹃 1%**（共 9 种、合计 31%；经典 Ex Nihilo 的树种子位）。
- **竹子来源**：泥土筛 5% 出 `minecraft:bamboo`（原版竹子物品就是竹笋）；竹桶、竹坩埚、
  脚手架和竹子堆肥从此有第一个来源。
- **下界树起点**：粉碎下界岩筛出**绯红 / 诡异菌岩孢子**各 5%，右键下界岩变成对应菌岩；
  之后用原版骨粉催菌、再催巨菌，得到绯红 / 诡异菌柄（木桶、木坩埚的来源）。
- **M6 补的筛出物**：泥土 → 草种子（右键泥土变草方块）/甜浆果；沙 → 可可豆/海带/5 种珊瑚/
  海晶砂粒/海晶碎片；锤下界岩 → **粉碎下界岩方块**（筛石英/荧石/下界疣/烈焰粉/岩浆膏/灵魂沙）、
  锤末地石 → **粉碎末地石方块**（筛紫颂果/末影珍珠）。两者都是方块类筛料，走原来的方块料层渲染；
  `SieveRenderer` 仍保留了非方块输入的图标兜底。

### 锤 / 钩

- 6 档锤（木/石/铁/金/钻石/下界合金），耐久 64/128/256/32/1561/2031；金/钻石工作台，
  下界合金锻造台。
- **只有左键破坏**（无右键）；`HarvestCheck` + `BreakSpeed` 6.0 让锤表方块可收获且不慢；
  创造模式用 `BlockEvent.BreakEvent` 补掉落；耐久扣真实主手栈。
- 木/骨钩；骨钩叶表概率 ×1.5；蚕已接 M6（右键树叶 → 侵染树叶 → 线）。

### 坩埚（M4）

- 瓷土（粘土球+骨粉）→ 7 瓷土合未烧制坩埚 → 熔炉烧成坩埚。
- 19 条熔点：圆石/石头/砂砾/花岗岩/闪长岩/安山岩（含磨制、深板岩、凝灰岩、方解石）250 mB，
  下界岩 1000 mB，雪块 500 mB 水，冰 1000 mB 水，沙子/红沙/尘土 100 mB。
- 熔料体积 = 产出量（1:1）；默认熔料上限 2000 mB（8 块圆石）。
- 热源必须在正下方；`lit=true` 区分点燃/熄灭的熔炉、篝火。

### 巫水（M6，2026-10-31）

- 自定义流体 `exnihilosatellitium:witch_water`（still/flowing + `FluidType` + 桶 + 液方块），
  紫色贴图由 `art/blocks/witch_water/generate.py` 生成。
- **古代孢子**（`ancient_spores`，紫色）：沙筛 1/128；右键泥土 → 菌丝；右键牛 → 哞菇（`common/item/AncientSporesItem`）。
- **发酵**：满水桶 + 3×3×3 内有菌丝，每 tick 进度 `+1 + 菌丝数/2`，满 1000 tick（1 个菌丝 50 秒）
  整桶变巫水；直接递蘑菇煲可无菌丝起酵（同样 50 秒，退回碗）。
- **巫水转化**两条：+沙子 → 灵魂沙；+橡树苗 → 深色橡树苗（`barrel/witch_water_*.json`）。
- **木坩埚**（Creatio 的 Wooden Crucible）：**11 种木材变种**（橡木 `wooden_crucible`、云杉、白桦、丛林、
  金合欢、深色橡木、红树、樱花、竹，以及下界的绯红/诡异菌柄），各用**自己的原木** + **任意木台阶** 合成；
  **不需要热源**，按 0.5 mB/t 被动熔化（一块树叶 ~10 秒），只收水类熔料（树叶 100 mB、树苗 250 mB）；
  底下有没有热源都一样，装熔岩会被拒（433 K 上限，和木桶一致）。
- **蚕 / 侵染树叶**（M6）：`silkworm` 右键树叶 → `infested_leaves`（BE 记住原树叶外观）；
  进度 0.0005/tick（100 秒满），超过 60% 后每 100 tick 向 3×3×3 内的树叶扩散一次；
  破坏时按进度掷 `线`（progress 概率 + 1/4 再次判定；**创造模式不掉落**）。渲染 = BER 画回原树叶，
  用 `WhiteningVertexConsumer` 把顶点色按进度混向白色（保留树叶纹理，不再盖白盒）+ 白灰粒子；
  **Jade** 显示原树叶名与侵染百分比。物品形态 `infested_leaves` 带 `infestation` 组件：
  创造栏里 0% 是「侵染中的树叶」、100% 是「被侵染的树叶」，放下即按组件设进度。
- **人偶**（M6）：`doll`（7 瓷土 + 钻石/绿宝石）→ `angry_doll`（烈焰粉×4 + 下界疣 + 荧石×2 + 红石）、
  `creepy_doll`（墨囊×4 + 下界疣 + 青金石×2 + 红石），两者都有「红石↔下界疣」互换的镜像配方；
  `shulker_doll` 漂浮人偶（紫染料×4 + 末地石 + 荧石×2 + 末影珍珠）、
  `guardian_doll` 守卫者人偶（海晶砂粒×4 + 红石 + 荧石×2 + 生鲑鱼）。
  桶规则：熔岩+愤怒→烈焰人、巫水+诡异→末影人、**巫水+漂浮→潜影贝**、**水+守卫者→守卫者**，
  **右键立即生效**（不再有 30 秒烹饪）：非和平直接生成生物，和平掉烈焰棒 / 末影珍珠 / 潜影壳 /
  海晶碎片或海晶砂粒（随机），桶恢复空桶。
- **瓷桶**（`porcelain_bucket`，物品）：通用 1000 mB 容器，能装熔岩/巫水/牛奶；
  内容存 `exnihilosatellitium:fluid_contents` 组件，`IFluidHandlerItem` 已接。
  3 瓷土按原版桶形（`x x` / ` x `）→ 未烧制瓷桶 → 熔炉烧 200 tick → 可用瓷桶。

### 自动化（M10，2026-11-01）

- 四台机器共用 `MachineBlock`（kind = 筛/锤，power = 电动/内能），BE 分 `ElectricMachineBlockEntity` /
  `FuelMachineBlockEntity`，GUI 分 `ElectricMachineMenu` / `FuelMachineMenu`。
- 槽位：`0` 工具、`1` 输入、输出 12 格（3×4）；电动另有 `2..5` 插件，内能 `2` 燃料。
- 工具消耗：筛网每件 −1 耐久、锤每件 −1 耐久，坏掉即消失；输出满时停机，剩余产物掉在机器上方。
- 电动无电 / 内能无燃料时停机，`Work` 归零，不消耗输入。
- 管道：下方漏斗只抽输出格；上方漏斗可补料。电动机器暴露只进不出的能量能力，内能机器没有能量能力。
- 内能燃料接受任何 `getBurnTime(RecipeType.SMELTING) > 0` 的物品；熔岩桶退回空桶。
- **自动输出**：`AutoOutput` + `OutputMask`（默认关、默认只推底面）；完成一件立刻推，20 tick 重试；
  配置入口是机器面板左侧突出的标签页，点开的配置面板也向左伸出（`MachineOutputScreen` 叠加层），
  不改菜单；面按钮用玩家相对方向（上下前后左右）。掩码只管自动弹出，管道/漏斗仍可从任意面抽输出格。

### 配置与 JEI

- `config/exnihilosatellitium-common.toml`：`crucible.capacity`、`crucible.meltCapacity`、
  `crucible.heatSpeedMultiplier`，运行时读取。
- `machine.ticksPerOperation` / `energyPerTick` / `energyCapacity` / `burnPerOperation`（M10）。
- JEI 分类：`sieve`、`barrel`、`barrel_compost`、`hammer`、`crook`、`crucible`、`crucible_heat`；
  锤子页隐藏“粉尘→自身”的 no-op 规则（玩法 47，展示 35）。

## 交接清单（下次会话先做）

- **1.20.1 分支**：同级 git worktree `../Ex-Nihilo-Satellitium-1.20.1`
  （`git worktree add ../Ex-Nihilo-Satellitium-1.20.1 1.20.1-forge` 可复现；
  `1.20.1-forge` @ `a564ab1`，含一次性验收清单），已到 **M10**（含 `crucible.*` + `machine.*` config），
  下一步只剩一次性画面/手感验收。
  分支的 `docs/status.md` 里有当前批次、验证表、构建/客户端命令（`runClient -PdevMods`）
  和固定套路。新会话在分支工作区开工即可。

- **先读** [porting.md](porting.md) §3 的 P2 记录；本批从 **1.20.1 分支补 M2** 开始（清单见「下一步」）。
- **先跑** `./gradlew build` + `python3 scripts/check-textures.py` + `python3 scripts/check-core-imports.py`
  + `python3 scripts/check-data-paths.py`（后两条必须 `0 known leak(s)` / `passed for 1.21.1`）。
- **datagen 自检**：`./gradlew runData --offline` 后 `git status` 应为空（已迁移的两批稳定）；
  配方要重新比对时用 `python3 scripts/compare-recipes.py --expect-old-only '*_mekanism.json'`。
- **数据管线验收（不需要进游戏）**：
  `python3 scripts/port_data.py --out build/port-1.20.1 --force` →
  `python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1`；
  四个生成器重跑不应产生 diff（`git status --short` 应为空）。
- 进游戏验收（服务端逻辑都已验过，剩下“看得见/摸得着”的）：
  - **M4 坩埚**：1 块圆石只占 12.5%、8 块满 2000、第 9 块被拒；沙子/尘土 100、花岗岩 250；
    桶/瓶抽流体；熔料↔流体显示切换；Jade 熔料剩余 + 热源速度；三个配置键热改；JEI 两页。
  - **M6 人偶**：5 种配方（含镜像摆法）；满桶右键立即出结果（和平掉战利品 / 非和平生成生物）；桶变空。
  - **M6 木坩埚**：11 种变种的外观与合成；树叶/树苗 → 水的手感（0.5 mB/t）；熔岩被拒的提示。
  - **M6 蚕**：右键树叶 → 侵染；变白进度；Jade 百分比；扩散；破坏掉线；创造模式不掉落。
  - **M6 瓷桶**：未烧制 → 烧制；舀水/熔岩/巫水；倒进木桶/坩埚。
  - **M3**：6 锤/2 钩、左键链、右键无反应、矿砾/矿砂 4–6、锤钩 JEI 35/10、蚕动画。
  - **M2/M6 筛子**：6 个输入、88 掉落；JEI 筛子页；粉碎方块平铺料层；粉碎下界岩两页（旧产物 + 菌岩孢子）。
  - **M10 机器**：四台机器右键开 GUI；能量条/火焰条/进度条动；插件槽只收插件、燃料槽只收燃料；
    筛网/锤在 GUI 里显示耐久；破坏机器掉出全部内容；贴图观感；上下漏斗在游戏里再走一遍。
  - **M10 自动输出 GUI（第二版）**：左侧标签页的观感/悬停/变绿；配置面板从左边伸出、与面板齐平、
    不挡机器 GUI；6 个面按钮（上/下/前/后/左/右）与 tooltip 世界方向对得上；绿色边框选中态；
    关掉最后一个面会自动关总开关；Esc/E 返回；开/关后状态下发与 `data get` 对得上。
  - **筛料渲染**：新料层不再穿过盘壁、不再和盘边交替闪烁；变薄动画照旧（三种筛料各看一遍）。
  - **木桶竖棱**：木桶/石桶四条竖棱不再是细条纹；俯视/平视都看一遍。
  - **机器镐挖掘**：木镐不掉落、石镐及以上正常掉机器（含内容物掉落逻辑不变）。
- **未验证**：上面这些画面/交互，以及 P1 重构后的客户端实测（服务端已 RCON 验过）、桶/筛旧内容目视。
- 不要提交 `build/`、`run/`、`reference/`、`libs/`。

## 已完成的里程碑

| 里程碑 | 内容 | 记录 |
|---|---|---|
| M1 | 木桶：堆肥 1000 分制 → 泥土、1000 mB 流体、比较器、七材质、内缩模型 | [DESIGN §8/§10](DESIGN.md) |
| M1.5 | 水/熔岩凝固、木桶限温 433 K、接雨水、熔岩掺红石/荧石；转化表 JSON 化 | [DESIGN §13/§14](DESIGN.md) |
| M2 | 筛子 + 三档筛网 + 数据驱动掉落 + JEI；4 碎块 → 矿方块 → 熔炼成锭 | [DESIGN §12](DESIGN.md) |
| 桶 JEI | `barrel` / `barrel_compost` 两个分类，读同一张 `BarrelRecipes` / `CompostRegistry` | [DESIGN §14](DESIGN.md) |
| M3 | 6 锤 / 2 钩 / `dust` / 蚕动画 / 熟蚕 / 粘土 / 47 条锤表 / 锤钩 JEI | 🚧 待验收（[DESIGN §15–16、§18](DESIGN.md)） |
| M4 | 坩埚 + 热源；19 熔点 / 9 热源；容量可配；Creatio 模型；熔料/流体显示；Jade；JEI 两页 | 服务端✅ / 画面🚧（[DESIGN §17](DESIGN.md)） |
| M6 | 巫水 / 古代孢子 / 瓷桶 / 11 种木坩埚 / 12 种木桶 / 蚕 / 5 种人偶 / 筛出物扩充 | 服务端✅ / 画面🚧（[DESIGN §21–26](DESIGN.md)） |
| M10 | 电动/内能 × 自动筛/自动锤 + 速度/时运插件 + 自动输出配置；手动与机器共用判定函数 | ✅（GUI/模型/动画作者已验收；自动输出 GUI 待目视）（[DESIGN §27](DESIGN.md)） |

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| P3 CraftTweaker | ✅ | 两版七张表 ZenScript 入口 + JSON 逃生口；CT、datapack fixture dump 逐字节一致 |
| P3 CT 生命周期 | ✅ | reload 清旧贡献；无 CT 正常启动（1.20.1 有旧世界 missing pack 警告） |
| P2 KubeJS 插件 | ✅ | 两版七张表绑定 + JSON 逃生口；KJS、命令 overlay、datapack fixture 三者 dump 逐字节一致 |
| P2 KJS 生命周期 | ✅ | 2101 用 `beforeScriptsLoaded`、2001 用 `registerBindings` 清旧贡献；无 KJS 正常启动 |
| P1.5 priority / conflicts | ✅ | p10 先提交仍赢 p5；`add ignored` / `replaced` / `removed` 带旧 source；两版 RCON |
| P1.5 生命周期 | ✅ | 重复提交去重、空 payload 拒绝、`dump all` 298 行、server stop 清理 3/4 条 |
| P1 TableBuilder / overlay | ✅ | 2026-11-03：七张表 add/remove/replace；`/reload` 前后 dump 逐字节一致；clear 21 条后回基线 |
| P1 `/exnihilo dump` | ✅ | 七张表排序行输出（RCON 可读）；头行只有表名 + 行数，便于跨路径 diff |
| P1 失败隔离 | ✅ | 坏 overlay 只 `[customize]` warning；坏数据包 `[sieve] rebuild failed` 且旧表不变 |
| P1 core 守卫 | ✅ | `check-core-imports.py` 已纳入 `common/table`，`0 known leak(s)` |
| P1 1.20.1 移植 | ✅ | 2026-11-03：分支 build + 纹理守卫；七张表 add/replace、`/reload` 一致、clear 回基线 |
| `./gradlew build` | ✅ | 2026-11-02：`BUILD SUCCESSFUL`（`--offline`）；JEI 依赖走 `maven.blamejared.com` |
| `python3 scripts/check-core-imports.py` | ✅ | core 层无 NeoForge import；`0 known leak(s)` |
| `python3 scripts/check-textures.py` | ✅ | 方块/物品 16×16、蚕动画 16×64 + mcmeta、坩埚贴图存在；无 `minecraft:item/*` 引用 |
| 数据表 JSON | ✅ | 50 hammer / 10 crook / 19 melt / 9 heat / 5 barrel / 67 compost / 6 sieve；全部可解析 |
| 客户端启动（最后一次） | ✅ | 2026-10-31 00:16：`[hammer] 47`、`[crook] 10`、`[crucible] 6`、`[jei] 47/6/5/3/10`；无 missing model/texture |
| 客户端启动（当前代码） | ✅ | 2026-10-31 09:26 客户端日志：`[jei] 35 hammer` / `19 meltables, 9 heat sources` / `5 barrel, 65 compost` / `4 sieve` / `10 crook`；`[sieve] 4 inputs, 52 drops`；无 missing model/texture |
| 桶：堆肥→泥土 | ✅ | 2026-10-29：苹果 +100 / 仙人掌 +100 / 树叶 +125，满 1000 后 `dirt=true` |
| 桶：流体装填 | ✅ | 2026-10-29：岩浆桶倒入成功；石桶 NBT 读回 `minecraft:lava` / 1000 |
| 桶：水+熔岩 / 熔岩+水 | ✅ | RCON：圆石 / 黑曜石两条 Solid 反应通过 |
| 桶：接雨水 | ✅ | `/weather rain` 12 秒积水 80 mB |
| 桶：木桶拒热 | ⚠️ | `tank.fill` 按 433 K 拒绝；消息/手感未实测 |
| 桶：红石/荧石掺料 | ⚠️ | 已进 `barrel/*.json`；未玩家实测 |
| 桶：流动水 → 黑曜石 | ✅ | RCON：满熔岩桶，上方放水源、桶顶格是 `water[level=8]` 流动水，1.5 秒后 `Solid=obsidian`（`FlowingFluid#getSource()` 归一化） |
| 桶：水+尘土→粘土 | ⚠️ | JSON 已写；未实测 |
| 桶：水+牛奶→史莱姆/粘液球 | ✅ | RCON 2026-11-02：满水 + 牛奶桶 → 桶清空、空桶返还；Normal 生成史莱姆、Peaceful 掉 `slime_ball` |
| 巫水：满水 + 菌丝发酵 | ✅ | RCON：满水木桶 + 2 菌丝，约 25 秒变 `exnihilosatellitium:witch_water`，`Spore` 归零 |
| 巫水：蘑菇煲起酵 | ✅ | RCON：只写 `Spore:1` 无菌丝，约 50 秒后变巫水 |
| 巫水：流体方块 | ✅ | RCON `setblock ... exnihilosatellitium:witch_water` 成功并可被 `execute if block` 命中 |
| 巫水：桶装/倒 | ⚠️ | `witch_water_bucket` 已注册；未玩家实测 |
| 巫水：两条转化 | ⚠️ | JSON 已加载（`[barrel] 7 transforms`）；未玩家实测 |
| 古代孢子：沙筛 | ⚠️ | `sieve/sand.json` 1/128；未实测 |
| 古代孢子：泥土→菌丝 / 牛→哞菇 | ⚠️ | 逻辑已写；未实测 |
| 巫水：画面/桶纹理 | ⚠️ | 流体用原版 water_still/flow + 紫色 tint；桶图标由原版水桶重着色；未进游戏目视 |
| 木坩埚：被动熔化 | ✅ | RCON：放树叶 10 秒 100 → 1.0 固体 / 99 mB 水（≈0.5 mB/t）；底下加篝火速率不变 |
| 木坩埚：11 种木材变种 | ✅ | RCON：11 个变种 `setblock` 后 `execute if block` 全部命中；生成脚本 `scripts/generate_wooden_crucible_variants.py` |
| 木坩埚：水类限定 | ⚠️ | `canAccept` 拒非水熔料、`fill` 拒 >433 K；未用熔岩桶实测 |
| 瓷桶：物品/组件 | ✅ | RCON：`summon item` 带 `fluid_contents` 可读回熔岩 1000 / 巫水 1000 |
| 瓷桶：画面/舀水/倒液体 | ⚠️ | 模型 predicate、tint、右键舀水、倒进木桶/坩埚均未玩家实测 |
| 瓷桶：合成/烧制 | ⚠️ | 3 瓷土（`x x` / ` x `）→ 未烧制瓷桶 → 烧制；未实测 |
| 桶 JEI | ✅ | 转化 1/2 页、堆肥 1/22 页已目视；[截图](images/jei-barrel-transform.png) |
| 筛子（1.21.1） | ✅ | 作者已实际筛出产物；2026-10-31 服务端日志 `[sieve] loaded 4 inputs, 52 drops`（泥土/沙两档新增未实测） |
| 筛子 JEI | ✅ | 7×3 + 166×60 已目视；[截图](images/jei-sieve.png)（还是 3 输入时的样子） |
| 筛子：泥土 → 种子/作物 | ⚠️ | `sieve/dirt.json` 6 条（小麦 15% / 甜菜·土豆·胡萝卜 6% / 西瓜·南瓜 5%）；服务端已加载，未实测 |
| 筛子：沙 → 仙人掌/甘蔗 | ⚠️ | `sieve/sand.json` 补 2 条（1/32、1/24）；未实测 |
| 筛子：M6 新资源 | ✅ | 服务端 `[sieve] 6 inputs, 72 drops`、`[hammer] 49 recipes`；粉碎下界岩/末地石可放置并掉自己 |
| 蚕：侵染树叶 / 扩散 | ✅ | RCON：进度 0.65 → 0.849（20 秒，0.0005/tick 对得上）；20 秒内 4/8 邻近树叶变侵染 |
| 蚕：右键转化 / 掉线 | ⚠️ | 逻辑已写；需玩家右键树叶实测、破坏看掉落 |
| 蚕：渲染 / 变白 | ⚠️ | BER 画回原树叶 + 白色遮罩（alpha 随进度）+ 白灰粒子；未目视 |
| 人偶：烹饪生成 | ✅ | RCON：写入 `CookEntity` 后非和平模式刷出烈焰人；和平模式掉烈焰棒 |
| 人偶：配方 / 桶规则 | ⚠️ | 8 条配方 + 4 条桶规则已加载（`[barrel] 11 transforms`）；未玩家实测 |
| 人偶：潜影贝 / 守卫者 | ✅ | RCON：Normal 难度刷出潜影贝/守卫者；和平掉落列表随机取一个（实测海晶砂粒/海晶碎片各一次） |
| 人偶：右键链路 | ✅ | 用 FakePlayer 走真实 `use()`：满水桶 + 守卫者人偶 → `use=SUCCESS`、`CookTicks=600`、`CookEntity=guardian`；30 秒后和平模式掉海晶碎片 |
| 人偶：即时转换 | ⚠️ | 现在任何难度都是右键立即生效（和平掉落 / 非和平生成）；`cook_ticks`/Jade 倒计时只给旧存档兜底，未再实测 |
| 蚕：Jade | ⚠️ | 显示 `原树叶：…` + `侵染：xx%`（成熟后显示「已成熟」）；未目视 |
| 粉碎下界岩/末地石：外观/筛 | ⚠️ | 方块已注册；未进游戏看贴图、未实际筛 |
| 草种子 | ⚠️ | `grass_seeds` 右键泥土 → 草方块；未实测 |
| 矿物链 | ⚠️ | 4 块→方块→熔炼已 RCON 通过；锤降档未玩家实测；9 锭未目视 |
| M3 锤/钩玩法 | ⚠️ | 左键链路/耐久/右键无反应/4–6 掉落未实测 |
| M3 贴图 | ✅ | 9 张 Creatio 采用 + 1 张换色 + 蚕 4 帧动画；[预览](images/m3-textures.png) |
| M3 收尾 | ⚠️ | 粘土/砂岩/熟蚕 JSON 已写，未实测 |
| M4 坩埚方块 | ⚠️ | 两个方块 + BE + 流体 capability + 比较器；仅右键投放/抽流体未实测 |
| M4 熔点/热源表 | ✅ | 19 + 9 条 JSON；服务端启动日志 `[crucible] loaded 19 meltables` / `9 heat sources` |
| M4 熔化速率 | ✅ | RCON 实测：篝火 0.25、点燃熔炉 0.15、熄灭熔炉 0（`lit=false` 不熔）；240 tick 内实测值与 `speed × tick` 差 ≤1 mB |
| M4 容量钳制 | ✅ | RCON：熔料 250 + 流体 7900 → 熔到 150/8000 停住；流体 8000 时熔料不动 |
| M4 比较器 | ✅ | RCON：7604/8000 → 信号 14，红石灯亮；100/8000 → 信号 0，灯灭 |
| M4 熔料/流体显示 | ⚠️ | 固体 > 流体显示熔料方块，反之流体；未目视 |
| M4 Jade | ⚠️ | 熔料剩余 + 热源速度；未目视 |
| M4 配置 | ✅ | 热改已验：`capacity` 8000→3000 下一秒内把 8000 mB 压到 3000；`heatSpeedMultiplier` 2.0 → 实测 0.5 mB/t |
| M4 JEI | ⚠️ | 坩埚/热源两页；未目视 |
| 瓷土/坩埚配方 | ⚠️ | 瓷土、未烧制坩埚、烧制；未实测 |
| 锤/钩 JEI | ⚠️ | 35 / 10 条 + catalyst；未目视 |
| M10 内能筛 | ✅ | RCON：120 tick 后输入 64→63、筛网 damage 1、`BurnHalfTicks` 3200→2600、产出燧石 |
| M10 电动筛 | ✅ | RCON（旧 4 秒档）：`Energy:20000`，101 tick 后 17980（20/tick 对得上）、输入 63、产出铝碎块/海晶结晶；5 秒/48000 档待重测 |
| M10 电动锤 + 速度 III + 时运 III | ✅ | RCON：5 秒 5 次（20 tick/次）、输入 64→59、锤 damage 5、20 砂砾（1×4 roll×5） |
| M10 内能锤 | ✅ | RCON：80 tick 一次、输入 63、锤 damage 1、`BurnHalfTicks` 2700、1 砂砾 |
| M10 管道 | ✅ | RCON：下方漏斗只抽到砂砾，锤/插件仍在；上方漏斗把圆石补进输入槽 |
| M10 内能筛（存档实证） | ✅ | 2026-11-01 存档 `新的世界`：`(198,72,-489)` 丝筛网 damage 12、粉碎末地石 52、煤 62，
  产出末地石×5 / 紫颂果×1 / 末影珍珠×2，`BurnHalfTicks` 1470——机器实际已处理 12 次 |
| M10 GUI / 仪表 / JEI | ✅ | 作者已进游戏确认：虚影/状态行/三块仪表/点箭头开 JEI/时运槽规则 |
| M10 模型 / 贴图 | ✅ | 作者确认：筛子薄托盘 + 锤子四柱压锤，32×32 贴图；预览图见 `docs/images/m10-*.png` |
| M10 工作动画 / 粒子 | ✅ | 作者确认：锤头下压铁砧、筛料薄层变薄、电动蓝火花/内能火焰烟 |
| M10 5 秒 / 48k / 叠加 | ✅ | RCON：无升级 124 tick → 1 次 + Work24、20 FE/t；2 个速度 III（同槽 count2）→ 7 次 + Work12（16 tick/件）、25 FE/t |
| M10 配方 | ⚠️ | 四台机器 + 6 个插件配方已注册，未实际合成 |
| M10 电动筛配方 | ⚠️ | 2026-11-01 按作者要求换成 `IRI / RBR / ISI`（铁锭角 + 红石 ×3 + **铁块** + 筛子底），替换原来 `IRI / RSI / IRI`；服务端 1423 条配方加载无报错，JEI 未目视 |
| supported mods（Mek 在场） | ✅ | 服务端加 Mekanism 10.7.19.85：日志 `tin/lead/osmium/uranium smelts into mekanism:ingot_*`；`summon` 自有 `tin_ingot` 得到 Air（未注册），`mekanism:ingot_tin` 正常；1423 → 3001 条配方（Mek 自带 + 我们的 12 条变体） |
| supported mods（Mek 不在） | ✅ | 无 Mek：1423 条配方照旧；熔炉烧 `tin_ore_gravel` 产出 `exnihilosatellitium:tin_ingot`（fallback 条件生效） |
| supported mods（Mek 在场熔炼） | ✅ | 有 Mek：熔炉烧同一块 `tin_ore_gravel` 产出 `mekanism:ingot_tin` |
| supported mods（JEI/客户端） | ⚠️ | 服务端已验；JEI 里的熔炼页与创造栏未在客户端目视 |
| M10 自动输出（下推） | ✅ | RCON 2026-11-01：`AutoOutput=1/OutputMask=1` + 下方箱子，一件后箱子拿到 `iron_crushed`、输出格清空 |
| M10 自动输出（关闭） | ✅ | RCON：`AutoOutput=0` 后产物留在槽 6..10，下方箱子不再增长 |
| M10 自动输出（换面） | ✅ | RCON：`OutputMask=2`（只推上）→ 缓冲 5 件 + 新 1 件全进上方箱子，下方不变 |
| M10 自动输出（20 tick 重试） | ✅ | RCON：无邻居时 3 燧石留槽；放箱子后**无新操作**，20 tick 内推进箱子 |
| M10 自动输出（内能筛） | ✅ | RCON：`fuel_sieve` 同配置 4 次产出 3 件进下方箱子（1 次 roll 空手） |
| M10 自动输出 GUI | ⚠️ | 首版（内嵌按钮 + 东南西北）作者反馈：面按钮方向不直观、入口位置不对；已改为左侧标签页 + 左侧抽屉 + 相对方向，**待目视** |
| 筛料渲染溢出 | ⚠️ | 作者截图确认料层与盘壁交替渲染；已改为 0.6 宽 + 抬到 10.5 px，**待目视** |
| 木桶/石桶竖棱 | ⚠️ | 作者反馈棱上渲染怪；已去掉 4 个 1×15 端面的 `rotation:90`（离线渲染对比确认条纹消失），**待目视** |
| 机器镐挖掘 | ⚠️ | 已加 `mineable/pickaxe` + `needs_stone_tool`（石镐及以上掉方块），**待游戏内挖一次确认** |
| 1.20.1 M2 build | ✅ | `1.20.1-forge` @ `9b92322`：`./gradlew build`（Temurin 17）通过 |
| 1.20.1 M2 筛子结构 | ✅ | RCON：`setblock ... sieve`、`[mesh=0/3]` 命中、`data merge` 的 `Mesh`/`Contents` 读回 |
| 1.20.1 M2 数据 | ✅ | `[sieve] loaded 3 inputs, 48 drops`；`[compost] loaded 65`；83 个 recipe JSON |
| 1.20.1 M2 锭物品 | ✅ | RCON `summon item ... tin_ingot` → `Tin Ingot`（9 种自有锭） |
| 1.20.1 M2 右键筛料 | ⚠️ | 需要玩家右键（客户端已启动）；未由 agent 实测 |
| 1.20.1 M3 表加载 | ✅ | `[hammer] 47`、`[crook] 10`、`[sieve] 4 inputs 62 drops` |
| 1.20.1 M3 物品 | ✅ | RCON summon 锤/钩/尘土均成功，`dust` 方块可放置 |
| 1.20.1 M3 手感 | ⚠️ | 锤左键链路/耐久、骨钩 ×1.5 待玩家 |
| 1.20.1 M4 坩埚 | ✅ | 表加载 39/9；RCON 篝火 0.25、熄灭停、火把 0.1；JEI 两页注册 |
| 1.20.1 M4 交互 | ⚠️ | 右键投料/取流体、Jade 画面待玩家 |
| P2 数据守卫 | ✅ | `python3 scripts/check-data-paths.py` → `passed for 1.21.1 (200 recipes, 72 loot tables, 44 tags)`；Tinkers 流体 result 已豁免 item 键检查 |
| P2 生成器幂等 | ✅ | 1.21.1 四个生成器写入仓库副本后 `diff -r` 与仓库逐字节一致（含 lang/筛表） |
| P2 移植数据 | ✅ | `port_data.py`：145 recipe / 69 loot / 41 tag / 125 其他；`check-data-paths --target 1.20.1` 通过 |
| P2 生成器 1.20.1 | ✅ | `--target 1.20.1 --out`：107 recipe / 60 loot / 39 tag 通过守卫；与 port 产物 240 文件逐字节一致 |
| P2 1.20.1 分支 | ⚠️ | 分支本身还没铺数据、没补 M2 |
| M7 datagen 第二批（配方） | ✅ | 2026-11-02：`ModRecipes` 生成 133 条；`compare-recipes.py` → `145 old, 133 generated, 12 old-only (0 unexpected), 0 new-only, 0 different` |
| M7 datagen 幂等 | ✅ | 删掉 133 条手写配方后再跑 `runData`：`HashCache ... written: 0`，没有旧文件复活 |
| M7 datagen 第二批（服务端） | ✅ | `runServer`：`Loaded 1423 recipes`（与迁移前同）、`[debug] our recipes=133`、无 recipe 解析错误 |
| M7 配方语义 | ✅ | 生成物只多出 codec 默认字段（`count:1` / `category:"misc"`）；没有 advancement 文件 |
| M7 datagen 移植链路 | ✅ | `port_data.py` 现在合并 main + generated：145 recipe / 69 loot / 41 tag / 126 other，`check-data-paths --target 1.20.1` 通过（只读 main 的旧默认会漏 133 配方） |
| M7 datagen 第三批（模型） | ✅ | 2026-11-02：`ModBlockStates` 71 blockstate + 29 模型、`ModItemModels` 144 物品模型；`compare-models.py` → `259 old, 244 generated, 244 identical` |
| M7 datagen 第三批（守卫） | ✅ | `check-textures.py` 现在同时扫 main+generated，并检查 blockstate→模型、parent 引用；故意写坏会报错 |
| M7 datagen 第三批（客户端） | ✅ | `runClient` 启动完成、纹理图集生成，日志无 `missing model` / `unable to load model` / missing texture |
| M7 datagen 第三批（生成器） | ✅ | 三个 Python 生成器原地重跑 `git status` 无资产 diff；不再写 blockstates/models |
| M7 datagen 第三批（画面） | ⚠️ | 桶/坩埚/筛子/机器/矿块的观感未目视（客户端已启动待看） |
| 科技模组锭（主 1.21.1 + Mek） | ✅ | 熔炉烧 `tin_ore_gravel` → `mekanism:ingot_tin`；自有 `tin_ingot` 未注册；`[materials]` 日志 4 条 |
| 科技模组锭（主，兜底） | ✅ | 无 Create：烧 `zinc_ore_gravel` → `exnihilosatellitium:zinc_ingot` |
| 科技模组锭（1.20.1 分支） | ✅ | 无模组：0 条解析错误、42 条变体跳过、`tin_ore_gravel → tin_ingot`；`conditions` 键生效 |
| 科技模组锭（Thermal/Create） | ✅ | 1.20.1 分支真装 Thermal Foundation 11.0.6.70 + Create 6.0.8：`tin/silver/lead/nickel → thermal:*_ingot`、`zinc → create:zinc_ingot`，RCON 熔炉实测通过，自有锭未注册 |
| IE 锭候选（1.20.1，仅 IE） | ✅ | 1.20.1 分支装 IE 10.2.0-183：`[materials]` 的 silver/lead/nickel/aluminum/uranium 全部 → `immersiveengineering:ingot_*` |
| IE 熔炼 RCON | ✅ | IE-only 分支：熔炉烧 `aluminum_ore_gravel` → `ingot_aluminum`，烧 `silver_ore_sand` → `ingot_silver` |
| IE 优先级（Thermal+Create+IE） | ✅ | `[materials]`：tin/silver/lead/nickel → Thermal、zinc → Create、aluminum/uranium → IE；0 条 `Unknown item` |
| IE 粉碎机（标签） | ⚠️ | 13 种矿块在 `forge:ores/<m>` 里，IE 的 crusher 输入也用该标签；多方块本身未跑 |
| IE 主 1.21.1 回退 | ✅ | 主线无 IE：`Loaded 1427 recipes`；熔炉烧 `aluminum_ore_gravel` → 自有 `aluminum_ingot`；IE 变体不加载 |
| 匠魂种子（条件物品） | ✅ | 只装 Tinkers：`summon` 两个种子物品成功；不装：`[sieve] 90` vs 92，无 unknown item |
| 匠魂钴 | ✅ | 只装 Tinkers：`[materials] cobalt → tconstruct:cobalt_ingot`；RCON 熔炉 `cobalt_ore_gravel → tconstruct:cobalt_ingot` |
| 匠魂熔炼配方 | ⚠️ | 78 条 `tconstruct:melting` 按 Tinkers 自己的格式生成，加载无报错；匠魂炉实际熔化未跑 |
| 下界合金碎片 | ✅ | 碎下界岩 0.4%，双线都加；`[sieve] 90`（无匠魂）/ 92（有匠魂） |
| 匠魂草种子右键 | ⚠️ | `TinkersGrassSeedItem` 右键草方块或泥土 → `*_vanilla_slime_grass`；需要玩家右键实测 |
| 钴碎块贴图 | ✅ | broken 用下界岩红棕基底，其余钴形态蓝色；`check-textures` 通过 |
| 配方条件键守卫 | ✅ | `check-data-paths.py` 对 1.20.1 的 `forge:conditions` 报错（负向测试） |
| 泥土筛石子（数量） | ✅ | 分支机器筛：7 次 → 圆石 34 / 安山岩 38 / 花岗岩 32 / 闪长岩 34（每种每次 4-6，100%） |
| 泥土筛树苗 | ✅ | 主线/分支 `[sieve] 6 inputs, 88 drops`；1.20.1 机器筛 8 次 → 橡木/金合欢树苗各 1（旧 5 种，≈22%） |
| 泥土筛新树苗（樱花/红树/杜鹃） | ✅ | 四条新表加载无警告；9 种合计 31%/次；数值待作者试玩 |
| 泥土筛竹子 | ✅ | 泥土加 `minecraft:bamboo` 5%；服务端 88 drops 加载无警告；竹子堆肥/竹桶/竹坩埚已有，只补来源 |
| 下界菌岩孢子（数据/物品） | ✅ | 主线 `runServer`：`[sieve] 6 inputs, 88 drops`、`[compost] 67 entries`；1.20.1 分支同样 88 / 67；两边 RCON `summon` 两个孢子物品名字正常 |
| 下界菌岩孢子（右键/骨粉） | ⚠️ | `NyliumSporesItem` 右键下界岩 → 菌岩；骨粉催菌/催巨菌沿用原版；未进游戏右键 |
| 孢子贴图/模型 | ✅ | 2 张 16×16 自有像素（`art/items/sieve_extras/generate.py`）；datagen 生成 item model；`check-textures` 通过 |
| 泥土筛石子（贴图/模型） | ✅ | 4 张 16×16 贴图 + 模型（主线 datagen 自动、分支手写）；`check-textures` 通过 |
| 矿块接 Create 粉碎 | ✅ | 分支装 Create 6.0.8：12 条 `create:crushing`（铁/金/铜/锌 ×3）0 解析错误；粉碎轮实际输出待目视 |
| 矿块接 Thermal 磨粉机 | ✅ | 全部走 `forge:ores/<m>` 标签（Thermal 自己的配方），无需我们补 |
| 筛表 min/max 兼容 | ✅ | 老表不写 `min`/`max` 仍按 1 件；`[sieve] 6 inputs, 76 drops`，无警告 |
| P1 core 守卫 | ✅ | `python3 scripts/check-core-imports.py` → `core purity check passed (0 known leak(s))` |
| P1 服务端启动 | ✅ | 2026-11-01 `runServer`：`[materials] loaded 12 entries`，`our recipes=133`，无 ERROR |
| P1 ConfigValues | ✅ | RCON：电动筛 217 tick，`Energy` 20000→15660（正好 20 FE/t） |
| P1 MachineHooks（自动输出） | ✅ | RCON：`AutoOutput=1/OutputMask=1` + 下方箱子，产物 1 秒内进箱子 |
| P1 MachineItemHandler（漏斗） | ✅ | RCON：下方漏斗只抽走 3 燧石，铁筛网/沙/输入仍留在机器 |
| P1 客户端 | ⚠️ | 只编译与跑服务端；画面未进游戏看（P1 不改渲染） |
| P0/P1 修复 | ⚠️ | 破坏返还、管道灌桶、F3+T 分离、红石/荧石未玩家实测 |
| 旧内容目视 | ⚠️ | 桶/筛比例、Jade tooltip 等仍待作者确认；桶的两道外凸箍已按反馈删除（2026-10-31） |

## 下一步（按价值排序）

> 自定义三路径：**P1 + P1.5 + P2（KJS）+ P3（CraftTweaker）两版都已完成**；
> 下一步是 [roadmap](customization-roadmap.md) 的 **P4 客户端 JEI 同步 + fixture CI**。

1. **1.20.1 一次性验收 M1.5–M10**：服务端逻辑都已 RCON 验过，剩下机器 GUI/自动输出抽屉/
   BER 动画、M6 美术与右键手感。Thermal / Create / IE 的锭与粉碎接入已真装验过（见当前批次），
   剩下的是机器多方块与画面。
2. **补 M4 / M6 / M10 的画面与右键验收**（含自动输出配置 GUI、筛料渲染、木桶竖棱、镐挖机器）：
   清单见交接清单。
3. **P3 状态机拆分**（P2 之后才有意义：`BarrelState` 等纯类 + 第一批 JUnit）。
4. **M7 余项**：只剩语言文件（建议保持手写 JSON，见 [datagen.md](datagen.md) §4.6）；
   标签/掉落/配方/模型四批都已完成。config 与 JEI 已齐。
5. **工程收尾**：advancement 树、mod 图标、CI、零测试（tech-debt P3 #37）。
6. **M8 月光魔法联动** / **M9 发布**。
7. **更多 supported mods**：IE + Tinkers 钴已接（1.20.1）；下一批等「工匠巧思」拓展补阿迪特
   （Tinkers 3 本体已移除），再看 IE 1.21.1 / 其它科技模组，按
   [data-format.md](data-format.md) §8 的规则加候选 + 手写变体即可（只接基础资源，不接合金）。

## 已拍板的决策

- **项目名 / 许可证**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`；
  MIT（Forge MDK 仍 LGPL-2.1），见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**：注册只认 jar 内 `materials.json`；表内材料有完整自有链；表外材料不自动发现。
- **方块贴图**：一般 vanilla-sampled；**坩埚例外**：采用 Creatio（MIT）模型/贴图并做模糊 + 5 色现代化，
  原始文件在 `art/blocks/crucible/creatio/`。
- **物品贴图**：自有或采用 Creatio（MIT）；Sequentia 是 CC BY-NC-SA，只作风格对照，不采用。
- **锤/钩**：6 锤 / 2 钩，左键唯一路径；粉粉尘→自身是安全兜底，不作为 JEI 配方展示。
- **坩埚**：熔料与流体分开记，熔料 1:1 产出流体；固体 > 流体显示熔料，反之流体；
  容量/熔料/热源倍率走 config，热源与熔点走数据包。**流动熔岩与源方块都按 0.2**
  （2026-11-02 所有者决定；`crucible_heat` 只按方块 id 匹配，不加 `level` 范围）。
- **坩埚被破坏时内容物一起消失**（只掉方块本体），不做返还：忠于 1.7.10 原版，
  也避免“先熔满再挖走”当免费流体袋。
- **数据包优先**：所有玩法表可覆盖/新增；单人 JEI 优先服务端表，专用服客户端回退内置镜像。
- **Jade 是可选依赖**：`compileOnly`，不打包；每个 provider 都要有 `config.jade.plugin_<ns>.<uid>` 翻译。
- **月光魔法是可选软依赖**：目前只占位。
- **分层契约（P1）**：`common/` 是 core（两版共用，可整包复制），顶层 `platform/` 是每版重写的胶水；
  core 只通过 `ConfigValues` / `Platform` / `MachineHooks` / `GuiLayers` 这几个窄门面碰平台，
  不用 `ModList` / `ModConfigSpec` / capability 类型。

## 踩过的坑

- **Jade provider 缺配置翻译会炸资源包**：`config.jade.plugin_exnihilosatellitium.<uid>` 必须有，
  否则 `AssertionError` 会导致 "removing all selected resourcepacks"。
- **`BlockDropsEvent#getTool()` 是复制栈**：扣耐久要扣 `player.getMainHandItem()`，扣事件里的工具无效。
- **`BlockDropsEvent` 创造模式不触发**：另挂 `BlockEvent.BreakEvent` 补掉落。
- **锤子不是镐**：不加 `HarvestCheck`/`BreakSpeed` 时 `canHarvestBlock` 会让方块直接掉空。
- **本地构建需要访问 `maven.neoforged.net`**：国内网络要挂代理（见 [migration-neoforge.md](migration-neoforge.md)）。
- **JEI/界面类改动先上原生控件**：筛子页曾因自绘背景返工；复盘见
  [incidents/2026-10-29-jei-layout.md](incidents/2026-10-29-jei-layout.md)。
- **直连 TLS 失败（libraries.minecraft.net / repo.maven）**：缓存已齐时用 `--offline`。
- **NeoForge 21.1 `FluidTank` NBT 是嵌套 `{Fluid:{id,amount}}`**，不是 1.20.1 的顶层 `FluidName`/`Amount`。
- **1.21.1 没有 `#forge:*` 物品标签**：用 `#c:*`；`#minecraft:crops/seeds` 也不是物品标签。
- **堆肥标签和 JSON 必须同步**：标签有、JSON 没有 = 按 1 点处理。
- **`#minecraft:saplings` 包含杜鹃 / 盛开的杜鹃**（1.21.1 与 1.20.1 都是），它们会按
  树苗标签的 125 堆肥；别把它们当成标签外物品再补 100。菌岩孢子不在任何原版标签里，
  才需要 `ModItemTags` 与 `compost/default.json` 两处都加。
- **内缩模型的外侧面不能加 `cullface`**：只保留在方块边界；坩埚模型已按此规则清洗。
- **`Properties.ofFullCopy(Blocks.FURNACE)` 会带上 `lit` 光照函数**：新方块没有 `lit` 状态，
  注册期就崩（`Cannot get property BooleanProperty{name=lit}`）。机器方块用 `IRON_BLOCK` 复制 + 手调强度。
- **`EnergyStorage(maxExtract = 0)` 的 `extractEnergy` 永远返回 0**：机器内部扣电要走自己的
  `MachineEnergy#drain`；同时 `receiveEnergy` 受 `maxReceive` 限制，读档要用 `setEnergyStored` 直写。
- **没有玩家时普通区块的 BE 不 tick**：RCON 测机器先 `forceload add`，否则会看到“摆放后只动了几十 tick 就停”。
- **漏斗会把机器里所有槽位当可抽**：`InvWrapper` 全开会让漏斗抽走筛网/插件，必须用
  `MachineItemHandler` 限制抽取范围。
- **`ContainerData` 客户端不能直接挂 BE**：BE 的 `set` 是空实现，菜单同步过来的数据写不进去，
  进度/能量/火焰条会永远冻在初始值。客户端菜单要用 `SimpleContainerData`，服务端才用 BE。
- **`renderBg` 里全局 pose 还没有平移到 `leftPos/topPos`**：自绘虚影/图标要自己加偏移，
  否则全部画到屏幕左上角。
- **1.21.1 的 `Slot#mayPlace` 默认恒为 `true`**，不会调用 `Container#canPlaceItem`；只用容器校验
  挡不住 GUI 拖拽/shift-click（插件会进筛网格）。必须给槽位写 `MachineSlot#mayPlace` 显式转发
  `canPlaceInSlot`（2026-11-01 修）。
- **`Active` 不写 NBT 就不会同步**：`getUpdateTag` 走 `saveAdditional`，漏了 `Active` 会让 BER/粒子
  永远判定停机；必须持久化并在翻转时 `sendBlockUpdated`（2026-11-01 修）。
- **`ambientocclusion: false` 会把机器渲染成纯黑**：别为了提亮凹槽关 AO，改用浅色内壁贴图。
- **相邻机器共面会 z-fighting**：模型整体内缩 1px（x/z 1..15），尤其是实心底座/四壁。
- **BER 头会遮住方块模型**：锤头交给 BER 后，方块模型要去掉静态头，否则会重影；物品模型要单独
  留一个带头版本，不然图标是空架子。
- **机器贴图 32×32 要放行检查脚本**：`check-textures.py` 只对 `machine_*` 允许 16/32，其他方块仍 16。
- **BER 里整块渲染输入物会穿模**：`renderSingleBlock` 铺 0.875 时方块占 1..15 px，正好穿过托盘内壁
  并与盘壁外侧面共面，表现为「料层和盘边交替闪烁」；缩到 0.6（3.2..12.8）并抬高 0.15 px 才干净。
- **1px 宽的端面不能带 `rotation: 90`**：面只有 1 px 宽、UV 矩形是 1×15，旋转后木纹被压进
  1 px → 棱上出现一排细条纹；端面用默认 UV（不旋转）才干净（木桶 2026-11-01 修）。
- **生成器不能无条件覆盖手写数据**：`lang` 只补缺失键、筛表按 item 合并；否则重跑
  `generate_sieve_assets.py` 会删掉 M2/M6 手加的 13 条沙筛掉落（P2 修）。
  新写生成器时，凡是作者可能手改的 JSON，都要走“合并”而不是整文件重写。
- **`art/items/sieve_extras/generate.py` 曾缺 `CHARS` 和 `SPRITES` 的收尾**（`20de823` 删块时带走），
  重跑会 `NameError`；本次补回并把菌岩孢子并进同一脚本（重跑能生成同样像素）。
- **`port_data.py` 只改路径/字段/命名空间，不改数值**：数值漂移要回生成器或手写源改，
  别在移植产物上打补丁（改完下一次 port 就丢）。
- **core 门面必须在注册之前安装**：`Platform.install()` / `ConfigValues.install()` /
  `OreMaterials.init()` 都在 mod 构造函数最前面；漏装会抛带说明的 `IllegalStateException`
  （`OreMaterials.all()` 同理），而不是到世界加载时才 NPE。配置门面是**委托读 spec**，
  所以 `/reload` 或改配置文件不需要重启。
- **`platform/` 不进 core 守卫**：判断标准是“1.20.1 要重写它吗”；要重写的才放顶层 `platform/`。
  core 门面（`common/config`、`common/platform`、`MachineHooks` 的调用方）永远不带 NeoForge import。
- **`ofFullCopy(IRON_BLOCK)` 不会把方块放进镐标签**：只继承 `requiresCorrectToolForDrops`，
  不加 `#minecraft:mineable/pickaxe` 时镐挖不掉方块；石镐门槛还要 `#minecraft:needs_stone_tool`。
- **配置层别用 `setScreen` 开**：那会把机器菜单关掉（`Screen#removed` → 菜单失效）。1.21.1 用
  `Minecraft#pushGuiLayer`（NeoForge `IMinecraftExtension` 的默认方法）+ 免写 `onClose`
  （`Screen#onClose` 已改成 `popGuiLayer`）；背景层会保留并自动按叠加顺序渲染（2026-11-01）。
- **`ContainerData` 的原版同步包是 short，NeoForge 客户端是 int**：`AdvancedContainerSetDataPayload`
  只有 NeoForge 客户端才走；48000 FE 在 NeoForge 客户端没问题，但新增字段别假设原版客户端能读。
- **`IContainerFactory` 菜单只调 `openMenu(provider)` 会崩客户端**：原版 `handleOpenScreen` 走
  `MenuType.create(id, inv)`，额外数据 buffer 为 `null`，客户端工厂 `readBlockPos()` 直接 NPE。
  必须 `openMenu(provider, buf -> buf.writeBlockPos(pos))`，让 NeoForge 发 `AdvancedOpenScreenPayload`
  （2026-11-01 修，`fff3535`）。
- **共享 BE 类型要列全七种方块**：漏一个放置后 BE 不创建。
- **子模型不要直接当方块模型用**：`barrel_base.json` 没有 textures，只作 parent。
- **JEI / 月光魔法生产 jar 不能直接丢 `run/mods`**：SRG 名会 `NoSuchFieldError`。
- **JEI 的 Modrinth maven 会拿到 Fabric jar**：用 `maven.blamejared.com` 的 neoforge 坐标。
- **IE 1.20.1 的锭 id 是 `ingot_aluminum`**（不是 `aluminum_ingot`），模组 id 是
  `immersiveengineering`；它的 crusher 输入只认 `forge:ores/<m>`（1.20.1），不吃 `c:ores`。
  IE 没有 1.21.1 版，主线只保留候选 / 变体。
- **IE 在专用服会打 `DISTXFORM` 报错**：那是 IE 自己尝试加载 client 类，server 仍然 `Done`，
  不要当成本项目的错误。
- **Tinkers 3 已经移除阿迪特**：3.12.1 没有 `molten_ardite` / `ardite_ingot`，只有残留的
  贴图文件；不要在 Tinkers 3 上硬加，等「工匠巧思」这类重现 1/2 代金属的拓展。
- **条件物品不能交给 datagen**：`sky/ender_slime_grass_seeds` 只在 `tconstruct` 在场时注册，
  `runData` 没有匠魂 → 不会生成模型；这两个模型/贴图必须手写。条件物品的创造栏也要 null 检查。
- **匠魂熔炼的 `result` 是流体**（`{tag, amount}`，没有 `item`/`id`）：`check-data-paths.py`
  现在只对带 item/id 的 result 检查版本键，否则 78 条熔炼会被误报。
- **筛表 `mod` 字段**：`"mod": "tconstruct"` 的条目在模组缺失时**在查物品之前**跳过，
  这样条件物品不会刷 unknown item；没有这个字段的条目行为不变。
- **数据表客户端只认 `assets/`**：`processResources` 把 `barrel/`、`compost/`、`sieve/`、`hammer/`、
  `crook/`、`crucible_melt/`、`crucible_heat/` 镜像到 `assets/`；data 仍是唯一源。
- **RCON 测桶前先 `forceload add`**；测熔炉要自己写 `CookTimeTotal`。
- **`/tick step` 仍按 20 TPS 走**：`step 250` 约 12.5 秒。
- **原版的锤/钩配方是斜的**：锤 `" x "/" yx"/"y  "`、钩 `"xx "/" x "/" x "`；写成十字/对角棍会被玩家一眼看出不对
  （2026-10-31 修正）。改配方前先翻 `reference/ex-nihilo-original/.../Recipes.java` 对形状。
- **方块交互别随便 `PASS_TO_DEFAULT_BLOCK_INTERACTION`**：坩埚满了以后右键熔料会落到默认交互上，
  在坩埚顶放一个方块（2026-10-31 修）。熔料/流体容器被拒时返回 `ItemInteractionResult.CONSUME`；潜行仍能放置。
- **`ItemInteractionResult.FAIL` 拦不住放置**：`InteractionResult.FAIL` 不 `consumesAction()`，
  `ServerPlayerGameMode#useItemOn` 会继续走 `ItemStack#useOn`。要“点了但什么都不做”只能用 `CONSUME`
  或 `SUCCESS`。
- **`/tick freeze` + `step` 不驱动强制加载区块里的方块实体**（没有玩家时）：坩埚/桶这类要验就得
  `tick unfreeze` 后按真实时间等，用 `/time query gametime` 前后差值算 tick 数再对照速率。
- **RCON 放比较器/中继器要先垫实心方块**（下方必须可支撑），否则 `setblock` 报成功但方块立刻掉落；
  比较器 `facing` 指向**输入**侧，输出在反方向。
- **不要用 `data merge` 往 `FluidTank` 写 `{id:"minecraft:air",amount:0}`**：日志会刷
  `Tried to load invalid fluid`（测试副作用，不影响存档）；清空直接不写 `Fluid` 键。

## 常用命令

```bash
./gradlew build
python3 scripts/check-textures.py
python3 scripts/check-core-imports.py                # core 层不得出现 NeoForge/Forge import
python3 scripts/check-data-paths.py                  # 数据目录/字段/命名空间与目标版本一致

# 数据管线（P2）
python3 scripts/port_data.py --out build/port-1.20.1 --force     # 1.21.1 -> 1.20.1 数据
python3 scripts/check-data-paths.py --target 1.20.1 --root build/port-1.20.1

# datagen（标签 / 掉落 / 配方 / blockstate+model）
./gradlew runData --offline
python3 scripts/compare-recipes.py --expect-old-only '*_mekanism.json'
python3 scripts/compare-models.py --expect-old-only 'models/block:barrel_base'  # 完整清单见 datagen.md §4.4

# 推送（SakuraCat 代理；SSH 22 会被代理拒，走 ssh.github.com:443）
export GIT_SSH_COMMAND='ssh -i ~/.ssh/id_ed25519_github -o IdentitiesOnly=yes -o ProxyCommand="nc -X connect -x 127.0.0.1:12450 %h %p"'
git -c url."ssh://git@ssh.github.com:443/WanConcubine/Ex-Nihilo-Satellitium.git".insteadOf="git@github.com:WanConcubine/Ex-Nihilo-Satellitium.git" push origin main refs/tags/v0.1.0
git -c url."ssh://git@ssh.github.com:443/WanConcubine/Ex-Nihilo-Satellitium.git".insteadOf="git@github.com:WanConcubine/Ex-Nihilo-Satellitium.git" push origin 1.20.1-forge refs/tags/v0.1.0-1.20.1
python3 scripts/generate_ore_variants.py             # 矿物链物品贴图 + 锤表 + lang（JSON 归 datagen）
python3 art/items/tools/restyle.py                   # 锤/钩/蚕/熟蚕/尘土贴图 + 预览
python3 art/blocks/crucible/restyle.py               # 坩埚模型/贴图（Creatio 现代化）
python3 scripts/generate_machine_assets.py           # M10 机器方块/插件/GUI 贴图（模型归 datagen）
./gradlew runServer   # RCON：localhost:25575 / eni
./gradlew runClient
```

> `generate_ore_variants.py`、`generate_sieve_assets.py`、`generate_machine_assets.py`、
> 两个 restyle 脚本都依赖 Pillow；系统 Python 没有时用 DSH 自带 Python 跑。
> 生成器现在**只写 PNG + 手写玩法表**：blockstate / 模型 / 配方 / 掉落 / 标签都归 `runData`。

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java           # 入口 + config 注册
├── registry/                          # Block / Item / BlockEntity / CreativeTab
├── platform/                          # 版本胶水（1.20.1 只重写这一层）
│   ├── NeoForgeModProbe.java          # ModProbe 的 NeoForge 实现（ModList）
│   ├── config/ModConfig.java          # ModConfigSpec + ConfigSource 实现
│   └── machine/                       # MachineEnergy / MachineItemHandler / MachineHooks
├── common/
│   ├── block/{Barrel,Sieve,Crucible,UnfiredCrucible}Block.java
│   ├── blockentity/{Barrel,Sieve,Crucible}BlockEntity.java
│   ├── config/                        # ConfigSource + ConfigValues 门面（core）
│   ├── platform/                      # ModProbe + Platform 门面（core）
│   ├── barrel/                        # BarrelRecipes + reload
│   ├── compost/                       # CompostRegistry + reload
│   ├── sieve/                         # SieveRegistry + reload + SieveDrop
│   ├── hammer/                        # HammerRecipes + reload
│   ├── crook/                         # CrookDrops + reload
│   ├── crucible/                      # CrucibleRecipes / CrucibleHeat + reload
│   ├── machine/                       # M10：SieveRolls / HammerRolls / UpgradeItem
│   │   ├── block/MachineBlock.java    # 四台机器共用一个方块类
│   │   ├── blockentity/               # MachineBlockEntity + 电动/内能子类 + MachineInventory
│   │   └── menu/                      # ElectricMachineMenu / FuelMachineMenu
│   ├── material/                      # materials.json 读取（注册期）
│   ├── event/{Common,Capability,Tool}Events.java
│   └── tag/ModTags.java
├── client/                            # BarrelRenderer / SieveRenderer / CrucibleRenderer / ClientEvents
│                                      # + screen/{Electric,Fuel}MachineScreen
└── compat/
    ├── MoonlightCompat.java
    ├── jade/                          # 可选 HUD：桶/筛/坩埚
    └── jei/                           # 筛/桶/堆肥/坩埚/锤/钩/机器 分类
└── datagen/                           # runData：标签、掉落、配方、blockstate+模型
                                       # ModRecipes / ModBlockStates / ModItemModels / WoodTypes
```

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。
JEI 截图与贴图预览在 `docs/images/`。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑/实现记录”，data-format 写“JSON/NBT/命令”，
  textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录；不要把同一份进度抄到多处。
- 新的“本轮”记录先加在 [DESIGN.md](DESIGN.md) 对应章节，本文件只留一行摘要。
- **批次叙事不要堆在这里**：只留「当前批次」；上一批/更早的搬运到 [history.md](history.md)，
  本文件只留一节「历史批次」摘要 + 链接（2026-11-02 整理时搬走过约 250 行）。
