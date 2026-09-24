"""A lock that lets several harness processes on this machine drive separate XNAT instances on shared
storage without any of them measuring while another one loads that storage.

A measurement (a cell's route and the settle after it) takes the lock exclusively. Everything else a run
does against its instance (wiping the project, staging a workload, restaging the inbox, configuring
anonymization, taking digests, setup) takes it shared. So at any moment either exactly one instance is being
measured and every other harness waits, or any number of them are doing their overhead side by side, which
is where the time goes: most of a run is the between-cell wipe, not the measurement.

A measurement that is waiting holds a turnstile that shared holders must pass on their way in, so a stream
of overhead cannot starve it. Built on flock(2) over two files, ``<path>`` and ``<path>.turnstile``; the
kernel drops the locks of a process that dies. All the harness processes sharing a lock must run on this
machine.
"""
from __future__ import annotations

import fcntl
import os
from contextlib import contextmanager
from typing import Iterator


class MeasureLock:
    def __init__(self, path: str | None):
        self.path = path
        self._busy_depth = 0
        self._measuring = False

    @contextmanager
    def measuring(self) -> Iterator[None]:
        """Exclusive: no other harness is measuring or doing overhead while this block runs."""
        if not self.path:
            yield
            return
        if self._busy_depth:
            raise RuntimeError("measuring() inside busy() would wait on itself")
        turnstile = _acquire(self.path + ".turnstile", fcntl.LOCK_EX)
        try:
            rw = _acquire(self.path, fcntl.LOCK_EX)
            self._measuring = True
            try:
                yield
            finally:
                self._measuring = False
                _release(rw)
        finally:
            _release(turnstile)

    @contextmanager
    def busy(self) -> Iterator[None]:
        """Shared: overhead that loads the instance or the storage; excludes measurements only."""
        if not self.path or self._busy_depth or self._measuring:
            # Already inside a section: re-entering would queue behind a waiting measurement that is
            # itself waiting for this process to leave.
            self._busy_depth += 1
            try:
                yield
            finally:
                self._busy_depth -= 1
            return
        turnstile = _acquire(self.path + ".turnstile", fcntl.LOCK_EX)
        try:
            rw = _acquire(self.path, fcntl.LOCK_SH)
        finally:
            _release(turnstile)
        self._busy_depth = 1
        try:
            yield
        finally:
            self._busy_depth = 0
            _release(rw)


def _acquire(path: str, mode: int) -> int:
    fd = os.open(path, os.O_RDWR | os.O_CREAT, 0o644)
    try:
        fcntl.flock(fd, mode)
    except BaseException:
        os.close(fd)
        raise
    return fd


def _release(fd: int) -> None:
    try:
        fcntl.flock(fd, fcntl.LOCK_UN)
    finally:
        os.close(fd)
