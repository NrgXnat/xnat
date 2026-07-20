# XNAT → Jakarta / Tomcat 10 Upgrade — Step Status

Status tracker for the staged real port (see the full plan and
[`phase0-compile-migration-summary.md`](phase0-compile-migration-summary.md) /
[`tomcat9-deploy-stack.md`](tomcat9-deploy-stack.md) for detail). Branch: `feature/turbine-5x`.

**Last updated:** 2026-07-19 (evening — Jakarta cutover boots + verified on Tomcat 10)

**Legend:** 🟢 Complete · 🟡 In progress · ⚪ Open

**Rollup:** Phase 0a 🟢 · Phase 0b 🟢 · **Phase 1: cutover LANDED and runtime-verified** on branch
`feature/jakarta-cutover` — full test suite green, local stack switched to Tomcat 10.1.57 /
Turbine 7.0 / Restlet 2.6 / Spring 6. Remaining 🟡/⚪ rows are the tail: hardening + breadth
(1-12 reflection audit, 1-15 Playwright suite), merge-time work (CI flip in 1-13, oauth2/
`AntPathRequestMatcher` in 1-2), and quality follow-ups (1-16/17 logging, springdoc).

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
| 0b-36 | Exercise item text render (`BaseElement`) | 🟢 | **Closed: no live call site in core.** `BaseElement.output(template)` has zero callers in xnat-web/xdat (no Java site passes a template name; no `.vm` calls `.output(`) — plugin-facing API only. Its mechanism (`TurbineUtils.resourceExists` + `VelocityUtils.render` via the service) is exactly the 0b-35-verified email path + the boot-test engine probe |
| 0b-37 | Screen breadth: report / edit / search / PDF / XML raw screens | 🟢 | Index, login, create/edit, delete, QuickSearch (incl. multi-match), search-results tabs, month dropdowns (earlier). 2026-07-19: **report** screens 200 + error-free for projectData / subjectData / mr / ct / petSessionData (`DisplayItemAction`; only stray `$ref` is the pre-existing upstream `HeaderIncludes.vm` `current_uri` else-branch calling `$om.getSubjectId()` on a project — identical under Velocity 1.7). **XML raw** (`XMLScreen` via `DisplayXMLAction` + `XDATActionRouter/xdataction/xml`, and `XMLSearch?data_type&id`) all return well-formed `text/xml`, both routes byte-identical. **PDF** (`XDATScreen_pdf`, MR-only — the sole `pdf/*_fo.xsl`): fails `NoClassDefFoundError org/apache/batik` — **pre-existing on `develop`** (monorepo commit `ba8c58d1f` excludes `batik` from fop, but FOP 0.20.5 `Driver.<init>` class-loads it unconditionally); not a migration regression — Turbine 5.1's `handleException` renders the error screen properly. Un-exclude `batik` if PDF is ever wanted |
| 0b-38 | Turbine security realm empty (`isAnonymousUser` true) — `$sessionData`/permission pull tools | 🟢 | **Resolved: NOT a bug (vestigial).** All user/permission resolution goes through Spring (`XDAT.getUserDetails()` ← `SecurityContextHolder`); templates call the realm user `data.getUser()` 0 times. No bridge needed — do not wire the realm at 7.0 either |
| 0b-39 | Full `./gradlew build` + test-compile green | 🟢 | |
| 0b-40 | `xnat-rest-tests` regression (REST `/data` + `/xapi`) | 🟢 | **24/24 passed (2026-07-19)** against the fixture instance: 12 GET dispatch probes (prearchive/search/scanners/scan_types/investigators/pars/automation×4/status/dicomdump), 3 no-op DELETE 405-guards, 3 prearchive batch POSTs, 4-format content negotiation, + both router round-trips (BEST_MATCH nested write, STARTS_WITH file-by-name up/download; throwaway project cleaned up, goldens still 11/11). Run **standalone**: `tests/s1600-rest-migration-smoke/playwright.config.ts` (new) — the repo's main config wires globalSetup/Teardown that create site prereqs + force "site default state", not fixture-safe (a first attempt's teardown did enable sitewide anonymization + reset session-timeout/admin-email/auth settings on the fixture — benign for goldens/REST, noted). Also fixed a latent `*/`-in-block-comment syntax error in the draft spec. Node ≥18 required (`/usr/local/bin/node` v23; repo `.nvmrc` wants 20 — nvm here only has 14/17). CI wiring still open but out of Phase-0b scope |
| 0b-41 | Golden-master baseline capture (2.3.3 vs 5.1 on same DB) | 🟢 | **Done 2026-07-19 — no semantic divergence.** Built `develop` (Restlet 1.1.10 / Turbine 2.3.3 / Velocity 1.7) from a worktree, WAR-swapped onto the same stack + same fixture DB (pg_dump bracketed), captured into `docs/goldens-develop/` (local, gitignored). Verdict: all 7 `/data` endpoints + buildinfo **byte-identical**; `app-index`/`app-quicksearch` **whitespace-identical** (`diff -w` = 0; ~110 leading-indentation lines each from Velocity 2 space gobbling); `data-jsession` differs only in Content-Type charset label (`text/plain` default ISO-8859-1 → UTF-8 in Restlet 2.x, ASCII body identical). Stack restored to 5.1 + snapshot DB; goldens re-baselined post-restore (pg_restore changes physical row order and the list queries have no ORDER BY) — 11/11. Stale `data-version.txt` golden (dropped endpoint) deleted |

