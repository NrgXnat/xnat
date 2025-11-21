package org.nrg.xapi.rest.contrast;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xnat.contrast.model.ContrastBolus;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.contrast.ContrastService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("Contrast Bolus API")
@XapiRestController
@RequestMapping(value = "/contrasts")
@Slf4j
public class ContrastApi extends AbstractXapiRestController {
    private final ContrastService contrastService;

    protected ContrastApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder,
                          final ContrastService contrastService) {
        super(userManagementService, roleHolder);
        this.contrastService = contrastService;
    }

    @ApiOperation(value = "Create a new contrast entry for a specific scan.")
    @ApiResponses({@ApiResponse(code = 200, message = "Contrast bolus successfully created."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = PUT,
            restrictTo = AccessLevel.Edit, value = {"/create/{sessionID}"})
    public ContrastBolus createContrast(@Experiment @PathVariable("sessionID") final String sessionId,
                                        @RequestBody final ContrastBolus contrast)
            throws Exception {
        final UserI user = getSessionUser();
        contrastService.save(contrast, user, true);
        return contrast;
    }

    @ApiOperation(value = "Update a specific contrast entry for a given scan.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully updated contrast bolus."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Edit, value = {
            "/save/{sessionID}"})
    public ResponseEntity<ContrastBolus> updateContrast(@Experiment @PathVariable("sessionID") final String sessionId,
                                                        @RequestBody final ContrastBolus contrast)
            throws Exception {
        final UserI user = getSessionUser();
        contrastService.save(contrast, user, false);
        return new ResponseEntity<>(contrast, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete contrast entry for a specific scan")
    @ApiResponses({@ApiResponse(code = 200, message = "The deleted contrast bolus."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Edit, value = {
            "/delete/{sessionID}"})
    public void deleteContrast(@Experiment @PathVariable("sessionID") final String sessionId,
                               @RequestBody final ContrastBolus contrast)
            throws Exception {
        final UserI user = getSessionUser();
        contrastService.delete(contrast, user);
    }

    @ApiOperation(value = "Get contrast entries for session.", notes = "Returns a list of Contrast Bolus entries",
            response = String.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "A list of Contrast Bolus elements"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = {"/{sessionID}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method =
            RequestMethod.GET, restrictTo = AccessLevel.Read)
    public ResponseEntity<List<ContrastBolus>> getContrastEntries(
            @ApiParam(value = "The session ID (Accesssion Number).", required = true) @PathVariable("sessionID") final String sessionId) {
        try {
            final UserI user = getSessionUser();
            return new ResponseEntity<>(contrastService.findContrast(sessionId, user), HttpStatus.OK);
        } catch (Throwable t) {
            log.error("ContrastApi exception: {}", t.getMessage(), t);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
