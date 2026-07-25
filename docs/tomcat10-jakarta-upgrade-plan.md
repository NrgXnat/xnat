# XNAT Framework Upgrade → Jakarta EE / Tomcat 10

> Planning document. No code changes are described as done here — this is the agreed approach and the verified facts behind it.

## Context

`CLAUDE.md` frames the work as three framework upgrades — **Restlet 2.6.0**, **Turbine 7.0**, **Velocity 2.4.1**. Investigation showed these are not independent line-item bumps; they are the framework-shaped pieces of one larger objective: **running XNAT on Apache Tomcat 10**.

Tomcat 10 *is* Jakarta EE 9 — it serves only `jakarta.servlet` applications, with no compatibility switch. Tomcat 10 is therefore the forcing function:

- **Turbine 7.0 is the only Jakarta-namespace Turbine**, so Tomcat 10 makes 7.0 mandatory (not a choice vs. 5.1).
- Turbine 7.0 bundles **Velocity 2.x** and requires **Torque 6.0** and **Java 17+**.
- Jakarta servlet forces **Spring 6.x + Spring Security 6.x** (the only versions on `jakarta`).
- **Restlet 2.6.0 ships a Jakarta servlet extension** (`org.restlet.ext.servlet`), so Restlet is compatible and not a blocker.

The three named upgrades are **necessary but not sufficient** for Tomcat 10. The full re-platform — Spring 6, Spring Security 6, Tomcat 10, Torque 6, and a `javax.*`→`jakarta.*` source migration — is **in scope**.

**Chosen strategy: staged real port (with compressed QA).** Do the dangerous framework API rewrites first on the *current* container (javax / Tomcat 9), **smoke-checkpointed** there (boot + render + critical flows), then perform one atomic Jakarta cutover to Tomcat 10 with only namespace + DI-version changes remaining — running the **single full regression at the Tomcat 10 end-state** (not once per phase).

### Considered and rejected: straight-to-jakarta

A one-shot migration directly to the jakarta end-state (Restlet 2.6.0, Turbine 7.0, Velocity 2.4.1, Spring 6, Spring Security 6, Tomcat 10, Torque 6, `javax→jakarta`) was evaluated in light of the spike findings (Appendices A–C) and **rejected**.

- **It saves little true work.** The expensive rewrites are version-identical across the javax/jakarta targets: the Turbine 2.3.3→4.0 rewrite (RunData→PipelineData over 197 screens + 56 actions, YAAFI service container, resource loader, `TurbineVelocity` removal) is the same for 5.1 and 7.0; the Restlet 1.1→2.x API rewrite is the same for 2.5.2 and 2.6.0. Neither is done twice. Staging duplicates only *mechanical* work — a namespace re-touch of servlet lines (part of the global OpenRewrite sweep regardless), a trivial Restlet version bump, and small YAAFI config re-tuning — roughly 1–2 weeks, most of which was the second QA pass now removed by the compression above.
- **It concentrates all risk into one un-bisectable change.** Turbine 7.0 and Spring 6 / Security 6 are jakarta-only, so nothing boots until the undocumented Turbine 4.0 container rewrite, the Spring Security 6 config rewrite, the namespace flip, Tomcat 10, and Torque 6 all land together. The #1 project risk — "does the YAAFI container boot" (Appendix C) — would be debugged simultaneously with four other hard migrations. That debugging tail plausibly exceeds the ~1–2 weeks staging costs, and it lands squarely on the estimate's upper band.
- **Straight would only win** if the javax intermediate were guaranteed throwaway *and* container confidence were high — but confidence is low by construction (no authoritative 2.3.3→7.0 migration doc), which is exactly when a runnable checkpoint is most valuable.

The compressed-QA refinement captures the one genuine saving of the straight approach (pay for full regression once) while preserving the staged approach's risk isolation on the least-documented work.

**Phase 1 grounding (no number change).** The architecture/security characterization (`xnat-web-framework-architecture.md` §4) firms Phase 1 rather than moving it: Torque is vestigial (removes a small assumed cost), while Security 6 is confirmed a real rewrite (removed voter/channel/`WebSecurityConfigurerAdapter` APIs) — which *defends the lower band* against being treated as a namespace sweep. The swing factor for Phase 1's upper end is preserving the `XnatSecurityExtension` plugin contract for external plugins. Range stays **6–12 weeks**.

## Current State (verified)

Version catalog — `gradle/libs.versions.toml`:

- Restlet `1.1.10` (`:133`); aliases still reference the retired `com.noelios.restlet` group (`:354-357`)
- Turbine `2.3.3` (`:150`); pre-Apache coordinate `turbine:turbine` (`:410`)
- Velocity `1.7` (`:152`), velocity-tools `2.0` (`:153`); artifact `org.apache.velocity:velocity` (`:189`)
- Spring `5.3.39` (`:139`), Spring Security `5.7.13` (`:143`), servlet-api `3.1.0` (`:135`), tomcat-embed `9.0.93` (`:148`), torque `3.3` (`:149`)
- Namespace: **73** `javax.servlet` importers in `xnat-web/src/main/java`, **0** `jakarta.servlet`

Usage scale:

