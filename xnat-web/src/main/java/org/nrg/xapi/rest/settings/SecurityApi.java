/*
 * web: org.nrg.xapi.rest.settings.SiteConfigApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.SecurityPreferences;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.security.provider.ProviderAttributes;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api(description = "Security Configuration Management API")
@XapiRestController
@RequestMapping(value = "/security")
@Slf4j
public class SecurityApi extends AbstractXapiRestController {
    @Autowired
    public SecurityApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final SecurityPreferences preferences, final List<AuthenticationProvider> providers) {
        super(userManagementService, roleHolder);

        _preferences = preferences;

        for (final ProviderAttributes attributes : Lists.transform(providers, new Function<AuthenticationProvider, ProviderAttributes>() {
            @Override
            public ProviderAttributes apply(final AuthenticationProvider provider) {
                return provider != null && provider instanceof XnatAuthenticationProvider ? new ProviderAttributes((XnatAuthenticationProvider) provider) : null;
            }
        })) {
            _providerAttributes.put(attributes.getProviderId(), attributes);
        }
    }

    @ApiOperation(value = "Returns the full map of security preferences.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Security preferences successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set security preferences."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ResponseEntity<Map<String, Object>> getSecurityPreferences(final HttpServletRequest request) {
        log.debug("User {} requested the all security preferences", getSessionUser().getUsername());
        return new ResponseEntity<>((Map<String, Object>) _preferences, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a map of the selected security preferences.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Security preferences successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set security preferences."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "values/{preferences}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ResponseEntity<Map<String, Object>> getSpecifiedSecurityPreferences(@PathVariable final List<String> preferences) {
        log.debug("User {} requested the security preferences {}", getSessionUser().getUsername(), Joiner.on(", ").join(preferences));
        final Map<String, Object> values = new HashMap<>();
        for (final String preference : preferences) {
            if (_preferences.containsKey(preference)) {
                values.put(preference, _preferences.get(preference));
            }
        }
        return new ResponseEntity<>(values, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the value of the selected security preference.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = Object.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Security preference successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access security preferences."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{property}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ResponseEntity<Object> getSpecifiedSecurityPreference(@ApiParam(value = "The security preference to retrieve.", required = true) @PathVariable final String property) {
        if (!_preferences.containsKey(property)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final Object value = _preferences.get(property);
        log.debug("User {} requested the value for the security preference {}, got value: {}", getSessionUser().getUsername(), property, value);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @ApiOperation(value = "Sets a single security preference.", notes = "Sets the security preference specified in the URL to the value set in the body.")
    @ApiResponses({@ApiResponse(code = 200, message = "Security preferences successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set security preferences."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "{property}", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Admin)
    public ResponseEntity<Void> setSecurityPreference(@ApiParam(value = "The property to be set.", required = true) @PathVariable("property") final String property,
                                                      @ApiParam("The value to be set for the property.") @RequestBody final String value) {
        log.info("User {} is setting the value of the security preference {} to: {}", getSessionUser().getUsername(), property, value);
        try {
            _preferences.set(value, property);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            log.error("Got an invalid preference name error for the preference: {}, which is weird because the site configuration is not strict", property);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the list of all providers defined on the system.", notes = "This includes providers that aren't enabled.", responseContainer = "Set", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "List of providers successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access security preferences."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "providers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ResponseEntity<Set<String>> getAuthenticationProviders() {
        return new ResponseEntity<>(_providerAttributes.keySet(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the attributes for the provider identified by the submitted ID.", response = ProviderAttributes.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Specified provider successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access security preferences."),
                   @ApiResponse(code = 404, message = "The requested provider ID doesn't exist in this system."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "providers/{providerId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ResponseEntity<ProviderAttributes> getAuthenticationProvider(@ApiParam(value = "The ID of the provider to retrieve.", required = true) @PathVariable("providerId") final String providerId) {
        if (!_providerAttributes.containsKey(providerId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(_providerAttributes.get(providerId), HttpStatus.OK);
    }

    private final SecurityPreferences _preferences;
    private final Map<String, ProviderAttributes> _providerAttributes = new HashMap<>();
}
