package org.nrg.xnat.archive.xapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@XapiRestController
@RequestMapping(value = "/direct-archive")
@Api("Direct Archive Session API")
public class DirectArchiveSessionApi extends AbstractXapiRestController {
    private final DirectArchiveSessionService directArchiveSessionService;
    private final PermissionsServiceI permissionsService;

    @Autowired
    public DirectArchiveSessionApi(final DirectArchiveSessionService directArchiveSessionService,
                                   final UserManagementServiceI userManagementService,
                                   final PermissionsServiceI permissionsService,
                                   final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.directArchiveSessionService = directArchiveSessionService;
        this.permissionsService = permissionsService;
    }

    @XapiRequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get direct archive sessions")
    public ResponseEntity<List<SessionData>> getPaginated(@RequestBody DirectArchiveSessionPaginatedRequest request) {
        return new ResponseEntity<>(directArchiveSessionService.getPaginated(getSessionUser(), request), HttpStatus.OK);
    }

    @XapiRequestMapping(path="{project}/{tag}/{name}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Read)
    @ApiOperation(value = "Get direct archive session")
    public ResponseEntity<SessionData> getSession(@Project @PathVariable String project,
                                                  @PathVariable String tag,
                                                  @PathVariable String name) throws NotFoundException {
        return new ResponseEntity<>(directArchiveSessionService.findByProjectTagName(project, tag, name),
                HttpStatus.OK);
    }

    @XapiRequestMapping(path="{id}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete direct archive session",
                  notes = "Removes the session's tracking row and, when the archive directory belongs to this session " +
                          "alone, its files. Refused with 409 while the session is receiving files or being archived. " +
                          "A site admin may pass force=true to delete a session left in a queued, building or archiving " +
                          "status; nothing verifies the archiver has given up on it, so forcing a session that is really " +
                          "being archived can leave a half-saved experiment. Files still landing are refused regardless.")
    public ResponseEntity<Void> delete(@PathVariable long id,
                                       @ApiParam("Claim the session whatever its status (site admins only; see notes)")
                                       @RequestParam(required = false, defaultValue = "false") boolean force)
            throws InvalidPermissionException, NotFoundException, ClientException, ServerException {
        directArchiveSessionService.delete(id, getSessionUser(), force);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @XapiRequestMapping(path="{project}/{tag}/{name}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Trigger direct archive of session",
                  notes = "Queues the session for building. Refused with 409 unless the session is receiving or in " +
                          "error. A site admin may pass force=true to re-queue a session left queued, building or " +
                          "archiving, for example by a node restart; nothing verifies that no worker is still " +
                          "processing it, so forcing a session that is really being archived puts two workers on it.")
    public ResponseEntity<Void> triggerArchive(@Project @PathVariable String project,
                                               @PathVariable String tag,
                                               @PathVariable String name,
                                               @ApiParam("Re-queue the session whatever its status (site admins only; see notes)")
                                               @RequestParam(required = false, defaultValue = "false") boolean force)
            throws ClientException, ServerException, NotFoundException, InvalidPermissionException {
        if (!permissionsService.getUserEditableProjects(getSessionUser()).contains(project)) {
            throw new InvalidPermissionException("User cannot trigger archive for project " + project);
        }
        SessionData sessionData = directArchiveSessionService.findByProjectTagName(project, tag, name);
        directArchiveSessionService.triggerArchive(sessionData, getSessionUser(), force);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {InvalidPermissionException.class})
    public String handlePermissions(final Exception e) {
        return e.getMessage();
    }

    /**
     * The shared XAPI advice only honours a status carried by an exception annotation, so a {@link ClientException}
     * would come back as a 500 whatever status the service put on it; answer with that status here.
     */
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<String> handleClientException(final ClientException e) {
        return ResponseEntity.status(e.getStatus().getCode()).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
    }
}
