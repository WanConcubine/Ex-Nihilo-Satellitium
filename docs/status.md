# 当前状态（交接卡）

> 最后更新：2026-11-04
> 先看这 20 行；批次叙事在 [history.md](history.md)，设计与取舍在 [DESIGN.md](DESIGN.md)。

## 一句话

**1.20.1 / Forge 47.4.28 / Java 17** 线；M1–M10、datagen 三批、P1/P1.5/P2/P3、数据包 v2 都已完成。
**1.0.0 已发**（tag `v1.0.0-1.20.1`）；0.2.0 准备线并入 1.0.0，商店上传待所有者。
**最新一批（2026-11-04 下午）**：村民人偶 / 脑白金 / 糖水 + 浓缩蜂蜜汁 / 蜜蜂 / 蜂箱配方 /
AE2 陨石粉。桶表新增 `result_fluid`、`delay_ticks`、`drain` 三种规则；与主线同步。
**联动**：22 种材料（14 金属 + 8 矿物）；Mek / Thermal / Create / IE / Tinkers / TiT / AE2 全部可选。
**当前表尺**：筛子 7 输入 / 113 掉落（无科技模组；沙/砂砾/尘土比主线少 3 条钴，历史差异）；
+AE2 117；全装 +TiT 139、再加 AE2 143；锤表 79（全装）/ 55（无模组）；桶 18（全装）/ 16（缺 Thermal）。
**下一步**：商店上传 → 一次性画面/手感验收 → P4 JEI 同步 → 修钴差异（见文末）。

## 当前批次（2026-11-04 下午，村民 / 脑白金 / 蜂蜜 / 蜜蜂）

- **村民人偶**（`villager_doll`）：8 绿宝石围一圈普通人偶；巫水满桶 + 右键 → 僵尸村民，
  和平掉腐肉。**脑白金**（`brain_platinum`）：蛋 4 + 糖 4 + 绿宝石，右键傻子村民 → 普通村民。
- **糖水 → 浓缩蜂蜜汁**：两个桶专用流体（无方块、无桶）；满水 + 糖 → 糖水，
  满糖水 + 蒲公英黄 30 秒 → 浓缩蜂蜜汁；玻璃瓶扣 250 mB → 蜂蜜瓶；浓缩蜂蜜桶每 100 tick
  1/6 概率在 6 格内养 1 只蜜蜂（上限 5）。
- **蜂箱**：3 木板 + 3 蜂蜜瓶 + 3 木板 → 原版蜂箱。
- **AE2 陨石粉**：沙、砂砾各 1/16 `ae2:sky_dust`；AE2 掉落 141 → 143。
- **实现**：`BarrelRecipes` 增加 `resultFluid` / `delayTicks` / `drain`；BE 增加延迟换液状态机
  （NBT `PendingFluidFrom/To/Ticks`）、玻璃瓶抽取、蜂蜜养蜂；JEI 流体结果槽 + 延迟/抽取提示；
  Jade 显示“转化中”。流体用 `TintedWaterFluidType` + 原版水贴图 tint。
- **验证**：`-PtechMods` 下 `[barrel] 18 transforms (2 fluid, 16 item)`、`[sieve] 143 drops`；
  data merge 验证 30 秒换液与养蜂；`/summon` 两个新物品正常；sand/gravel dump 含
  `sky_dust 0.0625`。**未验**：真实右键链条、脑白金右键、JEI、流体亮度。

## 历史批次（详细见 [history.md](history.md)）

- **2026-11-03 晚**：工匠巧思（TiT）阿迪特 / 叶绿素 + AE2 赛特斯石英。
- **2026-11-03**：数据包 v2 移植、末影蛋糕移植、三路径 P1/P1.5/P2/P3、Mek 氟石 + Thermal 六种。
- **2026-11-02**：datagen 第二/三批移植、Tinkers 3 / IE 接入、科技模组锭、0.1.0 发布。
- **2026-11-01**：M1–M10（桶/筛/锤钩/坩埚/巫水/瓷桶/蚕/人偶/机器）、P1 薄门面、P2 数据管线。

