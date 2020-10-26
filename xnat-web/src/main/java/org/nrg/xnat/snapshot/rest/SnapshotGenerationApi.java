package org.nrg.xnat.snapshot.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Read;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.helpers.resolvers.XftDataObjectIdResolver;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;

/**
 * @author pradeep.d
 */
@Api("XNAT 1.7.7 Snapshot Generation Plugin API")
@XapiRestController
@Slf4j
public class SnapshotGenerationApi extends AbstractXapiRestController {
    @Autowired
    public SnapshotGenerationApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final SnapshotGenerationService snapshotService, final XftDataObjectIdResolver resolver) {
        super(userManagementService, roleHolder);
        _snapshotService = snapshotService;
        _resolver = resolver;
    }

    @ApiOperation(value = "Get or generate a single snapshot.")
    @ApiResponses({@ApiResponse(code = 200, message = "Snapshot located or generated and returned."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The session or scan requested doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/experiments/{session}/scan/{scanId}/snapshot", "/projects/{project}/experiments/{session}/scan/{scanId}/snapshot", "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/snapshot",
                                 "/experiments/{session}/scan/{scanId}/snapshot/{view}", "/projects/{project}/experiments/{session}/scan/{scanId}/snapshot/{view}", "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/snapshot/{view}"},
                        produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE},
                        method = RequestMethod.GET, restrictTo = Read)
    public ResponseEntity<Resource> getSnapshot(final @PathVariable(required = false) @Project String project,
                                                final @PathVariable(required = false) @Subject String subject,
                                                final @PathVariable @Experiment String session,
                                                final @PathVariable String scanId,
                                                final @PathVariable(required = false) String view) throws NotFoundException, DataFormatException, InitializationException {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(new FileSystemResource(getSnapshotFile(project, subject, session, scanId, view)));
    }

    private File getSnapshotFile(final String project, final String subject, final String session, final String scanId, final String view) throws DataFormatException, NotFoundException, InitializationException {
        final boolean isThumbnail     = StringUtils.equalsIgnoreCase(view, THUMBNAIL);
        final boolean isThumbnailView = isThumbnail || StringUtils.endsWith(view, "_t");
        final boolean isGridView      = StringUtils.isNotBlank(view) && !isThumbnail;
        final String  gridView;
        if (isGridView) {
            gridView = isThumbnailView ? StringUtils.removeEnd(view, "_t") : view;
            log.debug("Getting snapshot for project {} subject {} session {} scan {} with {} grid view", StringUtils.defaultIfBlank(project, "none"), StringUtils.defaultIfBlank(subject, "none"), session, scanId, view);
        } else {
            gridView = null;
            log.debug("Getting snapshot for project {} subject {} session {} scan {}", StringUtils.defaultIfBlank(project, "none"), StringUtils.defaultIfBlank(subject, "none"), session, scanId);
        }

        final String sessionId = getExperimentId(project, subject, session);
        if (StringUtils.isBlank(sessionId)) {
            throw new NotFoundException(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, session);
        }

        final Pair<File, File> images = isGridView ? _snapshotService.getSnapshot(sessionId, scanId, gridView) : _snapshotService.getSnapshot(sessionId, scanId);
        if (images.equals(ImmutablePair.nullPair())) {
            throw new InitializationException("Something went wrong trying to retrieve snapshots for session ID {}: no exception was thrown but no valid files were returned by the snapshot service");
        }

        log.debug("Snapshot image path for scan {} of session {} with grid view {} found at path {}", scanId, sessionId, StringUtils.defaultIfBlank(view, "none"), images.getKey().getParent());
        return isThumbnailView ? images.getValue() : images.getKey();
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

    private static final String THUMBNAIL = "thumbnail";

    private final SnapshotGenerationService _snapshotService;
    private final XftDataObjectIdResolver   _resolver;
}
