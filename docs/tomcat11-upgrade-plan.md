# XNAT → Tomcat 11 Upgrade — Plan

Follow-on to the Tomcat 10 / Jakarta cutover (see
[`tomcat10-upgrade-status.md`](tomcat10-upgrade-status.md)). Drafted 2026-07-20, before the
Tomcat 10 branch (`feature/jakarta-cutover`) has merged; sequencing assumes it lands first.

**Status: A-1 scout complete (2026-07-20).** Container itself is a non-event; the one real risk
the scout found is **A-5's parameter-parsing throw**, and it is sharper and more specific than
the migration guide implied — see the A-1/A-5 rows below. Rest of Track A not started.

---

## Evidence base

Verified against primary sources on 2026-07-20 (not from memory):

- **Tomcat 11.0 migration guide** (`tomcat.apache.org/migration-11.0.html`): Java 17+ minimum;
  Jakarta EE 11 — Servlet 6.1 / Pages 4.0 / EL 6.0 / WebSocket 2.2 / Authentication 3.1 /
  Annotations 3.0. Notable behavior changes: `ServletRequest.getParameter()` **now throws** on
  parameter-parse failure (previously silent); default connector **`maxParameterCount` reduced
  10,000 → 1,000**; `FailedRequestFilter` removed; cookie quote handling per RFC 6265;
  byte→char conversion failures now throw; SecurityManager support removed.
- **Maven Central availability** (metadata queries): `jakarta.servlet-api` 6.1.0,
  `jakarta.servlet.jsp-api` 4.0.0, `jakarta.el-api` 6.0.1, `org.glassfish.expressly` 6.0.0,
  glassfish JSTL still 3.0.1 (no JSP-4.0 line yet), `tomcat-embed-core` 11.0.24,
  Spring Framework 7.0.8, Spring Security 7.1.0, `org.hibernate.orm:hibernate-core`
  6.6.54.Final / 7.4.5.Final, `commons-fileupload2-jakarta-servlet6` still milestone (2.0.0-M5),
  Restlet 2.7 still milestone (2.7.0-m3).
- **Spring 7.0.8 jar contents** (decisive, checked directly):
  - `spring-orm-7.0.8.jar` contains **only `orm/jpa`** — `orm.hibernate5`
    (`LocalSessionFactoryBean`, `HibernateTransactionManager`) is **gone**. XNAT's
    `AggregatedAnnotationSessionFactoryBean` extends it → **Spring 7 requires the Hibernate 6+
    migration first**.
  - `spring-webmvc-7.0.8.jar` `PathMatchConfigurer` has **no trailing-slash and no
    suffix-pattern methods** — both escape hatches XNAT relies on
    (`WebConfig.configurePathMatch`: `setUseRegisteredSuffixPatternMatch(true)`,
    `setUseTrailingSlashMatch(true)`) are removed.
- **Spring Security 7.1.0 jar contents**: `FilterSecurityInterceptor`, `AntPathRequestMatcher`,
  `ChannelProcessingFilter`, and the `authorizeRequests`-era configurers
  (`ExpressionUrlAuthorizationConfigurer`, `UrlAuthorizationConfigurer`) are **all removed**.
  XNAT's `UpdateSecurityFilterHandlerMethod` (live-updatable openUrls/adminUrls/requireLogin via
  metadata-source swapping on `FilterSecurityInterceptor`) and the `securityChannel` preference
  (via `ChannelProcessingFilter`) have no direct equivalents to migrate onto — they must be
  **redesigned** on `AuthorizationManager` / `redirectToHttps`.

## Strategy — two tracks, container first

The Tomcat 10 experience showed the value of separating container moves from framework moves.
Spring 6.2 runs on Tomcat 11 as a runtime (Servlet 6.1 is backward-compatible with 6.0 APIs), so:

- **Track A — Tomcat 11 on the current framework stack (the "Tomcat 11 migration" proper).**
  Small, low-risk, independently shippable. Bumps the container + EE APIs only.
- **Track B — the EE 11 / Spring 7 platform phase.** Large; contains all the debt the Tomcat 10
  cutover consciously deferred to "SS7/Spring 7 time". Not required to run on Tomcat 11, but
  Tomcat 11 is its natural target container. Gate: XNAT's own support/EOL requirements for
  Spring 6.2 (verify the support timeline at kickoff — not asserted here).

---

## Track A — Tomcat 11.0 on Spring 6.2 / Turbine 7 / Restlet 2.6

