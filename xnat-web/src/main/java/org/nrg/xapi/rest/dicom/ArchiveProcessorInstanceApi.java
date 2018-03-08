/*
 * web: org.nrg.xapi.rest.data.InvestigatorsApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api(description = "XNAT Data Archive Processor Instance API")
@XapiRestController
@RequestMapping(value = "/processors")
public class ArchiveProcessorInstanceApi extends AbstractXapiRestController {
    @Autowired
    public ArchiveProcessorInstanceApi(final ArchiveProcessorInstanceService service, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _service = service;
    }

    @ApiOperation(value = "Creates a new site processor from the submitted attributes.", notes = "Returns the newly created site processor with the submitted attributes.", response = ArchiveProcessorInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the newly created site processor."),
            @ApiResponse(code = 403, message = "Insufficient privileges to create the submitted site processor."),
            @ApiResponse(code = 404, message = "The requested site processor wasn't found."),
            @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<ArchiveProcessorInstance> createSiteProcessor(@RequestBody final ArchiveProcessorInstance processor) throws Exception {
        if (StringUtils.isBlank(processor.getProcessorClass())) {
            _log.error("User {} tried to create site processor without a processor class.", getSessionUser().getUsername());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.equals(ArchiveProcessorInstance.SITE_SCOPE,processor.getScope())) {
            _log.error("User {} tried to create site processor with non-site scope.", getSessionUser().getUsername());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ArchiveProcessorInstance created = _service.create(processor);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @ApiOperation(value = "Updates the requested site processor from the submitted attributes.", notes = "Returns the updated site processor.", response = ArchiveProcessorInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the updated site processor."),
            @ApiResponse(code = 304, message = "The requested site processor is the same as the submitted site processor."),
            @ApiResponse(code = 403, message = "Insufficient privileges to edit the requested site processor."),
            @ApiResponse(code = 404, message = "The requested site processor wasn't found."),
            @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/id/{processorId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<ArchiveProcessorInstance> updateSiteProcessor(@PathVariable("processorId") final int processorId, @RequestBody final ArchiveProcessorInstance processor) throws Exception {
        ArchiveProcessorInstance existingProcessor = _service.findSiteProcessorById(processorId);
        if (existingProcessor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean isDirty = false;
        // Only update fields that are actually included in the submitted data and differ from the original source.
        if (StringUtils.isNotBlank(processor.getScope()) && !StringUtils.equals(processor.getScope(), existingProcessor.getScope())) {
            existingProcessor.setScope(processor.getScope());
            isDirty = true;
        }
        if (processor.getProjectIdsList()!=null && !processor.getProjectIdsList().equals(existingProcessor.getProjectIdsList())) {
            existingProcessor.setProjectIdsList(processor.getProjectIdsList());
            isDirty = true;
        }
        if (processor.getPriority()!=existingProcessor.getPriority()) {
            existingProcessor.setPriority(processor.getPriority());
            isDirty = true;
        }
        if ((processor.getParameters()==null && existingProcessor.getParameters()!=null) || (processor.getParameters()!=null && existingProcessor.getParameters()==null) || ((processor.getParameters()!=null) && !processor.getParameters().equals(existingProcessor.getParameters()))) {
            existingProcessor.setParameters(processor.getParameters());
            isDirty = true;
        }
        if (StringUtils.isNotBlank(processor.getProcessorClass()) && !StringUtils.equals(processor.getProcessorClass(), existingProcessor.getProcessorClass())) {
            existingProcessor.setProcessorClass(processor.getProcessorClass());
            isDirty = true;
        }
        if (processor.isEnabled()!=existingProcessor.isEnabled()) {
            existingProcessor.setEnabled(processor.isEnabled());
            isDirty = true;
        }
        _service.update(existingProcessor);
        if (isDirty) {
            return new ResponseEntity<>(existingProcessor, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @ApiOperation(value = "Deletes the requested site processor from the submitted attributes.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site processor was successfully removed."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Insufficient privileges to edit the requested site processor."),
            @ApiResponse(code = 404, message = "The requested site processor wasn't found."),
            @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/id/{processorId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<Boolean> deleteSiteProcessor(@PathVariable("processorId") final int processorId) throws Exception {
        ArchiveProcessorInstance existingProcessor = _service.findSiteProcessorById(processorId);
        if (existingProcessor == null) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        try{
            _service.delete(processorId);
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        catch(Throwable t){
            _log.error("An error occurred initializing the user " + processorId, t);
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Get list of site processors.", notes = "The site processors function returns a list of all site processors configured in the XNAT system.", response = ArchiveProcessorInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently configured site processors."),
            @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "site/list", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<List<ArchiveProcessorInstance>> getAllSiteProcessors() {
        return new ResponseEntity<>(_service.getAllSiteProcessors(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get list of enabled site processors.", notes = "The enabled site processors function returns a list of all enabled site processors configured in the XNAT system.", response = ArchiveProcessorInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently enabled site processors."),
            @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "site/enabled", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<List<ArchiveProcessorInstance>> getAllEnabledSiteProcessors() {
        return new ResponseEntity<>(_service.getAllEnabledSiteProcessors(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get the requested site processor by ID.", notes = "Returns the requested site processor.", response = ArchiveProcessorInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the requested site processor."),
            @ApiResponse(code = 404, message = "The requested site processor wasn't found."),
            @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/id/{processorId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<ArchiveProcessorInstance> getSiteProcessor(@PathVariable("processorId") final int processorId) {
        ArchiveProcessorInstance processor = _service.findSiteProcessorById(processorId);

        if (processor!=null) {
            return new ResponseEntity<>(processor, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private static final Logger _log = LoggerFactory.getLogger(ArchiveProcessorInstanceApi.class);

    private final ArchiveProcessorInstanceService _service;
}
