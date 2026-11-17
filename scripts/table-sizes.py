#!/usr/bin/env python3
"""表尺统计：数 data 目录里的玩法表条目，按 `mod` 门拆成「基线 + 增量」。

为什么要有这个脚本：`docs/status.md` 的「表尺」抄了一串数字，每次加条目都会漂移。
这里给出唯一可复跑的口径，文档只贴脚本输出，不再手算。

用法：

    python3 scripts/table-sizes.py                                  # 当前工作树
    python3 scripts/table-sizes.py --root ../Ex-Nihilo-Satellitium-1.20.1

口径（改字段 / 目录时同步改这里和 docs/status.md「表尺」）：

- 筛表：一个文件一个输入，掉落数按 `outputs` 数组元素计；带根级 `"mod"` 的文件
  整份只在模组在场时生效，条目级 `"mod"` 的条目单独计增量。
- 锤表 / 钩表 / 桶表 / 熔炼 / 热源：一个文件一条规则（锤表按文件计，不数 `result`）。
- 堆肥：按 `values` 的键计，一个标签键算 1 条。
- 没有 `mod` 门的是基线；基线里已经排除条目级 `mod`。
"""

from __future__ import annotations

import argparse
import json
from collections import Counter
from pathlib import Path

TABLES = (
    ("sieve", "筛表"),
    ("hammer", "锤表"),
    ("crook", "钩表"),
    ("barrel", "桶表"),
    ("crucible_melt", "熔点"),
    ("crucible_heat", "热源"),
    ("compost", "堆肥"),
)


def count(directory: Path, table: str) -> tuple[int, Counter[str], str]:
    if not directory.is_dir():
        return 0, Counter(), "目录不存在"
    if table == "compost":
        values = json.loads((directory / "default.json").read_text(encoding="utf-8"))["values"]
        tags = sum(1 for key in values if key.startswith("#"))
        return len(values), Counter(), "标签 %d" % tags

    base = 0
    deltas: Counter[str] = Counter()
    inputs: set[str] = set()
    for path in sorted(directory.glob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        mod = data.get("mod")
        if table == "sieve":
            outputs = data.get("outputs", [])
        else:
            outputs = None
        if mod:
            deltas[mod] += len(outputs) if outputs is not None else 1
            continue
        if outputs is None:
            base += 1
            continue
        for entry in outputs:
            if "mod" in entry:
                deltas[entry["mod"]] += 1
            else:
                base += 1
        if "input" in data:
            inputs.add(data["input"])
    detail = "输入 %d" % len(inputs) if table == "sieve" else ""
    return base, deltas, detail


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=Path(__file__).resolve().parent.parent)
    args = parser.parse_args()
    root = Path(args.root)
    data = root / "src/main/resources/data/exnihilosatellitium"
    print("# %s" % root.resolve())
    opened: dict[str, int] = {}
    for table, label in TABLES:
        base, deltas, detail = count(data / table, table)
        opened[table] = base + sum(deltas.values())
        extra = "（%s）" % detail if detail else ""
        mods = "、".join("%s +%d" % (mod, n) for mod, n in sorted(deltas.items())) or "无 mod 增量"
        print("%s\t基线 %d%s；%s\t全开 %d" % (label, base, extra, mods, opened[table]))
    print(
        "筛表全开 %d / 锤表全开 %d / 堆肥 %d"
        % (opened["sieve"], opened["hammer"], opened["compost"])
    )


if __name__ == "__main__":
    main()
