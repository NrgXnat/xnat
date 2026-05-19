package org.nrg.xnat.web.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.File;
import java.io.Serial;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CrossProjectResourceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2396024231218104284L;

    private static final String TEMPLATE = "User %s attempted to access a catalog entry %s that resolves to a file outside of the archive space for the project %s.\nCatalog path:  %s\nCatalog entry: %s";

    public CrossProjectResourceException(final String username, final String resourceName, final String resourceProject, final String catalogFile, final String resourcePath) {
        super(formatMessage(username, resourceName, resourceProject, catalogFile, resourcePath));
    }

    private static String formatMessage(final String username, final String resourceName, final String resourceProject, final String catalogFile, final String resourcePath) {
        return String.format(TEMPLATE, username, resourceName, resourceProject, catalogFile, resourcePath);
    }
}
