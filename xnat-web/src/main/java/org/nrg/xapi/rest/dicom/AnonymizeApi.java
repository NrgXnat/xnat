/*
 * web: org.nrg.xapi.rest.dicom.AnonymizeApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.dicom;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "XNAT DICOM Anonymization API")
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

    @Operation(summary = "Gets the default anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the contents of the default anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access the default anonymization script."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "default", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = Authenticated)
    public String getDefaultAnonScript() throws InitializationException {
        try {
            return DefaultAnonUtils.getDefaultScript();
        } catch (IOException e) {
            log.error("The user {} tried to retrieve the default anonymization script, but an error occurred", getSessionUser().getUsername(), e);
            throw new InitializationException("An error occurred trying to retrieve the default anonymization script");
        }
    }

    @Operation(summary = "Gets the site-wide anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the contents of the site-wide anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access the site-wide anonymization script."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site", produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = Authenticated)
    public String getSiteWideAnonScript() throws InitializationException {
        try {
            return _anonUtils.getSiteWideScript();
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to retrieve the site-wide anonymization script, but an error occurred", getSessionUser().getUsername(), e);
            throw new InitializationException("An error occurred trying to retrieve the site-wide anonymization script");
        }
    }

    @Operation(summary = "Sets the site-wide anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully stored the contents of the site-wide anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to modify the site-wide anonymization script."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site", consumes = TEXT_PLAIN_VALUE, method = PUT, restrictTo = Admin)
    public void setSiteWideAnonScript(@RequestBody final String script) {
        _preferences.setSitewideAnonymizationScript(script);
    }

    @Operation(summary = "Indicates whether the site-wide anonymization script is enabled or disabled.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the status of the site-wide anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access the site-wide anonymization script settings."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public boolean isSiteWideAnonScriptEnabled() {
        return _preferences.getEnableSitewideAnonymizationScript();
    }

    @Operation(summary = "Enables or disables the site-wide anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully set the status of the site-wide anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to modify the site-wide anonymization script settings."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public void setSiteWideAnonScriptEnabled(@Parameter(description = "Whether the site-wide anonymization script should be enabled or disabled.", schema = @Schema(defaultValue = "true")) @RequestParam(required = false, defaultValue = "true") final boolean enable) {
        _preferences.setEnableSitewideAnonymizationScript(enable);
    }

    @Operation(summary = "Gets the project-specific anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the contents of the project-specific anonymization script."),
                   @ApiResponse(responseCode = "204", description = "The specified project was found but had no associated anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access the project-specific anonymization script."),
                   @ApiResponse(responseCode = "404", description = "The specified project wasn't found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
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

    @Operation(summary = "Sets the project-specific anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully stored the contents of the project-specific anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to modify the project-specific anonymization script."),
                   @ApiResponse(responseCode = "404", description = "The specified project wasn't found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}", consumes = TEXT_PLAIN_VALUE, method = PUT, restrictTo = Delete)
    public void setProjectAnonScript(@Parameter(description = "Indicates the ID of the project for which the anonymization script should be enabled or disabled.", required = true) @PathVariable("projectId") @Project final String projectId,
                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Whether the specified project's anonymization script should be enabled or disabled.", required = true) @RequestBody final String script) throws InitializationException {
        try {
            _anonUtils.setProjectScript(getSessionUser().getUsername(), script, projectId);
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to set the anonymization script for the project {}, but an error occurred. The submitted script contained the following:\n\n{}", getSessionUser().getUsername(), projectId, script, e);
            throw new InitializationException("An error occurred trying to set the anonymization script for the project " + projectId);
        }
    }

    @Operation(summary = "Indicates whether the project-specific anonymization script is enabled or disabled.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the status of the project-specific anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access the project-specific anonymization script settings."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "projects/{projectId}/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Read)
    public boolean isProjectAnonScriptEnabled(@PathVariable("projectId") @Project final String projectId) {
        return _anonUtils.isProjectScriptEnabled(projectId);
    }

    @Operation(summary = "Enables or disables the project-specific anonymization script.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully set the status of the project-specific anonymization script."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to modify the project-specific anonymization script settings."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
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
