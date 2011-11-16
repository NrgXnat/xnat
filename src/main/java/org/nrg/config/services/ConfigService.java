package org.nrg.config.services;

import java.util.List;

import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;

public interface ConfigService {
	
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

	//retrieve a String list of all tools (and project) that have configurations. 
	public List<String> getTools();
	public List<String> getTools(String projectID);
	
	//retrieve a list of all Configuration objects for a specified tool (and project)
	public List<Configuration> getConfigsByTool(String toolName);
	public List<Configuration> getConfigsByTool(String toolName, String projectID);
	
	//retrieve the most recent configuration by tool and path. You can store a configuration at a null tool and path (and project)
	public Configuration getConfig(String toolName, String path);
	public Configuration getConfig(String toolName, String path, String projectID);
	
	//retrieve the most recent configuration by tool and path (and project). Do not include any meta data. only the configuration.
	public String getConfigContents(String toolName, String path);
	public String getConfigContents(String toolName, String path, String projectID);
	
	//retrieve the most recent configuration by tool, path, and the ID set by the persistence mechanism (and project) 
	//if the ID is valid, but the configuration does not match toolName and path, return null
	public Configuration getConfigById(String toolName, String path, String id);
	public Configuration getConfigById(String toolName, String path, String id, String projectID);
	
	//create or replace a configuration specified by the parameters. This will set the status to enabled.
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, String projectID) throws ConfigServiceException;
	
	//return the status property for the configuration specified by the parameters.
	public String getStatus(String toolName, String path);
	public String getStatus(String toolName, String path, String projectID);
	
	//set the configuration's status property to "enabled"
	public void enable(String xnatUser, String reason, String toolName, String path);
	public void enable(String xnatUser, String reason, String toolName, String path, String projectID);

	//set the configuration's status property to "disabled"
	public void disable(String xnatUser, String reason, String toolName, String path);
	public void disable(String xnatUser, String reason, String toolName, String path, String projectID);
	
	//return a list of all configurations specified by the path ordered by date uploaded.
	public List<Configuration> getHistory(String toolName, String path);
	public List<Configuration> getHistory(String toolName, String path, String projectID);
	
	//retrieve a String list of all projects that have a configuration.
	public List<String> getProjects();
	public List<String> getProjects(String toolName);
}
