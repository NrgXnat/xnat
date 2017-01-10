/*
 * web: org.nrg.xapi.rest.task.XnatTaskApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.task;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * The Class XnatTaskApi.
 */
@Api(description = "The XNAT Task API")
@XapiRestController
public class XnatTaskApi extends AbstractXapiRestController {
    
    /**
     * Instantiates a new xnat task api.
     *
     * @param userManagementService the user management service
     * @param roleHolder the role holder
     * @param xnatNodeInfoService the xnat node info service
     * @param xnatTaskService the xnat task service
     */
    @Autowired
    public XnatTaskApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final XnatNodeInfoService xnatNodeInfoService, final XnatTaskService xnatTaskService) {
        super(userManagementService, roleHolder);
        _xnatNodeInfoService = xnatNodeInfoService;
        _xnatTaskService = xnatTaskService;
    }

    /**
     * Gets the task list.
     *
     * @return nodeAndTaskConfigurationStatus
     */
    @ApiOperation(value = "Get node configuration status.", notes = "Returns node configration status for this installation.", response = Properties.class)
    @ApiResponses({@ApiResponse(code = 200, message = "An array of propeties"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/xnatTask/checkNodeConfigurationStatus"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Properties> getNodeConfigurationStatus() {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
       	final Properties prop = new Properties();
       	// We'll display configuration if we have any configured nodes for this installation.
       	for (final XnatNodeInfo nodeInfo : _xnatNodeInfoService.getAll()) {
       		if (!nodeInfo.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
       			prop.put("isConfigured", String.valueOf(true));
       			return new ResponseEntity<>(prop, HttpStatus.OK);
       		}
       		
       	}
       	prop.put("isConfigured", String.valueOf(false));
        return new ResponseEntity<>(prop, HttpStatus.OK);
    }

    /**
     * Gets the task list.
     *
     * @return the task list
     */
    @ApiOperation(value = "Get list of XnatTask classes.", notes = "Returns a list of XnatTask properties", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "An array of propeties"), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/xnatTask/taskList"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Properties>> getTaskList() {
        final HttpStatus status = isPermitted();
        if (status != null) {
            return new ResponseEntity<>(status);
        }
        try {
            return new ResponseEntity<>(getTaskPropertiesList(), HttpStatus.OK);
        } catch (Throwable t) {
            _log.error("XnatTaskApi exception:  " + t.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the task properties list.
     *
     * @return the task properties list
     */
    private List<Properties> getTaskPropertiesList() {
        final List<Properties> taskList = Lists.newArrayList();
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/task/*-xnat-task.properties")) {
                final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                // Add configuration element properties
                if (properties.containsKey(XnatTask.TASK_ID) && properties.containsKey(XnatTask.CLASS)) {
                	try {
						final Class<?> clazz = Class.forName(properties.getProperty(XnatTask.CLASS));
						final List<String> configEleYaml = _xnatTaskService.getConfigurationElementsYaml(clazz);
						final Gson gson = new Gson();
						final String configEleJson = gson.toJson(configEleYaml);
						if (configEleJson != null) {
							properties.put("configurationElementsYaml", configEleJson);
						}
					} catch (ClassNotFoundException e) {
						_log.trace("Could not find class for class value in task properties file (class=?)", properties.getProperty(XnatTask.CLASS));
					}
                }
                taskList.add(properties);
            }
        } catch (IOException e) {
            _log.debug("Could not load XnatTask class properties resources (META-INF/xnat/task/*-xnat-task.properties)");
        }
        return taskList;
    }

    /**
     * The Constant _log.
     */
    private static final Logger _log = LoggerFactory.getLogger(XnatTaskApi.class);
    
    /** The _xnat node info service. */
    private final XnatNodeInfoService _xnatNodeInfoService;
    
    /** The _xnat task service. */
    private final XnatTaskService _xnatTaskService;
    
}
