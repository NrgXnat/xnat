package org.nrg.xnat.services.logging;

import java.util.Properties;

public interface LoggingService {
    /**
     * Resets the logging configuration from the master log4j properties and any extended log4j configurations
     * that were located in plugins, etc.
     *
     * @return The properties that were used when resetting the logging configuration.
     */
    Properties reset();
}
