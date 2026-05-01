/*
 * automation: org.nrg.automation.services.ScriptRunnerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.NrgService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Interface ScriptRunnerService.
 */
@SuppressWarnings("unused")
public interface ScriptRunnerService extends NrgService {

    /**
     * Gets the script for the specified script ID. If a script doesn't exist with that script ID, this method returns
     * null. Note that this method does no checking of the scope, associated entity, or event, but just returns the
     * script. You can get @{link Script scripts} for particular scopes or events by calling {@link
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScripts(Scope, String, String,
     * String, Map)}.
     *
     * @param scriptId the script id
     * @return the script
     */
    Script getScript(final String scriptId);

    /**
     * Gets the script for the specified script ID that is also associated (via {@link ScriptTrigger trigger}) with the
     * indicated scope, entity ID, and event. If a script doesn't exist with that script ID and trigger association,
     * this method returns null. Note that this method does no checking of the scope, associated entity, or event, but
     * just returns the script. You can get @{link Script scripts} for particular scopes or events by calling {@link
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScripts(Scope, String, String,
     * String, Map)}.
     *
     * @param scriptId the script id
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param filterMap the filter map
     * @return the script
     */
    Script getScript(final String scriptId, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap);

    /**
     * Deletes the script for the specified script ID. If a script doesn't exist with that script ID, this method throws
     * an {@link NrgServiceException}.
     *
     * @param scriptId the script id
     * @throws NrgServiceException the nrg service exception
     */
    void deleteScript(final String scriptId) throws NrgServiceException;

    /**
     * 
     * Gets the script for the specified scope and entity ID. This will only return scripts associated with the {@link
     * ScriptTrigger#DEFAULT_EVENT default event}. If a script and associated trigger doesn't exist for those criteria,
     * this method returns null.
     *
     * @param scope the scope
     * @param entityId the entity id
     * @return the scripts
     */
    List<Script> getScripts(final Scope scope, final String entityId);

    /**
      * Gets the script for the specified scope, entity, script ID, and event. If a script and associated trigger doesn't
     * exist for those criteria, this method returns null. For attributes you don't want to specify, pass null.
     *
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param filterMap the filter map
     * @return the script
     */
    List<Script> getScripts(final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap);

    /**
     * Gets all scripts registered on the system
     *
     * @return the scripts
     */
    List<Script> getScripts();

    /**
     * Gets a specific script
     *
     * @param scriptId the script id
     * @return the scripts
     */
    List<Script> getScripts(final String scriptId);

    /**
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the scope, event, and language arguments to default values. This is useful for creating a
     * site-wide script that can be run on demand.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     */
    void setScript(final String scriptId, final String scriptLabel, final String content);

    /**
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the event, language, eventClass and eventFilters arguments to default values.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param description the description
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final String description);

    /**
     * 
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the event, language, eventClass and eventFilters arguments to default values.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param scope the scope
     * @param entityId the entity id
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final Scope scope, final String entityId);

    /**
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the language, eventClass and evenFilters arguments to default values.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param eventFilters the event filters
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);

    /**
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the description to the default value.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param eventFilters the event filters
     * @param language the language
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language);

    /**
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the event, and language arguments to default values.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param description the description
     * @param scope the scope
     * @param entityId the entity id
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId);

    /**
     * A pared down version of {@link #setScript(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the language, eventClass and eventFilters arguments to default values.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param description the description
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param eventFilters the event filters
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);

    /**
     * Creates a script and trigger with the indicated attributes and saves them to the script repository. If objects
     * with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param scriptId the script id
     * @param scriptLabel the script label
     * @param content the content
     * @param description the description
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param eventFilters the event filters
     * @param language the language
     */
    void setScript(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language);

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script the script
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param eventFilters the event filters
     */
    void setScript(final Script script, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script the script
     * @param trigger the trigger
     */
    void setScript(final Script script, final ScriptTrigger trigger);
    
    /**
     * A convenience method that sets script and trigger property values from corresponding entries in the submitted
     * properties object.
     *
     * @param scriptId the script id
     * @param properties the properties
     * @throws NrgServiceException the nrg service exception
     */
    void setScript(final String scriptId, final Properties properties) throws NrgServiceException;

    /**
     * This attempts to run the submitted script. Note that this method does no checking of the scope, associated
     * entity, or event, but just executes the script. You can get @{link Script scripts} for particular scopes by
     * calling the {@link #getScripts()}, {@link ScriptRunnerService#getScripts(Scope, String)}, or {@link
     * #getScripts(Scope, String, String, String, Map)} methods.
     *
     * @param script the script
     * @return the script output
     * @throws NrgServiceException the nrg service exception
     */
    ScriptOutput runScript(final Script script) throws NrgServiceException;

    /**
     * This attempts to run the submitted script, passing in the <b>parameters</b> map as parameters to the script. Note
     * that this method does no checking of the scope, associated entity, or event, but just executes the script. You
     * can get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScripts(Scope, String, String, String, Map)} methods.
     *
     * @param script the script
     * @param parameters the parameters
     * @return the script output
     * @throws NrgServiceException the nrg service exception
     */
    ScriptOutput runScript(final Script script, Map<String, Object> parameters) throws NrgServiceException;

    /**
     * 
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, into the script execution environment. You can get @{link Script scripts} for
     * particular scopes by calling the {@link #getScripts()}, {@link ScriptRunnerService#getScripts(Scope, String)}, or
     * {@link #getScripts(Scope, String, String, String, Map)} methods.
     *
     * @param script the script
     * @param trigger the trigger
     * @return the script output
     * @throws NrgServiceException the nrg service exception
     */
    ScriptOutput runScript(final Script script, final ScriptTrigger trigger) throws NrgServiceException;

    /**
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, as well as the submitted parameters, into the script execution environment. You can
     * get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScripts(Scope, String, String, String, Map)} methods.
     *
     * @param script the script
     * @param trigger the trigger
     * @param parameters the parameters
     * @return the script output
     * @throws NrgServiceException the nrg service exception
     */
    ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters) throws NrgServiceException;

    /**
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, as well as the submitted parameters, into the script execution environment. You can
     * get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScripts(Scope, String, String, String, Map)} methods.
     *
     * @param script the script
     * @param trigger the trigger
     * @param parameters the parameters
     * @param exceptionOnError the exception on error
     * @return the script output
     * @throws NrgServiceException the nrg service exception
     */
    ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters, boolean exceptionOnError) throws NrgServiceException;

    /**
     * 
     * Set the system's {@link ScriptRunner script runners} to the submitted collection.
     *
     * @param runners the new runners
     */
    void setRunners(final Collection<Class<? extends ScriptRunner>> runners);

    /**
     * Checks for runner.
     *
     * @param language the language
     * @return true, if successful
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasRunner(final String language);

    /**
     * Indicates whether a {@link ScriptRunner script runner} compatible with the indicated language exists on the
     * system.
     *
     * @return the runners
     */
    List<String> getRunners();

    /**
     * Gets the {@link ScriptRunner script runner} compatible with the indicated language, if one exists on
     * the system. This returns <b>null</b> if no compatible runner is found.
     *
     * @param language the language
     * @return the runner
     */
    ScriptRunner getRunner(final String language);

    /**
     * Adds the submitted {@link ScriptRunner script runner} to the system.
     *
     * @param runner the runner
     */
    void addRunner(final Class<? extends ScriptRunner> runner);

    /**
     *  Adds the submitted {@link ScriptRunner script runners} to the system.
     *
     * @param runners the runners
     */
    @SuppressWarnings("unused")
    void addRunners(final Collection<Class<? extends ScriptRunner>> runners);
}
