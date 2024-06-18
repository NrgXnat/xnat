/*
 * core: org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.velocity.loaders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.servlet.XDATServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Tim Olsen &lt;tim@deck5consulting.com&gt;
 *         This custom implementation of the Velocity ResourceLoader will manage the loading of VM files from the file system
 *         OR the classpath. This allows VMs to be loaded from within JAR files but is still backwards compatible with the old
 *         file system structure.  Also, the loading enforces the templates/xnat-templates/xdat-templates/base-templates
 *         hierarchy.
 */
public class CustomClasspathResourceLoader extends ResourceLoader {
    public static final String       META_INF_RESOURCES = "META-INF/resources/";
    public static final List<String> TEMPLATE_PATHS     = ImmutableList.of("templates", "module-templates", "xnat-templates", "xdat-templates", "base-templates");

    public CustomClasspathResourceLoader() {
        super();

        synchronized (logger) {
            if (INSTANCE == null) {
                INSTANCE = this;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ExtendedProperties properties) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating customer classpath resource loader with extended properties: " + (properties == null ? "null" : properties.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        if (StringUtils.isEmpty(name)) {
            throw new ResourceNotFoundException("No template name provided");
        }

        if (name.contains("//")) {
            name = name.replace("//", "/");
        }

        // See if the resource is already in the cache.
        final Resource resource = getResource(name);

        // Just to be safe, check for null again.
        if (resource != null) {
            try {
                return resource.getInputStream();
            } catch (IOException e) {
                throw new ResourceNotFoundException(e);
            }
        }

        try {
            for (final String path : TEMPLATE_PATHS) {
                //iterate through potential sub-directories (templates, xnat-templates, etc) looking for a match.
                //ordering of the paths is significant as it enforces the VM overwriting model
                final String possible = Paths.get(path, name).toString();
                try {
                    final InputStream result = findMatch(name, possible);
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    logger.error("Got an error trying to find a match for " + name + " at path " + possible, e);
                }
            }

            // Check root (without sub directories... allowing one last location to plug in matches)
            final InputStream result = findMatch(name, name);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */
            throw new ResourceNotFoundException(e.getMessage());
        }

        throw new ResourceNotFoundException(String.format("CustomClasspathResourceLoader: cannot find resource %s", name));
    }

    /**
     * Looks for matching resource at the give possible path.  If its found, it caches the path and returns an InputStream.
     *
     * Returns null if no match is found.
     *
     * @param name     The name of the resource to find.
     * @param possible The path that may contain the resource.
     *
     * @return The input stream for the resource.
     */
    private InputStream findMatch(final String name, final String possible) {
        final File file = new File(XDATServlet.WEBAPP_ROOT, possible);
        if (file.exists()) {
            try {
                final FileSystemResource resource = new FileSystemResource(file);
                putResource(name, resource);
                return resource.getInputStream();
            } catch (FileNotFoundException e) {
                // This shouldn't happen because we check if it exists first, but just in case...
                logger.error("Couldn't find the file at location " + file.getAbsolutePath() + ", which is weird because I checked if it existed first.", e);
            } catch (IOException e) {
                logger.error("An error occurred trying to open the resource " + name + " at location " + file.getAbsolutePath(), e);
                return null;
            }
        }

        final URL found = getClass().getClassLoader().getResource(Paths.get(META_INF_RESOURCES, possible).toString());
        if (found != null) {
            try {
                final InputStream stream = found.openStream();
                if (stream != null) {
                    putResource(name, new UrlResource(found));
                    return stream;
                }
            } catch (IOException e) {
                logger.error("An error occurred trying to open the resource " + name + " at location " + found.toString(), e);
                return null;
            }
        }

        //check for file system file
        logger.debug("Didn't find the resource {} at possible location {}", name, possible);
        return null;
    }

    /**
     * Joins together multiple strings using the default separator character "/".  This will not duplicate the separator
     * if the joined strings already include them.
     *
     * @param elements The elements to join.
     *
     * @return The submitted elements joined by the separator.
     */
    @SafeVarargs
    public static <T extends String> String safeJoin(final T... elements) {
        return safeJoin('/', elements);
    }

