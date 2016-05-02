package org.nrg.xnat.initialization;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.python.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configures the properties for XNAT. This looks in three different places for
 */
@Configuration
@PropertySources({
     @PropertySource(value = PropertiesConfig.XNAT_HOME_URL, ignoreResourceNotFound = true),
     @PropertySource(value = PropertiesConfig.XNAT_CONFIG_HOME_URL, ignoreResourceNotFound = true),
     @PropertySource(value = PropertiesConfig.XNAT_CONFIG_URL, ignoreResourceNotFound = true)})
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

    public static final List<String> CONFIG_LOCATIONS = Collections.unmodifiableList(Arrays.asList(JAVA_XNAT_CONFIG, JAVA_XNAT_CONFIG_HOME, JAVA_XNAT_HOME, ENV_XNAT_HOME, ENV_HOME, ENV_HOME));
    public static final List<String> CONFIG_PATHS     = Collections.unmodifiableList(Arrays.asList("", XNAT_CONF_FILE, Paths.get(BASE_CONF_FOLDER, XNAT_CONF_FILE).toString(), Paths.get(BASE_CONF_FOLDER, XNAT_CONF_FILE).toString(), Paths.get(EXT_CONF_FOLDER, XNAT_CONF_FILE).toString(), Paths.get(BASE_CONF_FOLDER, XNAT_CONF_FILE).toString()));

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(final Environment environment) throws ConfigurationException {
        final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        final File configuration = getConfigurationFile(environment);
        if (_log.isDebugEnabled()) {
            _log.debug("Found configuration file " + configuration.getAbsolutePath());
        }
        // final ConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder(getConfigurationFile(environment));
        // configurer.setProperties(ConfigurationConverter.getProperties(configurationBuilder.getConfiguration()));
        return configurer;
    }

    @Bean
    public Path xnatHome() {
        if (_xnatHome == null) {
            // We just get the parent of the first folder in the list of configuration folders XNAT_HOME. This won't be
            // null because, if there are no valid configuration folders, the config folders method will have already
            // thrown an exception.
            _xnatHome = configFolderPaths().get(0).getParent();
            if (_log.isInfoEnabled()) {
                _log.info("Set path {} as the XNAT home folder.");
            }
        }
        return _xnatHome;
    }

    @Bean
    public List<String> configFilesLocations() {
        // The configuration service should be converted to use List<Path> instead of List<String> and this bean should
        // be deprecated and removed.
        if (_configFolderLocations.size() == 0) {
            configFolderPaths();
        }
        return _configFolderLocations;
    }

    @Bean
    public List<Path> configFolderPaths() {
        if (_configFolderPaths.size() == 0) {
            for (int index = 0; index < CONFIG_LOCATIONS.size(); index++) {
                final Path path = getConfigFolder(_environment, CONFIG_LOCATIONS.get(index), CONFIG_PATHS.get(index));
                if (path != null) {
                    if (_log.isInfoEnabled()) {
                        _log.info("Adding path {} to the list of available configuration folders.", path.toString());
                    }
                    _configFolderPaths.add(path);
                    _configFolderLocations.add(path.toString());
                }
            }
            if (_configFolderPaths.size() == 0) {
                throw new RuntimeException("No XNAT home specified in any of the accepted locations: " + Joiner.on(", ").join(CONFIG_URLS));
            }
        }
        return _configFolderPaths;
    }

    private static File getConfigurationFile(final Environment environment) {
        final List<Path> folders = getConfigFolderPaths(environment);
        for (final Path path : folders) {
            if (path.toFile().exists() && path.toFile().isFile()) {
                return path.toFile();
            }
            final File candidate = path.resolve(XNAT_CONF_FILE).toFile();
            if (candidate.exists()) {
                return candidate;
            }
        }
        throw new RuntimeException("Could not find a valid configuration file at any of the possible locations.");
    }

    private static List<Path> getConfigFolderPaths(final Environment environment) {
        final List<Path> configFolderPaths = new ArrayList<>();
        for (int index = 0; index < CONFIG_LOCATIONS.size(); index++) {
            final Path path = getConfigFolder(environment, CONFIG_LOCATIONS.get(index), CONFIG_PATHS.get(index));
            if (path != null) {
                if (_log.isInfoEnabled()) {
                    _log.info("Adding path {} to the list of available configuration folders.", path.toString());
                }
                configFolderPaths.add(path);
            }
        }
        if (configFolderPaths.size() == 0) {
            throw new RuntimeException("No XNAT home specified in any of the accepted locations: " + Joiner.on(", ").join(CONFIG_URLS));
        }
        return configFolderPaths;
    }

    private static Path getConfigFolder(final Environment environment, final String variable, final String relative) {
        final String url = "${" + variable + "}/" + relative;
        if (!CONFIG_URLS.contains(url)) {
            CONFIG_URLS.add(url);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Testing path for XNAT home candidate: {}", url);
        }
        final String value = environment.getProperty(variable);
        if (StringUtils.isBlank(value)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Value of environment variable {} was blank, not a candidate.", variable);
            }
            return null;
        }
        final Path candidate = Paths.get(value, relative);
        final File file      = candidate.toFile();
        if (file.exists()) {
            // If it's a directory...
            if (file.isDirectory()) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Environment variable {} resolved to path {}, which exists and is a directory, returning as XNAT home.", variable, candidate.toString());
                }
                // Then cool, just return that.
                return candidate;
            } else {
                // If it's a file, then the parent is probably a config folder, so if this is services.properties (the default) or the specific file identified by xnat.config...
                if (file.getName().equals(XNAT_CONF_FILE) || StringUtils.equals(candidate.toString(), environment.getProperty(JAVA_XNAT_CONFIG))) {
                    // So its parent is a config folder, QED.
                    return candidate.getParent();
                }
            }
        } else if (_log.isDebugEnabled()) {
            _log.debug("The path suggested by environment variable {} resolved to {}, which doesn't exist, not a candidate.", variable, candidate.toString());
        }
        return null;
    }

    private static final Logger _log = LoggerFactory.getLogger(PropertiesConfig.class);

    private static final List<String> CONFIG_URLS = new ArrayList<>();

    @Inject
    private Environment _environment;

    private final List<Path>   _configFolderPaths     = new ArrayList<>();
    private final List<String> _configFolderLocations = new ArrayList<>();
    private Path _xnatHome;
}
