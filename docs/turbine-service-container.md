# Turbine Service Container: XNAT's 13 Services and the 4.0 Config Migration

> Companion to `tomcat10-jakarta-upgrade-plan.md` (Phase 0b) and `xnat-web-framework-architecture.md`. Explains what Turbine's service container is, the exact 13 services XNAT configures and why, and why the 2.3.3→4.0 config-format change makes this the highest-risk Phase 0b artifact. Claims cite `file:line`; config paths are under `xnat-web/src/main/webapp/WEB-INF/conf/`.

## Why a service-container config is needed at all

Turbine is a **service-oriented framework**: it boots a set of singleton **services** (managed by a ServiceBroker with init/shutdown lifecycle), and the `/app/*` UI is built entirely on them. A single screen render is a pipeline across several services:

```
request → RunDataService (per-request state)
        → AssemblerBrokerService (load the screen class by name)
        → PullService (inject $content/$link/… into the context)
        → VelocityService (render the .vm to HTML)
        → response
```

So the service config is not optional glue — it *is* the engine wiring. **If the config is invalid Turbine won't start; if a required service is missing, screens won't render.**

## Why a *new* config (the 2.3.3 → 4.0 change)

- **Turbine 2.3.3** configures services with flat properties — `services.X.classname=…` in `TurbineResources.properties`. One file, one format.
- **Turbine 4.0** rewrote the service layer to delegate the reusable services to **Fulcrum** (a library of standalone **Avalon** components), hosted in an embedded Avalon container called **YAAFI** (Yet Another Avalon Framework Implementation). Avalon components are wired via **XML**, not flat properties:
  - `roleConfiguration.xml` — maps a role interface → implementation class
  - `componentConfiguration.xml` — per-component configuration/parameters

Services that moved to Fulcrum can no longer be declared with `services.X.classname=…`; they must be re-expressed as Avalon components in the YAAFI XML. That split — some services stay in `TurbineResources.properties`, some move to XML, and YAAFI itself must be enabled — is *why* a YAAFI/Fulcrum config is required, and why it is the least-documented, highest-risk artifact in Phase 0b (there is no authoritative Turbine 2.3.3→7.0 migration doc).

> **A first-pass draft of that config exists:** `turbine5-config-draft/` (`roleConfiguration.xml`, `componentConfiguration.xml`, and a `README.md` with the `TurbineResources.properties` delta + XNAT-specific tailoring). It is grounded in the Turbine 2.3.3→4.0 migration wiki and Fulcrum docs, kept out of `WEB-INF/` until Phase 0b, and validated by the Appendix C boot test.

## The 13 services (XNAT's exact set)

12 are declared in `TurbineResources.properties:272-286`; the 13th (Security) in `turbine-om.properties:20,29`. "5.1 fate" is from probing the Turbine 5.1 javadoc (see upgrade plan Appendix C, Finding 2).

| # | Service | Why Turbine uses it | XNAT-specific wiring | 5.1 fate |
|---|---|---|---|---|
| 1 | **RunDataService** | Builds the per-request `RunData` (request/response/session/params); created on every `/app` request | `RestletRunData` (Restlet bridge) | Survives (Turbine) |
| 2 | **VelocityService** | Velocity engine integration: screen + Context → HTML | `CustomClasspathResourceLoader` (`:506-508`) — the template-override hierarchy | Survives |
| 3 | **TemplateService** | Resolves template names → screen/layout/navigation classes, with caching | — | Survives |
| 4 | **PullService** | Injects "pull tools" into every template context | `$link`, `$content`, `$ui`, `$page`, `$dateFormatter`, + custom `$sessionData` = `SerializableSessionData` (`:430-442`) | Survives |
| 5 | **AssemblerBrokerService** | Loads module classes (screens/actions) by name from `module.packages` — how `"Index"` → `Index.java` | XNAT's 3 packages (`:96`) | Survives |
| 6 | **ServletService** | Bridge to `ServletContext`/`ServletConfig` (real paths, mime, init-params) | — | Survives |
| 7 | **SessionService** | Turbine-level tracking of active HTTP sessions | — | Survives |
| 8 | **FactoryService** | Generic object instantiation (used by broker/pool; optional custom classloaders) | — | → **Fulcrum** (YAAFI XML) |
| 9 | **PoolService** | Recyclable-object pooling (e.g. RunData) to reduce GC | — | → **Fulcrum** |
| 10 | **GlobalCacheService** | Application-global key/value cache | — | → **Fulcrum** |
| 11 | **CryptoService** | Pluggable hashing/encryption — historically for SecurityService password hashing | — | → **Fulcrum** |
| 12 | **UploadService** | Parses `multipart/form-data` uploads into params/files | used by `UploadBatch.java` | → **Fulcrum** |
| 13 | **SecurityService** | Users/roles/permissions/ACL | passive no-op stub (`DBSecurityService`+`PassiveUserManager`); real auth is Spring Security | `DBSecurityService` removed → passive stub |

Plus the container: **AvalonComponentService** (commented out at `TurbineResources.properties:272`) must be enabled in 5.1 to host services 8–12.

## Why "mapped to XNAT's *exact* 13"

A stock Turbine config won't do, because 5 services carry XNAT-specific wiring that must survive the translation:

- **VelocityService** → `CustomClasspathResourceLoader` (the whole template-override hierarchy: `templates` → `module-templates` → `xnat-templates` → `xdat-templates` → `base-templates`)
- **RunDataService** → `RestletRunData` (the Restlet↔Turbine bridge)
- **AssemblerBrokerService** → XNAT's 3 `module.packages`
- **PullService** → the custom `$sessionData` tool
- **SecurityService** → the passive stub

Miss any of those and either the container fails to boot or `/app` renders blank/500.

## Trim candidates (verify before writing the YAAFI XML)

Fewer live services = less Avalon config to get right. Three are plausibly redundant and worth a short usage spike before migrating:

- **SessionService (7)** — overlaps Spring Security's `SessionRegistry`.
- **CryptoService (11)** — tied to the vestigial SecurityService.
- **UploadService (12)** — used in only one action (`UploadBatch.java`); XNAT also has Spring multipart + (Restlet) upload paths.

## Bottom line

The upgrade does not change *what* these services do — they remain the runtime engine behind `/app`. It changes *how they are declared* (flat properties → Avalon XML for the 5 migrated services) and forces re-expressing XNAT's five customizations across the new split. That translation, performed without a migration doc, is the upper-band risk that the Appendix C boot test is designed to catch fast.
