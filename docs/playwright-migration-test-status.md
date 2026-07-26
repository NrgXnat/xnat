# Playwright Migration — Test Status

Running the `xnat-test-automation` Playwright suite (393 specs / ~70 suites) against the jakarta /
Tomcat 10 stack (`feature/jakarta-cutover`), finding and fixing failures. Fixes land in the **XNAT
repo** (migration code, `docker/init-xnat.sh`, `docker-compose.yml`); the test suite is the
develop‑calibrated reference and is **not** modified. Genuine migration behavior‑changes are also
recorded in `tomcat10-upgrade-status.md` and `plugin-migration-guide.md` (behavior‑change catalog).

**Status legend:** ✅ passing · 🔧 failing‑now‑fixed · ❌ failing‑still‑failing · ⚪ not yet run

**Fixture stack:** `:8080` jakarta 1.11.0‑SNAPSHOT · `TZ=America/Chicago` · `userRegistration=false`
(admin approval) · strict `passwordComplexity` · `auditTrailPlugin` loaded. Set up by
`docker/init-xnat.sh`; data by `docker/build-known-state.sh`.

**Fix commits are made after each test is fixed or given up on** (so we can regress). The
per‑test "ref" column cites the commit.

---

## S1001 — User Registration (8)
| Test | Name | Status | Problem → Fix (ref) |
|---|---|---|---|
| T1001.1 | Register user with valid, unique values | ✅ | audit‑trail step needed `auditTrailPlugin` (migrated + installed via docker plugin pipeline) |
| T1001.2 | Unique values for new user required | ✅ | passes |
| T1001.3 | Registration screen field validation | 🔧 | `init-xnat.sh` set `passwordComplexity=^.*$` → weak passwords accepted, no validation msg. Set strict regex matching the suite (`52a67845c`) |
| T1001.4 | Post‑registration screen links | 🔧 | fixture auto‑enabled self‑registered users → "verified‑but‑not‑enabled cannot log in" failed. `userRegistration=false` for admin approval (`ce28be7d4`) |
| T1001.4.1 | Resend email verification from forgot‑login page | ⏭️ skipped | skipped by a test.skip precondition (not run) |
| T1001.5 | SQL‑injection username validation (negative) | ✅ | passes |
| T1001.6 | Username validation for OpenID characters (XNAT‑6912) | ✅ | passes |
| T1001.7 | Hidden registration when admin disables it (XNAT‑6948) | ✅ | passes |

**S1001 status: GREEN** (7 pass, 1 skip, 0 fail).

## S1002 — Forgot Login / Password (4)
| Test | Name | Status | Problem → Fix (ref) |
|---|---|---|---|
| T1002.1 | Forgot username/password + last‑login date/time | 🔧 | (1) **SS6 regression**: `XDAT.loginUser` didn't persist SecurityContext to session → reset link landed on guest (`16c164461`, tracker 1‑20); (2) container ran UTC, suite expects America/Chicago → last‑login assertion off by ~5h (`a42e8550f`) |
| T1002.2 | Invalid data validation | ✅ | passes |
| T1002.2.1 | Forgot‑password password‑match validation | ✅ | passes |
| T1002.4 | Email format validation (negative) | ✅ | passes |

**S1002 status: GREEN** (4 pass, 0 fail).

---

## Suites passed clean (no fixes needed)
Green on first run against the fixture stack — tests listed by number; ✅ all pass.

| Suite | Result | Tests |
|---|---|---|
| S1003 — User Login | ✅ 8/8 | T1003.1 (login success/fail), .2 (required fields/expired pw), .3 (nonexistent/unverified), .4 (whitespace neg), .5 (auth response codes XNAT‑6998), .6 (auth header format XNAT‑6634), .7 (provider dropdown), .8 (forgot‑pw hidden w/o localdb) |
| S1006 — Security Bar Element | ✅ 1/1 | T1006.1 (username/renew/logout nav + help icon + session auto‑logout) |
| S1007 — Top Menu (partial) | ✅ 1/2 | T1007.2 (removing project access reflected in My Projects) — T1007.1 ❌ (see deferred) |

