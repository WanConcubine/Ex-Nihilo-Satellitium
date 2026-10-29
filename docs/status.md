# 当前状态（交接卡）

> 最后更新：2026-10-29
> 先看这 20 行，细节再翻 [DESIGN.md](DESIGN.md)、[data-format.md](data-format.md)、[textures.md](textures.md)。

## 一句话

**M1 木桶 + 七种材质变体已完成**（橡木/云杉/白桦/丛林/金合欢/深色橡木/石桶；
共享 BE、堆肥 → DIRT、1000 mB 流体、比较器、内缩 vanilla-sampled 模型、Jade tooltip）；
**M1.5 桶内方块转化、M2 筛子还没开始**。

## 现在停在哪

- 工作区：`/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium`。
- **2026-10-29 定位入档**：DESIGN 写明项目是「1.7.10 版 Ex Nihilo 1 经典复刻」，
  **联动是本职**（§6.1），承自 Ex Astris 的是**自动化思路**（§6.2：判定单一来源、
  FE 能源、升级插件）。新增 [parity.md](parity.md) 逐项对照表。**代码未改动。**
- **2026-10-28 正式改名**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；
  mod id、Java 包、assets/data 命名空间改为 `exnihilosatellitium`。
  改名前的旧世界不兼容，需要新世界（目前没有发布，影响只在本地测试档）。
- 上一个功能提交：v6.5 矿粒碎块边缘定稿（`5efa076`）。
- 本次改动：正式改名（mod id / Java 包 / assets+data 命名空间）+ 重跑构建、服务端和
  客户端资源加载；提交见 `git log -1`。随后又清掉工作区路径、构建缓存、旧日志里的旧名残留。
- 开发服 `run/server.properties` 的 `online-mode` / `enforce-secure-profile` 已恢复为 `true`；
  测试用的临时平台已清掉，spawn / gamerule 已复原。
- Jade 插件类已在 jar 里，但**没有装 Jade 做过真实 tooltip 验收**。

## 验证状态

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-10-28：`--offline` 下 `BUILD SUCCESSFUL in 8s`；直连 TLS 失败时用缓存 |
| `python3 scripts/check-textures.py` | ✅ | 七个子模型只引用 vanilla **block** 贴图 |
| 服务端启动 | ✅ | 无 Jade 也能启动；`Done (3.5s)` |
| 七种桶 BE | ✅ | 改名后 RCON：`exnihilosatellitium:barrel` 堆肥→DIRT、`DirtReady=1b`；七种材质共享同一 BE 注册表 |
| 堆肥容量/刻度 | ✅ | `data get` 读回 `Compost=1000`、`CompostScale=1000`；满后 1000 tick → DIRT |
| 堆肥数值表 | ✅ | `/reload` 日志 `[compost] loaded 65 entries`，无 unknown item / invalid tag |
| 石桶流体 NBT | ✅ | RCON 写 `FluidName`/`Amount`，读回 `minecraft:lava` / `1000` |
| 掉落表 | ✅ | `/loot spawn ... exnihilosatellitium:blocks/{spruce,stone}_barrel` 返回对应物品 |
| 资源重载 | ✅ | `/reload` 后 `Loaded 7 recipes`（正好七种桶配方）、`[compost] loaded 65 entries` |
| 客户端资源加载 | ✅ | 改名后客户端到主菜单；`Reloading ResourceManager` 无 missing model / missing texture |
| 实际进游戏目视 | ❌ | 只跑了客户端资源加载；没有截图验收比例、箍位置、物品栏图标 |
| 右键取泥土 | ⚠️ | 逻辑未改，仍未自动化（需要真实玩家） |
| 逐项右键堆肥 | ⚠️ | JSON/标签已对齐，但没有玩家逐项右键验证点数 |
| 比较器 | ⚠️ | 逻辑未改，仍未实测 |
| Jade tooltip | ⚠️ | 编译通过、无 Jade 启动通过；**未装 Jade 实测** |
| 合成配方 | ⚠️ | 7 个 JSON 都加载成功；没有玩家实测合成格 |
| 改名完整性 | ✅ | `git grep` 无旧 id/旧类名；jar 内 `mods.toml` 为 `exnihilosatellitium` / `Ex Nihilo: Satellitium` |

## 下一步（按价值排序）

1. **进游戏 5 分钟验收**（最便宜）：
   - 放七种桶，看颜色/箍/内壁是否可区分，比例是否一致；
   - 物品栏图标是否是空心 3D 桶；
   - 塞树苗看堆肥 box、粒子、50 秒颜色过渡；DIRT 后右键取泥土；
   - 装 Jade 看 tooltip。
2. **M2 筛子 + 筛网 + 数据驱动掉落**：空岛循环真正跑起来的关键。
3. **M1.5 桶内方块转化**：水 + 熔岩 → 石/黑曜石；也可以在这里给石桶加“只装熔岩”的差异化。
4. **M3 锤子 + 钩子**：锤碎圆石/砂砾，钩树叶出树苗/蚕。

