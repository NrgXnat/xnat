package org.nrg.xapi.rest.settings;

import com.google.common.base.Joiner;
import io.swagger.annotations.*;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.rest.AbstractXnatRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(description = "XNAT Site Configuration Management API")
@XapiRestController
@RequestMapping(value = "/siteConfig")
public class SiteConfigApi extends AbstractXnatRestApi {
    @ApiOperation(value = "Returns the full map of site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = SiteConfigPreferences.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {"application/json"}, method = {RequestMethod.GET})
    public ResponseEntity<SiteConfigPreferences> getAllSiteConfigProperties() {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the site configuration.");
        }
        return new ResponseEntity<>(_preferences, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a map of the selected site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(consumes = {"application/json"}, produces = {"application/json"}, method = {RequestMethod.PUT})
    public ResponseEntity<Map<String, String>> getSpecifiedSiteConfigProperties(@RequestBody final List<String> preferences) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the site configuration properties " + Joiner.on(", ").join(preferences));
        }

        final Map<String, String> properties = new HashMap<>();
        for (final String preference : preferences) {
            properties.put(preference, _preferences.getValue(preference));
        }
        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns a map of the selected site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "{property}", consumes = {"application/json"}, produces = {"application/json"}, method = {RequestMethod.GET})
    public ResponseEntity<String> getSpecifiedSiteConfigProperties(@ApiParam(value = "The site configuration property to retrieve.", required = true) @PathVariable final String property) {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        final String value = _preferences.getValue(property);
        if (_log.isDebugEnabled()) {
            _log.debug("User " + getSessionUser().getUsername() + " requested the value for the site configuration property " + property + ", got value: " + value);
        }
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @ApiOperation(value = "Sets a map of site configuration properties.", notes = "Sets the site configuration properties specified in the map.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/batch"}, consumes = {"application/json"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setBatchSiteConfigProperties(@ApiParam(value = "The map of site configuration properties to be set.", required = true) @RequestBody final Map<String, String> properties) {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_log.isDebugEnabled()) {
            final StringBuilder message = new StringBuilder("User ").append(getSessionUser().getUsername()).append(" is setting the values for the following properties:\n");
            for (final String name : properties.keySet()) {
                message.append(" * ").append(name).append(": ").append(properties.get(name)).append("\n");
            }
            _log.debug(message.toString());
        }

        for (final String name : properties.keySet()) {
            try {
                _preferences.set(name, properties.get(name));
            } catch (InvalidPreferenceName invalidPreferenceName) {
                _log.error("Got an invalid preference name error for the preference: " + name + ", which is weird because the site configuration is not strict");
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Sets a single site configuration property.", notes = "Sets the site configuration property specified in the URL to the value set in the body.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully set."), @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."), @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{property}"}, consumes = {"application/json"}, produces = {"application/json"}, method = {RequestMethod.POST})
    public ResponseEntity<Void> setSiteConfigProperty(@ApiParam(value = "The map of site configuration properties to be set.", required = true) @PathVariable("property") final String property, @RequestBody final String value) {
        HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("User {} is setting the value of the site configuration property {} to: {}", getSessionUser().getUsername(), property, value);
        }

        try {
            _preferences.set(property, value);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.error("Got an invalid preference name error for the preference: " + property + ", which is weird because the site configuration is not strict");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private static final Logger _log = LoggerFactory.getLogger(SiteConfigApi.class);

    @Autowired
    @Lazy
    private SiteConfigPreferences _preferences;
}
