# 1.5.0 发布材料

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.5.0`，**仅 1.21.1 主线**；1.20.1 线仍停在 `1.4.2`，扩容升级尚未移植。
> **性质：功能更新**——新增机器产物缓存扩容升级与 GUI 翻页；顺手修复输出合并时的复制 bug。
> 上一版：1.4.2（2026-11-11，1.20.1 线内容 / 1.21.1 版本对齐）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.5.0.jar` | 954 KB（977269 B） | `bc06c41f52e3e26f3bf0184cf1d7a094ee44181daacb14e894864754dc783ff6` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、图标、`META-INF/neoforge.mods.toml`（`version="1.5.0"`），
以及新的 `capacity_upgrade` 配方 / 标签 / 模型 / 贴图。产物构建于 2026-11-13（Java 21）。
jar 的 sha256 取自本次最终构建；重建会写入新的时间戳，sha 不会相同。

## 本版内容（相对 1.4.2）

- **扩容升级** `capacity_upgrade`：电动筛 / 电动锤的槽 4 新插件；每个物品 +1 页（12 格）
  产物缓存，堆叠上限 8（基础 1 页 + 8 = 9 页 / 108 格）。合成：
  `铁锭 纸 铁锭 / 纸 任意箱子 纸 / 铁锭 纸 铁锭`；箱子走
  `#exnihilosatellitium:chests`（箱子 / 陷阱箱 / 末影箱）。
- **产物翻页**：输出 3×4 下方新增 `< 1/9 >`，只对多页机器显示；客户端切页后由服务端
  全量重发菜单内容，避免跨页旧缓存造成的错位。拆掉扩容后高页物品先并回活动页，
  放不下的掉在机器上方，不会静默消失。
- **IO 不分页**：漏斗 / 管道 / 自动输出看到的是全部活动页拼成的平铺输出缓存；
  槽数随扩容变化，但不需要理解“页”。
- **修复**：`insertOutput` 在“先并进半组、后面没有空槽”时会把已并入部分再整份掉出，
  造成物品复制；现在只掉真正没放下的余量。
- **说明**：内能机器没有插件槽，保持 1 页；扩容不增加 FE/t 消耗。
- **开发环境**：主线 `run/mods` 增加 Mekanism 10.7.19 + Generators，便于给机器充电。

## 验证

- `./gradlew build --offline`：主线 1.5.0 通过。
- `check-textures.py`：308 模型 / 116 blockstate / 191 纹理。
- `check-core-imports.py`：通过。
- `check-data-paths.py`：298 recipes / 75 loot / 76 tags。
- `check-wiki.py`：8 页 / 8 译文 / 91 链接 / 74 JSON / 8 nav。
- RCON：8 扩容时第 9 页槽 113 物品保留；自动输出与漏斗都能从第 9 页槽 100 抽取；
  基础页 `6..17` 塞满后机器继续往槽 18–20 产出；拆扩容后槽 113 物品并回槽 6；
  基础页仍满时高页产物弹出且机器停机。
- **未目视**：GUI 翻页按钮 / 页标签、工作台实际合成、JEI 页面；客户端只确认了
  Mekanism 加载与机器资源无缺失报错。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.5.0（1.21.1）新增「扩容升级」：装进电动筛 / 电动锤的第 4 个插件槽，每个增加 1 页
> （12 格）产物缓存，最多 8 个（9 页）；输出区下方可翻页查看。漏斗、管道和自动输出
> 仍然把全部页当作一份平铺缓存。拆掉升级时高页物品会并回或掉出，不会丢失。
> 合成：铁锭 / 纸 / 铁锭 + 纸 / 任意箱子 / 纸 + 铁锭 / 纸 / 铁锭。
> 同时修复机器输出合并时可能复制物品的问题。

**English**：

> 1.5.0 (1.21.1) adds the **Expansion Upgrade**: install it in the electric sieve / hammer's
> 4th plugin slot; each item adds one 12-slot output page, up to 8 (9 pages total), and the
> output grid can be paged. Hoppers, pipes and auto output still see every page as one flat
> buffer. Removing the upgrade merges high pages back or drops the overflow instead of
> voiding it. Recipe: iron ingot / paper / iron ingot, paper / any chest / paper,
> iron ingot / paper / iron ingot. Also fixes a machine output duplication bug when a stack
> was partly merged and then ran out of space.

## 发布前检查

- [x] 主线 `mod_version=1.5.0`
- [x] `CHANGELOG.md` 的 1.5.0 节（中英双语）
- [x] README 下载表版本号
- [x] 主线 `build --offline` + 纹理 / core / data / wiki 守卫
- [x] tag `v1.5.0`（已推送）
- [ ] CurseForge / Modrinth 上传（所有者）
- [ ] 1.20.1 线移植扩容升级（后续批次）
