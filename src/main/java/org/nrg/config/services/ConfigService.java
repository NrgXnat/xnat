/*
 * org.nrg.config.services.ConfigService
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.services;

import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.services.NrgService;

import java.util.List;


public interface ConfigService extends NrgService {
	
	public static final int MAX_FILE_LENGTH = ConfigurationData.MAX_FILE_LENGTH;
	
	/*
	 *    HEY, YOU. All methods will return null if no configuration exists
	 *    "disabled" configurations are always returned. it is up to the client
	 *    to determine what to do with a configuration. 
	 */
	
	
	//return the most recent version of active configurations.
	public List<Configuration> getAll();
	
	//retrieve a Configuration by the ID set by the persistence mechanism
	public Configuration getById(Long id);
	public Configuration getById(Long id, boolean preload);

	//retrieve a String list of all tools (and project) that have configurations. 
	public List<String> getTools();
	public List<String> getTools(Long projectID);
	
	//retrieve a list of all Configuration objects for a specified tool (and project)
	public List<Configuration> getConfigsByTool(String toolName);
	public List<Configuration> getConfigsByTool(String toolName, Long projectID);
	
	//retrieve the most recent configuration by tool and path. You can store a configuration at a null tool and path (and project)
	public Configuration getConfig(String toolName, String path);
	public Configuration getConfig(String toolName, String path, Long projectID);
	
	//retrieve the most recent configuration by tool and path (and project). Do not include any meta data. only the configuration.
	public String getConfigContents(String toolName, String path);
	public String getConfigContents(String toolName, String path, Long projectID);
	
	//retrieve the configuration by tool, path, and the ID set by the persistence mechanism (and project) 
	//if the ID is valid, but the configuration does not match toolName and path, return null
	public Configuration getConfigById(String toolName, String path, String id);
	public Configuration getConfigById(String toolName, String path, String id, Long projectID);
	
	//retrieve the configuration by tool, path, and version (and project) 
	//if the ID is valid, but the configuration does not match toolName and path, return null
	public Configuration getConfigByVersion(String toolName, String path, int version);
	public Configuration getConfigByVersion(String toolName, String path, int version, Long projectID);
	
	//create or replace a configuration specified by the parameters. This will set the status to enabled.
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Long projectID) throws ConfigServiceException;

	//return the status property for the configuration specified by the parameters.
	public String getStatus(String toolName, String path);
	public String getStatus(String toolName, String path, Long projectID);
	
	//set the configuration's status property to "enabled"
	public void enable(String xnatUser, String reason, String toolName, String path);
	public void enable(String xnatUser, String reason, String toolName, String path, Long projectID);

	//set the configuration's status property to "disabled"
	public void disable(String xnatUser, String reason, String toolName, String path);
	public void disable(String xnatUser, String reason, String toolName, String path, Long projectID);
	
	//return a list of all configurations specified by the path ordered by date uploaded.
	public List<Configuration> getHistory(String toolName, String path);
	public List<Configuration> getHistory(String toolName, String path, Long projectID);
	
	//retrieve a String list of all projects that have a configuration.
	public List<Long> getProjects();
	public List<Long> getProjects(String toolName);
}
