package org.nrg.xnat.security.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static org.nrg.xdat.services.XdatUserAuthService.LOCALDB;
import static org.nrg.xnat.security.provider.ProviderAttributes.*;

@Component
@Slf4j
public class AuthenticationProviderConfigurationLocator {
    public AuthenticationProviderConfigurationLocator(final ConfigPaths configPaths, final MessageSource messageSource) {
        final List<Properties> definitions = getProviderDefinitions(configPaths, messageSource);

        log.info("Found {} provider definitions, preparing to collate them", definitions.size());
        for (final Properties definition : definitions) {
            final String providerId = definition.getProperty(PROVIDER_ID);

            if (_definitionsById.containsKey(providerId)) {
                throw new RuntimeException("There's already a provider definition for auth method '" + _definitionsById.get(providerId) + "' with the ID '" + providerId + "'. Provider IDs must be distinct even across different provider implementations.");
            }

            final String authMethod = definition.getProperty(PROVIDER_AUTH_METHOD);
            log.debug("Found provider with ID '{}' and auto method '{}'", providerId, authMethod);

            final Map<String, ProviderAttributes> authMethodDefinitions;
            if (_definitionsByAuthMethod.containsKey(authMethod)) {
                authMethodDefinitions = _definitionsByAuthMethod.get(authMethod);
            } else {
                authMethodDefinitions = new HashMap<>();
                _definitionsByAuthMethod.put(authMethod, authMethodDefinitions);
            }

            final ProviderAttributes attributes = new ProviderAttributes(definition);
            authMethodDefinitions.put(providerId, attributes);
            _definitionsById.put(providerId, attributes);
        }
    }

    /**
     * Gets a map of all providers for the indicated authentication method stored by the provider IDs.
     *
     * @param authMethod The authentication method of the providers you want to retrieve.
     *
     * @return A map of all providers for the indicated authentication method. If no providers for that
     *         method are defined, this returns an empty list.
     */
    @Nonnull
    public Map<String, ProviderAttributes> getProviderDefinitionsByAuthMethod(final String authMethod) {
        if (StringUtils.isBlank(authMethod) || !_definitionsByAuthMethod.containsKey(authMethod)) {
            log.info("Request for providers of auth method '{}' can't be fulfilled, I don't got that.", authMethod);
            return Collections.emptyMap();
        }
        final Map<String, ProviderAttributes> providers = _definitionsByAuthMethod.get(authMethod);
        if (log.isDebugEnabled()) {
            log.debug("Request for providers of auth method '{}' found {} definitions: {}", authMethod, providers.size(), StringUtils.join(providers.keySet(), ", "));
        }
        return providers;
    }

    /**
     * Gets the attributes for the provider with the specified ID. If no provider with that ID is defined
     * on the system, this returns null.
     *
     * @param providerId The ID of the provider to be retrieved.
     *
     * @return The attributes for the provider with the specified ID, null if no provider with that ID is
     *         defined on the system.
     */
    @SuppressWarnings("unused")
    @Nullable
    public ProviderAttributes getProviderDefinition(final String providerId) {
        if (StringUtils.isBlank(providerId) || !_definitionsById.containsKey(providerId)) {
            log.info("Request for provider with ID '{}' can't be fulfilled, I don't got that.", providerId);
            return null;
        }
        log.debug("Request for provider with ID '{}' found a corresponding definition", providerId);
        return _definitionsById.get(providerId);
    }

    /**
     * Finds all {@link XnatAuthenticationProvider XNAT authentication provider configurations} defined in properties files named <b>*-provider.properties</b>
     * and found in the configuration folder <b>auth</b> or on the classpath in <b>META-INF/xnat/security</b> or one of its subfolders.
     *
     * @param configPaths   The config paths locator.
     * @param messageSource The message source.
     *
     * @return A list of provider definitions.
     */
    private List<Properties> getProviderDefinitions(final Iterable<? extends Path> configPaths, final MessageSource messageSource) {
        final List<Properties> providers = new ArrayList<>();

        // Populate map of properties for each provider
        final Set<String> authFilePaths = new HashSet<>();
        //First see if there are any properties files in config/auth
        for (final Path currPath : configPaths) {
            final Path authPath = currPath.resolve("auth");

            log.debug("Searching auth path '{}' for provider definitions", authPath);
            final File directory = authPath.toFile();
            if (directory.exists() && directory.isDirectory()) {
                final Collection<File> files = FileUtils.listFiles(directory, PROVIDER_FILENAME_FILTER, DirectoryFileFilter.DIRECTORY);
                log.debug("Found {} files under auth path '{}'", files.size(), authPath);
                authFilePaths.addAll(Lists.transform(new ArrayList<>(files), new Function<File, String>() {
                    @Nullable
                    @Override
                    public String apply(final File file) {
                        final String path = file.toString();
                        log.trace("Adding path '{}' to auth file paths", path);
                        return path;
                    }
                }));
            }
        }
        if (!authFilePaths.isEmpty()) {
            //If there were provider properties files in config/auth, use them to populate provider list
            for (final String authFilePath : authFilePaths) {
                log.debug("Accessing properties from auth file '{}'", authFilePath);
                final Properties properties = new Properties();
                try (final InputStream inputStream = new FileInputStream(authFilePath)) {
                    properties.load(inputStream);
                    log.debug("Found {} properties in auth path '{}'", properties.size(), authFilePath);
                    final Properties provider = new Properties();
                    for (final Map.Entry<Object, Object> providerProperty : properties.entrySet()) {
                        final String key = providerProperty.getKey().toString();
                        final String value   = providerProperty.getValue().toString();
                        log.debug("Trying to add property '{}' with value '{}'",  key, value);
                        provider.put(key, value);
                    }
                    providers.add(provider);
                    log.debug("Added provider (name: {}, ID: {}, auth method: {}).", provider.get(PROVIDER_NAME), provider.get(PROVIDER_ID), provider.get(PROVIDER_AUTH_METHOD));
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
                log.debug("Loading properties from resource '{}'", resource.toString());
                providers.addAll(collate(PropertiesLoaderUtils.loadProperties(resource)));
            }
        } catch (IOException e) {
            log.warn("Tried to find plugin authentication provider definitions, but got an error trying to search {}", PROVIDER_CLASSPATH, e.getMessage());
        }

        if (providers.isEmpty()) {
            final Properties provider = new Properties();
            provider.put(PROVIDER_NAME, messageSource.getMessage("authProviders.localdb.defaults.name", EMPTY_OBJECT_ARRAY, "Database", Locale.getDefault()));
            provider.put(PROVIDER_ID, LOCALDB);
            provider.put(PROVIDER_AUTH_METHOD, LOCALDB);
            providers.add(provider);
            log.info("No provider definitions were found, so the default provider is being added: {}", provider.toString());
        }

        log.debug("Returning {} located provider definitions", providers.size());
        return providers;
    }

    @SuppressWarnings("unused")
    // TODO: Implement YAML definitions for providers.
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
                        properties.setProperty(PROVIDER_NAME, getDefaultName(id, definition, messageSource, emptyName));
                        properties.setProperty(PROVIDER_ID, id);
                        properties.setProperty(PROVIDER_VISIBLE, definition.hasNonNull(VISIBLE_BY_DEFAULT) ? definition.asText() : "false");
                        properties.setProperty(IMPLEMENTATION, definition.asText(IMPLEMENTATION));
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

    private final Map<String, Map<String, ProviderAttributes>> _definitionsByAuthMethod = new HashMap<>();
    private final Map<String, ProviderAttributes>              _definitionsById         = new HashMap<>();
}