## Phase 1 — Atomic Jakarta cutover to Tomcat 10

| # | Step | Status | Notes |
|---|------|:------:|-------|
| 1-1 | Spring 5.3 → 6.x | 🟢 | 6.2.19, **runtime-verified on Tomcat 10**. Boot required javac `-parameters` (Spring 6.1 removed `LocalVariableTableParameterNameDiscoverer` — aspects + `@RequestParam` binding) |
| 1-2 | Spring Security 5.7 → 6.x | 🟡 | **6.5.11 landed + compiles** (openid removed, test configs component-style, `authenticationIsRequired` visibility). **Runtime-verified on the live stack** (form login/session persistence via explicit `SecurityContextRepository`, logout, Basic auth, `ObjectPostProcessor` package move). Remaining: legacy `spring-security-oauth2` 2.5.2.RELEASE jar decision (classpath-only, zero core source refs — plugin-facing), `AntPathRequestMatcher` removal warnings (SS7 prep). Original prep notes: | **Prep landed early on 5.7 (testable):** `WebSecurityConfigurerAdapter` (removed in 6) → component style: `@Bean SecurityFilterChain` (lambda DSL) + explicit `@Bean AuthenticationManager`; `XnatSecurityExtension` hooks preserved; login/logout verified. **Keep `authorizeRequests`** — XNAT's real rules come from `UpdateSecurityFilterHandlerMethod` (BeanPostProcessor) swapping the `FilterSecurityInterceptor` metadata source (openUrls/adminUrls/requireLogin, live-updatable); `authorizeHttpRequests` builds an `AuthorizationFilter` the post-processor never sees → login redirect loop (tried + reverted). `AuthorizationManager` port is an SS**7** task. Remaining at cutover: version bump, `spring-security-openid` removal, legacy `oauth2` project, `ChannelProcessingFilter` deprecation |
| 1-3 | tomcat-embed 9 → 10 | 🟢 | 10.1.57 (catalog-only; no direct consumers) |
| 1-4 | servlet-api javax → jakarta 5+ | 🟢 | jakarta.servlet-api 6.0.0 via the kept `servlet-javax-servlet-api` alias; javax 3.1 kept **compileOnly in xnat-web only** for commons-fileupload 1.5 `FileUploadBase` deprecated-overload resolution |
| 1-5 | Torque 5 → 6 | 🟢 | **Deleted** (catalog + all decl sites; `org.apache.torque` excluded from Turbine 7). Runtime-reflection check rides the 1-12 audit |
| 1-6 | Restlet 2.5.2 → 2.6.0 (Jakarta) | 🟢 | now on Maven Central; 2.6 removed the WebDAV statuses (207/422/423/424/507) → `XnatWebDavStatus` constants (27 files) |
| 1-7 | Turbine 5.1 → 7.0 (Jakarta build) | 🟢 | 7.0 from Central + Fulcrum 4.0.0. **XNAT's 5.1-format config boots unchanged on 7.0** (`TurbineBootTest` green incl. the `empty_check` probe) and screens render byte-identical on Tomcat 10 |
| 1-8 | Pin Velocity 2.4.1 (confirm Turbine 7 tolerates override) | 🟢 | 2.4.1 wins over Turbine 7's 2.3 transitive; boot probe + byte-identical screen renders confirm |
| 1-9 | `javax.*` → `jakarta.*` across ~73 servlet source files | 🟢 | selective scripted sweep, 315 files (servlet/persistence/validation/mail/jms/inject/activation/el/interceptor/EE-annotation/EE-transaction/xml.bind); JSR-305, JCache, JDK javax.* untouched. Forced couplings: jakarta mail/jms/validation/xml.bind APIs, ActiveMQ 6.2.7, hibernate-validator 8, jakarta.inject-api **2.0.1** (1.0.x still ships javax packages!) |
| 1-10 | `web.xml` / Spring XML+Java config / JSP / TLD namespace migration | 🟢 | web.xml → jakartaee web-app_6_0; JSTL sun URIs → `jakarta.tags.*` (37 files) + glassfish JSTL 3.0.1; `default.jsp` needs nothing (implicit objects only); no Spring XML carried javax |
| 1-11 | Remove Restlet `ext.fileupload` (dropped after 2.5.2) → commons-fileupload direct (~5 files) | 🟢 | XNAT-owned `RestletFileUpload`/`RepresentationContext` adapters (ports of the Apache-2.0 Restlet 2.5.2 classes) over commons-fileupload core — no servlet types; 6 call-site files re-imported |
| 1-12 | Hand-audit reflection / string-built class names OpenRewrite can't see | 🟢 | **Audited 2026-07-19, clean on every vector.** (1) Java: zero EE-javax mentions remain — all hits are JSR-305/JDK/JCache namespaces that stay javax. (2) Runtime resources incl. the packaged WAR's 653 non-class files: only JAXP factory props + a JCache logger name. (3) Full fixture-DB data dump: zero `javax.` strings (no stored class names affected). (4) No composed `"javax"` string fragments. (5) All `Class.forName` feeders trace to those verified sources; Turbine valve/service configs additionally class-load-verified by `TurbineBootTest`. Residual risk is **third-party plugins** only — javax-era plugin jars need their own jakarta migration (see `plugin-migration-guide.md`) |
| 1-13 | Flip local dev + CI to Tomcat 10 / Java 17+ | 🟡 | **Local flipped (2026-07-19):** compose + Dockerfile default to `tomcat:10.1-jdk21-temurin`; jakarta WAR staged in `docker-context/`. Override `TOMCAT_BASE=tomcat:9-jdk21-temurin` for the javax WAR. CI flip remains for merge time. **Switched 2026-07-19**: the local fixture stack now runs the Jakarta build on Tomcat 10.1.57 (health 8/8, S1600 24/24, form login OK, goldens re-baselined 11/11 — file-download byte-identical against the real archive). CI flip remains |
| 1-14 | (Optional) `tomcat-jakartaee-migration` smoke-test of the Phase-0 WAR on Tomcat 10 | 🟢 | **Done 2026-07-19 — converted WAR fully functional on Tomcat 10.1.57.** `jakartaee-migration-1.0.12 -profile=EE` converts the 221MB Phase-0 WAR in 30s, zero warnings. Parallel container (jdk21 base, `xnat_t10` DB copy, fresh archive volume): boots clean in ~20s, health 8/8, **S1600 REST smoke 24/24** (incl. file up/download round-trip through bytecode-converted `ext.fileupload`), report + XML screens render (XML byte-identical to Tomcat 9). Goldens: 7/11 byte-identical; rest explained — `app-*` differ only in the Content-Type charset label (**Tomcat 10/Servlet 6 defaults responses to UTF-8**, was ISO-8859-1 — expect this at the real cutover), buildinfo carries the tool's `-migrated-1.0.12` version stamp, file-download 404 = empty archive volume (also the sole log ERROR, `SystemPathVerification`). Torn down after |
| 1-15 | Playwright suite (`xnat-web/tests/playwright/`) on Tomcat 10 | 🟢 | **15/15 green (2026-07-19).** The suite surfaced three silent cutover regressions in the JSP/XAPI admin surface, all fixed: **(a)** glassfish jakarta JSTL (all versions 2.0-3.0.1) has a defective `c:import var=` capture — its wrapper stream's `flush()` spills the capture into the page and empties the var whenever the included servlet flushes (Spring MVC/Restlet do; static files don't) → replaced all 16 sites with XNAT-owned `<xnat:import>` (`ImportTag` + `WEB-INF/xnat-tags.tld`); **(b)** Spring Security 6 added FORWARD+INCLUDE to the default dispatcher types, and `ChannelProcessingFilter`'s `if (response.isCommitted()) return;` then silently dropped every mid-page include (response commits after the >8K page header) — blanked the entire /page/* SPA family; fixed by restoring the SS 5.7 dispatcher set in `SecurityWebApplicationInitializer` (bisected at runtime via filter-map removal; vanilla Tomcat 10.1 verified NOT at fault); **(c)** Spring 6 disabled trailing-slash URL matching — XNAT JS PUTs like `/xapi/dicomscp/{id}/` 404'd, making admin saves silently no-op; `setUseTrailingSlashMatch(true)` restored in `WebConfig` (deprecated — revisit at Spring 7). Post-fix: goldens 11/11, S1600 24/24 |
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

