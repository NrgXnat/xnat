# Contributing to XNAT

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Gradle | 9.x (use `./gradlew` wrapper) |
| PostgreSQL | 12+ |
| Git | 2.x |

## Development Environment Setup

### 1. Java 21

Install JDK 21 and ensure `java -version` reports 21 before running any build commands.

### 2. XNAT Home Directory

Create the XNAT home directory structure and configuration file:

```bash
mkdir -p ~/xnat/config ~/xnat/logs ~/xnat/archive ~/xnat/prearchive ~/xnat/cache ~/xnat/work
```

Create `~/xnat/config/xnat-conf.properties`:

```properties
datasource.driver=org.postgresql.Driver
datasource.url=jdbc:postgresql://localhost/<db-name>
datasource.username=<db-user>
datasource.password=<db-password>

hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.hbm2ddl.auto=update
hibernate.show_sql=false
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true
```

### 3. Tomcat Configuration

Add `-Dxnat.home=<path-to-xnat-home>` to your Tomcat JVM arguments, for example in `CATALINA_OPTS`:

```bash
export CATALINA_OPTS="-Xmx4g -Dxnat.home=$HOME/xnat"
```

### 4. Build Verification

```bash
./gradlew :xnat-web:war
```

A successful build produces `xnat-web/build/libs/xnat-web-<version>.war`.

## Build Architecture

The build uses a three-tier architecture: **Convention Plugins** → **Version Catalog** → **module `build.gradle`**.

### Convention Plugins (`buildSrc/`)

Two convention plugins are applied by all modules:

| Plugin | Applied by | What it adds |
|--------|-----------|-------------|
| `buildlogic.java-common-conventions` | every module | Java 21 toolchain, repository list (JFrog → Maven Central), JaCoCo, `maven-publish`, test config, JAR manifest |
| `buildlogic.java-library-conventions` | library modules | `java-common-conventions` + `java-library` (exports API to consumers) |

> **Repository order matters.** JFrog repositories are listed before `mavenCentral()` because
> some dependencies (e.g. `jai_imageio`) have the correct version only on JFrog. Reversing
> the order causes resolution of an incompatible version.

### Version Catalog (`gradle/libs.versions.toml`)

All external dependency versions are defined in one place. Modules reference libraries by
alias without specifying a version:

```toml
# gradle/libs.versions.toml
[versions]
spring-framework = "5.3.39"

[libraries]
spring-core = { group = "org.springframework", name = "spring-core", version.ref = "spring-framework" }
```

```groovy
// any module's build.gradle
dependencies {
    api libs.spring.core   // version resolved from the catalog
}
```

To upgrade a dependency, change the version in `libs.versions.toml` — no other files need editing.

### Parent BOM (`parent/`)

`parent` is a `java-platform` module that publishes the same version constraints as a BOM
artifact (`org.nrg:parent`). External XNAT plugins consume it via:

```groovy
dependencies {
    implementation platform("org.nrg:parent:${version}")
}
```

This is the Gradle equivalent of the former Maven `<dependencyManagement>` parent POM.

## Module Build Pattern

Every library module follows the same minimal pattern:

```groovy
plugins {
    id 'buildlogic.java-library-conventions'
}

dependencies {
    api project(':framework')       // internal module dependency
    api libs.spring.core            // external dependency via Version Catalog
    testImplementation libs.junit.junit
}
```

Modules that must publish under non-default Maven coordinates override `group` or the
archive base name in their `build.gradle`. For example, `xdat` publishes as `org.nrg.xdat:core`.

See [README — Key Files](./README.md#key-files) for the role of each root-level config file.

## Build Commands

```bash
# Full build (compile + test + JAR/WAR)
./gradlew build

# WAR only (fastest path to a deployable artifact)
./gradlew :xnat-web:war

# Skip tests
./gradlew build -x test

# Single module
./gradlew :<module>:build

# Clean
./gradlew clean

# Publish to local Maven (~/.m2)
./gradlew publishToMavenLocal

# Temporarily disable Configuration Cache (useful when debugging config issues)
./gradlew build --no-configuration-cache
```

## Viewing Dependencies

```bash
# Full dependency tree for a module
./gradlew :framework:dependencies

# Runtime classpath only
./gradlew :framework:dependencies --configuration runtimeClasspath

# Check why a specific dependency was pulled in
./gradlew :xnat-web:dependencyInsight --dependency spring-core
```

## Running Tests

```bash
# All tests
./gradlew test

# Single module tests
./gradlew :framework:test

# Specific test class
./gradlew :framework:test --tests "org.nrg.framework.SomeTest"

# Specific test method
./gradlew :framework:test --tests "org.nrg.framework.SomeTest.testMethod"
```

## Branch Strategy

| Branch | Purpose | Base |
|--------|---------|------|
| `main` | Latest release — do not commit directly | — |
| `releases/<version>` | Release snapshot — do not commit directly | — |
| `develop` | 1.10.x development | `main` (at release tip) |
| `develop-1.9.x` | 1.9.x maintenance | `releases/1.9.3.4` |
| `feature/<ticket>` | Feature work | `develop` or `develop-1.9.x` |
| `fix/<ticket>` | Bug fix | `develop` or `develop-1.9.x` |

Feature and fix branches are merged to `develop` (or `develop-1.9.x`) via pull request.
Direct commits to `main` and `releases/*` are not allowed.

## Submitting Changes

1. Branch from `develop` (or `develop-1.9.x` for 1.9.x fixes):

   ```bash
   git checkout develop
   git pull
   git checkout -b feature/XNAT-1234-short-description
   ```

2. Make changes, commit with a message referencing the ticket:

   ```bash
   git commit -m "XNAT-1234 Short description of the change"
   ```

3. Push and open a pull request targeting `develop`.

4. A bug fix applying to both `develop-1.9.x` and `develop` should be committed to
   `develop-1.9.x` first, then cherry-picked to `develop`:

   ```bash
   git checkout develop
   git cherry-pick <commit-sha>
   ```

## Code Standards

- **Java version**: Java 21 toolchain (all modules)
- **Formatting**: follow the existing style in each module — no auto-formatter enforced globally
- **Tests**: new behavior requires tests; bugfixes should include a regression test where practical
- **Comments and documentation**: write in English
- **Imports**: all imports at the top of the file, grouped (stdlib → third-party → project); no function-level imports
