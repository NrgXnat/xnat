"""Synthetic DICOM corpus generation for the ingest performance suite.

Everything here is synthetic — no external data required for the default workloads. Each workload is
one XNAT session (a single Study Instance UID); every object gets fresh Series/SOP UIDs and a
recognisable synthetic patient. Pixels are uncompressed Explicit VR Little Endian so the pixel-anon
(rectangle redaction) modes work without server-side codecs. ``StudyDescription`` is set to the
target project so a C-STORE to the default receiver routes to it.

Workloads:
  small       many small single-frame objects   (default 200 x ~256 KB) — the "lots of small files" shape
  multiframe  few large multiframe objects       (default 4 x ~50 MB)    — the "large multiframe" shape
  mixed       a bit of both, plus Explicit BE and Deflated objects (native pydicom transfer syntaxes)

``--corpus DIR`` (see ``route_real``) instead re-tags a directory of real DICOM to one project with
fresh UIDs, for running the suite against representative site data.
"""
from __future__ import annotations

import csv
import hashlib
import shutil
import struct
from pathlib import Path

import pydicom
from pydicom import dcmread
from pydicom.dataset import Dataset, FileMetaDataset
from pydicom.sequence import Sequence
from pydicom.uid import (
    UID,
    DeflatedExplicitVRLittleEndian,
    ExplicitVRBigEndian,
    ExplicitVRLittleEndian,
    generate_uid,
)

ROOT_UID = "1.2.826.0.1.3680043.10.9999"
MR_STORAGE = UID("1.2.840.10008.5.1.4.1.1.4")          # MR Image Storage (single frame)
ENH_MR = UID("1.2.840.10008.5.1.4.1.1.4.1")            # Enhanced MR Image Storage (multiframe MR session)


def _uid() -> str:
    return generate_uid(prefix=ROOT_UID + ".")


def _identity(ds: Dataset, *, project: str, study_uid: str, series_uid: str,
              series_number: int, instance_number: int, label: str) -> None:
    """Give one object a fresh identity and route it to ``project`` (leaves pixels/encoding alone)."""
    ds.StudyInstanceUID = study_uid
    ds.SeriesInstanceUID = series_uid
    ds.SOPInstanceUID = _uid()
    ds.file_meta.MediaStorageSOPInstanceUID = ds.SOPInstanceUID
    ds.SeriesNumber = str(series_number)
    ds.InstanceNumber = str(instance_number)
    ds.PatientName = f"PERF^{label}"
    ds.PatientID = f"PERF-{label}"
    ds.PatientBirthDate = "19700101"
    ds.PatientSex = "O"
    ds.StudyDate = "20260101"
    ds.StudyTime = "120000"
    ds.AccessionNumber = f"ACC-{label}"[:16]
    # The default XNAT identifier routes on StudyDescription; a C-STORE lands in this project.
    ds.StudyDescription = project
    ds.SeriesDescription = f"{label} s{series_number}"[:64]
    ds.InstitutionName = "XNAT ingest-perf (synthetic)"
    ds.ReferringPhysicianName = "Perf^Referrer"


def _base_image(rows: int, cols: int, ts: UID = ExplicitVRLittleEndian) -> Dataset:
    ds = Dataset()
    ds.file_meta = FileMetaDataset()
    ds.file_meta.TransferSyntaxUID = ts
    ds.file_meta.MediaStorageSOPClassUID = MR_STORAGE
    ds.SOPClassUID = MR_STORAGE
    ds.Modality = "MR"
    ds.Rows, ds.Columns = rows, cols
    ds.SamplesPerPixel = 1
    ds.PhotometricInterpretation = "MONOCHROME2"
    ds.BitsAllocated = ds.BitsStored = 16
    ds.HighBit = 15
    ds.PixelRepresentation = 0
    ds.ImageType = ["ORIGINAL", "PRIMARY", "M", "NONE"]
    return ds


def _pixels(rows, cols, frames, rng, dtype: str = "<u2") -> bytes:
    """Smooth gradient plus mild noise: image-like (compressible) but not constant, in a single call.

    ``dtype`` must match the transfer syntax's byte order -- pydicom writes PixelData bytes through
    untouched, so an Explicit VR Big Endian object needs ">u2" or every sample is byte-swapped.
    """
    import numpy as np
    yy, xx = np.mgrid[0:rows, 0:cols]
    out = np.empty((frames, rows, cols), dtype=dtype)
    for f in range(frames):
        base = ((f * 13) % 4096 + yy + xx).astype(np.int64)
        out[f] = ((base + rng.integers(0, 64, size=(rows, cols))) & 0x0FFF).astype(dtype)
    return out.tobytes()


