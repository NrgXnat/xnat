/*
 * framework: org.nrg.framework.services.PropertiesService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface PropertiesService extends NrgService {

    public static String SERVICE_NAME = PropertiesService.class.getSimpleName();

    /**
     * Gets a list of the repositories configured for the properties service.
     * @return A list of file objects indicating the repository directories.
     */
    abstract public List<File> getRepositories();

    /**
     * Gets all of the current loaded bundles. Each bundle is identified by its
     * module and bundle namespace separated by a dot. If no module name is specified
     * in the bundle, the bundle name is used as the module name.
     * @return A map of the currently loaded bundles.
     */
    abstract public Map<String,Properties> getBundles();

    /**
     * Refreshes the bundles from the configured repositories.
     */
    abstract public void refreshBundles();

    /**
     * Gets the bundle identified by the <b>bundle</b> parameter. This is used to
     * find bundles under the default module namespace, i.e. <b>bundle.bundle</b>.
     * @param bundle    The bundle name.
     * @return The {@link java.util.Properties} object associated with the bundle key.
     */
    abstract public Properties getProperties(String bundle);

    /**
     * Gets the bundle identified by the <b>module</b> and <b>bundle</b> parameters.
     * This is used to find bundles with an explicit module ID, i.e. <b>module.bundle</b>.
     * @param module    The module name.
     * @param bundle    The bundle name.
     * @return The {@link java.util.Properties} object associated with the module/bundle key.
     */
    abstract public Properties getProperties(String module, String bundle);
}
