package org.nrg.xnat.archive.xapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
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

    @Autowired
    public DirectArchiveSessionApi(final DirectArchiveSessionService directArchiveSessionService,
                                   final UserManagementServiceI userManagementService,
                                   final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.directArchiveSessionService = directArchiveSessionService;
    }

    @XapiRequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get direct archive sessions")
    public ResponseEntity<List<SessionData>> getPaginated(@RequestBody DirectArchiveSessionPaginatedRequest request) {
        return new ResponseEntity<>(directArchiveSessionService.getPaginated(getSessionUser(), request), HttpStatus.OK);
    }

    @XapiRequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Read)
    @ApiOperation(value = "Get direct archive session")
    public ResponseEntity<SessionData> getSession(@Project @RequestParam String project,
                                                  @RequestParam String tag,
                                                  @RequestParam String name) {
        return new ResponseEntity<>(directArchiveSessionService.findByProjectTagName(project, tag, name),
                HttpStatus.OK);
    }

    @XapiRequestMapping(method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Delete)
    @ApiOperation(value = "Delete direct archive session")
    public ResponseEntity<Void> delete(@Project @RequestParam String project,
                                       @RequestParam long id) throws InvalidPermissionException, NotFoundException {
        directArchiveSessionService.delete(project, id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {InvalidPermissionException.class})
    public String handlePermissions(final Exception e) {
        return e.getMessage();
    }
}