- **Restlet** — ~162 files. Custom core: `XNATApplication extends org.restlet.Application`, `XNATComponent extends Component`, `XNATRestletServlet extends com.noelios.restlet.ext.servlet.ServerServlet`, `XnatSecureGuard extends Filter`, `SecureResource extends org.restlet.resource.Resource` with ~45+ subclasses. Uses internal `com.noelios.restlet.http.*` (`HttpConstants`, `HttpRequest`) — removed in 2.x. Wired in `xnat-web/.../initialization/XnatWebAppInitializer.java:55,73`. **Static decomposition:** **61** files `extends Resource`, **11** `com.noelios.*`, **2** internal `HttpConstants` — remainder import-only repackaging.
- **Turbine** — ~299 files, **197** screen classes extending `VelocitySecureScreen`/`RawScreen`/`VelocityAction`; `RunData` used in 285 files; custom `RestletRunData extends DefaultTurbineRunData` (`RestletRunData.java:19`). Config: `xnat-web/src/main/webapp/WEB-INF/conf/TurbineResources.properties` (706 lines, 12 services), `turbine-om.properties` (DBSecurityService + PassiveUserManager), `xnat17.properties`. **Static mechanical surface:** **107** `doBuildTemplate(RunData…)` + **62** `doPerform(RunData…)` = ~169 uniform signature swaps.
- **Velocity** — ~272 files, **~1,038 `.vm` templates** (webapp `*-templates/` trees). Custom `CustomClasspathResourceLoader extends ResourceLoader` overriding `getResourceStream(String)` (`CustomClasspathResourceLoader.java:69`, self-called `:280`) — the method removed in Velocity 2.x. Engine managed by Turbine's VelocityService; standalone use in `VelocityUtils`, `AdminUtils`, `ProjectAccessRequest`. **Static sweep:** only **18** of 1,038 templates use the removed `$velocityCount`/`$velocityHasNext`; **0** use hyphenated identifiers — syntactic edit surface ≈ **18 files**, not 1,038.

Restlet↔Turbine coupling (handle at the seams, not in isolation): `RestletRunData`, `TurbineScreenRepresentation`, `StandardTurbineScreen`, `SecureResource`, `XnatSecureGuard`.

## Target Versions

| Framework | From | Phase 0 (javax) | Phase 1 (jakarta) |
|---|---|---|---|
| Restlet | 1.1.10 | **2.5.2** (`org.restlet`, Maven Central, javax) | **2.6.0** (`org.restlet`, Maven Central, jakarta) |
| Turbine | 2.3.3 | **5.1** (javax, Velocity 2, Torque 5) | **7.0** (jakarta, Torque 6) |
| Velocity | 1.7 | **2.x** (via Turbine 5.1) | **2.4.1** (pin) |
| Spring | 5.3.39 | 5.3.39 (unchanged) | **6.x** |
| Spring Security | 5.7.13 | 5.7.13 (unchanged) | **6.x** |
| Tomcat embed | 9.0.93 | 9.x | **10.x** |
| Torque | 3.3 | 5.0 | **6.0** (transitive-only) |
| Hibernate / JPA | 5.x (`javax.persistence`) | 5.x | **6.x** (`jakarta.persistence`; 113 files) |
| Servlet | javax 3.1 | javax 3.1 | **jakarta 5+** |

## Mandatory Breaking-Change Citations

Per `CLAUDE.md` § citation rule — grounding quotes from the referenced docs:

- **Velocity** (`velocity.apache.org/engine/2.4.1/upgrading.html`): "`getResourceStream()` replaced with `getResourceReader(String name, String encoding)`" — directly breaks `CustomClasspathResourceLoader:69`. Also: "the internal Context API now enforces String keys everywhere"; "`$velocityCount` and `$velocityHasNext` removed" (use `$foreach.count/.index/.hasNext()`); "the hypen (`-`) cannot be used in variable names anymore" (restore via `parser.allow_hyphen_in_identifiers = true`); default encoding "changed from ISO-8859-1 to UTF-8"; `ExtendedProperties`→`org.apache.velocity.util.ExtProperties`; artifact `velocity`→`velocity-engine-core`.
- **Restlet** (`restlet.talend.com/documentation/whats-new/2.0/migration/`): "instead of extending the `Resource` class ... you should now extend `ServerResource`"; router default "matching mode is `Template.MODE_EQUALS` and the default query matching property is set to 'false'" (was `MODE_STARTS_WITH`). Coordinate change: the old `com.noelios.restlet` group and `org.restlet:org.restlet:1.1.x` are retired; the modern line is `org.restlet:org.restlet` (+ `org.restlet.ext.servlet`) on Maven Central; internal `com.noelios.restlet.http.*` no longer exists.
- **Turbine**: no single 2.3.3→7.0 migration doc exists (howtos chain only 2.3→4.0→5.0; `turbine-7-0/howto` has no 7.0 guide). Ground the 4.0 rewrite against the 2.3→4.0 and 4.0→5.0 howtos plus the 7.0 javadoc as each change is implemented. The Turbine 7.0 dependencies page confirms Jakarta Servlet 5+, Java 17, Torque 6.

> **Documentation gap to report** (per the planning step "report if any documentation is not found"): there is **no authoritative Turbine 2.3.3→7.0 migration document**. The path must be reconstructed from the chained 2.3→4.0→5.0 howtos + 7.0 apidocs. Treat every Turbine service / RunData / config change as verify-before-edit.

## Plan

### Phase 0 — Modernize on javax / Tomcat 9

No container change; each step must compile and pass tests before advancing.

**0a. Restlet 1.1.10 → 2.5.2** (most self-contained; ~162 files)

