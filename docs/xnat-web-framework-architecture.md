# XNAT-web Framework Architecture & Load-Bearing Map

> Companion to `tomcat10-jakarta-upgrade-plan.md`. Its purpose is to answer the scope-defining question the upgrade keeps hitting: **for each legacy framework, what is load-bearing vs. vestigial, and how do the request paths fit together?** Claims cite `file:line`; inferences are marked.

## 1. The web request-flow: four web frameworks coexist

XNAT-web is not one web framework but four, layered behind a single Spring Security filter chain. Everything is wired in `XnatWebAppInitializer.onStartup()` (`xnat-web/.../initialization/XnatWebAppInitializer.java`).

| URL pattern(s) | Handler | Framework | Registered at |
|---|---|---|---|
| `/xapi/*`, `/admin/*`, `/pages/*`, `/schemas/*` | Spring `DispatcherServlet` | **Spring MVC** (modern REST + MVC) | `getServletMappings()` `:93` + `getRootConfigClasses()` `:98` (`RootConfig`, plugins, `ControllerConfig`) |
| `/REST/*`, `/data/*` | `XNATRestletServlet` | **Restlet 1.1** (legacy REST API) | `:73`; component set via init-param `:55` |
| `/app/*` | `Turbine` | **Turbine + Velocity** (server-rendered UI: screens/actions) | `:72`; config via `XnatTurbineConfig` → `TurbineResources.properties` `:67`,`:203` |
| `/xdat/*` | `XDATServlet` | raw servlet (XDAT bootstrap/data) | `:71` |
| `/ajax/*`, `/servlet/XDATAjaxServlet`, `/servlet/AjaxServlet` | `XDATAjaxServlet` | raw servlet (AJAX endpoints) | `:74` |
| `/archive/*` | `ArchiveServlet` | raw servlet (archived file access) | `:75` |
| (all) | Spring Security filter chain | **Spring Security 5.7** | `SecurityWebApplicationInitializer` (`AbstractSecurityWebApplicationInitializer`) |

Cross-cutting: `XnatSessionEventPublisher` listener (`:65`), an `ApiIncludeContentTypeIsolationFilter` on `/xapi/*` INCLUDE dispatches (`:81`), and multipart config (`:112`).

**Implication for the upgrade:** the three named framework upgrades (Restlet, Turbine, Velocity) touch the `/REST` + `/data` + `/app` paths; the Jakarta flip additionally touches Spring MVC + Spring Security (all paths) and every `javax.servlet` filter/servlet above.

## 2. Framework-by-framework: role and load-bearing verdict

### Spring MVC — LOAD-BEARING (modern, actively grown)
Serves `/xapi/*` (the modern REST API) and `/admin`, `/pages`, `/schemas`. Root context is `RootConfig` + plugin `@Configuration`s + `ControllerConfig` (`XnatWebAppInitializer.java:98-104`). Not a legacy target; affected only by the Jakarta flip (Spring 6).

### Restlet 1.1 — LOAD-BEARING (legacy REST API)
`/REST/*` and `/data/*`. ~162 files, ~45 `SecureResource` subclasses. See upgrade plan Phase 0a. Note the modern REST surface is Spring MVC `/xapi`; Restlet is the *older* API kept for compatibility.

### Turbine + Velocity — LOAD-BEARING (server-rendered UI)
`/app/*` renders 197 screens + 56 actions via Velocity templates (~1,038 `.vm`). See upgrade plan Phase 0b + Appendices A–C. This is the deepest legacy coupling.

### Turbine **security** service — **VESTIGIAL** (stub)
`turbine-om.properties` configures `DBSecurityService` + `PassiveUserManager` as an explicit no-op ("will no[t] access the Database"; "for an application without Torque"). Java usage: 16 hits, all `TurbineSecurityException` in `XDATLoginUser.java` signatures — **zero** `getACL()`/permission/role calls. Real authz/authn is Spring Security (below). **Migrates to a passive stub, not a Fulcrum Security port.**

### Torque — **VESTIGIAL** (transitive only)
**Zero** `org.apache.torque` imports in XNAT code. Present solely as a Turbine transitive (`xnat-web/build.gradle:213-214` comment: "torque (turbine's core dep)") plus `torque-village`. XNAT's ORM is XFT/XDAT, not Torque. **The "Torque 3.3→6.0" line is not migration work** — only the old `group=torque` catalog coordinates realign with whatever Turbine 5.1/7.0 pulls.

## 3. Cross-framework seams

### Restlet ↔ Turbine bridge — LOAD-BEARING but bounded (4 files)
Restlet resources render Turbine/Velocity screens into REST responses. `TurbineScreenRepresentation` constructs a `RestletRunData` (`new` at `:111`), stuffs objects via `passObject` (`:142`), and hijacks screen output via `hijackOutput` (`:78`); consumers pull them back via `retrieveObject("table")` (`CustomTableScreen.java:40`, `TriageResources.java:24`). This is the one non-mechanical Turbine item (deprecated `setOut` semantics) — see upgrade plan Appendix B.2.

### Shared Spring context
All four frameworks resolve services from the same Spring context (`XDAT.getContextService()`, `XDAT.getUserDetails()` are used from Turbine screens/actions and Restlet resources alike), so Spring 6 changes ripple everywhere at the flip.

## 4. Spring Security — LOAD-BEARING, substantial, extensible (Phase 1 focus)

