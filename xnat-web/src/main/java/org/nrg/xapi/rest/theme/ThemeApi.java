/*
 * web: org.nrg.xapi.rest.theme.ThemeApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.theme;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.SanitizeUtils;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.entities.ThemeConfig;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Tag(name = "theme", description = "XNAT Theme Management API")
@XapiRestController
@RequestMapping(value = "/themes")
public class ThemeApi extends AbstractXapiRestController {
    @Autowired
    public ThemeApi(final ThemeService themeService, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _themeService = themeService;
    }

    @Operation(summary = "Get list of available themes.", description = "Use this to get a list of all available themes on the XNAT system.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reports the currently selected global theme (if there is one), whether or not it's enabled, and a list of available themes on the system in a JSON string."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<List<ThemeService.TypeOption>> getAllThemes() {
        return new ResponseEntity<>(_themeService.loadExistingThemes(), HttpStatus.OK);
    }

    @Operation(summary = "Accepts a multipart form with a zip file upload and extracts its contents in the theme system folder. If successful, the first (root) directory name (or theme name) unzipped is returned in the response. This will overwrite any other directories already existing with the same name without warning.", description = "The structure of the zipped package must have only directories at it's root.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Theme package successfully uploaded and extracted."), @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."), @ApiResponse(responseCode = "403", description = "Not authorized to upload a theme package."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Admin)
    public ResponseEntity<List<ThemeService.TypeOption>> uploadTheme(@Parameter(description = "Multipart file object being uploaded", required = true) @RequestParam(value = "themePackage", required = false) MultipartFile themePackage) {
        List<ThemeService.TypeOption> themeOptions = new ArrayList<>();
        try {
            if (!themePackage.getContentType().contains("zip")) {
                String error = "No valid files were uploaded. Theme package must be of type: application/x-zip-compressed";
                _log.error(error);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<String> dirs = _themeService.extractTheme(themePackage.getInputStream());
            for (String dir : dirs) {
                themeOptions.add(new ThemeService.TypeOption(dir, dir));
            }
        } catch (IOException e) {
            e.printStackTrace();
            String error = "An error occurred extracting the theme package";
            _log.error(error, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            String error = "An unknown error occurred accepting the theme package";
            _log.error(error, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(themeOptions, HttpStatus.OK);
    }

    @Operation(summary = "Get the currently selected global theme or a role based theme if specified.", description = "Use this to get the theme selected by the system administrator on the Theme Management page.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reports the currently selected global theme (if there is one) and whether or not it's enabled."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "current", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> getCurrentTheme() {
        final UserI              user  = getSessionUser();
        final Collection<String> roles = Roles.getRoles(user);
        for (final String role : roles) {
            final ThemeConfig theme = _themeService.getTheme(role);
            if (theme != null) {
                return new ResponseEntity<>(theme.getName(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("No current or default theme was found.", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get the currently selected global theme or a role based theme if specified.", description = "Use this to get the theme selected by the system administrator on the Theme Management page.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reports the currently selected global theme (if there is one) and whether or not it's enabled."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "role/{role}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<ThemeConfig> getThemeByRole(@Parameter(description = "\"global\" or role name of currently set theme", required = true) @PathVariable("role") final String role) {
        if ("global".equalsIgnoreCase(role)) {
            return new ResponseEntity<>(_themeService.getTheme(), HttpStatus.OK);
        }
        return new ResponseEntity<>(_themeService.getTheme(role), HttpStatus.OK);
    }

    @Operation(summary = "Sets the current global theme to the one specified.", description = "Returns the updated serialized theme object.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the current global theme."), @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."), @ApiResponse(responseCode = "403", description = "Not authorized to create or update this user."), @ApiResponse(responseCode = "404", description = "Theme not found."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "theme/{theme}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public ResponseEntity<ThemeConfig> setGlobalTheme(@Parameter(description = "The name of the theme to select.", required = true) @PathVariable("theme") final String theme, @RequestParam(value = "enabled", required = false, defaultValue = "true") final boolean enabled) {
        try {
            final ThemeConfig themeConfig = _themeService.setTheme(StringUtils.equals(theme, "null") ? null : theme, enabled);
            return new ResponseEntity<>(themeConfig, HttpStatus.OK); // TODO: fix the return on this. It's showing up as [object Object] on the page!
        } catch (ThemeService.ThemeNotFoundException e) {
            _log.error(e.getInvalidTheme() + " not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            _log.error("An error occurred setting the theme " + theme, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Deletes the theme with the specified name.", description = "Returns success on deletion. ")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Theme was successfully deleted."), @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."), @ApiResponse(responseCode = "403", description = "Not authorized to delete a theme."), @ApiResponse(responseCode = "404", description = "Theme not found."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "theme/{theme}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = Admin)
    public ResponseEntity<ThemeConfig> deleteTheme(@Parameter(description = "Name of the theme to delete", required = true) @PathVariable("theme") final String theme) {
        if (StringUtils.equals(theme, "null")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (SanitizeUtils.containsPathTraversal(theme)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!_themeService.themeExists(theme)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return deleteThemeAndReturnResponseEntity(theme);
    }

    private ResponseEntity<ThemeConfig> deleteThemeAndReturnResponseEntity(String theme) {
        try {
            final File file = new File(_themeService.getThemesPath() + File.separator + theme);
            FileUtils.deleteDirectory(file);
            if (!file.exists()) {
                final ThemeConfig themeConfig = _themeService.getTheme();
                final String      themeName   = (themeConfig != null) ? themeConfig.getName() : null;
                if (theme.equals(themeName)) {
                    _themeService.setTheme((ThemeConfig) null);
                }
                if (themeConfig != null) {
                    return new ResponseEntity<>(themeConfig, HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            _log.error("An error occurred when deleting a theme", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(ThemeApi.class);

    private final ThemeService _themeService;
}