| # | Step | Notes |
|---|------|-------|
| A-1 | **Scout** (analog of 1-14): run the current jakarta WAR unmodified on `tomcat:11.0-jdk21-temurin` via `--build-arg TOMCAT_BASE=...`, disposable DB copy, full harness (health, goldens, S1600, Playwright) | 🟢 **Done 2026-07-20.** Boots clean (0 SEVERE/ERROR beyond the expected empty-archive notice), Apache Tomcat/11.0.24 confirmed in-container. Health 8/8, goldens 10/11 (the 1 diff is the same empty-archive 404 every fresh scratch instance shows — not a T11 regression), S1600 24/24, `xnat-web` Playwright 15/15 — all byte-for-byte matching the Tomcat 10.1 baseline. **Surfaced the real finding: see A-5.** Container change itself is a non-event for XNAT |
| A-2 | Catalog bumps: `servlet-api` 6.0.0 → 6.1.0; `jsp-api` 3.1.1 → 4.0.0; `jakarta-el` 5.0.1 → 6.0.1; `expressly` 5.0.0 → 6.0.0; `tomcat-embed` 10.1.x → 11.0.x | All verified available. Turbine 7 already targets servlet-api 6.1 |
| A-3 | JSTL on JSP 4.0: glassfish JSTL has no 4.0 line (3.0.1 latest) — verify runtime compat in the scout | Our `<xnat:import>` tag already replaces the broken `c:import var=` capture, which reduces JSTL surface |
| A-4 | **`maxParameterCount` 10,000 → 1,000**: measure XNAT's largest form posts (site-config save, user edit, search forms) and set the connector value explicitly in `server.xml`/Dockerfile if any exceed ~800 | 🟡 **Scouted 2026-07-20 — real, but easy.** POST-body probe (500/1500 form fields) confirmed: T10.1 accepts 1,500 params (200), T11 rejects (500) at the documented 1,000 default. Fix is a one-line connector attribute (`maxParameterCount="10000"` or whatever XNAT's largest real form needs) — no code change. Still need the actual max-fields measurement across site-config/user-edit/search forms before setting the number |
| A-5 | **`getParameter()` now throws on parse failure**: audit Turbine parameter parsing (fulcrum-parser), Restlet form handling, and `XDATAjaxServlet` for paths that previously tolerated malformed bodies; add a malformed-body probe to S1600 | 🟡 **Scouted 2026-07-20 — sharper than the guide implied, and it's the same mechanism as A-4.** Both the malformed-percent-encoding probe and the too-many-params probe throw the identical `org.apache.tomcat.util.http.InvalidParameterException` from `Parameters.processParameters` (`Parameters.java:433` decode / `:425` count) — confirmed via the container's `localhost.<date>.log` stack traces (not in `docker logs`; Restlet/servlet exceptions there log via JUL). **The trigger path is Restlet's own bridge code**, not XNAT's: `org.restlet.ext.servlet.internal.ServletCall.getRequestEntity()` calls `HttpServletRequest.getParameterMap()` while building the request `Entity` for *every* request through `XNATRestletServlet` (i.e. all of `/data/*` and `/REST/*`), which is what turns a previously-tolerated malformed or oversized query string/body into an unhandled 500 for the entire REST surface, not just isolated form posts. (The call passes through Spring Security's `StrictHttpFirewall$StrictFirewalledRequest.getParameterMap()` on the way, but that's a passthrough wrapper — not the source.) Because both failure modes share one root cause, A-4's connector fix (raise `maxParameterCount`) closes the count case for free; the decode-failure case still needs either a Restlet-layer error handler that maps this exception to a clean 4xx, or upstream Tomcat/Restlet guidance — evaluate both before committing. Add both probes (malformed encoding, oversized param count) to S1600 as permanent regression guards once the fix lands |
| A-6 | Cookie quote-handling and byte→char strictness: covered by the harness; watch login/session cookies in the scout | |
| A-7 | Flip compose/Dockerfile default to `tomcat:11.0-jdk21-temurin`; goldens re-baseline if headers shift | Same mechanics as the 10.1 flip (1-13) |
| A-8 | Verification: boot test, goldens, S1600 24/24, Playwright 15/15, cross-version diff (t10 vs t11 on same DB — reuse the WAR-swap + pg_dump bracket procedure) | The harness is the asset; all of it transfers unchanged |

