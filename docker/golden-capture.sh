#!/usr/bin/env bash
# Capture (or check) normalized responses from a running XNAT for golden-master testing.
# Wraps docs/tools/golden_master.py: hits a curated set of /app screens and /data + /xapi endpoints,
# normalizes volatile bits (CSRF, session id, timestamps, cache-busters), and stores/compares each.
#
# Usage:
#   ./docker/golden-capture.sh update     # capture baselines into $GOLDEN_DIR (do this on a KNOWN-GOOD build)
#   ./docker/golden-capture.sh check      # diff current responses against the stored baselines
#
# Env: XNAT_URL (default http://localhost:8080), XNAT_USER/XNAT_PASS (default admin/admin),
#      GOLDEN_DIR (default docs/goldens)
#
# IMPORTANT: goldens are tied to the underlying DATA (same DB contents, same user). Capture and check
# against the SAME seeded/snapshotted database, or diffs will show data changes, not migration bugs.
set -uo pipefail

MODE="${1:-}"
case "$MODE" in update|check) ;; *) echo "usage: $0 {update|check}"; exit 2 ;; esac

XNAT_URL="${XNAT_URL:-http://localhost:8080}"
XNAT_USER="${XNAT_USER:-admin}"
XNAT_PASS="${XNAT_PASS:-admin}"
GOLDEN_DIR="${GOLDEN_DIR:-docs/goldens}"

here="$(cd "$(dirname "$0")" && pwd)"
GM="$here/../docs/tools/golden_master.py"
[ -f "$GM" ] || { echo "missing $GM"; exit 2; }
mkdir -p "$GOLDEN_DIR"
JAR="$(mktemp)"; BODY="$(mktemp)"; FULL="$(mktemp)"; trap 'rm -f "$JAR" "$BODY" "$FULL"' EXIT

# ---- endpoints: "name|path" (name -> $GOLDEN_DIR/<name>.txt) ---------------------------------------
# /app screens exercise Turbine 5.1 + Velocity 2; /data exercises the Restlet SecureResource shim +
# content negotiation; /xapi is Spring MVC (not migrated, sanity only). Data-specific rows (a report
# for a known project id, a zip download, etc.) are left for you to add once the DB is seeded.
ENDPOINTS=(
  "app-index|/app/template/Index.vm"
  "app-quicksearch|/app/template/QuickSearch.vm"
  "data-projects-json|/data/projects?format=json"
  "data-projects-xml|/data/projects?format=xml"
  "data-projects-html|/data/projects?format=html"
  "data-subjects-json|/data/subjects?format=json"
  "data-experiments-json|/data/experiments?format=json"
  "data-jsession|/data/JSESSION"
  "data-version|/data/version"
  "xapi-buildinfo|/xapi/siteConfig/buildInfo"
)

pass=0; fail=0; cap=0; nogold=0
GREEN=$'\033[32m'; RED=$'\033[31m'; YEL=$'\033[33m'; DIM=$'\033[2m'; RST=$'\033[0m'

echo "golden $MODE -> $XNAT_URL  (user: $XNAT_USER, goldens: $GOLDEN_DIR)"
for entry in "${ENDPOINTS[@]}"; do
  name="${entry%%|*}"; path="${entry#*|}"
  code=$(curl -sS -u "$XNAT_USER:$XNAT_PASS" -b "$JAR" -c "$JAR" \
              -o "$BODY" -w '%{http_code} %{content_type}' --max-time 60 "$XNAT_URL$path" 2>/dev/null) || code="000 -"
  # Prepend a status/content-type header so a 200->500 or content-type change shows up in the diff too.
  { printf '# GET %s -> %s\n' "$path" "$code"; cat "$BODY"; } > "$FULL"
  golden="$GOLDEN_DIR/$name.txt"

  if [ "$MODE" = "update" ]; then
    python3 "$GM" update "$FULL" "$golden" >/dev/null && printf '  %scaptured%s %-24s %s%s%s\n' "$GREEN" "$RST" "$name" "$DIM" "$code" "$RST" && cap=$((cap+1))
  else
    out="$(python3 "$GM" check "$FULL" "$golden" 2>&1)"; rc=$?
    case $rc in
      0) printf '  %sPASS%s   %-24s %s%s%s\n' "$GREEN" "$RST" "$name" "$DIM" "$code" "$RST"; pass=$((pass+1));;
      2) printf '  %sNOGOLD%s %-24s %s(run: %s update)%s\n' "$YEL" "$RST" "$name" "$DIM" "$0" "$RST"; nogold=$((nogold+1));;
      *) printf '  %sDIFF%s   %-24s %s%s%s\n' "$RED" "$RST" "$name" "$DIM" "$code" "$RST"
         echo "$out" | sed -n '2,25p' | sed 's/^/       /'; fail=$((fail+1));;
    esac
  fi
done

echo
if [ "$MODE" = "update" ]; then
  echo "  ${GREEN}$cap captured${RST} into $GOLDEN_DIR/ — review + commit them as the baseline."
else
  echo "  ${GREEN}$pass passed${RST}, ${RED}$fail differ${RST}, ${YEL}$nogold missing baseline${RST}"
fi
[ "$fail" -eq 0 ]
