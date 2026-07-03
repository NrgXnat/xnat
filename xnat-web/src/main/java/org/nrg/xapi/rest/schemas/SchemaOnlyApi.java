/*
 * web: org.nrg.xapi.rest.schemas.SchemaOnlyApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.schemas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "XNAT Data Type Schemas API")
// NOTE: the legacy @Api had hidden=true but springfox never honored it - these endpoints have always appeared
// in the published spec, so @Hidden is deliberately NOT applied to preserve baseline parity.
@XapiRestController
@RequestMapping(value = "/")
@Slf4j
public class SchemaOnlyApi extends AbstractXapiRestController {
    @Autowired
    public SchemaOnlyApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DataTypeSchemaService schemaService) {
        super(userManagementService, roleHolder);
        _schemaService = schemaService;
    }

    @Operation(summary = "Returns the requested XNAT data-type schema.", description = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd. This function returns the schema named SCHEMA.xsd in the folder named SCHEMA. You can use the function that allows you to specify the namespace as well if the folder name differs from the schema name. This tells you nothing about whether the data types defined in the schemas are active or configured.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "404", description = "The requested resource wasn't found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{schema:^[A-z0-9-_.]+\\.xsd$}", produces = APPLICATION_XML_VALUE, method = GET)
    public String getRequestedDataTypeSchema(@PathVariable("schema") final String schema) throws NotFoundException {
        final String document = _schemaService.getSchemaContents(schema);
        if (StringUtils.isBlank(document)) {
            throw new NotFoundException("The requested schema \"" + getSchemaPath(schema) + "\" could not be found on this system");
        }
        return document;
    }

    @Operation(summary = "Returns the requested XNAT data-type schema.", description = "XNAT data-type schemas are most often stored on the classpath in the folder schemas/SCHEMA/SCHEMA.xsd, but sometimes the folder name differs from the schema name. This function returns the schema named SCHEMA.xsd in the folder named NAMESPACE. This tells you nothing about whether the data types defined in the schemas are active or configured.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT data-type schemas successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "404", description = "The requested resource wasn't found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{namespace}/{schema:^[A-z0-9-_.]+\\.xsd$}", produces = APPLICATION_XML_VALUE, method = GET)
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
