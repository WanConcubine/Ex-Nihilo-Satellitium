# 1.3.2 发布材料

> 状态：**1.3.2 已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.3.2`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> 性质：**Bug 修复**——木桶满桶右键拦截 + 物品 / 流体管道 capability。
> 上一版：1.3.1（2026-11-05）。
> tag：`v1.3.2` / `v1.3.2-1.20.1`（2026-11-05）。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.3.2.jar` | 917 KB | `e3c2e14a7ef78854a30ab98ec8d2ef566388c9b38b1d1d9378521692b6cf4833` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.3.2.jar` | 923 KB | `0049790fd141107bb31b8996e9952d3a2f448b6c385f799a8e99c70517a6622b` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.3.2"` 已核对。产物构建于 2026-11-05。

## 本版内容（相对 1.3.1）

### 修复：满桶右键堆肥材料会放方块

- 可堆肥物品现在**始终**被木桶视为「已处理」：`BarrelBlockEntity#canUse` 对 `#compostable`
  直接返回 true；满桶 / 有流体时 `use()` 返回 `InteractionResult.CONSUME`，
  不再落到原版 `ItemStack#useOn` 的放置逻辑。
- 客户端预测走同一条 `canUse`，不会产生幽灵方块。
- 主线用 `ItemInteractionResult` 把 `consumesAction()` 映射为 `SUCCESS`；1.20.1 的
  `Block#use` 直接返回 `CONSUME`。

### 新增：木桶接管道

- **物品**：`platform/barrel/BarrelItemHandler`
  - 槽 0 = 堆肥输入（插入即按 `CompostRegistry` 计分，可一次吃多件）；
  - 槽 1 = 泥土 / 固体产物输出（dirtReady 或圆石/黑曜石等）。
  - 堆肥进行中、有固体、有泥土时拒绝输入；输出随时可抽。
  - 1.21.1 在 `CapabilityEvents` 注册 `Capabilities.ItemHandler.BLOCK`；1.20.1 在 BE
    `getCapability` 返回 `ForgeCapabilities.ITEM_HANDLER`。
- **流体**：1.21.1 原有 `FluidHandler.BLOCK` 保留；1.20.1 补上
  `ForgeCapabilities.FLUID_HANDLER`（`getCapability` 返回 tank）。
- 状态互斥不变：桶里有流体时不能堆肥；堆肥进度 > 0 时不能灌流体，管道也遵守。

## 验证

- 两版 `./gradlew build --offline` 通过；`check-textures.py`、`check-core-imports.py`、
  `check-data-paths.py`、`check-wiki.py` 通过。
- 纹理守卫：主线 295 模型 / 116 blockstate / 171 纹理；分支 297 / 116 / 167。
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

- [x] 两条线 `mod_version=1.3.2`
- [x] `CHANGELOG.md` 的 1.3.2 节
- [x] README 下载表版本号
- [x] 两版 `build` + 纹理 / 核心 / 数据 / wiki 守卫
- [ ] CurseForge / Modrinth 上传（所有者）
