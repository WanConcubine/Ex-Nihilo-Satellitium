# 当前状态（交接卡）

> 最后更新：2026-10-27
> 先看这 20 行，细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**M1 木桶已完成并提交**（堆肥 → DIRT、1000 mB 流体、比较器、内缩 vanilla-sampled 模型、Jade tooltip）；
**M1.5 桶内方块转化、M2 筛子还没开始**。

## 现在停在哪

- 工作区：`/Users/kaifengcrt/Projects/Ex-Nihilo-Imitation`，git 干净。
- 上一个功能提交：木桶 vanilla-sampled + 深灰箍 + 开口物品模型 + 当前模型渲染图。
- 没有做到一半的代码；没有未提交的构建产物（`run/`、`build/`、`reference/` 都忽略）。
- Jade 插件类已在 jar 里，但**没有装 Jade 做过真实 tooltip 验收**。

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 需要 `-Dnet.minecraftforge.gradle.check.certs=false`，见下 |
| `python3 scripts/check-textures.py` | ✅ | 允许 vanilla block，禁止 vanilla item |
| 服务端启动 | ✅ | 无 Jade 也能启动；`Done (…)` |
| 堆肥 → DIRT | ✅ | RCON：`Compost:100, CompostTicks:1` → 2 秒后**仍是 barrel**，`DirtReady=1` |
| 流体 NBT | ✅ | RCON 写 `FluidName` / `Amount` 后 `data get` 读回 |
| 客户端资源加载 | ✅ | 无 missing model / missing texture |
| 进游戏目视 | ❌ | 没有玩家截图：桶的比例、堆肥 box、过渡、物品栏图标都只靠渲染图和推断 |
| 右键取泥土 | ⚠️ | 逻辑已实现，未自动化（需要真实玩家） |
| 比较器 | ⚠️ | 逻辑已实现，未实测（RCON 的比较器读不到 BE） |
| Jade tooltip | ⚠️ | 编译通过、无 Jade 启动通过；**未装 Jade 实测** |

## 下一步（按价值排序）

1. **进游戏 5 分钟验收**（最便宜）：
   - 放桶、塞树苗/树叶，看堆肥 box、粒子、30 秒颜色过渡；
   - 等 DIRT 后右键取泥土，确认桶还在；
   - 看物品栏/手持是不是空心 3D 桶；
   - 装 Jade 看 tooltip。
2. **M2 筛子 + 筛网 + 数据驱动掉落**：空岛循环真正跑起来的关键。
3. **M1.5 桶内方块转化**：水 + 熔岩 → 石/黑曜石；和 M4 坩埚前后一起做也行。
4. **M3 锤子 + 钩子**：锤碎圆石/砂砾，钩树叶出树苗/蚕。

## 已拍板的决策

- **许可证 MIT**（2026-10-27 从 ARR 转）；Forge MDK 仍 LGPL-2.1，见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **方块走 vanilla-sampled**：模型直接引用 `minecraft:block/*`，不复制/不打包原版贴图。
  木桶 = oak/spruce 木板 + `gray_concrete` 深灰箍（8 个外凸体素）。
- **物品与特殊表面自有像素**：瓷土、堆肥面；不把 AI 生图放进 release。
- **不复制 Ex Nihilo 的 GPLv3 PNG**；`reference/` 只读、gitignore、不随包分发。
- **木桶 14×16×14 内缩**；物品模型用开口 block model + `display` 变换（没有封闭盖）。
- **Jade 是可选依赖**：`compileOnly` API，不打包；没装时插件类不加载。
- **月光魔法是可选软依赖**：`moonlight_magic`，目前只占位，没有实际联动内容。

## 踩过的坑（改代码前先看）

- **ForgeGradle 卡 `Checking server connections`**：加 `-Dnet.minecraftforge.gradle.check.certs=false`。
- **`FluidTank` NBT 是顶层** `FluidName` / `Amount`，不是 `{Fluid:{...}}`。
- **`#minecraft:crops` / `#minecraft:seeds` 不是 1.20.1 物品标签**；用 `#forge:*` 或具体 id。
  `minecraft:carrots/potatoes` 是方块 id，物品是 `carrot` / `potato`。
- **内缩模型的外侧面不能加 `cullface`**：墙在 z=1/x=1，不在方块边界，加了会整面被剔除。
- **堆肥标签和 JSON 必须同步**：服务端也先查 `#exnihiloimitatio:compostable`，JSON 里有但标签没有 = 不能堆肥。
- **Jade / 月光魔法生产 jar 不能直接丢 `run/mods` 做 dev 测试**：SRG 名会 `NoSuchFieldError`；
  联调要正式服务端/客户端，或用 deobf 依赖。

## 常用命令

```bash
./gradlew build -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
./gradlew runServer -Dnet.minecraftforge.gradle.check.certs=false   # RCON：localhost:25575 / eni
# 注意：run/ 被 gitignore；fresh clone 需要在 run/server.properties 自己开 RCON
./gradlew runClient -Dnet.minecraftforge.gradle.check.certs=false
```

## 文件地图

```
src/main/java/com/kaifengcrt/exnihiloimitatio/
├── ExNihiloImitatio.java           # 入口
├── registry/                       # Block / Item / BlockEntity / CreativeTab
├── common/
│   ├── block/BarrelBlock.java
│   ├── blockentity/BarrelBlockEntity.java
│   ├── compost/                    # CompostRegistry + reload listener
│   ├── event/CommonEvents.java
│   └── tag/ModTags.java
├── client/                         # BarrelRenderer + ClientEvents
└── compat/
    ├── MoonlightCompat.java
    └── jade/                       # 可选 HUD
```

数据在 `src/main/resources/data/exnihiloimitatio/`，格式见 [data-format.md](data-format.md)。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑”，data-format 写“JSON/NBT/命令”，textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录。
- 不要在本文件抄大段设计；链接过去。
