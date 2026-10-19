package org.nrg.xnat.services.cache;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CoalescingRunnerTest {
    private final CoalescingRunner _runner = new CoalescingRunner();

    @Test(timeout = 10000)
    public void callsDuringARunCostOneMoreRun() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger  runs    = new AtomicInteger();
        final Runnable task = () -> {
            if (runs.incrementAndGet() == 1) {
                started.countDown();
                await(release);
            }
        };

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final Future<?> first = executor.submit(() -> _runner.run("user", task));
            started.await();
            for (int call = 0; call < 5; call++) {
                _runner.run("user", task);
            }
            assertThat(runs).hasValue(1);

            release.countDown();
            first.get();
            assertThat(runs).hasValue(2);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(timeout = 10000)
    public void keysDoNotWaitForEachOther() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> _runner.run("a", () -> {
                started.countDown();
                await(release);
            }));
            started.await();

            final AtomicBoolean ranB = new AtomicBoolean();
            _runner.run("b", () -> ranB.set(true));
            assertThat(ranB).isTrue();
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void aFailedRunDoesNotBlockLaterCalls() {
        assertThatThrownBy(() -> _runner.run("user", () -> {
            throw new IllegalStateException("rebuild failed");
        })).isInstanceOf(IllegalStateException.class);

        final AtomicInteger runs = new AtomicInteger();
        _runner.run("user", runs::incrementAndGet);
        assertThat(runs).hasValue(1);
    }

    private static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
