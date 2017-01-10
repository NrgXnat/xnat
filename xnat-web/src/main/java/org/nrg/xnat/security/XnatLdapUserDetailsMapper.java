/*
 * web: org.nrg.xnat.security.XnatLdapUserDetailsMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.security.UserI;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class XnatLdapUserDetailsMapper extends LdapUserDetailsMapper {

    public static final String PROPERTY_PREFIX = "attributes.";
    public static final String PROPERTY_EMAIL  = PROPERTY_PREFIX + "email";
    public static final String PROPERTY_FIRST  = PROPERTY_PREFIX + "firstname";
    public static final String PROPERTY_LAST   = PROPERTY_PREFIX + "lastname";

    public XnatLdapUserDetailsMapper(final String authMethodId, final Map<String, String> properties, final XdatUserAuthService userAuthService, final SiteConfigPreferences preferences) {
        super();
        Assert.hasText(authMethodId, "You must provide an authentication method ID.");
        Assert.notEmpty(properties, "You must provide the authentication provider properties.");
        if (_log.isInfoEnabled()) {
            _log.info("Creating user details mapper with the auth method ID [" + authMethodId + "] and " + (properties != null && properties.size() > 0 ? "mapping properties: " + properties.toString() : "default mapping properties"));
        }
        _authMethodId = authMethodId;
        if (properties == null || properties.size() == 0) {
            _properties = new HashMap<String, String>(3) {{
                put(PROPERTY_EMAIL, "mail");
                put(PROPERTY_FIRST, "givenName");
                put(PROPERTY_LAST, "sn");
            }};
        } else {
            if (!properties.containsKey(PROPERTY_EMAIL)) {
                properties.put(PROPERTY_EMAIL, "mail");
            }
            if (!properties.containsKey(PROPERTY_FIRST)) {
                properties.put(PROPERTY_FIRST, "givenName");
            }
            if (!properties.containsKey(PROPERTY_LAST)) {
                properties.put(PROPERTY_LAST, "sn");
            }
            _properties = properties;
        }
        _userAuthService = userAuthService;
        _preferences = preferences;
    }

    @Override
    public UserI mapUserFromContext(DirContextOperations ctx, String username, final Collection<? extends GrantedAuthority> authorities) {
        UserDetails user = super.mapUserFromContext(ctx, username, authorities);

        String email = (String) ctx.getObjectAttribute(_properties.get(PROPERTY_EMAIL));
        String firstname = (String) ctx.getObjectAttribute(_properties.get(PROPERTY_FIRST));
        String lastname = (String) ctx.getObjectAttribute(_properties.get(PROPERTY_LAST));

        UserI userDetails = ContextService.getInstance().getBean(XdatUserAuthService.class).getUserDetailsByNameAndAuth(user.getUsername(), XdatUserAuthService.LDAP, _authMethodId, email, lastname, firstname);

        try {
            final UserI xdatUser = Users.getUser(userDetails.getUsername());
            if ((!XDAT.getSiteConfigPreferences().getEmailVerification() || xdatUser.isVerified()) && userDetails.getAuthorization().isEnabled()) {
                return userDetails;
            } else {
                throw new NewLdapAccountNotAutoEnabledException(
                        "Successful first-time authentication via LDAP, but accounts are not auto-enabled or email verification required.  We'll treat this the same as we would a user registration"
                        , userDetails
                );
            }
        } catch (Exception e) {
            throw new NewLdapAccountNotAutoEnabledException(
                    "Successful first-time authentication via LDAP, but accounts are not auto-enabled or email verification required.  We'll treat this the same as we would a user registration"
                    , userDetails
            );
        }
    }

    private static final Log _log = LogFactory.getLog(XnatLdapUserDetailsMapper.class);

    private final XdatUserAuthService   _userAuthService;
    private final SiteConfigPreferences _preferences;
    private final String                _authMethodId;
    private final Map<String, String>   _properties;
}