---

## Still failing / deferred

> **Fixture reset applied (baseline):** wiped and rebuilt to a clean known state — 3 users / 5
> projects / 10 subjects / 26 sessions / 25 files / 2 stored searches (`down -v` → fresh boot →
> `init-xnat.sh` → `build-known-state.sh`). Plugin + TZ intact. This is the baseline for all runs below.

### ⭐ ROOT CAUSE — Search‑UI cluster: `POST /REST/search` → 422 — 🔧 FIXED (`4bb23dca7`, tracker 1‑21)
**Gated ~26 tests** (all of S1004 quick‑search + S1008 advanced‑search, and parts of S1009/S1025). The
search UI (`dataTableSearch.js`, YUI Connect) POSTed the stored‑search bundle XML as the
`application/x-www-form-urlencoded` **body** of `/REST/search`. Under Tomcat 10 / Restlet 2.6 the servlet
parameter parser drains the body before `SearchResource.handlePost` runs, and the XML is exposed
**nowhere** — instrumented and proven: `getParameterMap()` = only the 4 query params, `getInputStream()`
`len=0` (EOF), `entity.getText()` = just the 82‑char query string. So the SAX reader got a body starting
with `XNAT_CSRF=…` → `Content is not allowed in prolog` → `422`. This is the **client‑side** face of the
1‑19 www‑form body‑consumption class (1‑19's `bodyOnlyForm` recovers *form‑shaped* params; a **raw‑XML**
body has nothing to recover — it's gone).
**Fix:** post the bundle as `text/xml` so Restlet keeps the raw entity (server already accepts bare XML;
Restlet 1.1 tolerates `text/xml` too). Server‑side defence in depth: `extractSearchXml` now uses a lenient
percent‑decoder (no throw on stray `%`, no `+`→space). **Verified:** t1008.1 PASS, S1008 0/10→4, 0
prolog/422 in the log. Remaining S1008/S1004 failures are a **separate** client‑side data‑table render
issue, not this 422.
**Bug‑class audit:** `ucfa.js:276` (move‑files) + `manageFeatures.js:137,149` (features) POST bodies to
Restlet the same way — flagged for verification when those suites run.

> **Also:** s1005/s1007 fail on interactive `/app` navigation (legacy three‑button landing, top‑menu
> dropdowns) — a separate, smaller `/app`‑nav cluster.

### S1004 — Quick Search Home — 422 cleared, data‑table render remains
The `POST /REST/search` 422 is fixed (`4bb23dca7`) — `/REST/search` returns 200 now. Residual failures are
the **client‑side data‑table (`#dataRows`) render path** (`format=xList` → JS builds `#dataRows`), a
separate cluster. Next: browser‑level dig into the `xList` response + the table‑render JS. *(search backend
green; table‑render JS unfixed)*

### S1008 — Advanced Search (🔧 4/12, 6 fail, 2 skip) — search 422 FIXED
Was 0/10 (all `POST /REST/search` 422). After the `text/xml` fix (`4bb23dca7`): **4 pass, 0 prolog/422 in
the log**. Remaining 6 fail on the **separate** client‑side data‑table (`#dataRows`) render path, not the
422. *(search root cause cleared; residual is the data‑table cluster)*

### S1009 — Quick Search (mixed)
T1009.2 (no‑match), T1009.3 (usability) ✅. T1009.1/.1.1 (quick‑search navigation) still ❌ but **not** the
422 — they now fail earlier in the `a[href="#taskbox"]` **top‑nav / createSubject** step (the `/app`
Turbine screen cluster), before reaching search. *(search 422 no longer the blocker here)*

### S1005 — Three Button Landing (❌ 1/1) — DEFERRED
T1005.1 (legacy three‑button landing navigation): sets the site landing to the legacy home screen,
then navigates Enroll‑Subject / Upload / Review buttons — times out (10s) on a landing element.
Same `/app`‑navigation cluster. *(no fix yet)*

### S1007 — Top Menu (❌ T1007.1; ✅ T1007.2)
T1007.1 (member user cannot see Administer tab; navigate other menus): times out (15s) in the
dropdown‑menu hover/click helper as `automationMember`. T1007.2 (my‑projects list) passes. *(no fix yet)*

---

### S1010‑S1013 batch (project/subject/report)
- **S1010 New/Edit Project — mostly ✅ (5/7, 1 skip):** T1010.1/.2/.3/.5/.7 pass (project CRUD + validation); T1010.4 ❌ (project‑report new study/investigator), T1010.6 ⏭️.
- **S1011 Project Report — ❌ 0/1** (T1011.1).
- **S1012 New/Edit Subject — ❌ 0/4** (all: add/edit, added‑screen, edit‑mgmt, enroll validation). Subject‑screen cluster.
- **S1013 Subject Report — ❌ 3/4** (T1013.1/.2/.3 fail; T1013.4 ✅ dup‑label sharing).
> **Subject/report cluster** (S1011/S1012/S1013 + T1010.4): subject enroll/edit + report screens time out. Separate `/app` Turbine/Velocity screen cluster — deferred for a focused pass.

### S1014‑S1017 batch
- **S1014 Prearchive — 2/5 (2 skip):** T1014.4 (no stray chars) ✅, T1014.5 (routing preserved) ✅; T1014.1/.2/.3 ❌ (details/actions/find — data‑table).
- **S1015 Request Help — ❌ 0/1.**
- **S1016 Download Session Images — 2/6:** T1016.2 (error validation) ✅, T1016.6 (numeric‑zero id) ✅; T1016.1/.3/.4/.5 ❌ (download via XML/ZIP, selection tables, role gating — session‑selection data‑table).
- **S1017 Email Data — ❌ 0/1** (six screens incl. subject/session/report — depends on the failing screen cluster).

> **DOMINANT CLUSTER (breadth‑pass conclusion):** most interactive‑UI failures across S1004/8/9 (search),
> S1011/12/13 (subject/report), S1014 (prearchive), S1016 (downloads) share the **client‑side data‑table
> (`#dataRows`) render path** — the JS fetches results (via `/REST/search` or `/data`) and builds the table.
> The confirmed root for the search path is **`POST /REST/search` → 422** (see ⭐ above). Fixing that
> (and re‑checking the `/data`‑fed listings) is the highest‑value next action — likely flips dozens of tests.
> Core REST/CRUD/auth/validation is green; the break is the interactive Turbine/Velocity + JS data tables.

## Suites not yet run
s1009 s1010 s1011 s1012 s1013 s1014 s1015 s1016 s1017 s1019 s1020 s1021 s1022 s1023 s1024 s1025 s1026
s1027 s1028 s1029 s1030 s1031 s1033 s1034 s1035 s1036 s1037 s1038 s1039 s1040 s1041 s1042 s1043 s1045
s1046 s1101 s1102 s1103 s1104 s1105 s1106 s1107 s1108 s1109 s1110 s1111 s1112 s1113 s1114 s1116 s1117
s1118 (+ dps/dpt/others)

> Many later suites depend on fixture DATA (projects/subjects/sessions/DICOM). Before running those,
> run `docker/build-known-state.sh`. Data‑missing failures are recorded as ❌ with reason
> "needs fixture data" (not a migration bug) unless the test uploads its own data.

## Running notes
- Run one suite at a time: `npm run test:sNNNN` (from `xnat-test-automation`, Node ≥18 —
  `~/.nvm/versions/node/v24.18.0/bin`). Email suites (~2 min/test) are slow.
- Diagnosis order for a failure: (a) migration code regression → fix in XNAT source; (b) fixture
  config → `init-xnat.sh`/`docker-compose`; (c) missing data → `build-known-state.sh`; (d) genuine
  test/feature gap in this fork → record as ❌ with the reason.
