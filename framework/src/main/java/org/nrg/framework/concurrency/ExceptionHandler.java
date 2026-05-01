/*
 * framework: org.nrg.framework.concurrency.ExceptionHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.concurrency;

import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Passed to instances of {@link LoggingThreadPoolExecutor} to customize how particular exceptions are logged.
 */
public interface ExceptionHandler {
    /**
     * Defines the default logging level for handled exceptions.
     */
    Level DEFAULT_LEVEL = Level.INFO;

    /**
     * Indicates whether this exception handler instance can handle the given throwable instance.
     *
     * @param throwable The throwable instance to evaluate.
     *
     * @return Returns <b>true</b> if this instance can handle the given throwable instance.
     */
    boolean handles(final Throwable throwable);

    /**
     * Sets the logging level to be used for handled exceptions. By default this is set to {@link #DEFAULT_LEVEL}.
     *
     * @param level The logging level to be used for handled exceptions.
     */
    void setHandledLogLevel(final Level level);

    /**
     * Actually handles the given throwable instance. This will replace any logging or other handling by the {@link
     * LoggingThreadPoolExecutor} instance.
     *
     * @param throwable The throwable to be handled.
     */
    void handle(final Throwable throwable);

    /**
     * Actually handles the given throwable instance. This will replace any logging or other handling by the {@link
     * LoggingThreadPoolExecutor} instance. The logger allows the exception handler to use the logging as configured for
     * the code that actually generated the exception rather than the namespace for the handler implementation.
     *
     * @param throwable The throwable to be handled.
     * @param logger    The logger to use for logging messages.
     */
    void handle(final Throwable throwable, final Logger logger);
}
