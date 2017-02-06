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
import org.nrg.xapi.exceptions.NotFoundException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
                          + "the ID for the catalog of resolved resources, which can be submitted to the download/{catalog} "
                          + "function to retrieve the catalog or to the download/{catalog}/zip function to retrieve the"
                          + "files in the catalog as a zip archive.",
                  response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The download catalog was successfully built."),
                   @ApiResponse(code = 204, message = "No resources were specified."),
                   @ApiResponse(code = 400, message = "Something is wrong with the request format."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 404, message = "The request was valid but one or more of the specified resources was not found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "download", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> createDownloadSessionsCatalog(@ApiParam("The resources to be cataloged.") @RequestBody final Map<String, List<String>> resources) throws InsufficientPrivilegesException, NoContentException {
        final UserI user = getSessionUser();

        if (resources.size() == 0) {
            throw new NoContentException("There were no resources specified in the request.");
        }

        _log.info("User {} requested download catalog for the following resources: {}", resources);
        return new ResponseEntity<>(_service.buildCatalogForResources(user, resources), HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieves the download catalog for the submitted catalog ID.",
                  notes = "This retrieves a catalog created earlier by the catalog service.",
                  response = CatCatalogBean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The download catalog was successfully built."),
                   @ApiResponse(code = 204, message = "No resources were specified."),
                   @ApiResponse(code = 400, message = "Something is wrong with the request format."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 404, message = "The request was valid but one or more of the specified resources was not found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "download/{catalogId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<CatCatalogBean> getDownloadSessionsCatalog(@ApiParam("The resources to be cataloged.") @PathVariable final String catalogId) throws InsufficientPrivilegesException, NoContentException, NotFoundException {
        final UserI user = getSessionUser();

        if (StringUtils.isBlank(catalogId)) {
            throw new NoContentException("There was no catalog specified in the request.");
        }

        _log.info("User {} requested download catalog: {}", catalogId);
        final CatCatalogBean catalog = _service.getCatalogForResources(user, catalogId);
        if (catalog == null) {
            throw new NotFoundException("No catalog with ID " + catalogId + " was found.");
        }
        return new ResponseEntity<>(catalog, HttpStatus.OK);
    }

    @ApiOperation(value = "Downloads the specified catalog as an XML file.",
                  response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The requested catalog was successfully downloaded."),
                   @ApiResponse(code = 204, message = "No catalog was specified."),
                   @ApiResponse(code = 400, message = "Something is wrong with the request format."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access the specified catalog."),
                   @ApiResponse(code = 404, message = "The request was valid but the specified catalog was not found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "download/{catalogId}/xml", produces = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> downloadSessionCatalogXml(@ApiParam("The ID of the catalog to be downloaded.") @PathVariable final String catalogId) throws InsufficientPrivilegesException, NoContentException, NotFoundException, IOException {
        final UserI user = getSessionUser();

        if (StringUtils.isBlank(catalogId)) {
            throw new NoContentException("There was no catalog specified in the request.");
        }

        _log.info("User {} requested download catalog: {}", catalogId);
        final CatCatalogBean catalog = _service.getCatalogForResources(user, catalogId);
        if (catalog == null) {
            throw new NotFoundException("No catalog with ID " + catalogId + " was found.");
        }
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                             .header(HttpHeaders.CONTENT_DISPOSITION, getAttachmentDisposition(catalogId, "xml"))
                             .header(HttpHeaders.CONTENT_LENGTH, Long.toString(_service.getCatalogSize(user, catalogId)))
                             .body((StreamingResponseBody) new StreamingResponseBody() {
                                 @Override
                                 public void writeTo(final OutputStream outputStream) throws IOException {
                                     try (final OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                                         catalog.toXML(writer, true);
                                     }
                                 }
                             });
    }

    @ApiOperation(value = "Downloads the contents of the specified catalog as a zip archive.",
                  response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The requested resources were successfully downloaded."),
                   @ApiResponse(code = 204, message = "No resources were specified."),
                   @ApiResponse(code = 400, message = "Something is wrong with the request format."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 404, message = "The request was valid but one or more of the specified resources was not found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(value = "download/{catalogId}/zip", produces = ZipStreamingResponseBody.MEDIA_TYPE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> downloadSessionCatalogZip(@ApiParam("The ID of the catalog of resources to be downloaded.") @PathVariable final String catalogId) throws InsufficientPrivilegesException, NoContentException, IOException {
        final UserI user = getSessionUser();

        if (StringUtils.isBlank(catalogId)) {
            throw new NoContentException("There was no catalog specified in the request.");
        }
        // TODO: Need to validate the catalog exists (404 if not), the user has permissions to view all resources (403 if not), if that's cool then proceed.
        _log.info("User {} requested download of the catalog {}", user.getLogin(), catalogId);

        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_TYPE, ZipStreamingResponseBody.MEDIA_TYPE)
                             .header(HttpHeaders.CONTENT_DISPOSITION, getAttachmentDisposition(catalogId, "zip"))
                             .body((StreamingResponseBody) new ZipStreamingResponseBody(_service.getResourcesForCatalog(user, catalogId)));
    }

    private static String getAttachmentDisposition(final String name, final String extension) {
        return String.format(ATTACHMENT_DISPOSITION, name, extension);
    }

    private static final String ATTACHMENT_DISPOSITION = "attachment; filename=\"%s.%s\"";

    private static final Logger _log = LoggerFactory.getLogger(CatalogApi.class);

    private final CatalogService _service;
}
