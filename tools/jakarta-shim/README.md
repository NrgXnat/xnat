# Jakarta shim JARs

Byte-code rewrites (`javax.*` EE namespaces → `jakarta.*`) of legacy libraries that XNAT depends on but that
have **no upstream Jakarta release**. Produced with the [Apache Tomcat jakartaee-migration
tool](https://github.com/apache/tomcat-jakartaee-migration) 1.0.8 (`-profile=EE`). These unblock running the
Restlet/Turbine/Velocity stack on Tomcat 10.1+ without rewriting XNAT's UI and legacy REST layers first.

| Artifact | Original | Notes |
|----------|----------|-------|
| `org.restlet-1.1.10-jakarta.jar` | `org.restlet:org.restlet:1.1.10` | core; only `javax.xml.*` (JDK) refs |
| `com.noelios.restlet-1.1.10-jakarta.jar` | `com.noelios.restlet:com.noelios.restlet:1.1.10` | HTTP engine |
| `com.noelios.restlet.ext.servlet-1.1.10-jakarta.jar` | `com.noelios.restlet:com.noelios.restlet.ext.servlet:1.1.10` | `ServerServlet` — the actual servlet bridge |
| `org.restlet.ext.fileupload-1.1.10-jakarta.jar` | `org.restlet:org.restlet.ext.fileupload:1.1.10` | commons-fileupload bridge |
| `turbine-2.3.3-jakarta.jar` | `turbine:turbine:2.3.3` | XNAT UI framework |
| `velocity-1.7-jakarta.jar` | `org.apache.velocity:velocity:1.7` | template engine |
| `velocity-tools-2.0-jakarta.jar` | `org.apache.velocity:velocity-tools:2.0` | view tools |

## Provenance / analysis

Full byte-code analysis, risk assessment and the original PoC are documented in the knowledge base:
*XNAT Restlet 响应格式与 Jakarta Shim 分析* (2026-05-27). Key facts:

- None of these JARs uses any API removed in Servlet 5/6 (verified per-method against the removal list).
- Class names are unchanged — `web.xml` servlet declarations and all `org.apache.turbine.*` /
  `org.restlet.*` imports in XNAT source keep working as-is.
- JVM linkage verified: 21/21 key classes load against `jakarta.servlet-api` 6.0, with e.g.
  `org.apache.turbine.Turbine`'s superclass resolving to `jakarta.servlet.http.HttpServlet`.
- `TurbineConfig` (standalone-mode helper) is missing 29 `ServletContext` methods added since Servlet 2.x —
  irrelevant to XNAT, which uses its own `XnatTurbineConfig` (`XnatWebAppInitializer`).

## Scripts

- `./rewrite.sh` — regenerates `libs/` from the original javax artifacts (Gradle cache / Maven Central) with
  the pinned migration-tool version. Only needed to bump the tool; `libs/` is committed.
- `./verify.sh` — four-gate verification: zero residual `javax/servlet` bytecode refs, `jakarta/servlet`
  refs present, zero reflection-by-name string constants, and a decisive JVM class-linkage test. Run after
  any regeneration; must print `ALL CHECKS PASSED`.

## How these get consumed

During the jakarta cutover (Phase 3 of the Tomcat 10 migration) the Restlet/Turbine/Velocity coordinates in
`gradle/libs.versions.toml` are switched to these files via a `flatDir` repository. Longer term they are to
be published to XNAT Artifactory as `org.nrg.jakarta:*:<version>-jakarta-1` with dependency-free POMs
(transitives are already managed explicitly by `xdat/build.gradle` and `xnat-web/build.gradle` excludes).

**Do not wire these into the build while the code base is still on `javax.*`** — the shimmed classes expose
`jakarta.servlet` types in their signatures and will not compile against javax-based callers.
