package org.nrg.xdat.services;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Defines the interface for an initializing service or entity.
 */
public interface Initializing {
    /**
     * Indicates whether the initializing object is ready to be initialized.
     *
     * @return Returns true if the object is ready, false otherwise.
     */
    boolean canInitialize();

    /**
     * Starts the initialization process for the initializing object.
     *
     * @return The returned future indicates true if the object initialized successfully and false otherwise.
     */
    Future<Boolean> initialize();

    /**
     * Indicates whether the object has completed initialization.
     *
     * @return Returns true if the object is initialized, false otherwise.
     */
    boolean isInitialized();

    /**
     * Returns a message indicating the current status of initialization. The contents of this
     * string are completely dependent on the implementation.
     */
    Map<String, String> getInitializationStatus();
}
