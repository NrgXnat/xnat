# XNAT → Jakarta / Tomcat 10 Upgrade — Step Status

Status tracker for the staged real port (see the full plan and
[`phase0-compile-migration-summary.md`](phase0-compile-migration-summary.md) /
[`tomcat9-deploy-stack.md`](tomcat9-deploy-stack.md) for detail). Branch: `feature/turbine-5x`.

**Last updated:** 2026-07-06

**Legend:** 🟢 Complete · 🟡 In progress · ⚪ Open

**Rollup:** Phase 0a 🟢 code complete, 🟡 runtime verification · Phase 0b 🟢 code + boot + deploy,
🟡 breadth/render verification · Phase 1 ⚪ not started.

Strategy: do the framework API rewrites on javax / Tomcat 9 first (Phase 0, fully testable), then one
atomic Jakarta cutover to Tomcat 10 (Phase 1, namespace + DI-version only).

---

## Phase 0a — Restlet 1.1.10 → 2.5.2 (javax, Tomcat 9)

| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0a-1 | Catalog: replace 4 `restlet-*`/`com.noelios` aliases → `org.restlet:2.5.2` (+ `ext.servlet`, `ext.fileupload`) | 🟢 | |
| 0a-2 | Update `build.gradle` refs (xdat, xnat-web, parent) | 🟢 | |
| 0a-3 | `Resource` → `ServerResource` across ~45 `SecureResource` subclasses | 🟢 | `SecureResource` shim |
| 0a-4 | Repackage imports `com.noelios.restlet.ext.servlet.ServerServlet` → `org.restlet.ext.servlet` | 🟢 | |
| 0a-5 | Replace internal `com.noelios.restlet.http.*` usage | 🟢 | |
| 0a-6 | Re-add explicit `MODE_STARTS_WITH` / query matching (`XNATApplication`, `XNATVirtualHost`, `XNATRestletFactory`) | 🟢 | routing works at runtime |
| 0a-7 | Update `XnatWebAppInitializer` servlet wiring | 🟢 | |
| 0a-8 | `SecureResource` shim: `post/put/delete`→`handleX`, `getAllowedMethods` (405), variant negotiation | 🟢 | verified for GET/DELETE at runtime |
| 0a-9 | `XnatServerResourceFinder` (no-arg Finder ctor) | 🟢 | |
| 0a-10 | Disposition API migration | 🟢 | |
| 0a-11 | `getFileItem`→`getPart` / `DiskFileItem`→`DiskPart` | 🟢 | compiles; upload not yet exercised |
| 0a-12 | Runtime verify `/data` GET in json / xml / html (content negotiation) | 🟡 | config-resource GET + 404 path confirmed; full negotiation matrix pending |
| 0a-13 | Runtime verify `/data` POST/PUT with XML body | 🟢 | `SearchResource.handlePost`: YUI Connect prepends the URL query to the POST body → form-wrapped XML. Fixed via `extractSearchXml()` (+ unit test). Fixes the Subjects tab "Failed to create search results." |
| 0a-14 | Runtime verify zip/tar download (`ZipRepresentation` Disposition) | ⚪ | |
| 0a-15 | Runtime verify multipart upload (`RestletFileUpload`) | ⚪ | |

## Phase 0b — Turbine 2.3.3 → 5.1 + Velocity 1.7 → 2.4.1 (javax, Tomcat 9)

### Dependencies / catalog
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-1 | `turbine:turbine` → `org.apache.turbine:turbine:5.1` | 🟢 | |
| 0b-2 | `org.apache.velocity:velocity` → `velocity-engine-core:2.x` | 🟢 | |
| 0b-3 | Bump velocity-tools | 🟢 | |
| 0b-4 | Torque 3.3 → 5.0 | 🟢 | |
| 0b-5 | Add Fulcrum deps (parser, intake, yaafi, security-memory) | 🟢 | |
| 0b-6 | `commons-lang3` 3.11 → 3.17 (Velocity `MethodMap` introspection) | 🟢 | |

