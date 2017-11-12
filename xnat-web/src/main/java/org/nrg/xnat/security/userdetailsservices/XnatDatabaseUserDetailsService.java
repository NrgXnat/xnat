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
import org.jetbrains.annotations.NotNull;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.services.AliasTokenService;
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

import static org.nrg.xdat.services.XdatUserAuthService.LOCALDB;

@Service
@Slf4j
public class XnatDatabaseUserDetailsService extends JdbcDaoImpl implements UserDetailsService {
    @Autowired
    public XnatDatabaseUserDetailsService(final DataSource dataSource) {
        super();
        setDataSource(dataSource);
    }

    @Autowired
    public void setUserAuthService(final XdatUserAuthService userAuthService) {
        _userAuthService = userAuthService;
    }

    @Autowired
    public void setAliasTokenService(final AliasTokenService aliasTokenService) {
        _aliasTokenService = aliasTokenService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException, PasswordExpiredException {
        final boolean      isPossibleAliasToken = AliasToken.isAliasFormat(username);
        final XdatUserAuth userAuth             = _userAuthService.getUserByNameAndAuth(username, LOCALDB, "");
        final boolean      hasUserByNameAndAuth = userAuth != null;

        // If the username doesn't exist and it's not an alias token...
        if (!hasUserByNameAndAuth && !isPossibleAliasToken) {
            // We don't got it.
            throw new UsernameNotFoundException("Found no user by the name " + username);
        }

        // If the username exists, just return that. Our work here is done.
        if (hasUserByNameAndAuth) {
            return getUserDetailsByName(username);
        }

        final AliasToken token = _aliasTokenService.locateToken(username);
        if (token == null) {
            throw new UsernameNotFoundException("The username appears to be an alias token, but the security token has expired or is invalid: " + username);
        }

        return getUserDetailsByName(token.getXdatUserId());
    }

    @NotNull
    private UserDetails getUserDetailsByName(final String username) {
        final UserDetails user = _userAuthService.getUserDetailsByNameAndAuth(username, LOCALDB);
        log.debug("Loaded user {} by username from user-auth service.", user.getUsername());
        return user;
    }

    private XdatUserAuthService _userAuthService;
    private AliasTokenService   _aliasTokenService;
}
