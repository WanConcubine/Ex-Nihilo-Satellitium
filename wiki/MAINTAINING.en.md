# Wiki Maintenance Contract

> Last checked: 2026-11-03 · Applies to: 1.21.1 / 1.20.1

This wiki is the **player-facing** documentation for custom recipes. The goal is not to "write it once", but that **it never goes stale when the table schema changes**.

## 1. Maintainers

- **Owner: the repository owner** (by default). The Owner is responsible for "whether the three paths are still equivalent and whether the documentation still holds".
- **Backup: whoever changes the table schema / loader / commands**. Whoever changes the code updates the corresponding page in the same commit.
- **Reviewer: the person who merges the PR**. Before merging, confirm that `python3 scripts/check-wiki.py` passes and that the capability matrix has not been written as "supported".

This repository has no separate docs team; the Owner may designate a release manager per release, but **must not treat "the documentation is stale" as an acceptable state**.

## 2. Documentation is the Definition of Done

For any of the following changes, the wiki **must** be updated in the same commit; otherwise the change counts as incomplete:

| Code change | Pages to update |
|---|---|
| Add / remove / rename a directory for the seven tables | `datapack.md`, `kubejs.md`, `cookbook.md`, `troubleshooting.md` |
| Add / remove / rename a field (including `min`/`max`/`mod`/`patch`/`remove`) | `datapack.md`; if KJS/CT also supports it, update `kubejs.md` / `crafttweaker.md` in sync |
| Change tag expansion rules, patch order, `waiting for tags` timing | `datapack.md`, `troubleshooting.md` |
| Change the `/exnihilo` command | `datapack.md`, `troubleshooting.md`, `cookbook.md` |
| Add a KJS event / script API | `kubejs.md`, the `README.md` capability matrix, `docs/customization-roadmap.md` |
| Add a CT recipe type / script API | `crafttweaker.md`, the `README.md` capability matrix, `docs/customization-roadmap.md` |
| Change the 1.20.1 / 1.21.1 paths, `pack_format`, tag namespaces | the `README.md` version matrix, `datapack.md`, `troubleshooting.md` |
| Change the material table / registration-time content | `README.md` "Things you cannot do through these three paths", `cookbook.md` |

## 3. Writing rules for the three paths

The long-term goal of this mod is **three equivalent paths: datapack / KubeJS / CraftTweaker**.
Until that is reached, the documentation **must state the gaps truthfully** and must not be written as "all supported" just to look good.

Rules:

1. **The capability matrix is the only conclusion.** The table in `README.md` decides whether each path "can / cannot";
   other pages may only explain it, not widen it.
2. **No "supported" without a test.** When adding a KJS/CT capability, there must be a reproducible example,
   and it must be ticked in the acceptance table of `docs/customization-roadmap.md`.
3. **The same rule stays in sync in three places.** If a JSON rule can also be written in KJS, `kubejs.md` must have the same example;
   likewise once CT supports it. Do not change only one place.
4. **Do not write a temporary limitation as permanent design.** For example, "CT cannot reach the seven tables" is a fact today,
   but it should be marked "current version", and the equivalence plan should be listed in the roadmap.
5. **Version differences are labelled separately.** Any form that applies only to 1.21.1 or 1.20.1 must name the version in the paragraph,
   not only in the matrix at the top of the page.

## 4. Versions and "Last checked"

Every page has this line at the top:

```text
> 最后核对：YYYY-MM-DD · 适用：1.21.1 / 1.20.1
```

- When you change a page's content, set the date to that day.
- Before a release (tag / jar), confirm page by page; pages that were not changed must also be confirmed as "still valid", and only then should the date be updated.
- If a version line is paused for maintenance, the page must explicitly say "1.20.1 not checked"; it must not be assumed that both versions hold by default.
- `scripts/check-wiki.py` checks whether this line exists; whether the date is fresh is judged by the Owner at release time.

## 5. Page ownership

| Page | Primary maintainer | Content boundary |
|---|---|---|
| `README.md` | Owner | capability matrix, version matrix, how to choose among the three paths |
| `datapack.md` | contributors who change the table schema | complete fields of the seven tables, vanilla recipes, tags |
| `kubejs.md` | contributors doing KJS integration | `kubejs/data`, script API, KJS boundaries |
| `crafttweaker.md` | contributors doing CT integration | CT boundaries, vanilla recipes, alternatives |
| `cookbook.md` | Owner + contributors | common tasks, the three ways side by side |
| `troubleshooting.md` | the people doing support / troubleshooting | logs, commands, version differences, common misunderstandings |
| `MAINTAINING.md` | Owner | this file |
| `../docs/customization-roadmap.md` | Owner | phased plan and acceptance for the three-path equivalence |

