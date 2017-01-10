/*
 * web: org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.userdetailsservices;

import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.PasswordExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

import javax.sql.DataSource;

public class XnatDatabaseUserDetailsService extends JdbcDaoImpl implements UserDetailsService {
    @Autowired
    public XnatDatabaseUserDetailsService(final XdatUserAuthService userAuthService, final DataSource dataSource) {
        super();
        setDataSource(dataSource);
        _userAuthService = userAuthService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException, PasswordExpiredException {
        final UserDetails user = _userAuthService.getUserDetailsByNameAndAuth(username, XdatUserAuthService.LOCALDB, "");
        if (_log.isDebugEnabled()) {
            _log.debug("Loaded user {} by username from user-auth service.", user.getUsername());
        }
        return user;
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatDatabaseUserDetailsService.class);

    private final XdatUserAuthService _userAuthService;
}
