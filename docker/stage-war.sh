#!/usr/bin/env bash
# Build the xnat-web WAR and stage it where the Dockerfile expects it
# (docker-context/xnat.war). Run before `docker compose up --build`.
#
# Usage: ./docker/stage-war.sh [--skip-build]
set -euo pipefail
cd "$(dirname "$0")/.."

if [ "${1:-}" != "--skip-build" ]; then
    echo ">> Building :xnat-web:war (Java 21 toolchain) ..."
    ./gradlew :xnat-web:war
fi

war="$(ls -t xnat-web/build/libs/*.war 2>/dev/null | head -1 || true)"
if [ -z "${war}" ]; then
    echo "!! No WAR found under xnat-web/build/libs/. Run without --skip-build." >&2
    exit 1
fi

mkdir -p docker-context
cp "${war}" docker-context/xnat.war
echo ">> Staged $(basename "${war}") -> docker-context/xnat.war ($(du -h docker-context/xnat.war | cut -f1))"
echo ">> Next: docker compose up --build"
