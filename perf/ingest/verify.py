"""Output digests of what a cell archived, so two builds can be compared for *what* they produced,
not only how long they took.

Three digests per project archive, taken after the route finishes and before the project is wiped:

- ``content``: SHA-256 over the sorted SHA-256s of every archived DICOM file (catalogs and logs
  excluded). Hashes rather than paths, because the importer gives received files a random suffix.
- ``catalogs``: SHA-256 over every catalog entry's stable attributes (SOP UID, format, content, ...),
  sorted, with the per-run attributes (URI/name with the random suffix, timestamps, event ids) dropped.
- ``session``: SHA-256 over the session's REST XML with the per-run attributes stripped: database ids,
  insert/modification stamps and users, the prearchive path, meta elements.

Same digests on two builds means the same objects, the same catalog entries and the same session
metadata; a difference names which of the three moved.
"""
from __future__ import annotations

import hashlib
import json
import re
import xml.etree.ElementTree as ET

from k8s import Cluster

# Catalog entry attributes that vary from run to run without the content having changed.
VOLATILE_ENTRY_ATTRS = {"URI", "name", "cachePath", "createdTime", "createdBy", "createdEventId",
                        "modifiedTime", "modifiedBy", "modifiedEventId"}
# Session XML: attributes stripped wherever they occur, then elements dropped wholesale.
VOLATILE_XML_ATTRS = ("ID", "xnat_abstractresource_id", "xnat_imagescandata_id", "xnat_experimentdata_id",
                      "insert_date", "insert_user", "last_modified", "row_last_modified", "activation_date",
                      "prearchivePath", "cachePath", "file_count", "file_size")
VOLATILE_XML_ELEMENTS = ("meta", "status", "subject_ID", "image_session_ID", "prearchivePath")


def _sha(text: str) -> str:
    return hashlib.sha256(text.encode()).hexdigest()[:16]


def _local(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def content_digest(cluster: Cluster, project: str) -> tuple[str, int]:
    out = cluster.exec(f"cd /data/xnat/archive/{project}/arc001 2>/dev/null && "
                       "find . -type f ! -name '*_catalog.xml' ! -name '*.log' -print0 | xargs -0 -r sha256sum | awk '{print $1}' | sort")
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


def archived_digests(cluster: Cluster, project: str) -> dict:
    content, files = content_digest(cluster, project)
    catalogs, ncat = catalog_digest(cluster, project)
    session, nsess, nscans = session_digest(cluster, project)
    return {"content": content, "files": files, "catalogs": catalogs, "catalog_count": ncat,
            "session": session, "sessions": nsess, "scans": nscans}


def compare_digests(base_cells: list[dict], cand_cells: list[dict]) -> list[str]:
    """Markdown lines: per (workload, route, anon), whether every candidate rep's digests appear among
    the baseline reps' (and whether the reps agree among themselves)."""
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
            bset, cset = {d[field] for d in bd}, {d[field] for d in cd}
            if not bd or not cd:
                cells_.append("n/a")
            elif cset <= bset and len(bset) == 1:
                cells_.append("same")
            elif cset <= bset:
                cells_.append("same (reps vary)")
            else:
                cells_.append("**DIFFERENT**")
        files = sorted({d["files"] for d in bd + cd})
        scans = sorted({d["scans"] for d in bd + cd})
        lines.append(f"| {key[0]} | {key[1]} | {key[2]} | {'/'.join(map(str, files))} | {'/'.join(map(str, scans))} | "
                     + " | ".join(cells_) + " |")
    return lines
