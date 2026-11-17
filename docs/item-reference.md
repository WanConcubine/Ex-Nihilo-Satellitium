# 物品资料参考（百科填写用）

> 适用：`[ExNSA] 无中生有：众星捧月` **1.5.0**。主线 **1.21.1 / NeoForge 21.1**；1.20.1 / Forge 47.4 分支同版本、数值一致，但**内容不完全相同**：BambooMod 宝石锤、匠魂碎矿锤、黏液草 / 黏液泥土只在 1.20.1 线；金属锤、龙之进化锤两版都有。
> 数据来源：仓库实际代码与数据表（`src/main/java`、`src/main/resources/data`、`src/generated`），**默认配置**。
> 换算：**20 tick = 1 秒**；概率均为「每次判定」的独立概率，不是保底。
> 本表只写**简介 + 数值 + 获得方式**；注册名、最大耐久按批量导出填写。

---

## 0. 先对齐四个底层数值

| 对象 | 数值 | 说明 |
|---|---|---|
| 筛子 | 100 工作量，2/tick | 每次右键让筛子继续工作 8 tick（16 点）；连续右键约 **2.5 秒（≈7 次）**筛完 1 个原料 |
| 锤 | 每次破坏 **-1 耐久**，挖掘速度 6.0 | 只在锤表内方块上替换掉落，其它方块按空手处理；等级只决定耐久，**不改变产物** |
| 钩 | 每次破坏树叶 **-1 耐久** | 在原版树叶掉落**之上追加**；骨钩概率 **×1.5**（单项上限 100%） |
| 桶 | 1000 mB；堆肥 1000 点 | 堆肥满后 **30 秒**变 1 个泥土；木桶上限 433 K（熔岩 1300 K 装不了） |

---

## 1. 材料

### 1.1 金属锭

矿石链对所有金属通用：**4 个碎片 → 1 个矿块 → 锤成 4–6 个下一级碎片**；矿块也可直接烧成锭（矿块烧 200 tick、0.1 经验）。表中「掉率」= 筛**沙砾 / 沙子 / 尘土**时出对应碎片（破碎 / 粉碎 / 粉末）的概率，三张表同率。

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 铝锭 `aluminum_ingot` | 轻金属，铝矿链的终点 | 筛出率 **12.5%（1/8）**，仅次于铁（20%） | 烧铝矿块；装 IE 时产物为 `immersiveengineering:ingot_aluminum`，否则为本模组铝锭 |
| 锇锭 `osmium_ingot` | 重铂族金属，锇矿链终点 | 筛出率 **10%（1/10）** | 烧锇矿块；装 Mekanism 时给 Mekanism 锇锭，否则本模组锇锭 |
| 阿迪特锭 `ardite_ingot` | 下界橙色合金金属 | **只在粉碎下界岩**里出，**1%** | 烧阿迪特矿块；装 TiT（`tinkers_thinking`）时给 TiT 锭，否则本模组锭 |
| 钴锭 `cobalt_ingot` | 下界蓝色金属，匠魂路线 | 筛出率 **1.56%（1/64）**；粉碎下界岩另 **1%** | 烧钴矿块；装匠魂时给匠魂钴锭，否则本模组钴锭 |
| 铅锭 `lead_ingot` | 重而软，前期廉价金属 | 筛出率 **3.12%（1/32）** | 烧铅矿块；Mekanism / Thermal / IE 任一在场就给对应锭，否则本模组铅锭 |
| 镍锭 `nickel_ingot` | 银白色，常与铁伴生 | 筛出率 **3.12%（1/32）** | 烧镍矿块；Thermal / IE 任一在场给对应锭，否则本模组镍锭 |
| 铂锭 `platinum_ingot` | 稀有贵金属，本模组自有 | 筛出率 **0.78%（1/128）**，全表最低 | 烧铂矿块；无模组依赖，始终是本模组铂锭 |
| 银锭 `silver_ingot` | 贵金属，可做锤 | 筛出率 **2.22%（1/45）** | 烧银矿块；Thermal / IE 任一在场给对应锭，否则本模组银锭 |
| 锡锭 `tin_ingot` | 最常见的有色金属 | 筛出率 **5.56%（1/18）** | 烧锡矿块；Mekanism / Thermal 任一在场给对应锭，否则本模组锡锭 |
| 铀锭 `uranium_ingot` | 放射性金属（仅材料，**没有铀锤**） | 筛出率 **1.11%（1/90）** | 烧铀矿块；Mekanism / IE 任一在场给对应锭，否则本模组铀锭 |
| 锌锭 `zinc_ingot` | 镀层金属 | 筛出率 **4.17%（1/24）** | 烧锌矿块；装 Create 时给 Create 锌锭，否则本模组锌锭 |

> ⚠️ 百科列表里的「**铱锭**」在本模组**不存在**。按条目排位（nickel 之后、platinum 之前）与字母序推断，它应是 **`osmium_ingot` 锇锭**（英文 Osmium）。建议改成「锇锭」再填上面的锇数据。

### 1.2 孢子 / 种子 / 瓷土

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 古代孢子 `ancient_spores` | 空岛获得菌丝、进而做巫水的起点 | 筛沙子 **0.78%（1/128）** | 右键**泥土**→菌丝；右键**牛**→蘑菇牛（可挤奶、剪蘑菇） |
| 绯红菌岩孢子 `crimson_nylium_spores` | 下界树（绯红）起点 | 筛**粉碎下界岩 5%** | 右键**下界岩**→绯红菌岩；之后用骨粉催菌菇、巨型菌菇 |
| 诡异菌岩孢子 `warped_nylium_spores` | 下界树（诡异）起点 | 筛**粉碎下界岩 5%** | 右键**下界岩**→诡异菌岩；同上 |
| 草种子 `grass_seeds` | 让空岛长草的种子 | 筛泥土/草方块 **6%** | 右键**泥土**→草方块 |
| 大地黏液草种子 `earth_slime_grass_seeds` | 匠魂黏液草（大地） | 筛泥土/草方块 **2%**（**仅 1.20.1 + Tinkers**） | 右键泥土或草方块→`tconstruct:earth_vanilla_slime_grass`（见 §7.8） |
| 碧空黏液草种子 `sky_slime_grass_seeds` | 匠魂黏液草（天空） | 筛泥土/草方块 **2%**（**仅 1.20.1 + Tinkers**） | 右键泥土或草方块→`tconstruct:sky_vanilla_slime_grass`（见 §7.8） |
| 末影黏液草种子 `ender_slime_grass_seeds` | 匠魂黏液草（末影） | 筛泥土/草方块 **1%**（**仅 1.20.1 + Tinkers**） | 右键泥土或草方块→`tconstruct:ender_vanilla_slime_grass`（见 §7.8） |
| 瓷土 `porcelain` | 坩埚、瓷桶、人偶的通用材料 | 1 黏土球 + 1 骨粉（无序合成） | 合成；烧制成坩埚/瓷桶 |

