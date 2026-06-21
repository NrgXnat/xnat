# XNAT

Single-repository source for XNAT, consolidating 25 previously separate repositories
under a unified Gradle 9 build. The full git history of `xnat-web` is preserved on `main`.

## Directory Structure

```
xnat/
├── settings.gradle           # Subproject registration (21 includes)
├── build.gradle              # Root build: sets group and version for all modules
├── gradle.properties         # Global build properties (version, parallel, cache, JVM args)
├── gradlew / gradlew.bat     # Gradle Wrapper — use these instead of a local gradle install
├── gradle/
│   ├── libs.versions.toml    # Version Catalog — single source of truth for all dependency versions
│   └── wrapper/              # Wrapper JAR and properties
├── buildSrc/                 # Convention plugins (shared build logic)
│   ├── build.gradle          # Declares groovy-gradle-plugin + maven-settings-plugin
│   ├── settings.gradle       # Wires Version Catalog into buildSrc
│   └── src/main/groovy/
│       ├── buildlogic.java-common-conventions.gradle   # Applied to every Java module
│       └── buildlogic.java-library-conventions.gradle  # Adds java-library on top of common
│
│   ── Modules (21 total, flat layout) ──
│
│   Layer 1 — Foundation (no internal dependencies)
├── parent/                   # java-platform BOM — publishes dependency constraints
├── test/                     # Shared test utilities and base classes
├── framework/                # Core NRG framework: DI, ORM, services, annotations
├── transaction/              # Transaction management
├── extattr/                  # Extended attributes
├── dicom-edit4/              # DICOM scripting DSL built on dcm4che 2.x (ANTLR 3)
├── dicom-edit6/              # DICOM scripting DSL v6 built on dcm4che 5.x (ANTLR 4)
│
│   Layer 2–3 — Services
├── prefs/                    # Preferences service (→ framework)
├── mail/                     # Mail service (→ framework)
├── mizer/                    # DICOM anonymizer (→ framework, transaction)
├── config/                   # Configuration service (→ prefs)
├── notify/                   # Notification service (→ mail)
├── automation/               # Automation and scripting engine (→ framework)
├── dicomtools/               # DICOM tools (→ config, framework, extattr, mizer)
├── dicom-image-utils/        # DICOM image processing utilities (→ mizer)
│
│   Layer 4–5 — Data & Domain
├── xdat/                     # XDAT data access layer, ORM, generated types (→ notify, config, …)
├── spawner/                  # UI element and form generation in Groovy DSL (→ xdat)
│
│   Layer 6–8 — Web Application
├── dicom-xnat-sop/           # DICOM SOP class handling for XNAT (→ xdat, dicom-edit6)
├── dicom-xnat-util/          # DICOM/XNAT bridge utilities (→ xdat, dicom-edit4, mizer)
├── xnat-web/                 # Main web application — produces xnat-web-<version>.war
│   ├── src/main/java/
│   │   ├── org/nrg/xnat/…                    # Original xnat-web source
│   │   ├── org/nrg/xdat/model/…              # From xnat-data-models (merged)
│   │   ├── org/nrg/xnat/helpers/prearchive/… # From session-builders (merged)
│   │   ├── org/nrg/ecat/…                    # From ecat4xnat (merged)
│   │   ├── org/nrg/dcm/xnat/…               # From dicom-xnat-mx (merged)
│   │   └── org/nrg/xnat/archive/…            # From prearc-importer (merged)
│   └── src/main/resources/schemas/           # XSD schemas from xnat-data-models (merged)
│
│   Layer 9 — Build Tooling
└── xnat-data-builder/        # Gradle plugin for external XNAT plugin development (published)

```


## Key Files

| File | Purpose |
|------|---------|
| `settings.gradle` | Lists all 21 subprojects; changing this file adds or removes a module from the build |
| `gradle.properties` | Sets `version`, enables parallel builds, build cache, daemon, and JVM heap (`-Xmx4g`) |
| `build.gradle` (root) | Minimal — sets `group = 'org.nrg'` and propagates `version` to all subprojects |
| `gradle/libs.versions.toml` | Version Catalog: ~155 version entries and 260+ library definitions extracted from `parent/pom.xml` |
| `buildSrc/src/main/groovy/buildlogic.java-common-conventions.gradle` | Shared config applied to every module: Java 21 toolchain, repository list, JaCoCo, publishing, test setup |
| `buildSrc/src/main/groovy/buildlogic.java-library-conventions.gradle` | Extends common conventions and adds the `java-library` plugin (for modules that export an API) |
| `parent/build.gradle` | Publishes the BOM (`org.nrg:parent`) so external XNAT plugins can consume unified dependency constraints |


## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21 | JDK 21 required |
| Gradle | 9.x | Provided by `./gradlew` — no separate install needed |
| PostgreSQL | 12+ | Required at runtime; not needed for compile/test |

## Quick Start

```bash
# Full build (all modules, includes tests)
./gradlew build

# Skip tests
./gradlew build -x test

# Build a single module
./gradlew :framework:build

# Build the deployable WAR
./gradlew :xnat-web:war
ls -lh xnat-web/build/libs/xnat-web-1.10.0.war
```

## Branches

| Branch | Purpose | Version |
|--------|---------|---------|
| `main` | Latest release (advances via fast-forward on each release) | 1.10.0 |
| `releases/1.9.3.4` | 1.9.3.4 release reference | 1.9.3.4 |
| `releases/1.9.3.5` | 1.9.3.5 release reference | 1.9.3.5 |
| `develop` | Active development | 1.10.1-SNAPSHOT |

Release tags `1.9.3.4` and `1.10.0` are reachable from `main`; tag `1.9.3.5` is reachable from `releases/1.9.3.5`.

## Documentation

- [Contributing](./CONTRIBUTING.md) — build architecture, development workflow, branch strategy
- [FAQ](./docs/faq.md) — dependency management, build troubleshooting, common tasks
- [Plugin Migration Guide](./docs/plugin-migration-guide.md) — how to adapt external XNAT plugins to build against this monorepo
