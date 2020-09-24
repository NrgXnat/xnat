package org.nrg.xnat.snapshot.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Read;

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

import java.nio.file.Paths;

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

    @ApiOperation(value = "Get or generate a single thumbnail snapshot.")
    @ApiResponses({@ApiResponse(code = 200, message = "Snapshot located or generated and returned."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The session or scan requested doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/experiments/{session}/scan/{scanId}/snapshot", "/projects/{project}/experiments/{session}/scan/{scanId}/snapshot", "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/snapshot"},
                        produces = MediaType.IMAGE_GIF_VALUE,
                        method = RequestMethod.GET, restrictTo = Read)
    public ResponseEntity<Resource> generateSnapshot(final @PathVariable(required = false) @Project String project,
                                                     final @PathVariable(required = false) @Subject String subject,
                                                     final @PathVariable @Experiment String session,
                                                     final @PathVariable String scanId) throws NotFoundException, DataFormatException {
        return generateSnapshotView(project, subject, session, scanId, null);
    }

    @ApiOperation(value = "Get or generate a single thumbnail snapshot.")
    @ApiResponses({@ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
                   @ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/experiments/{session}/scan/{scanId}/snapshot/grid/{gridView}", "/projects/{project}/experiments/{session}/scan/{scanId}/snapshot/grid/{gridView}", "/projects/{project}/subjects/{subject}/experiments/{session}/scan/{scanId}/snapshot/grid/{gridView}"},
                        produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE},
                        method = RequestMethod.GET, restrictTo = Read)
    public ResponseEntity<Resource> generateSnapshotView(final @PathVariable(required = false) @Project String project,
                                                         final @PathVariable(required = false) @Subject String subject,
                                                         final @PathVariable @Experiment String session,
                                                         final @PathVariable String scanId,
                                                         final @PathVariable String gridView) throws NotFoundException, DataFormatException {
        final boolean hasGridView = StringUtils.isNotBlank(gridView);
        if (hasGridView) {
            log.debug("Start snapshot generation for project {} subject {} session {} scan {} with {} grid view", StringUtils.defaultIfBlank(project, "none"), StringUtils.defaultIfBlank(subject, "none"), session, scanId, gridView);
        } else {
            log.debug("Start snapshot generation for project {} subject {} session {} scan {}", StringUtils.defaultIfBlank(project, "none"), StringUtils.defaultIfBlank(subject, "none"), session, scanId);
        }

        final String experimentId = getExperimentId(project, subject, session);
        final String imagePath    = hasGridView ? _snapshotService.generateSnapshot(experimentId, scanId, gridView) : _snapshotService.generateSnapshot(experimentId, scanId);
        log.debug("Snapshot image path for scan {} of session {} with grid view {} found at path {}", scanId, experimentId, StringUtils.defaultIfBlank(gridView, "none"), imagePath);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(new FileSystemResource(Paths.get(imagePath).toFile()));
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

    private final SnapshotGenerationService _snapshotService;
    private final XftDataObjectIdResolver   _resolver;
}
