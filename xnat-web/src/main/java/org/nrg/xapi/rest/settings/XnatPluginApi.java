/*
 * web: org.nrg.xapi.rest.settings.XnatPluginApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Api(description = "XNAT Plugin API")
@XapiRestController
@RequestMapping(value = "/plugins")
public class XnatPluginApi extends AbstractXapiRestController {
    @Autowired
    public XnatPluginApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final XnatPluginBeanManager manager) throws IOException {
        super(userManagementService, roleHolder);
        for (final String pluginId : manager.getPluginIds()) {
            final XnatPluginBean plugin = manager.getPlugin(pluginId);
            _plugins.put(pluginId, plugin);
        }
    }

    @ApiOperation(value = "Returns a list of all of the installed and active XNAT plugins with their properties.", notes = "The maps returned from this call include all of the properties specified in the plugin's property file.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<Map<String, XnatPluginBean>> getAllDataTypeSchemas() throws IOException {
        return new ResponseEntity<>(_plugins, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the indicated XNAT plugin with its properties.", notes = "The map returned from this call include all of the properties specified in the plugin's property file.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = "{plugin}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public ResponseEntity<XnatPluginBean> getRequestedDataTypeSchema(@PathVariable("plugin") final String plugin) throws IOException {
        if (!_plugins.containsKey(plugin)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(_plugins.get(plugin), HttpStatus.OK);
    }

    private final Map<String, XnatPluginBean> _plugins = Maps.newHashMap();
}
