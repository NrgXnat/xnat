"""Per-route ingest drivers for the performance suite.

Each ``route_*`` runs one workload through one ingest route against the (already anon-configured)
server and returns a list of phase records ``{phase, wall_s, nfs_mb, wchar_mb}``. Wall-clock is the
in-pod ``curl`` ``time_total`` for blocking calls, or the measured server-side poll duration for
async ones — never the kube-exec round-trip. NFS/wchar are the pod metric deltas bracketing the
phase. Anon mode is configured by the caller before the route runs (see ingest_perf.run); routes
never touch anon state.

Staging (done by the caller) provides in-pod paths via a ``Staged`` record.
"""
from __future__ import annotations

import json
import time
import uuid
from dataclasses import dataclass
from typing import Callable

from k8s import Cluster


@dataclass
class Staged:
    zip_pod_path: str        # workload zip inside the XNAT pod (for zip/cache/direct)
    sender_dir: str          # workload dir inside the sender pod (for cstore)
    inbox_pod_dir: str       # workload dir placed under the XNAT pod's inboxPath (for inbox)
    scp_service: str         # in-cluster DICOM SCP host (e.g. xnat-dicom-scp)
    scp_port: int
    files: int               # object count (for sanity checks)


def _phase(cluster: Cluster, name: str, fn: Callable[[], float | None]) -> dict:
    """Bracket ``fn`` with metric snapshots; wall comes from fn (authoritative) or the local clock."""
    m0 = cluster.metrics()
    t0 = time.monotonic()
    wall = fn()
    dt = time.monotonic() - t0
    m1 = cluster.metrics()
    return {"phase": name, "wall_s": round(wall if wall is not None else dt, 3),
            "nfs_mb": round((m1.nfs_write - m0.nfs_write) / 1e6, 1),
            "wchar_mb": round((m1.wchar - m0.wchar) / 1e6, 1)}


def _one_session(cluster: Cluster, project: str) -> tuple[str, str]:
    rows = cluster.prearchive_rows(project)
    if not rows:
        raise RuntimeError(f"no prearchive session for {project} after receive")
    return rows[0]["timestamp"], rows[0]["folderName"]


def _archive(cluster: Cluster, project: str, ts: str, folder: str) -> float:
    """Explicit archive of a built prearchive session, timed until the prearchive has drained."""
    t0 = time.monotonic()
    r = cluster.archive_session(project, ts, folder)
    if r.http >= 400:
        raise RuntimeError(f"archive failed HTTP {r.http}: {r.body[:200]}")
    cluster.wait_prearchive_empty(project)
    return time.monotonic() - t0


def _build_and_archive(cluster: Cluster, project: str) -> list[dict]:
    """Shared tail for routes that land in the prearchive: explicit build, then archive (no idle wait)."""
    ts_folder: dict[str, str] = {}

    def build() -> float:
        ts, folder = _one_session(cluster, project)
        ts_folder["ts"], ts_folder["folder"] = ts, folder
        r = cluster.build_session(project, ts, folder)
        # Unchecked, a failed build records its own error-response time as the build wall-clock and
        # only surfaces 900 s later as an archive timeout, blamed on the wrong phase. Only 4xx/5xx
        # is failure: the archive service answers a successful archive with a 303 to the new session,
        # and in-pod curl does not follow it.
        if r.http >= 400:
            raise RuntimeError(f"build failed HTTP {r.http}: {r.body[:200]}")
        return r.secs

    return [_phase(cluster, "build", build),
            _phase(cluster, "archive", lambda: _archive(cluster, project, ts_folder["ts"], ts_folder["folder"]))]


# ---------------------------------------------------------------- routes

def route_zip(cluster: Cluster, project: str, s: Staged) -> list[dict]:
    """DICOM-zip inbody import to the prearchive, then build + archive."""
    def receive() -> float:
        r = cluster.curl("POST", "/data/services/import", ctype="application/zip", data_file=s.zip_pod_path,
                         query=f"import-handler=DICOM-zip&inbody=true&project={project}&prearchive_code=0&Ignore-Unparsable=true")
        if r.http != 200:
            raise RuntimeError(f"zip import failed HTTP {r.http}: {r.body[:200]}")
        return r.secs
    return [_phase(cluster, "receive", receive), *_build_and_archive(cluster, project)]


def route_cstore(cluster: Cluster, project: str, s: Staged) -> list[dict]:
    """C-STORE from the sender pod to the SCP, then build + archive."""
    def receive() -> float:
        out = cluster.exec(
            f"python /work/sender.py --host {s.scp_service} --port {s.scp_port} --aet XNAT "
            f"--dir {s.sender_dir}",
            pod=cluster.SENDER_POD, container="sender", timeout=1800)
        summary = json.loads(out.strip().splitlines()[-1])
        if summary.get("failed"):
            raise RuntimeError(f"C-STORE failures: {summary['failed'][:3]}")
        return float(summary["seconds"])
    return [_phase(cluster, "receive", receive), *_build_and_archive(cluster, project)]


