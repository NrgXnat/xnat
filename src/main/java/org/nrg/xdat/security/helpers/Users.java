/*
 * core: org.nrg.xdat.security.helpers.Users
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import org.apache.commons.lang3.RandomStringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.PasswordAuthenticationException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class Users {
    private static final String DEFAULT_USER_SERVICE = "org.nrg.xdat.security.XDATUserMgmtServiceImpl";

    private static final Logger logger = LoggerFactory.getLogger(Users.class);

    private static UserManagementServiceI singleton = null;

    /**
     * Returns the currently configured user management service
     *
     * You can customize the implementation returned by adding a new implementation to the org.nrg.xdat.security.user.custom package (or a diffently configured package).
     *
     * You can change the default implementation returned via the security.userManagementService.default configuration parameter
     *
     * @return The service.
     */
    @Nonnull
    public static UserManagementServiceI getUserManagementService() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (singleton == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return singleton = contextService.getBean(UserManagementServiceI.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    // This is OK, we'll just create it from the indicated class.
                }
            }
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userManagementService.package", "org.nrg.xdat.security.user.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (UserManagementServiceI.class.isAssignableFrom(clazz)) {
                            singleton = (UserManagementServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IOException | IllegalAccessException e) {
                logger.error("", e);
            }

            //default to XDATUser implementation (unless a different default is configured)
            if (singleton == null) {
                try {
                    final String className = XDAT.safeSiteConfigProperty("security.userManagementService.default", DEFAULT_USER_SERVICE);
                    singleton = (UserManagementServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.error("", e);
                }
            }
            if (singleton == null) {
                throw new NrgServiceRuntimeException(NrgServiceError.UserServiceError, "Couldn't create an instance of the user management service.");
            }
        }
        return singleton;
    }

    /**
     * Return a freshly created (empty) user.
     *
     * @return
     */
    public static UserI createUser() {
        final UserManagementServiceI service = getUserManagementService();
        return service.createUser();
    }

    /**
     * Return a freshly created user object populated with the passed parameters.
     *
     * Object may or may not already exist in the database.
     *
     * @return
     */
    public static UserI createUser(Map<String, ? extends Object> params) throws UserFieldMappingException, UserInitException {
        final UserManagementServiceI service = getUserManagementService();
        return service.createUser(params);
    }

    /**
     * Return a User object for the referenced username.
     *
     * @return
     *
     * @throws UserNotFoundException
     */
    public static UserI getUser(String username) throws UserInitException, UserNotFoundException {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUser(username);
    }

    /**
     * Return a User object for the referenced user id.
     *
     * @return
     */
    public static UserI getUser(Integer user_id) throws UserNotFoundException, UserInitException {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUser(user_id);
    }

    /**
     * Return the user objects with matching email addresses
     *
     * @return
     */
    public static List<UserI> getUsersByEmail(String email) {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUsersByEmail(email);
    }

    /**
     * Return the path to the user's cache directory.
     *
     * @param user The user.
     *
     * @return The cache path for the user.
     */
    public static String getUserCacheUploadsPath(final UserI user) {
        return Paths.get(XDAT.getSiteConfigPreferences().getCachePath(), "USERS", user.getID().toString()).toString();
    }

    /**
     * Gets a File representing a sub-file within the user cache directory based on the passed dir hierarchy
     *
     * @param user The user.
     * @param dirs The directories.
     *
     * @return A file from the user cache.
     */
    public static File getUserCacheFile(final UserI user, String... dirs) {
        return (dirs == null ? Paths.get(getUserCacheUploadsPath(user)) : Paths.get(getUserCacheUploadsPath(user), dirs)).toFile();
    }

    /**
     * Return a complete list of all the users in the database.
     *
     * @return
     */
    public static List<UserI> getUsers() {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUsers();
    }

    /**
     * Return the guest user for this server
     *
     * @return
     *
     * @throws UserNotFoundException
     * @throws UserInitException
     */
    @Nonnull
    public static UserI getGuest() throws UserNotFoundException, UserInitException {
        return getUserManagementService().getGuestUser();
    }

    /**
     * Return a string identifying the type of user implementation that is being used (xdat:user)
     *
     * @return
     */
    public static String getUserDataType() {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUserDataType();
    }

    /**
     * clear any objects that might be cached for this user
     *
     * @param user
     */
    public static void clearCache(UserI user) {
        final UserManagementServiceI service = getUserManagementService();
        service.clearCache(user);
    }

    /**
     * Save the user object
     *
     * @param user
     * @param authenticatedUser
     * @param overrideSecurity  : whether to check if this user can modify this user object (should be false if authenticatedUser is null)
     * @param c
     *
     * @throws Exception
     */
    public static void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventMetaI c) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.save(user, authenticatedUser, overrideSecurity, c);
    }

    /**
     * Save the user object
     *
     * @param user
     * @param authenticatedUser
     * @param overrideSecurity  : whether to check if this user can modify this user object (should be false if authenticatedUser is null)
     * @param c
     *
     * @throws Exception
     */
    public static void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails c) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.save(user, authenticatedUser, overrideSecurity, c);
    }

    /**
     * Validate the user object and see if it meets whatever requirements have been met by the system
     *
     * @param user
     *
     * @return
     *
     * @throws Exception
     */
    public static ValidationResultsI validate(UserI user) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        return service.validate(user);
    }

    /**
     * Enable the user account
     *
     * @param user
     * @param authenticatedUser
     * @param ci
     *
     * @throws InvalidPermissionException
     * @throws Exception
     */
    public static void enableUser(UserI user, UserI authenticatedUser, EventDetails ci) throws InvalidPermissionException, Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.enableUser(user, authenticatedUser, ci);
    }

    /**
     * Disable the user account
     *
     * @param user
     * @param authenticatedUser
     * @param ci
     *
     * @throws InvalidPermissionException
     * @throws Exception
     */
    public static void disableUser(UserI user, UserI authenticatedUser, EventDetails ci) throws InvalidPermissionException, Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.disableUser(user, authenticatedUser, ci);
    }

    /**
     * See whether the passed user can authenticate using the passed credentials
     *
     * @param u
     * @param cred
     *
     * @return
     *
     * @throws PasswordAuthenticationException
     * @throws Exception
     */
    public static boolean authenticate(UserI u, Credentials cred) throws PasswordAuthenticationException, Exception {
        final UserManagementServiceI service = getUserManagementService();
        return service.authenticate(u, cred);
    }

    /**
     * Retrieve the username for this user PK (Integer)
     *
     * @param xdat_user_id
     *
     * @return
     */
    public static String getUsername(Object xdat_user_id) {
        if (xdat_user_id == null) {
            return null;
        } else if (xdat_user_id instanceof Integer) {
            return getUsername((Integer) xdat_user_id);
        } else {
            return null;
        }
    }

    /**
     * Retrieve the username for this user PK (Integer)
     *
     * @param xdat_user_id
     *
     * @return
     */
    public static String getUsername(Integer xdat_user_id) {
        if (xdat_user_id == null) {
            return null;
        }

        String u = getCachedUserIds().get(xdat_user_id);
        if (u == null) {
            //check if it was added since init
            try {
                u = (String) PoolDBUtils.ReturnStatisticQuery("select login FROM xdat_user WHERE xdat_user_id=" + xdat_user_id, "login", null, null);
                if (u != null) {
                    getCachedUserIds().put(xdat_user_id, u);
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        return u;
    }

    /**
     * Retrieve the user PK (Integer) for this username
     *
     * @param username The username.
     *
     * @return
     */
    public static Integer getUserid(String username) {
        if (username == null) {
            return null;
        }

        //retrieve cached id
        for (Entry<Integer, String> entry : getCachedUserIds().entrySet()) {
            if (username.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        //check if it was added since init
        Integer u;
        try {
            u = (Integer) PoolDBUtils.ReturnStatisticQuery("select xdat_user_id FROM xdat_user WHERE login='" + username + "'", "xdat_user_id", null, null);
            if (u != null) {
                getCachedUserIds().put(u, username);
            }
        } catch (Exception e) {
            logger.error("", e);
            u = null;
        }

        return u;
    }

    private static final Object               usercache = new Object();
    private static       Map<Integer, String> users     = null;

    private static Map<Integer, String> getCachedUserIds() {
        if (users == null) {
            synchronized (usercache) {
                users = new Hashtable<>();
                //initialize database users, only done once per server restart
                try {
                    users.putAll(XFTTable.Execute("select xdat_user_id,login FROM xdat_user ORDER BY xdat_user_id;", null, null).toHashtable("xdat_user_id", "login"));
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }
        return users;
    }

    /**
     * Get all usernames on this server
     *
     * @return
     */
    public static java.util.Collection<String> getAllLogins() {
        try {
            return ElementSecurity.GetDistinctIdValuesFor("xdat:user", "xdat:user.login", null).values();
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    /**
     * Retrn a random string for use as a salt
     *
     * @return
     */
    public static String createNewSalt() {
        String salt = RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom());
        return salt;
    }

    /**
     * Get the last login date/time for the user account
     *
     * @param user
     *
     * @return
     *
     * @throws SQLException
     * @throws Exception
     */
    public static Date getLastLogin(UserI user) throws SQLException, Exception {
        String query = "SELECT login_date FROM xdat_user_login WHERE user_xdat_user_id=" + user.getID() + " ORDER BY login_date DESC LIMIT 1";
        return (Date) PoolDBUtils.ReturnStatisticQuery(query, "login_date", user.getDBName(), user.getUsername());
    }

    /**
     * Verify if this user account is allowed to login (enabled, verified, not locked, etc)
     *
     * @param u
     */
    public static void validateUserLogin(UserI u) {
        if (!u.isEnabled()) {
            throw new DisabledException("Attempted login to disabled account: " + u.getUsername());
        }
        if ((XDAT.getSiteConfigPreferences().getEmailVerification() && !u.isVerified()) || !u.isAccountNonLocked()) {
            throw new CredentialsExpiredException("Attempted login to unverified or locked account: " + u.getUsername());
        }
    }
}
