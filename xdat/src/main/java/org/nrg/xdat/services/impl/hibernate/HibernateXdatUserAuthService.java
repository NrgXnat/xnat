/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateXdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.exceptions.UsernameAuthMappingNotFoundException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "SqlResolve"})
@Service("xdatUserAuthService")
@Slf4j
public class HibernateXdatUserAuthService extends AbstractHibernateEntityService<XdatUserAuth, XdatUserAuthDAO> implements XdatUserAuthService {
    @Autowired
    public HibernateXdatUserAuthService(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate template) {
        _preferences = preferences;
        _template    = template;
    }

    @Transactional
    @Override
    public boolean hasUserByNameAndAuth(final String authUser, final String authMethod) {
        return getDao().hasUserByAuthUsernameAndProvider(authUser, authMethod);
    }

    @Transactional
    @Override
    public boolean hasUserByNameAndAuth(final String authUser, final String authMethod, final String authMethodId) {
        return getDao().hasUserByAuthUsernameAndProvider(authUser, authMethod, authMethodId);
    }

    @Transactional
    @Override
    public String getXdatUsernameByAuthNameAndProvider(final String authUser, final String authMethod, final String authMethodId) {
        final String xdatUsername = getDao().getXdatUsernameByAuthUsernameAndProvider(authUser, authMethod, authMethodId);
        return exists(xdatUsername) ? xdatUsername : null;
    }

    @Override
    @Transactional
    public XdatUserAuth getUserByNameAndAuth(final String authUser, final String authMethod, final String authMethodId) {
        return getDao().findByAuthUsernameAndProvider(authUser, authMethod, authMethodId);
    }

    @Override
    @Transactional
    public XdatUserAuth getUserByXdatUsernameAndAuth(final String xdatUser, final String authMethod, final String authMethodId) {
        return getDao().findByXdatUsernameAndAuth(xdatUser, authMethod, authMethodId);
    }

    @Override
    @Transactional
    public List<XdatUserAuth> getUsersByName(final String authUser) {
        return getDao().findAllByAuthUsername(authUser);
    }

    @Override
    @Transactional
    public List<XdatUserAuth> getUsersByXdatUsername(final String xdatUser) {
        return getDao().findUsersByXdatUsername(xdatUser);
    }

    @Override
    @Transactional
    public boolean addFailedLoginAttempt(final XdatUserAuth userAuth) {
        final Date now = new Date();
        userAuth.setLastLoginAttempt(now);
        userAuth.setFailedLoginAttempts(userAuth.getFailedLoginAttempts() + 1);
        final boolean lockedOut = userAuth.getFailedLoginAttempts() >= _preferences.getMaxFailedLogins();
        if (lockedOut) {
            userAuth.setLockoutTime(now);
            log.info("Incremented the failed login count for the user {} through auth record {}, now set at {}, user is locked out until {}", userAuth.getXdatUsername(), userAuth.getAuthUser(), userAuth.getFailedLoginAttempts(), now);
        } else {
            log.info("Incremented the failed login count for the user {} through auth record {}, now set at {}, user is not locked out yet", userAuth.getXdatUsername(), userAuth.getAuthUser(), userAuth.getFailedLoginAttempts());
        }

        update(userAuth);

        return lockedOut;
    }

    @Override
    @Transactional
    public boolean addFailedLoginAttempt(final String authUser, final String authMethod, final String authMethodId) {
        return addFailedLoginAttempt(getUserByNameAndAuth(authUser, authMethod, authMethodId));
    }

    @Override
    @Transactional
    public void resetFailedLogins(final XdatUserAuth userAuth) {
        userAuth.setFailedLoginAttempts(0);
        userAuth.setLockoutTime(null);
        update(userAuth);
    }

