"""In-cluster C-STORE sender for the ingest performance suite.

Runs inside the sender pod (see k8s.Cluster.ensure_sender_pod). Sends a directory of pre-encoded
DICOM to an XNAT DICOM SCP over one association, proposing exactly one presentation context per
(SOP class, transfer syntax) present in the corpus so the wire encoding is the file's own — nothing
is transcoded. Prints a JSON summary (files/sent/bytes/seconds) that the orchestrator captures as
the send-phase wall-clock.

  python sender.py --host xnat-dicom-scp --port 8104 --aet XNAT --dir /work/corpus [--json out.json]
"""
from __future__ import annotations

import argparse
import json
import sys
import time
from pathlib import Path

from pydicom import dcmread
from pynetdicom import AE
from pynetdicom.sop_class import Verification


def files_under(root: Path) -> list[Path]:
    return sorted(p for p in root.rglob("*") if p.is_file() and not p.name.startswith(".")
                  and p.suffix.lower() not in {".tsv", ".txt", ".png", ".json"})


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", required=True)
    ap.add_argument("--port", type=int, default=8104)
    ap.add_argument("--aet", default="XNAT", help="called AE title (the XNAT receiver)")
    ap.add_argument("--aec", default="PERFSENDER", help="calling AE title")
    ap.add_argument("--dir", required=True)
    ap.add_argument("--json", help="write the summary here as well as to stdout")
    a = ap.parse_args()

    paths = files_under(Path(a.dir))
    if not paths:
        sys.exit(f"nothing to send under {a.dir}")

    # One presentation context per (SOP class, transfer syntax) actually present.
    contexts: dict[str, set[str]] = {}
    for p in paths:
        try:
            ds = dcmread(p, stop_before_pixels=True, force=True)
            contexts.setdefault(str(ds.SOPClassUID), set()).add(str(ds.file_meta.TransferSyntaxUID))
        except Exception:
            continue

    ae = AE(ae_title=a.aec)
    ae.acse_timeout, ae.dimse_timeout, ae.network_timeout, ae.maximum_pdu_size = 60, 600, 600, 0
    ae.add_requested_context(Verification)
    for sop, tss in contexts.items():
        for ts in sorted(tss):
            ae.add_requested_context(sop, [ts])

    result = {"dir": a.dir, "called_aet": a.aet, "files": len(paths), "sent": 0, "bytes": 0,
              "failed": [], "seconds": 0.0}
    t0 = time.monotonic()
    assoc = ae.associate(a.host, a.port, ae_title=a.aet)
    if not assoc.is_established:
        result["failed"] = [str(p) for p in paths]
        result["error"] = "association not established"
        print(json.dumps(result))
        sys.exit(1)
    try:
        for p in paths:
            try:
                status = assoc.send_c_store(p)
                if status and status.Status == 0x0000:
                    result["sent"] += 1
                    result["bytes"] += p.stat().st_size
                else:
                    result["failed"].append(f"{p}: status {getattr(status, 'Status', 'none')}")
            except Exception as e:
                result["failed"].append(f"{p}: {type(e).__name__}: {e}")
    finally:
        assoc.release()
    result["seconds"] = round(time.monotonic() - t0, 3)
    result["failed"] = result["failed"][:50]
    out = json.dumps(result)
    print(out)
    if a.json:
        Path(a.json).write_text(out)
    if result["failed"]:
        sys.exit(1)


if __name__ == "__main__":
    main()
