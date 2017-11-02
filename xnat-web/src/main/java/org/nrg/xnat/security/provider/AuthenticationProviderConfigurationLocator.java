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
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.services.XdatUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.xnat.initialization.XnatWebAppInitializer.EMPTY_ARRAY;

@Component
@Slf4j
public class AuthenticationProviderConfigurationLocator {
    @Autowired
    public AuthenticationProviderConfigurationLocator(final ConfigPaths configPaths, final SerializerService serializer, final MessageSource messageSource) {
        _configPaths = configPaths;
        _messageSource = messageSource;
        _emptyName = _messageSource.getMessage("authProviders.none.defaults.name", new Object[0], "", Locale.getDefault());

        final List<Properties> definitions = getDefinitions(_messageSource, _emptyName);
        for (final Properties definition : definitions) {
            final String type = definition.getProperty("type");
            if (_definitionsByType.containsKey(type)) {
                final Properties existing = _definitionsByType.get(type);
                log.warn("The definition for {} specifies its type as {}, but that type is already defined by {} and mapped to the implementation class {}. Duplicate provider type definitions are not allowed.", definition.getProperty("id"), definition.getProperty("type"), existing.getProperty("id"), existing.getProperty("implementation"));
            } else {
                _definitionsByType.put(type, definition);
            }
        }
    }

