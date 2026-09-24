"""In-cluster driver plumbing for the ingest performance suite.

All timed data-path work runs *inside* the XNAT pod (or a sender pod), so the kube API connection never
sits in the measured path. The orchestrator (this process) only issues ``kubectl exec``/``cp`` control
calls and parses JSON returned from in-pod ``curl``.

Metrics come from the pod, not the block layer: NFS writes are counted from
``/proc/self/mountstats`` ``bytes:`` field 6 (server write bytes) on the archive mount — the archive,
prearchive and cache mounts share one NFS export and report identical aggregate stats, so read one —
and app-level write bytes from ``/proc/1/io`` ``wchar``.
"""
from __future__ import annotations

import json
import os
import select
import shlex
import subprocess
import time
import uuid
from dataclasses import dataclass, field


@dataclass
class CurlResult:
    http: int
    secs: float          # curl's own time_total, measured in-pod (no API hop)
    body: str


@dataclass
class Metrics:
    nfs_write: int       # NFS server-write bytes (mountstats field 6 on the archive mount)
    wchar: int           # /proc/1/io wchar bytes (all write() bytes by the XNAT jvm)
    # per NFS operation on the archive mount: (operations, cumulative round-trip ms, cumulative execute ms),
    # from the mount's per-op statistics; only operations that have run at least once are listed
    ops: dict[str, tuple[int, int, int]] = field(default_factory=dict)


class _Shell:
    """One long-lived ``kubectl exec -i … sh`` into a container. A command is written to its stdin
    followed by a sentinel echo and stdout is read back up to the sentinel, so a control call costs a
    pipe round trip (tens of ms) instead of a fresh exec through the API connection (~0.9 s each, and a
    cell makes a dozen of them). The command's stderr is folded into its output, as the one-shot exec
    reported it on failure."""

    def __init__(self, kubectl: list[str]):
        self.p = subprocess.Popen([*kubectl, "sh"], stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                  stderr=subprocess.STDOUT, bufsize=0)

    def run(self, script: str, timeout: int) -> tuple[int, str]:
        tag = uuid.uuid4().hex
        try:
            self.p.stdin.write(f"{{\n{script}\n}} 2>&1\necho \"{tag} $?\"\n".encode())
            self.p.stdin.flush()
        except OSError as e:
            raise RuntimeError(f"exec channel closed: {e}") from e
        buf = bytearray()
        fd = self.p.stdout.fileno()
        deadline = time.monotonic() + timeout
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise TimeoutError(f"command timed out after {timeout}s in the exec channel")
            if not select.select([fd], [], [], min(remaining, 5.0))[0]:
                if self.p.poll() is not None:
                    raise RuntimeError(f"exec channel closed: {buf.decode(errors='replace')[-300:]}")
                continue
            chunk = os.read(fd, 65536)
            if not chunk:
                raise RuntimeError(f"exec channel closed: {buf.decode(errors='replace')[-300:]}")
            buf += chunk
            text = buf.decode(errors="replace")
            idx = text.rfind(tag)
            if idx != -1 and text.endswith("\n"):
                rc = text[idx + len(tag):].strip()
                if rc.isdigit():
                    return int(rc), text[:idx]

    def close(self) -> None:
        try:
            self.p.kill()
        except OSError:
            pass


