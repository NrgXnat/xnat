package org.nrg.xnat.compute.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@Api("Compute Hardware REST API")
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

    @ApiOperation(value = "Get a hardware config.", response = HardwareConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Hardware config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Hardware config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public HardwareConfig get(@PathVariable("id") final Long id) throws NotFoundException {
        return hardwareConfigService.retrieve(id).orElseThrow(() -> new NotFoundException("No hardware config found for ID " + id));
    }

    @ApiOperation(value = "Get all hardware configs.", response = HardwareConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Hardware configs successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public List<HardwareConfig> getAll() {
        return hardwareConfigService.retrieveAll();
    }

    @ApiOperation(value = "Create a hardware config.", response = HardwareConfig.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Hardware config successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public HardwareConfig create(@RequestBody final HardwareConfig hardwareConfig) {
        return hardwareConfigService.create(hardwareConfig);
    }

    @ApiOperation(value = "Update a hardware config.", response = HardwareConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Hardware config successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Hardware config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public HardwareConfig update(@PathVariable("id") final Long id,
                                 @RequestBody final HardwareConfig hardwareConfig) throws NotFoundException {
        if (!id.equals(hardwareConfig.getId())) {
            throw new IllegalArgumentException("The hardware config ID in the path must match the ID in the body.");
        }
        return hardwareConfigService.update(hardwareConfig);
    }

    @ApiOperation(value = "Delete a hardware config.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Hardware config successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Hardware config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = DELETE, restrictTo = Admin)
    public void delete(@PathVariable("id") final Long id) throws NotFoundException {
        hardwareConfigService.delete(id);
    }

}
