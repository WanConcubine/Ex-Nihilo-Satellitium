# Wiki 维护契约

> 最后核对：2026-11-03 · 适用：1.21.1 / 1.20.1

这份 wiki 是**玩家向**的自定义配方文档。目标不是“写一次”，而是**每次改表结构都不会过时**。

## 1. 维护者

- **Owner：仓库所有者**（默认）。Owner 对“三条路径是否仍然等价、文档是否仍然成立”负责。
- **Backup：每次改表结构 / loader / 命令的贡献者**。谁改代码，谁在同一提交里更新对应页面。
- **Reviewer：合并 PR 的人**。合并前确认 `python3 scripts/check-wiki.py` 通过，且能力矩阵没有被写成“支持”。

当前仓库没有单独的 docs 团队；Owner 可以按 release 指定一个 release manager，
但**不能把“文档过时”当成可接受状态**。

## 2. 文档是 Definition of Done

以下任一改动，**必须**在同一提交里更新 wiki，否则视为未完成：

| 代码改动 | 要更新的页面 |
|---|---|
| 新增/删除/重命名七张表的目录 | `datapack.md`、`kubejs.md`、`cookbook.md`、`troubleshooting.md` |
| 新增/删除/改名字段（含 `min`/`max`/`mod`/`patch`/`remove`） | `datapack.md`；如果 KJS/CT 也支持，同步 `kubejs.md` / `crafttweaker.md` |
| 改标签展开规则、补丁顺序、`waiting for tags` 时序 | `datapack.md`、`troubleshooting.md` |
| 改 `/exnihilo` 命令 | `datapack.md`、`troubleshooting.md`、`cookbook.md` |
| 新增 KJS 事件 / 脚本 API | `kubejs.md`、`README.md` 能力矩阵、`docs/customization-roadmap.md` |
| 新增 CT recipe type / 脚本 API | `crafttweaker.md`、`README.md` 能力矩阵、`docs/customization-roadmap.md` |
| 改 1.20.1 / 1.21.1 路径、`pack_format`、标签命名空间 | `README.md` 版本矩阵、`datapack.md`、`troubleshooting.md` |
| 改材料表 / 注册期内容 | `README.md`「不能通过这三条路线做到的事」、`cookbook.md` |

## 3. 三条路径的写作规则

本模组的长期目标是 **数据包 / KubeJS / CraftTweaker 三条等价路径**。
在达到之前，文档必须**如实写差距**，不能为了好看写成“都支持”。

规则：

1. **能力矩阵是唯一结论。** `README.md` 的表格决定每条路径“能/不能”；
   其他页面只能解释，不能扩大。
2. **没有测试就不写“支持”。** 新增 KJS/CT 能力时，必须有一个可复现的示例，
   并在 `docs/customization-roadmap.md` 的验收表里打勾。
3. **同一条规则三处同步。** 如果一条 JSON 规则在 KJS 里也能写，`kubejs.md` 必须有同样的例子；
   CT 支持后同理。不要只改一处。
4. **不把临时限制写成永久设计。** 例如“CT 碰不到七张表”目前是事实，
   但应标注“当前版本”，并在 roadmap 里列出等价化计划。
5. **版本差异单独标注。** 任何只适用于 1.21.1 或 1.20.1 的写法，必须在段落里写版本，
   不能只放在页面顶部的矩阵里。

## 4. 版本与“最后核对”

每个页面顶部有一行：

```text
> 最后核对：YYYY-MM-DD · 适用：1.21.1 / 1.20.1
```

- 改动页面内容时，把日期改成当天。
- 发布前（tag / jar）逐页确认；没有改动的页面也要确认“仍然成立”，然后更新日期。
- 如果某个版本线暂停维护，页面要显式写“1.20.1 未核对”，不能默认两版都成立。
- `scripts/check-wiki.py` 会检查这行是否存在；日期是否新鲜由 Owner 在 release 时判断。

## 5. 页面归属

| 页面 | 主要维护者 | 内容边界 |
|---|---|---|
| `README.md` | Owner | 能力矩阵、版本矩阵、三条路线怎么选 |
| `datapack.md` | 改表结构的贡献者 | 七张表的完整字段、原版配方、标签 |
| `kubejs.md` | 做 KJS 集成的贡献者 | `kubejs/data`、脚本 API、KJS 边界 |
| `crafttweaker.md` | 做 CT 集成的贡献者 | CT 边界、原版配方、替代方案 |
| `cookbook.md` | Owner + 贡献者 | 常见任务，三种写法对照 |
| `troubleshooting.md` | 支持/排错的人 | 日志、命令、版本差异、常见误解 |
| `MAINTAINING.md` | Owner | 本文件 |
| `../docs/customization-roadmap.md` | Owner | 三路径等价的分阶段计划与验收 |

