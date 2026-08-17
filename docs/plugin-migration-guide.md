# Plugin Migration Guide

This guide covers the changes required in external XNAT plugin projects to build against the monorepo-published artifacts (1.10.0+).

> Verified against: `dicom-query-retrieve` and `container-service`.

---

## Quick Diagnosis

Run these four commands in sequence to find all issues before fixing anything:

```bash
# 1. Dependency resolution — finds null versions and missing modules
./gradlew dependencies --configuration compileClasspath 2>&1 | grep -E "null|FAILED"

# 2. Main compile — finds missing packages/symbols
./gradlew compileJava 2>&1 | grep "package.*does not exist\|cannot find symbol" | sort | uniq

# 3. Test compile
./gradlew compileTestJava 2>&1 | grep "package.*does not exist"

# 4. Test runtime — finds compileOnly deps missing at runtime
./gradlew test --info 2>&1 | grep "ClassNotFoundException"
```

---

## 1. Remove All `importedProperties` Usage

The old Maven `parent` POM had a `<properties>` block; external plugins read version strings from it via the Spring Dependency Management plugin:

```groovy
def vActiveMQ = dependencyManagement.importedProperties["activemq.version"]
```

A Gradle `java-platform` BOM has no `<properties>` block. Every `importedProperties` call returns `null`, causing errors like `Could not find org.apache.activemq:activemq-all:null`.

**Fix:** delete the variable and remove the version string from the dependency declaration — the BOM manages it.

```groovy
// Before
def vActiveMQ = dependencyManagement.importedProperties["activemq.version"]
implementation "org.apache.activemq:activemq-all:${vActiveMQ}"

// After
implementation "org.apache.activemq:activemq-all"
```

Common `importedProperties` keys and their replacements:

| Old key | Replacement |
|---------|-------------|
| `activemq.version` | Remove version — BOM has `org.apache.activemq:activemq-all` |
| `jackson.version` | Remove version — BOM has `com.fasterxml.jackson.*` |
| `gson.version` | Remove version — BOM has `com.google.code.gson:gson` |
| `spring-security.version` | Remove version — BOM has `org.springframework.security:*` |
| `dcm4che.version` | Remove variable if unused |
| `commons-cli.version` | Remove version — BOM has `commons-cli:commons-cli` |
| `lombok.version` / `lombok.checksum` | See section 2 below |

---

## 2. Replace the Lombok Plugin with Plain Dependencies

The `io.franzbecker.gradle-lombok` plugin downloads Lombok independently and does not read
the BOM — its `lombok { version / sha256 }` values must be spelled out, so every Lombok
upgrade means touching every plugin. Drop the plugin and declare Lombok as ordinary
dependencies instead; the BOM supplies the version (the Spring Dependency Management
plugin applies managed versions to all configurations, including `annotationProcessor`):

**Remove** the plugin line and the whole `lombok {}` block — in either of the forms it
may appear in (the `importedProperties` variant resolves to `null` and fails; the
hardcoded variant works but couples every plugin to the Lombok version):

```groovy
// Remove:
plugins {
    id "io.franzbecker.gradle-lombok" version "5.0.0"   // ← delete
}
lombok {                                                // ← delete the whole block,
    version = "1.18.34"                                 //   whether hardcoded like this
    sha256 = "1ea5ad..."                                //   or reading importedProperties
}
```

**Add** four plain dependencies instead — no version, no checksum:

```groovy
dependencies {
    compileOnly "org.projectlombok:lombok"
    annotationProcessor "org.projectlombok:lombok"
    testCompileOnly "org.projectlombok:lombok"
    testAnnotationProcessor "org.projectlombok:lombok"
}
```

The BOM pins Lombok (currently 1.18.34), so future upgrades are a single version-catalog
change in the monorepo and plugins follow automatically. This is the same pattern the
monorepo's own modules use (`buildlogic.java-common-conventions`).

---

## 3. Remove Merged-Module Dependencies

Five satellite modules were merged into `org.nrg.xnat:web` and are no longer published separately. Declaring them causes `FAILED` resolution errors.

| Remove this dependency | Classes are now in |
|------------------------|-------------------|
| `org.nrg.xnat:xnat-data-models` | `org.nrg.xnat:web` |
| `org.nrg:session-builders` | `org.nrg.xnat:web` |
| `org.nrg:ecat4xnat` | `org.nrg.xnat:web` |
| `org.nrg:dicom-xnat-mx` | `org.nrg.xnat:web` |
| `org.nrg:prearc-importer` | `org.nrg.xnat:web` |

```groovy
// Remove — no longer published
implementation "org.nrg.xnat:xnat-data-models"
implementation "org.nrg:dicom-xnat-mx"
```

These classes are already available through `implementation "org.nrg.xnat:web"`.

---

## 4. Declare Dependencies No Longer Transitively Available

In the old multi-repo setup, `xnat-web` declared many libraries as `api`, leaking them onto plugin compile classpaths. The monorepo uses `implementation` for most of these, so plugins that directly use those classes must now declare them explicitly.

**Symptom:** `compileJava` reports `package org.apache.velocity.context does not exist` or `cannot find symbol`.

