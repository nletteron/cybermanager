#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../../.." && pwd)"
config_dir="${CM_CONFIG_DIR:-$repo_root/configuration/backends/auth}"
config_dir="$(cd -- "$config_dir" && pwd)"

if ! command -v mvn >/dev/null 2>&1; then
  printf 'Maven est requis pour demarrer le backend Auth.\n' >&2
  exit 1
fi

export CM_DB_MODE="${CM_DB_MODE:-flyway}"
export SPRING_CONFIG_LOCATION="${SPRING_CONFIG_LOCATION:-file:$config_dir/}"
export CM_FLYWAY_LOCATIONS="${CM_FLYWAY_LOCATIONS:-filesystem:$config_dir/db/migration}"

cd -- "$repo_root"
mvn -f "$repo_root/backend/cybermanager-parent/pom.xml" \
  -pl :cm-auth-api -am -Dmaven.test.skip=true install

exec mvn -f "$repo_root/backend/auth/cm-auth-api/pom.xml" \
  -Dmaven.test.skip=true spring-boot:run "$@"
