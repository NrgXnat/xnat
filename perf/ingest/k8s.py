"""In-cluster driver plumbing for the ingest performance suite.

All timed data-path work runs *inside* the XNAT pod (or a sender pod), so the kube API tunnel never
sits in the measured path. The orchestrator (this process) only issues ``kubectl exec``/``cp`` control
calls and parses JSON returned from in-pod ``curl``.

Metrics come from the pod, not the block layer: NFS writes to FSx are counted from
``/proc/self/mountstats`` ``bytes:`` field 6 (server write bytes) on the archive mount — the archive,
prearchive and cache mounts share one FSx export and report identical aggregate stats, so read one —
and app-level write bytes from ``/proc/1/io`` ``wchar``.
"""
from __future__ import annotations

import json
import shlex
import subprocess
import time
from dataclasses import dataclass


@dataclass
class CurlResult:
    http: int
    secs: float          # curl's own time_total, measured in-pod (no tunnel)
    body: str


@dataclass
class Metrics:
    nfs_write: int       # FSx serverwrite bytes (mountstats field 6 on the archive mount)
    wchar: int           # /proc/1/io wchar bytes (all write() bytes by the XNAT jvm)


class Cluster:
    """Talks to one XNAT pod. ``base`` is the in-pod XNAT URL (Tomcat, no tunnel)."""

    def __init__(self, context: str, namespace: str, pod: str, user: str, password: str,
                 container: str = "xnat", base: str = "http://localhost:8080"):
        self.ctx, self.ns, self.pod, self.container = context, namespace, pod, container
        self.user, self.password, self.base = user, password, base

    # ---- low-level exec / cp -------------------------------------------------
    def _kubectl(self, *args: str, input_bytes: bytes | None = None, timeout: int = 600) -> subprocess.CompletedProcess:
        cmd = ["kubectl", "--context", self.ctx, "-n", self.ns, *args]
        return subprocess.run(cmd, input=input_bytes, capture_output=True, timeout=timeout)

    def exec(self, script: str, *, pod: str | None = None, container: str | None = None,
             timeout: int = 600) -> str:
        """Run ``sh -c script`` in a pod; return stdout (raises on non-zero)."""
        r = self._kubectl("exec", pod or self.pod, "-c", container or self.container, "--",
                           "sh", "-c", script, timeout=timeout)
        if r.returncode != 0:
            raise RuntimeError(f"exec failed ({r.returncode}): {r.stderr.decode(errors='replace')[:400]}")
        return r.stdout.decode(errors="replace")

    def cp_to(self, local: str, dest: str, *, pod: str | None = None, container: str | None = None) -> None:
        tgt = f"{pod or self.pod}:{dest}"
        # cp is idempotent, so retry transient tunnel timeouts (the localhost API tunnel is flaky).
        last = ""
        for attempt in range(3):
            r = self._kubectl("cp", local, tgt, "-c", container or self.container, timeout=1800)
            if r.returncode == 0:
                return
            last = r.stderr.decode(errors="replace")[:400]
            time.sleep(3)
        raise RuntimeError(f"cp failed after 3 attempts: {last}")

    def wait_healthy(self, max_wait: int = 28800, poll: int = 20) -> None:
        """Block until the kube API (the localhost tunnel) answers. Patient by design: an overnight
        run pauses here through a tunnel outage and continues when it comes back (up to 8 h)."""
        t0 = time.monotonic()
        first = True
        while time.monotonic() - t0 < max_wait:
            r = self._kubectl("get", "pod", self.pod, "--no-headers", "--request-timeout=8s", timeout=15)
            if r.returncode == 0:
                if not first:
                    print(f"    tunnel back after {int(time.monotonic()-t0)}s", flush=True)
                return
            if first:
                print("    tunnel unreachable — waiting for it to recover...", flush=True)
                first = False
            time.sleep(poll)
        raise RuntimeError(f"kube API did not recover within {max_wait}s")

    # ---- in-pod curl against XNAT -------------------------------------------
    def curl(self, method: str, path: str, *, query: str = "", data_file: str | None = None,
             ctype: str | None = None, timeout: int = 1200) -> CurlResult:
        """curl inside the pod against the local XNAT; timing is curl's in-pod time_total."""
        url = f"{self.base}{path}" + (("?" + query) if query else "")
        parts = ["curl", "-sS", "-u", f"{self.user}:{self.password}", "-X", method]
        if ctype:
            parts += ["-H", f"Content-Type: {ctype}"]
        if data_file:
            parts += ["--data-binary", f"@{data_file}"]
        parts += ["-o", "/tmp/ip_body", "-w", "%{http_code} %{time_total}", url]
        # quote every argument (the -w format and any header contain spaces); then print the body
        # after the status line so we capture both.
        script = " ".join(shlex.quote(p) for p in parts) + "; echo; cat /tmp/ip_body"
        out = self.exec(script, timeout=timeout)
        first, _, body = out.partition("\n")
        code_s, _, secs_s = first.strip().partition(" ")
        return CurlResult(http=int(code_s or 0), secs=float(secs_s or 0.0), body=body)

    # ---- metrics -------------------------------------------------------------
    _METRIC_AWK = (
        r'''awk '/mounted on \/data\/xnat\/archive /{f=1;next} f&&/^[[:space:]]*bytes:/{print $7;exit}' '''
        r"/proc/self/mountstats; awk '/^wchar/{print $2}' /proc/1/io"
    )

    def metrics(self) -> Metrics:
        out = self.exec(self._METRIC_AWK).split()
        return Metrics(nfs_write=int(out[0]), wchar=int(out[1]))

    # ---- XNAT facts ----------------------------------------------------------
    def build_sha(self) -> str:
        body = self.curl("GET", "/xapi/siteConfig/buildInfo").body
        try:
            return json.loads(body).get("shaFull", "?")
        except Exception:
            return "?"

    def storage_layout(self) -> str:
        return self.exec(
            'for p in /data/xnat/archive /data/xnat/prearchive /data/xnat/cache; do '
            'printf "%s " "$p"; df -T "$p" 2>/dev/null | awk "NR==2{print \\$2}"; done')

    # ---- anonymization control ----------------------------------------------
    def set_site_anon(self, enable: bool, script_pod_path: str | None = None) -> None:
        if script_pod_path is not None:
            self.curl("PUT", "/xapi/anonymize/site", data_file=script_pod_path, ctype="text/plain")
        self.curl("PUT", "/xapi/anonymize/site/enabled", query=f"enable={'true' if enable else 'false'}",
                  ctype="application/json")

    def site_anon_enabled(self) -> bool:
        return "true" in self.curl("GET", "/xapi/anonymize/site/enabled").body.lower()

    def set_project_anon(self, project: str, enable: bool, script_pod_path: str | None = None) -> None:
        if script_pod_path is not None:
            self.curl("PUT", f"/xapi/anonymize/projects/{project}", data_file=script_pod_path, ctype="text/plain")
        self.curl("PUT", f"/xapi/anonymize/projects/{project}/enabled",
                  query=f"enable={'true' if enable else 'false'}", ctype="application/json")

    # ---- projects / receivers -----------------------------------------------
    def ensure_project(self, project: str) -> None:
        if self.curl("GET", f"/data/projects/{project}", query="format=json").http != 200:
            self.curl("PUT", f"/data/projects/{project}",
                      query=f"name={project}&secondary_ID={project}&description=ingest-perf")

    def ensure_receiver(self, ae: str, *, anon: bool, direct: bool = False, port: int = 8104) -> None:
        existing = self.curl("GET", "/xapi/dicomscp").body
        try:
            rows = json.loads(existing)
        except Exception:
            rows = []
        template = next((r for r in rows if r.get("aeTitle") == "XNAT"), rows[0] if rows else {})
        rid = next((r["id"] for r in rows if r.get("aeTitle") == ae and r.get("port") == port), None)
        body = json.dumps({
            "aeTitle": ae, "port": port, "enabled": True, "anonymizationEnabled": anon,
            "directArchive": direct, "customProcessing": False,
            "identifier": template.get("identifier"), "fileNamer": template.get("fileNamer"),
        })
        self._put_json("/xapi/dicomscp" + (f"/{rid}" if rid else ""), body, method="PUT" if rid else "POST")

    def _put_json(self, path: str, body: str, method: str = "PUT") -> CurlResult:
        # write the JSON to a file in the pod, then curl --data-binary @file (avoids shell quoting)
        self.exec("cat > /tmp/ip_json <<'EOF'\n" + body + "\nEOF")
        return self.curl(method, path, data_file="/tmp/ip_json", ctype="application/json")

    # ---- prearchive lifecycle ------------------------------------------------
    def prearchive_rows(self, project: str) -> list[dict]:
        body = self.curl("GET", f"/data/prearchive/projects/{project}", query="format=json").body
        try:
            return json.loads(body)["ResultSet"]["Result"]
        except Exception:
            return []

    def archived_count(self, project: str) -> int:
        body = self.curl("GET", f"/data/projects/{project}/experiments",
                         query="format=json&columns=label").body
        try:
            return len(json.loads(body)["ResultSet"]["Result"])
        except Exception:
            return 0

    def build_session(self, project: str, ts: str, folder: str) -> CurlResult:
        """action=build: synchronous RECEIVING->READY (no idle-timeout wait)."""
        return self.curl("POST", f"/data/prearchive/projects/{project}/{ts}/{folder}", query="action=build")

    def archive_session(self, project: str, ts: str, folder: str) -> CurlResult:
        return self.curl("POST", "/data/services/archive",
                         query=f"src=/prearchive/projects/{project}/{ts}/{folder}&overwrite=append")

    def wait_prearchive_empty(self, project: str, timeout: int = 900, poll: float = 1.0) -> float:
        t0 = time.monotonic()
        while time.monotonic() - t0 < timeout:
            if not self.prearchive_rows(project):
                return time.monotonic() - t0
            time.sleep(poll)
        raise TimeoutError(f"prearchive for {project} did not drain within {timeout}s")

    def wipe_project(self, project: str) -> None:
        # delete prearchive sessions, then archived subjects; ignore individual failures
        for r in self.prearchive_rows(project):
            self.curl("DELETE", f"/data/prearchive/projects/{project}/{r['timestamp']}/{r['folderName']}")
        body = self.curl("GET", f"/data/projects/{project}/subjects", query="format=json").body
        try:
            subs = [s["ID"] for s in json.loads(body)["ResultSet"]["Result"]]
        except Exception:
            subs = []
        for sid in subs:
            self.curl("DELETE", f"/data/projects/{project}/subjects/{sid}", query="removeFiles=true")
        # Wait for the deletes to actually take effect before the next cell — subject deletion can lag,
        # and re-importing the same session label mid-delete collides and stalls in the prearchive.
        # OT leftovers (un-deletable, datatype not on site) don't register as experiments, so
        # archived_count reaching 0 is a reliable "clean" signal.
        t0 = time.monotonic()
        while time.monotonic() - t0 < 120:
            if not self.prearchive_rows(project) and self.archived_count(project) == 0:
                return
            time.sleep(2)

    # ---- sender pod (C-STORE) ------------------------------------------------
    SENDER_POD = "ingest-perf-sender"

    def ensure_sender_pod(self, wheels_dir: str, sender_py: str, image: str = "python:3.12-slim") -> None:
        """Create a long-lived sender pod with pynetdicom installed offline, if not already running."""
        r = self._kubectl("get", "pod", self.SENDER_POD, "-o", "jsonpath={.status.phase}")
        if r.returncode == 0 and r.stdout.decode().strip() == "Running":
            return
        self._kubectl("delete", "pod", self.SENDER_POD, "--ignore-not-found", "--now")
        spec = json.dumps({
            "apiVersion": "v1", "kind": "Pod",
            "metadata": {"name": self.SENDER_POD, "labels": {"app": "ingest-perf-sender"}},
            "spec": {"restartPolicy": "Never", "containers": [{
                "name": "sender", "image": image,
                "command": ["sh", "-c", "sleep infinity"],
            }]},
        })
        last = ""
        for attempt in range(3):   # --validate=false skips the openapi download (a tunnel-blip risk)
            a = self._kubectl("apply", "-f", "-", "--validate=false", input_bytes=spec.encode())
            if a.returncode == 0:
                break
            last = a.stderr.decode(errors="replace")[:400]
            time.sleep(3)
        else:
            raise RuntimeError(f"sender pod create failed after 3 attempts: {last}")
        self._kubectl("wait", f"pod/{self.SENDER_POD}", "--for=condition=Ready", "--timeout=180s")
        self.exec("mkdir -p /wheels /work", pod=self.SENDER_POD, container="sender")
        for whl in sorted(__import__("pathlib").Path(wheels_dir).glob("*.whl")):
            self.cp_to(str(whl), f"/wheels/{whl.name}", pod=self.SENDER_POD, container="sender")
        self.cp_to(sender_py, "/work/sender.py", pod=self.SENDER_POD, container="sender")
        self.exec("pip install --no-index --find-links /wheels pydicom pynetdicom >/tmp/pip.log 2>&1 || "
                  "(cat /tmp/pip.log; exit 1)", pod=self.SENDER_POD, container="sender", timeout=300)

    def delete_sender_pod(self) -> None:
        self._kubectl("delete", "pod", self.SENDER_POD, "--ignore-not-found", "--now")
