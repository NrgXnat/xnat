/*
 * framework: org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The aggregated annotation session factory bean enhances the Spring {@link LocalSessionFactoryBean} class by adding
 * the ability to find entity packages more dynamically. By default this implementation finds all entity packages:
 *
 * <ul>
 * <li> Listed in files located in files named "*-entity-packages.txt" in any subfolder of the resource path
 * "META-INF/xnat/entities" (you can override this by calling the {@link
 * #AggregatedAnnotationSessionFactoryBean(String...)} constructor and specifying other resource paths</li>
 * <li> Specified in the {@link XnatPlugin#entityPackages()} attribute of XNAT plugins on the class path</li>
 * <li> All packages specified in {@link HibernateEntityPackageList} beans in a Spring application context</li>
 * </ul>
 */
public class AggregatedAnnotationSessionFactoryBean extends LocalSessionFactoryBean {
    /**
     * Creates the session factory bean searching the default resource path.
     */
    public AggregatedAnnotationSessionFactoryBean() {
        this("META-INF/xnat/entities/**/*-entity-packages.txt");
    }

    /**
     * Creates the session factory bean searching all resource paths specified in the <b>resourcePaths</b> parameter.
     * Each string in the array should be an Ant-style classpath pattern (the "classpath*:" portion is not required).
     *
     * @param resourcePaths Indicates the paths to be searched.
     */
    public AggregatedAnnotationSessionFactoryBean(final String... resourcePaths) {
        this(null, resourcePaths);
    }

    /**
     * Creates the session factory bean searching all resource paths specified in the <b>resourcePaths</b> parameter.
     * Each string in the array should be an Ant-style classpath pattern (the "classpath*:" portion is not required).
     *
     * @param pluginBeanManager The instance of the plugin bean manager to configure.
     * @param resourcePaths     Indicates the paths to be searched.
     */
    public AggregatedAnnotationSessionFactoryBean(final XnatPluginBeanManager pluginBeanManager, final String... resourcePaths) {
        _pluginBeanManager = pluginBeanManager;
        _packagesToScan.addAll(getXnatEntityPackages(resourcePaths));
        super.setPackagesToScan(_packagesToScan.toArray(new String[0]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPackagesToScan(final String[] packagesToScan) {
        _packagesToScan.addAll(Arrays.asList(packagesToScan));
        super.setPackagesToScan(_packagesToScan.toArray(new String[0]));
    }

    /**
     * Sets all the {@link HibernateEntityPackageList} beans that should be included in the
     *
     * @param packageLists All the available {@link HibernateEntityPackageList} beans in the current context.
     */
    public void setEntityPackageLists(final List<HibernateEntityPackageList> packageLists) {
        if (packageLists != null) {
            for (final HibernateEntityPackageList list : packageLists) {
                _packagesToScan.addAll(list);
            }
            setPackagesToScan(_packagesToScan.toArray(new String[0]));
        }
    }

    /**
     * Sets an instance of the {@link XnatPluginBeanManager} to allow retrieving any entity packages specified in the
     * {@link XnatPlugin} annotations.
     *
     * @param pluginBeanManager The instance of the plugin bean manager to set.
     */
    @SuppressWarnings("unused")
    public void setPluginBeanManager(final XnatPluginBeanManager pluginBeanManager) {
        _pluginBeanManager = pluginBeanManager;
    }

    /**
     * Finds all XNAT entity packages defined in files matching each of the patterns in the specified resource paths.
     *
     * @param resourcePaths The resource paths to search, in the form of Ant-style classpath patterns.
     *
     * @return All entity packages found in all resources found.
     */
    private Set<String> getXnatEntityPackages(final String[] resourcePaths) {
        final Set<String> packages = new HashSet<>();
        for (final String resourcePath : resourcePaths) {
            if (_log.isDebugEnabled()) {
                _log.debug("Searching for entity package definitions using the resource path: {}", resourcePath);
            }
            try {
                for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:" + resourcePath)) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Processing entity packages from the resource: {}", resource.getFilename());
                    }
                    try (final InputStream input = resource.getInputStream()) {
                        packages.addAll(IOUtils.readLines(input, "UTF-8"));
                    }
                }
            } catch (IOException e) {
                _log.error("An error occurred trying to locate resources on the path: classpath*:" + resourcePath, e);
            }
        }
        if (_pluginBeanManager != null) {
            for (final String pluginId : _pluginBeanManager.getPluginIds()) {
                final XnatPluginBean plugin = _pluginBeanManager.getPlugin(pluginId);
                if (_log.isDebugEnabled()) {
                    _log.debug("Processing entity packages from plugin {}: {}", plugin.getId(), Joiner.on(", ").join(plugin.getEntityPackages()));
                }
                packages.addAll(plugin.getEntityPackages());
            }
        }
        return packages;
    }

    private static final Logger _log = LoggerFactory.getLogger(AggregatedAnnotationSessionFactoryBean.class);

    private final Set<String> _packagesToScan = new HashSet<>();
    private XnatPluginBeanManager _pluginBeanManager;
}
