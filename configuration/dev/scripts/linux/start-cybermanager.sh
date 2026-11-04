#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../../.." && pwd)"
config_dir="${CM_CONFIG_DIR:-$repo_root/configuration/backends/cybermanager}"
config_dir="$(cd -- "$config_dir" && pwd)"

if ! command -v mvn >/dev/null 2>&1; then
  printf 'Maven est requis pour demarrer le backend CyberManager.\n' >&2
  exit 1
fi

export CM_DB_MODE="${CM_DB_MODE:-flyway}"
export SPRING_CONFIG_LOCATION="${SPRING_CONFIG_LOCATION:-file:$config_dir/}"
export CM_FLYWAY_LOCATIONS="${CM_FLYWAY_LOCATIONS:-filesystem:$config_dir/db/migration}"
export CM_ERROR_CATALOG_FILE="${CM_ERROR_CATALOG_FILE:-file:$repo_root/configuration/backends/cybermanager/error.json}"

cd -- "$repo_root"
mvn -f "$repo_root/backend/cybermanager-parent/pom.xml" \
  -pl :cm-api -am -Dmaven.test.skip=true install

exec mvn -f "$repo_root/backend/cybermanager/cm-api/pom.xml" \
  -Dmaven.test.skip=true spring-boot:run "$@"
