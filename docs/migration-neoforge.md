# NeoForge 1.21.1 迁移（spike 结论）

> 最后更新：2026-10-29。分支 `spike/neoforge-1.21.1`。

## 结论

**代码层面可行，网络层面是硬门槛。**

- ✅ 编译通过：NeoForge 21.1.265 + ModDevGradle 2.0.148 + JDK 21
- ✅ 服务端可启动：`Done (` 出现，`[materials] loaded 12 entries` 命中
  （CI run 40808132011，2 分 14 秒，含缓存）
- ❌ 本机无法构建/运行：`maven.neoforged.net` 解析到 `108.160.x.x`，TCP 443 超时；
  公网 DoH 给出同一地址，不是 DNS 污染，是路由不可达
- ❌ BMCLAPI 镜像只覆盖 `net.neoforged:neoforge`，缺 `neoform-runtime`、NeoForm 工具链

**所以：本地开发要么有代理，要么只能靠 CI 验证。**

## 环境

| 项 | 值 |
|---|---|
| JDK | Temurin 21.0.13（`~/jdks/jdk-21.0.13+9`，清华镜像下载） |
| NeoForge | 21.1.265 |
| 构建插件 | `net.neoforged.moddev` 2.0.148 |
| Gradle | 8.8（沿用 wrapper） |
| CI | `.github/workflows/neo-spike.yml`，失败时把日志推到 `ci-logs` 分支 |

## 已迁移（spike 范围）

- 入口：`@Mod` 构造注入 `(IEventBus, ModContainer)`，不再用 `FMLJavaModLoadingContext`
- 注册：`net.minecraftforge.*` → `net.neoforged.*`；`ForgeRegistries` → `Registries.BLOCK/ITEM`；
  `RegistryObject` → `DeferredHolder`
- 方块属性：`Properties.copy(...)` → `Properties.ofFullCopy(...)`
- 客户端：`RegisterColorHandlersEvent`、`FMLClientSetupEvent`、`EventBusSubscriber` 换包
- 元数据：`mods.toml` → `neoforge.mods.toml`；`loader_version_range` 必须是 `[4,)` 不是 `[47,)`
- 材料表 + 36 方块 + 36 物品 + 创意标签 + 染色，跑通

## 待迁移（正式迁移要做）

1. **`BarrelBlockEntity` 能力层**：`LazyOptional` / `Capability` 已移除，改 `Capabilities` +
   可空 `IFluidHandler`；`FluidTank` 仍在 `net.neoforged.neoforge.fluids.capability.templates`。
2. **BlockEntity 存档签名**：`saveAdditional(CompoundTag, HolderLookup.Provider)`。
3. **配方系统**（M2 要写）：1.21 的 `RecipeInput` + `MapCodec`，与 1.20.1 差异最大。
4. **网络**：若将来需要自定义包，`SimpleChannel` → `PayloadRegistrar`。
5. **Jade**：插件 API 换 1.21 版本。
6. **月光魔法联动**：等对方 1.21.1 版本；`ModList` 检查不变。

## 建议

- 若长期要在 1.21.1 上做：先解决本地依赖获取（代理最省事），否则每次改动都要走 CI，迭代太慢。
- 若暂时没有代理：M2 留在 1.20.1 做，代价是配方层将来要重写。
