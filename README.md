# 无中生有：东施效颦 / Ex Nihilo: Imitatio

> 面向 Minecraft **1.20.1 Forge** 的空岛资源获取模组，从零重写。
> **非官方作品**，与 Ex Nihilo 系列及其作者没有隶属关系。

“东施效颦”是自嘲式命名：我们明确承认自己是在模仿经典，而不是冒充原版。英文副标题
*Imitatio* 取自拉丁语“模仿”。

## 这是什么

一个 clean-room（净室）重写的 Ex Nihilo 风格模组：从一棵树、一桶水、一格熔岩开始，
通过木桶、筛子、锤子、钩子、坩埚等工具，把空岛一步步变成可发展的基地。

**没有使用任何现有 Ex Nihilo 分支的代码、贴图、模型或音效。** 只参考公开的玩法描述
（Wiki、MC百科、视频）进行设计。详见 [docs/DESIGN.md](docs/DESIGN.md)。

## 技术栈

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.28 |
| Java | 17 |
| Gradle | 8.8（Wrapper） |
| 构建插件 | ForgeGradle 6 |

## 当前进度

- [x] Gradle / Forge MDK 骨架，可编译
- [x] 模组元数据、Creative Tab、中英文本地化
- [x] 占位内容：`porcelain`（瓷土）、`barrel`（木桶）
- [ ] M1 木桶 + 堆肥 + 流体处理
- [ ] M2 筛子 + 筛网 + 数据驱动掉落
- [ ] M3 锤子 + 钩子
- [ ] M4 坩埚 + 热源
- [ ] M5 矿粒 / 粉碎矿 / 冶炼
- [ ] M6 瓷土 / 瓷桶 / 巫水 / 蚕
- [ ] M7 JEI / 配置 / 数据生成
- [ ] M8 与「月光魔法」联动

## 构建

```bash
./gradlew build        # 产物在 build/libs/
./gradlew runClient    # 开发客户端
./gradlew runServer    # 开发服务端
```

首次构建会下载 Gradle、Forge、Minecraft 依赖并反编译，耗时较长（约 5–15 分钟），
之后会走缓存。

## 联动

对「月光魔法」（`moonlight_magic`，1.20.1 Forge，1.2.0）做**软依赖**联动：

- `mods.toml` 里是 optional dependency（`mandatory=false`），没装月光魔法也能独立运行。
- 代码走 `compat/MoonlightCompat`，只通过 mod id + 注册名查找，不直接引用对方类。
- 联动内容用数据包实现（配方 / 掉落 / 堆肥值 + `forge:mod_loaded` 条件）。

候选内容：月光线做筛网、池尘做稀有筛出物、月光植物进堆肥、本模组矿粒作为月光仪式材料。
详见 [docs/DESIGN.md](docs/DESIGN.md) 第 6 节。

## 许可证

- **本项目的代码与美术资源：All Rights Reserved（保留所有权利）**，详见 [LICENSE](LICENSE)。
- Forge MDK 模板部分（构建脚本等）遵循 **LGPL-2.1**，详见 [LICENSE-FORGE-MDK.txt](LICENSE-FORGE-MDK.txt)。
- Minecraft / Forge 本身不包含在本仓库中，其版权归各自所有者。

## 致谢

感谢 Ex Nihilo 系列（Erasmus_Crowley、insaneau、Jozufozu、BloodWorkXGaming、SirLyle、
NovaMachina、thedarkcolour、LordDeatHunter 等）为社区提供的玩法范式。本项目仅借鉴
公开的玩法概念，不代表、也不包含他们的实现。
