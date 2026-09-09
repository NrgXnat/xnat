# Reviewer's guide — the Jakarta / Tomcat 10 cutover

<!-- ── Document set ─────────────────────────────────────────────────────────────────────────────────
     This file and the published reviewer's page carry the SAME TEXT. The rule is a superset:
     every block the page shows appears here VERBATIM, and this file may carry a few additional
     blocks the page omits — but that omit-list is CLOSED, and it is exactly:

         * Appendix A and its "Not rendered on the published page" note
         * the file-type breakdown ("Non-Java: 31 .vm …") and the by-module breakdown
         * the mechanical table's "Spot-check" column
         * the bucket table's "How to review" column (the page renders it as the budget bar)
         * the "Measured at feature/jakarta-cutover vs origin/develop" note (points at Appendix A)
         * the "Scale, for the record" line (the page carries it as the masthead + the -w table)
         * the H1 (the page splits it into an eyebrow + title)

     Anything else present here but not on the page is a BUG, not a licensed difference.

     THIS FILE IS THE SOURCE. Edit here, then regenerate the page from it. Do not hand-edit the
     page's prose — that is how §8's file counts drifted for a week while this file was correct.

     CHECK PARITY IN BOTH DIRECTIONS. A page-to-file check alone cannot see a block that exists
     only here, which is how "The diff also carries 19 files under docs/…" went missing from the
     page. Verify file-to-page too, and reconcile every hit against the closed list above.

     The page may differ in PRESENTATION only: section order is the same, but the numbering format
     differs (§04.1 there, §4.1 here), and the page adds visual devices this file cannot express
     (the 342|183|124 budget bar, the judgment items as cards, accent highlighting on the two
     PipelineData rows).
─────────────────────────────────────────────────────────────────────────────────────────────────── -->

**Read this before opening the diff.** The PR touches **~780 files** — but only about **124** need human
judgment. The rest is a scripted namespace migration whose correctness the build and test suites
establish better than reading ever will. This guide tells you which is which, and what the
non-mechanical changes are *for*.

> ## Review this diff with whitespace ignored
>
> **`git diff -w origin/develop...HEAD`** — on GitHub, append **`?w=1`** to the Files-changed URL.
>
> **Why:** the xnat-web project has **mixed line endings and no `.gitattributes`**. `origin/develop` carries **268**
> CRLF `.java` files; this branch still carries **188**. The 83 that changed did so *incidentally* — the
> migration's tooling rewrote every file it touched as LF. 
> The consequence for you: a raw diff reports **every line** of those 83 files as modified. The whitespace
> churn was left because we seem to be converging on LF-only as the standard.
>
> **How:** the `git diff` command is the same as the GitHub Files-changed page,

**Read the change through this guide rather than through the commit log** — here's why.
1. The diff has over 160 commits, many of which touch only `docs/`. However, searching the subject line
alone for `docs` will hide real build changes.
2. The commits constitute important debugging history.  This migration's regressions are almost
entirely **runtime-only**: the ones in the tracker were found by
Playwright and REST runs, not by the compiler. If something escapes review and surfaces after merge,
`git bisect` over these commits is what finds it, and a single 779-file commit would throw that away.
The document `docs/tomcat10-upgrade-status.md` also cites **21 commit SHAs** as evidence for individual 
findings; squashing orphans every one.  

---

## Where to spend your review budget

Of **649 changed `.java` files**:

| Bucket | Files | What it is | How to review |
|---|---:|---|---|
| **Import-only** | **342** | Nothing changed but `import` lines | Confirm the mapping, don't read bodies |
| **Trivial** | **183** | ≤ 8 real diff lines — usually one call or annotation | Skim, glance at the changed line |
| **Substantial** | **124** | Real logic, signatures or config | *This is the review.* |

*Measured with `-w`. On a raw diff these read 292 / 159 / 198 — 74 files look "substantial" purely
because of the CRLF→LF conversion.*

Non-Java: 31 `.vm` (Velocity), 29 `.jsp`, 13 `.gradle`, 9 `.tag`, 7 `.xml`, 3 `.properties`, 15 `.sh`,
15 `.md`.

