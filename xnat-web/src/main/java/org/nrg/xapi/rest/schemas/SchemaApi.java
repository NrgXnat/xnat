/*
 * web: org.nrg.xapi.rest.schemas.SchemaApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.schemas;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xnat.initialization.tasks.InitializeXftElementsTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api(description = "XNAT Data Type Schemas API")
@XapiRestController
@RequestMapping(value = "/schemas")
@Slf4j
public class SchemaApi extends AbstractXapiRestController {
    @Autowired
    public SchemaApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    /**
     * Initializes the various maps that underlie the data-type methods. This should only be called when XFT initialization has completed, optimally
     * by the {@link InitializeXftElementsTask} bean.
     *
     * @throws XFTInitException When an error occurs accessing XFT.
     */
    public void initialize() throws XFTInitException {
        if (_elementNames.isEmpty()) {
            synchronized (_elementNames) {
                try {
                    _elementNames.put("timestamp", Long.toString(Calendar.getInstance().getTimeInMillis()));
                    for (final GenericWrapperElement element : GenericWrapperElement.GetAllElements(false)) {
                        final String       formattedName = element.getFormattedName();
                        final List<String> dataTypeNames = getDataTypeNames(element);

                        _elements.put(formattedName, element);
                        _elementNames.putAll(formattedName, dataTypeNames);
                        _elementTypes.add(dataTypeNames.get(0));
                        _elementNameMappings.put(formattedName, formattedName);
                        for (final String dataTypeName : dataTypeNames) {
                            _elementNameMappings.put(dataTypeName, formattedName);
                        }
                    }
                } catch (ElementNotFoundException ignored) {
                    // Nothing to see here, people, move along.
                }
            }
        }
    }

    @ApiOperation(value = "Returns a list of all of the installed XNAT data-type schemas.", notes = "The strings returned from this function tell you the name of the schema and can be used with other methods on this API to retrieve the full schema document. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<List<String>> getAllDataTypeSchemas() throws IOException {
        final List<String> schemas = new ArrayList<>();
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:schemas/*/*.xsd")) {
            final Set<String> schemaPath = new LinkedHashSet<>(Arrays.asList(FilenameUtils.removeExtension(resource.getURI().toString().replaceAll("^.*/schemas/", "")).split("/")));
            schemas.add(Joiner.on("/").join(schemaPath));
        }
        return new ResponseEntity<>(schemas, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the requested XNAT data-type schema.", notes = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd. This function returns the schema named SCHEMA.xsd in the folder named SCHEMA. You can use the function that allows you to specify the namespace as well if the folder name differs from the schema name. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{schema}", produces = {MediaType.APPLICATION_XML_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getRequestedDataTypeSchema(@PathVariable("schema") final String schema) throws IOException {
        return getRequestedDataTypeSchema(schema, schema);
    }

    @ApiOperation(value = "Returns the requested XNAT data-type schema.", notes = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd, but sometimes the folder name differs from the schema name. This function returns the schema named SCHEMA.xsd in the folder named NAMESPACE. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{namespace}/{schema}", produces = {MediaType.APPLICATION_XML_VALUE}, method = {RequestMethod.GET})
    // TODO: Eventually these should return XML Document objects that are appropriately converted. Spring doesn't have a converter for that by default.
    public ResponseEntity<String> getRequestedDataTypeSchema(@PathVariable("namespace") final String namespace, @PathVariable("schema") final String schema) throws IOException {
        final Resource resource = BasicXnatResourceLocator.getResource("classpath:schemas/" + namespace + "/" + schema + ".xsd");
        if (resource == null || !resource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!resource.isReadable()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try (final InputStream input = resource.getInputStream()) {
            return new ResponseEntity<>(new Scanner(input, "UTF-8").useDelimiter("\\A").next(), HttpStatus.OK);
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
        return ResponseEntity.ok(_elementTypes);
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
        return ResponseEntity.ok(_elementNames);
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
    public ResponseEntity<SetMultimap<String, String>> getSpecifiedElementTypeNames(@ApiParam("The name of the data type to retrieve") @PathVariable final String dataType) throws NotFoundException {
        return ResponseEntity.ok(getElementNames(dataType));
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
    public ResponseEntity<SetMultimap<String, String>> getSpecifiedElementTypeNamesFromJsonPost(@ApiParam("The data types to be retrieved.") @RequestBody final Map<String, Object> attributes) throws NotFoundException {
        final List<String> elementNames = getElementNamesFromTypeAndTypes((List) attributes.get("dataTypes"), (String) attributes.get("dataType"));
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElementNames(elementNames));
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
    public ResponseEntity<SetMultimap<String, String>> getSpecifiedElementTypeNamesFromFormPost(@ApiParam("A list of data types to be retrieved.") @RequestParam(required = false) final List<String> dataTypes, @ApiParam("The data type to be retrieved.") @RequestParam(required = false) final String dataType) throws NotFoundException {
        final List<String> elementNames = getElementNamesFromTypeAndTypes(dataTypes, dataType);
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElementNames(elementNames));
    }

    @ApiOperation(value = "Gets a map of all available data types on the system with the full element definition.",
                  notes = "This can get pretty large.",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of available data types."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements/all", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getAllElements() {
        return ResponseEntity.ok(_elements);
    }

    @ApiOperation(value = "Gets information about the requested data type.",
                  notes = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = GenericWrapperElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements/{dataType}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getSpecifiedElement(@ApiParam("The data type to be retrieved.") @PathVariable final String dataType) throws NotFoundException {
        return ResponseEntity.ok(getElement(dataType));
    }

    @ApiOperation(value = "Gets the requested data types.",
                  notes = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Information on the requested data type."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getSpecifiedElementsFromJsonPost(@ApiParam("The data types to be retrieved.") @RequestBody final Map<String, Object> attributes) throws NotFoundException {
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
    @XapiRequestMapping(value = "datatypes/elements", consumes = {APPLICATION_FORM_URLENCODED_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public ResponseEntity<Map<String, GenericWrapperElement>> getSpecifiedElementsFromFormPost(@ApiParam("A list of data types to be retrieved.") @RequestParam(required = false) final List<String> dataTypes, @ApiParam("The data type to be retrieved.") @RequestParam(required = false) final String dataType) throws NotFoundException {
        final List<String> elementNames = getElementNamesFromTypeAndTypes(dataTypes, dataType);
        if (elementNames.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return ResponseEntity.ok(getElements(elementNames));
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

    private SetMultimap<String, String> getElementNames(final String elementName) throws NotFoundException {
        return getElementNames(Collections.singletonList(elementName));
    }

    private SetMultimap<String, String> getElementNames(final List<String> elementNames) throws NotFoundException {
        final Set<String> resolvedElementNames = resolveElementNames(elementNames);
        return Multimaps.filterKeys(_elementNames, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String elementName) {
                return resolvedElementNames.contains(elementName);
            }
        });
    }

    private Map<String, GenericWrapperElement> getElement(final String elementName) throws NotFoundException {
        return getElements(Collections.singletonList(elementName));
    }

    private Map<String, GenericWrapperElement> getElements(final List<String> elementNames) throws NotFoundException {
        final Set<String> resolvedElementNames = resolveElementNames(elementNames);
        return Maps.filterKeys(_elements, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String elementName) {
                return resolvedElementNames.contains(elementName);
            }
        });
    }

    private String resolveElementName(final String elementName) throws NotFoundException {
        if (_elementNameMappings.containsKey(elementName)) {
            return _elementNameMappings.get(elementName);
        }
        throw new NotFoundException(elementName);
    }

    private Set<String> resolveElementNames(final List<String> elementNames) throws NotFoundException {
        final Set<String> found    = new HashSet<>();
        final Set<String> notFound = new HashSet<>();
        for (final String elementName : elementNames) {
            try {
                found.add(resolveElementName(elementName));
            } catch (NotFoundException e) {
                notFound.add(elementName);
            }
        }

        if (!notFound.isEmpty()) {
            throw new NotFoundException(StringUtils.join(notFound, ", "));
        }

        return found;
    }

    /**
     * Returns different variations of the submitted element's data-type names:
     *
     * <ul>
     * <li><b><i>prefix</i>:<i>name</i></b>, e.g. <code>xnat:mrSessionData</code></li>
     * <li><b><i>prefix</i>:<i>properName</i></b>, e.g. <code>xnat:MRSession</code></li>
     * <li><b><i>uri</i>:<i>name</i></b>, e.g. <code>http://nrg.wustl.edu/xnat:MRSession</code></li>
     * <li><b><i>uri</i>:<i>properName</i></b>, e.g. <code>http://nrg.wustl.edu/xnat:MRSession</code></li>
     * </ul>
     *
     * @param element The data-type element to be rendered.
     *
     * @return The set of names for the data type.
     */
    private List<String> getDataTypeNames(final GenericWrapperElement element) {
        final String prefix     = element.getSchemaTargetNamespacePrefix() + ":";
        final String uri        = element.getSchemaTargetNamespaceURI();
        final String name       = element.getName();
        final String properName = StringUtils.removeStart(element.getProperName(), prefix);
        return Arrays.asList(prefix + name, prefix + properName, uri + ":" + name, uri + ":" + properName);
    }

    /**
     * Contains all elements mapped by the element's formatted name.
     */
    private final Map<String, GenericWrapperElement> _elements            = new ConcurrentSkipListMap<>();
    /**
     * Contains all names for an element mapped by the element's formatted name.
     */
    private final SetMultimap<String, String>        _elementNames        = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.<String, String>create());
    /**
     * Contains all data-type names (i.e. all of the names from all of the lists in the values in {@link #_elementNames})
     * mapped to the corresponding element's formatted name. The values here include the data-type element's formatted name
     * as well as all of the names returned by the {@link #getDataTypeNames(GenericWrapperElement)} method.
     */
    private final Map<String, String>                _elementNameMappings = new ConcurrentSkipListMap<>();
    /**
     * Contains all data types in the standard XNAT schema element format, e.g. <code>xnat:mrSessionData</code>.
     */
    private final Set<String>                        _elementTypes        = new ConcurrentSkipListSet<>();
}
