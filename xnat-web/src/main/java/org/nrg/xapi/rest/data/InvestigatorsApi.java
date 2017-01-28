/*
 * web: org.nrg.xapi.rest.data.InvestigatorsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.data;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.model.investigators.Investigator;
import org.nrg.xnat.services.investigators.InvestigatorService;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(description = "XNAT Data Investigators API")
@XapiRestController
@RequestMapping(value = "/investigators")
public class InvestigatorsApi extends AbstractXapiRestController {
    @Autowired
    public InvestigatorsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final InvestigatorService service) {
        super(userManagementService, roleHolder);
        _service = service;
    }

    @ApiOperation(value = "Get list of investigators.", notes = "The investigators function returns a list of all investigators configured in the XNAT system.", response = Investigator.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently configured investigators."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Investigator>> getInvestigators() {
        return new ResponseEntity<>(_service.getInvestigators(), HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the requested investigator.", notes = "Returns the investigator with the specified ID.", response = Investigator.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the requested investigator."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(value = "{investigatorId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Investigator> getInvestigator(@PathVariable("investigatorId") final int investigatorId) {
        final Investigator investigator = _service.getInvestigator(investigatorId);
        if (investigator == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(investigator, HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new investigator from the submitted attributes.", notes = "Returns the newly created investigator with the submitted attributes.", response = Investigator.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the newly created investigator."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to create the submitted investigator."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Investigator> createInvestigator(@RequestBody final Investigator investigator) throws Exception {
        if (StringUtils.isBlank(investigator.getFirstname()) || StringUtils.isBlank(investigator.getLastname())) {
            _log.error("User {} tried to create investigator without a first or last name.", getSessionUser().getUsername());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final UserI user = getSessionUser();
        final XFTItem item = XFTItem.NewItem(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, user);
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".title", investigator.getTitle());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".firstname", investigator.getFirstname());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".lastname", investigator.getLastname());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".department", investigator.getDepartment());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".institution", investigator.getInstitution());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".email", investigator.getEmail());
        item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".phone", investigator.getPhone());
        if (!SaveItemHelper.authorizedSave(item, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.CREATE_INVESTTGATOR))) {
            _log.error("Failed to create a new investigator for user {}. Check the logs for possible errors or exceptions.", user.getUsername());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(_service.getInvestigator(investigator.getFirstname(), investigator.getLastname()), HttpStatus.OK);
    }

    @ApiOperation(value = "Updates the requested investigator from the submitted attributes.", notes = "Returns the updated investigator.", response = Investigator.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the updated investigator."),
                   @ApiResponse(code = 304, message = "The requested investigator is the same as the submitted investigator."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to edit the requested investigator."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(value = "{investigatorId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Investigator> updateInvestigator(@PathVariable("investigatorId") final int investigatorId, @RequestBody final Investigator investigator) throws Exception {
        final UserI user = getSessionUser();

        final XnatInvestigatordata existing = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (existing == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean isDirty = false;
        // Only update fields that are actually included in the submitted data and differ from the original source.
        if (StringUtils.isNotBlank(investigator.getTitle()) && !StringUtils.equals(investigator.getTitle(), existing.getTitle())) {
            existing.setTitle(investigator.getTitle());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getFirstname()) && !StringUtils.equals(investigator.getFirstname(), existing.getFirstname())) {
            existing.setFirstname(investigator.getFirstname());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getLastname()) && !StringUtils.equals(investigator.getLastname(), existing.getLastname())) {
            existing.setLastname(investigator.getLastname());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getDepartment()) && !StringUtils.equals(investigator.getDepartment(), existing.getDepartment())) {
            existing.setDepartment(investigator.getDepartment());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getInstitution()) && !StringUtils.equals(investigator.getInstitution(), existing.getInstitution())) {
            existing.setInstitution(investigator.getInstitution());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getEmail()) && !StringUtils.equals(investigator.getEmail(), existing.getEmail())) {
            existing.setEmail(investigator.getEmail());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(investigator.getPhone()) && !StringUtils.equals(investigator.getPhone(), existing.getPhone())) {
            existing.setPhone(investigator.getPhone());
            isDirty = true;
        }

        if (isDirty) {
            if (!SaveItemHelper.authorizedSave(existing, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.MODIFY_INVESTTGATOR))) {
                _log.error("Failed to save the investigator with ID {}. Check the logs for possible errors or exceptions.");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(_service.getInvestigator(investigatorId), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @ApiOperation(value = "Deletes the requested investigator.", notes = "Returns true if the requested investigator was successfully deleted. Returns false otherwise.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns true to indicate the requested investigator was successfully deleted."),
                   @ApiResponse(code = 403, message = "The user doesn't have permission to delete investigators."),
                   @ApiResponse(code = 404, message = "The requested investigator wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @RequestMapping(value = "{investigatorId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Boolean> deleteInvestigator(@PathVariable("investigatorId") final int investigatorId) throws Exception {
        final UserI user = getSessionUser();
        if (!Roles.isSiteAdmin(user)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        final XnatInvestigatordata investigator = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (investigator == null) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        SaveItemHelper.authorizedDelete(investigator.getItem(), user, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.REMOVE_INVESTTGATOR));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    private static final Logger _log = LoggerFactory.getLogger(InvestigatorsApi.class);

    private final InvestigatorService _service;
}
