#!/usr/bin/env bash
#
# fresh-xnat.sh — tear down and bring up a completely clean XNAT instance.
#
# Runs the whole local dev/test bring-up in one shot:
#   1. docker compose down -v         drop the old containers AND named volumes (fresh DB + data)
#   2. stage-war.sh [+ stage-plugins] rebuild/stage the current WAR (and curated plugins) for the image
#   3. docker compose up --build -d   rebuild the image and boot XNAT (uninitialized)
#   4. init-xnat.sh                   wait for boot, first-install init + base site config + SMTP
#   5. build-known-state.sh           populate the reproducible data fixture   (skip with --no-data)
#
# XNAT ends up on http://localhost:8080 (admin/admin). SMTP creds come from the git-ignored
# docker/init-xnat.local.sh (sourced by init-xnat.sh), same as running init-xnat.sh by hand.
#
# Usage:
#   ./docker/fresh-xnat.sh                     # full clean rebuild: war + plugins + init + data
#   ./docker/fresh-xnat.sh --skip-war-build    # reuse the last-built WAR (no gradle rebuild) — faster
#   ./docker/fresh-xnat.sh --no-plugins        # don't stage the curated plugin set
#   ./docker/fresh-xnat.sh --no-data           # init only; skip the build-known-state.sh data fixture
#   ./docker/fresh-xnat.sh --skip-war-build --no-plugins --no-data   # fastest bare instance
#
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."   # repo root (where docker-compose.yml lives)

# ---- flags -------------------------------------------------------------------------
SKIP_WAR_BUILD=0
NO_PLUGINS=0
NO_DATA=0
for arg in "$@"; do
  case "$arg" in
    --skip-war-build) SKIP_WAR_BUILD=1 ;;
    --no-plugins)     NO_PLUGINS=1 ;;
    --no-data)        NO_DATA=1 ;;
    -h|--help)        awk 'NR==1{next} /^#/{sub(/^# ?/,""); print; next} {exit}' "${BASH_SOURCE[0]}"; exit 0 ;;
    *) echo "!! unknown flag: $arg (see --help)" >&2; exit 2 ;;
  esac
done

step() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }

# ---- 1. tear down old stack + volumes ----------------------------------------------
step "Dropping old stack + volumes (docker compose down -v)"
docker compose down -v --remove-orphans

# ---- 2. stage the WAR (+ plugins) for the image ------------------------------------
if [ "$SKIP_WAR_BUILD" -eq 1 ]; then
  step "Staging existing WAR (--skip-build; no gradle rebuild)"
  ./docker/stage-war.sh --skip-build
else
  step "Building + staging the current xnat-web WAR"
  ./docker/stage-war.sh
fi

if [ "$NO_PLUGINS" -eq 1 ]; then
  step "Skipping plugin staging (--no-plugins); clearing docker/plugins so none load"
  mkdir -p docker/plugins && rm -f docker/plugins/*.jar
else
  step "Staging curated plugins (docker/plugins.manifest)"
  # Best-effort: a missing/unbuildable plugin source shouldn't block the whole bring-up.
  ./docker/stage-plugins.sh || echo "!! plugin staging failed — continuing without (re-run ./docker/stage-plugins.sh, then: docker compose restart xnat)"
fi

# ---- 3. build the image + boot -----------------------------------------------------
step "Rebuilding image + booting XNAT (docker compose up --build -d)"
docker compose up --build -d

# ---- 4. init: wait for boot, first-install setup + base site config + SMTP ----------
step "Initializing XNAT (init-xnat.sh waits for boot, then configures the site)"
./docker/init-xnat.sh

# ---- 5. data fixture ---------------------------------------------------------------
if [ "$NO_DATA" -eq 1 ]; then
  step "Skipping data fixture (--no-data)"
else
  if ! command -v http >/dev/null 2>&1; then
    echo "!! build-known-state.sh needs httpie ('http') — not found. Skipping the data fixture."
    echo "   Install httpie and run: BASE_URL=http://localhost:8080 ADMIN_USER=admin ADMIN_PASS=admin ./docker/build-known-state.sh"
  else
    step "Populating the known-state data fixture (build-known-state.sh)"
    BASE_URL=http://localhost:8080 ADMIN_USER=admin ADMIN_PASS=admin ./docker/build-known-state.sh
  fi
fi

step "Done — XNAT is up at http://localhost:8080 (admin/admin)"
