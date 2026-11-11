# 纹理规范

> 状态：M1（木桶 + 七种材质）/ 最后更新：2026-10-28

## 两条路线

- **方块（木桶、筛子、坩埚等）：vanilla-sampled。**
  模型直接引用原版 `minecraft:block/*` 贴图，**不复制文件、不打包**。
  木桶的几何与 `display` 只在 `models/block/barrel_base.json` 里写一份；每种材质是它的
  **子模型**，只覆盖贴图变量：
  - 桶身/顶沿 = 该木种的 `<wood>_planks`（侧面 `rotation: 90` 让木纹竖起来）；
  - 内壁/桶底 = `stripped_<wood>_log`，保留竖向木纹、比外壁深一档；
  - 石桶 = `stone` + `smooth_stone` 顶沿 + `deepslate_tiles` 深色箍；
  - 木桶的两道箍 = 8 个外凸 0.5px 的体素 + `gray_concrete`。
- **物品与特殊表面（瓷土、堆肥面）：自有 16×16 手绘像素。**
  放在 `assets/exnihilosatellitium/textures/`，由脚本逐像素生成，可复现。

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
│   ├── machine_*            # M10 机器 / 插件（脚本生成，见下）
│   └── barrel_compost.png   # （1.1.1 起不再使用，见 DESIGN §49）
└── item/
    └── porcelain.png        # 瓷土（自有）
