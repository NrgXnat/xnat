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

    /**
     * Retrieves a particular configuration by the ID set by the persistence mechanism.
     *
     * @param id The ID of the persisted configuration.
     *
     * @return The requested configuration if it exists.
     */
    Configuration getById(long id);

    /**
     * Gets all of the tools associated with the system.
     *
     * @return The tools associated with the system.
     */
    List<String> getTools();

    /**
     * Gets all of the tools associated with the indicated project.
     *
     * @param projectID The project data info attribute of the desired project.
     *
     * @return The tools associated with the indicated project.
     *
     * @deprecated Call {@link #getTools(Scope, String)} instead.
     */
    @Deprecated
    List<String> getTools(Long projectID);

    /**
     * Gets all of the tools associated with the indicated entity.
     *
     * @param entityId The ID of the entity with which the configuration is associated.
     *
     * @return The tools associated with the indicated entity.
     */
    List<String> getTools(Scope scope, String entityId);

    //retrieve a list of all Configuration objects for a specified tool (and project)
    List<Configuration> getConfigsByTool(String toolName);

    /**
     * @param toolName
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getConfigsByTool(String, Scope, String)} instead.
     */
    @Deprecated
    List<Configuration> getConfigsByTool(String toolName, Long projectID);

    List<Configuration> getConfigsByTool(String toolName, Scope scope, String entityId);

    //retrieve the most recent configuration by tool and path. You can store a configuration at a null tool and path (and project)
    Configuration getConfig(String toolName, String path);

    /**
     * @param toolName
     * @param path
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getConfig(String, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration getConfig(String toolName, String path, Long projectID);

    Configuration getConfig(String toolName, String path, Scope scope, String entityId);

    //retrieve the most recent configuration by tool and path (and project). Do not include any meta data. only the configuration.
    String getConfigContents(String toolName, String path);

    /**
     * @param toolName
     * @param path
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getConfigContents(String, String, Scope, String)} instead.
     */
    @Deprecated
    String getConfigContents(String toolName, String path, Long projectID);

    String getConfigContents(String toolName, String path, Scope scope, String entityId);

    /**
     * @param toolName
     * @param path
     * @param id
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getConfigById(String, String, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration getConfigById(String toolName, String path, String id, Long projectID);

    Configuration getConfigById(String toolName, String path, String id, Scope scope, String entityId);

    //retrieve the configuration by tool, path, and version (and project)
    //if the ID is valid, but the configuration does not match toolName and path, return null
    Configuration getConfigByVersion(String toolName, String path, int version);

    /**
     * @param toolName
     * @param path
     * @param version
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getConfigByVersion(String, String, int, Scope, String)} instead.
     */
    @Deprecated
    Configuration getConfigByVersion(String toolName, String path, int version, Long projectID);

    Configuration getConfigByVersion(String toolName, String path, int version, Scope scope, String entityId);

    //create or replace a configuration specified by the parameters. This will set the status to enabled.
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException;

    /**
     * @param xnatUser
     * @param reason
     * @param toolName
     * @param path
     * @param contents
     * @param projectID
     *
     * @return
     *
     * @throws ConfigServiceException
     * @deprecated Call {@link #replaceConfig(String, String, String, String, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException;

    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Scope scope, String entityId) throws ConfigServiceException;

    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents) throws ConfigServiceException;

    /**
     * @param xnatUser
     * @param reason
     * @param toolName
     * @param path
     * @param unversioned
     * @param contents
     * @param projectID
     *
     * @return
     *
     * @throws ConfigServiceException
     * @deprecated Call {@link #replaceConfig(String, String, String, String, Boolean, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Long projectID) throws ConfigServiceException;

    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Scope scope, String entityId) throws ConfigServiceException;

    //return the status property for the configuration specified by the parameters.
    String getStatus(String toolName, String path);

    /**
     * @param toolName
     * @param path
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getStatus(String, String, Scope, String)} instead.
     */
    @Deprecated
    String getStatus(String toolName, String path, Long projectID);

    String getStatus(String toolName, String path, Scope scope, String entityId);

    //set the configuration's status property to "enabled"
    void enable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException;

    /**
     * @param xnatUser
     * @param reason
     * @param toolName
     * @param path
     * @param projectID
     *
     * @throws ConfigServiceException
     * @deprecated Call {@link #enable(String, String, String, String, Scope, String)} instead.
     */
    @Deprecated
    void enable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException;

    void enable(String xnatUser, String reason, String toolName, String path, Scope scope, String entityId) throws ConfigServiceException;

    //set the configuration's status property to "disabled"
    void disable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException;

    /**
     * @param xnatUser
     * @param reason
     * @param toolName
     * @param path
     * @param projectID
     *
     * @throws ConfigServiceException
     * @deprecated Call {@link #disable(String, String, String, String, Scope, String)} instead.
     */
    @Deprecated
    void disable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException;

    void disable(String xnatUser, String reason, String toolName, String path, Scope scope, String entityId) throws ConfigServiceException;

    //return a list of all configurations specified by the path ordered by date uploaded.
    List<Configuration> getHistory(String toolName, String path);

    /**
     * @param toolName
     * @param path
     * @param projectID
     *
     * @return
     *
     * @deprecated Call {@link #getHistory(String, String, Scope, String)} instead.
     */
    @Deprecated
    List<Configuration> getHistory(String toolName, String path, Long projectID);

    List<Configuration> getHistory(String toolName, String path, Scope scope, String entityId);

    //retrieve a String list of all projects that have a configuration.
    List<String> getProjects();

    List<String> getProjects(String toolName);
}
