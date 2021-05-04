/*
 * core: org.nrg.xdat.rest.AbstractXapiRestController
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest;

import static lombok.AccessLevel.PROTECTED;
import static org.nrg.framework.exceptions.NrgServiceError.Unknown;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides basic functions for integrating Spring REST controllers with XNAT.
 */
@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractXapiRestController {
    protected AbstractXapiRestController(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        _userManagementService = userManagementService;
        _roleHolder = roleHolder;
    }

    /**
     * Gets the attachment disposition for the specified filename. This is a standard format that looks like:
     *
     * <pre>attachment; filename="filename.txt"</pre>
     * <p>
     * This method accepts multiple strings, which it just concatenates until the last string, which is appended
     * to the previously concatenated strings after a dot (".") is appended. For example, if you called this method
     * with the strings "foo", "bar", and "txt", the resulting filename would be "foobar.txt". If you specify a
     * single string, no dot is added.
     *
     * @param parts The filename parts to concatenate and format.
     *
     * @return The filename formatted as an attachment disposition.
     */
    protected static String getAttachmentDisposition(final String... parts) {
        final int    maxIndex = parts.length - 1;
        final String filename = maxIndex == 0 ? parts[0] : StringUtils.join(ArrayUtils.subarray(parts, 0, maxIndex)) + "." + parts[maxIndex];
        return String.format(ATTACHMENT_DISPOSITION, filename);
    }

    /**
     * Retrieves the user object for the current session.
     *
     * @return The user object for the current session, or null if the user object can't be found.
     */
    protected UserI getSessionUser() {
        return Users.getUserPrincipal(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
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
        if (StringUtils.isBlank(username) || StringUtils.equalsIgnoreCase(username, user.getUsername())) {
            return getRoleHolder().getRoles(user);
        }
        // Only a site admin can get the roles for another user.
        if (!getRoleHolder().isSiteAdmin(user)) {
            return null;
        }
        try {
            final UserI other = getUserManagementService().getUser(username);
            return getRoleHolder().getRoles(other);
        } catch (UserInitException e) {
            throw new NrgServiceRuntimeException(Unknown, "An error occurred trying to access the user " + username, e);
        } catch (UserNotFoundException e) {
            log.info("User {} requested by {}, but not found", username, user.getUsername());
            return null;
        }
    }

    /**
     * Indicates whether the user is permitted to access a particular REST function. Access is granted if the
     * request URL matches one of the patterns specified in the open URLs list <i>or</i> if the user is a site
     * administrator <i>or</i> the user's login name matches one of the submitted <b>id</b> values. The latter case is
     * useful to test whether a user can edit the corresponding user account or is an owner of a project, for example.
     *
     * @param request  The request with a URI path to test.
     * @param openUrls The list of open URLs configured for the system.
     * @param ids      One or more IDs that can be tested against the username.
     *
     * @return Returns null if the user is permitted to access the API, otherwise it returns an error status code.
     *
     * @deprecated All uses of this and similar methods should be replaced by {@link XapiRequestMapping#restrictTo()}.
     */
    @Deprecated
    protected HttpStatus isPermitted(final HttpServletRequest request, final Collection<AntPathRequestMatcher> openUrls, final String... ids) {
        for (final AntPathRequestMatcher matcher : openUrls) {
            if (matcher.matches(request)) {
                return null;
            }
        }
        return isPermitted(ids);
    }

    /**
     * Indicates whether the user is permitted to access a particular REST function. Access is granted if:
     *
     * <ul>
     * <li>The user is a site administrator</li>
     * <li>
     * The user's login name matches one of the submitted <b>idsAndRoles</b> values (useful to test whether the
     * user can edit the corresponding user account)</li>
     * <li>
     * One of the user's roles matches one of the submitted <b>idsAndRoles</b> values (useful to test whether
     * the user is an owner or member of a project, for example)</li>
     * </ul>
     *
     * @param idsAndRoles One or more IDs or roles that can be tested against the user's login name and authorities.
     *
     * @return Returns null if the user is permitted to access the API, otherwise it returns an error status code.
     *
     * @deprecated All uses of this and similar methods should be replaced by {@link XapiRequestMapping#restrictTo()}.
     */
    @Deprecated
    protected HttpStatus isPermitted(final String... idsAndRoles) {
        final UserI user = getSessionUser();
        if (user == null) {
            log.debug("No user principal found, returning unauthorized.");
            return UNAUTHORIZED;
        }

        if (getRoleHolder().isSiteAdmin(user)) {
            log.debug("User {} is a site administrator, permitted.", user.getUsername());
            return null;
        }

        final List<String> idsAndRolesList = Arrays.asList(idsAndRoles);
        if (idsAndRolesList.contains(user.getUsername())) {
            log.debug("User {} appeared in the list of permitted users, permitted.", user.getUsername());
            return null;
        }
        if (!Collections.disjoint(idsAndRolesList, getRoleHolder().getRoles(user))) {
            log.debug("User {} has a role included in the list of permitted roles.", user.getUsername());
            return null;
        }

        return FORBIDDEN;
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
     *
     * @deprecated Use the {@link #isPermitted(String...)} method instead, which allows both user login names and roles.
     */
    @Deprecated
    protected HttpStatus hasPermittedRole(final String... roles) {
        final UserI user = getSessionUser();
        if (user == null) {
            return UNAUTHORIZED;
        }
        if (ListUtils.intersection(Arrays.asList(roles), new ArrayList<>(getRoleHolder().getRoles(user))).size() > 0 || getRoleHolder().isSiteAdmin(user)) {
            return null;
        }
        return FORBIDDEN;
    }

    /**
     * Writes out the properties and values that are being set by the current user.
     *
     * @param properties The properties and values being set.
     */
    @SuppressWarnings("unused")
    protected void logSetProperties(final Map<String, String> properties) {
        if (log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ").append(getSessionUser().getUsername()).append(" is setting the values for the following properties:\n");
            for (final String name : properties.keySet()) {
                message.append(" * ").append(name).append(": ").append(properties.get(name)).append("\n");
            }
            log.info(message.toString());
        }
    }

    /**
     * Writes out the properties and values that are being set by the current user.
     *
     * @param properties The properties and values being set.
     */
    protected void logSetProperties(final Properties properties) {
        if (log.isInfoEnabled()) {
            final StringBuilder message = new StringBuilder("User ").append(getSessionUser().getUsername()).append(" is setting the values for the following properties:\n");
            for (final String name : properties.stringPropertyNames()) {
                message.append(" * ").append(name).append(": ").append(properties.get(name)).append("\n");
            }
            log.info(message.toString());
        }
    }

    private static final String ATTACHMENT_DISPOSITION = "attachment; filename=\"%s\"";

    private final UserManagementServiceI _userManagementService;
    private final RoleHolder             _roleHolder;
}
