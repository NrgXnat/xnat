package org.nrg.xnat.event.listeners;

import org.nrg.framework.status.StatusListenerI;
import org.nrg.xnat.archive.Operation;

import java.util.List;

public interface ArchiveOperationListener extends StatusListenerI {
    /**
     * Returns an ID that identifies the listener by its properties.
     *
     * @return An ID based on the listener properties.
     */
    String getArchiveOperationId();

    /**
     * Returns the operations about which the listener wants to be notified.
     *
     * @return The operations to listen for.
     */
    List<Operation> getOperations();

    /**
     * Returns the project for the events about which the listener wants to be notified.
     *
     * @return The project to listen for.
     */
    String getProject();

    /**
     * Returns the timestamp for the events about which the listener wants to be notified.
     *
     * @return The timestamp to listen for.
     */
    String getTimestamp();

    /**
     * Returns the session for the events about which the listener wants to be notified.
     *
     * @return The session to listen for.
     */
    String getSession();
}
