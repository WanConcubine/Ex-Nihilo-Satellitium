# 纹理规范

> 状态：M1–M4（木桶 / 筛子 / 锤钩尘土 / 坩埚）/ 最后更新：2026-10-31

## 两条路线

- **方块（木桶、筛子、坩埚等）：vanilla-sampled。**
  模型直接引用原版 `minecraft:block/*` 贴图，**不复制文件、不打包**。
  木桶的几何与 `display` 只在 `models/block/barrel_base.json` 里写一份；每种材质是它的
  **子模型**，只覆盖贴图变量：
  - 桶身/顶沿 = 该木种的 `<wood>_planks`（侧面 `rotation: 90` 让木纹竖起来）；
  - 内壁/桶底 = `stripped_<wood>_log`，保留竖向木纹、比外壁深一档；
  - 石桶 = `stone` + `smooth_stone` 顶沿 + `deepslate_tiles` 深色箍；
  - 木桶的两道箍 = 8 个外凸 0.5px 的体素 + `gray_concrete`。
- **坩埚（M4）：采用 Creatio 的模型/贴图并现代化**：几何来自
  `art/blocks/crucible/creatio/block_crucible_big.json`，贴图来自
  `blockcrucible.png` / `blockcrucibleunfired.png`。`art/blocks/crucible/restyle.py`
  先做 0.8px 高斯模糊把 1.12 的高频噪点压成柔和块面，再量化到 5 色（烧制暖白瓷、
  未烧制陶土色）；模型的 `cullface` 只保留在方块边界，避免地板/短腿被相邻方块剔除。
- **物品与特殊表面（瓷土、堆肥面）：自有 16×16 手绘像素 / 脚本生成。**
  放在 `assets/exnihilosatellitium/textures/`，由脚本逐像素生成，可复现。
- **M3 工具、蚕、尘土：采用 Ex Nihilo: Creatio 的 MIT 贴图，再按 1.21 风格微调**
  （见文末「M3 工具图标」一节）。

这样和 Ex Nihilo 的“采样自原版”是同一个思路，但不复制它的 GPLv3 PNG。

## 风格

- **16×16**，1px 深色描边，无抗锯齿，左上光源，低饱和。
- 每种材质 4–6 个色阶；避免大面积纯黑/纯白。
- 对标 1.20 原版：干净、可读、克制，不追求写实噪点。
- 放大看结构要成立（16×），进游戏看整体（4× 截图）。

## 瓷土色板

| 用途 | 色值 |
|---|---|
| 描边 | `#4E483E` |
| 阴影 | `#9C9486` |
| 基础 | `#D6CEC0` |
| 亮面 | `#ECE6DB` |
| 高光 | `#FAF7F0` |

## 文件

```
assets/exnihilosatellitium/textures/
├── block/
│   └── barrel_compost.png       # 堆肥表面（自有）
└── item/
    ├── porcelain.png            # 瓷土（自有）
    ├── ancient_spores.png       # 古代孢子（M6，脚本生成，紫色）
    └── witch_water_bucket.png   # 巫水桶（M6，原版水桶重着色）
```

- **巫水流体不再自绘**：直接复用原版 `minecraft:block/water_still` / `water_flow`
  （灰度图），运行时乘 `IClientFluidTypeExtensions#getTintColor` 的紫色
  `0xFF6B44A8`。这样流动/动画全部跟原版水一致，只是颜色不同。
- **巫水桶**：`art/items/witch_water_bucket/generate.py` 读原版 `water_bucket.png`，
  只把蓝色水体按亮度映射到紫色色阶，桶身金属保持不变，生成
  `exnihilosatellitium:item/witch_water_bucket`（仓库里不存原版 PNG）。

古代孢子图标由 `art/items/ancient_spores/generate.py` 逐像素生成（紫色系，和巫水同一套色板），
要改就改脚本重跑，不要手改 PNG。

