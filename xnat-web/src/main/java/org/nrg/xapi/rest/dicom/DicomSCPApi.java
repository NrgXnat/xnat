/*
 * web: org.nrg.xapi.rest.dicom.DicomSCPApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicatePropertiesException;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.DicomScpInvalidAeTitleException;
import org.nrg.dcm.scp.exceptions.DicomScpInvalidRoutingExpressionException;
import org.nrg.dcm.scp.exceptions.DicomScpInvalidWhitelistedItemException;
import org.nrg.dcm.scp.exceptions.DicomScpUnknownDOIException;
import org.nrg.dcm.scp.exceptions.DicomScpUnsupportedRoutingExpressionException;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Tag(name = "XNAT DICOM SCP management API")
@XapiRestController
@RequestMapping(value = "/dicomscp")
@Slf4j
public class DicomSCPApi extends AbstractXapiRestController {
    @Autowired
    public DicomSCPApi(final DicomSCPManager manager, final UserManagementServiceI userManagementService,
                       final RoleHolder roleHolder, final ObjectMapper objectMapper) {
        super(userManagementService, roleHolder);
        _manager = manager;
        _objectMapper = objectMapper;
    }

    @Operation(summary = "Get map of all configured DICOM object identifiers and names.", description = "This function returns a map of all DICOM object identifiers defined for the current system along with each identifier's readable name. The default identifier will be the first in the list.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A map of DICOM object identifiers and names."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "identifiers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getDicomObjectIdentifiers() {
        return _manager.getDicomObjectIdentifierBeans();
    }

    @Operation(summary = "Resets all configured DICOM object identifiers.", description = "This function resets all of the DICOM object identifiers defined for the current system. This causes each identifier to reload its configuration on next access.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The DICOM object identifiers were successfully reset."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "identifiers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    @ResponseBody
    public void resetDicomObjectIdentifiers() {
        _manager.resetDicomObjectIdentifierBeans();
    }

    @Operation(summary = "Get implementation name of the specified DICOM object identifier.", description = "This function returns the fully-qualified class name of the specified DICOM object identifier. You can use the value 'default' to retrieve the default identifier even if you don't know the specific name.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The implementation class of the specified DICOM object identifier."),
                   @ApiResponse(responseCode = "404", description = "No DICOM object identifier with the specified ID was found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
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

    @Operation(summary = "Resets the specified DICOM object identifier.", description = "This function resets the specified DICOM object identifier. This causes the identifier to reload its configuration on next access.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The DICOM object identifiers were successfully reset."),
                   @ApiResponse(responseCode = "404", description = "No DICOM object identifier with the specified ID was found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
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

    @Operation(summary = "Get list of all configured DICOM SCP receiver definitions.", description = "The primary DICOM SCP retrieval function returns a list of all DICOM SCP receivers defined for the current system.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "A list of DICOM SCP receiver definitions."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public List<DicomSCPInstance> getDicomSCPInstances() {
        return _manager.getDicomSCPInstancesList();
    }

    @Operation(summary = "Gets the DICOM SCP receiver definition with the specified ID.", description = "Returns the DICOM SCP receiver definition with the specified ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receiver definition successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    @ResponseBody
    public DicomSCPInstance getDicomSCPInstance(@Parameter(description = "ID of the DICOM SCP receiver definition to fetch", required = true) @PathVariable("id") final int id) throws NotFoundException {
        return _manager.getDicomSCPInstance(id);
    }

    @Operation(summary = "Gets the DICOM SCP receiver definition with the specified AE title and port.", description = "Returns the DICOM SCP receiver definition with the specified AE title and port.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receiver definition successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "title/{title}/{port}",
                        method = RequestMethod.GET,
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        restrictTo = Admin)
    public DicomSCPInstance getDicomSCPInstanceByTitleAndPort(@Parameter(description = "AE title of the DICOM SCP receiver definition to fetch", required = true) @PathVariable final String title,
                                                              @Parameter(description = "Port of the DICOM SCP receiver definition to fetch", required = true) @PathVariable final int port) throws NotFoundException {
        return _manager.getDicomSCPInstance(title, port);
    }

    @Operation(summary = "Creates a new DICOM SCP receiver from the request body.", description = "The newly created DICOM SCP receiver instance is returned from the call. This should include the instance ID for the new object.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "The newly created DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "409", description = "A DICOM SCP receiver already exists and is enabled at the same AE title and port."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(method = RequestMethod.POST,
                        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        restrictTo = Admin)
    @ResponseBody
    public DicomSCPInstance createDicomSCPInstance(@RequestBody final DicomSCPInstance instance) throws DICOMReceiverWithDuplicatePropertiesException, DicomNetworkException, UnknownDicomHelperInstanceException, DicomScpInvalidWhitelistedItemException, DicomScpInvalidAeTitleException, DicomScpInvalidRoutingExpressionException, DicomScpUnsupportedRoutingExpressionException, DicomScpUnknownDOIException, GeneralSecurityException {
        return _manager.saveDicomSCPInstance(instance);
    }

    @Operation(summary = "Updates the DICOM SCP receiver definition object with the ID specified in the path variable. Note that any ID specified in the serialized definition in the request body is ignored and set to the value from the path variable.", description = "Returns the updated DICOM SCP receiver definition.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receiver definition successfully updated."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to create or update this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{id}",
                        method = RequestMethod.PUT,
                        restrictTo = Admin,
                        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
                        produces = MediaType.APPLICATION_JSON_VALUE)
    public DicomSCPInstance updateDicomSCPInstance(@Parameter(description = "The ID of the DICOM SCP receiver definition to update.", required = true) @PathVariable("id") final int id,
                                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                                                       mediaType = "application/json",
                                                       examples = @ExampleObject(value = "{\"aeTitle\": \"TITLE\", \"port\": 8104, \"enabled\": true}")
                                                   ))
                                                   @RequestBody final String instanceJson) throws NotFoundException, DICOMReceiverWithDuplicatePropertiesException, DicomScpInvalidAeTitleException, DicomScpInvalidWhitelistedItemException, DicomScpInvalidRoutingExpressionException, DicomNetworkException, UnknownDicomHelperInstanceException, DicomScpUnsupportedRoutingExpressionException, DicomScpUnknownDOIException, JsonProcessingException, GeneralSecurityException {
        // Set the ID to the value specified in the REST call. If ID not specified on PUT, value will be zero, so we
        // need to make sure it's set to the proper value. If they submit it under the wrong ID well...
        try {
            DicomSCPInstance existing = _manager.findById(id);
            DicomSCPInstance updatedInstance = _objectMapper.readerForUpdating(existing).readValue(instanceJson);
            return _manager.update(updatedInstance, false);
        } catch(Exception e) {
            log.error("Encountered exception while updating dicom scp instance {}", id, e);
            throw e;
        }
    }

    @Operation(summary = "Deletes the DICOM SCP receiver definition object with the specified ID.", description = "This call will stop the receiver if it's currently running.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receiver definition successfully deleted."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to delete this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{id}",
                        method = RequestMethod.DELETE,
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        restrictTo = Admin)
    public void deleteDicomSCPInstance(@Parameter(description = "The ID of the DICOM SCP receiver definition to delete.", required = true) @PathVariable("id") final int id) throws NotFoundException, DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        _manager.deleteDicomSCPInstance(id);
    }

    @Operation(summary = "Returns whether the DICOM SCP receiver definition with the specified ID is enabled.", description = "Returns true or false based on whether the specified DICOM SCP receiver definition is enabled or not.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receiver definition enabled status successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to view this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{id}/enabled", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public boolean getDicomSCPInstanceEnabled(@Parameter(description = "The ID of the DICOM SCP receiver definition to retrieve the enabled status for.", required = true) @PathVariable("id") final int id) throws NotFoundException {
        return _manager.getDicomSCPInstance(id).isEnabled();
    }

    @Operation(summary = "Sets the DICOM SCP receiver definition's enabled state.", description = "Sets the enabled state of the DICOM SCP receiver definition with the specified ID to the value of the flag parameter.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receiver definition enabled status successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to enable or disable this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{id}/enabled/{flag}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public DicomSCPInstance enableDicomSCPInstance(@Parameter(description = "ID of the DICOM SCP receiver definition to modify", required = true) @PathVariable("id") final int id,
                                                   @Parameter(description = "The value to set for the enabled status.", required = true) @PathVariable("flag") final Boolean flag) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException, IOException {
        return flag ? _manager.enableDicomSCPInstance(id) : _manager.disableDicomSCPInstance(id);
    }

    @Operation(summary = "Starts all enabled DICOM SCP receivers.", description = "This starts all enabled DICOM SCP receivers. The return value contains the AE titles and ports of all of the started receivers.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receivers successfully started."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to enable or disable this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "start", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public List<Triple<String, Integer, Boolean>> startAll() throws UnknownDicomHelperInstanceException, DicomNetworkException, GeneralSecurityException {
        return _manager.start();
    }

    @Operation(summary = "Stops all enabled DICOM SCP receivers.", description = "This stops all enabled DICOM SCP receivers. The return value contains the AE titles of all of the stopped receivers.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "DICOM SCP receivers successfully stopped."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to enable or disable this DICOM SCP receiver definition."),
                   @ApiResponse(responseCode = "404", description = "DICOM SCP receiver definition not found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "stop", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public List<Triple<String, Integer, Boolean>> stopDicomSCPInstances() throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        return _manager.stop();
    }

    private final DicomSCPManager _manager;
    private final ObjectMapper _objectMapper;
}
