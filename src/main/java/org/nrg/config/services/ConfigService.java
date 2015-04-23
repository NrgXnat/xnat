/*
 * org.nrg.config.services.ConfigService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/12/13 4:00 PM
 */
package org.nrg.config.services;

import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import java.util.List;


public interface ConfigService extends BaseHibernateService<Configuration> {
	
	int MAX_FILE_LENGTH = ConfigurationData.MAX_FILE_LENGTH;
	
	/*
	 *    HEY, YOU. All methods will return null if no configuration exists
	 *    "disabled" configurations are always returned. it is up to the client
	 *    to determine what to do with a configuration. 
	 */
	
	//return the most recent version of active configurations.
	List<Configuration> getAll();
	
	//retrieve a Configuration by the ID set by the persistence mechanism
	Configuration getById(long id);

	//retrieve a String list of all tools (and project) that have configurations. 
	public List<String> getTools();

	/**
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getTools(Scope,String)} instead.
	 */
	@Deprecated
	List<String> getTools(Long projectID);
	public List<String> getTools(Scope scope, String entityId);

	//retrieve a list of all Configuration objects for a specified tool (and project)
	public List<Configuration> getConfigsByTool(String toolName);

	/**
	 *
	 * @param toolName
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getConfigsByTool(String, Scope, String)} instead.
	 */
	@Deprecated
	public List<Configuration> getConfigsByTool(String toolName, Long projectID);
	public List<Configuration> getConfigsByTool(String toolName, Scope scope, String entityId);

	//retrieve the most recent configuration by tool and path. You can store a configuration at a null tool and path (and project)
	public Configuration getConfig(String toolName, String path);

	/**
	 *
	 * @param toolName
	 * @param path
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getConfig(String, String, Scope, String)} instead.
	 */
	@Deprecated
	public Configuration getConfig(String toolName, String path, Long projectID);
	public Configuration getConfig(String toolName, String path, Scope scope, String entityId);

	//retrieve the most recent configuration by tool and path (and project). Do not include any meta data. only the configuration.
	public String getConfigContents(String toolName, String path);

	/**
	 *
	 * @param toolName
	 * @param path
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getConfigContents(String, String, Scope, String)} instead.
	 */
	@Deprecated
	public String getConfigContents(String toolName, String path, Long projectID);
	public String getConfigContents(String toolName, String path, Scope scope, String entityId);

	//retrieve the configuration by tool, path, and the ID set by the persistence mechanism (and project) 
	//if the ID is valid, but the configuration does not match toolName and path, return null
	public Configuration getConfigById(String toolName, String path, String id);

	/**
	 *
	 * @param toolName
	 * @param path
	 * @param id
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getConfigById(String, String, String, Scope, String)} instead.
	 */
	@Deprecated
	public Configuration getConfigById(String toolName, String path, String id, Long projectID);
	public Configuration getConfigById(String toolName, String path, String id, Scope scope, String entityId);

	//retrieve the configuration by tool, path, and version (and project) 
	//if the ID is valid, but the configuration does not match toolName and path, return null
	public Configuration getConfigByVersion(String toolName, String path, int version);

	/**
	 *
	 * @param toolName
	 * @param path
	 * @param version
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getConfigByVersion(String, String, int, Scope, String)} instead.
	 */
	@Deprecated
	public Configuration getConfigByVersion(String toolName, String path, int version, Long projectID);
	public Configuration getConfigByVersion(String toolName, String path, int version, Scope scope, String entityId);

	//create or replace a configuration specified by the parameters. This will set the status to enabled.
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException;

	/**
	 *
	 * @param xnatUser
	 * @param reason
	 * @param toolName
	 * @param path
	 * @param contents
	 * @param projectID
	 * @return
	 * @throws ConfigServiceException
	 * @deprecated Call {@link #replaceConfig(String, String, String, String, String, Scope, String)} instead.
	 */
	@Deprecated
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Scope scope, String entityId) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents) throws ConfigServiceException;

	/**
	 *
	 * @param xnatUser
	 * @param reason
	 * @param toolName
	 * @param path
	 * @param unversioned
	 * @param contents
	 * @param projectID
	 * @return
	 * @throws ConfigServiceException
	 * @deprecated Call {@link #replaceConfig(String, String, String, String, Boolean, String, Scope, String)} instead.
	 */
	@Deprecated
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Long projectID) throws ConfigServiceException;
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Scope scope, String entityId) throws ConfigServiceException;

	//return the status property for the configuration specified by the parameters.
	public String getStatus(String toolName, String path);

	/**
	 *
	 * @param toolName
	 * @param path
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getStatus(String, String, Scope, String)} instead.
	 */
	@Deprecated
	public String getStatus(String toolName, String path, Long projectID);
	public String getStatus(String toolName, String path, Scope scope, String entityId);

	//set the configuration's status property to "enabled"
	public void enable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException;

	/**
	 *
	 * @param xnatUser
	 * @param reason
	 * @param toolName
	 * @param path
	 * @param projectID
	 * @throws ConfigServiceException
	 * @deprecated Call {@link #enable(String, String, String, String, Scope, String)} instead.
	 */
	@Deprecated
	public void enable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException;
	public void enable(String xnatUser, String reason, String toolName, String path, Scope scope, String entityId) throws ConfigServiceException;

	//set the configuration's status property to "disabled"
	public void disable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException;

	/**
	 *
	 * @param xnatUser
	 * @param reason
	 * @param toolName
	 * @param path
	 * @param projectID
	 * @throws ConfigServiceException
	 * @deprecated Call {@link #disable(String, String, String, String, Scope, String)} instead.
	 */
	@Deprecated
	public void disable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException;
	public void disable(String xnatUser, String reason, String toolName, String path, Scope scope, String entityId) throws ConfigServiceException;

	//return a list of all configurations specified by the path ordered by date uploaded.
	public List<Configuration> getHistory(String toolName, String path);

	/**
	 *
	 * @param toolName
	 * @param path
	 * @param projectID
	 * @return
	 * @deprecated Call {@link #getHistory(String, String, Scope, String)} instead.
	 */
	@Deprecated
	public List<Configuration> getHistory(String toolName, String path, Long projectID);
	public List<Configuration> getHistory(String toolName, String path, Scope scope, String entityId);

	//retrieve a String list of all projects that have a configuration.
	public List<Long> getProjects();
	public List<Long> getProjects(String toolName);
}
