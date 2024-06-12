/*
 * framework: org.nrg.framework.concurrency.AbstractExceptionHandler
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
 * Provides common functions for exception handlers.
 */
public abstract class AbstractExceptionHandler implements ExceptionHandler {
    @Override
    public abstract boolean handles(final Throwable throwable);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHandledLogLevel(final Level level) {
        _level = level;
    }

    /**
     * This default implementation calls the implementing class's {@link #handle(Throwable, Logger)} method, passing
     * <b>null</b> for the logger parameter.
     * 
     * @param throwable The throwable to be handled.
     */
    @Override
    public void handle(final Throwable throwable) {
        handle(throwable, null);
    }

    @Override
    public abstract void handle(final Throwable throwable, final Logger logger);

    /**
     * Logs the message through the logger with the logging level configured through {@link #setHandledLogLevel(Level)}.
     *
     * @param logger  The logger to use for logging messages.
     * @param message The message to be logged.
     */
    protected void log(final Logger logger, final String message) {
        switch (_level) {
            case TRACE:
                logger.trace(message);
                break;

            case DEBUG:
                logger.debug(message);
                break;

            case INFO:
                logger.info(message);
                break;

            case WARN:
                logger.warn(message);
                break;

            case ERROR:
                logger.error(message);
                break;
        }
    }
    
    private Level _level = DEFAULT_LEVEL;
}
