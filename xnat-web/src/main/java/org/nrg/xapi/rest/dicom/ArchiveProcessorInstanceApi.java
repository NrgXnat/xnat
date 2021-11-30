/*
 * web: org.nrg.xapi.rest.dicom.ArchiveProcessorInstanceApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2018-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.NotModifiedException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.ArchiveProcessorInstanceSummary;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.processors.ArchiveProcessor;
import org.nrg.xnat.processors.StudyRemappingArchiveProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("XNAT Data Archive Processor Instance API")
@XapiRestController
@RequestMapping(value = "/processors")
@Slf4j
public class ArchiveProcessorInstanceApi extends AbstractXapiRestController {
    @Autowired
    public ArchiveProcessorInstanceApi(final ArchiveProcessorInstanceService service, final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DicomSCPManager manager, final List<ArchiveProcessor> processors) {
        super(userManagementService, roleHolder);
        _service = service;
        _processorNames = processors.stream().map(ArchiveProcessor::getClass).map(Class::getName).collect(Collectors.toList());
        _manager = manager;
    }

    @ApiOperation(value = "Get list of processor classes.", notes = "The processor classes function returns a list of all processor classes in the XNAT system.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the processor classes."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "classes", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = DataAccess)
    public List<String> getProcessorClasses() {
        return _processorNames;
    }

    @ApiOperation(value = "Creates a new site processor instance from the submitted attributes.", notes = "Returns the newly created site processor instance with the submitted attributes.", response = ArchiveProcessorInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the newly created site processor instance."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to create the submitted site processor instance."),
                   @ApiResponse(code = 404, message = "The requested site processor instance wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = DataAdmin)
    public ArchiveProcessorInstance createSiteProcessor(@RequestBody final ArchiveProcessorInstance processor) throws Exception {
        if (StringUtils.isBlank(processor.getProcessorClass())) {
            throw new DataFormatException("You must specify a processor class to create a site processor instance.");
        }
        if (!_processorNames.contains(processor.getProcessorClass())) {
            throw new DataFormatException("The specified processor class " + processor.getProcessorClass() + " does not exist.");
        }
        processor.checkForValidProject();
        return _service.create(processor);
    }

    @ApiOperation(value = "Updates the requested site processor instance from the submitted attributes.", notes = "Returns the updated site processor instance.", response = ArchiveProcessorInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the updated site processor instance."),
                   @ApiResponse(code = 304, message = "The requested site processor is the same as the submitted site processor instance."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to edit the requested site processor instance."),
                   @ApiResponse(code = 404, message = "The requested site processor instance wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/id/{instanceId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = DataAdmin)
    public ArchiveProcessorInstance updateSiteProcessor(@PathVariable("instanceId") final long instanceId, @RequestBody final ArchiveProcessorInstance processor) throws NotFoundException, DataFormatException, NotModifiedException {
        final ArchiveProcessorInstance existingProcessor = _service.findSiteProcessorById(instanceId);
        if (existingProcessor == null) {
            throw new NotFoundException("archive processor", instanceId);
        }
        final boolean updated;
        try {
            updated = existingProcessor.update(processor);
        } catch (DataFormatException e) {
            throw new DataFormatException("User " + getSessionUser().getUsername() + " tried to create processor based on project before project is set: " + e.getMessage());
        }
        if (updated) {
            if (_processorNames.contains(processor.getProcessorClass())) {
                throw new DataFormatException("The specified processor class " + processor.getProcessorClass() + " can't be found on this system.");
            }
            _service.update(existingProcessor);
            return existingProcessor;
        }
        throw new NotModifiedException("No changes were specified for the archive processor " + instanceId);
    }

    @ApiOperation(value = "Deletes the requested site processor instance from the submitted attributes.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site processor instance was successfully removed."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to edit the requested site processor instance."),
                   @ApiResponse(code = 404, message = "The requested site processor instance wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/id/{instanceId}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = DataAdmin)
    public boolean deleteSiteProcessor(@PathVariable("instanceId") final long instanceId) throws NotFoundException {
        final ArchiveProcessorInstance processor = _service.findSiteProcessorById(instanceId);
        if (processor == null) {
            throw new NotFoundException("archive processor", instanceId);
        }
        try {
            _service.delete(instanceId);
            return true;
        } catch (Throwable t) {
            log.error("An error occurred deleting the processor instance " + instanceId, t);
            return false;
        }
    }

    @ApiOperation(value = "Get list of site processor instances.", notes = "The site processors function returns a list of all site processor instances configured in the XNAT system.", response = ArchiveProcessorInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently configured site processor instances."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "site/list", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = DataAccess)
    public List<ArchiveProcessorInstance> getAllSiteProcessors() {
        return _service.getAllSiteProcessors();
    }

    @ApiOperation(value = "Get list of enabled site processor instances.", notes = "The enabled site processors function returns a list of all enabled site processor instances configured in the XNAT system.", response = ArchiveProcessorInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently enabled site processor instances."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "site/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = DataAccess)
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessors() {
        return _service.getAllEnabledSiteProcessors();
    }

    @ApiOperation(value = "Get basic information about all enabled site processor instances.", notes = "The enabled site processors summary information function returns a list of basic information for all enabled site processor instances configured in the XNAT system.", response = ArchiveProcessorInstanceSummary.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of basic information for all of the currently enabled site processor instances."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "site/enabled/summary", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public List<ArchiveProcessorInstanceSummary> getAllEnabledSiteProcessorsSummaryInformation() {
        return _service.getAllEnabledSiteProcessors().stream().map(ArchiveProcessorInstanceSummary::new).collect(Collectors.toList());
    }

    @ApiOperation(value = "Get list of enabled site processor instances for specified SCP receiver.", notes = "The enabled site processors function returns a list of all enabled site processor instances configured in the XNAT system for this receiver. Receiver should be specified like aeTitle:port.", response = ArchiveProcessorInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Returns a list of all of the currently enabled site processor instances for this receiver."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "site/enabled/receiver/{aeAndPort}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = DataAccess)
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsForAe(@PathVariable("aeAndPort") final String aeAndPort) {
        return _service.getAllEnabledSiteProcessorsForAe(aeAndPort);
    }

    @ApiOperation(value = "Returns whether the provided AE and port are able to remap the data.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Remapping successfully checked."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/canRemap/receiver/{aeAndPort}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public boolean receiverCanRemap(@PathVariable("aeAndPort") final String aeAndPort) throws NotFoundException {
        final String[] aePortArray = aeAndPort.split(":");
        if (!_manager.getDicomSCPInstance(aePortArray[0], Integer.parseInt(aePortArray[1])).isCustomProcessing()) {
            return false;
        }
        return _service.getAllEnabledSiteProcessorsForAe(aeAndPort)
                       .stream()
                       .map(ArchiveProcessorInstance::getProcessorClass)
                       .filter(StringUtils::isNotBlank)
                       .map(Reflection::getClass)
                       .filter(Objects::nonNull)
                       .anyMatch(StudyRemappingArchiveProcessor.class::isAssignableFrom);
    }

    @ApiOperation(value = "Get the requested site processor instance by ID.", notes = "Returns the requested site processor instance.", response = ArchiveProcessorInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Returns the requested site processor instance."),
                   @ApiResponse(code = 404, message = "The requested site processor instance wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred.")})
    @XapiRequestMapping(value = "site/id/{instanceId}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = DataAccess)
    public ArchiveProcessorInstance getSiteProcessor(@PathVariable("instanceId") final long instanceId) throws NotFoundException {
        return Optional.ofNullable(_service.findSiteProcessorById(instanceId)).orElseThrow(() -> new NotFoundException("archive processor", instanceId));
    }

    private final ArchiveProcessorInstanceService _service;
    private final List<String>                    _processorNames;
    private final DicomSCPManager                 _manager;
}
