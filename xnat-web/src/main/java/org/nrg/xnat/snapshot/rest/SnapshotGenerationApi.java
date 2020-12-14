package org.nrg.xnat.snapshot.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.helpers.resolvers.XftDataObjectIdResolver;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.util.Optional;

import static org.nrg.xdat.security.helpers.AccessLevel.Read;

/**
 * Snapshot generation API.
 *
 */
@Api("Snapshot Generation API")
@XapiRestController
@Slf4j
public class SnapshotGenerationApi extends AbstractXapiRestController {
    @Autowired
    public SnapshotGenerationApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final SnapshotGenerationService snapshotService, final XftDataObjectIdResolver resolver) {
        super(userManagementService, roleHolder);
        _snapshotService = snapshotService;
        _resolver = resolver;
    }

    @ApiOperation(value = "Get a single image or montage of full-size images from the scan. Generate the resource if necessary.")
    @ApiResponses({@ApiResponse(code = 200, message = "Snapshot located or generated and returned."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The session or scan requested doesn't exist."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {
            "/experiments/{session}/scan/{scanId}/snapshot",
            "/projects/{project}/experiments/{session}/scan/{scanId}/snapshot",
            "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/snapshot",
            "/experiments/{session}/scan/{scanId}/snapshot/{view}",
            "/projects/{project}/experiments/{session}/scan/{scanId}/snapshot/{view}",
            "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/snapshot/{view}"},
            produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE},
            method = RequestMethod.GET, restrictTo = Read)
    public ResponseEntity<Resource> getSnapshot(final @PathVariable(required = false) @Project String project,
                                                final @PathVariable(required = false) @Subject String subject,
                                                final @PathVariable @Experiment String session,
                                                final @PathVariable String scanId,
                                                final @PathVariable(required = false) String view) throws Exception {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(new FileSystemResource(getSnapshotFile(project, subject, session, scanId, view)));
    }

    @ApiOperation(value = "Get a single thumbnail or montage of thumbnails. Generate the resource if necessary.")
    @ApiResponses({@ApiResponse(code = 200, message = "Thumbnail located or generated and returned."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The session or scan requested doesn't exist."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {
            "/experiments/{session}/scan/{scanId}/thumbnail",
            "/projects/{project}/experiments/{session}/scan/{scanId}/thumbnail",
            "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/thumbnail",
            "/experiments/{session}/scan/{scanId}/thumbnail/{view}",
            "/projects/{project}/experiments/{session}/scan/{scanId}/thumbnail/{view}",
            "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/thumbnail/{view}"},
            produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE},
            method = RequestMethod.GET, restrictTo = Read)
    public ResponseEntity<Resource> getThumbnail(final @PathVariable(required = false) @Project String project,
                                                 final @PathVariable(required = false) @Subject String subject,
                                                 final @PathVariable @Experiment String session,
                                                 final @PathVariable String scanId,
                                                 final @PathVariable(required = false) String view) throws Exception {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(new FileSystemResource( getThumbnailFile(project, subject, session, scanId, view)));
    }

    private File getSnapshotFile(final String project, final String subject, final String session, final String scanId, final String view) throws Exception {
        GridviewDimensions gridviewDimensions = new GridviewDimensions( view);

        final String sessionId = getExperimentId(project, subject, session);
        if (StringUtils.isBlank(sessionId)) {
            throw new NotFoundException(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, session);
        }

        Optional< FileResource> fileResource = _snapshotService.getSnapshot( session, scanId, gridviewDimensions.rows, gridviewDimensions.cols);

        if( fileResource.isPresent()) {
            File resource = fileResource.get().getFile();
            if (resource != null) {
                log.debug("Snapshot path for scan {} of session {} with grid view {} found at path {}", scanId, sessionId, StringUtils.defaultIfBlank(view, "none"), fileResource.get().getRoot());
                return resource;
            }
        }
        throw new NotFoundException( "snapshot", String.format("SessionId: %s, ScanId: %s", sessionId, scanId));
    }

    private File getThumbnailFile(final String project, final String subject, final String session, final String scanId, final String view) throws Exception {
        GridviewDimensions gridviewDimensions = new GridviewDimensions( view);

        final String sessionId = getExperimentId(project, subject, session);
        if (StringUtils.isBlank(sessionId)) {
            throw new NotFoundException(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, session);
        }

        Optional< FileResource> fileResource = _snapshotService.getThumbnail( session, scanId, gridviewDimensions.rows, gridviewDimensions.cols, 0.5f, 0.5f);

        if( fileResource.isPresent()) {
            File resource = fileResource.get().getFile();
            if (resource != null) {
                log.debug("Thumbnail path for scan {} of session {} with grid view {} found at path {}", scanId, sessionId, StringUtils.defaultIfBlank(view, "none"), fileResource.get().getRoot());
                return resource;
            }
        }
        throw new NotFoundException( "thumbnail", String.format("SessionId: %s, ScanId: %s", sessionId, scanId));
    }

    private String getExperimentId(final String project, final String subject, final String experiment) throws DataFormatException, NotFoundException {
        final XftDataObjectIdResolver.PSEParameters.Builder builder = XftDataObjectIdResolver.PSEParameters.builder();
        if (StringUtils.isNotBlank(project)) {
            builder.project(project);
        }
        if (StringUtils.isNotBlank(subject)) {
            builder.subject(subject);
        }
        builder.experiment(experiment);
        return _resolver.resolve(builder.build());
    }

    private class GridviewDimensions {
        int rows = 1;
        int cols = 1;
        public GridviewDimensions( String s) {
            if( ! StringUtils.isEmpty(s)) {
                String[] tokens = s.toUpperCase().split("X");
                this.cols = Integer.parseInt( tokens[0]);
                this.rows = Integer.parseInt( tokens[1]);
            }
        }
    }

    private static final String THUMBNAIL = "thumbnail";

    private final SnapshotGenerationService _snapshotService;
    private final XftDataObjectIdResolver   _resolver;
}
