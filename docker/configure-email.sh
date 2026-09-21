#!/usr/bin/env bash
#
# configure-email.sh — configure XNAT's outbound SMTP server (the email-only slice of init-xnat.sh).
#
# Points XNAT at a real, authenticated mail server (Gmail by default) so registration/verification
# emails actually deliver to the mailbox the Playwright email tests read via the Gmail API. The box's
# own postfix can't deliver (direct-MX is rejected on the EC2 IP's missing rDNS), so this authenticated
# relay is what makes the email tests pass.
#
# WHEN TO RUN IT:
#   * AFTER deploy-fresh.sh  -> YES if you want email tests. deploy-fresh drops the DB, which wipes the
#     stored SMTP config; the Playwright harness's globalSetup applies all other site config but NOT the
#     SMTP server, so nothing else restores this.
#   * AFTER deploy-update.sh -> NO. A code-only redeploy keeps the DB, so the SMTP config persists.
#   * If email tests are skipped (SKIP_EMAIL_VERIFICATIONS=true) -> NOT needed at all.
#
# SECRETS: never hard-code ADMIN_PASS / SMTP_PASS / SMTP_USER in this tracked script. Put them in a
# git-ignored local overrides file next to this script (configure-email.local.sh), or point
# CONFIG_EMAIL_ENV at one. It is sourced first, so its values win. Example configure-email.local.sh:
#     ADMIN_PASS='...'                          # the site admin password on this box
#     SMTP_USER='automation@xnatworks.io'
#     SMTP_PASS='abcd efgh ijkl mnop'           # a Google App Password, NOT the account login password
#
# Usage (on dave-alldev, as david):
#     ./configure-email.sh
#     BASE_URL=http://localhost:8080 ADMIN_USER=admin ./configure-email.sh
#     SMTP_HOST=smtp.example.com SMTP_PORT=587 ./configure-email.sh
#
set -euo pipefail

_here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
_overrides="${CONFIG_EMAIL_ENV:-$_here/configure-email.local.sh}"
# shellcheck source=/dev/null
[[ -f "$_overrides" ]] && { source "$_overrides"; echo "(loaded local overrides: $_overrides)"; }

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-}"
AUTH="${ADMIN_USER}:${ADMIN_PASS}"

# Outbound SMTP defaults (Gmail). Override any of these via env or the overrides file.
SMTP_HOST="${SMTP_HOST:-smtp.gmail.com}"
SMTP_PORT="${SMTP_PORT:-587}"
SMTP_USER="${SMTP_USER:-automation@xnatworks.io}"
SMTP_PASS="${SMTP_PASS:-}"
SMTP_PROTOCOL="${SMTP_PROTOCOL:-smtp}"
SMTP_AUTH="${SMTP_AUTH:-true}"
SMTP_STARTTLS="${SMTP_STARTTLS:-true}"

WAIT_SECS="${WAIT_SECS:-300}"

json_escape() { printf '%s' "${1:-}" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'; }

[[ -n "$ADMIN_PASS" ]] || { echo "ERROR: ADMIN_PASS is empty — set it in $_overrides (or the environment)." >&2; exit 1; }
[[ -n "$SMTP_PASS" ]]  || { echo "ERROR: SMTP_PASS is empty — set it (a Google App Password for $SMTP_USER) in $_overrides." >&2; exit 1; }

# Wait until XNAT is up AND admin auth works (a fresh DB re-inits from prefs-init.ini; give it time).
echo ">> Waiting for XNAT at $BASE_URL and admin auth (up to ${WAIT_SECS}s)"
deadline=$((SECONDS + WAIT_SECS))
until curl -fsS -u "$AUTH" "$BASE_URL/data/JSESSION" >/dev/null 2>&1; do
  (( SECONDS < deadline )) || { echo "ERROR: XNAT not reachable/authenticating within ${WAIT_SECS}s at $BASE_URL" >&2; exit 1; }
  sleep 5
done
echo "  reachable + admin auth OK"

# Configure the SMTP server. XNAT stores it as a SmtpServer object: top-level
# hostname/port/protocol/username/password plus a mailProperties map (auth, starttls). POST it as the
# 'smtpServer' JSON string to /xapi/notifications. That endpoint may reply 500 while still applying the
# change, so don't fail on it — verify by reading it back.
echo ">> Configuring SMTP -> $SMTP_HOST:$SMTP_PORT as $SMTP_USER (auth=$SMTP_AUTH starttls=$SMTP_STARTTLS)"
inner=$(printf '{"hostname":"%s","port":%s,"protocol":"%s","username":"%s","password":"%s","mailProperties":{"mail.smtp.auth":"%s","mail.smtp.starttls.enable":"%s"}}' \
  "$SMTP_HOST" "$SMTP_PORT" "$SMTP_PROTOCOL" "$(json_escape "$SMTP_USER")" "$(json_escape "$SMTP_PASS")" "$SMTP_AUTH" "$SMTP_STARTTLS")
body="{\"smtpServer\": \"$(json_escape "$inner")\"}"
printf '%s' "$body" | curl -sS -u "$AUTH" -X POST -H "Content-Type: application/json" "$BASE_URL/xapi/notifications" -d @- >/dev/null 2>&1 || true

if curl -fsS -u "$AUTH" "$BASE_URL/xapi/notifications/smtp" 2>/dev/null | grep -q "\"$SMTP_HOST\""; then
  echo "  OK: smtp -> $SMTP_HOST:$SMTP_PORT (username=$SMTP_USER, auth=$SMTP_AUTH, starttls=$SMTP_STARTTLS)"
else
  echo "  WARN: smtp config not confirmed — check $BASE_URL/xapi/notifications/smtp" >&2
fi