### 1.3 矿石碎片（破碎 / 粉碎 / 粉末）与石子

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 破碎矿 `*_broken` | 第一级碎片（矿沙砾的原料） | 筛**沙砾**；掉率见上表 | 4 个 → `*_ore_gravel`（2×2）；矿沙砾也可直接烧成锭 |
| 粉碎矿 `*_crushed` | 第二级碎片（矿沙子的原料） | 筛**沙子**；掉率同上 | 4 个 → `*_ore_sand`；或由矿沙砾锤出 **4–6** 个 |
| 粉末矿 `*_powdered` | 第三级碎片（矿粉尘的原料） | 筛**尘土**；掉率同上 | 4 个 → `*_ore_dust`；或由矿沙子锤出 **4–6** 个 |
| 石子 `*_pebble` | 空岛最基础的建筑原料 | 筛泥土：圆石/安山岩/闪长岩/花岗岩各 **100% × 4–6**；深板岩 **50% × 3–5** | 4 个 → 对应方块（深板岩石子 → 深板岩圆石） |

---

## 2. 功能方块

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 木桶 `barrel`（橡木） | 最基础的多用桶：接水、堆肥、桶内转化 | 1000 mB；木桶不能存 >433 K 的流体（熔岩不行）；满桶 + 另一种流体/上方流体可固化 | 6 木板 + 1 木板台阶 |
| 竹桶 `bamboo_barrel` | 竹木变体，行为与木桶完全一致 | 同木桶 | 6 竹板 + 1 竹台阶 |
| 石桶 `stone_barrel` | **唯一能存熔岩**的桶 | 1000 mB，可存任意温度流体 | 6 石头 + 1 平滑石头台阶 |
| 木坩埚 `wooden_crucible` | 早期坩埚，只能熔「水类」 | 8000 mB 储罐 / 2000 mB 固体容量；**无热源也以 0.5 mB/t 熔化**（8 圆石约 200 秒） | 6 原木 + 1 木台阶（各木材变体同理） |
| 坩埚 `crucible` | 瓷坩埚，需要下方热源 | 8000 mB / 2000 mB；必须热源；可熔岩；热源 **0.1–0.4 mB/t** | 7 瓷土 → 未烧制坩埚 → 烧制 |
| 未烧制坩埚 `unfired_crucible` | 坩埚的半成品 | 烧 200 tick、0.3 经验 → 坩埚 | 7 瓷土（`x x / x x / xxx`） |
| 筛子 `sieve` | 手动筛矿的核心方块 | 见 §0；装筛网后右键放 1 个原料，约 2.5 秒出结果 | 4 木板 + 1 木板台阶 + 2 木棍 |
| 侵染中的树叶 `infested_leaves` | 蚕啃过的树叶，会自己蔓延 | 每 tick +0.0005 → **100 秒成熟**；60% 后每 100 tick 向相邻树叶扩散；破坏按进度掉线（100% 时保底 1 根 + 25% 第二根），**不掉方块本身** | 用**蚕**右键任意树叶 |
| 末影蛋糕 `end_cake` | 用来去末地的蛋糕 | 放下只有 1 片；每颗末影之眼 +1 片（最多 6 颗 = 7 片）；吃一片回 2 饥饿 | 8 末影珍珠 + 1 蛋糕 |
| 电动自动筛子 `electric_sieve` | FE 驱动的自动筛 | **5 秒/次、20 FE/t ≈ 2000 FE/次**；48000 FE 缓冲；速度/时运/扩容各 1 槽（每槽可叠 8）；基础 12 格输出（扩容最多 9 页 / 108 格）+ 6 面自动输出 | 4 铁锭 + 3 红石 + 1 铁块 + 1 筛子 |
| 电动自动锤 `electric_hammer` | FE 驱动的自动锤 | 同上 | 4 铁锭 + 4 红石 + 1 铁锤 |
| 内能自动筛子 `fuel_sieve` | 烧燃料的自动筛，无插件 | **5 秒/次，每次 200 燃烧刻**（1 煤 1600 = 8 次） | 4 铁锭 + 3 圆石 + 1 熔炉 + 1 筛子 |
| 内能自动锤 `fuel_hammer` | 烧燃料的自动锤，无插件 | 同上 | 4 铁锭 + 3 圆石 + 1 熔炉 + 1 铁锤 |

---

## 3. 建筑方块

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 粉碎下界岩 `crushed_netherrack` | 下界战利品袋 | 筛出：石英 20%、萤石粉 12%、下界疣 10%、烈焰粉 6%、岩浆膏 4%、灵魂沙 2%、绯红/诡异菌岩孢子各 5%、钴/阿迪特破碎矿各 1%、下界合金碎片 **0.4%** | 锤**下界岩**得 1–2 个 |
| 粉碎末地石 `crushed_end_stone` | 末地战利品袋 | 筛出：紫颂果 6%、末影珍珠 5%（装龙之进化时另加 1/8 破碎龙矿） | 锤**末地石**得 1–2 个 |
| 尘土 `dust` | 第三级筛选原料 | 筛出：骨粉 20%、红石 12.5%、萤石粉 8.3%、火药 6.2% + 各粉末矿；可放进坩埚熔成 100 mB 熔岩 | 锤**沙子** 1:1 |
| 矿物沙砾 `*_ore_gravel` | 一级矿块，可建筑/可烧 | 烧 200 tick → 1 锭 | 4 个破碎矿（2×2） |
| 矿物沙子 `*_ore_sand` | 二级矿块 | 同上；锤可得 4–6 粉碎矿 | 4 个粉碎矿 |
| 矿物粉尘 `*_ore_dust` | 三级矿块，矿链终点 | 同上；锤粉尘块原样返回（不增产） | 4 个粉末矿 |

---

## 4. 食物

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 蚕 `silkworm` | 不能直接吃，用来「种」线 | 钩树叶 **2%**（骨钩 **3%**）；右键树叶 → 侵染树叶 | 见 §7.1 |
| 熟蚕 `cooked_silkworm` | 空岛前期的小零食 | **2 饥饿 / 0.6 饱和度系数**；熔炉 200 tick、烟熏 100 tick、营火 600 tick | 烧蚕 |

---

## 5. 工具

