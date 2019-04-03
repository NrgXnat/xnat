/*
 * web: org.nrg.xapi.rest.schemas.SchemaApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.schemas;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.schema.DataTypeSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.nrg.xft.schema.impl.DefaultDataTypeSchemaService.getSchemaPath;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Api("XNAT Data Type Schemas API")
@XapiRestController
@RequestMapping(value = "/")
@Slf4j
public class SchemaOnlyApi extends AbstractXapiRestController {
    @Autowired
    public SchemaOnlyApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DataTypeSchemaService schemaService) {
        super(userManagementService, roleHolder);
        _schemaService = schemaService;
    }

    @ApiOperation(value = "Returns the requested XNAT data-type schema.", notes = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd. This function returns the schema named SCHEMA.xsd in the folder named SCHEMA. You can use the function that allows you to specify the namespace as well if the folder name differs from the schema name. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{schema}", method = GET, produces = APPLICATION_XML_VALUE)
    public String getRequestedDataTypeSchema(@PathVariable("schema") final String schema) throws NotFoundException {
        return getRequestedDataTypeSchema(StringUtils.removeEnd(schema, ".xsd"), schema);
    }

    @ApiOperation(value = "Returns the requested XNAT data-type schema.", notes = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd, but sometimes the folder name differs from the schema name. This function returns the schema named SCHEMA.xsd in the folder named NAMESPACE. This tells you nothing about whether the data types defined in the schemas are active or configured.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{namespace}/{schema}", produces = APPLICATION_XML_VALUE, method = GET)
    // TODO: Eventually these should return XML Document objects that are appropriately converted. Spring doesn't have a converter for that by default.
    public String getRequestedDataTypeSchema(@PathVariable("namespace") final String namespace, @PathVariable("schema") final String schema) throws NotFoundException {
        final String document = _schemaService.getSchemaContents(namespace, schema);
        if (StringUtils.isBlank(document)) {
            throw new NotFoundException("The requested schema \"" + getSchemaPath(namespace, schema) + "\" could not be found on this system");
        }
        return document;
    }

    private final DataTypeSchemaService _schemaService;
}
