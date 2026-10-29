"""Ore-block overlay: vanilla base texture + our own fleck layer.

Blocks stay vanilla-sampled (AGENTS 硬规则 2): the base face references
minecraft:block/gravel (or sand / light_gray_concrete_powder) directly, and the
only PNG we ship is this 16x16 fleck mask, tinted per material at runtime.

    矿砾方块 = minecraft:block/gravel              + ore_fleck (tintindex 0)
    矿砂方块 = minecraft:block/sand                + ore_fleck
    矿尘方块 = minecraft:block/light_gray_concrete_powder + ore_fleck

    python3 art/blocks/ore_overlay/generate.py

The old hand-drawn block bodies were thrown away on purpose: they read as
bright sticks at 16x16 and duplicated the vanilla look for no gain.
"""

import importlib.util
import json
import os
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

# 三个灰度：游戏中 BlockColor 相乘后得到 亮面/本体/描边；描边保证浅色金属在浅底也读得出来
FLECK_BODY, FLECK_HI, FLECK_RIM = 0xBE, 0xFF, 0x3C

# 1px 碎点的位置：按 5x5 网格抖动铺开，避免连成一条斜线（平铺时最忌讳这个）
FLECKS = [(2, 2), (6, 3), (12, 2), (3, 7), (8, 6), (13, 7),
          (2, 12), (7, 12), (12, 12), (5, 4), (10, 10), (14, 14)]

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
    """Extract one vanilla block texture into the gitignored cache (preview only)."""
    target = VANILLA_CACHE / "assets/minecraft/textures/block" / f"{name}.png"
    if not target.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(VANILLA_JAR) as jar:
            jar.extract(f"assets/minecraft/textures/block/{name}.png", VANILLA_CACHE)
    return Image.open(target).convert("RGBA")


def fleck_mask():
    """16x16 grayscale fleck layer, transparent elsewhere. Shipped in-game."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    cells = set(FLECKS)
    for i, (x, y) in enumerate(FLECKS):
        img.putpixel((x, y), (FLECK_BODY, FLECK_BODY, FLECK_BODY, 255))
        if i % 3 == 2:  # 少数碎点画成横躺的两像素，破掉“小竖条”的重复感
            if (x + 1, y) not in cells and x < 15:
                img.putpixel((x + 1, y), (FLECK_HI, FLECK_HI, FLECK_HI, 255))
            continue
        if (x, y - 1) not in cells and y > 0:
            img.putpixel((x, y - 1), (FLECK_HI, FLECK_HI, FLECK_HI, 255))
    # 三个点画成 2x2 小簇
    for x, y in FLECKS[::4]:
        for dx, dy in ((1, 0), (0, 1), (1, 1)):
            if (x + dx, y + dy) not in cells and x + dx < 16 and y + dy < 16:
                img.putpixel((x + dx, y + dy), (FLECK_BODY, FLECK_BODY, FLECK_BODY, 255))
    # 只补右下方向的投影（不是整圈描边，整圈会变成“小黑框”）
    filled = {(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3]}
    for x, y in sorted(filled):
        for nx, ny in ((x + 1, y), (x, y + 1)):
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in filled:
                if img.getpixel((nx, ny))[3] == 0:
                    img.putpixel((nx, ny), (FLECK_RIM, FLECK_RIM, FLECK_RIM, 255))
    return img


def _rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def composite(metal, kind):
    """Preview: vanilla base multiplied by the tinted fleck layer (BlockColor 的近似)."""
    base = vanilla_block(BASES[kind]).copy()
    pal = [_rgb(c) for c in PALETTES[metal]]
    mask = fleck_mask()
    tint_body = pal[2]
    for y in range(16):
        for x in range(16):
            px = mask.getpixel((x, y))
            if not px[3]:
                continue
            k = px[0] / 255.0          # 与游戏内 tint 相乘等价
            col = tuple(min(255, int(c * k)) for c in tint_body)
            base.putpixel((x, y), col + (255,))
    return base


def item_for(metal, stage):
    p = PIECES_DIR / f"{metal}_{stage}.png"
    if p.exists():
        return Image.open(p).convert("RGBA")
    return getattr(_pieces, stage)(metal)


def rarity_of(metal):
    mats = json.load(open(ROOT / "src/main/resources/exnihilosatellitium/materials.json", encoding="utf-8"))["materials"]
    return next((m["rarity"] for m in mats if m["id"] == metal), "?")


def six_states():
    fal = fleck_mask()
    fal.save(HERE / "ore_fleck.png")
    # 白色版也导一份，方便看 mask 本身
    white = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            v = fal.getpixel((x, y))
            if v[3]:
                white.putpixel((x, y), (255, 255, 255, 255))
    white.save(HERE / "ore_fleck_mask.png")

    metals = list(PALETTES)
    steps = [("broken", "破碎矿", None), ("gravel", "矿砾方块", "gravel"),
             ("crushed", "粉碎矿", None, ), ("sand", "矿砂方块", "sand"),
             ("powdered", "粉末", None), ("dust", "矿尘方块", "dust")]
    scale, cell = 4, 64
    left, row_h = 118, cell + 20
    sheet = Image.new("RGB", (left + len(steps) * (cell + 14) + 18, 88 + len(metals) * row_h + 10), (24, 24, 28))
    d = ImageDraw.Draw(sheet)
    d.text((16, 14), "六态（方块 = 原版底 + 自有矿斑叠加）", font=font(22), fill=(238, 238, 238))
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

    # 3x3 平铺：检查叠加层在重复纹理下会不会露馅
    tile = 48
    tw = 24 + 3 * (tile * 3 + 24)
    tl = Image.new("RGB", (tw, 74 + tile * 3), (24, 24, 28))
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
    return len(metals)


if __name__ == "__main__":
    n = six_states()
    print(f"wrote ore_fleck.png, ore_fleck_mask.png, preview_six_states.png, preview_tiling.png ({n} metals)")
