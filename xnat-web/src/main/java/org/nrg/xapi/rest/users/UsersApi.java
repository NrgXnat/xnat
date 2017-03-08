/*
 * web: org.nrg.xapi.rest.users.UsersApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.users;

import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.Patterns;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.model.users.User;
import org.nrg.xapi.model.users.UserFactory;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Api(description = "User Management API")
@XapiRestController
@RequestMapping(value = "/users")
public class UsersApi extends AbstractXapiRestController {
    @Autowired
    public UsersApi(final SiteConfigPreferences preferences, final UserManagementServiceI userManagementService, final UserFactory factory,
                    final RoleHolder roleHolder, final SessionRegistry sessionRegistry, final AliasTokenService aliasTokenService, final JdbcTemplate jdbcTemplate) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
        _sessionRegistry = sessionRegistry;
        _aliasTokenService = aliasTokenService;
        _factory = factory;
        _jdbcTemplate = jdbcTemplate;
    }

    @ApiOperation(value = "Get list of users.", notes = "The primary users function returns a list of all users of the XNAT system. This includes just the username and nothing else. You can retrieve a particular user by adding the username to the REST API URL or a list of users with abbreviated user profiles by calling /xapi/users/profiles.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of usernames."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<String>> usersGet() {
        if (_preferences.getRestrictUserListAccessToAdmins()) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }
        return new ResponseEntity<List<String>>(new ArrayList<>(Users.getAllLogins()), HttpStatus.OK);
    }

    @ApiOperation(value = "Get list of user profiles.", notes = "The users' profiles function returns a list of all users of the XNAT system with brief information about each.", response = User.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of user profiles."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "profiles", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<User>> usersProfilesGet() {
        if (_preferences.getRestrictUserListAccessToAdmins()) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }

        final List<User> usersList = _jdbcTemplate.query("SELECT enabled, login AS username, xdat_user_id AS id, firstname AS firstName, lastname AS lastName, email, verified, last_modified, auth.max_login AS lastSuccessfulLogin FROM xdat_user JOIN xdat_user_meta_data ON xdat_user.xdat_user_id=xdat_user_meta_data.meta_data_id JOIN (SELECT xdat_username, max(last_successful_login) max_login FROM xhbm_xdat_user_auth GROUP BY xdat_username) auth ON xdat_user.login=auth.xdat_username ORDER BY xdat_user.xdat_user_id", new RowMapper<User>() {
            @Override
            public User mapRow(final ResultSet resultSet, final int i) throws SQLException {
                Timestamp lastSuccessfulLogin = resultSet.getTimestamp("lastSuccessfulLogin");
                Date lastSuccessfulLoginDate = null;
                if(lastSuccessfulLogin!=null){
                    lastSuccessfulLoginDate = new Date(lastSuccessfulLogin.getTime());
                }
                Timestamp lastModified = resultSet.getTimestamp("last_modified");
                Date lastModifiedDate = null;
                if(lastModified!=null){
                    lastModifiedDate = new Date(lastModified.getTime());
                }

                return new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("firstName"), resultSet.getString("lastName"), resultSet.getString("email"), null, null, null, true, lastModifiedDate, null, resultSet.getInt("enabled") == 1, resultSet.getInt("verified") == 1, lastSuccessfulLoginDate);
            }
        });

        return new ResponseEntity<>(usersList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get user profile.", notes = "The user profile function returns a user of the XNAT system with brief information.", response = User.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A user profile."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the user profile."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "profile/{username}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<User> usersProfileGet(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("username") final String username) {
        if (_preferences.getRestrictUserListAccessToAdmins()) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }

        List<User> usersList = null;
        String regex = "^[a-zA-Z0-9]+[a-zA-Z0-9._-]*$";
        if(username.matches(regex)){
            usersList = _jdbcTemplate.query("SELECT enabled, login AS username, xdat_user_id AS id, firstname AS firstName, lastname AS lastName, email, verified, last_modified, auth.max_login AS lastSuccessfulLogin FROM xdat_user JOIN xdat_user_meta_data ON xdat_user.xdat_user_id=xdat_user_meta_data.meta_data_id JOIN (SELECT xdat_username, max(last_successful_login) max_login FROM xhbm_xdat_user_auth GROUP BY xdat_username) auth ON xdat_user.login=auth.xdat_username WHERE xdat_user.login='"+username+"'", new RowMapper<User>() {
                @Override
                public User mapRow(final ResultSet resultSet, final int i) throws SQLException {
                    Timestamp lastSuccessfulLogin = resultSet.getTimestamp("lastSuccessfulLogin");
                    Date lastSuccessfulLoginDate = null;
                    if(lastSuccessfulLogin!=null){
                        lastSuccessfulLoginDate = new Date(lastSuccessfulLogin.getTime());
                    }
                    Timestamp lastModified = resultSet.getTimestamp("last_modified");
                    Date lastModifiedDate = null;
                    if(lastModified!=null){
                        lastModifiedDate = new Date(lastModified.getTime());
                    }

                    return new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("firstName"), resultSet.getString("lastName"), resultSet.getString("email"), null, null, null, true, lastModifiedDate, null, resultSet.getInt("enabled") == 1, resultSet.getInt("verified") == 1, lastSuccessfulLoginDate);
                }
            });
        }
        if(usersList!=null && usersList.size()>0) {
            return new ResponseEntity<>(usersList.get(0), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Get list of users who are enabled or who have interacted with the site somewhat recently.", notes = "The users' profiles function returns a list of all users of the XNAT system with brief information about each.", response = User.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of user profiles."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "current", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<User>> currentUsersProfilesGet() {
        if (_preferences.getRestrictUserListAccessToAdmins()) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }

        final List<User> usersList = _jdbcTemplate.query("SELECT enabled, login AS username, xdat_user_id AS id, firstname AS firstName, lastname AS lastName, email, verified, last_modified, auth.max_login AS lastSuccessfulLogin FROM xdat_user JOIN xdat_user_meta_data ON xdat_user.xdat_user_id=xdat_user_meta_data.meta_data_id JOIN (SELECT xdat_username, max(last_successful_login) max_login FROM xhbm_xdat_user_auth GROUP BY xdat_username) auth ON xdat_user.login=auth.xdat_username WHERE (xdat_user.enabled=1 OR (max_login > (CURRENT_DATE - INTERVAL '1 year') OR (max_login IS NULL AND (xdat_user_meta_data.last_modified > (CURRENT_DATE - INTERVAL '1 year') ) ) )) ORDER BY xdat_user.xdat_user_id", new RowMapper<User>() {
            @Override
            public User mapRow(final ResultSet resultSet, final int i) throws SQLException {
                Timestamp lastSuccessfulLogin = resultSet.getTimestamp("lastSuccessfulLogin");
                Date lastSuccessfulLoginDate = null;
                if(lastSuccessfulLogin!=null){
                    lastSuccessfulLoginDate = new Date(lastSuccessfulLogin.getTime());
                }
                Timestamp lastModified = resultSet.getTimestamp("last_modified");
                Date lastModifiedDate = null;
                if(lastModified!=null){
                    lastModifiedDate = new Date(lastModified.getTime());
                }

                return new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("firstName"), resultSet.getString("lastName"), resultSet.getString("email"), null, null, null, true, lastModifiedDate, null, resultSet.getInt("enabled") == 1, resultSet.getInt("verified") == 1, lastSuccessfulLoginDate);
            }
        });

        return new ResponseEntity<>(usersList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get list of active users.", notes = "Returns a map of usernames for users that have at least one currently active session, i.e. logged in or associated with a valid application session. The number of active sessions and a list of the session IDs is associated with each user.", response = Map.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of active users."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "active", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Map<String, Object>>> getActiveUsers() {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

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
            final int count = sessions.size();
            if (count == 0) {
                continue;
            }

            // Now add user with a session or more to the list of active users.
            final ArrayList<String> sessionIds = new ArrayList<>();
            for (final SessionInformation session : sessions) {
                sessionIds.add(session.getSessionId());
            }

            final Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("sessions", sessionIds);
            sessionData.put("count", count);

            activeUsers.put(username, sessionData);
        }
        return new ResponseEntity<>(activeUsers, HttpStatus.OK);
    }

    @ApiOperation(value = "Get information about active sessions for the indicated user.", notes = "Returns a map containing a list of session IDs and usernames for users that have at least one currently active session, i.e. logged in or associated with a valid application session. This also includes the number of active sessions for each user.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of active users."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 404, message = "The indicated user has no active sessions or is not a valid user."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "active/{username}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<String>> getUserActiveSessions(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("username") final String username) {
        final HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        for (final Object principal : _sessionRegistry.getAllPrincipals()) {
            final Object located = locatePrincipalByUsername(username);
            if (located == null) {
                continue;
            }
            final List<SessionInformation> sessions = _sessionRegistry.getAllSessions(principal, false);
            final List<String> sessionIds = new ArrayList<>();
            for (final SessionInformation session : sessions) {
                sessionIds.add(session.getSessionId());
            }
            return new ResponseEntity<>(sessionIds, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Gets the user with the specified user ID.", notes = "Returns the serialized user object with the specified user ID.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@ApiParam(value = "Username of the user to fetch.", required = true) @PathVariable("username") final String username) {
        if (_preferences.getRestrictUserListAccessToAdmins()) {
            final HttpStatus status = isPermitted(username);
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }
        final UserI user;
        try {
            user = getUserManagementService().getUser(username);
            return user == null ? new ResponseEntity<User>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(_factory.getUser(user), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Updates the user object with the specified username.", notes = "Returns the updated serialized user object with the specified username.", response = User.class)
    @ApiResponses({@ApiResponse(code = 201, message = "User successfully created."),
                   @ApiResponse(code = 400, message = "The submitted data was invalid."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to update this user."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<User> createUser(@RequestBody final User model) throws NotFoundException, PasswordComplexityException, DataFormatException, UserInitException, ResourceAlreadyExistsException {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        validateUser(model);

        final UserI user = getUserManagementService().createUser();

        if (user == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Failed to create a user object for user " + model.getUsername());
        }

        user.setLogin(model.getUsername());
        user.setFirstname(model.getFirstName());
        user.setLastname(model.getLastName());
        user.setEmail(model.getEmail());
        if (model.isEnabled() != null) {
            user.setEnabled(model.isEnabled());
        }
        if (model.isVerified() != null) {
            user.setVerified(model.isVerified());
        }

        user.setPassword(model.getPassword());
        user.setAuthorization(model.getAuthorization());

        try {
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Added, "Requested by user " + getSessionUser().getUsername(), "Created new user " + user.getUsername() + " through XAPI user management API."));

            if (model.isVerified() && model.isEnabled()) {
                //When a user is enabled and verified, send a new user email
                try {
                    AdminUtils.sendNewUserEmailMessage(user.getUsername(), user.getEmail());
                } catch (Exception e) {
                    _log.error("", e);
                }
            }
            return new ResponseEntity<>(_factory.getUser(user), HttpStatus.CREATED);
        } catch (Exception e) {
            _log.error("Error occurred modifying user " + user.getLogin());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(value = "Updates the user object with the specified username.", notes = "Returns the updated serialized user object with the specified username.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully updated."),
                   @ApiResponse(code = 304, message = "The user object was not modified because no attributes were changed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to update this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<User> updateUser(@ApiParam(value = "The username of the user to create or update.", required = true) @PathVariable("username") final String username, @RequestBody final User model) throws NotFoundException, PasswordComplexityException, UserInitException {
        final HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        final UserI user;
        try {
            user = getUserManagementService().getUser(username);
        } catch (UserNotFoundException e) {
            throw new NotFoundException("User with username " + username + " was not found.");
        }

        if (user == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Failed to retrieve user object for user " + username);
        }
        boolean oldEnabledFlag = user.isEnabled();
        boolean oldVerifiedFlag = user.isVerified();

        boolean isDirty = false;
        if ((StringUtils.isNotBlank(model.getUsername())) && (!StringUtils.equals(user.getUsername(), model.getUsername()))) {
            user.setLogin(model.getUsername());
            isDirty = true;
        }
        if ((StringUtils.isNotBlank(model.getFirstName())) && (!StringUtils.equals(user.getFirstname(), model.getFirstName()))) {
            user.setFirstname(model.getFirstName());
            isDirty = true;
        }
        if ((StringUtils.isNotBlank(model.getLastName())) && (!StringUtils.equals(user.getLastname(), model.getLastName()))) {
            user.setLastname(model.getLastName());
            isDirty = true;
        }
        if ((StringUtils.isNotBlank(model.getEmail())) && (!StringUtils.equals(user.getEmail(), model.getEmail()))) {
            user.setEmail(model.getEmail());
            isDirty = true;
        }
        // Don't do password compare: we can't.
        if (StringUtils.isNotBlank(model.getPassword())) {
            user.setPassword(model.getPassword());
            isDirty = true;
        }
        if (model.getAuthorization() != null && !model.getAuthorization().equals(user.getAuthorization())) {
            user.setAuthorization(model.getAuthorization());
            isDirty = true;
        }
        final Boolean enabled = model.isEnabled();
        if (enabled != null && enabled != user.isEnabled()) {
            user.setEnabled(enabled);
            if (!enabled) {
                //When a user is disabled, deactivate all their AliasTokens
                try {
                    _aliasTokenService.deactivateAllTokensForUser(user.getLogin());
                } catch (Exception e) {
                    _log.error("", e);
                }
            }
            isDirty = true;
        }
        final Boolean verified = model.isVerified();
        if (verified != null && verified != user.isVerified()) {
            user.setVerified(verified);
            isDirty = true;
        }

        if (!isDirty) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        try {
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Modified, "", ""));
            if (model.isVerified() && model.isEnabled() && (!oldEnabledFlag || !oldVerifiedFlag)) {
                //When a user is enabled and verified, send a new user email
                try {
                    AdminUtils.sendNewUserEmailMessage(user.getUsername(), user.getEmail());
                } catch (Exception e) {
                    _log.error("", e);
                }
            }
            return new ResponseEntity<>(_factory.getUser(user), HttpStatus.OK);
        } catch (Exception e) {
            _log.error("Error occurred modifying user " + user.getLogin());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Invalidates all active sessions associated with the specified username.", notes = "Returns a list of session IDs that were invalidated.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully invalidated."),
                   @ApiResponse(code = 304, message = "Indicated user has no active sessions, so no action was taken."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to create or update this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "active/{username}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<List<String>> invalidateUser(final HttpSession current, @ApiParam(value = "The username of the user to invalidate.", required = true) @PathVariable("username") final String username) throws NotFoundException {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final UserI user;
        final String currentSessionId;
        if (StringUtils.equals(getSessionUser().getUsername(), username)) {
            user = getSessionUser();
            currentSessionId = current.getId();
        } else {
            try {
                user = getUserManagementService().getUser(username);
                if (user == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                currentSessionId = null;
            } catch (UserInitException e) {
                _log.error("An error occurred initializing the user " + username, e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (UserNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        final Object located = locatePrincipalByUsername(user.getUsername());
        if (located == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
        final List<SessionInformation> sessions = _sessionRegistry.getAllSessions(located, false);
        if (sessions.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
        final List<String> sessionIds = new ArrayList<>();
        for (final SessionInformation session : sessions) {
            final String sessionId = session.getSessionId();
            if (!StringUtils.equals(currentSessionId, sessionId)) {
                sessionIds.add(sessionId);
                session.expireNow();
            }
        }
        return new ResponseEntity<>(sessionIds, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether the user with the specified user ID is enabled.", notes = "Returns true or false based on whether the specified user is enabled or not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/enabled", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Boolean> usersIdEnabledGet(@ApiParam(value = "The ID of the user to retrieve the enabled status for.", required = true) @PathVariable("username") final String username) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user.isEnabled(), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Sets the user's enabled state.", notes = "Sets the enabled state of the user with the specified user ID to the value of the flag parameter.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/enabled/{flag}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Boolean> usersIdEnabledFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("username") final String username, @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable("flag") Boolean flag) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            boolean oldEnabledFlag = user.isEnabled();
            user.setEnabled(flag);
            try {
                getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
                if (flag && !oldEnabledFlag && user.isVerified()) {
                    //When a user is enabled, send a new user email if they're also verified
                    try {
                        AdminUtils.sendNewUserEmailMessage(username, user.getEmail());
                    } catch (Exception e) {
                        _log.error("", e);
                    }
                }
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred " + (flag ? "enabling" : "disabling") + " user " + user.getLogin());
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns whether the user with the specified user ID is verified.", notes = "Returns true or false based on whether the specified user is verified or not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User verified status successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/verified", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Boolean> usersIdVerifiedGet(@ApiParam(value = "The ID of the user to retrieve the verified status for.", required = true) @PathVariable("username") final String username) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user.isVerified(), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Sets the user's verified state.", notes = "Sets the verified state of the user with the specified user ID to the value of the flag parameter.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User verified status successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to verify or un-verify this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/verified/{flag}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Boolean> usersIdVerifiedFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("username") final String username, @ApiParam(value = "The value to set for the verified status.", required = true) @PathVariable("flag") Boolean flag) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            boolean oldVerifiedFlag = user.isVerified();
            user.setVerified(flag);
            try {
                getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
                if (flag && !oldVerifiedFlag && user.isEnabled()) {
                    //When a user is verified, send a new user email if they're also enabled
                    try {
                        AdminUtils.sendNewUserEmailMessage(username, user.getEmail());
                    } catch (Exception e) {
                        _log.error("", e);
                    }
                }
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred " + (flag ? "enabling" : "disabling") + " user " + user.getLogin());
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns the roles for the user with the specified user ID.", notes = "Returns a collection of the user's roles.", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User roles successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/roles", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> usersIdRolesGet(@ApiParam(value = "The ID of the user to retrieve the roles for.", required = true) @PathVariable("username") final String username) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final Collection<String> roles = getUserRoles(username);
        return roles != null ? new ResponseEntity<>(roles, HttpStatus.OK) : new ResponseEntity<Collection<String>>(HttpStatus.FORBIDDEN);
    }

    @ApiOperation(value = "Adds one or more roles to a user.", notes = "Assigns one or more new roles to a user.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "All specified user roles successfully added."),
                   @ApiResponse(code = 202, message = "Some user roles successfully added, but some may have failed. Check the return value for roles that the service was unable to add."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/roles", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Collection<String>> usersIdAddRoles(@ApiParam(value = "ID of the user to add a role to", required = true) @PathVariable("username") final String username,
                                                              @ApiParam(value = "The user's new roles.", required = true) @RequestBody final List<String> roles) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        final Collection<String> failed = Lists.newArrayList();

        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            for (final String role : roles) {
                try {
                    getRoleHolder().addRole(getSessionUser(), user, role);
                } catch (Exception e) {
                    failed.add(role);
                    _log.error("Error occurred adding role " + role + " to user " + user.getLogin() + ".", e);
                }
            }
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (failed.size() == 0) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(failed, HttpStatus.ACCEPTED);
    }

    @ApiOperation(value = "Removes one or more roles from a user.", notes = "Removes one or more new roles from a user.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "All specified user roles successfully removed."),
                   @ApiResponse(code = 202, message = "Some user roles successfully removed, but some may have failed. Check the return value for roles that the service was unable to remove."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/roles", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<Collection<String>> usersIdRemoveRoles(@ApiParam(value = "ID of the user to remove role from", required = true) @PathVariable("username") final String username,
                                                                 @ApiParam(value = "The roles to be removed.", required = true) @RequestBody final List<String> roles) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        final Collection<String> failed = Lists.newArrayList();

        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            for (final String role : roles) {
                try {
                    getRoleHolder().deleteRole(getSessionUser(), user, role);
                } catch (Exception e) {
                    failed.add(role);
                    _log.error("Error occurred remove role " + role + " from user " + user.getLogin() + ".", e);
                }
            }
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (failed.size() == 0) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(failed, HttpStatus.ACCEPTED);
    }

    @ApiOperation(value = "Adds a role to a user.", notes = "Assigns a new role to a user.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User role successfully added."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/roles/{role}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Boolean> usersIdAddRole(@ApiParam(value = "ID of the user to add a role to", required = true) @PathVariable("username") final String username,
                                                  @ApiParam(value = "The user's new role.", required = true) @PathVariable("role") final String role) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            try {
                getRoleHolder().addRole(getSessionUser(), user, role);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred adding role " + role + " to user " + user.getLogin() + ".");
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Remove a user's role.", notes = "Removes a user's role.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User role successfully removed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/roles/{role}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> usersIdRemoveRole(@ApiParam(value = "ID of the user to delete a role from", required = true) @PathVariable("username") final String username,
                                                     @ApiParam(value = "The user role to delete.", required = true) @PathVariable("role") String role) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            try {
                getRoleHolder().deleteRole(getSessionUser(), user, role);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred removing role " + role + " from user " + user.getLogin() + ".");
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Returns the groups for the user with the specified user ID.", notes = "Returns a collection of the user's groups.", response = Set.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User groups successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/groups", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Set<String>> usersIdGroupsGet(@ApiParam(value = "The ID of the user to retrieve the groups for.", required = true) @PathVariable("username") final String username) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Map<String, UserGroupI> groups = Groups.getGroupsForUser(user);
            return new ResponseEntity<>(groups.keySet(), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Adds the user to one or more groups.", notes = "Assigns the user to one or more new groups.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully added for all specified groups."),
                   @ApiResponse(code = 202, message = "User was successfully added to some of the specified groups, but some may have failed. Check the return value for groups that the service was unable to add."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/groups", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Collection<String>> usersIdAddGroups(@ApiParam(value = "ID of the user to add to the specified groups", required = true) @PathVariable("username") final String username,
                                                               @ApiParam(value = "The groups to which the user should be added.", required = true) @RequestBody final List<String> groups) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        final Collection<String> failed = Lists.newArrayList();

        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            for (final String group : groups) {
                try {
                    Groups.addUserToGroup(group, user, getSessionUser(), EventUtils.ADMIN_EVENT(getSessionUser()));
                } catch (Exception e) {
                    failed.add(group);
                    _log.error("Error occurred adding user " + user.getLogin() + " to group " + group + ".", e);
                }
            }
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (failed.size() == 0) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(failed, HttpStatus.ACCEPTED);
    }

    @ApiOperation(value = "Removes the user from one or more groups.", notes = "Removes the user from one or more groups.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully removed from all specified groups."),
                   @ApiResponse(code = 202, message = "User was successfully removed from some of the specified groups, but some may have failed. Check the return value for groups that the service was unable to remove."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/groups", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<Collection<String>> usersIdRemoveGroups(@ApiParam(value = "ID of the user to remove role from", required = true) @PathVariable("username") final String username,
                                                                  @ApiParam(value = "The groups from which the user should be removed.", required = true) @RequestBody final List<String> groups) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        final Collection<String> failed = Lists.newArrayList();

        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            for (final String group : groups) {
                try {
                    Groups.removeUserFromGroup(user, getSessionUser(), group, EventUtils.ADMIN_EVENT(getSessionUser()));
                } catch (Exception e) {
                    failed.add(group);
                    _log.error("Error occurred removing group " + group + " from user " + user.getLogin() + ".", e);
                }
            }
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (failed.size() == 0) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(failed, HttpStatus.ACCEPTED);
    }

    @ApiOperation(value = "Adds a user to a group.", notes = "Assigns user to a group.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully added to group."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/groups/{group}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Boolean> usersIdAddGroup(@ApiParam(value = "ID of the user to add to a group", required = true) @PathVariable("username") final String username, @ApiParam(value = "The user's new group.", required = true) @PathVariable("group") final String group) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if (user.getID().equals(Users.getGuest().getID())) {
                return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
            }
            try {
                Groups.addUserToGroup(group, user, getSessionUser(), null);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred adding user " + user.getLogin() + " to group " + group + ".");
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Removes a user from a group.", notes = "Removes a user from a group.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User's group successfully removed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "{username}/groups/{group}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> usersIdRemoveGroup(@ApiParam(value = "ID of the user to remove from group", required = true) @PathVariable("username") final String username, @ApiParam(value = "The group to remove the user from.", required = true) @PathVariable("group") final String group) {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            try {
                Groups.removeUserFromGroup(user, getSessionUser(), group, null);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred removing user " + user.getLogin() + " from group " + group + ".");
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Nullable
    private Object locatePrincipalByUsername(final String username) {
        Object located = null;
        for (final Object principal : _sessionRegistry.getAllPrincipals()) {
            if (principal instanceof String && username.equals(principal)) {
                located = principal;
                break;
            } else if (principal instanceof UserDetails && username.equals(((UserDetails) principal).getUsername())) {
                located = principal;
                break;
            } else if (username.equals(principal.toString())) {
                located = principal;
                break;
            }
        }
        return located;
    }

    private void validateUser(final User model) throws DataFormatException, UserInitException, ResourceAlreadyExistsException {
        final DataFormatException exception = new DataFormatException();

        if (StringUtils.isBlank(model.getUsername())) {
            exception.addMissing("username");
        } else if (!Patterns.USERNAME.matcher(model.getUsername()).matches()) {
            exception.addInvalid("username");
        }

        try {
            final UserI user = getUserManagementService().getUser(model.getUsername());
            if (user != null) {
                throw new ResourceAlreadyExistsException("user", model.getUsername());
            }
        } catch (UserNotFoundException ignored) {
            // This is actually what we want.
        }

        if (StringUtils.isBlank(model.getEmail())) {
            exception.addMissing("email");
        } else if (!Patterns.EMAIL.matcher(model.getEmail()).matches()) {
            exception.addInvalid("email");
        }

        if (exception.hasDataFormatErrors()) {
            throw exception;
        }
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

    private static final Logger _log = LoggerFactory.getLogger(UsersApi.class);

    private final SiteConfigPreferences _preferences;
    private final SessionRegistry       _sessionRegistry;
    private final AliasTokenService     _aliasTokenService;
    private final UserFactory           _factory;
    private final JdbcTemplate          _jdbcTemplate;
}
