package org.nrg.xapi.rest.dicomscp;

import io.swagger.annotations.*;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.rest.NotFoundException;
import org.nrg.xdat.rest.AbstractXnatRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

// @XnatRestlet({"/services/dicomscp", "/services/dicomscp/instance/{SCP_ID}", "/services/dicomscp/instance/{SCP_ID}/{ACTION}", "/services/dicomscp/{ACTION}"})

@Api(description = "XNAT DICOM SCP management API")
@RestController
@RequestMapping(value = "/dicomscp")
public class DicomSCPApi extends AbstractXnatRestApi {
    private static final Logger _log = LoggerFactory.getLogger(DicomSCPApi.class);

    @ApiOperation(value = "Get list of all configured DICOM SCP receiver definitions.", notes = "The primary DICOM SCP retrieval function returns a list of all DICOM SCP receivers defined for the current system.", response = DicomSCPInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of DICOM SCP receiver definitions."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {"application/json", "application/xml"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DicomSCPInstance>> dicomSCPsGet() {
        return new ResponseEntity<>(_manager.getDicomSCPInstances(), HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the DICOM SCP receiver definition with the specified ID.", notes = "Returns the DICOM SCP receiver definition with the specified ID.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}"}, produces = {"application/json", "application/xml", "text/html"}, method = {RequestMethod.GET})
    public ResponseEntity<DicomSCPInstance> dicomSCPInstanceGet(@ApiParam(value = "ID of the DICOM SCP receiver definition to fetch", required = true) @PathVariable("id") final String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return _manager.hasDicomSCP(id) ? new ResponseEntity<>(_manager.getDicomSCPInstance(id), HttpStatus.OK)
                                        : new ResponseEntity<DicomSCPInstance>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Creates or updates the DICOM SCP receiver definition object with the specified ID.", notes = "Returns the updated DICOM SCP receiver definition.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully created or updated."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to create or update this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}"}, produces = {"application/json", "application/xml", "text/html"}, method = {RequestMethod.PUT})
    public ResponseEntity<DicomSCPInstance> dicomSCPInstanceCreateOrUpdate(@ApiParam(value = "The ID of the DICOM SCP receiver definition to create or update.", required = true) @PathVariable("id") final String id, @RequestBody final DicomSCPInstance instance) throws NotFoundException {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            try {
                _manager.create(instance);
            } catch (NrgServiceException e) {
                _log.error("An error occurred trying to create a new DICOM SCP instance", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            _manager.setDicomSCPInstance(instance);
        }
        return new ResponseEntity<>(instance, HttpStatus.OK);
    }

    @ApiOperation(value = "Deletes the DICOM SCP receiver definition object with the specified ID.", notes = "This call will stop the receiver if it's currently running.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully created or updated."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to delete this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}"}, produces = {"application/json", "application/xml", "text/html"}, method = {RequestMethod.DELETE})
    public ResponseEntity<Void> dicomSCPInstanceDelete(@ApiParam(value = "The ID of the DICOM SCP receiver definition to delete.", required = true) @PathVariable("id") final String id) throws NotFoundException {
        HttpStatus status = isPermitted(id);
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
    @RequestMapping(value = {"/{id}/enabled"}, produces = {"application/json"}, method = {RequestMethod.GET})
    public ResponseEntity<Boolean> dicomSCPInstanceEnabledGet(@ApiParam(value = "The ID of the DICOM SCP receiver definition to retrieve the enabled status for.", required = true) @PathVariable("id") final String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return _manager.hasDicomSCP(id) ? new ResponseEntity<>(_manager.getDicomSCPInstance(id).isEnabled(), HttpStatus.OK)
                                        : new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Sets the DICOM SCP receiver definition's enabled state.", notes = "Sets the enabled state of the DICOM SCP receiver definition with the specified ID to the value of the flag parameter.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition enabled status successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/enabled/{flag}"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<Void> dicomSCPInstanceSetEnabledFlag(@ApiParam(value = "ID of the DICOM SCP receiver definition to modify", required = true) @PathVariable("id") final String id,
                                                               @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable("flag") final Boolean flag) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        _manager.getDicomSCPInstance(id).setEnabled(flag);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Starts all enabled DICOM SCP receivers.", notes = "This starts all enabled DICOM SCP receivers. The return value notes the number of ", responseContainer = "List", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receivers successfully started."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/start"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<List<String>> dicomSCPInstancesStart() {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return new ResponseEntity<>(_manager.startDicomSCPs(), HttpStatus.OK);
    }

    @ApiOperation(value = "Starts the DICOM SCP receiver.", notes = "This starts the DICOM SCP receiver. Note that this will start the receiver regardless of its enabled or disabled setting. This returns true if the instance was started and false if not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver successfully started."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/start"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> dicomSCPInstanceStart(@ApiParam(value = "ID of the DICOM SCP receiver to start.", required = true) @PathVariable("id") final String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        _manager.startDicomSCP(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @ApiOperation(value = "Stops all enabled DICOM SCP receivers.", notes = "This stops all enabled DICOM SCP receivers. The return value notes the number of ", responseContainer = "List", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receivers successfully stopped."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/stop"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<List<String>> dicomSCPInstancesStop() {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        return new ResponseEntity<>(_manager.stopDicomSCPs(), HttpStatus.OK);
    }

    @ApiOperation(value = "Stops the DICOM SCP receiver.", notes = "This stops the DICOM SCP receiver. Note that this will stop the receiver regardless of its enabled or disabled setting. This returns true if the instance was stopped and false if not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver successfully stopped."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."), @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/stop"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<Boolean> dicomSCPInstanceStop(@ApiParam(value = "ID of the DICOM SCP receiver to stop.", required = true) @PathVariable("id") final String id) {
        HttpStatus status = isPermitted(id);
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (!_manager.hasDicomSCP(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        _manager.stopDicomSCP(id);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @Inject
    private DicomSCPManager _manager;
}
