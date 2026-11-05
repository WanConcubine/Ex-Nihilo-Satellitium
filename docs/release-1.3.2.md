# 1.3.2 发布材料（1.20.1 线）

> 状态：**1.3.2 已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.3.2`；本文件是 **1.20.1 / Forge 47.4** 线，1.21.1 线的同版本说明在 `main` 分支。
> 性质：**Bug 修复**——木桶满桶右键拦截 + 物品 / 流体管道 capability。
> 上一版：1.3.1（2026-11-05）。
> tag：`v1.3.2-1.20.1`（2026-11-05）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.3.2.jar` | 923 KB | `0049790fd141107bb31b8996e9952d3a2f448b6c385f799a8e99c70517a6622b` |

jar 包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/mods.toml`；
`mods.toml` 里的 `version="1.3.2"` 已核对。产物构建于 2026-11-05。

## 本版内容（相对 1.3.1）

### 修复：满桶右键堆肥材料会放方块

- 可堆肥物品现在**始终**被木桶视为「已处理」：`BarrelBlockEntity#canUse` 对 `#compostable`
  直接返回 true；满桶 / 有流体时 `use()` 返回 `InteractionResult.CONSUME`，
  不再落到原版放置逻辑。
- 客户端预测走同一条 `canUse`。

### 新增：木桶接管道

- **物品**：`platform/barrel/BarrelItemHandler`
  - 槽 0 = 堆肥输入（插入即按 `CompostRegistry` 计分，可一次吃多件）；
  - 槽 1 = 泥土 / 固体产物输出。
  - 堆肥进行中、有固体、有泥土时拒绝输入；输出随时可抽。
  - BE 的 `getCapability` 返回 `ForgeCapabilities.ITEM_HANDLER`。
- **流体**：BE 的 `getCapability` 返回 `ForgeCapabilities.FLUID_HANDLER`（tank）。
- 状态互斥不变：桶里有流体时不能堆肥；堆肥进度 > 0 时不能灌流体，管道也遵守。

## 验证

- `./gradlew build --offline` 通过；`check-textures.py` 通过（297 模型 / 116 blockstate / 167 纹理）。
- 分支没有 core/data 守卫脚本（与 1.3.0 / 1.3.1 相同）。
- **未做**：进游戏测满桶右键、物品 / 流体管道（开发环境没有装管道模组）、JEI 目视。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.3.2 修复：
> - 修复满桶右键堆肥材料（如树叶）时会把方块放到桶顶的问题。
> - 木桶现在支持物品和流体管道：物品槽 0 输入堆肥、槽 1 输出泥土 / 固体产物；
>   1.20.1 补上了缺失的管道能力。堆肥与流体仍然互斥。

**English**：

> 1.3.2 fixes:
> - Fixed compostable items (e.g. leaves) being placed on top of a full barrel when
>   right-clicked.
> - Barrels now expose item and fluid capabilities to pipes: item slot 0 accepts
>   compost, slot 1 outputs dirt / solid products; 1.20.1 gained the missing
>   capabilities. Compost and fluid remain mutually exclusive.

## 发布前检查

- [x] `mod_version=1.3.2`
- [x] `CHANGELOG.md` 的 1.3.2 节
- [x] README 下载表版本号
- [x] `build` + `check-textures.py`
- [ ] CurseForge / Modrinth 上传（所有者）
