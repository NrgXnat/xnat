package org.nrg.xnat.helpers.prearchive.handlers;

/**
 * Defines the interface for a class that can handle a particular prearchive operation.
 */
public interface PrearchiveOperationHandler {
    /**
     * Executes the prearchive operation.
     *
     * @throws Exception When an unrecoverable error occurs during the prearchive operation.
     */
    void execute() throws Exception;
}
