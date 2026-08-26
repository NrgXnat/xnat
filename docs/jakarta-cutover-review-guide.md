# Reviewer's guide — the Jakarta / Tomcat 10 cutover

**Read this before opening the diff.** The PR touches ~780 files, but only about **124** need human
judgment. The rest is a scripted namespace migration whose correctness is better established by the
build and the test suites than by reading it line by line. This guide tells you which is which, and
what the non-mechanical changes are *for*.

> ## Review this diff with whitespace ignored
>
> **`git diff -w origin/develop...HEAD`** — on GitHub, append **`?w=1`** to the Files-changed URL.
>
> **Why:** the repo has **mixed line endings and no `.gitattributes`**. `origin/develop` carries **268**
> CRLF `.java` files; this branch still carries **188**. The 83 that changed did so *incidentally* —
> the migration's tooling rewrote every file it touched as LF. Nobody decided to normalize anything.
>
> The consequence for you is that a raw diff reports **every line** of those 83 files as modified. That
> is not a rounding error, it is most of the diff:
>
> | | raw | `-w` |
> |---|---|---|
> | insertions | 23,160 | **12,112** |
> | deletions | 13,461 | **2,413** |
>
> Roughly 11,000 insertions and 11,000 deletions are line-ending churn. Every count in this guide is
> whitespace-insensitive; if your numbers are ~2× larger, you are reading the raw diff.
>
> **Please don't ask for a line-ending fix in this PR.** Normalizing the remaining 188 files would add a
> large whitespace-only change on top of an already-large review. Whether to adopt a `.gitattributes` is
> tracked separately as item 1-37.

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

**Scale:** 163 commits, ~780 files. Insertions/deletions: **+12,112 / −2,413** ignoring whitespace
(+23,160 / −13,461 raw — see the note at the top).
**61 of the 163 commits are `docs:`** (tracker updates). If your client lets you exclude `docs/`, the
code history is ~102 commits.

> **Why the history wasn't squashed** — a deliberate decision, not an oversight, so please don't open the
> review by asking for one.
>
> This migration's regressions are almost entirely **runtime-only**: the ones recorded in the tracker were
> found by Playwright and REST runs, not by the compiler. If something escapes review and surfaces after
> merge, `git bisect` over these commits is the tool that finds it, and a single 779-file commit would
> throw that away. The tracker also cites **22 commit SHAs** as evidence for individual findings, and
> squashing would orphan every one of them.
>
> Collapsing just the `docs:` commits was considered and rejected too. They aren't one kind of thing — some
> narrate an adjacent code change, but others are verification milestones spanning many commits, or
> standalone reference documents. Only about ten have anywhere to go, **32 of them touch the same tracker
> file**, and at least one commit titled `docs:` also carries real build changes. The tidying isn't worth
> the reordering.
>
> **Read the change through this guide rather than through the log.** That is what it is for. Full
> reasoning is in `docs/tomcat10-upgrade-status.md`, item **1-39**.

---

## 2. Where to spend your review budget

Of **649 changed Java files**:

| Bucket | Files | What it is | How to review |
|---|---:|---|---|
| **Import-only** | **342** | Nothing changed but `import` lines | **Skim.** Confirm the package mapping, don't read bodies |
| **Trivial** | **183** | ≤ 8 real diff lines, typically one call or annotation | **Skim**, glance at the changed line |
| **Substantial** | **124** | Real logic, signatures, or config | **This is the review.** |

*(Measured with `-w`. On a raw diff these read 292 / 159 / 198 — 74 files look "substantial" purely
because of the CRLF→LF conversion.)*

Non-Java: 31 `.vm` (Velocity), 29 `.jsp`, 13 `.gradle`, 9 `.tag`, 7 `.xml`, 3 `.properties`, 15 `.sh`,
13 `.md`.

**By module:** xnat-web 502 · xdat 160 · framework 25 · docs 17 · docker 17 · automation 16 ·
notify 10 · mail 9 · prefs 5 · config 4 · spawner 2 · gradle 2.

### The largest substantial files

Read these first — they are where the real work is. Sizes are `-w` (`+added / −deleted`):

| Δ | File | |
|---|---|---|
| **+386 / −52** | `xnat-web/…/restlet/resources/SecureResource.java` | The Restlet 1.1 → 2.x shim for the **59** classes extending it — see §4.1 |
| +253 / −0 | `xnat-web/src/test/…/CustomVariableFormAppliesToRepositoryTest.java` | new test |
| +151 / −0 | `xnat-web/…/web/tags/ImportTag.java` | **new** — XNAT-owned `<xnat:import>`, replacing the defective glassfish JSTL 3 `c:import var=` capture |
| +142 / −0 | `xnat-web/src/test/…/turbine/TurbineBootTest.java` | new test — boots the Turbine service container |
| +137 / −0 | `xnat-web/src/test/…/dcm/xnat/daos/DicomMappingEntityDaoTest.java` | new test |
| +116 / −27 | `xnat-web/…/initialization/SecurityConfig.java` | **the SS6 config — see §4.2** |
| +115 / −0 | `xdat/…/xapi/configuration/OpenApiConfig.java` | **new** — springdoc replaces dead springfox |
| +85 / −0 | `xnat-web/…/restlet/XnatServerResourceFinder.java` | **new** — instantiates resources via the legacy constructor |
| +83 / −19 | `xnat-web/…/restlet/resources/search/SearchResource.java` | |
| +61 / −0 | `xnat-web/src/test/…/SecureResourceOkParityTest.java` | new test — pins the 200/204 parity behavior |
| +2 / −74 | `mail/…/api/MailMessage.java` | mostly deletion |
| +2 / −60 | `xnat-web/…/customforms/daos/CustomVariableFormAppliesToRepository.java` | mostly deletion |