class Cluster:
    """Talks to one XNAT pod. ``base`` is the in-pod XNAT URL (Tomcat, no API hop)."""

    def __init__(self, context: str, namespace: str, pod: str, user: str, password: str,
                 container: str = "xnat", base: str = "http://localhost:8080"):
        self.ctx, self.ns, self.pod, self.container = context, namespace, pod, container
        self.user, self.password, self.base = user, password, base
        self._warned_no_nfs = False
        self._prearchive_root: str | None = None
        self._shells: dict[tuple[str, str], _Shell] = {}
        self._jsession: str | None = None

    # ---- low-level exec / cp -------------------------------------------------
    def _kubectl(self, *args: str, input_bytes: bytes | None = None, timeout: int = 600) -> subprocess.CompletedProcess:
        cmd = ["kubectl", "--context", self.ctx, "-n", self.ns, *args]
        return subprocess.run(cmd, input=input_bytes, capture_output=True, timeout=timeout)

    def exec(self, script: str, *, pod: str | None = None, container: str | None = None,
             timeout: int = 600) -> str:
        """Run ``script`` under ``sh`` in a pod through its persistent exec channel; return stdout
        (raises on non-zero). A channel that dies (connection drop, pod restart) is discarded and the
        error surfaces as a transient one, so the caller's cell retry re-runs the work rather than
        this method re-sending a possibly non-idempotent command."""
        key = (pod or self.pod, container or self.container)
        shell = self._shells.get(key)
        if shell is None:
            shell = self._shells[key] = _Shell(["kubectl", "--context", self.ctx, "-n", self.ns,
                                                 "exec", "-i", key[0], "-c", key[1], "--"])
        try:
            rc, out = shell.run(script, timeout)
        except (RuntimeError, TimeoutError):
            shell.close()
            self._shells.pop(key, None)
            raise
        if rc != 0:
            raise RuntimeError(f"exec failed ({rc}): {out[-400:]}")
        return out

    def cp_to(self, local: str, dest: str, *, pod: str | None = None, container: str | None = None) -> None:
        tgt = f"{pod or self.pod}:{dest}"
        # cp is idempotent, so retry transient API timeouts (the API connection can be flaky).
        last = ""
        for attempt in range(3):
            r = self._kubectl("cp", local, tgt, "-c", container or self.container, timeout=1800)
            if r.returncode == 0:
                return
            last = r.stderr.decode(errors="replace")[:400]
            time.sleep(3)
        raise RuntimeError(f"cp failed after 3 attempts: {last}")

    def wait_healthy(self, max_wait: int = 28800, poll: int = 20) -> None:
        """Block until the kube API answers. Patient by design: an overnight
        run pauses here through a connection outage and continues when it comes back (up to 8 h)."""
        t0 = time.monotonic()
        first = True
        while time.monotonic() - t0 < max_wait:
            r = self._kubectl("get", "pod", self.pod, "--no-headers", "--request-timeout=8s", timeout=15)
            if r.returncode == 0:
                if not first:
                    print(f"    API back after {int(time.monotonic()-t0)}s", flush=True)
                return
            if first:
                print("    API unreachable — waiting for it to recover...", flush=True)
                first = False
            time.sleep(poll)
        raise RuntimeError(f"kube API did not recover within {max_wait}s")

    # ---- in-pod curl against XNAT -------------------------------------------
    def curl(self, method: str, path: str, *, query: str = "", data_file: str | None = None,
             ctype: str | None = None, timeout: int = 1200) -> CurlResult:
        """curl inside the pod against the local XNAT; timing is curl's in-pod time_total. Requests
        ride one XNAT session cookie rather than basic auth: BCrypt on every request was the top
        CPU consumer in the pod profile, and at one poll per second that was the harness's doing."""
        r = self._curl(method, path, query, data_file, ctype, timeout, self._cookie())
        if r.http in (401, 403, 302):              # session gone: log in again, retry once
            self._jsession = None
            r = self._curl(method, path, query, data_file, ctype, timeout, self._cookie())
        return r

    def _cookie(self) -> list[str]:
        if self._jsession is None:
            r = self._curl("POST", "/data/JSESSION", "", None, None, 60, ["-u", f"{self.user}:{self.password}"])
            if r.http != 200 or not r.body.strip():
                raise RuntimeError(f"could not open an XNAT session: HTTP {r.http} {r.body[:120]}")
            self._jsession = r.body.strip()
        return ["-b", f"JSESSIONID={self._jsession}"]

    def _curl(self, method: str, path: str, query: str, data_file: str | None, ctype: str | None,
              timeout: int, auth: list[str]) -> CurlResult:
        url = f"{self.base}{path}" + (("?" + query) if query else "")
        parts = ["curl", "-sS", *auth, "-X", method]
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
        try:
            return CurlResult(http=int(code_s or 0), secs=float(secs_s or 0.0), body=body)
        except ValueError:
            # curl itself failed (unreadable data file, connection refused): no status line, just its
            # message. Report it as HTTP 0 so the route's error names the cause instead of a parse error.
            return CurlResult(http=0, secs=0.0, body=out.strip())

    # ---- metrics -------------------------------------------------------------
    # The NFS mount that contains the archive, whichever path it is mounted at: one export mounted
    # at /data/xnat, or a separate mount per data directory. Mount points are compared as path
    # prefixes so /data/xnat matches /data/xnat/archive but /data/xnat-other does not.
    # The per-op lines of the archive mount's section ("WRITE: ops trans timeouts sent recv queue rtt execute
    # [errors]") give each NFS operation's count and cumulative round-trip time: the storage's latency as
    # this instance saw it, which is what shared storage changes when a neighbour loads it.
    _METRIC_AWK = (
        r'''awk '/^device /{f=0;g=0} '''
        r'''/^device .* mounted on .* with fstype nfs/{f=(index("/data/xnat/archive/", $5 "/")==1);next} '''
        r'''f&&/^[[:space:]]*bytes:/{print "nfs=" $7} '''
        r'''f&&/per-op statistics/{g=1;next} '''
        r'''f&&g&&$1~/^[A-Z_0-9]+:$/&&$2>0{n=$1;sub(/:$/,"",n);print "op_" n "=" $2 "," $8 "," $9}' '''
        r"""/proc/self/mountstats; awk '/^wchar/{print "wchar=" $2}' /proc/1/io"""
    )

    def metrics(self) -> Metrics:
        # Each figure is labelled, so a missing one cannot shift the other into its place: the
        # mountstats awk prints nothing when the archive is not an NFS mount (RBD, local-path, or
        # another mount point), and /proc/1/io can be unreadable. A missing NFS figure reads 0 and
        # is reported once; wall-clock still measures.
        fields = dict(t.split("=", 1) for t in self.exec(self._METRIC_AWK).split() if "=" in t)
        if "nfs" not in fields and not self._warned_no_nfs:
            print("  ! archive mount reports no NFS stats; nfs_mb will read 0 for this run")
            self._warned_no_nfs = True
        ops = {}
        for key, value in fields.items():
            if key.startswith("op_"):
                count, rtt, execute = (int(x) for x in value.split(","))
                ops[key[3:]] = (count, rtt, execute)
        return Metrics(nfs_write=int(fields.get("nfs", 0)), wchar=int(fields.get("wchar", 0)), ops=ops)

    def instance_facts(self) -> dict:
        """What this run measured on: the pod's node and its instance type, the image and its digest, the
        JVM, XNAT's build, and the pod clock's offset. Several instances measured side by side must be
        told apart, and a leg's reps come from different ones."""
        facts: dict = {"context": self.ctx, "namespace": self.ns, "pod": self.pod}
        r = self._kubectl("get", "pod", self.pod, "-o", "json", timeout=60)
        if r.returncode == 0:
            pod = json.loads(r.stdout)
            facts["node"] = pod.get("spec", {}).get("nodeName")
            for status in pod.get("status", {}).get("containerStatuses", []):
                if status.get("name") == self.container:
                    facts["image"], facts["image_id"] = status.get("image"), status.get("imageID")
                    facts["restarts"] = status.get("restartCount")
            if facts.get("node"):
                n = subprocess.run(["kubectl", "--context", self.ctx, "get", "node", facts["node"], "-o",
                                    r"jsonpath={.metadata.labels.node\.kubernetes\.io/instance-type}"],
                                   capture_output=True, timeout=60)
                facts["instance_type"] = n.stdout.decode().strip() or None
        try:
            facts["java"] = self.exec("java -version 2>&1 | head -1").strip()
        except Exception as e:
            facts["java"] = f"? ({str(e)[:80]})"
        try:
            info = json.loads(self.curl("GET", "/xapi/siteConfig/buildInfo").body)
            facts["xnat_version"], facts["build_sha"] = info.get("version"), info.get("shaFull")
        except Exception:
            pass
        facts["pod_clock_offset_s"] = self.pod_clock_offset()
        try:
            facts["data_roots"] = self.data_roots()
        except Exception as e:
            facts["data_roots"] = f"? ({str(e)[:80]})"
        return facts

    STAGING_DIR = ".xnat-tmp"

    def data_roots(self) -> dict[str, dict]:
        """The archive, prearchive and cache paths from XNAT's site configuration, each with the mount point
        and filesystem type it sits on. A build that stages anonymized files beside the data finds its data
        roots here, not in the mount layout, so a mismatch between the two quietly changes what is measured."""
        roots = {}
        for name in ("archivePath", "prearchivePath", "cachePath"):
            path = self.curl("GET", f"/xapi/siteConfig/{name}").body.strip().strip('"').rstrip("/")
            mount, fstype = "", ""
            if path:
                out = self.exec(f"df -PT {shlex.quote(path)} 2>/dev/null | awk 'NR==2{{print $7, $2}}'").split()
                if len(out) == 2:
                    mount, fstype = out
            roots[name] = {"path": path, "mount": mount, "fstype": fstype}
        return roots

    def clear_staging(self, paths: list[str]) -> None:
        """Remove the staging directory a build leaves at each data root, so the next run shows whether it
        makes one again."""
        self.exec("; ".join(f"rm -rf {shlex.quote(p.rstrip('/') + '/' + self.STAGING_DIR)}" for p in paths) + "; true")

    def staging_present(self, paths: list[str]) -> dict[str, bool]:
        out = self.exec("; ".join(f"test -d {shlex.quote(p.rstrip('/') + '/' + self.STAGING_DIR)} && echo {i}=1 || echo {i}=0"
                                  for i, p in enumerate(paths)))
        flags = dict(t.split("=", 1) for t in out.split() if "=" in t)
        return {p: flags.get(str(i)) == "1" for i, p in enumerate(paths)}

    # ---- XNAT facts ----------------------------------------------------------
    def build_sha(self) -> str:
        body = self.curl("GET", "/xapi/siteConfig/buildInfo").body
        try:
            return json.loads(body).get("shaFull", "?")
        except Exception:
            return "?"

    def pod_clock_offset(self) -> float:
        """Pod clock minus this machine's, in seconds. The pod's log lines are stamped by the node and
        the cells by this machine; matching one to the other needs the skew (many seconds on some clusters)."""
        t0 = time.time()
        pod = float(self.exec("date -u +%s.%N").strip())
        t1 = time.time()
        return round(pod - (t0 + t1) / 2, 3)

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

    def direct_archive_rows(self, project: str) -> list[dict]:
        self.exec(f"mkdir -p {self._STAGE} && printf '{{\"page\":1,\"size\":200}}' > {self._STAGE}/da-req.json")
        body = self.curl("POST", "/xapi/direct-archive", data_file=f"{self._STAGE}/da-req.json", ctype="application/json").body
        try:
            return [r for r in json.loads(body) if r.get("project") == project]
        except Exception:
            return []

    _STAGE = "/tmp/ingest-perf"

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

    def prearchive_root(self) -> str:
        """The instance's prearchivePath from siteConfig (cached): the layout differs between
        deployments, and a wrong root here would make every async wait time out."""
        if self._prearchive_root is None:
            v = self.curl("GET", "/xapi/siteConfig/prearchivePath").body.strip().strip('"')
            self._prearchive_root = v or "/data/xnat/prearchive"
        return self._prearchive_root

    def prearchive_file_count(self, project: str, ts: str, folder: str) -> int:
        """Objects landed in a prearchive session so far, counted under SCANS (any file name, since
        the importer names output from the source). Catalogs only appear at build, after any caller
        here has stopped polling. The progress signal for the async routes.

        On disk a session is <prearchivePath>/<project>/<timestamp>/<session>: the REST URL's
        ``projects/`` segment is not part of the path."""
        scans = shlex.quote(f"{self.prearchive_root()}/{project}/{ts}/{folder}/SCANS")
        out = self.exec(f"find {scans} -type f 2>/dev/null | wc -l")
        try:
            return int(out.strip())
        except ValueError:
            return 0

    def wait_prearchive_empty(self, project: str, timeout: int = 900, poll: float = 0.5) -> float:
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
        # and any direct-archive session rows: a direct archive interrupted mid-flight (pod restart) leaves its
        # row behind, and every later direct import of the same study then waits on it until the 600 s timeout
        for r in self.direct_archive_rows(project):
            self.curl("DELETE", f"/xapi/direct-archive/{r['id']}")
        self.exec(f"rm -f /data/xnat/archive/{project}/arc001/*.xml 2>/dev/null; true")   # its top-level session XML, too
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
                # The >2 GB `huge` object is built and reloaded in Python (~4 GB peak); request enough
                # RAM that the scheduler reserves it on a node with headroom (else node pressure OOMs it).
                "resources": {"requests": {"memory": "6Gi"}, "limits": {"memory": "8Gi"}},
            }]},
        })
        last = ""
        for attempt in range(3):   # --validate=false skips the openapi download (a connection-blip risk)
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
