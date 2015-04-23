package org.nrg.automation.services;

import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.NrgService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface ScriptRunnerService extends NrgService {

    /**
     * Gets the script for the specified script ID. If a script doesn't exist with that script ID, this method returns
     * null. Note that this method does no checking of the scope, associated entity, or event, but just returns the
     * script. You can get @{link Script scripts} for particular scopes or events by calling {@link
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScript(Scope, String, String)}.
     *
     * @param scriptId The ID of the script to locate.
     *
     * @return The {@link Script} object if a script with the indicated script ID is found, <b>null</b> otherwise.
     */
    public abstract Script getScript(final String scriptId);

    /**
     * Gets the script for the specified script ID that is also associated (via {@link ScriptTrigger trigger}) with the
     * indicated scope, entity ID, and event. If a script doesn't exist with that script ID and trigger association,
     * this method returns null. Note that this method does no checking of the scope, associated entity, or event, but
     * just returns the script. You can get @{link Script scripts} for particular scopes or events by calling {@link
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScript(Scope, String, String)}.
     *
     * @return The {@link Script} object if a script with the indicated script ID and association is found, <b>null</b>
     * otherwise.
     */
    public abstract Script getScript(final String scriptId, final Scope scope, final String entityId, final String event);

    /**
     * Deletes the script for the specified script ID. If a script doesn't exist with that script ID, this method throws
     * an {@link NrgServiceException}.
     *
     * @param scriptId The ID of the script to delete.
     *
     * @throws NrgServiceException When a script with the indicated script ID can not be found.
     */
    public abstract void deleteScript(final String scriptId) throws NrgServiceException;

    /**
     * Gets the script for the specified scope and entity ID. This will only return scripts associated with the {@link
     * ScriptTrigger#DEFAULT_EVENT default event}. If a script and associated trigger doesn't exist for those criteria,
     * this method returns null.
     *
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     *
     * @return The associated {@link Script scripts} if any with the indicated associations is found, <b>null</b>
     * otherwise.
     */
    public abstract List<Script> getScripts(final Scope scope, final String entityId);

    /**
     * Gets the script for the specified scope, entity, script ID, and event. If a script and associated trigger doesn't
     * exist for those criteria, this method returns null. For attributes you don't want to specify, pass null.
     *
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     *
     * @return The associated {@link Script script} if any with the indicated associations is found, <b>null</b>
     * otherwise.
     */
    public abstract Script getScript(final Scope scope, final String entityId, final String event);

    /**
     * Gets all scripts registered on the system.
     *
     * @return All scripts on the system.
     */
    public abstract List<Script> getScripts();

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the scope, event, language, and language version arguments to default values. This is useful for creating a
     * site-wide script that can be run on demand.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     */
    public abstract void setScript(final String scriptId, final String content);

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the scope, event, language, and language version arguments to default values. This is useful for creating a
     * site-wide script that can be run on demand.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     */
    public abstract void setScript(final String scriptId, final String content, final String description);

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the event, language, and language version arguments to default values.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     */
    public abstract void setScript(final String scriptId, final String content, final Scope scope, final String entityId);

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the language and language version arguments to default values.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     */
    public abstract void setScript(final String scriptId, final String content, final Scope scope, final String entityId, final String event);

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the description to the default value.
     *
     * @param scriptId        The ID of the script to set.
     * @param content         The content to set for the script.
     * @param scope           The scope for the script.
     * @param entityId        The associated entity for the script.
     * @param event           The event for the script.
     * @param language        The script language for this script.
     * @param languageVersion The compatible language version(s).
     */
    public abstract void setScript(final String scriptId, final String content, final Scope scope, final String entityId, final String event, final String language, final String languageVersion);

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the event, language, and language version arguments to default values.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     */
    public abstract void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId);

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String, String)} that
     * sets the language and language version arguments to default values.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param event       The event for the script.
     */
    public abstract void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String event);

    /**
     * Creates a script and trigger with the indicated attributes and saves them to the script repository. If objects
     * with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param scriptId        The ID of the script to set.
     * @param content         The content to set for the script.
     * @param description     The description of the script.
     * @param scope           The scope for the script.
     * @param entityId        The associated entity for the script.
     * @param event           The event for the script.
     * @param language        The script language for this script.
     * @param languageVersion The compatible language version(s).
     */
    public abstract void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String event, final String language, final String languageVersion);

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script   The script object to set.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     */
    public abstract void setScript(final Script script, final Scope scope, final String entityId, final String event);

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script  The script object to set.
     * @param trigger The script trigger to set.
     */
    public abstract void setScript(final Script script, final ScriptTrigger trigger);

    /**
     * A convenience method that sets script and trigger property values from corresponding entries in the submitted
     * properties object.
     *
     * @param scriptId   The ID of the script to set.
     * @param properties The properties to set on the script.
     */
    public abstract void setScript(final String scriptId, final Properties properties) throws NrgServiceException;

    /**
     * This attempts to run the submitted script. Note that this method does no checking of the scope, associated
     * entity, or event, but just executes the script. You can get @{link Script scripts} for particular scopes by
     * calling the {@link #getScripts()}, {@link ScriptRunnerService#getScripts(Scope, String)}, or {@link
     * #getScript(Scope, String, String)} methods.
     *
     * @param script The script to run.
     *
     * @return The results of the script execution.
     */
    public abstract Object runScript(final Script script) throws NrgServiceException;

    /**
     * This attempts to run the submitted script, passing in the <b>parameters</b> map as parameters to the script. Note
     * that this method does no checking of the scope, associated entity, or event, but just executes the script. You
     * can get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScript(Scope, String, String)} methods.
     *
     * @param script     The script to run.
     * @param parameters The parameters to pass to the script.
     *
     * @return The results of the script execution.
     */
    public abstract Object runScript(final Script script, Map<String, Object> parameters) throws NrgServiceException;

    /**
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, into the script execution environment. You can get @{link Script scripts} for
     * particular scopes by calling the {@link #getScripts()}, {@link ScriptRunnerService#getScripts(Scope, String)}, or
     * {@link #getScript(Scope, String, String)} methods.
     *
     * @param script  The script to run.
     * @param trigger The associated trigger for the script execution.
     *
     * @return The results of the script execution.
     */
    public abstract Object runScript(final Script script, final ScriptTrigger trigger) throws NrgServiceException;

    /**
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, as well as the submitted parameters, into the script execution environment. You can
     * get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScript(Scope, String, String)} methods.
     *
     * @param script     The script to run.
     * @param trigger    The associated trigger for the script execution.
     * @param parameters The parameters to pass to the script.
     *
     * @return The results of the script execution.
     */
    public abstract Object runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters) throws NrgServiceException;

    /**
     * Set the system's {@link ScriptRunner script runners} to the submitted collection.
     *
     * @param runners The {@link ScriptRunner script runners} to be added to the system.
     */
    public abstract void setRunners(final Collection<ScriptRunner> runners);

    boolean hasRunner(String language);

    /**
     * Indicates whether a {@link ScriptRunner script runner} compatible with the indicated language and version exists
     * on the system.
     *
     * @param language The script language for the script.
     * @param version  The script version.
     *
     * @return <b>true</b> if a compatible version exists, <b>false</b> otherwise.
     */
    public abstract boolean hasRunner(final String language, final String version);

    ScriptRunner getRunner(String language);

    /**
     * Gets the {@link ScriptRunner script runner} compatible with the indicated language and version, if one exists on
     * the system. This returns <b>null</b> if no compatible runner is found.
     *
     * @param language The script language for the script.
     * @param version  The script version.
     *
     * @return The compatible {@link ScriptRunner script runner} if a compatible version exists, <b>null</b> otherwise.
     */
    public abstract ScriptRunner getRunner(final String language, final String version);

    /**
     * Adds the submitted {@link ScriptRunner script runner} to the system.
     *
     * @param runner The {@link ScriptRunner script runner} to be added to the system.
     */
    public abstract void addRunner(final ScriptRunner runner);

    /**
     * Adds the submitted {@link ScriptRunner script runners} to the system.
     *
     * @param runners The {@link ScriptRunner script runners} to be added to the system.
     */
    public abstract void addRunners(final Collection<ScriptRunner> runners);
}
