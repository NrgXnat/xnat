/*
 * XnatDatabaseUserDetailsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat.security.userdetailsservices;

import org.nrg.framework.services.ContextService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.PasswordExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

public class XnatDatabaseUserDetailsService extends JdbcDaoImpl implements UserDetailsService {

    // MIGRATION: This needs to go away and be replaced by a standard property for an XNAT user details service.
    public static final String DB_PROVIDER = "";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException, PasswordExpiredException {
        UserDetails user = getXdatUserAuthService().getUserDetailsByNameAndAuth(username, XdatUserAuthService.LOCALDB, DB_PROVIDER);
        if (_log.isDebugEnabled()) {
            _log.debug("Loaded user {} by username from user-auth service.", user.getUsername());
        }
        return user;
    }

    private XdatUserAuthService getXdatUserAuthService() {
        if (_xdatUserAuthService == null) {
            _xdatUserAuthService = _contextService.getBean(XdatUserAuthService.class);
        }
        return _xdatUserAuthService;
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatDatabaseUserDetailsService.class);

    @Autowired
    @Qualifier("rootContextService")
    @Lazy
    private ContextService _contextService;

    private XdatUserAuthService _xdatUserAuthService;
}
