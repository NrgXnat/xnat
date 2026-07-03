/*
 * web: org.nrg.xapi.rest.schemas.SchemaApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.schemas;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.SanitizeUtils;
import org.nrg.xapi.authorization.GuestUserAccessXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.nrg.xnat.initialization.tasks.InitializeXftElementsTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag(name = "XNAT Data Type Schemas API")
@XapiRestController
@RequestMapping(value = "/schemas")
@Slf4j
public class SchemaApi extends AbstractXapiRestController {
    @Autowired
    public SchemaApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @Autowired(required = false)
    public void setDbBackedSchemaService(final DBBackedSchemaService dbBackedSchemaService) {
        _dbBackedSchemaService = dbBackedSchemaService;
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
                        _elementTypes.add(dataTypeNames.getFirst());
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

    @Operation(summary = "Returns a list of all of the installed XNAT data-type schemas.", description = "The strings returned from this function tell you the name of the schema and can be used with other methods on this API to retrieve the full schema document. This tells you nothing about whether the data types defined in the schemas are active or configured.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public List<String> getAllDataTypeSchemas() throws InitializationException {
        try {
            final List<String> schemas = new ArrayList<>();
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:schemas/*/*.xsd")) {
                try {
                    schemas.add(StringUtils.join(new LinkedHashSet<>(Arrays.asList(FilenameUtils.removeExtension(resource.getURI().toString().replaceAll("^.*/schemas/", "")).split("/"))), "/"));
                } catch (IOException e) {
                    log.error("Couldn't get a URI for the resource \"{}\", skipping", resource);
                }
            }
            if (_dbBackedSchemaService != null) {
                for (final DBBackedSchema dbSchema : _dbBackedSchemaService.findAllSchema()) {
                    final String path = StringUtils.stripStart(dbSchema.getPath(), "/");
                    if (StringUtils.isNotBlank(path) && !schemas.contains(path)) {
                        schemas.add(path);
                    }
                }
            }
            return schemas;
        } catch (IOException e) {
            throw new InitializationException("Couldn't retrieve schema resources from the classpath using pattern: \"classpath*:schemas/*/*.xsd\"");
        }
    }

    @Operation(summary = "Returns the requested XNAT data-type schema.", description = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd. This function returns the schema named SCHEMA.xsd in the folder named SCHEMA. You can use the function that allows you to specify the namespace as well if the folder name differs from the schema name. This tells you nothing about whether the data types defined in the schemas are active or configured.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "404", description = "The requested resource wasn't found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{schema}", produces = {MediaType.APPLICATION_XML_VALUE}, method = {RequestMethod.GET})
    public String getRequestedDataTypeSchema(@PathVariable("schema") final String schema) throws IOException, NotFoundException, InsufficientPrivilegesException {
        return getRequestedDataTypeSchema(schema, schema);
    }

    @Operation(summary = "Returns the requested XNAT data-type schema.", description = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd, but sometimes the folder name differs from the schema name. This function returns the schema named SCHEMA.xsd in the folder named NAMESPACE. This tells you nothing about whether the data types defined in the schemas are active or configured.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "404", description = "The requested resource wasn't found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{namespace}/{schema}", produces = {MediaType.APPLICATION_XML_VALUE}, method = {RequestMethod.GET})
    // TODO: Eventually these should return XML Document objects that are appropriately converted. Spring doesn't have a converter for that by default.
    public String getRequestedDataTypeSchema(@PathVariable("namespace") final String namespace, @PathVariable("schema") final String schema) throws IOException, NotFoundException, InsufficientPrivilegesException {
        if (SanitizeUtils.containsPathTraversal(namespace) || SanitizeUtils.containsPathTraversal(schema)) {
            throw new IllegalArgumentException("Invalid namespace or schema specification");
        }
        log.debug("Schema request for namespace={}, schema={}", namespace, schema);
        final Resource resource = getResource(namespace, schema);
        if (resource != null && resource.exists()) {
            log.debug("Found classpath resource for namespace={}, schema={}: {}", namespace, schema, resource);
            if (!resource.isReadable()) {
                throw new InsufficientPrivilegesException("classpath:schemas/" + namespace + "/" + schema + ".xsd");
            }
            try (final InputStream input = resource.getInputStream()) {
                return new Scanner(input, "UTF-8").useDelimiter("\\A").next();
            }
        }
        log.debug("No classpath resource found for namespace={}, schema={}, trying DB-backed schema lookup", namespace, schema);
        final String dbContent = getSchemaContentFromDB(namespace, schema);
        if (dbContent != null) {
            log.debug("Found DB-backed schema for namespace={}, schema={} ({} chars)", namespace, schema, dbContent.length());
            return dbContent;
        }
        log.warn("Schema not found for namespace={}, schema={} (checked classpath and DB)", namespace, schema);
        throw new NotFoundException("classpath:schemas/" + namespace + "/" + schema + ".xsd");
    }

    private Resource getResource(String namespace, String schema) {
        return BasicXnatResourceLocator.getResource("classpath:schemas/" + SanitizeUtils.sanitizeFilePath(namespace) + "/" + SanitizeUtils.sanitizeFilePath(schema) + ".xsd");
    }

    @Operation(summary = "Gets a list of the available data types on the system.",
                  description = "The available data types can be used as parameters for this call in the form /xapi/access/datatypes/{dataType}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of available data types."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public Set<String> getAllElementTypes() {
        return _elementTypes;
    }

    @Operation(summary = "Gets a map of the available data types on the system along with the various data type element names and types. This map includes a timestamp indicating when the list of data types was generated using the key \"timestamp\".",
                  description = "The available data types (i.e. the keys in the returned map) can be used as parameters for this call in the form /xapi/access/datatypes/names/{dataType}. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs). The timestamp element in this list indicates when the list was generated. This allows clients to check whether the data type list has been updated since the last call to this method.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of available data types."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names/all", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public SetMultimap<String, String> getAllElementTypeNames() {
        return _elementNames;
    }

    @Operation(summary = "Gets the element names and types for the specified data type.",
                  description = "The available data types that can be used as parameters for this call can be retrieved by calling /xapi/access/datatypes/names/all. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The element names and types for the specified data type."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the specified data type."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names/{dataType}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public SetMultimap<String, String> getSpecifiedElementTypeNames(@Parameter(description = "The name of the data type to retrieve") @PathVariable final String dataType) throws NotFoundException {
        return getElementNames(dataType);
    }

    @Operation(summary = "Gets information about the requested data types.",
                  description = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The element names and types for the specified data types."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the specified data types."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public SetMultimap<String, String> getSpecifiedElementTypeNamesFromJsonPost(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The data types to be retrieved.") @RequestBody final Map<String, Object> attributes) throws NotFoundException, DataFormatException {
        return getDataTypeMaps(getElementNamesFromTypeAndTypes(((List<?>) attributes.get("dataTypes")).stream().map(String.class::cast).collect(Collectors.toList()), (String) attributes.get("dataType")));
    }

    @Operation(summary = "Gets information about the requested data type.",
                  description = "The available element displays from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Information on the requested data type."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/names", consumes = {APPLICATION_FORM_URLENCODED_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public SetMultimap<String, String> getSpecifiedElementTypeNamesFromFormPost(@Parameter(description = "A list of data types to be retrieved.") @RequestParam(required = false) final List<String> dataTypes, @Parameter(description = "The data type to be retrieved.") @RequestParam(required = false) final String dataType) throws NotFoundException, DataFormatException {
        return getDataTypeMaps(getElementNamesFromTypeAndTypes(dataTypes, dataType));
    }

    @Operation(summary = "Gets a map of all available data types on the system with the full element definition.",
                  description = "This can get pretty large.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of available data types."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the list of available data types."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements/all", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public Map<String, GenericWrapperElement> getAllElements() {
        return _elements;
    }

    @Operation(summary = "Gets information about the requested data type.",
                  description = "The available data types from the call /xapi/access/datatypes/elements/all can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Information on the requested data type."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements/{dataType}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public Map<String, GenericWrapperElement> getSpecifiedElement(@Parameter(description = "The data type to be retrieved.") @PathVariable final String dataType) throws NotFoundException {
        return getElement(dataType);
    }

    @Operation(summary = "Gets the requested data types.",
                  description = "The available data types from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Information on the requested data type."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public Map<String, GenericWrapperElement> getSpecifiedElementsFromJsonPost(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The data types to be retrieved.") @RequestBody final Map<String, Object> attributes) throws NotFoundException, DataFormatException {
        return getElementMaps(getElementNamesFromTypeAndTypes(((List<?>) attributes.get("dataTypes")).stream().map(String.class::cast).collect(Collectors.toList()), (String) attributes.get("dataType")));
    }

    @Operation(summary = "Gets information about the requested data type.",
                  description = "The available element displays from the call /xapi/access/datatypes can be used as the data type parameter for this call. This call is accessible to guest users when the site preference require login is set to false (i.e. open XNATs).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Information on the requested data type."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the available data type."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/elements", consumes = {APPLICATION_FORM_URLENCODED_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public Map<String, GenericWrapperElement> getSpecifiedElementsFromFormPost(@Parameter(description = "A list of data types to be retrieved.") @RequestParam(required = false) final List<String> dataTypes, @Parameter(description = "The data type to be retrieved.") @RequestParam(required = false) final String dataType) throws NotFoundException, DataFormatException {
        return getElementMaps(getElementNamesFromTypeAndTypes(dataTypes, dataType));
    }

    @Operation(summary = "Gets the image session datatypes which are searchable within the system.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The list of searchable image session datatypes which are available from the system."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "You do not have sufficient permissions to access the searchable datatypes."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "datatypes/searchable", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(GuestUserAccessXapiAuthorization.class)
    @ResponseBody
    public List<String> getListOfSearchableDatatypes() throws Exception {
        List<ElementSecurity> elementSecurities = ElementSecurity.GetSecureElements().stream().filter(x -> x.isSearchable()).collect(Collectors.toList());
        List<String> results = new ArrayList<>();
        for (ElementSecurity elementSecurity : elementSecurities) {
            try {
                String elementName = elementSecurity.getElementName();
                List<Object> parentElements = SchemaElement.GetElement(elementName).getGenericXFTElement().getExtendedElements()
                        .stream()
                        .map(x -> x.get(PARENT_ELEMENT_INDEX))
                        .collect(Collectors.toList());
                if (parentElements.contains(elementName+"/imageSessionData")) {
                    results.add(elementName);
                }
            } catch (ElementNotFoundException ignored) {

            }
        }
        return results;
    }

    @Nonnull
    private List<String> getElementNamesFromTypeAndTypes(final List<String> dataTypes, final String dataType) {
        final List<String> allDataTypes = new ArrayList<>(dataTypes);
        if (StringUtils.isNotBlank(dataType)) {
            allDataTypes.add(dataType);
        }
        return allDataTypes;
    }

    @Nonnull
    private Map<String, GenericWrapperElement> getElementMaps(final List<String> elementNames) throws DataFormatException, NotFoundException {
        if (elementNames.isEmpty()) {
            throw new DataFormatException("No data-type names found on the request: must specify one of \"dataType\" or \"dataTypes\"");
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return getElements(elementNames);
    }

    @Nonnull
    private SetMultimap<String, String> getDataTypeMaps(final List<String> elementNames) throws DataFormatException, NotFoundException {
        if (elementNames.isEmpty()) {
            throw new DataFormatException("No data-type names found on the request: must specify one of \"dataType\" or \"dataTypes\"");
        }
        log.info("Getting the data types {}", StringUtils.join(elementNames, ", "));
        return getElementNames(elementNames);
    }

    private SetMultimap<String, String> getElementNames(final String elementName) throws NotFoundException {
        return getElementNames(Collections.singletonList(elementName));
    }

    private SetMultimap<String, String> getElementNames(final List<String> elementNames) throws NotFoundException {
        final Set<String> resolvedElementNames = resolveElementNames(elementNames);
        return Multimaps.filterKeys(_elementNames, resolvedElementNames::contains);
    }

    private Map<String, GenericWrapperElement> getElement(final String elementName) throws NotFoundException {
        return getElements(Collections.singletonList(elementName));
    }

    private Map<String, GenericWrapperElement> getElements(final List<String> elementNames) throws NotFoundException {
        final Set<String> resolvedElementNames = resolveElementNames(elementNames);
        return _elements.keySet().stream().filter(resolvedElementNames::contains).collect(Collectors.toMap(Function.identity(), _elements::get));
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
        final String uri        = element.getSchemaTargetNamespaceURI();
        final String prefix     = element.getSchemaTargetNamespacePrefix();
        final String altPrefix  = StringUtils.substringAfterLast(uri, "/");
        final String name       = element.getName();
        final String properName = StringUtils.removeStart(element.getProperName(), prefix + ":");
        return Arrays.asList(
                prefix + ":" + name,
                prefix + "_" + name,
                prefix + ":" + properName,
                prefix + "_" + properName,
                altPrefix + ":" + name,
                altPrefix + "_" + name,
                altPrefix + ":" + properName,
                altPrefix + "_" + properName,
                uri + ":" + name,
                uri + ":" + properName
        );
    }

    @Nullable
    private String getSchemaContentFromDB(final String namespace, final String schema) {
        if (_dbBackedSchemaService == null) {
            log.debug("DBBackedSchemaService not available, skipping DB lookup for namespace={}, schema={}", namespace, schema);
            return null;
        }
        final String baseName = StringUtils.removeEnd(schema, ".xsd");
        for (final String candidatePath : new String[]{
                "/" + namespace + "/" + baseName,
                "/" + namespace + "/" + schema,
                "/" + baseName
        }) {
            log.debug("Trying DB schema lookup by path: {}", candidatePath);
            final DBBackedSchema dbSchema = _dbBackedSchemaService.findConfigByPath(candidatePath);
            if (dbSchema != null && StringUtils.isNotBlank(dbSchema.getContent())) {
                log.debug("Found DB-backed schema by path={} (name={}, id={})", candidatePath, dbSchema.getName(), dbSchema.getId());
                return dbSchema.getContent();
            }
        }
        // Also try lookup by schema name (e.g., "prefix:complexType") since
        // GetAllSchemaLocations uses the XFTDataModel fileName as the URL path
        // segment, which is the schema name for DB-backed schemas.
        for (final String candidateName : new String[]{namespace, baseName, schema}) {
            log.debug("Trying DB schema lookup by name: {}", candidateName);
            final DBBackedSchema dbSchema = _dbBackedSchemaService.findConfigByName(candidateName);
            if (dbSchema != null && StringUtils.isNotBlank(dbSchema.getContent())) {
                log.debug("Found DB-backed schema by name={} (path={}, id={})", candidateName, dbSchema.getPath(), dbSchema.getId());
                return dbSchema.getContent();
            }
        }
        log.debug("No DB-backed schema found for namespace={}, schema={}", namespace, schema);
        return null;
    }

    private DBBackedSchemaService _dbBackedSchemaService;

    /**
     * Contains all elements mapped by the element's formatted name.
     */
    private final Map<String, GenericWrapperElement> _elements            = new ConcurrentSkipListMap<>();
    /**
     * Contains all names for an element mapped by the element's formatted name.
     */
    private final SetMultimap<String, String>        _elementNames        = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.create());
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

    private static final int PARENT_ELEMENT_INDEX = 1;
}
