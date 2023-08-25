package org.nrg.xnat.compute.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api("Compute Environments REST API")
@XapiRestController
@RequestMapping(value = "/compute/environments")
public class ComputeEnvironmentConfigsApi extends AbstractXapiRestController {

    private final ComputeEnvironmentConfigService computeEnvironmentConfigService;

    @Autowired
    public ComputeEnvironmentConfigsApi(final UserManagementServiceI userManagementService,
                                        final RoleHolder roleHolder,
                                        final ComputeEnvironmentConfigService computeEnvironmentConfigService) {
        super(userManagementService, roleHolder);
        this.computeEnvironmentConfigService = computeEnvironmentConfigService;
    }

    @ApiOperation(value = "Get all compute environment configs or all compute environment configs for a given type.", response = ComputeEnvironmentConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute environment configs successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public List<ComputeEnvironmentConfig> getAll(@RequestParam(value = "type", required = false) final ComputeEnvironmentConfig.ConfigType type) {
        if (type != null) {
            return computeEnvironmentConfigService.getByType(type);
        } else {
            return computeEnvironmentConfigService.getAll();
        }
    }

    @ApiOperation(value = "Get a compute environment config.", response = ComputeEnvironmentConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute environment config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Compute environment config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ComputeEnvironmentConfig get(@PathVariable("id") final Long id) throws NotFoundException {
        return computeEnvironmentConfigService.retrieve(id)
                .orElseThrow(() -> new NotFoundException("Compute environment config not found."));
    }

    @ApiOperation(value = "Create a compute environment config.", response = ComputeEnvironmentConfig.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Compute environment config successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Admin)
    public ComputeEnvironmentConfig create(@RequestBody final ComputeEnvironmentConfig computeEnvironmentConfig) {
        return computeEnvironmentConfigService.create(computeEnvironmentConfig);
    }

    @ApiOperation(value = "Update a compute environment config.", response = ComputeEnvironmentConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute environment config successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Compute environment config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public ComputeEnvironmentConfig update(@PathVariable("id") final Long id,
                                           @RequestBody final ComputeEnvironmentConfig computeEnvironmentConfig) throws NotFoundException {
        if (!id.equals(computeEnvironmentConfig.getId())) {
            throw new IllegalArgumentException("The ID in the path must match the ID in the body.");
        }

        return computeEnvironmentConfigService.update(computeEnvironmentConfig);
    }

    @ApiOperation(value = "Delete a compute environment config.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Compute environment config successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Compute environment config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = RequestMethod.DELETE, restrictTo = Admin)
    public void delete(@PathVariable("id") final Long id) throws NotFoundException {
        computeEnvironmentConfigService.delete(id);
    }

    @ApiOperation(value = "Get all available compute environment configs for the provided execution scope.", response = ComputeEnvironmentConfig.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute environment configs successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/available", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Read)
    public List<ComputeEnvironmentConfig> getAvailable(@RequestParam final Map<String, String> params) {
        ComputeEnvironmentConfig.ConfigType type = null;

        // If the type is specified, remove it from the params map so it doesn't get used as an execution scope.
        if (params.containsKey("type")) {
            type = ComputeEnvironmentConfig.ConfigType.valueOf(params.get("type"));
            params.remove("type");
        }

        // Get the execution scope from the params map.
        Map<Scope, String> executionScope = params.entrySet().stream()
                .filter(entry -> Scope.getCodes().contains(entry.getKey()))
                .collect(Collectors.toMap(entry -> Scope.getScope(entry.getKey()), Map.Entry::getValue));

        return computeEnvironmentConfigService.getAvailable(type, executionScope);
    }

}
