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
SCRIPTS = ROOT / "scripts/fixtures/scripts"
EXPECTED = "sieve minecraft:sand -> minecraft:diamond 0.5 x1"
SCRIPT_EXPECTED = (
    "sieve minecraft:sand -> minecraft:emerald 0.4 x1",       # KubeJS fixture
    "sieve minecraft:sand -> minecraft:lapis_lazuli 0.35 x1",  # CraftTweaker fixture
)


def install(run_dir: Path, with_scripts: bool) -> Path:
    target = run_dir / "world" / "datapacks" / "eni-fixture"
    if target.exists():
        shutil.rmtree(target)
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copytree(FIXTURE, target)
    if with_scripts:
        kjs = run_dir / "kubejs" / "server_scripts"
        kjs.mkdir(parents=True, exist_ok=True)
        shutil.copy2(SCRIPTS / "kubejs" / "eni_fixture.js", kjs / "eni_fixture.js")
        zs = run_dir / "scripts"
        zs.mkdir(parents=True, exist_ok=True)
        shutil.copy2(SCRIPTS / "crafttweaker" / "eni_fixture.zs", zs / "eni_fixture.zs")
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
    parser.add_argument("--scripts", action="store_true",
                        help="also install the KubeJS/CraftTweaker fixtures (jars must be in run/mods)")
    args = parser.parse_args()

    target = install(Path(args.run_dir), args.scripts)
    print(f"fixture installed: {target}")
    if args.install_only:
        return 0

    rcon("reload", args.port, args.password)
    time.sleep(3)
    dump = rcon("exnihilo dump sieve", args.port, args.password)
    expected = (EXPECTED,) + (SCRIPT_EXPECTED if args.scripts else ())
    missing = [line for line in expected if line not in dump]
    if not missing:
        for line in expected:
            print(f"fixture check passed: {line}")
        return 0
    print("fixture check failed: missing line(s)")
    for line in missing:
        print(f"  - {line}")
    print(dump[:2000])
    return 1


if __name__ == "__main__":
    sys.exit(main())