def route_cache(cluster: Cluster, project: str, s: Staged) -> list[dict]:
    """Compressed Uploader flow: PUT the zip into the user cache, then commit+autoarchive (async)."""
    upload_id = "perf" + uuid.uuid4().hex[:10]
    src = f"/user/cache/resources/{upload_id}/files/upload.zip"

    def put() -> float:
        r = cluster.curl("PUT", f"/data{src}", query="inbody=true", ctype="application/zip", data_file=s.zip_pod_path)
        if r.http not in (200, 201):
            raise RuntimeError(f"cache PUT failed HTTP {r.http}: {r.body[:200]}")
        return r.secs

    def commit_archive() -> float:
        arch0 = cluster.archived_count(project)
        t0 = time.monotonic()
        r = cluster.curl("POST", "/data/services/import",
                         query=(f"import-handler=DICOM-zip&src={src}&http-session-listener={upload_id}"
                                f"&project={project}&action=commit&auto-archive=TRUE&prearchive_code=1&Ignore-Unparsable=true"))
        if r.http != 200:
            raise RuntimeError(f"cache import failed HTTP {r.http}: {r.body[:200]}")
        # async: source=UPLOADER so the rebuilder skips it; commit drives the archive. Wait for a new
        # archived session to register (the object must archive as a real imaging experiment).
        while time.monotonic() - t0 < 600:
            if cluster.archived_count(project) > arch0:
                return time.monotonic() - t0
            time.sleep(0.5)
        raise RuntimeError("cache commit did not archive within 600s")

    return [_phase(cluster, "cache_put", put), _phase(cluster, "commit_archive", commit_archive)]


def route_direct(cluster: Cluster, project: str, s: Staged) -> list[dict]:
    """Direct-to-archive: zip import with Direct-Archive=true, then force the archive trigger."""
    uri: dict[str, str] = {}

    def receive() -> float:
        r = cluster.curl("POST", "/data/services/import", ctype="application/zip", data_file=s.zip_pod_path,
                         query=(f"import-handler=DICOM-zip&inbody=true&project={project}"
                                f"&Direct-Archive=true&Ignore-Unparsable=true"))
        if r.http != 200:
            raise RuntimeError(f"direct import failed HTTP {r.http}: {r.body[:200]}")
        uri["body"] = r.body.strip()
        return r.secs

    def trigger() -> float:
        arch0 = cluster.archived_count(project)
        t0 = time.monotonic()
        # The receive returns /xapi/direct-archive/{project}/{tag}/{name}; POST it to force triggerArchive.
        path = next((ln for ln in uri["body"].splitlines() if "/direct-archive/" in ln), "")
        if "/direct-archive/" in path:
            cluster.curl("POST", path[path.index("/xapi/"):] if "/xapi/" in path else "/xapi" + path[path.index("/direct-archive/"):])
        while time.monotonic() - t0 < 600:
            if cluster.archived_count(project) > arch0:
                return time.monotonic() - t0
            time.sleep(0.5)
        raise RuntimeError("direct-archive did not complete within 600s")

    return [_phase(cluster, "receive", receive), _phase(cluster, "trigger_archive", trigger)]


def route_inbox(cluster: Cluster, project: str, s: Staged) -> list[dict]:
    """Inbox: files already staged under inboxPath; POST import-handler=inbox (async). The import
    listener queues XNAT's own Rebuild once the last object has landed, so an explicit build request
    finds the session locked (HTTP 409): the build phase waits for that build to reach READY instead,
    then the session is archived explicitly like the other prearchive routes. The project must NOT
    auto-archive: the Rebuild handler would queue an archive carrying the inbox import request id,
    and every such operation dies in AbstractPrearchiveOperationHandler.progress() with a
    LazyInitializationException on the detached DicomInboxImportRequest proxy, leaving the session
    QUEUED_ARCHIVING for good (setup sets PERF to Manual). The build may already be under way when
    receive returns, so the pod's ``Built DICOM session`` line is the authoritative build time here."""
    ts_folder: dict[str, str] = {}

    def receive() -> float:
        t0 = time.monotonic()
        r = cluster.curl("POST", "/data/services/import",
                         query=f"import-handler=inbox&path={s.inbox_pod_dir}&PROJECT_ID={project}&cleanupAfterImport=true")
        if r.http not in (200, 201):
            raise RuntimeError(f"inbox import failed HTTP {r.http}: {r.body[:200]}")
        # async JMS: wait until the session appears AND every object has landed. The row appears
        # after the first file, so building on that alone builds and archives a fraction of the
        # workload -- a different, nondeterministic amount of work per rep.
        while time.monotonic() - t0 < 1800:
            rows = cluster.prearchive_rows(project)
            if rows and cluster.prearchive_file_count(
                    project, rows[0]["timestamp"], rows[0]["folderName"]) >= s.files:
                return time.monotonic() - t0
            time.sleep(0.5)
        raise RuntimeError(f"inbox import did not deliver {s.files} files within 1800s")

    def build() -> float:
        t0 = time.monotonic()
        while time.monotonic() - t0 < 900:
            rows = cluster.prearchive_rows(project)
            status = rows[0].get("status", "") if rows else ""
            if status == "READY":
                ts_folder["ts"], ts_folder["folder"] = rows[0]["timestamp"], rows[0]["folderName"]
                return time.monotonic() - t0
            if status in ("ERROR", "CONFLICT") or status.startswith("QUEUED_ARCHIVING"):
                raise RuntimeError(f"inbox session unexpectedly in status {status} (project auto-archive on?)")
            time.sleep(0.5)
        raise RuntimeError("inbox session did not reach READY within 900s")

    return [_phase(cluster, "receive", receive), _phase(cluster, "build", build),
            _phase(cluster, "archive", lambda: _archive(cluster, project, ts_folder["ts"], ts_folder["folder"]))]


ROUTES: dict[str, Callable[[Cluster, str, Staged], list[dict]]] = {
    "zip": route_zip,
    "cstore": route_cstore,
    "cache": route_cache,
    "direct": route_direct,
    "inbox": route_inbox,
}
