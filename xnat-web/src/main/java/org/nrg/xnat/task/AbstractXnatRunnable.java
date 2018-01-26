package org.nrg.xnat.task;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService;

/**
 * Provides the functionality for the {@link #run()} method, including boilerplate and
 * common functionality like logging performance metrics. Subclasses should provide task-specific
 * functionality in the {@link #runTask()} method.
 */
@Slf4j
public abstract class AbstractXnatRunnable implements Runnable {
    /**
     * This is where subclasses should implement their specific functionality.
     */
    protected abstract void runTask();

    /**
     * Creates log entries on start and completion and calls the subclass's {@link #runTask()} method.
     */
    @Override
    public void run() {
        DefaultLoggingService.start(this);

        try {
            runTask();
        } finally {
            DefaultLoggingService.finish(this);
        }
    }
}
