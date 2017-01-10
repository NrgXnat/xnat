/*
 * web: org.nrg.xapi.rest.settings.AutomationApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import io.swagger.annotations.*;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.preferences.AutomationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Api(description = "Automation Service API")
@XapiRestController
@RequestMapping(value = "/automation")
public class AutomationApi extends AbstractXapiRestController {
    @Autowired
    public AutomationApi(final AutomationPreferences preferences, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
    }

    @ApiOperation(value = "Returns the full map of automation settings for this XNAT application.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Automation settings successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to retrieve the requested setting."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getAllAutomationPreferences() {
        if (_log.isDebugEnabled()) {
            _log.info("User " + getSessionUser().getUsername() + " requested the system automation settings.");
        }

        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        return new ResponseEntity<>(_preferences.getPreferenceMap(), HttpStatus.OK);
    }

    @ApiOperation(value = "Sets a map of automation properties.", notes = "Sets the automation properties specified in the map.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Automation properties successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set automation properties."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST)
    public ResponseEntity<Void> setBatchAutomationPreferences(@ApiParam(value = "The map of automation preferences to be set.", required = true) @RequestBody final Map<String, String> properties) {
        if (_log.isDebugEnabled()) {
            _log.info("User " + getSessionUser().getUsername() + " requested to set a batch of automation preferences.");
        }

        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        // Is this call initializing the system?
        for (final String name : properties.keySet()) {
            try {
                _preferences.set(properties.get(name), name);
                if (_log.isInfoEnabled()) {
                    _log.info("Set property {} to value: {}", name, properties.get(name));
                }
            } catch (InvalidPreferenceName invalidPreferenceName) {
                _log.error("Got an invalid preference name error for the preference: {}, failed to set value to: {}", name, properties.get(name));
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns whether internal scripting is enabled for this XNAT application.", notes = "Internal scripting may be used by XNAT itself even when disabled, but this setting indicates whether users and administrators can configure and execute scripts internally to the application process. Access to this setting is restricted to site administrators.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Internal scripting setting successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to retrieve the requested setting."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "enabled", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Boolean> isInternalScriptingEnabled() {
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the internal scripting enabled setting.");
        }

        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        return new ResponseEntity<>(_preferences.isInternalScriptingEnabled(), HttpStatus.OK);
    }

    @ApiOperation(value = "Sets the internal scripting enabled flag for this XNAT application to the submitted value.", notes = "Internal scripting may be used by XNAT itself even when disabled, but this setting indicates whether users and administrators can configure and execute scripts internally to the application process. Access to this setting is restricted to site administrators.", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Internal scripting setting successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to change the requested setting."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @RequestMapping(value = "enabled/{setting}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<Boolean> setInternalScriptingEnabled(@PathVariable("setting") final boolean setting) {
        if (_log.isDebugEnabled()) {
            _log.warn("User {} requested to change the internal scripting enabled setting to {}.", getSessionUser().getUsername(), setting);
        }

        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        // Only change the setting if they're different.
        if (_preferences.isInternalScriptingEnabled() != setting) {
            _preferences.setInternalScriptingEnabled(setting);
        }

        return new ResponseEntity<>(_preferences.isInternalScriptingEnabled(), HttpStatus.OK);
    }

    private static final Logger _log = LoggerFactory.getLogger(AutomationApi.class);

    private final AutomationPreferences _preferences;
}
