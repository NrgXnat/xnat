# Reviewer's guide — the Jakarta / Tomcat 10 cutover

**Read this before opening the diff.** The PR touches 779 files, but only about **198** need human
judgment. The rest is a scripted namespace migration whose correctness is better established by the
build and the test suites than by reading it line by line. This guide tells you which is which, and
what the non-mechanical changes are *for*.

> Measured at `feature/jakarta-cutover` vs `origin/develop`. Re-run the commands in
> [Appendix A](#appendix-a--how-these-numbers-were-produced) if the branch has moved.

---

## 1. What this is

One atomic cutover from the javax / Tomcat 9 stack to Jakarta EE / Tomcat 10.1:

| | before | after |
|---|---|---|
| Servlet | javax.servlet 3.1 | **jakarta.servlet-api 6.0.0** (Servlet 6.0 = Tomcat 10.1) |
| Container | Tomcat 9 | **Tomcat 10.1** (`tomcat-embed` 10.1.57) |
| Java | 8/11 line | **21** |
| Spring / Security | 5.3 / 5.7 | **6.2.19 / 6.5.11** |
| Hibernate | 5.6 (javax artifacts) | **5.6.15.Final, `-jakarta` transformed artifacts** — `hibernate-core-jakarta`, `hibernate-envers-jakarta`. *Not* a Hibernate 6 upgrade |
| Restlet | 1.1 (`com.noelios`) | **2.6.0** (Jakarta edition) |
| Turbine / Velocity | 2.3.3 / 1.7 | **7.0 / 2.4.1** |
| JMS broker | ActiveMQ 5.x | **6.2.7** client; broker must be 6.x |

Note the Hibernate row: this cutover moves to the **jakarta-transformed 5.6 line**, not to Hibernate 6.
The artifact names differ (`hibernate-core-jakarta`), which matters because `hibernate-ehcache` /
`hibernate-jcache` still pull the javax `hibernate-core` — excluding it is the consuming module's job,
and `xnat-web/build.gradle` does exactly that.

It is atomic by design: no intermediate commit compiles against a half-migrated stack, so this cannot
be split into independently mergeable PRs.

**Scale:** 154 commits, 779 files, +22,894 / −13,461.
**52 of the 154 commits are `docs:`** (tracker updates). If your client lets you exclude `docs/`, the
code history is ~100 commits.

---

## 2. Where to spend your review budget

Of **649 changed Java files**:

| Bucket | Files | What it is | How to review |
|---|---:|---|---|
| **Import-only** | **292** | Nothing changed but `import` lines | **Skim.** Confirm the package mapping, don't read bodies |
| **Trivial** | **159** | ≤ 8 changed lines, typically one call or annotation | **Skim**, glance at the changed line |
| **Substantial** | **198** | Real logic, signatures, or config | **This is the review.** |

Non-Java: 31 `.vm` (Velocity), 29 `.jsp`, 13 `.gradle`, 9 `.tag`, 7 `.xml`, 3 `.properties`, 15 `.sh`,
13 `.md`.

**By module:** xnat-web 502 · xdat 160 · framework 25 · docs 17 · docker 17 · automation 16 ·
notify 10 · mail 9 · prefs 5 · config 4 · spawner 2 · gradle 2.

### The outlier

`xnat-web/.../restlet/resources/SecureResource.java` — **4,430 changed lines**, an order of magnitude
larger than anything else. It is the Restlet 1.1 → 2.x compatibility surface for the **59** classes that
extend it directly.
Budget separate time for it; see §4.1.

The next-largest substantial files, in descending size:

```
712  restlet/extensions/WorkflowsRestlet.java
710  restlet/services/prearchive/BatchPrearchiveActionsA.java
628  event/listeners/AutomationEventScriptHandler.java
616  restlet/services/mail/MailRestlet.java
589  restlet/services/FeatureDefinitionRestlet.java
526  restlet/resources/prearchive/PrearcSessionResourceCatalogFiles.java
522  utils/XnatHttpUtils.java
518  turbine/modules/screens/BulkDeleteActionScreen.java
486  restlet/resources/AutomationResource.java
446  turbine/modules/actions/SetArcSpecs.java
416  restlet/extensions/StudyRoutingRestlet.java
```

---

## 3. The mechanical changes — what they are, and what to spot-check

These account for the 292 + 159 skimmable files. Each is a rule applied uniformly; the risk is a
*missed* site (which the build or the javax guard catches), not a wrong one.

| Change | Added lines | Spot-check |
|---|---:|---|
| `org.restlet.*` imports (1.1 → 2.6) | 622 | Type moved packages; no behavior implied |
| `RunData` → `PipelineData` in framework overrides | 476 | Each gains `RunData data = pipelineData.getRunData();` — see below |
| `javax.persistence` → `jakarta.persistence` | 366 | JPA annotations only |
| `javax.servlet` → `jakarta.servlet` | 265 | |
| `javax.{mail,jms,validation,xml.bind,inject,el,annotation}` | 138 | |
| Restlet WebDAV status constants → `XnatWebDavStatus` | 101 | 2.6 removed 207/422/423/424/507; constants restored locally |
| JSTL sun URIs → `jakarta.tags.*` | 44 | In `.jsp` / `.tag` |

**`RunData` → `PipelineData` deserves one paragraph**, because it is the one mechanical change with a
silent failure mode. Turbine 7 dispatches to the two-arg `(PipelineData, Context)` method. Since
`RunData extends PipelineData`, a leftover `(RunData, Context)` override still *compiles* — it is just
a more-specific overload that the framework never calls, so the screen renders nothing and the action
never runs, with no error. Every converted method therefore bridges with
`RunData data = pipelineData.getRunData();` and XNAT's own `RunData` helper methods are deliberately
left alone. If you see a `(RunData, Context)` method that looks like a framework override, flag it.

**Deliberately still `javax`, and correct:** JSR-305 (`javax.annotation.Nonnull`), JDK-owned JAXP
(`javax.xml.parsers`, `javax.xml.XMLConstants`), `javax.sql`, `javax.naming`, JCache (`javax.cache`).
These did not move to Jakarta.

**Verified:** every `javax.*` EE reference in the diff's added lines is in `docs/` prose describing the
migration. **Zero in source.**

---

## 4. The non-mechanical changes — where judgment was applied

This is the actual review. Each item below is a decision, not a rename, and each has a rationale that
is easy to mistake for a mistake.

### 4.1 `SecureResource` → `ServerResource` shim *(the 4,430-line file)*

Restlet 1.1's `Resource` and 2.x's `ServerResource` have different lifecycles. Rather than rewrite all
59 resource classes, the 1.1 contract is preserved on top of 2.x: `get/post/put(Variant)` are bridged to
the legacy `represent`/`handleX` methods, and `XnatServerResourceFinder` instantiates resources through
the legacy `(Context, Request, Response)` constructor that 2.x's default `Finder` no longer supports.

**Look for:** whether the bridge preserves method-allowed semantics (405 vs 404) and content negotiation.
Two known behavior deltas are documented in §6.

### 4.2 Spring Security 6 keeps the **deprecated** `authorizeRequests`

You will see `authorizeRequests` where SS6 wants `authorizeHttpRequests`. **This is intentional, and
reverting it breaks login.** XNAT's real authorization rules are installed at runtime by
`UpdateSecurityFilterHandlerMethod`, a `BeanPostProcessor` that swaps the `FilterSecurityInterceptor`'s
metadata source so site config (open URLs, admin URLs, require-login) is live-updatable.
`authorizeHttpRequests` builds an `AuthorizationFilter` that the post-processor never sees, producing a
login redirect loop — this was tried and reverted. Porting to `AuthorizationManager` is a Spring
Security **7** task, tracked separately.

### 4.3 The Restlet `RunData` bridge

Legacy `/app` screens served through Restlet need a Turbine `RunData`. `TurbineScreenRepresentation`
builds one via `RunDataService.getRunData("restlet", …)` using `ServletUtils` and
`Response.getCurrent()`. This is the seam between two frameworks; read it for thread-safety and for
assumptions about a current response being bound.

### 4.4 Velocity 2: `directive.if.empty_check=false`

Velocity 2 changed `#if($x)` semantics — empty string, empty collection and zero are now *falsy*, where
1.7 treated any non-null object as truthy. There are ~2,000 bare-reference `#if($x)` sites across ~600
templates; a per-site audit was rejected as unreviewable. Instead the 1.7 semantic is restored globally
via `services.VelocityService.directive.if.empty_check=false` in `TurbineResources.properties`, and a
boot test pins it by evaluating a probe through the live engine.

### 4.5 log4j2 ↔ Logback

Turbine 7 requires `log4j-core`; XNAT logs via Logback/SLF4J. Both live on the classpath. Turbine 7 logs
through `System.Logger` (no hard cast to a log4j2 `LoggerContext`, unlike 5.1), so `log4j-core` is a
swappable backend. Watch for SLF4J multiple-binding warnings in your local boot.

### 4.6 The build-time javax-EE guard

`xnat-web/build.gradle` adds `verifyNoOrphanedJavaxEE`, wired into `check`. It fails the build if a
runtime jar references a `javax.*` EE package that **nothing on the classpath provides** — the exact trap
that silently broke container command-save. It is precise by construction: a javax reference that still
resolves is not failed. Genuinely-unreachable library features are **allowlisted with a stated reason**;
review the allowlist entries as claims to be checked, not as noise.

**This guard requires a JDK 21 *daemon*** — see §5.

### 4.7 Turbine 7 `eventSubmit_` dispatch

Turbine 7 resolves `eventSubmit_doXxx` handlers by exact `(PipelineData, Context)` signature. Action
methods were migrated accordingly; a handler left on the old signature is silently never dispatched
(same class of failure as §3).

---

## 5. Building and verifying locally

```bash
./gradlew compileJava compileTestJava verifyNoOrphanedJavaxEE
```

**The Gradle daemon must be Java 21.** It is pinned by `gradle/gradle-daemon-jvm.properties`
(`toolchainVersion=21`), so this should be automatic. Why it matters: task actions that open jars run in
the *daemon* JVM, and JDK 22+ zip64 CEN validation refuses to read `aspectjweaver-1.8.10`, killing the
guard with a `ZipException` that names no file. A JDK-21 *toolchain* does **not** cover this — a
toolchain governs forked compile/test JVMs, not the daemon.

Runtime requirements for a deployment: Tomcat **10.1**, Java **21**, and an ActiveMQ broker on the
**6.x** line — a 5.x broker fails to boot the jakarta client *silently*. **Every plugin must also be a
jakarta rebuild**: one javax-compiled plugin implementing `WebApplicationInitializer` throws
`AbstractMethodError` at context start and *every* URL returns 404 with no database error.

---

## 6. Known behavior changes (intentional)

Flag these only if you disagree with the decision — they are not oversights.

- **Restlet 2.6 default success status.** A handler finishing with a bodyless/empty entity returns
  **204** where 1.1 returned 200. Clients that hard-check `== 200` are affected.
- **`x-www-form-urlencoded` body parameters on `SecureResource` POST/PUT.** Restlet 2.6 derives the
  entity from the servlet parameter map. Four `getBodyVariable` sites now honor a form *body* parameter
  that `develop` silently dropped (Restlet 1.1 returned an already-consumed stream). Low risk — no
  exercised client posts them — but it is a genuine semantic change.
- **`UserFavoriteResource`** rejects an apostrophe in a path parameter with **400** instead of a
  misleading 404.
- **`spring-security-oauth2` 2.5.2 is retained but deprecated.** It is javax-era and unreachable from
  core, but exported `api` to plugins; removing it in 1.11 risks breaking an external OAuth/OpenID auth
  plugin. Scheduled for removal in 1.12.

---

## 7. Deliberately *not* in this PR

Don't flag these as gaps; each is tracked with a rationale:

| Item | Why deferred |
|---|---|
| Collapse `SecureResource`'s body/query double-read into one `loadParameters()` | The *bug* is fixed; the cleaner model is a deliberate refactor for the Restlet-cleanup era |
| `framework` off `hibernate-types-55` (javax) → jakarta JSON type | Latent, not broken; used paths verified; needs a type migration, not a dependency yank |
| `spring-security-oauth2` removal | §6 |
| `XnatServerResourceFinder`'s general `null → 404` fallback → 500 | Externally observable; belongs with test updates |
| Upstream Turbine log4j-decoupling PR | XNAT does not depend on it merging |

---

## 8. Suggested review order

1. **§4 items first**, in this order: 4.2 (security), 4.1 (Restlet shim), 4.3, 4.6. If time is short,
   these four are the review.
2. **Build files** — `gradle/libs.versions.toml`, `parent/build.gradle`, `xnat-web/build.gradle`
   (13 `.gradle` files). Small, high-leverage, and where the allowlist lives.
3. **Templates** — 31 `.vm`, 29 `.jsp`, 9 `.tag`. Mostly namespace changes; §4.4 is the judgment call.
4. **The 198 substantial Java files**, largest first (list in §2).
5. **Skim** the 292 import-only and 159 trivial files.

Suggested split by expertise: security/auth → 4.2 + the SS6 config; REST/Restlet → 4.1, 4.3, 4.7;
build/release → 4.6 + §5; UI/templates → 4.4 + the `.vm`/`.jsp`/`.tag` set.

---

## Appendix A — how these numbers were produced

```bash
BASE=origin/develop
git rev-list --count $BASE..HEAD
git diff --shortstat $BASE...HEAD
git diff --name-only $BASE...HEAD | sed -E 's/.*\.([A-Za-z0-9]+)$/\1/' | sort | uniq -c | sort -rn

# bucket every changed .java file: import-only vs trivial vs substantial
for f in $(git diff --name-only $BASE...HEAD -- '*.java'); do
  body=$(git diff -U0 $BASE...HEAD -- "$f" | grep -E '^[+-]' | grep -vE '^(\+\+\+|---)')
  nonimport=$(printf '%s\n' "$body" | grep -vE '^[+-]\s*import ' | grep -vE '^[+-]\s*$' | wc -l)
  total=$(printf '%s\n' "$body" | grep -c .)
  if   [ "$nonimport" -eq 0 ]; then echo "mechanical $f"
  elif [ "$total" -le 8 ];     then echo "trivial    $f"
  else                              echo "substantial $total $f"; fi
done
```

Full per-item history — mechanism, audit scope, audit result — is in
[`tomcat10-upgrade-status.md`](tomcat10-upgrade-status.md), indexed `1-NN`. Plugin authors porting their
own code should start from [`plugin-migration-guide.md`](plugin-migration-guide.md).
