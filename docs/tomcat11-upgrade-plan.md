# XNAT → Tomcat 11 Upgrade — Plan

Follow-on to the Tomcat 10 / Jakarta cutover (see
[`tomcat10-upgrade-status.md`](tomcat10-upgrade-status.md)). Drafted 2026-07-20, before the
Tomcat 10 branch (`feature/jakarta-cutover`) has merged; sequencing assumes it lands first.

**Status: A-1 scout complete (2026-07-20).** Container itself is a non-event; the one real risk
the scout found is **A-5's parameter-parsing throw**, and it is sharper and more specific than
the migration guide implied — see the A-1/A-5 rows below. Rest of Track A not started.

**Update 2026-09-16: first real-box deployment.** The jakarta WAR (`f35c55964d`) + the 22-plugin set is running on
**Apache Tomcat 11.0.26** on `dave-tc11` (Ubuntu 24.04, Java 21, ActiveMQ **Artemis** 2.40 broker), against the
box's existing 1.10 database. Boots clean; the A-4 connector setting is in place and verified. Details in the
[deployment log](#dave-tc11-deployment-log-2026-09-16) at the end. This is a bare-metal/CI-box path — A-7's
compose/Dockerfile flip is still to do.

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
| A-4 | **`maxParameterCount` 10,000 → 1,000**: measure XNAT's largest form posts (site-config save, user edit, search forms) and set the connector value explicitly in `server.xml`/Dockerfile if any exceed ~800 | 🟡 **Scouted 2026-07-20 — real, but easy.** POST-body probe (500/1500 form fields) confirmed: T10.1 accepts 1,500 params (200), T11 rejects (500) at the documented 1,000 default. Fix is a one-line connector attribute (`maxParameterCount="10000"` or whatever XNAT's largest real form needs) — no code change. Still need the actual max-fields measurement across site-config/user-edit/search forms before setting the number. **Deployed 2026-09-16 on dave-tc11 with `maxParameterCount="10000"`** on the 8080 connector; re-ran the probe on the real box: **1,500-param POST → 200** (500-param control → 200). Two facts from that box worth knowing: the CI-templated Tomcat **9** `server.xml` already sets `maxParameterCount="1000"` explicitly — so CI has been running at T11's limit all along and nothing exceeded it in practice; and stock 11.0.26 `server.xml` does not set it, so a bare install silently gets 1,000 |
| A-5 | **`getParameter()` now throws on parse failure**: audit Turbine parameter parsing (fulcrum-parser), Restlet form handling, and `XDATAjaxServlet` for paths that previously tolerated malformed bodies; add a malformed-body probe to S1600 | 🟡 **Scouted 2026-07-20 — sharper than the guide implied, and it's the same mechanism as A-4.** Both the malformed-percent-encoding probe and the too-many-params probe throw the identical `org.apache.tomcat.util.http.InvalidParameterException` from `Parameters.processParameters` (`Parameters.java:433` decode / `:425` count) — confirmed via the container's `localhost.<date>.log` stack traces (not in `docker logs`; Restlet/servlet exceptions there log via JUL). **The trigger path is Restlet's own bridge code**, not XNAT's: `org.restlet.ext.servlet.internal.ServletCall.getRequestEntity()` calls `HttpServletRequest.getParameterMap()` while building the request `Entity` for *every* request through `XNATRestletServlet` (i.e. all of `/data/*` and `/REST/*`), which is what turns a previously-tolerated malformed or oversized query string/body into an unhandled 500 for the entire REST surface, not just isolated form posts. (The call passes through Spring Security's `StrictHttpFirewall$StrictFirewalledRequest.getParameterMap()` on the way, but that's a passthrough wrapper — not the source.) Because both failure modes share one root cause, A-4's connector fix (raise `maxParameterCount`) closes the count case for free; the decode-failure case still needs either a Restlet-layer error handler that maps this exception to a clean 4xx, or upstream Tomcat/Restlet guidance — evaluate both before committing. Add both probes (malformed encoding, oversized param count) to S1600 as permanent regression guards once the fix lands |
| A-6 | Cookie quote-handling and byte→char strictness: covered by the harness; watch login/session cookies in the scout | |
| A-7 | Flip compose/Dockerfile default to `tomcat:11.0-jdk21-temurin`; goldens re-baseline if headers shift | Same mechanics as the 10.1 flip (1-13). **Real-box path done first (2026-09-16):** dave-tc11 runs 11.0.26 from a verified Apache tarball, not the container image — see the deployment log. Compose flip still open |
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
   *(2026-09-16: the A-4 connector setting and a full boot on 11.0.26 are proven on a real box — dave-tc11 —
   ahead of the catalog bumps; A-2/A-3 remain the gating code changes.)*
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

---

## dave-tc11 deployment log (2026-09-16)

First XNAT-on-Tomcat-11 outside a container. Box: `dave-tc11`, Ubuntu 24.04.5, Java 21.0.12, Postgres with an
existing **1.10.0** `xnat` DB (reused, not wiped), **ActiveMQ Artemis 2.40.0** on 61616 (`artemis.service`, OpenWire
via `activemq-openwire-legacy-5.19.0`) — a broker the Tomcat 10 work never exercised.

