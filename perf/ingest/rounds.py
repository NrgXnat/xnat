"""Drive an A/B across several XNAT instances that share storage: rounds of legs, one harness process per
instance, measurements serialized by a measurement lock so no instance is measured while another one loads
the storage (see measure_lock.py).

A plan (JSON, see fourway.example.json) names the instances, the legs (image tags), the rounds (which leg
each instance runs in each round), the matrix, and where results go. In each round every instance, in
parallel: swaps to its leg's image (helm upgrade, then waits until XNAT reports that version), runs setup
and a warm-up, then runs its block: the plan's matrix, one rep, under its own label <prefix>-<leg>-b<n>, so
no two processes ever write one results file. The next round starts when every instance has finished, so
whatever the shared storage does at a given hour lands on the legs of that round alike. A block that
completed leaves a marker and is skipped on a rerun, so a rerun resumes.

    uv run rounds.py fourway.json --dry-run   # print the schedule and the commands
    uv run rounds.py fourway.json             # run or resume every round
    uv run rounds.py fourway.json --merge     # merge each leg's blocks into <prefix>-<leg>.json

Only run against synthetic / dev instances, never one that holds patient data.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
from pathlib import Path

import merge_results
import report
from k8s import Cluster
from measure_lock import MeasureLock

HERE = Path(__file__).resolve().parent
_print_lock = threading.Lock()


def say(message: str) -> None:
    with _print_lock:
        print(f"== {datetime.now(timezone.utc).strftime('%H:%M:%SZ')} {message}", flush=True)


def load_plan(path: Path) -> dict:
    plan = json.loads(path.read_text())
    plan["out"] = str(Path(plan["out"]).expanduser())
    plan["lock"] = str(Path(plan["lock"]).expanduser())
    n = len(plan["instances"])
    for i, row in enumerate(plan["rounds"]):
        if len(row) != n:
            raise SystemExit(f"round {i + 1} names {len(row)} legs for {n} instances")
        unknown = set(row) - set(plan["legs"])
        if unknown:
            raise SystemExit(f"round {i + 1} names unknown legs {sorted(unknown)}")
    return plan


def schedule(plan: dict) -> list[list[dict]]:
    """Per round, per instance: the leg, its block number (in round order, then instance order) and label."""
    counts: dict[str, int] = {}
    rounds = []
    for r, row in enumerate(plan["rounds"], start=1):
        entries = []
        for ns, leg in zip(plan["instances"], row):
            counts[leg] = counts.get(leg, 0) + 1
            entries.append({"round": r, "instance": ns, "leg": leg, "block": counts[leg],
                            "label": f"{plan['prefix']}-{leg}-b{counts[leg]}"})
        rounds.append(entries)
    return rounds


class Instance:
    def __init__(self, plan: dict, ns: str):
        self.plan, self.ns = plan, ns
        self.ctx, self.pod = plan["context"], plan.get("pod", "xnat-0")
        self.k8s = f"{self.ctx}:{ns}:{self.pod}"
        self.log_dir = Path(plan["out"]) / "logs"
        self.log_dir.mkdir(parents=True, exist_ok=True)

    def _run(self, cmd: list[str], log: Path, timeout: int | None = None) -> int:
        with open(log, "a") as f:
            f.write(f"\n$ {' '.join(cmd)}\n")
            f.flush()
            return subprocess.run(cmd, stdout=f, stderr=subprocess.STDOUT, timeout=timeout, cwd=HERE).returncode

    def _kubectl(self, *args: str, timeout: int = 60) -> subprocess.CompletedProcess:
        return subprocess.run(["kubectl", "--context", self.ctx, "-n", self.ns, *args],
                              capture_output=True, timeout=timeout)

    def swap(self, tag: str, version: str, log: Path) -> None:
        """helm upgrade to ``tag`` and wait until XNAT answers with ``version``. Shared with the other
        instances' overhead, never with a measurement: a restarting XNAT touches the storage too."""
        with MeasureLock(self.plan["lock"]).busy():
            if not self._serving(tag, version):
                for attempt in range(1, 7):
                    cmd = ["helm", "--kube-context", self.ctx, "upgrade", self.plan.get("release", "xnat"),
                           self.plan["chart"], "--version", self.plan["chart_version"], "-n", self.ns,
                           "--reuse-values", "--set", f"image.tag={tag}", "--wait=false"]
                    if self._run(cmd, log, timeout=300) == 0:
                        break
                    say(f"{self.ns}: helm upgrade to {tag} failed (attempt {attempt}); retrying in 2 min")
                    if attempt == 6:
                        raise RuntimeError(f"{self.ns}: helm upgrade to {tag} failed six times")
                    time.sleep(120)
            deadline = time.monotonic() + 1800
            while not self._serving(tag, version):
                if time.monotonic() > deadline:
                    raise RuntimeError(f"{self.ns}: XNAT did not come up on {tag} within 30 min")
                time.sleep(15)

    def _serving(self, tag: str, version: str) -> bool:
        try:
            r = self._kubectl("get", "pod", self.pod, "-o", "json")
            if r.returncode != 0:
                return False
            pod = json.loads(r.stdout)
            image = next((c.get("image", "") for c in pod["spec"]["containers"] if c["name"] == "xnat"), "")
            ready = any(s.get("name") == "xnat" and s.get("ready") for s in pod.get("status", {}).get("containerStatuses", []))
            if image.rsplit(":", 1)[-1] != tag or not ready:
                return False
            r = self._kubectl("exec", self.pod, "-c", "xnat", "--", "curl", "-s", "-m", "10", "-u",
                              f"{self.plan.get('user', 'admin')}:{self.plan.get('password', 'admin')}",
                              "http://localhost:8080/xapi/siteConfig/buildInfo")
            return json.loads(r.stdout or b"{}").get("version") == version
        except Exception:
            return False

    def harness(self, args: list[str], log: Path) -> int:
        cmd = [sys.executable, str(HERE / "ingest_perf.py"), *args, "--k8s", self.k8s,
               "--user", self.plan.get("user", "admin"), "--pass", self.plan.get("password", "admin"),
               "--lock", self.plan["lock"]]
        if self.plan.get("wheels"):
            cmd += ["--wheels", str(Path(self.plan["wheels"]).expanduser())]
        return self._run(cmd, log)

    def cluster(self) -> Cluster:
        return Cluster(self.ctx, self.ns, self.pod, self.plan.get("user", "admin"), self.plan.get("password", "admin"))

    def pod_timing(self, since: str, dest: Path) -> None:
        r = self._kubectl("logs", self.pod, "-c", "xnat", f"--since-time={since}", "--timestamps", timeout=300)
        keep = ("Built DICOM session", "Merged ", "Archived ", "Direct-archived", "Stored ")
        lines = [l for l in r.stdout.decode(errors="replace").splitlines() if any(k in l for k in keep)]
        dest.write_text("\n".join(lines) + ("\n" if lines else ""))


