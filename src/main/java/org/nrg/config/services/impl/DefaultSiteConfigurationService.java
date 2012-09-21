/**
 * SiteConfiguration
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/18/12 by rherri01
 */
package org.nrg.config.services.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.exceptions.DuplicateConfigurationDetectedException;
import org.nrg.config.exceptions.SiteConfigurationFileNotFoundException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.SiteConfigurationService;
import org.springframework.stereotype.Service;

@Service
public class DefaultSiteConfigurationService implements SiteConfigurationService {
	
	public DefaultSiteConfigurationService() {
		_initialized = false;
	}

    @Override
    public Properties getSiteConfiguration() throws ConfigServiceException {
    	initSiteConfiguration();
        return _siteConfiguration;
    }

    @Override
    public void setSiteConfiguration(String username, Properties siteConfiguration) throws ConfigServiceException {
    	initSiteConfiguration();
        setPersistedSiteConfiguration(username, siteConfiguration, "Setting site configuration");
        _siteConfiguration = siteConfiguration;
    }

    public Configuration getPersistedSiteConfiguration() {
        return _service.getConfig("site", "siteConfiguration");
    }

    private void setPersistedSiteConfiguration(String username, Properties properties, String message) throws ConfigServiceException {
        if (_log.isInfoEnabled()) {
            if (StringUtils.isBlank(username)) {
                _log.info(message + ", no user details available");
            } else {
                _log.info(message + ", user: " + username);
            }
        }

        _service.replaceConfig(username, message, "site", "siteConfiguration", convertPropertiesToString(properties, message));
    }

    private void initSiteConfiguration() throws ConfigServiceException {
    	if(!_initialized) {
    		refreshSiteConfiguration();
    		_initialized = true;
    	}
    }

    @Override
    public String getSiteConfigurationProperty(String property) throws ConfigServiceException {
    	initSiteConfiguration();
        Properties properties = getSiteConfiguration();
        return properties.getProperty(property);
    }

    @Override
    public void setSiteConfigurationProperty(String username, String property, String value) throws ConfigServiceException {
    	initSiteConfiguration();
        Properties properties = convertStringToProperties(getPersistedSiteConfiguration().getContents());
        properties.setProperty(property, value);
        setPersistedSiteConfiguration(username, properties, "Setting site configuration property value: " + property);
        _siteConfiguration.setProperty(property, value);
    }

    private synchronized void refreshSiteConfiguration() throws ConfigServiceException {
        Properties persistentProperties = getPropertiesFromFile(findSiteConfigurationFile());
        Properties transientProperties = new Properties();

        processCustomProperties(persistentProperties, transientProperties);

        Configuration configuration = getPersistedSiteConfiguration();
        if (configuration == null) {
            setPersistedSiteConfiguration(SYSTEM_STARTUP_CONFIG_REFRESH_USER, persistentProperties, "Setting site configuration");
        } else {
            int hash = persistentProperties.hashCode();
            persistentProperties.putAll(convertStringToProperties(configuration.getContents()));
            if (hash != persistentProperties.hashCode()) {
                setPersistedSiteConfiguration(SYSTEM_STARTUP_CONFIG_REFRESH_USER, persistentProperties, "Setting site configuration");
            }
        }

        persistentProperties.putAll(transientProperties);
        
        _siteConfiguration = persistentProperties;
    }

    private void processCustomProperties(final Properties persistentProperties, final Properties transientProperties) {
        Map<String, File> customConfigPropertiesFileNames = new HashMap<String, File>();
        File overrideConfigFile = null;
        for(String configFilesLocationPath: _configFilesLocations) {
	        File configFilesLocation = new File(configFilesLocationPath);
	        if(configFilesLocation.exists() && configFilesLocation.isDirectory()) {
		        
		        for (File file : configFilesLocation.listFiles(CUSTOM_PROPERTIES_FILTER)) {
		        	if(customConfigPropertiesFileNames.containsKey(file.getName())) {
			        	throw new DuplicateConfigurationDetectedException(customConfigPropertiesFileNames.get(file.getName()), file);
		        	}
		        	else {
				        customConfigPropertiesFileNames.put(file.getName(), file);

				        if(getNamespaceForCustomPropertyFile(file).equals("override")) {
				        	overrideConfigFile = file;	// save this guy for last, he trumps all
				        }
				        else {
				        	processSingleCustomPropertyFile(persistentProperties, transientProperties, file);
				        }
		        	}
		        }
	        }
        }
        if(overrideConfigFile != null) {
        	processOverrideCustomPropertyFile(transientProperties, overrideConfigFile);
        }
    }
    
