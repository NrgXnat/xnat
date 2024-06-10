/*
 * web: org.nrg.xapi.rest.settings.XnatPluginApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static lombok.AccessLevel.PRIVATE;

@Api("XNAT Plugin API")
@XapiRestController
@RequestMapping(value = "/plugins")
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class XnatPluginApi extends AbstractXapiRestController {
    @Autowired
    public XnatPluginApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final XnatPluginBeanManager manager) {
        super(userManagementService, roleHolder);
        _plugins = new HashMap<>(manager.getPluginBeans());
        log.debug("Plugin API controller loaded {} plugins: {}", getPlugins().size(), StringUtils.join(getPlugins().keySet(), ", "));
    }

    @ApiOperation(value = "Returns a list of all of the installed and active XNAT plugins with their properties.", notes = "The maps returned from this call include all of the properties specified in the plugin's property file.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public Map<String, XnatPluginBean> getAllPlugins() {
        return getPlugins();
    }

    @ApiOperation(value = "Returns the indicated XNAT plugin with its properties.", notes = "The map returned from this call include all of the properties specified in the plugin's property file.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The requested resource wasn't found."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{plugin}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public XnatPluginBean getRequestedPlugin(@PathVariable("plugin") final String plugin) throws NotFoundException {
        if (!getPlugins().containsKey(plugin)) {
            throw new NotFoundException("No plugin with ID " + plugin + " could be found on this system");
        }
        return getPlugins().get(plugin);
    }

    private final Map<String, XnatPluginBean> _plugins;
}
