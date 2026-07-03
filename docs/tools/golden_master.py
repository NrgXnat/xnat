#!/usr/bin/env python3
"""Golden-master capture/normalize/diff harness (prototype).

For a *mechanical* migration, the same screen with the same inputs must produce
the same HTML. This harness normalizes away the volatile bits (CSRF tokens,
session ids, timestamps, dates, cache-busters) so a stable golden baseline can
be diffed after each phase.

Usage:
  golden_master.py update <rendered.html> <golden.html>   # write/refresh baseline
  golden_master.py check  <rendered.html> <golden.html>   # exit 0 = match, 1 = differs, 2 = no golden
In real use, <rendered.html> is captured via HTTP from the running app
(/app/... screens, /REST /data /xapi responses); here it's a sandbox render.
"""
import sys, re, difflib, pathlib

# Volatile-content normalization rules (extend per XNAT specifics).
RULES = [
    (re.compile(r'(name="XNAT_CSRF"[^>]*value=")[^"]*(")'), r'\1__CSRF__\2'),
    (re.compile(r'jsessionid=[0-9A-Fa-f]+', re.I),          'jsessionid=__SID__'),
    (re.compile(r'\b\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}(:\d{2})?\b'), '__TS__'),
    (re.compile(r'(Mon|Tue|Wed|Thu|Fri|Sat|Sun),?\s+.{0,30}\d{4}'), '__DATE__'),
    (re.compile(r'([?&](v|_|cb|ts)=)\d+'),                  r'\1__CACHEBUST__'),
    (re.compile(r'[ \t]+$', re.M),                          ''),   # trailing ws
]

def normalize(text):
    for pat, repl in RULES:
        text = pat.sub(repl, text)
    return "\n".join(line for line in text.splitlines()).strip() + "\n"

def main(argv):
    if len(argv) != 4 or argv[1] not in ("update", "check"):
        print(__doc__); return 2
    mode, rendered_f, golden_f = argv[1], argv[2], argv[3]
    rendered = normalize(pathlib.Path(rendered_f).read_text())
    gp = pathlib.Path(golden_f)
    if mode == "update":
        gp.parent.mkdir(parents=True, exist_ok=True)
        gp.write_text(rendered)
        print(f"BASELINE written: {golden_f}  ({len(rendered)} normalized chars)")
        return 0
    if not gp.exists():
        print(f"NO GOLDEN: {golden_f} (run 'update' first)"); return 2
    golden = gp.read_text()
    if rendered == golden:
        print(f"PASS: {rendered_f} == golden (after normalization)"); return 0
    print(f"FAIL: {rendered_f} differs from golden after normalization:")
    for line in difflib.unified_diff(golden.splitlines(), rendered.splitlines(),
                                     "golden", "current", lineterm=""):
        print("   " + line)
    return 1

if __name__ == "__main__":
    sys.exit(main(sys.argv))