**By module:** xnat-web 502 · xdat 160 · framework 25 · docs 19 · docker 17 · automation 16 ·
notify 10 · mail 9 · prefs 5 · config 4 · spawner 2 · gradle 2 · dicom-edit6 2 — plus single files in
`xnat-data-builder`, `parent`, `mizer`, `buildSrc` and at the repo root (`gradle.properties`,
`Dockerfile`, `docker-compose.yml`, `.gitignore`).

The diff carries **19 files under `docs/`**. These files can be consulted during review. **§8** says what 
each one is, which are worth opening during
review, and which are dated records you should *not* read as descriptions of the shipped state. **The 
single most important is `docs/tomcat10-upgrade-status.md`.** It is referred to in this document as 
"the tracker" and was 
updated by the AI to document problems encountered during the migration and the fixes applied. This is
a good place to look if you want to know why something is like it is. Refer to **§8** for suggested uses
of the tracker and other documentation. Some, or all, of these doc files 
may not ultimately be appropriate to remain in the repository, but they were an important part of the 
migration effort and all are retained for the history they provide.


The diff also contains commits to the docker subproject, Dockerfile and docker-compose.yml. These provide a very
convenient way for docker-compose.yml to provision a server and allied scripts to configure and 
populate it for testing – particularly because we do not have any CI builds of Tomcat10 stacks. 
These tools are also being used during plugin migration to quickly verify the success of 
the migration without having to deploy to an external server.
These changes are still in the repository, but they are not part of the migration per se. 
Review them with this in mind. Changes here need not block entire the migration.

---

## Suggested order

**Start here.**

1. **The judgment calls first** — 4.2 (security), 4.1 (Restlet shim), 4.3, 4.6. **If your time is short,
   these four are the review.**
2. **Build files** — `libs.versions.toml`, `parent/build.gradle`, `xnat-web/build.gradle`. Small,
   high-leverage, and where the allowlist lives.
3. **Templates** — 31 `.vm`, 29 `.jsp`, 9 `.tag`.
4. **The 124 substantial Java files**, largest first.
5. **Skim** the 342 import-only and 183 trivial files.

**Split by expertise:** security/auth → 4.2 and the SS6 config · REST/Restlet → 4.1, 4.3, 4.7 ·
build/release → 4.6 and §5 · UI/templates → 4.4 and the `.vm`/`.jsp`/`.tag` set.

Throughout: when something looks surprising, the tracker probably already explains it, and says what was
measured — **§8** is how to search it.

---

## 1. The stack

One atomic cutover. No intermediate commit compiles against a half-migrated stack, so this cannot be
split into independently mergeable PRs.

| | Before | After |
|---|---|---|
| Servlet | javax.servlet 3.1 | **jakarta.servlet-api 6.0.0** (Servlet 6.0 = Tomcat 10.1) |
| Container | Tomcat 9 | **Tomcat 10.1** (embed 10.1.57) |
| Java | 8/11 line | **21** |
| Spring / Security | 5.3 / 5.7 | **6.2.19 / 6.5.11** |
| Hibernate | 5.6 javax artifacts | **5.6.15 `-jakarta` transformed** — *not* a Hibernate 6 upgrade |
| Restlet | 1.1 (`com.noelios`) | **2.6.0** |
| Turbine / Velocity | 2.3.3 / 1.7 | **7.0 / 2.4.1** |
| JMS | ActiveMQ 5.x | **6.2.7** client — broker must be 6.x |

The Hibernate row matters: the artifact names differ (`hibernate-core-jakarta`), and
`hibernate-ehcache`/`hibernate-jcache` still pull the javax `hibernate-core`. Excluding it is the
consuming module's job, which `xnat-web/build.gradle` does.

---
## 2. The largest substantial files

Read these first — they are where the real work is. Sizes ignore whitespace (`+added / −deleted`). Note
what the list contains: **five of the twelve are new tests**, and three are new infrastructure.

