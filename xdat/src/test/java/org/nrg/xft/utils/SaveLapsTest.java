/*
 * core: org.nrg.xft.utils.SaveLapsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * A lap reports the time it spent itself: the laps opened inside it are theirs, so the laps of one save walk
 * add up to the walk instead of counting the inner ones over again for every level.
 */
public class SaveLapsTest {
    @Test
    public void reportsOnlyTheTimeALapSpentItself() throws Exception {
        SaveLaps.reset();
        final long walk   = SaveLaps.start();
        final long insert = SaveLaps.start();
        Thread.sleep(100);
        SaveLaps.add(SaveLaps.Lap.INSERT, insert);
        SaveLaps.add(SaveLaps.Lap.STORE_ITEM, walk);

        assertTrue(SaveLaps.summary(), millis(SaveLaps.Lap.INSERT) >= 100);
        assertTrue(SaveLaps.summary(), millis(SaveLaps.Lap.STORE_ITEM) < 50);
    }

    @Test
    public void countsEveryCallOfALap() {
        SaveLaps.reset();
        SaveLaps.add(SaveLaps.Lap.INSERT, SaveLaps.start());
        SaveLaps.add(SaveLaps.Lap.INSERT, SaveLaps.start());

        assertEquals(SaveLaps.summary(), 2, count(SaveLaps.Lap.INSERT));
    }

    private static long millis(final SaveLaps.Lap lap) {
        return Long.parseLong(find(lap).group(1));
    }

    private static int count(final SaveLaps.Lap lap) {
        return Integer.parseInt(find(lap).group(2));
    }

    private static Matcher find(final SaveLaps.Lap lap) {
        final Matcher matcher = Pattern.compile(Pattern.quote(lap.label) + " (\\d+) ms/(\\d+)").matcher(SaveLaps.summary());
        assertTrue(SaveLaps.summary(), matcher.find());
        return matcher;
    }
}
