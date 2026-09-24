"""Output digests of what a cell archived, so two builds can be compared for *what* they produced,
not only how long they took.

Four digests per project archive, taken once the archiver has gone quiet and before the project is wiped:

- ``content``: SHA-256 over the sorted SHA-256s of every archived DICOM file's raw bytes (catalogs and
  logs excluded). Hashes rather than paths, because the importer gives received files a random suffix.
- ``objects``: the same, but each file hashed as its dataset re-encoded to Explicit VR Little Endian with
  DeidentificationMethodCodeSequence (0012,0064) removed (``verify/ObjectDigest.java``, run in the pod on
  the webapp's dcm4che). The anonymizer records the ID of the script it applied in that sequence, and the
  ID moves every time a script is (re)installed, so anonymized bytes differ between runs whose objects are
  otherwise identical. ``content`` is the strict check; ``objects`` says whether a byte difference is more
  than that record (or the encoding).
- ``catalogs``: SHA-256 over every catalog entry's stable attributes (SOP UID, format, content, ...),
  sorted, with the per-run attributes (URI/name with the random suffix, timestamps, event ids) dropped.
- ``session``: SHA-256 over the session's REST XML with the per-run attributes stripped: database ids,
  insert/modification stamps and users, the prearchive path, meta elements.

The archiver keeps working for a few seconds after the session becomes visible through REST (the
direct-archive session XML is removed, every catalog is rewritten with its audit trail and dimensions), so
the digests wait until two consecutive one-second snapshots of the archive agree.

Same digests on two builds means the same objects, the same catalog entries and the same session
metadata; a difference names which of them moved.
"""
from __future__ import annotations

import hashlib
import json
import re
import time
import xml.etree.ElementTree as ET
from pathlib import Path

from k8s import Cluster

# Catalog entry attributes that vary from run to run without the content having changed.
VOLATILE_ENTRY_ATTRS = {"URI", "name", "cachePath", "createdTime", "createdBy", "createdEventId",
                        "modifiedTime", "modifiedBy", "modifiedEventId"}
# Session XML: attributes stripped wherever they occur, then elements dropped wholesale.
VOLATILE_XML_ATTRS = ("ID", "xnat_abstractresource_id", "xnat_imagescandata_id", "xnat_experimentdata_id",
                      "insert_date", "insert_user", "last_modified", "row_last_modified", "activation_date",
                      "prearchivePath", "cachePath", "file_count", "file_size")
VOLATILE_XML_ELEMENTS = ("meta", "status", "subject_ID", "image_session_ID", "prearchivePath")

# Archived DICOM files: everything below a session directory that is not a catalog or a log. The session
# XML the direct archiver leaves in arc001/ for a second is at depth 1 and so excluded.
_DICOM_FILES = "find . -mindepth 2 -type f ! -name '*_catalog.xml' ! -name '*.log'"
_TOOL_SRC = Path(__file__).parent / "verify" / "ObjectDigest.java"
_TOOL_POD = "/tmp/ingest-perf/ObjectDigest.java"
_WEBAPP_LIB = "/usr/local/tomcat/webapps/ROOT/WEB-INF/lib"
_tool_staged: set[int] = set()


def _sha(text: str) -> str:
    return hashlib.sha256(text.encode()).hexdigest()[:16]


