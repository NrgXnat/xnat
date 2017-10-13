/*
 * web: org.nrg.xnat.security.config.AuthenticationProviderAggregator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.AuthenticationProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class AuthenticationProviderAggregator extends ArrayList<AuthenticationProvider> {
    public AuthenticationProviderAggregator(final List<AuthenticationProvider> standaloneProviders, final Map<String, AuthenticationProviderConfigurator> configurators, final ConfigPaths configFolderPaths) {
        final List<Map<String, String>> providers = new ArrayList<>();

        // Populate map of properties for each provider
        try {
            final ArrayList<String> authFilePaths = new ArrayList<>();
            try {
                //First see if there are any properties files in config/auth
                for (final Path currPath : configFolderPaths) {
                    final Path authPath = Paths.get(currPath.toString(), "auth");

                    log.debug("AuthPath is {}", authPath.toString());
                    final Collection<File> files = FileUtils.listFiles(authPath.toFile(),
                                                                       new RegexFileFilter("^." + PROVIDER_FILENAME),
                                                                       DirectoryFileFilter.DIRECTORY);
                    for (final File file : files) {
                        if (!authFilePaths.contains(file.toString())) {
                            authFilePaths.add(file.toString());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not find auth properties files in config/auth.");
            }
            if (!authFilePaths.isEmpty()) {
                //If there were provider properties files in config/auth, use them to populate provider list
                for (final String authFilePath : authFilePaths) {
                    log.debug("Accessing properties from " + authFilePath);
                    final Properties properties = new Properties();
                    try (final InputStream inputStream = new FileInputStream(authFilePath)) {
                        properties.load(inputStream);
                    }
                    log.debug("Found " + properties.size() + " properties.");
                    final HashMap<String, String> newProv = new HashMap<>();
                    for (final Map.Entry<Object, Object> providerProperty : properties.entrySet()) {
                        log.debug("Trying to add property " + providerProperty.getKey().toString() + " with value " + providerProperty.getValue().toString());
                        newProv.put(providerProperty.getKey().toString(), providerProperty.getValue().toString());
                    }
                    providers.add(newProv);
                    log.debug("Added provider (name:" + newProv.get("name") + ", id:" + newProv.get("id") + ", type:" + newProv.get("type") + ").");
                }
            }

            //If the provider list is still empty, try to get providers from plugins
            if (providers.isEmpty()) {
                //If no properties files were found in the config directories, look for properties files that might be from plugins
                final List<Resource> resources = BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/auth/**/" + PROVIDER_FILENAME);
                if (resources == null || resources.isEmpty()) {
                    final Map<String, String> dbProv = new HashMap<>();
                    dbProv.put("name", "Database");
                    dbProv.put("id", "localdb");
                    dbProv.put("type", "db");
                    providers.add(dbProv);
                } else {
                    for (final Resource resource : resources) {
                        HashMap<String, String> newProv = new HashMap<>();

                        final Properties provider = PropertiesLoaderUtils.loadProperties(resource);
                        for (Map.Entry<Object, Object> providerProperty : provider.entrySet()) {
                            newProv.put(providerProperty.getKey().toString(), providerProperty.getValue().toString());
                        }
                        providers.add(newProv);
                    }
                }
            }


        } catch (Exception e) {
            log.error("Error getting authentication provider properties", e);
        }

        // Create providers from provider list
        for (final Map<String, String> provider : providers) {
            String name = provider.get("name");
            String id   = provider.get("id");
            String type = provider.get("type");

            assert !StringUtils.isBlank(name) : "You must provide a name for all authentication provider configurations";
            assert !StringUtils.isBlank(id) : "You must provide an ID for all authentication provider configurations";
            assert !StringUtils.isBlank(type) : "You must provide a type for all authentication provider configurations";

            if (configurators.containsKey(type)) {
                addAll(configurators.get(type).getAuthenticationProviders(id, name, provider));
            }
        }

        if (standaloneProviders != null) {
            addAll(standaloneProviders);
        }

        Collections.sort(this, new Comparator<AuthenticationProvider>() {
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
    }

    private static final String PROVIDER_FILENAME = "*-provider.properties";
}
