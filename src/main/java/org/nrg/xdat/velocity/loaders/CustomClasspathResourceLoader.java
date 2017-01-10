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
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.nrg.xdat.servlet.XDATServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author Tim Olsen &lt;tim@deck5consulting.com&gt;
 * This custom implementation of the Velocity ResourceLoader will manage the loading of VM files from the file system
 * OR the classpath. This allows VMs to be loaded from within JAR files but is still backwards compatible with the old
 * file system structure.  Also, the loading enforces the templates/xnat-templates/xdat-templates/base-templates
 * hierarchy.
 */
public class CustomClasspathResourceLoader extends ResourceLoader {
    private static final Logger logger = LoggerFactory.getLogger(CustomClasspathResourceLoader.class);

    public static final String META_INF_RESOURCES = "META-INF/resources/";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init(ExtendedProperties arg0) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating customer classpath resource loader with extended properties: " + (arg0 == null ? "null" : arg0.toString()));
        }
    }

    public static final List<String> paths = ImmutableList.of("templates", "module-templates", "xnat-templates", "xdat-templates", "base-templates");
    private static Map<String, String> templatePaths = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        InputStream result;

        if (StringUtils.isEmpty(name)) {
            throw new ResourceNotFoundException("No template name provided");
        }

        if (name.contains("//")) {
            name = name.replace("//", "/");
        }

        try {
            final ClassLoader classLoader = this.getClass().getClassLoader();

            //VMs can be located in a lot of places.  file system vs classpath, xnat-templates vs templates
            //for improved efficiency, we cache the location were we last found a template, that way the loader doesn't have to look for it each time.
            final String known = templatePaths.get(name);

            if (known != null) {
                try {
                    if (known.startsWith("f:")) {
                        //VMs found on the file system, will have f: on the start of their path.
                        try {
                            result = new BufferedInputStream(new FileInputStream(new File(XDATServlet.WEBAPP_ROOT, known.substring(2))));
                            //noinspection ConstantConditions
                            if (result != null) {
                                return result;
                            } else {
                                throw new ResourceNotFoundException(known);
                            }
                        } catch (Exception e) {
                            throw new ResourceNotFoundException(known);
                        }
                    } else if (known.startsWith("c:")) {
                        //VMs fond on the classpath, will have c: on the start of their path.
                        result = classLoader.getResourceAsStream(safeJoin(META_INF_RESOURCES, known.substring(2)));
                        if (result != null) {
                            return result;
                        } else {
                            throw new ResourceNotFoundException(known);
                        }
                    } else {
                        throw new ResourceNotFoundException(name);
                    }
                } catch (Exception e) {
                    //ignore
                }
            }

            for (final String path : paths) {
                //iterate through potential sub-directories (templates, xnat-templates, etc) looking for a match.
                //ordering of the paths is significant as it enforces the VM overwriting model
                final String possible = safeJoin(path, name);
                try {
                    result = findMatch(name, possible);
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    logger.error("Got an error trying to find a match for " + name + " at path " + possible, e);
                }
            }

            //check root (without sub directories... allowing one last location to plug in matches)
            result = findMatch(name, name);
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
     * @return The input stream for the resource.
     */
    private InputStream findMatch(String name, String possible) {
        try {
            File f = new File(XDATServlet.WEBAPP_ROOT, possible);
            if (f.exists()) {
                InputStream result = new BufferedInputStream(new FileInputStream(f));
                templatePaths.put(name.intern(), ("f:" + possible).intern());
                return result;
            }
        } catch (FileNotFoundException e) {
            //ignore.  shouldn't happen because we check if it exists first.
        }

        final InputStream result = this.getClass().getClassLoader().getResourceAsStream(safeJoin(META_INF_RESOURCES, possible));
        if (result != null) {
            //once we find a match, lets cache the name of it
            templatePaths.put(name.intern(), ("c:" + possible).intern());
        } else {
            //check for file system file
            logger.debug("Didn't find the requested resource at " + possible);
        }

        return result;
    }

    /**
     * Joins together multiple strings using the default separator character "/".  This will not duplicate the separator
     * if the joined strings already include them.
     *
     * @param elements  The elements to join.
     *
     * @return The submitted elements joined by the separator.
     */
    @SafeVarargs
    public static <T extends String> String safeJoin(T... elements) {
        return safeJoin('/', elements);
    }

    /**
     * Joins together multiple strings using the referenced separator.  However, it will not duplicate the separator if the joined strings already include them.
     *
     * @param separator The separator on which to join.
     * @param elements  The elements to join.
     * @return The submitted elements joined by the separator.
     */
    @SafeVarargs
    public static <T extends String> String safeJoin(final Character separator, T... elements) {
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
     * @return The URLs of all Velocity templates located in the specified directory.
     */
    public static List<URL> findVMsByClasspathDirectory(String dir) {
        final List<URL> matches = Lists.newArrayList();
        for (String folder : paths) {
            matches.addAll(findVMsByClasspathDirectory(folder, dir));
        }
        return matches;
    }

	/**
	 * Find vm by classpath directory and file name.
	 *
	 * @param dir the dir
	 * @param templateFileName the template file name
	 * @return the list
	 */
	public static List<URL> findVMByClasspathDirectoryAndFileName(String dir, String templateFileName) {
        final List<URL> matches = Lists.newArrayList();
        for (final String folder : paths) {
            for (final URL vmURL : findVMsByClasspathDirectory(folder, dir)) {
            	final String vmFile = vmURL.getPath();
                final String vmFileName = vmFile.substring(vmFile.replace("\\",  "/").lastIndexOf('/')+1);
                if (vmFileName.equals(templateFileName)) {
                	matches.add(vmURL);
                }
            };
        }
        return matches;
	}

    /**
     * Identifies all of the VM files in the specified directory
     * Adds META-INF/resources to the package.
     * Looks in templates, xnat-templates, xdat-templates, and base-templates
     *
     * @param folder    The root directory to search.
     * @param dir       The subdirectory to search.
     * @return The URLs of all Velocity templates located in the specified directory.
     */
    public static List<URL> findVMsByClasspathDirectory(final String folder, String dir) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        assert loader != null;

        final List<URL> matches = Lists.newArrayList();
        try {
            final org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver((ClassLoader) null).getResources("classpath*:" + safeJoin(META_INF_RESOURCES, folder, dir, "*.vm"));
            for (org.springframework.core.io.Resource r : resources) {
                matches.add(r.getURL());
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
     * @return The input stream for the requested resource.
     * @throws ResourceNotFoundException
     */
    public static InputStream getInputStream(String resource) throws ResourceNotFoundException {
        CustomClasspathResourceLoader loader = new CustomClasspathResourceLoader();
        return loader.getResourceStream(resource);
    }

    @Override
    public boolean isSourceModified(Resource arg0) {
        return false;
    }

    @Override
    public long getLastModified(Resource arg0) {
        return 0;
    }
}
