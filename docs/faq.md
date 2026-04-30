# FAQ

## Dependency Management

### How do I change a dependency version?

Edit the relevant entry in `gradle/libs.versions.toml` under `[versions]`:

```toml
# Example: upgrade Spring Framework from 5.3.39 to 5.3.40
spring-framework = "5.3.40"
```

Every library that references `version.ref = "spring-framework"` picks up the new version
automatically. No other files need changing.

### How do I add a new third-party dependency?

Three steps:

**Step 1** — Add a version entry in `gradle/libs.versions.toml` (skip if the library
shares an existing version):

```toml
[versions]
caffeine = "3.1.8"
```

**Step 2** — Add a library definition in `[libraries]`:

```toml
[libraries]
caffeine = { group = "com.github.ben-manes.caffeine", name = "caffeine", version.ref = "caffeine" }
```

**Step 3** — Add the library to `parent/build.gradle` constraints so external plugins
also receive the unified version:

```groovy
dependencies {
    constraints {
        api libs.caffeine
    }
}
```

After that, any module can reference it without specifying a version:

```groovy
dependencies {
    implementation libs.caffeine
}
```

### How do I remove a dependency that is no longer used?

1. Remove all references from each module's `build.gradle`.
2. Remove the entry from `parent/build.gradle` constraints.
3. Remove the library definition from `gradle/libs.versions.toml` `[libraries]`.
4. If no other library uses that version alias, remove it from `[versions]` as well.

Use `grep` to confirm nothing else references it:

```bash
grep "caffeine" gradle/libs.versions.toml parent/build.gradle */build.gradle
```

### How does the version data flow work?

```
gradle/libs.versions.toml      ← single source of truth (versions + coordinates)
        │
        ├──→ parent/build.gradle (constraints) ──→ publishes BOM (org.nrg:parent)
        │                                                ↓
        │                                   external plugins consume via platform()
        │
        └──→ each module's build.gradle ──→ references libs.<alias> (no inline version)
```

### How do I declare a dependency between two internal modules?

Use `project()` directly — do not go through the Version Catalog for internal modules:

```groovy
// Example: the framework module depends on the config module
dependencies {
    api project(':config')
}
```

### How do I add several libraries that share a version (e.g. Jackson)?

Define the version once in `[versions]`, then reference it from multiple `[libraries]` entries:

```toml
[versions]
jackson = "2.13.5"

[libraries]
jackson-core        = { group = "com.fasterxml.jackson.core", name = "jackson-core",        version.ref = "jackson" }
jackson-databind    = { group = "com.fasterxml.jackson.core", name = "jackson-databind",    version.ref = "jackson" }
jackson-annotations = { group = "com.fasterxml.jackson.core", name = "jackson-annotations", version.ref = "jackson" }
```

To upgrade all three, change only the `jackson` version entry.

### What are the naming rules for Version Catalog aliases?

- Use **kebab-case** (lowercase + hyphens)
- Hyphens act as group separators — Gradle generates type-safe accessors with `.` in place of `-`
- **Avoid standalone numeric segments** (e.g. `hibernate-jpa-2-1`) — Gradle cannot generate
  a valid accessor for a numeric property name; use a descriptive word instead (e.g. `hibernate-jpa-api`)

| TOML alias | Gradle accessor |
|------------|----------------|
| `spring-core` | `libs.spring.core` |
| `jackson-databind` | `libs.jackson.databind` |
| `commons-lang3` | `libs.commons.lang3` |
| `hibernate-jpa-api` | `libs.hibernate.jpa.api` |

---

## Build System

### What does the `parent` module do?

`parent` is a `java-platform` module (no source code). It publishes a BOM artifact
(`org.nrg:parent`) containing version constraints for all dependencies:

- **Internally**: other modules can import it via `platform(project(':parent'))` to opt in
  to unified version management
- **Externally**: XNAT plugins consume it via `platform("org.nrg:parent:<version>")`,
  equivalent to inheriting from the former Maven parent POM

### What is the relationship between Convention Plugins and the Version Catalog?

| Component | Responsibility |
|-----------|---------------|
| `libs.versions.toml` | Defines versions and library coordinates |
| `java-common-conventions` | Java 21 toolchain, repositories, publishing, JaCoCo, test setup |
| `java-library-conventions` | Extends common conventions; adds `java-library` plugin |
| `parent/build.gradle` | Repackages the Version Catalog as a publishable BOM |

`buildSrc` can access the Version Catalog because `buildSrc/settings.gradle` wires it in
via `dependencyResolutionManagement`.

### Why does the build take so long the first time?

Gradle downloads all dependency JARs on the first run. Subsequent builds use the local
cache. To also enable the build cache (which reuses compiled outputs):

```bash
# Already enabled by default in gradle.properties:
org.gradle.caching=true
```

### How do I debug a build failure?

```bash
# Detailed task output
./gradlew build --info

# Full debug log
./gradlew build --debug

# If the error mentions Configuration Cache, disable it temporarily
./gradlew build --no-configuration-cache
```

### How do I add a new module?

1. Create the module directory under the repo root.
2. Add `include 'module-name'` to `settings.gradle`.
3. Create `module-name/build.gradle` applying the appropriate convention plugin.

```groovy
// module-name/build.gradle
plugins {
    id 'buildlogic.java-library-conventions'
}

dependencies {
    api project(':framework')
    // add other dependencies
}
```

### How is the project version managed?

The `version` property in the root `gradle.properties` is the single source:

```properties
version=1.10.0
```

The root `build.gradle` propagates it to every subproject via `allprojects { version = ... }`.
To cut a new release, update this one line.

---

## Maven Coordinate Overrides

Some modules publish under Maven coordinates that differ from their directory name, to
maintain backward compatibility with existing external plugins:

| Module directory | Published as (`group:artifactId`) | What is overridden |
|-----------------|-----------------------------------|--------------------|
| `dicom-edit4` | `org.nrg.dicom:dicom-edit4` | group |
| `dicom-edit6` | `org.nrg.dicom:dicom-edit6` | group |
| `mizer` | `org.nrg.dicom:mizer` | group |
| `xdat` | `org.nrg.xdat:core` | group + artifactId |
| `spawner` | `org.nrg.xnat:spawner` | group |
| `extattr` | `org.nrg:ExtAttr` | artifactId |
| `dicom-image-utils` | `org.nrg:DicomImageUtils` | artifactId |
| `xnat-web` (JAR) | `org.nrg.xnat:web` | artifactId |
| `xnat-data-builder` | `org.nrg.xnat.build:xnat-data-builder` | group |
