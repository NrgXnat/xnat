package org.nrg.xnat.security.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.*;

public class AuthenticationProviderAggregator extends ArrayList<AuthenticationProvider> {

    public AuthenticationProviderAggregator(List<AuthenticationProvider> standaloneProviders, Map<String, AuthenticationProviderConfigurator> configurators, Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("The list of authentication providers cannot be set to null.");
        }

        String commaDelineatedProviders = properties.getProperty("provider.providers.enabled");
        assert !StringUtils.isBlank(commaDelineatedProviders) : "You must specify at least one authentication provider configuration.";
        String[] providerArray=commaDelineatedProviders.split("[\\s,]+");
        HashMap<String, HashMap<String, String>> providerMap = new HashMap<>();
        for(String prov : providerArray){
            providerMap.put(prov, new HashMap<String, String>());
        }

        // Populate map of properties
        for(Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            StringTokenizer st = new StringTokenizer(key, ".");
            String provider = st.nextToken();
            if (provider.equals("provider")) {
                String name = st.nextToken();
                if(providerMap.containsKey(name)) {
                    StringBuilder providerProperty = new StringBuilder();
                    while (st.hasMoreTokens()) {
                        if (providerProperty.length() > 0) {
                            providerProperty.append(".");
                        }
                        providerProperty.append(st.nextToken());
                    }
                    providerMap.get(name).put(providerProperty.toString(), (String) entry.getValue());
                }
            }
        }

        // Create providers
        for(String prov: providerArray){
            String name = providerMap.get(prov).get("name");
            String id = providerMap.get(prov).get("id");
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
}