## 已拍板的决策

- **项目名**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`，
  Java 包 `com.kaifengcrt.exnihilosatellitium`，资源命名空间 `exnihilosatellitium`（2026-10-28）。
- **许可证 MIT**（2026-10-27 从 ARR 转）；Forge MDK 仍 LGPL-2.1，见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **联动条目一律人工定值**（2026-10-29）：不做“发现即自动注册”，也不按 tag / worldgen 密度
  自动推概率。理由：所有分支（原版、Omnia、Sequentia、Ex Deorum）都是人工逐条定值；
  自动概率脱离手感、会打穿整合包进度设计。发现只用于**覆盖自检**，不参与定档。
  详见 [DESIGN.md](DESIGN.md) §6.1 第 4 条。
- **方块走 vanilla-sampled**：模型直接引用 `minecraft:block/*`，不复制/不打包原版贴图。
  七种桶共用 `barrel_base.json` 几何；木桶侧重木种木板 + 去皮原木内壁 + `gray_concrete` 深灰箍，
  石桶用 `stone` + `smooth_stone` + `deepslate_tiles`。
- **物品与特殊表面自有像素**：瓷土、堆肥面；不把 AI 生图放进 release。
- **原版 Ex Nihilo 与 Omnia 都是 GPLv3**：只对照玩法数值/行为，不复制代码、JSON、贴图、模型、
  音效；`reference/` 和本地 Omnia jar 只读、gitignore、不随包分发。复制 GPL 资源就必须改 GPLv3。
- **木桶 14×16×14 内缩**；物品模型用开口 block model + `display` 变换（没有封闭盖）。
- **七种桶共享一个 BE 类型**；行为/NBT/Jade 完全一致，石桶暂不做差异化。
- **堆肥用 1000 分制**：以 **Omnia 1.10.2** 的表为骨架，原版 1.7.10 校验比例；
  Sequentia 只借 1000 刻度，不采用它的重平衡。树苗/树叶 125、面包 125、花/蘑菇 100、
  腐肉/蜘蛛眼/小麦 85、肉/南瓜派 175、鱼 120、瓜块 150、胡萝卜等 80。
  满 1000 后 1000 tick（50 秒）变 DIRT。老存档按 `CompostScale` 缺失自动 ×10 迁移。
- **Jade 是可选依赖**：`compileOnly` API，不打包；没装时插件类不加载。
- **月光魔法是可选软依赖**：`moonlight_magic`，目前只占位，没有实际联动内容。

## 踩过的坑（改代码前先看）

- **ForgeGradle 卡 `Checking server connections`**：加 `-Dnet.minecraftforge.gradle.check.certs=false`。
- **直连 TLS 失败（libraries.minecraft.net / repo.maven TLS handshake）**：缓存已齐时用 `--offline` 构建/启动。
- **`FluidTank` NBT 是顶层** `FluidName` / `Amount`，不是 `{Fluid:{...}}`。
- **`#minecraft:crops` / `#minecraft:seeds` 不是 1.20.1 物品标签**；用 `#forge:*` 或具体 id。
  `minecraft:carrots/potatoes` 是方块 id，物品是 `carrot` / `potato`。
- **内缩模型的外侧面不能加 `cullface`**：墙在 z=1/x=1，不在方块边界，加了会整面被剔除。
- **堆肥标签和 JSON 必须同步**：服务端也先查 `#exnihilosatellitium:compostable`，JSON 里有但标签没有 = 不能堆肥。
- **堆肥刻度从 100 改成 1000**：老存档没有 `CompostScale`，`load()` 会 ×10 并缩放计时；新存档写 `CompostScale:1000`。
- **共享 BE 类型要列全七种方块**：`BlockEntityType.Builder.of` 漏一个，放置后 BE 不创建 / ticker 为 null。
- **旧配方 `#minecraft:planks` 必须删掉**：它会和具体木种配方同时匹配，产物不可预测。
- **子模型不要直接当方块模型用**：`barrel_base.json` 没有 `textures`，只作为 parent。
- **Jade / 月光魔法生产 jar 不能直接丢 `run/mods` 做 dev 测试**：SRG 名会 `NoSuchFieldError`；
  联调要正式服务端/客户端，或用 deobf 依赖。

## 常用命令

```bash
./gradlew build -Dnet.minecraftforge.gradle.check.certs=false
python3 scripts/check-textures.py
python3 scripts/generate_barrel_variants.py          # 从材质表重生成七种桶的 JSON
./gradlew runServer -Dnet.minecraftforge.gradle.check.certs=false   # RCON：localhost:25575 / eni
# 注意：run/ 被 gitignore；fresh clone 需要在 run/server.properties 自己开 RCON
./gradlew runClient -Dnet.minecraftforge.gradle.check.certs=false
```

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java           # 入口
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

数据在 `src/main/resources/data/exnihilosatellitium/`，格式见 [data-format.md](data-format.md)。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑”，data-format 写“JSON/NBT/命令”，textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录。
- 不要在本文件抄大段设计；链接过去。
