/*
 * web: org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.userdetailsservices;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.PasswordExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@Slf4j
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
        if (log.isDebugEnabled()) {
            log.debug("Loaded user {} by username from user-auth service.", user.getUsername());
        }
        return user;
    }

    private final XdatUserAuthService _userAuthService;
}
