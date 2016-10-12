/*
 * web: org.nrg.xapi.rest.data.InvestigatorsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import com.google.common.base.Joiner;
import io.swagger.annotations.*;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(description = "XNAT Archive and Resource Management API")
@XapiRestController
@RequestMapping(value = "/archive")
public class CatalogApi extends AbstractXapiRestController {
    @Autowired
    public CatalogApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final CatalogService service) {
        super(userManagementService, roleHolder);
        _service = service;
    }

    @ApiOperation(value = "Refresh the catalog entry for one or more resources.", notes = "The resource should be identified by standard archive-relative paths, such as /archive/experiments/XNAT_E0001 or /archive/projects/XNAT_01/subjects/XNAT_01_01.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The refresh operation(s) were completed successfully."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "catalogs/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> refreshResourceCatalog(@ApiParam("The list of resources to be refreshed.") @RequestBody final List<String> resources) throws ServerException, ClientException {
        return refreshResourceCatalogWithOptions(null, resources);
    }

    @ApiOperation(value = "Refresh the catalog entry for one or more resources, performing only the operations specified.", notes = "The resource should be identified by standard archive-relative paths, such as /archive/experiments/XNAT_E0001 or /archive/projects/XNAT_01/subjects/XNAT_01_01. The available operations are All, Append, Checksum, Delete, and PopulateStats. They should be comma separated but without spaces. Omitting the operations implies All.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The refresh operation(s) were completed successfully."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "catalogs/refresh/{operations}", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> refreshResourceCatalogWithOptions(
            @ApiParam("The operations to be performed") @PathVariable final List<CatalogService.Operation> operations,
            @ApiParam("The list of resources to be refreshed.") @RequestBody final List<String> resources) throws ServerException, ClientException {
        final UserI user = getSessionUser();

        _log.info("User {} requested catalog refresh for the following resources: " + Joiner.on(", ").join(resources));
        if (operations == null) {
            _service.refreshResourceCatalogs(user, resources);
        } else {
            _service.refreshResourceCatalogs(user, resources, operations);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private static final Logger _log = LoggerFactory.getLogger(CatalogApi.class);

    private final CatalogService _service;
}
