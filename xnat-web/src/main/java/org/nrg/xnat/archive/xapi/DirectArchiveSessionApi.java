package org.nrg.xnat.archive.xapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
}