### Code
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-7 | `CustomClasspathResourceLoader`: `getResourceStream`→`getResourceReader` + `ExtProperties` | 🟢 | |
| 0b-8 | Render via `VelocityService` not the `Velocity` singleton (`AdminUtils`, `BaseElement`, `UserGroupManager`, `VelocityUtils.render`) | 🟢 | email/text wired, not yet exercised (see 0b-35/36) |
| 0b-9 | Route all 20 `Velocity.resourceExists()` sites → `TurbineUtils.resourceExists()` | 🟢 | fixed screen/report/search dispatch app-wide |
| 0b-10 | 197 screen classes: `RunData`→`PipelineData`, reconcile `VelocitySecureScreen`/`RawScreen`/`VelocityAction` | 🟢 | compiles + renders |
| 0b-11 | Port `RestletRunData` to the 5.x RunData service | 🟢 | |
| 0b-12 | `TurbineScreenRepresentation` RunData bridge (`RunDataService.getRunData("restlet",…)`) | 🟢 | |

### Config
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-13 | Rewrite `TurbineResources.properties` to the 4.0+ service-container format | 🟢 | |
| 0b-14 | `roleConfiguration.xml` (7 Fulcrum security roles) | 🟢 | |
| 0b-15 | `componentConfiguration.xml` (parser `urlCaseFolding=lower`, managers) | 🟢 | |
| 0b-16 | `turbine-classic-pipeline.xml` (valve pipeline) | 🟢 | order corrected (0b-27) + homepage valve (0b-20) |
| 0b-17 | Disable vestigial `DBSecurityService` / `turbine-om.properties` include | 🟢 | |
| 0b-18 | `$ui` UIManager → `UITool` + declare `UIService` | 🟢 | |
| 0b-19 | `TemplateService.default.extension=vm` | 🟢 | fixed "Page not found: Default" |
| 0b-20 | `DefaultHomepageTargetValve` (restore `template.homepage` default) | 🟢 | + `default.jsp` root redirect |

### Templates (~1,038 `.vm`)
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-21 | `$page.addAttribute` → `$page.addBodyAttribute` | 🟢 | 5 templates found/fixed so far |
| 0b-22 | Sweep `$velocityCount`/`$velocityHasNext` → `$foreach.*` | 🟡 | fix-on-sight; no full sweep yet |
| 0b-23 | Hyphen-in-identifier + `#if` null-check (`directive.if.empty_check`) semantics | ⚪ | not audited; may need `parser.allow_hyphen_in_identifiers=true` |
| 0b-24 | Non-UTF-8 template encoding check | ⚪ | |

### Runtime bring-up (Tomcat 9 deploy)
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-25 | `fulcrum-yaafi` dep (YAAFI container) | 🟢 | |
| 0b-26 | Fulcrum in-memory security (PullService `UserManager` per render) | 🟢 | |
| 0b-27 | Pipeline order: `DetermineRedirectRequestedValve` after `ExecutePageValve` | 🟢 | fixed all redirect-after-save actions |
| 0b-28 | Parser `urlCaseFolding` none → lower | 🟢 | fixed path-info/param lookups app-wide |
| 0b-29 | Logback `ConsoleAppender` (XNAT logs → docker stdout) | 🟢 | makes XNAT's Logback side of the dual-logging setup visible; full unification deferred to 1-16 (see decision note) |
| 0b-30 | Site-root / homepage default | 🟢 | see 0b-20 |

### Verification
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-31 | `TurbineBootTest` (service container + pipeline + security + resourceExists + homepage valve) | 🟢 | green |
| 0b-32 | Tomcat 9 deploy stack (compose: Tomcat 9 + Postgres + ActiveMQ) | 🟢 | |
| 0b-33 | `docker/health-check.sh` | 🟢 | |
| 0b-34 | `docker/golden-capture.sh` (golden-master capture/check) | 🟢 | baselines not yet captured |
| 0b-35 | Exercise email render path (`AdminUtils` templates) | 🟡 | wired; not triggered |
| 0b-36 | Exercise item text render (`BaseElement`) | 🟡 | wired; not triggered |
| 0b-37 | Screen breadth: report / edit / search / PDF / XML raw screens | 🟡 | Index, login, create/edit, delete flows verified |
| 0b-38 | Turbine security realm empty (`isAnonymousUser` true) — `$sessionData`/permission pull tools | ⚪ | bridge Turbine-user↔Spring-user only if a template needs it |
| 0b-39 | Full `./gradlew build` + test-compile green | 🟢 | |
| 0b-40 | `xnat-rest-tests` regression (REST `/data` + `/xapi`) | ⚪ | after smoke tests pass |
| 0b-41 | Golden-master baseline capture (2.3.3 vs 5.1 on same DB) | ⚪ | needs `develop` build on same DB |

