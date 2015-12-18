/**
 * SiteConfigurationService
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 * <p/>
 * Released under the Simplified BSD License
 * <p/>
 * Created on 9/18/12 by rherri01
 */
package org.nrg.config.services;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.services.NrgService;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public interface SiteConfigurationService extends NrgService {

    /**
     * Initialized the site configuration from whatever persistent stores the service implementation uses.
     */
    void initSiteConfiguration() throws SiteConfigurationException;

    /**
     * Sets the {@link #getConfigFilesLocationsRoot() configuration file root} to the submitted location,  {@link
     * #resetSiteConfiguration() clears all cached properties}, and {@link #initSiteConfiguration() reloads properties
     * from any properties files} found in the submitted list.
     * @param configFilesLocationsRoot    The root location to search for configuration files.
     * @return The resulting site configuration properties.
     */
    Properties updateSiteConfiguration(final String configFilesLocationsRoot) throws SiteConfigurationException;

    /**
     * Sets the {@link #getConfigFilesLocations() list of configuration file locations} to the submitted list, {@link
     * #resetSiteConfiguration() clears all cached properties}, and {@link #initSiteConfiguration() reloads properties
     * from any properties files} found in the submitted list.
     * @param configFilesLocations    The list of locations where configuration files can be found.
     * @return The resulting site configuration properties.
     */
    Properties updateSiteConfiguration(final List<String> configFilesLocations) throws SiteConfigurationException;

    /**
     * Sets the {@link #getConfigFilesLocationsRoot() configuration file root} to the submitted location, sets the
     * {@link #getConfigFilesLocations() list of configuration file locations} to the submitted list, {@link
     * #resetSiteConfiguration() clears all cached properties}, and {@link #initSiteConfiguration() reloads properties
     * from any properties files} found in the submitted list.
     * @param configFilesLocationsRoot    The root location to search for configuration files.
     * @param configFilesLocations        The list of locations where configuration files can be found.
     * @return The resulting site configuration properties.
     */
    Properties updateSiteConfiguration(final String configFilesLocationsRoot, final List<String> configFilesLocations) throws SiteConfigurationException;

    /**
     * Resets the site configuration. This returns the service to its uninitialized state, discarding any cached or
     * persistent data. The site can then be {@link #initSiteConfiguration() initialized} again.
     */
    void resetSiteConfiguration();

    /**
     * Gets the site configuration as a Java {@link java.util.Properties} object.
     * @return The initialized Java {@link java.util.Properties} object.
     * @throws SiteConfigurationException Thrown when an error occurs resolving or accessing the configuration service.
     */
    Properties getSiteConfiguration() throws SiteConfigurationException;

    /**
     * Gets the value of the indicated property from the site configuration.
     * @param property    The name of the property to be retrieved.
     * @return The value of the property.
     * @throws SiteConfigurationException
     */
    String getSiteConfigurationProperty(String property) throws SiteConfigurationException;

    /**
     * Sets the value of the indicated property to the submitted value.
     * @param property    The name of the property to be set.
     * @param value       The value to set for the property.
     * @throws SiteConfigurationException
     */
    void setSiteConfigurationProperty(String username, String property, String value) throws SiteConfigurationException;

    /**
     * Gets the boolean value of the indicated property. If the property isn't found, the indicated default value is
     * returned instead.
     * @param property    The name of the property to be set.
     * @param _default    The default value to be returned if the property isn't found.
     * @return The boolean value requested.
     */
    boolean getBoolSiteConfigurationProperty(final String property, final boolean _default);

    /**
     * Gets the list of locations (relative to the {@link #getConfigFilesLocationsRoot() file location root} where
     * config files can be found.
     * @return The list of paths (as strings) to be searched for config files.
     */
    List<String> getConfigFilesLocations();

    /**
     * Sets the list of locations (relative to the {@link #getConfigFilesLocationsRoot() file location root} where
     * config files can be found. Note that this does <i>not</i> re-run the properties processing step and load any new
     * properties, but only updates the stored list of file locations. To reload the site configuration completely, you
     * need to also call {@link #resetSiteConfiguration()} and {@link #initSiteConfiguration()}.  Alternatively, you can
     * call {@link #updateSiteConfiguration(List)} or {@link #updateSiteConfiguration(String, List)}, which both perform
     * all of the steps necessary to re-process the site configuration from as close to scratch as possible.
     * @param configFilesLocations    The list of paths (as strings) to be searched for config files.
     */
    void setConfigFilesLocations(final List<String> configFilesLocations);

    /**
     * The absolute path to prepend to any paths in the injected configFilesLocations that are relative.
     * @return The root location for configuration files.
     */
    String getConfigFilesLocationsRoot();

    /**
     * Sets the absolute path to prepend to any paths in the {@link #getConfigFilesLocations() list of file locations}.
     * Note that this does <i>not</i> re-run the properties processing step and load any new properties, but only
     * updates the stored file location root. To reload the site configuration completely, you need to also call {@link
     * #resetSiteConfiguration()} and {@link #initSiteConfiguration()}.  Alternatively, you can call {@link
     * #updateSiteConfiguration(String, List)}, which performs all of the steps necessary to re-process the site
     * configuration from as close to scratch as possible.
     * @param configFilesLocationRoot    The root location for configuration files.
     */
    void setConfigFilesLocationsRoot(final String configFilesLocationRoot);

    /**
     * Gets the pattern for matching file names for custom properties files.
     */
    String getCustomPropertiesNamePattern();

    /**
     * Sets the pattern for matching file names for custom properties files. By default, this is set to {@link
     * #CUSTOM_PROPERTIES_NAME}.
     */
    void setCustomPropertiesNamePattern(final String pattern);

    Pattern CUSTOM_PROPERTIES_NAME = Pattern.compile("^.*-config\\.properties");
    FileFilter CUSTOM_PROPERTIES_FILTER = new FileFilter() {
        public boolean accept(final File file) {
            return file.exists() && file.isFile() && CUSTOM_PROPERTIES_NAME.matcher(file.getName()).matches();
        }
    };
}