## 交接清单（新会话先看）

- **工作区**：`/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium-1.20.1`（`1.20.1-forge` worktree）；
  主工作区 `/Users/kaifengcrt/Projects/Ex-Nihilo-Satellitium` 是 `main`。两条线都已推 origin。
- **构建**：`JAVA_HOME=/Users/kaifengcrt/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./gradlew build`。
- **客户端**：`./gradlew runClient -PdevMods -PtechMods`；**专用服**：`./gradlew runServer -PtechMods`
  （`libs/` 有 jar 才加载；不带 `-PtechMods` 是 113 / 55 的基表）。
- **先跑**：`./gradlew build` + `python3 scripts/check-textures.py`；分支没有 core/data 守卫脚本，
  涉及数据表时回主线跑 `check-data-paths.py --target 1.20.1`。
- **已知不一致**：沙/砂砾/尘土各少 1 条钴（1/64），基表 113 vs 主线 116；分支没有
  `generate_sieve_assets.py`，要么补脚本要么手工对齐。
- **dev 环境**：`libs/` 有 Mek/Thermal/Create/IE/Tinkers/TiT + AE2/GuideME；删掉即回基表。
  热力人偶只在 Thermal 在场（`-PtechMods`）时注册；不带时桶 16 条、物品不存在。
- **进游戏验收（剩余）**：一次性验收清单里的画面/右键手感；村民人偶/脑白金/糖水链/养蜂/
  热力人偶；TiT/AE2 实际筛出与 JEI 页；匠魂炉实际熔化；自动输出抽屉。
- 不要提交 `build/`、`run/`、`reference/`、`libs/`、`.gradle/`。

## 功能现状

- 核心循环与主线一致：12 桶、7 输入筛表、6 锤 / 2 钩、39 熔点 + 9 热源、巫水/瓷桶/11 木坩埚/
  蚕/8 人偶（含 2 只热力条件人偶、村民人偶）、四台机器 + 自动输出。
- 村民/蜂蜜：村民人偶、脑白金、桶专用糖水/浓缩蜂蜜汁（无方块无桶）、养蜂、蜂箱配方。
- 数据布局：`recipes/`、`loot_tables/`、`tags/blocks|items/`，标签命名空间 `forge:`；
  JSON 字段是 1.20.1 形态（裸 `conditions`）。
- 22 种材料（14 金属 + 8 矿物）；`gems` 只在对应模组在场时注册；锭候选顺序与主线相同。
- 联动：Mek / Thermal / Create / IE / Tinkers / TiT / AE2，全部可选、缺失降级。
- 已知差异：筛表少 3 条钴（沙 / 砂砾 / 尘土 1/64）；分支没有 core/data 守卫脚本。

## 一次性验收清单（作者进游戏）

> 服务端逻辑都已 RCON 验过；下面只列“要看一眼/摸一下”的项。建议创造模式 + JEI。

- [ ] **桶**：12 种桶合成（4 板 + 台阶）；水+熔岩→圆石、熔岩+水→黑曜石；堆肥成土；
      木桶拒绝熔岩的提示；满桶上方倒流体固化。
- [ ] **筛/锤/钩**：7 张筛料表（泥土/沙/砂砾/尘土/草方块/粉碎下界岩/粉碎末地石），三种筛网耐久；6 锤左键、2 钩；
      粉碎下界岩/末地石能筛。
- [ ] **坩埚**：瓷土→未烧制→烧制；投圆石/沙子；篝火/火把热源；熔料与流体显示切换；
      11 种木坩埚被动熔树叶/树苗、拒绝熔岩。
- [ ] **巫水线**：满水桶 + 菌丝发酵；巫水桶装/倒；巫水+沙→灵魂沙、巫水+树苗→深色橡树苗；
      沙筛古代孢子 → 泥土变菌丝、牛变哞菇。
- [ ] **瓷桶**：未烧制→烧制；舀水/熔岩/巫水；倒进木桶/坩埚；图标流体层染色。
- [ ] **蚕/人偶**：蚕右键树叶→侵染、变白、扩散、掉线、Jade 百分比；8 种人偶配方与
      桶规则（和平掉战利品 / 非和平生成生物）；热力两只 / 村民人偶的右键 / JEI / 模型。
