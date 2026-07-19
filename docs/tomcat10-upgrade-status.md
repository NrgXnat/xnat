# XNAT → Jakarta / Tomcat 10 Upgrade — Step Status

Status tracker for the staged real port (see the full plan and
[`phase0-compile-migration-summary.md`](phase0-compile-migration-summary.md) /
[`tomcat9-deploy-stack.md`](tomcat9-deploy-stack.md) for detail). Branch: `feature/turbine-5x`.

**Last updated:** 2026-07-19

**Legend:** 🟢 Complete · 🟡 In progress · ⚪ Open

**Rollup:** Phase 0a 🟢 code + runtime verified (known-state fixture + goldens) · Phase 0b 🟢 code +
boot + deploy + email/render verification + template semantics/encoding (0b-21..24 all closed),
🟡 remaining screen breadth + REST smoke run + cross-version diff · Phase 1 ⚪ not started, but
Spring Security 6 prep (1-2) landed early and Torque (1-5) downgraded to likely-delete.

Test harness: `docs/tools/build-known-state.sh` (REST fixture: 3 users / 5 projects / subjects /
MR-CT-PET sessions / files / stored searches / project config, Mailpit SMTP sink on :8025) +
`docker/golden-capture.sh` (normalize-and-diff goldens in `docs/goldens/`, local-only) +
`docs/data-rest-endpoint-reference.md(.json)` + REST smoke spec drafted in `xnat-test-automation`
(`s1600-rest-migration-smoke`, registered in `playwright.config.ts`).

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
| 0a-8 | `SecureResource` shim: `post/put/delete`→`handleX`, `getAllowedMethods` (405), variant negotiation | 🟢 | incl. **no-variant** `post/put/delete` bridge (prearchive delete/move/rebuild 405 fix) |
| 0a-9 | `XnatServerResourceFinder` (no-arg Finder ctor) | 🟢 | |
| 0a-10 | Disposition API migration | 🟢 | typed `Disposition` + `CacheDirective` emission (see 0a-17) |
| 0a-11 | `getFileItem`→`getPart` / `DiskFileItem`→`DiskPart` | 🟢 | upload exercised via known-state builder |
| 0a-12 | Runtime verify `/data` GET in json / xml / html (content negotiation) | 🟢 | golden captures: projects/subjects/experiments × json/xml/html + session detail |
| 0a-13 | Runtime verify `/data` POST/PUT with XML body | 🟢 | `SearchResource.handlePost`: YUI Connect prepends the URL query to the POST body → form-wrapped XML. Fixed via `extractSearchXml()` (+ unit test). Fixes the Subjects tab "Failed to create search results." |
| 0a-14 | Runtime verify zip/tar download (`ZipRepresentation` Disposition) | 🟢 | file download golden capture; Content-Disposition emits via typed API |
| 0a-15 | Runtime verify multipart upload (`RestletFileUpload`) | 🟢 | inbody + file up/download exercised by known-state builder |
| 0a-16 | Restore **both** Restlet 1.1 router defaults in `createInboundRoot()`: `MODE_STARTS_WITH` matching **and** `MODE_BEST_MATCH` routing | 🟢 | 2.x defaults (`EQUALS`+`FIRST_MATCH`) broke trailing-path URIs (`getRemainingPart()`: file by name, catalog subpaths) and, with `STARTS_WITH` alone, misrouted writes to first-attached prefix routes |
| 0a-17 | Response headers: `org.restlet.http.headers` must be `Series<Header>` (was `Form`) — CCE→500 *after* body commit; `Cache-Control`/`Content-Disposition` are STANDARD_HEADERS (silently dropped from raw Series) → routed through `getCacheDirectives()` / typed `Disposition` + `applyDisposition()` in the bridges | 🟢 | Restlet errors log via JUL → Tomcat `localhost.<date>.log`, NOT the app console |

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
| 0b-20 | `DefaultHomepageTargetValve` (restore `template.homepage` default) | 🟢 | + `default.jsp` root redirect. Corrected to the exact 2.3.3 `TemplateSessionValidator` condition (`!hasScreen() && empty template`, **no action check**) — message-only action paths (e.g. QuickSearch no-match) otherwise 500 with "Couldn't map Template null" |

