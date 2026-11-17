#!/usr/bin/env python3
"""P4 S4a: end-to-end datapack fixture against a running dev server.

Installs `scripts/fixtures/datapack` into `<run-dir>/world/datapacks/eni-fixture`,
reloads the server over RCON, then asserts that `/exnihilo dump sieve` contains
the fixture's line. This is the first step of the fixture CI: datapack path only,
no KubeJS/CraftTweaker (S4b).

    python3 scripts/check-fixture.py                     # server must be running
    python3 scripts/check-fixture.py --install-only      # CI: install before start
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FIXTURE = ROOT / "scripts/fixtures/datapack"
EXPECTED = "sieve minecraft:sand -> minecraft:diamond 0.5 x1"


def install(run_dir: Path) -> Path:
    target = run_dir / "world" / "datapacks" / "eni-fixture"
    if target.exists():
        shutil.rmtree(target)
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copytree(FIXTURE, target)
    return target


def rcon(command: str, port: int, password: str) -> str:
    result = subprocess.run(
        [sys.executable, str(ROOT / "scripts" / "rcon.py"), command,
         "--port", str(port), "--password", password],
        capture_output=True, text=True, check=True,
    )
    return result.stdout


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--run-dir", default=str(ROOT / "run"))
    parser.add_argument("--port", type=int, default=25575)
    parser.add_argument("--password", default="eni")
    parser.add_argument("--install-only", action="store_true")
    args = parser.parse_args()

    target = install(Path(args.run_dir))
    print(f"fixture installed: {target}")
    if args.install_only:
        return 0

    rcon("reload", args.port, args.password)
    time.sleep(3)
    dump = rcon("exnihilo dump sieve", args.port, args.password)
    if EXPECTED in dump:
        print(f"fixture check passed: {EXPECTED}")
        return 0
    print("fixture check failed: expected line not found")
    print(dump[:2000])
    return 1


if __name__ == "__main__":
    sys.exit(main())