| Δ | File | |
|---|---|---|
| **+386 / −52** | `xnat-web/…/xnat/restlet/resources/SecureResource.java` | Restlet 1.1 → 2.x shim for the **59** classes extending it — §4.1 |
| +253 / −0 | `xnat-web/src/test/…/xnat/customforms/daos/CustomVariableFormAppliesToRepositoryTest.java` | new test |
| +151 / −0 | `xnat-web/…/xnat/web/tags/ImportTag.java` | **new** — XNAT-owned `<xnat:import>`, replacing the defective glassfish JSTL 3 `c:import var=` capture |
| +142 / −0 | `xnat-web/src/test/…/xnat/turbine/TurbineBootTest.java` | new test — boots the Turbine service container |
| +137 / −0 | `xnat-web/src/test/…/dcm/xnat/daos/DicomMappingEntityDaoTest.java` | new test |
| +116 / −27 | `xnat-web/…/xnat/initialization/SecurityConfig.java` | **the SS6 config — §4.2** |
| +115 / −0 | `xnat-web/…/xapi/configuration/OpenApiConfig.java` | **new** — springdoc replaces dead springfox |
| +85 / −0 | `xnat-web/…/xnat/restlet/XnatServerResourceFinder.java` | **new** — instantiates resources via the legacy constructor |
| +83 / −19 | `xnat-web/…/xnat/restlet/resources/search/SearchResource.java` | |
| +61 / −0 | `xnat-web/src/test/…/xnat/restlet/resources/SecureResourceOkParityTest.java` | new test — pins the 200/204 parity behavior |
| +2 / −74 | `mail/…/mail/api/MailMessage.java` | mostly deletion |
| +2 / −60 | `xnat-web/…/xnat/customforms/daos/CustomVariableFormAppliesToRepository.java` | mostly deletion |

All paths elide `src/main/java/org/nrg` (or `src/test/java/org/nrg`) at the `…`.

---


## 3. Mechanical changes — spot-check, don't read

These account for the 525 skimmable files. Each is a rule applied uniformly; the risk is a *missed*
site — which the build or the javax guard catches — not a wrong one.

| Change | Added lines | Spot-check |
|---|---:|---|
| `org.restlet.*` imports (1.1 → 2.6) | 513 | Type moved packages; no behavior implied |
| `RunData` → `PipelineData` in framework overrides | 481 | Each gains `RunData data = pipelineData.getRunData();` — see below |
| `javax.persistence` → `jakarta.persistence` | 367 | JPA annotations only |
| `javax.servlet` → `jakarta.servlet` | 267 | |
| `javax.{mail,jms,validation,xml.bind,inject,el,annotation}` | 136 | |
| Restlet WebDAV status constants → `XnatWebDavStatus` | 102 | 2.6 removed 207/422/423/424/507; constants restored locally |
| JSTL sun URIs → `jakarta.tags.*` | 45 | In `.jsp` / `.tag` |

**`RunData` → `PipelineData` is the one mechanical change that can fail quietly.**

Turbine 7 dispatches to the two-arg `(PipelineData, Context)` method. Because `RunData extends
PipelineData`, a leftover `(RunData, Context)` override still *compiles* — it is simply a more-specific
overload the framework never calls, so the screen renders nothing and the action never runs, with no
error. Every converted method bridges with `RunData data = pipelineData.getRunData();`, and XNAT's own
`RunData` helper methods are deliberately left alone. **If you see a `(RunData, Context)` method that
looks like a framework override, flag it.**

### Representative files — verify each rule once, then trust it

Rather than skim 525 files, read these eight. Each changes one to three real lines and demonstrates
exactly one rule. If the rule is right here, it is right everywhere it was applied.

| Rule | File | Δ | What you'll see |
|---|---|---:|---|
| Restlet 1.1 → 2.6 package move | `restlet/representations/BeanRepresentation.java` | +1/−1 | `org.restlet.resource.OutputRepresentation` → `org.restlet.representation.…` |
| **`RunData` → `PipelineData`** (two-arg) | `xdat/…/turbine/modules/actions/ActivateAction.java` | +3/−1 | `doPerform(PipelineData, Context)` + the `getRunData()` bridge — note the helper calls below it *stay* on `RunData` |
| **`RunData` → `PipelineData`** (`isAuthorized`) | `xdat/…/turbine/modules/actions/AdminAction.java` | +3/−1 | The single-arg flavor; `super.isAuthorized(data)` still compiles because `RunData` *is-a* `PipelineData` |
| `javax.persistence` → `jakarta` | `automation/…/daos/PersistentEventDAO.java` | +1/−1 | One import line |
| `javax.servlet` → `jakarta` | `xdat/…/xapi/authorization/AbstractXapiAuthorization.java` | +1/−1 | One import line |
| Other EE (validation shown) | `automation/…/impl/DefaultScriptRunnerService.java` | +1/−1 | `javax.validation.constraints.NotNull` → `jakarta.…` |
| Restlet WebDAV status constants | `xnat-web/…/archive/GradualDicomImporter.java` | +2/−1 | `Status.SERVER_ERROR_INSUFFICIENT_STORAGE` → `XnatWebDavStatus.…` |
| JSTL → `jakarta.tags` | `webapp/page/admin/index.jsp` | +1/−1 | `uri="http://java.sun.com/jsp/jstl/core"` → `uri="jakarta.tags.core"` |

