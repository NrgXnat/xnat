package org.nrg.xnat.security.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.services.XdatUserAuthService;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;

@Slf4j
public class AuthenticationProviderConfigurationLocator {
    public AuthenticationProviderConfigurationLocator(final String providerType, final ConfigPaths configPaths, final MessageSource messageSource) {
        final List<Properties> definitions = getProviderDefinitions(providerType, configPaths, messageSource);
        for (final Properties definition : definitions) {
            final String type = definition.getProperty("type");
            if (StringUtils.equals(providerType, type)) {
                final String id = definition.getProperty("id");
                if (_definitions.containsKey("id")) {
                    throw new RuntimeException("There's already a provider definition with the ID {}, can't have duplicate IDs.");
                }
                _definitions.put(id, definition);
            }
        }
    }

    public Map<String, Properties> getProviderDefinitions() {
        return _definitions;
    }

    @SuppressWarnings("unused")
    public Properties getProviderDefinition(final String providerId) {
        if (StringUtils.isBlank(providerId) || !_definitions.containsKey(providerId)) {
            return null;
        }
        return _definitions.get(providerId);
    }

    /**
     * Finds all {@link XnatAuthenticationProvider XNAT authentication provider configurations} defined in properties files named <b>*-provider.properties</b>
     * and found in the configuration folder <b>auth</b> or on the classpath in <b>META-INF/xnat/security</b> or one of its subfolders.
     *
     * @param providerType  The type of provider definition to be retrieved.
     * @param configPaths   The config paths locator.
     * @param messageSource The message source.
     *
     * @return A list of provider definitions.
     */
    private List<Properties> getProviderDefinitions(final String providerType, final Iterable<? extends Path> configPaths, final MessageSource messageSource) {
        final List<Properties> providers = new ArrayList<>();

        // Populate map of properties for each provider
        final ArrayList<String> authFilePaths = new ArrayList<>();
        //First see if there are any properties files in config/auth
        for (final Path currPath : configPaths) {
            final Path authPath = Paths.get(currPath.toString(), "auth");

            log.debug("AuthPath is {}", authPath.toString());
            final File directory = authPath.toFile();
            if (directory.exists() && directory.isDirectory()) {
                final Collection<File> files = FileUtils.listFiles(directory, PROVIDER_FILENAME_FILTER, DirectoryFileFilter.DIRECTORY);
                for (final File file : files) {
                    if (!authFilePaths.contains(file.toString())) {
                        authFilePaths.add(file.toString());
                    }
                }
            }
        }
        if (!authFilePaths.isEmpty()) {
            //If there were provider properties files in config/auth, use them to populate provider list
            for (final String authFilePath : authFilePaths) {
                log.debug("Accessing properties from " + authFilePath);
                final Properties properties = new Properties();
                try (final InputStream inputStream = new FileInputStream(authFilePath)) {
                    properties.load(inputStream);
                    log.debug("Found " + properties.size() + " properties.");
                    final Properties provider = new Properties();
                    for (final Map.Entry<Object, Object> providerProperty : properties.entrySet()) {
                        log.debug("Trying to add property " + providerProperty.getKey().toString() + " with value " + providerProperty.getValue().toString());
                        provider.put(providerProperty.getKey().toString(), providerProperty.getValue().toString());
                    }
                    providers.add(provider);
                    log.debug("Added provider (name:" + provider.get("name") + ", id:" + provider.get("id") + ", type:" + provider.get("type") + ").");
                } catch (FileNotFoundException e) {
                    log.info("Tried to load properties from found properties file at {}, but got a FileNotFoundException", authFilePath);
                } catch (IOException e) {
                    log.warn("Tried to load properties from found properties file at {}, but got an error trying to load", authFilePath, e.getMessage());
                }
            }
        }

        //If no properties files were found in the config directories, look for properties files that might be from plugins
        try {
            final List<Resource> resources = BasicXnatResourceLocator.getResources(PROVIDER_CLASSPATH);
            for (final Resource resource : resources) {
                providers.addAll(collate(PropertiesLoaderUtils.loadProperties(resource)));
            }
        } catch (IOException e) {
            log.warn("Tried to find plugin authentication provider definitions, but got an error trying to search {}", PROVIDER_CLASSPATH, e.getMessage());
        }

        if (providers.isEmpty() && StringUtils.equals(providerType, XdatUserAuthService.LOCALDB)) {
            final Properties provider = new Properties();
            provider.put("name", messageSource.getMessage("authProviders.localdb.defaults.name", EMPTY_OBJECT_ARRAY, "Database", Locale.getDefault()));
            provider.put("id", "db");
            provider.put("type", XdatUserAuthService.LOCALDB);
            providers.add(provider);
        }

        return providers;
    }

