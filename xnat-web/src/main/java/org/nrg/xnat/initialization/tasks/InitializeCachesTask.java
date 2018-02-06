package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.XnatCache;
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
    @Autowired
    public InitializeCachesTask(final List<XnatCache> caches) {
        for (final XnatCache cache : caches) {
            if (Initializing.class.isAssignableFrom(cache.getClass())) {
                _caches.add((Initializing) cache);
            }
        }
    }

    @Override
    public String getTaskName() {
        return "Initialize all caches that implement the Initializing interface.";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        processInitializingCaches();
        processUninitializedCaches();

        if (!_caches.isEmpty() || !_initializing.isEmpty()) {
            throw new InitializingTaskException(Info, "There are {} initializing caches that have not yet started initializing and {} that have started but have not yet completed initializing", _caches.size(), _initializing.size());
        }
    }

    private void processUninitializedCaches() {
        final List<Initializing> uninitialized = new ArrayList<>();
        for (final Initializing cache : _caches) {
            if (!cache.isInitialized() && cache.canInitialize()) {
                _initializing.put(((XnatCache) cache).getCacheName(), cache.initialize());
            } else {
                uninitialized.add(cache);
            }
        }
        _caches.clear();
        _caches.addAll(uninitialized);
    }

    private void processInitializingCaches() {
        final List<String> completedKeys = new ArrayList<>();
        for (final String cache : _initializing.keySet()) {
            final Future<Boolean> future = _initializing.get(cache);
            if (future.isDone()) {
                completedKeys.add(cache);
                try {
                    final boolean result = future.get();
                    log.info("The initializing cache {} completed {}", cache, result ? "successfully" : "unsuccessfully");
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("The initializing cache " + cache + " encountered an error during initialization", e);
                }
            } else if (future.isCancelled()) {
                completedKeys.add(cache);
                log.warn("The initializing cache {} was cancelled before completing initialization", cache);
            }
        }
        for (final String completed : completedKeys) {
            _initializing.remove(completed);
        }
    }

    private final List<Initializing>           _caches       = new ArrayList<>();
    private final Map<String, Future<Boolean>> _initializing = new HashMap<>();
}
