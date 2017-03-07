/*
 * web: org.nrg.xapi.rest.dicom.DicomSCPApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.DicomSCP;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(description = "XNAT DICOM SCP management API")
@XapiRestController
@RequestMapping(value = "/dicomscp")
public class DicomSCPApi extends AbstractXapiRestController {
    @Autowired
    public DicomSCPApi(final DicomSCPManager manager, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _manager = manager;
    }

    @ApiOperation(value = "Get map of all configured DICOM object identifiers and names.", notes = "This function returns a map of all DICOM object identifiers defined for the current system along with each identifier's readable name. The default identifier will be the first in the list.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A map of DICOM object identifiers and names."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "identifiers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, String>> getDicomObjectIdentifiers() {
        return new ResponseEntity<>(_manager.getDicomObjectIdentifierBeans(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get implementation name of the specified DICOM object identifier.", notes = "This function returns the fully-qualified class name of the specified DICOM object identifier. You can use the value 'default' to retrieve the default identifier even if you don't know the specific name.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The implementation class of the specified DICOM object identifier."),
                   @ApiResponse(code = 404, message = "No DICOM object identifier with the specified ID was found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "identifiers/{beanId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getDicomObjectIdentifier(@PathVariable("beanId") String beanId) {
        // If they specified "default", then get the first bean in the list: they're sorted so that the default is first.
        if (StringUtils.equals("default", beanId)) {
            return new ResponseEntity<>(_manager.getDefaultDicomObjectIdentifier().getClass().getName(), HttpStatus.OK);
        }
        final DicomObjectIdentifier<XnatProjectdata> identifier = _manager.getDicomObjectIdentifier(beanId);
        if (identifier == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(identifier.getClass().getName(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get list of all configured DICOM SCP receiver definitions.", notes = "The primary DICOM SCP retrieval function returns a list of all DICOM SCP receivers defined for the current system.", response = DicomSCPInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of DICOM SCP receiver definitions."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DicomSCPInstance>> dicomSCPsGet() {
        return new ResponseEntity<>(_manager.getDicomSCPInstances(), HttpStatus.OK);
    }

    @ApiOperation(value = "Creates a new DICOM SCP receiver from the request body.", notes = "The newly created DICOM SCP receiver instance is returned from the call. This should include the instance ID for the new object.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The newly created DICOM SCP receiver definition."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."), @ApiResponse(code = 409, message = "A DICOM SCP receiver already exists and is enabled at the same port."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<DicomSCPInstance> dicomSCPCreate(@RequestBody final DicomSCPInstance instance) throws IOException, EnabledDICOMReceiverWithDuplicatePortException {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        _manager.create(instance);
        return new ResponseEntity<>(instance, HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the DICOM SCP receiver definition with the specified ID.", notes = "Returns the DICOM SCP receiver definition with the specified ID.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET})
    public ResponseEntity<DicomSCPInstance> dicomSCPInstanceGet(@ApiParam(value = "ID of the DICOM SCP receiver definition to fetch", required = true) @PathVariable("id") final int id) {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return _manager.hasDicomSCP(id) ? new ResponseEntity<>(_manager.getDicomSCPInstance(id), HttpStatus.OK)
                                        : new ResponseEntity<DicomSCPInstance>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Updates the DICOM SCP receiver definition object with the specified ID.", notes = "Returns the updated DICOM SCP receiver definition.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully updated."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to create or update this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.PUT})
    public ResponseEntity<DicomSCPInstance> dicomSCPInstanceCreateOrUpdate(@ApiParam(value = "The ID of the DICOM SCP receiver definition to update.", required = true) @PathVariable("id") final int id, @RequestBody final DicomSCPInstance instance) throws NotFoundException, EnabledDICOMReceiverWithDuplicatePortException {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_manager.hasDicomSCP(id)) {
            // Set the ID to the value specified in the REST call. If ID not specified on PUT, value will be zero, so we
            // need to make sure it's set to the proper value. If they submit it under the wrong ID well...
            instance.setId(id);
            _manager.setDicomSCPInstance(instance);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(instance.isEnabled()){
            _manager.startDicomSCP(id);
        }
        else{
            _manager.stopDicomSCP(id);
        }
        return new ResponseEntity<>(instance, HttpStatus.OK);
    }

    @ApiOperation(value = "Deletes the DICOM SCP receiver definition object with the specified ID.", notes = "This call will stop the receiver if it's currently running.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully created or updated."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to delete this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.DELETE})
    public ResponseEntity<Void> dicomSCPInstanceDelete(@ApiParam(value = "The ID of the DICOM SCP receiver definition to delete.", required = true) @PathVariable("id") final int id) throws NotFoundException {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            _manager.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NrgServiceException e) {
            _log.error("An error occurred trying to delete the DICOM SCP instance " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns whether the DICOM SCP receiver definition with the specified ID is enabled.", notes = "Returns true or false based on whether the specified DICOM SCP receiver definition is enabled or not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition enabled status successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}/enabled"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> dicomSCPInstanceEnabledGet(@ApiParam(value = "The ID of the DICOM SCP receiver definition to retrieve the enabled status for.", required = true) @PathVariable("id") final int id) {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return _manager.hasDicomSCP(id) ? new ResponseEntity<>(_manager.getDicomSCPInstance(id).isEnabled(), HttpStatus.OK)
                                        : new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Sets the DICOM SCP receiver definition's enabled state.", notes = "Sets the enabled state of the DICOM SCP receiver definition with the specified ID to the value of the flag parameter.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition enabled status successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}/enabled/{flag}"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.PUT})
    public ResponseEntity<Void> dicomSCPInstanceSetEnabledFlag(@ApiParam(value = "ID of the DICOM SCP receiver definition to modify", required = true) @PathVariable("id") final int id,
                                                               @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable("flag") final Boolean flag) throws EnabledDICOMReceiverWithDuplicatePortException {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final DicomSCPInstance instanceToChange = _manager.getDicomSCPInstance(id);
        instanceToChange.setEnabled(flag);
        _manager.setDicomSCPInstance(instanceToChange);

        if(flag){
            _manager.startDicomSCP(id);
        }
        else{
            _manager.stopDicomSCP(id);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Starts all enabled DICOM SCP receivers.", notes = "This starts all enabled DICOM SCP receivers. The return value contains the definitions of all of the started receivers.", responseContainer = "List", response = DicomSCP.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receivers successfully started."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"start"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.PUT})
    public ResponseEntity<List<DicomSCP>> dicomSCPInstancesStart() {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return new ResponseEntity<>(_manager.startDicomSCPs(), HttpStatus.OK);
    }

    @ApiOperation(value = "Starts the DICOM SCP receiver.", notes = "This starts the DICOM SCP receiver. Note that this will start the receiver regardless of its enabled or disabled setting. This returns true if the instance was started and false if not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver successfully started."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}/start"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> dicomSCPInstanceStart(@ApiParam(value = "ID of the DICOM SCP receiver to start.", required = true) @PathVariable("id") final int id) {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        _manager.startDicomSCP(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @ApiOperation(value = "Stops all enabled DICOM SCP receivers.", notes = "This stops all enabled DICOM SCP receivers. The return value contains the definitions of all of the started receivers.", responseContainer = "List", response = DicomSCP.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receivers successfully stopped."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"stop"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.PUT})
    public ResponseEntity<List<DicomSCP>> dicomSCPInstancesStop() {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return new ResponseEntity<>(_manager.stopDicomSCPs(), HttpStatus.OK);
    }

    @ApiOperation(value = "Stops the DICOM SCP receiver.", notes = "This stops the DICOM SCP receiver. Note that this will stop the receiver regardless of its enabled or disabled setting. This returns true if the instance was stopped and false if not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver successfully stopped."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"{id}/stop"}, produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> dicomSCPInstanceStop(@ApiParam(value = "ID of the DICOM SCP receiver to stop.", required = true) @PathVariable("id") final int id) {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        _manager.stopDicomSCP(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    private static final Logger _log = LoggerFactory.getLogger(DicomSCPApi.class);

    private final DicomSCPManager _manager;
}
