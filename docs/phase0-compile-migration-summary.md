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
| `SecureResource extends Resource` + ~45 subclasses' `represent()/getVariants()/handleX()/allowX()` fail to override | **compatibility shim** — extend `ServerResource`; bridge both the variant `get/post/put/delete(Variant)` **and** the no-variant `get()/post(Representation)/put(Representation)/delete()` forms → the 1.1 hooks (the no-variant forms matter for resources that declare no variants — see runtime table); `getAllowedMethods()` consults `allowX()` (preserves 405); no-arg `getPreferredVariant()`; `set/isModifiable/Readable` retained | 1.1 `Resource` model removed; shim avoids rewriting every resource |
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

## Verified since (boot test — `TurbineBootTest`, commit `7889e5a14`)
The Turbine 5.1 service container now **boots XNAT's actual config** (`WEB-INF/conf/TurbineResources.properties`
+ the YAAFI role/component XML) via `TurbineConfig`, outside a servlet container. All 8 services initialize:
AvalonComponentService (YAAFI), VelocityService (Velocity 2.4.1 through the migrated custom loader, incl. the
`macros/TurbineMacros.vm` velocimacro library), TemplateService, RunDataService, AssemblerBrokerService,
ServletService, PullService, SessionService. Fixes it drove: **`fulcrum-yaafi`** dep (Turbine ships it
`<optional>`), and **disabling the vestigial Turbine SecurityService** (`turbine-om.properties` only declared
the removed `DBSecurityService`; XNAT uses Spring Security).

---

## Runtime bring-up (Tomcat 9 + PostgreSQL + ActiveMQ deploy)
Everything below was compile-green and boot-test-green but surfaced only on a real deploy, one at a
time. Each is a Turbine-4.0-rewrite behavior/config delta that XNAT's 2.3.3 config predated. The
[deploy stack](tomcat9-deploy-stack.md) + Logback→console change made these findable.

### Service-container init
| Symptom | Fix | Root cause |
|---|---|---|
| `NoClassDefFoundError …yaafi…ServiceContainerConfiguration` at boot | add `fulcrum-yaafi:2.0.1` | Turbine ships the YAAFI container as an `<optional>` dep → not transitive |
| `InitializationException: …DBSecurityService is unavailable` | comment `include = turbine-om.properties` | Turbine 4.0 moved security to Fulcrum; XNAT's `DBSecurityService` is gone (and vestigial — XNAT uses Spring Security) |
| Servlet init: `IllegalArgumentException: is parameter must not be null` (JAXB) at `Turbine.configure` | add `WEB-INF/conf/turbine-classic-pipeline.xml` | 4.0 replaced the hard-coded `doGet()` flow with a configurable Valve pipeline loaded via JAXB |
| First `/app` render: `unknown service org.apache.fulcrum.security.UserManager` | wire in-memory Fulcrum turbine security (`fulcrum-security-memory` + 7 YAAFI roles) | `PullService.populateContext()` unconditionally looks up a `TurbineUserManager` per render |

### Velocity / templates
| Symptom | Fix | Root cause |
|---|---|---|
| `NoSuchMethodError: MethodUtils.getMethodObject` → `MethodMap.<clinit>` on first `$obj.method()` | `commons-lang3` **3.11 → 3.17** | Velocity 2.4.1 needs `getMethodObject` (lang3 3.15+); XNAT force-pinned 3.11 |
| Login page renders literal `$page.addAttribute(...)` | swap 5 templates to `$page.addBodyAttribute(...)` | Turbine 4.0 renamed `HtmlPageAttributes.addAttribute` → `addBodyAttribute`; Velocity echoes unresolved refs |
| `$ui.*` (skins) — `ClassNotFoundException …pull.util.UIManager` | `tool.global.ui` → `pull.tools.UITool` + declare `services.UIService` | 4.0 moved skin handling from the UIManager pull tool to a dedicated `UIService` |
| Every login → "Custom site login landing page cannot be found!" | `TurbineUtils.resourceExists()` → `CustomClasspathResourceLoader` | the Velocity-singleton issue below |
| Open any edit/report/search screen → "Could not find screen for null" | route all **20** `Velocity.resourceExists()` sites (`XDATActionRouter.passToScreen`, `ElementDisplay`, `ElementSecurityWizard`, `BaseElement`, `DisplayItemAction`, `EditItemAction`, `SearchResults`, `UserGroupManager`, `XdatStoredSearch`) through `TurbineUtils.resourceExists()` | same singleton issue — `passToScreen()` never set a screen template because every existence check returned false |
| Render-via-singleton: email bodies, item text, group-permission SQL | new `VelocityUtils.render(context, name)` → `VelocityService.handleRequest()`; routed `AdminUtils.populateVmTemplate`, `BaseElement.output`, `UserGroupManager` through it | `Velocity.getTemplate(name)` on the singleton can't find XNAT templates (same disconnect). Dev-only `Velocity.evaluate(rawString)` calls kept (self-contained strings, no loader needed) |

