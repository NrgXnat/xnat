/*
 * web: org.nrg.xapi.rest.users.UsersApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.users;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.Patterns;
import org.nrg.xapi.authorization.UserGroupXapiAuthorization;
import org.nrg.xapi.authorization.UserResourceXapiAuthorization;
import org.nrg.xapi.exceptions.*;
import org.nrg.xapi.model.users.User;
import org.nrg.xapi.model.users.UserAuth;
import org.nrg.xapi.model.users.UserFactory;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserChangeRequestService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Api("User Management API")
@XapiRestController
@RequestMapping(value = "/users")
@Slf4j
public class UsersApi extends AbstractXapiRestController {
    @Autowired
    public UsersApi(final UserManagementServiceI userManagementService,
                    final UserFactory factory,
                    final RoleHolder roleHolder,
                    final SessionRegistry sessionRegistry,
                    final AliasTokenService aliasTokenService,
                    final PermissionsServiceI permissionsService,
                    final NamedParameterJdbcTemplate jdbcTemplate,
                    final SiteConfigPreferences siteConfig,
                    final UserChangeRequestService userChangeRequestService) {
        super(userManagementService, roleHolder);
        _sessionRegistry = sessionRegistry;
        _aliasTokenService = aliasTokenService;
        _permissionsService = permissionsService;
        _factory = factory;
        _jdbcTemplate = jdbcTemplate;
        _siteConfig = siteConfig;
        _userChangeRequestService = userChangeRequestService;
    }

    @ApiOperation(value = "Get list of users.",
                  notes = "The primary users function returns a list of all users of the XNAT system. This includes just the username and nothing else. You can retrieve a particular user by adding the username to the REST API URL or a list of users with abbreviated user profiles by calling /xapi/users/profiles.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "A list of usernames."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<String> usersGet() {
        return new ArrayList<>(Users.getAllLogins());
    }

    @ApiOperation(value = "Get list of user profiles.",
                  notes = "The users' profiles function returns a list of all users of the XNAT system with brief information about each.",
                  responseContainer = "List",
                  response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "A list of user profiles."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of users."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "profiles", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<User> usersProfilesGet() {
        return User.getAllUsers(_jdbcTemplate);
    }

    @ApiOperation(value = "Get user profile.",
                  notes = "The user profile function returns a user of the XNAT system with brief information.",
                  response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "A user profile."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the user profile."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "profile/{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public User usersProfileGet(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable @Username final String username) throws DataFormatException, NotFoundException {
        if (!Users.isValidUsername(username)) {
            throw new DataFormatException("Invalid username");
        }
        return User.getUser(_jdbcTemplate, username);
    }

    @ApiOperation(value = "Get user auth details.",
                  notes = "The user authDetails function returns info about authentication methods that can be used for a given XNAT account.",
                  responseContainer = "List",
                  response = UserAuth.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User auth info."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the user profile."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "authDetails/{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<UserAuth> usersAuthDetailsGet(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable @Username final String username) throws DataFormatException {
        if (!Users.isValidUsername(username)) {
            throw new DataFormatException("Invalid username");
        }
        return UserAuth.getUserAuths(_jdbcTemplate, username);
    }

    @ApiOperation(value = "Get list of users who are enabled or who have interacted with the site somewhat recently.",
                  notes = "The users' profiles function returns a list of all users of the XNAT system with brief information about each.",
                  responseContainer = "List",
                  response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "A list of user profiles."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "current", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<User> currentUsersProfilesGet() {
        return User.getCurrentUsers(_jdbcTemplate, getMaxLoginInterval(), getLastModifiedInterval());
    }

    @ApiOperation(value = "Get list of active users.",
                  notes = "Returns a map of usernames for users that have at least one currently active session, i.e. logged in or associated with a valid application session. The number of active sessions and a list of the session IDs is associated with each user.",
                  responseContainer = "Map",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "A list of active users."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "active", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    @ResponseBody
    public Map<String, Map<String, Object>> getActiveUsers() {
        final Map<String, Map<String, Object>> activeUsers = new HashMap<>();
        for (final Object principal : _sessionRegistry.getAllPrincipals()) {
            final String username;
            if (principal instanceof String) {
                username = (String) principal;
            } else if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            final List<SessionInformation> sessions = _sessionRegistry.getAllSessions(principal, false);

            // Sometimes there are no sessions, which is weird but OK, we don't want to see those entries.
            if (sessions.isEmpty()) {
                continue;
            }

            final Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("sessions", sessions.stream().map(INFO_TO_ID_FUNCTION).collect(Collectors.toList()));
            sessionData.put("count", sessions.size());

            activeUsers.put(username, sessionData);
        }
        return activeUsers;
    }

    @ApiOperation(value = "Get information about active sessions for the indicated user.",
                  notes = "Returns a map containing a list of session IDs and usernames for users that have at least one currently active session, i.e. logged in or associated with a valid application session. This also includes the number of active sessions for each user.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "A list of active users."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access this user's sessions."),
                   @ApiResponse(code = 404, message = "The indicated user has no active sessions or is not a valid user."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "active/{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    @ResponseBody
    public List<String> getUserActiveSessions(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable @Username final String username) throws NotModifiedException {
        final Object located = locatePrincipalByUsername(username);
        if (located == null) {
            throw new NotModifiedException("No sessions found for user " + username);
        }
        final List<SessionInformation> sessions = _sessionRegistry.getAllSessions(located, false);
        if (sessions.isEmpty()) {
            throw new NotModifiedException("No sessions found for user " + username);
        }
        return sessions.stream().map(INFO_TO_ID_FUNCTION).collect(Collectors.toList());
    }

    @ApiOperation(value = "Gets the user with the specified user ID.",
                  notes = "Returns the serialized user object with the specified user ID.",
                  response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to view this user."),
            @ApiResponse(code = 404, message = "User not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public User getUser(@ApiParam(value = "Username of the user to fetch.", required = true) @PathVariable("username") @Username final String username) throws InitializationException {
        return getUserByUsername(username);
    }

    @ApiOperation(value = "Gets current user.", notes = "Returns the serialized user object for the logged-in user.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to view this user."),
            @ApiResponse(code = 404, message = "User not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "me", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public User get() throws InitializationException {
        return getUserByUsername(getSessionUser().getUsername());
    }

    private User getUserByUsername(String username) throws InitializationException {
        try {
            final UserI user = getUserManagementService().getUser(username);
            return _factory.getUser(user, Roles.isSiteAdmin(getSessionUser()));
        } catch (UserInitException | UserNotFoundException e) {
            throw new InitializationException("An error occurred initializing the user " + username, e);
        }
    }

    @ApiOperation(value = "Creates a new user from the request body.",
                  notes = "Returns the newly created user object.",
                  response = User.class)
    @ApiResponses({@ApiResponse(code = 201, message = "User successfully created."),
                   @ApiResponse(code = 400, message = "The submitted data was invalid."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to update this user."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = AccessLevel.Admin)
    @ResponseStatus(CREATED)
    public User createUser(@RequestBody final User model) throws DataFormatException, ResourceAlreadyExistsException, UserInitException {
        validateUser(model);

        final UserI user = getUserManagementService().createUser();
        if (user == null) {
            throw new UserInitException("Failed to create a user object for user " + model.getUsername());
        }

        user.setLogin(model.getUsername());
        user.setFirstname(model.getFirstName());
        user.setLastname(model.getLastName());
        user.setEmail(model.getEmail());
        user.setPassword(model.getPassword());
        user.setAuthorization(model.getAuthorization());

        if (model.getEnabled() != null) {
            user.setEnabled(model.getEnabled());
        }
        if (model.getEnabled() != null) {
            user.setVerified(model.getVerified());
        }

        try {
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Added, "Requested by user " + getSessionUser().getUsername(), "Created new user " + user.getUsername() + " through XAPI user management API."));

            if (BooleanUtils.isTrue(model.getVerified()) && BooleanUtils.isTrue(model.getEnabled())) {
                //When a user is enabled and verified, send a new user email
                try {
                    AdminUtils.sendNewUserEmailMessage(user.getUsername(), user.getEmail());
                } catch (Exception e) {
                    log.error("An error occurred trying to send email to the admin: new user '{}' created with email '{}'", user.getUsername(), user.getEmail(), e);
                }
            }
            return _factory.getUser(user, true);
        } catch (Exception e) {
            throw new UserInitException("Error occurred creating user " + user.getLogin(), e);
        }
    }

    @ApiOperation(value = "Updates the user object with the specified username.",
                  notes = "Returns the updated serialized user object with the specified username.",
                  response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully updated."),
                   @ApiResponse(code = 304, message = "The user object was not modified because no attributes were changed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to update this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}", consumes = {APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public User updateUser(@ApiParam(value = "The username of the user to create or update.", required = true) @PathVariable @Username final String username, @RequestBody final User model) throws UserInitException, XapiException, UserNotFoundException {
        return updateUser(username, model, true);
    }

    @ApiOperation(value = "Update ones own user account.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully updated."),
            @ApiResponse(code = 304, message = "The user object was not modified because no attributes were changed."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to update this user."),
            @ApiResponse(code = 404, message = "User not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authenticated)
    public User update(@RequestBody final User model) throws UserInitException, XapiException, UserNotFoundException {
        UserI user = getSessionUser();
        return updateUser(user.getUsername(), model, Roles.isSiteAdmin(user));
    }

    private User updateUser(String username, User model, boolean adminUpdate) throws XapiException, UserNotFoundException, UserInitException {
        final UserI user        = getUserManagementService().getUser(username);
        boolean oldEnabledFlag  = user.isEnabled();
        boolean oldVerifiedFlag = user.isVerified();

        if ((StringUtils.isNotBlank(model.getUsername())) && (!StringUtils.equals(user.getUsername(), model.getUsername()))) {
            throw new DataFormatException("The username for the submitted user object must match the username for the API call");
        }

        AtomicBoolean isDirty = new AtomicBoolean(false);
        String pendingNewEmail = null;
        if ((StringUtils.isNotBlank(model.getEmail())) && (!StringUtils.equals(user.getEmail(), model.getEmail()))) {
            if (!adminUpdate) {
                // Only admins can set an email address that's already being used.
                if (!Users.getUsersByEmail(model.getEmail()).isEmpty()) {
                    throw new XapiException(HttpStatus.BAD_REQUEST,
                            "The email address you've specified is already in use.");
                }

                if (!model.getEmail().contains("@")) {
                    throw new XapiException(HttpStatus.BAD_REQUEST, "Please use a valid email.");
                }
            }
            if (!adminUpdate && _siteConfig.getEmailVerification()) {
                // Need to re-verify the new email, set pendingNewEmail to trigger this after saving the user
                pendingNewEmail = model.getEmail();
            } else {
                user.setEmail(model.getEmail());
                isDirty.set(true);
            }
        }

        // Don't do password compare: we can't.
        if (StringUtils.isNotBlank(model.getPassword())) {
            if (!adminUpdate && (StringUtils.isBlank(model.getCurrentPassword()) ||
                    !Users.isPasswordValid(user.getPassword(), model.getCurrentPassword(), user.getSalt()))) {
                throw new XapiException(HttpStatus.BAD_REQUEST, "Current password needed to update password");
            }
            user.setPassword(model.getPassword());
            isDirty.set(true);
        }

        if (adminUpdate) {
            if ((StringUtils.isNotBlank(model.getFirstName())) && (!StringUtils.equals(user.getFirstname(), model.getFirstName()))) {
                user.setFirstname(model.getFirstName());
                isDirty.set(true);
            }
            if ((StringUtils.isNotBlank(model.getLastName())) && (!StringUtils.equals(user.getLastname(), model.getLastName()))) {
                user.setLastname(model.getLastName());
                isDirty.set(true);
            }
            if (model.getAuthorization() != null && !model.getAuthorization().equals(user.getAuthorization())) {
                user.setAuthorization(model.getAuthorization());
                isDirty.set(true);
            }
            final Boolean enabled = model.getEnabled();
            if (enabled != null && enabled != user.isEnabled()) {
                user.setEnabled(enabled);
                if (!enabled) {
                    //When a user is disabled, deactivate all their AliasTokens
                    try {
                        _aliasTokenService.deactivateAllTokensForUser(user.getLogin());
                    } catch (Exception e) {
                        log.error("Unable to deactivate alias tokens for {}", user.getLogin(), e);
                    }
                }
                isDirty.set(true);
            }
            final Boolean verified = model.getVerified();
            if (verified != null && verified != user.isVerified()) {
                user.setVerified(verified);
                isDirty.set(true);
            }
        }

        if (!isDirty.get() && pendingNewEmail == null) {
            throw new NotModifiedException("No attributes were changed for user " + username);
        }

        try {
            // if the user only updated email and verification is on, no changes have yet been made
            if (isDirty.get()) {
                getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Modified, "", ""));
            }
            if (BooleanUtils.toBooleanDefaultIfNull(model.getVerified(), false) && BooleanUtils.toBooleanDefaultIfNull(model.getEnabled(), false) && (!oldEnabledFlag || !oldVerifiedFlag)) {
                //When a user is enabled and verified, send a new user email
                try {
                    AdminUtils.sendAdminEmail("User " + user.getUsername() + " updated", "The user account " + user.getUsername() + " was updated by the user " + getSessionUser().getUsername() + ".");
                } catch (Exception e) {
                    log.error("An error occurred trying to send email to the admin: user '{}' updated by {}", user.getUsername(), getSessionUser().getUsername(), e);
                }
            } else if (pendingNewEmail != null) {
                // if we set pendingNewEmail, the user has updated email address and needs to verify before we execute the change
                if (!AdminUtils.issueEmailChangeRequest(user, pendingNewEmail)) {
                    throw new XapiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Unable to send email for change request, please contact site admin");
                }
            }
            return _factory.getUser(user, adminUpdate);
        } catch (Exception e) {
            log.error("Error occurred modifying user '{}'", user.getUsername(), e);
            if (e instanceof PasswordComplexityException) {
                throw new XapiException(HttpStatus.BAD_REQUEST, e.getMessage());
            } else {
                throw new UserInitException("Error occurred modifying user " + user.getUsername(), e);
            }
        }
    }

    @ApiOperation(value = "Invalidates all active sessions associated with the specified username.",
                  notes = "Returns a list of session IDs that were invalidated.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully invalidated."),
                   @ApiResponse(code = 304, message = "Indicated user has no active sessions, so no action was taken."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to invalidate this user's sessions."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "active/{username}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.User)
    public List<String> invalidateUser(final HttpSession current, @ApiParam(value = "The username of the user to invalidate.", required = true) @PathVariable @Username final String username) throws InitializationException, UserNotFoundException, UserInitException, NotModifiedException {
        final UserI  user;
        final String currentSessionId;
        if (StringUtils.equals(getSessionUser().getUsername(), username)) {
            user = getSessionUser();
            currentSessionId = current.getId();
        } else {
            user = getUserManagementService().getUser(username);
            currentSessionId = null;
        }
        final Object located = locatePrincipalByUsername(user.getUsername());
        if (located == null) {
            throw new UserNotFoundException(username);
        }
        final List<SessionInformation> sessions = _sessionRegistry.getAllSessions(located, false);
        if (sessions.isEmpty()) {
            throw new NotModifiedException("No sessions were found for the user " + username);
        }

        return sessions.stream().map(INFO_TO_ID_INVALIDATOR_FUNCTION).filter(sessionId -> !StringUtils.equalsIgnoreCase(sessionId, currentSessionId)).collect(Collectors.toList());
    }

    @ApiOperation(value = "Returns whether the user with the specified user ID is enabled.",
                  notes = "Returns true or false based on whether the specified user is enabled or not.",
                  response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to get whether this user is enabled."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public boolean usersIdEnabledGet(@ApiParam(value = "The ID of the user to retrieve the enabled status for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username).isEnabled();
    }

    @ApiOperation(value = "Sets the user's enabled state.",
                  notes = "Sets the enabled state of the user with the specified user ID to the value of the flag parameter.")
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/enabled/{flag}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void usersIdEnabledFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable @Username final String username, @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable Boolean flag) throws UserNotFoundException, UserInitException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        if (user.isEnabled() == flag) {
            return;
        }
        user.setEnabled(flag);
        try {
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
        } catch (Exception e) {
            throw new InitializationException("An error occurred " + (flag ? "enabling" : "disabling") + " user " + user.getUsername(), e);
        }
        if (flag && user.isVerified()) {
            //When a user is enabled, send a new user email if they're also verified
            try {
                AdminUtils.sendNewUserEmailMessage(username, user.getEmail());
            } catch (Exception e) {
                log.error("An error occurred trying to send email to the admin: user '{}' enabled with email '{}'", user.getUsername(), user.getEmail(), e);
            }
        }
    }

    @ApiOperation(value = "Returns whether the user with the specified user ID is verified.",
                  notes = "Returns true or false based on whether the specified user is verified or not.",
                  response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User verified status successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/verified", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public boolean usersIdVerifiedGet(@ApiParam(value = "The ID of the user to retrieve the verified status for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username).isVerified();
    }

    @ApiOperation(value = "Sets the user's verified state.",
                  notes = "Sets the verified state of the user with the specified user ID to the value of the flag parameter.")
    @ApiResponses({@ApiResponse(code = 200, message = "User verified status successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to verify or un-verify this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/verified/{flag}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void usersIdVerifiedFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable @Username final String username, @ApiParam(value = "The value to set for the verified status.", required = true) @PathVariable Boolean flag) throws UserNotFoundException, UserInitException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        if (user.isVerified() == flag) {
            return;
        }
        user.setVerified(flag);
        try {
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
        } catch (Exception e) {
            throw new InitializationException("An error occurred " + (flag ? "verifying" : "unverifying") + " user " + user.getUsername(), e);
        }
        if (flag && user.isVerified()) {
            //When a user is enabled, send a new user email if they're also verified
            try {
                AdminUtils.sendNewUserEmailMessage(username, user.getEmail());
            } catch (Exception e) {
                log.error("An error occurred trying to send email to the admin: user '{}' verified with email '{}'", user.getUsername(), user.getEmail(), e);
            }
        }
    }

    @ApiOperation(value = "Returns all of the roles on the system, with a list of users assigned to each role.",
                  notes = "Users may appear in more than one role.",
                  responseContainer = "Map",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User roles successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "rolemap", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Map<String, Collection<String>> getRolesAndUsers() {
        return getRoleHolder().getRolesAndUsers();
    }

    @ApiOperation(value = "Returns all of the roles on the system, with a list of users assigned to each role.",
                  notes = "Users may appear in more than one role.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User roles successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "roles", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Collection<String> getRoles() {
        return getRoleHolder().getRoles();
    }

    @ApiOperation(value = "Returns the roles for the user with the specified user ID.",
                  notes = "Returns a collection of the user's roles.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User roles successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "roles/{role}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Collection<String> getUsersWithRole(@ApiParam(value = "The ID of the user to retrieve the roles for.", required = true) @PathVariable final String role) {
        return getRoleHolder().getUsers(role);
    }

    @ApiOperation(value = "Returns the roles for the user with the specified user ID.",
                  notes = "Returns a collection of the user's roles.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User roles successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public Collection<String> usersIdRolesGet(@ApiParam(value = "The ID of the user to retrieve the roles for.", required = true) @PathVariable @Username final String username) {
        return getUserRoles(username);
    }

    @ApiOperation(value = "Adds one or more roles to a user.",
                  notes = "Assigns one or more new roles to a user.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "All specified user roles successfully added."),
                   @ApiResponse(code = 202, message = "Some user roles successfully added, but some may have failed. Check the return value for roles that the service was unable to add."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to add roles to this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public ResponseEntity<Collection<String>> usersIdAddRoles(@ApiParam(value = "ID of the user to add a role to", required = true) @PathVariable @Username final String username,
                                                              @ApiParam(value = "The user's new roles.", required = true) @RequestBody final List<String> roles) throws NotFoundException, InitializationException, UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String role : roles) {
            try {
                getRoleHolder().addRole(getSessionUser(), user, role);
            } catch (Exception e) {
                failed.add(role);
                log.error("Error occurred adding role " + role + " to user " + user.getLogin() + ".", e);
            }
        }

        return failed.isEmpty() ? ResponseEntity.ok(failed) : ResponseEntity.accepted().body(failed);
    }

    @ApiOperation(value = "Removes one or more roles from a user.",
                  notes = "Removes one or more new roles from a user.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "All specified user roles successfully removed."),
                   @ApiResponse(code = 202, message = "Some user roles successfully removed, but some may have failed. Check the return value for roles that the service was unable to remove."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to remove roles from this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Admin)
    public ResponseEntity<Collection<String>> usersIdRemoveRoles(@ApiParam(value = "ID of the user to remove role from", required = true) @PathVariable @Username final String username,
                                                                 @ApiParam(value = "The roles to be removed.", required = true) @RequestBody final List<String> roles) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String role : roles) {
            try {
                getRoleHolder().deleteRole(getSessionUser(), user, role);
            } catch (Exception e) {
                failed.add(role);
                log.error("Error occurred adding role " + role + " to user " + user.getLogin() + ".", e);
            }
        }

        return failed.isEmpty() ? ResponseEntity.ok(failed) : ResponseEntity.accepted().body(failed);
    }

    @ApiOperation(value = "Adds a role to a user.",
                  notes = "Assigns a new role to a user.")
    @ApiResponses({@ApiResponse(code = 200, message = "User role successfully added."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to add a role to this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles/{role}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void usersIdAddRole(@ApiParam(value = "ID of the user to add a role to", required = true) @PathVariable @Username final String username,
                               @ApiParam(value = "The user's new role.", required = true) @PathVariable final String role) throws UserNotFoundException, UserInitException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        try {
            getRoleHolder().addRole(getSessionUser(), user, role);
        } catch (Exception e) {
            throw new InitializationException("Error occurred adding role " + role + " to user " + user.getLogin() + ".", e);
        }
    }

    @ApiOperation(value = "Remove a user's role.",
                  notes = "Removes a user's role.")
    @ApiResponses({@ApiResponse(code = 200, message = "User role successfully removed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to remove a role from this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles/{role}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Admin)
    public void usersIdRemoveRole(@ApiParam(value = "ID of the user to delete a role from", required = true) @PathVariable @Username final String username,
                                  @ApiParam(value = "The user role to delete.", required = true) @PathVariable String role) throws UserNotFoundException, UserInitException, InitializationException, ConflictedStateException {
        final UserI user = getUserManagementService().getUser(username);
        try {
            getRoleHolder().deleteRole(getSessionUser(), user, role);
        } catch (IllegalArgumentException e) {
            if (StringUtils.equals(UserRole.ROLE_ADMINISTRATOR, role)) {
                throw new ConflictedStateException(e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            throw new InitializationException("Error occurred removing role " + role + " from user " + user.getLogin() + ".", e);
        }
    }

    @ApiOperation(value = "Returns the groups for the user with the specified user ID.",
                  notes = "Returns a collection of the user's groups.",
                  responseContainer = "Set",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User groups successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to get the groups for this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public Set<String> usersIdGroupsGet(@ApiParam(value = "The ID of the user to retrieve the groups for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return Groups.getGroupsForUser(getUserManagementService().getUser(username)).keySet();
    }

    @ApiOperation(value = "Adds the user to one or more groups.",
                  notes = "Assigns the user to one or more new groups.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully added for all specified groups."),
                   @ApiResponse(code = 202, message = "User was successfully added to some of the specified groups, but some may have failed. Check the return value for groups that the service was unable to add."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to add this user to groups."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserGroupXapiAuthorization.class)
    public ResponseEntity<Collection<String>> usersIdAddGroups(@ApiParam(value = "ID of the user to add to the specified groups", required = true) @PathVariable @Username final String username,
                                                               @ApiParam(value = "The groups to which the user should be added.", required = true) @UserGroup @RequestBody final List<String> groups) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String group : groups) {
            try {
                Groups.addUserToGroup(group, user, getSessionUser(), EventUtils.ADMIN_EVENT(getSessionUser()));
            } catch (Exception e) {
                failed.add(group);
                log.error("Error occurred adding user " + user.getLogin() + " to group " + group + ".", e);
            }
        }
        return failed.isEmpty() ? ResponseEntity.ok(Collections.emptyList()) : ResponseEntity.accepted().body(failed);
    }

    @ApiOperation(value = "Removes the user from one or more groups.",
                  notes = "Removes the user from one or more groups.",
                  responseContainer = "List",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully removed from all specified groups."),
                   @ApiResponse(code = 202, message = "User was successfully removed from some of the specified groups, but some may have failed. Check the return value for groups that the service was unable to remove."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to remove this user from groups."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.User)
    public ResponseEntity<Collection<String>> usersIdRemoveGroups(@ApiParam(value = "ID of the user to remove role from", required = true) @PathVariable @Username final String username,
                                                                  @ApiParam(value = "The groups from which the user should be removed.", required = true) @RequestBody final List<String> groups) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String group : groups) {
            try {
                Groups.removeUserFromGroup(user, getSessionUser(), group, EventUtils.ADMIN_EVENT(getSessionUser()));
            } catch (Exception e) {
                failed.add(group);
                log.error("Error occurred removing group " + group + " from user " + user.getLogin() + ".", e);
            }
        }
        return failed.isEmpty() ? ResponseEntity.ok(Collections.emptyList()) : ResponseEntity.accepted().body(failed);
    }

    @ApiOperation(value = "Adds a user to a group.",
                  notes = "Assigns user to a group.")
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully added to group."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to assign this user to groups."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups/{group}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserGroupXapiAuthorization.class)
    public void usersIdAddGroup(@ApiParam(value = "ID of the user to add to a group", required = true) @PathVariable @Username final String username, @ApiParam(value = "The user's new group.", required = true) @UserGroup @PathVariable final String group) throws UserNotFoundException, UserInitException, DataFormatException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        if (user.getID().equals(Users.getGuest().getID())) {
            throw new DataFormatException("You can't add the guest user to groups");
        }
        try {
            Groups.addUserToGroup(group, user, getSessionUser(), null);
        } catch (Exception e) {
            throw new InitializationException("Error occurred adding user " + user.getUsername() + " to group " + group, e);
        }
    }

    @ApiOperation(value = "Removes a user from a group.",
                  notes = "Removes a user from a group.")
    @ApiResponses({@ApiResponse(code = 200, message = "User's group successfully removed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to remove this user from groups."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups/{group}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.User)
    public void usersIdRemoveGroup(@ApiParam(value = "ID of the user to remove from group", required = true) @PathVariable @Username final String username, @ApiParam(value = "The group to remove the user from.", required = true) @PathVariable final String group) throws UserNotFoundException, UserInitException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        try {
            Groups.removeUserFromGroup(user, getSessionUser(), group, null);
        } catch (Exception e) {
            throw new InitializationException("Error occurred removing user " + user.getLogin() + " from group " + group, e);
        }
    }

    @ApiOperation(value = "Returns list of projects that user has edit access.",
                  notes = "Returns list of projects that user has edit access.",
                  responseContainer = "List",
                  response = String.class)
    @XapiRequestMapping(value = "projects", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<String> getProjectsByUser() {
        return _permissionsService.getUserEditableProjects(getSessionUser());
    }

    @ApiOperation(value = "Returns username for signed-in user",
                  response = String.class)
    @XapiRequestMapping(value = "username", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public String getUsername() {
        return getSessionUser().getUsername();
    }

    @ApiOperation(value = "Cancels a change request.")
    @ApiResponses({@ApiResponse(code = 200, message = "Change request canceled."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "changeRequest/{type}", produces = APPLICATION_JSON_VALUE, method = DELETE)
    public void cancelChangeRequest(@ApiParam(value = "Type of change request", required = true) @PathVariable("type") final String type) {
        _userChangeRequestService.cancelRequest(getSessionUser().getUsername(), type);
    }

    @SuppressWarnings("unused")
    public static class Event {
        public static String Added                 = "Added User";
        public static String Disabled              = "Disabled User";
        public static String Enabled               = "Enabled User";
        public static String DisabledForInactivity = "Disabled User Due To Inactivity";
        public static String Modified              = "Modified User";
        public static String ModifiedEmail         = "Modified User Email";
        public static String ModifiedPassword      = "Modified User Password";
        public static String ModifiedPermissions   = "Modified User Permissions";
        public static String ModifiedSettings      = "Modified User Settings";
        public static String VerifiedEmail         = "Verified User Email";
    }

    @Nullable
    private Object locatePrincipalByUsername(final String username) {
        return _sessionRegistry.getAllPrincipals().stream().filter((principal) -> principal instanceof String && username.equals(principal) ||
                                                                                  principal instanceof UserDetails && username.equals(((UserDetails) principal).getUsername()) ||
                                                                                  username.equals(principal.toString())).findFirst().orElse(null);
    }

    private void validateUser(final User model) throws DataFormatException, ResourceAlreadyExistsException, UserInitException {
        final DataFormatException exception = new DataFormatException();
        exception.validateBlankAndRegex("username", model.getUsername(), Users.PATTERN_USERNAME);
        exception.validateBlankAndRegex("email", model.getEmail(), Users.PATTERN_EMAIL);
        exception.validateBlankAndRegex("firstName", model.getFirstName(), Patterns.LIMIT_XSS_CHARS);
        exception.validateBlankAndRegex("lastName", model.getLastName(), Patterns.LIMIT_XSS_CHARS);
        if (exception.hasDataFormatErrors()) {
            throw exception;
        }

        final String username = model.getUsername();
        try {
            getUserManagementService().getUser(username);
            throw new ResourceAlreadyExistsException("user", username);
        } catch (UserNotFoundException ignored) {
            // This is actually what we want.
        }
    }

    private int getLastModifiedInterval() {
        return Integer.max(_siteConfig.getSecurityLastModifiedInterval(), 1);
    }

    private int getMaxLoginInterval() {
        return Integer.max(_siteConfig.getSecurityMaxLoginInterval(), 1);
    }

    private static class SessionInfoToIdFunction implements Function<SessionInformation, String> {
        SessionInfoToIdFunction(final boolean invalidate) {
            _invalidate = invalidate;
        }

        @Override
        public String apply(final SessionInformation sessionInformation) {
            if (_invalidate) {
                sessionInformation.expireNow();
            }
            return sessionInformation.getSessionId();
        }

        private final boolean _invalidate;
    }

    private static final SessionInfoToIdFunction INFO_TO_ID_FUNCTION             = new SessionInfoToIdFunction(false);
    private static final SessionInfoToIdFunction INFO_TO_ID_INVALIDATOR_FUNCTION = new SessionInfoToIdFunction(true);

    private final SessionRegistry            _sessionRegistry;
    private final AliasTokenService          _aliasTokenService;
    private final PermissionsServiceI        _permissionsService;
    private final UserFactory                _factory;
    private final NamedParameterJdbcTemplate _jdbcTemplate;
    private final SiteConfigPreferences      _siteConfig;
    private final UserChangeRequestService   _userChangeRequestService;
}
