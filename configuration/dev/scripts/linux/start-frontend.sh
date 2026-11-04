#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../../.." && pwd)"

if ! command -v npm >/dev/null 2>&1; then
  printf 'Node.js et npm sont requis pour demarrer le frontend.\n' >&2
  exit 1
fi

cd -- "$repo_root/frontend"
npm ci
exec npm start -- "$@"
