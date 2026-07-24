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
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.rest.AbstractXapiProjectRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
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
import java.util.HashMap;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.nrg.xnat.helpers.merge.AnonUtils.ENABLE_SITEWIDE_ANONYMIZATION_SCRIPT;
import static org.nrg.xnat.helpers.merge.AnonUtils.SITEWIDE_ANONYMIZATION_SCRIPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Api("XNAT DICOM Anonymization API")
@XapiRestController
@RequestMapping(value = "/anonymize")
@Slf4j
public class AnonymizeApi extends AbstractXapiProjectRestController {
    @Autowired
    public AnonymizeApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final AnonUtils anonUtils) {
        super(userManagementService, roleHolder);
        _anonUtils = anonUtils;
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
    public void setSiteWideAnonScript(@RequestBody final String script) throws InitializationException {
        try {
            _anonUtils.setSiteWideScript(getSessionUser().getUsername(), script);
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to set the site-wide anonymization script, but an error occurred. The submitted script contained the following:\n\n{}", getSessionUser().getUsername(), script, e);
            throw new InitializationException("An error occurred trying to set the site-wide anonymization script");
        }
    }

    @ApiOperation(value = "Gets the site-wide anonymization settings.", notes = "Returns the script contents and enabled state from the config service, which holds the values actually applied to incoming DICOM.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the site-wide anonymization settings."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the site-wide anonymization settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "settings", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Map<String, String> getSiteWideAnonSettings() {
        final Configuration       configuration = _anonUtils.getSiteWideScriptConfiguration();
        final Map<String, String> settings      = new HashMap<>();
        settings.put(SITEWIDE_ANONYMIZATION_SCRIPT, configuration != null ? StringUtils.defaultString(configuration.getContents()) : "");
        settings.put(ENABLE_SITEWIDE_ANONYMIZATION_SCRIPT, Boolean.toString(configuration != null && StringUtils.equals(configuration.getStatus(), Configuration.ENABLED_STRING)));
        return settings;
    }

    @ApiOperation(value = "Sets the site-wide anonymization settings.", notes = "Accepts the sitewideAnonymizationScript and enableSitewideAnonymizationScript values as a map. Omitted or null values are left unchanged. The values are written to the config service with the current user and mirrored to the site-config preferences.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully stored the site-wide anonymization settings."),
                   @ApiResponse(code = 400, message = "The enableSitewideAnonymizationScript value was not a boolean."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the site-wide anonymization settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "settings", consumes = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public void setSiteWideAnonSettings(@RequestBody final Map<String, Object> settings) throws DataFormatException, InitializationException {
        // JSON null values are treated the same as omitted keys — "leave unchanged" — so clients that
        // serialize absent fields as explicit nulls can't accidentally clear the script or disable
        // anonymization.
        final Object  scriptValue = settings.get(SITEWIDE_ANONYMIZATION_SCRIPT);
        final String  script      = scriptValue != null ? scriptValue.toString() : null;
        final Boolean enable      = parseEnable(settings.get(ENABLE_SITEWIDE_ANONYMIZATION_SCRIPT));
        try {
            _anonUtils.setSiteWideSettings(getSessionUser().getUsername(), script, enable);
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to set the site-wide anonymization settings, but an error occurred", getSessionUser().getUsername(), e);
            throw new InitializationException("An error occurred trying to set the site-wide anonymization settings");
        }
    }

    @ApiOperation(value = "Indicates whether the site-wide anonymization script is enabled or disabled.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved the status of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to access the site-wide anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public boolean isSiteWideAnonScriptEnabled() {
        // Read the config service state: it's the copy applied to incoming DICOM.
        return _anonUtils.isSiteWideScriptEnabled();
    }

    @ApiOperation(value = "Enables or disables the site-wide anonymization script.")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully set the status of the site-wide anonymization script."),
                   @ApiResponse(code = 403, message = "Insufficient permissions to modify the site-wide anonymization script settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "site/enabled", consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public void setSiteWideAnonScriptEnabled(@ApiParam(value = "Whether the site-wide anonymization script should be enabled or disabled.", defaultValue = "true") @RequestParam(required = false, defaultValue = "true") final boolean enable) throws InitializationException {
        try {
            _anonUtils.setSiteWideSettings(getSessionUser().getUsername(), null, enable);
        } catch (ConfigServiceException e) {
            log.error("The user {} tried to {} the site-wide anonymization script, but an error occurred", getSessionUser().getUsername(), enable ? "enable" : "disable", e);
            throw new InitializationException("An error occurred trying to set the site-wide anonymization script status");
        }
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

    /**
     * Strictly parses the enable flag: null (absent or JSON null) means "leave unchanged", and anything
     * other than true/false is rejected rather than silently coerced to false — this flag turns
     * de-identification of incoming DICOM on and off.
     */
    private static Boolean parseEnable(final Object value) throws DataFormatException {
        if (value == null) {
            return null;
        }
        final String text = value.toString();
        if ("true".equalsIgnoreCase(text)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(text)) {
            return Boolean.FALSE;
        }
        throw new DataFormatException("The " + ENABLE_SITEWIDE_ANONYMIZATION_SCRIPT + " value must be either true or false: " + text);
    }

    private final AnonUtils _anonUtils;
}