| 物品 | 简介 | 数值 | 获得方式 |
|---|---|---|---|
| 木钩 `wooden_crook` | 起步钩子 | 耐久 **64**；概率 **×1.0** | 4 木棍（`xx / x / x`） |
| 骨钩 `bone_crook` | 强化钩子 | 耐久 **128**；概率 **×1.5**（树叶树苗 25%→37.5%、蚕 2%→3%） | 4 骨头 |
| 木筛网 `wood_mesh` | 最便宜的筛网 | 耐久 **10**（= 筛 10 次）；**不改变掉落** | 9 木棍 |
| 线筛网 `silk_mesh` | 中期筛网 | 耐久 **64** | 9 线 |
| 铁筛网 `iron_mesh` | 长期筛网 | 耐久 **256** | 9 铁栏杆 |
| 瓷桶 `porcelain_bucket` | 万用 1000 mB 容器 | 可装**任意**流体（熔岩、巫水、糖水…）；对着桶/坩埚/储罐右键装倒；**不能像水桶那样直接放流体方块** | 3 瓷土 → 未烧制瓷桶 → 烧制 |
| 未烧制瓷桶 `unfired_porcelain_bucket` | 瓷桶半成品 | 烧 200 tick、0.3 经验 | 3 瓷土（`x x / x`） |
| 巫水桶 `witch_water_bucket` | 巫水桶装 | 1000 mB，可放置 | 舀巫水 |
| 糖水桶 `sugar_water_bucket` | 糖水桶装 | 1000 mB，可放置 | 舀糖水 |
| 浓缩蜂蜜汁桶 `concentrated_honey_bucket` | 蜂蜜链中间产物桶装 | 1000 mB，可放置 | 舀浓缩蜂蜜汁 |
| 脑白金 `brain_platinum` | 治「傻子」村民 | 对**傻子村民**右键 → 变成普通（失业）村民；其他目标不消耗 | 4 鸡蛋 + 4 糖 + 1 绿宝石 |
| 时运升级 I/II/III | 机器增产插件 | 触发时增加 **1/2/3 次整表独立判定**；首个必触发，之后每个 80% 概率递减；锤机只算 1 个 | I：4 金锭 + 1 绿宝石；II：I + 4 金锭 + 4 绿宝石；III：II + 4 钻石 + 4 绿宝石 |
| 速度升级 I/II/III | 机器提速插件 | 速度 **×1.5 / ×2.5 / ×4.0**；叠加每个只加前一个加成的 80%；每多一个插件耗电 +25% | I：4 糖 + 1 金锭；II：I + 4 金锭 + 4 糖；III：II + 4 钻石 + 4 糖 |
| 扩容升级 `capacity_upgrade` | 产物缓存插件（1.5.0，两版） | 每个 **+1 页（12 格）**，堆叠上限 **8** → 基础 1 页 + 8 = **9 页 / 108 格**；**不增加耗电**；只进电动筛 / 电动锤的槽 4 | 3×3：铁锭 / 纸 / 铁锭 + 纸 / **任意箱子** / 纸 + 铁锭 / 纸 / 铁锭（箱子 = 箱子 / 陷阱箱 / 末影箱） |

### 5.1 锤（同一条目，耐久不同）

| 锤 | 耐久 | 锤 | 耐久 |
|---|---|---|---|
| 木锤 `wooden_hammer` | 64 | 金锤 `golden_hammer` | 32 |
| 石锤 `stone_hammer` | 128 | 钻石锤 `diamond_hammer` | 1561 |
| 铁锤 `iron_hammer` | 256 | 下界合金锤 `netherite_hammer` | 2031 |
| 锡/铝/锌/铜锤 | 256 | 银锤 / 铅锤 | 320 / 384 |
| 镍锤 / 锇锤 | 448 / 512 | 铂锤 | **1920** |

- 合成：3 个材料 + 2 根木棍（`x / yx / y`）；下界合金锤由钻石锤 + 下界合金锭锻造。
- **所有锤的等级只影响耐久，不改变锤表产物**。
- 龙之进化锤（双足飞龙 / 神龙 / 混沌）不吃耐久，**每次 256 FE**（可装 DE 模块）。
- 1.20.1 另有 3 把 **Bamboo 宝石锤**（樱 / 枫 / 银杏，统一 **2048** 耐久，需 BambooMod 在场）和
  **匠魂碎矿锤**（`ex_nihilo_hammer`，匠魂 3 工具，材料染色 + 强化槽，掉落仍走本模组锤表）；
  主线没有这几把（BambooMod / Tinkers 3 都没有 1.21.1 构建）。

---

## 6. 人偶（桶里召唤生物）

机制：满桶（1000 mB）里右键人偶 → **立刻**消耗整桶流体并在桶顶生成生物；**和平难度**下改为掉落列表里的 1 个物品（多选一随机）。熔岩类规则**必须用石桶**。

| 人偶 | 简介 | 数值 | 合成 |
|---|---|---|---|
| 人偶 `doll` | 所有人偶的底子 | 仅合成材料 | 5 瓷土 + 1 钻石**或** 1 绿宝石 |
| 愤怒人偶 `angry_doll` | 熔岩里换烈焰人 | 熔岩 → 烈焰人；和平掉烈焰棒 | 人偶 + 4 烈焰粉 + 下界疣 + 红石 + 2 萤石粉 |
| 狂风人偶 `blitz_doll` | 熔岩里换狂风（Thermal） | 熔岩 → `thermal:blitz`；和平掉狂风棒 | 人偶 + 4 硝石粉 + 下界疣 + 红石 + 2 萤石粉（需 Thermal） |
| 暴雪人偶 `blizz_doll` | 水里换暴雪（Thermal） | 水 → `thermal:blizz`；和平掉暴雪棒 | 人偶 + 4 雪球 + 下界疣 + 红石 + 2 萤石粉（需 Thermal） |
| 诡异人偶 `creepy_doll` | 巫水里换末影人 | 巫水 → 末影人；和平掉末影珍珠 | 人偶 + 4 墨囊 + 下界疣 + 红石 + 2 青金石 |
| 漂浮人偶 `shulker_doll` | 巫水里换潜影贝 | 巫水 → 潜影贝；和平掉潜影壳 | 人偶 + 4 紫色染料 + 末地石 + 末影珍珠 + 2 萤石粉 |
| 守卫者人偶 `guardian_doll` | 水里换守卫者 | 水 → 守卫者；和平掉**海晶碎片或海晶砂粒**（随机 1 个） | 人偶 + 4 海晶砂粒 + 红石 + 生鲑鱼 + 2 萤石粉 |
| 村民人偶 `villager_doll` | 巫水里换僵尸村民 | 巫水 → 僵尸村民；和平掉腐肉 | 人偶 + 8 绿宝石 |

---

## 7. 重点机制（数值细节）

### 7.1 蚕与线

1. **获得**：拿**钩**打带钩表的树叶（橡木/云杉/白桦/丛林/金合欢/深色橡木/红树/樱花/杜鹃/盛开的杜鹃），每次判定 **2%**；骨钩 **3%**。用普通工具/空手**不会**出蚕。
2. **种树**：拿蚕右键**任意树叶** → 变成「侵染中的树叶」。
3. **成熟**：每 tick +0.0005，**2000 tick（100 秒）**到 100%。
4. **蔓延**：进度 **>60%（60 秒）**后，每 100 tick 在周围 3×3×3 里随机挑一片普通树叶侵染。
5. **收获**：破坏时按进度掉线——进度 p 时先判 **p 概率 1 根线**，再判 **0.25p 概率再多 1 根**；100% 时 = 1 根保底 + 25% 第二根（平均 1.25 根）。**方块本身不掉落**（创造栏里有 0% 和 100% 两个版本）。
6. 蚕可烧成**熟蚕**（2 饥饿）。

