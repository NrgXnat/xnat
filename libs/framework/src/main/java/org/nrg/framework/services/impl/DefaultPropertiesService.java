/*
 * framework: org.nrg.framework.services.impl.DefaultPropertiesService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services.impl;

import org.apache.commons.io.FilenameUtils;
import org.nrg.framework.services.PropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.*;

@Service
public class DefaultPropertiesService implements PropertiesService, ApplicationContextAware {

    /**
     * Initializes the service after the context has been initialized.
     */
    @PostConstruct
    public void initialize() {
        List repositories;
        try {
            repositories = _context.getBean("propertiesRepositories", List.class);
        } catch (NoSuchBeanDefinitionException e) {
            handleNoRepositoriesState();
            return;
        }
        if (repositories == null || repositories.size() == 0) {
            handleNoRepositoriesState();
            return;
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Found repositories list containing " + repositories.size() + " items.");
        }
        _repositories = new ArrayList<>();
        for (Object repository : repositories) {
            final File file = new File(repository.toString());
            assert file.exists() : "The repository " + file.getAbsolutePath() + " does not exist!";
            if (_log.isDebugEnabled()) {
                _log.debug("Adding properties repository: " + file.getAbsolutePath());
            }
            _repositories.add(file);
        }
        refreshBundles();
    }

    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     * <p>Invoked after population of normal bean properties but before an init callback such
     * as {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
     * or a custom init-method. Invoked after {@link org.springframework.context.ResourceLoaderAware#setResourceLoader},
     * {@link org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher} and
     * {@link org.springframework.context.MessageSourceAware}, if applicable.
     *
     * @param context the ApplicationContext object to be used by this object
     * @throws org.springframework.context.ApplicationContextException
     *          in case of context initialization errors
     * @throws org.springframework.beans.BeansException
     *          if thrown by application context methods
     * @see org.springframework.beans.factory.BeanInitializationException
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    /**
     * Gets a list of the repositories configured for the properties service.
     * @return A list of file objects indicating the repository directories.
     */
    @Override
    public List<File> getRepositories() {
        return _repositories;
    }

    /**
     * Gets all of the current loaded bundles. Each bundle is identified by its
     * module and bundle namespace separated by a dot. If no module name is specified
     * in the bundle, the bundle name is used as the module name.
     * @return A map of the currently loaded bundles.
     */
    @Override
    public Map<String, Properties> getBundles() {
        return _bundles;
    }

    /**
     * Refreshes the bundles from the configured repositories.
     */
    @Override
    public void refreshBundles() {
        if (_repositories == null) {
            handleNoRepositoriesState();
            return;
        }
        _bundles = new HashMap<>();
        for (File repository : _repositories) {
            // If our repository doesn't exist from the default path and it's not absolute,
            // then maybe it's meant to be relative to a web app? Let's see!
            if (!repository.exists() && !repository.isAbsolute()) {
                // If we don't have a context we can work with, then just quit now.
                if (_context == null || !(_context instanceof XmlWebApplicationContext)) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Unable to resolve relative repository, no appropriate Spring Web context found: " + repository.getPath());
                    }
                    continue;
                }
                // So get the servlet context, because this is where we can find the web app folder.
                ServletContext context = ((XmlWebApplicationContext) _context).getServletContext();
                // If we don't have a servlet context, all is lost.
                if (context == null) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Unable to resolve relative repository, no appropriate Web application context found: " + repository.getPath());
                    }
                    continue;
                }
                File resolved = new File(context.getRealPath(repository.getPath()));
                if (!resolved.exists()) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Failed to resolve relative repository " + repository.getPath() + " to absolute path " + resolved.getAbsolutePath());
                    }
                    continue;
                }
                repository = resolved;
            }
            File[] propertyFiles = repository.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(".*\\.properties") || name.matches(".*\\.xml");
                }
            });
            if (propertyFiles != null && propertyFiles.length > 0) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Found " + propertyFiles.length + " properties bundles in repository: " + repository.getAbsolutePath());
                }
                for (File propertyFile : propertyFiles) {
                    try {
                        String extension = FilenameUtils.getExtension(propertyFile.getName());
                        Properties properties = new Properties();
                        if (extension.equals("properties")) {
                            properties.load(new FileReader(propertyFile));
                        } else {
                            properties.loadFromXML(new FileInputStream(propertyFile));
                        }
                        final String filename = FilenameUtils.removeExtension(propertyFile.getName());
                        String name;
                        if (properties.containsKey("module")) {
                            name = generateModulePropertiesKey(properties.getProperty("module"), filename);
                            if (_log.isDebugEnabled()) {
                                _log.debug("Found bundle ID from the bundle's module property definition: " + name);
                            }
                        } else {
                            name = generateModulePropertiesKey(filename, filename);
                            if (_log.isDebugEnabled()) {
                                _log.debug("Using default bundle ID from the bundle's file name: " + name);
                            }
                        }
                        _bundles.put(name, properties);
                    } catch (IOException ignored) {
                        // We know the file's there because we just found it.
                    }
                }
            } else if (_log.isDebugEnabled()) {
                _log.debug("No properties files were found at the repository location: " + repository.getAbsolutePath());
            }
        }
    }

    /**
     * Gets the bundle identified by the <b>bundle</b> parameter. This is used to
     * find bundles under the default module namespace, i.e. <b>bundle.bundle</b>.
     *
     * @param bundle    The bundle name.
     * @return The {@link java.util.Properties} object associated with the bundle key.
     */
    @Override
    public Properties getProperties(final String bundle) {
        return getProperties(bundle, bundle);
    }

    /**
     * Gets the bundle identified by the <b>module</b> and <b>bundle</b> parameters.
     * This is used to find bundles with an explicit module ID, i.e. <b>module.bundle</b>.
     *
     * @param module The module name.
     * @param bundle The bundle name.
     * @return The {@link java.util.Properties} object associated with the module/bundle key.
     */
    @Override
    public Properties getProperties(final String module, final String bundle) {
        if (getBundles() == null) {
            return null;
        }
        return getBundles().get(generateModulePropertiesKey(module, bundle));
    }

    private static void handleNoRepositoriesState() {
        _log.warn("No repositories configured for the properties service");
    }

    private static String generateModulePropertiesKey(String module, String properties) {
        return String.format("%s.%s", module, properties);
    }

    private static final Logger _log = LoggerFactory.getLogger(DefaultPropertiesService.class);
    private List<File> _repositories;
    private ApplicationContext _context;
    private Map<String, Properties> _bundles;
}
