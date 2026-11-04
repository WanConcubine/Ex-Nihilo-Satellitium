# 1.1.1 发布材料

> 状态：**1.1.1 已构建并打 tag；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.1.1`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> 上一版：1.1.0（2026-11-04）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.1.1.jar` | 876 KB | `972e3e9c4e4c2c6e80012889e005eca5c9a1b6c6b9127bf9fcc5a40018031d51` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.1.1.jar` | 878 KB | `ee6e1bd6c3c3bdc7da7cd96152222879f1d13004dd36e9d6cfb46919b183bba0` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.1.1"` 已核对。产物构建于 2026-11-04。

jar 文件名仍带 MC 版本（`<mod_id>-<mc_version>-<mod_version>.jar`），两条线同时上传不会撞名。

## 本版改进（相对 1.1.0）

- **木桶堆肥外观**：堆肥内容改为原版**泥土方块模型**，顶点色按投入物品的颜色加权混色：
  腐肉偏红褐、树叶偏绿，混投取比例色；计时中逐渐淡回泥土本色，成土后显示原版泥土色。
  此前只有一个固定棕色贴图，远看像纯色方块。
- **熔料外观**：坩埚（含 11 种木坩埚）里正在熔的方块改用其真实方块模型渲染；
  木坩埚里的树叶显示为树叶方块，且固体没熔完时不会中途切成蓝色水盒。
- **堆肥成土时间** 50 秒 → **30 秒**（`COMPOST_TICKS` 1000 → 600），JEI 提示同步。
- **数据**：木桶 NBT 新增 `CompostItems`（`[{id, points}]`），只用于客户端混色；
  旧存档没有该键时按纯泥土显示。删除不再使用的 `barrel_compost.png`。

截图：[1.1.1-compost-crucible.png](images/1.1.1-compost-crucible.png)（左起：腐肉 / 树叶 /
50-50 / 已成土 / 木坩埚中的树叶）。

## 商店页文案（中文，增量）

**更新标题**：1.1.1 — 堆肥混色与木坩埚外观修正

**正文**：

- 木桶堆肥现在显示真实的泥土模型，并按照你投入的物品颜色混色：腐肉会偏红褐，树叶会偏绿，
  成土后恢复泥土本色。
- 木坩埚里的树叶会显示为树叶方块（不再是一个纯蓝水盒），熔完才变成水。
- 成土时间从 50 秒缩短到 30 秒。

**英文短描述**：

Barrel compost now shows a real dirt block tinted by the items you put in, wooden crucibles
show the leaves block while melting, and composting now takes 30 seconds instead of 50.

## 发布前检查

- [x] 两条线 `mod_version=1.1.1`
- [x] `CHANGELOG.md` 的 1.1.1 节
- [x] README 下载表版本号
- [x] 两条线 `build` 成功，jar 内版本号 1.1.1
- [x] 两份 jar 的大小 / sha256 回填
- [x] 画面核对（两条线 dev client，临时脚本已删除）
- [x] 打 tag：`v1.1.1`（main）、`v1.1.1-1.20.1`（1.20.1-forge）
- [ ] 推送 tag 到 origin
- [ ] CurseForge 上传 1.21.1 / 1.20.1 两个文件
- [ ] Modrinth 上传对应两个版本
- [ ] 把商店下载链接补进 README

## 已知限制

- 坩埚的“熔料 → 流体”切换仍按经典规则（固体 > 流体）；木坩埚例外，固体全部熔完后才
  显示水，这是本版的有意行为。
- 1.20.1 分支筛表比主线少 3 条钴（沙/砂砾/尘土各 1/64），基表 113 vs 116。
- 客户端画面长期欠账仍在：机器 GUI、自动输出抽屉、JEI tooltip 等未逐项目视。