def _local(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def settle(cluster: Cluster, project: str, timeout: float = 120) -> None:
    """Wait until the archive stops changing: two consecutive snapshots (paths, sizes, mtimes, catalog
    bytes) one second apart agree and no session XML is left at the top of arc001/."""
    cmd = (f"cd /data/xnat/archive/{project}/arc001 2>/dev/null && find . -type f -printf '%p %s %T@\\n' | sort | md5sum; "
           "find . -name '*_catalog.xml' -print0 | sort -z | xargs -0 -r cat | md5sum; ls *.xml 2>/dev/null | wc -l")
    prev, t0 = None, time.monotonic()
    while time.monotonic() - t0 < timeout:
        snap = cluster.exec(cmd)
        if snap == prev and snap.split()[-1] == "0":
            return
        prev = snap
        time.sleep(1)
    print(f"  verify: archive of {project} still changing after {timeout:.0f}s; digesting anyway")


def content_digest(cluster: Cluster, project: str) -> tuple[str, int]:
    out = cluster.exec(f"cd /data/xnat/archive/{project}/arc001 2>/dev/null && "
                       f"{_DICOM_FILES} -print0 | xargs -0 -r sha256sum | awk '{{print $1}}' | sort")
    hashes = [h for h in out.split() if h]
    return _sha("\n".join(hashes)), len(hashes)


def object_digest(cluster: Cluster, project: str) -> tuple[str, int]:
    if id(cluster) not in _tool_staged:
        cluster.exec(f"mkdir -p {_TOOL_POD.rsplit('/', 1)[0]}")
        cluster.cp_to(str(_TOOL_SRC), _TOOL_POD)
        _tool_staged.add(id(cluster))
    out = cluster.exec(
        f"cd /data/xnat/archive/{project}/arc001 2>/dev/null && "
        f"CP=$(ls {_WEBAPP_LIB}/dcm4che-core-5*.jar):$(ls {_WEBAPP_LIB}/slf4j-api-*.jar) && "
        f"{_DICOM_FILES} | sort > /tmp/ingest-perf/objdigest.paths && "
        f"java -cp \"$CP\" {_TOOL_POD} < /tmp/ingest-perf/objdigest.paths > /tmp/ingest-perf/objdigest.out 2> /tmp/ingest-perf/objdigest.err "
        "|| { cat /tmp/ingest-perf/objdigest.err; exit 1; }; awk '{print $1}' /tmp/ingest-perf/objdigest.out | sort")
    hashes = [h for h in out.split() if h]
    return _sha("\n".join(hashes)), len(hashes)


def catalog_digest(cluster: Cluster, project: str) -> tuple[str, int]:
    # One document per catalog, separated so a concatenation cannot be mistaken for one catalog.
    out = cluster.exec(f"cd /data/xnat/archive/{project}/arc001 2>/dev/null && "
                       "find . -name '*_catalog.xml' -print0 | sort -z | xargs -0 -r -I{} sh -c 'cat \"{}\"; printf \"\\n\\034\\n\"'")
    entries: list[str] = []
    catalogs = 0
    for doc in out.split("\034"):
        doc = doc.strip()
        if not doc:
            continue
        catalogs += 1
        root = ET.fromstring(doc)
        for el in root.iter():
            if _local(el.tag) == "entry":
                stable = sorted(f"{k}={v}" for k, v in el.attrib.items() if k not in VOLATILE_ENTRY_ATTRS)
                entries.append(" ".join(stable))
    return _sha("\n".join(sorted(entries))), catalogs


def session_digest(cluster: Cluster, project: str) -> tuple[str, int, int]:
    body = cluster.curl("GET", f"/data/projects/{project}/experiments", query="format=json&columns=ID").body
    try:
        ids = sorted(r["ID"] for r in json.loads(body)["ResultSet"]["Result"])
    except Exception:
        ids = []
    docs: list[str] = []
    scans = 0
    for sid in ids:
        xml = cluster.curl("GET", f"/data/experiments/{sid}", query="format=xml").body
        xml = re.sub(r"<!--.*?-->", "", xml, flags=re.S)   # hidden_fields comments carry database ids
        for attr in VOLATILE_XML_ATTRS:
            xml = re.sub(rf'\s{attr}="[^"]*"', "", xml)
        for element in VOLATILE_XML_ELEMENTS:
            xml = re.sub(rf"<(\w+:)?{element}\b[^>]*/>", "", xml)
            xml = re.sub(rf"<(\w+:)?{element}\b[^>]*>.*?</(\w+:)?{element}>", "", xml, flags=re.S)
        scans += len(re.findall(r"<(?:\w+:)?scan\b", xml))
        docs.append(re.sub(r"\s+", " ", xml).strip())
    return _sha("\n".join(docs)), len(ids), scans


def archived_digests(cluster: Cluster, project: str, *, settled: bool = False) -> dict:
    """Digest what the project archived. Pass ``settled`` when the caller has already waited for the
    archiver to go quiet (see settle), as the run loop does inside its measurement."""
    if not settled:
        settle(cluster, project)
    content, files = content_digest(cluster, project)
    objects, nobj = object_digest(cluster, project)
    if nobj != files:
        raise RuntimeError(f"object digest covered {nobj} of {files} archived files")
    catalogs, ncat = catalog_digest(cluster, project)
    session, nsess, nscans = session_digest(cluster, project)
    return {"content": content, "objects": objects, "files": files, "catalogs": catalogs, "catalog_count": ncat,
            "session": session, "sessions": nsess, "scans": nscans}


def _verdict(base: list[dict], cand: list[dict], field: str) -> str:
    if not base or not cand or any(field not in d for d in base + cand):
        return "n/a"
    bset, cset = {d[field] for d in base}, {d[field] for d in cand}
    if cset <= bset and len(bset) == 1:
        return "same"
    if cset <= bset:
        return "same (reps vary)"
    return "**DIFFERENT**"


def compare_digests(base_cells: list[dict], cand_cells: list[dict]) -> list[str]:
    """Markdown lines: per (workload, route, anon), whether every candidate rep's digests appear among
    the baseline reps' (and whether the reps agree among themselves). A content difference that the
    object digest does not see is reported as such rather than as DIFFERENT."""
    def by_key(cells: list[dict]) -> dict[tuple, list[dict]]:
        out: dict[tuple, list[dict]] = {}
        for c in cells:
            if c.get("ok") and c.get("digests"):
                out.setdefault((c["workload"], c["route"], c["anon"]), []).append(c["digests"])
        return out

    b, c = by_key(base_cells), by_key(cand_cells)
    lines = ["| workload | route | anon | files | scans | content | catalogs | session |", "|---|---|---|---|---|---|---|---|"]
    for key in sorted(set(b) | set(c)):
        bd, cd = b.get(key, []), c.get(key, [])
        cells_ = []
        for field in ("content", "catalogs", "session"):
            verdict = _verdict(bd, cd, field)
            if field == "content" and verdict == "**DIFFERENT**":
                objects = _verdict(bd, cd, "objects")
                if objects.startswith("same"):
                    verdict = objects.replace("same", "same objects", 1) + " (bytes differ: deid record/encoding)"
            cells_.append(verdict)
        files = sorted({d["files"] for d in bd + cd})
        scans = sorted({d["scans"] for d in bd + cd})
        lines.append(f"| {key[0]} | {key[1]} | {key[2]} | {'/'.join(map(str, files))} | {'/'.join(map(str, scans))} | "
                     + " | ".join(cells_) + " |")
    return lines