木桶没有自有 PNG；`art/barrel/*` 是早期自绘实验，不参与游戏。
瓷桶（M6）是物品桶：`art/items/porcelain_bucket/generate.py` 用原版 `bucket.png`（空桶，带桶沿）
映射出瓷白 / 陶土两套桶身，再从 `water_bucket.png` 提取水体像素做白色遮罩
`porcelain_bucket_fluid.png`（不要拿 water_bucket 当桶身，会把桶口挖成镂空）；
模型用 `exnihilosatellitium:filled` predicate 在空桶/满桶之间切换，遮罩由 `ItemColor` 按流体上色。
七种材质的 blockstate、子模型、物品模型、配方和掉落表由
`python3 scripts/generate_barrel_variants.py` 从同一张表生成，几何仍只维护
`models/block/barrel_base.json`。新增木种 = 在脚本表里加一行 + 补 lang。

## 矿物方块：单层 tint（现状）

一个**完整 cube**，六面 `tintindex: 0`，底图直接引用原版，颜色由 `BlockColor` 给：

| 方块 | 底图（引用，不打包） |
|---|---|
| 矿砾方块 | `minecraft:block/gravel` |
| 矿砂方块 | `minecraft:block/sand` |
| 矿尘方块 | `minecraft:block/gray_concrete_powder` |

```json
{ "parent": "minecraft:block/block",
  "textures": { "particle": "minecraft:block/gravel", "all": "minecraft:block/gravel" },
  "elements": [{ "from": [0,0,0], "to": [16,16,16],
    "faces": { "up": { "texture": "#all", "tintindex": 0, "cullface": "up" }, "...": {} } }] }
```

**为什么不再用“底图 + 叠加遮罩”**（试过，废弃）：

1. 双层模型在**世界里**能染色，但**物品栏图标**里叠加层不出现（GUI 渲染路径不同）。
2. 为了躲 z-fighting 把底块内缩 `0.02`，会让相邻方块之间出现缝隙。

**代价**：单层 tint 是整表面 100% 染色，丢掉了原版“21% 斑块”的观感。
要拿回斑块只能走**运行期合成贴图**（1.7.10 的做法：底图 + 遮罩 + 颜色在客户端合成），
尚未实现，记在 [status.md](status.md) 待办里。

原版参数（参考用，不代表我们实现）：矿砾遮罩 21% 覆盖 / alpha 163，矿砂 100% / 132，矿尘 100% / 102。

## 矿物碎块（M5 预览）

原版 Ex Nihilo / Omnia 是**三段式**，不是一种“矿粒”：

| 物品 | 4 个合成 | 来源 | 后续 |
|---|---|---|---|
| 破碎矿物 `broken` | 对应矿物沙砾块 | 筛沙砾 / 锤矿物块 | 烧炼沙砾块 → 锭 |
| 粉碎矿物 `crushed` | 对应矿物沙子块 | 筛沙子 / 锤矿物沙砾块 | 烧炼沙子块 → 锭 |
| 粉状矿物 `powdered` | 对应矿物粉尘块 | 筛尘土 / 锤矿物沙子块 | 烧炼粉尘块 → 锭 |

旧 Ex Nihilo 用 `base + template + color` 运行时染色（`ItemBrokenBase` / `ItemCrushedBase` /
`ItemPowderedBase`），不是每种金属一张 PNG。我们按同样思路**重绘三种不同剪影**：

- `art/items/ore_pieces/generate.py`：每种金属 × 三种阶段，输出
  铁/金/铜/锡/铅/银/镍/铝/铂/铀/锌 共 **33 张 16×16 PNG**，以及
  `preview_stages_v65.png`（四种金属的三阶段对比）、`preview_all_v65.png`（全部 11 种）和
  `reference_vs_v65_broken.png`（旧版 vs 当前版）。
- 剪影区分：
  - `broken` = **暗色石质大块 + 金属色矿点**（参考旧 Ex Nihilo 的深色矿石块，不是整块金属色）；
  - `crushed` = **散落的小金属碎片**，大小/位置不规则，像图二那种碎屑；
  - `powdered` = 宽底粉堆 + 颗粒/团块。
  三者在 16×16 下必须一眼能分。
- `broken` 边缘：轮廓保持完整，**左上受光内边 + 右下暗边**，不画整圈黑线、不加缺口，保证一眼是一块。
- 风格：暗色 1px 边、透明背景、左上光源、4–5 阶；`broken` 的石体用共享深灰调，
  矿点用金属亮色；`crushed` / `powdered` 用金属色阶。颜色参考 1.20 原版矿物/锭，
  但**不复制**旧 Ex Nihilo 或原版 PNG。
