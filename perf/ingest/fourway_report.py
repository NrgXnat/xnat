"""Side-by-side report for a multi-leg run (several builds, each merged from its blocks by rounds.py --merge).

For every workload x route x anon cell it gives each leg's median wall-clock with the spread over its reps,
and the change at each step from one leg to the next. It then shows what the storage was doing: the NFS
operations each leg caused (fewer is the structural win), and the mean NFS round trip each leg and each
instance saw (similar round trips mean the legs were measured under similar conditions; a leg or instance
that saw slower storage stands out). It checks the archived output of each leg against the previous one,
and, when one leg ran on every instance, estimates how much faster or slower each instance was.

    uv run fourway_report.py --leg 1.10.1=results/fourway-A.json --leg develop=results/fourway-B.json \
        --leg ingest-io=results/fourway-C.json --leg archive-phase=results/fourway-D.json -o fourway.md
"""
from __future__ import annotations

import argparse
import statistics
from collections import defaultdict
from pathlib import Path

import report

MIN_OPS = 50   # a phase with fewer NFS operations says little about the storage's latency


def load_legs(specs: list[str]) -> list[tuple[str, dict]]:
    legs = []
    for spec in specs:
        name, _, path = spec.partition("=")
        legs.append((name, report.load(Path(path))))
    return legs


def cell_key(cell: dict) -> tuple:
    return cell["workload"], cell["route"], cell["anon"]


def totals(cells: list[dict]) -> dict[tuple, list[float]]:
    out: dict[tuple, list[float]] = defaultdict(list)
    for c in cells:
        if c.get("ok") and c.get("phases"):
            out[cell_key(c)].append(sum(p["wall_s"] for p in c["phases"]))
    return out


def phase_walls(cells: list[dict]) -> dict[tuple, list[float]]:
    out: dict[tuple, list[float]] = defaultdict(list)
    for c in cells:
        if c.get("ok"):
            for p in c.get("phases", []):
                out[cell_key(c) + (p["phase"],)].append(p["wall_s"])
    return out


def fmt_spread(xs: list[float]) -> str:
    if not xs:
        return "–"
    med = statistics.median(xs)
    return f"{med:.2f} ({min(xs):.2f}–{max(xs):.2f})" if len(xs) > 1 else f"{med:.2f}"


def pct(base: list[float], cand: list[float]) -> str:
    if not base or not cand:
        return "–"
    b, c = statistics.median(base), statistics.median(cand)
    return "–" if b == 0 else f"{(c - b) / b * 100:+.0f} %"


def rtts(cells: list[dict], by: str | None = None) -> dict[str, list[float]]:
    out: dict[str, list[float]] = defaultdict(list)
    for c in cells:
        if not c.get("ok"):
            continue
        group = (c.get(by) or "?") if by else "all"
        for p in c.get("phases", []):
            if p.get("nfs_rtt_ms") is not None and p.get("nfs_ops", 0) >= MIN_OPS:
                out[group].append(p["nfs_rtt_ms"])
    return out


def quantile(xs: list[float], q: float) -> float:
    xs = sorted(xs)
    return xs[min(len(xs) - 1, int(q * len(xs)))]


