# 1.3.0 发布材料（1.20.1 线）

> 状态：**1.3.0 已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.3.0`；本文件是 **1.20.1 / Forge 47.4** 线，1.21.1 线的同版本说明在 `main` 分支。
> 性质：**内容更新**——龙之进化（Draconic Evolution）龙尘 / 龙锭联动 + 破碎龙矿贴图修正。
> 上一版：1.2.0（2026-11-04）。
> 已推送：`1.20.1-forge` b046e19 / `main` 794413e；tag `v1.3.0-1.20.1`（2026-11-05）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.3.0.jar` | 918 KB | `3856c6eb396198e54189681c2dd27a544779240d7c1e3e629b2ad2100e306516` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/mods.toml`；
`mods.toml` 里的 `version="1.3.0"` 已核对。产物构建于 2026-11-05。

## 本版内容（相对 1.2.0）

### 新增：龙之进化

- **龙尘（`draconium`）**：`gems` 门控材料（`draconicevolution`），tier `rare`、rarity 64。
  唯一入口是**粉碎末地石 1/8** → `draconium_broken`；4 破碎 → 矿砾，矿砂 / 矿尘同理。
- **熔炼**：矿砾 / 矿砂 / 矿尘进 `forge:ores/draconium`，直接复用 DE 自己的标签熔炼，
  出 `draconicevolution:draconium_ingot`，不写重复变体。
- **锤表**：矿砾→粉碎 4–6、矿砂→粉末 4–6、矿尘→自身；三条都带 `mod` 门（本分支手写）。
- **不碰终局**：觉醒龙（4 龙块 + 6 龙核 + 龙心）与混沌碎片（混沌守卫）保持 DE 原流程。

### 修复

- **破碎龙矿石壳**：`broken` 阶段改用末地石色（`STONE_PALETTES["draconium"]`），
  不再退回默认灰石。

### 数据规模

- 材料 **26** 种（14 金属 + 12 矿物）。
- 筛表：基表 115；装 DE +1（全装 173 → 174）。
- 锤表：装 DE +3（全装 88 → 91）。

## 验证

- `build`、`check-textures.py`、`check-data-paths.py --target 1.20.1` 通过。
- 真装 DE 3.1.2.621 + Brandons Core 3.2.1.302 + CodeChicken Lib 4.4.0.528
  （全装叠加 `-PtechMods -PdeMods`）：`[materials] 26`、`[sieve] 174`、`[hammer] 91`；
  RCON 熔炉 `draconium_ore_dust` → `draconicevolution:draconium_ingot`。
- `runData` 重跑无 diff；纹理守卫 296 模型 / 166 纹理。
- **未做**：手动右键筛、JEI 页面目视。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.3.0 新增龙之进化（Draconic Evolution）联动：
> - 龙尘可由空岛获得——粉碎末地石 1/8 出破碎龙矿，4 合 1 压成矿块，
>   再由龙之进化自己的矿标签熔成龙锭。
> - 修复破碎龙矿的贴图基底：现在使用末地石底色，不再像普通砂砾。
> 龙之进化不在场时整条矿链不注册，原版数据完全不变。

**English**：

> 1.3.0 adds a Draconic Evolution integration:
> - Draconium is now obtainable on a skyblock: crushed end stone has a 1/8 chance to
>   drop broken draconium ore, four pieces compress into an ore block, and DE's own
>   ore tag smelting turns it into a draconium ingot.
> - Fixed the broken draconium texture: it now uses an end stone base instead of
>   the default grey stone.
> The whole chain is not registered when Draconic Evolution is absent, so vanilla
> data is unchanged.

## 发布前检查

- [x] `mod_version=1.3.0`
- [x] `CHANGELOG.md` 的 1.3.0 节
- [x] README 下载表版本号
- [x] `build` 成功，jar 内版本号 1.3.0
- [x] jar 大小 / sha256 回填
- [x] 打 tag：`v1.3.0-1.20.1`
- [x] 推送分支与 tag 到 origin
- [ ] CurseForge 上传 1.20.1 文件
- [ ] Modrinth 上传对应版本
- [ ] 把商店下载链接补进 README

## 已知限制

- **DE 3.1.2.621 是 1.20.1 的 release**（1.21.1 线对应 3.1.4.633 beta）。
- **龙尘只有粉碎末地石一个来源**。
- **没有手写龙尘熔炼变体**：完全依赖 DE 的 `forge:ores/draconium` 标签。
- **DE 的 datagen 需要 Curios**：测 DE 时 DE jar 不能常驻 `libs/` 以外的默认加载路径，
  否则 `runData` 会崩；本分支用 `-PdeMods` 隔离。
- 本分支筛表仍比主线少 3 条钴（历史差异）。
- 手动筛 / JEI 画面仍未逐项目视。