### Jakarta cutover — runtime verification (2026-07-19, branch `feature/jakarta-cutover`)
Source-ported WAR boots clean on Tomcat 10.1.57 + Turbine 7 + Restlet 2.6 + Spring 6/SS 6.5:
health 8/8 · goldens 7/11 with only the known-cosmetic diffs (charset labels, empty-archive 404) —
**screen bodies byte-identical under Turbine 7** · S1600 24/24 (incl. multipart through the new
fileupload shim) · form login/logout + session persistence + report/XML screens verified.
Boot-blocker fixes, in order hit: javamelody 2.8 (javax listener), javac `-parameters`,
ehcache `jakarta` classifier (javax-JAXB config parser), jakarta EL for validator 8,
SS 6.4 `ObjectPostProcessor` package move, SS6 explicit `SecurityContextRepository` on
`XnatAuthenticationFilter` (form-login session persistence).

**Full `./gradlew test` suite GREEN on the jakarta stack (2026-07-19).** Cutover-era test fixes:
hibernate-validator groupId (org.hibernate → org.hibernate.validator — the old coordinate is an
empty relocation jar at 8.x, which silently disabled Hibernate's AUTO bean-validation!), Jakarta EL 5
(expressly), SS6 `TestingAuthenticationToken` 3-arg ctor (isAuthenticated), `NestedServletException`
removal, and a long-inert null `actionProviders()` stub bean in EventServiceTestConfig that Spring 6's
by-name resolution shortcut suddenly matched once `-parameters` exposed parameter names.

**Remaining Phase-1 follow-ups:** 1-12 reflection audit; 1-15 Playwright suite; 1-16/17 logging
unification (logback 1.5/slf4j 2); legacy `spring-security-oauth2` jar removal decision;
`AntPathRequestMatcher` deprecations (SS7 prep); 1-13 flip compose/CI defaults.

**DONE — springfox → springdoc-openapi (2026-07-20).** Removed the dead springfox 2.9 jars
(`parent` api export, `spawner` — swapped to a direct `swagger-annotations` dep since it was leaning on
springfox to pull it transitively, `xnat-data-builder` runtimeOnly; catalog `swagger=2.9.2` + aliases
dropped, `springdoc=2.8.6` added) and restored the Swagger UI at `/xapi/swagger-ui.html` via
springdoc-openapi 2.8.6. springdoc is Boot-oriented but XNAT is a non-Boot WAR, so `OpenApiConfig`
manually `@Import`s the three config classes (core → web-mvc → ui, ordered so the
`@ConditionalOnMissingBean` back-off resolves as under Boot), supplies the `@ConfigurationProperties`
beans programmatically (no Boot relaxed binding), and provides an `OpenAPI` bean carrying the old
`apiInfo.*` metadata + `/xapi` server base path (the `Docket.pathMapping("/xapi")` equivalent). The 2591
legacy `io.swagger.annotations` (Swagger 1.x) are inert under springdoc — the spec is generated from the
Spring MVC mappings (348 paths documented); a full annotation rewrite to OpenAPI 3 (`@Operation`/
`@Parameter`/…) is deferred as optional doc-fidelity polish. `SiteConfigApi` got an explicit
`@Tag(name = "site-config-api")` so the `page/admin/content.jsp` deep-link
`/xapi/swagger-ui.html#/site-config-api` still resolves (springfox auto-derived that kebab-case group
from the class name; springdoc defaults to the class simple name).

*Servlet-path root cause (the integration wrinkle):* XNAT's DispatcherServlet is mapped at `/xapi/*`
(not root), a manual non-Boot registration springdoc can't discover, and springdoc builds every
generated URL — the `/swagger-ui.html`→`/swagger-ui/index.html` redirect (`SwaggerUiHome`) and the UI's
spec/config URLs (`SwaggerWelcomeWebMvc`) — from context-path + the `spring.mvc.servlet.path` property.
Publishing that property as `/xapi` (via a static `BeanFactoryPostProcessor` so it lands before
springdoc's `@Value` fields resolve) fixes the redirect **and** the spec-fetch URLs with one mechanism.

*Bug found + fixed during verification (`WebConfig`):* the `/xapi/v3/api-docs` spec came back
base64-encoded inside a JSON string. Root cause — `WebConfig.configureMessageConverters` **replaces**
Spring's default converter list, dropping the default `ByteArrayHttpMessageConverter`; springdoc's
`OpenApiWebMvcResource` returns `byte[]` (produces `application/json`), which then fell through to
`MappingJackson2HttpMessageConverter` and was serialized as base64. Fixed by registering
`ByteArrayHttpMessageConverter` first. **Bug class = "byte[] response with no raw byte[] converter →
Jackson base64"; swept all `xnat-web`/`xdat` controllers for `byte[]`/`ResponseEntity<byte[]>` returns —
springdoc's endpoint is the only one, so the fix is general and zero-regression** (health-check 8/8
green: JSON/XML/HTML/text all intact). Verified: swagger-ui.html→index 200, api-docs/swagger-config raw
JSON under `/xapi`, `servers:[/xapi]`, `site-config-api` tag present (11 tagged ops).

**DEFERRED — do not re-investigate: commons-fileupload 1.5 → commons-fileupload2 (2026-07-20).**
Evaluated whether moving to fileupload2 lets us drop the `servlet-api-javax-legacy` (3.1.0) compileOnly
shim in `xnat-web/build.gradle:315-318`. Conclusion: **not worth it now.** The shim is a single
`compileOnly` line, *never packaged* (runtime is already pure jakarta); it exists only so javac can
resolve fileupload 1.5 `FileUploadBase`'s deprecated `javax.HttpServletRequest` overloads, which XNAT
never calls. fileupload2 *would* remove it (fu2-core carries zero servlet types — verified against the
`2.0.0-M5` core jar: package is entirely `org.apache.commons.fileupload2.core`, `RequestContext`-based,
no javax/jakarta servlet imports), **but it is a ground-up API rewrite, not a bump**: `FileUpload` →
abstract `AbstractFileUpload<R,I,F>` (3 type params, 3 abstract methods) forcing a full rewrite of the
custom `RestletFileUpload`/`RepresentationContext` bridge (1-11); `FileUploadBase` and
`DefaultFileItemFactory` deleted (→ `DiskFileItemFactory.builder().get()`, 4 sites); `UploadContext` →
`RequestContext`; `FileItemIterator` → `FileItemInputIterator`; `FileItem.write(File)` →
`write(Path)`; `FileUploadException` repackaged (5 files); `List<FileItem>` → `List<DiskFileItem>`
generics. **Dominant risk: fileupload2 has no GA release** (latest `2.0.0-M5`, a milestone) and this is
the library parsing untrusted multipart upload input (DICOM/zip import). fileupload 1.5 is the
CVE-2023-24998–patched release and runs fine on jakarta via its servlet-agnostic core. Revisit only
when fileupload2 reaches GA *or* an independent forcing function appears (new 1.5 CVE, or
Servlet 6.1/Tomcat 11 breaking the core — the A-1 scout did **not** show this).

## Known blockers / next up
**Phase 0 is complete, and the 1-14 scout run proves the Jakarta runtime works**: the
bytecode-converted Phase-0 WAR is fully functional on Tomcat 10.1.57 (S1600 24/24, health 8/8,
screens render). The remaining Phase-1 risk is therefore the *source-level* port, not the runtime:
version bumps (Spring 6 / Spring Security 6 / tomcat-embed 10 / Restlet 2.6 / Turbine 7.0 /
Torque-delete), `javax`→`jakarta` namespace (~73 servlet files + web.xml/JSP/TLD, 1-9/1-10), and
the `ext.fileupload` replacement (1-11). Known cutover expectations from the scout: Tomcat 10
defaults response charsets to UTF-8 (golden header diffs) and the empty-archive
`SystemPathVerification` error is environmental.
5. **Phase 1 kickoff** — with 1-2 prepped and 1-5 likely a deletion, the cutover is close to the
   intended shape: namespace (`javax`→`jakarta`) + version bumps + `ext.fileupload` replacement (1-11).
