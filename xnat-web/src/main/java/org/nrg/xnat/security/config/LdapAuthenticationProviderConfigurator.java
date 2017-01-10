/*
 * web: org.nrg.xnat.security.config.LdapAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.config;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.XnatLdapAuthoritiesPopulator;
import org.nrg.xnat.security.XnatLdapUserDetailsMapper;
import org.nrg.xnat.security.provider.XnatLdapAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LdapAuthenticationProviderConfigurator extends AbstractAuthenticationProviderConfigurator {
    @Autowired
    public LdapAuthenticationProviderConfigurator(final XdatUserAuthService userAuthService, final SiteConfigPreferences preferences) {
        super();
        setConfiguratorId("ldap");
        _userAuthService = userAuthService;
        _preferences = preferences;
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name) {
        throw new NotImplementedException("You must provide LDAP properties in order to configure an LDAP connection.");
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name, Map<String, String> properties) {
        try {
            XnatLdapAuthenticationProvider ldapAuthProvider = new XnatLdapAuthenticationProvider(getBindAuthenticator(properties, getLdapContextSource(properties)), new XnatLdapAuthoritiesPopulator());
            ldapAuthProvider.setUserDetailsContextMapper(new XnatLdapUserDetailsMapper(id, properties, _userAuthService, _preferences));
            ldapAuthProvider.setName(name);
            ldapAuthProvider.setProviderId(id);
            if (properties.get("order") != null) {
                ldapAuthProvider.setOrder(Integer.parseInt(properties.get("order")));
            }
            return Arrays.asList(new AuthenticationProvider[] { ldapAuthProvider });
        } catch (Exception exception) {
            _log.error("Something went wrong when configuring the LDAP authentication provider", exception);
            return null;
        }
    }

    private BindAuthenticator getBindAuthenticator(final Map<String, String> properties, final DefaultSpringSecurityContextSource ldapServer) {
        BindAuthenticator ldapBindAuthenticator = new BindAuthenticator(ldapServer);
        ldapBindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(properties.get("search.base"), properties.get("search.filter"), ldapServer));
        return ldapBindAuthenticator;
    }

    private DefaultSpringSecurityContextSource getLdapContextSource(final Map<String, String> properties) throws Exception {
        return new DefaultSpringSecurityContextSource(properties.get("address")) {{
            setUserDn(properties.get("userdn"));
            setPassword(properties.get("password"));
            afterPropertiesSet();
        }};
    }

    private static final Log _log = LogFactory.getLog(LdapAuthenticationProviderConfigurator.class);

    private final XdatUserAuthService _userAuthService;
    private final SiteConfigPreferences _preferences;
}
