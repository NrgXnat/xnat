/*
 * web: org.nrg.xapi.rest.data.CatalogApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import com.google.common.base.Joiner;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.web.http.ZipStreamingResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.List;
import java.util.Map;

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

    @ApiOperation(value = "Creates a download catalog for the submitted sessions and other data objects.",
                  notes = "The map submitted to this call supports lists of object IDs organized by key type: sessions, "
                          + "scan_type, scan_format, recon, assessors, and resources. The response for this method is "
                          + "the catalog of resolved resources, which can be submitted to the download/{catalog} "
                          + "function to retrieve the files as a zip archive.",
                  response = CatCatalogBean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The download catalog was successfully built."),
                   @ApiResponse(code = 204, message = "No resources were specified."),
                   @ApiResponse(code = 400, message = "Something is wrong with the request format."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 404, message = "The request was valid but one or more of the specified resources was not found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "download", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<CatCatalogBean> createDownloadSessionsCatalog(@ApiParam("The resources to be cataloged.") @RequestBody final Map<String, List<String>> resources) throws InsufficientPrivilegesException, NoContentException {
        final UserI user = getSessionUser();

        if (resources.size() == 0) {
            throw new NoContentException("There were no resources specified in the request.");
        }

        _log.info("User {} requested download catalog for the following resources: {}", resources);
        return new ResponseEntity<>(_service.getCatalogForResources(user, resources), HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a download catalog for the submitted sessions and other data objects.",
                  notes = "The map submitted to this call supports lists of object IDs organized by key type: sessions, "
                          + "scan_type, scan_format, recon, assessors, and resources. The response for this method is "
                          + "the catalog of resolved resources, which can be submitted to the download/{catalog} "
                          + "function to retrieve the files as a zip archive.",
                  response = CatCatalogBean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The download catalog was successfully built."),
                   @ApiResponse(code = 204, message = "No resources were specified."),
                   @ApiResponse(code = 400, message = "Something is wrong with the request format."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 404, message = "The request was valid but one or more of the specified resources was not found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "download/{catalogId}", produces = ZipStreamingResponseBody.MEDIA_TYPE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> downloadSessionCatalog(@ApiParam("The ID of the catalog of resources to be downloaded.") @PathVariable final String catalogId) throws InsufficientPrivilegesException, NoContentException, IOException {
        final UserI user = getSessionUser();

        if (StringUtils.isBlank(catalogId)) {
            throw new NoContentException("There was no catalog specified in the request.");
        }
        // TODO: Need to validate the catalog exists (404 if not), the user has permissions to view all resources (403 if not), if that's cool then proceed.
        _log.info("User {} requested download of the catalog {}", user.getLogin(), catalogId);

        return ResponseEntity.ok()
                             .header("Content-Type", ZipStreamingResponseBody.MEDIA_TYPE)
                             .header("Content-Disposition", "attachment; filename=\"" + user.getLogin() + "-" + catalogId + ".zip\"")
                             .body((StreamingResponseBody) new ZipStreamingResponseBody(_service.getResourcesForCatalog(user, catalogId)));
    }

    private static final Logger _log = LoggerFactory.getLogger(CatalogApi.class);

    private final CatalogService _service;
}