- [ ] **热力人偶**：工作台配方（硝石粉 / 雪球两条）；满熔岩 + 狂风人偶出狂风人、
      满水 + 暴雪人偶出暴雪人；和平模式掉 `thermal:*_rod`。
- [ ] **村民 / 蜂蜜**：村民人偶 + 巫水 → 僵尸村民；脑白金右键傻子村民；
      水+糖 / 糖水+蒲公英黄 30 秒 / 玻璃瓶抽蜂蜜；浓缩蜂蜜桶养蜂；蜂箱配方。
- [ ] **四台机器**：合成；右键开 GUI；能量/火焰/进度条；空槽虚影与状态行；
      插件槽只收对应插件（锤子时运槽上限 1）；燃料槽只收燃料；JEI 箭头点击。
- [ ] **自动输出**：左侧标签页 → 面板；6 个面按钮（上/下/前/后/左/右）与 tooltip 世界方向；
      开启后产物弹出；关掉最后一个面自动关总开关；Esc/E 返回。
- [ ] **机器动画**：锤头下压、筛料薄层变薄、电动蓝火花/内能火焰烟；破坏机器掉全部内容。
- [ ] **管道**：下方漏斗只抽输出格；上方漏斗补料；电动只进不出能量。
- [ ] **配置**：`run/config/exnihilosatellitium-common.toml` 的 `crucible.*` / `machine.*`
      热改生效（坩埚容量、机器速度/耗电）。
- [ ] **TiT / AE2**：草方块筛出叶绿素碎矿、碎下界岩筛出阿迪特碎矿；沙筛出赛特斯水晶/粉；
      两条 AE2 掉落在 JEI 筛子页可见。

## 验证状态

> 只保留当前仍有效的证据；更早的逐项记录见 [history.md](history.md)。
> **测试绿 ≠ 画面/手感验过**；没进游戏看过的一律标 ⚠️。

| 项 | 状态 | 证据 / 说明 |
|---|---|---|
| `./gradlew build` | ✅ | 2026-11-04：`BUILD SUCCESSFUL`（`--offline`，Java 17） |
| `check-textures.py` | ✅ | 270 模型 / 102 blockstate / 153 纹理；七个子模型只引用 vanilla block 贴图 |
| datagen 三批 | ✅ | `ModRecipes` 143 条；71 blockstate + 29 方块模型 + 157 物品模型；`written: 0` 幂等 |
| 数据表加载 | ✅ | 全装：`[materials] 22`、`[sieve] 7 inputs / 143 drops`、`[hammer] 79`、`[barrel] 18`（缺 Thermal 16）、`[compost] 67`、`[crucible] 39/9` |
| 三路径 P1–P3 | ✅ | 七张表 add/remove/replace/`dump`；`/reload` 前后 dump 逐字节一致；KJS 2001 + CT 14 生效 |
| P1 守卫 | ⚠️ | 分支只跑 build + 纹理；`common/table` 的 core 守卫在主线跑 |
| TiT 阿迪特 / 叶绿素 | ✅ | 22 entries；`ardite → tinkers_thinking:ardite_ingot`、`chlorophyll → chlorophyte_compound`；RCON 熔炉通过；缺 TiT 静默跳过 |
| AE2 赛特斯 / 陨石粉 | ✅ | 139 → 143 drops；RCON sand 含 crystal 0.16667 / dust 0.08333 / sky_dust 0.0625，gravel 含 sky_dust；缺 AE2 时 139、无 unknown |
| Mek / Thermal / Create / IE | ✅ | 锭候选与矿砾熔炼 6/6、IE 五锭、Create 锌 + 12 条 crushing；多方块未跑 |
| Tinkers 钴 / 种子 / 熔炼 | ✅ | 钴 → `tconstruct:cobalt_ingot`；两个黏液草种子条件注册；84 条匠魂熔炼加载无报错 |
| M10 机器 | ✅ | 电动/内能 × 筛/锤、插件、漏斗、自动输出 RCON 全过；JEI 箭头点击已修 |
| 桶 / 堆肥 / 固化 | ✅ | RCON：堆肥成土、满桶固化、牛奶史莱姆、比较器/雨水 |
| 坩埚 / 巫水 / 瓷桶 | ✅ | 39+9 表、篝火/熔炉速率、巫水发酵与转化、瓷桶 capability |
| 蚕 / 人偶 | ✅ | 侵染进度/扩散、8 种人偶（含热力 2 只、村民）生成与和平掉落 RCON 验过 |
| 热力人偶 | ✅ / ⚠️ | `[barrel] 18`、dump 两条规则（`thermal:blitz` / `thermal:blizz`，和平 `thermal:*_rod`）、`/summon` 两物品通过；配方经 `/reload` 解析探测；合成 / 右键 / JEI 未实测 |
| 村民 / 蜂蜜 | ✅ / ⚠️ | 两个新物品 `/summon` 正常、配方由 datagen 生成；data merge 验证 30 秒换液、蜜蜂生成（`PersistenceRequired`）；真实右键 / 玻璃瓶抽取 / JEI 未测 |
| 客户端画面 | ⚠️ | 最近一次客户端冒烟停在 2026-11-02；新物品/流体、热力人偶、TiT/AE2 物品、JEI 页未目视 |
| 合成 / 右键手感 | ⚠️ | 全部配方 JSON 已加载，但没有玩家逐项实测 |

