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

## 2. Hardcode Lombok Version

The `io.franzbecker.gradle-lombok` plugin downloads Lombok independently and does not read the BOM.

```groovy
// Before — both values are null
lombok {
    version = dependencyManagement.importedProperties["lombok.version"] as String
    sha256 = dependencyManagement.importedProperties["lombok.checksum"] as String
}

// After
lombok {
    version = "1.18.34"
    sha256 = "1ea5ad6c6afcff902d75072b2aaa6695585aebee9f12127e15c0036ba95d2918"
}
```

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

