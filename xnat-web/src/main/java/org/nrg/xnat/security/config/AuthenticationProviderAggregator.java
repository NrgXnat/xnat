package org.nrg.xnat.security.config;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.*;

public class AuthenticationProviderAggregator extends ArrayList<AuthenticationProvider> {

    public AuthenticationProviderAggregator(List<AuthenticationProvider> standaloneProviders, Map<String, AuthenticationProviderConfigurator> configurators) {
        ArrayList<String> providerArray = new ArrayList<>();
        String            dbName        = "Database";
        String            dbId          = "localdb";
        String            dbType        = "db";
        providerArray.add(dbType);
        HashMap<String, HashMap<String, String>> providerMap = new HashMap<>();
        providerMap.put(dbType, new HashMap<String, String>());
        providerMap.get(dbType).put("name", dbName);
        providerMap.get(dbType).put("id", dbId);
        providerMap.get(dbType).put("type", dbType);

        // Populate map of properties
        try {
            String               filenameEnd = "-provider.properties";
            final List<Resource> resources   = BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/auth/**/*" + filenameEnd);
            for (final Resource resource : resources) {
                String filename = resource.getFilename();
                String id       = filename.substring(0, (filename.length() - filenameEnd.length()));
                providerMap.put(id, new HashMap<String, String>());
                providerArray.add(id);
                final Properties provider = PropertiesLoaderUtils.loadProperties(resource);
                for (Map.Entry<Object, Object> providerProperty : provider.entrySet()) {
                    providerMap.get(id).put(providerProperty.getKey().toString(), providerProperty.getValue().toString());
                }
            }
        } catch (Exception e) {
            _log.error("", e);
        }

        // Create providers
        for (String prov : providerArray) {
            String name = providerMap.get(prov).get("name");
            String id   = providerMap.get(prov).get("id");
            String type = providerMap.get(prov).get("type");

            assert !StringUtils.isBlank(name) : "You must provide a name for all authentication provider configurations";
            assert !StringUtils.isBlank(id) : "You must provide an ID for all authentication provider configurations";
            assert !StringUtils.isBlank(type) : "You must provide a type for all authentication provider configurations";

            if (configurators.containsKey(type)) {
                AuthenticationProviderConfigurator configurator = configurators.get(type);
                addAll(configurator.getAuthenticationProviders(id, name, providerMap.get(prov)));
            }
        }

        if (standaloneProviders != null) {
            addAll(standaloneProviders);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(AuthenticationProviderAggregator.class);
}
