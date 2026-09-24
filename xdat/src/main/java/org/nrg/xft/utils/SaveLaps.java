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
 * Per-thread stopwatches for the parts of one {@code DBAction.StoreItem} walk (the item walk itself, the
 * reference walks, the database lookups for existing rows, the lookups against the transaction's own saved
 * items, the statement building), so the save's timing line can say where its time went. A walk runs on one
 * thread; the top-level StoreItem resets the laps before it starts and reads them when it ends.
 * <p>
 * Laps nest, and each reports only its own time: the time the laps opened inside it take is theirs. So the
 * laps of a walk add up to the walk, and a recursive part such as the item walk shows the time its bodies
 * spent outside every other lap rather than counting its inner calls over again for every level. Cheap enough
 * to leave on: two nanoTime reads and a few array writes per timed call.
 */
public final class SaveLaps {
    public enum Lap {
        STORE_ITEM("store-item"), SINGLE_REFS("single-refs"), MULTI_REFS("multi-refs"), MAPPING("mapping"),
        PK_MATCHES("pk-matches"), UNIQUE_MATCHES("unique-matches"), SAVED_LOOKUPS("saved-lookups"),
        INSERT("insert"), NEXT_ID("next-id"), UPDATE("update"), HAS_NEW_FIELDS("has-new-fields"), META("meta");

        final String label;

        Lap(final String label) {
            this.label = label;
        }
    }

    private static final int MAX_DEPTH = 256;

    /** One thread's stopwatches: own nanos and calls per lap, and the nested time of each open lap. */
    private static final class Laps {
        final long[] nanos  = new long[Lap.values().length];
        final int[]  counts = new int[Lap.values().length];
        final long[] nested = new long[MAX_DEPTH];
        int          depth;
    }

    private static final ThreadLocal<Laps> LAPS = ThreadLocal.withInitial(Laps::new);

    private SaveLaps() {
    }

    public static void reset() {
        final Laps laps = LAPS.get();
        Arrays.fill(laps.nanos, 0L);
        Arrays.fill(laps.counts, 0);
        laps.depth = 0;
    }

    /**
     * Opens a lap. Pass the result to {@link #add} when the lap ends; the laps opened in between are nested in it.
     */
    public static long start() {
        final Laps laps = LAPS.get();
        if (laps.depth < MAX_DEPTH) {
            laps.nested[laps.depth] = 0;
        }
        laps.depth++;
        return System.nanoTime();
    }

    /**
     * Closes the lap opened by {@link #start} and credits it with its own time: the time since it started, less
     * the time of the laps closed inside it.
     */
    public static void add(final Lap lap, final long started) {
        final long elapsed = System.nanoTime() - started;
        final Laps laps    = LAPS.get();
        long       nested  = 0;
        if (laps.depth > 0) {
            laps.depth--;
            if (laps.depth < MAX_DEPTH) {
                nested = laps.nested[laps.depth];
            }
            if (laps.depth > 0 && laps.depth <= MAX_DEPTH) {
                laps.nested[laps.depth - 1] += elapsed;
            }
        }
        laps.nanos[lap.ordinal()] += elapsed - nested;
        laps.counts[lap.ordinal()]++;
    }

    /**
     * @return "store-item 1200 ms/1004, pk-matches 12 ms/200, ..." for the laps that ran at all.
     */
    public static String summary() {
        final Laps          laps = LAPS.get();
        final StringBuilder sb   = new StringBuilder();
        for (final Lap lap : Lap.values()) {
            if (laps.counts[lap.ordinal()] == 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(lap.label).append(' ').append(TimeUnit.NANOSECONDS.toMillis(laps.nanos[lap.ordinal()])).append(" ms/").append(laps.counts[lap.ordinal()]);
        }
        return sb.length() == 0 ? "no laps" : sb.toString();
    }
}
