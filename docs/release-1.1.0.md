# 1.1.0 发布材料

> 状态：**1.1.0 已构建并打 tag；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.1.0`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> 上一版：1.0.0（2026-11-04，首个正式版）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.1.0.jar` | 868 KB | `023b8072219db2ca3d78b39ce41a94cc9b1b277f153b67e0439169f35f2d7128` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.1.0.jar` | 870 KB | `bfca30a30c49cc9a6c2353f6224f0e1313dea29fb52ed4658facd2d89fb350b3` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.1.0"` 已核对。产物构建于 2026-11-04。

jar 文件名仍带 MC 版本（`<mod_id>-<mc_version>-<mod_version>.jar`），两条线同时上传不会撞名。

## 本版改进（相对 1.0.0）

- **热力（Thermal）人偶**：狂风人偶 / 暴雪人偶，只装 Thermal Foundation 时注册；
  熔岩/水桶规则分别生成 `thermal:blitz` / `thermal:blizz`。
- **村民人偶 / 脑白金**：巫水桶生成僵尸村民；脑白金右键傻子村民转普通村民（免 Shift）。
- **糖水 → 浓缩蜂蜜汁**：水+糖、糖水+蒲公英黄 30 秒、玻璃瓶抽 250 mB 得蜂蜜瓶；
  两种流体都有 source block 和桶，可放置/舀取；浓缩蜂蜜桶会吸引蜜蜂（附近上限 5）。
- **蜂箱配方**：3 木板 + 3 蜂蜜瓶 + 3 木板 → 原版蜂箱。
- **AE2 陨石粉**：沙、砂砾各 1/16 出 `ae2:sky_dust`。
- **桶表**：新增 `result_fluid` / `delay_ticks` / `drain` 规则，JEI / Jade / `/exnihilo dump` 同步；
  桶内流体改为四面盒渲染并修正 1.20.1 流体 tint 初始化顺序。

## 商店页文案（中文）

**标题**：无中生有：众星捧月 (Ex Nihilo: Satellitium)

**一句话简介**：面向 1.21.1 / 1.20.1 的独立空岛资源获取模组：木桶堆肥、筛子、锤子、钩子、
坩埚、糖水/蜂蜜链与自动化，以及可选的科技模组联动。

**正文**：

从一棵树、一桶水、一格熔岩开始，把空岛一步步变成基地。本模组独立实现 Ex Nihilo 风格的
空岛资源链，不复制任何 GPLv3 分支的代码或资源：

- **木桶**：堆肥成土、1000 mB 流体、18 条桶内转化（圆石/黑曜石/下界岩/末地石/灵魂沙/
  深色橡树苗/史莱姆/8 种人偶/糖水/浓缩蜂蜜）。
- **筛子**：7 种筛料；泥土出 9 种树苗、竹子、四种石子；草方块出叶绿素碎矿；粉碎下界岩
  出菌岩孢子、钴碎块、下界合金碎片、阿迪特碎矿；沙子出农业/海洋资源、AE2 赛特斯石英与
  陨石粉；粉碎末地石出末地资源。
- **锤子 / 钩子**：石头降档、矿砾→粉碎→粉末、树叶取苗/苹果/蚕。
- **坩埚**：熔点 + 热源；木坩埚被动熔树叶/树苗。
- **巫水 / 瓷桶 / 蚕 / 人偶 / 蜂蜜**：经典中后期链，外加村民人偶、脑白金、糖水→浓缩蜂蜜、
  养蜂与蜂箱配方。
- **M10 自动化**：电动 / 内能 × 自动筛 / 自动锤，速度/时运插件，自动输出。
- **可选联动**：Mekanism、Thermal、Create、Immersive Engineering、Tinkers' Construct 3、
  工匠巧思（Tinkers' Thinking，1.20.1）、Applied Energistics 2。装了谁就把矿砾熔成谁家的锭；
  1.20.1 的匠魂还能把碎块/粉碎/粉末熔成 45 mB、矿块熔成 180 mB；AE2 额外掉赛特斯与陨石粉。
- **JEI / Jade**：配方页与方块提示。

**英文短描述**：

An independent Ex Nihilo-style skyblock resource mod for Minecraft 1.21.1 (NeoForge) and 1.20.1
(Forge). Buckets, sieves, hammers, crooks, crucibles, a sugar-water/honey chain, automation, and
optional integrations with Mekanism, Thermal, Create, Immersive Engineering, Tinkers' Construct,
Tinkers' Thinking and Applied Energistics 2. No GPL assets copied.

## 建议的商店字段

| 字段 | 值 |
|---|---|
| 名称 | Ex Nihilo: Satellitium / 无中生有：众星捧月 |
| 许可证 | MIT |
| 分类 | Technology、Game Mechanics、World Generation（按平台可选 “Skyblock”） |
| 游戏版本 | 1.21.1（NeoForge）、1.20.1（Forge） |
| 依赖 | 无硬依赖；JEI / Jade / AE2 可选；1.20.1 的 IE / Tinkers + Mantle / 工匠巧思可选 |
| 更新日志 | 复制 [CHANGELOG.md](../CHANGELOG.md) 的 1.1.0 节 |
| 源码 / 问题 | `https://github.com/WanConcubine/Ex-Nihilo-Satellitium` |

## 发布前检查

- [x] 两条线 `mod_version=1.1.0`
- [x] `CHANGELOG.md` 的 1.1.0 节（含未验项）
- [x] README 下载表与支持模组更新
- [x] 两条线 `build` 成功，jar 内版本号 1.1.0
- [x] 两份 jar 的大小 / sha256 回填
- [x] 打 tag：`v1.1.0`（main）、`v1.1.0-1.20.1`（1.20.1-forge）
- [ ] 推送 tag 到 origin
- [ ] CurseForge 上传 1.21.1 / 1.20.1 两个文件
- [ ] Modrinth 上传对应两个版本
- [ ] 把商店下载链接补进 README
- [ ] 可选：补一张商店页头图 / 截图

## 已知限制

- 村民人偶 / 脑白金 / 糖水链 / 玻璃瓶抽取 / 新 JEI 页尚未完成正式客户端逐项验收；
  服务端逻辑和 `data merge` 路径已验证。
- 1.20.1 分支筛表比主线少 3 条钴（沙/砂砾/尘土各 1/64），基表 113 vs 116。
- 客户端画面为长期欠账：机器 GUI、自动输出抽屉、BER、Jade tooltip 等仍待作者确认。