- 已经进 `assets/textures/item/<m>_<stage>.png`（由 `scripts/generate_ore_variants.py`
  从 `art/` 复制/生成），并保证 16×16。
- **自有锭**：`art/items/ingots/generate.py` 用同一套 5 阶色板生成
  `<m>_ingot.png`，16×16；只给 `materials.json` 里没有 `ingot` 的 9 种材料生成
  （铁/金/铜用原版锭贴图，不重复画）。锭的形状是斜置金属条：顶面受光、正面中间调、
  左端面暗部，1px 内切边，不复制原版锭 PNG。
- 如果以后要省文件，可以只保留三张灰度 base + `tintindex`，用 1.20 的 `ItemColor`
  按金属染色；当前先按每金属每阶段一张，便于逐个调色。

## 引用规则

- **允许**模型引用 `minecraft:block/*`：这是 vanilla-sampled 的正常做法，贴图不随 jar 分发。
- **禁止**模型引用 `minecraft:item/*`：物品图标必须是 `exnihilosatellitium:item/*`
  （自有，或采用自 MIT 授权的 Creatio 后再落到本命名空间）。
- 自有方块/物品引用 `exnihilosatellitium:*`，文件必须存在且为 16×16。
- 不复制 Ex Nihilo 系列中 **GPLv3（原版 / Omnia）或 CC BY-NC-SA（Sequentia）** 项目的 PNG；
  只有 **MIT** 的 Ex Nihilo: Creatio 允许直接采用，采用时在
  [THIRD-PARTY.md](../THIRD-PARTY.md) 登记来源与许可证。

## AI 政策

- **不把 AI 生图直接放进 release。**
- 瓷土、堆肥面由 `art/items/generate.py` 与 `art/barrel/generate.py` 逐像素生成；
  也可由画师手绘后替换同名文件。
- moodboard / 参考图可以用 AI，但**不进仓库**、不参与打包。
- 每个新贴图在 PR/提交里注明来源（脚本、画师、vanilla 引用或 MIT 的 Creatio）。

## 检查

```bash
python3 scripts/check-textures.py
```

会失败的情况：

- 模型纹理层指向 `minecraft:item/*`；
- 引用了不存在的 `exnihilosatellitium:*` 贴图；
- PNG 不是 16×16。

`minecraft:block/*` 是允许的；这条检查专门防“物品图标又变回原版占位”。

## 评审流程

1. 进游戏 4× 截图，和原版方块放一起看整体。
2. 方块轮廓、箍的粗细、木纹方向优先看；像素细节其次。
3. 通过后再进 `assets/`、再提交。

预览脚本：`art/barrel/render_current.py` 会顺着 parent 链读取当前模型和原版贴图，
输出 `showcase_current.png`、`showcase_variants.png`、`showcase_variants_3d.png`。
`render3d.py` 只对应旧的自绘方案，不再代表游戏内模型。

## 已知待办

- 桶身切角八边形还没做；当前是 14×16 内缩方桶 + 外凸箍。
- 已补齐 1.16/1.20 木材：红树 / 樱花 / 竹（`stripped_bamboo_block`）/ 绯红 / 诡异（菌柄），
  现在 11 种木桶 + 石桶；竹和下界菌柄的「内壁」贴图在生成脚本里单独指定。
- 筛子、坩埚的 vanilla-sampled 模型与贴图待做。
- 巫水有 still/flow 两套；水/熔岩仍走原版流体渲染。
- `art/barrel/barrel_side*.png` 等早期自绘图留作参考；如果将来要改用自有贴图，
  需要同时去掉模型里的外凸箍，避免和贴图里画好的箍叠在一起。

## M3 工具图标（2026-10-30，Creatio 贴图 + 1.21 风格微调）

6 种锤（木/石/铁/金/钻石）、木/骨钩、尘土方块**采用 Ex Nihilo: Creatio（1.12.2）的
16×16 PNG**；下界合金锤由钻石锤换色，蚕是 Creatio 原图的 4 帧蠕动动画（只移动头/中/尾 1px）。
Creatio 是 MIT 授权，允许复制与再分发；完整版权/许可见
[THIRD-PARTY.md](../THIRD-PARTY.md) 的「Ex Nihilo: Creatio」一节。

