# 1.4.2 发布材料（1.20.1 线）

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.4.2`；本文件是 **1.20.1 / Forge 47.4** 线，1.21.1 线这一版没有代码改动。
> 性质：**内容更新**——大地黏液草种子、黏液泥土筛出匠魂树苗、种子改种黏液草变体；
> 顺带补 1.4.1 漏掉的连锁强化图标。
> 上一版：1.4.1（2026-11-11）。
> tag：`v1.4.2-1.20.1`（2026-11-11）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.2.jar` | 997 KB | `c688d9d8dae56295fbc578bce9d84b6ecc4558569eb6afeaafc0be6d9e6898d5` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png`、`META-INF/mods.toml`，以及可选的
`compat/tconstruct/*`（匠魂 source set）；`mods.toml` 里的 `version="1.4.2"` 已核对。
产物构建于 2026-11-11（Java 17 / ForgeGradle 6）。jar 的 sha256 取自本次最终构建；
ForgeGradle 重新打包会写入新的时间戳，重建出来的 sha 不会相同。

## 本版内容（相对 1.4.1）

### 新增

- **大地黏液草种子** `earth_slime_grass_seeds`（装匠魂才有）：泥土 / 草方块各 **2%** 筛出，
  右键泥土 / 草方块 → `tconstruct:earth_earth_slime_grass`。
- **黏液泥土筛表** `sieve/{earth,sky,ender}_slime_dirt.json`（根级 `mod: tconstruct`）：
  每种黏液泥土 **10%** 出同种 `tconstruct:<type>_slime_sapling`、**25%** 出同种黏液球；
  大地那档是原版 `minecraft:slime_ball`（匠魂没有 earth slime ball）。

### 变更

- **种子改种黏液草变体**：碧空 / 大地 / 末影种子现在种
  `tconstruct:<type>_<type>_slime_grass`，不再是 `*_vanilla_slime_grass`。
  黏液草挖掉掉 `<type>_slime_dirt`（匠魂自己的掉落表），vanilla 变体只掉原版泥土——
  这是空岛第一条黏液泥土来源。
- 循环：堆肥 → 泥土 → 筛种子 → 种黏液草 → 挖黏液泥土 → 筛树苗 → 种在黏液泥土上。

### 修复

- **连锁强化图标**：补 `assets/exnihilosatellitium/tinkering/modifier_icons.json` 与
  16×16 `gui/modifiers/chain.png`；强化工作台 / JEI 不再空图标（1.4.1 漏的）。

### 数据

- 筛子：装匠魂 **10 输入 / 183 掉落**（1.4.1 是 7 / 175）；无匠魂仍是 7 / 116。
- 纹理：315 模型 / 116 blockstate / 213 纹理。

## 验证

- `./gradlew build --offline` 通过（Java 17）。
- `check-textures.py` 通过：315 模型 / 116 blockstate / 213 纹理。
- 主线 `check-data-paths.py --target 1.20.1`：两个资源根都通过。
- `runServer -PtechMods -PdeMods`：`[sieve] loaded 10 inputs, 183 drops`、`[hammer] 91`、
  `[materials] 26`，无未知物品 / 解析错误；`-PtinkersMods` 单装时 `10 inputs, 128 drops`。
- RCON：`setblock tconstruct:sky_sky_slime_grass` 能放住；`/loot spawn ... mine` 该方块
  掉落 `tconstruct:sky_slime_dirt`；大地 / 末影同一套匠魂掉落表。
- **未做**：黏液泥土实际手筛的画面、树苗生长、JEI 筛子页 / 图标页面。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.4.2 新增（仅 1.20.1；1.21.1 只同步版本号）：
> - 补齐**大地黏液草种子**：筛泥土 / 草方块 2% 获得，右键泥土或草方块种出大地黏液草。
> - 三种黏液草种子现在种出的是**黏液草**方块，挖掉会掉**黏液泥土**——空岛终于有黏液土了。
> - 黏液泥土可以过筛：10% 出同种**匠魂树苗**、25% 出同种黏液球。
> 1.4.2 修复：
> - 连锁强化在工作台 / JEI 里没有图标的问题。

**English**：

> 1.4.2 adds (1.20.1 only; the 1.21.1 build just shares the version number):
> - **Earthslime grass seeds**: 2% from sifting dirt / grass blocks; right-click dirt or grass
>   to grow earthslime grass.
> - The three slime grass seeds now grow the **slime-typed** grass, which drops **slime dirt**
>   when mined - the first slime-soil source in the skyblock.
> - Sifting slime dirt: 10% for the matching **Tinkers sapling**, 25% for the matching slime ball.
> 1.4.2 fixes:
> - The chain modifier now has its icon in the modifier worktable / JEI.

## 发布前检查

- [x] `mod_version=1.4.2`
- [x] `CHANGELOG.md` 的 1.4.2 节（中英双语）
- [x] README 下载表版本号
- [x] `build` + `check-textures.py` + `check-data-paths.py`
- [x] tag `v1.4.2-1.20.1`（已推送）
- [ ] CurseForge / Modrinth 上传（所有者）