Estimated shape: comparable to the 1-14 + 1-13 work — days, not weeks, if the scout is clean.

## Track B — Spring 7 / Spring Security 7 / Hibernate 6 platform phase

Ordered by dependency; B-1 is the long pole and is prerequisite to B-2.

| # | Step | Notes |
|---|------|-------|
| B-1 | **Hibernate 5.6-jakarta → 6.6.x (evaluate 7.4.x)**: rework `AggregatedAnnotationSessionFactoryBean`/`orm.hibernate5` wiring to `orm.jpa` or native Hibernate 6 bootstrap; HQL/criteria changes; dialect + ID-generation semantics; `vladmihalcea hibernate-types` → hypersistence-utils; envers; jcache region factory | **Required by Spring 7** (orm.hibernate5 removed — verified from jar). Do it ON Spring 6.2 first (6.2 supports both), fully testable before the Spring bump — same staged-port philosophy as Phase 0 |
| B-2 | Spring Framework 6.2 → 7.0.x | After B-1. Also removes our two `PathMatchConfigurer` escape hatches (verified): see B-3/B-4 |
| B-3 | **Trailing-slash cleanup**: fix XNAT JS URL builders that emit `/xapi/...{id}/` (e.g. `dicomScpManager.js` `scpUrl()`), then drop `setUseTrailingSlashMatch(true)` | The Spring 6 shim was explicitly temporary; grep-able, finite list |
| B-4 | **Suffix-pattern replacement**: `setUseRegisteredSuffixPatternMatch` gone — inventory `.json`/format-suffix usage on /xapi and move to `Accept`/`format=` param negotiation | Needs a usage inventory first; may be small |
| B-5 | Spring Security 6.5 → 7.x: **redesign** `UpdateSecurityFilterHandlerMethod` on `AuthorizationManager` (live-updatable open/admin/requireLogin rules — the design constraint that kept us on `authorizeRequests`); replace `ChannelProcessingFilter`/securityChannel pref (evaluate `redirectToHttps` DSL); `AntPathRequestMatcher` → `PathPatternRequestMatcher` everywhere (incl. `DefaultInteractiveAgentDetector`, `SecurityConfig` matchers) | All four legacy classes verified absent from SS 7.1.0 jars. Re-verify the include/forward dispatcher-type default at SS7 (we pin the 5.7 set — keep pinning) |
| B-6 | Deferred-debt sweep that naturally lands here: springdoc-openapi (springfox replacement), logging unification (logback 1.5 / slf4j 2 — tomcat10 tracker 1-16/17), legacy `spring-security-oauth2` jar removal, commons-fileupload2 + Restlet 2.7 when they GA | Watch `commons-fileupload2-jakarta-servlet6` and Restlet 2.7 for GA — both remove standing shims (`servlet-api-javax-legacy` compileOnly hack; `restlet-fileupload` pin note) |
| B-7 | Full verification battery + cross-version golden diff against the Track-A baseline | |

## Risks / open questions

1. **Hibernate 6 migration size is the dominant unknown** — XNAT's XFT layer plus the `xhbm`
   Hibernate entities and prefs/config services all sit on the 5.x native-session API. Scope it
   with a compile-picture spike (same technique as the Phase-1 dependency bumps) before
   committing to a timeline.
2. **`UpdateSecurityFilterHandlerMethod` redesign** is architectural, not mechanical: the
   `AuthorizationManager` model has no mutable metadata source; the live-update requirement
   needs a custom `AuthorizationManager` that consults XNAT preferences at decision time
   (which may actually simplify it — no more filter-swapping).
3. **JSTL has no JSP-4.0 release** — **partially de-risked, re-verify at A-2/A-3.** The A-1 scout
   ran the WAR *unmodified* — confirmed by inspecting `docker-context/xnat.war`, it still packages
   `jakarta.servlet.jsp.jstl-3.0.1`/`jstl-api-3.0.0` and the `xnat:import` tag still compiles
   against `jakarta.servlet.jsp-api:3.1.1` (Track A's catalog bumps, A-2, haven't landed). What
   the scout actually showed: those JSP-3.1-era artifacts run correctly on Tomcat 11's JSP-4.0
   Jasper engine via its backward-compat path — goldens on JSTL-bearing pages (`app-index`,
   `app-quicksearch`) passed byte-for-byte. That's a good signal but doesn't retire the open
   question — A-3 still needs to bump to `jsp-api 4.0.0` and re-verify glassfish JSTL 3.0.1 (still
   the latest on Maven Central as of this writing) against the real JSP-4.0 API, not just the
   JSP-4.0 *runtime*.