## 下一步（按价值排序）

1. **1.0.0 商店收尾**：tag `v1.0.0-1.20.1` 已打；CurseForge / Modrinth 上传待所有者。
2. **一次性进游戏验收 M1.5–M10**：桶/筛/锤/钩/坩埚/巫水/瓷桶/蚕/人偶（含热力两只、村民）+
   糖水链/养蜂、四台机器、自动输出抽屉、BER 动画、JEI 页面、TiT/AE2 实际筛出。
3. **修分支筛表钴差异**：沙/砂砾/尘土各补 1 条 1/64（或补 `generate_sieve_assets.py`），
   与主线基表 116 对齐。
4. **P4 JEI 同步 + fixture CI**；P3 状态机拆分；advancement / CI。
5. **更多 supported mods**：AE2 充能赛特斯 / 天石粉、TiT 其它矿、IE 1.21.1。

## 已拍板的决策

- **项目名**：`Ex Nihilo: Satellitium` /「无中生有：众星捧月」；mod id `exnihilosatellitium`，
  Java 包 `com.kaifengcrt.exnihilosatellitium`，资源命名空间 `exnihilosatellitium`（2026-10-28）。
- **许可证 MIT**（2026-10-27 从 ARR 转）；Forge MDK 仍 LGPL-2.1，见 [THIRD-PARTY.md](../THIRD-PARTY.md)。
- **材料表 + 自持矿物链**（2026-10-29）：自动发现保留，但注册只认材料表。表内材料由我们
  手写颜色 / `tier` / `rarity`，并在游戏里拥有完整自有链（矿砾 → 破碎 → 粉碎 → 粉末 → 锭）；
  表外材料只生成骨架行、默认关闭，作者填值并重启后开启。不做“扫到即生效”。
  详见 [DESIGN.md](DESIGN.md) §6.1 第 4 条、§6.4；表结构见 [data-format.md](data-format.md) §8。
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
  联调要正式服务端/客户端，或用 deobf 依赖。本分支已改成 `libs/` + `fg.deobf` +
  `mixin.env.remapRefMap`（见「现在停在哪」），新加 mod 照这个套路放。
- **Jade provider 缺 `config.jade.plugin_<ns>.<uid>` 翻译会直接崩客户端**：jade 11 在
  `JadeClient.onGui` 里 `AssertionError`（`Missing config translation`），不是可选提示。
  分支的木桶插件 uid 是 `barrel`，`config.jade.plugin_exnihilosatellitium.barrel` 已补。
