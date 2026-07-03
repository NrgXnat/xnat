/*
 * web: org.nrg.xapi.rest.data.InvestigatorsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.*;
import org.nrg.xapi.model.xft.Investigator;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.exception.XftItemException;
import org.nrg.xnat.services.investigators.InvestigatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Tag(name = "XNAT Data Investigators API")
@XapiRestController
@RequestMapping(value = "/investigators")
@Slf4j
public class InvestigatorsApi extends AbstractXapiRestController {
    @Autowired
    public InvestigatorsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final InvestigatorService service) {
        super(userManagementService, roleHolder);
        _service = service;
    }

    @Operation(summary = "Get list of investigators.", description = "The investigators function returns a list of all investigators configured in the XNAT system.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a list of all of the currently configured investigators."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public List<Investigator> getInvestigators() {
        return _service.getInvestigators();
    }

    @Operation(summary = "Gets the requested investigator.", description = "Returns the investigator with the specified ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the requested investigator."),
                   @ApiResponse(responseCode = "404", description = "The requested investigator wasn't found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "{investigatorId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    public Investigator getInvestigator(@PathVariable("investigatorId") final int investigatorId) throws NotFoundException {
        return _service.getInvestigator(investigatorId);
    }

    @Operation(summary = "Creates a new investigator from the submitted attributes.", description = "Returns the newly created investigator with the submitted attributes.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the newly created investigator."),
                   @ApiResponse(responseCode = "403", description = "Insufficient privileges to create the submitted investigator."),
                   @ApiResponse(responseCode = "404", description = "The requested investigator wasn't found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody
    public Investigator createInvestigator(@RequestBody final Investigator investigator) throws DataFormatException, ResourceAlreadyExistsException, InitializationException {
        if (StringUtils.isBlank(investigator.getFirstname()) || StringUtils.isBlank(investigator.getLastname())) {
            log.error("User {} tried to create investigator without a first or last name.", getSessionUser().getUsername());
            throw new DataFormatException("Can't create investigator without a first or last name.");
        }
        try {
            return  _service.createInvestigator(investigator, getSessionUser());
        } catch (XftItemException e) {
            throw new InitializationException("Failed to create investigator", e);
        }
    }

    @Operation(summary = "Updates the requested investigator from the submitted attributes.", description = "Returns the updated investigator.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the updated investigator."),
                   @ApiResponse(responseCode = "304", description = "The requested investigator is the same as the submitted investigator."),
                   @ApiResponse(responseCode = "403", description = "Insufficient privileges to edit the requested investigator."),
                   @ApiResponse(responseCode = "404", description = "The requested investigator wasn't found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "{investigatorId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = PUT)
    @ResponseBody
    public Investigator updateInvestigator(@PathVariable("investigatorId") final int investigatorId, @RequestBody final Investigator investigator) throws NotFoundException, InitializationException, XftItemException {
        return _service.updateInvestigator(investigatorId, investigator, getSessionUser());
    }

    @Operation(summary = "Deletes the requested investigator.", description = "Returns true if the requested investigator was successfully deleted. Returns false otherwise.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns true to indicate the requested investigator was successfully deleted."),
                   @ApiResponse(responseCode = "403", description = "The user doesn't have permission to delete investigators."),
                   @ApiResponse(responseCode = "404", description = "The requested investigator wasn't found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "{investigatorId}", produces = APPLICATION_JSON_VALUE, method = DELETE)
    @ResponseBody
    public boolean deleteInvestigator(@PathVariable("investigatorId") final int investigatorId) throws NotFoundException, InsufficientPrivilegesException, XftItemException {
        _service.deleteInvestigator(investigatorId, getSessionUser());
        return true;
    }

    private final InvestigatorService _service;
}
