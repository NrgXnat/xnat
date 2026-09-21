# Working agreements for this repo

## Read fully, cite precisely

Before fixing or explaining behavior in third-party or framework code, fetch and read the actual
source and documentation for the exact version in use (Maven Central sources jars, upstream
repos) — do not reason from recall past one failed hypothesis. In explanations, commit messages,
and tracker notes, cite specific evidence: `file:line` for repo code, class + line for upstream
sources, and the reference document for behavioral claims.

## Root-cause discipline

When a fix works but the mechanism isn't fully understood, keep digging before
committing: build a minimal repro, compare against a known-good baseline (the
pre-migration stack, vanilla Tomcat, upstream sources from Maven Central), and
identify the exact upstream line or behavior change responsible. A workaround
that masks an ununderstood mechanism leaves the real bug latent for unexercised
paths (plugins, error pages, rarely-used screens).

After ANY fix, run down the generalization before considering the task done:

1. **Name the bug class**, not just the instance ("SS6 by-name resolution +
   `-parameters` exposes same-named beans", not "the actionProviders test bean").
2. **Sweep the codebase for the class** — grep/script an audit for every other
   place the same mechanism could bite, including production code when the
   instance was found in tests (and vice versa).
3. **Fix at the right altitude** — prefer the shared/config-level fix over the
   call-site patch when the mechanism is general (e.g., a shared
   SecurityContextRepository bean rather than one filter's setter).
4. **Record the outcome** in the commit message and, for migration work, in
   `docs/tomcat10-upgrade-status.md`: the mechanism, the audit scope, and the
   audit result — "swept N sites, all benign" is as valuable as a fix.

If a fix must ship before the dig completes, say so explicitly and leave the
open question in the tracker rather than letting it close silently.
