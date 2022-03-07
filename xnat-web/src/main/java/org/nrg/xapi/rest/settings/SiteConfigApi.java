/*
 * web: org.nrg.xapi.rest.settings.SiteConfigApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.authorization.SiteConfigPreferenceXapiAuthorization;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.preferences.SiteConfigAccess;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.nrg.xdat.preferences.SiteConfigPreferences.SITE_URL;
import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api
@XapiRestController
@RequestMapping(value = "/siteConfig")
@Slf4j
public class SiteConfigApi extends AbstractXapiRestController {
    @Autowired
    public SiteConfigApi(final SiteConfigPreferences preferences, final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final XnatAppInfo appInfo, final SiteConfigAccess access, final NamedParameterJdbcTemplate template) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
        _appInfo = appInfo;
        _access = access;
        _template = template;
    }

    @ApiOperation(value = "Returns the full map of site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, Object> getSiteConfigProperties(final HttpServletRequest request) {
        final UserI  user     = getSessionUser();
        final String username = user.getUsername();
        if (!_appInfo.isInitialized()) {
            if (!Roles.isSiteAdmin(user)) {
                log.error("User {} is trying to access the site configuration properties but the system hasn't been initialized yet!", user.getUsername());
                return Collections.emptyMap();
            }
            log.info("The site is being initialized by user {}. Setting default values from context.", username);
            if (!_preferences.containsKey(SITE_URL) || StringUtils.isBlank(_preferences.getSiteUrl())) {
                _preferences.setSiteUrl(XnatHttpUtils.getServerRoot(request));
            }
        } else {
            log.debug("User {} requested the site configuration.", username);
        }
        return _preferences.entrySet().stream()
                .filter(entry -> _access.canRead(user, entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> ObjectUtils.defaultIfNull(entry.getValue(), "")));
    }

    @ApiOperation(value = "Sets a map of site configuration properties.", notes = "Sets the site configuration properties specified in the map.")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public void setSiteConfigProperties(@ApiParam(value = "The map of site configuration properties to be set.", required = true) @RequestBody final Map<String, Object> properties) {
        // Is this call initializing the system?
        final boolean isInitialized  = _appInfo.isInitialized();
        final boolean isInitializing = !isInitialized && properties.containsKey("initialized") && getInitializedValue(properties.get("initialized"));

        // First try to handle any submitted preferences that should be handled as a group.
        final List<? extends Set<String>> includedPrefsGroups = findPrefsGroups(properties.keySet());
        if (!includedPrefsGroups.isEmpty()) {
            final Set<String> referenced = new HashSet<>();
            for (final Set<String> groupPreferences : includedPrefsGroups) {
                referenced.addAll(groupPreferences);
                final Map<String, String> group = new HashMap<>();
                for (final String groupPreference : groupPreferences) {
                    group.put(groupPreference, properties.get(groupPreference).toString());
                }
                try {
                    _preferences.setBatch(group);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    log.error("Got an invalid preference name error when setting the preferences: {}, which is weird because the site configuration is not strict", groupPreferences, invalidPreferenceName);
                }
            }
            // Remove all referenced properties. The assumption is that settings handled in prefs groups need to be
            // handled in those groups and shouldn't be handled individually.
            for (final String property : referenced) {
                properties.remove(property);
            }
        }

        if (!properties.isEmpty()) {
            for (final String name : properties.keySet()) {
                try {
                    // If we're initializing, we're going to make sure everything else is set BEFORE we set initialized to true, so skip it here.
                    if (isInitializing && name.equals("initialized")) {
                        continue;
                    }
                    if (!isInitialized && properties.containsKey("adminEmail")) {
                        _template.update(EMAIL_UPDATE, properties);
                    }
                    final Object value = properties.get(name);
                    if (value instanceof List) {
                        //noinspection unchecked,rawtypes
                        _preferences.setListValue(name, (List) value);
                    } else if (value instanceof Map) {
                        //noinspection unchecked,rawtypes
                        _preferences.setMapValue(name, (Map) value);
                    } else if (value.getClass().isArray()) {
                        _preferences.setArrayValue(name, (Object[]) value);
                    } else {
                        _preferences.set(value.toString(), name);
                    }
                    log.info("Set property {} to value: {}", name, value);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    log.error("Got an invalid preference name error for the preference: " + name + ", which is weird because the site configuration is not strict");
                }
            }

            // If we're initializing...
            if (isInitializing) {
                // Now make the initialized setting true. This will kick off the initialized event handler.
                _preferences.setInitialized(true);
            }
        }
    }

    @ApiOperation(value = "Returns a map of the selected site configuration properties.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "values/{preferences}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(SiteConfigPreferenceXapiAuthorization.class)
    public Map<String, Object> getSpecifiedSiteConfigProperties(@PathVariable final List<String> preferences) {
        log.debug("User {} requested the site configuration preferences {}", getSessionUser().getUsername(), StringUtils.join(preferences, ", "));
        return _preferences.keySet().stream().filter(preferences::contains).collect(Collectors.toMap(Function.identity(), _preferences::get));
    }

    @ApiOperation(value = "Returns the value of the selected site configuration property.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = Object.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration property successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{property}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(SiteConfigPreferenceXapiAuthorization.class)
    public Object getSpecifiedSiteConfigProperty(@ApiParam(value = "The site configuration property to retrieve.", required = true) @PathVariable final String property) throws NotFoundException {
        if (!_preferences.containsKey(property)) {
            throw new NotFoundException("No site configuration property named " + property);
        }
        final Object value = _preferences.get(property);
        log.debug("User {} requested the value for the site configuration property {}, got value: {}", getSessionUser().getUsername(), property, value);
        return value;
    }

    @ApiOperation(value = "Sets a single site configuration property.", notes = "Sets the site configuration property specified in the URL to the value set in the body.")
    @ApiResponses({@ApiResponse(code = 200, message = "Site configuration properties successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{property}", consumes = {TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public void setSiteConfigProperty(@ApiParam(value = "The property to be set.", required = true) @PathVariable("property") final String property,
                                      @ApiParam("The value to be set for the property.") @RequestBody final String value) throws InitializationException {
        log.info("User '{}' set the value of the site configuration property {} to: {}", getSessionUser().getUsername(), property, value);

        if (StringUtils.equals("initialized", property) && StringUtils.equals("true", value)) {
            _preferences.setInitialized(true);
        } else {
            try {
                _preferences.set(value, property);
            } catch (InvalidPreferenceName invalidPreferenceName) {
                throw new InitializationException("Got an invalid preference name error for the preference: " + property + ", which is weird because the site configuration is not strict");
            }
        }
    }

    @ApiOperation(value = "Returns a map of application build properties.", notes = "This includes the implementation version, Git commit hash, and build number and number.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Application build properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "buildInfo", produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, String> getBuildInfo() {
        log.debug("User {} requested the application build information.", getSessionUser().getUsername());
        return _appInfo.getSystemProperties();
    }

    @ApiOperation(value = "Returns a map of extended build attributes.", notes = "The values are dependent on what attributes are set for the build. It is not unexpected that there are no extended build attributes.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Extended build attributes successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "buildInfo/attributes", produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, Map<String, String>> getBuildAttributeInfo() {
        log.debug("User {} requested the extended application build attributes.", getSessionUser().getUsername());
        return _appInfo.getSystemAttributes();
    }

    @ApiOperation(value = "Returns a map of extended build attributes.", notes = "The values are dependent on what attributes are set for the build. It is not unexpected that there are no extended build attributes.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Extended build attributes successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "buildInfo/{property}", produces = APPLICATION_JSON_VALUE, method = GET)
    public String getBuildProperty(@ApiParam("Indicates the specific property to be returned") @PathVariable final String property) {
        log.debug("User {} requested the build property {}.", getSessionUser().getUsername(), property);
        return _appInfo.getSystemProperty(property);
    }

    @ApiOperation(value = "Returns the system uptime.", notes = "This returns the uptime as a map of time units: days, hours, minutes, and seconds.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "System uptime successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "uptime", produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, String> getSystemUptime() {
        log.debug("User {} requested the system uptime map.", getSessionUser().getUsername());
        return _appInfo.getUptime();
    }

    @ApiOperation(value = "Returns the system uptime.", notes = "This returns the uptime as a formatted string.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "System uptime successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "uptime/display", produces = APPLICATION_JSON_VALUE, method = GET)
    public String getFormattedSystemUptime() {
        log.debug("User {} requested the formatted system uptime.", getSessionUser().getUsername());
        return _appInfo.getFormattedUptime();
    }

    private List<? extends Set<String>> findPrefsGroups(final Set<String> keySet) {
        final List<Set<String>> includedPrefsGroups = new ArrayList<>();
        for (final Set<String> group : PREFS_GROUPS) {
            if (keySet.containsAll(group)) {
                includedPrefsGroups.add(group);
            }
        }
        return includedPrefsGroups;
    }

    private static boolean getInitializedValue(final Object initialized) {
        if (initialized == null) {
            return false;
        }
        if (initialized instanceof Boolean) {
            return (Boolean) initialized;
        }
        if (initialized instanceof String) {
            return BooleanUtils.toBoolean((String) initialized);
        }
        return BooleanUtils.toBoolean(initialized.toString());
    }

    private static final String                      EMAIL_UPDATE = "UPDATE xdat_user SET email = :adminEmail WHERE login IN ('admin', 'guest')";
    private static final List<? extends Set<String>> PREFS_GROUPS = Collections.singletonList(ImmutableSet.of("enableSitewideSeriesImportFilter", "sitewideSeriesImportFilterMode", "sitewideSeriesImportFilter"));

    private final SiteConfigPreferences      _preferences;
    private final XnatAppInfo                _appInfo;
    private final SiteConfigAccess           _access;
    private final NamedParameterJdbcTemplate _template;
}
