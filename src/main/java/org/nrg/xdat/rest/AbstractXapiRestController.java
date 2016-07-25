package org.nrg.xdat.rest;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

/**
 * Provides basic functions for integrating Spring REST controllers with XNAT.
 */
// TODO: This is because IntelliJ refuses to make module associations between Gradle and Maven projects, so these show as unused.
@SuppressWarnings("unused")
public abstract class AbstractXapiRestController {
    protected AbstractXapiRestController(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        _userManagementService = userManagementService;
        _roleHolder = roleHolder;
    }

    protected UserManagementServiceI getUserManagementService() {
        return _userManagementService;
    }

    protected RoleHolder getRoleHolder() {
        return _roleHolder;
    }

    /**
     * Retrieves the user object for the current session.
     *
     * @return The user object for the current session, or null if the user object can't be found.
     */
    protected UserI getSessionUser() {
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ((principal instanceof UserI)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found principal for user: " + ((UserI) principal).getLogin());
            }
            return (UserI) principal;
        }
        return null;
    }

    /**
     * Gets the roles for the current user. System administrators can get roles for other users by calling the {@link
     * #getUserRoles(String)} method instead.
     *
     * @return A collection of the current user's roles.
     */
    protected Collection<String> getUserRoles() {
        return getUserRoles(null);
    }

    /**
     * Gets the roles for the indicated user if permitted: if the username is the same as the current user's (this is
     * the same as calling the {@link #getUserRoles()} method) or if the current user is a site administrator. If there
     * is no current user (i.e. not logged in), the user is not allowed to retrieve the indicated user's roles, or the
     * indicated user doesn't exist on the system, this method returns null.
     *
     * @param username The username for which to retrieve roles.
     *
     * @return A collection of the indicated user's roles if permitted, null otherwise.
     */
    protected Collection<String> getUserRoles(final String username) {
        final UserI user = getSessionUser();
        if (user == null) {
            return null;
        }
        // Both cases indicate retrieving roles for self, so this is always OK.
        if (StringUtils.isBlank(username) || user.getUsername().equals(username)) {
            return _roleHolder.getRoles(user);
        }
        // Only a site admin can get the roles for another user.
        if (!_roleHolder.isSiteAdmin(user)) {
            return null;
        }
        try {
            final UserI other = _userManagementService.getUser(username);
            return _roleHolder.getRoles(other);
        } catch (UserInitException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to access the user " + username, e);
        } catch (UserNotFoundException e) {
            _log.info("User {} requested by {}, but not found", username, user.getUsername());
            return null;
        }
    }

    /**
     * Indicates whether the user is permitted to access a particular REST function. Access is granted if the user is a
     * site administrator <i>or</i> the user's login name matches one of the submitted <b>id</b> values. The latter case
     * is useful to test whether a user can edit the corresponding user account or is an owner of a project, for
     * example.
     *
     * @param ids One or more IDs that can be tested against the username.
     *
     * @return Returns null if the user is permitted to access the API, otherwise it returns an error status code.
     */
    protected HttpStatus isPermitted(final String... ids) {
        final UserI user = getSessionUser();
        if (user == null) {
            if (_log.isDebugEnabled()) {
                _log.debug("No user principal found, returning unauthorized.");
            }
            return HttpStatus.UNAUTHORIZED;
        }
        if (Arrays.asList(ids).contains(user.getUsername()) || _roleHolder.isSiteAdmin(user)) {
            if (_log.isDebugEnabled()) {
                _log.debug("User " + user.getUsername() + (_roleHolder.isSiteAdmin(user) ? " is a site administrator, permitted." : " appeared in the list of permitted users."));
            }
            return null;
        }
        return HttpStatus.FORBIDDEN;
    }

    /**
     * Indicates whether the user is permitted to access a particular REST function. Access is granted if the user is a
     * site administrator <i>or</i> the user's login name matches one of the submitted <b>id</b> values. The latter case
     * is useful to test whether a user can edit the corresponding user account or is an owner of a project, for
     * example.
     *
     * @param roles One or more roles that can be tested against the user.
     *
     * @return Returns null if the user is permitted to access the API, otherwise it returns an error status code.
     */
    protected HttpStatus hasPermittedRole(final String... roles) {
        final UserI user = getSessionUser();
        if (user == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ListUtils.intersection(Arrays.asList(roles), new ArrayList<>(_roleHolder.getRoles(user))).size() > 0 || _roleHolder.isSiteAdmin(user)) {
            return null;
        }
        return HttpStatus.FORBIDDEN;
    }

    /**
     * Writes out the properties and values that are being set by the current user.
     *
     * @param properties The properties and values being set.
     */
    protected void logSetProperties(final Map<String, String> properties) {
        if (_log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ").append(getSessionUser().getUsername()).append(" is setting the values for the following properties:\n");
            for (final String name : properties.keySet()) {
                message.append(" * ").append(name).append(": ").append(properties.get(name)).append("\n");
            }
            _log.info(message.toString());
        }
    }

    /**
     * Writes out the properties and values that are being set by the current user.
     *
     * @param properties The properties and values being set.
     */
    protected void logSetProperties(final Properties properties) {
        if (_log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ").append(getSessionUser().getUsername()).append(" is setting the values for the following properties:\n");
            for (final String name : properties.stringPropertyNames()) {
                message.append(" * ").append(name).append(": ").append(properties.get(name)).append("\n");
            }
            _log.info(message.toString());
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractXapiRestController.class);

    private final UserManagementServiceI _userManagementService;
    private final RoleHolder             _roleHolder;
}


