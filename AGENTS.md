# AGENTS.md —— 在这个仓库里干活的契约

> 这是入口，不是文档本体。**每次会话先读 [docs/status.md](docs/status.md) 的「现在停在哪 / 验证状态 / 下一步」。**
> 当前状态只写在 status.md；DESIGN 写“为什么/架构”，data-format 写“JSON/NBT”，textures 写“美术规则”。

## 1. 项目是什么

- **Minecraft 1.20.1 / Forge 47.4.x / Java 17**，mod id `exnihilosatellitium`。
- 独立实现的 Ex Nihilo 风格空岛模组；**不是** Ex Nihilo 的官方续作。
- 许可证 **MIT**；第三方声明见 [THIRD-PARTY.md](THIRD-PARTY.md)。

## 2. 构建与验证

```bash
# ForgeGradle 的 TLS 环境检查在部分网络会卡住，统一加这个参数
./gradlew build -Dnet.minecraftforge.gradle.check.certs=false

# 纹理守卫：允许 vanilla block，禁止 vanilla item
python3 scripts/check-textures.py

# 开发服（RCON 已配：localhost:25575 / eni）
./gradlew runServer -Dnet.minecraftforge.gradle.check.certs=false

# 开发客户端
./gradlew runClient -Dnet.minecraftforge.gradle.check.certs=false
```

- 改完**至少**跑 `build` + `check-textures.py`。
- 服务端逻辑用 RCON 验（`/setblock`、`/data merge`、`/execute if block`）。
- **测试绿 ≠ 画面/手感验过**；没进游戏看过就不要在 status.md 写“已验证”。
- 不要在 dev 的 `run/mods` 里塞生产 jar（Jade/月光魔法）做测试；SRG 名会 `NoSuchFieldError`。

## 3. 硬规则

1. **不复制 GPLv3 代码/资源。**
   - 原版 Ex Nihilo 与 Ex Nihilo Omnia 都是 **GPLv3**；`reference/` 和本地 Omnia jar 只读、已 gitignore，**不要提交、不要打包、不要 import 它的类**。
   - 可以对照**玩法数值/行为**，但不要复制它的代码、JSON 文件、贴图、模型或音效；数值表和规则用本项目自己的 JSON/代码重新表达。
   - 需要行为参考时，自己重写；只借鉴功能性思路。要复制 GPL 资源就必须得到所有者同意并把相应部分按 GPLv3 处理。
2. **方块走 vanilla-sampled，物品走自有像素。**
   - 允许模型引用 `minecraft:block/*`（不复制文件、不随 jar 分发）。
   - **禁止**模型引用 `minecraft:item/*`；物品图标必须是 `exnihilosatellitium:item/*`。
   - 不把 AI 生图直接放进 release；moodboard 不进仓库。
3. **堆肥标签和 JSON 必须同步。**
   - 服务端也先查 `#exnihilosatellitium:compostable`；JSON 有但标签没有 = 不能堆肥。
   - 标签有但 JSON 没有 = 按 1 点处理。新增条目两处都加。
4. **内缩模型的外侧面不要加 `cullface`。**
   - 墙在 x/z=1，不在方块边界；加了会在相邻方块旁整面被剔除。
5. **许可证变更必须得到仓库所有者明确同意。**
   - 默认 MIT；不要为了抄某个 GPL 资源而偷偷改 license。

## 4. 代码地图

```
com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java        # 入口，注册 DeferredRegister
├── registry/                    # ModBlocks / ModItems / ModBlockEntities / CreativeTabs
├── common/
│   ├── block/BarrelBlock.java   # 形状、交互分发、ticker、比较器
│   ├── blockentity/             # 状态机、FluidTank、NBT、粒子/音效
│   ├── compost/                # JSON 注册表 + reload listener
│   ├── event/CommonEvents.java # AddReloadListenerEvent
│   └── tag/ModTags.java
├── client/                      # BarrelRenderer、BER 注册
└── compat/
    ├── MoonlightCompat.java     # 可选软依赖，只按注册名查找
    └── jade/                    # 可选 Jade 插件
```

- 方块状态尽量少；自定义进度放 BE，`getUpdateTag`/`getUpdatePacket` 同步。
- 数据驱动优先：配方/掉落/堆肥值能进 JSON 就不要硬编码。
- 新方块默认 16×16 纹理；内缩模型要和 `getShape`/`getCollisionShape` 同步。

## 5. 文档分工

| 文档 | 写什么 |
|---|---|
| [docs/status.md](docs/status.md) | **当前状态**：停在哪、验证表、下一步、坑 |
| [docs/DESIGN.md](docs/DESIGN.md) | 原则、目标、核心循环、架构、里程碑、决策记录 |
| [docs/data-format.md](docs/data-format.md) | 堆肥 JSON、物品标签、NBT 键、命令、比较器 |
| [docs/textures.md](docs/textures.md) | 纹理路线、色板、引用规则、AI 政策、检查 |
| [README.md](README.md) | 面向玩家/贡献者的入口 |
| [THIRD-PARTY.md](THIRD-PARTY.md) | 第三方与参考来源 |

- 改完功能：先更新 `status.md` 验证表，再补 `DESIGN.md` 的实现记录。
- 改 JSON/NBT/命令：更新 `data-format.md`。
- 改贴图/模型引用规则：更新 `textures.md`，并跑检查脚本。
- 不要在多处抄同一份“当前进度”；status.md 是唯一出处。

## 6. 不要提交

`build/`、`run/`、`reference/`、`libs/`、`art/barrel/.vanilla_textures/`、`.gradle/`。
提交前 `git status --short` 看一眼。
