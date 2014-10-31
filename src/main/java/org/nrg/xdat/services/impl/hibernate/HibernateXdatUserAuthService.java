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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.utils.AuthUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth) {
        return getUserDetailsByNameAndAuth(user, auth, "", null, null, null);
    }

    @Override
    @Transactional
    public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String id) {
        return getUserDetailsByNameAndAuth(user, auth, id, null, null, null);
    }

    @Override
    @Transactional
    public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String id, String email) {
        return getUserDetailsByNameAndAuth(user, auth, id, email, null, null);
    }

    @Override
    @Transactional
    public XDATUserDetails getUserDetailsByNameAndAuth(String username, String auth, String id, String email, String lastname, String firstname) {
        List<XDATUserDetails> users = loadUsersByUsernameAndAuth(username, auth, id);
        return getUserDetailsForUserList(users, username, auth, id, email, lastname, firstname);
    }

    @Override
    @Transactional
    public XDATUserDetails getUserDetailsByUsernameAndMostRecentSuccessfulLogin(String username) {
        List<XDATUserDetails> users = loadUsersByUsernameAndMostRecentSuccessfulLogin(username);
        String auth = null, id = null;
        if (users.size() > 0 && users.get(0) != null) {
            auth = users.get(0).getAuthorization().getAuthMethod();
            id = users.get(0).getAuthorization().getAuthMethodId();
        }
        return getUserDetailsForUserList(users, username, auth, id, null, null, null);
    }

    public List<GrantedAuthority> loadUserAuthorities(String username) {
        return (new JdbcTemplate(_datasource)).query("SELECT login as username, 'ROLE_USER' as authority FROM xdat_user WHERE login = ?", new String[]{username}, new RowMapper<GrantedAuthority>() {
            public GrantedAuthority mapRow(ResultSet rs, int rowNum) throws SQLException {
                String roleName = rs.getString(2);
                GrantedAuthorityImpl authority = new GrantedAuthorityImpl(roleName);
                if (_log.isDebugEnabled()) {
                    _log.debug("Found authority: " + authority.getAuthority() + " for role name: " + roleName);
                }
                return authority;
            }
        });
    }

    public Boolean newUserAccountsAreAutoEnabled() {
        return XFT.GetUserRegistration();
    }

    protected XDATUserDetails getUserDetails(String username, String auth, String id) {
        XDATUserDetails userDetails = null;
        try {
            XdatUserAuth userAuth = getUserByNameAndAuth(username, auth, id);
            if (userAuth == null) {
                userAuth = getUserByXdatUsernameAndAuth(username, auth, id);
            }
            userDetails = new XDATUserDetails(userAuth.getXdatUsername());
            userDetails.setAuthorization(userAuth);
        } catch (Exception exception) {
            _log.error(exception);
        }
        return userDetails;
    }

    private XDATUserDetails getUserDetailsForUserList(List<XDATUserDetails> users, String username, String auth, String id, String email, String lastname, String firstname) {
        XDATUserDetails userDetails = null;

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

        UserDetails user = users.get(0); // contains no GrantedAuthority[]

        Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();

        dbAuthsSet.addAll(loadUserAuthorities(user.getUsername()));

        List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>(dbAuthsSet);

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

    private XDATUserDetails handleNewLdapUser(final String id, String username, String email, String firstName, String lastName) {
        XDATUserDetails userDetails = null;

        try {
            String ldapUsername = username;
            username = findUnusedLocalUsernameForNewLDAPUser(ldapUsername);
            _log.debug("Adding LDAP user '" + username + "' to database.");

            PersistentWorkflowI wrk = PersistentWorkflowUtils.buildAdminWorkflow(null, "xdat:user", username, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Created user from LDAP", null, null));

            try {
                XDATUser newUser = createXDATUser(username, email, firstName, lastName);

                SaveItemHelper.authorizedSave(newUser, XDAT.getUserDetails(), true, false, true, false, wrk.buildEvent());
                wrk.setId(newUser.getStringProperty("xdat_user_id"));

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
                newUserAuth.setEnabled(newUserAccountsAreAutoEnabled());
                // </HACK_ALERT>

                userDetails = new XDATUserDetails(newUser);
                userDetails.setAuthorization(newUserAuth);

                XDAT.setUserDetails(userDetails);

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

        return userDetails;
    }

    private XDATUser createXDATUser(final String username, final String email, final String firstName, final String lastName) throws Exception {
        Map<String, String> newUserProperties = new HashMap<String, String>();
        newUserProperties.put(XFT.PREFIX + ":user.login", username);
        newUserProperties.put(XFT.PREFIX + ":user.email", email);
        newUserProperties.put(XFT.PREFIX + ":user.primary_password", null);
        newUserProperties.put(XFT.PREFIX + ":user.firstname", firstName);
        newUserProperties.put(XFT.PREFIX + ":user.lastname", lastName);
        newUserProperties.put(XFT.PREFIX + ":user.primary_password.encrypt", "true");
        // TODO: Need to add ability to verify email address in cases where we may not completely trust LDAP repo.
        newUserProperties.put(XFT.PREFIX + ":user.verified", "true");
        newUserProperties.put(XFT.PREFIX + ":user.enabled", newUserAccountsAreAutoEnabled().toString());

        PopulateItem populater = new PopulateItem(newUserProperties, null, XFT.PREFIX + ":user", true);
        ItemI item = populater.getItem();

        return new XDATUser(item);
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

    private List<XDATUserDetails> loadUsersByUsernameAndAuth(String username, String auth, String id) {
        id = (id == null) ? "" : id;
        return loadUsersByQuery(getUsersByUsernameAndAuthQuery(), username, auth, id);
    }

    private List<XDATUserDetails> loadUsersByUsernameAndMostRecentSuccessfulLogin(String username) {
        return loadUsersByQuery(getUsersByXDATUsernameAndMostRecentSuccessfulLoginQuery(), username);
    }

    private List<XDATUserDetails> loadUsersByQuery(String query, String... params) {
        List<XDATUserDetails> u =
                (new JdbcTemplate(_datasource)).query(query, params, new RowMapper<XDATUserDetails>() {
                    public XDATUserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String username = rs.getString(1);
                        String method = rs.getString(2);
                        String xdatUsername = rs.getString(3);
                        boolean enabled = rs.getBoolean(4);
                        Integer failedLoginAttempts = rs.getInt(5);
                        String methodId = rs.getString(6);
                        Date lastSuccessfulLogin = rs.getDate(7);
                        XdatUserAuth u = new XdatUserAuth(username, method, methodId, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, xdatUsername, failedLoginAttempts, lastSuccessfulLogin);
                        XDATUserDetails xdat = null;
                        try {
                            xdat = new XDATUserDetails(u.getXdatUsername());
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

            existingLocalUsernames = (new JdbcTemplate(_datasource)).query("SELECT login FROM xdat_user WHERE login = ?", new String[]{usernameToTest}, new RowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getString(1);
                }
            });

        } while (existingLocalUsernames.size() > 0);

        return usernameToTest;
    }

    private static final String[] EXCLUSION_PROPERTIES                      = new String[] { "xdatUsername", "id", "enabled", "verified", "created", "timestamp", "disabled", "failedLoginAttempts"};
    private static final String[] EXCLUSION_PROPERTIES_XDAT_USER_DETAILS    = new String[] { "authUser", "id", "enabled", "verified", "created", "timestamp", "disabled", "failedLoginAttempts"};
    private static final String[] EXCLUSION_PROPERTIES_USERNAME             = new String[] { "xdatUsername", "id", "enabled", "verified", "created", "timestamp", "disabled", "authMethodId", "failedLoginAttempts"};
    private static final String[] EXCLUSION_PROPERTIES_XDATUSER             = new String[] { "authUser",     "id", "enabled", "verified", "created", "timestamp", "disabled", "authMethodId", "failedLoginAttempts"};

    private static final Log _log = LogFactory.getLog(HibernateXdatUserAuthService.class);

    @Inject
    private XdatUserAuthDAO _dao;

    @Inject
    private DataSource _datasource;
}
