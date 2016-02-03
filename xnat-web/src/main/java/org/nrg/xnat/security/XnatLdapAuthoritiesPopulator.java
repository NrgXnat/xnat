/*
 * org.nrg.xnat.security.XnatLdapAuthoritiesPopulator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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