```

木桶没有自有 PNG；`art/barrel/*` 是早期自绘实验，不参与游戏。
七种材质的 blockstate、子模型、物品模型、配方和掉落表由
`python3 scripts/generate_barrel_variants.py` 从同一张表生成，几何仍只维护
`models/block/barrel_base.json`。新增木种 = 在脚本表里加一行 + 补 lang。

## 矿物方块（M2 起）：底图 + 遮罩 + 纯色，按 alpha 混合

原版与 Omnia 都不是“画矿块”，而是三件套：**共用底图 + 共用遮罩（单色 + alpha）+ 一个纯色**，
运行期按 alpha 混成最终贴图（`ImageManipulator.Recolor` → `Composite`）。
实测它们的遮罩参数（我们按同一组数字重画，像素是自己的）：

| 状态 | 遮罩覆盖 | 遮罩 alpha | 读感 |
|---|---|---|---|
| 矿砾方块 | 21%（碎点 + 2~3px 小团） | 163 ≈ 64% | 砂砾上叠局部色斑，底图纹理保留 |
| 矿砂方块 | 100%（整面） | 128–142 ≈ 50% | 整块罩一层半透明色，接近纯色 |
| 矿尘方块 | 100%（整面） | 97–108 ≈ 40% | 同上，更淡 |

1.20.1 不做运行期合图，改成 **vanilla 底 + 我们的遮罩 + BlockColor**：

| 方块 | 底图（引用，不打包） | 遮罩 |
|---|---|---|
| 矿砾方块 | `minecraft:block/gravel` | `ore_mask_gravel`（21% 碎点 / 178） |
| 矿砂方块 | `minecraft:block/sand` | `ore_mask_sand`（100% / 126） |
| 矿尘方块 | `minecraft:block/gray_concrete_powder` | `ore_mask_dust`（100% / 110） |

- 三张遮罩是**唯一**随包分发的方块 PNG（`art/blocks/ore_overlay/`，16×16，白色 + alpha）。
- **罩色取 `materials.json` 的 `color`（身份色，原版 `OreList` 的那套）**，不要取物品调色板的
  中间阶：物品 `PALETTES` 是明暗阶，铁的第 3 阶是 `#B0B0B0`（灰），拿它罩色会让铁发白。
  原版物品与方块共用同一个 `Color`，这点要保留。
- 模型两个 element：底 cube `tintindex -1` 不吃色；遮罩面 `tintindex 0` 吃 `BlockColor`。
  方块 render layer 设 `translucent`，才能得到半透明罩色。
- 预览：`python3 art/blocks/ore_overlay/generate.py`。

**别走的两条路**（都试过，效果不对）：

1. 按材料生成整套方块底图——16×16 下自绘噪点读成小方块或竖条。
2. 在底图上画“清晰的矿斑/碎点”——原版是**纯色罩色**，不是有明暗的碎块；画碎点会变成另一种美术语言，
   和物品三态打架。

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
- 目前只在 `art/` 里做预览，没有进 `assets/`；M5 加 `broken_ore` / `crushed_ore` /
  `powdered_ore` 物品时再提升为 `textures/item/*.png`，并保证 16×16。
- 如果以后要省文件，可以只保留三张灰度 base + `tintindex`，用 1.20 的 `ItemColor`
  按金属染色；当前先按每金属每阶段一张，便于逐个调色。

## 引用规则

- **允许**模型引用 `minecraft:block/*`：这是 vanilla-sampled 的正常做法，贴图不随 jar 分发。
- **禁止**模型引用 `minecraft:item/*`：物品图标必须是自有的 `exnihilosatellitium:item/*`。
- 自有方块/物品引用 `exnihilosatellitium:*`，文件必须存在且为 16×16。
- 不复制 Ex Nihilo 系列或其他 GPLv3 项目的 PNG。

## AI 政策

- **不把 AI 生图直接放进 release。**
- 瓷土、堆肥面由 `art/items/generate.py` 与 `art/barrel/generate.py` 逐像素生成；
  也可由画师手绘后替换同名文件。
- moodboard / 参考图可以用 AI，但**不进仓库**、不参与打包。
- 每个新贴图在 PR/提交里注明来源（脚本、画师或 vanilla 引用）。

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
- 1.20 新增木种（mangrove / cherry / bamboo）没有做；现有六种经典木 + 石桶。
- 筛子、坩埚的 vanilla-sampled 模型与贴图待做。
- 流体侧面、流动纹理还没画；现在只有液面。
- `art/barrel/barrel_side*.png` 等早期自绘图留作参考；如果将来要改用自有贴图，
  需要同时去掉模型里的外凸箍，避免和贴图里画好的箍叠在一起。

## 末影蛋糕（2026-11-03）

- 4 张 16×16 方块贴图（Creatio MIT 原图经 restyle）：`end_cake_top.png` / `end_cake_side.png` /
  `end_cake_bottom.png` / `end_cake_inner.png`。
- 直接采用 **Ex Nihilo: Creatio（MIT）** 的 4 张 `end_cake_*.png`（原图只读在
  `art/blocks/end_cake/creatio/`）；`art/blocks/end_cake/restyle.py` 裁掉透明边
  （14×14 / 14×8）、放大到 16×16、轻微提高饱和度/对比度并加 1px 深色描边。
  预览 `art/blocks/end_cake/preview.png`（4× nearest，不进 jar）；物品栏图标预览
  `art/blocks/end_cake/inventory.png`（`render_inventory.py`，复用 barrel 渲染器）。
- 模型只写 `parent: minecraft:block/cake` / `cake_slice1..6` + 覆盖
  `particle/bottom/top/side/inside`，几何沿用 vanilla，不复制模型文件。

## 材质锤（2026-11-08）

- 9 把金属锤（锡/铝/锌/铜/银/铅/镍/锇/铂）和 3 把 Bamboo 宝石锤（樱/枫/银杏）
  都在铁锤 / 钻石锤底图上换头色，木柄不变；生成器在主线的
  `art/items/tools/hammer_variants.py`，本线只提交生成的 PNG。
- 金属复用 `art/items/ore_pieces/generate.py` 的五阶调色板；宝石用 BambooMod 宝石的
  采样色，只取颜色，不复制像素。16×16 RGBA，模型仍是 `item/handheld`。

## DE 锤（2026-11-08）

- 双足飞龙 / 神龙 / 混沌三把是 **3D cube 模型**（左右锤头 + 发光核心 + 长柄），
  每个面用 `textures/item/de_hammer/` 分区贴图；模型从主线复制，不使用 2D 图标。
- 生成器在主线的 `art/items/tools/hammer_variants.py`，本线只提交生成的 PNG。

## 碎矿锤（2026-11-09，仅 1.20.1）

- `ex_nihilo_hammer` 沿用铁锤的 16×16 轮廓，拆成 **head / handle** 两张灰度部件图：
  head 映射到金属调色板的 000/063/140/178/216/255 亮度档，handle 映射到匠魂木调色板的
  000/102/178/255 档。仍是自有像素，不复制匠魂贴图。
- 生成器在本线的 `art/items/tools/crushing_hammer_parts.py`（`--out` 可预览到 /tmp）。
- 模型 loader 是 `exnihilosatellitium:tool`：装匠魂时委托 TiC 的 `tconstruct:tool`，按每个部件
  的实际材料材质染色（部件模型 + `ToolModel.registerItemColors`）；没装匠魂时回退成
  `forge:item_layers`，不会出现 unknown loader。另补 `*_tconstruct_unknown.png` 别名图，
  避免裸物品模型刷 “Missing textures”。
- 部件贴图是**灰度**的，不要直接放彩色图；否则匠魂的 `grey_to_color` 调色板无法正确映射。
- 强化 overlay 是**彩色**小图案（不是灰度），按锤头位置画在 16×16 透明画布上；生成器
  `art/items/tools/crushing_hammer_overlays.py`，目前 17 个（急速/发光/火焰/强化/钻石/绿宝石/
  下界合金/亡灵杀手/自动烧炼/幸运/丝触/经验/不毁/斩首/磁力/坏死，加上 1.4.1 的**连锁**
  `chain`：锤头上一段钢色 + 金色的两节锁链）。装了 `exnihilosatellitium:chain` 才会画出来。
- DE 连锁模块物品 `chain_mining_module` 是自绘 16×16：深色模块底 + 深色边框 + 三节由白到金
  的锁链，`models/item/chain_mining_module.json` 走 vanilla `item/generated`；不引用 DE 的贴图。