    private void processSingleCustomPropertyFile(final Properties persistentProperties, final Properties transientProperties, File file) {
		String namespace = getNamespaceForCustomPropertyFile(file);
        Properties customProperties = getPropertiesFromFile(file);
        for (String rawPropertyName : customProperties.stringPropertyNames()) {
        	String polishedPropertyName = rawPropertyName;
        	if(! Boolean.valueOf(persistentProperties.getProperty("allowCustomPropertiesWithDefaultNamespace"))) {
        		if(! rawPropertyName.startsWith(namespace)) {
        			polishedPropertyName = qualifyPropertyName(namespace, rawPropertyName);
        		}
        	}
            if (persistentProperties.containsKey(polishedPropertyName) || transientProperties.containsKey(polishedPropertyName)) {
            	throw new DuplicateConfigurationDetectedException(polishedPropertyName);
            }
            else if(polishedPropertyName.equals(CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME) 
            		|| polishedPropertyName.equals(qualifyPropertyName(namespace, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME)) 
            ) {
            	// this is a meta-property: ignore
            }
            else if(propertiesArePersistent(namespace, customProperties)) {
                persistentProperties.setProperty(polishedPropertyName, customProperties.getProperty(rawPropertyName));
            }
            else {
                transientProperties.setProperty(polishedPropertyName, customProperties.getProperty(rawPropertyName));
            }
        }
    }

    private void processOverrideCustomPropertyFile(final Properties transientProperties, File file) {
        Properties properties = getPropertiesFromFile(file);
        transientProperties.putAll(properties);
    }
    
    /**
     * Allow them to specify transience as
     * persist=false
     * OR
     * mynamespace.persist=false
     * 
     * You really shouldn't have both in the same file, but if you do, the namespaced one will take precedence.
     */
    private boolean propertiesArePersistent(String namespace, Properties props) {
    	boolean persistent = true;
		
    	if(propertyExistsAndIsFalse(props, qualifyPropertyName(namespace, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME))) {
    		persistent = false;
    	}
    	else if(propertyExistsAndIsFalse(props, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME)) {
    		persistent = false;
    	}
    	
    	return persistent;
    }
    
    private String getNamespaceForCustomPropertyFile(File f) {
    	return f.getName().substring(0, f.getName().indexOf("-"));
    }
    
    private String qualifyPropertyName(String namespace, String unqualifiedPropertyName) {
    	return namespace + "." + unqualifiedPropertyName;
    }
    
    private boolean propertyExistsAndIsFalse(Properties props, String propName) {
    	return props.getProperty(propName) != null && props.getProperty(propName).equalsIgnoreCase("FALSE");
    }
    
    private File findSiteConfigurationFile() {
    	File siteConfigFile = null;
    	int numberOfSiteConfigFilesFound = 0;
    	
    	for(String configFilesLocationPath: _configFilesLocations) {
        	File potentialSiteConfigFile = new File(configFilesLocationPath, "siteConfiguration.properties");
        	if(potentialSiteConfigFile.exists()) {
        		if(++numberOfSiteConfigFilesFound > 1) {
            		throw new DuplicateConfigurationDetectedException(siteConfigFile, potentialSiteConfigFile);
        		}
        		else {
        			siteConfigFile = potentialSiteConfigFile;
        		}
        	}
        }
    	
    	if(0 == numberOfSiteConfigFilesFound) {
    		throw new SiteConfigurationFileNotFoundException();
    	}
    	else {
    		return siteConfigFile;
    	}
    }

    private static String convertPropertiesToString(final Properties properties, String message) {
        StringWriter writer = new StringWriter();
        try {
            properties.store(new PrintWriter(writer), message);
        } catch (IOException e) {
        	throw new RuntimeException("Failed convertPropertiesToString", e);
        }
        return writer.getBuffer().toString();
    }

    private static Properties convertStringToProperties(final String contents) {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(contents.getBytes()));
        } catch (IOException e) {
        	throw new RuntimeException("Failed convertStringToProperties", e);
        }
        return properties;
    }

    private static Properties getPropertiesFromFile(File file)  {
    	try {
	        Properties props = new Properties();
	        InputStream propsIn = new FileInputStream(file);
	        props.load(propsIn);
	        return props;
    	}
    	catch(IOException e) {
    		throw new RuntimeException("Failed getPropertiesFromFile", e);
    	}
    }
    
    /**
     * Exposed so unit test can inject alternative locations
     */
    public List<String> getConfigFilesLocations() {
    	return _configFilesLocations;
    }
    /**
     * Exposed so unit test can inject alternative locations
     */
    public void setConfigFilesLocations(List<String> configFilesLocations) {
    	_configFilesLocations = configFilesLocations;
    	_initialized = false;
    }
    
    @Resource(name="configFilesLocations")
    private List<String> _configFilesLocations;

    @Inject
    private ConfigService _service;

    private static final Pattern CUSTOM_PROPERTIES_NAME = Pattern.compile("^.*-config\\.properties");
    private static final FileFilter CUSTOM_PROPERTIES_FILTER = new FileFilter() {
        public boolean accept(final File file) {
            return file.exists() && file.isFile() && CUSTOM_PROPERTIES_NAME.matcher(file.getName()).matches();
        }
    };
    private static final Log _log = LogFactory.getLog(DefaultSiteConfigurationService.class);
    private static final String SYSTEM_STARTUP_CONFIG_REFRESH_USER = "admin";
    private static final String CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME = "persist";
    private Properties _siteConfiguration;
    private boolean _initialized;
}
