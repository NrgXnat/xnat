# XNAT DICOM ingest performance suite

A reusable A/B harness for measuring DICOM **ingest** cost across every ingest route, with and
without anonymization. It records **wall-clock** and **NFS write volume** per ingest phase, so two
server builds (e.g. `develop` vs a candidate WAR) can be compared on the same stack.

Built to validate the `perf/ingest-io` change (single-pass receive + rename-in-place file
anonymization, building on XNAT-8737 / `b61c0620`), where the payoff is on NFS-backed storage: fewer
writes per object become real wall-clock as per-write latency rises.

## Why not the Java perf suite / jmeter

`NrgXnat/xnat-rest-tests`'s `TestPerformanceDicom` is the canonical regression harness but it
measures **wall-clock only**, does a destructive `hardResetXnat()` over SSH between methods (doesn't
fit a k8s pod), and models regression-vs-stored-baseline rather than a two-WAR A/B. jmeter can't
speak DIMSE (C-STORE) or model the prearchive/build/commit lifecycle. This suite is small, runs
**in-cluster** for clean timing, captures the NFS-write signal, and produces a direct A/B report. It
reuses the same route mechanics the Java suite exercises (verified against the core source).

## How it measures

Every timed operation runs **inside the XNAT pod** (or a sender pod), so the `kubectl` API tunnel is
never in the measured path. The orchestrator only issues control calls and parses JSON from in-pod
`curl`.

- **Wall-clock**: in-pod `curl`'s own `time_total` for blocking calls; measured server-side poll
  duration (±1 s) for async routes (cache/inbox/direct trigger). The RECEIVING→READY *idle timeout*
  (`sessionXmlRebuilderInterval`, default 5 min) is **never** timed — every route drives
  `action=build`/`action=commit`/the direct-archive trigger explicitly.
- **NFS write**: FSx `serverwrite` bytes from `/proc/self/mountstats` (`bytes:` field 6) on the
  archive mount. Archive/prearchive/cache share one FSx export and report identical aggregate stats,
  so one number covers the pod's writes to NFS. Also `/proc/1/io` `wchar` (all `write()` bytes).

## Routes and anon modes

| Route | Mechanics |
|---|---|
| `zip` | DICOM-zip inbody `POST /data/services/import` → prearchive; then `action=build` + `/data/services/archive` |
| `cstore` | sender pod → SCP receiver `XNAT` on 8104; then build + archive |
| `cache` | Compressed Uploader flow: `PUT /data/user/cache/...` then `POST import src=…&action=commit&auto-archive` (async) |
| `direct` | zip import with `Direct-Archive=true`; then `POST /xapi/direct-archive/{p}/{tag}/{name}` to force the archive |
| `inbox` | files staged under `inboxPath`; `POST import-handler=inbox` (async JMS); then build + archive |

Anon is one axis with two dimensions — **where** the script runs × **what** it does:

| mode | site script | project script | exercises |
|---|---|---|---|
| `none` | off | off | control (no anonymization) |
| `site-header` | header edits | — | **receive** anon (single-pass receive, Tier 2) |
| `site-pixel` | header + rectangle redaction | — | receive anon + streaming pixel edit |
| `project-header` | off | header edits | **prearchive→archive** anon (rename-in-place, Tier 1) |
| `project-pixel` | off | header + redaction | archive anon + streaming pixel edit |

Site scripts run at receive; project scripts run at the merge/direct-archive (`ProjectAnonymizer`).
Pixel modes use uncompressed workloads (redaction needs decodable pixels).

## Workloads (`corpus.py`, synthetic — no external data needed)

| name | shape | default size |
|---|---|---|
| `small` | many small single-frame objects | 200 × ~256 KB (~53 MB) |
| `multiframe` | few large multiframe objects | 4 × ~52 MB (~210 MB) |
| `mixed` | LE + Explicit BE + Deflated spread | ~42 objects (~70 MB) |
| `huge` | **one uncompressed multiframe object > 2 GiB** — the XNAT-8737 boundary | 1 × ~2.05 GiB |

`huge` is heavy (2 GB copied into the pod, then imported/archived over NFS) — run it **selectively**
(`--workloads huge --reps 1`), not in a full matrix. Pass `--corpus DIR` to run any workload against
real DICOM instead (re-tagged to one project with fresh UIDs).

## Usage

```bash
cd perf/ingest
# one-time: create the PERF project + receiver, stage anon scripts into the pod
uv run python ingest_perf.py setup --k8s adapt-dev-amd64-v1:xnat-perf:xnat-0 --user admin --pass admin

# baseline build (develop), then Kate swaps the candidate WAR and re-runs with --label candidate
uv run python ingest_perf.py run --k8s adapt-dev-amd64-v1:xnat-perf:xnat-0 --user admin --pass admin \
    --label baseline --workloads small,multiframe --routes zip,cstore,cache,direct,inbox \
    --anon none,site-header,site-pixel,project-header,project-pixel --reps 3 --out results/

# the >2 GB case (run on its own; slow)
uv run python ingest_perf.py run --k8s ... --label baseline-huge \
    --workloads huge --routes zip,cstore --anon none,site-pixel,project-pixel --reps 1 --out results/

# compare two runs
uv run python ingest_perf.py report --compare results/baseline.json results/candidate.json -o report.md

# clean up (delete sender pod, restore anon; --delete-project also removes PERF)
uv run python ingest_perf.py teardown --k8s ... [--delete-project]
```

Results are one JSON per `--label` (self-describing header: build sha, storage layout, corpus
manifest) plus the A/B `report.md`.

## Constraints

- **Synthetic / dev targets only** (Scout PHI rules). Never point this at a PHI-bearing instance.
- The suite drives an existing server; it never deploys WARs — swap builds out of band between runs.
- Requires `kubectl` access to the pod and (for `cstore`) permission to run a short-lived sender pod.
- The `cstore` and `huge` cells install pydicom/pynetdicom into the sender pod **offline**, from a
  local wheel directory: `--wheels DIR` (default `~/QA/ingest-io/wheels`). Populate it once with
  `uvx pip download pydicom pynetdicom -d ~/QA/ingest-io/wheels`. Other routes don't need it.
- Async-phase wall-clock has ±1 s poll granularity (stated in the report footer).
