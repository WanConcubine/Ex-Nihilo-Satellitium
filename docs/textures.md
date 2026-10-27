# 纹理规范

> 状态：M1（木桶 + 瓷土）／最后更新：2026-10-27

## 两条路线

- **方块（木桶、筛子、坩埚等）：vanilla-sampled。**
  模型直接引用原版 `minecraft:block/*` 贴图，**不复制文件、不打包**。
  木桶身 = `oak_planks` / `spruce_planks`（侧面 `rotation: 90` 让木纹竖起来），
  两道铁箍 = 8 个外凸 0.5px 的体素 + `iron_block`。
- **物品与特殊表面（瓷土、堆肥面）：自有 16×16 手绘像素。**
  放在 `assets/exnihiloimitatio/textures/`，由脚本逐像素生成，可复现。

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
assets/exnihiloimitatio/textures/
├── block/
│   └── barrel_compost.png   # 堆肥表面（自有）
└── item/
    └── porcelain.png        # 瓷土（自有）
```

木桶没有自有 PNG；`art/barrel/*` 是早期自绘实验，不参与游戏。

## 引用规则

- **允许**模型引用 `minecraft:block/*`：这是 vanilla-sampled 的正常做法，贴图不随 jar 分发。
- **禁止**模型引用 `minecraft:item/*`：物品图标必须是自有的 `exnihiloimitatio:item/*`。
- 自有方块/物品引用 `exnihiloimitatio:*`，文件必须存在且为 16×16。
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
- 引用了不存在的 `exnihiloimitatio:*` 贴图；
- PNG 不是 16×16。

`minecraft:block/*` 是允许的；这条检查专门防“物品图标又变回原版占位”。

## 评审流程

1. 进游戏 4× 截图，和原版方块放一起看整体。
2. 方块轮廓、箍的粗细、木纹方向优先看；像素细节其次。
3. 通过后再进 `assets/`、再提交。

早期自绘木桶的 3D 预览脚本 `art/barrel/render3d.py` 只对应旧方案，不再代表游戏内模型。

## 已知待办

- 桶身切角八边形还没做；当前是 14×16 内缩方桶 + 外凸铁箍。
- 木种变体（oak/spruce/birch/jungle/acacia/dark oak）还没做。
- 筛子、坩埚的 vanilla-sampled 模型与贴图待做。
- 流体侧面、流动纹理还没画；现在只有液面。
