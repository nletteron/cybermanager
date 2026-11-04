#!/bin/sh
set -eu

{
    printf 'window.__CM_CONFIG__ = '
    jq -n --arg auth "$CM_AUTH_API_URL" --arg api "$CM_API_URL" \
        '{authApiUrl: $auth, usersApiUrl: $api}'
    printf ';\n'
} > /usr/share/nginx/html/runtime-config.js
