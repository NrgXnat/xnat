package org.nrg.xnat.services.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs a task for a key at most once at a time. A caller that finds the key's task running neither waits nor runs
 * it: it has the running task run once more when it finishes. However many calls arrive during one run, they cost a
 * single further run, and the last run starts after the last call.
 */
final class CoalescingRunner {
    /**
     * Holds a key while its task runs, mapped to whether another run has been asked for since the current one
     * started.
     */
    private final ConcurrentMap<String, Boolean> _running = new ConcurrentHashMap<>();

    void run(final String key, final Runnable task) {
        final AtomicBoolean claimed = new AtomicBoolean();
        _running.compute(key, (ignored, rerun) -> {
            claimed.set(rerun == null);
            return rerun != null;
        });
        if (!claimed.get()) {
            return;
        }
        boolean finished = false;
        try {
            do {
                task.run();
            } while (_running.compute(key, (ignored, rerun) -> rerun ? Boolean.FALSE : null) != null);
            finished = true;
        } finally {
            if (!finished) {
                // A run asked for during a failed one is dropped: callers evict before calling, so readers rebuild.
                _running.remove(key);
            }
        }
    }
}
