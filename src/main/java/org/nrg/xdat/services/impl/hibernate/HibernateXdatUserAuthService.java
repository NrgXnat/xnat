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
import org.jetbrains.annotations.Nullable;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.exceptions.UsernameAuthMappingNotFoundException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "SqlResolve"})
@Service("xdatUserAuthService")
@Slf4j
public class HibernateXdatUserAuthService extends AbstractHibernateEntityService<XdatUserAuth, XdatUserAuthDAO> implements XdatUserAuthService {
    @Autowired
    public void setSiteConfigPreferences(final SiteConfigPreferences preferences) {
        _preferences = preferences;
    }

    @Autowired
    public void setXdatUserAuthDAO(final XdatUserAuthDAO dao) {
        _dao = dao;
    }

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        _jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public boolean hasUserByNameAndAuth(final String user, final String authMethod) {
        return getDao().hasUserByNameAndAuth(user, authMethod);
    }

    @Transactional
    @Override
    public boolean hasUserByNameAndAuth(final String user, final String authMethod, final String authMethodId) {
        return getDao().hasUserByNameAndAuth(user, authMethod, authMethodId);
    }

    @Override
    @Transactional
    public XdatUserAuth getUserByNameAndAuth(final String user, final String auth, final String id) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setAuthUser(user);
        return getXdatUserAuthByExample(auth, id, example, EXCLUSION_PROPERTIES);
    }

    @Override
    @Transactional
    public XdatUserAuth getUserByXdatUsernameAndAuth(String user, String auth, String id) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(user);
        return getXdatUserAuthByExample(auth, id, example, EXCLUSION_PROPERTIES_XDAT_USER_DETAILS);
    }

    @Override
    @Transactional
    public List<XdatUserAuth> getUsersByName(String user) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setAuthUser(user);
        return _dao.findByExample(example, EXCLUSION_PROPERTIES_USERNAME);
    }

    @Override
    @Transactional
    public List<XdatUserAuth> getUsersByXdatUsername(String xdatUser) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(xdatUser);
        return _dao.findByExample(example, EXCLUSION_PROPERTIES_XDATUSER);
    }

    @Override
    @Transactional
    public boolean addFailedLoginAttempt(final XdatUserAuth user) {
        final Date now = new Date();
        user.setLastLoginAttempt(now);

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        final boolean lockedOut = user.getFailedLoginAttempts() == _preferences.getMaxFailedLogins();
        if (lockedOut) {
            user.setLockoutTime(now);
            log.info("Incremented the failed login count for the user {} through auth record {}, now set at {}, user is locked out until {}", user.getXdatUsername(), user.getAuthUser(), user.getFailedLoginAttempts(), now);
        } else {
            log.info("Incremented the failed login count for the user {} through auth record {}, now set at {}, user is not locked out yet", user.getXdatUsername(), user.getAuthUser(), user.getFailedLoginAttempts());
        }

        update(user);

        return lockedOut;
    }

    @Override
    @Transactional
    public boolean addFailedLoginAttempt(final String user, final String provider, final String providerId) {
        return addFailedLoginAttempt(getUserByNameAndAuth(user, provider, providerId));
    }

    @Override
    @Transactional
    public void resetFailedLogins(final XdatUserAuth user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutTime(null);
        update(user);
    }

    @Override
    @Transactional
    public void resetFailedLogins(final String user, final String provider, final String providerId) {
        resetFailedLogins(getUserByNameAndAuth(user, provider, providerId));
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(String user, String auth) {
        return getUserDetailsByNameAndAuth(user, auth, "", null, null, null);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(String user, String auth, String id) {
        return getUserDetailsByNameAndAuth(user, auth, id, null, null, null);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(String user, String auth, String id, String email) {
        return getUserDetailsByNameAndAuth(user, auth, id, email, null, null);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByNameAndAuth(String username, String auth, String id, String email, String lastName, String firstName) {
        List<UserI> users = loadUsersByUsernameAndAuth(username, auth, id);
        return getUserDetailsForUserList(users, username, auth, id, email, lastName, firstName);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(String username) {
        final List<UserI> users = loadUsersByUsernameAndMostRecentSuccessfulLogin(username);
        final String      auth;
        final String      id;
        if (users.size() > 0 && users.get(0) != null) {
            auth = users.get(0).getAuthorization().getAuthMethod();
            id = users.get(0).getAuthorization().getAuthMethodId();
        } else {
            auth = null;
            id = null;
        }
        return getUserDetailsForUserList(users, username, auth, id, null, null, null);
    }

    public List<GrantedAuthority> loadUserAuthorities(String username) {
        return _jdbcTemplate.query("SELECT login AS username, 'ROLE_USER' AS authority FROM xdat_user WHERE login = ?", new String[]{username}, new RowMapper<GrantedAuthority>() {
            public GrantedAuthority mapRow(final ResultSet results, final int rowNum) throws SQLException {
                final String                 roleName  = results.getString(2);
                final SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

                log.debug("Found authority: {} for role name: {}", authority.getAuthority(), roleName);
                return authority;
            }
        });
    }

    @Override
    protected XdatUserAuthDAO getDao() {
        return _dao;
    }

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
            log.error("An error occurred trying to retrieve the user auth entry for user " + username + " via auth method " + authMethod + " " + authMethodId, exception);
            return null;
        }
    }

    @Nullable
    private XdatUserAuth getXdatUserAuthByExample(final String auth, final String id, final XdatUserAuth example, final String[] exclusionProperties) {
        example.setAuthMethod(auth);
        if (!StringUtils.isBlank(id)) {
            example.setAuthMethodId(id);
        }
        final List<XdatUserAuth> auths = _dao.findByExample(example, exclusionProperties);
        return auths != null && auths.size() != 0 ? auths.get(0) : null;
    }

    private UserI getUserDetailsForUserList(final List<UserI> users, final String username, final String authMethod, final String authMethodId, final String email, final String lastName, final String firstName) {
        if (users.size() == 0) {
            if (StringUtils.equals(XdatUserAuthService.LOCALDB, authMethod)) {
                log.debug("Query returned no results for user '{}'", username);
                throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.notFound", new Object[]{username}, "Username {0} not found"));
            } else {
                log.debug("Query returned no results for user '{}' with auth method {} provider {}", username, authMethod, authMethodId);
                throw new UsernameAuthMappingNotFoundException(username, authMethod, authMethodId, email, lastName, firstName);
            }
        }

        if (new HashSet<>(loadUserAuthorities(users.get(0).getUsername())).isEmpty()) {
            log.debug("User '" + username + "' has no authorities and will be treated as 'not found'");
            throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.noAuthority", new Object[]{username}, "User {0} has no GrantedAuthority"));
        }

        return getUserDetails(username, authMethod, authMethodId);
    }

    private List<UserI> loadUsersByUsernameAndAuth(String username, String auth, String id) {
        final XdatUserAuth example = new XdatUserAuth(username, auth, StringUtils.defaultIfBlank(id, null));
        return getUsersFromUserAuths(_dao.findByExample(example, EXCLUSION_PROPERTIES_USERNAME_AUTH_METHOD));
    }

    private List<UserI> loadUsersByUsernameAndMostRecentSuccessfulLogin(String username) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(username);
        example.setLockoutTime(null);
        return getUsersFromUserAuths(_dao.findByExample(example, EXCLUSION_PROPERTIES_XDATUSERLOCK));
    }

    private List<UserI> getUsersFromUserAuths(final List<XdatUserAuth> userAuths) {
        final List<UserI> users = new ArrayList<>();
        for (final XdatUserAuth authUser : userAuths) {
            try {
                final UserI user = Users.getUser(authUser.getXdatUsername());
                user.setAuthorization(new XdatUserAuth(authUser));
                if (!users.contains(user)) {
                    users.add(user);
                }
            } catch (Exception e) {
                log.error("An unknown error occurred", e);
            }
        }
        return users;
    }

    private static final String[] EXCLUSION_PROPERTIES                      = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "verified", "failedLoginAttempts", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_USERNAME             = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "verified", "authMethodId", "failedLoginAttempts", "authMethod", "lastSuccessfulLogin", "passwordUpdated", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_USERNAME_AUTH_METHOD = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "failedLoginAttempts", "lastSuccessfulLogin", "lastLoginAttempt", "passwordUpdated", "lockoutTime", "authorities");
    private static final String[] EXCLUSION_PROPERTIES_XDAT_USER_DETAILS    = AbstractHibernateEntity.getExcludedProperties("authUser", "verified", "failedLoginAttempts", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_XDATUSER             = AbstractHibernateEntity.getExcludedProperties("authUser", "verified", "authMethodId", "failedLoginAttempts", "authMethod", "lastSuccessfulLogin", "passwordUpdated", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_XDATUSERLOCK         = AbstractHibernateEntity.getExcludedProperties("authUser", "verified", "authMethodId", "failedLoginAttempts", "authMethod", "lastSuccessfulLogin", "passwordUpdated");

    private SiteConfigPreferences _preferences;
    private XdatUserAuthDAO       _dao;
    private JdbcTemplate          _jdbcTemplate;
}
