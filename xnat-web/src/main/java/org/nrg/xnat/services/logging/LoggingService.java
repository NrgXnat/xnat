package org.nrg.xnat.services.logging;

// import java.util.Properties;

import java.util.List;

public interface LoggingService {
    /**
     * Resets the logging configuration from the master logging configuration and any extended logging configurations
     * specified by plugins, etc.
     *
     * @return The URLs or paths to the logging configurations that were loaded on reset.
     */
    List<String> reset();

    <T extends Runnable> void start(T runnable);

    <T extends Runnable> void update(T runnable, String message, Object... parameters);

    <T extends Runnable> void finish(T runnable);
}