- **AE2 生产 jar 不能直丢 `run/mods`**：ForgeGradle dev 会因 SRG mixin
  `@Shadow field f_129762_ was not located` 崩服；必须放 `libs/` 并由 `-PtechMods` 走 `fg.deobf`。
- **条件物品的模型不能交给 datagen**：热力人偶只在 Thermal 在场时注册，模型手写；
  `ModItemModels.HAND_FLAT_MODELS` 按名字跳过，否则 datagen 一旦看到 Thermal 就会写出同名
  模型和手写文件撞路径。字段是 `@Nullable`，用它的地方（创造栏等）必须 null 检查。
- **TiT 阿迪特/叶绿素是单源**：只从碎下界岩 / 草方块出，不再扩散到砂砾/沙子/尘土；
  改筛表前先看 `scripts/generate_sieve_assets.py`（分支没有这个脚本，需手工对齐）。
## 常用命令

```bash
export JAVA_HOME=/Users/kaifengcrt/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew build --offline
python3 scripts/check-textures.py

./gradlew runServer -PtechMods          # RCON：localhost:25575 / eni
./gradlew runClient -PdevMods -PtechMods
./gradlew runData --offline

# 推送（SakuraCat 代理；代理开着时走 ssh.github.com:443；代理没开时直连 22 也可，2026-11-04 实测）
export GIT_SSH_COMMAND='ssh -i ~/.ssh/id_ed25519_github -o IdentitiesOnly=yes -o ProxyCommand="nc -X connect -x 127.0.0.1:12450 %h %p"'
git -c url."ssh://git@ssh.github.com:443/WanConcubine/Ex-Nihilo-Satellitium.git".insteadOf="git@github.com:WanConcubine/Ex-Nihilo-Satellitium.git" push origin 1.20.1-forge
```

## 文件地图

```
src/main/java/com/kaifengcrt/exnihilosatellitium/
├── ExNihiloSatellitium.java        # 入口（Forge 的 FMLJavaModLoadingContext）
├── registry/                       # Block / Item / BlockEntity / MenuType / CreativeTab
├── common/
│   ├── block/                      # Barrel / Sieve / Crucible / UnfiredCrucible / Machine / Liquid
│   ├── blockentity/                # 桶 / 筛 / 坩埚 / 侵染树叶的状态机、NBT、比较器
│   ├── fluid/                      # 巫水（ForgeFlowingFluid + FluidType）
│   ├── barrel/ compost/            # 转化表 / 堆肥表 + reload listener
│   ├── sieve/ hammer/ crook/       # 筛表 / 锤表 / 叶表 + reload listener
│   ├── crucible/                   # 熔点 + 热源表 + reload listener
│   ├── machine/                    # 四台机器：SieveRolls / HammerRolls / UpgradeItem
│   │   ├── block/ blockentity/ menu/
│   ├── material/                   # materials.json 读取（注册期，ingots 候选）
│   ├── config/                     # ExConfig（ForgeConfigSpec）
│   ├── network/                    # 机器输出配置用的 C2S 包
│   ├── event/                      # CommonEvents（reload）、能力注册
│   └── tag/ModTags.java
├── client/                         # BER（桶/筛/坩埚/机器/蚕）、GUI 屏、ClientEvents
├── compat/                         # MoonlightCompat、jade、jei、kubejs、crafttweaker（可选软依赖）
├── datagen/                        # 三批：标签/掉落表 + 配方 + blockstate/模型
└── platform/                       # 本版的机器能量/物品能力适配
```

数据在 `src/main/resources/data/exnihilosatellitium/`（`recipes/`、`loot_tables/`、`tags/`），
格式见 [data-format.md](data-format.md)；datagen 现状见 [datagen.md](datagen.md)。

## 文档维护

- **当前状态只写在本文件**；DESIGN 写“为什么/架构/里程碑”，data-format 写“JSON/NBT/命令”，textures 写“美术规则”。
- 改完功能先回写本文件的验证表，再改 DESIGN 的实现记录。
- 不要在本文件抄大段设计；链接过去。
- **批次叙事只留当前批次**；上一批/更早的搬到 [history.md](history.md)（2026-11-02 整理时搬走过约 100 行）。
