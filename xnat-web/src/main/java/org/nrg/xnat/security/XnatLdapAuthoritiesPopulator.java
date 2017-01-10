/*
 * web: org.nrg.xnat.security.XnatLdapAuthoritiesPopulator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Collection;
import java.util.Collections;

public class XnatLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    public Collection<GrantedAuthority> getGrantedAuthorities(final DirContextOperations userData, final String username) {
        return Collections.singletonList((GrantedAuthority) new SimpleGrantedAuthority("ROLE_USER"));
    }
}
