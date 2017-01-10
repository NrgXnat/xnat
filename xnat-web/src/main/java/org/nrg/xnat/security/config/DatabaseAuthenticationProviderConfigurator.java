/*
 * web: org.nrg.xnat.security.config.DatabaseAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.config;

import org.nrg.xdat.security.helpers.Users;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseAuthenticationProviderConfigurator extends AbstractAuthenticationProviderConfigurator {
    @Autowired
    public DatabaseAuthenticationProviderConfigurator(final XnatDatabaseUserDetailsService userDetailsService) {
        super();
        _userDetailsService = userDetailsService;
        setConfiguratorId("db");
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name) {
        List<AuthenticationProvider> providers = new ArrayList<>();

        ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("salt");

        final XnatDatabaseAuthenticationProvider sha2DatabaseAuthProvider = new XnatDatabaseAuthenticationProvider();
        sha2DatabaseAuthProvider.setUserDetailsService(_userDetailsService);
        sha2DatabaseAuthProvider.setPasswordEncoder(Users.getEncoder());
        sha2DatabaseAuthProvider.setName(name);
        sha2DatabaseAuthProvider.setProviderId(id);
        sha2DatabaseAuthProvider.setSaltSource(saltSource);
        providers.add(sha2DatabaseAuthProvider);

        return providers;
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name, Map<String, String> properties) {
        final List<AuthenticationProvider> providers = getAuthenticationProviders(id, name);
        for(final AuthenticationProvider provider : providers){
            if(XnatAuthenticationProvider.class.isAssignableFrom(provider.getClass())){
                if (properties.get("order") != null) {
                    ((XnatAuthenticationProvider)provider).setOrder(Integer.parseInt(properties.get("order")));
                }
            }
        }
        return providers;
    }

    private final XnatDatabaseUserDetailsService _userDetailsService;
}
