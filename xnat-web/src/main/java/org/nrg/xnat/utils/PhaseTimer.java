/*
 * web: org.nrg.xnat.utils.PhaseTimer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

/**
 * Times the consecutive phases of one operation, for a single summary log line at the end.
 * <p>
 * Build and archive of a session are long sequences of per-file work over network storage, and
 * their wall clock is dominated by round trips rather than bytes, so which phase is slow cannot be
 * inferred from what was written. Each {@link #lap} records the time since the previous one under
 * a name, and {@link #toString} renders the total and the laps in order, so one line per session
 * says where the time went.
 */
public final class PhaseTimer {
    private final long          _start = System.nanoTime();
    private final StringBuilder _laps  = new StringBuilder();
    private       long          _last  = _start;

    /** Closes the phase that began at construction or at the previous lap, recording it under <b>name</b>. */
    public void lap(final String name) {
        final long now = System.nanoTime();
        if (_laps.length() > 0) {
            _laps.append(' ');
        }
        _laps.append(name).append('=').append((now - _last) / 1_000_000).append("ms");
        _last = now;
    }

    public long totalMillis() {
        return (System.nanoTime() - _start) / 1_000_000;
    }

    @Override
    public String toString() {
        return "total=" + totalMillis() + "ms" + (_laps.length() == 0 ? "" : " [" + _laps + "]");
    }
}
