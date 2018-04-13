package org.nrg.xapi.rest.users;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.model.users.ElementDisplayModel;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.Initializing;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Api(description = "Data Access API")
@XapiRestController
@RequestMapping(value = "/access")
@Slf4j
public class DataAccessApi extends AbstractXapiRestController {
    public static final String BROWSEABLE                = "browseable";
    public static final String BROWSEABLE_CREATEABLE     = "browseableCreateable";
    public static final String CREATEABLE                = "createable";
    public static final String READABLE                  = "readable";
    public static final String SEARCHABLE                = "searchable";
    public static final String SEARCHABLE_BY_DESC        = "searchableByDesc";
    public static final String SEARCHABLE_BY_PLURAL_DESC = "searchableByPluralDesc";

    @Autowired
    public DataAccessApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final GroupsAndPermissionsCache cache) {
        super(userManagementService, roleHolder);
        _cache = cache;
    }

    @ApiOperation(value = "Gets a list of the available element displays.", notes = "The available element displays can be used as parameters for this call in the form /xapi/access/displays/{DISPLAY}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available element displays."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Set<String>> getAvailableDataTypes() throws XFTInitException {
        return new ResponseEntity<>(getAvailableElements(), OK);
    }

    @ApiOperation(value = "Gets a list of the available element displays.", notes = "The available element displays can be used as parameters for this call in the form /xapi/access/displays/{DISPLAY}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available element displays."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/{dataType}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<GenericWrapperElement> getDataType(@ApiParam("The data type to be normalized.") @PathVariable final String dataType) throws XFTInitException {
        log.info("Trying to get the data type");
        try {
            return new ResponseEntity<>(GenericWrapperElement.GetElement("FieldDefinitionSet"), OK);
        } catch (ElementNotFoundException e) {
            return new ResponseEntity<>(NOT_FOUND);
        }
    }

    @ApiOperation(value = "Gets a list of the available element displays.", notes = "The available element displays can be used as parameters for this call in the form /xapi/access/displays/{DISPLAY}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).", response = String.class, responseContainer = "List")
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

    @ApiOperation(value = "Gets the last modified timestamp for the current user.", notes = "This indicates the time of the latest update to elements relevant to the user. An update to these elements can mean that permissions for the user have changed and the various displays should be refreshed if cached on the client side.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available element displays."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays/modified", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Date> getLastModified() {
        return new ResponseEntity<>(_cache.getLastUpdateTime(getSessionUser()), OK);
    }

    @ApiOperation(value = "Gets a list of the element displays of the specified type for the current user.", notes = "This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of element displays of the specified type for the current user."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available element displays."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "displays/{display}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<List<ElementDisplayModel>> getElementDisplays(@PathVariable final String display) throws DataFormatException {
        final UserI              user   = getSessionUser();
        final UserHelperServiceI helper = UserHelper.getUserHelperService(user);

        final List<ElementDisplay> displays;
        switch (display) {
            case "browseable":
                displays = helper.getBrowseableElementDisplays();
                break;

            case "browseableCreateable":
                displays = helper.getBrowseableCreateableElementDisplays();
                break;

            case "createable":
                displays = helper.getCreateableElementDisplays();
                break;

            case "searchable":
                displays = helper.getSearchableElementDisplays();
                break;

            case "searchableByDesc":
                displays = helper.getSearchableElementDisplaysByDesc();
                break;

            case "searchableByPluralDesc":
                displays = helper.getSearchableElementDisplaysByPluralDesc();
                break;

            default:
                throw new DataFormatException("The requested element display \"" + display + "\" is not recognized.");
        }

        final List<ElementDisplayModel> models = Lists.newArrayList(Iterables.filter(Lists.transform(displays, new Function<ElementDisplay, ElementDisplayModel>() {
            @Nullable
            @Override
            public ElementDisplayModel apply(@Nullable final ElementDisplay elementDisplay) {
                try {
                    return elementDisplay != null ? new ElementDisplayModel(elementDisplay) : null;
                } catch (Exception e) {
                    log.warn("An exception occurred trying to transform the element display \"" + elementDisplay.getElementName() + "\"", e);
                    return null;
                }
            }
        }), Predicates.<ElementDisplayModel>notNull()));

        return new ResponseEntity<>(models, OK);
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

    private Set<String> getAvailableElements() throws XFTInitException {
        if (_availableElements.isEmpty()) {
            synchronized (_availableElements) {
                try {
                    _availableElements.addAll(Lists.transform(GenericWrapperElement.GetAllElements(false), new Function<GenericWrapperElement, String>() {
                        @Override
                        public String apply(final GenericWrapperElement element) {
                            return element.getType().toString();
                        }
                    }));
                } catch (ElementNotFoundException ignored) {
                    //
                }
            }
        }
        return _availableElements;
    }


    private static final List<String> AVAILABLE_ELEMENT_DISPLAYS = Arrays.asList(BROWSEABLE, BROWSEABLE_CREATEABLE, CREATEABLE, SEARCHABLE, SEARCHABLE_BY_DESC, SEARCHABLE_BY_PLURAL_DESC);

    private final Set<String> _availableElements = new ConcurrentSkipListSet<>();

    private final GroupsAndPermissionsCache _cache;
}
