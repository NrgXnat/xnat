/*
 * org.nrg.framework.status.LoggerStatusReporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/2/13 12:20 PM
 */
package org.nrg.framework.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerStatusReporter implements StatusListenerI {
    private final Logger        logger;
    private       StatusMessage lastMessage;

    public LoggerStatusReporter(final Logger logger) {
        this.logger = logger;
    }

    public LoggerStatusReporter(final Class<?> source) {
        this(LoggerFactory.getLogger(source));
    }

    /* (non-Javadoc)
     * @see org.nrg.StatusListener#notify(org.nrg.StatusMessage)
     */
    public void notify(final StatusMessage message) {
        this.lastMessage = message;
        switch (message.getStatus()) {
            case PROCESSING:
            case COMPLETED:
                logger.info("{}: {}", message.getSource(), message.getMessage());
                break;
            case WARNING:
                logger.warn("{}: {}", message.getSource(), message.getMessage());
                break;
            case FAILED:
                logger.error("{}: {}", message.getSource(), message.getMessage());
                break;
        }
    }

    public StatusMessage getLastMessage() {
        return lastMessage;
    }
}