### 7.2 蜂蜜链（浓缩蜂蜜汁 → 蜂蜜）

| 步骤 | 操作 | 数值 |
|---|---|---|
| ① 糖水 | 满水桶（1000 mB）+ **糖** | **立即**变成等量糖水 |
| ② 浓缩蜂蜜汁 | 满糖水（1000 mB）+ **黄色染料** | **600 tick（30 秒）**后整桶变成浓缩蜂蜜汁，浓度不变（1000 mB） |
| ③ 蜂蜜瓶 | 桶里至少 250 mB 浓缩蜂蜜汁 + **玻璃瓶** | **消耗 250 mB** → 1 瓶蜂蜜；一满桶 = **4 瓶** |
| ④ 蜂蜜块 | 4 瓶蜂蜜 | 原版合成（1 个蜂蜜块） |
| ⑤ 蜂箱 | 6 木板 + 3 瓶蜂蜜 | 本模组追加的替代配方（不用找蜂巢） |
| ⑥ 引蜂 | 桶里有任意量浓缩蜂蜜汁 | 每 **100 tick（5 秒）** 判 **1/6**，在 6 格内生成 1 只蜜蜂；6 格内已有 5 只则停 |

> 所以「概率」只出现在两处：② 是**固定 30 秒**不是概率；⑥ 是 **5 秒 1/6**。玻璃瓶取蜜是**定量 250 mB**。

### 7.3 巫水链

- **做法 A**：满水桶 + 周围 3×3×3 内有**菌丝**。每 tick 进度 `+1 + 菌丝数/2`，满 **600** 变巫水（1 块菌丝 = 30 秒；2 块 = 15 秒；越多越快）。
- **做法 B**：满水桶 + **蘑菇煲**（碗退还），种下进度后按 1/tick 发酵，**30 秒**。
- 用途：巫水 + 沙子 → 灵魂沙；巫水 + 橡树树苗 → 深色橡树树苗；三种巫水人偶。

### 7.4 桶内转化全表（18 条，装 Thermal 时）

| 基底 | 加入 | 结果 | 备注 |
|---|---|---|---|
| 水 | 尘土 | 黏土块 | 立即 |
| 水 | 熔岩 | 圆石 | 立即（木桶可用） |
| 水 | 牛奶桶 | 史莱姆 | 召唤，和平掉粘液球 |
| 水 | 守卫者人偶 | 守卫者 | 召唤 |
| 水 | 暴雪人偶 | 暴雪 | 召唤（需 Thermal） |
| 水 | 糖 | 糖水 | 立即，换流体 |
| 巫水 | 沙子 | 灵魂沙 | 立即 |
| 巫水 | 橡树树苗 | 深色橡树树苗 | 立即 |
| 巫水 | 诡异人偶 | 末影人 | 召唤 |
| 巫水 | 漂浮人偶 | 潜影贝 | 召唤 |
| 巫水 | 村民人偶 | 僵尸村民 | 召唤 |
| 熔岩 | 红石 | 下界岩 | 需**石桶** |
| 熔岩 | 萤石粉 | 末地石 | 需**石桶** |
| 熔岩 | 水 | 黑曜石 | 需**石桶** |
| 熔岩 | 愤怒人偶 | 烈焰人 | 召唤，需**石桶** |
| 熔岩 | 狂风人偶 | 狂风 | 召唤，需**石桶** + Thermal |
| 糖水 | 黄色染料 | 浓缩蜂蜜汁 | 30 秒 |
| 浓缩蜂蜜汁 | 玻璃瓶 | 蜂蜜瓶 | 消耗 250 mB |

其它：**满桶 + 上方仍有另一种流体**会直接固化（满水 + 上方熔岩 = 圆石；满熔岩 + 上方水 = 黑曜石，需石桶）；雨天露天可接水（降雨 0.5 时约 **100 秒**满桶）。

### 7.5 堆肥（67 条）

- 满 **1000 点** → 30 秒 → 1 个泥土；桶里有流体或固体产物时不能堆肥。
- 参考值：树苗/树叶 **125**（8 个 = 1 泥土）、花/菌菇 **100**、种子/作物 **80**、面包 **125**、苹果 **100**、金苹果 **200**（最高）、西瓜/南瓜 **150**、腐肉 **85**、线 **40**。
- 管道可插入堆肥（物品 capability）。

### 7.6 坩埚

- 储罐 **8000 mB**，固体容量 **2000 mB**（8 个圆石）。
- **木坩埚**：只吃水类配方，不加热也以 **0.5 mB/t** 熔化（8 圆石约 200 秒）。
- **瓷坩埚**：必须下方有热源；熔岩类配方只能用它。
- 热源速度（mB/t）：灵魂火 **0.4**、灵魂营火 **0.35**、火 **0.3**、营火 **0.25**、熔岩 **0.2**、高炉/熔炉/烟熏炉（点燃）**0.15**、火把 **0.1**。
- 39 条熔点：圆石/石头/花岗岩/闪长岩/安山岩/凝灰岩/深板岩/深板岩圆石/沙砾/方解石 **250 mB 熔岩**；沙子/红沙 **100**；下界岩 **1000**；冰 **1000 水**；雪块 **500**；树叶 **100 水**；树苗 **250 水**。

### 7.7 机器与插件算法

- **电动**：基础 **100 tick/次（5 秒）**、**20 FE/t**；单次约 **2000 FE**；缓冲 **48000 FE**（≈24 次）。
- **内能**：**200 燃烧刻/次**，1 煤（1600）= 8 次；没有插件槽。
- **速度插件**：×1.5 / ×2.5 / ×4.0，第一个全额、之后每个只加前一个加成的 80%（8 个速度 III ≈ ×14，7 tick/次）；插件越多耗电越高（每多一个 +25%）。
- **时运插件**：每个触发时给 **tier 次**整表独立判定；首个 100%，之后 80%、64%…；锤机只吃 1 个时运。
- 机器每次操作消耗 1 个原料、工具 **-1 耐久**（能量锤扣 256 FE）；产物进基础 12 格输出，可按 6 个面自动输出（每 20 tick 推一次）。
- **扩容升级（1.5.0）**：电动筛 / 电动锤槽 4，每件 +1 页（12 格），最多 8 件 → 9 页；内能机器没有插件槽，固定 1 页；**不加 FE/t**。漏斗 / 管道 / 自动输出看到的是全部活动页拼成的平铺缓存，不需要知道页；拆扩容时高页物品先并回活动页，放不下掉在机器上方。