    /**
     * Joins together multiple strings using the referenced separator.  However, it will not duplicate the separator if the joined strings already include them.
     *
     * @param separator The separator on which to join.
     * @param elements  The elements to join.
     *
     * @return The submitted elements joined by the separator.
     */
    @SafeVarargs
    public static <T extends String> String safeJoin(final Character separator, final T... elements) {
        final String converted = separator.toString();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i]);
            if ((i + 1) < elements.length) {
                if (!elements[i].endsWith(converted) && !elements[(i + 1)].startsWith(converted)) {
                    sb.append(separator);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Identifies all of the VM files in the specified directory
     * Adds META-INF/resources to the package.
     * Looks in templates, xnat-templates, xdat-templates, and base-templates
     *
     * @param dir The directory to search.
     *
     * @return The URLs of all Velocity templates located in the specified directory.
     */
    public static List<URL> findVMsByClasspathDirectory(final String dir) {
        final List<URL> matches = Lists.newArrayList();
        for (String folder : TEMPLATE_PATHS) {
            matches.addAll(findVMsByClasspathDirectory(folder, dir));
        }
        return matches;
    }

    /**
     * Find vm by classpath directory and file name.
     *
     * @param dir              the dir
     * @param templateFileName the template file name
     *
     * @return the list
     */
    public static List<URL> findVMByClasspathDirectoryAndFileName(final String dir, final String templateFileName) {
        final List<URL> matches = Lists.newArrayList();
        for (final String folder : TEMPLATE_PATHS) {
            for (final URL vmURL : findVMsByClasspathDirectory(folder, dir)) {
                final String vmFile = vmURL.getPath();
                final String vmFileName = vmFile.substring(vmFile.replace("\\", "/").lastIndexOf('/') + 1);
                if (vmFileName.equals(templateFileName)) {
                    matches.add(vmURL);
                }
            }
        }
        return matches;
    }

    /**
     * Identifies all of the VM files in the specified directory
     * Adds META-INF/resources to the package.
     * Looks in templates, xnat-templates, xdat-templates, and base-templates
     *
     * @param folder The root directory to search.
     * @param dir    The subdirectory to search.
     *
     * @return The URLs of all Velocity templates located in the specified directory.
     */
    public static List<URL> findVMsByClasspathDirectory(final String folder, final String dir) {
        final List<URL> matches = Lists.newArrayList();
        try {
            final Resource[] resources = new PathMatchingResourcePatternResolver((ClassLoader) null).getResources("classpath*:" + safeJoin(META_INF_RESOURCES, folder, dir, "*.vm"));
            for (final Resource resource : resources) {
                matches.add(resource.getURL());
            }
        } catch (IOException e) {
            //not sure if we care about this, I don't think so
            logger.error("", e);
        }
        return matches;
    }

    /**
     * Static convenience method for retrieving an InputStream outside of the normal Turbine context.
     *
     * @param resource The resource to load.
     *
     * @return The input stream for the requested resource.
     *
     * @throws ResourceNotFoundException When the requested resource can't be located.
     */
    public static InputStream getInputStream(final String resource) throws ResourceNotFoundException {
        // This shouldn't really happen: the loader gets created very early, for obvious reasons before we need to load resources.
        if (INSTANCE == null) {
            // But just in case, we'll call the constructor and initialize the static instance.
            new CustomClasspathResourceLoader();
        }
        return INSTANCE.getResourceStream(resource);
    }

    @Override
    public boolean isSourceModified(final org.apache.velocity.runtime.resource.Resource resource) {
        return resource.isSourceModified();
    }

    @Override
    public long getLastModified(final org.apache.velocity.runtime.resource.Resource resource) {
        return resource.getLastModified();
    }

    private synchronized Cache getCache() {
        if (cacheManager == null) {
            cacheManager = XDAT.getCacheManager();
        }
        return cacheManager != null ? cacheManager.getCache(RESOURCE_CACHE_NAME) : null;
    }

    private void putResource(final String name, final Resource resource) {
        final Cache cache = getCache();
        if (cache != null) {
            cache.put(name, resource);
        }
    }

    private Resource getResource(final String name) {
        final Cache cache = getCache();
        if (cache != null) {
            final Cache.ValueWrapper item = cache.get(name);
            if (item != null) {
                return (Resource) item.get();
            }
        }
        return null;
    }

    private static final Logger       logger              = LoggerFactory.getLogger(CustomClasspathResourceLoader.class);
    private static final String       RESOURCE_CACHE_NAME = "CustomClasspathResourceLoaderResourceCache";

    private static CustomClasspathResourceLoader INSTANCE;

    private CacheManager cacheManager;
}