> **⚠ Recurring pattern — the `org.apache.velocity.app.Velocity` singleton is disconnected.** In Velocity 2 /
> Turbine 5.1 the `VelocityService` owns its own `VelocityEngine`; the static `Velocity` singleton is a
> *separate, unconfigured* engine (in 1.7/2.3.3 they were the same). So **any** `Velocity.*` static call is
> disconnected from the templates XNAT actually renders. All named-template usages — existence checks
> (`resourceExists`) and renders (`getTemplate`) — are now routed through the VelocityService/its loader.
> Rule of thumb: grep for `org.apache.velocity.app.Velocity` — every remaining hit is suspect (only
> `BaseElement`'s dev-only `evaluate(rawString)` and `VelocityUtils.init()` remain).

### Request handling / dispatch
| Symptom | Fix | Root cause |
|---|---|---|
| `#parse("/screens/${schemaElement.getSQLName()}_search.vm")` — literal `${…}` → ResourceNotFound | parser `urlCaseFolding` **none → lower** | XNAT's `GetPassedParameter` lowercases keys, so it relies on the parser folding param **names** to lowercase (2.3.3 default); `none` broke path-info/param lookups app-wide |
| Post-login redirect: `Page not found: Default` | `services.TemplateService.default.extension = vm` | generic `getDefaultPage()` resolves `default.template + "." + default.extension`; unset extension → bare "Default" (no engine) |
| Action redirect (Create Project) → blank 200, no 302, record saved | reorder pipeline: `DetermineRedirectRequestedValve` **after** `ExecutePageValve` | the action runs in `ExecutePageValve`; the redirect valve sends at the *start* of `invoke()` (before `invokeNext`), so placed earlier it checked before the action set the URI — affected **all** redirect-after-save actions |
| Any empty-target `/app` request (site root `/` → `serverRoot + "/"` → `/app`; bare `/app`; server-side redirects) → "Couldn't map Template null to any Screen class!" | **`DefaultHomepageTargetValve`** (after `DetermineTargetValve`): when the screen target *and* action are empty, default the screen template to `template.homepage` (Index.vm). Plus `default.jsp` welcome file (302 root → `/app/template/Index.vm`) as belt-and-suspenders | Turbine 4.0 **dropped the `template.homepage`/`screen.homepage` auto-default** — the constants still exist but no 5.1 class reads them, so any empty-target `/app` request no longer resolved to Index.vm. Valve restores it at the source; boot test guards the wiring |
| Subjects tab / any data-table search → "Failed to create search results."; log shows `SearchResource … SAXParseException: Content is not allowed in prolog` | `SearchResource.extractSearchXml()` — strip the prepended form params, take from the `<?xml` marker (URL-decode if percent-encoded) before SAX | YUI Connect prepends the POST URL's query string to the body, so the entity is `XNAT_CSRF=…&format=json&…&<encoded XML>`, not bare XML; Restlet 1.1 tolerated it, 2.x `getText()` returns the whole thing. Client + `handlePost` reader are unchanged from `develop` — only the entity content differs |
| Any write endpoint on a resource that declares **no** variants → `405 Method Not Allowed` (e.g. prearchive `delete`/`move`/`rebuild`, `/REST/services/prearchive/delete`). No server-side exception logged | Override the **no-variant** `post(Representation)`/`put(Representation)`/`delete()` in `SecureResource` to delegate to the variant forms (→ `handlePost/handlePut/handleDelete`), mirroring `get()`/`get(Variant)` | The 2.x shim overrode only the `(Representation, Variant)` write hooks. `doNegotiatedHandle()` takes the variant path **only when `getVariants()` is non-empty**; with no variants it falls to the no-arg `post(entity)`/`delete()`, whose `ServerResource` defaults require an `@Post`/`@Delete` annotation (XNAT has none) and otherwise 405. `SearchResource` seeds 3 variants so it worked; the prearchive batch actions seed none, so they didn't |
| File up/download by name and other trailing-path URIs → `404 Not Found` from Restlet (e.g. `PUT .../resources/{ID}/files/readme.txt`), even though the resource exists; **and** with only the matching-mode restored, subject/other writes 404/misroute | Restore **both** Restlet 1.1 router defaults in `XNATApplication.createInboundRoot()`: `securedRouter.setDefaultMatchingMode(Template.MODE_STARTS_WITH)` **and** `securedRouter.setRoutingMode(Router.MODE_BEST_MATCH)` | 2.x changed both defaults: matching `STARTS_WITH→EQUALS` and routing `BEST_MATCH→FIRST_MATCH`. Resources read the trailing path via `getRemainingPart()` (filename, catalog subpath, DICOMDIR), so routes are registered at the prefix and need `STARTS_WITH`. But under `STARTS_WITH` many routes match a URI as a prefix, so `BEST_MATCH` (most specific wins) is also required — with `FIRST_MATCH` the first-attached prefix route intercepts (e.g. `.../subjects/{ID}` grabbed by `.../subjects` or `.../projects/{ID}`). Under the old `EQUALS` default only one route matched, so the missing `BEST_MATCH` was masked |
| Any response that sets a custom header (e.g. file download → `Cache-Control`; forced download → `Content-Disposition`) → `500` **after the body is written** (`Connection: close`); no app-logged error — only a `SEVERE` in Tomcat's `localhost.<date>.log`: `ClassCastException: org.restlet.data.Parameter cannot be cast to org.restlet.data.Header` at `HeaderUtils.addExtensionHeaders` | In `SecureResource.setResponseHeader`/`setContentDisposition`, store the `org.restlet.http.headers` attribute as a `Series<Header>` (`new Series<>(Header.class)`), not a `Form`/`Series<Parameter>` | Restlet 1.1's response-headers attribute was a `Form` (`Series<Parameter>`); 2.x expects `Series<Header>` and iterates it as `Header` at commit time, so the old `Form` blows up in the connector *after* the entity is committed. Restlet logs via JUL (routed to Tomcat's `localhost` log, not the app console), which is why it looked like a silent 500. NB: `Cache-Control`/`Content-Disposition` are in Restlet's `STANDARD_HEADERS` (warned + skipped) — the type fix stops the 500; to actually emit them use the typed `CacheDirective`/`Disposition` APIs (see next row) |
| `Cache-Control` and `Content-Disposition` still not emitted after the type fix — a file download served with `must-revalidate`/attachment name showed neither header (Restlet logs `"... is not allowed as such"` and drops them) | `setResponseHeader` routes `"Cache-Control"` to `getResponse().getCacheDirectives().add(new CacheDirective(...))`; `setContentDisposition` records a typed `org.restlet.data.Disposition` (`TYPE_ATTACHMENT`/`TYPE_INLINE` + filename), which the `get/post/put/delete` bridges stamp onto the returned representation via `applyDisposition()` | Both headers are in Restlet 2.x's `STANDARD_HEADERS` set, so `HeaderUtils.addExtensionHeaders` refuses them in the raw header `Series` — they must go through the typed connector APIs, which Restlet re-serializes to the wire header at commit time |

### Ops / observability
| Symptom | Fix | Root cause |
|---|---|---|
| XNAT errors invisible in `docker logs` (only Turbine's showed) | add a `ConsoleAppender` to every Logback logger + root | XNAT logs SLF4J→Logback to per-area **files** under `${xnat.home}/logs`; only Turbine's log4j2 reached stdout. This is the container side of the log4j2↔Logback item |
| After a container restart, logging in (with Chrome DevTools open) lands on Tomcat's 404 for `/.well-known/appspecific/com.chrome.devtools.json` instead of the homepage | `SecurityConfig` now installs an `HttpSessionRequestCache` whose matcher **skips** `/.well-known/**`, favicon, apple-touch-icon so those can't become the saved request | Not a migration regression — the default request cache saves *every* unauthenticated request and overwrites on each, and `SavedRequestAwareAuthenticationSuccessHandler` redirects to the last one saved. Chrome's DevTools probe fires unauthenticated while the login page is up, becomes the saved request, and wins the post-login redirect |

**Verified working end-to-end:** boot, login/auth, Velocity 2 screen render (with `$ui`/skins, `$page`,
pull tools), and a full **create → save → 302 → report** action workflow.

## Still unverified
1. ~~Restlet `/data` REST surface~~ — **CLOSED.** `SecureResource`→`ServerResource` shim, 405/no-variant bridge, router matching+routing defaults, content negotiation (json/xml/html), and custom-header emission all exercised via the known-state fixture + golden-master (`docs/goldens/`). See the Request-handling and header rows above.
2. ~~`ZipRepresentation` Disposition (zip/tar download); multipart upload~~ — **CLOSED** by the typed `Disposition` fix (Content-Disposition now emits) + known-state file up/download. `RestletRunData` screen bridge exercised via `/app` render.
3. ~~`/data` POST/PUT with an XML body~~ — **CLOSED.** `SearchResource.extractSearchXml()` strips the YUI-prepended form params before SAX (see dispatch table); data-table search verified against the fixture.
4. ~~Turbine security realm is empty (`isAnonymousUser` always true)~~ — **NOT A BUG (vestigial).** XNAT's user/permission stack is fully decoupled from the Turbine realm: `$user` (150 templates) and `TurbineUtils.getUser(RunData)` both resolve via `XDAT.getUserDetails()` → `SecurityContextHolder` (Spring Security); `$user.checkFeature/isSiteAdmin/checkRole/isGuest` and `$turbineUtils.isSiteAdmin/canEdit/canDelete(user,…)` all take the Spring `UserI` explicitly. Templates call the Turbine realm user `data.getUser()` **0 times** (the only Java reference is a commented-out line). The empty realm is never consulted by any security path — safe to leave, and no Turbine-user↔Spring-user bridge is needed.
5. Breadth (remaining): a handful of `/app` screens + a couple of action workflows exercised so far. Email + item-text render paths are wired to VelocityService but not yet exercised end-to-end.

Validation path: continue the [smoke-test checklist](tomcat9-deploy-stack.md) / `./docker/health-check.sh`,
then automated regression — `xnat-rest-tests` (REST `/data` + `/xapi`) + golden-master (`docs/tools/golden_master.py`) for `/app` screens.
