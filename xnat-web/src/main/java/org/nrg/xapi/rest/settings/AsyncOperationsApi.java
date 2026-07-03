/*
 * web: org.nrg.xapi.rest.settings.PreferencesApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.DataFormatException;
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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Tag(name = "async-operations", description = "Asynchronous Operations Preferences API")
@XapiRestController
@RequestMapping(value = "/asyncOps")
@Slf4j
public class AsyncOperationsApi extends AbstractXapiRestController {
    @Autowired
    public AsyncOperationsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final AsyncOperationsPreferences preferences) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
    }

    @Operation(summary = "Returns the full map of async operations preferences for this XNAT application.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Async operations preferences successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Insufficient privileges to retrieve the requested settings."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Properties getAsyncOperationsPreferences() {
        log.info("User {} requested the async operations preferences.", getSessionUser().getUsername());
        return _preferences.asProperties();
    }

    @Operation(summary = "Sets a map of async operations preferences.", description = "Sets the async operations preferences specified in the map.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Async operations preferences successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set async operations preferences."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public int setAsyncOperationsPreferences(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The map of async operations preferences to be set.", required = true) @RequestBody final Map<String, Object> preferences) throws NoContentException, InvalidPreferenceName, DataFormatException {
        if (preferences.isEmpty()) {
            throw new NoContentException("You must specify one or more async operations preferences to be set.");
        }

        validateAsyncOperationsPreferences(preferences);

        final AtomicInteger count = new AtomicInteger();
        for (final Map.Entry<String, Object> entry : preferences.entrySet()) {
            final String value   = ObjectUtils.defaultIfNull(entry.getValue(), "").toString();
            final String current = _preferences.set(value, entry.getKey());
            if (!StringUtils.equals(value, current)) {
                count.incrementAndGet();
            }
        }
        return count.get();
    }

    @Operation(summary = "Returns the value of a particular async operations preference.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Preference settings successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Insufficient privileges to retrieve the requested setting."),
                   @ApiResponse(responseCode = "404", description = "Tool ID not found in the system."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{preference}", produces = APPLICATION_JSON_VALUE, restrictTo = Admin)
    public String getAsyncOperationsPreference(@PathVariable final String preference) throws NotFoundException {
        log.info("User {} requested the value for the async operations preference {}.", getSessionUser().getUsername(), preference);
        final Properties properties = _preferences.asProperties();
        if (properties.containsKey(preference)) {
            return properties.getProperty(preference);
        }
        throw new NotFoundException("There is no preference with the name " + preference + " associated with the async operations preferences.");
    }

    @Operation(summary = "Sets the value for the indicated async operations preference.", description = "This method returns the previously set value for the indicated preference.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Preference value successfully stored."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Insufficient privileges to retrieve the requested setting."),
                   @ApiResponse(responseCode = "404", description = "Tool ID not found in the system."),
                   @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")})
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

    private void validateAsyncOperationsPreferences(final Map<String, Object> preferences) throws DataFormatException {
        final int frequency = preferences.containsKey(AsyncOperationsPreferences.CLEANUP_FREQUENCY) ? (int) preferences.get(AsyncOperationsPreferences.CLEANUP_FREQUENCY) : _preferences.getEventTrackingDataCleanupFrequency();
        final int ttl       = preferences.containsKey(AsyncOperationsPreferences.CLEANUP_TTL) ? (int) preferences.get(AsyncOperationsPreferences.CLEANUP_TTL) : _preferences.getEventTrackingDataCleanupFrequency();
        if (frequency <= 0) {
            throw new DataFormatException("The cleanup frequency must be a positive integer.");
        }
        if (ttl <= 0) {
            throw new DataFormatException("The cleanup time-to-live must be a positive integer.");
        }
        if (Duration.of(ttl, ChronoUnit.DAYS).compareTo(Duration.of(frequency, ChronoUnit.HOURS)) < 0) {
            throw new DataFormatException("The cleanup frequency must be be set to a smaller time interval than the time-to-live.");
        }
    }

    private final AsyncOperationsPreferences _preferences;
}
