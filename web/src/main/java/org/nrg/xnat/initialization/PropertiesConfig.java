/*
 * web: org.nrg.xnat.initialization.PropertiesConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.IniImporter;
import org.nrg.framework.utilities.OrderedProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Configures the properties for XNAT. This looks in three different places for
 */
@Configuration
@PropertySources({@PropertySource(value = PropertiesConfig.XNAT_HOME_URL, ignoreResourceNotFound = true),
                  @PropertySource(value = PropertiesConfig.XNAT_CONFIG_HOME_URL, ignoreResourceNotFound = true),
                  @PropertySource(value = PropertiesConfig.XNAT_CONFIG_URL, ignoreResourceNotFound = true),
                  @PropertySource(value = PropertiesConfig.XNAT_HOME_NODE_URL, ignoreResourceNotFound = true),
                  @PropertySource(value = PropertiesConfig.NODE_CONFIG_HOME_URL, ignoreResourceNotFound = true),
                  @PropertySource(value = PropertiesConfig.NODE_CONFIG_URL, ignoreResourceNotFound = true)})
@Slf4j
public class PropertiesConfig {

    public static final String ENV_HOME              = "HOME";
    public static final String ENV_XNAT_HOME         = "XNAT_HOME";
    public static final String JAVA_XNAT_HOME        = "xnat.home";
    public static final String JAVA_XNAT_CONFIG_HOME = "xnat.config.home";
    public static final String JAVA_XNAT_CONFIG      = "xnat.config";
    public static final String XNAT_CONF_FILE        = "xnat-conf.properties";
    public static final String BASE_CONF_FOLDER      = "config";
    public static final String EXT_CONF_FOLDER       = "xnat/config";
    public static final String XNAT_HOME_URL         = "file:${" + JAVA_XNAT_HOME + "}/" + BASE_CONF_FOLDER + "/" + XNAT_CONF_FILE;
    public static final String XNAT_CONFIG_HOME_URL  = "file:${" + JAVA_XNAT_CONFIG_HOME + "}/" + XNAT_CONF_FILE;
    public static final String XNAT_CONFIG_URL       = "file:${" + JAVA_XNAT_CONFIG + "}";
    public static final String NODE_CONF_FILE        = "node-conf.properties";
    public static final String XNAT_HOME_NODE_URL    = "file:${" + JAVA_XNAT_HOME + "}/" + BASE_CONF_FOLDER + "/" + NODE_CONF_FILE;
    public static final String NODE_CONFIG_HOME_URL  = "file:${" + JAVA_XNAT_CONFIG_HOME + "}/" + NODE_CONF_FILE;
    public static final String NODE_CONFIG_URL       = "file:${" + JAVA_XNAT_CONFIG + "}";

    public static final List<String> CONFIG_LOCATIONS  = Collections.unmodifiableList(Arrays.asList(JAVA_XNAT_CONFIG, JAVA_XNAT_CONFIG_HOME, JAVA_XNAT_HOME, ENV_XNAT_HOME, ENV_HOME, ENV_HOME));
    public static final List<String> CONFIG_PATHS      = Collections.unmodifiableList(Arrays.asList("", XNAT_CONF_FILE, Paths.get(BASE_CONF_FOLDER, XNAT_CONF_FILE).toString(), Paths.get(BASE_CONF_FOLDER, XNAT_CONF_FILE).toString(), Paths.get(EXT_CONF_FOLDER, XNAT_CONF_FILE).toString(), Paths.get(BASE_CONF_FOLDER, XNAT_CONF_FILE).toString()));
    public static final List<String> NODE_CONFIG_PATHS = Collections.unmodifiableList(Arrays.asList("", NODE_CONF_FILE, Paths.get(BASE_CONF_FOLDER, NODE_CONF_FILE).toString(), Paths.get(BASE_CONF_FOLDER, NODE_CONF_FILE).toString(), Paths.get(EXT_CONF_FOLDER, NODE_CONF_FILE).toString(), Paths.get(BASE_CONF_FOLDER, NODE_CONF_FILE).toString()));

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(final Environment environment) {
        _environment = environment;

        final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);

