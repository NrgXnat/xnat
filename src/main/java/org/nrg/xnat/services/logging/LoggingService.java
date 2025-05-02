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
import org.nrg.xnat.services.logging.impl.AppenderHierarchy;
import org.slf4j.event.Level;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LoggingService {
    String ADDITIVITY    = "additivity";
    String APPENDER      = "appender";
    String APPENDER_REF  = "appender-ref";
    String APPENDERS     = "appenders";
    String CATEGORY      = "category";
    String CONFIGURATION = "configuration";
    String CLASS         = "class";
    String FILE          = "file";
    String LEVEL         = "level";
    String LOGGER        = "logger";
    String LOGGERS       = "loggers";
    String NAME          = "name";
    String PRIMARY       = "primary";
    String REF           = "ref";
    String ROOT          = "ROOT";

    /**
     * Resets the logging configuration from the master logging configuration and any extended logging configurations
     * specified by plugins, etc.
     *
     * @return The URLs or paths to the logging configurations that were loaded on reset.
     */
    List<String> reset();

    /**
     * Logs the launch of the runnable task and starts a timer.
     *
     * @param runnable The runnable instance.
     * @param <T>      The type of the runnable instance.
     */
    <T extends Runnable> void start(T runnable);

    /**
     * Updates the status of a runnable task and creates a log entry that includes the submitted message.
     *
     * @param runnable   The runnable instance.
     * @param message    The message to display.
     * @param parameters The parameters to include in the message.
     * @param <T>        The type of the runnable instance.
     */
    <T extends Runnable> void update(T runnable, String message, Object... parameters);

    /**
     * Logs the completion of the runnable task, stops the corresponding timer created in {@link #start(Runnable)},
     * and notes the elapsed time for task completion.
     *
     * @param runnable The runnable instance.
     * @param <T>      The type of the runnable instance.
     */
    <T extends Runnable> void finish(T runnable);

    /**
     * Returns the default folder for writing logs.
     *
     * @return The default logging folder.
     */
    Path getLogsFolder();

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
     * Returns a list of the loggers and appenders configured in the primary and plugin logging configurations. These are considered
     * "primary" elements because they are the ones that are loaded from XNAT's own logging configurations. Other loggers and appenders
     * may be configured in other ways (e.g., programmatically or through other libraries), but this method focuses on the ones that are
     * defined by XNAT and its plugins.
     *
     * @return A map of loggers and appenders configured in the primary logging and plugin logging configurations.
     */
    Map<String, Set<String>> getPrimaryElements();

    /**
     * Returns a list of appenders with their loggers and current logging levels.
     *
     * @return A list of appenders with their loggers and current logging levels.
     */
    List<AppenderHierarchy> getAppenders();

    /**
     * Returns the specified appender with its loggers and current logging levels.
     *
     * @return The specified appender with its loggers and current logging levels.
     *
     * @throws NotFoundException If the specified appender does not exist.
     */
    AppenderHierarchy getAppender(final String appenderName) throws NotFoundException;

    /**
     * Returns a map of all loggers and their current logging levels.
     *
     * @return A map of all loggers and their current logging levels.
     */
    Map<String, Level> getLoggerLevels();

    /**
     * Returns the current logging level for the specified logger.
     *
     * @param loggerName The name of the logger for which to retrieve the logging level.
     *
     * @return The current logging level for the specified logger.
     *
     * @throws NotFoundException If the specified logger does not exist.
     */
    Level getLoggerLevel(String loggerName) throws NotFoundException;

    /**
     * Sets the logging level for the specified logger.
     *
     * @param loggerName The name of the logger for which to set the logging level.
     * @param level      The new logging level to set for the specified logger.
     *
     * @return The previous logging level for the specified logger.
     *
     * @throws NotFoundException If the specified logger does not exist.
     */
    Level setLoggerLevel(String loggerName, Level level) throws NotFoundException;

    /**
     * Returns a map of all loggers and the path to the current log file. Note that not all loggers necessarily have a log file, e.g.
     * a logger that only routes to a console appender won't have a log file. These loggers will be omitted from this map. Additionally,
     * some loggers may have multiple appenders, each with its own log file. This method returns the path to the log file for the first
     * file-based appender in each logger's list of appenders.
     *
     * @return A map of all loggers with at least one file-based appender and the path to the current log file.
     */
    Map<String, Path> getLoggerPaths();

    /**
     * Returns the path to the current log file for the indicated logger. Note that not all loggers necessarily have a log file, e.g.
     * a logger that only routes to a console appender won't have a log file. This method will return null for those loggers. Additionally,
     * some loggers may have multiple appenders, each with its own log file. This method returns the path to the log file for the first
     * file-based appender in each logger's list of appenders.
     *
     * @param loggerName The name of the logger for which to retrieve the log path.
     *
     * @return The path to the current log file for the indicated logger
     *
     * @throws NotFoundException If the specified logger does not exist.
     */
    Path getLoggerPath(String loggerName) throws NotFoundException;
}