- Catalog + wiring: replace the four `restlet-*` aliases (`:354-357`) with `org.restlet:org.restlet` and `org.restlet:org.restlet.ext.servlet` at `2.5.2` (drop the `restlet` version pin); update refs in `xdat/build.gradle`, `xnat-web/build.gradle`, `parent/build.gradle`. The modern artifacts are on Maven Central — no special repo needed.
- Code: `Resource`→`ServerResource` across ~45+ `SecureResource` subclasses; repackage imports (`com.noelios.restlet.ext.servlet.ServerServlet`→`org.restlet.ext.servlet.ServerServlet`); remove internal `com.noelios.restlet.http.*` usage; re-add explicit `MODE_STARTS_WITH` / query matching where XNAT relied on 1.1 router defaults (`XNATApplication`, `XNATVirtualHost`, `XNATRestletFactory`); update `XnatWebAppInitializer.java:55,73`.
- `ext.fileupload`: still present at `2.5.2` (keep for Phase 0); it is **removed in 2.6.0**, so the ~5 `RestletFileUpload` call sites (`SecureResource.java:751,1276`, `UserCacheResource.java:408`, `SearchResource.java:96`, `TriageRestlet.java:741`) get replaced with direct commons-fileupload parsing at Phase 1.
- Representative files: `xnat-web/.../restlet/{XNATApplication,XNATComponent}.java`, `.../restlet/servlet/XNATRestletServlet.java`, `.../restlet/guard/XnatSecureGuard.java`, `.../restlet/resources/SecureResource.java`.

**0b. Turbine 2.3.3 → 5.1 + Velocity 1.7 → 2.x together** (coupled; the largest chunk)

- Catalog: `turbine:turbine`→`org.apache.turbine:turbine:5.1`; `org.apache.velocity:velocity`→`velocity-engine-core:2.x`; bump velocity-tools; add/adjust Torque 5.0; revisit the `fulcrum`/`velocity` excludes in `xnat-web/build.gradle`.
- Code: fix `CustomClasspathResourceLoader` — implement `getResourceReader(String, String)` in place of `getResourceStream(String)` (`:69`, `:280`); migrate `Velocity.init()` / `VelocityContext` / `Template` usage in `VelocityUtils`, `AdminUtils`, `ProjectAccessRequest`; reconcile 197 screen classes against Turbine 5.x `VelocitySecureScreen` / `RawScreen` / `VelocityAction` and the `RunData` API; port `RestletRunData` to the 5.x RunData service.
- Config: rewrite `TurbineResources.properties`, `turbine-om.properties`, `xnat17.properties` to the Turbine 4.0+ service-container format — see **`turbine-service-container.md`** for the exact 13 services, why Turbine uses each, XNAT's five custom wirings that must survive, and the flat-properties→Avalon/YAAFI XML split. A **first-pass draft YAAFI config** is ready in **`turbine5-config-draft/`** (`roleConfiguration.xml` + `componentConfiguration.xml` + `README.md` with the properties delta and open items). Verify the three trim candidates (Session/Crypto/Upload) before finalizing.
- Templates: syntactic sweep is **~18 files** (static count: 18 use `$velocityCount`/`$velocityHasNext`→`$foreach.*`; 0 hyphenated identifiers), not 1,038. The remaining check is *behavioral, not mechanical*: `#if` null-check semantics (`directive.if.empty_check`) across the ~405 `#foreach`/`#if` templates and any non-UTF-8 encodings — validated via the render test (Appendix C Rung 3), not edited blindly.

End of Phase 0: all three frameworks modern, still javax, still Tomcat 9, all tests passing.

### Phase 1 — Atomic Jakarta cutover to Tomcat 10

Single coordinated branch (namespace + DI-version changes only; the API rewrites are already done):

- Spring 5.3→**6.x**, Spring Security 5.7→**6.x**; tomcat-embed 9→**10**; servlet-api javax→**jakarta**; Torque 5→**6** (transitive-only — see note below).
- **Spring Security 6 config rewrite** (characterized in `xnat-web-framework-architecture.md` §4): `SecurityConfig` (368 lines) `extends WebSecurityConfigurerAdapter` (removed) and uses the removed voter model (`UnanimousBased`/`RoleVoter`), the removed channel metadata-source model (`ChannelProcessingFilter`/`DefaultFilterInvocationSecurityMetadataSource`), `authorizeRequests()`, `.and()` chaining, and `http.apply(...)`. Migrate to a `SecurityFilterChain` bean + `AuthorizationManager` + `requiresChannel()` **while preserving the `XnatSecurityExtension` plugin contract** external plugins depend on. First-class Phase 1 workstream, not a namespace sweep.
- Restlet 2.5.2→**2.6.0**; Turbine 5.1→**7.0** (Jakarta build).
- **Logging — permanent resolution of log4j2↔Logback here.** Turbine 7.0 also uses log4j2 (2.24.3) — same tension as 5.1, now with SLF4J 2.0 and Logback 1.4/jakarta. Since the whole logging stack is re-platformed for jakarta anyway, decide once: strongest candidate is **unify on log4j2** (retire Logback, `logback.xml`→`log4j2.xml`), eliminating the two-backend tension for good. See `dep-dryrun-phase0-findings.md` § Cross-phase.
- Replace the ~5 `RestletFileUpload` call sites with direct commons-fileupload(2) request parsing (extension gone in 2.6.0).
- Run `javax.*`→`jakarta.*` across the full surface (static counts, all modules): `javax.servlet` **109**, `javax.persistence` **113**, `javax.validation` **37**, `javax.mail` **17**, `javax.xml.bind` **10**, plus **29** `web.xml`/JSP/TLD. Use OpenRewrite `java.migrate.jakarta` for the mechanical bulk, but **do not blind-rewrite `javax.annotation`** — most of the 255 hits are JSR-305 `@Nonnull`, which stays `javax`; only `@PostConstruct`/`@Resource` move. Hand-audit reflection / string-constructed class names the tools can't see.
- **Hibernate 5→6** (surfaced by the 113 `javax.persistence` files) — `jakarta.persistence` forces Hibernate 6 (HQL/`Criteria`/bootstrap changes). A distinct ORM sub-migration bundled under Spring 6; size it with a pre-Phase-1 Hibernate audit.
- Flip local dev + CI to Tomcat 10 / Java 17+ runtime.