    @SuppressWarnings("unused")
    private static List<Properties> getDefinitions(final MessageSource messageSource, final String emptyName) {
        final List<Properties> providerImplementations = new ArrayList<>();
        try {
            final List<Resource> providerMaps = BasicXnatResourceLocator.getResources(PROVIDER_MAPS);
            for (final Resource providerMap : providerMaps) {
                try (final InputStream inputStream = providerMap.getInputStream()) {
                    final JsonNode         mapping = new ObjectMapper(new YAMLFactory()).readTree(inputStream);
                    final Iterator<String> ids     = mapping.get(IMPLEMENTATIONS).fieldNames();
                    while (ids.hasNext()) {
                        final String   id         = ids.next();
                        final JsonNode definition = mapping.get(id);
                        if (!definition.hasNonNull(IMPLEMENTATION)) {
                            throw new RuntimeException("Every authentication provider definition must include the implementing class, but the default for " + id + " defined in the resource " + providerMap.getURI().toString() + " did not.");
                        }

                        final Properties properties = new Properties();
                        properties.setProperty("name", getDefaultName(id, definition, messageSource, emptyName));
                        properties.setProperty("type", id);
                        properties.setProperty("visible", definition.hasNonNull(VISIBLE_BY_DEFAULT) ? definition.asText() : "false");
                        properties.setProperty("implementation", definition.asText(IMPLEMENTATION));
                        providerImplementations.add(properties);
                    }
                } catch (IOException e) {
                    log.error("Unable to read the provider map " + providerMap.getURI() + " due to an I/O error", e);
                }
            }
        } catch (IOException e) {
            log.error("Unable to find provider maps that match the pattern " + PROVIDER_MAPS + " due to an I/O error", e);
        }
        return providerImplementations;
    }

    private static String getDefaultName(final String id, final JsonNode definition, final MessageSource messageSource, final String emptyName) {
        if (definition.hasNonNull(DEFAULT_NAME_KEY)) {
            final String name = messageSource.getMessage(definition.findValue(DEFAULT_NAME_KEY).asText(), new Object[0], "", Locale.getDefault());
            if (StringUtils.isNotBlank(name)) {
                return name;
            }
        }
        if (definition.hasNonNull(DEFAULT_NAME)) {
            final String name = definition.findValue(DEFAULT_NAME_KEY).asText();
            if (StringUtils.isNotBlank(name)) {
                return name;
            }
        }
        return messageSource.getMessage(String.format(MESSAGE_KEY_PATTERN, id), new Object[0], emptyName, Locale.getDefault());
    }

    private static Collection<Properties> collate(final Properties properties) {
        final Map<String, Properties> collated = new HashMap<>();
        for (final String key : properties.stringPropertyNames()) {
            final Matcher matcher    = PROPERTY_NAME_VALUE_PATTERN.matcher(key);
            final String  providerId = matcher.group("providerId");
            final String  property   = matcher.group("property");
            final String  value      = properties.getProperty(key);
            if (!collated.containsKey(providerId)) {
                collated.put(providerId, new Properties());
            }
            collated.get(providerId).setProperty(property, value);
        }
        return collated.values();
    }

    private static final String          IMPLEMENTATIONS             = "implementations";
    private static final String          DEFAULT_NAME_KEY            = "default-name-key";
    private static final String          DEFAULT_NAME                = "default-name";
    private static final String          VISIBLE_BY_DEFAULT          = "visible-by-default";
    private static final String          IMPLEMENTATION              = "implementation";
    private static final String          MESSAGE_KEY_PATTERN         = "authProviders.%s.defaults.name";
    private static final String          PROVIDER_MAPS               = "classpath*:META-INF/xnat/security/**/*-provider-map.yaml";
    private static final String          PROVIDER_FILENAME           = "*-provider.properties";
    private static final String          PROVIDER_CLASSPATH          = "classpath*:META-INF/xnat/auth/**/" + PROVIDER_FILENAME;
    private static final RegexFileFilter PROVIDER_FILENAME_FILTER    = new RegexFileFilter("^." + PROVIDER_FILENAME);
    private static final Pattern         PROPERTY_NAME_VALUE_PATTERN = Pattern.compile("^(?:provider\\.)?(?<providerId>[A-z0-9_-]+)\\.(?<property>.*)$");

    private final Map<String, Properties> _definitions = new HashMap<>();
}
