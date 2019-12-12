/*
 * web: org.nrg.xapi.rest.dicom.DicomSCPApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicatePropertiesException;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api("XNAT DICOM SCP management API")
@XapiRestController
@RequestMapping(value = "/dicomscp")
@Slf4j
public class DicomSCPApi extends AbstractXapiRestController {
    @Autowired
    public DicomSCPApi(final DicomSCPManager manager, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _manager = manager;
    }

    @ApiOperation(value = "Get map of all configured DICOM object identifiers and names.", notes = "This function returns a map of all DICOM object identifiers defined for the current system along with each identifier's readable name. The default identifier will be the first in the list.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "A map of DICOM object identifiers and names."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "identifiers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getDicomObjectIdentifiers() {
        return _manager.getDicomObjectIdentifierBeans();
    }

    @ApiOperation(value = "Resets all configured DICOM object identifiers.", notes = "This function resets all of the DICOM object identifiers defined for the current system. This causes each identifier to reload its configuration on next access.")
    @ApiResponses({@ApiResponse(code = 200, message = "The DICOM object identifiers were successfully reset."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "identifiers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    @ResponseBody
    public void resetDicomObjectIdentifiers() {
        _manager.resetDicomObjectIdentifierBeans();
    }

    @ApiOperation(value = "Get implementation name of the specified DICOM object identifier.", notes = "This function returns the fully-qualified class name of the specified DICOM object identifier. You can use the value 'default' to retrieve the default identifier even if you don't know the specific name.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The implementation class of the specified DICOM object identifier."),
                   @ApiResponse(code = 404, message = "No DICOM object identifier with the specified ID was found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "identifiers/{beanId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public String getDicomObjectIdentifier(@PathVariable("beanId") String beanId) throws NotFoundException {
        // If they specified "default", then get the first bean in the list: they're sorted so that the default is first.
        if (StringUtils.equals("default", beanId)) {
            return _manager.getDefaultDicomObjectIdentifier().getClass().getName();
        }
        final DicomObjectIdentifier<XnatProjectdata> identifier = _manager.getDicomObjectIdentifier(beanId);
        if (identifier == null) {
            throw new NotFoundException("Couldn't find DICOM object identifier with ID " + beanId);
        }
        return identifier.getClass().getName();
    }

    @ApiOperation(value = "Resets the specified DICOM object identifier.", notes = "This function resets the specified DICOM object identifier. This causes the identifier to reload its configuration on next access.")
    @ApiResponses({@ApiResponse(code = 200, message = "The DICOM object identifiers were successfully reset."),
                   @ApiResponse(code = 404, message = "No DICOM object identifier with the specified ID was found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "identifiers/{beanId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    @ResponseBody
    public void resetDicomObjectIdentifier(@PathVariable("beanId") String beanId) throws NotFoundException {
        // If they specified "default", then get the first bean in the list: they're sorted so that the default is first.
        if (StringUtils.equals("default", beanId)) {
            _manager.resetDicomObjectIdentifier();
        } else {
            final DicomObjectIdentifier<XnatProjectdata> identifier = _manager.getDicomObjectIdentifier(beanId);
            if (identifier == null) {
                throw new NotFoundException("Couldn't find DICOM object identifier with ID " + beanId);
            }
            _manager.resetDicomObjectIdentifier(beanId);
        }
    }

    @ApiOperation(value = "Get list of all configured DICOM SCP receiver definitions.", notes = "The primary DICOM SCP retrieval function returns a list of all DICOM SCP receivers defined for the current system.", response = DicomSCPInstance.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of DICOM SCP receiver definitions."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public List<DicomSCPInstance> getDicomSCPInstances() {
        return _manager.getDicomSCPInstancesList();
    }

    @ApiOperation(value = "Gets the DICOM SCP receiver definition with the specified ID.", notes = "Returns the DICOM SCP receiver definition with the specified ID.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public DicomSCPInstance getDicomSCPInstance(@ApiParam(value = "ID of the DICOM SCP receiver definition to fetch", required = true) @PathVariable("id") final int id) throws NotFoundException {
        return _manager.getDicomSCPInstance(id);
    }

    @ApiOperation(value = "Gets the DICOM SCP receiver definition with the specified AE title and port.", notes = "Returns the DICOM SCP receiver definition with the specified AE title and port.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "title/{title}/{port}",
                        method = RequestMethod.GET,
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        restrictTo = Admin)
    public DicomSCPInstance getDicomSCPInstanceByTitleAndPort(@ApiParam(value = "AE title of the DICOM SCP receiver definition to fetch", required = true) @PathVariable final String title,
                                                              @ApiParam(value = "Port of the DICOM SCP receiver definition to fetch", required = true) @PathVariable final int port) throws NotFoundException {
        return _manager.getDicomSCPInstance(title, port);
    }

    @ApiOperation(value = "Creates a new DICOM SCP receiver from the request body.", notes = "The newly created DICOM SCP receiver instance is returned from the call. This should include the instance ID for the new object.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The newly created DICOM SCP receiver definition."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(code = 409, message = "A DICOM SCP receiver already exists and is enabled at the same AE title and port."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(method = RequestMethod.POST,
                        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        restrictTo = Admin)
    @ResponseBody
    public DicomSCPInstance createDicomSCPInstance(@RequestBody final DicomSCPInstance instance) throws DICOMReceiverWithDuplicatePropertiesException, DicomNetworkException, UnknownDicomHelperInstanceException {
        return _manager.saveDicomSCPInstance(instance);
    }

    @ApiOperation(value = "Updates the DICOM SCP receiver definition object with the ID specified in the path variable. Note that any ID specified in the serialized definition in the request body is ignored and set to the value from the path variable.",
                  notes = "Returns the updated DICOM SCP receiver definition.", response = DicomSCPInstance.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to create or update this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}",
                        method = RequestMethod.PUT,
                        restrictTo = Admin,
                        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
                        produces = MediaType.APPLICATION_JSON_VALUE)
    public DicomSCPInstance updateDicomSCPInstance(@ApiParam(value = "The ID of the DICOM SCP receiver definition to update.", required = true) @PathVariable("id") final int id,
                                                   @ApiParam(examples = @Example(value = {
                                                       @ExampleProperty(
                                                           mediaType = "application/json",
                                                           value = "{\"aeTitle\": \"TITLE\", \"port\": 8104, \"enabled\": true}"
                                                       )
                                                   }))
                                                   @RequestBody final DicomSCPInstance instance) throws NotFoundException, DICOMReceiverWithDuplicatePropertiesException, UnknownDicomHelperInstanceException, DicomNetworkException {
        if (_manager.hasDicomSCPInstance(id)) {
            throw new NotFoundException("Could not find DICOM SCP instance with ID " + id);
        }
        // Set the ID to the value specified in the REST call. If ID not specified on PUT, value will be zero, so we
        // need to make sure it's set to the proper value. If they submit it under the wrong ID well...
        instance.setId(id);
        return _manager.saveDicomSCPInstance(instance);
    }

    @ApiOperation(value = "Deletes the DICOM SCP receiver definition object with the specified ID.", notes = "This call will stop the receiver if it's currently running.")
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to delete this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}",
                        method = RequestMethod.DELETE,
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        restrictTo = Admin)
    public void deleteDicomSCPInstance(@ApiParam(value = "The ID of the DICOM SCP receiver definition to delete.", required = true) @PathVariable("id") final int id) throws NotFoundException, DicomNetworkException, UnknownDicomHelperInstanceException {
        if (!_manager.hasDicomSCPInstance(id)) {
            throw new NotFoundException("Could not find DICOM SCP instance with ID " + id);
        }
        _manager.deleteDicomSCPInstance(id);
    }

    @ApiOperation(value = "Returns whether the DICOM SCP receiver definition with the specified ID is enabled.", notes = "Returns true or false based on whether the specified DICOM SCP receiver definition is enabled or not.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition enabled status successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}/enabled", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public boolean getDicomSCPInstanceEnabled(@ApiParam(value = "The ID of the DICOM SCP receiver definition to retrieve the enabled status for.", required = true) @PathVariable("id") final int id) throws NotFoundException {
        return _manager.getDicomSCPInstance(id).isEnabled();
    }

    @ApiOperation(value = "Sets the DICOM SCP receiver definition's enabled state.", notes = "Sets the enabled state of the DICOM SCP receiver definition with the specified ID to the value of the flag parameter.")
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receiver definition enabled status successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{id}/enabled/{flag}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public DicomSCPInstance enableDicomSCPInstance(@ApiParam(value = "ID of the DICOM SCP receiver definition to modify", required = true) @PathVariable("id") final int id,
                                                   @ApiParam(value = "The value to set for the enabled status.", required = true) @PathVariable("flag") final Boolean flag) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException {
        return flag ? _manager.enableDicomSCPInstance(id) : _manager.disableDicomSCPInstance(id);
    }

    @ApiOperation(value = "Starts all enabled DICOM SCP receivers.", notes = "This starts all enabled DICOM SCP receivers. The return value contains the AE titles and ports of all of the started receivers.", responseContainer = "List", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receivers successfully started."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "start", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public List<Triple<String, Integer, Boolean>> startAll() throws UnknownDicomHelperInstanceException, DicomNetworkException {
        return _manager.start();
    }

    @ApiOperation(value = "Stops all enabled DICOM SCP receivers.", notes = "This stops all enabled DICOM SCP receivers. The return value contains the AE titles of all of the stopped receivers.", responseContainer = "List", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "DICOM SCP receivers successfully stopped."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to enable or disable this DICOM SCP receiver definition."),
                   @ApiResponse(code = 404, message = "DICOM SCP receiver definition not found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "stop", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public List<Triple<String, Integer, Boolean>> stopDicomSCPInstances() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return _manager.stop();
    }

    private final DicomSCPManager _manager;
}
