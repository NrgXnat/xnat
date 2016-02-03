/*
 * org.nrg.xnat.security.config.LdapAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.config;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xnat.security.XnatLdapAuthoritiesPopulator;
import org.nrg.xnat.security.XnatLdapUserDetailsMapper;
import org.nrg.xnat.security.provider.XnatLdapAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LdapAuthenticationProviderConfigurator extends AbstractAuthenticationProviderConfigurator {
    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name) {
        throw new NotImplementedException("You must provide LDAP properties in order to configure an LDAP connection.");
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name, Map<String, String> properties) {
        try {
            XnatLdapAuthenticationProvider ldapAuthProvider = new XnatLdapAuthenticationProvider(getBindAuthenticator(properties, getLdapContextSource(properties)), new XnatLdapAuthoritiesPopulator());
            ldapAuthProvider.setUserDetailsContextMapper(new XnatLdapUserDetailsMapper(id, properties));
            ldapAuthProvider.setName(name);
            ldapAuthProvider.setProviderId(id);
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
}