def _write(ds: Dataset, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    ds.save_as(path, enforce_file_format=True)


def _enhanced_mr(rows: int, cols: int, frames: int, pixel_bytes: bytes) -> Dataset:
    """A real multiframe MR object (Enhanced MR Image Storage, Modality MR) that XNAT archives as an
    MR session — unlike SC Multiframe (OT), which lands as secondary resources with no experiment.
    Minimal but valid: shared pixel-measures/orientation + per-frame plane position and frame content.
    numpy-free (so ``huge`` can be built in the sender pod)."""
    ds = Dataset()
    ds.file_meta = FileMetaDataset()
    ds.file_meta.TransferSyntaxUID = ExplicitVRLittleEndian
    ds.file_meta.MediaStorageSOPClassUID = ENH_MR
    ds.SOPClassUID = ENH_MR
    ds.Modality = "MR"
    ds.Rows, ds.Columns = rows, cols
    ds.SamplesPerPixel = 1
    ds.PhotometricInterpretation = "MONOCHROME2"
    ds.BitsAllocated = ds.BitsStored = 16
    ds.HighBit = 15
    ds.PixelRepresentation = 0
    ds.NumberOfFrames = str(frames)
    ds.ImageType = ["ORIGINAL", "PRIMARY", "M", "NONE"]
    ds.ContentDate, ds.ContentTime = "20260101", "120000"
    dorg = Dataset()
    dorg.DimensionOrganizationUID = _uid()
    ds.DimensionOrganizationSequence = Sequence([dorg])
    shared = Dataset()
    pm = Dataset(); pm.PixelSpacing = [1.0, 1.0]; pm.SliceThickness = 1.0
    shared.PixelMeasuresSequence = Sequence([pm])
    po = Dataset(); po.ImageOrientationPatient = [1.0, 0.0, 0.0, 0.0, 1.0, 0.0]
    shared.PlaneOrientationSequence = Sequence([po])
    ds.SharedFunctionalGroupsSequence = Sequence([shared])
    per_frame = Sequence()
    for i in range(frames):
        item = Dataset()
        pp = Dataset(); pp.ImagePositionPatient = [0.0, 0.0, float(i)]
        item.PlanePositionSequence = Sequence([pp])
        fc = Dataset(); fc.StackID = "1"; fc.InStackPositionNumber = i + 1; fc.DimensionIndexValues = [i + 1]
        item.FrameContentSequence = Sequence([fc])
        per_frame.append(item)
    ds.PerFrameFunctionalGroupsSequence = per_frame
    ds.PixelData = pixel_bytes
    return ds


# ---------------------------------------------------------------- workloads

def gen_small(out: Path, project: str, count: int = 200, rows: int = 362, cols: int = 362) -> None:
    """Many small single-frame objects (~rows*cols*2 bytes each; 362x362x16bit ~= 256 KB)."""
    import numpy as np
    rng = np.random.default_rng(1)
    study = _uid()
    for i in range(1, count + 1):
        ds = _base_image(rows, cols)
        ds.NumberOfFrames = "1"
        ds.PixelData = _pixels(rows, cols, 1, rng)
        _identity(ds, project=project, study_uid=study, series_uid=_uid(),
                  series_number=i, instance_number=1, label="small")
        _write(ds, out / f"{i:04d}.dcm")


def gen_multiframe(out: Path, project: str, count: int = 4, frames: int = 100,
                   rows: int = 512, cols: int = 512) -> None:
    """Few large multiframe Enhanced-MR objects (frames*rows*cols*2 bytes; 100x512x512x16bit ~= 50 MB each)."""
    import numpy as np
    rng = np.random.default_rng(2)
    study = _uid()
    for i in range(1, count + 1):
        ds = _enhanced_mr(rows, cols, frames, _pixels(rows, cols, frames, rng))
        _identity(ds, project=project, study_uid=study, series_uid=_uid(),
                  series_number=i, instance_number=1, label="mfmr")
        _write(ds, out / f"{i:02d}.dcm")


def gen_mixed(out: Path, project: str) -> None:
    """A smaller spread in one session: single-frame Explicit LE, Explicit BE and Deflated series, plus
    a multiframe Enhanced-MR series (all pydicom-native).

    One session, so one label for every object -- ``_identity`` derives the patient from it, and a
    label per group made one zip into four sessions. One SeriesNumber per series: it is a series-level
    attribute, and varying it within a SeriesInstanceUID made the importer file each object under its
    own scan while the builder catalogued them as one, which the archiver then rejected as unreferenced.
    The multiframe members are Enhanced MR rather than secondary capture, so they archive as scans
    instead of secondary resources.
    """
    import numpy as np
    rng = np.random.default_rng(3)
    study = _uid()
    plan = [("explicitLE", ExplicitVRLittleEndian, 20, 362, 362, 1),
            ("explicitBE", ExplicitVRBigEndian, 10, 256, 256, 1),
            ("deflatedLE", DeflatedExplicitVRLittleEndian, 10, 256, 256, 1),
            ("multiframe", ExplicitVRLittleEndian, 2, 512, 512, 60)]
    for series_number, (label, ts, cnt, rows, cols, frames) in enumerate(plan, start=1):
        series = _uid()
        for i in range(1, cnt + 1):
            if frames > 1:
                ds = _enhanced_mr(rows, cols, frames, _pixels(rows, cols, frames, rng))
            else:
                ds = _base_image(rows, cols, ts=ts)
                ds.NumberOfFrames = "1"
                ds.PixelData = _pixels(rows, cols, 1, rng, ">u2" if ts == ExplicitVRBigEndian else "<u2")
            _identity(ds, project=project, study_uid=study, series_uid=series,
                      series_number=series_number, instance_number=i, label="mixed")
            _write(ds, out / label / f"{i:03d}.dcm")


def gen_huge(out: Path, project: str, frames: int = 4100, rows: int = 512, cols: int = 512) -> None:
    """One uncompressed multiframe object whose PixelData exceeds 2^31 bytes — the XNAT-8737 boundary
    (b61c0620) the candidate builds on. Default 4100 x 512x512x16bit ~= 2.05 GB (just past 2 GiB).
    Built memory-efficiently: one 512 KB frame tiled ``frames`` times (no full-stack numpy array)."""
    study = _uid()
    # numpy-free (so this can run in the sender pod, which has pydicom but not numpy): a horizontal
    # ramp row tiled into one 512 KB frame, tiled again into the >2 GB PixelData in one allocation.
    row = struct.pack(f"<{cols}H", *((c & 0x0FFF) for c in range(cols)))
    frame = row * rows
    ds = _enhanced_mr(rows, cols, frames, frame * frames)
    _identity(ds, project=project, study_uid=study, series_uid=_uid(),
              series_number=1, instance_number=1, label="hugemr")
    _write(ds, out / "huge_2gb.dcm")


WORKLOADS = {"small": gen_small, "multiframe": gen_multiframe, "mixed": gen_mixed, "huge": gen_huge}


# ---------------------------------------------------------------- real data

def route_real(src: Path, out: Path, project: str) -> None:
    """Re-tag a directory of real DICOM to one project with fresh study/SOP UIDs (one session)."""
    study = _uid()
    n = 0
    for p in sorted(src.rglob("*")):
        if not p.is_file() or p.suffix.lower() in {".tsv", ".txt", ".png", ".json"}:
            continue
        try:
            ds = dcmread(p, force=True)
            if "SOPInstanceUID" not in ds or ds.file_meta is None:
                raise ValueError("not a usable DICOM object")
        except Exception:
            continue
        n += 1
        _identity(ds, project=project, study_uid=study, series_uid=_uid(),
                  series_number=n, instance_number=1, label="real")
        _write(ds, out / p.name)
    if n == 0:
        raise SystemExit(f"no usable DICOM under {src}")


# ---------------------------------------------------------------- manifest

def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def manifest(directory: Path) -> list[dict]:
    rows = []
    for p in sorted(directory.rglob("*.dcm")):
        ds = dcmread(p, stop_before_pixels=True, force=True)
        ts = str(ds.file_meta.get("TransferSyntaxUID", "")) if ds.file_meta else ""
        frames = ds.get("NumberOfFrames", 1)
        try:
            frames = int(frames)
        except Exception:
            frames = 1
        rows.append({
            "path": str(p.relative_to(directory)), "sha256": _sha256(p), "ts": ts,
            "ts_name": UID(ts).name if ts else "", "frames": frames, "size": p.stat().st_size,
        })
    return rows


def write_manifest(directory: Path) -> list[dict]:
    rows = manifest(directory)
    with (directory / "manifest.tsv").open("w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["path", "sha256", "ts", "ts_name", "frames", "size"], delimiter="\t")
        w.writeheader()
        w.writerows({k: r[k] for k in w.fieldnames} for r in rows)
    return rows


def build_workload(name: str, out: Path, project: str, *, corpus: Path | None = None) -> list[dict]:
    """Generate (or route) a workload into ``out`` and return its manifest rows."""
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)
    if corpus is not None:
        route_real(corpus, out, project)
    elif name in WORKLOADS:
        WORKLOADS[name](out, project)
    else:
        raise SystemExit(f"unknown workload {name!r}; choose from {sorted(WORKLOADS)} or pass --corpus")
    return write_manifest(out)


if __name__ == "__main__":  # smoke: python corpus.py <workload> <out> [project]
    import sys

    wl = sys.argv[1] if len(sys.argv) > 1 else "small"
    dst = Path(sys.argv[2] if len(sys.argv) > 2 else f"/tmp/perf_corpus/{wl}")
    proj = sys.argv[3] if len(sys.argv) > 3 else "PERF"
    print(f"pydicom {pydicom.__version__}: building {wl} -> {dst} (project {proj})")
    m = build_workload(wl, dst, proj)
    total = sum(r["size"] for r in m)
    print(f"  {len(m)} objects, {total/1e6:.1f} MB, transfer syntaxes: {sorted({r['ts_name'] for r in m})}")
