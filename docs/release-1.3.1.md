# 1.3.1 发布材料

> 状态：**1.3.1 已构建、打 tag 并推送；CurseForge / Modrinth 上传待所有者操作。**
> 版本号：`1.3.1`；两条线同版本号，分别对应 1.21.1 与 1.20.1。
> 性质：**内容更新**——深板岩石子 + 末影接口（Ender IO）起步 + 脑白金物品提示。
> 上一版：1.3.0（2026-11-05）。
> 已推送：`main` d7bdadb / `1.20.1-forge` a982fc2；tag `v1.3.1` / `v1.3.1-1.20.1`（2026-11-05）。
> 注：原始 1.3.1 jar 已被后续构建覆盖；下表 sha 为 2026-11-05 按 tag 重建的 jar，
> 内容对应 tag 源码，字节可能因构建时间戳与原始构建不同。

## 产物

| 线 | 加载器 | jar | 大小 | sha256 |
|---|---|---|---|---:|
| `main` | NeoForge 21.1.x / MC 1.21.1 | `build/libs/exnihilosatellitium-1.21.1-1.3.1.jar` | 915 KB | `23b4117c55aae8699b4c3c79a667ccec8ebdd4dc1a77a12d415a6761e57ea5c4` |
| `1.20.1-forge` | Forge 47.4.x / MC 1.20.1 | `build/libs/exnihilosatellitium-1.20.1-1.3.1.jar` | 920 KB | `f814f789a248abca2ce2d58d7e2350a8e5a6a4c1667e5c01edc06367523b65c5` |

两份 jar 都包含 `LICENSE`、`THIRD-PARTY.md`、`icon.png` 和 `META-INF/*mods.toml`；
`mods.toml` 里的 `version="1.3.1"` 已核对。产物构建于 2026-11-05。

## 本版内容（相对 1.3.0）

### 新增：深板岩石子（两版，无门控）

- **泥土筛 50% × 3-5** 个 `exnihilosatellitium:deepslate_pebble`；
  **4 个 → `minecraft:cobbled_deepslate`**（2×2），熔炉烧成 `minecraft:deepslate`。
- 草方块表**不加**（`generate_sieve_assets.py` 明确过滤），保持泥土 / 草方块两套石头来源的区分。
- 用途：1.20.1 的 Ender IO「Primitive Alloy Smelter」要 6 个深板岩；
  1.21.1 的深板岩火焰合成也要它。

### 新增：末影接口（Ender IO）起步（两版，mod 门控）

- `sieve/dust_enderio.json`（根级 `"mod": "enderio"` + `"patch": true`）：尘土 **2%** 出
  `enderio:grains_of_infinity`（1.20.1/1.21.1 的「无限粉」，供虚空框架 / 电容 / 无限杆）。
- **不新增基岩粉物品、不写「粉 → 基岩」**。1.12.2 的 `dustBedrock`（基岩粉）/
  `compressed1xDustBedrock`（基岩粉块）在 1.20.1/1.21.1 的 Ender IO 6.x/8.x 已不存在。
- 没装 Ender IO 时整文件跳过，基表不变。

### 改进：脑白金提示

- `BrainPlatinumItem#appendHoverText` 增加中英双语 tooltip：
  中文「对傻子村民使用使其变为普通村民」；英文
  "Use on a nitwit villager to turn it into an ordinary villager"。

### 数据规模

- 筛表基表 118 → **119**（1.20.1 115 → **116**）；装 Ender IO 各 +1（120 / 117）。
- 纹理/模型 **295 / 171**（分支 **297 / 167**）；配方 **285**。

## 验证

- 两版 `./gradlew build --offline` 通过；`check-textures.py`、`check-core-imports.py`、
  `check-data-paths.py`、`check-wiki.py` 通过。
- 纹理守卫：主线 295 模型 / 116 blockstate / 171 纹理；分支 297 / 116 / 167。
- **未做**：进游戏手筛深板岩 / 看无限粉掉落、看脑白金 tooltip、JEI 页面目视；
  未真装 Ender IO 验联动。

## 商店更新日志（可直接粘贴）

**中文**：

> 1.3.1 内容更新：
> - 新增**深板岩石子**：泥土有 50% 概率筛出 3-5 个，4 个合成深板岩圆石，熔炉烧成深板岩。
>   1.20.1 的末影接口「原始合金炉」从此有了空岛来源。
> - **末影接口（Ender IO）**：尘土有 2% 概率筛出无限粉（`grains_of_infinity`）；
>   没装 Ender IO 时整条跳过。不新增物品、不改原版数据。
> - **脑白金**物品提示：对傻子村民使用，使其变为普通村民。

**English**：

> 1.3.1 content update:
> - Added **deepslate pebbles**: dirt has a 50% chance to sift 3-5 pebbles; four craft
>   cobbled deepslate, which smelts into deepslate. This gives 1.20.1 Ender IO's
>   Primitive Alloy Smelter a skyblock source.
> - **Ender IO**: dust has a 2% chance to sift Grains of Infinity
>   (`enderio:grains_of_infinity`); the patch is skipped when Ender IO is absent.
>   No new items, no vanilla data changes.
> - **Brain Platinum** now has a tooltip: use it on a nitwit villager to turn it
>   into an ordinary villager.

## 发布前检查

- [x] 两条线 `mod_version=1.3.1`
- [x] `CHANGELOG.md` 的 1.3.1 节
- [x] README 下载表版本号
- [x] 两版 `build` + 纹理 / 核心 / 数据 / wiki 守卫
- [x] `runData` 两版已重跑（生成物已提交）
- [ ] CurseForge / Modrinth 上传（所有者）