4. **Plugin ecosystem**: Track A is invisible to plugins; Track B (SS7) breaks any plugin
   touching `AntPathRequestMatcher`/`authorizeRequests` — plan a `plugin-migration-guide.md`
   section and a deprecation window.
5. Turbine/Fulcrum cadence: Turbine 7.0 targets Servlet 6.1 (its POM), so Track A is aligned;
   no Turbine 8 dependency for either track.

## Suggested sequencing

1. Merge the Tomcat 10 cutover; let it settle (CI flip, plugin feedback).
2. ~~Run A-1 (scout) opportunistically~~ **Done 2026-07-20** — see A-1/A-4/A-5 above.
3. Ship Track A as the "Tomcat 11 migration": A-2 (catalog bumps) → A-3 (re-verify JSTL against
   the real JSP 4.0 API) → the A-4/A-5 fix (raise `maxParameterCount` after measuring XNAT's
   largest form; decide the Restlet-layer handling for the decode-failure case) → A-6/A-7/A-8.
4. Kick off B-1 (Hibernate 6 on Spring 6.2) as its own tracked phase with a compile-picture
   spike; B-2..B-7 follow as one coordinated platform phase, mirroring the Phase-0/Phase-1
   staging that worked for Tomcat 10.

---

## A-1 scout log (2026-07-20)

Raw evidence backing the A-1/A-4/A-5 findings above, run against a disposable `xnat_t11`
database (fixture snapshot restore) and `docker build --build-arg TOMCAT_BASE=tomcat:11.0-jdk21-temurin`,
port 8081, alongside the untouched Tomcat 10.1 stack on 8080:

- Boot: 0 SEVERE/ERROR beyond the expected empty-archive `SystemPathVerification` notice.
  `Apache Tomcat/11.0.24` confirmed via `catalina.<date>.log`.
- Health check 8/8, goldens 10/11 (`data-file-download` 404 — empty archive, not a regression),
  S1600 24/24, `xnat-web/tests/playwright` 15/15 — all matching the Tomcat 10.1 baseline exactly.
- `maxParameterCount`: POST body with 1,500 `x-www-form-urlencoded` fields to `/data/JSESSION` —
  Tomcat 10.1.57 returns 200, Tomcat 11.0.24 returns 500. 500-field POST returns 200 on both
  (under the 1,000 default).
  - *Correction en route*: an initial probe sent the 1,500 params as a query string instead of a
    POST body and got 400 on **both** versions — that was `maxHttpHeaderSize` (the request-line
    length limit), not `maxParameterCount`, coincidentally masking the real difference. The
    corrected POST-body probe is the one that isolates the actual behavior change.
- `getParameter()` throw-on-malformed-input: POST body `field=%zz%gg&other=%` (invalid
  percent-encoding) to `/data/JSESSION` — Tomcat 10.1.57 returns 200 (decodes tolerantly, ignores
  the bad field), Tomcat 11.0.24 returns 500.
- Both T11 500s trace to the identical exception, read directly from the container's
  `/usr/local/tomcat/logs/localhost.<date>.log` (Restlet/servlet exceptions log there via JUL,
  not to `docker logs`):
  `org.apache.tomcat.util.http.InvalidParameterException` thrown from
  `org.apache.tomcat.util.http.Parameters.processParameters` (`Parameters.java:433` for the
  decode failure, `:425` for the count-exceeded case), reached via
  `Request.getParameterMap()` → `RequestFacade.getParameterMap()` →
  `StrictHttpFirewall$StrictFirewalledRequest.getParameterMap()` (Spring Security, passthrough
  only — not the source) → **`org.restlet.ext.servlet.internal.ServletCall.getRequestEntity()`**
  → `HttpRequest.getEntity()` → `Decoder.beforeHandle()`. The trigger is Restlet's own servlet
  bridge eagerly reading `getParameterMap()` while building the request `Entity` for every call —
  meaning this fires for any request through `XNATRestletServlet` (`/data/*`, `/REST/*`), not
  just XNAT's own form-handling code.
- Teardown: scout container, image, and `xnat_t11` database all removed; confirmed the Tomcat
  10.1 stack on :8080 unaffected throughout (`data/projects?format=json` → 200 after teardown).
