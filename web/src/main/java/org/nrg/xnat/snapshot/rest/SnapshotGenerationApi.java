package org.nrg.xnat.snapshot.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Read;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Snapshot generation API.
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
                        produces = MediaType.IMAGE_GIF_VALUE,
                        method = RequestMethod.GET, restrictTo = Read)
    public Resource getSnapshot(@ApiParam("Specifies the project that contains the image session") final @PathVariable(required = false) @Project String project,
                                @ApiParam("Specifies the subject associated with the image session") final @PathVariable(required = false) @Subject String subject,
                                @ApiParam("Specifies the ID or label for the image session (if this is the label, you must also specify the project at least)") final @PathVariable @Experiment String session,
                                @ApiParam("The scan for which the thumbnail should be generated") final @PathVariable String scanId,
                                @ApiParam("Specifies the grid layout for montage views; must be in the format *numRows*X*numColumns*, e.g. 3X3, 5X2, etc.") final @PathVariable(required = false) String view) throws DataFormatException, NotFoundException, InitializationException {
        try {
            return new FileSystemResource(getSnapshotFile(project, subject, session, scanId, view));
        } catch (IOException e) {
            throw new InitializationException(e);
        }
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
                        produces = MediaType.IMAGE_GIF_VALUE,
                        method = RequestMethod.GET, restrictTo = Read)
    public Resource getThumbnail(@ApiParam("Specifies the project that contains the image session") final @PathVariable(required = false) @Project String project,
                                 @ApiParam("Specifies the subject associated with the image session") final @PathVariable(required = false) @Subject String subject,
                                 @ApiParam("Specifies the ID or label for the image session (if this is the label, you must also specify the project at least)") final @PathVariable @Experiment String session,
                                 @ApiParam("The scan for which the thumbnail should be generated") final @PathVariable String scanId,
                                 @ApiParam("Specifies the grid layout for montage views; must be in the format *numRows*X*numColumns*, e.g. 3X3, 5X2, etc.") final @PathVariable(required = false) String view) throws DataFormatException, NotFoundException, InitializationException {
        try {
            return new FileSystemResource(getThumbnailFile(project, subject, session, scanId, view));
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    private File getSnapshotFile(final String project, final String subject, final String session, final String scanId, final String view) throws DataFormatException, NotFoundException, IOException, InitializationException {
        final String sessionId = getExperimentId(project, subject, session);
        if (StringUtils.isBlank(sessionId)) {
            throw new NotFoundException(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, session);
        }

        final GridviewDimensions     dimensions   = new GridviewDimensions(view);
        final Optional<FileResource> fileResource = _snapshotService.getSnapshot(session, scanId, dimensions.rows, dimensions.columns);

        final File resource = fileResource.orElseThrow(() -> new NotFoundException("snapshot", String.format("SessionId: %s, ScanId: %s", sessionId, scanId))).getFile();
        log.debug("Snapshot path for scan {} of session {} with grid view {} found at path {}", scanId, sessionId, StringUtils.defaultIfBlank(view, "none"), fileResource.get().getRoot());
        return resource;
    }

    private File getThumbnailFile(final String project, final String subject, final String session, final String scanId, final String view) throws DataFormatException, NotFoundException, IOException, InitializationException {
        final String sessionId = getExperimentId(project, subject, session);
        if (StringUtils.isBlank(sessionId)) {
            throw new NotFoundException(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, session);
        }

        final GridviewDimensions     dimensions   = new GridviewDimensions(view);
        final Optional<FileResource> fileResource = _snapshotService.getThumbnail(session, scanId, dimensions.rows, dimensions.columns, 0.5f, 0.5f);

        File resource = fileResource.orElseThrow(() -> new NotFoundException("thumbnail", String.format("SessionId: %s, ScanId: %s", sessionId, scanId))).getFile();
        log.debug("Thumbnail path for scan {} of session {} with grid view {} found at path {}", scanId, sessionId, StringUtils.defaultIfBlank(view, "none"), fileResource.get().getRoot());
        return resource;
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

    private static class GridviewDimensions {
        public GridviewDimensions(final String dimensions) {
            if (!StringUtils.isBlank(dimensions)) {
                final Matcher matcher = DIMENSIONS.matcher(dimensions);
                if (matcher.matches()) {
                    columns = Integer.parseInt(matcher.group("x"));
                    rows = Integer.parseInt(matcher.group("y"));
                } else {
                    rows = 1;
                    columns = 1;
                }
            } else {
                rows = 1;
                columns = 1;
            }
        }

        private final int rows;
        private final int columns;
    }

    private static final Pattern DIMENSIONS = Pattern.compile("^\\s*(?<x>\\d+)\\s*X\\s*(?<y>\\d+)\\s*$");

    private final SnapshotGenerationService _snapshotService;
    private final XftDataObjectIdResolver   _resolver;
}
