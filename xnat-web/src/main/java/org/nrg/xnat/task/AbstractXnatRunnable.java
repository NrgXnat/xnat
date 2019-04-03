package org.nrg.xnat.task;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.services.logging.LoggingService;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService;

/**
 * Provides the functionality for the {@link #run()} method, including boilerplate and
 * common functionality like logging performance metrics. Subclasses should provide task-specific
 * functionality in the {@link #runTask()} method.
 */
@Getter
@Setter
@Accessors(prefix = "_")
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
        start();

        try {
            runTask();
        } finally {
            finish();
        }
    }

    private void start() {
        if (getLoggingService() != null) {
            getLoggingService().start(this);
        } else {
            log.warn("This task is starting now, but the logging service is not yet available");
        }
    }

    private void finish() {
        if (getLoggingService() != null) {
            getLoggingService().finish(this);
        } else {
            log.warn("This task is finishing now, but the logging service is not yet available");
        }
    }

    private LoggingService getLoggingService() {
        return DefaultLoggingService.getInstance();
    }
}
