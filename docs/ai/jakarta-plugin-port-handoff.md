# Handoff: porting an XNAT plugin to 1.11 with an AI coding agent

**For the human:** point your agent at this file *and* at `docs/plugin-migration-guide.md`, name the
plugin repository, and let it work. This file is the procedure and the discipline; the guide is the
reference. Neither replaces the other.

**For the agent:** read `docs/plugin-migration-guide.md` before touching anything — particularly Part 2
and the Behavior-Change Catalog. Most of what you are about to hit is already written down there. Then
follow the phases below in order.

This procedure came out of porting roughly twenty production plugins. The phase order and the discipline
rules are the parts that were learned expensively; the mechanics are the easy half.

---

## What "done" means

A port is done when **all** of these hold. Anything less, say so plainly rather than reporting success:

1. `./gradlew clean <pluginJarTask>` is green, tests included.
2. The plugin jar loads into a **running 1.11 instance** and the instance boots clean — see
   *compile-clean is not boot-clean* below. Compilation alone is **not** done.
3. Any bug you fixed has had its **class** swept, with the audit scope and result stated.
4. New behavior-change findings are added to the guide's catalog.
5. You have reported what you did *not* do, and why.

---

## Phase 0 — Orient before editing

```bash
git -C <plugin> branch --show-current && git -C <plugin> log --oneline -3
git -C <plugin> remote -v
```

Establish, and state in your first report:

- **Base branch.** Branch `feature/tomcat10` off the plugin's own `develop` (or `main` if it has no
  `develop`). Do not branch off an older migration branch.
- **Framework surface** — this predicts the whole job:

```bash
grep -rl 'import org.restlet'  <plugin>/src/main/java | wc -l    # Restlet resources
find <plugin>/src -path '*turbine/modules*' -name '*.java' | wc -l  # Turbine screens/actions
grep -rl 'RunData'             <plugin>/src/main/java | wc -l    # framework overrides
grep -rl '@XnatRestlet'        <plugin>/src/main/java | wc -l
grep -rlE 'javax\.(persistence|servlet|validation|mail|jms|xml\.bind)' <plugin>/src/main/java | wc -l
```

Zero across the framework rows means this is a build-file-only port (about an hour). Non-zero means
source changes too (about a day).

---

## Phase 1 — Build file (guide §J1)

Make the build changes, then run **only** the resolution check:

```bash
./gradlew dependencies --configuration compileClasspath 2>&1 | grep -E "null|FAILED|Could not find"
```

**Fix resolution errors before compiling anything.** An unresolvable dependency produces dozens of
misleading `cannot find symbol` errors that vanish once resolution is fixed; chasing those first wastes
a cycle and, worse, invites you to "fix" code that was never broken.

Watch for: coordinate renames (`turbine:turbine`, `velocity-engine-core`), the `-jakarta` Hibernate
artifacts, `mavenLocal` scoping, and the JDK 21 toolchain with `fork = true`.

---

## Phase 2 — Mechanical source sweep (guide §J2)

Migrate **by package, never by prefix.**

```bash
# correct — enumerate the packages that actually moved
find src -name '*.java' -exec sed -i '' -E \
  's/javax\.(persistence|servlet|validation|mail|jms|transaction)/jakarta.\1/g' {} +
```

**Never** run a blanket `javax` → `jakarta` replacement. These stay `javax` and rewriting them breaks
the build: JSR-305 (`javax.annotation.Nonnull`/`Nullable`), JCache (`javax.cache`), JAXP
(`javax.xml.parsers`, `javax.xml.XMLConstants`), `javax.sql`, `javax.naming`. Note that jsr250
`javax.annotation.PostConstruct` *does* move while jsr305 `javax.annotation.Nonnull` does not — same
package prefix, opposite answers.

Afterwards, confirm what remains is only what should:

```bash
grep -rhoE 'javax\.[a-zA-Z.]+' src/main/java | sort | uniq -c | sort -rn
```

---

## Phase 3 — Framework overrides

Convert Turbine screens/actions to `(PipelineData, Context)` with the `getRunData()` bridge. Your own
helper methods stay on `RunData`.

**This is the one change that fails silently**, so verify it explicitly rather than trusting the edit:

```bash
grep -rnE '(protected|public)[^(]*(doBuildTemplate|doPerform|isAuthorized)[^(]*\([^)]*RunData' src/main/java
```

Any hit that is a framework override is a dead method — it compiles, and Turbine never calls it.

---

## Phase 4 — The verification loop

Run these **in order**. Each finds a different class of problem, and later ones are meaningless until
earlier ones are clean:

```bash
./gradlew dependencies --configuration compileClasspath   # resolution
./gradlew compileJava                                     # main compile
./gradlew compileTestJava                                 # test compile
./gradlew test                                            # test runtime (compileOnly deps missing here)
./gradlew clean <pluginJarTask>                           # the artifact
```

Then **deploy to a real 1.11 instance and read the startup log.** This is not optional; see below.

---

## The discipline that actually mattered

These are the rules whose absence produced wrong work. They cost more than the mechanics did.

### Measure; don't infer

The single most common failure mode. Plausible reasoning that was never checked produced, in one
session: a bug class defined wrongly, a site count off by 5×, and a risk assessment that didn't apply.
Each was corrected only by going and measuring.

