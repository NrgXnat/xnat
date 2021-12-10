/*
 * web: org.nrg.xapi.rest.dicom.AnonymizeApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.rest.AbstractXapiProjectRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Api("XNAT DICOM Anonymization API")
@XapiRestController
@RequestMapping(value = "/anonymize")
@Slf4j
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
    @XapiRequestMapping(value = "default", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = Authenticated)
    public String getDefaultAnonScript() throws InitializationException {
        try {
            return DefaultAnonUtils.getDefaultScript();
        } catch (IOException e) {
            log.error("The user {} tried to retrieve the default anonymization script, but an error occurred", getSessionUser().getUsername(), e);
            throw new InitializationException("An error occurred trying to retrieve the default anonymization script");
        }
    }

    @ApiOperation(value = "Gets the site-wide anonymization script.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the contents of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the site-wide anonymization script."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = Authenticated)
    public String getSiteWideAnonScript() throws InitializationException {
        try {
            return _anonUtils.getSiteWideScript();
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to retrieve the site-wide anonymization script, but an error occurred", getSessionUser().getUsername(), e);
            throw new InitializationException("An error occurred trying to retrieve the site-wide anonymization script");
        }
    }

    @ApiOperation(value = "Sets the site-wide anonymization script.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully stored the contents of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the site-wide anonymization script."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site", consumes = TEXT_PLAIN_VALUE, method = PUT, restrictTo = Admin)
    public void setSiteWideAnonScript(@RequestBody final String script) {
        _preferences.setSitewideAnonymizationScript(script);
    }

    @ApiOperation(value = "Indicates whether the site-wide anonymization script is enabled or disabled.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the status of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the site-wide anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public boolean isSiteWideAnonScriptEnabled() {
        return _preferences.getEnableSitewideAnonymizationScript();
    }

    @ApiOperation(value = "Enables or disables the site-wide anonymization script.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully set the status of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the site-wide anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public void setSiteWideAnonScriptEnabled(@ApiParam(value = "Whether the site-wide anonymization script should be enabled or disabled.", defaultValue = "true") @RequestParam(required = false, defaultValue = "true") final boolean enable) {
        _preferences.setEnableSitewideAnonymizationScript(enable);
    }

    @ApiOperation(value = "Gets the project-specific anonymization script.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the contents of the project-specific anonymization script."),
                   @ApiResponse(code = 204, message = "The specified project was found but had no associated anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the project-specific anonymization script."),
                   @ApiResponse(code = 404, message = "The specified project wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = Read)
    public String getProjectAnonScript(@PathVariable("projectId") @Project final String projectId) throws NoContentException, InitializationException {
        try {
            final String script = _anonUtils.getProjectScript(projectId);
            if (StringUtils.isBlank(script)) {
                throw new NoContentException("There's no anonymization script associated with the project " + projectId);
            }
            return script;
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to retrieve the anonymization script for the project {}, but an error occurred", getSessionUser().getUsername(), projectId, e);
            throw new InitializationException("An error occurred trying to retrieve the anonymization script for the project " + projectId);
        }
    }

    @ApiOperation(value = "Sets the project-specific anonymization script.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully stored the contents of the project-specific anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the project-specific anonymization script."),
                   @ApiResponse(code = 404, message = "The specified project wasn't found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}", consumes = TEXT_PLAIN_VALUE, method = PUT, restrictTo = Delete)
    public void setProjectAnonScript(@ApiParam(value = "Indicates the ID of the project for which the anonymization script should be enabled or disabled.", required = true) @PathVariable("projectId") @Project final String projectId,
                                     @ApiParam(value = "Whether the specified project's anonymization script should be enabled or disabled.", required = true) @RequestBody final String script) throws InitializationException {
        try {
            _anonUtils.setProjectScript(getSessionUser().getUsername(), script, projectId);
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to set the anonymization script for the project {}, but an error occurred. The submitted script contained the following:\n\n{}", getSessionUser().getUsername(), projectId, script, e);
            throw new InitializationException("An error occurred trying to set the anonymization script for the project " + projectId);
        }
    }

    @ApiOperation(value = "Indicates whether the project-specific anonymization script is enabled or disabled.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the status of the project-specific anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the project-specific anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Read)
    public boolean isProjectAnonScriptEnabled(@PathVariable("projectId") @Project final String projectId) {
        return _anonUtils.isProjectScriptEnabled(projectId);
    }

    @ApiOperation(value = "Enables or disables the project-specific anonymization script.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully set the status of the project-specific anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the project-specific anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}/enabled", consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Delete)
    public void setProjectAnonScriptEnabled(@PathVariable("projectId") @Project final String projectId,
                                            @RequestParam(required = false, defaultValue = "true") final boolean enable) throws NrgServiceException {
        if (enable) {
            _anonUtils.enableProjectSpecific(getSessionUser().getUsername(), projectId);
        } else {
            _anonUtils.disableProjectSpecific(getSessionUser().getUsername(), projectId);
        }
    }

    private final AnonUtils             _anonUtils;
    private final SiteConfigPreferences _preferences;
}