    @Override
    @Transactional
    public void resetFailedLogins(final String authUser, final String authMethod, final String authMethodId) {
        resetFailedLogins(getUserByNameAndAuth(authUser, authMethod, authMethodId));
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(final String authUser, final String authMethod) {
        return getUserDetailsByNameAndAuth(authUser, authMethod, null, null, null, null);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(final String authUser, final String authMethod, final String authMethodId) {
        return getUserDetailsByNameAndAuth(authUser, authMethod, authMethodId, null, null, null);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(final String authUser, final String authMethod, final String authMethodId, final String email) {
        return getUserDetailsByNameAndAuth(authUser, authMethod, authMethodId, email, null, null);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(String authUser, String authMethod, String authMethodId, String email, String lastName, String firstName) {
        if (StringUtils.isBlank(authUser)) {
            throw new IllegalArgumentException("A username must be provided.");
        }
        final UserI user = getUserDetailsForAuthUser(loadUserByUsernameAndAuth(authUser, authMethod, authMethodId), authUser, authMethod, authMethodId, email, lastName, firstName);
        if (user == null || !StringUtils.equalsIgnoreCase(authUser, user.getAuthorization().getAuthUser())) {
            throw new UsernameNotFoundException("No matching user found for username '" + authUser + "' with auth method " + authMethod + " provider " + authMethodId);
        }
        return user;
    }

    @Override
    @Transactional
    public UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(final String xdatUser) {
        final UserI  user = loadUserByXdatUsernameAndMostRecentSuccessfulLogin(xdatUser);
        final String auth;
        final String id;
        if (user != null) {
            auth = user.getAuthorization().getAuthMethod();
            id   = user.getAuthorization().getAuthMethodId();
        } else {
            auth = null;
            id   = null;
        }
        return getUserDetailsForAuthUser(user, xdatUser, auth, id, null, null, null);
    }

    @SuppressWarnings("SpringTransactionalMethodCallsInspection")
    protected UserI getUserDetails(final String username, final String authMethod, final String authMethodId) {
        try {
            XdatUserAuth userAuth = getUserByNameAndAuth(username, authMethod, authMethodId);
            if (userAuth == null) {
                userAuth = getUserByXdatUsernameAndAuth(username, authMethod, authMethodId);
            }
            final UserI userDetails = Users.getUser(userAuth.getXdatUsername());
            userDetails.setAuthorization(userAuth);
            return userDetails;
        } catch (Exception exception) {
            log.error("An error occurred trying to retrieve the user auth entry for user {} via auth method {} {}", username, authMethod, authMethodId, exception);
            return null;
        }
    }

    /**
     * Reimplementation of {@link UserManagementServiceI#exists(String)} to prevent circular dependency on
     * initialization.
     *
     * @param xdatUser The username to check.
     *
     * @return Returns <b>true</b> if a user with the specified name exists on the system, <b>false</b> otherwise.
     */
    private boolean exists(final String xdatUser) {
        return Boolean.TRUE.equals(_template.queryForObject(UserManagementServiceI.QUERY_CHECK_USER_EXISTS,
                                                            new MapSqlParameterSource(UserManagementServiceI.PARAM_USERNAME, xdatUser),
                                                            Boolean.class));
    }

    private List<GrantedAuthority> loadUserAuthorities(String xdatUser) {
        return _template.query(QUERY_LOAD_USER_AUTHORITIES, new MapSqlParameterSource(UserManagementServiceI.PARAM_USERNAME, xdatUser), (results, rowNum) -> {
            final String           roleName  = results.getString(2);
            final GrantedAuthority authority = Users.getGrantedAuthority(roleName);
            log.debug("Found authority: {} for role name: {}", authority.getAuthority(), roleName);
            return authority;
        });
    }

    private UserI getUserDetailsForAuthUser(final UserI user, final String username, final String authMethod, final String authMethodId, final String email, final String lastName, final String firstName) {
        if (user == null) {
            if (StringUtils.equals(XdatUserAuthService.LOCALDB, authMethod)) {
                log.debug("Query returned no results for user '{}'", username);
                throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.notFound", new Object[]{username}, "Username {0} not found"));
            } else {
                log.debug("Query returned no results for user '{}' with auth method {} provider {}", username, authMethod, authMethodId);
                throw new UsernameAuthMappingNotFoundException(username, authMethod, authMethodId, email, lastName, firstName);
            }
        }

        if (new HashSet<>(loadUserAuthorities(user.getUsername())).isEmpty()) {
            log.debug("User '{}' has no authorities and will be treated as 'not found'", username);
            throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.noAuthority", new Object[]{username}, "User {0} has no GrantedAuthority"));
        }

        return getUserDetails(username, authMethod, authMethodId);
    }

    private UserI loadUserByUsernameAndAuth(final String authUser, final String authMethod, final String authMethodId) {
        final XdatUserAuth userAuth = getDao().findByAuthUsernameAndProvider(authUser, authMethod, authMethodId);
        if (userAuth == null) {
            log.info("No user auth entry found for user {} with auth method {} provider {}", authUser, authMethod, authMethodId);
            return null;
        }
        if (!StringUtils.equalsIgnoreCase(authUser, userAuth.getAuthUser()) && !StringUtils.equalsIgnoreCase(authUser, userAuth.getXdatUsername())) {
            log.warn("User auth entry for user {} with auth method {} provider {} has mismatched auth username {} and xdat username {}", authUser, authMethod, authMethodId, userAuth.getAuthUser(), userAuth.getXdatUsername());
            return null;
        }
        return getUserFromUserAuth(userAuth);
    }

    private UserI loadUserByXdatUsernameAndMostRecentSuccessfulLogin(final String xdatUser) {
        final XdatUserAuth userAuth = getDao().findByXdatUsernameAndMostRecentSuccessfulLogin(xdatUser);
        if (userAuth == null) {
            log.info("No user auth entry found for user {} with successful recent login", xdatUser);
            return null;
        }
        return getUserFromUserAuth(userAuth);
    }

    private UserI getUserFromUserAuth(final XdatUserAuth userAuth) {
        try {
            final UserI user = Users.getUser(userAuth.getXdatUsername());
            user.setAuthorization(new XdatUserAuth(userAuth));
            return user;
        } catch (UserInitException e) {
            log.error("An error occurred initializing the user object for user {}", userAuth.getXdatUsername(), e);
        } catch (UserNotFoundException e) {
            log.warn("User {} not found", userAuth.getXdatUsername());
        }
        return null;
    }

    private static final String QUERY_LOAD_USER_AUTHORITIES = "SELECT login AS username, 'ROLE_USER' AS authority FROM xdat_user WHERE login = :" + UserManagementServiceI.PARAM_USERNAME;

    private final SiteConfigPreferences      _preferences;
    private final NamedParameterJdbcTemplate _template;
}
