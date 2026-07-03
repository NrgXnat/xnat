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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.constants.Scope;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.authorization.SiteConfigPreferenceXapiAuthorization;
import org.nrg.xapi.exceptions.DataFormatException;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.nrg.xdat.preferences.SiteConfigPreferences.SITE_URL;
import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@XapiRestController
@RequestMapping(value = "/siteConfig")
@Slf4j
public class SiteConfigApi extends AbstractXapiRestController {

    @Autowired
    public SiteConfigApi(
            final SiteConfigPreferences preferences,
            final UserManagementServiceI userManagementService,
            final RoleHolder roleHolder,
            final XnatAppInfo appInfo,
            final SiteConfigAccess access,
            final NamedParameterJdbcTemplate template,
            final ConfigService configService) {
        super(userManagementService, roleHolder);
        _preferences = preferences;
        _appInfo = appInfo;
        _access = access;
        _template = template;
        _configService = configService;
    }

    @Operation(summary = "Returns the full map of site configuration properties.", description = "Complex objects may be returned as encapsulated JSON strings.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Site configuration properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
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

    @Operation(summary = "Sets a map of site configuration properties.", description = "Sets the site configuration properties specified in the map.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Site configuration properties successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public void setSiteConfigProperties(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The map of site configuration properties to be set.", required = true) @RequestBody final Map<String, Object> properties) throws DataFormatException {
        // Is this call initializing the system?
        final boolean isInitialized  = _appInfo.isInitialized();
        final boolean isInitializing = !isInitialized && properties.containsKey("initialized") && getInitializedValue(properties.get("initialized"));

        validateReCaptcha(properties);
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
                    if (value instanceof List<?> list) {
                        //noinspection unchecked,rawtypes
                        _preferences.setListValue(name, list);
                    } else if (value instanceof Map<?,?> map) {
                        //noinspection unchecked,rawtypes
                        _preferences.setMapValue(name, (Map) map);
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

    private static void validateReCaptcha(Map<String, Object> properties) throws DataFormatException {
        if (Boolean.TRUE.equals(properties.get("uiNewUserRequireCaptcha"))) {
            Object privateKey = properties.get("uiNewUserCaptchaPrivate");
            Object publicKey = properties.get("uiNewUserCaptchaPublic");
            if (StringUtils.isBlank(String.valueOf(privateKey)) || StringUtils.isBlank(String.valueOf(publicKey))) {
                throw new DataFormatException("ReCAPTCHA keys can not be empty.");
            }
        }
    }

    @Operation(summary = "Returns a map of the selected site configuration properties.", description = "Complex objects may be returned as encapsulated JSON strings.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Site configuration properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "values/{preferences}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(SiteConfigPreferenceXapiAuthorization.class)
    public Map<String, Object> getSpecifiedSiteConfigProperties(@PathVariable final List<String> preferences) {
        log.debug("User {} requested the site configuration preferences {}", getSessionUser().getUsername(), StringUtils.join(preferences, ", "));
        return _preferences.keySet().stream().filter(preferences::contains).collect(Collectors.toMap(Function.identity(), _preferences::get));
    }

    @Operation(summary = "Returns the value of the selected site configuration property.", description = "Complex objects may be returned as encapsulated JSON strings.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Site configuration property successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to access site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{property}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authorizer)
    @AuthDelegate(SiteConfigPreferenceXapiAuthorization.class)
    public Object getSpecifiedSiteConfigProperty(@Parameter(description = "The site configuration property to retrieve.", required = true) @PathVariable final String property) throws NotFoundException {
        if (!_preferences.containsKey(property)) {
            throw new NotFoundException("No site configuration property named " + property);
        }
        final Object value = _preferences.get(property);
        log.debug("User {} requested the value for the site configuration property {}, got value: {}", getSessionUser().getUsername(), property, value);
        return value;
    }

    @Operation(summary = "Sets a single site configuration property.", description = "Sets the site configuration property specified in the URL to the value set in the body.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Site configuration properties successfully set."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "403", description = "Not authorized to set site configuration properties."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{property}", consumes = {TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE}, produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public void setSiteConfigProperty(@Parameter(description = "The property to be set.", required = true) @PathVariable("property") final String property,
                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The value to be set for the property.") @RequestBody final String value) throws InitializationException {
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

    @Operation(summary = "Disable orphaned series import filters for deleted projects.", description = "Disable orphaned series import filters for deleted projects.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Orphaned series import filters successfully disabled"),
            @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(responseCode = "403", description = "Not authorized to modify site configuration properties."),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value="orphaned-projects/disable", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public void disableOrphanedSif(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The deleted project IDs (comma-separated).", required = true) @RequestBody final String orphanedProjectIds) throws ConfigServiceException {
        final UserI user = getSessionUser();
        final String[] orphanedIds = Arrays.stream(orphanedProjectIds.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toArray(String[]::new);

        for (final String projectId : orphanedIds) {
            _configService.disable(user.getUsername(), "Deleted project", "seriesImportFilter", "config", Scope.Project, projectId);
        }

        final String currentEnabledProjects = _preferences.getEnableProjectsSeriesImportFilter();
        final Set<String> enabledSifs = StringUtils.isBlank(currentEnabledProjects)
                ? new HashSet<>()
                : new HashSet<>(Arrays.asList(currentEnabledProjects.split(",")));
        enabledSifs.removeAll(Arrays.asList(orphanedIds));
        _preferences.setEnableProjectsSeriesImportFilter(String.join(",", enabledSifs));
    }

    @Operation(summary = "Returns a map of application build properties.", description = "This includes the implementation version, Git commit hash, and build number and number.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Application build properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "buildInfo", produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, String> getBuildInfo() {
        log.debug("User {} requested the application build information.", getSessionUser().getUsername());
        return _appInfo.getSystemProperties();
    }

    @Operation(summary = "Returns a map of extended build attributes.", description = "The values are dependent on what attributes are set for the build. It is not unexpected that there are no extended build attributes.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Extended build attributes successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "buildInfo/attributes", produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, Map<String, String>> getBuildAttributeInfo() {
        log.debug("User {} requested the extended application build attributes.", getSessionUser().getUsername());
        return _appInfo.getSystemAttributes();
    }

    @Operation(summary = "Returns a map of extended build attributes.", description = "The values are dependent on what attributes are set for the build. It is not unexpected that there are no extended build attributes.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Extended build attributes successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "buildInfo/{property}", produces = APPLICATION_JSON_VALUE, method = GET)
    public String getBuildProperty(@Parameter(description = "Indicates the specific property to be returned") @PathVariable final String property) {
        log.debug("User {} requested the build property {}.", getSessionUser().getUsername(), property);
        return _appInfo.getSystemProperty(property);
    }

    @Operation(summary = "Returns the system uptime.", description = "This returns the uptime as a map of time units: days, hours, minutes, and seconds.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "System uptime successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "uptime", produces = APPLICATION_JSON_VALUE, method = GET)
    public Map<String, String> getSystemUptime() {
        log.debug("User {} requested the system uptime map.", getSessionUser().getUsername());
        return _appInfo.getUptime();
    }

    @Operation(summary = "Returns the system uptime.", description = "This returns the uptime as a formatted string.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "System uptime successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
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
        if (initialized instanceof Boolean boolean1) {
            return boolean1;
        }
        if (initialized instanceof String string) {
            return BooleanUtils.toBoolean(string);
        }
        return BooleanUtils.toBoolean(initialized.toString());
    }

    private static final String                      EMAIL_UPDATE = "UPDATE xdat_user SET email = :adminEmail WHERE login IN ('admin', 'guest')";
    private static final List<? extends Set<String>> PREFS_GROUPS = Collections.singletonList(ImmutableSet.of("enableSitewideSeriesImportFilter", "sitewideSeriesImportFilterMode", "sitewideSeriesImportFilter"));

    private final SiteConfigPreferences      _preferences;
    private final XnatAppInfo                _appInfo;
    private final SiteConfigAccess           _access;
    private final NamedParameterJdbcTemplate _template;
    private final ConfigService _configService;
}
