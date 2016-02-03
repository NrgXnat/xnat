package org.nrg.xapi.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.model.User;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(description = "The XNAT POC User Management API")
@RestController
@RequestMapping(value = "/api/users")
public class UsersApi {
    private static final Logger _log = LoggerFactory.getLogger(UsersApi.class);

    @ApiOperation(value = "Get list of users.", notes = "The primary users function returns a list of all users of the XNAT system.", response = User.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "An array of users"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {"application/json", "application/xml"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<String>> usersGet() {
        return new ResponseEntity<List<String>>(new ArrayList<>(Users.getAllLogins()), HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the user with the specified user ID.", notes = "Returns the serialized user object with the specified user ID.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this user."), @ApiResponse(code = 404, message = "User not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}"}, produces = {"application/json", "application/xml", "text/html"}, method = {RequestMethod.GET})
    public ResponseEntity<User> usersIdGet(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final UserI user;
        try {
            user = Users.getUser(id);
            return user == null ? new ResponseEntity<User>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(new User(user), HttpStatus.OK);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Creates or updates the user object with the specified user ID.", notes = "Returns the updated serialized user object with the specified user ID.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User successfully created or updated."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to create or update this user."), @ApiResponse(code = 404, message = "User not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}"}, produces = {"application/json", "application/xml", "text/html"}, method = {RequestMethod.PUT})
    public ResponseEntity<User> usersIdPut(@ApiParam(value = "The ID of the user to create or update.", required = true) @PathVariable("id") String id, @RequestBody User model) throws NotFoundException {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final UserI user;
        try {
            user = Users.getUser(id);
        } catch (UserInitException e) {
            _log.error("An error occurred initializing the user " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if ((StringUtils.isNotBlank(model.getFirstname())) && (StringUtils.equals(model.getFirstname(), user.getFirstname()))) {
            user.setFirstname(model.getFirstname());
        }
        if ((StringUtils.isNotBlank(model.getLastname())) && (StringUtils.equals(model.getLastname(), user.getLastname()))) {
            user.setLastname(model.getLastname());
        }
        if ((StringUtils.isNotBlank(model.getEmail())) && (StringUtils.equals(model.getEmail(), user.getEmail()))) {
            user.setEmail(model.getEmail());
        }
        if (StringUtils.isNotBlank(model.getPassword())) {
            user.setPassword(model.getPassword());
        }
        try {
            Users.save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, Event.Modified, "", ""));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            _log.error("Error occurred modifying user " + user.getLogin());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(value = "Returns whether the user with the specified user ID is enabled.", notes = "Returns true or false based on whether the specified user is enabled or not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this user."), @ApiResponse(code = 404, message = "User not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/enabled"}, produces = {"application/json"}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> usersIdEnabledGet(@ApiParam(value = "The ID of the user to retrieve the enabled status for.", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = Users.getUser(id);
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
    @ApiResponses({@ApiResponse(code = 200, message = "User enabled status successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this user."), @ApiResponse(code = 404, message = "User not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/enabled/{flag}"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> usersIdEnabledFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("id") String id, @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable("flag") Boolean flag) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = Users.getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.setEnabled(flag);
            try {
                Users.save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
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
    @ApiResponses({@ApiResponse(code = 200, message = "User verified status successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this user."), @ApiResponse(code = 404, message = "User not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/verified"}, produces = {"application/json"}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> usersIdVerifiedGet(@ApiParam(value = "The ID of the user to retrieve the verified status for.", required = true) @PathVariable("id") String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = Users.getUser(id);
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
    @ApiResponses({@ApiResponse(code = 200, message = "User verified status successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to verify or unverify this user."), @ApiResponse(code = 404, message = "User not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/verified/{flag}"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> usersIdVerifiedFlagPut(@ApiParam(value = "ID of the user to fetch", required = true) @PathVariable("id") String id, @ApiParam(value = "The value to set for the verified status.", required = true) @PathVariable("flag") Boolean flag) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            final UserI user = Users.getUser(id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            user.setVerified(flag);
            try {
                Users.save(user, getSessionUser(), false, new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, flag ? Event.Enabled : Event.Disabled, "", ""));
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

    private UserI getSessionUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ((principal instanceof UserI)) {
            return (UserI) principal;
        }
        return null;
    }

    private HttpStatus isPermitted(String id) {
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        if ((sessionUser.getUsername().equals(id)) || (isPermitted() == null)) {
            return null;
        }
        return HttpStatus.FORBIDDEN;
    }

    private HttpStatus isPermitted() {
        UserI sessionUser = getSessionUser();
        if ((sessionUser instanceof XDATUser)) {
            return ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }

        return null;
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
}
