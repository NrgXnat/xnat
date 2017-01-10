/*
 * web: org.nrg.xapi.rest.settings.SiteConfigApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.base.Joiner;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.ApiException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatHttpUtils;
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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Api(description = "Site Configuration Management API")
@XapiRestController
@RequestMapping(value = "/siteConfig")
public class SiteConfigApi extends AbstractXapiRestController {
    @Autowired
    public SiteConfigApi(final SiteConfigPreferences preferences, final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final XnatAppInfo appInfo) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
        _appInfo = appInfo;
    }

    @PostConstruct
    public void checkForFoundPreferences() {
        if (!_appInfo.isInitialized()) {
            Map<String, String> tempPrefs = _appInfo.getFoundPreferences();
            if (tempPrefs != null) {
                _found.putAll(tempPrefs);
            }
            if (_found.size() > 0) {
                _hasFoundPreferences = true;
            }
        }
    }

    @ApiOperation(value = "Returns the full map of site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSiteConfigProperties(final HttpServletRequest request) {
        if (!_appInfo.isOpenUrlRequest(request)) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }
        final String username = getSessionUser().getUsername();
        if (_log.isDebugEnabled()) {
            _log.debug("User " + username + " requested the site configuration.");
        }

        final Map<String, Object> preferences = getPreferences();

        if (!_appInfo.isInitialized()) {
            if (_log.isInfoEnabled()) {
                _log.info("The site is being initialized by user {}. Setting default values from context.", username);
            }
            if (!preferences.containsKey("siteUrl") || StringUtils.isBlank(preferences.get("siteUrl").toString())) {
                preferences.put("siteUrl", XnatHttpUtils.getServerRoot(request));
            }
        }

        return new ResponseEntity<>(preferences, HttpStatus.OK);
    }

    @ApiOperation(value = "Sets a map of site configuration properties.", notes = "Sets the site configuration properties specified in the map.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST)
    public ResponseEntity<Void> setSiteConfigProperties(@ApiParam(value = "The map of site configuration properties to be set.", required = true) @RequestBody final Map<String, String> properties) throws ApiException, InitializationException {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        // Is this call initializing the system?
        final boolean isInitializing = properties.containsKey("initialized") && StringUtils.equals("true", properties.get("initialized"));
        for (final String name : properties.keySet()) {
            try {
                // If we're initializing, we're going to make sure everything else is set BEFORE we set initialized to true, so skip it here.
                if (isInitializing && name.equals("initialized")) {
                    continue;
                }
                _preferences.set(properties.get(name), name);
                if (_log.isInfoEnabled()) {
                    _log.info("Set property {} to value: {}", name, properties.get(name));
                }
            } catch (InvalidPreferenceName invalidPreferenceName) {
                _log.error("Got an invalid preference name error for the preference: " + name + ", which is weird because the site configuration is not strict");
            }
        }

        // If we're initializing...
        if (isInitializing) {
            // Now make the initialized setting true. This will kick off the initialized event handler.
            _preferences.setInitialized(true);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a map of the selected site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "values/{preferences}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSpecifiedSiteConfigProperties(@PathVariable final List<String> preferences) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the site configuration preferences " + Joiner.on(", ").join(preferences));
        }

        final Map<String, Object> values = new HashMap<>();
        for (final String preference : preferences) {
            if (getPreferences().containsKey(preference)) {
                values.put(preference, getPreferences().get(preference));
            }
        }
        return new ResponseEntity<>(values, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the value of the selected site configuration property.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = Object.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration property successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "{property}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Object> getSpecifiedSiteConfigProperty(final HttpServletRequest request, @ApiParam(value = "The site configuration property to retrieve.", required = true) @PathVariable final String property) {
        if (!_appInfo.isOpenUrlRequest(request)) {
            final HttpStatus status = isPermitted();
            if (status != null) {
                return new ResponseEntity<>(status);
            }
        }
        if (!getPreferences().containsKey(property)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final Object value = getPreferences().get(property);
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the value for the site configuration property " + property + ", got value: " + value);
        }
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @ApiOperation(value = "Sets a single site configuration property.", notes = "Sets the site configuration property specified in the URL to the value set in the body.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "{property}", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<Void> setSiteConfigProperty(@ApiParam(value = "The property to be set.", required = true) @PathVariable("property") final String property,
                                                      @ApiParam("The value to be set for the property.") @RequestBody final String value) throws InitializationException, ApiException {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_log.isInfoEnabled()) {
            _log.info("User {} is setting the value of the site configuration property {} to: {}", getSessionUser().getUsername(), property, value);
        }

        if (StringUtils.equals("initialized", property) && StringUtils.equals("true", value)) {
            _preferences.setInitialized(true);
        } else {
            try {
                _preferences.set(value, property);
            } catch (InvalidPreferenceName invalidPreferenceName) {
                _log.error("Got an invalid preference name error for the preference: " + property + ", which is weird because the site configuration is not strict");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a map of application build properties.", notes = "This includes the implementation version, Git commit hash, and build number and number.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Application build properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "buildInfo", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Properties> getBuildInfo() {
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the application build information.");
        }

        return new ResponseEntity<>(_appInfo.getSystemProperties(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a map of extended build attributes.", notes = "The values are dependent on what attributes are set for the build. It is not unexpected that there are no extended build attributes.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Extended build attributes successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "buildInfo/attributes", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Map<String, String>>> getBuildAttributeInfo() {
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the extended application build attributes.");
        }

        return new ResponseEntity<>(_appInfo.getSystemAttributes(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the system uptime.", notes = "This returns the uptime as a map of time units: days, hours, minutes, and seconds.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "System uptime successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "uptime", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getSystemUptime() {
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the system uptime map.");
        }

        return new ResponseEntity<>(_appInfo.getUptime(), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the system uptime.", notes = "This returns the uptime as a formatted string.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "System uptime successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "uptime/display", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> getFormattedSystemUptime() {
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the formatted system uptime.");
        }

        return new ResponseEntity<>(_appInfo.getFormattedUptime(), HttpStatus.OK);
    }

    private Map<String, Object> getPreferences() {
        if (!_hasFoundPreferences) {
            return _preferences.getPreferenceMap();
        }
        final Map<String, Object> preferences = new HashMap<>(_preferences.getPreferenceMap());
        preferences.putAll(_found);
        return preferences;
    }

    private static final Logger _log = LoggerFactory.getLogger(SiteConfigApi.class);

    private final SiteConfigPreferences _preferences;
    private final XnatAppInfo           _appInfo;
    private final Map<String, String> _found = new HashMap<>();
    private boolean _hasFoundPreferences;
}
