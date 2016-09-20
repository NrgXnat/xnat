/*
 * web: org.nrg.xnat.security.config.DatabaseAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.config;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseAuthenticationProviderConfigurator extends AbstractAuthenticationProviderConfigurator {
    @Autowired
    public DatabaseAuthenticationProviderConfigurator(final XnatDatabaseUserDetailsService userDetailsService, final SiteConfigPreferences preferences) {
        super();
        _userDetailsService = userDetailsService;
        _preferences = preferences;
        setConfiguratorId("db");
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name) {
        List<AuthenticationProvider> providers = new ArrayList<>();

        ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("salt");

        XnatDatabaseAuthenticationProvider sha2DatabaseAuthProvider = new XnatDatabaseAuthenticationProvider(_preferences.getEmailVerification());
        sha2DatabaseAuthProvider.setUserDetailsService(_userDetailsService);
        sha2DatabaseAuthProvider.setPasswordEncoder(new ShaPasswordEncoder(256));
        sha2DatabaseAuthProvider.setName(name);
        sha2DatabaseAuthProvider.setProviderId(id);
        sha2DatabaseAuthProvider.setSaltSource(saltSource);
        providers.add(sha2DatabaseAuthProvider);

        return providers;
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name, Map<String, String> properties) {
        List<AuthenticationProvider> provs = getAuthenticationProviders(id, name);
        for(AuthenticationProvider prov : provs){
            if(XnatAuthenticationProvider.class.isAssignableFrom(prov.getClass())){
                if (properties.get("order") != null) {
                    ((XnatAuthenticationProvider)prov).setOrder(Integer.parseInt(properties.get("order")));
                }
            }
        }
        return provs;
    }

    private final XnatDatabaseUserDetailsService _userDetailsService;
    private final SiteConfigPreferences          _preferences;
}
