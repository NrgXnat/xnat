package org.nrg.xnat.turbine.utils;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * A burst of project creations must cost one rewrite of the archive specification XML, not one each, and the last
 * change must never go unwritten. The executor is a mock, so each test runs the scheduled task itself.
 */
public class ArcSpecManagerDeferredRunTest {
    private static final long     DELAY = 10;
    private static final TimeUnit UNIT  = TimeUnit.SECONDS;

    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final AtomicInteger            runs     = new AtomicInteger();

    private ArcSpecManager.DeferredRun deferred;

    @Test
    public void manyRequestsBeforeTheRunCostOneRun() {
        deferred = new ArcSpecManager.DeferredRun(executor, runs::incrementAndGet, DELAY, UNIT);
        for (int i = 0; i < 100; i++) {
            deferred.request();
        }

        assertThat(scheduled()).hasSize(1);
        scheduled().get(0).run();
        assertThat(runs).hasValue(1);
    }

    @Test
    public void aRequestMadeWhileTheTaskRunsGetsAnotherRun() {
        deferred = new ArcSpecManager.DeferredRun(executor, () -> {
            // The run has started and may have read the state already, so this change needs a run of its own.
            if (runs.incrementAndGet() == 1) {
                deferred.request();
            }
        }, DELAY, UNIT);
        deferred.request();
        scheduled().get(0).run();

        assertThat(scheduled()).hasSize(2);
    }

    @Test
    public void theRewriteThreadIsADaemonThatExitsWhenIdle() {
        final ScheduledThreadPoolExecutor rewrites = ArcSpecManager.rewriteExecutor(1, TimeUnit.MINUTES);
        try {
            // The executor retires an idle core thread only when told to, and starts a new one at the next task.
            assertThat(rewrites.allowsCoreThreadTimeOut()).isTrue();
            assertThat(rewrites.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(60);
            assertThat(rewrites.getThreadFactory().newThread(() -> {}).isDaemon()).isTrue();
        } finally {
            rewrites.shutdownNow();
        }
    }

    /** The tasks handed to the executor so far, in order, each with the run's delay. */
    private List<Runnable> scheduled() {
        final ArgumentCaptor<Runnable> tasks = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, atLeastOnce()).schedule(tasks.capture(), eq(DELAY), eq(UNIT));
        return tasks.getAllValues();
    }
}
