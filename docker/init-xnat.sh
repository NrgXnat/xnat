#!/usr/bin/env bash
#
# init-xnat.sh — initialize a freshly-booted (uninitialized) XNAT and apply a small base
# site configuration + real outbound SMTP. Creates NO users/projects/data.
#
# Phase 2 of the local dev/test workflow:
#   1. docker compose up --build            # boots XNAT UNINITIALIZED (like the develop branch)
#   2. ./docker/init-xnat.sh            # <-- THIS: first-install setup + base config + SMTP
#   3. ./docker/build-known-state.sh    # populate the reproducible data fixture
#
# Idempotent and safe to re-run: it waits for XNAT to finish its own schema init, then applies
# the first-install setup (once), a small base site config, and points XNAT at a real mail server.
#
# Requirements: curl (no httpie needed — this runs before any data tooling). XNAT seeds an
# admin/admin account on first boot (init_security_000.sql).
#
# Usage:
#   SMTP_PASS='<gmail-app-password>' ./docker/init-xnat.sh      # real outbound mail (Gmail default)
#   BASE_URL=http://localhost:8080 ADMIN_USER=admin ADMIN_PASS=admin SMTP_PASS=... ./docker/init-xnat.sh
#   ./docker/init-xnat.sh                                       # init + config; mail left unset (no SMTP_PASS)
#   SMTP_HOST='' ./docker/init-xnat.sh                          # skip mail config entirely
#   SITE_CONFIG_JSON='{"userRegistration":true}' ./docker/init-xnat.sh   # override base config
#
# SECRETS: never hard-code SMTP_PASS (or SMTP_USER) in this tracked script. Put them in a
# git-ignored local overrides file — by default docker/init-xnat.local.sh, or point
# INIT_XNAT_ENV at one. It is sourced first (see below), so its values win over the defaults.
# Example docker/init-xnat.local.sh:
#     SMTP_USER='you@gmail.com'
#     SMTP_PASS='abcd efgh ijkl mnop'   # a Google App Password, not your login password

set -euo pipefail

# ---- configuration ------------------------------------------------------------------
# Local overrides / secrets (git-ignored). Sourced BEFORE the defaults so SMTP_USER/SMTP_PASS/etc.
# set there take effect without living in this tracked file.
_here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
_overrides="${INIT_XNAT_ENV:-$_here/init-xnat.local.sh}"
# shellcheck source=/dev/null
[[ -f "$_overrides" ]] && { source "$_overrides"; _sourced_overrides="$_overrides"; }

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin}"
AUTH="${ADMIN_USER}:${ADMIN_PASS}"

SITE_ID="${SITE_ID:-XNAT}"
SITE_URL="${SITE_URL:-$BASE_URL}"
ADMIN_EMAIL="${ADMIN_EMAIL:-${ADMIN_USER}@example.org}"

# Outbound SMTP — points XNAT at a REAL mail server. Defaults to Gmail SMTP for
# automation@xnatworks.io (the mailbox the Playwright email tests read via the Gmail API), so a
# registration/verification email actually gets delivered and can be verified end to end.
#   - Supply the account password via SMTP_PASS (a Google App Password) — from the git-ignored
#     overrides file above, NOT hard-coded here. If auth is on and SMTP_PASS is empty, mail is LEFT
#     UNCONFIGURED (XNAT won't send until you set it).
#   - Other providers: override SMTP_HOST/PORT/USER/PROTOCOL/AUTH/STARTTLS.
#   - Set SMTP_HOST='' to leave XNAT's mail config untouched.
#   - Local capture-only sink instead? Run Mailpit yourself and set
#       SMTP_HOST=<host> SMTP_PORT=<port> SMTP_AUTH=false SMTP_STARTTLS=false SMTP_PASS=x
SMTP_HOST="${SMTP_HOST:-smtp.gmail.com}"
SMTP_PORT="${SMTP_PORT:-587}"
SMTP_USER="${SMTP_USER:-automation@xnatworks.io}"
SMTP_PASS="${SMTP_PASS:-}"
SMTP_PROTOCOL="${SMTP_PROTOCOL:-smtp}"
SMTP_AUTH="${SMTP_AUTH:-true}"
SMTP_STARTTLS="${SMTP_STARTTLS:-true}"

# Small, dev-friendly base site config applied right after init. Override wholesale with
# SITE_CONFIG_JSON (or set it to '' to skip). Defaults:
#   passwordComplexity ^.*$              : allow simple fixture/test passwords
#   userRegistration   true              : the self-registration flow (S1001 tests) is exercisable
#   uiAllowNonAdminProjectCreation true  : non-admin owners can create projects in tests
SITE_CONFIG_JSON="${SITE_CONFIG_JSON-{\"passwordComplexity\":\"^.*\$\",\"userRegistration\":true,\"uiAllowNonAdminProjectCreation\":true}}"

WAIT_SECS="${WAIT_SECS:-900}"

