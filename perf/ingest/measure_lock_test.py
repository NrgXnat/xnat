"""Tests for measure_lock: run with ``uv run python -m unittest measure_lock_test`` from perf/ingest.

Each thread stands in for a harness process: it has its own MeasureLock and so its own file descriptors,
and flock(2) locks held through different descriptors exclude each other as they would across processes.
"""
from __future__ import annotations

import tempfile
import threading
import time
import unittest
from pathlib import Path

from measure_lock import MeasureLock


class Recorder:
    def __init__(self):
        self.events: list[tuple[float, float, str, str]] = []
        self._lock = threading.Lock()

    def hold(self, who: str, kind: str, seconds: float) -> None:
        start = time.monotonic()
        time.sleep(seconds)
        with self._lock:
            self.events.append((start, time.monotonic(), who, kind))


def overlaps(a, b) -> bool:
    return a[0] < b[1] and b[0] < a[1]


class MeasureLockTest(unittest.TestCase):
    def setUp(self):
        self.dir = tempfile.TemporaryDirectory()
        self.path = str(Path(self.dir.name) / "measure.lock")

    def tearDown(self):
        self.dir.cleanup()

    def run_threads(self, *targets):
        threads = [threading.Thread(target=t) for t in targets]
        for t in threads:
            t.start()
        for t in threads:
            t.join(timeout=10)
            self.assertFalse(t.is_alive(), "a lock holder never finished")

    def test_measurements_never_overlap_each_other_or_overhead(self):
        rec = Recorder()

        def measurer(name):
            lock = MeasureLock(self.path)
            for _ in range(3):
                with lock.measuring():
                    rec.hold(name, "measure", 0.05)

        def worker(name):
            lock = MeasureLock(self.path)
            for _ in range(3):
                with lock.busy():
                    rec.hold(name, "busy", 0.05)

        self.run_threads(lambda: measurer("m1"), lambda: measurer("m2"), lambda: worker("w1"), lambda: worker("w2"))
        measures = [e for e in rec.events if e[3] == "measure"]
        self.assertEqual(6, len(measures))
        for m in measures:
            for other in rec.events:
                if other is not m:
                    self.assertFalse(overlaps(m, other), f"{m} overlapped {other}")

    def test_overhead_runs_side_by_side(self):
        rec = Recorder()
        barrier = threading.Barrier(3)

        def worker(name):
            lock = MeasureLock(self.path)
            barrier.wait()
            with lock.busy():
                rec.hold(name, "busy", 0.3)

        started = time.monotonic()
        self.run_threads(*(lambda n=n: worker(n) for n in ("w1", "w2", "w3")))
        self.assertLess(time.monotonic() - started, 0.8, "shared holders were serialized")

    def test_a_waiting_measurement_goes_before_new_overhead(self):
        rec = Recorder()
        first_busy_in = threading.Event()
        measurer_waiting = threading.Event()

        def early_worker():
            lock = MeasureLock(self.path)
            with lock.busy():
                first_busy_in.set()
                rec.hold("early", "busy", 0.4)

        def measurer():
            first_busy_in.wait()
            lock = MeasureLock(self.path)
            measurer_waiting.set()
            with lock.measuring():
                rec.hold("m", "measure", 0.1)

        def late_worker():
            measurer_waiting.wait()
            time.sleep(0.1)   # the measurer is now queued behind the early worker
            lock = MeasureLock(self.path)
            with lock.busy():
                rec.hold("late", "busy", 0.05)

        self.run_threads(early_worker, measurer, late_worker)
        by = {e[2]: e for e in rec.events}
        self.assertLess(by["m"][1], by["late"][1] + 1e-9)
        self.assertGreaterEqual(by["late"][0], by["m"][1] - 1e-3, "new overhead jumped a waiting measurement")

    def test_nested_overhead_does_not_wait_on_itself(self):
        lock = MeasureLock(self.path)
        with lock.busy():
            with lock.busy():
                pass
        with lock.measuring():
            with lock.busy():
                pass

    def test_measuring_inside_overhead_is_refused(self):
        lock = MeasureLock(self.path)
        with lock.busy():
            with self.assertRaises(RuntimeError):
                with lock.measuring():
                    pass

    def test_no_path_means_no_locking(self):
        lock = MeasureLock(None)
        with lock.measuring():
            with lock.busy():
                pass


if __name__ == "__main__":
    unittest.main()