### Templates (~1,038 `.vm`)
| # | Step | Status | Notes |
|---|------|:------:|-------|
| 0b-21 | `$page.addAttribute` → `$page.addBodyAttribute` | 🟢 | 5 templates found/fixed so far |
| 0b-22 | Sweep `$velocityCount`/`$velocityHasNext` → `$foreach.*` | 🟢 | **full static sweep done**: 44 occurrences / 18 templates → `$foreach.count` (all innermost-loop, no counter-config overrides → semantics-preserving); `$velocityHasNext` 0 hits. Month dropdowns + search tabs verified in browser |
| 0b-23 | Hyphen-in-identifier + `#if` null-check (`directive.if.empty_check`) semantics | 🟢 | hyphen-identifiers: static sweep found **0**. `#if`: Velocity 2.x defaults `empty_check=true` (empty string/collection, zero number → falsy) vs 1.7 (any non-null, non-false object → truthy); ~2,000 bare-ref `#if($x)` sites in ~600 templates → per-site audit rejected, set `services.VelocityService.directive.if.empty_check=false` in `TurbineResources.properties`. `TurbineBootTest` evaluates a probe through the service's actual engine to pin it |
| 0b-24 | Non-UTF-8 template encoding check | 🟢 | all 1,038 `.vm` decode as valid UTF-8 (`iconv` sweep) — safe under Velocity 2.x's UTF-8 default (1.7 defaulted ISO-8859-1) |

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
| 0b-34 | `docker/golden-capture.sh` (golden-master capture/check) | 🟢 | baselines captured against the known-state fixture; all checks passing. Goldens are local-only (`docs/goldens/` gitignored). NB `GOLDEN_DIR` default is cwd-relative — run from the repo root, or captures land elsewhere (a 2026-07-08 run from `docker/` put them in `docker/docs/goldens/`; relocated) |
| 0b-35 | Exercise email render path (`AdminUtils` templates) | 🟢 | verified end-to-end via Mailpit: `ReportIssue` (`populateVmTemplate`→`VelocityUtils.render`, both text+html templates, zero unrendered `$refs`) + `XDATForgotLogin` both branches (username-by-email, password-reset incl. `RESET_URL` token). NB pre-existing upstream text/html arg swap in `ReportIssue`, preserved |
| 0b-36 | Exercise item text render (`BaseElement`) | 🟡 | same `populateVmTemplate` mechanism as 0b-35 (validated); `BaseElement` call site itself not triggered |
| 0b-37 | Screen breadth: report / edit / search / PDF / XML raw screens | 🟡 | Index, login, create/edit, delete, QuickSearch (incl. multi-match screen), search-results tabs, edit-form month dropdowns verified. Report/PDF/XML raw still open |
| 0b-38 | Turbine security realm empty (`isAnonymousUser` true) — `$sessionData`/permission pull tools | 🟢 | **Resolved: NOT a bug (vestigial).** All user/permission resolution goes through Spring (`XDAT.getUserDetails()` ← `SecurityContextHolder`); templates call the realm user `data.getUser()` 0 times. No bridge needed — do not wire the realm at 7.0 either |
| 0b-39 | Full `./gradlew build` + test-compile green | 🟢 | |
| 0b-40 | `xnat-rest-tests` regression (REST `/data` + `/xapi`) | 🟡 | S1600 REST smoke spec drafted (dispatch probes, content negotiation, router round-trips) + `rest-migration-smoke` project registered in `playwright.config.ts`; not yet run in CI |
| 0b-41 | Golden-master baseline capture (2.3.3 vs 5.1 on same DB) | ⚪ | goldens currently characterize the migrated build against the fixture; the cross-version diff (develop vs 5.1, same DB) still needs a `develop` build |

## Phase 1 — Atomic Jakarta cutover to Tomcat 10

| # | Step | Status | Notes |
|---|------|:------:|-------|
| 1-1 | Spring 5.3 → 6.x | ⚪ | |
| 1-2 | Spring Security 5.7 → 6.x | 🟡 | **Prep landed early on 5.7 (testable):** `WebSecurityConfigurerAdapter` (removed in 6) → component style: `@Bean SecurityFilterChain` (lambda DSL) + explicit `@Bean AuthenticationManager`; `XnatSecurityExtension` hooks preserved; login/logout verified. **Keep `authorizeRequests`** — XNAT's real rules come from `UpdateSecurityFilterHandlerMethod` (BeanPostProcessor) swapping the `FilterSecurityInterceptor` metadata source (openUrls/adminUrls/requireLogin, live-updatable); `authorizeHttpRequests` builds an `AuthorizationFilter` the post-processor never sees → login redirect loop (tried + reverted). `AuthorizationManager` port is an SS**7** task. Remaining at cutover: version bump, `spring-security-openid` removal, legacy `oauth2` project, `ChannelProcessingFilter` deprecation |
| 1-3 | tomcat-embed 9 → 10 | ⚪ | |
| 1-4 | servlet-api javax → jakarta 5+ | ⚪ | |
| 1-5 | Torque 5 → 6 | ⚪ | **Likely delete instead of upgrade:** zero source references to `org.apache.torque` or `com.workingdogs.village` in xnat-web/xdat — pure classpath ballast from Turbine 2.3.3 (its OM security is already replaced by Fulcrum in-memory, see 0b-17/26). Verify nothing reflects it at runtime, then drop the deps |
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
1. **0b-37** — walk the remaining screen types (report / PDF / XML raw) via the smoke-test checklist;
   0b-36 (`BaseElement` item-text) closes with it. NB: the `empty_check=false` change (0b-23) affects
   rendered screens — spot-check a couple of already-verified screens too.
2. **0b-40** — run the drafted S1600 REST smoke suite against the fixture instance.
3. **0b-41** — cross-version golden diff: stand up `develop` on a copy of the known-state DB, capture,
   diff against the 5.1 goldens.
5. **Phase 1 kickoff** — with 1-2 prepped and 1-5 likely a deletion, the cutover is close to the
   intended shape: namespace (`javax`→`jakarta`) + version bumps + `ext.fileupload` replacement (1-11).
