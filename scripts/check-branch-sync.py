#!/usr/bin/env python3
"""Keep the 1.21.1 main tree and the 1.20.1 branch in sync (tech-debt #51).

The two lines are supposed to differ only by an explicit, owner-approved list:
1.20.1-only content (BambooMod gems, the slime-dirt chain, TConstruct data) and
the known gaps tracked in docs/status.md. Everything else must match, or the
next release ships two different games.

The guard compares *entries*, not files, so re-ordering or re-formatting a
table never hides a real change, and an allowlisted file still fails when
somebody adds a new entry to it.

    python3 scripts/check-branch-sync.py
    python3 scripts/check-branch-sync.py --branch ../Ex-Nihilo-Satellitium-1.20.1
    python3 scripts/check-branch-sync.py --list   # show every diff, ignore ALLOW

ALLOW is a list of (table, file, entry, kind, reason); file/entry are fnmatch
patterns, kind is main_only / branch_only / changed. Add an entry only with a
reason that points at a decision or a tracked item.
"""

from __future__ import annotations

import argparse
import fnmatch
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_BRANCH = ROOT.parent / "Ex-Nihilo-Satellitium-1.20.1"
DATA = "src/main/resources/data/exnihilosatellitium"

# table -> (directory, expanded list key or None for whole-file records)
TABLES: dict[str, tuple[str, str | None]] = {
    "sieve": ("sieve", "outputs"),
    "crook": ("crook", "drops"),
    "compost": ("compost", "values"),
    "hammer": ("hammer", None),
    "barrel": ("barrel", None),
    "crucible_melt": ("crucible_melt", None),
    "crucible_heat": ("crucible_heat", None),
}

# Known, intentional differences. Every entry needs a reason.
ALLOW: list[tuple[str, str, str, str, str]] = [
    # --- 1.20.1-only content, owner-approved ---------------------------------
    # BambooMod increment: +8 dirt, +8 grass_block, +3 gravel gems (table-sizes 里 +19)
    ("sieve", "dirt", "bamboomod:*", "branch_only", "1.20.1 专属：BambooMod 增量（+19）"),
    ("sieve", "grass_block", "bamboomod:*", "branch_only", "1.20.1 专属：BambooMod 增量（+19）"),
    ("sieve", "gravel", "bamboomod:*", "branch_only", "1.20.1 专属：BambooMod 宝石增量（+3）"),
    # Slime-dirt chain (1.4.2: 大地黏液草种子 / 黏液泥土 -> 匠魂树苗)
    ("sieve", "*_slime_dirt", "*", "branch_only", "1.20.1 专属：黏液泥土 -> 匠魂树苗链"),
    (
        "sieve",
        "dirt",
        "exnihilosatellitium:earth_slime_grass_seeds|tconstruct",
        "branch_only",
        "1.20.1 专属：大地黏液草种子",
    ),
    (
        "sieve",
        "grass_block",
        "exnihilosatellitium:earth_slime_grass_seeds|tconstruct",
        "branch_only",
        "1.20.1 专属：大地黏液草种子",
    ),
    # --- blocked on a tracked item, not accepted as permanent ----------------
    # docs/status.md 下一步 9: 1.20.1 缺钴 1/64；在修好之前这条白名单防止缺口扩散。
    (
        "sieve",
        "sand",
        "exnihilosatellitium:cobalt_*",
        "main_only",
        "已知缺口：1.20.1 缺钴 1/64（status 待办 9），修好后删除本行",
    ),
    (
        "sieve",
        "gravel",
        "exnihilosatellitium:cobalt_*",
        "main_only",
        "已知缺口：1.20.1 缺钴 1/64（status 待办 9），修好后删除本行",
    ),
    (
        "sieve",
        "dust",
        "exnihilosatellitium:cobalt_*",
        "main_only",
        "已知缺口：1.20.1 缺钴 1/64（status 待办 9），修好后删除本行",
    ),
    # --- loader-level naming, not content drift ------------------------------
    # data_target.py 规则：1.21.1 用 c: 标签，1.20.1 用 forge:。
    ("compost", "default", "#c:*", "main_only", "标签命名空间：c:（1.21.1）"),
    ("compost", "default", "#forge:*", "branch_only", "标签命名空间：forge:（1.20.1）"),
    # 1.20.3 把 minecraft:grass 改名为 minecraft:short_grass。
    ("compost", "default", "minecraft:short_grass|", "main_only", "1.20.3 更名：grass -> short_grass"),
    ("compost", "default", "minecraft:grass|", "branch_only", "1.20.3 更名：grass -> short_grass"),
]


def canon(value: object) -> str:
    return json.dumps(value, sort_keys=True, separators=(",", ":"))


def records(root: Path, table: str) -> dict[tuple[str, str], str]:
    directory, expand = TABLES[table]
    base = root / DATA / directory
    out: dict[tuple[str, str], str] = {}
    if not base.is_dir():
        return out
    for path in sorted(base.glob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        if expand and isinstance(data, dict) and isinstance(data.get(expand), (list, dict)):
            items = data[expand]
            if isinstance(items, list):
                pairs = ((entry.get("item", "?"), entry) for entry in items)
            else:  # compost `values` is a mapping
                pairs = ((key, {"value": value}) for key, value in items.items())
            for entry_id, entry in pairs:
                mod = entry.get("mod", "")
                out[(path.stem, f"{entry_id}|{mod}")] = canon(entry)
        else:
            out[(path.stem, "*")] = canon(data)
    return out


def allowed(table: str, file: str, entry: str, kind: str) -> str | None:
    for a_table, a_file, a_entry, a_kind, reason in ALLOW:
        if (
            a_table == table
            and fnmatch.fnmatchcase(file, a_file)
            and fnmatch.fnmatchcase(entry, a_entry)
            and a_kind == kind
        ):
            return reason
    return None


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--branch", default=str(DEFAULT_BRANCH), help="1.20.1 worktree path")
    parser.add_argument("--list", action="store_true", help="ignore ALLOW and print every diff")
    args = parser.parse_args()

    branch = Path(args.branch).resolve()
    if not (branch / DATA).is_dir():
        print(f"branch tree not found: {branch / DATA}")
        return 2

    known: dict[str, int] = {}
    unknown: list[str] = []
    for table in TABLES:
        main_recs = records(ROOT, table)
        branch_recs = records(branch, table)
        if not main_recs and not branch_recs:
            continue
        diffs = 0
        for key in sorted(set(main_recs) | set(branch_recs)):
            file, entry = key
            if key in main_recs and key in branch_recs:
                if main_recs[key] == branch_recs[key]:
                    continue
                kind = "changed"
                detail = f"main={main_recs[key]} branch={branch_recs[key]}"
            elif key in main_recs:
                kind, detail = "main_only", main_recs[key]
            else:
                kind, detail = "branch_only", branch_recs[key]
            diffs += 1
            label = f"{table}/{file}:{entry} [{kind}]"
            reason = None if args.list else allowed(table, file, entry, kind)
            if reason:
                known[reason] = known.get(reason, 0) + 1
            else:
                unknown.append(f"{label} — {detail}")
        print(f"{table:16} main={len(main_recs):3} branch={len(branch_recs):3} diffs={diffs}")

    for reason, count in sorted(known.items()):
        print(f"  known: {count} × {reason}")
    if unknown:
        print("branch sync check failed:")
        for entry in unknown:
            print(f"  - {entry}")
        print("Fix the data, or add an ALLOW entry with a reason that points at a decision.")
        return 1
    print(f"branch sync check passed ({sum(known.values())} known diff(s))")
    return 0


if __name__ == "__main__":
    sys.exit(main())
