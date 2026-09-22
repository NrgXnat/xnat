"""XNAT DICOM ingest performance harness — CLI.

Runs one synthetic (or supplied) corpus through several ingest routes, with and without
anonymization, entirely in-cluster (so the kube tunnel never sits in the measured path), and records
wall-clock + NFS-write per phase. Compare two runs (baseline vs candidate WAR) with ``report``.

    uv run ingest_perf.py setup    --k8s ctx:ns:pod --user admin --pass admin
    uv run ingest_perf.py run      --k8s ctx:ns:pod --user admin --pass admin --label baseline \
                                   [--workloads small,multiframe] [--routes zip,cstore,cache,direct,inbox] \
                                   [--anon none,site-header,site-pixel,project-header,project-pixel] \
                                   [--reps 3] [--corpus DIR] [--out results/]
    uv run ingest_perf.py report   --compare results/baseline.json results/candidate.json -o report.md
    uv run ingest_perf.py teardown --k8s ctx:ns:pod --user admin --pass admin

Only run against synthetic / dev instances (Scout PHI rules).
"""
from __future__ import annotations

import argparse
import os
import shutil
import sys
import uuid
import zipfile
from pathlib import Path

import corpus
import report
from k8s import Cluster
from routes import ROUTES, Staged

HERE = Path(__file__).resolve().parent
PROJECT = "PERF"
POD_STAGE = "/tmp/ingest-perf"                 # workloads + anon scripts inside the XNAT pod
SENDER_STAGE = "/work"                         # workloads inside the sender pod
WHEELS = str(Path.home() / "QA/ingest-io/wheels")   # default --wheels: pydicom/pynetdicom wheels for the sender pod
IN_SENDER = {"huge"}                           # >2 GB: generated in the sender pod, cstore-only (won't cross the tunnel)

# anon mode -> (site_enable, site_script, project_enable, project_script). Scripts are basenames in anon/.
ANON = {
    "none":           (False, None, False, None),
    "site-header":    (True, "site_header.das", False, None),
    "site-pixel":     (True, "site_pixel.das", False, None),
    "project-header": (False, None, True, "project_header.das"),
    "project-pixel":  (False, None, True, "project_pixel.das"),
}


def make_cluster(args) -> Cluster:
    ctx, ns, pod = args.k8s.split(":")
    c = Cluster(ctx, ns, pod, args.user, args.__dict__["pass"])
    c.wheels = getattr(args, "wheels", WHEELS)
    return c


def _is_transient(e: Exception) -> bool:
    """True for kube-tunnel connectivity errors (retry the cell), false for real app failures."""
    s = str(e).lower()
    kube = ("tls handshake timeout", "unable to connect to the server", "i/o timeout",
            "context deadline exceeded", "connection refused", "connection reset",
            "broken pipe", "unexpected eof", "error validating", "the server is currently unable",
            "cp failed after", "exec failed")
    app = ("did not archive within", "did not complete within", "failed http", "contains no")
    return any(k in s for k in kube) and not any(a in s for a in app)


def _zip_dir(src: Path, dst_zip: Path) -> None:
    with zipfile.ZipFile(dst_zip, "w", zipfile.ZIP_STORED) as z:
        for p in sorted(src.rglob("*")):
            if p.is_file() and p.name != "manifest.tsv":
                z.write(p, p.relative_to(src))


def cmd_setup(args) -> None:
    c = make_cluster(args)
    print(f"setup: build {c.build_sha()[:10]} on {c.ctx}/{c.ns}/{c.pod}")
    c.ensure_project(PROJECT)
    c.ensure_receiver("XNAT", anon=True)          # site/project anon controlled globally; receiver stays on
    # stage the anon scripts into the pod
    c.exec(f"mkdir -p {POD_STAGE}/anon")
    for das in sorted((HERE / "anon").glob("*.das")):
        c.cp_to(str(das), f"{POD_STAGE}/anon/{das.name}")
    print(f"  project {PROJECT} ready, receiver XNAT on 8104, anon scripts staged under {POD_STAGE}/anon")


def configure_anon(c: Cluster, mode: str) -> None:
    site_en, site_script, proj_en, proj_script = ANON[mode]
    c.set_site_anon(site_en, f"{POD_STAGE}/anon/{site_script}" if site_script else None)
    c.set_project_anon(PROJECT, proj_en, f"{POD_STAGE}/anon/{proj_script}" if proj_script else None)