The two `PipelineData` rows are the ones worth actually reading — they are the pattern whose *absence*
is invisible.

**Deliberately still `javax`, and correct:** JSR-305 (`javax.annotation.Nonnull`), JDK-owned JAXP
(`javax.xml.parsers`, `javax.xml.XMLConstants`), `javax.sql`, `javax.naming`, JCache. These did not move
to Jakarta. Verified: every `javax.*` EE reference in the diff's added lines sits in `docs/` prose
describing the migration — **zero in source**.

---

## 4. Judgment calls — this is the review

Each item below is a decision, not a rename, and each has a rationale that is easy to mistake for a
mistake. If your time is short, the first four are the review.

### 4.1 · Restlet shim

**`SecureResource` → `ServerResource` — +386/−52, the largest single file**

Restlet 1.1's `Resource` and 2.x's `ServerResource` have different lifecycles. Rather than rewrite all 59
resource classes, the 1.1 contract is preserved on top of 2.x: `get/post/put(Variant)` bridge to the
legacy `represent`/`handleX` methods, and `XnatServerResourceFinder` instantiates through the legacy
`(Context, Request, Response)` constructor that 2.x's default `Finder` no longer supports. **Look for:**
whether the bridge preserves method-allowed semantics (405 vs 404) and content negotiation.

### 4.2 · Security — read this one

**Spring Security 6 deliberately keeps the *deprecated* `authorizeRequests`**

You will see `authorizeRequests` where SS6 wants `authorizeHttpRequests`. **This is intentional, and
reverting it breaks login.** XNAT's real authorization rules are installed at runtime by
`UpdateSecurityFilterHandlerMethod`, a `BeanPostProcessor` that swaps the `FilterSecurityInterceptor`'s
metadata source so site config — open URLs, admin URLs, require-login — stays live-updatable.
`authorizeHttpRequests` builds an `AuthorizationFilter` the post-processor never sees, producing a login
redirect loop. It was tried and reverted. Porting to `AuthorizationManager` is a Spring Security
**7** task.

### 4.3 · Framework seam

**The Restlet → Turbine `RunData` bridge**

Legacy `/app` screens served through Restlet need a Turbine `RunData`. `TurbineScreenRepresentation`
builds one via `RunDataService.getRunData("restlet", …)` using `ServletUtils` and
`Response.getCurrent()`. Read it for thread-safety and for assumptions about a current response being
bound.

### 4.4 · Templates

**Velocity 2: `directive.if.empty_check=false`**

Velocity 2 changed `#if($x)` semantics — empty string, empty collection and zero became *falsy*, where
1.7 treated any non-null object as truthy. There are ~2,000 bare-reference `#if($x)` sites across ~600
templates; a per-site audit was rejected as unreviewable. The 1.7 semantic is restored globally in
`TurbineResources.properties`, and a boot test pins it by evaluating a probe through the live engine.

### 4.5 · Logging

**log4j2 ↔ Logback coexistence**

Turbine 7 requires `log4j-core`; XNAT logs via Logback/SLF4J. Both are on the classpath. Turbine 7 logs
through `System.Logger` — no hard cast to a log4j2 `LoggerContext`, unlike 5.1 — so `log4j-core` is a
swappable backend. Watch for SLF4J multiple-binding warnings on local boot.

### 4.6 · Build gate

**The build-time javax-EE guard**

