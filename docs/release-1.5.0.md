# 1.5.0 发布材料（1.20.1 线）

> 状态：**已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.5.0`；本文件是 **1.20.1 / Forge 47.4** 线。与 1.21.1 主线 `v1.5.0` 同版本、
> 同功能，两条线版本号重新对齐。
> 性质：**功能更新**——机器产物缓存扩容升级 + GUI 翻页；修复输出合并时的复制 bug。
> 上一版：1.4.2（2026-11-11）。
> tag：`v1.5.0-1.20.1`（2026-11-13）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.5.0.jar` | 1007 KB（1030832 B） | `50678cbcacd93ef35ec8d05f9de7c4a428faf5a97ab53da291a69cce1ee80304` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、图标、`META-INF/mods.toml`（`version="1.5.0"`），
以及新的 `capacity_upgrade` 配方 / 标签 / 模型 / 贴图。产物构建于 2026-11-13（Java 17 /
ForgeGradle 6）。jar 的 sha256 取自本次最终构建；重建会写入新的时间戳，sha 不会相同。

## 本版内容（相对 1.4.2）

- **扩容升级** `capacity_upgrade`：电动筛 / 电动锤的槽 4 新插件；每个物品 +1 页（12 格）
  产物缓存，堆叠上限 8（基础 1 页 + 8 = 9 页 / 108 格）。合成：
  `铁锭 纸 铁锭 / 纸 任意箱子 纸 / 铁锭 纸 铁锭`；箱子走
  `#exnihilosatellitium:chests`（箱子 / 陷阱箱 / 末影箱）。
- **产物翻页**：输出 3×4 下方新增 `< 1/9 >`，只对多页机器显示；客户端切页后由服务端
  全量重发菜单内容，避免跨页旧缓存错位。拆掉扩容后高页物品先并回活动页，
  放不下的掉在机器上方，不会静默消失。
- **IO 不分页**：漏斗 / 管道 / 自动输出看到的是全部活动页拼成的平铺输出缓存。
- **修复**：`insertOutput` 在“先并进半组、后面没有空槽”时会把已并入部分再整份掉出，
  造成物品复制；现在只掉真正没放下的余量。
- **说明**：内能机器没有插件槽，保持 1 页；扩容不增加 FE/t。

## 验证

- `./gradlew build --offline`：1.20.1 通过（Java 17）。
- `check-textures.py`：316 模型 / 116 blockstate / 214 纹理。
- 主线 `check-data-paths.py --target 1.20.1 --root src/main/resources`：202 recipes / 10 tags。
- 主线 `check-data-paths.py --target 1.20.1 --root src/generated/resources`：199 recipes /
  75 loot / 72 tags。
- RCON：8 扩容时第 9 页槽 113 物品保留；自动输出与漏斗都能从槽 100 抽取；
  电动锤基础页 `6..17` 塞满后继续在槽 18 产出砂砾；拆扩容后槽 113 物品并回槽 6。
- **未目视**：GUI 翻页按钮 / 页标签、工作台实际合成、JEI 页面。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.5.0 新增「扩容升级」：装进电动筛 / 电动锤的第 4 个插件槽，每个增加 1 页（12 格）
> 产物缓存，最多 8 个（9 页）；输出区下方可翻页查看。漏斗、管道和自动输出仍然把全部页
> 当作一份平铺缓存。拆掉升级时高页物品会并回或掉出，不会丢失。合成：铁锭 / 纸 / 铁锭 +
> 纸 / 任意箱子 / 纸 + 铁锭 / 纸 / 铁锭。同时修复机器输出合并时可能复制物品的问题。

**English**：

> 1.5.0 adds the **Expansion Upgrade**: install it in the electric sieve / hammer's 4th
> plugin slot; each item adds one 12-slot output page, up to 8 (9 pages total), and the output
> grid can be paged. Hoppers, pipes and auto output still see every page as one flat buffer.
> Removing the upgrade merges high pages back or drops the overflow instead of voiding it.
> Recipe: iron ingot / paper / iron ingot, paper / any chest / paper, iron ingot / paper /
> iron ingot. Also fixes a machine output duplication bug when a stack was partly merged and
> then ran out of space.

## 发布前检查

- [x] `mod_version=1.5.0`
- [x] `CHANGELOG.md` 的 1.5.0 节（中英双语）
- [x] README 下载表版本号
- [x] `build --offline` + 纹理守卫 + data 守卫
- [x] tag `v1.5.0-1.20.1`（已推送）
- [ ] CurseForge / Modrinth 上传（所有者）