        final File xnatConfiguration = getXnatConfigurationFile(environment);
        log.debug("Found xnat configuration file {}", xnatConfiguration.getAbsolutePath());

        try {
            final File nodeConfiguration = getNodeConfigurationFile(environment);
            log.debug("Found node configuration file {}", nodeConfiguration.getAbsolutePath());
        } catch (RuntimeException e) {
            // We won't throw a runtime exception for the node configuration.  Just log it.  Node configuration is currently not required.
            log.info("No node configuration found.");
        }

        return configurer;
    }

    @Bean
    public Path xnatHome() {
        if (_xnatHome == null) {
            // We just get the parent of the first folder in the list of configuration folders XNAT_HOME. This won't be
            // null because, if there are no valid configuration folders, the config folders method will have already
            // thrown an exception.
            _xnatHome = configPaths().get(0).getParent();
            log.info("Set path {} as the XNAT home folder.", _xnatHome);
        }
        return _xnatHome;
    }

    @Bean
    public List<String> configFilesLocations() {
        // The configuration service should be converted to use List<Path> instead of List<String> and this bean should
        // be deprecated and removed.
        if (_configFolderLocations.isEmpty()) {
            configPaths();
        }
        return _configFolderLocations;
    }

    @Bean
    public List<Path> configFiles() {
        if (_configFiles.isEmpty()) {
            configPaths();
        }
        return _configFiles;
    }

    @Bean
    public OrderedProperties initPrefs() {
        // Initialize from the system defaults.
        final OrderedProperties initPrefs = new OrderedProperties(IniImporter.getIniProperties("/META-INF/xnat/defaults/sys-init.properties"));

        // Now try to get any override properties that are configured.
        initPrefs.addProperties("prefs-init", IniImporter.getIniProperties(configPaths(), "prefs-init.ini"));
        initPrefs.addProperties("prefs-override", IniImporter.getIniProperties(configPaths(), "prefs-override.ini"));

        return initPrefs;
    }

    @Bean
    public ConfigPaths configPaths() {
        if (_configPaths.isEmpty()) {
            final Map<String, String> paths = new HashMap<>();
            for (int index = 0; index < CONFIG_LOCATIONS.size(); index++) {
                paths.put(CONFIG_LOCATIONS.get(index), CONFIG_PATHS.get(index));
                final Path path = getConfigFolder(_environment, CONFIG_LOCATIONS.get(index), CONFIG_PATHS.get(index), XNAT_CONF_FILE, CONFIG_URLS);
                if (path != null) {
                    log.info("Adding path {} to the list of available configuration folders.", path);
                    final boolean isFile = path.toFile().isFile();
                    final Path    folder      = isFile ? path.getParent() : path;
                    if (!_configPaths.contains(folder)) {
                        _configPaths.add(folder);
                    }
                    if (!_configFolderLocations.contains(folder.toString())) {
                        _configFolderLocations.add((folder).toString());
                    }
                    if (isFile && !_configFiles.contains(path)) {
                        _configFiles.add(path);
                    }
                }
            }
            if (_configPaths.isEmpty()) {
                final StringBuilder writer = new StringBuilder("No XNAT home specified in any of the accepted locations:\n");
                for (final String variable : paths.keySet()) {
                    writer.append(" * ");
                    final String value = _environment.getProperty(variable);
                    if (StringUtils.isBlank(value)) {
                        writer.append(variable).append(": Not defined");
                    } else {
                        writer.append(variable).append(": ").append(value).append("/").append(paths.get(variable));
                    }
                    writer.append("\n");
                }
                throw new RuntimeException(writer.toString());
            }
        }
        return _configPaths;
    }

    @Bean
    public MessageSource messageSource() {
        return new ReloadableResourceBundleMessageSource() {{
            setBasename("classpath:org/nrg/xnat/messages/system");
        }};
    }

    private static File getNodeConfigurationFile(final Environment environment) {
        return findConfigurationFile(environment, NODE_CONF_FILE, NODE_CONFIG_PATHS, NODE_CONFIG_URLS);
    }

    private static File getXnatConfigurationFile(final Environment environment) {
        return findConfigurationFile(environment, XNAT_CONF_FILE, CONFIG_PATHS, CONFIG_URLS);
    }

    private static File findConfigurationFile(final Environment environment, final String configFile, final List<String> configPathList, final List<String> configUrlList) {
        final List<Path> folders = getConfigPaths(environment, configFile, configPathList, configUrlList);
        for (final Path path : folders) {
            log.debug("Checking path {}", path);
            if (path.toFile().exists() && path.toFile().isFile()) {
                log.debug("The path {} exists and is a file, using this to initialize", path);
                return path.toFile();
            }
            final File candidate = path.resolve(configFile).toFile();
            if (candidate.exists()) {
                log.debug("Found the file {} at the candidate path {}, using this to initialize", configFile, path);
                return candidate;
            }
        }
        throw new RuntimeException("Could not find a valid configuration file at any of the possible locations.");
    }

    private static List<Path> getConfigPaths(final Environment environment, final String configFile, final List<String> paths, final List<String> urls) {
        final List<Path> configPaths = new ArrayList<>();
        for (int index = 0; index < CONFIG_LOCATIONS.size(); index++) {
            final Path path = getConfigFolder(environment, CONFIG_LOCATIONS.get(index), paths.get(index), configFile, urls);
            if (path != null) {
                log.info("Adding path {} to the list of available configuration folders.", path);
                configPaths.add(path);
            } else {
                log.debug("The location {} and path {} did not resolve to a usable path.", CONFIG_LOCATIONS.get(index), paths.get(index));
            }
        }
        if (configPaths.isEmpty()) {
            throw new RuntimeException("No XNAT home specified in any of the accepted locations: " + Joiner.on(", ").join(urls));
        }
        return configPaths;
    }

    private static Path getConfigFolder(final Environment environment, final String variable, final String relative, final String configFile, final List<String> urls) {
        final String url = "${" + variable + "}/" + relative;
        if (!urls.contains(url)) {
            urls.add(url);
        }
        log.debug("Testing path for XNAT home candidate: {}", url);
        final String value = environment.getProperty(variable);
        if (StringUtils.isBlank(value)) {
            log.debug("Value of environment variable {} was blank, not a candidate.", variable);
            return null;
        } else {
            log.debug("Found value of '{}' for environment variable {}", value, variable);
        }

        final Path candidate = Paths.get(value, relative);
        final File file      = candidate.toFile();
        if (file.exists()) {
            // If it's a directory...
            if (file.isDirectory()) {
                // Then cool, just return that.
                log.debug("Environment variable {} resolved to path {}, which exists and is a directory, returning as XNAT home.", variable, candidate);
                return candidate;
            } else {
                log.debug("Environment variable {} resolved to path {}, which exists but is not a directory, checking to see if it's a known configuration file.", variable, candidate);
                // If it's a file, then the parent is probably a config folder, so if this is xnat-conf.properties (the default) or the specific file identified by xnat.config...
                if (file.getName().equals(configFile) || StringUtils.equals(candidate.toString(), environment.getProperty(JAVA_XNAT_CONFIG))) {
                    // So its parent is a config folder, QED.
                    log.debug("Environment variable {} resolved to path {}, this is a known configuration file so returning this.", variable, candidate);
                    return candidate;
                }
            }
        }

        log.debug("The environment variable {} resolved to path {}, this doesn't indicate a directory or known configuration file so returning null.", variable, candidate);

        return null;
    }

    private static final List<String> CONFIG_URLS      = new ArrayList<>();
    private static final List<String> NODE_CONFIG_URLS = new ArrayList<>();

    private static Environment _environment;

    private final ConfigPaths  _configPaths           = new ConfigPaths();
    private final List<String> _configFolderLocations = new ArrayList<>();
    private final List<Path>   _configFiles           = new ArrayList<>();

    private Path _xnatHome;

}
