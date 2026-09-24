/*
 * core: org.nrg.xft.utils.SaveLaps
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Per-thread stopwatches for the parts of one {@code DBAction.StoreItem} walk (the database lookups for
 * existing rows, the lookups against the transaction's own saved items, the statement building), so the
 * save's timing line can say where its time went. A walk runs on one thread; the top-level StoreItem resets
 * the laps before it starts and reads them when it ends. Cheap enough to leave on: two nanoTime reads per
 * timed call.
 */
public final class SaveLaps {
    public enum Lap {
        PK_MATCHES("pk-matches"), UNIQUE_MATCHES("unique-matches"), SAVED_LOOKUPS("saved-lookups"),
        INSERT("insert"), NEXT_ID("next-id"), UPDATE("update"), HAS_NEW_FIELDS("has-new-fields"), META("meta");

        final String label;

        Lap(final String label) {
            this.label = label;
        }
    }

    private static final ThreadLocal<long[]> NANOS  = ThreadLocal.withInitial(() -> new long[Lap.values().length]);
    private static final ThreadLocal<int[]>  COUNTS = ThreadLocal.withInitial(() -> new int[Lap.values().length]);

    private SaveLaps() {
    }

    public static void reset() {
        Arrays.fill(NANOS.get(), 0L);
        Arrays.fill(COUNTS.get(), 0);
    }

    public static long start() {
        return System.nanoTime();
    }

    public static void add(final Lap lap, final long started) {
        NANOS.get()[lap.ordinal()] += System.nanoTime() - started;
        COUNTS.get()[lap.ordinal()]++;
    }

    /**
     * @return "pk-matches 12 ms/200, unique-matches 310 ms/201, ..." for the laps that ran at all.
     */
    public static String summary() {
        final long[]        nanos  = NANOS.get();
        final int[]         counts = COUNTS.get();
        final StringBuilder sb     = new StringBuilder();
        for (final Lap lap : Lap.values()) {
            if (counts[lap.ordinal()] == 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(lap.label).append(' ').append(TimeUnit.NANOSECONDS.toMillis(nanos[lap.ordinal()])).append(" ms/").append(counts[lap.ordinal()]);
        }
        return sb.length() == 0 ? "no laps" : sb.toString();
    }
}
