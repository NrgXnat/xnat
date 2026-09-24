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

Every timed operation runs **inside the XNAT pod** (or a sender pod), so the `kubectl` API connection is
never in the measured path. The orchestrator only issues control calls and parses JSON from in-pod
`curl`.

- **Wall-clock**: in-pod `curl`'s own `time_total` for blocking calls; measured server-side poll
  duration (±1 s) for async routes (cache/inbox/direct trigger). The RECEIVING→READY *idle timeout*
  (`sessionXmlRebuilderInterval`, default 5 min) is **never** timed — every route drives
  `action=build`/`action=commit`/the direct-archive trigger explicitly.
- **NFS write**: NFS server-write bytes from `/proc/self/mountstats` (`bytes:` field 6) on the
  archive mount. Archive/prearchive/cache share one NFS export and report identical aggregate stats,
  so one number covers the pod's writes to NFS. Also `/proc/1/io` `wchar` (all `write()` bytes).

## Routes and anon modes

| Route | Mechanics |
|---|---|
| `zip` | DICOM-zip inbody `POST /data/services/import` → prearchive; then `action=build` + `/data/services/archive` |
| `cstore` | sender pod → SCP receiver `XNAT` on 8104; then build + archive |
| `cache` | Compressed Uploader flow: `PUT /data/user/cache/...` then `POST import src=…&action=commit&auto-archive` (async) |
| `direct` | zip import with `Direct-Archive=true`; then `POST /xapi/direct-archive/{p}/{tag}/{name}` to force the archive |
| `inbox` | files staged under `inboxPath`; `POST import-handler=inbox` (async JMS); XNAT's own Rebuild builds the session (the route waits for READY), then explicit archive. Needs the project on Manual prearchive handling, which `setup` sets — see `route_inbox` |

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
uv run python ingest_perf.py setup --k8s my-cluster:xnat:xnat-0 --user admin --pass admin

# baseline build (develop), then swap in the candidate WAR and re-run with --label candidate
uv run python ingest_perf.py run --k8s my-cluster:xnat:xnat-0 --user admin --pass admin \
    --label baseline --workloads small,multiframe --routes zip,cstore,cache,direct,inbox \
    --anon none,site-header,site-pixel,project-header,project-pixel --reps 3 --out results/

# the >2 GB case (run on its own; slow)
uv run python ingest_perf.py run --k8s ... --label baseline-huge \
    --workloads huge --routes zip,cstore --anon none,site-pixel,project-pixel --reps 1 --out results/

# compare two runs
uv run python ingest_perf.py report --compare results/baseline.json results/candidate.json -o report.md

# output verification: add --verify to both runs and the report gains a per-cell table saying whether the
# candidate archived the same objects, catalog entries and session XML as the baseline (see verify.py)
uv run python ingest_perf.py run --k8s ... --label candidate --verify --workloads small,mixed,multiframe \
    --routes zip,direct --anon none,project-header --reps 2 --out results/

# clean up (delete sender pod, restore anon; --delete-project also removes PERF)
uv run python ingest_perf.py teardown --k8s ... [--delete-project]
```

Results are one JSON per `--label` (self-describing header: build sha, storage layout, corpus
manifest, and the instance it ran on: node, instance type, image digest, JVM) plus the A/B `report.md`.
Each phase also records `nfs_ops` and `nfs_rtt_ms`, the NFS operations it caused on the archive mount and
their mean round trip, from the pod's `mountstats`: how often the phase went to the storage, and how fast
the storage answered while it did.

## Several instances on shared storage (`rounds.py`, `measure_lock.py`)

Most of a run is not measurement: the wipe between cells (XNAT deleting the previous session and its files)
took 60–80 % of the wall time in the runs so far. Several instances can share that overhead without
measuring each other, if no instance is ever measured while another loads the storage:

- `--lock PATH` on `setup` and `run` makes every harness process that names the same lock file (all on this
  machine) take it **exclusively** for a cell's route and the settle after it, and **shared** for everything
  else: staging, anonymization setup, the wipe, the inbox restage, digests. So at any moment one instance is
  measured and every other waits, or any number do their overhead side by side. A waiting measurement goes
  before new overhead. Running instances side by side *without* the lock would let them slow each other,
  and slow the builds that make more NFS round trips the most, which inflates their differences.
- `rounds.py PLAN` runs rounds of legs across instances (`fourway.example.json`): each round, every instance
  swaps to its leg's image, runs setup and a warm-up, then its block under its own label
  `<prefix>-<leg>-b<n>`, all under one lock. The next round starts when every instance is done, so whatever
  the shared storage does at a given hour lands on that round's legs alike, and rotating legs across
  instances keeps any instance's quirks out of the comparison. Completed blocks leave a marker; a rerun
  resumes. `--dry-run` prints the schedule; `--merge` writes `<prefix>-<leg>.json` per leg
  (`merge_results.py`), which `report --compare` reads like any results file.
- Give each instance its own node of one instance type, its own database, and a fresh database at the start:
  the example plan runs 1.10.1 on every instance first and only ever upgrades afterwards.
- `uv run python -m unittest measure_lock_test` checks the lock's guarantees.

## Constraints

- **Synthetic / dev targets only.** Never point this at an instance that holds patient data.
- The suite drives an existing server; it never deploys WARs — swap builds out of band between runs.
- Requires `kubectl` access to the pod and (for `cstore`) permission to run a short-lived sender pod.
- The `cstore` and `huge` cells install pydicom/pynetdicom into the sender pod **offline**, from a
  local wheel directory: `--wheels DIR` (default `wheels/` beside the harness, or `$INGEST_PERF_WHEELS`). Populate it once with
  `uvx pip download pydicom pynetdicom -d wheels`. Other routes don't need it.
- Async-phase wall-clock has ±1 s poll granularity (stated in the report footer).
