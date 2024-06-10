/*
 * web: org.nrg.xnat.services.logging.LoggingService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
    Map<String, String> getConfigurationResources();

    /**
     * Returns the requested resource configured the logging system. The resource is identified by
     * the ID or key in the map returned by {@link #getConfigurationResources()}.
     *
     * @param resourceId The ID of the resource to retrieve.
     *
     * @return The contents of the requested logging configuration resource.
     */
    String getConfigurationResource(final String resourceId) throws IOException, NotFoundException;

    /**
     * Returns a list of the loggers and appenders configured in the primary logging configuration.
     *
     * @return A map of loggers and appenders configured in the primary logging configuration.
     */
    Map<String, List<String>> getPrimaryElements();
}
