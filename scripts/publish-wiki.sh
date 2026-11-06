#!/usr/bin/env bash
# Publish the player wiki with one command.
#
#   ./scripts/publish-wiki.sh -m "docs(wiki): ..."
#
# It runs the wiki guard, a strict MkDocs build, commits only the wiki-related
# paths, and pushes to origin/main. GitHub Actions then builds and deploys Pages.
#
# Options:
#   -m, --message TEXT   commit message (default: docs(wiki): update YYYY-MM-DD)
#   -n, --dry-run        run checks and show what would be committed; do not commit
#       --no-push        commit locally, skip git push
#   -h, --help           show this help
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PATHS=(wiki mkdocs.yml scripts/check-wiki.py requirements-wiki.txt)
MESSAGE=""
DRY_RUN=0
NO_PUSH=0

usage() {
  sed -n '2,12p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -m|--message)
      [[ $# -ge 2 ]] || { echo "error: $1 needs a value" >&2; exit 2; }
      MESSAGE="$2"
      shift 2
      ;;
    -n|--dry-run)
      DRY_RUN=1
      shift
      ;;
    --no-push)
      NO_PUSH=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "error: unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

branch="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$branch" != "main" ]]; then
  echo "error: publish from main (current branch: $branch)" >&2
  exit 1
fi

changed="$(git status --porcelain -- "${PATHS[@]}")"
if [[ -z "$changed" ]]; then
  echo "nothing to publish: no changes under ${PATHS[*]}"
  exit 0
fi

MKDOCS="$ROOT/.venv/bin/mkdocs"
if [[ ! -x "$MKDOCS" ]]; then
  if command -v mkdocs >/dev/null 2>&1; then
    MKDOCS="$(command -v mkdocs)"
  else
    echo "error: mkdocs not found." >&2
    echo "  python3 -m venv .venv && .venv/bin/pip install -r requirements-wiki.txt" >&2
    exit 1
  fi
fi

echo "==> changed"
printf '%s\n' "$changed"

echo "==> wiki guard"
python3 scripts/check-wiki.py

echo "==> strict build"
"$MKDOCS" build --strict

if [[ $DRY_RUN -eq 1 ]]; then
  echo "dry run: checks passed; nothing committed"
  exit 0
fi

git add -- "${PATHS[@]}"
if git diff --cached --quiet -- "${PATHS[@]}"; then
  echo "nothing to commit: only untracked/ignored changes"
  exit 0
fi

if [[ -z "$MESSAGE" ]]; then
  MESSAGE="docs(wiki): update $(date +%F)"
fi

# Commit only the wiki paths; unrelated staged files stay staged.
git commit -m "$MESSAGE" -- "${PATHS[@]}"
git log -1 --oneline

if [[ $NO_PUSH -eq 1 ]]; then
  echo "committed; push skipped (--no-push)"
  exit 0
fi

echo "==> push origin main"
git push origin main

cat <<'EOF'

pushed. GitHub Actions will build and deploy:
  https://github.com/WanConcubine/Ex-Nihilo-Satellitium/actions/workflows/wiki.yml
  https://wanconcubine.github.io/Ex-Nihilo-Satellitium/
EOF
