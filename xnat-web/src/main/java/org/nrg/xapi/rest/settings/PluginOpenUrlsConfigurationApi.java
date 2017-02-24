/*
 * web: org.nrg.xapi.rest.settings.PluginOpenUrlsConfigurationApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.preferences.PluginOpenUrlsPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * The Class PluginOpenUrlsConfigurationApi.
 */
@Api(description = "Plugin Open URLs Authorization API")
@XapiRestController
public class PluginOpenUrlsConfigurationApi extends AbstractXapiRestController {

	/**
	 * Instantiates a new plugin open url configuration api.
	 *
	 * @param userManagementService the user management service
	 * @param roleHolder the role holder
	 * @param openUrlsPreference the open urls preference
	 */
	@Autowired
    public PluginOpenUrlsConfigurationApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final PluginOpenUrlsPreference openUrlsPreference) {
        super(userManagementService, roleHolder);
        _openUrlsPreference = openUrlsPreference;
    }

    /**
     * Gets the plugin open url configuration.
     *
     * @return the plugin open url configuration
     */
    @ApiOperation(value = "Gets the plugin open URL configuration.", notes = "Returns plugin open URL configration for this installation.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "An array of propeties"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/pluginOpenUrls/settings"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Properties> getPluginOpenUrlsConfiguration() {
    	final HttpStatus status = isPermitted();
    	if (status != null) {
    		return new ResponseEntity<>(status);
    	}
    	final List<String> pluginOpenUrls = _openUrlsPreference.getPluginOpenUrls();
    	final List<String> pluginOpenUrlsAllowed = _openUrlsPreference.getAllowedPluginOpenUrls();
    	final Properties prop = new Properties();
    	for (final String openUrl : pluginOpenUrls) {
    		prop.put(openUrl, String.valueOf(pluginOpenUrlsAllowed.contains(openUrl)));
    	}
    	return new ResponseEntity<>(prop, HttpStatus.OK);
    }

    /**
     * Sets the plugin open url configuration.
     *
     * @param config the config
     * @return the response entity
     */
    @ApiOperation(value = "Sets the plugin open URL configuration.", notes = "Sets plugin open URL configration for this installation.", response = Void.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/pluginOpenUrls/settings"}, consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> setPluginOpenUrlsConfiguration(@RequestBody final Map<String, Boolean> config) {
    	if (!Roles.isSiteAdmin(getSessionUser())) {
    		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    	}
    	final List<String> authList = Lists.newArrayList();
    	for (final Entry<String, Boolean> entry : config.entrySet()) {
    		if (entry.getValue()) {
    			authList.add(entry.getKey());
    		}
    	}
    	_openUrlsPreference.setAllowedPluginOpenUrls(authList);
    	// Commenting this call out.  Changes to the hanges to the openUrlList still require Tomcat retart.
       	return new ResponseEntity<>(HttpStatus.OK);
    }

    //private static final Logger _logger = LoggerFactory.getLogger(PluginOpenUrlsConfigurationApi.class);
    
    /** The _open urls preference. */
    private final PluginOpenUrlsPreference _openUrlsPreference;
    
}
