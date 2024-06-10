/*
 * web: org.nrg.xapi.rest.dicom.DicomSCPApi
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
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.security.helpers.Roles;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Api(description = "XNAT DICOM transaction management API")
@XapiRestController
@RequestMapping(value = "/dicom")
public class DicomTransactionApi extends AbstractXapiRestController {
    @Autowired
    public DicomTransactionApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final DicomInboxImportRequestService importRequestService) {
        super(userManagementService, roleHolder);
        _importRequestService = importRequestService;
    }

    @ApiOperation(value = "Get a list of all outstanding (i.e. not completed or failed) inbox import requests.", response = DicomInboxImportRequest.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "All outstanding inbox import requests are returned."),
                   @ApiResponse(code = 403, message = "The user has insufficient authorization to access the list of inbox import requests."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "list/active", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DicomInboxImportRequest>> getOutstandingDicomInboxImportRequests() {
        UserI user = getSessionUser();
        if(Roles.isSiteAdmin(user)){
            return new ResponseEntity<>(_importRequestService.getOutstandingDicomInboxImportRequests(), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(_importRequestService.getOutstandingDicomInboxImportRequestsForUser(user.getUsername()), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Get a list of all inbox import requests.", response = DicomInboxImportRequest.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "All inbox import requests are returned."),
            @ApiResponse(code = 403, message = "The user has insufficient authorization to access the list of inbox import requests."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "list/all", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DicomInboxImportRequest>> getDicomInboxImportRequests() {
        UserI user = getSessionUser();
        if(Roles.isSiteAdmin(user)){
            return new ResponseEntity<>(_importRequestService.getAll(), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(_importRequestService.getDicomInboxImportRequestsForUser(user.getUsername()), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Retrieves the requested inbox import request.", response = DicomInboxImportRequest.class)
    @ApiResponses({@ApiResponse(code = 200, message = "All outstanding inbox import requests are returned."),
                   @ApiResponse(code = 403, message = "The user has insufficient authorization to access the list of inbox import requests."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DicomInboxImportRequest> getDicomInboxImportRequest(@PathVariable final long id) {
        DicomInboxImportRequest request = _importRequestService.getDicomInboxImportRequest(id);
        UserI user = getSessionUser();
        if(request==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else if (Roles.isSiteAdmin(user) || StringUtils.equals(request.getUsername(),user.getUsername())) {
            return new ResponseEntity<>(request, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private final DicomInboxImportRequestService _importRequestService;
}
