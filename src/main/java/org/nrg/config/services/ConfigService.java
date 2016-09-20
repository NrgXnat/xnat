/*
 * org.nrg.config.services.ConfigService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
     * @param scope    The {@link Scope scope} of the specified entity ID.
     *
     * @return The tools associated with the indicated entity.
     */
    List<String> getTools(Scope scope, String entityId);

    //retrieve a list of all Configuration objects for a specified tool (and project)
    List<Configuration> getConfigsByTool(String toolName);

    /**
     * Gets configurations associated with the tool and project.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param projectID    The project ID of the associated project.
     *
     * @return A list of the configurations associated with the tool and project.
     *
     * @deprecated Call {@link #getConfigsByTool(String, Scope, String)} instead.
     */
    @Deprecated
    List<Configuration> getConfigsByTool(String toolName, Long projectID);

    List<Configuration> getConfigsByTool(String toolName, Scope scope, String entityId);

    //retrieve the most recent configuration by tool and path. You can store a configuration at a null tool and path (and project)
    Configuration getConfig(String toolName, String path);

    /**
     * Gets configurations associated with the tool, path, and project.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param projectID    The project ID of the associated project.
     *
     * @return A list of the configurations associated with the tool, path, and project.
     *
     * @deprecated Call {@link #getConfig(String, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration getConfig(String toolName, String path, Long projectID);

    /**
     * Gets configurations associated with the tool, path, and entity.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param scope        The {@link Scope scope} of the specified entity ID.
     * @param entityId     The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return A list of the configurations associated with the tool, path, and project.
     */
    Configuration getConfig(String toolName, String path, Scope scope, String entityId);

    /**
     * Retrieves the most recent configuration by tool and path (and project). Does not include any meta data, only the
     * configuration.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     *
     * @return The contents of the configuration associated with the tool and path.
     */
    String getConfigContents(String toolName, String path);

    /**
     * Gets the contents of configurations associated with the tool, path, and project.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param projectID    The project ID of the associated project.
     *
     * @return A list of the configurations associated with the tool, path, and project.
     *
     * @deprecated Call {@link #getConfig(String, String, Scope, String)} instead.
     */
    @Deprecated
    String getConfigContents(String toolName, String path, Long projectID);

    /**
     * Gets the contents of configurations associated with the tool, path, and project.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param scope        The {@link Scope scope} of the specified entity ID.
     * @param entityId     The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return A list of the configurations associated with the tool, path, and entity.
     */
    String getConfigContents(String toolName, String path, Scope scope, String entityId);

    /**
     * Gets the contents of configurations associated with the tool, path, and project. If the ID is valid, but the
     * configuration does not match the tool name and path, this returns null.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param id           The ID of the target configuration.
     * @param projectID    The project ID of the associated project.
     *
     * @return A list of the configurations associated with the tool, path, and entity.
     *
     * @deprecated Call {@link #getConfigById(String, String, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration getConfigById(String toolName, String path, String id, Long projectID);

    /**
     * Gets the contents of configurations associated with the tool, path, and entity. If the ID is valid, but the
     * configuration does not match the tool name and path, this returns null.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param id           The ID of the target configuration.
     * @param scope        The {@link Scope scope} of the specified entity ID.
     * @param entityId     The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return A list of the configurations associated with the tool, path, and entity.
     */
    Configuration getConfigById(String toolName, String path, String id, Scope scope, String entityId);

    /**
     * Retrieves the configuration by tool, path, and version.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param version     The version of the configuration you wish to retrieve.
     * @return The specified version of the configuration.
     */
    Configuration getConfigByVersion(String toolName, String path, int version);

    /**
     * Retrieves the configuration by tool, path, project, and version.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param version      The version of the configuration you wish to retrieve.
     * @param projectID    The project ID of the associated project.
     *
     * @return The specified version of the configuration.
     *
     * @deprecated Call {@link #getConfigByVersion(String, String, int, Scope, String)} instead.
     */
    @Deprecated
    Configuration getConfigByVersion(String toolName, String path, int version, Long projectID);

    /**
     * Retrieves the configuration by tool, path, entity, and version.
     *
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param version      The version of the configuration you wish to retrieve.
     * @param scope        The {@link Scope scope} of the specified entity ID.
     * @param entityId     The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return The specified version of the configuration.
     */
    Configuration getConfigByVersion(String toolName, String path, int version, Scope scope, String entityId);

    //create or replace a configuration specified by the parameters. This will set the status to enabled.
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException;

    /**
     * Replaces the configuration indicated by the various parameters. The username is used to record who was
     * responsible for the change.
     *
     * @param xnatUser     The username of the user requesting the update.
     * @param reason       The reason for the update.
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param contents     The contents of the configuration.
     * @param projectID    The project ID of the associated project.
     *
     * @return The updated configuration object.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     * @deprecated Call {@link #replaceConfig(String, String, String, String, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException;

    /**
     * Replaces the configuration indicated by the various parameters. The username is used to record who was
     * responsible for the change.
     *
     * @param xnatUser     The username of the user requesting the update.
     * @param reason       The reason for the update.
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param contents     The contents of the configuration.
     * @param scope        The {@link Scope scope} of the specified entity ID.
     * @param entityId     The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return The updated configuration object.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Scope scope, String entityId) throws ConfigServiceException;

    /**
     * Replaces the configuration indicated by the various parameters. The username is used to record who was
     * responsible for the change.
     *
     * @param xnatUser     The username of the user requesting the update.
     * @param reason       The reason for the update.
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param unversioned  Indicates that the configuration is unversioned.
     * @param contents     The contents of the configuration.
     *
     * @return The updated configuration object.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents) throws ConfigServiceException;

    /**
     * Replaces the configuration indicated by the various parameters. The username is used to record who was
     * responsible for the change.
     *
     * @param xnatUser     The username of the user requesting the update.
     * @param reason       The reason for the update.
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param unversioned  Indicates that the configuration is unversioned.
     * @param contents     The contents of the configuration.
     * @param projectID    The project ID of the associated project.
     *
     * @return The updated configuration object.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     * @deprecated Call {@link #replaceConfig(String, String, String, String, Boolean, String, Scope, String)} instead.
     */
    @Deprecated
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Long projectID) throws ConfigServiceException;

    /**
     * Replaces the configuration indicated by the various parameters. The username is used to record who was
     * responsible for the change.
     *
     * @param xnatUser     The username of the user requesting the update.
     * @param reason       The reason for the update.
     * @param toolName     The name of the tool for which you wish to retrieve configurations.
     * @param path         The path for which you wish to retrieve configurations.
     * @param unversioned  Indicates that the configuration is unversioned.
     * @param contents     The contents of the configuration.
     * @param scope        The {@link Scope scope} of the specified entity ID.
     * @param entityId     The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return The updated configuration object.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, Boolean unversioned, String contents, Scope scope, String entityId) throws ConfigServiceException;

    /**
     * Returns the status for the configuration specified by the parameters.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     *
     * @return The current status of the indicated configuration.
     */
    String getStatus(String toolName, String path);

    /**
     * Returns the status for the configuration specified by the parameters.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param projectID   The project ID of the associated project.
     *
     * @return The current status of the indicated configuration.
     *
     * @deprecated Call {@link #getStatus(String, String, Scope, String)} instead.
     */
    @Deprecated
    String getStatus(String toolName, String path, Long projectID);

    /**
     * Returns the status for the configuration specified by the parameters.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param scope       The {@link Scope scope} of the specified entity ID.
     * @param entityId    The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return The current status of the indicated configuration.
     */
    String getStatus(String toolName, String path, Scope scope, String entityId);

    /**
     * Enables the indicated configuration.
     *
     * @param xnatUser    The username of the user requesting the update.
     * @param reason      The reason for the update.
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    void enable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException;

    /**
     * Enables the indicated configuration.
     *
     * @param xnatUser    The username of the user requesting the update.
     * @param reason      The reason for the update.
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param projectID   The project ID of the associated project.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     *
     * @deprecated Call {@link #enable(String, String, String, String, Scope, String)} instead.
     */
    @Deprecated
    void enable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException;

    /**
     * Enables the indicated configuration.
     *
     * @param xnatUser    The username of the user requesting the update.
     * @param reason      The reason for the update.
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param scope       The {@link Scope scope} of the specified entity ID.
     * @param entityId    The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    void enable(String xnatUser, String reason, String toolName, String path, Scope scope, String entityId) throws ConfigServiceException;

    /**
     * Disables the indicated configuration.
     *
     * @param xnatUser    The username of the user requesting the update.
     * @param reason      The reason for the update.
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    void disable(String xnatUser, String reason, String toolName, String path) throws ConfigServiceException;

    /**
     * Disables the indicated configuration.
     *
     * @param xnatUser    The username of the user requesting the update.
     * @param reason      The reason for the update.
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param projectID   The project ID of the associated project.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     *
     * @deprecated Call {@link #disable(String, String, String, String, Scope, String)} instead.
     */
    @Deprecated
    void disable(String xnatUser, String reason, String toolName, String path, Long projectID) throws ConfigServiceException;

    /**
     * Disables the indicated configuration.
     *
     * @param xnatUser    The username of the user requesting the update.
     * @param reason      The reason for the update.
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param scope       The {@link Scope scope} of the specified entity ID.
     * @param entityId    The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    void disable(String xnatUser, String reason, String toolName, String path, Scope scope, String entityId) throws ConfigServiceException;

    /**
     * Return a list of all configurations specified by the path ordered by date uploaded.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     *
     * @return A list of all configurations specified by the path ordered by date uploaded.
     */
    List<Configuration> getHistory(String toolName, String path);

    /**
     * Return a list of all configurations specified by the path ordered by date uploaded.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param projectID   The project ID of the associated project.
     *
     * @return A list of all configurations specified by the path ordered by date uploaded.
     *
     * @deprecated Call {@link #getHistory(String, String, Scope, String)} instead.
     */
    @Deprecated
    List<Configuration> getHistory(String toolName, String path, Long projectID);

    /**
     * Return a list of all configurations specified by the path ordered by date uploaded.
     *
     * @param toolName    The name of the tool for which you wish to retrieve configurations.
     * @param path        The path for which you wish to retrieve configurations.
     * @param scope       The {@link Scope scope} of the specified entity ID.
     * @param entityId    The ID of the target entity. The ID is understood in relation to the scope.
     *
     * @return A list of all configurations specified by the path ordered by date uploaded.
     */
    List<Configuration> getHistory(String toolName, String path, Scope scope, String entityId);

    /**
     * Retrieves a list of the IDs of all projects that have a configuration.
     *
     * @return A list of the IDs of all projects that have a configuration.
     */
    List<String> getProjects();

    /**
     * Retrieves a list of the IDs of all projects that have a configuration for the indicated tool.
     *
     * @param toolName    The toolname to find.
     *
     * @return A list of the IDs of all projects that have a configuration for the indicated tool.
     */
    List<String> getProjects(String toolName);
}
