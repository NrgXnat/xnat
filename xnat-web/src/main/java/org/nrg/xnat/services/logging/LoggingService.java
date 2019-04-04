package org.nrg.xnat.services.logging;

import org.nrg.xapi.exceptions.NotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    /**
     * Returns a list of the resources that configured the logging system.
     *
     * @return A list of all logging configuration resources.
     */
    List<String> getConfigurationResources();

    /**
     * Returns the requested resource configured the logging system.
     *
     * @return The contents of the requested logging configuration resource.
     */
    String getConfigurationResource(final String resource) throws IOException, NotFoundException;

    /**
     * Returns a list of the loggers and appenders configured in the primary logging configuration.
     *
     * @return A map of loggers and appenders configured in the primary logging configuration.
     */
    Map<String, List<String>> getPrimaryElements();
}
