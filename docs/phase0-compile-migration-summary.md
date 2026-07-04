# Phase 0 compile migration — error buckets, fixes, and why

**Date:** 2026-07-03 · **Branch:** `feature/turbine-5x` · **Commits:** `224313df9..545abed4e` (10 commits)
**Outcome:** `:xdat` and `:xnat-web` both compile (`compileJava` BUILD SUCCESSFUL) against **Restlet 2.5.2 / Turbine 5.1 / Velocity 2.4.1** on javax / Tomcat 9. Compile-green, **not yet runtime-verified** (see end).

This is a periodic snapshot of the code changes made during the compiler-driven migration, grouped by root cause. Almost every bucket traces to one of three framework redesigns: **Turbine's 4.0 Avalon/Fulcrum rewrite**, **Velocity 2's ResourceLoader SPI**, or **Restlet 2.0's package + `ServerResource` reorg**. The only judgment calls were the two shims in section E.

---

## A. Turbine 5.1 — service-facade removals (the 4.0 rewrite)
Turbine 4.0 replaced static `TurbineXxx` singletons with an Avalon/YAAFI service container; services are now looked up by name.

| Bucket | Fix | Why |
|---|---|---|
| `TurbineVelocity.getContext(data)` (16 sites) | `TurbineUtils.getVelocityContext(pd)` helper → `VelocityService.getContext()` via `TurbineServices` | Static facade deleted in 4.0 |
| `TurbineTemplate.getService()`, `TurbineRunDataFacade.getService()` | `(X) TurbineServices.getInstance().getService(X.SERVICE_NAME)` | Same — facades gone |
| `ScreenLoader/ActionLoader.getInstance().getInstance(name)` | `.getAssembler(name)` | `GenericLoader` API renamed |
| `RunData` manual setters (`setRequest/Response/ParameterParser/CookieParser/ServletConfig/ServerData`) | `RunDataService.getRunData("restlet", …)` (builds + populates internally) | 4.0 moved RunData construction into the service |
| `TurbineSession.getActiveSessions()` | `SessionService` lookup | Static session facade removed |
| `TurbineSecurityException` | `throws Exception` | Security moved to Fulcrum; class gone |

## B. Turbine 5.1 — package / type relocations
| Bucket | Fix | Why |
|---|---|---|
| `org.apache.turbine.util.parser.*` | `org.apache.fulcrum.parser.*` | Parser service extracted to Fulcrum |
| `services.intake.model.Group` | `org.apache.fulcrum.intake.model.Group` | Intake → Fulcrum |
| `ParameterParser.keys()` | `getKeys()` / `Collections.enumeration(...)` | Fulcrum parser API differs from Turbine's |
| `getFileItem(String):FileItem` | `getPart(String):javax.servlet.http.Part` | Fulcrum parser uses Servlet 3 `Part`, not commons-fileupload |
| `Part.write(File)` | `Files.copy(getInputStream, …)` | `Part.write` takes a `String` path, not a `File` |
| ECS `data.getPage().output()`, `isPageSet()` | removed | Turbine 4.0 dropped the ECS Page model |
| `RunData → PipelineData` framework overrides (232 methods) | **codemod**: change signature, inject `RunData x = pipelineData.getRunData()` | 4.0 changed `doBuildTemplate/doPerform/isAuthorized/doOutput/getContentType` to `PipelineData` |

## C. Velocity 2.x — ResourceLoader SPI change
| Bucket | Fix | Why |
|---|---|---|
| `CustomClasspathResourceLoader.getResourceStream(String):InputStream` | `getResourceReader(String,String):Reader` (+ built-in `buildReader`) | Velocity 2 changed the `ResourceLoader` SPI |
| `init(ExtendedProperties)` | `init(org.apache.velocity.util.ExtProperties)` | `ExtendedProperties` removed |

## D. Restlet 1.1 → 2.x — package reorganization (~97 files)
2.0 split the monolithic packages apart.

