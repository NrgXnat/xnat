# Email Verification Setup (XNAT send + Playwright read)

How to get XNAT's new-user **email verification** working end to end so the
`xnat-test-automation` Playwright suite (S1001 registration tests) can register a user,
receive the verification email, and read it back.

There are **two independent sides**, and both must work:

```
  ┌─────────────┐  1. sends verification email     ┌──────────────┐
  │    XNAT     │ ───────────────────────────────▶ │  Gmail SMTP  │
  │ (this repo) │    via real SMTP (Gmail relay)   │ smtp.gmail…  │
  └─────────────┘                                  └──────┬───────┘
        ▲                                                 │ 2. delivers to
        │ init-xnat.sh                                    ▼    automation+<user>@xnatworks.io
        │ configures SMTP                            ┌──────────────────────────┐
                                                     │  automation@xnatworks.io │  (one real mailbox;
  ┌─────────────┐   3. reads the inbox via Gmail API │  Gmail / Workspace inbox │   plus-addressing routes
  │  Playwright  │ ◀──────────────────────────────── │                          │   automation+X@ → here)
  │ test harness │    (OAuth: credentials + tokens)  └──────────────────────────┘
  └─────────────┘
```

- **Side A — XNAT sends:** XNAT must be pointed at a *real* SMTP server (we use Gmail as a relay
  via a Google **App Password**). A dev sink like Mailpit only *captures* locally and never reaches
  Gmail, so it can't satisfy the test's read.
- **Side B — Playwright reads:** the suite reads the `automation@xnatworks.io` inbox through the
  **Gmail API** (OAuth), so it needs that mailbox to exist, the Gmail API enabled, and a one-time
  authorization producing `tokens.json`.

The verification email itself carries a **single-use tokenized link**
(`/app/template/VerifyEmail.vm?a=<alias>&s=<secret>`, 24 h expiry); clicking it flips the account
`verified=0 → 1`. S1001 tests only assert the email was *received*; they don't click the link.

---

## Prerequisites

| Side | Need |
|------|------|
| XNAT | A running XNAT stack (`docker compose up --build`); admin login (`admin/admin` by default). |
| SMTP | A Google account you control (here `david@xnatworks.io`) with **2-Step Verification ON** and a **16-char App Password**. |
| Mailbox | A **real** `automation@xnatworks.io` Gmail/Workspace mailbox that actually receives mail. |
| Google Cloud | An OAuth **client** (`credentials.json` / client-secret JSON) and the **Gmail API enabled** in that project. |
| Node | **Node ≥ 22** (v24 tested). The repo `.nvmrc` wants 20; an old nvm default (v14/v17) will fail. |

---

## Side A — XNAT: send real email

### A1. Boot XNAT uninitialized, then init + configure

```bash
cd /Volumes/Offload/projects/xnatworks/rtv/xnat
./docker/stage-war.sh                 # build the WAR (if code changed)
docker compose up --build             # boots UNINITIALIZED (like develop) — no auto-init
```

### A2. Get a Google App Password (one-time, per Google account)

App Passwords require 2-Step Verification on the account.

1. Google Account → **Security** → **2-Step Verification** (enable if needed).
2. Google Account → **Security** → **App passwords** → create one (any name).
3. Google shows a 16-char password like `abcd efgh ijkl mnop`. The spaces are cosmetic — you can
   include or strip them. **Store it as a secret; never commit it.**

### A3. Point XNAT at Gmail SMTP with `init-xnat.sh`

`docker/init-xnat.sh` completes first-install setup, applies base site config
(`userRegistration=true`, relaxed `passwordComplexity`, …), and configures SMTP. Defaults target
Gmail; supply the App Password via `SMTP_PASS`:

```bash
SMTP_USER='david@xnatworks.io' \
SMTP_PASS='abcd efgh ijkl mnop' \
./docker/init-xnat.sh
```

**Keep the secret out of the tracked script.** `init-xnat.sh` sources a **git-ignored** local
overrides file first — `docker/init-xnat.local.sh` (or point `INIT_XNAT_ENV` at one) — so you can
persist your creds without hard-coding them or retyping each run:

```bash
cat > docker/init-xnat.local.sh <<'EOF'
SMTP_USER='you@gmail.com'
SMTP_PASS='abcd efgh ijkl mnop'   # a Google App Password
EOF
./docker/init-xnat.sh          # picks up the overrides automatically
```

That path is in `.gitignore` (`docker/*.local.sh`); never commit it.

Overridable env (defaults shown): `SMTP_HOST=smtp.gmail.com`, `SMTP_PORT=587`,
`SMTP_USER=automation@xnatworks.io`, `SMTP_PROTOCOL=smtp`, `SMTP_AUTH=true`, `SMTP_STARTTLS=true`.
If `SMTP_AUTH=true` and `SMTP_PASS` is empty, the script **skips** mail config (warns) rather than
half-configuring it.