C_OK=$'\033[32m'; C_WARN=$'\033[33m'; C_ERR=$'\033[31m'; C_DIM=$'\033[2m'; C_RST=$'\033[0m'
log()  { printf '%s\n' "$*"; }
step() { printf '%s>>%s %s\n' "$C_DIM" "$C_RST" "$*"; }
cadmin() { curl -fsS -u "$AUTH" "$@"; }   # authenticated curl; fail on HTTP error
json_escape() { printf '%s' "${1:-}" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'; }   # escape a value for embedding in JSON

# ---- phases -------------------------------------------------------------------------
wait_for_xnat() {
  step "Waiting for XNAT at $BASE_URL (up to ${WAIT_SECS}s)"
  local deadline=$((SECONDS + WAIT_SECS)) state
  while (( SECONDS < deadline )); do
    # /xapi/siteConfig/initialized is reachable pre-init (initUrls in configured-urls.yaml).
    state="$(curl -fsS -u "$AUTH" "$BASE_URL/xapi/siteConfig/initialized" 2>/dev/null || true)"
    case "$state" in
      true|false) log "  ${C_OK}reachable${C_RST} (initialized=$state)"; return 0 ;;
    esac
    sleep 5
  done
  log "${C_ERR}XNAT did not become reachable within ${WAIT_SECS}s — is 'docker compose up' running?${C_RST}"
  exit 1
}

initialize_site() {
  step "Site initialization"
  local state
  state="$(curl -fsS -u "$AUTH" "$BASE_URL/xapi/siteConfig/initialized" 2>/dev/null || true)"
  if [[ "$state" == "true" ]]; then log "  ${C_DIM}(already initialized)${C_RST}"; return 0; fi
  # POST the minimal first-install config to lift XnatInitCheckFilter's setup gate. XnatAppInfo
  # re-reads the 'initialized' preference on the next request, so no restart is needed.
  cadmin -X POST -H "Content-Type: application/json" "$BASE_URL/xapi/siteConfig" \
    -d "{\"initialized\":true,\"siteId\":\"$SITE_ID\",\"siteUrl\":\"$SITE_URL\",\"adminEmail\":\"$ADMIN_EMAIL\"}" >/dev/null
  log "  ${C_OK}initialized${C_RST} (siteId=$SITE_ID, adminEmail=$ADMIN_EMAIL)"
}

configure_site() {
  [[ -n "$SITE_CONFIG_JSON" ]] || { log "  ${C_DIM}(SITE_CONFIG_JSON empty — skipping base config)${C_RST}"; return 0; }
  step "Base site configuration"
  cadmin -X POST -H "Content-Type: application/json" "$BASE_URL/xapi/siteConfig" -d "$SITE_CONFIG_JSON" >/dev/null
  log "  ${C_OK}siteConfig${C_RST} $SITE_CONFIG_JSON"
}

configure_mail() {
  [[ -n "$SMTP_HOST" ]] || { log "  ${C_DIM}(SMTP_HOST empty — leaving mail config as-is)${C_RST}"; return 0; }
  if [[ "$SMTP_AUTH" == "true" && -z "$SMTP_PASS" ]]; then
    log "  ${C_WARN}SMTP_PASS is empty${C_RST} — set it (e.g. a Google App Password for $SMTP_USER) to enable outbound mail."
    log "  ${C_DIM}Put it in ${_sourced_overrides:-$_overrides} (git-ignored). Leaving XNAT mail unconfigured for now.${C_RST}"
    return 0
  fi
  step "Mail (SMTP -> $SMTP_HOST:$SMTP_PORT as ${SMTP_USER:-<none>}; auth=$SMTP_AUTH starttls=$SMTP_STARTTLS)"
  # XNAT stores SMTP as a SmtpServer object: top-level hostname/port/protocol/username/password
  # plus a mailProperties map (mail.smtp.auth, mail.smtp.starttls.enable). POST it as the
  # 'smtpServer' JSON string to /xapi/notifications (the /notifications/smtp form endpoint
  # mis-binds its trailing Properties arg -> 500). That endpoint may itself reply 500 while still
  # applying the change, so don't fail on it — verify by reading it back.
  local inner body
  inner=$(printf '{"hostname":"%s","port":%s,"protocol":"%s","username":"%s","password":"%s","mailProperties":{"mail.smtp.auth":"%s","mail.smtp.starttls.enable":"%s"}}' \
    "$SMTP_HOST" "$SMTP_PORT" "$SMTP_PROTOCOL" "$(json_escape "$SMTP_USER")" "$(json_escape "$SMTP_PASS")" "$SMTP_AUTH" "$SMTP_STARTTLS")
  body="{\"smtpServer\": \"$(json_escape "$inner")\"}"
  printf '%s' "$body" | curl -sS -u "$AUTH" -X POST -H "Content-Type: application/json" "$BASE_URL/xapi/notifications" -d @- >/dev/null 2>&1 || true
  if curl -fsS -u "$AUTH" "$BASE_URL/xapi/notifications/smtp" 2>/dev/null | grep -q "\"$SMTP_HOST\""; then
    log "  ${C_OK}smtp${C_RST} -> $SMTP_HOST:$SMTP_PORT (username=$SMTP_USER, auth=$SMTP_AUTH, starttls=$SMTP_STARTTLS)"
  else
    log "  ${C_WARN}smtp config not confirmed${C_RST} (check $BASE_URL/xapi/notifications/smtp)"
  fi
}

# ---- main ---------------------------------------------------------------------------
[[ -n "$ADMIN_PASS" ]] || { log "${C_ERR}ADMIN_PASS is empty — set it in the environment.${C_RST}"; exit 1; }
[[ -n "${_sourced_overrides:-}" ]] && log "${C_DIM}(loaded local overrides: $_sourced_overrides)${C_RST}"
wait_for_xnat
initialize_site
configure_site
configure_mail
log ""
log "${C_OK}=== XNAT initialized + configured (empty) ===${C_RST}"
log "  Next: ${C_DIM}ADMIN_PASS=$ADMIN_PASS ./docker/build-known-state.sh${C_RST}  (populate the data fixture)"