Production config is `org.nrg.xnat.initialization.SecurityConfig` (`xnat-web/.../initialization/SecurityConfig.java`, 368 lines) — **not** the test-only `WebSecurityConfigurerAdapter` classes. Filter chain registered by `SecurityWebApplicationInitializer` (`AbstractSecurityWebApplicationInitializer`).

**Architecture (all `file:line` in `SecurityConfig.java`):**
- `@EnableWebSecurity` **`extends WebSecurityConfigurerAdapter`** (`:89`).
- Custom auth stack: `XnatProviderManager` (`:123`), `XnatDatabaseAuthenticationProvider` + pluggable `AuthenticationProvider` list (`:108-115`,`:238`), `XnatDatabaseUserDetailsService` (`:134`), `DelegatingPasswordEncoder` with `LegacySha256PasswordEncoder` (`:231`).
- `configure(AuthenticationManagerBuilder)` sets parent manager + providers + extensions (`:252`).
- `configure(HttpSecurity)` (`:276`): `authorizeRequests().anyRequest().authenticated()` + `formLogin()` (`:294`), session management with concurrency (`:301`), header policy (`:309`), `csrf().disable()` (`:313`), anonymous/guest (`:314`), logout (`:315`), and a custom filter stack — `ChannelProcessingFilter`, `RequestContextFilter`, `XnatAuthenticationFilter`, `XnatInitCheckFilter`, `XnatExpiredPasswordFilter` positioned relative to stock filters (`:318-322`), plus `http.apply(new XnatBasicAuthConfigurer<>(...))` (`:299`).
- Authorization model: `UnanimousBased` + `RoleVoter` + `AuthenticatedVoter` (`:139`).
- Channel security: `ChannelProcessingFilter` + `ChannelDecisionManagerImpl` + `DefaultFilterInvocationSecurityMetadataSource` (`:211`).
- **Plugin extension contract:** `XnatSecurityExtension` implementations contribute both `configure(HttpSecurity)` and `configure(AuthenticationManagerBuilder)` (`:117-120`,`:267`,`:324`). **External plugins depend on this contract** — it must survive the migration.

**Verdict:** definitively load-bearing and non-trivial. This is a real Spring Security 5.7→6 migration, not a namespace tweak.

### Spring Security 6 migration surface (what actually breaks)

| Removed/changed in Security 6 | Used at | Migration |
|---|---|---|
| `WebSecurityConfigurerAdapter` | `:89` | → a `SecurityFilterChain` `@Bean` (restructures the whole class) |
| Global `configure(AuthenticationManagerBuilder)` | `:252` | → an `AuthenticationManager`/`ProviderManager` bean |
| `authorizeRequests()` | `:294` | → `authorizeHttpRequests()` |
| `AccessDecisionVoter`/`UnanimousBased`/`RoleVoter`/`AuthenticatedVoter` | `:139` | **voter model removed** → `AuthorizationManager` |
| `ChannelProcessingFilter` + `DefaultFilterInvocationSecurityMetadataSource` | `:211` | metadata-source/channel model removed → `http.requiresChannel()` |
| `AntPathRequestMatcher` | `:216` | → `requestMatchers(...)` |
| `.and()` chaining | throughout `configure(HttpSecurity)` | → lambda DSL (removed in 6.1+) |
| `http.apply(SecurityConfigurerAdapter)` | `:299` | → `http.with(...)` (6.2+) — and update `XnatBasicAuthConfigurer` |
| `javax.servlet.SessionCookieConfig` | `:77`,`:289` | → `jakarta.servlet` |

Surviving largely as-is: the custom filters, `SessionRegistry`/concurrency, password encoder, provider/userDetails beans. The **hard part is the `configure()`-override → bean-based rewrite while preserving the `XnatSecurityExtension` plugin contract** (whose signatures also reference `HttpSecurity`/`AuthenticationManagerBuilder`, so the contract itself may need a compatibility shim for external plugins).

## 5. Load-bearing vs. vestigial — summary

| Component | Verdict | Evidence |
|---|---|---|
| Spring MVC (`/xapi`, admin/pages/schemas) | **Load-bearing** (modern) | `XnatWebAppInitializer.java:93-104` |
| Spring Security (`SecurityConfig`) | **Load-bearing**, substantial, extensible | §4 |
| Restlet (`/REST`, `/data`) | **Load-bearing** (legacy REST) | Phase 0a |
| Turbine + Velocity (`/app`) | **Load-bearing** (server UI) | Phase 0b |
| Restlet↔Turbine bridge | **Load-bearing**, bounded (4 files) | §3 |
| Turbine security service | **Vestigial** (stub) | §2 |
| Torque | **Vestigial** (transitive only) | §2 |

## 6. Implications for the upgrade plan

- **Scope shrinks** where vestigial: Torque and Turbine-security become config-alignment/stub tasks, not migrations (already reflected in the plan's risks).
- **Scope is real** for Spring Security: Phase 1 includes a genuine 5.7→6 rewrite of a 368-line config with several removed APIs (voters, channel metadata source, `WebSecurityConfigurerAdapter`) **plus** preserving the `XnatSecurityExtension` plugin contract — this is a first-class Phase 1 workstream, not a namespace sweep.
- **Four coexisting web frameworks** means the Jakarta flip is genuinely all-or-nothing at the servlet layer: Spring MVC, Restlet, Turbine, and the raw servlets all share the one `javax→jakarta` boundary.