These are all provided by XNAT at runtime, so declare them as `compileOnly`:

```groovy
// Provided by XNAT at runtime — compileOnly keeps them off the published POM
compileOnly "javax.servlet:javax.servlet-api"        // HttpServletRequest, etc.
compileOnly "javax.jms:javax.jms-api"                // Destination, etc.
compileOnly "org.apache.velocity:velocity"            // Context, etc.
compileOnly "commons-lang:commons-lang"               // commons-lang 2.x (not lang3)
compileOnly "commons-fileupload:commons-fileupload"
compileOnly "fop:fop"                                 // group is fop, not org.apache.fop
```

Add only what your plugin actually imports — not all plugins need all of these.

> **`fop` group name:** the Maven coordinates are `fop:fop`, not `org.apache.fop:fop`.

---

## 5. Add Missing Test-Scope Dependencies

`compileOnly` dependencies are absent from the test compile and runtime classpath. If test code uses the same classes, declare them separately:

```groovy
testImplementation "javax.servlet:javax.servlet-api"
testImplementation "commons-lang:commons-lang"
```

**Symptom:** `compileTestJava` reports `package ... does not exist`, or tests fail with `ClassNotFoundException`.

---

## 6. Dependencies Not in the BOM

Some libraries are in the monorepo version catalog but not in the published `org.nrg:parent` BOM. Declaring them without a version produces `Could not find group:artifact:.` (empty version, not `null`).

**Known case: `xalan:xalan`**

```groovy
// Before — no version, not in BOM → build fails
implementation ("xalan:xalan") { transitive = false }

// After — hardcode the version
implementation ("xalan:xalan:2.7.2") { transitive = false }
```

---

## 7. `bomProperties` Is a No-Op

```groovy
// Has no effect under a Gradle java-platform BOM, but does not cause errors
bomProperties(["javassist.version": vJavassist])
```

This can be left in place or removed — it will not break the build.

---
---

# Part 2 — Jakarta EE / Tomcat 10 Migration (targeting 1.11.0+)

Everything above gets a plugin building against the **javax** monorepo (1.10.x). This part covers the
next jump: building against the **Jakarta / Tomcat 10** line (1.11.0‑SNAPSHOT+), where the core
frameworks moved up — **Restlet 1.1 → 2.6, Turbine 2.3.3 → 7.0, Velocity 1.7 → 2.4.1, Spring 5 → 6,
Spring Security 5 → 6, Hibernate 5 → 6, `javax.*` → `jakarta.*`, Java 17+/21**. A plugin that touches
any of the legacy frameworks (Restlet `/REST` resources, Turbine `/app` screens/actions, Velocity
templates, Hibernate entities) needs source changes, not just a version bump.

> Worked example: `xnatx/audit_trail_plugin` on branch `feature/tomcat10` — a small plugin with two
> Restlet resources + four Turbine screens; its diff is the canonical reference for the steps below.

## J1. Build script

```groovy
buildscript { ext { vXnat = "1.11.0-SNAPSHOT" } }        // the jakarta line
plugins { id "org.nrg.xnat.build.xnat-data-builder" version "${vXnat}" }
dependencyManagement.imports { mavenBom "org.nrg:parent:${vXnat}" }
```

**Migrated dependency coordinates** — the artifact *coordinates* changed, not just versions, so old
ones resolve to an empty version (`Could not find turbine:turbine:`):

| Old coordinate | New coordinate |
|---|---|
| `turbine:turbine` | `org.apache.turbine:turbine` (7.0) |
| `org.apache.velocity:velocity` | `org.apache.velocity:velocity-engine-core` (2.4.1) |
| `org.restlet:org.restlet` | *unchanged coordinate*, now 2.6.0 (from the BOM) |

**`mavenLocal()` — scope it to `org.nrg.*`.** You need it to consume a locally‑published SNAPSHOT of
core, but an unscoped `mavenLocal()` will shadow *partial* third‑party artifacts (see the
`ehcache‑*‑jakarta` gotcha in the catalog). Always:

```groovy
repositories {
    mavenLocal { content { includeGroupByRegex 'org\\.nrg.*' } }
    // …the usual XNAT/Maven Central repos…
}
```

**Java toolchain — pin 21.** The Gradle daemon may run a newer JDK (e.g. 25) that Lombok can't
process. Match core:

```groovy
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
compileJava { options.fork = true }   // a cross-JDK toolchain must fork
```

**Fulcrum deps for Turbine screens.** Turbine 7 no longer pulls the Fulcrum suite transitively, and if
a screen calls `data.getParameters()` it touches `org.apache.fulcrum.parser.ParameterParser` (whose
supertype `Recyclable` lives in `fulcrum-pool`). These are provided by the XNAT runtime, so `compileOnly`:

```groovy
compileOnly "org.apache.fulcrum:fulcrum-parser:4.0.0"
compileOnly "org.apache.fulcrum:fulcrum-pool:1.0.5"
```

## J2. Source changes

**Restlet 1.1 → 2.6 import remap** (in every `SecureResource`/`ServerResource` subclass):