def stage_workload(c: Cluster, name: str, routes: list[str], corpus_dir: Path | None) -> tuple[Staged, Path | None, int]:
    """Stage a workload where each route needs it. Returns (staged, local_dir_or_None, total_bytes).

    Big workloads (IN_SENDER, e.g. >2 GB ``huge``) are generated *inside the sender pod* and only
    C-STOREd — a 2 GB file won't cross the kube tunnel in reasonable time — so there is no local dir.
    """
    sender_dir = f"{SENDER_STAGE}/{name}"
    if name in IN_SENDER:
        c.ensure_sender_pod(c.wheels, str(HERE / "sender.py"))
        c.cp_to(str(HERE / "corpus.py"), "/work/corpus.py", pod=c.SENDER_POD, container="sender")
        c.exec(f"rm -rf {sender_dir}; mkdir -p {sender_dir}", pod=c.SENDER_POD, container="sender")
        c.exec(f"cd /work && python -c \"from corpus import gen_{name}; from pathlib import Path; "
               f"gen_{name}(Path('{sender_dir}'), '{PROJECT}')\"",
               pod=c.SENDER_POD, container="sender", timeout=900)
        nbytes = int(c.exec(f"du -sb {sender_dir} | cut -f1", pod=c.SENDER_POD, container="sender").split()[0])
        return Staged(zip_pod_path="", sender_dir=sender_dir, inbox_pod_dir="",
                      scp_service="xnat-dicom-scp", scp_port=8104, files=1), None, nbytes

    local = Path("/tmp/ingest-perf-local") / name
    man = corpus.build_workload(name, local, PROJECT, corpus=corpus_dir)
    zip_local = local.parent / f"{name}.zip"
    _zip_dir(local, zip_local)
    zip_pod = f"{POD_STAGE}/{name}.zip"
    if {"zip", "cache", "direct"} & set(routes):
        c.exec(f"mkdir -p {POD_STAGE}")
        c.cp_to(str(zip_local), zip_pod)
    if "cstore" in routes:
        c.exec(f"rm -rf {sender_dir}; mkdir -p {sender_dir}", pod=c.SENDER_POD, container="sender")
        c.cp_to(str(local), sender_dir, pod=c.SENDER_POD, container="sender")
    nbytes = sum(p.stat().st_size for p in local.rglob("*.dcm"))
    return Staged(zip_pod_path=zip_pod, sender_dir=sender_dir, inbox_pod_dir="",
                  scp_service="xnat-dicom-scp", scp_port=8104, files=len(man)), local, nbytes


def _inbox_root(c: Cluster) -> str:
    v = c.curl("GET", "/xapi/siteConfig/inboxPath").body.strip().strip('"')
    return v or "/data/xnat/inbox"


def restage_inbox(c: Cluster, name: str, local: Path) -> str:
    """Inbox consumes (cleanupAfterImport) the files, so place a fresh copy under inboxPath per rep."""
    root = _inbox_root(c)
    dest = f"{root}/{PROJECT}/{name}-{uuid.uuid4().hex[:8]}"
    c.exec(f"mkdir -p {dest}")
    c.cp_to(str(local), dest)
    return dest


