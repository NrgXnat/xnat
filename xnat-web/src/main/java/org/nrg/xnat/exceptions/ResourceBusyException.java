package org.nrg.xnat.exceptions;

import java.io.File;

/**
 * Thrown when a resource's catalog cannot be acquired within the configured
 * lock timeout. Signals to upstream handlers that the operation should be
 * retried later (typically mapped to HTTP 503 Service Unavailable).
 *
 * <p>Distinct from a hard failure: the underlying catalog and target files
 * are guaranteed to be in a consistent, unchanged state when this is thrown.
 */
@SuppressWarnings("serial")
public class ResourceBusyException extends Exception {

    private final File catalogFile;

    public ResourceBusyException(File catalogFile) {
        super(buildMessage(catalogFile));
        this.catalogFile = catalogFile;
    }

    public ResourceBusyException(File catalogFile, Throwable cause) {
        super(buildMessage(catalogFile), cause);
        this.catalogFile = catalogFile;
    }

    public File getCatalogFile() {
        return catalogFile;
    }

    private static String buildMessage(File catalogFile) {
        return "Resource catalog is busy: " + (catalogFile != null ? catalogFile.getAbsolutePath() : "<unknown>");
    }
}