Optional throwaway milestone: run `tomcat-jakartaee-migration` on the Phase-0 WAR to smoke-test booting on Tomcat 10 before committing to the source migration (disposable; strips jar signatures, fragile on reflection).

## Effort & Time Estimate

For **one dedicated senior developer working full-time with AI coding support**. Ranges are calendar-weeks of focused work; the wide bands reflect genuine unknowns (chiefly the undocumented Turbine 4.0 rewrite).

| Phase | Scope driver | Estimate |
|---|---|---|
| 0a — Restlet 1.1.10 → 2.5.2 | ~162 files, mechanical API/package rewrite + REST-layer testing | **2–4 weeks** |
| 0b — Turbine 2.3.3 → 5.1 + Velocity 1.7 → 2.x | 197 screens + 56 actions (mechanical, spike-validated), 12-service config rewrite across the undocumented 4.0 boundary, 1,038 template sweep, custom resource loader, Restlet↔Turbine bridge rework; **YAAFI container boot proven in sandbox (6/6 services)** | **7–12 weeks** |
| 1 — Jakarta cutover (Spring 6, Security 6, **Hibernate 6**, Tomcat 10, `javax→jakarta`, fileupload) | Security 6 rewrite characterized (`SecurityConfig` + removed voter/channel/`WebSecurityConfigurerAdapter` APIs, preserve `XnatSecurityExtension` contract) + namespace migration (servlet 109 / persistence 113 / validation 37 / mail 17 / jaxb 10 + 29 config) + **Hibernate 5→6 (113 JPA files)** + container flip. Torque vestigial (no work). | **6–13 weeks** |
| Cross-cutting — integration, smoke-checkpoints per phase + **one** full regression (Playwright) at the Tomcat 10 end-state, deploy hardening | Compressed QA: full regression run once, not per phase (see strategy note) | **3–5 weeks** |
| **Total** | | **≈ 4.5–8 months (18–34 weeks)** |