def run_block(plan: dict, entry: dict, dry_run: bool) -> bool:
    out = Path(plan["out"])
    label, leg, ns = entry["label"], entry["leg"], entry["instance"]
    marker = out / f"{label}.done"
    spec = plan["legs"][leg]
    tag, version = spec["tag"], spec.get("version", spec["tag"])
    matrix = list(plan["block"]) + (list(plan.get("first_block_extra", [])) if entry["block"] == 1 else [])
    run_args = []
    for m in matrix:
        run_args.append(["run", "--label", label, "--reps", "1", "--out", str(out),
                         "--workloads", m["workloads"], "--routes", m["routes"], "--anon", m["anon"]]
                        + (["--verify"] if plan.get("verify", True) else []))
    warm = plan.get("warmup")
    if dry_run:
        say(f"round {entry['round']} {ns}: leg {leg} ({tag}) block {label}" + ("  [done]" if marker.exists() else ""))
        for a in run_args:
            print("      ingest_perf.py " + " ".join(a[1:]))
        return True
    if marker.exists():
        say(f"{ns}: {label} already complete, skipping")
        return True
    inst = Instance(plan, ns)
    log = inst.log_dir / f"{label}.log"
    say(f"{ns}: round {entry['round']}, leg {leg}: swapping to {tag}")
    inst.swap(tag, version, log)
    if inst.harness(["setup"], log) != 0:
        say(f"{ns}: setup failed on {tag}; see {log}")
        return False

    # The builds under test stage anonymized files at the data root a file sits under, and fall back to the
    # temp directory (a copy, as before) without a word when the site configuration's roots don't match
    # where the files are. So check the roots, and check the staging directory appears where it should,
    # before measuring anything: a silent fallback would make a build look like its baseline.
    c = inst.cluster()
    roots = c.data_roots()
    archive, prearchive = roots["archivePath"], roots["prearchivePath"]
    if not archive["mount"] or archive["mount"] != prearchive["mount"]:
        say(f"{ns}: archive {archive} and prearchive {prearchive} are not on one mount; not measuring")
        return False
    staging_roots = [archive["path"], prearchive["path"]]
    expects_staging = bool(spec.get("stages_on_volume"))
    staging = {"roots": roots, "expects_staging": expects_staging}
    with MeasureLock(plan["lock"]).busy():
        c.clear_staging(staging_roots)
    if warm:
        # A fresh label per attempt: resuming a warm-up would skip its cells, and the staging check below needs
        # them to have run.
        attempt = datetime.now(timezone.utc).strftime("%H%M%S")
        inst.harness(["run", "--label", f"{plan['prefix']}-warmup-{ns}-r{entry['round']}-{attempt}", "--reps", "1",
                      "--out", str(out / "warmup"), "--workloads", warm["workloads"], "--routes", warm["routes"],
                      "--anon", warm["anon"]], log)
        staging["after_warmup"] = c.staging_present(staging_roots)
        if not _staging_as_expected(ns, leg, expects_staging, staging["after_warmup"], "the warm-up"):
            (out / f"{label}.staging.json").write_text(json.dumps(staging, indent=2))
            return False
        with MeasureLock(plan["lock"]).busy():
            c.clear_staging(staging_roots)
    started = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    say(f"{ns}: running {label}")
    ok = True
    for a in run_args:
        if inst.harness(a, log) != 0:
            ok = False
            say(f"{ns}: {label} invocation failed ({' '.join(a[7:13])}); see {log}")
    inst.pod_timing(started, out / f"{label}-timing.log")
    staging["after_block"] = c.staging_present(staging_roots)
    (out / f"{label}.staging.json").write_text(json.dumps(staging, indent=2))
    ok = _staging_as_expected(ns, leg, expects_staging, staging["after_block"], label) and ok
    results = out / f"{label}.json"
    failed = [x for x in report.load(results).get("cells", []) if not x.get("ok")] if results.exists() else ["missing"]
    if ok and not failed:
        marker.write_text(started + "\n")
        say(f"{ns}: {label} complete")
        return True
    say(f"{ns}: {label} incomplete ({len(failed)} failed cells); a rerun resumes it")
    return False


