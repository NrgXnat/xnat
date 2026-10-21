# Migration Test-Coverage Analysis — `/data`, `/REST`, `/app`

Scope: how well the `xnat-test-automation` E2E suite covers the server surface changed by the
Tomcat-10/Jakarta framework migration (Restlet 1.1→2.5, Turbine 2.3→5.1, Velocity 1.7→2.x). Companion to
[phase0-compile-migration-summary.md](phase0-compile-migration-summary.md) and
[tomcat10-upgrade-status.md](tomcat10-upgrade-status.md).

Source analysed: `xnat-test-automation` (Playwright, ported from Katalon). Date: 2026-07-07.

## Method

- Enumerated the REST surface from `xnat-web/.../restlet/XNATApplication.java`: **181 route attachments**,
  **91 unique resource classes**, **79 `SecureResource`-family subclasses**.
- Enumerated what the suite actually calls: **619 endpoint call-sites** built as `` `${config.baseURL}/…` ``
  template literals through `tests/utils/` fetch helpers (`fetchWithRetry`, `rest-resource-creation`,
  `general-rest-requests`) plus browser `/app` navigations.
- Cross-referenced verbs, path families, and per-endpoint coverage.

## What the suite is

A **UI E2E suite**, not an API suite: 392 specs across 99 areas, 46 page objects. It drives the browser
through workflows and uses a `tests/utils/` REST helper layer for **setup/teardown and actions the UI can't
automate** (e.g. the qooxdoo prearchive move modal). Every spec carries `requirement`/`specification`
traceability annotations; the repo has a per-suite `Documentation/gap-analysis/` set.

The migration surface is therefore hit two ways: **directly** via REST helpers, **indirectly** via the browser.

## Coverage by migration layer

| Layer | Migration phase | Suite call-sites | Verdict |
|---|---|---|---|
| `/data` + `/REST` (Restlet) | **0a** | **387** direct, 96 distinct paths | **Strong** |
| `/app` (Turbine/Velocity) | **0b** | 58 explicit navs + *all* page objects | **Broad but indirect** |
| `/xapi` (Spring MVC) | Phase 1 only | 168 | Out of scope for Phase 0 |

**Verb distribution (`/data`+`/REST`):** 87 PUT, 68 GET, 65 POST, 45 DELETE, 2 HEAD. The write paths — where
the migration bit us — are exercised heavily. Setup helpers fail the test on non-2xx, so a 405/500 regression
on a **covered** endpoint is caught, not silently passed. These act as de-facto contract tests.

## Migration-critical flows that ARE covered

- **Prearchive delete/move/rebuild** — `PrearchivePage` clicks the real Delete/Archive/Review buttons
  (`PrearchivePage.ts:333/340/349`); move/rebuild also driven via REST. So the no-variant **405 fix** has live
  coverage.
- **`SearchResource` POST** (form-wrapped XML fix) — `/REST/search/elements`, saved searches, data listings.
- Import (`/data/services/import`), project/subject/experiment CRUD, resource file upload/download,
  anon-script config (`/data/config/edit/projects/{}/image/dicom/script`), and the Spring-Security login
  redirect (every spec logs in).

## Gaps — migration-affected endpoints with **zero** coverage

Restlet resources (Phase-0a surface) the suite never calls, by verb or UI — the class where the no-variant
405 could still hide, because they are **API-only with no UI trigger**:

- `/data/automation/*` (16 routes: scripts, runners, scriptVersions, workflows, handlers, templates)
- `/data/workflows`, `/data/pars`, `/data/status/{id}`
- `/data/scanners`, `/data/scan_types`
- `/data/services/`: `dicomdump`, `ecatdump`, `move-files`, `validate-archive`, `refresh/catalog`, `tokens/*`

Secondary limitations:
- **No content-negotiation matrix** — raw `?format=xml|csv` representations (Restlet variant + Turbine/Velocity
  raw-screen paths) are thinly exercised.
- **Skips:** 63 `test.skip` + 19 `.skip()` across ~50 spec files.
- REST coverage is **incidental to UI setup** — it tracks "what workflows need," not "what the API exposes."

## Risk read

The suite is a strong safety net for Phase 0 on the **interactive** surface — it would catch the regressions
already hit (prearchive 405, search POST, login redirect). The residual blind spot is precisely the bug
class we fixed: **a write endpoint that declares no variants and has no UI path** would return 405 undetected.

## Recommendation — REST smoke suite (drafted)

A small, side-effect-free REST smoke closes the gap. Drafted at:

> `xnat-test-automation/tests/s1600-rest-migration-smoke/t1600.1.spec.ts`

Design: probe each endpoint with its **own supported verb** and assert the two migration failure signatures
never occur — **405** (no-variant dispatch regression) and **5xx** (Restlet/Turbine/Velocity dispatch/render
blow-up). No data mutation:
- Reads via GET on the API-only families above.
- No-variant 405 guard via **DELETE against a nonexistent target** (dispatch runs before the existence check,
  so pre-fix→405 / post-fix→404; nothing is touched).
- Prearchive batch **POST with empty body** (zero sessions → no-op).
- **Content-negotiation matrix** — `GET /data/projects?format=json|xml|csv|html`, asserting each renders.
- PUT omitted deliberately (create-on-PUT side effects; already covered by ~87 functional-suite PUTs).

Run (after registering a `rest-migration-smoke` project with `testMatch: ['**/s1600-*/**/*.spec.ts']` in
`playwright.config.ts`):

```bash
BASE_URL=http://localhost:8080 ADMIN_USERNAME=admin ADMIN_PASSWORD=… \
  npx playwright test --project=rest-migration-smoke
```

Optional follow-on: expand to per-resource-class coverage (all 79 `SecureResource` subclasses) rather than the
representative gap set.