| Restlet 1.1 import | Restlet 2.6 import |
|---|---|
| `org.restlet.data.Request` | `org.restlet.Request` |
| `org.restlet.data.Response` | `org.restlet.Response` |
| `org.restlet.resource.Representation` | `org.restlet.representation.Representation` |
| `org.restlet.resource.Variant` | `org.restlet.representation.Variant` |
| `org.restlet.data.MediaType` / `Status` / `org.restlet.Context` | *unchanged* |

**Turbine `RunData` → `PipelineData`** for framework‑override methods whose base signature changed
(`doBuildTemplate`, `doPerform`, `isAuthorized`; screen `finalProcessing` kept `RunData`):

```java
// before
protected void doBuildTemplate(RunData data, Context context) throws Exception { … }
// after
protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
    RunData data = pipelineData.getRunData();   // inserted; body unchanged
    …
}
```
Import `org.apache.turbine.pipeline.PipelineData`. Leave XNAT's own `RunData` helper methods alone —
only the framework overrides change. **Why it bites silently:** `RunData extends PipelineData`, so a leftover
`doBuildTemplate(RunData, Context)` still *compiles* (it's a more‑specific overload), but the framework only ever
calls the `(PipelineData, Context)` method — your `(RunData, Context)` override is **dead code, never invoked**
(the screen renders nothing custom / the action never runs), with no compile error to warn you. Grep every plugin
Turbine module for `doBuildTemplate(.*RunData`/`doPerform(.*RunData` after migrating. *(dicom-query-retrieve, item 1‑31)*
Other Turbine renames you may hit: `TurbineVelocity.getContext(data)`
→ `TurbineUtils.getVelocityContext(data)`; `ActionLoader.getInstance().getInstance(x)` → `.getAssembler(x)`;
`org.apache.turbine.util.parser.ParameterParser` → `org.apache.fulcrum.parser.ParameterParser`. **Multipart
uploads changed model in fulcrum‑parser 4.0.0:** `ParameterParser.getFileItem(String)` → commons‑fileupload
`FileItem` is **gone** — use `getPart(String)` → `jakarta.servlet.http.Part`. Port `FileItem.write(File)` to
copying `part.getInputStream()` with `Files.copy(in, temp.toPath(), REPLACE_EXISTING)` rather than `Part.write()`
(whose target path is resolved relative to the servlet `MultipartConfig` location, not an absolute temp path);
`part.delete()` throws `IOException` (the old `FileItem.delete()` didn't) — wrap it. *(dicom-query-retrieve, item 1‑31)*

**`javax.*` → `jakarta.*`** for the EE APIs the plugin uses directly: `javax.servlet` → `jakarta.servlet`,
`javax.mail` → `jakarta.mail`, `javax.jms`, `javax.persistence` → `jakarta.persistence`, `javax.validation`
→ `jakarta.validation`, `javax.xml.bind` → `jakarta.xml.bind`, `javax.inject` (use `jakarta.inject-api:2.x`).
**Do not** rewrite `javax.annotation.Nonnull` (JSR‑305), JDK `javax.*` (`javax.sql`, `javax.naming`), or
JCache `javax.cache` — those stay.

---

## Behavior‑Change Catalog

The compile is the easy part. These are **runtime behavior changes** in the upgraded dependencies that
caused real, silent failures during the core migration — the ones a plugin is most likely to trip over.
Format: **what changed → symptom → fix**. References are commits / `tomcat10-upgrade-status.md` items.

### Spring Security 6
- **SecurityContextHolder is no longer auto‑persisted to the session.** SS5's
  `SecurityContextPersistenceFilter` saved it at end‑of‑request; SS6 removed that. → A *programmatic*
  login (`SecurityContextHolder.getContext().setAuthentication(...)`, e.g. email verification / password
  reset / SSO callback) authenticates for that request only and is **lost on the next request** — the user
  silently reverts to guest. → Persist it explicitly: save to the session under
  `HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY`, or use `SecurityContextRepository.saveContext(ctx, req, res)`. *(item 1‑20 / `XDAT.loginUser`)*
- **`authorizeRequests` vs `authorizeHttpRequests`.** `authorizeHttpRequests` builds an
  `AuthorizationFilter` that a `FilterSecurityInterceptor` metadata post‑processor never sees. → If your
  plugin's security rules are applied by post‑processing the filter chain, they vanish → redirect loop. →
  Keep `authorizeRequests` until the SS7/`AuthorizationManager` port. *(item 1‑2)*
- **Default dispatcher types gained FORWARD + INCLUDE.** → Filters that early‑return on
  `response.isCommitted()` (e.g. `ChannelProcessingFilter`) now silently drop mid‑page `include`s once the
  response commits → blank SPA/JSP fragments. → Restore the SS 5.7 dispatcher set in your
  `SecurityWebApplicationInitializer`. *(item 1‑15)*
- **`WebSecurityConfigurerAdapter` removed** → component‑style `@Bean SecurityFilterChain` + explicit
  `@Bean AuthenticationManager`. **`ObjectPostProcessor`** moved packages in SS 6.4.

### Spring 6 (core / MVC)
- **`WebMvcConfigurerAdapter` removed** (deprecated since Spring 5) → `implements WebMvcConfigurer`. Its
  methods are now `default`, so existing overrides (`configureMessageConverters`, etc.) still apply
  unchanged. Sibling of the SS6 `WebSecurityConfigurerAdapter` removal above — a plugin with standalone
  MVC/security test configs hits **both**, and only at `compileTestJava` (main code is unaffected).
  *(container-service `*RestApiTestConfig`)*
- **Parameter names no longer read from bytecode.** Spring 6.1 dropped
  `LocalVariableTableParameterNameDiscoverer`. → `@RequestParam`/aspects that rely on parameter names fail
  to bind. → Compile with **`javac -parameters`**. *(item 1‑1)*
  - **Plugins with a standalone build must opt in themselves.** XNAT core sets this in its build
    convention (`buildSrc/.../buildlogic.java-common-conventions.gradle`), but a plugin repo has its own
    Gradle build and does *not* inherit it. Symptom: the plugin compiles fine and loads, but a controller
    whose handler args omit an explicit name 500s at request time —
    `IllegalArgumentException: Name for argument of type [String] not specified … Ensure that the compiler
    uses the '-parameters' flag`. container‑service's `GET /xapi/commands/available` did exactly this, and
    because the project‑report page polls it, the 500 cascaded into UI instability (locators never "stable").
    → add `tasks.withType(JavaCompile).configureEach { options.compilerArgs << '-parameters' }`. Verify with
    `javap -v <Controller>.class | grep -c MethodParameters` (0 = missing the flag). *(container-service)*
- **Trailing‑slash URL matching disabled.** → JS that PUTs to `/xapi/foo/{id}/` gets 404; saves silently
  no‑op. → `configurer.setUseTrailingSlashMatch(true)` (deprecated; revisit at Spring 7). *(item 1‑15)*
- **By‑name bean resolution + `-parameters`.** Spring 6's by‑name shortcut can suddenly match a same‑named
  stray/stub bean once parameter names are exposed. → surprise autowire of the wrong bean. *(test‑suite fix)*
- **`byte[]` responses.** If you *replace* the MVC message‑converter list you drop the default
  `ByteArrayHttpMessageConverter`; a `byte[]`/`ResponseEntity<byte[]>` endpoint then serializes as base64
  via Jackson. → register `ByteArrayHttpMessageConverter` first. *(springdoc fix)*
- **`@Autowired` on a `@Bean` method is now a hard error.** Spring 5 tolerated (and deprecated) marking a
  `@Bean` factory method `@Autowired`; Spring 6 rejects it at context-parse time:
  `BeanDefinitionParsingException: @Bean method 'x' must not be declared as autowired; remove the
  method-level @Autowired annotation`. This is a **boot** failure the compiler can't see, and because it
  fails the plugin's `@Configuration` parse it takes the whole ROOT context down (`Context [] startup
  failed` → REST 404). → just delete the `@Autowired` — a `@Bean` method's parameters are autowired by type
  automatically, so it was always redundant. Grep the plugin for `@Autowired` immediately above `@Bean`.
  *(query_tracker_plugin)*
- **`HttpStatus` → `HttpStatusCode` return types.** Spring 6 introduced the `HttpStatusCode` interface and
  changed methods that used to return the `HttpStatus` enum to return it — notably
  `ResponseEntity.getStatusCode()` and `ClientResponse.statusCode()`. Compile error:
  `incompatible types: HttpStatusCode cannot be converted to HttpStatus`. `value()` moved to
  `HttpStatusCode`, but `getReasonPhrase()`/`series()`/the enum constants stay on `HttpStatus`. → keep the
  variable as `HttpStatusCode` where you only need `value()`; where you need `getReasonPhrase()` or the enum,
  resolve it back with `HttpStatus.resolve(code.value())` (may be null for non‑standard codes, so guard it) —
  don't cast, a `ResponseEntity` can now hold a custom code. *(ohif-viewer-xnat-plugin)* Note `xsync` used the
  reverse (`HttpStatus.valueOf(resp.getStatusCode().value())`) where a `List<HttpStatus>.contains` / `.series()` /
  an exception ctor needed a real `HttpStatus` — same idea, pick the direction the downstream call demands.
- **`HttpMethod` is a final class now, not an `enum`.** Spring 6 turned `org.springframework.http.HttpMethod`
  into a final class, which breaks two idioms with a bare `cannot find symbol`: (a) `HttpMethod.resolve(String)`
  was **removed** → use `HttpMethod.valueOf(String)` (returns a non‑constant `HttpMethod` for an unknown method
  instead of `resolve()`'s `null`); (b) you can no longer `switch (method) { case GET: … }` — the `case` labels
  fail because they're no longer enum constants. → replace the switch with an `if/else` chain on
  `HttpMethod.GET.equals(method)` etc. (an unsupported method then falls through to the `else`, matching the old
  `default`). *(xsync, item 1‑32)*

### Restlet 2.6
- **Default success status 200 → 204** for handlers that finish with a bodyless/empty‑entity OK. → clients
  that hard‑check `== 200` (pyxnat/XNATpy, upload scripts, test harnesses) break. → mark bodyless‑OK and
  restore `SUCCESS_OK` after Restlet's rewrite (the `okParity` pattern). *(200→204 item)*
- **`x‑www‑form‑urlencoded` body + query merge.** The 2.6 servlet connector rebuilds the request entity
  from the container's merged `getParameterMap()` (query ∪ body, Servlet 6 §3.1). → code that reads params
  from *both* the body form and the query double‑counts them. → read the merged namespace once, or subtract
  the query (`bodyOnlyForm`). *(item 1‑19)*
- **A `x‑www‑form‑urlencoded` *raw* body is lost entirely.** If a client POSTs a non‑form payload (raw XML,
  JSON, …) but leaves the default `application/x-www-form-urlencoded` content type, the container drains the
  stream into `getParameterMap()` and Restlet rebuilds the entity from that map. A raw body has no
  `name=value` structure to recover, so by the time the resource runs the payload is **gone**:
  `getInputStream()` is at EOF, it is not a parameter, and `entity.getText()` returns only the query string
  (→ e.g. SAX "Content is not allowed in prolog"). Restlet 1.1 handed back the raw body; 2.6 does not. → **fix
  at the client: send the correct `Content-Type`** (`text/xml`, `application/json`) so Restlet keeps the raw
  entity. Applies to any JS/HTTP caller (YUI `asyncRequest`, `fetch`, XHR) posting a body to a `/REST`|`/data`
  resource that reads `entity.getText()`. *(item 1‑21, commit `4bb23dca7`)*
- **Response headers must be `Series<Header>`, not `Form`.** The attribute is `org.restlet.http.headers`; a
  `Form` there throws a CCE *after* the body commits (→ opaque 500), and STANDARD_HEADERS
  (`Cache-Control`, `Content-Disposition`) set on a raw Series are silently dropped. → use the typed API
  (`getCacheDirectives()`, typed `Disposition`). *(item 0a‑17)*
- **Router defaults changed** to `EQUALS` + `FIRST_MATCH`. → trailing‑path URIs (file‑by‑name, catalog
  subpaths) 404, and writes misroute to the first prefix route. → restore `MODE_STARTS_WITH` matching +
  `MODE_BEST_MATCH` routing in `createInboundRoot()`. *(item 0a‑16)*
- **Empty‑valued params** now surface as `""` where 1.1 gave `null`. → normalize empty → null for parity.
  *(`101f83f42`)* — **`ext.fileupload` dropped** after 2.5.2 → `commons-fileupload2` `jakarta-servlet6`
  (`JakartaServletFileUpload` parsing the `HttpServletRequest` directly). *(item 1‑11)*
- **WebDAV `Status` constants removed** (207/422/423/424/507). A `SecureResource`/`ServerResource` subclass
  that referenced e.g. `Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY` no longer compiles
  (`cannot find symbol … location: class Status`). → use core's `org.nrg.xnat.restlet.util.XnatWebDavStatus`
  constants (`XnatWebDavStatus.CLIENT_ERROR_UNPROCESSABLE_ENTITY`, a `new Status(422, …)` on the plugin's
  compile classpath via `org.nrg.xnat:web`) — matching how xnat-web's own `SecureResource` sets 422. The
  standard 4xx/5xx constants (`CLIENT_ERROR_GONE`, `SERVER_ERROR_INTERNAL`, …) are still on `Status`; only the
  WebDAV extensions moved. Verify replacements against the actual 2.6 `Status` bytecode before guessing a
  rename — several 1.1 constants were dropped, not renamed. *(item 1‑6; xnat_cr_plugin)*

### Turbine 7 / Velocity 2
- **`eventSubmit_doXxx` uses exact‑type dispatch.** Turbine 7 resolves the handler via
  `getClass().getMethod("doXxx", {PipelineData, Context})` — an **exact** match. → a handler left on the old
  `(RunData, Context)` signature isn't found; the event silently falls through to the empty base
  `doPerform` → **HTTP 200 but no‑op** (a form that appears to submit and does nothing). → migrate every
  `eventSubmit`‑dispatched `doXxx` to `(PipelineData, Context)`. *(item 1‑18)*
- **Velocity 2 `#if` empty‑check.** 2.x defaults `directive.if.empty_check=true`: empty string, empty
  collection, and numeric zero are **falsy**; 1.7 treated any non‑null, non‑false object as truthy. →
  `#if($emptyString)` / `#if($zero)` blocks silently stop rendering. → core sets
  `directive.if.empty_check=false`; a plugin's own templates inherit the engine setting, but be aware if you
  ship a separate Velocity config. *(item 0b‑23)*
- **Velocity 2 default encoding is UTF‑8** (1.7 was ISO‑8859‑1); `$velocityCount`/`$velocityHasNext` →
  `$foreach.count`/`$foreach.hasNext`; `$page.addAttribute` → `addBodyAttribute`; the resource‑loader SPI
  changed (`getResourceStream` → `getResourceReader`, `ExtProperties`).
- **Fulcrum not transitive** (see J1) — `ParameterParser`/`Recyclable` `NoClassDefFound` at compile.

### Hibernate 6
- **`hibernate-validator` groupId changed** (`org.hibernate` → `org.hibernate.validator`). The *old*
  coordinate resolves to an **empty relocation jar** at 8.x → Hibernate's AUTO bean‑validation is **silently
  disabled** (no error, validations just stop running). → use the new groupId. *(test‑suite fix)*
- **Legacy `Criteria` API removed** → port to JPA `CriteriaBuilder`. *(`fe5a52f85`)* — **`hibernate-core-jakarta`**
  is a *differently‑named* artifact from javax `hibernate-core`; `hibernate-ehcache`/`hibernate-jcache` still
  pull the javax one, so both can land on the classpath (the exclusion is the consuming module's job, not the
  BOM's).

### Build / dependency‑resolution gotchas
- **Lombok vs the JDK — and old jars vs the JDK.** Lombok 1.18.34 throws `NoSuchFieldException
  com.sun.tools.javac.code.TypeTag.UNKNOWN` on a too‑new JDK. *Separately*, JDK 22+ tightened zip64 CEN
  validation and **refuses to read old jars** on the compile classpath (e.g. `aspectjweaver-1.8.10`, which
  the BOM still pins) with `error reading …: Invalid CEN header (invalid zip64 extra data field size)` — and
  that then cascades into dozens of bogus `cannot find symbol` errors across the module. → pin a **JDK 21
  toolchain** (see J1); it fixes both. A plugin with no explicit toolchain silently runs on the Gradle
  launcher JDK. *(container-service)*
  **The same CEN failure also hits any Gradle *task action* that opens jars itself** — the monorepo's
  `verifyNoOrphanedJavaxEE` guard calls `new java.util.zip.ZipFile(jar)` over `runtimeClasspath` and dies with a bare
  `java.util.zip.ZipException: Invalid CEN header …` (no file named) on the same `aspectjweaver-1.8.10`. Here a JDK‑21
  **toolchain does not help**: task actions execute in the **daemon** JVM, which the toolchain does not govern (same
  distinction as the old‑wrapper case below). → set the *daemon* JDK — `JAVA_HOME=…/temurin-21…/Home ./gradlew …` or
  `org.gradle.java.home` in `gradle.properties`. Measured: JDK 21.0.11 reads that jar (987 entries), JDK 25.0.2
  throws. Such a task can also sit green for months and only fail when something unrelated changes the classpath and
  invalidates its up‑to‑date marker. *(item 1‑33)*
- **`mavenLocal()` shadows partial third‑party artifacts.** `ehcache:3.10.8` uses a **`-jakarta` classifier**
  jar; if `~/.m2` has the base metadata but not that classifier, an unscoped high‑priority `mavenLocal()` locks
  resolution to `~/.m2` and fails (`Could not find ehcache-3.10.8-jakarta.jar`). → scope `mavenLocal` to
  `org.nrg.*` (J1). *(item 662607097 / this session)*
- **springdoc floor.** springdoc **2.8.17** drags in `spring-boot-starter-logging` → `log4j-to-slf4j`, which
  collides with Turbine 7's `log4j-core` (Turbine casts to a log4j2 `LoggerContext` at init) → YAAFI boot
  fails. → hold springdoc at **2.8.6** on the Turbine 7 graph. *(`a3d4ad449`)*
- **glassfish JSTL 3 `c:import var=`** capture is defective — the wrapper's `flush()` spills the capture into
  the page and empties the var whenever the included servlet flushes. → replace `<c:import var=…>` with an
  XNAT‑owned import tag (or `<xnat:import>`). *(item 1‑15)*
- **springfox is dead on Spring 6** → springdoc‑openapi (non‑Boot WARs need to `@Import` the config classes
  manually). For a *plugin* you usually don't touch the swagger annotations at all: drop the
  `io.springfox:springfox-swagger2` / `springfox-swagger-ui` deps, add `io.swagger:swagger-annotations:1.5.20`
  so the legacy `@Api`/`@ApiOperation` still compile, and let core's springdoc generate the docs from the MVC
  mappings (it ignores the 1.x annotations). *(container-service)* **javamelody** needs its 2.x (jakarta)
  line — a javax listener fails Tomcat 10 boot.
- **java‑platform BOM constraints can't carry `exclude`** — it's a no‑op that only surfaces (as a hard error)
  when publishing Gradle module metadata (`generateMetadataFileForMavenPublication`). *(this session, `parent`)*
- **`mavenLocal` shadowing, the other lever.** Where scoping `mavenLocal` to `org.nrg.*` isn't convenient (an
  existing repos block), the targeted fix for the ehcache‑jakarta case is a per‑repo content filter:
  `mavenLocal { content { excludeGroup "org.ehcache" } }` — pushes just that group to Central/jfrog where the
  `-jakarta` classifier lives. *(container-service)*
- **Old Gradle wrapper can't run on the launcher JDK.** Plugins that haven't been touched recently often pin
  an old wrapper (e.g. **Gradle 8.14.3**) that the current launcher JDK (25) can't run — the *buildscript*
  compile dies with `BUG! … Unsupported class file major version 69` (69 = Java 25) before any of your code
  compiles. Note the JDK‑21 **toolchain** does NOT help here: it governs the plugin compile, not the Gradle
  daemon's own JVM. → **bump the wrapper** to match the rest of the migrated set
  (`gradle/wrapper/gradle-wrapper.properties`: `gradle-9.4.1-bin.zip`) so it runs on JDK 25 and also builds in
  the docker staging pipeline; or, as a one‑off, run with `JAVA_HOME` pointing at JDK 21. *(ldap-auth-plugin)*
- **Gradle 9 removed `AbstractCompile.destinationDir`.** After bumping an old wrapper to 9.x, a build script
  reading `compileJava.destinationDir` (common in the `idea {}` block) fails with
  `Could not get unknown property 'destinationDir' … JavaCompile`. → use the provider API:
  `compileJava.destinationDirectory.get().asFile`. *(ldap-auth-plugin)*
- **Merged satellite modules also bite the older base branches.** Bitbucket plugins based on a pre‑1.10
  `java-upgrade`/`master` line still declare `org.nrg.xnat:xnat-data-models` (and friends from §3); these must
  be dropped even when the rest of the build looks 1.10‑ready. *(ldap-auth-plugin)*
- **Gradle 9 forbids mutating a configuration after it's resolved.** The old spread/`all {}` exclude idiom —
  `configurations { all*.exclude group: … ; all { exclude group: … } }` — throws
  `Cannot mutate the dependencies of configuration ':…' after the configuration was resolved` under Gradle 9
  (some plugin, e.g. the data‑builder or maven‑settings, realizes a configuration during the configuration
  phase, and the `all { exclude }` then tries to add an exclude to it). → declare the config, do the
  `extendsFrom`, and move the excludes into a **lazy** block:
  `configurations.configureEach { exclude group: …; … }`. *(xnat-dicomweb-plugin)*
- **`cannot access Status` (or other core supertypes) with no obvious cause.** Referencing
  `org.nrg.action.ClientException`/`ActionException` — thrown all over `/data`‑style service code — forces the
  compiler to load their supertype chain, which exposes **`org.restlet.data.Status`** in its API. If the
  plugin doesn't otherwise touch Restlet, restlet isn't on the compile classpath and you get
  `error: cannot access Status` at the `new ClientException(...)` site (not at any import). → add
  `compileOnly "org.restlet:org.restlet"` even for a plugin with zero Restlet resources. The same
  "transitive supertype not on the classpath" shape also produces `cannot access ParameterParser` (needs
  `fulcrum-parser`/`fulcrum-pool`, see J1) — when an error names a type you never imported, it's this. *(xnat-dicomweb-plugin)*
- **Core API signatures change across the 1.9→1.11 jump.** A plugin coming off an old base can call a core
  method whose signature moved — e.g. `DirectArchiveSessionService.getOrCreate` lost its 2‑arg form in 1.11.0
  (only `(SessionData, AtomicBoolean, String)` remains) → `method … cannot be applied to given types`. Where
  the plugin already reflectively probes for one signature and hard‑calls another as a fallback, make the
  fallback reflective too so it compiles against 1.11.0 yet still runs on an older XNAT. *(xnat-dicomweb-plugin)*

### Runtime — compile‑clean is not boot‑clean
A jar that compiles and carries a valid `META-INF/xnat/*-plugin.properties` descriptor can still **fail XNAT
startup**: the plugin's Spring beans are created in the *host* application context, so they interact with core's
beans. Budget a **load‑into‑a‑real‑XNAT‑and‑iterate** pass after the compile is clean — the first failing bean
is rarely the last.
- **Unqualified injection whose type now has several candidates.** e.g. container‑service's
  `KubernetesClientFactoryImpl(ExecutorService …)` broke boot with `NoUniqueBeanDefinitionException: expected
  single matching bean but found 3` — `threadPoolExecutorFactoryBean` + `scheduledExecutorFactoryBean` (XNAT
  core) plus the plugin's own `containerServiceThreadPoolExecutorFactoryBean`. A bean the plugin author "knew"
  was unique in their old test app is ambiguous inside a full XNAT. → add `@Qualifier("…")` (or `@Primary`).
  This is **not** a jakarta symptom — it just only surfaces once the plugin actually loads. *(container-service)*
- **A bundled transitive that shadows a core library the host needs.** A fat‑jar plugin ships its own copy of
  everything on `implementAndInclude`, and those classes can *win* over the host's on the plugin classloader.
  container‑service bundled **commons‑io 2.15.1** (transitive via docker‑java); XNAT's Turbine stack needs
  **commons‑io 2.21.0** (`commons-fileupload2-core` calls a build API only present in the newer copy). The stale
  bundled copy shadowed it → `IllegalAccessError` in `DiskFileItemFactory` → `org.apache.fulcrum.upload`
  init fails → **Turbine `init() failed`** → every `/app` screen (Login.vm, all `.vm` templates) returns empty
  while REST (`/data`, `/xapi`) still answers `200`. The split REST‑works/screens‑dead symptom is the tell.
  → Keep the library on the **compile** classpath (the source imports it) but strip it from the **bundle** so the
  host's copy wins at runtime. Do the strip in the `fatJar` task's content filter
  (`exclude "org/apache/commons/io/**"`), **not** as a `configurations { implementAndInclude { exclude … } }`
  rule: a configuration‑level `exclude` propagates through `extendsFrom` to `compileClasspath` and breaks
  compilation. Verify with `unzip -l <fat.jar> | grep -c 'org/apache/commons/io/'` == 0. *(container-service)*
- **Diagnose from the container log, not `docker logs` alone**, and read the *innermost* `Caused by:` — Spring
  wraps the real error (here `NoUniqueBeanDefinitionException`) under a chain of `UnsatisfiedDependencyException`.
- **A broken plugin takes the whole ROOT webapp down** (`Context initialization failed` → `Context [] startup
  failed`). Pull the jar from `${xnat.home}/plugins` and restart to recover while you fix it.
- **An unresponsive external ActiveMQ broker hangs startup *silently and forever*.** The jakarta cutover moved
  JMS to `jakarta.jms` and bumped the client to **ActiveMQ 6.2.7** (`libs.versions.toml`), so any *external*
  broker must be **ActiveMQ Classic 6.x** (compose uses `apache/activemq-classic:6.1.4`). Point XNAT at a broker
  that isn't 6.x/healthy — e.g. a leftover pre‑migration **5.x** broker, or one that's wedged/flow‑controlled —
  and the first JMS send blocks indefinitely: `DefaultGroupsAndPermissionsCache.initialize()` sends
  `InitializeGroupRequest` *synchronously* on the init thread (`:333`), and `spring.activemq.broker-url`'s common
  `?wireFormat.maxInactivityDuration=0` disables the dead‑connection timeout, so the send never returns. Because
  `InitializeCachesTask` runs the caches sequentially on one thread, that freezes **all** initializing tasks
  behind it — notably `UpdateUserAuthTable`, which backfills the `xhbm_xdat_user_auth` localdb mappings for
  SQL‑seeded users (admin, guest). Net symptom: a fresh DB where the app boots and renders the login page, but
  **every login fails `BadCredentialsException`** and `SELECT count(*) FROM xhbm_xdat_user_auth` is **0** — the
  user exists in `xdat_user` but has no auth mapping. The tell is a thread dump of `taskScheduler-4` parked in
  `ActiveMQConnection.syncSendPacket` → `…cache.initialize(:333)`. → Use the documented default embedded broker
  `spring.activemq.broker-url=vm://localhost`, or ensure the external broker is a healthy 6.x (and drop
  `maxInactivityDuration=0` / prefer a `failover:` URL so a broker blip defers cleanly instead of hanging boot).
  Note the init tasks log to `configuration.log`/`tasks.log` (not `application.log`) and were `WARN` by default —
  raise `org.nrg.xnat.initialization`/`…initialization.tasks` to `INFO` to see the stall. *(bin‑tomcat10 fresh‑DB
  login failure; not a plugin bug — a deployment/broker‑version issue exposed by the client bump)*
- **A third‑party jar compiled against `javax` EE lands on the jakarta classpath → latent `NoClassDefFoundError`.**
  The source `javax→jakarta` sweep only fixes *your* code; a dependency jar that was itself built against
  `javax.persistence` / `javax.servlet` / `javax.xml.bind` / … stays javax, and on Tomcat 10 (only the `jakarta.*`
  APIs present) it throws `NoClassDefFoundError` **the instant one of its classes links** — invisible to
  compile‑clean *and* boot‑clean; it only fires when a specific code path loads that class. Real instance:
  `jackson-datatype-hibernate5` (javax) — `HibernateAnnotationIntrospector` refs `javax.persistence.Transient`;
  when Jackson serialized a Hibernate `@Entity` JSON column (container command‑save) it 500'd, so no container
  could launch and the entire DICOM‑modification suite failed. → Use the artifact's **`-jakarta`** variant (often a
  renamed package/class too: `Hibernate5Module` → `Hibernate5JakartaModule`). **Swapping your own
  registration is not sufficient:** any transitive library with its own `ObjectMapper` that calls
  `findAndRegisterModules()` (e.g. `com.vladmihalcea:hibernate-types-55` when binding a JSONB `@Entity` column)
  ServiceLoader‑discovers **every** `Module` jar on the classpath — so if the javax variant is still *present* it
  gets registered and 500s regardless. The javax variant must therefore be **globally excluded**
  (`configurations.all { exclude group: "com.fasterxml.jackson.datatype", module: "jackson-datatype-hibernate5" }`),
  not merely out‑referenced, and the `-jakarta` variant declared at **runtime** scope so those transitive
  auto‑discovery paths (not just your test/REST serialization) find it. To catch the whole class at build
  time, XNAT core adds `xnat-web:verifyNoOrphanedJavaxEE` (wired into `check`): it fails the build if any runtime
  jar references a jakarta‑migrated `javax.*` package that **nothing on the classpath provides** (reference‑vs‑
  provision, so JDK‑owned `javax.transaction.xa`/JCache/JSR‑305/JAXP are excluded; genuinely‑unreachable library
  features go on a documented allowlist). Plugins that bundle deps can copy the task into their build. *(status doc 1‑21)*

