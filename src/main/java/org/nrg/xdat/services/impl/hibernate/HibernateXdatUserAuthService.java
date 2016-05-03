/*
 * org.nrg.xdat.services.impl.hibernate.HibernateXdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.services.impl.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@Service
public class HibernateXdatUserAuthService extends AbstractHibernateEntityService<XdatUserAuth, XdatUserAuthDAO> implements XdatUserAuthService {

    @Override
    @Transactional
    public XdatUserAuth getUserByNameAndAuth(String user, String auth, String id) {
        XdatUserAuth example = new XdatUserAuth();
        example.setAuthUser(user);
        example.setAuthMethod(auth);
        if (!StringUtils.isBlank(id)) {
            example.setAuthMethodId(id);
        }
        List<XdatUserAuth> auths = _dao.findByExample(example, EXCLUSION_PROPERTIES);
        if (auths == null || auths.size() == 0) {
            return null;
        }
        return auths.get(0);
    }

    @Override
    @Transactional
    public XdatUserAuth getUserByXdatUsernameAndAuth(String user, String auth, String id) {
        XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(user);
        example.setAuthMethod(auth);
        if (!StringUtils.isBlank(id)) {
            example.setAuthMethodId(id);
        }
        List<XdatUserAuth> auths = _dao.findByExample(example, EXCLUSION_PROPERTIES_XDAT_USER_DETAILS);
        if (auths == null || auths.size() == 0) {
            return null;
        }
        return auths.get(0);
    }

    @Override
    @Transactional
    public List<XdatUserAuth> getUsersByName(String user) {
        XdatUserAuth example = new XdatUserAuth();
        example.setAuthUser(user);
        return _dao.findByExample(example, EXCLUSION_PROPERTIES_USERNAME);
    }

    @Override
    @Transactional
    public List<XdatUserAuth> getUsersByXdatUsername(String xdatUser) {
        XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(xdatUser);
        return _dao.findByExample(example, EXCLUSION_PROPERTIES_XDATUSER);
    }

    @Override
    protected XdatUserAuthDAO getDao() {
        return _dao;
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
    public UserI getUserDetailsByNameAndAuth(String username, String auth, String id, String email, String lastname, String firstname) {
        List<UserI> users = loadUsersByUsernameAndAuth(username, auth, id);
        return getUserDetailsForUserList(users, username, auth, id, email, lastname, firstname);
    }

    @Override
    @Transactional
    public UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(String username) {
        List<UserI> users = loadUsersByUsernameAndMostRecentSuccessfulLogin(username);
        String auth = null, id = null;
        if (users.size() > 0 && users.get(0) != null) {
            auth = users.get(0).getAuthorization().getAuthMethod();
            id = users.get(0).getAuthorization().getAuthMethodId();
        }
        return getUserDetailsForUserList(users, username, auth, id, null, null, null);
    }

    public List<GrantedAuthority> loadUserAuthorities(String username) {
        return (_jdbcTemplate).query("SELECT login as username, 'ROLE_USER' as authority FROM xdat_user WHERE login = ?", new String[]{username}, new RowMapper<GrantedAuthority>() {
            public GrantedAuthority mapRow(ResultSet rs, int rowNum) throws SQLException {
                String roleName = rs.getString(2);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
                if (_log.isDebugEnabled()) {
                    _log.debug("Found authority: " + authority.getAuthority() + " for role name: " + roleName);
                }
                return authority;
            }
        });
    }

    protected UserI getUserDetails(String username, String auth, String id) {
        UserI userDetails = null;
        try {
            XdatUserAuth userAuth = getUserByNameAndAuth(username, auth, id);
            if (userAuth == null) {
                userAuth = getUserByXdatUsernameAndAuth(username, auth, id);
            }
            userDetails = Users.getUser(userAuth.getXdatUsername());
            userDetails.setAuthorization(userAuth);
        } catch (Exception exception) {
            _log.error(exception);
        }
        return userDetails;
    }

    private UserI getUserDetailsForUserList(List<UserI> users, String username, String auth, String id, String email, String lastname, String firstname) {
        UserI userDetails = null;

        if (users.size() == 0) {
            if (auth.equals(XdatUserAuthService.LDAP)) {
                userDetails = handleNewLdapUser(id, username, email, firstname, lastname);
                if (users.size() == 0) {
                    users.add(userDetails);
                } else {
                    users.set(0, userDetails);
                }
            } else {
                _log.debug("Query returned no results for user '" + username + "'");
                throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.notFound", new Object[]{username}, "Username {0} not found"));
            }
        }

        UserI user = users.get(0); // contains no GrantedAuthority[]

        Set<GrantedAuthority> dbAuthsSet = new HashSet<>();

        dbAuthsSet.addAll(loadUserAuthorities(user.getUsername()));

        List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);

        if (dbAuths.size() == 0) {
            _log.debug("User '" + username + "' has no authorities and will be treated as 'not found'");
            throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.noAuthority", new Object[]{username}, "User {0} has no GrantedAuthority"));
        }

        if (userDetails == null) {
            // If we just created a new user account above, the user_auth DB record won't yet be committed at this point.
            // So we'll just return the object that was already created.
            // For subsequent logins, this code here will pull the auth record and set it.
            userDetails = getUserDetails(username, auth, id);
        }

        return userDetails;
    }

    private UserI handleNewLdapUser(final String id, String username, String email, String firstName, String lastName) {
    	UserI newUser=null;
        try {
            String ldapUsername = username;
            username = findUnusedLocalUsernameForNewLDAPUser(ldapUsername);
            _log.debug("Adding LDAP user '" + username + "' to database.");

            PersistentWorkflowI wrk = PersistentWorkflowUtils.buildAdminWorkflow(null, "xdat:user", username, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Created user from LDAP", null, null));

            try {
                newUser = createXDATUser(username, email, firstName, lastName);

                Users.save(newUser, XDAT.getUserDetails(), true, wrk.buildEvent());
                wrk.setId(newUser.getID().toString());

                XdatUserAuth newUserAuth = new XdatUserAuth(ldapUsername, XdatUserAuthService.LDAP, id, username, true, 0);
                XDAT.getXdatUserAuthService().create(newUserAuth);

                // <HACK_ALERT>
                /*
                * We must save enabled flag to DB as true above, because the administrator code for enabling a user account does not flip this flag
                * (no time to mess with that now).
                * But for purposes of determining whether or not the user can log in right now after we've just created their account,
                * we use the system-wide auto-enable config setting.
                * Must clone a new object to return, rather than modifying the existing, so that Hibernate still saves the desired values to the DB.
                */
                newUserAuth = new XdatUserAuth(newUserAuth);
                newUserAuth.setEnabled(_preferences.getUserRegistration());
                // </HACK_ALERT>

                newUser.setAuthorization(newUserAuth);

                XDAT.setUserDetails(newUser);

                PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
            } catch (Exception e) {
                _log.error(e);
                try {
                    PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
                } catch (Exception e1) {
                    _log.error(e);
                }
            }
        } catch (EventRequirementAbsent exception) {
            _log.error(exception);
            throw new UsernameNotFoundException(SpringSecurityMessageSource.getAccessor().getMessage("JdbcDaoImpl.notFound", new Object[]{username}, "Username {0} not found"));
        }

        return newUser;
    }

    private UserI createXDATUser(final String username, final String email, final String firstName, final String lastName) throws Exception {
        Map<String, String> newUserProperties = new HashMap<>();
        newUserProperties.put(XFT.PREFIX + ":user.login", username);
        newUserProperties.put(XFT.PREFIX + ":user.email", email);
        newUserProperties.put(XFT.PREFIX + ":user.primary_password", null);
        newUserProperties.put(XFT.PREFIX + ":user.firstname", firstName);
        newUserProperties.put(XFT.PREFIX + ":user.lastname", lastName);
        newUserProperties.put(XFT.PREFIX + ":user.primary_password.encrypt", "true");
        // TODO: Need to add ability to verify email address in cases where we may not completely trust LDAP repo.
        newUserProperties.put(XFT.PREFIX + ":user.verified", "true");
        newUserProperties.put(XFT.PREFIX + ":user.enabled", Boolean.toString(_preferences.getUserRegistration()));

        return Users.createUser(newUserProperties);
    }

    private String getUsersByUsernameAndAuthQuery() {
        return "select xhbm_xdat_user_auth.auth_user,xhbm_xdat_user_auth.auth_method,xhbm_xdat_user_auth.xdat_username,xhbm_xdat_user_auth.enabled,xhbm_xdat_user_auth.failed_login_attempts,xhbm_xdat_user_auth.auth_method_id, xhbm_xdat_user_auth.last_successful_login from xhbm_xdat_user_auth JOIN xdat_user ON xhbm_xdat_user_auth.xdat_username=xdat_user.login where xhbm_xdat_user_auth.failed_login_attempts<" + getMaxLoginAttemptsForQuery() + "  and xhbm_xdat_user_auth.auth_user = ? and xhbm_xdat_user_auth.auth_method = ? and COALESCE(xhbm_xdat_user_auth.auth_method_id, '') = ?";
    }

    private String getUsersByXDATUsernameAndMostRecentSuccessfulLoginQuery() {
        return "select xhbm_xdat_user_auth.auth_user,xhbm_xdat_user_auth.auth_method,xhbm_xdat_user_auth.xdat_username,xhbm_xdat_user_auth.enabled,xhbm_xdat_user_auth.failed_login_attempts,xhbm_xdat_user_auth.auth_method_id, xhbm_xdat_user_auth.last_successful_login from xhbm_xdat_user_auth JOIN xdat_user ON xhbm_xdat_user_auth.xdat_username=xdat_user.login where xhbm_xdat_user_auth.failed_login_attempts<" + getMaxLoginAttemptsForQuery() + "  and xhbm_xdat_user_auth.xdat_username = ? ORDER BY last_successful_login DESC";
    }

    private Integer getMaxLoginAttemptsForQuery() {
        Integer maxFailedLoginAttempts = AuthUtils.MAX_FAILED_LOGIN_ATTEMPTS;
        if (maxFailedLoginAttempts <= -1) {
            maxFailedLoginAttempts = Integer.MAX_VALUE;
        }
        return maxFailedLoginAttempts;
    }

    private List<UserI> loadUsersByUsernameAndAuth(String username, String auth, String id) {
        id = (id == null) ? "" : id;
        return loadUsersByQuery(getUsersByUsernameAndAuthQuery(), username, auth, id);
    }

    private List<UserI> loadUsersByUsernameAndMostRecentSuccessfulLogin(String username) {
        return loadUsersByQuery(getUsersByXDATUsernameAndMostRecentSuccessfulLoginQuery(), username);
    }

    private List<UserI> loadUsersByQuery(String query, String... params) {
        List<UserI> u =
                (_jdbcTemplate).query(query, params, new RowMapper<UserI>() {
                    public UserI mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String username = rs.getString(1);
                        String method = rs.getString(2);
                        String xdatUsername = rs.getString(3);
                        boolean enabled = rs.getBoolean(4);
                        Integer failedLoginAttempts = rs.getInt(5);
                        String methodId = rs.getString(6);
                        Date lastSuccessfulLogin = rs.getDate(7);
                        XdatUserAuth u = new XdatUserAuth(username, method, methodId, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, xdatUsername, failedLoginAttempts, lastSuccessfulLogin);
                        UserI xdat = null;
                        try {
                            xdat = Users.getUser(u.getXdatUsername());
                            xdat.setAuthorization(u);
                        } catch (Exception e) {
                            _log.error(e);
                        }
                        return xdat;
                    }
                });
        if (_log.isDebugEnabled()) {
            _log.debug("Found " + u.size() + " results for the submitted user query");
        }
        return u;
    }

    private String findUnusedLocalUsernameForNewLDAPUser(String ldapUsername) {
        // we will punt on this for now and just create a new user account if their is already a local account
        // the Cadillac solution would be to link the two (assuming the user proves that they own the local account also)

        ldapUsername=ldapUsername.replaceAll("[^A-Za-z0-9]", "_");
        //This is necessary to ensure that the XNAT username for the LDAP user does not contain characters that will break XNAT.

        String usernameToTest = ldapUsername;
        int testCount = -1;
        List<String> existingLocalUsernames;

        do {
            if (++testCount > 0) {
                usernameToTest = ldapUsername + "_" + String.format("%02d", testCount);
            } else if (testCount > 99) {
                throw new RuntimeException("Ran out of possible XNAT user ids to check (last one checked was " + usernameToTest + ")");
            }

            existingLocalUsernames = (_jdbcTemplate).query("SELECT login FROM xdat_user WHERE login = ?", new String[]{usernameToTest}, new RowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getString(1);
                }
            });

        } while (existingLocalUsernames.size() > 0);

        return usernameToTest;
    }

    private static final String[] EXCLUSION_PROPERTIES                      = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "verified", "failedLoginAttempts");
    private static final String[] EXCLUSION_PROPERTIES_XDAT_USER_DETAILS    = AbstractHibernateEntity.getExcludedProperties("authUser", "verified", "failedLoginAttempts");
    private static final String[] EXCLUSION_PROPERTIES_USERNAME             = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "verified", "authMethodId", "failedLoginAttempts", "authMethod", "lastSuccessfulLogin", "passwordUpdated");
    private static final String[] EXCLUSION_PROPERTIES_XDATUSER             = AbstractHibernateEntity.getExcludedProperties("authUser", "verified", "authMethodId", "failedLoginAttempts", "authMethod", "lastSuccessfulLogin", "passwordUpdated");
    private static final Log _log = LogFactory.getLog(HibernateXdatUserAuthService.class);

    @Inject
    private XdatUserAuthDAO _dao;

    @Inject
    private SiteConfigPreferences _preferences;

    @Autowired
    @Lazy
    private JdbcTemplate _jdbcTemplate;
}
