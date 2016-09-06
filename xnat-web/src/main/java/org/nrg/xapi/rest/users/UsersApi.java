package org.nrg.xapi.rest.users;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.model.users.User;
import org.nrg.xapi.rest.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(description = "User Management API")
@XapiRestController
@RequestMapping(value = "/users")
public class UsersApi extends AbstractXapiRestController {
    @Autowired
    public UsersApi(final SiteConfigPreferences preferences, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
    }

    @ApiOperation(value = "Get list of users.", notes = "The primary users function returns a list of all users of the XNAT system. This includes just the username and nothing else. You can retrieve a particular user by adding the username to the REST API URL or a list of users with abbreviated user profiles by calling /xapi/users/profiles.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of usernames."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of usernames."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
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
    @RequestMapping(value = {"profiles"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> usersProfilesGet() {
        if (_preferences.getRestrictUserListAccessToAdmins()) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }
        List<UserI> users = getUserManagementService().getUsers();
        List<Map<String, String>> userMaps = null;
        if (users != null && users.size() > 0) {
            userMaps = new ArrayList<>();
            for (UserI user : users) {
                try {
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("firstname", user.getFirstname());
                    userMap.put("lastname", user.getLastname());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("id", String.valueOf(user.getID()));
                    userMap.put("enabled", String.valueOf(user.isEnabled()));
                    userMap.put("verified", String.valueOf(user.isVerified()));
                    userMaps.add(userMap);
                } catch (Exception e) {
                    _log.error("", e);
                }
            }
        }
        return new ResponseEntity<>(userMaps, HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the user with the specified user ID.", notes = "Returns the serialized user object with the specified user ID.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = {"{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<User> usersIdGet(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final UserI user;
        try {
            user = getUserManagementService().getUser(id);
            return user == null ? new ResponseEntity<User>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(new User(user), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Creates or updates the user object with the specified username.", notes = "Returns the updated serialized user object with the specified username.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully created or updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to create or update this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = {"{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<User> usersIdPut(@ApiParam(value = "The username of the user to create or update.", required = true) @PathVariable("id") String username, @RequestBody User model) throws NotFoundException {
        HttpStatus status = isPermitted(username);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        UserI user;
        try {
            user = getUserManagementService().getUser(username);
        } catch (Exception e) {
            //Create new User
            user = getUserManagementService().createUser();
            user.setLogin(username);
        }
        if (user == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Failed to retrieve or create a user object for user " + username);
        }
        if ((StringUtils.isNotBlank(model.getFirstName())) && (!StringUtils.equals(model.getFirstName(), user.getFirstname()))) {
            user.setFirstname(model.getFirstName());
        }
        if ((StringUtils.isNotBlank(model.getLastName())) && (!StringUtils.equals(model.getLastName(), user.getLastname()))) {
            user.setLastname(model.getLastName());
        }
        if ((StringUtils.isNotBlank(model.getEmail())) && (!StringUtils.equals(model.getEmail(), user.getEmail()))) {
            user.setEmail(model.getEmail());
        }
        if (model.isEnabled() != user.isEnabled()) {
            user.setEnabled(model.isEnabled());
            if (!model.isEnabled()) {
                //When a user is disabled, deactivate all their AliasTokens
                try {
                    XDAT.getContextService().getBean(AliasTokenService.class).deactivateAllTokensForUser(user.getLogin());
                } catch (Exception e) {
                    _log.error("", e);
                }
            }
        }
        if (model.isVerified() != user.isVerified()) {
            user.setVerified(model.isVerified());
        }

        try {
            getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Modified, "", ""));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            _log.error("Error occurred modifying user " + user.getLogin());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(value = "Returns whether the user with the specified user ID is enabled.", notes = "Returns true or false based on whether the specified user is enabled or not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = {"{id}/enabled"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> usersIdEnabledGet(@ApiParam(value = "The ID of the user to retrieve the enabled status for.", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user.isEnabled(), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/enabled/{flag}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> usersIdEnabledFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("id") String id, @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable("flag") Boolean flag) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.setEnabled(flag);
            try {
                getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred " + (flag ? "enabling" : "disabling") + " user " + user.getLogin());
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/verified"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> usersIdVerifiedGet(@ApiParam(value = "The ID of the user to retrieve the verified status for.", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user.isVerified(), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/verified/{flag}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> usersIdVerifiedFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("id") String id, @ApiParam(value = "The value to set for the verified status.", required = true) @PathVariable("flag") Boolean flag) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.setVerified(flag);
            try {
                getUserManagementService().save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred " + (flag ? "enabling" : "disabling") + " user " + user.getLogin());
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/roles"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Collection<String>> usersIdRolesGet(@ApiParam(value = "The ID of the user to retrieve the roles for.", required = true) @PathVariable("id") final String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final Collection<String> roles = getUserRoles(id);
        return roles != null ? new ResponseEntity<>(roles, HttpStatus.OK) : new ResponseEntity<Collection<String>>(HttpStatus.FORBIDDEN);
    }

    @ApiOperation(value = "Adds a role to a user.", notes = "Assigns a new role to a user.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User role successfully added."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = {"{id}/roles/{role}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> usersIdAddRole(@ApiParam(value = "ID of the user to add a role to", required = true) @PathVariable("id") String id, @ApiParam(value = "The user's new role.", required = true) @PathVariable("role") String role) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
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
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/roles/{role}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.DELETE})
    public ResponseEntity<Boolean> usersIdRemoveRole(@ApiParam(value = "ID of the user to delete a role from", required = true) @PathVariable("id") String id, @ApiParam(value = "The user role to delete.", required = true) @PathVariable("role") String role) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
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
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/groups"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Set<String>> usersIdGroupsGet(@ApiParam(value = "The ID of the user to retrieve the groups for.", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Map<String, UserGroupI> groups = Groups.getGroupsForUser(user);
            return new ResponseEntity<>(groups.keySet(), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Adds a user to a group.", notes = "Assigns user to a group.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully added to group."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."),
                   @ApiResponse(code = 404, message = "User not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = {"{id}/groups/{group}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> usersIdAddGroup(@ApiParam(value = "ID of the user to add to a group", required = true) @PathVariable("id") String id, @ApiParam(value = "The user's new group.", required = true) @PathVariable("group") final String group) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            try {
                Groups.addUserToGroup(group, user, getSessionUser(), null);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred adding user " + user.getLogin() + " to group " + group + ".");
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
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
    @RequestMapping(value = {"{id}/groups/{group}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.DELETE})
    public ResponseEntity<Boolean> usersIdRemoveGroup(@ApiParam(value = "ID of the user to remove from group", required = true) @PathVariable("id") final String id, @ApiParam(value = "The group to remove the user from.", required = true) @PathVariable("group") final String group) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = getUserManagementService().getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            try {
                Groups.removeUserFromGroup(user, group, null);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                _log.error("Error occurred removing user " + user.getLogin() + " from group " + group + ".");
            }
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
}