**Spike-driven revision (0b: 8–16 → 7–13 weeks).** Three validation spikes (Appendix A + B) confirmed the code-transformation bulk of 0b is uniform and compiler-driven: 197 screens **and** 56 actions ride one mechanical `RunData`→`PipelineData` pattern, the `TurbineVelocity` facade removal is 15 sites, and templates are largely zero-edit. This firms the lower band and trims ~3 weeks off the top by removing the "197 bespoke screens" tail risk. The upper band deliberately holds: it is still governed by the **undocumented `TurbineResources.properties` service-container rewrite** and runtime behavior of the Turbine 4.0 container, which the spikes did not touch. The one newly-identified non-mechanical item — the **Restlet↔Turbine bridge** (`TurbineScreenRepresentation`'s output-hijacking, now deprecated) — is bounded to ~4 files and folded into 0b (~1 week incl. runtime validation).

**Static pre-Phase-0b findings (counts, no deps required).** A read-only static sweep tightened both bands and surfaced one scope addition: (a) the Velocity template edit surface is **~18 files**, not 1,038 (only 18 use removed loop vars; 0 hyphenated identifiers) — firms 0b's lower band; (b) the Turbine mechanical swap is a bounded **~169** `doBuildTemplate`/`doPerform` overrides, and Restlet decomposes to **61**/**11**/**2** — both compiler-driven; (c) in-repo there is exactly **one** `XnatSecurityExtension` type (the base), so Phase 1's security swing factor is external-plugin compatibility, not internal breadth; (d) **new**: the jakarta surface is broader than "~73 files" — `javax.persistence` (**113**) makes **Hibernate 5→6** an explicit Phase 1 sub-migration, nudging Phase 1 to **6–13 weeks** and the total to **18–35 weeks**.

**What AI support accelerates well** (bulk of the raw edits): mechanical import/package rewrites, `Resource`→`ServerResource` and `getResourceStream`→`getResourceReader` transforms, `$velocityCount`→`$foreach.*` template sweeps, OpenRewrite-style `javax→jakarta` bulk changes, and drafting the migrated config files.

**What it does *not* materially shorten** (dominates the schedule): debugging runtime behavior after the Turbine 4.0 service-container swap (no migration doc — the single largest risk), reconciling Spring Security 6's rewritten config against XNAT's auth, end-to-end regression testing, and QA cycles against a live Tomcat 10 + PostgreSQL deployment.

**Assumptions:** one dedicated developer (more hands help 0a and template sweeps but the Turbine core work does not parallelize cleanly); no mid-project scope additions; test/QA environments available on demand. Treat 0b as the critical-path item — if any phase overruns, it will be this one.

## Key Files

- `gradle/libs.versions.toml`, `parent/build.gradle`, `xdat/build.gradle`, `xnat-web/build.gradle`
- `xnat-web/.../initialization/XnatWebAppInitializer.java`
- `xnat-web/.../restlet/**` (application, component, servlet, guard, resources, representations, rundata)
- `xdat/.../velocity/loaders/CustomClasspathResourceLoader.java`
- `xdat/.../xft/utils/VelocityUtils.java`, `xdat/.../turbine/utils/AdminUtils.java`
- `xnat-web/.../turbine/modules/screens/**`, `xdat/.../turbine/modules/screens/**`
- `xnat-web/src/main/webapp/WEB-INF/conf/{TurbineResources,turbine-om,xnat17}.properties`
- `xnat-web/src/main/webapp/**/*.vm`

## Risks / Unknowns

- **Turbine 4.0 rewrite** (Avalon/YAAFI service container, `RunData`, config format, security service→Fulcrum) is the highest-risk work and lacks a direct migration doc. Verify each service against the chained howtos + apidocs. **Bounded in Appendix C:** service-survival mapped (8 survive, 5 → Fulcrum components, YAAFI now mandatory), with a 4-rung boot/render verification ladder.
- **Turbine security config** — `DBSecurityService` / `PassiveUserManager`: **confirmed vestigial** (Appendix C, Finding 1) — 16 Java hits, all `TurbineSecurityException`, zero ACL/permission calls; real auth is Spring Security. Reduces to a passive-stub config swap, not a Fulcrum Security migration.
- **Restlet coordinates/versions — RESOLVED.** Modern line is on **Maven Central under groupId `org.restlet`** (not `org.restlet.jee`/Talend). Last stable javax = **2.5.2**; first jakarta = **2.6.0**; latest = 2.7.0-m3. The old 1.1.10 coords (`com.noelios.restlet:*`, `org.restlet:org.restlet:1.1.10`) live on the Talend repo — verify their removal doesn't leave a dangling repo requirement.
- **Restlet `ext.fileupload` dropped after 2.5.2** — absent in 2.6.0. Replace `RestletFileUpload` (~5 call sites) with direct commons-fileupload parsing during Phase 1.
- **`xnat-web` exclude list** — legacy `exclude group:` entries (log4j 1.x, velocity, fulcrum, etc.) must be revisited as transitive graphs change.
- **Velocity 2.4.1 vs Turbine 7's bundled Velocity** — pin 2.4.1 in the catalog and confirm Turbine 7 tolerates the override.
- **Torque is vestigial** (`xnat-web-framework-architecture.md` §2) — **zero** `org.apache.torque` imports in XNAT code; it is a Turbine transitive only. "Torque 3.3→6.0" is not migration work — only the old `group=torque`/`village` catalog coordinates realign with whatever Turbine pulls.
- **Spring Security is load-bearing and substantial** (`xnat-web-framework-architecture.md` §4) — the production `SecurityConfig` is a real 5.7→6 rewrite (removed `WebSecurityConfigurerAdapter`/voter/channel-metadata APIs) that must preserve the `XnatSecurityExtension` plugin contract. Do not treat Phase 1 security as a namespace sweep.
- **Four coexisting web frameworks** (`xnat-web-framework-architecture.md` §1) — Spring MVC (`/xapi`), Restlet (`/REST`,`/data`), Turbine (`/app`), and raw servlets share one `javax→jakarta` servlet boundary, so the Jakarta flip is all-or-nothing at that layer.
- **Hibernate 5→6 is in Phase 1** — the 113 `javax.persistence` files mean the Jakarta flip drags in a Hibernate 6 ORM migration (HQL/`Criteria`/bootstrap), not just a namespace change. Pressures Phase 1's upper band; quantify with a pre-Phase-1 Hibernate audit.
- **`javax.annotation` is a rewrite trap** — of 255 hits, most are JSR-305 `@Nonnull` (stays `javax`); only `@PostConstruct`/`@Resource` move to `jakarta`. Do not point a blanket OpenRewrite recipe at `javax.annotation`.
- **Phase-0 dependency graph — resolved + sandbox-boot-corrected** (`dep-dryrun-phase0-findings.md`) — dry-run resolves cleanly and **SLF4J stays 1.7.36**. Fixes: drop the double **Torque**, exclude legacy **Avalon**, check **commons-collections 3.2.2**, add `fulcrum-cache`+`fulcrum-upload` explicitly; runtime-verify **Fulcrum vs pinned commons-lang3 3.11**. **Corrected by rung 5:** Turbine 5.1 **requires `log4j-core`** at init — do NOT exclude it; instead **align all log4j2 module versions** and **reconcile log4j2↔Logback**. **Cross-phase:** Turbine **7.0 also uses log4j2** (2.24.3), so this tension persists to the jakarta target — keep the 0b fix tactical and make the permanent decision (candidate: unify on log4j2) at Phase 1. See `dep-dryrun-phase0-findings.md` § Cross-phase.
- **Test-coverage — bifurcated** (`timing-analysis-detailed.md`). *In-repo:* **0** of 381 bundled JUnit tests exercise the 197 Turbine screens or ~162 Restlet resources (Playwright = 2 specs). *But externally:* the **`NrgXnat/xnat-rest-tests`** suite (1,314 REST-assured integration tests) **does** cover the REST layers — measured ~**149** legacy Restlet `/data` call sites (Phase 0a) and ~**164** `/xapi` (Phase 1). **So:** (1) run `xnat-rest-tests` against the deployed server after each phase as the **REST regression net** (verify its `nrg_test`/`xnat-data-models` deps work against the jakarta build for Phase 1); (2) the real blind spot is the **Turbine `/app` screen HTML** (not covered anywhere) — mitigate with the **golden-master harness** (`tools/golden_master.py`, prototype-validated), a ~1–3 wk add taking the estimate to ≈19–37 wk.

## Verification

- Per step: `./gradlew :<module>:compileJava` then `:<module>:test` (e.g. `:xdat:test`, `:xnat-web:test`) — green before advancing. Full `./gradlew build`.
- Restlet: exercise `/REST/*` and `/data/*` endpoints (resource GET/PUT, multipart upload, auth via `XnatSecureGuard`).
- Turbine/Velocity: render representative screens across `/app/*` (secure screen, report, edit, PDF, XML raw screen) and email templates (`AdminUtils`), confirming template resolution through `CustomClasspathResourceLoader`.
- Phase 1: deploy the WAR to a real Tomcat 10 + Java 17 instance against PostgreSQL; run the Playwright suite in `xnat-web/tests/playwright/`; verify login/session (Spring Security 6) and the Restlet↔Turbine bridge screens.

## Appendix A — Phase 0b spike (validated): the Index screen

A worked, javadoc-verified example of the Turbine 2.3.3→5.1 + Velocity 1.7→2.x migration for one representative screen, done to de-risk the path. All API facts below are confirmed against the Turbine 5.1 apidocs and Maven Central (not from memory). **No code has been changed** — this documents what the edits will be.

### Dependency surface of a single screen

Rendering `Index.vm` pulls in a small concrete screen riding on a shared stack:

- `xnat-web/.../turbine/modules/screens/Index.java:35` → `extends SecureScreen`
- `xdat/.../turbine/modules/screens/SecureScreen.java:68` → `extends org.apache.turbine.modules.screens.VelocitySecureScreen`
- `xdat/.../velocity/loaders/CustomClasspathResourceLoader.java` (Velocity SPI — how `.vm` files are located)
- `xnat-web/.../xnat-templates/screens/Index.vm` (template)
- `TurbineResources.properties` (service + loader registration — shared config)

### Verified API facts (Turbine 5.1 / velocity-tools 3.1)

- `VelocitySecureScreen` methods take **`PipelineData`, not `RunData`**: `doBuildTemplate(PipelineData)`, abstract `doBuildTemplate(PipelineData, org.apache.velocity.context.Context)`, abstract `isAuthorized(PipelineData)`. Parent chain: `VelocitySecureScreen` → `VelocityScreen` → `TemplateScreen`.
- `RunData` still exists (`org.apache.turbine.util.RunData`) and **extends `PipelineData`**. All methods XNAT uses are intact: `setMessage`, `getMessage`, `getTemplateInfo`, `getParameters`, `getScreen`, `getAction`, `getResponse` (returns **javax** `HttpServletResponse`), `getSession`, `setScreenTemplate`.
- Get `RunData` from `PipelineData` via the interface default method **`pipelineData.getRunData()`** (the old `getRunData(pipelineData)` valve helper was removed in 5.0).
- `TemplateScreen.doRedirect(PipelineData, String)` accepts a `RunData` argument unchanged (IS-A `PipelineData`).
- **The `TurbineVelocity` static facade is removed in 5.x.** Replacement is the `VelocityService` interface: `getContext(PipelineData)` / `getContext()` / `getNewContext()`, with `SERVICE_NAME`, `handleRequest(...)`, `requestFinished(Context)`. `getContext(PipelineData)` accepts a `RunData` too.
- Velocity tools: coordinate moves `org.apache.velocity:velocity-tools:2.0` → **`org.apache.velocity.tools:velocity-tools-generic:3.1`**; `org.apache.velocity.tools.generic.EscapeTool` (no-arg ctor) unchanged. Engine: `velocity-engine-core:2.4.1`.

### The edits, by tier

**Tier 1 — 197 screens: uniform, compiler-driven.** Per screen, swap the signature and derive `RunData`:

```java
// Index.java :37 (and the near-empty xdat Index.java :19)
protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
    final RunData data = pipelineData.getRunData();
    // ...every remaining line unchanged (TurbineUtils.GetPassedParameter(...,data), context.put(...), etc.)
```
Import delta: add `org.apache.turbine.pipeline.PipelineData` (keep `RunData` where still referenced).

**Tier 2 — the `TurbineVelocity` facade removal: 15 files / 15 live call sites, one variant.** Every use is `TurbineVelocity.getContext(data)` (16 sites, 1 already commented; `GrantProjectAccess` has only a stale import). Centralize once, then swap uniformly:

```java
// new static helper (e.g. in TurbineUtils)
public static Context getVelocityContext(final PipelineData pipelineData) {
    return ((VelocityService) TurbineServices.getInstance()
            .getService(VelocityService.SERVICE_NAME)).getContext(pipelineData);
}
// then, across all sites:  TurbineVelocity.getContext(data) -> TurbineUtils.getVelocityContext(data)
// and drop `import org.apache.turbine.services.velocity.TurbineVelocity;`
```
Affected files: screens `Login`, `Register`, `VerifyEmail` (xdat + xnat-web), `ResendVerification`, `ForgotLogin`, `InactiveAccount`, `RegisterExternalLogin`, `XDATScreen_UpdateUser`, `SecureScreen` (×2, `:149`,`:310`); action `SecureAction:233`; ajax `RequestProjectBundle`, `RequestSearchData` (×2); stale-import-only `GrantProjectAccess`.

**Tier 3 — shared base work, done once (the genuine engineering).**
- `SecureScreen.java`: signatures `RunData`→`PipelineData` at `:146`,`:285`,`:307` (+ two-arg `:146`); `RunData data = pipelineData.getRunData();` at top of each; the two `TurbineVelocity.getContext(data)` (`:149`,`:310`) via the Tier-2 helper. Bodies otherwise unchanged; `doRedirect(data,...)` (`:325`,`:383`) unchanged; `javax.servlet.http.HttpServletResponse` (`:58`,`:471`,`:559`) stays javax.
- `CustomClasspathResourceLoader.java`: `init(ExtendedProperties)`→`init(org.apache.velocity.util.ExtProperties)` (`:59`); `getResourceStream(String)`→`getResourceReader(String, String)` returning `Reader` via the built-in `buildReader(stream, encoding)` (`:69`), reusing the existing `findMatch`/`TEMPLATE_PATHS` lookup. **Sandbox-validated** (Appendix C Rung 4): a standalone port of this migration renders through the `TEMPLATE_PATHS` override hierarchy under Velocity 2.4.1.
- `TurbineResources.properties`: `VelocityService` + `CustomClasspathResourceLoader` registration and module-package list move to the Turbine 4.0+ service-container format.

**Template — `Index.vm`: zero edits expected.** Uses only `#set`/`#if`/`#else`/`#parse`, `$data`, `$content`; no `#foreach`, `$velocityCount`, or hyphenated identifiers; ASCII. Only semantic to eyeball: `#if($data.message)` (`:16`) under Velocity 2's `directive.if.empty_check=true`.

### De-risking outcome

The Phase 0b screen/velocity layer is now fully characterized and bounded — Tier 1 is uniform boilerplate, Tier 2 is a helper plus a 15-site swap, and only Tier 3 (~3 shared files + config) is real engineering. This tightens the lower end of the 0b estimate; the residual risk remains the `TurbineResources.properties` service-container rewrite and runtime behavior of the Turbine 4.0 container, as originally flagged.

## Appendix B — Two follow-on spikes (validated)

Run to confirm the Appendix A pattern holds across the base types the Index screen did not exercise: Turbine **actions**, and the **Restlet↔Turbine bridge**. All facts confirmed against the Turbine 5.1 apidocs and repo source; no code changed.

### B.1 — Actions (`VelocitySecureAction`): mechanical, same pattern

- `xdat/.../turbine/modules/actions/SecureAction.java:65` → `extends VelocitySecureAction`. In 5.1: `VelocitySecureAction extends VelocityAction`, with `perform(PipelineData)` and abstract `isAuthorized(PipelineData)` — the RunData→PipelineData story is identical to screens.
- Only the framework override changes: `SecureAction.isAuthorized(RunData)` (`:231`) → `isAuthorized(PipelineData)` with `RunData data = pipelineData.getRunData();` at top. Its `TurbineVelocity.getContext(data)` (`:233`) is already inside the Appendix A Tier-2 set.
- **XNAT's own helper methods keep `RunData` signatures** (`error`, `redirectToReportScreen`, `redirectToScreen`, `handleException`, `displayProjectEditError`, etc.) — concrete actions derive `data` and pass it, unchanged. Verified RunData methods all present in 5.1: `setRedirectURI`, `addMessage`, `getRequest`, `setScreenTemplate`, `getResponse`, `getSession`, `getParameters`, `getAction`, `getTemplateInfo`. `javax.servlet.*` (`Cookie`, `HttpServletRequest`, `HttpSession`) stays javax.
- **~56 `SecureAction`/`VelocityAction` subclasses** fold into the same mechanical Tier-1 swap as the 197 screens. **No new risk.**

### B.2 — Restlet↔Turbine bridge: compiles, but needs a bounded rework

The bridge subclasses two Turbine **implementation** classes (not interfaces), so the first question was survival:

- `xnat-web/.../restlet/rundata/RestletRunData.java:19` → `extends org.apache.turbine.services.rundata.DefaultTurbineRunData` — **exists in 5.1**; now `extends DefaultPipelineData implements RunData, TurbineRunData, PipelineData, Recyclable, AutoCloseable`. Compiles.
- `RestletRunDataService.java:14` → `extends TurbineRunDataService` — **exists in 5.1** (`extends TurbineBaseService implements RunDataService`). Compiles. (Note: `TurbineResources.properties:277,299` register the *stock* service/RunData; `RestletRunData` is instead `new`'d directly — see below.)

Two genuine behavioral breaks (structure survives; behavior does not):

1. **Output hijacking is deprecated.** `RestletRunData.hijackOutput(PrintWriter)` (`:31`) calls `setOut(PrintWriter)`, which in 5.1 is **deprecated** ("no replacement planned, response writer will not be cached") and absent from the `RunData` interface. The bridge's whole purpose — render a Turbine screen into a Restlet response — relies on this. Likely fix: rework `TurbineScreenRepresentation` to render via `VelocityService.handleRequest(context, template, writer)` instead of hijacking the RunData writer.
2. **Pooled/Recyclable lifecycle.** `DefaultTurbineRunData` is now `Recyclable` and normally pool-managed; `TurbineScreenRepresentation.java:111` does `new RestletRunData()` directly, so it is not initialized through the pool/PipelineData path the 5.1 pipeline expects. The custom `passObjects` map + `recycle()` interaction needs checking.

**Coupling is bounded to 4 files:** `RestletRunData` + `RestletRunDataService`, producer `TurbineScreenRepresentation.java` (`:77` `hijackOutput`, `:111` `new`, `:142` `passObject`), consumers `CustomTableScreen.java:40` and `TriageResources.java:24` (`retrieveObject("table")`).

**Outcome:** this is the one part of the screen/velocity layer that is *not* purely mechanical — a small rendering-path redesign plus runtime validation, ~1 week, folded into 0b. It does not widen the 0b upper band (contained to 4 files), unlike the service-container config work.

## Appendix C — Phase 0b service-container boot/render verification

This addresses the residual upper-band risk: does XNAT's 12-service + `PassiveUserManager`/`DBSecurityService` config boot and render on the Turbine 4.0+ YAAFI/Fulcrum container? Two static findings shrink the risk before any runtime test, then a 4-rung ladder confirms boot + render cheaply. Facts probed against the Turbine 5.1 apidocs and repo config; no code changed.

### Finding 1 — the Turbine security config is vestigial (config swap, not a migration)

- `turbine-om.properties` sets `SecurityService=DBSecurityService` + `user.manager=PassiveUserManager` and comments it as a no-op placeholder ("will no[t] access the Database"; "for an application without Torque"). It exists only to satisfy Turbine's "a SecurityService must exist" requirement.
- Java grep for Turbine security APIs: **16 hits, all `TurbineSecurityException`** in `XDATLoginUser.java` signatures — **zero `getACL()`/`AccessControlList`/permission/role calls**. Real auth is Spring Security.
- Therefore the scariest-looking config item is a **passive-stub swap**, not a Fulcrum Security migration.

### Finding 2 — service-survival map (probed against 5.1 javadoc)

| Survive as-is (stay in `TurbineResources.properties`) | Removed → now Fulcrum components (need YAAFI config) |
|---|---|
| RunData, Servlet, AssemblerBroker, Pull, Template, Velocity, Session; `PassiveUserManager` still present | Crypto, Factory, Pool, GlobalCache, Upload |
| | `AvalonComponentService` — currently commented out at `TurbineResources.properties:272`; **now mandatory** as the YAAFI container hosting the Fulcrum services |
| | `DBSecurityService` — removed; maps to the surviving passive path (verify exact 5.1 `SecurityService` classname) |

Bounded config work: **8 services translate trivially; 5 move into new `componentConfiguration.xml` + `roleConfiguration.xml` (standard Fulcrum config); the YAAFI container must be enabled; security reduces to the passive stub.** (`roleConfiguration.xml` is already anticipated but commented at `TurbineResources.properties:690`.) Full per-service breakdown, XNAT-specific wiring, and the config-format rationale are in **`turbine-service-container.md`**.

### The verification ladder (cheapest first)

1. **Static translation & class resolution** (hours, no runtime) — produce the updated `TurbineResources.properties` (8 survivors), `componentConfiguration.xml` + `roleConfiguration.xml` (5 Fulcrum services + YAAFI), passive-security stub; compile-time resolve every FQCN against the 5.1 jars.
2. **Programmatic container boot in a JUnit test** (minutes, no Tomcat) — the key spike. Bootstrap standalone and assert every service initializes:
   ```java
   TurbineConfig tc = new TurbineConfig(webappRoot, "/WEB-INF/conf/TurbineResources.properties");
   tc.initialize();  // fails fast on any misconfigured/missing service or role wiring
   for (String svc : /* all 12 SERVICE_NAME constants */)
       assertNotNull(TurbineServices.getInstance().getService(svc));
   ```
   Definitively answers "does the 12-service + YAAFI + passive-security config boot," including init order and Fulcrum role resolution, without booting XNAT.
3. **Headless render test** (no full app) — from the booted container, exercise VelocityService ↔ migrated `CustomClasspathResourceLoader` ↔ pull tools:
   ```java
   VelocityService v = (VelocityService) TurbineServices.getInstance().getService(VelocityService.SERVICE_NAME);
   String html = v.handleRequest(v.getContext(/* stub */), "screens/Index.vm");  // asserts loader resolution + Velocity 2 render
   ```
4. **Full integration** — deploy the XNAT WAR on **Tomcat 9** (still javax, Phase 0b) with Turbine 5.1; hit `/app/template/Index.vm` and representative screens (servlet wired at `XnatWebAppInitializer.java:72`); run the Playwright suite; exercise the Restlet↔Turbine bridge under a live request.

Rungs 1–3 are the de-risking spike and can run against a small test harness before migrating a single screen — converting the upper-band unknown ("does the container come up") into an afternoon pass/fail.

> **✅ Rungs 2–6 already executed (standalone sandbox).** A throwaway Turbine 5.1 + Fulcrum project: (2) booted the YAAFI container from the drafted `turbine5-config-draft/` config — **all 6 Fulcrum services initialize (6/6)**; (3) rendered a Velocity 2.4.1 template (`$foreach.*` works); (4) ran a **port of XNAT's `CustomClasspathResourceLoader` on the Velocity 2.x SPI** and confirmed the **`TEMPLATE_PATHS` override hierarchy** resolves; (5) booted **full `TurbineConfig`** and rendered a template **through Turbine's own `VelocityService` + the custom loader**; (6) rendered a **real Turbine screen** — servlet-stubbed `RunData` → a `VelocityScreen` subclass with the migrated `doBuildTemplate(PipelineData, Context)` → `TurbineVelocityService.handleRequest`, exercising a `#macro`, `#parse`, `$foreach`, and `$data` (RunData) access. Findings folded into the docs: render chain = `VelocityService`→`TemplateService`→`ServletService`+`AssemblerBrokerService`(+YAAFI); add `fulcrum-cache`+`fulcrum-upload` explicitly; **Turbine 5.1 requires `log4j-core`** (align log4j2 versions; reconcile with Logback — cross-phase). **Both of the highest-value INTEG unknowns are now proven** (container boot + the screen-render mechanism), firming 0b's lower band. Residual 0b risk narrows to what a standalone harness can't reach: name-based module loading + pull tools + real XNAT data, i.e. the real app on **Tomcat 9** (Appendix C Rung 4), plus the log4j2↔Logback reconciliation. See `turbine5-config-draft/README.md`.

> **Scaffold deferred.** The Rung-2 `TurbineConfig` boot test (a `@Disabled` JUnit skeleton with the 12-service assertions + trimmed config fixtures under `src/test/resources`) is intentionally **not created yet** — it cannot compile or run until Turbine 5.1 is on the classpath (`TurbineConfig`/`TurbineServices`/`VelocityService` differ at 2.3.3). Build it as the **first task at the start of Phase 0b**, immediately after the 5.1 dependencies enter the version catalog.
