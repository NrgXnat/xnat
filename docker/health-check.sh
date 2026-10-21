#!/usr/bin/env bash
# Smoke-test a running XNAT (the Tomcat 9 deploy stack) against the Phase 0 migration seams.
# Walks the checklist in docs/tomcat9-deploy-stack.md and reports PASS/FAIL/SKIP per check.
#
# Usage:
#   ./docker/health-check.sh
#   XNAT_URL=http://localhost:8080 XNAT_USER=admin XNAT_PASS=admin ./docker/health-check.sh
#
# Exit code: 0 if no checks FAIL, 1 otherwise. Checks needing seeded data are SKIPped, not failed.
set -uo pipefail

XNAT_URL="${XNAT_URL:-http://localhost:8080}"
XNAT_USER="${XNAT_USER:-admin}"
XNAT_PASS="${XNAT_PASS:-admin}"
WAIT="${WAIT:-300}"                 # seconds to wait for server readiness
BODY="$(mktemp)"; trap 'rm -f "$BODY"' EXIT

pass=0; fail=0; skip=0
GREEN=$'\033[32m'; RED=$'\033[31m'; YEL=$'\033[33m'; DIM=$'\033[2m'; RST=$'\033[0m'

# req METHOD PATH [auth|noauth] [extra curl args...] -> sets HC_CODE, HC_CTYPE (body in $BODY)
req() {
    local method="$1" path="$2" mode="${3:-auth}"
    shift $(( $# >= 3 ? 3 : $# ))
    local auth=(); [ "$mode" = "auth" ] && auth=(-u "$XNAT_USER:$XNAT_PASS")
    read -r HC_CODE HC_CTYPE < <(
        curl -sS -X "$method" "${auth[@]}" -o "$BODY" \
             -w '%{http_code} %{content_type}' --max-time 30 "$@" "$XNAT_URL$path" 2>/dev/null \
        || echo "000 -"
    )
}

report() { # NAME RESULT DETAIL   (RESULT = PASS|FAIL|SKIP)
    local color; case "$2" in PASS) color=$GREEN; pass=$((pass+1));; FAIL) color=$RED; fail=$((fail+1));; SKIP) color=$YEL; skip=$((skip+1));; esac
    printf '  %s%-5s%s %-46s %s%s%s\n' "$color" "$2" "$RST" "$1" "$DIM" "$3" "$RST"
}

expect() { # NAME  wanted-regex  detail-what-it-exercises
    if [[ "$HC_CODE" =~ $2 ]]; then report "$1" PASS "$HC_CODE $HC_CTYPE"
    else report "$1" FAIL "got $HC_CODE (want $2) — $3"; fi
}

echo "XNAT smoke test -> $XNAT_URL  (user: $XNAT_USER)"

# --- 0. wait for readiness -----------------------------------------------------
printf '  waiting for server (up to %ss) ' "$WAIT"
deadline=$(( $(date +%s) + WAIT ))
until req GET / noauth; [ "$HC_CODE" != "000" ] || { [ "$(date +%s)" -ge "$deadline" ]; }; do
    printf '.'; sleep 5
done
echo
if [ "$HC_CODE" = "000" ]; then
    echo "${RED}Server not reachable at $XNAT_URL after ${WAIT}s.${RST}"; exit 1
fi

# --- 1. Turbine/Velocity render (unauthenticated login page) -------------------
req GET /app/template/Login.vm noauth
expect "login page renders (Velocity 2 / Turbine)" '^(200|302)$' "Velocity SPI + VelocityService"

# --- 2. auth + Restlet resource + session (XnatSecureGuard) --------------------
req GET /data/JSESSION
expect "auth + session (/data/JSESSION)" '^200$' "XnatSecureGuard Filter + Restlet resource"

# --- 3. SecureResource shim + content negotiation ------------------------------
req GET "/data/projects?format=json"
expect "GET /data/projects json" '^200$' "SecureResource->ServerResource get(Variant)/represent"
req GET "/data/projects?format=xml"
expect "GET /data/projects xml"  '^200$' "content negotiation (XML variant)"
req GET "/data/projects?format=html"
expect "GET /data/projects html" '^200$' "content negotiation (HTML variant)"

# --- 4. method allowance (getAllowedMethods) -----------------------------------
req DELETE "/data/projects"
expect "DELETE /data/projects -> 405" '^(405|403)$' "getAllowedMethods() bridges 1.1 allowX()"

# --- 5. Turbine screen render via the main servlet -----------------------------
req GET "/app/template/Index.vm"
expect "GET /app screen (Index.vm)" '^(200|302)$' "RunData->PipelineData + Velocity render"

# --- 6. XAPI still responds (Spring MVC, not migrated but co-resident) ----------
req GET "/xapi/siteConfig/buildInfo"
expect "GET /xapi/siteConfig/buildInfo" '^(200|401|403)$' "XAPI reachable alongside Restlet"

# --- 7. checks that need seeded data (manual) ----------------------------------
report "zip/tar download (Disposition)" SKIP "needs a session with files — verify manually"
report "multipart upload (getPart)"     SKIP "needs an upload form — verify manually"

echo
echo "  ${GREEN}$pass passed${RST}, ${RED}$fail failed${RST}, ${YEL}$skip skipped${RST}"
[ "$fail" -eq 0 ]
