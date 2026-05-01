package org.nrg.framework.utilities;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LapStopWatchTest {
    @Test
    public void testLapStopWatch() throws InterruptedException {
        final LapStopWatch stopWatch = LapStopWatch.createStarted();

        List<Long> lapTimes1 = stopWatch.getLapTimes();
        assertThat(lapTimes1).isNotNull().isEmpty();

        Thread.sleep(100);

        stopWatch.lap();

        List<Long> lapTimes2 = stopWatch.getLapTimes();
        assertThat(lapTimes2).isNotNull().isNotEmpty().hasSize(1);

        Thread.sleep(2000);

        stopWatch.lap("Just waited two seconds");

        List<Long> lapTimes3 = stopWatch.getLapTimes();
        assertThat(lapTimes3).isNotNull().isNotEmpty().hasSize(2);

        Thread.sleep(100);
        stopWatch.stop();

        List<Long> lapTimes4 = stopWatch.getLapTimes();
        assertThat(lapTimes4).isNotNull().isNotEmpty().hasSize(3);

        final long totalTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        final long lapTime1  = lapTimes4.get(0);
        final long lapTime2  = lapTimes4.get(1);
        final long lapTime3  = lapTimes4.get(2);
        assertThat(lapTime1).isGreaterThan(0).isLessThan(totalTime);
        assertThat(lapTime2).isGreaterThan(0).isLessThan(totalTime);
        assertThat(lapTime3).isGreaterThan(0).isLessThan(totalTime);
        assertThat(totalTime).isGreaterThanOrEqualTo(lapTime1 + lapTime2 + lapTime3);

        final List<LapStopWatch.Lap> laps = stopWatch.getLaps();
        final LapStopWatch.Lap       lap1 = laps.get(0);
        final LapStopWatch.Lap       lap2 = laps.get(1);
        final LapStopWatch.Lap       lap3 = laps.get(2);

        assertThat(lap1).isNotNull()
                        .hasFieldOrPropertyWithValue("lapTime", lapTime1)
                        .hasFieldOrPropertyWithValue("message", "");
        assertThat(lap2).isNotNull()
                        .hasFieldOrPropertyWithValue("lapTime", lapTime2)
                        .hasFieldOrPropertyWithValue("message", "Just waited two seconds");
        assertThat(lap3).isNotNull()
                        .hasFieldOrPropertyWithValue("lapTime", lapTime3)
                        .hasFieldOrPropertyWithValue("message", "Stop");
        assertThat(stopWatch).isNotNull()
                             .hasFieldOrPropertyWithValue("time", lap3.getOverallTime());

        stopWatch.toTable(System.out);
        stopWatch.toCSV(System.out);

        Thread.sleep(100);
    }

    private static void randomSleep() {
        try {
            Thread.sleep(RandomUtils.nextLong(100, 2001));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
