package org.nrg.xnat.compute.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Compute Environments REST API")
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

    @Operation(summary = "Get all compute environment configs or all compute environment configs for a given type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compute environment configs successfully retrieved."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public List<ComputeEnvironmentConfig> getAll(@RequestParam(value = "type", required = false) final ComputeEnvironmentConfig.ConfigType type) {
        if (type != null) {
            return computeEnvironmentConfigService.getByType(type);
        } else {
            return computeEnvironmentConfigService.getAll();
        }
    }

    @Operation(summary = "Get a compute environment config.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compute environment config successfully retrieved."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "404", description = "Compute environment config not found."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ComputeEnvironmentConfig get(@PathVariable("id") final Long id) throws NotFoundException {
        return computeEnvironmentConfigService.retrieve(id)
                .orElseThrow(() -> new NotFoundException("Compute environment config not found."));
    }

    @Operation(summary = "Create a compute environment config.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compute environment config successfully created."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Admin)
    public ComputeEnvironmentConfig create(@RequestBody final ComputeEnvironmentConfig computeEnvironmentConfig) {
        return computeEnvironmentConfigService.create(computeEnvironmentConfig);
    }

    @Operation(summary = "Update a compute environment config.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compute environment config successfully updated."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "404", description = "Compute environment config not found."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public ComputeEnvironmentConfig update(@PathVariable("id") final Long id,
                                           @RequestBody final ComputeEnvironmentConfig computeEnvironmentConfig) throws NotFoundException {
        if (!id.equals(computeEnvironmentConfig.getId())) {
            throw new IllegalArgumentException("The ID in the path must match the ID in the body.");
        }

        return computeEnvironmentConfigService.update(computeEnvironmentConfig);
    }

    @Operation(summary = "Delete a compute environment config.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compute environment config successfully deleted."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "404", description = "Compute environment config not found."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = RequestMethod.DELETE, restrictTo = Admin)
    public void delete(@PathVariable("id") final Long id) throws NotFoundException {
        computeEnvironmentConfigService.delete(id);
    }

    @Operation(summary = "Get all available compute environment configs for the provided execution scope.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compute environment configs successfully retrieved."),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
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
