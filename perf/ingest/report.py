"""Results IO and the baseline-vs-candidate A/B report.

A results file is JSON: a ``header`` (label, build sha, storage layout, per-workload corpus manifest)
and ``cells`` (one per route x workload x anon x rep, each with its phase records). ``compare`` reads
two results files and emits a markdown table: for each (workload, route, anon) cell, the median
wall-clock and NFS-write per phase for baseline and candidate with the delta.
"""
from __future__ import annotations

import json
import statistics
from collections import defaultdict
from pathlib import Path


def write_results(path: Path, header: dict, cells: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps({"header": header, "cells": cells}, indent=2))


def load(path: Path) -> dict:
    return json.loads(Path(path).read_text())


def _median(xs: list[float]) -> float:
    return round(statistics.median(xs), 2) if xs else 0.0


def _aggregate(cells: list[dict]) -> dict:
    """(workload, route, anon) -> {phase -> {wall:[..], nfs:[..]}} medianed, plus per-cell totals."""
    acc: dict[tuple, dict[str, dict[str, list[float]]]] = defaultdict(lambda: defaultdict(lambda: {"wall": [], "nfs": []}))
    for c in cells:
        key = (c["workload"], c["route"], c["anon"])
        for ph in c["phases"]:
            acc[key][ph["phase"]]["wall"].append(ph["wall_s"])
            acc[key][ph["phase"]]["nfs"].append(ph["nfs_mb"])
    out: dict[tuple, dict] = {}
    for key, phases in acc.items():
        per_phase = {ph: {"wall": _median(v["wall"]), "nfs": _median(v["nfs"])} for ph, v in phases.items()}
        total_wall = round(sum(p["wall"] for p in per_phase.values()), 2)
        total_nfs = round(sum(p["nfs"] for p in per_phase.values()), 1)
        out[key] = {"phases": per_phase, "total_wall": total_wall, "total_nfs": total_nfs}
    return out


def _delta(base: float, cand: float) -> str:
    if base == 0:
        return "—" if cand == 0 else "n/a"
    return f"{(cand - base) / base * 100:+.0f}%"


def compare(baseline_path: Path, candidate_path: Path, out: Path) -> str:
    base, cand = load(baseline_path), load(candidate_path)
    bagg, cagg = _aggregate(base["cells"]), _aggregate(cand["cells"])

    lines: list[str] = []
    lines.append("# XNAT ingest performance — A/B\n")
    for label, data in (("baseline", base), ("candidate", cand)):
        h = data["header"]
        lines.append(f"- **{label}**: `{h.get('label')}` · build `{(h.get('build_sha') or '?')[:10]}` · "
                     f"reps {h.get('reps')} · {h.get('cell_count', len(data['cells']))} cells")
    lines.append(f"\nStorage: `{base['header'].get('storage', '').strip()}`\n")

    keys = sorted(set(bagg) | set(cagg))
    lines.append("## Per-cell totals (median over reps)\n")
    lines.append("| workload | route | anon | wall base→cand (Δ) | NFS MB base→cand (Δ) |")
    lines.append("|---|---|---|---|---|")
    for k in keys:
        b, c = bagg.get(k), cagg.get(k)
        bw, cw = (b or {}).get("total_wall", 0), (c or {}).get("total_wall", 0)
        bn, cn = (b or {}).get("total_nfs", 0), (c or {}).get("total_nfs", 0)
        lines.append(f"| {k[0]} | {k[1]} | {k[2]} | {bw:.2f}→{cw:.2f} s ({_delta(bw, cw)}) "
                     f"| {bn:.1f}→{cn:.1f} ({_delta(bn, cn)}) |")

    lines.append("\n## Per-phase detail\n")
    lines.append("| workload | route | anon | phase | wall base→cand (Δ) | NFS MB base→cand (Δ) |")
    lines.append("|---|---|---|---|---|---|")
    for k in keys:
        phases = sorted(set((bagg.get(k, {}).get("phases", {}))) | set(cagg.get(k, {}).get("phases", {})))
        for ph in phases:
            bp = bagg.get(k, {}).get("phases", {}).get(ph, {"wall": 0, "nfs": 0})
            cp = cagg.get(k, {}).get("phases", {}).get(ph, {"wall": 0, "nfs": 0})
            lines.append(f"| {k[0]} | {k[1]} | {k[2]} | {ph} | {bp['wall']:.2f}→{cp['wall']:.2f} s "
                         f"({_delta(bp['wall'], cp['wall'])}) | {bp['nfs']:.1f}→{cp['nfs']:.1f} ({_delta(bp['nfs'], cp['nfs'])}) |")

    if any(c.get("digests") for c in base["cells"] + cand["cells"]):
        from verify import compare_digests   # here so a results-only use of this module needs no cluster plumbing
        lines.append("\n## Output verification (archived files, catalog entries, session XML)\n")
        lines.append("_same = every candidate rep's digest matches the baseline's; a difference names what changed._\n")
        lines.extend(compare_digests(base["cells"], cand["cells"]))

    lines.append("\n---\n_Wall-clock is in-pod curl time (blocking phases) or server-side poll duration "
                 "(async phases, ±1 s). NFS MB = FSx serverwrite delta. Deltas are candidate vs baseline; "
                 "negative is faster/less._")
    text = "\n".join(lines) + "\n"
    out.write_text(text)
    return text
