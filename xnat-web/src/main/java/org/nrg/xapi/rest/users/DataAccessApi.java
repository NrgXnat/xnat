package org.nrg.xapi.rest.users;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
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
import org.nrg.xnat.initialization.tasks.InitializeXftElementsTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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

    /**
     * Initializes the various maps that underlie the data-type methods. This should only be called when XFT initialization has completed, optimally
     * by the {@link InitializeXftElementsTask} bean.
     *
     * @throws XFTInitException When an error occurs accessing XFT.
     */
    public void initialize() throws XFTInitException {
        if (_elementTypeNames.isEmpty()) {
            synchronized (_elementTypeNames) {
                try {
                    _elementTypeNames.put("timestamp", Long.toString(Calendar.getInstance().getTimeInMillis()));
                    for (final GenericWrapperElement element : GenericWrapperElement.GetAllElements(false)) {
                        final String      formattedName = element.getFormattedName();
                        final Set<String> dataTypeNames = getDataTypeNames(element);

                        _elements.put(formattedName, element);
                        _elementTypes.add(element.getType().toString());
                        _elementTypeNames.putAll(formattedName, dataTypeNames);
                        for (final String dataTypeName : dataTypeNames) {
                            _elements.put(dataTypeName, element);
                        }
                    }
                } catch (ElementNotFoundException ignored) {
                    // Nothing to see here, people, move along.
                }
            }
        }
    }

    @ApiOperation(value = "Gets a list of the available data types on the system, preceded by a timestamp indicating when the list of data types was generated.",
                  notes = "The available data types can be used as parameters for this call in the form /xapi/access/datatypes/{dataType}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).  The first element in this list is a timestamp indicating when the list was generated. This allows clients to check whether the data type list has been updated since the last call to this method.",
                  response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available data types."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Set<String>> getAllElementTypes() {
        return ResponseEntity.ok(getElementTypes());
    }

    @ApiOperation(value = "Gets a map of the available data types on the system along with the various data type element names and types. This map includes a timestamp indicating when the list of data types was generated using the key \"timestamp\".",
                  notes = "The available data types can be used as parameters for this call in the form /xapi/access/datatypes/{dataType}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).  The first element in this list is a timestamp indicating when the list was generated. This allows clients to check whether the data type list has been updated since the last call to this method.",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available data types."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names/all", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<SetMultimap<String, String>> getAllElementTypeNames() {
        return ResponseEntity.ok(getElementTypeNames());
    }

    @ApiOperation(value = "Gets a map of the available data types on the system along with the various data type element names and types. This map includes a timestamp indicating when the list of data types was generated using the key \"timestamp\".",
                  notes = "The available data types can be used as parameters for this call in the form /xapi/access/datatypes/{dataType}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).  The first element in this list is a timestamp indicating when the list was generated. This allows clients to check whether the data type list has been updated since the last call to this method.",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available data types."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names/{dataType}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Set<String>> getSpecifiedElementTypeNames(@ApiParam("The name of the data type to retrieve") @PathVariable final String dataType) {
        return ResponseEntity.ok(getElementTypeNames(dataType));
    }

    @ApiOperation(value = "Gets information about the requested data types.",
                  notes = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = GenericWrapperElement.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<SetMultimap<String, String>> getSpecifiedElementTypeNamesFromJsonPost(@ApiParam("The data types to be retrieved.") @RequestBody final Map<String, Object> attributes) {
        final List<String> elementNames = getElementNamesFromTypeAndTypes((List) attributes.get("dataTypes"), (String) attributes.get("dataType"));
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElementTypeNames(elementNames));
    }

    @ApiOperation(value = "Gets information about the requested data type.",
                  notes = "The available element displays from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = GenericWrapperElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names", consumes = {APPLICATION_FORM_URLENCODED_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<SetMultimap<String, String>> getSpecifiedElementTypeNamesFromFormPost(@ApiParam("The data type to be retrieved.") @RequestParam(required = false) final String dataType, @RequestParam(required = false) final List<String> dataTypes) {
        final List<String> elementNames = getElementNamesFromTypeAndTypes(dataTypes, dataType);
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElementTypeNames(elementNames));
    }

    @ApiOperation(value = "Gets a map of all available data types on the system with the full element definition.",
                  notes = "This can get pretty large.",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available data types."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/full/all", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getAllElements() {
        return ResponseEntity.ok(getElements());
    }

    @ApiOperation(value = "Gets information about the requested data type.",
                  notes = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = GenericWrapperElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/full/{dataType}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<GenericWrapperElement> getSpecifiedElement(@ApiParam("The data type to be retrieved.") @PathVariable final String dataType) throws NotFoundException {
        return ResponseEntity.ok(getElement(dataType));
    }

    @ApiOperation(value = "Gets the requested data types.",
                  notes = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/full", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getSpecifiedElementsFromJsonPost(@ApiParam("The data types to be retrieved.") @RequestBody final Map<String, Object> attributes) {
        final List<String> elementNames = getElementNamesFromTypeAndTypes((List) attributes.get("dataTypes"), (String) attributes.get("dataType"));
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElements(elementNames));
    }

    @ApiOperation(value = "Gets information about the requested data type.",
                  notes = "The available element displays from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = GenericWrapperElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/full", consumes = {APPLICATION_FORM_URLENCODED_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getSpecifiedElementsFromFormPost(@ApiParam("The data type to be retrieved.") @RequestParam(required = false) final String dataType, @RequestParam(required = false) final List<String> dataTypes) {
        final List<String> elementNames = getElementNamesFromTypeAndTypes(dataTypes, dataType);
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElements(elementNames));
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
        return new ResponseEntity<>(_cache.getLastUpdateTime(getSessionUser()), OK);
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

    private List<String> getElementNamesFromTypeAndTypes(final List dataTypes, final String dataType) {
        final List<String> allDataTypes = new ArrayList<>();
        if (dataTypes != null) {
            for (final Object object : dataTypes) {
                if (object == null) {
                    continue;
                }
                allDataTypes.add(object.toString());
            }
        }
        if (StringUtils.isNotBlank(dataType)) {
            allDataTypes.add(dataType);
        }
        return allDataTypes;
    }

    private Set<String> getElementTypes() {
        return _elementTypes;
    }

    private SetMultimap<String, String> getElementTypeNames() {
        return _elementTypeNames;
    }

    private Set<String> getElementTypeNames(final String elementName) {
        final String key = resolveElementName(elementName);
        return key == null ? Collections.<String>emptySet() : _elementTypeNames.get(key);
    }

    private SetMultimap<String, String> getElementTypeNames(final List<String> elementNames) {
        return Multimaps.filterKeys(_elementTypeNames, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String elementName) {
                final String key = resolveElementName(elementName);
                return StringUtils.isNotBlank(key) && elementNames.contains(key);
            }
        });
    }

    private Map<String, GenericWrapperElement> getElements() {
        return _elements;
    }

    private GenericWrapperElement getElement(final String elementName) throws NotFoundException {
        final String key = resolveElementName(elementName);
        if (key == null) {
            throw new NotFoundException(elementName);
        }
        return _elements.get(elementName);
    }

    private Map<String, GenericWrapperElement> getElements(final List<String> elementNames) {
        return Maps.filterKeys(_elements, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String elementName) {
                final String key = resolveElementName(elementName);
                return StringUtils.isNotBlank(key) && elementNames.contains(key);
            }
        });
    }

    private String resolveElementName(final String elementName) {
        if (StringUtils.isBlank(elementName)) {
            return null;
        }
        return _elementTypeNames.containsKey(elementName) ? elementName : _elements.containsKey(elementName) ? _elements.get(elementName).getFormattedName() : null;
    }

    private Set<String> getDataTypeNames(final GenericWrapperElement element) {
        final String name       = element.getName();
        final String prefix     = element.getSchemaTargetNamespacePrefix() + ":";
        final String properName = StringUtils.removeStart(element.getProperName(), prefix);
        final String uri        = element.getSchemaTargetNamespaceURI();
        return Sets.newHashSet(prefix + ":" + name, prefix + ":" + properName, uri + ":" + name, uri + ":" + properName);
    }

    private static final List<String> AVAILABLE_ELEMENT_DISPLAYS = Arrays.asList(BROWSEABLE, BROWSEABLE_CREATEABLE, CREATEABLE, SEARCHABLE, SEARCHABLE_BY_DESC, SEARCHABLE_BY_PLURAL_DESC);

    private final Map<String, GenericWrapperElement> _elements         = new ConcurrentSkipListMap<>();
    private final Set<String>                        _elementTypes     = new ConcurrentSkipListSet<>();
    private final SetMultimap<String, String>        _elementTypeNames = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.<String, String>create());

    private final GroupsAndPermissionsCache _cache;
}
