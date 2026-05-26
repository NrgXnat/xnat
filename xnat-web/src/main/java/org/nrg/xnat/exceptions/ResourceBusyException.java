package org.nrg.xnat.exceptions;

import java.io.File;

/** Thrown when a resource's catalog lock cannot be acquired in time. Recommended HTTP mapping: 503. */
@SuppressWarnings("serial")
public class ResourceBusyException extends Exception {

    private final File catalogFile;

    public ResourceBusyException(File catalogFile, Throwable cause) {
        super("Resource catalog is busy: " + (catalogFile != null ? catalogFile.getAbsolutePath() : "<unknown>"), cause);
        this.catalogFile = catalogFile;
    }

    public File getCatalogFile() {
        return catalogFile;
    }
}
