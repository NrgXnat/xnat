/**
 * SiteConfigurationService
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/18/12 by rherri01
 */
package org.nrg.config.services;

import java.util.Properties;

import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.services.NrgService;

public interface SiteConfigurationService extends NrgService {

    /**
     * Gets the site configuration as a Java {@link java.util.Properties} object.
     * @return The initialized Java {@link java.util.Properties} object.
     * @throws ConfigServiceException Thrown when an error occurs resolving or accessing the configuration service.
     */
    public abstract Properties getSiteConfiguration() throws ConfigServiceException;

    /**
     * Sets the site configuration from the submitted Java {@link Properties} object. This updates the data stored in the
     * configuration service, but does not modify or update the source properties bundle stored on the local disk.
     * @param siteConfiguration    The initialized Java {@link Properties} object.
     * @throws ConfigServiceException Thrown when an error occurs resolving or accessing the configuration service.
     */
    public abstract void setSiteConfiguration(String username, Properties siteConfiguration) throws ConfigServiceException;

    /**
     * Gets the value of the indicated property from the site configuration.
     * @param property    The name of the property to be retrieved.
     * @return The value of the property.
     * @throws ConfigServiceException
     */
    public abstract String getSiteConfigurationProperty(String property) throws ConfigServiceException;

    /**
     * Sets the value of the indicated property to the submitted value.
     * @param property    The name of the property to be set.
     * @param value       The value to set for the property.
     * @throws ConfigServiceException
     */
    public abstract void setSiteConfigurationProperty(String username, String property, String value) throws ConfigServiceException;
    
    /**
     * The absolute path to prepend to any paths in the injected configFilesLocations that are relative.
     * @return The root location for configuration files.
     */
    public abstract String getConfigFilesLocationsRoot();
}