    /**
     * Finds all {@link XnatAuthenticationProvider XNAT authentication provider configurations} defined in properties files named <b>*-provider.properties</b>
     * and found in the configuration folder <b>auth</b> or on the classpath in <b>META-INF/xnat/security</b> or one of its subfolders. Paired with {@link
     * #getProviders(List)}, you can get a list of defined providers, filter out any already existing providers or specifically black-listed providers, then
     * create only the remaining providers.
     *
     * @return A list of provider definitions.
     */
    public List<Properties> getProviderDefinitions() {
        final List<Properties> providers = new ArrayList<>();

        // Populate map of properties for each provider
        final ArrayList<String> authFilePaths = new ArrayList<>();
        //First see if there are any properties files in config/auth
        for (final Path currPath : _configPaths) {
            final Path authPath = Paths.get(currPath.toString(), "auth");

            log.debug("AuthPath is {}", authPath.toString());
            final Collection<File> files = FileUtils.listFiles(authPath.toFile(), PROVIDER_FILENAME_FILTER, DirectoryFileFilter.DIRECTORY);
            for (final File file : files) {
                if (!authFilePaths.contains(file.toString())) {
                    authFilePaths.add(file.toString());
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

        if (providers.isEmpty()) {
            final Properties provider = new Properties();
            provider.put("name", _messageSource.getMessage("authProviders.localdb.defaults.name", EMPTY_ARRAY, "Database", Locale.getDefault()));
            provider.put("id", "db");
            provider.put("type", XdatUserAuthService.LOCALDB);
            providers.add(provider);
        }

        return providers;
    }

    /**
     * Gets an instance of each of the {@link XnatAuthenticationProvider XNAT authentication provider configurations} in the submitted definitions.
     * Paired with {@link #getProviderDefinitions()}, you can get a list of defined providers, filter out any already existing providers or specifically
     * black-listed providers, then create only the remaining providers.
     *
     * @param definitions The list of provider definitions
     *
     * @return Any {@link XnatAuthenticationProvider authentication provider instances} created from the list of definitions.
     */
    public List<XnatAuthenticationProvider> getProviders(final List<Properties> definitions) {
        final List<XnatAuthenticationProvider> providers = new ArrayList<>();

        // Create providers from provider list
        for (final Properties definition : definitions) {
            final String name           = definition.getProperty("name");
            final String id             = definition.getProperty("id");
            final String type           = definition.getProperty("type", "");
            final String implementation = definition.getProperty("implementation", "");

            assert StringUtils.isNotBlank(name) : "You must provide a name for all authentication provider configurations";
            assert StringUtils.isNotBlank(id) : "You must provide an ID for all authentication provider configurations";
            assert StringUtils.isNotBlank(type) || StringUtils.isNotBlank(implementation) : "You must provide a type or implementation class for all authentication provider configurations";

            final XnatAuthenticationProvider provider = getProvider(definition);
            if (provider != null) {
                providers.add(provider);
            }
        }

        Collections.sort(providers, new Comparator<AuthenticationProvider>() {
            public int compare(AuthenticationProvider o1, AuthenticationProvider o2) {
                if (XnatAuthenticationProvider.class.isAssignableFrom(o1.getClass())) {
                    if (XnatAuthenticationProvider.class.isAssignableFrom(o2.getClass())) {
                        if (((XnatAuthenticationProvider) o1).getOrder() == ((XnatAuthenticationProvider) o2).getOrder()) {
                            return 0;
                        }
                        return ((XnatAuthenticationProvider) o1).getOrder() < ((XnatAuthenticationProvider) o2).getOrder() ? -1 : 1;
                    } else {
                        return 1;
                    }
                } else {
                    if (XnatAuthenticationProvider.class.isAssignableFrom(o2.getClass())) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });

        return providers;
    }

    private XnatAuthenticationProvider getProvider(final Properties definition) {
        final String implementationClass = definition.getProperty("implementation", _definitionsByType.get(definition.getProperty("type")).getProperty("implementation"));
        if (StringUtils.isBlank(implementationClass)) {
            log.error("There is no implementation specified explicitly or through the provider type for the provider definition {}", definition.getProperty("id"));
            return null;
        }
        try {
            final Class<? extends XnatAuthenticationProvider> providerClass = Class.forName(implementationClass).asSubclass(XnatAuthenticationProvider.class);
            final Constructor<? extends XnatAuthenticationProvider> constructor = getConstructorByPriority(providerClass);
            if (constructor == null) {
                log.warn("Tried to create an instance of the {} authentication provider as defined for ID {}, but couldn't find one of the standard constructors. This may be OK: you can create it explicitly.", implementationClass, definition.getProperty("id"));
            } else {
                try {
                    final Class<?>[] types = constructor.getParameterTypes();
                    switch (types.length) {
                        case 0:
                            return constructor.newInstance();

                        case 2:
                            return constructor.newInstance(definition.getProperty("name"), definition.getProperty("id"));

                        case 3:
                            return constructor.newInstance(definition.getProperty("name"), definition.getProperty("id"), definition.getProperty("type"));

                        case 4:
                            return constructor.newInstance(definition.getProperty("name"), definition.getProperty("id"), definition.getProperty("type"), Integer.parseInt(definition.getProperty("order")));

                        case 1:
                            if (Properties.class.isAssignableFrom(types[0])) {
                                return constructor.newInstance(definition);
                            }
                            return constructor.newInstance(definition.getProperty("name"));
                    }
                } catch (IllegalAccessException e) {
                    log.error("The located constructor is inaccessible", e);
                } catch (InstantiationException | InvocationTargetException e) {
                    log.error("The located constructor resulted in an error", e);
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("The authentication provider ID {} specified an implementation class that couldn't be found: {}", definition.getProperty("id"), implementationClass);
        }
        return null;
    }

    private static Constructor<? extends XnatAuthenticationProvider> getConstructorByPriority(final Class<? extends XnatAuthenticationProvider> providerClass) {
        try {
            return providerClass.getConstructor(Properties.class);
        } catch (NoSuchMethodException e) {
            try {
                return providerClass.getConstructor(String.class, String.class, String.class, Integer.class);
            } catch (NoSuchMethodException e1) {
                try {
                    return providerClass.getConstructor(String.class, String.class, String.class);
                } catch (NoSuchMethodException e2) {
                    try {
                        return providerClass.getConstructor(String.class, String.class);
                    } catch (NoSuchMethodException e3) {
                        try {
                            return providerClass.getConstructor(String.class);
                        } catch (NoSuchMethodException e4) {
                            try {
                                return providerClass.getConstructor();
                            } catch (NoSuchMethodException e5) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }

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

    private final ConfigPaths       _configPaths;
    private final MessageSource     _messageSource;
    private final String            _emptyName;

    private final Map<String, Properties> _definitionsByType = new HashMap<>();
}
