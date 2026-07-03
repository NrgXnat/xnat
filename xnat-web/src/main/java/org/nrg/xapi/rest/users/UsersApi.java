/*
 * web: org.nrg.xapi.rest.users.UsersApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xapi.rest.UserGroup;
import org.nrg.xapi.rest.Username;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatAuthenticationProviderApiPojo;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserChangeRequestService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.XnatProviderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.ProviderNotFoundException;
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

import static org.nrg.xdat.security.services.UserManagementServiceI.PARAM_USERNAME;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Tag(name = "User Management API")
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
                    final UserChangeRequestService userChangeRequestService,
                    final XnatProviderManager manager,
                    final XdatUserAuthService service) {
        super(userManagementService, roleHolder);
        _sessionRegistry          = sessionRegistry;
        _aliasTokenService        = aliasTokenService;
        _permissionsService       = permissionsService;
        _factory                  = factory;
        _jdbcTemplate             = jdbcTemplate;
        _siteConfig               = siteConfig;
        _userChangeRequestService = userChangeRequestService;
        _manager                  = manager;
        _service                  = service;
    }

    @Operation(summary = "Get list of users.",
                  description = "The primary users function returns a list of all users of the XNAT system. This includes just the username and nothing else. You can retrieve a particular user by adding the username to the REST API URL or a list of users with abbreviated user profiles by calling /xapi/users/profiles.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of usernames."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<String> usersGet() {
        return new ArrayList<>(Users.getAllLogins());
    }

    @Operation(summary = "Get list of user profiles.",
                  description = "The users' profiles function returns a list of all users of the XNAT system with brief information about each.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of user profiles."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of users."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "profiles", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<User> usersProfilesGet() {
        return User.getAllUsers(_jdbcTemplate);
    }

    @Operation(summary = "Get user profile.",
                  description = "The user profile function returns a user of the XNAT system with brief information.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A user profile."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the user profile."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "profile/{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public User usersProfileGet(@Parameter(description = "ID of the user to fetch", required = true) @PathVariable @Username final String username) throws DataFormatException, NotFoundException {
        return getUserProfile(username);
    }

    @Operation(summary = "Get user profile.",
                  description = "The user profile function returns the current user with brief information.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A user profile."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the user profile."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "profile", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    @ResponseBody
    public User usersProfileGet() throws DataFormatException, NotFoundException {
        return getUserProfile(getSessionUser().getUsername());
    }

    @Operation(summary = "Get user auth details.",
                  description = "The user authDetails function returns info about authentication methods that can be used for a given XNAT account.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User auth info."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the user profile."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "authDetails/{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    @ResponseBody
    public List<UserAuth> usersAuthDetailsGet(@Parameter(description = "ID of the user to fetch", required = true) @PathVariable @Username final String username) throws InsufficientPrivilegesException, DataFormatException {
        final UserI user = getSessionUser();
        if (!Roles.isSiteAdmin(user) && !StringUtils.equalsIgnoreCase(username, user.getUsername())) {
            throw new InsufficientPrivilegesException(user.getUsername(), username, "The user " + user.getUsername() + " attempted to get authentication details  for user " + username + ". This requires administrator privileges.");
        }
        if (!Users.isValidUsername(username)) {
            throw new DataFormatException("Invalid username");
        }
        return UserAuth.getUserAuths(_jdbcTemplate, username);
    }

    @Operation(summary = "Get configured auth providers.",
                  description = "Returns info about authentication methods that have been configured")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Auth provider info."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "authProviders", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<XnatAuthenticationProviderApiPojo> getConfiguredAuthProviders() {
        return _manager.getVisibleEnabledProviders().entrySet().stream()
                       .map(entry -> new XnatAuthenticationProviderApiPojo(entry.getKey(), entry.getValue().getName(), entry.getValue().getAuthMethod()))
                       .collect(Collectors.toList());
    }

    @Operation(summary = "Get list of users who are enabled or who have interacted with the site somewhat recently.",
                  description = "The users' profiles function returns a list of all users of the XNAT system with brief information about each.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of user profiles."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "current", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserResourceXapiAuthorization.class)
    @ResponseBody
    public List<User> currentUsersProfilesGet() {
        return User.getCurrentUsers(_jdbcTemplate, getMaxLoginInterval(), getLastModifiedInterval());
    }

    @Operation(summary = "Get list of active users.",
                  description = "Returns a map of usernames for users that have at least one currently active session, i.e. logged in or associated with a valid application session. The number of active sessions and a list of the session IDs is associated with each user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of active users."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "active", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    @ResponseBody
    public Map<String, Map<String, Object>> getActiveUsers() {
        final Map<String, Map<String, Object>> activeUsers = new HashMap<>();
        for (final Object principal : _sessionRegistry.getAllPrincipals()) {
            final String username;
            if (principal instanceof String string) {
                username = string;
            } else if (principal instanceof UserDetails details) {
                username = details.getUsername();
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

    @Operation(summary = "Get information about active sessions for the indicated user.",
                  description = "Returns a map containing a list of session IDs and usernames for users that have at least one currently active session, i.e. logged in or associated with a valid application session. This also includes the number of active sessions for each user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of active users."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access this user's sessions."),
                   @ApiResponse(responseCode = "404", description = "The indicated user has no active sessions or is not a valid user."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "active/{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    @ResponseBody
    public List<String> getUserActiveSessions(@Parameter(description = "ID of the user to fetch", required = true) @PathVariable @Username final String username) throws NotModifiedException {
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

    @Operation(summary = "Gets the user with the specified user ID.",
                  description = "Returns the serialized user object with the specified user ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public User getUser(@Parameter(description = "Username of the user to fetch.", required = true) @PathVariable("username") @Username final String username) throws InitializationException {
        try {
            final UserI user = getUserManagementService().getUser(username);
            return _factory.getUser(user);
        } catch (UserInitException | UserNotFoundException e) {
            throw new InitializationException("An error occurred initializing the user " + username, e);
        }
    }

    @Operation(summary = "Creates a new user from the request body.",
                  description = "Returns the newly created user object.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "User successfully created."),
                   @ApiResponse(responseCode = "400", description = "The submitted data was invalid."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to update this user."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
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
            XdatUserAuth newUserAuth = null;
            if (user.getAuthorization() != null) {
                try {
                    newUserAuth = (XdatUserAuth)user.getAuthorization();
                    if (StringUtils.isBlank(newUserAuth.getXdatUsername())) {
                        newUserAuth.setXdatUsername(user.getUsername()); //consistent with setupAuthorization method
                    }
                } catch(ClassCastException ignored) {}
            }
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Added, "Requested by user " + getSessionUser().getUsername(), "Created new user " + user.getUsername() + " through XAPI user management API."), newUserAuth);

            if (BooleanUtils.isTrue(model.getVerified()) && BooleanUtils.isTrue(model.getEnabled())) {
                setupAuthorization(user);
                //When a user is enabled and verified, send a new user email
                try {
                    AdminUtils.sendNewUserEmailMessage(user.getUsername(), user.getEmail());
                } catch (Exception e) {
                    log.error("An error occurred trying to send email to the admin: new user '{}' created with email '{}'", user.getUsername(), user.getEmail(), e);
                }
            }
            return _factory.getUser(user);
        } catch (Exception e) {
            throw new UserInitException("Error occurred creating user " + user.getLogin() + " Cause: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "Updates the user object with the specified username.",
                  description = "Returns the updated serialized user object with the specified username.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully updated."),
                   @ApiResponse(responseCode = "304", description = "The user object was not modified because no attributes were changed."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to update this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}", consumes = {APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE}, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public User updateUser(@Parameter(description = "The username of the user to create or update.", required = true) @PathVariable @Username final String username, @RequestBody final User model) throws UserInitException, XapiException, UserNotFoundException {
        return updateUser(username, model, true);
    }

    @Operation(summary = "Update ones own user account.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully updated."),
                   @ApiResponse(responseCode = "304", description = "The user object was not modified because no attributes were changed."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to update this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authenticated)
    public User update(@RequestBody final User model) throws UserInitException, XapiException, UserNotFoundException {
        UserI user = getSessionUser();
        return updateUser(user.getUsername(), model, Roles.isSiteAdmin(user));
    }


    private User updateUser(String username, User model, boolean adminUpdate) throws XapiException, UserNotFoundException, UserInitException {
        final UserI user            = getUserManagementService().getUser(username);
        boolean     oldEnabledFlag  = user.isEnabled();
        boolean     oldVerifiedFlag = user.isVerified();

        if ((StringUtils.isNotBlank(model.getUsername())) && (!StringUtils.equals(user.getUsername(), model.getUsername()))) {
            throw new DataFormatException("The username for the submitted user object must match the username for the API call");
        }

        AtomicBoolean isDirty         = new AtomicBoolean(false);
        String        pendingNewEmail = null;
        if ((StringUtils.isNotBlank(model.getEmail())) && (!StringUtils.equals(user.getEmail(), model.getEmail()))) {

            if (!Users.isValidEmail(model.getEmail())) {
                throw new DataFormatException("Invalid email format");
            }

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
            if (!adminUpdate && (StringUtils.isBlank(model.getCurrentPassword()) || !Users.passwordMatches(user.getPassword(), model.getCurrentPassword()))) {
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
            if (model.getAuthorization() != null) {
                if (!hasValidAuthorizationInformation(model)) {
                    throw new XapiException(HttpStatus.BAD_REQUEST, "Invalid authorization information");
                }
                if (!model.getAuthorization().equals(user.getAuthorization())) {
                    user.setAuthorization(model.getAuthorization());
                    isDirty.set(true);
                }
            }
            final Boolean enabled  = model.getEnabled();
            final Boolean verified = model.getVerified();
            if (enabled != null && enabled != user.isEnabled()) {
                user.setEnabled(enabled);
                if (user.isEnabled() && (user.isVerified() || Boolean.TRUE.equals(verified))) {
                    try {
                        AdminUtils.sendNewUserEmailMessage(username, user.getEmail());
                    } catch (Exception e) {
                        log.error("An error occurred trying to send email to the admin: user '{}' enabled with email '{}'", user.getUsername(), user.getEmail(), e);
                    }
                }
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
                setupAuthorization(user);
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
            return _factory.getUser(user);
        } catch (Exception e) {
            log.error("Error occurred modifying user '{}'", user.getUsername(), e);
            if (e instanceof PasswordComplexityException) {
                throw new XapiException(HttpStatus.BAD_REQUEST, e.getMessage());
            } else {
                throw new UserInitException("Error occurred modifying user " + user.getUsername(), e);
            }
        }
    }

    @Operation(summary = "Invalidates all active sessions associated with the specified username.",
                  description = "Returns a list of session IDs that were invalidated.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully invalidated."),
                   @ApiResponse(responseCode = "304", description = "Indicated user has no active sessions, so no action was taken."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to invalidate this user's sessions."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "active/{username}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.User)
    public List<String> invalidateUser(final HttpSession current, @Parameter(description = "The username of the user to invalidate.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException, NotModifiedException {
        final UserI  user;
        final String currentSessionId;
        if (StringUtils.equals(getSessionUser().getUsername(), username)) {
            user             = getSessionUser();
            currentSessionId = current.getId();
        } else {
            user             = getUserManagementService().getUser(username);
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

    @Operation(summary = "Returns whether the user with the specified user ID is enabled.",
                  description = "Returns true or false based on whether the specified user is enabled or not.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User enabled status successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get whether this user is enabled."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public boolean usersIdEnabledGet(@Parameter(description = "The ID of the user to retrieve the enabled status for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username).isEnabled();
    }

    @Operation(summary = "Sets the user's enabled state.",
                  description = "Sets the enabled state of the user with the specified user ID to the value of the flag parameter.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User enabled status successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to enable or disable this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/enabled/{flag}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void usersIdEnabledFlagPut(@Parameter(description = "ID of the user to fetch", required = true) @PathVariable @Username final String username, @Parameter(description = "The value to set for the enabled status.", required = true) @PathVariable Boolean flag) throws UserNotFoundException, UserInitException, InitializationException {
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

    @Operation(summary = "Returns whether the user with the specified user ID is verified.",
                  description = "Returns true or false based on whether the specified user is verified or not.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User verified status successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/verified", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public boolean usersIdVerifiedGet(@Parameter(description = "The ID of the user to retrieve the verified status for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username).isVerified();
    }

    @Operation(summary = "Sets the user's verified state.",
                  description = "Sets the verified state of the user with the specified user ID to the value of the flag parameter.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User verified status successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to verify or un-verify this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/verified/{flag}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void usersIdVerifiedFlagPut(@Parameter(description = "ID of the user to fetch", required = true) @PathVariable @Username final String username, @Parameter(description = "The value to set for the verified status.", required = true) @PathVariable Boolean flag) throws UserNotFoundException, UserInitException, InitializationException {
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

    @Operation(summary = "Returns all of the roles on the system, with a list of users assigned to each role.",
                  description = "Users may appear in more than one role.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User roles successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "rolemap", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Map<String, Collection<String>> getRolesAndUsers() {
        return getRoleHolder().getRolesAndUsers();
    }

    @Operation(summary = "Returns all of the roles on the system, with a list of users assigned to each role.",
                  description = "Users may appear in more than one role.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User roles successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "roles", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Collection<String> getRoles() {
        return getRoleHolder().getRoles();
    }

    @Operation(summary = "Returns the roles for the user with the specified user ID.",
                  description = "Returns a collection of the user's roles.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User roles successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "roles/{role}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Collection<String> getUsersWithRole(@Parameter(description = "The ID of the user to retrieve the roles for.", required = true) @PathVariable final String role) {
        return getRoleHolder().getUsers(role);
    }

    @Operation(summary = "Returns the roles for the user with the specified user ID.",
                  description = "Returns a collection of the user's roles.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User roles successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public Collection<String> usersIdRolesGet(@Parameter(description = "The ID of the user to retrieve the roles for.", required = true) @PathVariable @Username final String username) {
        return getUserRoles(username);
    }

    @Operation(summary = "Adds one or more roles to a user.",
                  description = "Assigns one or more new roles to a user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "All specified user roles successfully added."),
                   @ApiResponse(responseCode = "202", description = "Some user roles successfully added, but some may have failed. Check the return value for roles that the service was unable to add."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to add roles to this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public ResponseEntity<Collection<String>> usersIdAddRoles(@Parameter(description = "ID of the user to add a role to", required = true) @PathVariable @Username final String username,
                                                              @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The user's new roles.", required = true) @RequestBody final List<String> roles) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String role : roles) {
            try {
                getRoleHolder().addRole(getSessionUser(), user, role);
            } catch (Exception e) {
                failed.add(role);
                log.error("Error occurred adding role {} to user {}.", role, user.getLogin(), e);
            }
        }

        return failed.isEmpty() ? ResponseEntity.ok(failed) : ResponseEntity.accepted().body(failed);
    }

    @Operation(summary = "Removes one or more roles from a user.",
                  description = "Removes one or more new roles from a user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "All specified user roles successfully removed."),
                   @ApiResponse(responseCode = "202", description = "Some user roles successfully removed, but some may have failed. Check the return value for roles that the service was unable to remove."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to remove roles from this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Admin)
    public ResponseEntity<Collection<String>> usersIdRemoveRoles(@Parameter(description = "ID of the user to remove role from", required = true) @PathVariable @Username final String username,
                                                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The roles to be removed.", required = true) @RequestBody final List<String> roles) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String role : roles) {
            try {
                getRoleHolder().deleteRole(getSessionUser(), user, role);
            } catch (Exception e) {
                failed.add(role);
                log.error("Error occurred adding role {} to user {}.", role, user.getLogin(), e);
            }
        }

        return failed.isEmpty() ? ResponseEntity.ok(failed) : ResponseEntity.accepted().body(failed);
    }

    @Operation(summary = "Adds a role to a user.",
                  description = "Assigns a new role to a user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User role successfully added."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to add a role to this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles/{role}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void usersIdAddRole(@Parameter(description = "ID of the user to add a role to", required = true) @PathVariable @Username final String username,
                               @Parameter(description = "The user's new role.", required = true) @PathVariable final String role) throws UserNotFoundException, UserInitException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        try {
            getRoleHolder().addRole(getSessionUser(), user, role);
        } catch (Exception e) {
            throw new InitializationException("Error occurred adding role " + role + " to user " + user.getLogin() + ".", e);
        }
    }

    @Operation(summary = "Remove a user's role.",
                  description = "Removes a user's role.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User role successfully removed."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to remove a role from this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/roles/{role}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Admin)
    public void usersIdRemoveRole(@Parameter(description = "ID of the user to delete a role from", required = true) @PathVariable @Username final String username,
                                  @Parameter(description = "The user role to delete.", required = true) @PathVariable String role) throws UserNotFoundException, UserInitException, InitializationException, ConflictedStateException {
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

    @Operation(summary = "Returns the projects to which the user belongs along with the user's role in the project.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User projects successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get the projects for this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/projects", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public Map<String, String> getUserProjects(@Parameter(description = "The ID of the user to retrieve the projects for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException {
        if (!Users.exists(username)) {
            throw new UserNotFoundException(username);
        }
        return _jdbcTemplate.queryForStream(QUERY_USER_PROJECT_ROLES, new MapSqlParameterSource(PARAM_USERNAME, username), (results, rowNum) -> {
                                final String project = results.getString("project");
                                final String role    = results.getString("role");
                                return new AbstractMap.SimpleEntry<>(project, role);
                            })
                            .filter(entry -> entry.getKey() != null)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Operation(summary = "Returns the groups for the user with the specified user ID.",
                  description = "Returns a collection of the user's groups.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User groups successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to get the groups for this user."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.User)
    public Set<String> usersIdGroupsGet(@Parameter(description = "The ID of the user to retrieve the groups for.", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return Groups.getGroupsForUser(getUserManagementService().getUser(username)).keySet();
    }

    @Operation(summary = "Adds the user to one or more groups.",
                  description = "Assigns the user to one or more new groups.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully added for all specified groups."),
                   @ApiResponse(responseCode = "202", description = "User was successfully added to some of the specified groups, but some may have failed. Check the return value for groups that the service was unable to add."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to add this user to groups."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserGroupXapiAuthorization.class)
    public ResponseEntity<Collection<String>> usersIdAddGroups(@Parameter(description = "ID of the user to add to the specified groups", required = true) @PathVariable @Username final String username,
                                                               @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The groups to which the user should be added.", required = true) @UserGroup @RequestBody final List<String> groups) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String group : groups) {
            try {
                Groups.addUserToGroup(group, user, getSessionUser(), null);
            } catch (Exception e) {
                failed.add(group);
                log.error("Error occurred adding user {} to group {}.", user.getLogin(), group, e);
            }
        }
        return failed.isEmpty() ? ResponseEntity.ok(Collections.emptyList()) : ResponseEntity.accepted().body(failed);
    }

    @Operation(summary = "Removes the user from one or more groups.",
                  description = "Removes the user from one or more groups.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully removed from all specified groups."),
                   @ApiResponse(responseCode = "202", description = "User was successfully removed from some of the specified groups, but some may have failed. Check the return value for groups that the service was unable to remove."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to remove this user from groups."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.User)
    public ResponseEntity<Collection<String>> usersIdRemoveGroups(@Parameter(description = "ID of the user to remove role from", required = true) @PathVariable @Username final String username,
                                                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The groups from which the user should be removed.", required = true) @RequestBody final List<String> groups) throws UserNotFoundException, UserInitException {
        final UserI user = getUserManagementService().getUser(username);

        final Collection<String> failed = new ArrayList<>();
        for (final String group : groups) {
            try {
                Groups.removeUserFromGroup(user, getSessionUser(), group, null);
            } catch (Exception e) {
                failed.add(group);
                log.error("Error occurred removing group {} from user {}.", group, user.getLogin(), e);
            }
        }
        return failed.isEmpty() ? ResponseEntity.ok(Collections.emptyList()) : ResponseEntity.accepted().body(failed);
    }

    @Operation(summary = "Adds a user to a group.",
                  description = "Assigns user to a group.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User successfully added to group."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to assign this user to groups."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups/{group}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authorizer)
    @AuthDelegate(UserGroupXapiAuthorization.class)
    public void usersIdAddGroup(@Parameter(description = "ID of the user to add to a group", required = true) @PathVariable @Username final String username, @Parameter(description = "The user's new group.", required = true) @UserGroup @PathVariable final String group) throws UserNotFoundException, UserInitException, DataFormatException, InitializationException {
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

    @Operation(summary = "Removes a user from a group.",
                  description = "Removes a user from a group.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User's group successfully removed."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to remove this user from groups."),
                   @ApiResponse(responseCode = "404", description = "User not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/groups/{group}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.User)
    public void usersIdRemoveGroup(@Parameter(description = "ID of the user to remove from group", required = true) @PathVariable @Username final String username, @Parameter(description = "The group to remove the user from.", required = true) @PathVariable final String group) throws UserNotFoundException, UserInitException, InitializationException {
        final UserI user = getUserManagementService().getUser(username);
        try {
            Groups.removeUserFromGroup(user, getSessionUser(), group, null);
        } catch (Exception e) {
            throw new InitializationException("Error occurred removing user " + user.getLogin() + " from group " + group, e);
        }
    }

    @Operation(summary = "Returns list of projects that user has edit access.",
                  description = "Returns list of projects that user has edit access.")
    @XapiRequestMapping(value = "projects", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<String> getProjectsByUser() {
        return _permissionsService.getUserEditableProjects(getSessionUser());
    }

    @Operation(summary = "Returns username for signed-in user")
    @XapiRequestMapping(value = "username", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public String getUsername() {
        return getSessionUser().getUsername();
    }

    @Operation(summary = "Cancels a change request.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Change request canceled."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "changeRequest/{type}", produces = APPLICATION_JSON_VALUE, method = DELETE)
    public void cancelChangeRequest(@Parameter(description = "Type of change request", required = true) @PathVariable("type") final String type) {
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
                                                                                  principal instanceof UserDetails ud && username.equals(ud.getUsername()) ||
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
        if (model.getAuthorization() != null && !hasValidAuthorizationInformation(model)) {
            throw new DataFormatException("Invalid authorization information");
        } else if (model.getAuthorization() == null && siteHasSingletonNonLocalAuth()) {
            throw new DataFormatException("Missing authorization information");
        }
    }

    private int getLastModifiedInterval() {
        return Integer.max(_siteConfig.getSecurityLastModifiedInterval(), 1);
    }

    private int getMaxLoginInterval() {
        return Integer.max(_siteConfig.getSecurityMaxLoginInterval(), 1);
    }

    private User getUserProfile(String username) throws DataFormatException, NotFoundException {
        if (!Users.isValidUsername(username)) {
            throw new DataFormatException("Invalid username");
        }
        return User.getUser(_jdbcTemplate, username);
    }

    private void setupAuthorization(final UserI user) throws ProviderNotFoundException {
        if (user == null) {
            return;
        }
        final String username = user.getUsername();
        if (Users.isGuest(username)) {
            return;
        }
        final UserAuthI userAuth = user.getAuthorization();
        if (userAuth != null && hasValidAuthorizationInformation(user) && !_service.hasUserByNameAndAuth(userAuth.getAuthUser(), userAuth.getAuthMethod(), userAuth.getAuthMethodId())) {
            final XdatUserAuth auth = new XdatUserAuth(userAuth.getAuthUser(), userAuth.getAuthMethod(), userAuth.getAuthMethodId());
            auth.setXdatUsername(username);
            try {
                _service.create(auth);
            } catch (Exception e) {
                log.error("Unable to create authorization for the user", e);
                throw e;
            }
        }
    }

    private boolean hasValidAuthorizationInformation(final UserI user) {
        return hasValidAuthorizationInformation(user.getAuthorization());
    }

    private boolean siteHasSingletonNonLocalAuth() {
        Map<String, XnatAuthenticationProvider> providers = _manager.getVisibleEnabledProviders();
        return providers.size() == 1 && !providers.entrySet().iterator().next().getKey().equals(XdatUserAuthService.LOCALDB);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasValidAuthorizationInformation(final User user) {
        return hasValidAuthorizationInformation(user.getAuthorization());
    }

    private boolean hasValidAuthorizationInformation(final UserAuthI userAuth) {
        if (userAuth != null) {
            final String authUser       = userAuth.getAuthUser();
            final String authMethod     = userAuth.getAuthMethod();
            final String authProviderId = userAuth.getAuthMethodId();
            if (authUser != null && authMethod != null && authProviderId != null) {
                return _manager.getProvider(authMethod, authProviderId) != null;
            }
        }
        return false;
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

    private static final String QUERY_USER_PROJECT_ROLES = "SELECT g.tag AS project, g.id AS role " +
                                                           "FROM xdat_user u " +
                                                           "         LEFT JOIN xdat_user_groupid gid ON u.xdat_user_id = gid.groups_groupid_xdat_user_xdat_user_id " +
                                                           "         LEFT JOIN xdat_usergroup g ON gid.groupid = g.id " +
                                                           "WHERE u.login = :" + PARAM_USERNAME;

    private final SessionRegistry            _sessionRegistry;
    private final AliasTokenService          _aliasTokenService;
    private final PermissionsServiceI        _permissionsService;
    private final UserFactory                _factory;
    private final NamedParameterJdbcTemplate _jdbcTemplate;
    private final SiteConfigPreferences      _siteConfig;
    private final UserChangeRequestService   _userChangeRequestService;
    private final XnatProviderManager        _manager;
    private final XdatUserAuthService        _service;
}
