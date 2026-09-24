"""Merge the block files of a multi-instance run into one results file per leg.

In a run driven by rounds.py every block (one leg on one instance in one round) is written by its own
harness process under its own label, so no two processes ever write one file. This gathers a leg's blocks
into a single results file that ``ingest_perf.py report --compare`` reads like any other: each cell keeps
its phases and digests, gains the block it came from and that block's instance facts, and is renumbered so
that every block contributes its own reps.

    uv run merge_results.py --label fourway-D --out results/fourway-D.json results/fourway-D-b*.json
"""
from __future__ import annotations

import argparse
from pathlib import Path

import report


def merge(paths: list[Path], label: str) -> dict:
    cells: list[dict] = []
    blocks: list[dict] = []
    workloads: dict = {}
    next_rep = 1
    for path in sorted(paths):
        data = report.load(path)
        header = data.get("header", {})
        blocks.append({"label": header.get("label"), "file": path.name, "instance": header.get("instance"),
                       "build_sha": header.get("build_sha"), "storage": header.get("storage")})
        workloads.update(header.get("workloads", {}))
        block_cells = data.get("cells", [])
        reps = sorted({c.get("rep", 1) for c in block_cells})
        renumber = {rep: next_rep + i for i, rep in enumerate(reps)}
        next_rep += len(reps)
        for cell in block_cells:
            merged = dict(cell)
            merged["block"] = header.get("label")
            merged["block_rep"] = cell.get("rep", 1)
            merged["rep"] = renumber[cell.get("rep", 1)]
            merged["instance"] = (header.get("instance") or {}).get("namespace")
            cells.append(merged)
    shas = sorted({b["build_sha"] for b in blocks if b.get("build_sha")})
    return {"header": {"label": label,
                       "build_sha": shas[0] if len(shas) == 1 else ",".join(s[:10] for s in shas),
                       "storage": blocks[0].get("storage", "") if blocks else "",
                       "reps": next_rep - 1, "cell_count": len(cells), "workloads": workloads,
                       "blocks": blocks},
            "cells": cells}


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--label", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("blocks", nargs="+")
    args = ap.parse_args()
    merged = merge([Path(p) for p in args.blocks], args.label)
    report.write_results(Path(args.out), merged["header"], merged["cells"])
    h = merged["header"]
    print(f"wrote {args.out}: {len(h['blocks'])} blocks, {h['cell_count']} cells, reps {h['reps']}, "
          f"build {h['build_sha'][:21]}")


if __name__ == "__main__":
    main()
