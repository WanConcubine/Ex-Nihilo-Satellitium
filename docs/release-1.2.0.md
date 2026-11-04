# 1.2.0 发布材料

> 状态：**1.2.0 已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.2.0`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> 性质：**内容更新**——两个模组联动 + 两条筛出物 + ER 矿块标签修复。
> 上一版：1.1.2（2026-11-04）。
> 已推送：`main` 1b8856b / `1.20.1-forge` 89dd8d1；tag `v1.2.0` / `v1.2.0-1.20.1`（2026-11-04）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.2.0.jar` | 905 KB | `6965be81ad12eacfd43c8b9f2b4307d36b53b1b81f17bc65c290de160f4b5ff3` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.2.0.jar` | 911 KB | `aae62c918759d0fd0662cd4ba1623604a0b3a2f47f06f4c185e7c616838a9b81` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.2.0"` 已核对。产物构建于 2026-11-04。

## 本版内容（相对 1.1.2）

### 新增

- **骨粉 / 烈焰粉**：尘土 20% 出 `minecraft:bone_meal`（经典 Ex Nihilo 1/5）；
  沙子 3% 出 `minecraft:blaze_powder`，碎下界岩 6% 保留。
- **Extreme Reactors（两版）**：`yellorium`（1/8）、`anglesite` / `benitoite`（各 1/64）；
  三档矿链 → 熔成 ER 锭 / 晶体；`gems` 门控，没装 ER 时整条不注册。
- **BambooMod（仅 1.20.1）**：泥土/草方块各 8 条（4 树苗 2%、竹笋 4%、米种 5%、豆 4%、番茄 4%），
  砂砾 3 宝石各 1/64；全部 `mod` 门控。
- **匠魂（仅 1.20.1）**：yellorium 碎/粉/砂 45 mB、矿块 180 mB、ER 锭 90 / 块 810；
  2 条铸造回 ER 锭/块；复用 ER 的 `forge:yellorium` 流体。

### 修复

- **ER 矿块识别**：yellorium 矿块同时进 `ores/yellorite` 与 `ores/uranium`
  （1.21.1 为 `c:`），`yellorium_broken` 进 `raw_materials/yellorium`。
  Mekanism `enriching` / IE crusher / Create crushing 因此能处理我们的矿块
  （输出对方的铀粉 / 粉碎铀，与 ER 原矿一致）。

### 数据规模

- 材料 **25** 种（14 金属 + 11 矿物）。
- 筛表：基表 118 / 1.20.1 115；装 ER 各 +9；1.20.1 装 BambooMod +19（全装 173）。
- 匠魂：92 条熔炼 + 2 条铸造（94 条匠魂配方）。

## 验证

- 两版 `build`、`check-textures.py`、`check-data-paths.py`、`check-wiki.py` 通过。
- 两版真装 ER + ZeroCore：`[materials] 25`（1.20.1 全装）、`[sieve] 154`；
  RCON 熔炉三种 ER 产物通过，`RecipesUsed` 指向 `*_smelting_bigreactors`。
- 1.20.1 真装 BambooMod：`[sieve] 173`、`[hammer] 88`、`/setblock` 树苗/竹笋通过。
- 匠魂配方服务端加载无解析错误；实机熔炉熔化尚未逐项点过。
- **未做**：手动右键筛、树苗生长、JEI 页面目视。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.2.0 新增两个模组联动：
> - Extreme Reactors（两版）：黄铀 / 铅矾 / 蓝锥矿可由空岛矿链获得，并能在 Mekanism / IE / Create
>   里按铀矿处理；1.20.1 还能在匠魂里熔成 yellorium 流体并铸回锭。
> - BambooMod（仅 1.20.1）：竹苗 / 竹笋 / 稻豆番茄 / 三种宝石可由泥土和砂砾筛出。
> 另外：尘土 20% 出骨粉，沙子 3% 出烈焰粉。BambooMod 与匠魂只支持 1.20.1 线。

**English**：

> 1.2.0 adds two mod integrations:
> - Extreme Reactors (both lines): yellorium / anglesite / benitoite are obtainable from the
>   skyblock ore chain and are treated as uranium ore by Mekanism / IE / Create; on 1.20.1 the
>   Tinkers smeltery can melt yellorium and cast it back into ingots.
> - BambooMod (1.20.1 only): saplings, bamboo shoots, rice/beans/tomatoes and three gems are
>   sifted from dirt and gravel.
> Also: 20% bone meal from dust, 3% blaze powder from sand. BambooMod and Tinkers are 1.20.1-only.

## 发布前检查

- [x] 两条线 `mod_version=1.2.0`
- [x] `CHANGELOG.md` 的 1.2.0 节
- [x] README 下载表版本号 / CHANGELOG 链接
- [x] 两条线 `build` 成功，jar 内版本号 1.2.0
- [x] 两份 jar 的大小 / sha256 回填
- [x] 打 tag：`v1.2.0`（main）、`v1.2.0-1.20.1`（1.20.1-forge）
- [x] 推送分支与 tag 到 origin
- [ ] CurseForge 上传 1.21.1 / 1.20.1 两个文件
- [ ] Modrinth 上传对应两个版本
- [ ] 把商店下载链接补进 README

## 已知限制

- **BambooMod 只有 1.20.1**；1.21.1 线不加载。
- **anglesite / benitoite 没有匠魂熔炼**：没有对应熔融流体；要支持需新增 molten 流体。
- **Thermal 磨粉机没有 yellorium / uranium 配方**；Mekanism / IE / Create 按 `forge:ores/uranium` 工作。
- 1.20.1 分支筛表仍比主线少 3 条钴（历史差异）。
- 手动筛 / 树苗生长 / JEI 画面仍未逐项目视。