Under the hood it POSTs the full `SmtpServer` (host/port/protocol/username/password +
`mail.smtp.auth`/`mail.smtp.starttls.enable`) to `/xapi/notifications`. (The
`/xapi/notifications/smtp` form endpoint mis-binds its Properties arg and 500s — the script uses the
JSON-body endpoint, which may itself reply 500 while still applying the change, so it verifies by
reading `/xapi/notifications/smtp` back.)

### A4. Verify XNAT is actually sending

Register a probe user with a plus-addressed recipient and confirm the success screen:

```bash
U=http://localhost:8080; A=admin:admin
jar=$(mktemp); csrf=$(curl -s -c "$jar" "$U/app/template/Register.vm" \
  | grep -oE "csrfToken = '[0-9a-f-]{36}'" | grep -oE "[0-9a-f-]{36}" | head -1)
curl -s -b "$jar" -c "$jar" -o /tmp/reg.html -X POST "$U/app/action/XDATRegisterUser" \
  --data-urlencode "xdat:user.login=probe" \
  --data-urlencode "xdat:user.primary_password=Test123!xyz" \
  --data-urlencode "xdat:user.firstname=Pr" --data-urlencode "xdat:user.lastname=Obe" \
  --data-urlencode "xdat:user.email=automation+probe@xnatworks.io" \
  --data-urlencode "phone=555" --data-urlencode "lab=L" \
  --data-urlencode "XNAT_CSRF=$csrf" >/dev/null
grep -oiE 'Email Verification Sent|unable to send' /tmp/reg.html   # want: Email Verification Sent
curl -s -o /dev/null -X DELETE -u $A "$U/xapi/users/probe"; rm -f "$jar"
```

- `Email Verification Sent` → the send succeeded (no `MailException` thrown).
- `unable to send you the verification email` → SMTP failed; recheck host/port/App Password/auth.

**From-address nuance:** XNAT's From is the site `adminEmail`. If that differs from the
authenticated SMTP account, Gmail **rewrites the From to the SMTP account** (e.g. `david@`) and still
delivers — harmless, since the test searches by *recipient + subject*, not From. (Just don't be
surprised if the received message shows `From: david@…`.)

---

## Side B — Playwright: read the automation inbox

Repo: `/Volumes/Offload/projects/xnatworks/xnat-test-automation`.

### B1. The address scheme — plus-addressing on ONE mailbox

`getEmailAddressWithSuffix(username)` builds the registrant's email from `SITE_ADMIN_EMAIL` by
inserting `+username`:

```
SITE_ADMIN_EMAIL = automation@xnatworks.io
→ registered user's email = automation+<username>@xnatworks.io
```

Every test uses a unique `+username` variant, but **all mail lands in the one `automation@xnatworks.io`
inbox**. That mailbox must be a real Gmail/Workspace account that receives mail.

### B2. Google Cloud setup (one-time)

1. **OAuth client** — a Desktop/loopback OAuth client in a Google Cloud project. Download its
   **client-secret JSON** (this is `credentials.json`). Its redirect is `http://localhost`.
2. **Enable the Gmail API** in that **same project**:
   `https://console.developers.google.com/apis/api/gmail.googleapis.com/overview?project=<PROJECT_NUMBER>`
   → **Enable**, then wait ~1–5 min. (If you skip this, authorization saves tokens but the first API
   call 403s: *"Gmail API has not been used in project … before or it is disabled."*)
3. If the OAuth app is in **Testing** mode, add the mailbox account to the app's **Test users**.

### B3. `.env`

```dotenv
BASE_URL=http://localhost:8080
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin

SITE_ADMIN_EMAIL=automation@xnatworks.io       # the real inbox (+ plus-addressing base)
AUTOMATION_EMAIL_USERNAME=automation

GMAIL_CREDENTIALS_JSON=/Users/drm/Documents/Creds/client_secret_<...>.json
GMAIL_TOKENS_PATH=/Users/drm/Documents/Creds/tokens

SKIP_EMAIL_VERIFICATIONS=false                  # true = bypass Gmail reads entirely (see B7)
```

### B4. Node version

The suite needs Node ≥ 22. If your shell's nvm default is old:

```bash
export PATH="$HOME/.nvm/versions/node/v24.18.0/bin:$PATH"
node --version   # v24.x
```

### B5. Authorize the Gmail API (one-time per machine) — **read this whole step first**

```bash
cd /Volumes/Offload/projects/xnatworks/xnat-test-automation
npm run authorize:gmail
```

It prints an auth URL. Open it, sign in **as the account that receives `automation@xnatworks.io`
mail**, and consent. Then come the two non-obvious parts:

- **⚠️ "This site can't be reached / localhost refused to connect" is EXPECTED.** The OAuth client's
  redirect is a bare `http://localhost` and the script does **not** run a listener to catch it. The
  authorization code is delivered **in the browser's address bar, not on the page.**

  Look at the URL bar. It reads like:
  ```
  http://localhost/?iss=https://accounts.google.com&code=4/0AXEQx...long...&scope=https://www.googleapis.com/auth/gmail.readonly
  ```
  **Copy the `code` value — everything between `code=` and the next `&`.** Extract it exactly:
  ```bash
  printf '%s\n' '<PASTE THE FULL http://localhost/?... URL>' | sed -n 's/.*[?&]code=\([^&]*\).*/\1/p'
  ```
  **URL-decode it:** if you see `%2F`, replace with `/` (the code starts `4/0A…`). Other `%XX` too.

