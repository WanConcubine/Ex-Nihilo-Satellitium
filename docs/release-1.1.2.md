# 1.1.2 发布材料

> 状态：**1.1.2 已构建并打 tag；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.1.2`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> **性质：纯 Bug 修复版**——没有新增玩法、数据表、贴图或渲染改动。
> 上一版：1.1.1（2026-11-04）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.1.2.jar` | 876 KB | `403df8bd69c40b304245423fc1d2177d573cbbbf3aad4bd85033a4a992b4db2b` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.1.2.jar` | 878 KB | `97c7ea40cffb93a0b97eb8516a72a08af3309d4e65709849d9b6afa173b029cf` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.1.2"` 已核对。产物构建于 2026-11-04。

jar 文件名仍带 MC 版本（`<mod_id>-<mc_version>-<mod_version>.jar`），两条线同时上传不会撞名。

## 本版修复（相对 1.1.1）

- **1.20.1：木钩 / 骨钩不掉耐久。** `common/event/ToolEvents#onBreak` 的 crook 分支只添加了
  钩子表的额外掉落，漏掉了工具磨损；用木钩/骨钩打树叶时耐久始终是满的。
  现在按锤子同款逻辑处理：非创造模式、破坏的方块确实有钩子表时，对**实际手持的**钩子
  扣 1 点耐久；耐久耗尽时正常触发损坏回调。
- **1.21.1 线同一逻辑本来就正确**（`BlockDropsEvent` → `wearMainHand`），本版只同步版本号，
  没有代码改动。
- 没有改动任何配方、筛表、堆肥表、桶表、渲染或贴图。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.1.2 是一个纯 Bug 修复版。
> 修复了 1.20.1 上木钩 / 骨钩破坏树叶时不掉耐久的问题：现在每次有效破坏会正常消耗 1 点耐久，
> 耗尽时正常损坏。1.21.1 线的耐久逻辑本来就正常，本版只同步版本号。
> 没有新增玩法内容，也不影响存档兼容。

**English**：

> 1.1.2 is a bug-fix-only release.
> Fixed wooden and bone crooks not losing durability when breaking leaves on 1.20.1; each valid
> leaf break now costs 1 durability and the crook breaks normally at 0. The 1.21.1 line already
> behaved correctly; this release only syncs the version number.
> No gameplay content was added, and existing worlds are unaffected.

## 发布前检查

- [x] 两条线 `mod_version=1.1.2`
- [x] `CHANGELOG.md` 的 1.1.2 节（明确标注 Bug 修复）
- [x] README 下载表版本号
- [x] 两条线 `build` 成功，jar 内版本号 1.1.2
- [x] 两份 jar 的大小 / sha256 回填
- [x] 耐久修复实测（服务端 FakePlayer + `gameMode.destroyBlock`）
- [ ] 打 tag：`v1.1.2`（main）、`v1.1.2-1.20.1`（1.20.1-forge）
- [ ] 推送分支与 tag 到 origin
- [ ] CurseForge 上传 1.21.1 / 1.20.1 两个文件
- [ ] Modrinth 上传对应两个版本
- [ ] 把商店下载链接补进 README

## 已知限制

- 创造模式下钩子仍然不磨损（与锤子及原版工具一致，是有意行为）。
- 1.20.1 分支筛表比主线少 3 条钴（沙/砂砾/尘土各 1/64），基表 113 vs 116。
- 客户端画面长期欠账仍在：机器 GUI、自动输出抽屉、JEI tooltip 等未逐项目视。
