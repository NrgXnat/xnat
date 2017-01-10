/*
 * web: org.nrg.xnat.security.XnatLdapContextMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xdat.services.XdatUserAuthService;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;

public class XnatLdapContextMapper implements UserDetailsContextMapper {

    public XnatLdapContextMapper(final XdatUserAuthService service) {
        this(service, "");
    }

    public XnatLdapContextMapper(final XdatUserAuthService service, final String authMethodId) {
        super();
        _service = service;
        _authMethodId = authMethodId;
    }

    @Override
    public UserDetails mapUserFromContext(final DirContextOperations ctx, final String username, final Collection<? extends GrantedAuthority> authorities) {
        String email = ctx.getObjectAttribute("mail").toString();
        return _service.getUserDetailsByNameAndAuth(username, XdatUserAuthService.LDAP, _authMethodId, email);
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("LdapUserDetailsMapper only supports reading from a context.");
    }

    private final XdatUserAuthService _service;

    private final String _authMethodId;
}