def _staging_as_expected(ns: str, leg: str, expected: bool, present: dict[str, bool], when: str) -> bool:
    """A leg that stages beside the data must have made a staging directory under a data root during a run
    with project scripts; one that doesn't must not have. Missing on one root only is reported, not fatal:
    the other root's anonymization may not have run in that window."""
    made = [p for p, yes in present.items() if yes]
    if expected and not made:
        say(f"{ns}: leg {leg} made no staging directory under {sorted(present)} during {when}: it fell back to "
            f"the temp directory, so its numbers would be the old copy path; not counting it")
        return False
    if expected and len(made) < len(present):
        say(f"{ns}: leg {leg} staged under {made} but not {[p for p in present if p not in made]} during {when}")
    if not expected and made:
        say(f"{ns}: leg {leg} is not expected to stage beside the data, yet {made} appeared during {when}")
    return True


def run(plan: dict, dry_run: bool) -> int:
    Path(plan["out"]).mkdir(parents=True, exist_ok=True)
    for entries in schedule(plan):
        r = entries[0]["round"]
        say(f"round {r}: " + ", ".join(f"{e['instance']}={e['leg']}" for e in entries))
        with ThreadPoolExecutor(max_workers=len(entries)) as pool:
            results = list(pool.map(lambda e: _guarded(plan, e, dry_run), entries))
        if not all(results):
            say(f"round {r} did not complete on every instance; stopping so the next round starts level. "
                f"Rerun to resume.")
            return 1
    say("all rounds complete" if not dry_run else "dry run complete")
    return 0


def _guarded(plan: dict, entry: dict, dry_run: bool) -> bool:
    try:
        return run_block(plan, entry, dry_run)
    except Exception as e:
        say(f"{entry['instance']}: {entry['label']} failed: {str(e)[:200]}")
        return False


def merge(plan: dict) -> None:
    out = Path(plan["out"])
    for leg in plan["legs"]:
        blocks = sorted(out.glob(f"{plan['prefix']}-{leg}-b*.json"))
        if not blocks:
            say(f"leg {leg}: no blocks yet")
            continue
        label = f"{plan['prefix']}-{leg}"
        merged = merge_results.merge(blocks, label)
        report.write_results(out / f"{label}.json", merged["header"], merged["cells"])
        say(f"leg {leg}: {len(blocks)} blocks, {merged['header']['cell_count']} cells -> {label}.json")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("plan")
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--merge", action="store_true", help="merge each leg's blocks and exit")
    args = ap.parse_args()
    plan = load_plan(Path(args.plan))
    if args.merge:
        merge(plan)
        return
    sys.exit(run(plan, args.dry_run))


if __name__ == "__main__":
    main()
