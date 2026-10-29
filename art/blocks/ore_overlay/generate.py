"""Ore-block colour masks: vanilla base + one flat colour wash (Ex Nihilo's scheme).

原版/ Omnia 的方块都不是手绘矿块，而是三件套：
    base(灰度) + template(单色 + alpha) -> 按 alpha 混合成最终贴图

我们换成 1.20.1 的做法：底图仍引用原版（vanilla-sampled，不打包），
只出三张 16x16 的「遮罩」PNG，游戏里由 BlockColor 相乘上色，方块走 translucent。

实测原版模板的覆盖与 alpha（本项目按同一组数字重画，像素是自己的）：
    gravel 21% 覆盖, alpha 163  -> 稀疏色斑
    sand   100% 覆盖, alpha 132 -> 整面罩色
    dust   100% 覆盖, alpha 102 -> 整面罩色（更淡）

    python3 art/blocks/ore_overlay/generate.py
"""

import importlib.util
import json
import os
import random
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
PIECES_DIR = ROOT / "art" / "items" / "ore_pieces"
VANILLA_JAR = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"
VANILLA_CACHE = HERE / ".vanilla_textures"

_spec = importlib.util.spec_from_file_location("ore_pieces_gen", PIECES_DIR / "generate.py")
_pieces = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_pieces)
OSMIUM = ("#26313D", "#3F5B78", "#608FC4", "#8FB4DC", "#C4DCF4")
_pieces.PALETTES.setdefault("osmium", OSMIUM)
PALETTES = dict(_pieces.PALETTES)

BASES = {"gravel": "gravel", "sand": "sand", "dust": "light_gray_concrete_powder"}


def font(size):
    for p in ("/System/Library/Fonts/PingFang.ttc", "/System/Library/Fonts/Hiragino Sans GB.ttc"):
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size, index=0)
            except Exception:
                pass
    return ImageFont.load_default()


def vanilla_block(name):
    target = VANILLA_CACHE / "assets/minecraft/textures/block" / f"{name}.png"
    if not target.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(VANILLA_JAR) as jar:
            jar.extract(f"assets/minecraft/textures/block/{name}.png", VANILLA_CACHE)
    return Image.open(target).convert("RGBA")


def gravel_mask(target=55, alpha=163):
    """21% 覆盖、统一 alpha 163 的稀疏色斑（原版 gravel 模板就是这两档 alpha）。"""
    centres = [(3, 3), (10, 4), (12, 10), (5, 11), (8, 14), (2, 8)]
    cand = []
    for cx, cy in centres:
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
                if d <= 2.6:
                    cand.append((d, x, y))
    cand.sort(key=lambda t: (t[0], t[1], t[2]))
    picked = {(x, y) for _, x, y in cand[:target]}
    return [[(255, 255, 255, alpha if (x, y) in picked else 0) for x in range(16)] for y in range(16)]


def wash(level, jitter, seed):
    """整面罩色：alpha 在 level±jitter 之间抖动，保证不是死板一块。"""
    rng = random.Random(seed)
    return [[(255, 255, 255, max(0, min(255, level + rng.randint(-jitter, jitter))))
             for _ in range(16)] for _ in range(16)]


def mask_image(kind):
    rows = {"gravel": gravel_mask,
            "sand": lambda: wash(132, 12, 21),
            "dust": lambda: wash(102, 10, 33)}[kind]()
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), rows[y][x])
    return img


def _rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def composite(metal, kind):
    """预览：按 alpha 把纯色混到底图上，等价于游戏里的 tint + translucent。"""
    base = vanilla_block(BASES[kind]).copy()
    col = _rgb(PALETTES[metal][2])
    m = mask_image(kind)
    for y in range(16):
        for x in range(16):
            a = m.getpixel((x, y))[3] / 255.0
            if a <= 0:
                continue
            br, bg, bb, _ = base.getpixel((x, y))
            base.putpixel((x, y), (int(col[0] * a + br * (1 - a)),
                                   int(col[1] * a + bg * (1 - a)),
                                   int(col[2] * a + bb * (1 - a)), 255))
    return base


def item_for(metal, stage):
    p = PIECES_DIR / f"{metal}_{stage}.png"
    if p.exists():
        return Image.open(p).convert("RGBA")
    return getattr(_pieces, stage)(metal)


def rarity_of(metal):
    mats = json.load(open(ROOT / "src/main/resources/exnihilosatellitium/materials.json", encoding="utf-8"))["materials"]
    return next((m["rarity"] for m in mats if m["id"] == metal), "?")


def main():
    for kind, name in (("gravel", "ore_mask_gravel"), ("sand", "ore_mask_sand"), ("dust", "ore_mask_dust")):
        mask_image(kind).save(HERE / f"{name}.png")

    metals = list(PALETTES)
    steps = [("broken", "破碎矿", None), ("gravel", "矿砾方块", "gravel"),
             ("crushed", "粉碎矿", None), ("sand", "矿砂方块", "sand"),
             ("powdered", "粉末", None), ("dust", "矿尘方块", "dust")]
    cell, left, row_h = 64, 118, 84
    sheet = Image.new("RGB", (left + 6 * (cell + 14) + 18, 88 + len(metals) * row_h + 10), (24, 24, 28))
    d = ImageDraw.Draw(sheet)
    d.text((16, 14), "六态（方块 = 原版底 + 纯色按 alpha 混合）", font=font(22), fill=(238, 238, 238))
    for j, (_, cap, _) in enumerate(steps):
        d.text((left + j * (cell + 14), 54), cap, font=font(14), fill=(168, 168, 174))
    for i, metal in enumerate(metals):
        y = 84 + i * row_h
        d.text((16, y + 8), metal, font=font(17), fill=(236, 236, 236))
        d.text((16, y + 32), f"1/{rarity_of(metal)}", font=font(12), fill=(140, 140, 146))
        for j, (stage, _, kind) in enumerate(steps):
            spr = composite(metal, kind) if kind else item_for(metal, stage)
            big = spr.resize((cell, cell), Image.NEAREST)
            ci = Image.new("RGB", (cell, cell), (33, 33, 37))
            ci.paste(big, (0, 0), big)
            sheet.paste(ci, (left + j * (cell + 14), y))
    sheet.save(HERE / "preview_six_states.png")

    tile = 48
    tl = Image.new("RGB", (24 + 3 * (tile * 3 + 24), 74 + tile * 3), (24, 24, 28))
    td = ImageDraw.Draw(tl)
    td.text((16, 14), "3×3 平铺（copper）", font=font(18), fill=(236, 236, 236))
    for i, kind in enumerate(("gravel", "sand", "dust")):
        src = composite("copper", kind).resize((tile, tile), Image.NEAREST)
        x0, y0 = 24 + i * (tile * 3 + 24), 52
        for ty in range(3):
            for tx in range(3):
                tl.paste(src, (x0 + tx * tile, y0 + ty * tile), src)
        td.text((x0, y0 + tile * 3 + 2), BASES[kind], font=font(12), fill=(160, 160, 166))
    tl.save(HERE / "preview_tiling.png")
    print(f"wrote 3 masks + previews ({len(metals)} metals)")


if __name__ == "__main__":
    main()
