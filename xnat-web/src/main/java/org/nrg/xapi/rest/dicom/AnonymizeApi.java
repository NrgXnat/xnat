/*
 * web: org.nrg.xapi.rest.dicom.AnonymizeApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.rest.AbstractXapiProjectRestController;
import org.nrg.xapi.rest.ProjectId;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.nrg.xdat.security.helpers.AccessLevel.*;

@Api(description = "XNAT DICOM Anonymization API")
@XapiRestController
@RequestMapping(value = "/anonymize")
public class AnonymizeApi extends AbstractXapiProjectRestController {
    @Autowired
    public AnonymizeApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final AnonUtils anonUtils, final SiteConfigPreferences preferences) {
        super(userManagementService, roleHolder);
        _anonUtils = anonUtils;
        _preferences = preferences;
    }

    @ApiOperation(value = "Gets the default anonymization script.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the contents of the default anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the default anonymization script."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "default", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET, restrictTo = Authenticated)
    @ResponseBody
    public ResponseEntity<String> getDefaultAnonScript() throws NrgServiceException {
        try {
            return new ResponseEntity<>(DefaultAnonUtils.getDefaultScript(), HttpStatus.OK);
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the default anonymization script.");
        }
    }

    @ApiOperation(value = "Gets the site-wide anonymization script.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the contents of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the site-wide anonymization script."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    @ResponseBody
    public ResponseEntity<String> getSiteWideAnonScript() throws ConfigServiceException {
        return new ResponseEntity<>(_anonUtils.getSiteWideScript(), HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the site-wide anonymization script.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully stored the contents of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the site-wide anonymization script."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site", consumes = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public ResponseEntity<Void> setSiteWideAnonScript(@RequestBody final String script) throws ConfigServiceException {
        _preferences.setSitewideAnonymizationScript(script);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Indicates whether the site-wide anonymization script is enabled or disabled.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the status of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the site-wide anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ResponseEntity<Boolean> isSiteWideAnonScriptEnabled() throws ConfigServiceException {
        return new ResponseEntity<>(_preferences.getEnableSitewideAnonymizationScript(), HttpStatus.OK);
    }

    @ApiOperation(value = "Enables or disables the site-wide anonymization script.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully set the status of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the site-wide anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public ResponseEntity<Void> setSiteWideAnonScriptEnabled(@ApiParam(value = "Whether the site-wide anonymization script should be enabled or disabled.", required = true) @RequestParam(required= false, defaultValue = "true") final boolean enable) throws ConfigServiceException {
        _preferences.setEnableSitewideAnonymizationScript(enable);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Gets the project-specific anonymization script.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the contents of the project-specific anonymization script."),
                   @ApiResponse(code = 204, message = "The specified project was found but had no associated anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the project-specific anonymization script."),
                   @ApiResponse(code = 404, message = "The specified project wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET, restrictTo = Read)
    @ResponseBody
    public ResponseEntity<String> getProjectAnonScript(@PathVariable("projectId") @ProjectId final String projectId) throws NrgServiceException, NoContentException {
        final String script = _anonUtils.getProjectScript(projectId);
        if (StringUtils.isBlank(script)) {
            throw new NoContentException("There's no anonymization script associated with the project " + projectId);
        }
        return new ResponseEntity<>(script, HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the project-specific anonymization script.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully stored the contents of the project-specific anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the project-specific anonymization script."),
                   @ApiResponse(code = 404, message = "The specified project wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}", consumes = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.PUT, restrictTo = Owner)
    public ResponseEntity<Void> setProjectAnonScript(@ApiParam(value = "Indicates the ID of the project to be enabled or disabled.", required = true)
                                                         @PathVariable("projectId")
                                                         @ProjectId final String projectId,
                                                     @ApiParam(value = "Whether the specified project's anonymization script should be enabled or disabled.", required = true)
                                                     @RequestBody final String script) throws NrgServiceException {
        try {
            _anonUtils.setProjectScript(getSessionUser().getUsername(), script, projectId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ConfigServiceException e) {
            _log.error("An error occurred when user " + getSessionUser().getUsername() + " tried to set an anonymization script for project " + projectId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Indicates whether the project-specific anonymization script is enabled or disabled.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the status of the project-specific anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the project-specific anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}/enabled", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Read)
    public ResponseEntity<Boolean> isProjectAnonScriptEnabled(@PathVariable("projectId") @ProjectId final String projectId) throws NrgServiceException {
        return new ResponseEntity<>(_anonUtils.isProjectScriptEnabled(projectId), HttpStatus.OK);
    }

    @ApiOperation(value = "Enables or disables the project-specific anonymization script.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully set the status of the project-specific anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the project-specific anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}/enabled", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Owner)
    public ResponseEntity<Void> setProjectAnonScriptEnabled(@PathVariable("projectId") @ProjectId final String projectId,
                                                            @RequestParam(required= false, defaultValue = "true") final boolean enable) throws NrgServiceException {
        if (enable) {
            _anonUtils.enableProjectSpecific(getSessionUser().getUsername(), projectId);
        } else {
            _anonUtils.disableProjectSpecific(getSessionUser().getUsername(), projectId);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private static final Logger _log = LoggerFactory.getLogger(AnonymizeApi.class);

    private final AnonUtils             _anonUtils;
    private final SiteConfigPreferences _preferences;
}