Before you record a count, a class, or a "this is safe" in a commit message or a doc: run the command
that proves it. If you cannot prove it, say it is unverified.

### A zero-result grep is a hypothesis, not a finding

Absence of matches usually means your pattern is wrong. Real examples from this codebase: a search that
missed every hit because the docs use a **non-ASCII hyphen** (U+2011) rather than `-`; a case-sensitive
search that "proved" a rule was missing when it was present with different capitalisation.

When a grep returns nothing and you expected something, **doubt the grep first**. Confirm with a
deliberately broader pattern before concluding.

### Name the bug class by mechanism — not by symptom, and not by the code you patched

The expensive version of this: an earlier fix swept "everything that calls the helper I just patched"
and defined the class by the symptom it had seen (a parameter counted twice). A second bug with the
*same root cause* but the opposite symptom, in code that bypassed that helper, was structurally
invisible to the sweep and survived for weeks.

Define the class as the **mechanism** — "the framework derives X from Y, and Y behaves differently
under condition Z" — then enumerate every site that mechanism can reach, including code that does not
use the shared helper, and including **plugins as well as core**.

### "Benign" requires evidence

A site you have found but not fixed is either proven harmless or it is an open bug. "Guarded", "looks
low-impact", "probably not exercised" are hypotheses. Verification is usually cheap: grep for the
producer, trace the render chain, or just call the endpoint. Do that instead of deferring.

### Compile-clean is not boot-clean

The most dangerous property of this migration: a plugin can build perfectly and still fail catastrophically
at load. A javax-compiled plugin implementing `WebApplicationInitializer` throws `AbstractMethodError` at
context startup, the whole webapp fails, and **every URL 404s with nothing naming your plugin**. Unqualified
bean injections that resolve in the plugin's own tests can go ambiguous in the host context.

Always finish by deploying to a real instance and reading the log. "It compiles" is a checkpoint, not a
result.

### Read the exact-version source before explaining framework behavior

When a dependency misbehaves, fetch its sources jar and read it. Do not theorize past one failed
hypothesis. Cite `File.java:line`. Recall-based explanations of framework behavior in this stack have a
poor track record — repeatedly wrong until the source was in hand.

### Diagnose to the right layer

Before deciding *why* something failed, establish *where*. A tool, transport or usage error — no
response, no exit status, a command that never ran — is not an application result. Two real examples: a
"test failure" that was the wrong Node version, and a "deploy failure" that was a stale filesystem
mount. If repeated fixes don't change the outcome, suspect the diagnosis.

### Don't change externally-observable error statuses

Returning a "more correct" status (404 → 400, 500 → 404) breaks a test suite calibrated against the
previous release. Report the improvement; don't bundle it into a migration.

---

## Bug classes worth sweeping proactively

Each has bitten more than one plugin:

| Class | Sweep for | Why it hides |
|---|---|---|
| Dead Turbine override | `doBuildTemplate(.*RunData`, `doPerform(.*RunData` | compiles; never dispatched |
| Blanket javax rewrite | `jakarta.annotation.Nonnull`, `jakarta.cache`, `jakarta.xml.parsers` | these should not exist — if present, a prefix replace went too far |
| Raw entity reads | `getEntity().getText()`, `isEntityAvailable()` | Restlet 2.6 derives form entities from the servlet parameter map, which containers populate for POST only |
| Masked constructor failure | `throw new Exception(` inside a resource constructor | the finder turns any non-`ResourceException` into a bare 404 |
| Springfox remnants | `import springfox` | dead on Spring 6; annotation-only usage may still compile |
| Velocity 2 truthiness | `#if($x)` on bare references | empty string / empty collection / zero became falsy |

---

## Agent-specific hazards

Things that went wrong for an agent specifically, not for a human:

- **Never run a prose-wide regex over a file containing fenced code blocks.** A whitespace-collapsing
  cleanup meant for prose destroyed indentation in every ` ``` ` block of a document — 574 changed lines
  instead of 47. Stash fenced blocks before any document-wide substitution, and diff against `HEAD`
  afterwards to confirm the change is the size you intended.
- **Verify an example by reading it before recommending it.** A file chosen as the "smallest, clearest
  example" of a pattern turned out to be a *commented-out* method. Line counts don't tell you whether
  code is live.
- **Re-read a file immediately before editing it** in a long session. Earlier reads go stale, and an edit
  whose anchor text has drifted fails or, worse, matches somewhere unintended.
- **Report failures the moment you see them**, not after diagnosing. If a destructive operation aborts
  half-way, say so immediately — the human may need to act before you finish investigating.

---

## Reporting back

Close with a report that a reviewer can check rather than has to trust:

- **What changed**, by category, with counts.
- **What was verified, and how** — the exact commands and their results. Distinguish "compiles",
  "tests pass" and "boots on a real instance"; they are different claims.
- **What you swept**, the class definition you used, and the result — including "swept N sites, all
  clean", which is as valuable as a fix.
- **What you did not do**, and why. Deferred items, unverified assumptions, anything you were unsure of.
- **New findings for the catalog** — if you hit a behavior change that isn't in
  `docs/plugin-migration-guide.md`, add it there as part of the work.
