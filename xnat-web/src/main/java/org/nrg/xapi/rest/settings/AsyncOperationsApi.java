/*
 * web: org.nrg.xapi.rest.settings.PreferencesApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Properties;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api(description = "Asynchronous Operations Preferences API")
@XapiRestController
@RequestMapping(value = "/asyncOps")
@Slf4j
public class AsyncOperationsApi extends AbstractXapiRestController {
    @Autowired
    public AsyncOperationsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final AsyncOperationsPreferences preferences) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
    }

    @ApiOperation(value = "Returns the full map of async operations preferences for this XNAT application.", response = Properties.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Async operations preferences successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to retrieve the requested settings."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Properties getAsyncOperationsPreferences() {
        log.info("User {} requested the async operations preferences.", getSessionUser().getUsername());
        return _preferences.asProperties();
    }

    @ApiOperation(value = "Sets a map of async operations preferences.", notes = "Sets the async operations preferences specified in the map.", response = Integer.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Async operations preferences successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set async operations preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public int setAsyncOperationsPreferences(@ApiParam(value = "The map of async operations preferences to be set.", required = true) @RequestBody final Map<String, Object> preferences) throws NoContentException, InvalidPreferenceName {
        if (preferences.isEmpty()) {
            throw new NoContentException("You must specify one or more async operations preferences to be set.");
        }

        return _preferences.setBatch(Maps.transformValues(preferences, new Function<Object, String>() {
            @Override
            public String apply(@Nullable final Object value) {
                return value == null ? "" : value.toString();
            }
        })).size();
    }

    @ApiOperation(value = "Returns the value of a particular async operations preference.")
    @ApiResponses({@ApiResponse(code = 200, message = "Preference settings successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to retrieve the requested setting."),
                   @ApiResponse(code = 404, message = "Tool ID not found in the system."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{preference}", produces = APPLICATION_JSON_VALUE, restrictTo = Admin)
    public String getAsyncOperationsPreference(@PathVariable final String preference) throws NotFoundException {
        log.info("User {} requested the value for the async operations preference {}.", getSessionUser().getUsername(), preference);
        final Properties properties = _preferences.asProperties();
        if (properties.containsKey(preference)) {
            return properties.getProperty(preference);
        }
        throw new NotFoundException("There is no preference with the name " + preference + " associated with the async operations preferences.");
    }

    @ApiOperation(value = "Sets the value for the indicated async operations preference.", notes = "This method returns the previously set value for the indicated preference.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Preference value successfully stored."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Insufficient privileges to retrieve the requested setting."),
                   @ApiResponse(code = 404, message = "Tool ID not found in the system."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{preference}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public String getAsyncOperationsPreference(@PathVariable final String preference, final @RequestBody String value) throws NotFoundException {
        log.info("User {} is setting the value for the async operations preference {} to '{}'.", getSessionUser().getUsername(), preference, value);
        try {
            final String oldValue = _preferences.asProperties().getProperty(preference);
            _preferences.set(value, preference);
            return oldValue;
        } catch (InvalidPreferenceName invalidPreferenceName) {
            throw new NotFoundException("There is no preference with the name " + preference + " associated with the async operations preferences.");
        }
    }

    private final AsyncOperationsPreferences _preferences;
}