### 7.8 匠魂黏液树：树苗从哪来（1.20.1 + Tinkers）

**1.4.2 起本模组能自己产黏液泥土和树苗，但整条链只在 1.20.1 + Tinkers 线。**

- **种子 → 黏液草**：大地 / 碧空 / 末影黏液草种子（泥土、草方块各 **2% / 2% / 1%**），右键泥土或草方块 → `tconstruct:{earth,sky,ender}_vanilla_slime_grass`。草方块会自己蔓延到泥土。
- **黏液草 → 黏液泥土**：挖掉黏液草时，在匠魂原本的 `minecraft:dirt` 之外**额外**掉一份 `tconstruct:<type>_slime_dirt`（`LootTableLoadEvent` 追加，匠魂的掉落表文件不改）。
- **黏液泥土 → 树苗**：`sieve/{earth,sky,ender}_slime_dirt.json`，**10%** 出同种匠魂树苗、**25%** 出同种黏液球（大地那档是原版 `minecraft:slime_ball`）。**这是空岛唯一不靠结构生成的自有树苗来源**。
- **骨粉不会出树苗**：匠魂 `SlimeGrassBlock` 的骨粉只放黏液蕨 / 黏液高草（`performBonemeal` 传 `sapling=false`；只有下界黏液菌岩 `SlimeNyliumBlock` 才是 `true`）。
- **黏液岛（原版途径，仍更快）**：破坏黏液树叶 **5%** 掉对应树苗；时运 I/II/III 为 **6.25% / 8.33% / 10%**。黏液树叶只长在黏液岛上——匠魂的浮空岛结构（主世界天空，`spacing 35`、`frequency 0.5`，海洋/沙滩/河流/山地/恶地/丘陵/针叶林/森林生物群系）。
- 拿到树苗后可以种在我们的黏液草上：`#tconstruct:slimy_grass` 包含 `*_vanilla_slime_grass`，而树苗要求「黏液泥土或黏液草方块」。
- 黏液树苗共 5 种：`earth_slime_sapling` 绿心树苗、`sky_slime_sapling` 天根树苗、`ender_slime_sapling` 末影皮树胎生苗、`blood_slime_sapling` 血菌、`ichor_slime_sapling` 灵浆树苗（后两种长在黏液菌岩上）。

---

## 8. 版本差异 & 待确认

1. **三种黏液草种子 / 黏液泥土筛表**只在 **1.20.1 + Tinkers** 存在；主线（1.21.1）没有这些物品，百科若分版本请标注。
2. **铱锭**：本模组无此物品，应为**锇锭**（见 §1.1）。
3. 1.20.1 另有**匠魂碎矿锤**（匠魂 3 工具，走我们自己的锤表，带连锁强化 I–III）和 3 把 Bamboo 宝石锤（2048 耐久）；主线没有。
4. **扩容升级**（1.5.0）两版都有；1.20.1 线才有 1.4.1 的连锁强化与 1.4.2 的黏液链。
5. 1.21.1 与 1.20.1 的筛 / 锤 / 桶 / 坩埚 / 机器**数值**一致；差异只在「额外物品」和「可选模组带来的条目」。

---

## 9. 满联动筛表（以筛料为准）

口径：

- **满联动** = 全部可选模组装上（热力 / ER / AE2 / Mekanism / 末影接口 / 龙之进化；1.20.1 再加匠魂 / 工匠巧思 / BambooMod）。
- 每一次筛 1 个原料，**逐条独立 roll**：概率不归一，一次可以同时出好几样（泥土的四色石子各 100%，等于每次必出 16–24 个石子）。
- `数量` = 单次判定成功的个数；`4–6` 表示 4、5、6 等概率。
- 最后一列「模组 · 版本」：`—` 是基线；`两版` 两条线都能装；`仅主线` 只有 1.21.1 生效；`仅 1.20.1` 是匠魂 / 工匠巧思 / BambooMod。
- 并集装法下共 **187** 条：主线实际可达 **155**（160 条里 5 条依赖 1.21.1 不存在的模组），1.20.1 全开 **184**。

### 泥土（`minecraft:dirt`）— 34 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 圆石石子 `exnihilosatellitium:cobblestone_pebble` | 100% | 4–6 | — · 两版 |
| 安山岩石子 `exnihilosatellitium:andesite_pebble` | 100% | 4–6 | — · 两版 |
| 花岗岩石子 `exnihilosatellitium:granite_pebble` | 100% | 4–6 | — · 两版 |
| 闪长岩石子 `exnihilosatellitium:diorite_pebble` | 100% | 4–6 | — · 两版 |
| 深板岩石子 `exnihilosatellitium:deepslate_pebble` | 50% | 3–5 | — · 两版 |
| 小麦种子 `minecraft:wheat_seeds` | 15% | 1 | — · 两版 |
| 橡树树苗 `minecraft:oak_sapling` | 8% | 1 | — · 两版 |
| 甜菜种子 `minecraft:beetroot_seeds` | 6% | 1 | — · 两版 |
| 胡萝卜 `minecraft:carrot` | 6% | 1 | — · 两版 |
| 草种子 `exnihilosatellitium:grass_seeds` | 6% | 1 | — · 两版 |
| 马铃薯 `minecraft:potato` | 6% | 1 | — · 两版 |
| 南瓜种子 `minecraft:pumpkin_seeds` | 5% | 1 | — · 两版 |
| 水稻 `bamboomod:riceseed` | 5% | 1 | BambooMod · 仅 1.20.1 |
| 甜浆果 `minecraft:sweet_berries` | 5% | 1 | — · 两版 |
| 竹子 `minecraft:bamboo` | 5% | 1 | — · 两版 |
| 西瓜种子 `minecraft:melon_seeds` | 5% | 1 | — · 两版 |
| 云杉树苗 `minecraft:spruce_sapling` | 4% | 1 | — · 两版 |
| 番茄 `bamboomod:tomato` | 4% | 1 | BambooMod · 仅 1.20.1 |
| 白桦树苗 `minecraft:birch_sapling` | 4% | 1 | — · 两版 |
| 竹笋 `bamboomod:bamboo_shoot` | 4% | 1 | BambooMod · 仅 1.20.1 |
| 豆 `bamboomod:bean` | 4% | 1 | BambooMod · 仅 1.20.1 |
| 丛林树苗 `minecraft:jungle_sapling` | 3% | 1 | — · 两版 |
| 樱花树苗 `minecraft:cherry_sapling` | 3% | 1 | — · 两版 |
| 红树胎生苗 `minecraft:mangrove_propagule` | 3% | 1 | — · 两版 |
| 金合欢树苗 `minecraft:acacia_sapling` | 3% | 1 | — · 两版 |
| 大地黏液草种子 `exnihilosatellitium:earth_slime_grass_seeds` | 2% | 1 | 匠魂 · 仅 1.20.1 |
| 杜鹃花丛 `minecraft:azalea` | 2% | 1 | — · 两版 |
| 枫树苗 `bamboomod:maple_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 桧树苗 `bamboomod:hinoki_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 樱树苗 `bamboomod:sakura_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 碧空黏液草种子 `exnihilosatellitium:sky_slime_grass_seeds` | 2% | 1 | 匠魂 · 仅 1.20.1 |
| 银杏树苗 `bamboomod:ginkgo_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 末影黏液草种子 `exnihilosatellitium:ender_slime_grass_seeds` | 1% | 1 | 匠魂 · 仅 1.20.1 |
| 盛开的杜鹃花丛 `minecraft:flowering_azalea` | 1% | 1 | — · 两版 |

