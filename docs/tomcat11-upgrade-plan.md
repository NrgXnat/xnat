# XNAT → Tomcat 11 Upgrade — Plan

Follow-on to the Tomcat 10 / Jakarta cutover (see
[`tomcat10-upgrade-status.md`](tomcat10-upgrade-status.md)). Drafted 2026-07-20, before the
Tomcat 10 branch (`feature/jakarta-cutover`) has merged; sequencing assumes it lands first.

**Status: PLANNED — no work started.**

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
| A-1 | **Scout** (analog of 1-14): run the current jakarta WAR unmodified on `tomcat:11.0-jdk21-temurin` via `--build-arg TOMCAT_BASE=...`, disposable DB copy, full harness (health, goldens, S1600, Playwright) | The compose parameterization makes this a ~30-minute experiment; do it FIRST — it will surface most of A-4..A-6 empirically |
| A-2 | Catalog bumps: `servlet-api` 6.0.0 → 6.1.0; `jsp-api` 3.1.1 → 4.0.0; `jakarta-el` 5.0.1 → 6.0.1; `expressly` 5.0.0 → 6.0.0; `tomcat-embed` 10.1.x → 11.0.x | All verified available. Turbine 7 already targets servlet-api 6.1 |
| A-3 | JSTL on JSP 4.0: glassfish JSTL has no 4.0 line (3.0.1 latest) — verify runtime compat in the scout | Our `<xnat:import>` tag already replaces the broken `c:import var=` capture, which reduces JSTL surface |
| A-4 | **`maxParameterCount` 10,000 → 1,000**: measure XNAT's largest form posts (site-config save, user edit, search forms) and set the connector value explicitly in `server.xml`/Dockerfile if any exceed ~800 | Silent-truncation-turned-error class; measure, don't guess |
| A-5 | **`getParameter()` now throws on parse failure**: audit Turbine parameter parsing (fulcrum-parser), Restlet form handling, and `XDATAjaxServlet` for paths that previously tolerated malformed bodies; add a malformed-body probe to S1600 | Behavior change most likely to bite at runtime, not compile |
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
3. **JSTL has no JSP-4.0 release** — if the scout shows breakage, options are the Wasp/glassfish
   successor line (check at kickoff) or widening `<xnat:import>`-style local tags.
4. **Plugin ecosystem**: Track A is invisible to plugins; Track B (SS7) breaks any plugin
   touching `AntPathRequestMatcher`/`authorizeRequests` — plan a `plugin-migration-guide.md`
   section and a deprecation window.
5. Turbine/Fulcrum cadence: Turbine 7.0 targets Servlet 6.1 (its POM), so Track A is aligned;
   no Turbine 8 dependency for either track.

## Suggested sequencing

1. Merge the Tomcat 10 cutover; let it settle (CI flip, plugin feedback).
2. Run A-1 (scout) opportunistically — it is nearly free and informs everything.
3. Ship Track A as the "Tomcat 11 migration".
4. Kick off B-1 (Hibernate 6 on Spring 6.2) as its own tracked phase with a compile-picture
   spike; B-2..B-7 follow as one coordinated platform phase, mirroring the Phase-0/Phase-1
   staging that worked for Tomcat 10.
