package org.nrg.xnat.compute.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Tag(name = "Compute Hardware REST API")
@XapiRestController
@RequestMapping(value = "/compute/hardware")
public class HardwareConfigsApi extends AbstractXapiRestController {

    private final HardwareConfigService hardwareConfigService;

    @Autowired
    public HardwareConfigsApi(final UserManagementServiceI userManagementService,
                              final RoleHolder roleHolder,
                              final HardwareConfigService hardwareConfigService) {
        super(userManagementService, roleHolder);
        this.hardwareConfigService = hardwareConfigService;
    }

    @Operation(summary = "Get a hardware config.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hardware config successfully retrieved."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "404", description = "Hardware config not found."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public HardwareConfig get(@PathVariable("id") final Long id) throws NotFoundException {
        return hardwareConfigService.retrieve(id).orElseThrow(() -> new NotFoundException("No hardware config found for ID " + id));
    }

    @Operation(summary = "Get all hardware configs.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hardware configs successfully retrieved."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public List<HardwareConfig> getAll() {
        return hardwareConfigService.retrieveAll();
    }

    @Operation(summary = "Create a hardware config.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hardware config successfully created."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public HardwareConfig create(@RequestBody final HardwareConfig hardwareConfig) {
        return hardwareConfigService.create(hardwareConfig);
    }

    @Operation(summary = "Update a hardware config.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hardware config successfully updated."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "404", description = "Hardware config not found."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public HardwareConfig update(@PathVariable("id") final Long id,
                                 @RequestBody final HardwareConfig hardwareConfig) throws NotFoundException {
        if (!id.equals(hardwareConfig.getId())) {
            throw new IllegalArgumentException("The hardware config ID in the path must match the ID in the body.");
        }
        return hardwareConfigService.update(hardwareConfig);
    }

    @Operation(summary = "Delete a hardware config.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hardware config successfully deleted."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "404", description = "Hardware config not found."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = DELETE, restrictTo = Admin)
    public void delete(@PathVariable("id") final Long id) throws NotFoundException {
        hardwareConfigService.delete(id);
    }

}