## Phase 1 — Atomic Jakarta cutover to Tomcat 10

| # | Step | Status | Notes |
|---|------|:------:|-------|
| 1-1 | Spring 5.3 → 6.x | ⚪ | |
| 1-2 | Spring Security 5.7 → 6.x | ⚪ | |
| 1-3 | tomcat-embed 9 → 10 | ⚪ | |
| 1-4 | servlet-api javax → jakarta 5+ | ⚪ | |
| 1-5 | Torque 5 → 6 | ⚪ | |
| 1-6 | Restlet 2.5.2 → 2.6.0 (Jakarta) | ⚪ | |
| 1-7 | Turbine 5.1 → 7.0 (Jakarta build) | ⚪ | requires Java 17+, Torque 6 |
| 1-8 | Pin Velocity 2.4.1 (confirm Turbine 7 tolerates override) | ⚪ | |
| 1-9 | `javax.*` → `jakarta.*` across ~73 servlet source files | ⚪ | consider OpenRewrite `java.migrate.jakarta` |
| 1-10 | `web.xml` / Spring XML+Java config / JSP / TLD namespace migration | ⚪ | incl. the new `default.jsp` |
| 1-11 | Remove Restlet `ext.fileupload` (dropped after 2.5.2) → commons-fileupload direct (~5 files) | ⚪ | |
| 1-12 | Hand-audit reflection / string-built class names OpenRewrite can't see | ⚪ | |
| 1-13 | Flip local dev + CI to Tomcat 10 / Java 17+ | ⚪ | |
| 1-14 | (Optional) `tomcat-jakartaee-migration` smoke-test of the Phase-0 WAR on Tomcat 10 | ⚪ | disposable milestone |
| 1-15 | Playwright suite (`xnat-web/tests/playwright/`) on Tomcat 10 | ⚪ | |
| 1-16 | Unify logging on Logback (the log4j2↔Logback reconciliation) | ⚪ | **Deferred here deliberately — not doable on 5.1** (see note). At 7.0 Turbine logs via `java.lang.System.Logger`; add `org.slf4j:slf4j-jdk-platform-logging` and drop the log4j2 impls |
| 1-17 | Consume the upstream Turbine log4j-decoupling PR | ⚪ | PR prepared against Turbine 7.0 trunk (`../turbine-core`, branch `feature/decouple-log4j-core-from-transitive-deps`); marks log4j2 impls `<optional>`. If merged by 7.0 release, no exclusion needed; else exclude `log4j-core`/`log4j-jpl` in XNAT's build. **XNAT does not depend on this PR merging** — the PR only changes the upstream default. Because 7.0 logs via `System.Logger` (no hard cast to `core.LoggerContext`, unlike 5.1), log4j-core is a swappable backend, so 1-16 can exclude it in XNAT's own build regardless of the PR. The PR's value is ecosystem-wide (default posture, transitive fan-out, Log4Shell surface, expressing upstream intent), not a gate for us |

### Decision — logging stays dual on 5.1; unify at 7.0
Turbine **5.1** hard-uses log4j-core in code: `Turbine.configureLogging()` calls
`LogManager.getContext(false)` and **casts to `org.apache.logging.log4j.core.LoggerContext`** at init, so
log4j-core cannot be removed (Turbine won't start) and cannot be swapped for `log4j-to-slf4j` (the cast
would fail). A single Logback backend on 5.1 would require backporting the entire 7.0 `System.Logger`
rewrite into a Turbine fork + maintaining it — not worth it for a temporary phase. So the upstream
"mark log4j impls optional" PR (a 3-line pom change) applies **only to 7.0**, where the code already logs
through `System.Logger`. On 5.1 we accept dual logging: Turbine → log4j2 → stdout (`WEB-INF/conf/log4j2.xml`),
XNAT → Logback → stdout (0b-29 console appender); both visible in `docker logs`, both tunable. Reconciliation
is therefore a Phase-1 task (1-16/1-17), not Phase-0b.

---

## Known blockers / next up
1. **0b-35/36** — exercise the newly-wired email + text render paths end-to-end.
2. **0b-37** — walk the remaining screen types (report / PDF / XML raw) via the smoke-test checklist.
3. **0b-14/15** — `/data` zip/tar download + multipart upload still unverified (Restlet 2.x Disposition + `RestletFileUpload`).
4. **0b-22/23** — decide whether to do a full `.vm` audit now or continue fix-on-sight.
