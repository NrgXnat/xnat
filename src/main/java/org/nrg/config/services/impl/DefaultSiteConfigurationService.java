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
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.exceptions.DuplicateConfigurationDetectedException;
import org.nrg.config.exceptions.InvalidSiteConfigurationPropertyChangedListenerException;
import org.nrg.config.exceptions.SiteConfigurationFileNotFoundException;
import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.SiteConfigurationService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

@Service
public class DefaultSiteConfigurationService implements SiteConfigurationService, ServletContextAware {

    @Override
    public Properties getSiteConfiguration() throws ConfigServiceException {
    	initSiteConfiguration();
    	Properties siteConfigurationCopy = new Properties();
    	siteConfigurationCopy.putAll(_siteConfiguration);
        return siteConfigurationCopy;
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

    @Override
    public String getSiteConfigurationProperty(String property) throws ConfigServiceException {
    	initSiteConfiguration();
        Properties properties = getSiteConfiguration();
        return properties.getProperty(property);
    }

    @Override
    public void setSiteConfigurationProperty(String username, String property, String value) throws ConfigServiceException {
    	initSiteConfiguration();
        if(propertyIsDirty(property, value)) {
            Properties properties = convertStringToProperties(getPersistedSiteConfiguration().getContents());
	        properties.setProperty(property, value);
	        setPersistedSiteConfiguration(username, properties, "Setting site configuration property value: " + property);
	        _siteConfiguration.setProperty(property, value);
	        notifyListeners(property, value);
        }
    }
    
    @Override
    public void setServletContext(final ServletContext context) {
        _context = context;
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
    
	@Override
	public String getConfigFilesLocationsRoot() {
            if (_configFilesLocationsRoot == null && _context != null) {
                _configFilesLocationsRoot = _context.getRealPath("/");
            }
            return _configFilesLocationsRoot;
	}

    /**
     * Provided to allow for config file location root overrides. This is mainly preserved for testing purposes.
     * @param configFilesLocationRoot    The root to use for locating configuration files.
     */
    @SuppressWarnings("unused")
    public void setConfigFilesLocationsRoot(final String configFilesLocationRoot) {
        _configFilesLocationsRoot = configFilesLocationRoot;
        _initialized = false;
    }

    /**
	 * We won't know the servlet path until runtime, so this can't be done via Spring.
	 * Servlet will set the root and then we'll update the location list here.
	 */
	private void prependConfigFilesLocationsRootToAllConfigFilesLocations() {
		if(StringUtils.isNotBlank(getConfigFilesLocationsRoot())) {
			for(int i = 0; i < _configFilesLocations.size(); ++i) {
				File configFilesLocation = new File(_configFilesLocations.get(i));
				if(!configFilesLocation.isAbsolute()) {
					String absoluteConfigFilesLocation = getConfigFilesLocationsRoot() + File.separator + _configFilesLocations.get(i);
					_configFilesLocations.set(i, absoluteConfigFilesLocation);
				}
			}
		}
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

    private boolean propertyIsDirty(String property, String value) {
        return (
                _siteConfiguration.getProperty(property) == null && value != null)
                || (_siteConfiguration.getProperty(property) != null && ! _siteConfiguration.getProperty(property).equals(value)
        );
    }

    private void notifyListeners(String property, String value) {
        notifySiteLevelListener(property, value);
        notifyNamespaceLevelListener(property, value);
        notifyPropertyLevelListener(property, value);
    }

    private void notifySiteLevelListener(String property, String value) {
        notifyListener(PROPERTY_CHANGED_LISTENER_PROPERTY, property, value);
    }

    private void notifyNamespaceLevelListener(String property, String value) {
        String namespace = getNamespaceForCustomProperty(property);
        notifyNamespaceLevelListener(namespace, property, value);
    }

    private void notifyNamespaceLevelListener(String namespace, String property, String value) {
        if(! StringUtils.isBlank(namespace)) {
            notifyListener(namespace + "." + PROPERTY_CHANGED_LISTENER_PROPERTY, property, value);
        }
    }

    private void notifyPropertyLevelListener(String property, String value) {
        notifyListener(property + "." + PROPERTY_CHANGED_LISTENER_PROPERTY, property, value);
    }

    private void notifyListener(String listenerPropertyName, String property, String value) {
        String listenerClassName = _siteConfiguration.getProperty(listenerPropertyName);
        if(! StringUtils.isBlank(listenerClassName)) {
            Class<?> listenerClass = null;
            try {
                listenerClass = Class.forName(listenerClassName);
                SiteConfigurationPropertyChangedListener listener;
                try {
                    listener = (SiteConfigurationPropertyChangedListener) listenerClass.newInstance();
                }
                catch(IllegalAccessException e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' did not have a public no-arg constructor to call.", listenerClassName), e);
                }
                catch(InstantiationException e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' is not an instantiable type.", listenerClassName), e);
                }
                catch(Exception e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' failed in the constructor.", listenerClassName), e);
                }

                try {
                    listener.siteConfigurationPropertyChanged(property, value);
                }
                catch(Exception e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Something went wrong while invoking listener '%s'.", listener.getClass().getName()), e);
                }
            }
            catch(ClassNotFoundException e) {
                throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' was not found.", listenerClassName), e);
            }
            catch(ExceptionInInitializerError e) {
                throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' failed in a static initializer.", listenerClassName), e);
            }
            catch(ClassCastException e) {
                assert listenerClass != null;
                throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' was not of type '%s'.", listenerClass.getName(), SiteConfigurationPropertyChangedListener.class.getName()), e);
            }
        }
    }

    private synchronized void refreshSiteConfiguration() throws ConfigServiceException {
        prependConfigFilesLocationsRootToAllConfigFilesLocations();

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
            if(! rawPropertyName.startsWith(namespace)) {
                polishedPropertyName = qualifyPropertyName(namespace, rawPropertyName);
                if (_log.isDebugEnabled()) {
                    _log.debug("Processing property: " + polishedPropertyName);
                }
            }
            if (persistentProperties.containsKey(polishedPropertyName) || transientProperties.containsKey(polishedPropertyName)) {
                throw new DuplicateConfigurationDetectedException(polishedPropertyName);
            } else if (polishedPropertyName.equals(CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME)
                    || polishedPropertyName.equals(qualifyPropertyName(namespace, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME))
                    ) {
                // this is a meta-property: ignore
                if (_log.isDebugEnabled()) {
                    _log.debug("Found persistence setting, ignoring meta-property");
                }
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

    private String getNamespaceForCustomProperty(String propertyName) {
        if(!propertyName.contains(".")) {
            return null;
        }
        else {
            return propertyName.substring(0, propertyName.indexOf("."));
        }
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

        Map<String, File> notFoundLocations = new HashMap<String, File>();
        for(String configFilesLocationPath: _configFilesLocations) {
            File potentialSiteConfigFile = new File(configFilesLocationPath, SITE_CONFIGURATION_PROPERTIES_FILENAME);
            if(potentialSiteConfigFile.exists()) {
                if(++numberOfSiteConfigFilesFound > 1) {
                    throw new DuplicateConfigurationDetectedException(siteConfigFile, potentialSiteConfigFile);
                }
                else {
                    siteConfigFile = potentialSiteConfigFile;
                }
            } else {
                notFoundLocations.put(configFilesLocationPath, potentialSiteConfigFile);
            }
        }

        if(0 == numberOfSiteConfigFilesFound) {
            throw new SiteConfigurationFileNotFoundException(SITE_CONFIGURATION_PROPERTIES_FILENAME, getAbsolutePaths(notFoundLocations));
        }
        else {
            return siteConfigFile;
        }
    }

    private List<String> getAbsolutePaths(final Map<String, File> locations) {
        final List<String> absolutePaths = new ArrayList<String>(locations.size());
        for (Map.Entry<String, File> location : locations.entrySet()) {
            absolutePaths.add(location.getKey() + ": " + location.getValue().getAbsolutePath());
        }
        return absolutePaths;
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

    @Resource(name="configFilesLocations")
    private List<String> _configFilesLocations;
    
    @Inject
    private ConfigService _service;

    private static final Log _log = LogFactory.getLog(DefaultSiteConfigurationService.class);

    private static final Pattern CUSTOM_PROPERTIES_NAME = Pattern.compile("^.*-config\\.properties");
    private static final String SYSTEM_STARTUP_CONFIG_REFRESH_USER = "admin";
    private static final String CUSTOM_PROPERTIES_PERSISTENCE_SETTING_PROPNAME = "persist";
    private static final String SITE_CONFIGURATION_PROPERTIES_FILENAME = "siteConfiguration.properties";
    private static final String PROPERTY_CHANGED_LISTENER_PROPERTY = "property.changed.listener";
    private static final FileFilter CUSTOM_PROPERTIES_FILTER = new FileFilter() {
        public boolean accept(final File file) {
            return file.exists() && file.isFile() && CUSTOM_PROPERTIES_NAME.matcher(file.getName()).matches();
        }
    };

    private ServletContext _context;
    private Properties _siteConfiguration;
    private boolean _initialized;
    private String _configFilesLocationsRoot;
}
