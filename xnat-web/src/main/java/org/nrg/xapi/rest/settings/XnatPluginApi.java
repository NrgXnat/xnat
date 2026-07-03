/*
 * web: org.nrg.xapi.rest.settings.XnatPluginApi
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

@Tag(name = "XNAT Plugin API")
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

    @Operation(summary = "Returns a list of all of the installed and active XNAT plugins with their properties.", description = "The maps returned from this call include all of the properties specified in the plugin's property file.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public Map<String, XnatPluginBean> getAllPlugins() {
        return getPlugins();
    }

    @Operation(summary = "Returns the indicated XNAT plugin with its properties.", description = "The map returned from this call include all of the properties specified in the plugin's property file.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XNAT plugin properties successfully retrieved."),
                   @ApiResponse(responseCode = "401", description = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(responseCode = "404", description = "The requested resource wasn't found."),
                   @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(value = "{plugin}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = {RequestMethod.GET})
    public XnatPluginBean getRequestedPlugin(@PathVariable("plugin") final String plugin) throws NotFoundException {
        if (!getPlugins().containsKey(plugin)) {
            throw new NotFoundException("No plugin with ID " + plugin + " could be found on this system");
        }
        return getPlugins().get(plugin);
    }

    private final Map<String, XnatPluginBean> _plugins;
}
