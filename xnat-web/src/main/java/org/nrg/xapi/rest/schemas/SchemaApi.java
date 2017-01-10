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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FilenameUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Api(description = "XNAT Data Type Schemas API")
@XapiRestController
@RequestMapping(value = "/schemas")
public class SchemaApi extends AbstractXapiRestController {
    @Autowired
    public SchemaApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @ApiOperation(value = "Returns a list of all of the installed XNAT data-type schemas.", notes = "The strings returned from this function tell you the name of the schema and can be used with other methods on this API to retrieve the full schema document. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
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
    @RequestMapping(value = "{schema}", produces = {MediaType.APPLICATION_XML_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<String> getRequestedDataTypeSchema(@PathVariable("schema") final String schema) throws IOException, ParserConfigurationException, SAXException {
        return getRequestedDataTypeSchema(schema, schema);
    }

    @ApiOperation(value = "Returns the requested XNAT data-type schema.", notes = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd, but sometimes the folder name differs from the schema name. This function returns the schema named SCHEMA.xsd in the folder named NAMESPACE. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "{namespace}/{schema}", produces = {MediaType.APPLICATION_XML_VALUE}, method = {RequestMethod.GET})
    // TODO: Eventually these should return XML Document objects that are appropriately converted. Spring doesn't have a converter for that by default.
    public ResponseEntity<String> getRequestedDataTypeSchema(@PathVariable("namespace") final String namespace, @PathVariable("schema") final String schema) throws IOException, ParserConfigurationException, SAXException {
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
}
