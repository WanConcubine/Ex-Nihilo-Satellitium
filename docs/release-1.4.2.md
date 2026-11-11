# 1.4.2 发布材料

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.4.2`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> **性质：内容更新 + 版本对齐**——1.20.1 补黏液草种子 / 黏液泥土 / 匠魂树苗与连锁图标；
> 1.21.1 线没有代码改动，只同步版本号。
> 上一版：1.4.1（2026-11-11）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.4.2.jar` | 948 KB | `a8f2b9236f1fff04b6c263c4265cec9305a92ca64882da4f99a0038441a49047` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.4.2.jar` | 1000 KB | `2b1c5812a878de24c0e8ea4dd17fd6b2ff38bdc069312dcf18e9354fde559368` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.4.2"` 已核对。产物构建于 2026-11-11（主线 Java 21、
`1.20.1-forge` Java 17）。jar 的 sha256 取自本次最终构建；ForgeGradle 重新打包会写入
新的时间戳，重建出来的 sha 不会相同。

## 本版内容（相对 1.4.1）

### 1.20.1 线（`1.20.1-forge` 分支）

- **大地黏液草种子** `earth_slime_grass_seeds`：泥土 / 草方块各 2% 筛出；右键泥土 / 草方块 →
  `tconstruct:earth_vanilla_slime_grass`。
- **黏液草额外掉黏液泥土**：种子仍然种 `*_vanilla_slime_grass`（原途径不变）；
  `LootTableLoadEvent` 给它的掉落表**追加**一份 `<type>_slime_dirt`，原有 `minecraft:dirt`
  保留，匠魂文件不改——空岛第一条黏液泥土来源。
- **黏液泥土筛表** `sieve/{earth,sky,ender}_slime_dirt.json`：10% 出同种匠魂树苗、
  25% 出同种黏液球（大地用原版 `minecraft:slime_ball`）。
- **连锁强化图标**：补 `tinkering/modifier_icons.json` + 16×16 图标，工作台 / JEI 不再空图标。

### 1.21.1 线（`main`）

- **无改动**。这一版只把 `mod_version` 改成 `1.4.2`，jar 与 1.4.1 只有版本号不同；
  两条线的版本号继续对齐。

## 验证

- `./gradlew build --offline`：两版都通过。
- `check-textures.py`：主线 307 模型 / 116 blockstate / 190 纹理；1.20.1 315 模型 /
  116 blockstate / 213 纹理。
- `check-data-paths.py --target 1.20.1`（主线脚本跑 1.20.1 资源根）：两个根都通过。
- 1.20.1 `runServer -PtechMods -PdeMods`：`[sieve] loaded 10 inputs, 183 drops`、
  `[hammer] 91`、`[materials] 26`，无未知物品 / 解析错误；`-PtinkersMods` 单装时 10 / 128。
- 1.20.1 RCON：`setblock tconstruct:sky_vanilla_slime_grass` + `/loot spawn ... mine` 得到
  `minecraft:dirt` + `tconstruct:sky_slime_dirt` 两件（原有泥土没丢）；大地 / 末影同理。
- **未做**：黏液泥土实际手筛、树苗生长、JEI 筛子页 / 强化图标页面。

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
> - **Earthslime grass seeds**: 2% from sifting dirt / grass blocks.
> - The three slime grass seeds now grow the **slime-typed** grass, which drops **slime dirt**
>   when mined - the first slime-soil source in the skyblock.
> - Sifting slime dirt: 10% for the matching **Tinkers sapling**, 25% for the matching slime ball.
> 1.4.2 fixes:
> - The chain modifier now has its icon in the modifier worktable / JEI.

## 发布前检查

- [x] 两版 `mod_version=1.4.2`
- [x] `CHANGELOG.md` 的 1.4.2 节（中英双语）
- [x] README 下载表版本号
- [x] 两版 `build --offline` + `check-textures.py`
- [x] tag `v1.4.2` / `v1.4.2-1.20.1`（已推送）
- [ ] CurseForge / Modrinth 上传（所有者）
