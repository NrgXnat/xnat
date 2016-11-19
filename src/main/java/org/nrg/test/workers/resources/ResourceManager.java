package org.nrg.test.workers.resources;

import org.nrg.test.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;

/**
 *
 */
public class ResourceManager {
    public static ResourceManager getInstance() {
        if (_instance == null) {
            synchronized (MUTEX) {
                _instance = new ResourceManager();
            }
        }
        return _instance;
    }

    private ResourceManager() {
        if (_instance != null) {
            // This should never happen because the constructor is private, but in case someone tries to strong-arm it through reflection...
            throw new RuntimeException("An instance of ResourceManager is already initialized. You should call the ResourceManager.getInstance() method instead.");
        }
        _log.info("Resource manager instance initialized.");
    }

    public File getTestResourceFile(final String resourcePath) throws ResourceNotFoundException {
        final URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new ResourceNotFoundException("Unable to find the resource " + resourcePath);
        }
        return getTestResourceFile(resource);
    }

    public File getTestResourceFile(final URL resource) throws ResourceNotFoundException {
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            try {
                switch (resource.getProtocol()) {
                    case "file":
                        final String path = URLDecoder.decode(resource.getFile(), Charset.defaultCharset().name());
                        return Paths.get(path).toFile();
                    default:
                        throw new ResourceNotFoundException("The protocol for the URL " + resource.toString() + " is not yet supported.");
                }
            } catch (UnsupportedEncodingException ignored) {
                // We're just not going to deal with this.
                return null;
            }
        }
    }

    public InputStream getTestResourceInputStream(final String resourcePath) throws ResourceNotFoundException {
        final URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new ResourceNotFoundException("Unable to find the resource " + resourcePath);
        }
        return getTestResourceInputStream(resource);
    }

    public InputStream getTestResourceInputStream(final URL resource) throws ResourceNotFoundException {
        try {
            return resource.openStream();
        } catch (IOException e) {
            throw new ResourceNotFoundException(resource, e.getMessage());
        }
    }

    private static final Logger _log  = LoggerFactory.getLogger(ResourceManager.class);
    private static final Object MUTEX = new Object();

    private static ResourceManager _instance;
}
