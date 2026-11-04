# 1.0.0 发布材料

> 状态：**1.0.0 已定稿并打 tag；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.0.0`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> 原 0.2.0 准备线在验收后直接定为 **1.0.0**（2026-11-04）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.0.0.jar` | 845 KB | `34b3875b6986053f8fde3e145275d2595f5a41491a94eab2dc8a2b75b2c1ccb9` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.0.0.jar` | 848 KB | `f526d01f2f9cc6b8ed2ffa1f1abd663b4c2bbabe23ace8b7e031c6b756a31e52` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.0.0"` 已核对。产物构建于 2026-11-04。

## 商店页文案（中文）

**标题**：无中生有：众星捧月 (Ex Nihilo: Satellitium)

**一句话简介**：面向 1.21.1 / 1.20.1 的独立空岛资源获取模组：木桶堆肥、筛子、锤子、钩子、
坩埚，以及可选的科技模组联动。

**正文**：

从一棵树、一桶水、一格熔岩开始，把空岛一步步变成基地。本模组独立实现 Ex Nihilo 风格的
空岛资源链，不复制任何 GPLv3 分支的代码或资源：

- **木桶**：堆肥成土、1000 mB 流体、12 条桶内转化（圆石/黑曜石/下界岩/末地石/灵魂沙/
  深色橡树苗/史莱姆/人偶）。
- **筛子**：7 种筛料；泥土出 9 种树苗、竹子、四种石子；草方块出叶绿素碎矿；粉碎下界岩
  出菌岩孢子、钴碎块、下界合金碎片、阿迪特碎矿；沙子出农业/海洋资源与 AE2 赛特斯石英；
  粉碎末地石出末地资源。
- **锤子 / 钩子**：石头降档、矿砾→粉碎→粉末、树叶取苗/苹果/蚕。
- **坩埚**：熔点 + 热源；木坩埚被动熔树叶/树苗。
- **巫水 / 瓷桶 / 蚕 / 人偶**：经典中后期链。
- **M10 自动化**：电动 / 内能 × 自动筛 / 自动锤，速度/时运插件，自动输出。
- **可选联动**：Mekanism、Thermal、Create、Immersive Engineering、Tinkers' Construct 3、
  工匠巧思（Tinkers' Thinking，1.20.1）、Applied Energistics 2。装了谁就把矿砾熔成谁家的锭；
  1.20.1 的匠魂还能把碎块/粉碎/粉末熔成 45 mB、矿块熔成 180 mB；AE2 只掉赛特斯石英。
- **JEI / Jade**：配方页与方块提示。

**英文短描述**：

An independent Ex Nihilo-style skyblock resource mod for Minecraft 1.21.1 (NeoForge) and 1.20.1
(Forge). Buckets, sieves, hammers, crooks, crucibles, automation, and optional integrations
with Mekanism, Thermal, Create, Immersive Engineering, Tinkers' Construct, Tinkers' Thinking and
Applied Energistics 2. No GPL assets copied.

## 建议的商店字段

| 字段 | 值 |
|---|---|
| 名称 | Ex Nihilo: Satellitium / 无中生有：众星捧月 |
| 许可证 | MIT |
| 分类 | Technology、Game Mechanics、World Generation（按平台可选 “Skyblock”） |
| 游戏版本 | 1.21.1（NeoForge）、1.20.1（Forge） |
| 依赖 | 无硬依赖；JEI / Jade / AE2 可选；1.20.1 的 IE / Tinkers + Mantle / 工匠巧思可选 |
| 更新日志 | 复制 [CHANGELOG.md](../CHANGELOG.md) 的 1.0.0 节 |
| 源码 / 问题 | `https://github.com/WanConcubine/Ex-Nihilo-Satellitium` |

## 发布前检查

- [x] 两条线 `mod_version=1.0.0`
- [x] `CHANGELOG.md` 的 1.0.0 节（含已知限制）
- [x] README 下载表与支持模组更新
- [x] 两条线 `build` 成功，jar 内版本号 1.0.0
- [x] 所有者确认版本号（0.2.0 准备线直接发 1.0.0）与发布日期
- [x] 最终构建后回填两份 jar 的大小 / sha256
- [x] 打 tag：`v1.0.0`（main）、`v1.0.0-1.20.1`（1.20.1-forge）
- [ ] 推送 tag 到 origin
- [ ] CurseForge 上传 1.21.1 / 1.20.1 两个文件
- [ ] Modrinth 上传对应两个版本
- [ ] 把商店下载链接补进 README
- [ ] 可选：补一张商店页头图 / 截图

## 已知限制（发布时保留）

- 部分画面与手感仍待作者验收：机器 GUI/动画、自动输出抽屉、筛料层、木桶竖棱、
  手动筛右键、匠魂种子右键、匠魂炉实际熔化。
- 阿迪特 / 叶绿素由 1.20.1 的「工匠巧思」补回（Tinkers 3.12.1 已移除阿迪特）；
  AE2 只做赛特斯石英，不做充能赛特斯 / 天石粉。
- IE、Tinkers 与工匠巧思仅有 1.20.1 版本；1.21.1 只保留永不命中的候选/变体。
- 没有 advancement 树、自动化测试与 CI；「月光魔法」只有软依赖占位。