`verifyNoOrphanedJavaxEE` is wired into `check`. It fails the build when a runtime jar references a
`javax.*` EE package that **nothing on the classpath provides** — the exact trap that silently broke
container command-save. It is precise by construction: a javax reference that still resolves is not
failed. Unreachable library features are **allowlisted with a stated reason** — review those entries as
claims to check, not as noise.

### 4.7 · Turbine dispatch

**`eventSubmit_` handler signatures**

Turbine 7 resolves `eventSubmit_doXxx` handlers by exact `(PipelineData, Context)` signature. A handler
left on the old signature is silently never dispatched — the same failure class as §3.

---

## 5. Build & verify

```bash
./gradlew compileJava compileTestJava verifyNoOrphanedJavaxEE
```

**The Gradle *daemon* must be Java 21 — a toolchain is not enough.**

It is pinned by `gradle/gradle-daemon-jvm.properties`, so this should be automatic. Why it matters: task
actions that open jars run in the *daemon* JVM, and JDK 22+ zip64 CEN validation refuses to read
`aspectjweaver-1.8.10`, killing the guard with a `ZipException` that names no file. A JDK-21 *toolchain*
governs forked compile/test JVMs, not the daemon.

**Deployment requirements:** Tomcat 10.1, Java 21, and an ActiveMQ broker on the 6.x line — a 5.x broker
fails to boot the jakarta client *silently*. **Every plugin must also be a jakarta rebuild**: one
javax-compiled plugin implementing `WebApplicationInitializer` throws `AbstractMethodError` at context
start, and *every* URL then returns 404 with no database error to point at.

---

## 6. Intentional behavior changes

Flag these only if you disagree with the decision — they are not oversights.

- **Restlet 2.6 default success status.** A handler finishing with a bodyless entity returns **204**
  where 1.1 returned 200. Clients hard-checking `== 200` are affected.
- **Form-body parameters on `SecureResource` POST/PUT.** Restlet 2.6 derives the entity from the servlet
  parameter map. Four `getBodyVariable` sites now honor a form *body* parameter that `develop` silently
  dropped. Low risk, but a genuine semantic change.
- **`UserFavoriteResource`** rejects an apostrophe in a path parameter with **400** instead of a
  misleading 404.
- **`spring-security-oauth2` 2.5.2 retained but deprecated.** javax-era and unreachable from core, but
  exported `api` to plugins — removing it in 1.11 risks breaking an external OAuth/OpenID auth plugin.
  Scheduled for 1.12.

---

## 7. Deliberately not in this PR

| Item | Why deferred |
|---|---|
| Collapse `SecureResource`'s body/query double-read | The bug is fixed; the cleaner model is a deliberate refactor for the Restlet-cleanup era |
| `framework` off `hibernate-types-55` (javax) | Latent, not broken; used paths verified; needs a type migration, not a dependency yank |
| `spring-security-oauth2` removal | See §6 |
| Finder's `null → 404` fallback → 500 | Externally observable; belongs with test updates |
| Upstream Turbine log4j-decoupling PR | XNAT does not depend on it merging |

---

## 8. The documentation, and how to use it

The diff carries **19 files under `docs/`** — 18 new, plus `plugin-migration-guide.md` rewritten from
187 lines to 773. That weight is deliberate. The hard parts of this migration were **runtime** behaviors
no diff shows: a Turbine override that compiles and is never called, a form-encoded `PUT` whose body
disappears, an ActiveMQ send that parks forever and silently starves the boot task that creates login
credentials. None of it is recoverable from the code, so it was written down as it was found.

Don't review these as code. Do use the first group — it answers most of the questions the diff will
raise.

### 8.1 Worth opening during the review

