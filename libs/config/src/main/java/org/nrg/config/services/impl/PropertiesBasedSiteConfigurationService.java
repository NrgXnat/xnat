/*
 * config: org.nrg.config.services.impl.PropertiesBasedSiteConfigurationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.DuplicateConfigurationDetectedException;
import org.nrg.config.exceptions.InvalidSiteConfigurationPropertyChangedListenerException;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.exceptions.SiteConfigurationFileNotFoundException;
import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manages all the implementation around retrieving properties files and converting them into properties that can be used in the site configuration service. The abstract methods defined by this class can be implemented to control how the persistent store is implemented.
 */
@Slf4j
public abstract class PropertiesBasedSiteConfigurationService implements InitializingBean, ServletContextAware, SiteConfigurationService {
    private static final String SITE_CONFIGURATION_PROPERTIES_FILENAME = "site-config.properties";
    private static final String SITE_CONFIGURATION_PROPERTIES_PACKAGE  = "classpath*:META-INF/xnat/preferences/";

    public static final String SITE_CONFIGURATION_PROPERTIES_RESOURCE = SITE_CONFIGURATION_PROPERTIES_PACKAGE + SITE_CONFIGURATION_PROPERTIES_FILENAME;

    /**
     * Sets a preference value in the implemented persistent store.
     *
     * @param username The name of the user requesting the changed preference value.
     * @param property The name of the site configuration property.
     * @param value    The value to be set for the site configuration property.
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    abstract protected void setPreferenceValue(final String username, final String property, final String value) throws SiteConfigurationException;

    /**
     * Initializes all properties from the persistent store. This method is called after the service has parsed all
     * discovered properties files. The <b>properties</b> parameter that's passed into this method contains all the
     * property values found during parsing. Any properties in this properties bundle that already exist in the
     * persistent store will have their value overwritten with the persisted value (i.e. the value in the persistent
     * store takes precedence over the discovered value). Any properties that don't already exist in the persistent
     * store are added to the persistent store and initialized with the value found in the discovered properties bundle.
     *
     * @param properties All properties found during property discovery.
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    abstract protected void getPreferenceValuesFromPersistentStore(final Properties properties) throws SiteConfigurationException;

    /**
     * This initializes the site configuration once the overall start-up process has completed.
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    @Override
    public void afterPropertiesSet() throws SiteConfigurationException {
        initSiteConfiguration();
    }

    /**
     * Initializes the site configuration service. This implementation loads the site configuration from the persistent
     * {@link NrgPreferenceService preferences service}, as well as scanning for properties files that match the in all
     * of the {@link #getConfigFilesLocations() specified configuration folders} located under the {@link
     * #getConfigFilesLocationsRoot()}
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    @Override
    public void initSiteConfiguration() throws SiteConfigurationException {
        if (_siteConfiguration == null) {
            log.debug("Initializing the site configuration");
            if (_environment != null) {
                _environment.getActiveProfiles();
            }
            processSiteConfiguration();
        }
    }

    /**
     * Sets the {@link #getConfigFilesLocationsRoot() configuration file root} to the submitted location, {@link
     * #resetSiteConfiguration() clears all cached properties}, and {@link #initSiteConfiguration() reloads properties
     * from any properties files} found in the submitted list.
     *
     * @param configFilesLocationsRoot The root location to search for configuration files.
     *
     * @return The resulting site configuration properties.
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    @Override
    public Properties updateSiteConfiguration(final String configFilesLocationsRoot) throws SiteConfigurationException {
        return updateSiteConfiguration(configFilesLocationsRoot, null);
    }

    /**
     * Sets the {@link #getConfigFilesLocations() list of configuration file locations} to the submitted list, {@link
     * #resetSiteConfiguration() clears all cached properties}, and {@link #initSiteConfiguration() reloads properties
     * from any properties files} found in the submitted list.
     *
     * @param configFilesLocations The list of locations where configuration files can be found.
     *
     * @return The resulting site configuration properties.
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    @Override
    public Properties updateSiteConfiguration(final List<String> configFilesLocations) throws SiteConfigurationException {
        return updateSiteConfiguration(null, configFilesLocations);
    }

    /**
     * Sets the {@link #getConfigFilesLocationsRoot() configuration file root} to the submitted location, sets the
     * {@link #getConfigFilesLocations() list of configuration file locations} to the submitted list, {@link
     * #resetSiteConfiguration() clears all cached properties}, and {@link #initSiteConfiguration() reloads properties
     * from any properties files} found in the submitted list.
     *
     * @param configFilesLocationsRoot The root location to search for configuration files.
     * @param configFilesLocations     The list of locations where configuration files can be found.
     *
     * @return The resulting site configuration properties.
     *
     * @throws SiteConfigurationException When an error occurs accessing or updating the site configuration.
     */
    @Override
    public Properties updateSiteConfiguration(final String configFilesLocationsRoot, final List<String> configFilesLocations) throws SiteConfigurationException {
        if (StringUtils.isNotBlank(configFilesLocationsRoot)) {
            setConfigFilesLocationsRoot(configFilesLocationsRoot);
        }
        if (configFilesLocations != null && configFilesLocations.size() > 0) {
            setConfigFilesLocations(configFilesLocations);
        }
        resetSiteConfiguration();
        initSiteConfiguration();
        return getSiteConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetSiteConfiguration() {
        _siteConfiguration = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getSiteConfiguration() {
        checkSiteConfigurationInit();
        final Properties siteConfigurationCopy = new Properties();
        siteConfigurationCopy.putAll(_siteConfiguration);
        return siteConfigurationCopy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSiteConfigurationProperty(String property) {
        checkSiteConfigurationInit();
        return getSiteConfiguration().getProperty(property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSiteConfigurationProperty(final String username, final String property, final String value) throws SiteConfigurationException {
        if (log.isDebugEnabled()) {
            log.debug("The user {} is attempting to set the value of the {} property to the value {}", username, property, value);
        }
        checkSiteConfigurationInit();
        if (propertyIsDirty(property, value)) {
            if (log.isDebugEnabled()) {
                log.debug("The property {} is dirty, actually setting it to the value {}", property, value);
            }
            setPreferenceValue(username, property, value);
            _siteConfiguration.setProperty(property, value);
            notifyListeners(property, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolSiteConfigurationProperty(final String property, final boolean defaultValue) {
        final String value = getSiteConfigurationProperty(property);
        return StringUtils.isBlank(value) ? defaultValue : BooleanUtils.toBoolean(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getIntegerSiteConfigurationProperty(final String property) {
        final String value = getSiteConfigurationProperty(property);
        return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getLongSiteConfigurationProperty(final String property) {
        final String value = getSiteConfigurationProperty(property);
        return StringUtils.isNotBlank(value) ? Long.parseLong(value) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getFloatSiteConfigurationProperty(final String property) {
        final String value = getSiteConfigurationProperty(property);
        return StringUtils.isNotBlank(value) ? Float.parseFloat(value) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getDoubleSiteConfigurationProperty(final String property) {
        final String value = getSiteConfigurationProperty(property);
        return StringUtils.isNotBlank(value) ? Double.parseDouble(value) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServletContext(final ServletContext context) {
        _context = context;
    }

    /**
     * Exposed so unit test can inject alternative locations
     */
    @Override
    public List<String> getConfigFilesLocations() {
        return new ArrayList<String>() {{
            addAll(_configFilesLocations);
        }};
    }

    /**
     * Provided to allow for configuration file location overrides or additions.
     *
     * @param configFilesLocations The locations where site configuration property files can be found.
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource(name = "configFilesLocations")
    @Override
    public void setConfigFilesLocations(final List<String> configFilesLocations) {
        _configFilesLocations.clear();
        _configFilesLocations.addAll(configFilesLocations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigFilesLocationsRoot() {
        if (_configFilesLocationsRoot == null && _context != null) {
            _configFilesLocationsRoot = _context.getRealPath("/");
        }
        return _configFilesLocationsRoot;
    }

    /**
     * Provided to allow for config file location root overrides. This is mainly preserved for testing purposes.
     *
     * @param configFilesLocationRoot The root to use for locating configuration files.
     */
    @Override
    public void setConfigFilesLocationsRoot(final String configFilesLocationRoot) {
        _configFilesLocationsRoot = configFilesLocationRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCustomPropertiesNamePattern() {
        return _customPropertiesName.pattern();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCustomPropertiesNamePattern(final String pattern) {
        _customPropertiesName = Pattern.compile(pattern);
        _fileFilter = file -> file.exists() && file.isFile() && _customPropertiesName.matcher(file.getName()).matches();
    }

    /**
     * Sets the JDBC template.
     *
     * @param jdbcTemplate The template to set.
     */
    @Autowired(required = false)
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        _jdbcTemplate = jdbcTemplate;
    }

    protected void setEnvironment(final Environment environment) {
        _environment = environment;
    }

    /**
     * Checks for existing rows in the configuration service tables labeled as 'siteConfiguration'. If found, the latest
     * version is taken, converted into a properties file, and stored in the preferences-based site configuration. This
     * is a legacy conversion operation and should be removed in later versions of this library.
     *
     * @return The properties found in the site configuration properties files.
     */
    protected Properties checkForConfigServiceSiteConfiguration() {
        if (_jdbcTemplate != null) {
            @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"}) final List<String> contents = _jdbcTemplate.query("SELECT d.contents FROM xhbm_configuration c, xhbm_configuration_data d WHERE c.path = 'siteConfiguration' AND c.config_data = d.id ORDER BY c.version DESC LIMIT 1", (rs, rowNum) -> rs.getString(1));
            // By the nature of the query above, the size should only ever be 0 or 1.
            if (contents != null && contents.size() == 1) {
                final Properties existing = new Properties();
                try {
                    existing.load(new StringReader(contents.get(0)));
                    if (log.isDebugEnabled()) {
                        log.debug("Found {} properties stored in the configuration service-based site configuration.", existing.stringPropertyNames().size());
                        for (final String property : existing.stringPropertyNames()) {
                            log.debug(" * Setting the {} property to value: {}", property, existing.getProperty(property));
                        }
                    }
                    return existing;
                } catch (IOException e) {
                    log.warn("Something went wrong trying to load properties from the existing configuration service-based site configuration.", e);
                }
            }
        }
        return null;
    }

    /**
     * We won't know the servlet path until runtime, so this can't be done via Spring. Servlet will set the root, and then we'll update the location list here.
     */
    private void prependConfigFilesLocationsRootToAllConfigFilesLocations() {
        if (StringUtils.isNotBlank(getConfigFilesLocationsRoot())) {
            for (int i = 0; i < _configFilesLocations.size(); ++i) {
                final File configFilesLocation = new File(_configFilesLocations.get(i));
                if (!configFilesLocation.isAbsolute()) {
                    _configFilesLocations.set(i, Paths.get(getConfigFilesLocationsRoot(), _configFilesLocations.get(i)).toString());
                }
            }
        }
    }

    private void checkSiteConfigurationInit() {
        if (_siteConfiguration == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "You must explicitly initialize the site configuration service before attempting to access service preferences.");
        }
    }

    private boolean propertyIsDirty(String property, String value) {
        return (_siteConfiguration.getProperty(property) == null && value != null) || (_siteConfiguration.getProperty(property) != null && !_siteConfiguration.getProperty(property).equals(value));
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
        notifyNamespaceLevelListener(getNamespaceForCustomProperty(property), property, value);
    }

    private void notifyNamespaceLevelListener(String namespace, String property, String value) {
        if (!StringUtils.isBlank(namespace)) {
            notifyListener(namespace + "." + PROPERTY_CHANGED_LISTENER_PROPERTY, property, value);
        }
    }

    private void notifyPropertyLevelListener(String property, String value) {
        notifyListener(property + "." + PROPERTY_CHANGED_LISTENER_PROPERTY, property, value);
    }

    private void notifyListener(String listenerPropertyName, String property, String value) {
        final String listenerClassName = _siteConfiguration.getProperty(listenerPropertyName);
        if (!StringUtils.isBlank(listenerClassName)) {
            Class<?> listenerClass = null;
            try {
                listenerClass = Class.forName(listenerClassName);
                SiteConfigurationPropertyChangedListener listener;
                try {
                    listener = (SiteConfigurationPropertyChangedListener) listenerClass.newInstance();
                } catch (IllegalAccessException e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' did not have a public no-arg constructor to call.", listenerClassName), e);
                } catch (InstantiationException e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' is not an instantiable type.", listenerClassName), e);
                } catch (Exception e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' failed in the constructor.", listenerClassName), e);
                }

                try {
                    listener.siteConfigurationPropertyChanged(property, value);
                } catch (Exception e) {
                    throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Something went wrong while invoking listener '%s'.", listener.getClass().getName()), e);
                }
            } catch (ClassNotFoundException e) {
                throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' was not found.", listenerClassName), e);
            } catch (ExceptionInInitializerError e) {
                throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' failed in a static initializer.", listenerClassName), e);
            } catch (ClassCastException e) {
                assert listenerClass != null;
                throw new InvalidSiteConfigurationPropertyChangedListenerException(String.format("Listener '%s' was not of type '%s'.", listenerClass.getName(), SiteConfigurationPropertyChangedListener.class.getName()), e);
            }
        }
    }

    private synchronized void processSiteConfiguration() throws SiteConfigurationException {
        prependConfigFilesLocationsRootToAllConfigFilesLocations();

        final Properties persistentProperties = getPropertiesFromStream(findSiteConfiguration());
        final Properties transientProperties  = new Properties();

        try {
            processCustomProperties(persistentProperties, transientProperties);
        } catch (FileNotFoundException e) {
            throw new SiteConfigurationFileNotFoundException(e);
        }

        getPreferenceValuesFromPersistentStore(persistentProperties);

        persistentProperties.putAll(transientProperties);
        _siteConfiguration = persistentProperties;
    }

    private void processCustomProperties(final Properties persistentProperties, final Properties transientProperties) throws FileNotFoundException {
        final Map<String, File> customConfigPropertiesFileNames = new HashMap<>();
        File                    overrideConfigFile              = null;
        for (String configFilesLocationPath : _configFilesLocations) {
            final File configFilesLocation = new File(configFilesLocationPath);
            if (configFilesLocation.exists() && configFilesLocation.isDirectory()) {

                final File[] files = configFilesLocation.listFiles(_fileFilter);
                if (files != null) {
                    for (File file : files) {
                        if (customConfigPropertiesFileNames.containsKey(file.getName())) {
                            throw new DuplicateConfigurationDetectedException(customConfigPropertiesFileNames.get(file.getName()), file);
                        } else {
                            customConfigPropertiesFileNames.put(file.getName(), file);

                            if (getNamespaceForCustomPropertyFile(file).equals("override")) {
                                overrideConfigFile = file;    // save this guy for last, he trumps all
                            } else {
                                processSingleCustomPropertyFile(persistentProperties, transientProperties, file);
                            }
                        }
                    }
                }
            }
        }
        if (overrideConfigFile != null) {
            processOverrideCustomPropertyFile(transientProperties, overrideConfigFile);
        }
    }

    private void processSingleCustomPropertyFile(final Properties persistentProperties, final Properties transientProperties, File file) throws FileNotFoundException {
        final String     namespace        = getNamespaceForCustomPropertyFile(file);
        final Properties customProperties = getPropertiesFromStream(new FileInputStream(file));
        for (final String rawPropertyName : customProperties.stringPropertyNames()) {
            final String polishedPropertyName = !rawPropertyName.startsWith(namespace) ? qualifyPropertyName(namespace, rawPropertyName) : rawPropertyName;
            if (log.isDebugEnabled()) {
                log.debug("Processing property: " + polishedPropertyName);
            }
            if (persistentProperties.containsKey(polishedPropertyName) || transientProperties.containsKey(polishedPropertyName)) {
                throw new DuplicateConfigurationDetectedException(polishedPropertyName);
            } else if (polishedPropertyName.equals(CUSTOM_PROPERTIES_PERSISTENCE_SETTING_NAME) || polishedPropertyName.equals(qualifyPropertyName(namespace, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_NAME))) {
                // this is a meta-property: ignore
                if (log.isDebugEnabled()) {
                    log.debug("Found persistence setting, ignoring meta-property");
                }
            } else if (propertiesArePersistent(namespace, customProperties)) {
                persistentProperties.setProperty(polishedPropertyName, customProperties.getProperty(rawPropertyName));
            } else {
                transientProperties.setProperty(polishedPropertyName, customProperties.getProperty(rawPropertyName));
            }
        }
    }

    private void processOverrideCustomPropertyFile(final Properties transientProperties, File file) throws FileNotFoundException {
        Properties properties = getPropertiesFromStream(new FileInputStream(file));
        transientProperties.putAll(properties);
    }

    /**
     * Allow them to specify transience as persist=false OR namespace.persist=false
     * <p/>
     * You really shouldn't have both in the same file, but if you do, the namespaced one will take precedence.
     */
    private boolean propertiesArePersistent(String namespace, Properties props) {
        return !propertyExistsAndIsFalse(props, qualifyPropertyName(namespace, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_NAME)) &&
               !propertyExistsAndIsFalse(props, CUSTOM_PROPERTIES_PERSISTENCE_SETTING_NAME);
    }

    private String getNamespaceForCustomPropertyFile(File f) {
        return f.getName().substring(0, f.getName().indexOf("-"));
    }

    private String getNamespaceForCustomProperty(String propertyName) {
        return propertyName.contains(".") ? propertyName.substring(0, propertyName.indexOf(".")) : null;
    }

    private String qualifyPropertyName(String namespace, String unqualifiedPropertyName) {
        return namespace + "." + unqualifiedPropertyName;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean propertyExistsAndIsFalse(Properties props, String propName) {
        return props.getProperty(propName) != null && props.getProperty(propName).equalsIgnoreCase("FALSE");
    }

    private InputStream findSiteConfiguration() throws SiteConfigurationFileNotFoundException {
        final List<File>        siteConfigFiles   = new ArrayList<>();
        final Map<String, File> notFoundLocations = new HashMap<>();
        for (final String configFilesLocationPath : _configFilesLocations) {
            final File potentialSiteConfigFile = new File(configFilesLocationPath, SITE_CONFIGURATION_PROPERTIES_FILENAME);
            if (potentialSiteConfigFile.exists()) {
                if (siteConfigFiles.size() > 1) {
                    throw new DuplicateConfigurationDetectedException(siteConfigFiles.get(0), potentialSiteConfigFile);
                } else {
                    siteConfigFiles.add(potentialSiteConfigFile);
                }
            } else {
                notFoundLocations.put(configFilesLocationPath, potentialSiteConfigFile);
            }
        }

        if (!notFoundLocations.isEmpty()) {
            log.info("Found {} locations that didn't exist: {}", notFoundLocations.size(), String.join(", ", notFoundLocations.keySet()));
        }

        if (siteConfigFiles.isEmpty()) {
            try {
                final List<org.springframework.core.io.Resource> resources = BasicXnatResourceLocator.getResources(SITE_CONFIGURATION_PROPERTIES_RESOURCE);
                if (resources.isEmpty()) {
                    throw new SiteConfigurationFileNotFoundException(SITE_CONFIGURATION_PROPERTIES_FILENAME, _configFilesLocations);
                }
                if (resources.size() > 1) {
                    log.warn("I somehow managed to find more than one site configuration file, {} to be exact: {}", resources.size(), resources.stream().map(Object::toString).collect(Collectors.joining(", ")));
                }
                return resources.get(0).getInputStream();
            } catch (IOException e) {
                throw new SiteConfigurationFileNotFoundException(SITE_CONFIGURATION_PROPERTIES_FILENAME, Collections.singletonList(SITE_CONFIGURATION_PROPERTIES_PACKAGE));
            }
        } else {
            final File siteConfigFile = siteConfigFiles.get(0);
            try {
                return new FileInputStream(siteConfigFile);
            } catch (FileNotFoundException e) {
                throw new SiteConfigurationFileNotFoundException(siteConfigFile.getName(), _configFilesLocations);
            }
        }
    }

    private static Properties getPropertiesFromStream(final InputStream stream) {
        try {
            final Properties props = new Properties();
            props.load(stream);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed getPropertiesFromStream", e);
        }
    }

    private static final String CUSTOM_PROPERTIES_PERSISTENCE_SETTING_NAME = "persist";
    private static final String PROPERTY_CHANGED_LISTENER_PROPERTY         = "property.changed.listener";

    private Environment    _environment;
    private JdbcTemplate   _jdbcTemplate;
    private ServletContext _context;
    private String         _configFilesLocationsRoot;

    private final List<String> _configFilesLocations = new ArrayList<>();
    private       Properties   _siteConfiguration    = null;
    private       FileFilter   _fileFilter           = CUSTOM_PROPERTIES_FILTER;
    private       Pattern      _customPropertiesName = CUSTOM_PROPERTIES_NAME;
}