**What went in.**
- **Tomcat 11.0.26**, fresh from `dlcdn.apache.org`, SHA-512 verified against Apache's digest locally *and* on the box.
  Installed as `/home/xnat/tomcat-11`; the pre-staged 11.0.25 kept as `tomcat-11.0.25.bak`. Stock `webapps/`
  (ROOT, docs, examples, manager, host-manager) removed. `conf/server.xml`: only the 8080 connector edited —
  `connectionTimeout="20000" redirectPort="8443"` (as the CI 9 template) plus **`maxParameterCount="10000"`** (A-4).
  `context.xml` left stock (the `<Manager>` diff vs 9 is commentary — both commented out, Tomcat inverted the default
  wording). `bin/setenv.sh` copied verbatim from the 9 tree; it pins `CATALINA_HOME="/home/xnat/tomcat"`, the symlink,
  so it is version-portable.
- **WAR:** `xnat-web-1.11.0-SNAPSHOT.war`, 254,596,031 bytes, clean-built from **`f35c55964d`**. *Correction, same
  day:* the published WAR **does** exist on jfrog — `org/nrg/xnat/web/xnat-web/1.11.0-SNAPSHOT/`, 254,596,153 bytes,
  manifest `Implementation-Sha: f35c55964d`, `Build-Number: Manual` — and was missed by a probe that only checked the
  `web` library-jar path (tracker 1-38). The rebuild was swapped for the **published bytes** the same afternoon
  (16:59): downloaded on the box, sha1 `02910f09cfeb574d7cc2336cbb3da6d9990b1666` matching Artifactory's checksum
  header, `buildInfo` now reporting the artifact's own `buildDate` (Sep 10 18:54 UTC) and `buildNumber: Manual`.
  Same boot profile — 0 failure signatures, 35 init tasks, 302/200/200. The rebuild is kept on the box as
  `/home/david/ROOT.war.rebuild-<stamp>`.
- **Plugins:** the 22 jakarta jars. The 9 javax jars moved aside to `plugins.bak-javax-<stamp>` (moved, never overlaid).

**Cutover.** This box uses the newer CI layout — `tomcat-{9,10,11}` side by side and `/home/xnat/tomcat` a **symlink**
that `tomcat.service` follows (`ExecStart=/home/xnat/tomcat/bin/startup.sh`). Cutover was `systemctl stop tomcat`,
`ln -sfn /home/xnat/tomcat-11 /home/xnat/tomcat`, start. **Rollback is the same `ln -sfn` back to `tomcat-9`** plus
restoring the plugins backup; the 9 tree and its `ROOT.war` are untouched.

**Result.**
| check | result |
|---|---|
| `org.apache.catalina.util.ServerInfo` on the tree | Apache Tomcat/11.0.26 |
| `ps` | `catalina.home=/home/xnat/tomcat`, `--add-opens` flags present |
| `Deployment of … ROOT.war has finished` | **30,800 ms** (the healthy ~30 s figure; ~6.5 s means the context died) |
| `AbstractMethodError` / `SEVERE` / `FATAL` / `NoClassDefFoundError` / `ClassNotFoundException` / `BeanCreationException` / `startup failed` | **0** each |
| XNAT init tasks | **35** completed, incl. **`Update the user authentication table`** — the 1-20 tell, i.e. the Artemis broker acks the jakarta 6.2.7 client's sends |
| `GET /` · `/app/template/Login.vm` · `/xapi/siteConfig/buildInfo` | 302 · **200** · **200** (`1.11.0-SNAPSHOT`, `f35c55964d`, `feature/jakarta-cutover`) |
| A-4 probe: 1,500 `x-www-form-urlencoded` params → `/data/JSESSION` | **200** (500-param control 200); on the 1,000 default this is a 500 |

**Residual errors, attributed by timestamp against the 16:29:59 cutover** (`/home/xnat/logs/*.log` is shared with the
previous app, so raw counts mislead): `xft.db.DBAction` ×84 — **all pre-cutover**, the old 1.10 app's; JupyterHub cull
×4 post-cutover (403 from the hub — was failing every ~2.5 min before cutover too; box-local token/auth, not
migration); `ElementSecurity` ×3 post-cutover — the same three `xsync:xsync*Data/project` paths seen on every fresh
boot on dave-alldev. Nothing Tomcat 11 introduced.

**Lessons that cost time** (also in the ops runbook memory):
- `bin/version.sh` on a non-live tree reports the **live** version, because `setenv.sh` pins `CATALINA_HOME` to the
  symlink. Verify a tree with `java -cp <tree>/lib/catalina.jar org.apache.catalina.util.ServerInfo`.
- Truncating `catalina.out` as root leaves a root-owned file; `catalina.sh:417` `touch`es it as `xnat` → `Permission
  denied` → exit 2 → systemd rate-limits after 5 attempts. `rm` it, `daemon-reload`, `reset-failed`, start.
- The `strings | grep javax/servlet` jar check **over-reports**: `pipeline_engine_ui` carries 81 refs in unregistered
  legacy `plexiviewer/servlet/*` classes and `xsync` 3 in shaded commons-logging — byte-identical class sets to what
  boots clean on dave-alldev. The fatal case is a class Tomcat/Spring will *instantiate* (a `WebApplicationInitializer`
  or a registered servlet), not any javax string. Discriminate by searching the jar for initializers/`META-INF/services`
  and diffing the sorted `unzip -Z1` class list against a known-good box.

