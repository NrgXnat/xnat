/*
 * prefs: org.nrg.prefs.tests.PreferenceBeanConcurrencyTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.prefs.configuration.ConcurrentPrefsTestsConfiguration;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.tools.concurrent.ConcurrentPrefsToolPreferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests preference beans read, written and invalidated from several threads, as when the dist-events plugin
 * invalidates a preference from its message listener while requests read it.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ConcurrentPrefsTestsConfiguration.class)
public class PreferenceBeanConcurrencyTests {
    @Autowired
    public PreferenceBeanConcurrencyTests(final ConcurrentPrefsToolPreferenceBean bean, final NrgPreferenceService preferenceService) {
        _bean              = bean;
        _preferenceService = preferenceService;
    }

    @AfterEach
    public void reset() throws InvalidPreferenceName {
        _bean.setAfterSlowPrefRead(null);
        _bean.setSlowPref("slowInitial");
        _bean.setPlainPref("plainInitial");
        _bean.invalidate("derivedPref");
        _executor.shutdownNow();
    }

    @Test
    public void testInvalidateDuringLoadDoesNotCacheOldValue() throws Exception {
        _preferenceService.setPreferenceValue(_bean.getToolId(), "slowPref", "old");
        _bean.invalidate("slowPref");

        final Future<Object> read = readSlowPrefAndPause();
        try {
            // Another node stores a new value and tells this one to drop its copy while the read is loading the old one.
            _executor.submit(() -> {
                _preferenceService.setPreferenceValue(_bean.getToolId(), "slowPref", "new");
                _bean.invalidate("slowPref");
                return null;
            }).get(10, SECONDS);
        } finally {
            _release.countDown();
        }
        assertThat(read.get(10, SECONDS)).isEqualTo("old");
        assertThat(_bean.get("slowPref")).isEqualTo("new");
    }

    @Test
    public void testSetDuringLoadDoesNotCacheOldValue() throws Exception {
        _bean.setSlowPref("old");
        _bean.invalidate("slowPref");

        final Future<Object> read = readSlowPrefAndPause();
        try {
            _executor.submit(() -> {
                _bean.setSlowPref("new");
                return null;
            }).get(10, SECONDS);
        } finally {
            _release.countDown();
        }
        assertThat(read.get(10, SECONDS)).isEqualTo("old");
        assertThat(_bean.get("slowPref")).isEqualTo("new");
    }

    @Test
    public void testGetterCanReadAnotherPreferenceThroughMap() throws InvalidPreferenceName {
        _bean.setPlainPref("plainChanged");
        _bean.invalidate("derivedPref");
        assertThat(_bean.get("derivedPref")).isEqualTo("derived+plainChanged");
    }

    @Test
    public void testConcurrentReadsWritesAndInvalidations() throws Exception {
        final Queue<Throwable> failures = new ConcurrentLinkedQueue<>();
        final AtomicBoolean    stop     = new AtomicBoolean();
        final List<Future<?>>  tasks    = new ArrayList<>();
        for (int reader = 0; reader < 4; reader++) {
            tasks.add(_executor.submit(repeat(stop, failures, () -> {
                final String key = randomKey();
                _bean.get(key);
                _bean.containsKey(key);
                _bean.entrySet();
                _bean.size();
            })));
        }
        for (int invalidator = 0; invalidator < 2; invalidator++) {
            tasks.add(_executor.submit(repeat(stop, failures, () -> _bean.invalidate(randomKey()))));
        }
        for (int writer = 0; writer < 2; writer++) {
            tasks.add(_executor.submit(repeat(stop, failures, () -> _bean.setPlainPref("plain" + ThreadLocalRandom.current().nextInt(10)))));
        }
        Thread.sleep(SECONDS.toMillis(3));
        stop.set(true);
        for (final Future<?> task : tasks) {
            task.get(10, SECONDS);
        }
        assertThat(failures).isEmpty();

        _bean.setPlainPref("plainFinal");
        assertThat(_bean.get("plainPref")).isEqualTo("plainFinal");
        _bean.invalidate("derivedPref");
        assertThat(_bean.get("derivedPref")).isEqualTo("derived+plainFinal");
    }

    private Future<Object> readSlowPrefAndPause() throws InterruptedException {
        final AtomicBoolean paused = new AtomicBoolean();
        _bean.setAfterSlowPrefRead(() -> {
            if (paused.compareAndSet(false, true)) {
                _loaded.countDown();
                try {
                    _release.await(10, SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        final Future<Object> read = _executor.submit(() -> _bean.get("slowPref"));
        assertThat(_loaded.await(10, SECONDS)).isTrue();
        return read;
    }

    private static Runnable repeat(final AtomicBoolean stop, final Queue<Throwable> failures, final ThrowingRunnable action) {
        return () -> {
            while (!stop.get()) {
                try {
                    action.run();
                } catch (Throwable e) {
                    failures.add(e);
                    return;
                }
            }
        };
    }

    private static String randomKey() {
        return KEYS[ThreadLocalRandom.current().nextInt(KEYS.length)];
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final String[] KEYS = {"slowPref", "plainPref", "derivedPref"};

    private final ConcurrentPrefsToolPreferenceBean _bean;
    private final NrgPreferenceService              _preferenceService;
    private final ExecutorService                   _executor = Executors.newCachedThreadPool();
    private final CountDownLatch                    _loaded   = new CountDownLatch(1);
    private final CountDownLatch                    _release  = new CountDownLatch(1);
}
