"""Tests for the exec channel's heartbeat and stall detection, against a local ``sh`` standing in for
``kubectl exec … sh``: run with ``uv run python -m unittest shell_test`` from perf/ingest."""
from __future__ import annotations

import time
import unittest

from k8s import _Shell


class ShellTest(unittest.TestCase):
    def setUp(self):
        self.shell = _Shell(["env"])   # runs `env sh`: a local shell in place of the pod's

    def tearDown(self):
        self.shell.close()

    def test_returns_output_and_exit_status(self):
        self.assertEqual((0, "hello\n"), self.shell.run("echo hello", 10))
        rc, _ = self.shell.run("false", 10)
        self.assertEqual(1, rc)

    def test_a_slow_quiet_command_is_not_mistaken_for_a_stall(self):
        self.shell.STALL_S = 3   # shorter than the command, longer than a heartbeat
        self.shell.HEARTBEAT_S = 1
        rc, out = self.shell.run("sleep 5; echo done", 20)
        self.assertEqual((0, "done\n"), (rc, out))
        self.assertNotIn("\x00", out)

    def test_quick_commands_pay_no_heartbeat_latency(self):
        started = time.monotonic()
        for _ in range(5):
            self.shell.run("true", 10)
        self.assertLess(time.monotonic() - started, 3)

    def test_a_silent_channel_is_reported_as_stalled(self):
        self.shell.STALL_S = 2
        self.shell.HEARTBEAT_S = 60   # no heartbeat arrives within the stall window
        with self.assertRaises(RuntimeError) as caught:
            self.shell.run("sleep 10", 30)
        self.assertIn("stalled", str(caught.exception))


if __name__ == "__main__":
    unittest.main()
