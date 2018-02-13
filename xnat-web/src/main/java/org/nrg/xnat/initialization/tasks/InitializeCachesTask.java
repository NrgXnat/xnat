package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.XnatCache;
import org.nrg.xft.schema.XFTManager;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.nrg.xnat.initialization.tasks.InitializingTaskException.Level.Info;

@Component
@Slf4j
public class InitializeCachesTask extends AbstractInitializingTask {
    public static final String INITIALIZING_MESSAGE = "There are {} initializing caches that have not yet started initializing and {} that have started but have not yet completed initializing";

    @Autowired
    public InitializeCachesTask(final List<XnatCache> caches) {
        _stopWatch = LapStopWatch.createStarted(log, Level.INFO);
        for (final XnatCache cache : caches) {
            if (Initializing.class.isAssignableFrom(cache.getClass())) {
                _caches.add((Initializing) cache);
            }
        }
        _stopWatch.lap("Added {} caches to the initialization queue", caches.size());
    }

    @Override
    public String getTaskName() {
        return "Initialize all caches that implement the Initializing interface.";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (!XFTManager.isComplete()) {
            throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "XFTManager has not yet completed initialization. Deferring execution.");
        }

        _stopWatch.lap("Starting cache initialization");
        processInitializingCaches();
        processUninitializedCaches();

        if (!_caches.isEmpty() || !_initializing.isEmpty()) {
            _stopWatch.lap(INITIALIZING_MESSAGE, _caches.size(), _initializing.size());
            throw new InitializingTaskException(Info, INITIALIZING_MESSAGE, _caches.size(), _initializing.size());
        } else {
            _stopWatch.stop();
            if (log.isInfoEnabled()) {
                log.info(_stopWatch.toTable());
            }
        }
    }

    private void processInitializingCaches() {
        final List<String> completedKeys = new ArrayList<>();
        for (final String cache : _initializing.keySet()) {
            _stopWatch.lap("Checking initializing cache {}", cache);
            final Future<Boolean> future = _initializing.get(cache);
            if (future.isDone()) {
                completedKeys.add(cache);
                try {
                    final boolean result = future.get();
                    _stopWatch.lap("The initializing cache {} completed {}", cache, result ? "successfully" : "unsuccessfully");
                } catch (InterruptedException | ExecutionException e) {
                    _stopWatch.lap(Level.WARN, "The initializing cache {} encountered an error during initialization", cache, e);
                }
            } else if (future.isCancelled()) {
                completedKeys.add(cache);
                _stopWatch.lap(Level.WARN, "The initializing cache {} was cancelled before completing initialization", cache);
            }
        }
        for (final String completed : completedKeys) {
            _initializing.remove(completed);
        }
    }

    private void processUninitializedCaches() {
        final List<Initializing> uninitialized = new ArrayList<>();
        for (final Initializing cache : _caches) {
            final String cacheName = ((XnatCache) cache).getCacheName();
            if (!cache.isInitialized() && cache.canInitialize()) {
                _stopWatch.lap("Cache {} is not yet initialized but indicates it's ready, so let's go", cacheName);
                _initializing.put(cacheName, cache.initialize());
            } else {
                _stopWatch.lap("Cache {} is not yet ready to be initialized", cacheName);
                uninitialized.add(cache);
            }
        }
        _caches.clear();
        _caches.addAll(uninitialized);
    }

    private final List<Initializing>           _caches       = new ArrayList<>();
    private final Map<String, Future<Boolean>> _initializing = new HashMap<>();

    private final LapStopWatch _stopWatch;
}
