/*
 * org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.userdetailsservices;

import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.PasswordExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

import javax.inject.Inject;

public class XnatDatabaseUserDetailsService extends JdbcDaoImpl implements UserDetailsService {

    // MIGRATION: This needs to go away and be replaced by a standard property for an XNAT user details service.
	public static final String DB_PROVIDER = "";
	
	@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException, PasswordExpiredException {
		UserDetails user = _service.getUserDetailsByNameAndAuth(username, XdatUserAuthService.LOCALDB, DB_PROVIDER);
        if (_log.isDebugEnabled()) {
            _log.debug("Loaded user {} by username from user-auth service.", user.getUsername());
        }
        return user;
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatDatabaseUserDetailsService.class);

    @Inject
    private XdatUserAuthService _service;
}