| Bucket | Fix | Why |
|---|---|---|
| `data.{Request,Response}` | `org.restlet.{Request,Response}` | Promoted out of `data` |
| `resource.{Representation,Variant,*Representation}` | `org.restlet.representation.*` | New package |
| `{Router,VirtualHost,Filter}`, `util.{Template,Variable}` | `org.restlet.routing.*` | New package |
| `com.noelios.restlet.*` | `org.restlet.ext.servlet.*` / `engine.*` | Noelios impl folded into `org.restlet` |
| `ServletCall.getRequest` | `org.restlet.ext.servlet.ServletUtils.getRequest` | Internalized |
| `util.DateUtils` | `engine.util.DateUtils` | Moved |
| Wildcard imports (`data.*`, `resource.*`) | augmented with new-location wildcards | Old wildcards no longer cover moved types |
| `Status` ambiguous | explicit `import org.restlet.data.Status;` | Two wildcards each offered a `Status`; explicit single-type import wins (JLS) |

## E. Restlet 2.x — the semantic model change (the hard part; judgment calls)
2.0 abstracted `Resource` and replaced the 1.1 negotiation model with `ServerResource`.

| Bucket | Fix | Why |
|---|---|---|
| `SecureResource extends Resource` + ~45 subclasses' `represent()/getVariants()/handleX()/allowX()` fail to override | **compatibility shim** — extend `ServerResource`; bridge `get/post/put/delete(Variant)` → the 1.1 hooks; `getAllowedMethods()` consults `allowX()` (preserves 405); no-arg `getPreferredVariant()`; `set/isModifiable/Readable` retained | 1.1 `Resource` model removed; shim avoids rewriting every resource |
| Resources instantiated by `Router.attach(uri, class)` | **`XnatServerResourceFinder`** — a `Finder` that uses the legacy `(Context,Request,Response)` ctor | 2.x default `Finder` needs a no-arg ctor + `init()`; XNAT resources have only the 3-arg ctor |
| `Application.createRoot()` | `createInboundRoot()` | Renamed in 2.0 |
| `Route.setMatchingMode` | `TemplateRoute` (what `attach` returns) | Moved off the `Route` base |
| `Response.setChallengeRequest(x)` | `getChallengeRequests().add(x)` | Singular → list |
| Representation `set/getDownloadName`, `setDownloadable` | `org.restlet.data.Disposition` (filename + `TYPE_ATTACHMENT`) | 2.x models Content-Disposition as an object |

## F. Dependency / classpath
| Bucket | Fix | Why |
|---|---|---|
| `package org.apache.fulcrum.* does not exist` (in `:xdat`) | add `fulcrum-parser` / `fulcrum-intake` deps | `xdat` pulls Turbine with `transitive = false` |
| `NoSuchMethodError` log4j (sandbox, runtime) | align all log4j2 modules to one version; **keep** `log4j-core` | Turbine 5.1 casts to `log4j-core`'s `LoggerContext`; api/core version mismatch breaks it |
| Missing `fulcrum-cache` / `fulcrum-upload` | declare explicitly | Turbine 5.1 does not pull them transitively |

## G. Codemod-quality findings (process notes)
- The RunData codemod initially **missed `doOutput` / `getContentType`** (also `RawScreen` overrides) → added them.
- It could **inject a live statement into a commented-out method** → added a guard that strips injections following a commented `{`.
- Injected **`final` locals** blocked reassignment (`data = …`) → made injected locals non-final.
- **javac `-Xmaxerrs 100`** masked the true error tail → raised via a temp Gradle init script to see the real count (49, not "100+").

---

## Still runtime-unverified (needs a deploy, not a compile)
1. `XnatServerResourceFinder` instantiation path (double-`init()` is expected/idempotent since no resource overrides `doInit()`).
2. `ServerResource` negotiation bridge — `get(Variant)` ↔ `represent()`, and 405 via `getAllowedMethods()`.
3. `RestletRunData("restlet")` wiring + `Response.getCurrent()` in the screen bridge.
4. YAAFI container boot; `turbine-om.properties` SecurityService still points at the removed `DBSecurityService`.
5. log4j2 ↔ Logback reconciliation.

Validation path: Tomcat 9 deploy → `xnat-rest-tests` (REST `/data` + `/xapi`) + golden-master for `/app` Turbine screens.
