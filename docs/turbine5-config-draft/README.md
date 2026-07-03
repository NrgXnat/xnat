# Turbine 5.1 YAAFI config — DRAFT (Phase 0b)

First-pass Avalon/YAAFI service-container config for the Turbine 2.3.3→5.1 migration. **Drafts, not wired into the build** — kept under `docs/` (not `WEB-INF/conf/`) until Phase 0b begins and Turbine 5.1 is on the classpath. Grounded in the Apache Turbine "Migrate 2.3.3→4.0" wiki + the Fulcrum component docs.

## ✅ VALIDATED — sandbox boot 6/6 (Appendix C Rungs 2–3 pulled forward)

A standalone Turbine 5.1 + Fulcrum sandbox (throwaway Gradle project) booted the YAAFI container from this `roleConfiguration.xml`/`componentConfiguration.xml` and looked up **all 6 Fulcrum services successfully** (`FactoryService`, `PoolService`, `GlobalCacheService`, `CryptoService`, `ParserService`, `UploadService`), then rendered a Velocity 2.4.1 template. **All 6 drafted role FQCNs are confirmed correct.** Findings that must be applied to XNAT's Phase-0b build:

1. **Add `org.apache.fulcrum:fulcrum-cache:2.0.1` explicitly** — Turbine 5.1 does NOT pull it transitively (the `GlobalCacheService` role is unresolvable without it).
2. **Add `org.apache.fulcrum:fulcrum-upload:2.0.0` explicitly** — same; the `UploadService` role needs it.
3. **Also exclude `org.apache.logging.log4j:log4j-slf4j-impl`** (a second log4j2→SLF4J binding) in addition to `log4j-core/jcl/web` — see `dep-dryrun-phase0-findings.md`.
4. **`javax.servlet-api` must be on the classpath at parser init** — a non-issue in the XNAT webapp (it's present), but confirms `ParserService` is servlet-coupled.
5. Fulcrum's native `commons-lang3` is **3.17**; XNAT forces it down to **3.11** — the `NoSuchMethodError` runtime risk is real (still the one open item; needs a compile/run, not a boot).

## ✅ RUNG 5 — full `TurbineConfig` boot + render through Turbine's VelocityService

The sandbox was extended to boot Turbine 5.1 via `org.apache.turbine.util.TurbineConfig` and render `screens/Index.vm` through **Turbine's own `VelocityService`** using a port of XNAT's custom loader — **success**: the override template rendered (`XNAT-TEMPLATES … loop 1 loop 2 loop 3`). This proves the last untested seam (Turbine `VelocityService` ↔ custom resource loader ↔ Velocity 2.4.1). Findings:

- **Turbine 5.1 render service-dependency chain:** `VelocityService` → `TemplateService` → `ServletService` + `AssemblerBrokerService`, all hosted alongside the YAAFI `AvalonComponentService`. Minimal working `TurbineResources.properties` = these 4 Turbine services + AvalonComponentService (+ the 6 Fulcrum roles).
- **Turbine 5.1 requires `log4j-core`** at init (`LoggerContext`) — the dep-dry-run's "exclude log4j-core" is wrong; keep it and **align all log4j2 modules to one version** (api/core mismatch → `NoSuchMethodError`). Reconciling log4j2 with XNAT's Logback is a genuine Phase-0b task. See `dep-dryrun-phase0-findings.md` (CORRECTION).

## ✅ RUNG 6 — real Turbine **screen** render (RunData + module + macro + `#parse`)

Extended to render a real Turbine screen, not just a raw template: a stubbed servlet request (dynamic proxies) drove `RunDataService` to build a `DefaultTurbineRunData`; a `VelocityScreen` subclass with the migrated `doBuildTemplate(PipelineData, Context)` populated the context from `RunData`; and `TemplateScreen.doBuild` → `VelocityScreen.buildTemplate` → `TurbineVelocityService.handleRequest` rendered the screen template through the custom loader — with a `#macro`, a `#parse` of a nested partial, `$foreach` (`$foreach.count`), and `$data` (RunData) access, all under Velocity 2.4.1. **Output rendered correctly (macro / #parse / $foreach all OK).**

- **Proven:** `RunData` construction (servlet-stubbed), the `RunData`→`PipelineData` screen contract, the screen→VelocityService→custom-loader render, and Velocity 2 template features (macro/parse/foreach) in a screen context.
- **Not provable in a standalone harness:** name-based module loading (`ScreenLoader`/AssemblerBroker) — `Turbine.configure()` NPEs on `getResourceAsStream` because the faked servlet env has no real `getRealPath`. Works in a servlet container; this is exactly the "deploy to real Tomcat 9" rung.
- **Not tested:** pull tools (`$content`/`$link`) and real XNAT data/beans (need the live app + Spring/DB).
- Turbine's `VelocityService` accepts the XNAT-style config keys (`services.VelocityService.resource.loader=custom`, `…custom.resource.loader.class=…`) and drove the custom loader correctly under Velocity 2.4.1.

Files:
- `roleConfiguration.xml` — role interface → default impl for the 6 Fulcrum components.
- `componentConfiguration.xml` — per-component settings.

See `../turbine-service-container.md` for why these 5 (+parser) services moved to Fulcrum and the other 8 stay in `TurbineResources.properties`.

## `TurbineResources.properties` delta (to be applied at Phase 0b)

Enable the YAAFI container and point it at the two XML files:

```properties
services.AvalonComponentService.classname=org.apache.turbine.services.avaloncomponent.TurbineYaafiComponentService
services.AvalonComponentService.componentConfiguration=/WEB-INF/conf/componentConfiguration.xml
services.AvalonComponentService.componentRoles=/WEB-INF/conf/roleConfiguration.xml
# Eagerly initialize the components RunData/screens depend on:
services.AvalonComponentService.lookup=org.apache.fulcrum.factory.FactoryService
services.AvalonComponentService.lookup=org.apache.fulcrum.parser.ParserService
```

Then **remove** the now-Fulcrum service declarations (they are configured via YAAFI XML now, not `services.X.classname`):

```properties
# DELETE — moved to roleConfiguration.xml / componentConfiguration.xml:
#   services.CryptoService.classname=...
#   services.FactoryService.classname=...
#   services.PoolService.classname=...
#   services.GlobalCacheService.classname=...
#   services.UploadService.classname=...
# DELETE — parameter/cookie parsing now provided by Fulcrum ParserService:
#   services.RunDataService.default.parameter.parser=org.apache.turbine.util.parser.DefaultParameterParser
#   services.RunDataService.default.cookie.parser=org.apache.turbine.util.parser.DefaultCookieParser
```

**Keep** (survive as Turbine services, per the survival map): RunData, Servlet, AssemblerBroker, Pull, Template, Velocity, Session — plus XNAT's custom wiring on VelocityService (`CustomClasspathResourceLoader`), AssemblerBroker (`module.packages`), PullService (`$sessionData`), RunDataService (`RestletRunData`). `AvalonComponentService` must init **before** RunDataService (it hosts the parser it depends on) — order it early in the service list.

## XNAT-specific tailoring (decisions baked into the drafts)

| Setting | Value | Why |
|---|---|---|
| `parser/urlCaseFolding` | **`none`** | XNAT request params are case-sensitive camelCase (`search_field`, `topTab`, …); `lower` would break parameter lookup. Highest-impact XNAT-specific choice. |
| `pool/capacity` for `DefaultTurbineRunData` | `512` | Carried from the commented 2.3.3 setting (`TurbineResources.properties:613`). |
| `upload` sizes | 20 MB / 10 MB | Aligned with the Spring multipart defaults in `XnatWebAppInitializer`; real limits come from `xnat-conf.properties`. |
| `crypto`, `upload` roles | included, **flagged trim** | Crypto is only reached via the vestigial security service; upload is used only by `UploadBatch.java`. |

## Open items to verify (Phase 0b, before going live)

1. **Exact Fulcrum FQCNs/versions** Turbine 5.1 resolves (some impls may live in an `.impl` subpackage). Boot test catches mismatches.
2. **Security service** — `DBSecurityService` is removed; how the passive stub is expressed in 5.1 (via `turbine-om.properties` and/or a Fulcrum Security role) is still unconfirmed. Not in these drafts yet.
3. **`parameterEncoding` = utf-8** vs XNAT's historical ISO-8859-1 usage (`SecureAction.java:102`) — confirm request-param decoding is unchanged.
4. **`automaticUpload` = true** interaction with the Spring/Restlet upload paths — may need `false`.
5. Whether **Crypto/Upload/Session** can be dropped entirely (the trim spike from `turbine-service-container.md`).

Validation path: `roleConfiguration.xml` + `componentConfiguration.xml` → the Appendix C **Rung 2** `TurbineConfig` boot test asserts every service (including these YAAFI components) initializes.
