package org.nrg.xapi.rest.users;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ServerException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.model.users.ElementDisplayModel;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nullable;
import java.util.*;

import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("Data Access API")
@XapiRestController
@RequestMapping(value = "/access")
@Slf4j
public class DataAccessApi extends AbstractXapiRestController {
    public static final String BROWSEABLE                = "browseable";
    public static final String BROWSEABLE_CREATEABLE     = "browseableCreateable";
    public static final String BROWSEABLE_CREATABLE      = "browseableCreatable";
    public static final String CREATEABLE                = "createable";
    public static final String CREATABLE                 = "creatable";
    public static final String READABLE                  = "readable";
    public static final String SEARCHABLE                = "searchable";
    public static final String SEARCHABLE_BY_DESC        = "searchableByDesc";
    public static final String SEARCHABLE_BY_PLURAL_DESC = "searchableByPluralDesc";

    @Autowired
    public DataAccessApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final GroupsAndPermissionsCache cache, final UserGroupServiceI groupService) {
        super(userManagementService, roleHolder);
        _cache = cache;
        _groupService = groupService;
    }

    @ApiOperation(value = "Gets the projects and roles associated with the current user.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of projects and roles for the current user."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Collection<Map<String, String>>> getProjectRoles() {
        return ResponseEntity.ok(getRoleHolder().getUserProjectRoles(getSessionUser()));
    }

    @ApiOperation(value = "Gets a list of the available element displays.",
                  notes = "The available element displays can be used as parameters for this call in the form /xapi/access/displays/{DISPLAY}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available element displays."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableElementDisplays() {
        return new ResponseEntity<>(AVAILABLE_ELEMENT_DISPLAYS, OK);
    }

    @ApiOperation(value = "Gets the last modified timestamp for the current user.",
                  notes = "This indicates the time of the latest update to elements relevant to the user. An update to these elements can mean that permissions for the user have changed and the various displays should be refreshed if cached on the client side.",
                  response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available element displays."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays/modified", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Date> getLastModified() {
        return new ResponseEntity<>(_cache.getUserLastUpdateTime(getSessionUser()), OK);
    }

    @ApiOperation(value = "Gets a list of the element displays of the specified type for the current user.",
                  notes = "This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of element displays of the specified type for the current user."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays/{display}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<List<ElementDisplayModel>> getElementDisplays(@PathVariable final String display) throws DataFormatException {
        return ResponseEntity.ok(getElementDisplayModels(getSessionUser(), display));
    }

    @ApiOperation(value = "Gets the projects and roles associated with the specified user. This can only be called by users with administrative privileges.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of projects and roles for the specified user."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the user's projects and roles."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/projects", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<Collection<Map<String, String>>> getUserProjectRoles(@ApiParam("The user to get the project roles for.") @PathVariable final String username) {
        return ResponseEntity.ok(getRoleHolder().getUserProjectRoles(username));
    }

    @ApiOperation(value = "Gets the last modified timestamp for the specified user. This can only be called by users with administrative privileges.",
                  notes = "This indicates the time of the latest update to elements relevant to the specified user. An update to these elements can mean that permissions for the specified user have changed and the various displays should be refreshed if cached on the client side.",
                  response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Timestamp of when the list of available element displays for the specified user was updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the user's cache timestamp."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/displays/modified", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<Date> getLastModifiedForUser(@ApiParam("The user to get the last modified timestamp for.") @PathVariable final String username) {
        return ResponseEntity.ok(_cache.getUserLastUpdateTime(username));
    }

    @ApiOperation(value = "Gets a list of the element displays of the specified type for the specified user. This can only be called by users with administrative privileges.",
                  response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of element displays of the specified type for the specified user."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays for the specified user."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{username}/displays/{display}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<List<ElementDisplayModel>> getElementDisplaysForUser(@PathVariable final String username, @PathVariable final String display) throws DataFormatException, ServerException, NotFoundException {
        try {
            return ResponseEntity.ok(getElementDisplayModels(Users.getUser(username), display));
        } catch (UserInitException e) {
            throw new ServerException(e);
        } catch (UserNotFoundException e) {
            throw new NotFoundException("User with username " + username + " was not found.");
        }
    }

    @ApiOperation(value = "Returns a map indicating the status of the cache initialization.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A map with information on the status of cache initialization."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 404, message = "Indicates that the cache implementation doesn't have the ability to report its status."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "cache/status", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<Map<String, String>> getCacheStatus() {
        if (Initializing.class.isAssignableFrom(_cache.getClass())) {
            final Initializing initializing = (Initializing) _cache;
            return new ResponseEntity<>(initializing.getInitializationStatus(), OK);
        }
        return new ResponseEntity<>(NOT_FOUND);
    }

    @ApiOperation(value = "Clears the element cache for the current user=.")
    @ApiResponses({@ApiResponse(code = 200, message = "The user's cache was properly cleared."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "cache/flush", method = DELETE, restrictTo = Authenticated)
    @ResponseBody
    public ResponseEntity<Void> flushUserCacheStatus() {
        _cache.clearUserCache(getSessionUser().getUsername());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Clears the element cache for the specified user.")
    @ApiResponses({@ApiResponse(code = 200, message = "The specified user's cache was properly cleared."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to clear the specified user's cache.."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "cache/flush/{username}", method = DELETE, restrictTo = User)
    @ResponseBody
    public ResponseEntity<Void> flushUserCacheStatus(@ApiParam("Indicates the name of the user whose cache should be flushed. If not provided, this flushes the cache of the current user.") @PathVariable @Username final String username) throws InsufficientPrivilegesException {
        final UserI user = getSessionUser();

        if (StringUtils.isNotBlank(username) && !StringUtils.equalsIgnoreCase(username, user.getUsername()) && !Roles.isSiteAdmin(user)) {
            throw new InsufficientPrivilegesException(user.getUsername(), username, "The user " + user.getUsername() + " attempted to clear the element cache for user " + username + ". This requires administrator privileges.");
        }

        _cache.clearUserCache(username);

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Finds any irregular permissions settings for standard project groups (Owners, Members, Collaborators).", responseContainer = "List", response = Map.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Irregular permissions were found and fixed."),
                   @ApiResponse(code = 204, message = "There were no irregular permissions to be fixed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to clear the specified user's cache.."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "permissions/group/{projectId}", restrictTo = Admin)
    @ResponseBody
    public List<Map<String, Object>> getProjectPermissions(final @PathVariable @Project String projectId) {
        return _groupService.getProjectGroupPermissions(projectId);
    }

    @ApiOperation(value = "Finds any irregular permissions settings for standard project groups (Owners, Members, Collaborators).", responseContainer = "List", response = Map.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Irregular permissions were found and fixed."),
                   @ApiResponse(code = 204, message = "There were no irregular permissions to be fixed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to clear the specified user's cache.."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "permissions/irregular/find", restrictTo = Admin)
    @ResponseBody
    public List<Map<String, Object>> findIrregularPermissions() {
        return _groupService.findIrregularProjectGroups();
    }

    @ApiOperation(value = "Finds and fixes any irregular permissions settings for standard project groups (Owners, Members, Collaborators).", responseContainer = "List", response = Integer.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Irregular permissions were found and fixed."),
                   @ApiResponse(code = 204, message = "There were no irregular permissions to be fixed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to clear the specified user's cache.."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "permissions/irregular/fix", method = POST, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<List<Integer>> fixIrregularPermissions() {
        final List<Map<String, Object>> irregulars = findIrregularPermissions();
        if (irregulars.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        log.warn("Found project groups with irregular permission mappings, preparing to fix:\n\n * {}", StringUtils.join(UserGroupManager.formatIrregularProjectGroups(irregulars), "\n * "));
        return ResponseEntity.ok(_groupService.fixIrregularProjectGroups());
    }

    private List<ElementDisplayModel> getElementDisplayModels(final UserI user, final @PathVariable String display) throws DataFormatException {
        final UserHelperServiceI helper = UserHelper.getUserHelperService(user);

        final List<ElementDisplay> displays;
        switch (display) {
            case BROWSEABLE:
                displays = helper.getBrowseableElementDisplays();
                break;

            case BROWSEABLE_CREATEABLE:
            case BROWSEABLE_CREATABLE:
                displays = helper.getBrowseableCreateableElementDisplays();
                break;

            case CREATEABLE:
            case CREATABLE:
                displays = helper.getCreateableElementDisplays();
                break;

            case SEARCHABLE:
                displays = helper.getSearchableElementDisplays();
                break;

            case SEARCHABLE_BY_DESC:
                displays = helper.getSearchableElementDisplaysByDesc();
                break;

            case SEARCHABLE_BY_PLURAL_DESC:
                displays = helper.getSearchableElementDisplaysByPluralDesc();
                break;

            default:
                throw new DataFormatException("The requested element display \"" + display + "\" is not recognized.");
        }

        return Lists.newArrayList(Iterables.filter(Lists.transform(displays, new Function<ElementDisplay, ElementDisplayModel>() {
            @Nullable
            @Override
            public ElementDisplayModel apply(@Nullable final ElementDisplay elementDisplay) {
                try {
                    return elementDisplay != null ? new ElementDisplayModel(elementDisplay) : null;
                } catch (Exception e) {
                    log.warn("An exception occurred trying to transform the element display \"{}\"", elementDisplay.getElementName(), e);
                    return null;
                }
            }
        }), Predicates.<ElementDisplayModel>notNull()));
    }

    private static final List<String> AVAILABLE_ELEMENT_DISPLAYS = Arrays.asList(BROWSEABLE, BROWSEABLE_CREATEABLE, CREATEABLE, SEARCHABLE, SEARCHABLE_BY_DESC, SEARCHABLE_BY_PLURAL_DESC);

    private final GroupsAndPermissionsCache _cache;
    private final UserGroupServiceI         _groupService;
}
