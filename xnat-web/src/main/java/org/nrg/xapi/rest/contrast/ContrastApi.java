package org.nrg.xapi.rest.contrast;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Contrast Bolus API")
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

    @Operation(summary = "Create a new contrast entry for a specific scan.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Contrast bolus successfully created."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "403", description = "Not allowed."),
            @ApiResponse(responseCode = "404", description = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = PUT,
            restrictTo = AccessLevel.Edit, value = {"/create/{sessionID}"})
    public ContrastBolus createContrast(@Experiment @PathVariable("sessionID") final String sessionId,
                                        @RequestBody final ContrastBolus contrast)
            throws Exception {
        final UserI user = getSessionUser();
        contrastService.save(contrast, user, true);
        return contrast;
    }

    @Operation(summary = "Update a specific contrast entry for a given scan.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated contrast bolus."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "403", description = "Not allowed."),
            @ApiResponse(responseCode = "404", description = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Edit, value = {
            "/save/{sessionID}"})
    public ResponseEntity<ContrastBolus> updateContrast(@Experiment @PathVariable("sessionID") final String sessionId,
                                                        @RequestBody final ContrastBolus contrast)
            throws Exception {
        final UserI user = getSessionUser();
        contrastService.save(contrast, user, false);
        return new ResponseEntity<>(contrast, HttpStatus.OK);
    }

    @Operation(summary = "Delete contrast entry for a specific scan")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The deleted contrast bolus."),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "403", description = "Not allowed."),
            @ApiResponse(responseCode = "404", description = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Edit, value = {
            "/delete/{sessionID}"})
    public void deleteContrast(@Experiment @PathVariable("sessionID") final String sessionId,
                               @RequestBody final ContrastBolus contrast)
            throws Exception {
        final UserI user = getSessionUser();
        contrastService.delete(contrast, user);
    }

    @Operation(summary = "Get contrast entries for session.", description = "Returns a list of Contrast Bolus entries")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A list of Contrast Bolus elements"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = {"/{sessionID}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method =
            RequestMethod.GET, restrictTo = AccessLevel.Read)
    public ResponseEntity<List<ContrastBolus>> getContrastEntries(
            @Parameter(description = "The session ID (Accesssion Number).", required = true) @PathVariable("sessionID") final String sessionId) {
        try {
            final UserI user = getSessionUser();
            return new ResponseEntity<>(contrastService.findContrast(sessionId, user), HttpStatus.OK);
        } catch (Throwable t) {
            log.error("ContrastApi exception: {}", t.getMessage(), t);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