def build(legs: list[tuple[str, dict]]) -> str:
    names = [n for n, _ in legs]
    tot = {n: totals(d["cells"]) for n, d in legs}
    keys = sorted(set().union(*(set(t) for t in tot.values())))
    lines = ["# XNAT ingest performance: " + " → ".join(names) + "\n"]
    for n, d in legs:
        h = d["header"]
        instances = sorted({(b.get("instance") or {}).get("namespace", "?") for b in h.get("blocks", [])})
        lines.append(f"- **{n}**: `{h.get('label')}` · build `{str(h.get('build_sha'))[:21]}` · "
                     f"{h.get('reps')} reps from {len(h.get('blocks', []))} blocks on {', '.join(instances) or '?'}")

    steps = list(zip(names, names[1:]))
    lines.append("\n## Per cell: median wall-clock, s, with the range over reps\n")
    lines.append("| workload | route | anon | " + " | ".join(names) + " | " +
                 " | ".join(f"{b}→{c}" for b, c in steps) + " | " + f"{names[0]}→{names[-1]} |")
    lines.append("|---|---|---|" + "---|" * (len(names) + len(steps) + 1))
    for k in keys:
        row = [fmt_spread(tot[n].get(k, [])) for n in names]
        deltas = [pct(tot[b].get(k, []), tot[c].get(k, [])) for b, c in steps]
        lines.append(f"| {k[0]} | {k[1]} | {k[2]} | " + " | ".join(row) + " | " + " | ".join(deltas) +
                     f" | {pct(tot[names[0]].get(k, []), tot[names[-1]].get(k, []))} |")

    ph = {n: phase_walls(d["cells"]) for n, d in legs}
    pkeys = sorted(set().union(*(set(p) for p in ph.values())))
    lines.append("\n## Per phase: median wall-clock, s\n")
    lines.append("| workload | route | anon | phase | " + " | ".join(names) + " |")
    lines.append("|---|---|---|---|" + "---|" * len(names))
    for k in pkeys:
        vals = [f"{statistics.median(ph[n][k]):.2f}" if ph[n].get(k) else "–" for n in names]
        lines.append(f"| {k[0]} | {k[1]} | {k[2]} | {k[3]} | " + " | ".join(vals) + " |")

    lines.append("\n## NFS operations per cell, median\n")
    lines.append("_How many times each build went to the storage for the same work._\n")
    ops = {n: defaultdict(list) for n in names}
    for n, d in legs:
        for c in d["cells"]:
            if c.get("ok") and any("nfs_ops" in p for p in c.get("phases", [])):
                ops[n][cell_key(c)].append(sum(p.get("nfs_ops", 0) for p in c["phases"]))
    okeys = sorted(set().union(*(set(o) for o in ops.values())))
    if okeys:
        lines.append("| workload | route | anon | " + " | ".join(names) + " |")
        lines.append("|---|---|---|" + "---|" * len(names))
        for k in okeys:
            vals = [f"{statistics.median(ops[n][k]):.0f}" if ops[n].get(k) else "–" for n in names]
            lines.append(f"| {k[0]} | {k[1]} | {k[2]} | " + " | ".join(vals) + " |")
    else:
        lines.append("_No NFS operation counts recorded._")

    lines.append("\n## Storage conditions: mean NFS round trip per phase, ms\n")
    lines.append(f"_Over phases with at least {MIN_OPS} NFS operations. Similar figures across legs and instances "
                 "mean they were measured under similar storage conditions._\n")
    lines.append("| leg | phases | median | p90 |")
    lines.append("|---|---|---|---|")
    for n, d in legs:
        xs = rtts(d["cells"]).get("all", [])
        lines.append(f"| {n} | {len(xs)} | " + (f"{statistics.median(xs):.3f} | {quantile(xs, 0.9):.3f} |" if xs else "– | – |"))
    by_instance: dict[str, list[float]] = defaultdict(list)
    for _, d in legs:
        for inst, xs in rtts(d["cells"], by="instance").items():
            by_instance[inst].extend(xs)
    if len(by_instance) > 1:
        lines.append("\n| instance | phases | median | p90 |")
        lines.append("|---|---|---|---|")
        for inst, xs in sorted(by_instance.items()):
            lines.append(f"| {inst} | {len(xs)} | {statistics.median(xs):.3f} | {quantile(xs, 0.9):.3f} |")

    for n, d in legs:
        per_inst: dict[str, dict[tuple, list[float]]] = defaultdict(lambda: defaultdict(list))
        for c in d["cells"]:
            if c.get("ok") and c.get("phases"):
                per_inst[c.get("instance") or "?"][cell_key(c)].append(sum(p["wall_s"] for p in c["phases"]))
        if len(per_inst) < 2:
            continue
        overall = tot[n]
        lines.append(f"\n## Instance calibration from {n}, which ran on {len(per_inst)} instances\n")
        lines.append("_Each instance's cell times relative to the leg's median for the same cell, median over cells. "
                     "Legs rotate across instances, so these offsets average out of the comparison; large ones "
                     "would still deserve a look._\n")
        lines.append("| instance | cells | relative wall-clock |")
        lines.append("|---|---|---|")
        for inst, cells in sorted(per_inst.items()):
            ratios = [statistics.median(v) / statistics.median(overall[k]) for k, v in cells.items()
                      if overall.get(k) and statistics.median(overall[k]) > 0]
            if ratios:
                lines.append(f"| {inst} | {len(ratios)} | {statistics.median(ratios):.3f} |")
        break

    if any(c.get("digests") for _, d in legs for c in d["cells"]):
        from verify import compare_digests
        for (b, bd), (c, cd) in zip(legs, legs[1:]):
            lines.append(f"\n## Output verification: {c} against {b}\n")
            lines.extend(compare_digests(bd["cells"], cd["cells"]))

    lines.append("\n---\n_Wall-clock is in-pod curl time (blocking phases) or server-side poll duration (async phases, "
                 "±1 s), summed over a cell's phases. Measurements were serialized across instances by the "
                 "measurement lock, so no instance was measured while another loaded the storage._")
    return "\n".join(lines) + "\n"


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--leg", action="append", required=True, metavar="NAME=RESULTS.json",
                    help="a leg in order, oldest first; repeat")
    ap.add_argument("-o", "--out", default="fourway.md")
    args = ap.parse_args()
    text = build(load_legs(args.leg))
    Path(args.out).write_text(text)
    print(text)


if __name__ == "__main__":
    main()
