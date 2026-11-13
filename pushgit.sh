#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_ORIGIN="git@github.com:samkowskiykostya/charmings_android.git"

cd "$SCRIPT_DIR"

cleanup() {
    git remote remove origin 2>/dev/null || true
}

trap cleanup EXIT

ORIGIN_URL="$DEFAULT_ORIGIN"
if git remote get-url origin &>/dev/null; then
    ORIGIN_URL="$(git remote get-url origin)"
fi

git remote add origin "$ORIGIN_URL"
git push --set-upstream origin main
