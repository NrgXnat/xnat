/*
 * core: org.nrg.xdat.security.helpers.Users
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserAttributes;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Users {
    /**
     * Returns the currently configured user management service.  You can customize the implementation returned by
     * adding a new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured
     * package). You can change the default implementation returned via the security.userManagementService.default
     * configuration parameter.
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
                    singleton = contextService.getBean(UserManagementServiceI.class);
                    if (singleton != null) {
                        return singleton;
                    }
                } catch (NoSuchBeanDefinitionException ignored) {
                    // This is OK, we'll just create it from the indicated class.
                }
            }
            try {
                final List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userManagementService.package", "org.nrg.xdat.security.user.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (UserManagementServiceI.class.isAssignableFrom(clazz)) {
                            singleton = (UserManagementServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IOException | IllegalAccessException e) {
                log.error("", e);
            }

            //default to XDATUser implementation (unless a different default is configured)
            if (singleton == null) {
                try {
                    final String className = XDAT.safeSiteConfigProperty("security.userManagementService.default", DEFAULT_USER_SERVICE);
                    singleton = (UserManagementServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.error("", e);
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
     * @return Returns the newly created (empty) user object
     */
    public static UserI createUser() {
        final UserManagementServiceI service = getUserManagementService();
        return service.createUser();
    }

    /**
     * Return a freshly created user object populated with the passed parameters. The user object may or may not already
     * exist in the database.
     *
     * @return Returns the newly created user
     */
    public static UserI createUser(Map<String, ?> params) throws UserFieldMappingException, UserInitException {
        final UserManagementServiceI service = getUserManagementService();
        return service.createUser(params);
    }

    /**
     * Return a User object for the referenced username.
     *
     * @return Returns the user object with the given username
     *
     * @throws UserNotFoundException When the requested user can't be found.
     */
    public static UserI getUser(String username) throws UserInitException, UserNotFoundException {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUser(username);
    }

    /**
     * Return a User object for the referenced user id.
     *
     * @return Returns the user object that has the given ID
     */
    public static UserI getUser(Integer user_id) throws UserNotFoundException, UserInitException {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUser(user_id);
    }

    /**
     * Returns the user object for the system's primary administrator as configured in {@link SiteConfigPreferences#getPrimaryAdminUsername()}.
     *
     * @return The primary administrator.
     */
    public static UserI getAdminUser() {
        final XnatUserProvider provider = XDAT.getContextService().getBean("primaryAdminUserProvider", XnatUserProvider.class);
        if (provider == null) {
            log.warn("Couldn't locate the primaryAdminUserProvider user provider instance. Things will probably go badly from here on out.");
            return null;
        }
        return provider.get();
    }

    /**
     * Return the user objects with matching email addresses
     *
     * @return Returns a list of users who have the given email
     */
    public static List<? extends UserI> getUsersByEmail(String email) {
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
     * @return Returns a list of all users
     */
    public static List<? extends UserI> getUsers() {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUsers();
    }

    /**
     * Return the guest user for this server. This calls the {@link #getGuest(boolean)} method, passing <b>false</b> for
     * the <b>invalidate</b> parameter (i.e. the guest user is <i>not</i> regenerated).
     *
     * @return Returns the guest user.
     *
     * @throws UserNotFoundException When the requested user can't be found.
     * @throws UserInitException     When an error occurs accessing the user records.
     */
    @Nonnull
    public static UserI getGuest() throws UserNotFoundException, UserInitException {
        return getGuest(false);
    }

    /**
     * Return the guest user for this server.
     *
     * @param invalidate Indicates whether any cached guest user should be invalidated and regenerated.
     *
     * @return Returns the guest user.
     *
     * @throws UserNotFoundException When the requested user can't be found.
     * @throws UserInitException     When an error occurs accessing the user records.
     */
    @Nonnull
    public static UserI getGuest(final boolean invalidate) throws UserNotFoundException, UserInitException {
        final UserManagementServiceI service = getUserManagementService();
        if (invalidate) {
            service.invalidateGuest();
        }
        return service.getGuestUser();
    }

    /**
     * Return a string identifying the type of user implementation that is being used (xdat:user)
     *
     * @return Returns the string identifying the user implementation
     */
    public static String getUserDataType() {
        final UserManagementServiceI service = getUserManagementService();
        return service.getUserDataType();
    }

    /**
     * clear any objects that might be cached for this user
     *
     * @param user The user whose cache should be cleared.
     */
    public static void clearCache(UserI user) {
        final UserManagementServiceI service = getUserManagementService();
        service.clearCache(user);
    }

    /**
     * Save the user object
     *
     * @param user              The user object to be saved.
     * @param authenticatedUser The user saving the user object.
     * @param overrideSecurity  Whether to check if this user can modify this user object (should be false if authenticatedUser is null)
     * @param event             The event metadata.
     *
     * @throws Exception When something goes wrong.
     */
    public static void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventMetaI event) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.save(user, authenticatedUser, overrideSecurity, event);
    }

    /**
     * Save the user object
     *
     * @param user              The user object to be saved.
     * @param authenticatedUser The user saving the user object.
     * @param overrideSecurity  Whether to check if this user can modify this user object (should be false if authenticatedUser is null)
     * @param event             The event details.
     *
     * @throws Exception When something goes wrong.
     */
    public static void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails event) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.save(user, authenticatedUser, overrideSecurity, event);
    }

    /**
     * Validate the user object and see if it meets whatever requirements have been met by the system
     *
     * @param user The user object to be validated.
     *
     * @return Returns whether the user meets whatever requirements have been met by the system
     *
     * @throws Exception When something goes wrong.
     */
    public static ValidationResultsI validate(UserI user) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        return service.validate(user);
    }

    /**
     * Enable the user account
     *
     * @param user              The user object to be enabled.
     * @param authenticatedUser The user saving the user object.
     * @param event             The event details.
     *
     * @throws Exception When something goes wrong.
     */
    public static void enableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.enableUser(user, authenticatedUser, event);
    }

    /**
     * Disable the user account
     *
     * @param user              The user object to be disabled.
     * @param authenticatedUser The user saving the user object.
     * @param event             The event details.
     *
     * @throws Exception When something goes wrong.
     */
    public static void disableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        service.disableUser(user, authenticatedUser, event);
    }

    /**
     * See whether the passed user can authenticate using the passed credentials
     *
     * @param user        The user to be authenticated.
     * @param credentials The credentials to be used for authentication.
     *
     * @return Whether authentication was successful
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean authenticate(UserI user, Credentials credentials) throws Exception {
        final UserManagementServiceI service = getUserManagementService();
        return service.authenticate(user, credentials);
    }

    /**
     * Retrieve the username for this user PK (Integer)
     *
     * @param xdatUserId The user record primary key.
     *
     * @return Returns the username for a given user ID
     */
    public static String getUsername(final Object xdatUserId) {
        return xdatUserId instanceof Integer
               ? getUsername((Integer) xdatUserId)
               : null;
    }

    /**
     * Retrieve the username for this user PK (Integer)
     *
     * @param xdatUserId The user record primary key.
     *
     * @return The user's username.
     */
    public static String getUsername(Integer xdatUserId) {
        if (xdatUserId == null) {
            return null;
        }

        final String username = getCachedUserIds().get(xdatUserId);
        if (username != null) {
            return username;
        }

        //check if it was added since init
        try {
            final String queried = XDAT.getNamedParameterJdbcTemplate().queryForObject("SELECT login FROM xdat_user WHERE xdat_user_id = :xdatUserId", new MapSqlParameterSource("xdatUserId", xdatUserId), String.class);
            getCachedUserIds().put(xdatUserId, queried);
            return queried;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Retrieve the user PK (Integer) for this username
     *
     * @param username The username.
     *
     * @return The user's ID
     */
    public static Integer getUserId(final String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }

        // Retrieve cached ID if stored
        final Map<Integer, String> cache = getCachedUserIds();
        if (cache.containsValue(username)) {
            for (final int userId : cache.keySet()) {
                if (StringUtils.equalsIgnoreCase(username, cache.get(userId))) {
                    return userId;
                }
            }
        }

        // Check if it was added since init
        try {
            final Integer userId = XDAT.getNamedParameterJdbcTemplate().queryForObject("select xdat_user_id FROM xdat_user WHERE login = :username", new MapSqlParameterSource("username", username), Integer.class);
            cache.put(userId, username);
            return userId;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Get all usernames on this server
     *
     * @return Returns a collection of the usernames on this server
     */
    public static Collection<String> getAllLogins() {
        try {
            //noinspection unchecked
            return ElementSecurity.GetDistinctIdValuesFor("xdat:user", "xdat:user.login", null).values();
        } catch (Exception e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    /**
     * Return a random 64-character string for use as a salt
     *
     * @return A new random salt
     */
    @Nonnull
    public static String createNewSalt() {
        return createRandomString(64);
    }

    /**
     * Return a random string with the indicated number of characters.
     *
     * @return A new random string.
     */
    @Nonnull
    public static String createRandomString(final int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, new SecureRandom());
    }

    /**
     * Get the last login date/time for the user account
     *
     * @param user The user to check for login date/time.
     *
     * @return Returns the last login date/time for the user account
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("RedundantThrows")
    public static Date getLastLogin(UserI user) throws Exception {
        try {
            return XDAT.getNamedParameterJdbcTemplate().queryForObject("SELECT login_date FROM xdat_user_login WHERE user_xdat_user_id = :xdatUserId ORDER BY login_date DESC LIMIT 1", new MapSqlParameterSource("xdatUserId", user.getID()), Date.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Verify if this user account is allowed to login (enabled, verified, not locked, etc)
     *
     * @param user The user account to verify.
     */
    @SuppressWarnings("unused")
    public static void validateUserLogin(UserI user) {
        if (!user.isEnabled()) {
            throw new DisabledException("Attempted login to disabled account: " + user.getUsername());
        }
        boolean verificationRequired = XDAT.getSiteConfigPreferences().getEmailVerification();
        if ((verificationRequired && !user.isVerified()) || !user.isAccountNonLocked()) {
            throw new CredentialsExpiredException("Attempted login to unverified or locked account: " + user.getUsername());
        }
    }

    /**
     * Tests password and salt for existing and updated user to determine what value should be set when user is saved.
     * The password value will always be encoded using the returned salt.
     *
     * @param existing The existing user.
     * @param updated  The updated user.
     *
     * @return The password and salt attributes to be set returned in a map.
     *
     * @throws PasswordComplexityException If the password doesn't meet the system's specified complexity requirements.
     */
    public static Map<UserAttributes, String> getUpdatedPassword(final UserI existing, final UserI updated) throws PasswordComplexityException {
        final String  savedPassword    = existing.getPassword();
        final String  savedSalt        = existing.getSalt();
        final boolean hasSavedPassword = StringUtils.isNotBlank(savedPassword);
        final boolean hasSavedSalt     = StringUtils.isNotBlank(savedSalt);

        final String  updatedPassword    = updated.getPassword();
        final boolean hasUpdatedPassword = StringUtils.isNotBlank(updatedPassword) && !StringUtils.equals(savedPassword, updatedPassword);

        // If there's no saved password, no saved salt, or an updated password, create a new salt whether or not we already have one.
        final String saltToSet = !hasSavedPassword || !hasSavedSalt || hasUpdatedPassword ? Users.createNewSalt() : savedSalt;

        // check if the password is being updated (also do this if password remains the same but salt is empty)
        final String passwordToSet;

        if (hasUpdatedPassword) {
            // The user specified a new password, test for validity.
            final String message = XDAT.getContextService().getBean(PasswordValidatorChain.class).isValid(updatedPassword, updated);
            if (StringUtils.isNotBlank(message)) {
                throw new PasswordComplexityException(message);
            }

            passwordToSet = encode(updatedPassword, saltToSet);
        } else if (!hasSavedPassword) {
            // If the user didn't specify a new password and there's no saved password, generate a new random one.
            passwordToSet = encode(createRandomString(32), saltToSet);
        } else if (!hasSavedSalt) {
            // If there's no saved salt, the password is plain text.
            passwordToSet = encode(savedPassword, saltToSet);
        } else {
            passwordToSet = savedPassword;
        }

        return new HashMap<UserAttributes, String>() {{
            put(UserAttributes.password, passwordToSet);
            put(UserAttributes.salt, saltToSet);
        }};
    }

    public static String encode(final String value, final String salt) {
        return _encoder.encodePassword(value, salt);
    }

    public static boolean isPasswordValid(final String encoded, final String plaintext, final String salt) {
        return _encoder.isPasswordValid(encoded, plaintext, salt);
    }

    @SuppressWarnings("deprecation")
    public static PasswordEncoder getEncoder() {
        return _encoder;
    }

    private static Map<Integer, String> getCachedUserIds() {
        if (_users.isEmpty()) {
            synchronized (_users) {
                //initialize database users, only done once per server restart
                try {
                    //noinspection unchecked
                    _users.putAll(XFTTable.Execute("select xdat_user_id,login FROM xdat_user ORDER BY xdat_user_id;", null, null).toHashtable("xdat_user_id", "login"));
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        return _users;
    }

    private static final ShaPasswordEncoder   _encoder             = new ShaPasswordEncoder(256);
    private static final Map<Integer, String> _users               = new ConcurrentHashMap<>();
    private static final String               DEFAULT_USER_SERVICE = "org.nrg.xdat.security.XDATUserMgmtServiceImpl";

    private static UserManagementServiceI singleton = null;
}