- **⚠️ Paste it FAST.** Auth codes are **single-use and short-lived**. If you dawdle you get
  `invalid_grant: Bad Request` on the token exchange — that means the code expired/was used, **not**
  a config problem. Just re-run `npm run authorize:gmail`, redo the browser step, and paste a fresh
  code within a minute.

On success it prints `✅ Tokens saved to: …/tokens/tokens.json` and the connected email. **Confirm
that address is the automation mailbox** — if it's a different account, the suite reads the wrong
inbox and every email search comes up empty even though XNAT sent the mail.

### B6. Harness quirk — the vestigial `StoredCredential` check

`tests/utils/gmail-service.ts` `authorize()` still requires a legacy Katalon `StoredCredential`
file *before* it reads `tokens.json` (which is what it actually uses). With only `tokens.json` you
get: `Gmail tokens not found at: …/tokens/StoredCredential`.

- **Quick unblock:** `touch "$GMAIL_TOKENS_PATH/StoredCredential"` (empty placeholder satisfies the
  existence check; `tokens.json` is still what's read).
- **Proper fix:** delete the `StoredCredential` existence check in `gmail-service.ts` — it should
  only need `tokens.json`.

### B7. Escape hatch — run without Gmail

To exercise the UI flow without a real inbox read, set `SKIP_EMAIL_VERIFICATIONS=true`. Then
`verifyEmailExists()` returns success without contacting Gmail. You still need XNAT's SMTP to *send*
successfully (so "Email Verification Sent" renders) — e.g. point SMTP at a local **Mailpit**:
`SMTP_HOST=<host> SMTP_PORT=<port> SMTP_AUTH=false SMTP_STARTTLS=false ./docker/init-xnat.sh`.

---

## Run it

```bash
cd /Volumes/Offload/projects/xnatworks/xnat-test-automation
export PATH="$HOME/.nvm/versions/node/v24.18.0/bin:$PATH"
npx playwright test tests/s1001-user-registration/t1001.1.spec.ts --project=parallel-tests --reporter=list
```

A working run logs (from the Gmail side):
```
[GMAIL] query: ... to:automation+<user>@xnatworks.io subject:Verify Your Email Address For XNAT
[GMAIL] Found 1 email(s) that match
[EMAIL] Email found and verified with expected content
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Registration shows **"unable to send you the verification email"** | XNAT SMTP not (correctly) configured — bad host/port/App Password, or auth/TLS off | Re-run `init-xnat.sh` with correct `SMTP_*`; confirm `GET /xapi/notifications/smtp` shows Gmail |
| Test times out on **"Email Verification Sent"** | XNAT couldn't send (as above), or SMTP still points at a non-delivering sink | Fix SMTP (Side A); "Email Verification Sent" must render first |
| `tokens.json not found at: …/StoredCredential` | Vestigial Katalon check in `gmail-service.ts` | `touch $GMAIL_TOKENS_PATH/StoredCredential` (or remove the check — B6) |
| `invalid_grant: Bad Request` at code exchange | Auth code expired / already used (single-use, short-lived) | Re-run `authorize:gmail`, grab a fresh code from the **address bar**, paste within a minute |
| `Gmail API has not been used in project … or it is disabled` (403) | Gmail API not enabled in the OAuth client's project | Enable it in Cloud Console (B2), wait a few min, re-run `authorize:gmail` |
| `localhost refused to connect` after consent | Expected — no local listener for the `http://localhost` redirect | The code is in the **URL bar**, not the page; copy it from there (B5) |
| Auth succeeds but email search finds **0** | Wrong account authorized, or mail not actually delivered to `automation@` | Confirm `authorize:gmail`'s printed email == the automation mailbox; check the inbox manually |
| `npm ...` / `npx ...` fails with a Node syntax error | Old Node (nvm default v14/v17) | Use Node ≥ 22 (B4) |

---

## Known limitation — the audit-trail step (not email)

`T1001.1`'s **final** step ("Verify registration entry in audit trail") hits
`/REST/services/summary/audit/admin` + `WorkflowSummaryTable.vm`. That is the **core XNAT audit REST
API (XNAT-6775, 1.10)** — **not a plugin** — and it **404s on this branch** because the feature
landed in upstream `NrgXnat/xnat` develop *after* this fork's base (`aa9529f3d`, 2026-07-01). The
cloud dev lines run a newer XNAT that has it.

So against a local stack, email verification (registration → send → Gmail read) fully works, but the
audit step can't pass without pulling a post-`aa9529f3d` XNAT. Dispositions: skip/soft-fail that step
locally, or accept `T1001.1` as "mail-verified; audit step requires a newer XNAT base."
