/*
 * core: org.nrg.xdat.daos.XdatUserAuthDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.services.XdatUserAuthService;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Repository
@Slf4j
public class XdatUserAuthDAO extends AbstractHibernateDAO<XdatUserAuth> {
    /**
     * Check to see if a user exists with the given authentication username (i.e. username on the external authentication provider)
     * and authentication method. Practically speaking, this method is only useful for the <pre>localdb</pre> authentication method, since
     * other authentication methods typically require both a username and an ID (e.g. LDAP DN, Shibboleth ID, etc.) to uniquely identify
     * a user. For other authentication methods, use {@link #hasUserByAuthUsernameAndProvider(String, String, String)}.
     *
     * @param authUser   The username on the external authentication provider
     * @param authMethod The authentication method
     *
     * @return Returns <pre>true</pre> if a user exists with the given authentication username and method, <pre>false</pre> otherwise.
     *
     * @see #hasUserByAuthUsernameAndProvider(String, String, String)
     * @deprecated Use {@link #hasUserByAuthUsernameAndProvider(String, String)} instead.
     */
    @Deprecated
    public boolean hasUserByNameAndAuth(final String authUser, final String authMethod) {
        return hasUserByAuthUsernameAndProvider(authUser, authMethod, null);
    }

    /**
     * Check to see if a user exists with the given authentication username (i.e. username on the external authentication provider)
     * and authentication method. Practically speaking, this method is only useful for the <pre>localdb</pre> authentication method, since
     * other authentication methods typically require both a username and an ID (e.g. LDAP DN, Shibboleth ID, etc.) to uniquely identify
     * a user. For other authentication methods, use {@link #hasUserByAuthUsernameAndProvider(String, String, String)}.
     *
     * @param authUser   The username on the external authentication provider
     * @param authMethod The authentication method
     *
     * @return Returns <pre>true</pre> if a user exists with the given authentication username and method, <pre>false</pre> otherwise.
     *
     * @see #hasUserByAuthUsernameAndProvider(String, String, String)
     */
    public boolean hasUserByAuthUsernameAndProvider(final String authUser, final String authMethod) {
        return hasUserByAuthUsernameAndProvider(authUser, authMethod, null);
    }

    /**
     * Check to see if a user exists with the given authentication username (i.e. username on the external authentication provider)
     * and authentication method. For the <pre>localdb</pre> authentication method, use {@link #hasUserByAuthUsernameAndProvider(String, String)}.
     *
     * @param authUser     The username on the external authentication provider
     * @param authMethod   The authentication method
     * @param authMethodId The authentication method ID
     *
     * @return Returns <pre>true</pre> if a user exists with the given authentication username and method, <pre>false</pre> otherwise.
     *
     * @see #hasUserByAuthUsernameAndProvider(String, String)
     * @deprecated Use {@link #hasUserByAuthUsernameAndProvider(String, String, String)} instead.
     */
    @Deprecated
    public boolean hasUserByNameAndAuth(final String authUser, final String authMethod, final String authMethodId) {
        return hasUserByAuthUsernameAndProvider(authUser, authMethod, authMethodId);
    }

    /**
     * Check to see if a user exists with the given authentication username (i.e. username on the external authentication provider)
     * and authentication method. For the <pre>localdb</pre> authentication method, use {@link #hasUserByAuthUsernameAndProvider(String, String)}.
     *
     * @param authUser     The username on the external authentication provider
     * @param authMethod   The authentication method
     * @param authMethodId The authentication method ID
     *
     * @return Returns <pre>true</pre> if a user exists with the given authentication username and method, <pre>false</pre> otherwise.
     *
     * @see #hasUserByAuthUsernameAndProvider(String, String)
     */
    public boolean hasUserByAuthUsernameAndProvider(final String authUser, final String authMethod, final String authMethodId) {
        if (StringUtils.isBlank(authUser)) {
            throw new IllegalArgumentException("A username must be provided.");
        }
        final Query<Boolean> query = createQuery(StringUtils.isBlank(authMethodId) ? QUERY_HAS_USER_AND_AUTH : QUERY_HAS_USER_AND_AUTH_AND_ID, Boolean.class)
                .setParameter(PARAM_AUTH_USER, authUser)
                .setParameter(PARAM_AUTH_METHOD, authMethod);
        if (StringUtils.isNotBlank(authMethodId)) {
            query.setParameter(PARAM_AUTH_METHOD_ID, authMethodId);
        }
        return query.getSingleResult();
    }

    /**
     * Get the XDAT username for a user with the given authentication username (i.e. username on the external authentication provider)
     * and authentication method.
     *
     * @param authUser     The username on the external authentication provider
     * @param authMethod   The authentication method
     * @param authMethodId The authentication method ID
     *
     * @return Returns the XDAT username for the user with the given authentication username and method.
     *
     * @deprecated Use {@link #getXdatUsernameByAuthUsernameAndProvider(String, String, String)} instead.
     */
    @Deprecated
    public String getUsernameByNameAndAuth(final String authUser, final String authMethod, final String authMethodId) {
        return getXdatUsernameByAuthUsernameAndProvider(authUser, authMethod, authMethodId);
    }

    /**
     * Get the XDAT username for a user with the given authentication username (i.e. username on the external authentication provider)
     * and authentication method.
     *
     * @param authUser     The username on the external authentication provider
     * @param authMethod   The authentication method
     * @param authMethodId The authentication method ID
     *
     * @return Returns the XDAT username for the user with the given authentication username and method.
     */
    public String getXdatUsernameByAuthUsernameAndProvider(final String authUser, final String authMethod, final String authMethodId) {
        if (StringUtils.isBlank(authUser)) {
            throw new IllegalArgumentException("A username must be provided.");
        }
        return createQuery(QUERY_GET_USERNAME_BY_USER_AND_AUTH_AND_ID, String.class).setParameter(PARAM_AUTH_USER, authUser)
                                                                                    .setParameter(PARAM_AUTH_METHOD, authMethod)
                                                                                    .setParameter(PARAM_AUTH_METHOD_ID, authMethodId)
                                                                                    .getSingleResult();
    }

    /**
     * Finds the {@link XdatUserAuth} entry for a user with the given authentication username (i.e. username on the external authentication provider)
     * and provider.
     *
     * @param authUser     The username on the external authentication provider
     * @param authMethod   The authentication method
     * @param authMethodId The authentication method ID
     *
     * @return Returns the {@link XdatUserAuth} entry for the user with the given authentication username and provider.
     */
    public XdatUserAuth findByAuthUsernameAndProvider(final String authUser, final String authMethod, final String authMethodId) {
        if (StringUtils.isAnyBlank(authUser, authMethod)) {
            throw new IllegalArgumentException("You must provide both a username and an authentication method.");
        }
        if (!StringUtils.equals(XdatUserAuthService.LOCALDB, authMethod) && StringUtils.isBlank(authMethodId)) {
            throw new IllegalArgumentException("You must provide both an authentication method ID for all authentication methods that are NOT 'localdb'.");
        }
        return findByUserAuthExample(new XdatUserAuth(authUser, authMethod, StringUtils.defaultIfBlank(authMethodId, null)), EXCLUSION_PROPERTIES_USERNAME_AUTH_METHOD);
    }

    /**
     * Finds the {@link XdatUserAuth} entry for a user with the given XDAT username (i.e. username on the local XNAT system)
     * with the most recent successful login.
     *
     * @param xdatUser The username on the local XNAT system
     *
     * @return Returns the {@link XdatUserAuth} entry for the user with the specified username and recent successful login.
     */
    public XdatUserAuth findByXdatUsernameAndMostRecentSuccessfulLogin(final String xdatUser) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(xdatUser);
        example.setLockoutTime(null);
        return findByUserAuthExample(example, EXCLUSION_PROPERTIES_XDAT_USER_LOCK);
    }

    /**
     * Finds the {@link XdatUserAuth} entry for a user with the given XDAT username (i.e. username on the local XNAT system)
     * and specified provider.
     *
     * @param xdatUser     The username on the local XNAT system
     * @param authMethod   The authentication method
     * @param authMethodId The authentication method ID
     *
     * @return Returns the {@link XdatUserAuth} entry for the user with the given authentication username and provider.
     */
    public XdatUserAuth findByXdatUsernameAndAuth(final String xdatUser, final String authMethod, final String authMethodId) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(xdatUser);
        example.setAuthMethod(authMethod);
        example.setAuthMethodId(StringUtils.defaultIfBlank(authMethodId, null));
        return findByUserAuthExample(example, EXCLUSION_PROPERTIES_XDAT_USER_DETAILS);
    }

    /**
     * Finds all {@link XdatUserAuth} entries for users with the given authentication username (i.e. username on the external
     * authentication provider).
     *
     * @param authUser The username on the external authentication provider
     *
     * @return Returns a list of {@link XdatUserAuth} entries for users with the given authentication username.
     */
    public List<XdatUserAuth> findAllByAuthUsername(final String authUser) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setAuthUser(authUser);
        return findAllByUserAuthExample(example, EXCLUSION_PROPERTIES_USERNAME);
    }

    /**
     * Finds all {@link XdatUserAuth} entries for users with the given XDAT username (i.e. username on the local XNAT system).
     *
     * @param xdatUser The username on the local XNAT system
     *
     * @return Returns a list of {@link XdatUserAuth} entries for users with the given XDAT username.
     */
    public List<XdatUserAuth> findUsersByXdatUsername(final String xdatUser) {
        final XdatUserAuth example = new XdatUserAuth();
        example.setXdatUsername(xdatUser);
        return findAllByUserAuthExample(example, EXCLUSION_PROPERTIES_XDAT_USER);
    }

    /**
     * Finds a single {@link XdatUserAuth} entry matching the given example, ignoring the specified properties.
     * If more than one match is found, the first one with a matching authentication or XDAT username (depending
     * on which is provided) is returned.
     *
     * @param example             The example {@link XdatUserAuth} to match.
     * @param exclusionProperties The properties to ignore when matching.
     *
     * @return Returns a single {@link XdatUserAuth} entry matching the given example, or <pre>null</pre> if no match is found.
     */
    private XdatUserAuth findByUserAuthExample(final XdatUserAuth example, final String[] exclusionProperties) {
        final AtomicBoolean hasAuthUser = new AtomicBoolean(true);
        final String username = StringUtils.getIfBlank(example.getAuthUser(), () -> {
            hasAuthUser.set(false);
            return example.getXdatUsername();
        });
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("A username–either auth username or XDAT username–must be provided.");
        }
        final List<XdatUserAuth> userAuths = findAllByUserAuthExample(example, exclusionProperties);
        if (CollectionUtils.isEmpty(userAuths)) {
            return null;
        }
        return userAuths.stream().filter(userAuth -> StringUtils.equalsIgnoreCase(username, hasAuthUser.get() ? userAuth.getAuthUser() : userAuth.getXdatUsername())).findFirst().orElse(null);
    }

    private List<XdatUserAuth> findAllByUserAuthExample(final XdatUserAuth example, final String[] exclusionProperties) {
        return findByExample(example, exclusionProperties);
    }

    private static final String   PARAM_AUTH_USER                            = "authUser";
    private static final String   PARAM_AUTH_METHOD                          = "authMethod";
    private static final String   PARAM_AUTH_METHOD_ID                       = "authMethodId";
    private static final String   QUERY_HAS_USER_AND_AUTH                    = "SELECT COUNT(*) > 0 FROM XdatUserAuth where enabled = true AND authUser = :" + PARAM_AUTH_USER + " AND authMethod = :" + PARAM_AUTH_METHOD;
    private static final String   QUERY_HAS_USER_AND_AUTH_AND_ID             = QUERY_HAS_USER_AND_AUTH + " AND authMethodId = :" + PARAM_AUTH_METHOD_ID;
    private static final String   QUERY_GET_USERNAME_BY_USER_AND_AUTH_AND_ID = "SELECT xdatUsername FROM XdatUserAuth where enabled = true AND authUser = :" + PARAM_AUTH_USER + " AND authMethod = :" + PARAM_AUTH_METHOD + " AND authMethodId = :" + PARAM_AUTH_METHOD_ID;
    private static final String[] EXCLUSION_PROPERTIES_USERNAME              = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "verified", PARAM_AUTH_METHOD_ID, "failedLoginAttempts", PARAM_AUTH_METHOD, "lastSuccessfulLogin", "passwordUpdated", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_XDAT_USER_DETAILS     = AbstractHibernateEntity.getExcludedProperties(PARAM_AUTH_USER, "verified", "failedLoginAttempts", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_XDAT_USER             = AbstractHibernateEntity.getExcludedProperties(PARAM_AUTH_USER, "verified", PARAM_AUTH_METHOD_ID, "failedLoginAttempts", PARAM_AUTH_METHOD, "lastSuccessfulLogin", "passwordUpdated", "lockoutTime");
    private static final String[] EXCLUSION_PROPERTIES_USERNAME_AUTH_METHOD  = AbstractHibernateEntity.getExcludedProperties("xdatUsername", "failedLoginAttempts", "lastSuccessfulLogin", "lastLoginAttempt", "passwordUpdated", "lockoutTime", "authorities");
    private static final String[] EXCLUSION_PROPERTIES_XDAT_USER_LOCK        = AbstractHibernateEntity.getExcludedProperties(PARAM_AUTH_USER, "verified", PARAM_AUTH_METHOD_ID, "failedLoginAttempts", PARAM_AUTH_METHOD, "lastSuccessfulLogin", "passwordUpdated");
}