| File | Lines | What it is | Reach for it when |
|---|---:|---|---|
| `tomcat10-upgrade-status.md` | 895 | the tracker — ~40 findings numbered `1-NN`, each stating mechanism, audit scope and audit result | A change looks wrong, or you want to know whether a bug's whole *class* was swept |
| `xnat-web-framework-architecture.md` | 95 | which of the four coexisting web frameworks serves which URL, and what is load-bearing vs vestigial; cites `file:line` | You need to know what `/app` vs `/data` vs `/xapi` vs `/xdat` actually runs through — §4.1 and §4.3 assume it |
| `data-rest-endpoint-reference.md` (+ `.json`) | 242 | static-analysis reconstruction of the `/data` + `/REST` surface; a best-effort *lower bound*, since that API is not self-describing | Judging the blast radius of a `SecureResource` change — §4.1, §6 |
| `turbine-service-container.md` | 74 | Turbine's 13 configured services and the 2.3.3 → 4.0 config-format rewrite | Reviewing `TurbineResources.properties` / `componentConfiguration.xml` / `roleConfiguration.xml` |
| `playwright-migration-test-status.md` | 210 | per-spec status of the 393-spec suite run against this branch | You want to know what runtime verification actually covered. The suite is the **develop-calibrated reference and was not modified** — fixes landed in this repo instead |
| `migration-test-coverage-analysis.md` | 97 | that suite measured against the changed surface: 181 route attachments, 91 resource classes, 619 call-sites | You want the coverage *gaps* rather than the passes |

### 8.2 For after the merge — not part of this review

| File | Lines | What it is |
|---|---:|---|
| `plugin-migration-guide.md` | 773 | **The reference** for plugin authors, and canonical for the Breaking-API matrix and the Behavior-Change Catalog |
| `ai/jakarta-plugin-port-handoff.md` | 280 | Procedure and discipline for porting a plugin with an AI agent — distilled from ~20 production ports |
| `tomcat11-upgrade-plan.md` | 162 | What comes next, and the one real risk its scout found |

### 8.3 Record — do not read as current state

`phase0-compile-migration-summary.md` (174) · `tomcat10-jakarta-upgrade-plan.md` (295) ·
`tomcat9-deploy-stack.md` (79) · `turbine5-config-draft/` · `tools/pipelinedata_codemod.py` — the codemod
that performed §3's `RunData` → `PipelineData` conversion.

These describe the **javax / Tomcat 9 Phase-0 stack**, or a planned approach, at a dated point on the way
here. They will disagree with what shipped, on purpose — flagging that as an inconsistency is a false
positive. (`email-verification-setup.md` is operational setup for the test fixture's SMTP path, not
migration content.)

### 8.4 Three ways to use the tracker

1. **"This looks wrong."** Search it for the symptom, the class name, or the status code before writing
   the comment. Most of the choices that look like mistakes are one of the numbered items, recorded with
   the measurement that produced them — §4.2's deprecated `authorizeRequests` and §6's 200 → 204 are both
   there. **If it isn't there, that's a real finding** — raise it loudly.
2. **"Was the rest of this class checked?"** This repo's `CLAUDE.md` requires every fix to name its bug
   class, sweep for it, and record the result — *"swept N sites, all benign"* included. So a missing audit
   line is itself reviewable. Items **1-24 → 1-35 → 1-36** are the worked example: a bug class defined by
   symptom, then measured, found wrong, and corrected on paper.
3. **"What will this cost plugin authors?"** The Behavior-Change Catalog in `plugin-migration-guide.md`.
   This is the most valuable thing you can correct: an error a plugin author will hit that is **not** in
   the catalog is a gap that costs someone a day.

### 8.5 Not defects

- **The three porting documents overlap on purpose.** A published porting page is orientation for a
  human; `plugin-migration-guide.md` is the dense reference and is **canonical for the Breaking-API
  matrix**; `ai/jakarta-plugin-port-handoff.md` is the agent procedure. Depth is meant to differ; facts
  are not. A **contradiction** between them is a bug worth reporting — duplication isn't.
- **The tracker is a log, not a document.** Reading it front to back is a poor use of your budget; the
  item numbers are its index. It is also cited **by commit SHA in 21 places**, which is why the history
  isn't squashed — and why the PR asks to be merged with a **merge commit rather than a squash-merge**,
  which would kill every one of those citations.

Two documentation decisions are deliberately left open rather than settled here: the `.gitattributes`
line-ending question (**1-37**, above), and whether a `1.11.0-SNAPSHOT` gets published so plugin authors
don't each have to build core first (**1-38**).

---

## Appendix A — how these numbers were produced

*Not rendered on the published page.*

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

Numbers measured against `origin/develop`. Per-item history — mechanism, audit scope, audit result — is
indexed `1-NN` in [`tomcat10-upgrade-status.md`](tomcat10-upgrade-status.md). Plugin authors porting their
own code should start from [`plugin-migration-guide.md`](plugin-migration-guide.md).
