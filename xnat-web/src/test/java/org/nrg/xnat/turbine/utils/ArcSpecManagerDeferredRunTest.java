package org.nrg.xnat.turbine.utils;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A burst of project creations must cost one rewrite of the archive specification XML, not one each, and the last
 * change must never go unwritten.
 */
public class ArcSpecManagerDeferredRunTest {
    private static final long DELAY_MS = 100;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger            runs     = new AtomicInteger();

    @After
    public void shutDown() {
        executor.shutdownNow();
    }

    @Test
    public void manyRequestsBeforeTheRunCostOneRun() throws Exception {
        final ArcSpecManager.DeferredRun deferred = new ArcSpecManager.DeferredRun(executor, runs::incrementAndGet, DELAY_MS, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 100; i++) {
            deferred.request();
        }
        Thread.sleep(DELAY_MS * 5);
        assertThat(runs).hasValue(1);
    }

    @Test
    public void aRequestMadeWhileTheTaskRunsGetsAnotherRun() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final ArcSpecManager.DeferredRun deferred = new ArcSpecManager.DeferredRun(executor, () -> {
            if (runs.incrementAndGet() == 1) {
                started.countDown();
                await(release);
            }
        }, DELAY_MS, TimeUnit.MILLISECONDS);

        deferred.request();
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        // The first run has started and may have read the state already, so this change needs a run of its own.
        deferred.request();
        release.countDown();

        Thread.sleep(DELAY_MS * 5);
        assertThat(runs).hasValue(2);
    }

    @Test
    public void aFailedRunDoesNotBlockLaterRuns() throws Exception {
        final ArcSpecManager.DeferredRun deferred = new ArcSpecManager.DeferredRun(executor, () -> {
            if (runs.incrementAndGet() == 1) {
                throw new IllegalStateException("the first rewrite fails");
            }
        }, DELAY_MS, TimeUnit.MILLISECONDS);

        deferred.request();
        Thread.sleep(DELAY_MS * 5);
        deferred.request();
        Thread.sleep(DELAY_MS * 5);
        assertThat(runs).hasValue(2);
    }

    @Test
    public void theRewriteThreadExitsWhenIdleAndStartsAgainAtTheNextRequest() throws Exception {
        final ScheduledThreadPoolExecutor idling = ArcSpecManager.rewriteExecutor(DELAY_MS, TimeUnit.MILLISECONDS);
        try {
            final ArcSpecManager.DeferredRun deferred = new ArcSpecManager.DeferredRun(idling, runs::incrementAndGet, DELAY_MS, TimeUnit.MILLISECONDS);
            deferred.request();
            Thread.sleep(DELAY_MS * 5);
            assertThat(runs).hasValue(1);
            final long deadline = System.currentTimeMillis() + 5000;
            while (idling.getPoolSize() > 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertThat(idling.getPoolSize()).as("threads left once idle").isZero();

            deferred.request();
            Thread.sleep(DELAY_MS * 5);
            assertThat(runs).hasValue(2);
        } finally {
            idling.shutdownNow();
        }
    }

    private static void await(final CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