| 本仓库 | Creatio 源 |
|---|---|
| `textures/item/wooden_crook.png` | `items/tools/crook/crook_wood.png` |
| `textures/item/bone_crook.png` | `items/tools/crook/crook_bone.png` |
| `textures/item/wooden_hammer.png` | `items/hammerwood.png` |
| `textures/item/stone_hammer.png` | `items/hammerstone.png` |
| `textures/item/iron_hammer.png` | `items/hammeriron.png` |
| `textures/item/golden_hammer.png` | `items/hammergold.png` |
| `textures/item/diamond_hammer.png` | `items/hammerdiamond.png` |
| `textures/item/netherite_hammer.png` | 由 `hammerdiamond.png` 换色（自有衍生） |
| `textures/block/dust.png` | `blocks/blockdust.png` |
| `textures/item/silkworm.png` | Creatio `items/silkworm.png` + **4 帧动画**（原图只读，帧由脚本位移生成） |
| `textures/item/cooked_silkworm.png` | `items/silkwormcooked.png`（同款轮廓，改为褐色） |

- **锤/钩剪影不变，只改颜色**：由 `art/items/tools/restyle.py` 从
  `art/items/tools/creatio/` 里的原始采用图生成。原始 PNG 只读，不手改；
  要再调风格就改脚本重跑。
- 微调规则（对标 1.21 原版工具/骨头）：
  | 贴图 | 处理 |
  |---|---|
  | 木钩 / 骨钩 | 1px 方向光斜切：右下暗边、左上亮边（同原版骨/木剑） |
  | 木锤 | 暗边 `#372910` → `#20180a` |
  | 石锤 / 铁锤 | 描边改为原版工具近黑 `#181818`（石锤内圈 `#2e2e2e`） |
  | 金锤 | 1.12 的黄绿色 → 1.21 暖金色（`#825d16` / `#e9b115` / `#eaee57` / `#fdff76`） |
  | 钻石锤 | Creatio 已是 1.21 钻石色板，保持原样 |
  | 下界合金锤 | 钻石锤的青色系映射到原版下界合金的暗紫灰（`#231012`…`#867b86`），木柄不变 |
  | 蚕 | **4 帧蠕动动画**：保留 Creatio 原图的轮廓/绿眼，只把头、中段、尾分别上下移动 1px（第 1 帧就是原图） |
  | 熟蚕 | 与生蚕同轮廓，黑描边 → 暖深棕，体色改成 `#a8845f` / `#8d6a4a` 褐 |
  | 尘土方块 | 保留 Creatio 噪点，110 色 → 4 色量化（原版沙子只有 6 色） |
- **动画格式**：`silkworm.png` 是 16×64（4 帧 × 16），旁边 `silkworm.png.mcmeta`
  写 `{"animation":{"frametime":6}}`；`check-textures.py` 会按“有 mcmeta 的贴图”
  允许 16×(16n)，其余仍必须是 16×16。
- 预览：[m3-textures.png](images/m3-textures.png)（10 个物品 8×、尘土方块等轴、蚕取第 1 帧；
  由脚本生成）；蚕的循环见 [silkworm-animation.gif](images/silkworm-animation.gif)。
- 6 个锤/钩的物品模型是 `item/handheld`（手持角度同原版工具）；蚕/熟蚕是 `item/generated`。
- 尘土方块模型从 `minecraft:block/gray_concrete_powder` 改为自有
  `exnihilosatellitium:block/dust`（不再依赖原版粉状方块当占位）。
- 旧的 `art/items/tools/generate.py`（完全自绘）已删除；现在的 `restyle.py` 做颜色映射，
  并负责从原图生成蚕动画帧。矿尘方块（`ore_dust_block`）仍走 vanilla-sampled + tint，未受影响。
- **Sequentia（传承）1.21 是 CC BY-NC-SA 4.0**，只能看风格、不能采用其贴图。
- 待办：进游戏 4× 目视 6 种锤/2 种钩、蚕动画、尘土方块接缝。
