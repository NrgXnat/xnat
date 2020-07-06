package org.nrg.xnat.snapshot.rest;

import static org.nrg.xdat.security.helpers.AccessLevel.Read;
import java.io.File;
import java.io.IOException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pradeep.d
 *
 */
@Api("XNAT 1.7.7 Snapshot Generation Plugin API")
@XapiRestController
@RequestMapping(value = "/projects")
@Slf4j
public class SnapshotGenerationApi extends AbstractXapiRestController {

	/**
	 * @param userManagementService
	 * @param roleHolder
	 * @param snapshotService
	 */
	@Autowired
	protected SnapshotGenerationApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder,
			final SnapshotGenerationService snapshotService) {
		super(userManagementService, roleHolder);
		_snapshotService = snapshotService;
	}

	/**
	 * @param projectID
	 * @param sessionIdentifier
	 * @param scanIdentifier
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Get or generate a single thumbnail snapshot.")
	@ApiResponses({ @ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
			@ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
			@ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
			@ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
			@ApiResponse(code = 500, message = "Unexpected error") })
	@XapiRequestMapping(value = "/{projectID}/experiments/{sessionIdentifier}/scan/{scanIdentifier}/snapshot", produces = {
			MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.IMAGE_PNG_VALUE }, method = RequestMethod.GET, restrictTo = Read)
	public ResponseEntity<Resource> generateSnapshot(@PathVariable final String projectID,
			@PathVariable final String sessionIdentifier, @PathVariable final String scanIdentifier)
			throws IOException {
		_log.debug("Start Snapshot Generation- Snapshots");
		String imagePath = _snapshotService.generateSnapshot(projectID, sessionIdentifier, scanIdentifier,
				"notApplicable");
		_log.debug(" Snapshot Generation image path " + imagePath);
		File file = new File(imagePath);
		Resource fileSystemResource = new FileSystemResource(file);
		return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(fileSystemResource);
	}

	/**
	 * @param projectID
	 * @param sessionIdentifier
	 * @param scanIdentifier
	 * @param gridView
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value = "Get or generate a single thumbnail snapshot.")
	@ApiResponses({ @ApiResponse(code = 200, message = "Dataset definition successfully resolved."),
			@ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
			@ApiResponse(code = 403, message = "Insufficient privileges to resolve the dataset definition."),
			@ApiResponse(code = 404, message = "The requested dataset definition doesn't exist."),
			@ApiResponse(code = 500, message = "Unexpected error") })
	@XapiRequestMapping(value = "{projectID}/experiments/{sessionIdentifier}/scan/{scanIdentifier}/snapshot/grid/{gridView}", produces = {
			MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.IMAGE_PNG_VALUE }, method = RequestMethod.GET, restrictTo = Read)
	public ResponseEntity<Resource> generateSnapshotView(@PathVariable final String projectID,
			@PathVariable final String sessionIdentifier, @PathVariable final String scanIdentifier,
			@PathVariable final String gridView) throws IOException {
		_log.debug("Start Snapshot Generation- GridView");
		String imagePath = _snapshotService.generateSnapshot(projectID, sessionIdentifier, scanIdentifier, gridView);
		_log.debug(" Snapshot Generation image path " + imagePath);
		File file = new File(imagePath);
		Resource fileSystemResource = new FileSystemResource(file);
		return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(fileSystemResource);
	}

	private final SnapshotGenerationService _snapshotService;
	private static final Logger _log = LoggerFactory.getLogger(SnapshotGenerationApi.class);

}