## 6. 发布前检查清单

```bash
python3 scripts/check-wiki.py
```

- [ ] 脚本通过（链接、围栏、JSON、必需页面/章节、版本矩阵、nav 覆盖、译文覆盖与 JSON 一致性）
- [ ] `mkdocs build --strict` 通过（新增页面已同时加进 `mkdocs.yml` 的 `nav` 和 `_sidebar.md`，
      并且有对应的 `.en.md` 译文）
- [ ] 每个页面顶部日期已更新
- [ ] `README.md` 能力矩阵与实际代码一致
- [ ] 1.20.1 / 1.21.1 的 `pack_format`、标签命名空间、配方路径没有写反
- [ ] 新增/修改的示例至少手动 `/reload` 验证过一次（或由测试包覆盖）
- [ ] `docs/customization-roadmap.md` 的“当前状态”已更新

## 7. 过时处理

发现页面过时时：

1. 在页面顶部把日期改成实际核对日期，并在正文标注 `⚠️ 待核对`；
2. 开一个 issue/待办，写清“哪条规则、哪个版本、证据”；
3. 不要直接删掉旧写法——先标注“旧字段/旧版本”，等下一个 release 再移除。

## 8. 为什么不把文档自动生成

七张表的最终形态会有三种入口（JSON / KJS / CT），字段说明、示例、边界条件不完全同构；
全自动生成容易产出“正确但没人看得懂”的页面。当前策略是：

- **schema 以代码为准**（`docs/data-format.md` + loader）；
- **玩家解释以 wiki 为准**；
- **用 `check-wiki.py` 做结构守卫**，用 release 检查做语义守卫。

等三条路径真正等价、字段稳定后，再考虑从 schema 生成 API 参考页。

## 9. 静态站点

`wiki/` 同时是 [MkDocs Material](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/mkdocs.yml) 站点的源目录：`mkdocs build` 只产出 `site/`
（已 gitignore），**不需要服务器**，托管走 GitHub Pages。

- **导航有两处**：`mkdocs.yml` 的 `nav`（站点侧栏）和 `_sidebar.md`（GitHub 目录页 / 旧 docsify 约定）。
  `scripts/check-wiki.py` 要求两处都覆盖所有内容页；新增页面两处都要加，否则守卫失败。
- **链到 `docs/` 必须用 GitHub 绝对 URL**：站点只发布 `wiki/`，`../docs/...` 在站内是坏链，
  `mkdocs build --strict` 会直接失败。
- 本地预览：

  ```bash
  python3 -m venv .venv
  .venv/bin/pip install -r requirements-wiki.txt
  .venv/bin/mkdocs serve   # http://127.0.0.1:8000
  ```
- 发布：push 到 `main` 触发 `.github/workflows/wiki.yml`（先跑守卫，再 `mkdocs build --strict`，
  最后部署 Pages）；PR 只校验、不发布。首次启用要在仓库 Settings → Pages 里把 Source 选成
  “GitHub Actions”。

## 10. 多语言

站点是中英双语的：`/` 是中文，`/en/` 是英文，用 `mkdocs-static-i18n` 的后缀结构。

- **每个内容页必须有英文孪生文件**：`datapack.md` ↔ `datapack.en.md`。新增页面时两个一起建，
  否则 `scripts/check-wiki.py` 直接失败。
- **JSON 示例必须逐字一致**：只翻代码块以外的散文；守卫会把两边的 JSON 代码块解析后比较，
  只改了一边就报错。
- **英文页头部行**固定为 `> Last checked: YYYY-MM-DD · Applies to: 1.21.1 / 1.20.1`；改内容时
  和中文页一起改日期。
- **英文页不允许残留中文**（代码块与行内代码除外）；守卫会扫 `\u4e00-\u9fff`，并报出行号。
- **侧栏标题来自 `mkdocs.yml` 的 `nav_translations`**：新增导航项时中英标题都要加，否则英文侧栏
  会漏出中文。
- **站内相对链接照原样写** `datapack.md` 即可：构建时插件会把它指到当前语言的页面，不需要手写
  `/en/`；反过来，手写 `/en/` 会让中文页跳到英文页。
- **不能开 `theme.features: navigation.instant`**：语言切换需要整页跳转，模型下开启会让
  `mkdocs build --strict` 直接失败。

