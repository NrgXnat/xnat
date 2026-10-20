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
| `buildlogic.java-common-conventions` | every module | Java 21 toolchain, repository list (JFrog → Maven Central), JaCoCo, `maven-publish`, test config, JAR manifest with git build info (commit SHA, branch, dirty state) |
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

### REST integration tests

The Gradle tests above are unit tests. End-to-end coverage runs against a live XNAT server over
the REST API and lives in a separate repository,
[NrgXnat/xnat-rest-tests](https://github.com/NrgXnat/xnat-rest-tests) — a Maven suite covering
core XNAT and several common plugins:

```bash
git clone https://github.com/NrgXnat/xnat-rest-tests.git
cd xnat-rest-tests
mvn clean test -Dxnat.config=mycustom.properties -Dxnat.baseurl=<test-server-url>
```

Properties may be set on the command line or in a file under `src/test/resources/config`
(`local.properties` by default, overridden with `-Dxnat.config`). Command-line values take
precedence. See that repository's README for the full list.

If a change here is observable through the REST API, add or update coverage there in a companion
pull request and link the two.

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

2. Make changes and commit them with a message in the format described under
   [Commit Messages](#commit-messages):

   ```bash
   git commit -m "fix(import): keep a failed upload's partial session out of the archive"
   ```

3. Push and open a pull request targeting `develop`. Give the pull request a title in the same
   format as a commit subject.

4. A bug fix applying to both `develop-1.9.x` and `develop` should be committed to
   `develop-1.9.x` first, then cherry-picked to `develop`:

   ```bash
   git checkout develop
   git cherry-pick <commit-sha>
   ```

## Commit Messages

Commit messages follow [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/):

```text
<type>(<scope>)!: <summary>

<body>

<footers>
```

- **Type** (required): the kind of change.

  | Type | Use for |
  |------|---------|
  | `feat` | A new feature or capability |
  | `fix` | A bug fix |
  | `perf` | Making something faster or use less, without changing what it does |
  | `refactor` | Restructuring code without fixing a bug or adding a feature |
  | `test` | Adding or correcting tests only |
  | `docs` | Documentation only |
  | `build` | The Gradle build or dependencies |
  | `ci` | GitHub Actions workflows |
  | `chore` | Anything else that doesn't change shipped code, such as a release version bump |

- **Scope** (optional): the module or area the change is in, in lowercase, such as `import`,
  `archive`, `xft`, `xdat`, `dicom-edit6`, `mizer`, `ui`, `deps` or `release`. Leave it out when
  the change has no single home.
- **`!`** (optional): marks a change that breaks backward compatibility. It goes after the scope,
  right before the colon: `fix(xft)!:`, not `fix!(xft):`. Tools that read Conventional Commits
  can't parse the second form, so changelogs and other output built from the history miss the
  commit.
- **Summary** (required): one line in the imperative mood ("add", "keep", "stop"), starting
  lowercase, with no trailing period. Say what the change does, not which ticket it is for.
- **Body** (optional): what changed and why, wrapped at 72 columns. Name the Jira ticket here
  (`XNAT-1234`) rather than in the summary.
- **Footers** (optional): a breaking change needs a `BREAKING CHANGE:` footer as well as the `!`,
  saying what broke and what callers or sites have to change.

For example:

```text
feat: add NodeLockService for Postgres advisory-lock leader election
fix(dicom-edit6): keep retained private values intact in retainPrivateTags
perf(ingest): reduce file I/O during DICOM ingest and de-identification
build(deps): let plugins take dcm4che artifacts from XNAT
chore(release): bump develop to 1.10.3-SNAPSHOT after cutting 1.10.2-rc
```

and, for a breaking change:

```text
fix(xft)!: stop the JVM finalizer from running a save's change-info insert

DBItemCache named its change-info insert finalize(), so besides the
call from PoolDBUtils.sendBatch, the JVM ran it on its finalizer thread
for every collected cache. [...] The method is now addChangeInfo(), and
sendBatch calls it as before.

BREAKING CHANGE: DBItemCache.finalize() is now addChangeInfo(). Code
that called finalize() to add the change-info row has to call
addChangeInfo() instead.
```

Pull requests are squash-merged, so each one becomes a single commit on `develop`. GitHub takes
that commit's subject from the commit when the pull request has one commit, and from the pull
request title when it has more. That's why the title has to follow the same format. GitHub adds
the pull request number, as in `(#121)`, so don't add it yourself. Whoever merges can edit the
subject in the merge dialog, so check it there before confirming.

Older commits use an `XNAT-1234 Summary` form. Don't copy it in new commits.

## Code Standards

- **Java version**: Java 21 toolchain (all modules)
- **Formatting**: follow the existing style in each module — no auto-formatter enforced globally
- **Tests**: new behavior requires tests; bugfixes should include a regression test where practical
- **Comments and documentation**: write in English
- **Imports**: all imports at the top of the file, grouped (stdlib → third-party → project); no function-level imports
