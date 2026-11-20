# 1.5.1 发布材料

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.5.1`；两条线同版本、同功能（`v1.5.1` / `v1.5.1-1.20.1`）。
> **性质：配置项 + 游戏内配置界面**——新增矿物隐藏开关与保留白名单，两线都能在模组列表里调。
> 上一版：1.5.0（2026-11-13，机器扩容升级）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.5.1.jar` | 1000123 B | `872b3cd3bf8ef4426139a415bd485ff49a88da62b11f1a0c14214fcda671eaed` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.5.1.jar` | 1106144 B | `75d7cce499e7188ebf1f3402e0e56b28bd87c44860d9f015750c70a18370d559` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、图标、`META-INF/*.toml`（`version="1.5.1"`）、
`client/ConfigScreens`（1.20.1 是手写小屏，故多一个内部类）以及 `lang/*` 里的配置标签。
jar 的 sha256 取自本次最终构建；重建会写入新的时间戳，sha 不会相同。

## 本版内容（相对 1.5.0）

- **矿物开关** `ores.hideUnprovided`（默认 `false`）：`materials.json` 里列的候选模组一个都没装时，
  该材料的整条链（筛 / 锤产出、创造栏条目、JEI 页面）不再出现——例如没装匠魂却筛出钴 / 阿迪特。
  判定口径是 `OreMaterial.hasOwnProduct()`：本模组只能回退到自己注册的锭（候选模组没加载，或像铂这样
  压根没有候选表）。铁 / 金 / 铜指向原版物品，不受影响。
- **保留白名单** `ores.keep`（默认 `["platinum"]`）：本模组自有的经典内容不受开关影响；只影响隐藏规则，
  注册期就被跳过的宝石条目不会被它复活。写的是 `materials.json` 的 `id`，配置校验按 `[a-z0-9_]{1,64}`。
- **不注销注册**：物品 / 方块仍在（旧存档安全，`/give` 仍能拿到），只是不再产出和展示。
- **会话快照**：`OreMaterials` 每次会话只读一次这两个选项，避免中途改配置时筛 / 锤表与创造栏不同步；
  改完必须重启（与配置注释、界面提示一致）。
- **游戏内配置界面**：1.21.1 注册 `IConfigScreenFactory` → NeoForge 通用 `ConfigurationScreen`
  （8 个键都在，`[ores]` 标 `gameRestart()`，关界面时自动弹重启确认）；1.20.1 是手写小屏，
  只放 `[ores]` 两项，常驻重启提示、保存后显示“重启后生效”，标 `worldRestart()`。
  `client/ConfigScreens` 由 `FMLEnvironment.dist` 守门，专用服不会加载客户端类。
- **文档**：`docs/data-format.md` §8（键表 + 实现入口）、`CHANGELOG.md`、`docs/status.md`。

## 验证

- `./gradlew build`：主线 `exnihilosatellitium-1.21.1-1.5.1.jar`、1.20.1 `...-1.20.1-1.5.1.jar` 均通过。
- `check-textures.py`：主线 308 模型 / 116 blockstate / 191 纹理；1.20.1 316 / 116 / 214。
- `check-core-imports.py`：主线 0 known leak；1.20.1 仍是原有 1 个（`MachineBlockEntity` 的能力引用）。
- `check-data-paths.py`：主线 298 recipes / 75 loot / 76 tags。
- `check-wiki.py`：8 页 / 8 译文 / 91 链接 / 74 JSON / 8 nav。
- 1.21.1 开发服：`hideUnprovided=true` 时日志隐藏集合为
  `silver_ / nickel_ / aluminum_ / zinc_ / cobalt_ / ardite_`（本机只有 Mekanism 在场，锡 / 铅 / 锇 / 铀
  保留；铂因默认 `keep` 保留），锤表 67 → 49 条规则；默认关时仍是筛表 119 / 锤表 67。
- 服务端启动正常，`client/ConfigScreens` 没有被专用服加载（dist 守门生效）。
- **未目视**：1.21.1 的通用配置屏、1.20.1 的手写小屏、JEI 与创造栏的实际显示（开发客户端探针没起来）。
- 1.20.1 线：`build` + 纹理 / core 守卫通过。

## 商店更新日志（可直接粘贴）

**中文**

> **1.5.1 — 矿物开关与游戏内配置界面（1.21.1 / 1.20.1 同版本）**
>
> 新增两个配置项：
>
> - `ores.hideUnprovided`（默认关）：材料表里写了候选模组、但你一个都没装的那种矿，筛 / 锤产出、
>   创造栏和 JEI 都不再出现——比如没装匠魂就不会再筛出钴 / 阿迪特；锡、铅等金属同理，缺少对应模组时
>   一起隐藏（铁 / 金 / 铜 不受影响）。
> - `ores.keep`（默认 `["platinum"]`）：白名单，本模组自有的经典矿不会被上面那个开关误删（铂默认保留）。
>
> 两项都**需要重启游戏**才生效；游戏内也能改：1.21.1 在模组列表点 Config，1.20.1 同一个按钮会打开配置屏。
>
> 物品注册不变，旧存档安全，`/give` 仍能拿到；只是不再产出与展示。

**English**

> **1.5.1 — Ore toggle and in-game config screen (1.21.1 / 1.20.1, same version)**
>
> Two new options:
>
> - `ores.hideUnprovided` (off by default): when none of the candidate mods listed in the material table is
>   installed, that ore stops appearing in sifting / hammering, the creative tab and JEI — e.g. cobalt and
>   ardite without Tinkers' Construct, or tin / lead without their mods (iron / gold / copper are unaffected).
> - `ores.keep` (default `["platinum"]`): a whitelist so classic content this mod owns (platinum) is not
>   removed by the switch above.
>
> Both need a game restart; you can also change them in game — the mod list's Config button on 1.21.1,
> and a small config screen on the same button on 1.20.1.
>
> Registration is unchanged, existing worlds stay safe and `/give` still works; the ore just stops dropping
> or showing up.

## 发布前检查

- [x] 两条线 `mod_version=1.5.1`
- [x] `CHANGELOG.md` 的 1.5.1 节（中英双语）
- [x] README 下载表版本号
- [x] 主线 `build` + 纹理 / core / data / wiki 守卫；1.20.1 `build` + 纹理 / core 守卫
- [x] tag `v1.5.1` / `v1.5.1-1.20.1`（已推送）
- [ ] CurseForge / Modrinth 上传（所有者）