def cmd_run(args) -> None:
    c = make_cluster(args)
    workloads = args.workloads.split(",")
    routes = args.routes.split(",")
    anon_modes = args.anon.split(",")
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    out_path = out_dir / f"{args.label}.json"

    if "cstore" in routes:
        c.ensure_sender_pod(c.wheels, str(HERE / "sender.py"))

    header = {"label": args.label, "build_sha": c.build_sha(), "storage": c.storage_layout(),
              "reps": args.reps, "workloads": {}, "routes": routes, "anon": anon_modes}
    cells: list[dict] = []
    done: set[tuple] = set()
    if out_path.exists():                     # resume: keep prior ok cells, skip re-running them
        prev = report.load(out_path)
        cells = [x for x in prev.get("cells", []) if x.get("ok")]
        header["workloads"] = prev.get("header", {}).get("workloads", {})
        done = {(x["workload"], x["route"], x["anon"], x["rep"]) for x in cells}
        print(f"resuming: {len(done)} cells already recorded in {out_path}")

    def remaining(wl: str) -> bool:
        applicable = ["cstore"] if wl in IN_SENDER else routes
        return any((wl, rt, md, rp) not in done
                   for rt in applicable for md in anon_modes for rp in range(1, args.reps + 1))

    for wl in workloads:
        if not remaining(wl):
            print(f"workload {wl}: all cells already done, skipping")
            continue
        staged, local, nbytes = stage_workload(c, wl, routes, Path(args.corpus) if args.corpus else None)
        header["workloads"][wl] = {"files": staged.files, "bytes": nbytes}
        print(f"workload {wl}: {staged.files} objects, {nbytes/1e6:.1f} MB")
        for mode in anon_modes:
            configure_anon(c, mode)
            for route in routes:
                if wl in IN_SENDER and route != "cstore":
                    print(f"  [skip] {wl}/{route}: >2 GB workload is cstore-only (won't cross the tunnel)")
                    continue
                for rep in range(1, args.reps + 1):
                    if (wl, route, mode, rep) in done:
                        continue
                    c.wait_healthy()   # before each cell; a >8h outage raises here and aborts (checkpoint saved)
                    cell = {"workload": wl, "route": route, "anon": mode, "rep": rep, "files": staged.files}
                    for attempt in range(1, 6):
                        try:
                            c.wipe_project(PROJECT)
                            if route == "inbox":
                                staged.inbox_pod_dir = restage_inbox(c, wl, local)
                            cell["phases"] = ROUTES[route](c, PROJECT, staged)
                            cell["ok"] = True
                            break
                        except Exception as e:
                            if _is_transient(e) and attempt < 5:
                                print(f"    transient on {route} rep{rep} (try {attempt}): {str(e)[:70]} — waiting + retrying cell", flush=True)
                                c.wait_healthy()   # pause through the outage, then retry the same cell
                                continue
                            cell["phases"], cell["ok"], cell["error"] = [], False, str(e)[:300]
                            break
                    tag = "ok" if cell["ok"] else f"FAIL: {cell.get('error', '')[:80]}"
                    walls = " ".join(f"{p['phase']}={p['wall_s']}s/{p['nfs_mb']}MB" for p in cell["phases"])
                    print(f"  [{mode}] {route} rep{rep}: {tag}  {walls}", flush=True)
                    cells.append(cell)
                    # checkpoint after every cell so a tunnel drop mid-sweep never loses prior results
                    header["cell_count"] = len(cells)
                    report.write_results(out_path, header, cells)
    c.wipe_project(PROJECT)
    print(f"\nwrote {out_path} ({len(cells)} cells, {sum(1 for x in cells if not x['ok'])} failed)")


def cmd_report(args) -> None:
    base, cand = args.compare
    text = report.compare(Path(base), Path(cand), Path(args.out))
    print(text)


def cmd_teardown(args) -> None:
    c = make_cluster(args)
    c.delete_sender_pod()
    # restore a benign anon state: site enabled with the header script, no project script
    configure_anon(c, "site-header")
    if args.delete_project:
        c.curl("DELETE", f"/data/projects/{PROJECT}")
    print("teardown: sender pod deleted, anon restored to site-header" +
          (", project deleted" if args.delete_project else ""))


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    def add_conn(p):
        p.add_argument("--k8s", required=True, help="context:namespace:pod")
        p.add_argument("--user", default="admin")
        p.add_argument("--pass", default="admin", dest="pass")
        p.add_argument("--wheels", default=WHEELS,
                       help="directory of pydicom/pynetdicom wheels installed into the sender pod "
                            "offline (cstore and huge only); default %(default)s")

    p = sub.add_parser("setup"); add_conn(p); p.set_defaults(fn=cmd_setup)
    p = sub.add_parser("run"); add_conn(p)
    p.add_argument("--label", required=True)
    p.add_argument("--workloads", default="small,multiframe")
    p.add_argument("--routes", default="zip,cstore,cache,direct,inbox")
    p.add_argument("--anon", default="none,site-header,site-pixel,project-header,project-pixel")
    p.add_argument("--reps", type=int, default=3)
    p.add_argument("--corpus", default=None, help="use real DICOM under this dir instead of synthesizing")
    p.add_argument("--out", default="results")
    p.set_defaults(fn=cmd_run)
    p = sub.add_parser("report")
    p.add_argument("--compare", nargs=2, metavar=("BASELINE", "CANDIDATE"), required=True)
    p.add_argument("-o", "--out", default="report.md")
    p.set_defaults(fn=cmd_report)
    p = sub.add_parser("teardown"); add_conn(p)
    p.add_argument("--delete-project", action="store_true")
    p.set_defaults(fn=cmd_teardown)

    args = ap.parse_args()
    args.fn(args)


if __name__ == "__main__":
    main()