Note what this list contains: **five of the twelve are new tests**, and three are new infrastructure.
`SecureResource` is the largest single file but it is *not* an order of magnitude beyond the rest — a raw
diff makes it look that way because of the line-ending conversion.

---

## 3. The mechanical changes — what they are, and what to spot-check

These account for the 342 + 183 skimmable files. Each is a rule applied uniformly; the risk is a
*missed* site (which the build or the javax guard catches), not a wrong one.

| Change | Added lines | Spot-check |
|---|---:|---|
| `org.restlet.*` imports (1.1 → 2.6) | 513 | Type moved packages; no behavior implied |
| `RunData` → `PipelineData` in framework overrides | 481 | Each gains `RunData data = pipelineData.getRunData();` — see below |
| `javax.persistence` → `jakarta.persistence` | 367 | JPA annotations only |
| `javax.servlet` → `jakarta.servlet` | 267 | |
| `javax.{mail,jms,validation,xml.bind,inject,el,annotation}` | 136 | |
| Restlet WebDAV status constants → `XnatWebDavStatus` | 102 | 2.6 removed 207/422/423/424/507; constants restored locally |
| JSTL sun URIs → `jakarta.tags.*` | 45 | In `.jsp` / `.tag` |

**`RunData` → `PipelineData` deserves one paragraph**, because it is the one mechanical change with a
silent failure mode. Turbine 7 dispatches to the two-arg `(PipelineData, Context)` method. Since
`RunData extends PipelineData`, a leftover `(RunData, Context)` override still *compiles* — it is just
a more-specific overload that the framework never calls, so the screen renders nothing and the action
never runs, with no error. Every converted method therefore bridges with
`RunData data = pipelineData.getRunData();` and XNAT's own `RunData` helper methods are deliberately
left alone. If you see a `(RunData, Context)` method that looks like a framework override, flag it.

### Representative files — verify the rule once, then trust it

Rather than skim 525 files, read these eight. Each changes one to three real lines and demonstrates exactly one rule. If the rule is right here, it is right everywhere it was applied.

| Rule | File | Δ | What you'll see |
|---|---|---:|---|
| Restlet 1.1 → 2.6 package move | `xnat-web/…/restlet/representations/BeanRepresentation.java` | +1/−1 | `org.restlet.resource.OutputRepresentation` → `org.restlet.representation.OutputRepresentation` |
| **`RunData` → `PipelineData`** (two-arg) | `xdat/…/turbine/modules/actions/ActivateAction.java` | +3/−1 | `doPerform(PipelineData, Context)` + the `getRunData()` bridge — note the helper calls below it *stay* on `RunData` |
| **`RunData` → `PipelineData`** (`isAuthorized`) | `xdat/…/turbine/modules/actions/AdminAction.java` | +3/−1 | The single-arg flavor; `super.isAuthorized(data)` still compiles because `RunData` *is-a* `PipelineData` |
| `javax.persistence` → `jakarta` | `automation/…/daos/PersistentEventDAO.java` | +1/−1 | One import line |
| `javax.servlet` → `jakarta` | `xdat/…/xapi/authorization/AbstractXapiAuthorization.java` | +1/−1 | One import line |
| Other EE (validation shown) | `automation/…/services/impl/DefaultScriptRunnerService.java` | +1/−1 | `javax.validation.constraints.NotNull` → `jakarta.…` |
| Restlet WebDAV status constants | `xnat-web/…/archive/GradualDicomImporter.java` | +2/−1 | `Status.SERVER_ERROR_INSUFFICIENT_STORAGE` → `XnatWebDavStatus.…` (2.6 dropped 207/422/423/424/507) |
| JSTL → `jakarta.tags` | `xnat-web/…/webapp/page/admin/index.jsp` | +1/−1 | `uri="http://java.sun.com/jsp/jstl/core"` → `uri="jakarta.tags.core"` |

The two `PipelineData` entries are the ones worth actually reading — they are the pattern whose *absence*
is invisible.

**Deliberately still `javax`, and correct:** JSR-305 (`javax.annotation.Nonnull`), JDK-owned JAXP
(`javax.xml.parsers`, `javax.xml.XMLConstants`), `javax.sql`, `javax.naming`, JCache (`javax.cache`).
These did not move to Jakarta.

**Verified:** every `javax.*` EE reference in the diff's added lines is in `docs/` prose describing the
migration. **Zero in source.**

---

## 4. The non-mechanical changes — where judgment was applied

This is the actual review. Each item below is a decision, not a rename, and each has a rationale that
is easy to mistake for a mistake.

### 4.1 `SecureResource` → `ServerResource` shim *(+386 / −52, the largest single file)*

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
4. **The 124 substantial Java files**, largest first (list in §2).
5. **Skim** the 342 import-only and 183 trivial files.

Suggested split by expertise: security/auth → 4.2 + the SS6 config; REST/Restlet → 4.1, 4.3, 4.7;
build/release → 4.6 + §5; UI/templates → 4.4 + the `.vm`/`.jsp`/`.tag` set.

---

## Appendix A — how these numbers were produced

```bash
BASE=origin/develop
git rev-list --count $BASE..HEAD
git diff -w --shortstat $BASE...HEAD   # -w matters: see the note at the top
git diff --name-only $BASE...HEAD | sed -E 's/.*\.([A-Za-z0-9]+)$/\1/' | sort | uniq -c | sort -rn

# bucket every changed .java file: import-only vs trivial vs substantial
for f in $(git diff --name-only $BASE...HEAD -- '*.java'); do
  body=$(git diff -w -U0 $BASE...HEAD -- "$f" | grep -E '^[+-]' | grep -vE '^(\+\+\+|---)')
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
