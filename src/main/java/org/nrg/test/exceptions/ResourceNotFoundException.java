package org.nrg.test.exceptions;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

/**
 * Indicates that the requested resource could not be located using the specified string, URI, or URL. 
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ResourceNotFoundException extends NrgServiceRuntimeException {
    public ResourceNotFoundException(final String path) {
        this(path, null);
    }
    
    public ResourceNotFoundException(final Path path) {
        this(path, null);
    }
    
    public ResourceNotFoundException(final File file) {
        this(file, null);
    }
    
    public ResourceNotFoundException(final URI uri) {
        this(uri, null);
    }
    
    public ResourceNotFoundException(final URL url) {
        this(url, null);
    }
    
    public ResourceNotFoundException(final String path, final String message) {
        super(getMessageForType(path, message));
        _resourcePath = path;
    }

    public ResourceNotFoundException(final Path path, final String message) {
        super(getMessageForType(path, message));
        _resourcePath = path;
    }

    public ResourceNotFoundException(final File file, final String message) {
        super(getMessageForType(file, message));
        _resourcePath = file;
    }

    public ResourceNotFoundException(final URI uri, final String message) {
        super(getMessageForType(uri, message));
        _resourcePath = uri;
    }

    public ResourceNotFoundException(final URL url, final String message) {
        super(getMessageForType(url, message));
        _resourcePath = url;
    }

    /**
     * Returns the original resource path object. This may be a string, Path, File, URI, or URL object.
     * @return The original resource path object.
     */
    public Object getResourcePath() {
        return _resourcePath;
    }

    private static String getMessageForType(final Object resourcePath, final String message) {
        final StringBuilder buffer = new StringBuilder("Could not find resource with ");
        buffer.append(resourcePath.getClass().getSimpleName()).append(" with address ");
        buffer.append(resourcePath.toString());
        if (StringUtils.isNotBlank(message)) {
            buffer.append(": ").append(message);
        }
        return buffer.toString();
    }

    private final Object _resourcePath;
}