## 6. Pre-release checklist

```bash
python3 scripts/check-wiki.py
```

- [ ] The script passes (links, fences, JSON, required pages/sections, version matrix, nav coverage, translation coverage and JSON parity)
- [ ] `mkdocs build --strict` passes (new pages have been added both to the `nav` in `mkdocs.yml` and to `_sidebar.md`,
      and have a matching `.en.md` translation)
- [ ] The date at the top of every page has been updated
- [ ] The `README.md` capability matrix matches the actual code
- [ ] The `pack_format`, tag namespaces, and recipe paths for 1.20.1 / 1.21.1 are not swapped
- [ ] Every new / changed example has been verified at least once manually with `/reload` (or is covered by a test pack)
- [ ] The "current status" of `docs/customization-roadmap.md` has been updated

## 7. Handling stale pages

When you find that a page is stale:

1. At the top of the page, change the date to the actual check date, and mark `⚠️ needs checking` in the body;
2. Open an issue / todo that states clearly "which rule, which version, what evidence";
3. Do not delete the old form outright — mark it "old field / old version" first, and remove it in the next release.

## 8. Why the documentation is not auto-generated

The final form of the seven tables will have three entry points (JSON / KJS / CT), and the field descriptions, examples, and boundary conditions are not fully isomorphic;
fully automated generation tends to produce pages that are "correct but nobody can understand". The current strategy is:

- **the schema follows the code** (`docs/data-format.md` + loader);
- **player explanations follow the wiki**;
- **`check-wiki.py` is the structural guard, and the release check is the semantic guard**.

Once the three paths are truly equivalent and the fields are stable, consider generating the API reference pages from the schema.

## 9. Static site

`wiki/` is also the source directory of the [MkDocs Material](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/mkdocs.yml) site: `mkdocs build` only produces `site/`
(which is gitignored), **no server is needed**, and hosting goes through GitHub Pages.

- **There are two navigation places**: the `nav` in `mkdocs.yml` (the site sidebar) and `_sidebar.md` (the GitHub directory page / legacy docsify convention).
  `scripts/check-wiki.py` requires that both cover all content pages; a new page must be added to both, otherwise the guard fails.
- **Links to `docs/` must use absolute GitHub URLs**: the site only publishes `wiki/`, so `../docs/...` is a broken link on the site,
  and `mkdocs build --strict` fails immediately.
- Local preview:

  ```bash
  python3 -m venv .venv
  .venv/bin/pip install -r requirements-wiki.txt
  .venv/bin/mkdocs serve   # http://127.0.0.1:8000
  ```
- Release: pushing to `main` triggers `.github/workflows/wiki.yml` (it runs the guards first, then `mkdocs build --strict`,
  and finally deploys Pages); a PR only validates and does not publish. On first setup, choose
  "GitHub Actions" as the Source in the repository's Settings → Pages.

## 10. Multiple languages

The site is bilingual: `/` is Chinese, `/en/` is English, using the suffix structure of `mkdocs-static-i18n`.

- **Every content page needs an English twin**: `datapack.md` ↔ `datapack.en.md`. When you add a page, create both,
  otherwise `scripts/check-wiki.py` fails.
- **JSON examples must be byte-identical**: translate only the prose outside code fences; the guard parses both
  sides' JSON code blocks and compares them, so editing one side only is an error.
- **The English header line** is always `> Last checked: YYYY-MM-DD · Applies to: 1.21.1 / 1.20.1`; when you change
  a page, update the date on both sides.
- **No Chinese may remain in an English page** (code fences and inline code excepted); the guard scans for
  `\u4e00-\u9fff` and reports the line number.
- **Sidebar titles come from `nav_translations` in `mkdocs.yml`**: when you add a nav entry, add both the Chinese
  title and its translation, otherwise the English sidebar leaks Chinese.
- **Write internal links as-is** (`datapack.md`): the plugin points them at the current language at build time, so
  do not hard-code `/en/`; hard-coding it would send Chinese readers to the English page.
- **`theme.features: navigation.instant` must stay off**: the language switcher needs a full page load, and enabling
  it makes `mkdocs build --strict` fail.