### 草方块（`minecraft:grass_block`）— 34 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 圆石石子 `exnihilosatellitium:cobblestone_pebble` | 100% | 4–6 | — · 两版 |
| 安山岩石子 `exnihilosatellitium:andesite_pebble` | 100% | 4–6 | — · 两版 |
| 花岗岩石子 `exnihilosatellitium:granite_pebble` | 100% | 4–6 | — · 两版 |
| 闪长岩石子 `exnihilosatellitium:diorite_pebble` | 100% | 4–6 | — · 两版 |
| 小麦种子 `minecraft:wheat_seeds` | 15% | 1 | — · 两版 |
| 橡树树苗 `minecraft:oak_sapling` | 8% | 1 | — · 两版 |
| 甜菜种子 `minecraft:beetroot_seeds` | 6% | 1 | — · 两版 |
| 胡萝卜 `minecraft:carrot` | 6% | 1 | — · 两版 |
| 草种子 `exnihilosatellitium:grass_seeds` | 6% | 1 | — · 两版 |
| 马铃薯 `minecraft:potato` | 6% | 1 | — · 两版 |
| 南瓜种子 `minecraft:pumpkin_seeds` | 5% | 1 | — · 两版 |
| 水稻 `bamboomod:riceseed` | 5% | 1 | BambooMod · 仅 1.20.1 |
| 甜浆果 `minecraft:sweet_berries` | 5% | 1 | — · 两版 |
| 竹子 `minecraft:bamboo` | 5% | 1 | — · 两版 |
| 西瓜种子 `minecraft:melon_seeds` | 5% | 1 | — · 两版 |
| 云杉树苗 `minecraft:spruce_sapling` | 4% | 1 | — · 两版 |
| 番茄 `bamboomod:tomato` | 4% | 1 | BambooMod · 仅 1.20.1 |
| 白桦树苗 `minecraft:birch_sapling` | 4% | 1 | — · 两版 |
| 竹笋 `bamboomod:bamboo_shoot` | 4% | 1 | BambooMod · 仅 1.20.1 |
| 豆 `bamboomod:bean` | 4% | 1 | BambooMod · 仅 1.20.1 |
| 丛林树苗 `minecraft:jungle_sapling` | 3% | 1 | — · 两版 |
| 樱花树苗 `minecraft:cherry_sapling` | 3% | 1 | — · 两版 |
| 红树胎生苗 `minecraft:mangrove_propagule` | 3% | 1 | — · 两版 |
| 金合欢树苗 `minecraft:acacia_sapling` | 3% | 1 | — · 两版 |
| 大地黏液草种子 `exnihilosatellitium:earth_slime_grass_seeds` | 2% | 1 | 匠魂 · 仅 1.20.1 |
| 杜鹃花丛 `minecraft:azalea` | 2% | 1 | — · 两版 |
| 枫树苗 `bamboomod:maple_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 桧树苗 `bamboomod:hinoki_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 樱树苗 `bamboomod:sakura_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 碧空黏液草种子 `exnihilosatellitium:sky_slime_grass_seeds` | 2% | 1 | 匠魂 · 仅 1.20.1 |
| 银杏树苗 `bamboomod:ginkgo_sapling` | 2% | 1 | BambooMod · 仅 1.20.1 |
| 末影黏液草种子 `exnihilosatellitium:ender_slime_grass_seeds` | 1% | 1 | 匠魂 · 仅 1.20.1 |
| 盛开的杜鹃花丛 `minecraft:flowering_azalea` | 1% | 1 | — · 两版 |
| 破碎叶绿素矿 `exnihilosatellitium:chlorophyll_broken` | 1% | 1 | 工匠巧思 · 仅 1.20.1 |

### 沙砾（`minecraft:gravel`）— 32 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 燧石 `minecraft:flint` | 25% | 1 | — · 两版 |
| 破碎铁矿 `exnihilosatellitium:iron_broken` | 20% | 1 | — · 两版 |
| 煤炭 `minecraft:coal` | 12.5% | 1 | — · 两版 |
| 破碎氟石矿 `exnihilosatellitium:fluorite_broken` | 12.5% | 1 | Mekanism · 两版 |
| 破碎铝矿 `exnihilosatellitium:aluminum_broken` | 12.5% | 1 | — · 两版 |
| 破碎黄铀矿 `exnihilosatellitium:yellorium_broken` | 12.5% | 1 | ER · 两版 |
| 破碎锇矿 `exnihilosatellitium:osmium_broken` | 10% | 1 | — · 两版 |
| 陨石粉 `ae2:sky_dust` | 6.25% | 1 | AE2 · 两版 |
| 破碎磷灰石矿 `exnihilosatellitium:apatite_broken` | 5.556% | 1 | 热力 · 两版 |
| 破碎铜矿 `exnihilosatellitium:copper_broken` | 5.556% | 1 | — · 两版 |
| 破碎锡矿 `exnihilosatellitium:tin_broken` | 5.556% | 1 | — · 两版 |
| 青金石 `minecraft:lapis_lazuli` | 5% | 1 | — · 两版 |
| 破碎硝石矿 `exnihilosatellitium:niter_broken` | 4.167% | 1 | 热力 · 两版 |
| 破碎硫磺矿 `exnihilosatellitium:sulfur_broken` | 4.167% | 1 | 热力 · 两版 |
| 破碎锌矿 `exnihilosatellitium:zinc_broken` | 4.167% | 1 | — · 两版 |
| 破碎朱砂矿 `exnihilosatellitium:cinnabar_broken` | 3.125% | 1 | 热力 · 两版 |
| 破碎金矿 `exnihilosatellitium:gold_broken` | 3.125% | 1 | — · 两版 |
| 破碎铅矿 `exnihilosatellitium:lead_broken` | 3.125% | 1 | — · 两版 |
| 破碎镍矿 `exnihilosatellitium:nickel_broken` | 3.125% | 1 | — · 两版 |
| 破碎银矿 `exnihilosatellitium:silver_broken` | 2.222% | 1 | — · 两版 |
| 枫宝石 `bamboomod:maple_gem` | 1.562% | 1 | BambooMod · 仅 1.20.1 |
| 樱宝石 `bamboomod:sakura_gem` | 1.562% | 1 | BambooMod · 仅 1.20.1 |
| 破碎红宝石矿 `exnihilosatellitium:ruby_broken` | 1.562% | 1 | 热力 · 两版 |
| 破碎蓝宝石矿 `exnihilosatellitium:sapphire_broken` | 1.562% | 1 | 热力 · 两版 |
| 破碎蓝锥矿矿 `exnihilosatellitium:benitoite_broken` | 1.562% | 1 | ER · 两版 |
| 破碎钴矿 `exnihilosatellitium:cobalt_broken` | 1.562% | 1 | — · 仅主线 |
| 破碎铅矾矿 `exnihilosatellitium:anglesite_broken` | 1.562% | 1 | ER · 两版 |
| 银杏宝石 `bamboomod:ginkgo_gem` | 1.562% | 1 | BambooMod · 仅 1.20.1 |
| 破碎铀矿 `exnihilosatellitium:uranium_broken` | 1.111% | 1 | — · 两版 |
| 破碎铂矿 `exnihilosatellitium:platinum_broken` | 0.781% | 1 | — · 两版 |
| 钻石 `minecraft:diamond` | 0.781% | 1 | — · 两版 |
| 绿宝石 `minecraft:emerald` | 0.667% | 1 | — · 两版 |

### 沙子（`minecraft:sand`）— 39 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 粉碎铁矿 `exnihilosatellitium:iron_crushed` | 20% | 1 | — · 两版 |
| 赛特斯石英水晶 `ae2:certus_quartz_crystal` | 16.667% | 1 | AE2 · 两版 |
| 粉碎氟石矿 `exnihilosatellitium:fluorite_crushed` | 12.5% | 1 | Mekanism · 两版 |
| 粉碎铝矿 `exnihilosatellitium:aluminum_crushed` | 12.5% | 1 | — · 两版 |
| 粉碎黄铀矿 `exnihilosatellitium:yellorium_crushed` | 12.5% | 1 | ER · 两版 |
| 粉碎锇矿 `exnihilosatellitium:osmium_crushed` | 10% | 1 | — · 两版 |
| 赛特斯石英粉 `ae2:certus_quartz_dust` | 8.333% | 1 | AE2 · 两版 |
| 陨石粉 `ae2:sky_dust` | 6.25% | 1 | AE2 · 两版 |
| 粉碎磷灰石矿 `exnihilosatellitium:apatite_crushed` | 5.556% | 1 | 热力 · 两版 |
| 粉碎铜矿 `exnihilosatellitium:copper_crushed` | 5.556% | 1 | — · 两版 |
| 粉碎锡矿 `exnihilosatellitium:tin_crushed` | 5.556% | 1 | — · 两版 |
| 甘蔗 `minecraft:sugar_cane` | 4.167% | 1 | — · 两版 |
| 粉碎硝石矿 `exnihilosatellitium:niter_crushed` | 4.167% | 1 | 热力 · 两版 |
| 粉碎硫磺矿 `exnihilosatellitium:sulfur_crushed` | 4.167% | 1 | 热力 · 两版 |
| 粉碎锌矿 `exnihilosatellitium:zinc_crushed` | 4.167% | 1 | — · 两版 |
| 海带 `minecraft:kelp` | 4% | 1 | — · 两版 |
| 仙人掌 `minecraft:cactus` | 3.125% | 1 | — · 两版 |
| 粉碎朱砂矿 `exnihilosatellitium:cinnabar_crushed` | 3.125% | 1 | 热力 · 两版 |
| 粉碎金矿 `exnihilosatellitium:gold_crushed` | 3.125% | 1 | — · 两版 |
| 粉碎铅矿 `exnihilosatellitium:lead_crushed` | 3.125% | 1 | — · 两版 |
| 粉碎镍矿 `exnihilosatellitium:nickel_crushed` | 3.125% | 1 | — · 两版 |
| 可可豆 `minecraft:cocoa_beans` | 3% | 1 | — · 两版 |
| 烈焰粉 `minecraft:blaze_powder` | 3% | 1 | — · 两版 |
| 粉碎银矿 `exnihilosatellitium:silver_crushed` | 2.222% | 1 | — · 两版 |
| 海晶砂粒 `minecraft:prismarine_crystals` | 2% | 1 | — · 两版 |
| 海晶碎片 `minecraft:prismarine_shard` | 2% | 1 | — · 两版 |
| 管珊瑚 `minecraft:tube_coral` | 2% | 1 | — · 两版 |
| 粉碎红宝石矿 `exnihilosatellitium:ruby_crushed` | 1.562% | 1 | 热力 · 两版 |
| 粉碎蓝宝石矿 `exnihilosatellitium:sapphire_crushed` | 1.562% | 1 | 热力 · 两版 |
| 粉碎蓝锥矿矿 `exnihilosatellitium:benitoite_crushed` | 1.562% | 1 | ER · 两版 |
| 粉碎钴矿 `exnihilosatellitium:cobalt_crushed` | 1.562% | 1 | — · 仅主线 |
| 粉碎铅矾矿 `exnihilosatellitium:anglesite_crushed` | 1.562% | 1 | ER · 两版 |
| 粉碎铀矿 `exnihilosatellitium:uranium_crushed` | 1.111% | 1 | — · 两版 |
| 气泡珊瑚 `minecraft:bubble_coral` | 1% | 1 | — · 两版 |
| 火珊瑚 `minecraft:fire_coral` | 1% | 1 | — · 两版 |
| 脑纹珊瑚 `minecraft:brain_coral` | 1% | 1 | — · 两版 |
| 鹿角珊瑚 `minecraft:horn_coral` | 1% | 1 | — · 两版 |
| 粉碎铂矿 `exnihilosatellitium:platinum_crushed` | 0.781% | 1 | — · 两版 |
| 古代孢子 `exnihilosatellitium:ancient_spores` | 0.78% | 1 | — · 两版 |

### 尘土（`exnihilosatellitium:dust`）— 28 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 粉末铁矿 `exnihilosatellitium:iron_powdered` | 20% | 1 | — · 两版 |
| 骨粉 `minecraft:bone_meal` | 20% | 1 | — · 两版 |
| 粉末氟石矿 `exnihilosatellitium:fluorite_powdered` | 12.5% | 1 | Mekanism · 两版 |
| 粉末铝矿 `exnihilosatellitium:aluminum_powdered` | 12.5% | 1 | — · 两版 |
| 粉末黄铀矿 `exnihilosatellitium:yellorium_powdered` | 12.5% | 1 | ER · 两版 |
| 红石 `minecraft:redstone` | 12.5% | 1 | — · 两版 |
| 粉末锇矿 `exnihilosatellitium:osmium_powdered` | 10% | 1 | — · 两版 |
| 荧石粉 `minecraft:glowstone_dust` | 8.333% | 1 | — · 两版 |
| 火药 `minecraft:gunpowder` | 6.25% | 1 | — · 两版 |
| 粉末磷灰石矿 `exnihilosatellitium:apatite_powdered` | 5.556% | 1 | 热力 · 两版 |
| 粉末铜矿 `exnihilosatellitium:copper_powdered` | 5.556% | 1 | — · 两版 |
| 粉末锡矿 `exnihilosatellitium:tin_powdered` | 5.556% | 1 | — · 两版 |
| 粉末硝石矿 `exnihilosatellitium:niter_powdered` | 4.167% | 1 | 热力 · 两版 |
| 粉末硫磺矿 `exnihilosatellitium:sulfur_powdered` | 4.167% | 1 | 热力 · 两版 |
| 粉末锌矿 `exnihilosatellitium:zinc_powdered` | 4.167% | 1 | — · 两版 |
| 粉末朱砂矿 `exnihilosatellitium:cinnabar_powdered` | 3.125% | 1 | 热力 · 两版 |
| 粉末金矿 `exnihilosatellitium:gold_powdered` | 3.125% | 1 | — · 两版 |
| 粉末铅矿 `exnihilosatellitium:lead_powdered` | 3.125% | 1 | — · 两版 |
| 粉末镍矿 `exnihilosatellitium:nickel_powdered` | 3.125% | 1 | — · 两版 |
| 粉末银矿 `exnihilosatellitium:silver_powdered` | 2.222% | 1 | — · 两版 |
| 无限粉 `enderio:grains_of_infinity` | 2% | 1 | 末影接口 · 两版 |
| 粉末红宝石矿 `exnihilosatellitium:ruby_powdered` | 1.562% | 1 | 热力 · 两版 |
| 粉末蓝宝石矿 `exnihilosatellitium:sapphire_powdered` | 1.562% | 1 | 热力 · 两版 |
| 粉末蓝锥矿矿 `exnihilosatellitium:benitoite_powdered` | 1.562% | 1 | ER · 两版 |
| 粉末钴矿 `exnihilosatellitium:cobalt_powdered` | 1.562% | 1 | — · 仅主线 |
| 粉末铅矾矿 `exnihilosatellitium:anglesite_powdered` | 1.562% | 1 | ER · 两版 |
| 粉末铀矿 `exnihilosatellitium:uranium_powdered` | 1.111% | 1 | — · 两版 |
| 粉末铂矿 `exnihilosatellitium:platinum_powdered` | 0.781% | 1 | — · 两版 |

### 粉碎下界岩（`exnihilosatellitium:crushed_netherrack`）— 11 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 下界石英 `minecraft:quartz` | 20% | 1 | — · 两版 |
| 荧石粉 `minecraft:glowstone_dust` | 12% | 1 | — · 两版 |
| 下界疣 `minecraft:nether_wart` | 10% | 1 | — · 两版 |
| 烈焰粉 `minecraft:blaze_powder` | 6% | 1 | — · 两版 |
| 绯红菌岩孢子 `exnihilosatellitium:crimson_nylium_spores` | 5% | 1 | — · 两版 |
| 诡异菌岩孢子 `exnihilosatellitium:warped_nylium_spores` | 5% | 1 | — · 两版 |
| 岩浆膏 `minecraft:magma_cream` | 4% | 1 | — · 两版 |
| 灵魂沙 `minecraft:soul_sand` | 2% | 1 | — · 两版 |
| 破碎钴矿 `exnihilosatellitium:cobalt_broken` | 1% | 1 | — · 两版 |
| 破碎阿迪特矿 `exnihilosatellitium:ardite_broken` | 1% | 1 | — · 两版 |
| 下界合金碎片 `minecraft:netherite_scrap` | 0.4% | 1 | — · 两版 |

### 粉碎末地石（`exnihilosatellitium:crushed_end_stone`）— 3 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 破碎龙矿 `exnihilosatellitium:draconium_broken` | 12.5% | 1 | 龙之进化 · 两版 |
| 紫颂果 `minecraft:chorus_fruit` | 6% | 1 | — · 两版 |
| 末影珍珠 `minecraft:ender_pearl` | 5% | 1 | — · 两版 |

### 大地黏液泥土（`tconstruct:earth_slime_dirt`）— 2 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 黏液球 `minecraft:slime_ball` | 25% | 1 | 匠魂 · 仅 1.20.1 |
| 绿心树苗 `tconstruct:earth_slime_sapling` | 10% | 1 | 匠魂 · 仅 1.20.1 |

### 碧空黏液泥土（`tconstruct:sky_slime_dirt`）— 2 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 碧空黏液球 `tconstruct:sky_slime_ball` | 25% | 1 | 匠魂 · 仅 1.20.1 |
| 天根树苗 `tconstruct:sky_slime_sapling` | 10% | 1 | 匠魂 · 仅 1.20.1 |

### 末影黏液泥土（`tconstruct:ender_slime_dirt`）— 2 条

| 产物 | 概率 | 数量 | 模组 · 版本 |
|---|---:|---:|---|
| 末影黏液球 `tconstruct:ender_slime_ball` | 25% | 1 | 匠魂 · 仅 1.20.1 |
| 末影皮树胎生苗 `tconstruct:ender_slime_sapling` | 10% | 1 | 匠魂 · 仅 1.20.1 |

> 备注：`benitoite` 的现有语言文件是「蓝锥矿矿」（重复一个「矿」），表格照抄物品名；要改成「蓝锥矿」只需改 lang。
> 1.20.1 的钴**不在**沙砾 / 沙子 / 尘土里（只有粉碎下界岩 1%），所以主线的钴反而更容易凑。

---

### 附：当前表尺（无科技模组）

筛子 7 输入 / **119** 掉落（1.20.1 基线 **116**）；锤表 **55**；桶 **16**（装 Thermal **18**）；
堆肥 **67**；坩埚 **39** 熔点 / **9** 热源。

装模组的**增量**（不影响上表基础掉率）：

| 模组 | 筛表 | 锤表 |
|---|---:|---:|
| 热力（Thermal） | +18 | +18 |
| Extreme Reactors | +9 | +9 |
| AE2 | +4 | — |
| Mekanism | +3 | +3 |
| 末影接口（Ender IO） | +1 | — |
| 龙之进化（DE） | +1 | +3 |
| 匠魂（Tinkers） | +4（1.20.1 +12） | — |
| 工匠巧思（TiT） | +1 | +3 |
| BambooMod（仅 1.20.1） | +19 | — |

以上由 `python3 scripts/table-sizes.py` 统计（加 `--root ../Ex-Nihilo-Satellitium-1.20.1` 看分支）；
「全开」筛表 160 / 184、锤表 91 / 91。
