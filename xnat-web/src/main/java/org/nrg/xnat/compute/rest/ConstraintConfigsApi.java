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
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("Compute Constraints REST API")
@XapiRestController
@RequestMapping(value = "/compute/constraints")
public class ConstraintConfigsApi extends AbstractXapiRestController {

    private final ConstraintConfigService constraintConfigService;

    @Autowired
    public ConstraintConfigsApi(final UserManagementServiceI userManagementService,
                                final RoleHolder roleHolder,
                                final ConstraintConfigService constraintConfigService) {
        super(userManagementService, roleHolder);
        this.constraintConfigService = constraintConfigService;
    }

    @ApiOperation(value = "Get a constraint config.", response = ConstraintConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Placement constraint config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Placement constraint config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public ConstraintConfig get(@PathVariable final Long id) throws NotFoundException  {
        return constraintConfigService.retrieve(id)
                .orElseThrow(() -> new NotFoundException("No placement constraint config found with ID " + id));
    }

    @ApiOperation(value = "Get all constraint configs.", response = ConstraintConfig.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Placement constraint configs successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public List<ConstraintConfig> getAll() {
        return constraintConfigService.getAll();
    }

    @ApiOperation(value = "Create a constraint config.", response = ConstraintConfig.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Placement constraint config successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(CREATED)
    @XapiRequestMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public ConstraintConfig create(@RequestBody final ConstraintConfig constraintConfig) {
        return constraintConfigService.create(constraintConfig);
    }

    @ApiOperation(value = "Update a constraint config.", response = ConstraintConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Placement constraint config successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Placement constraint config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public ConstraintConfig update(@PathVariable final Long id,
                                   @RequestBody final ConstraintConfig constraintConfig) throws NotFoundException {
        if (!id.equals(constraintConfig.getId())) {
            throw new IllegalArgumentException("Placement constraint config ID in path does not match ID in body.");
        }

        return constraintConfigService.update(constraintConfig);
    }

    @ApiOperation(value = "Delete a constraint config.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Placement constraint config successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Placement constraint config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = DELETE, restrictTo = Admin)
    public void delete(@PathVariable final Long id) throws NotFoundException {
        constraintConfigService.delete(id);
    }

}
