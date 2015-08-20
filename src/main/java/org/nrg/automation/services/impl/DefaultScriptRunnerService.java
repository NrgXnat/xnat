package org.nrg.automation.services.impl;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.automation.annotations.Supports;
import org.nrg.automation.entities.Event;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.automation.services.EventService;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.font.ScriptRun;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class DefaultScriptRunnerService implements ScriptRunnerService, InitializingBean {

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if
     * initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (_packages == null) {
            _packages = new ArrayList<>();
        } else if (!_packages.contains("org.nrg.automation.runners")) {
            _packages.add("org.nrg.automation.runners");
        }

        final List<Class<? extends ScriptRunner>> runners = new ArrayList<>();
        for (final String pkg : _packages) {
            final Reflections reflections = new Reflections(pkg, new SubTypesScanner());
            final Set<Class<? extends ScriptRunner>> runnerClasses = reflections.getSubTypesOf(ScriptRunner.class);
            if (runnerClasses != null) {
                for (final Class<? extends ScriptRunner> runner : runnerClasses) {
                    if (runner.isAnnotationPresent(Supports.class)) {
                        runners.add(runner);
                    }
                }
            }
        }
        setRunners(runners);
    }

    /**
     * Tells the service which packages to scan for script runners. By default, this service will always scan the
     * package <b>org.nrg.automation.runners</b>.
     * @param packages    The packages to be scanned for script runners.
     */
    public void setRunnerPackages(final List<String> packages) {
        _packages = packages;
    }

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
    @Override
    public Script getScript(final String scriptId) {
        return _scriptService.getByScriptId(scriptId);
    }

    /**
     * Gets the script for the specified script ID that is also associated (via {@link ScriptTrigger trigger}) with the
     * indicated scope, entity ID, and event. If a script doesn't exist with that script ID and trigger association,
     * this method returns null. Note that this method does no checking of the scope, associated entity, or event, but
     * just returns the script. You can get @{link Script scripts} for particular scopes or events by calling {@link
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScript(Scope, String, String)}.
     *
     * @param scriptId The ID of the script to locate.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     *
     * @return The {@link Script} object if a script with the indicated script ID and association is found, <b>null</b>
     * otherwise.
     */
    @Override
    @Transactional
    public Script getScript(final String scriptId, final Scope scope, final String entityId, final String event) {
        final ScriptTrigger trigger = _triggerService.getByScopeEntityAndEvent(scope, entityId, event);
        if (trigger == null) {
            return null;
        }
        return _scriptService.getByScriptId(scriptId);
    }

    /**
     * Deletes the script for the specified script ID. If a script doesn't exist with that script ID, this method throws
     * an {@link NrgServiceException}.
     *
     * @param scriptId The ID of the script to delete.
     *
     * @throws NrgServiceException When a script with the indicated script ID can not be found.
     */
    @Override
    @Transactional
    public void deleteScript(final String scriptId) throws NrgServiceException {
        final Script script = _scriptService.getByScriptId(scriptId);
        if (script == null) {
            throw new NrgServiceException(NrgServiceError.UnknownEntity, "Can't find script with script ID: " + scriptId);
        }
        final List<ScriptTrigger> triggers = _triggerService.getByScriptId(scriptId);
        for (final ScriptTrigger trigger : triggers) {
            _triggerService.delete(trigger);
        }
        _scriptService.delete(script);
    }

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
    @Override
    public List<Script> getScripts(final Scope scope, final String entityId) {
        final List<ScriptTrigger> triggers = _triggerService.getByScope(scope, entityId);
        if (triggers == null || triggers.size() == 0) {
            return new ArrayList<>();
        }
        final List<Script> scripts = new ArrayList<>(triggers.size());
        for (final ScriptTrigger trigger : triggers) {
            scripts.add(_scriptService.getByScriptId(trigger.getScriptId()));
        }
        return scripts;
    }

    /**
     * Gets the script for the specified scope, entity, script ID, and event. If a script and associated trigger doesn't
     * exist for those criteria, this method returns null. For attributes you don't want to specify, pass null.
     *
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     *
     * @return The associated {@link Script scripts} if any with the indicated associations is found, <b>null</b>
     * otherwise.
     */
    @Override
    public Script getScript(final Scope scope, final String entityId, final String event) {
        final ScriptTrigger trigger = _triggerService.getByScopeEntityAndEvent(scope, entityId, event);
        if (trigger == null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found no script triggers associated with scope {}, entity ID {}, and event {}.", scope, entityId, event);
            }
            return null;
        }
        final Script script = _scriptService.getByScriptId(trigger.getScriptId());
        if (_log.isDebugEnabled()) {
            if (script == null) {
                _log.debug("Found no script associated with scope {}, entity ID {}, and event {}.", scope, entityId, event);
            } else {
                _log.debug("Found script {} associated with scope {}, entity ID {}, and event {}.", script.getScriptId(), scope, entityId, event);
            }
        }
        return script;
    }

    /**
     * Gets all scripts registered on the system.
     *
     * @return All scripts on the system.
     */
    @Override
    public List<Script> getScripts() {
        return _scriptService.getAll();
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the scope, event, language, and language version arguments to default values. This is useful for creating a
     * site-wide script that can be run on demand.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     */
    @Override
    public void setScript(final String scriptId, final String content) {
        setScriptImpl(scriptId, content, null, Scope.Site, null, ScriptTrigger.DEFAULT_EVENT, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the scope, event, language, and language version arguments to default values. This is useful for creating a
     * site-wide script that can be run on demand.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final String description) {
        setScriptImpl(scriptId, content, description, Scope.Site, null, ScriptTrigger.DEFAULT_EVENT, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the event, language, and language version arguments to default values.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final Scope scope, final String entityId) {
        setScriptImpl(scriptId, content, null, scope, entityId, ScriptTrigger.DEFAULT_EVENT, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the language and language version arguments to default values.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final Scope scope, final String entityId, final String event) {
        setScriptImpl(scriptId, content, null, scope, entityId, event, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the description to the default value.
     *
     * @param scriptId The ID of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     * @param language The script language for this script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final Scope scope, final String entityId, final String event, final String language) {
        setScriptImpl(scriptId, content, null, scope, entityId, event, language);
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the event, language, and language version arguments to default values.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId) {
        setScriptImpl(scriptId, content, description, scope, entityId, ScriptTrigger.DEFAULT_EVENT, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScript(String, String, String, Scope, String, String, String)} that
     * sets the language and language version arguments to default values.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param event       The event for the script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String event) {
        setScriptImpl(scriptId, content, description, scope, entityId, event, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * Creates a script and trigger with the indicated attributes and saves them to the script repository. If objects
     * with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param scriptId    The ID of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param event       The event for the script.
     * @param language    The script language for this script.
     */
    @Override
    public void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String event, final String language) {
        setScriptImpl(scriptId, content, description, scope, entityId, event, language);
    }

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script   The script object to set.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param event    The event for the script.
     */
    @Override
    public void setScript(final Script script, final Scope scope, final String entityId, final String event) {
        final String resolved = StringUtils.isBlank(event) ? ScriptTrigger.DEFAULT_EVENT : event;
        String triggerName = _triggerService.getDefaultTriggerName(script.getScriptId(), scope, entityId, resolved);
        String triggerDescription = getDefaultTriggerDescription(script.getScriptId(), scope, entityId, resolved);
        final ScriptTrigger trigger = _triggerService.create(triggerName, triggerDescription, script.getScriptId(), Scope.encode(scope, entityId), resolved);
        setScript(script, trigger);
    }

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script  The script object to set.
     * @param trigger The script trigger to set.
     */
    @Override
    public void setScript(final Script script, final ScriptTrigger trigger) {
        saveScript(script);
        saveTrigger(trigger);
    }

    /**
     * A convenience method that sets script and trigger property values from corresponding entries in the submitted
     * properties object.
     *
     * @param scriptId   The ID of the script to set.
     * @param properties The properties to set on the script.
     */
    @Override
    public void setScript(final String scriptId, final Properties properties) throws NrgServiceException {
        if (StringUtils.isBlank(scriptId)) {
            throw new NrgServiceException(NrgServiceError.Unknown, "You must specify the script ID to use this method.");
        }
        final String content = properties.getProperty("content");
        final String description = properties.getProperty("description");
        final Scope scope = properties.containsKey("scope") ? Scope.getScope(properties.getProperty("scope")) : null;
        final String entityId = properties.getProperty("entityId");
        final String event = properties.getProperty("event", ScriptTrigger.DEFAULT_EVENT);
        final String language = properties.getProperty("language", ScriptRunner.DEFAULT_LANGUAGE);
        setScript(scriptId, content, description, scope, entityId, event, language);
    }

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
    @Override
    public ScriptOutput runScript(final Script script) throws NrgServiceException {
        return runScript(script, null, new HashMap<String, Object>());
    }

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
    @Override
    public ScriptOutput runScript(final Script script, final Map<String, Object> parameters) throws NrgServiceException {
        return runScript(script, null, parameters);
    }

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
    @Override
    public ScriptOutput runScript(final Script script, final ScriptTrigger trigger) throws NrgServiceException {
        return runScript(script, trigger, null);
    }

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
    @Override
    public ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters) throws NrgServiceException {
        if (!hasRunner(script.getLanguage())) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownScriptRunner, "There is no script runner that supports " + script.getLanguage() + ".");
        }

        final ScriptRunner runner = getRunner(script.getLanguage());

        final Properties properties = script.toProperties();
        for (final String key : properties.stringPropertyNames()) {
            // Don't override properties that are passed in explicitly.
            if (!parameters.containsKey(key)) {
                parameters.put(key, properties.getProperty(key));
            }
        }
        if (trigger != null) {
            final Map<String, String> items = Scope.decode(trigger.getAssociation());
            if (!parameters.containsKey("scope")) {
                parameters.put("scope", items.get("scope"));
            }
            if (!parameters.containsKey("entityId")) {
                parameters.put("entityId", items.get("entityId"));
            }
            if (!parameters.containsKey("event")) {
                parameters.put("event", trigger.getEvent());
            }
        }
        try {
            final ScriptOutput results = runner.run(parameters);
            if (_log.isDebugEnabled()) {
                _log.debug("Got the following results from running " + formatScriptAndParameters(script, parameters));
                if (results.getResults() == null) {
                    _log.debug(" * Null results");
                } else {
                    _log.debug(" * Object type: " + results.getResults().getClass());
                    final String renderedResults = results.toString();
                    _log.debug(" * Results: " + (renderedResults.length() > 64 ? renderedResults.substring(0, 63) + "..." : renderedResults));
                }
            }
            return results;
        } catch (Throwable e) {
            String message = "Found an error while running a " + script.getLanguage() + " script";
            _log.error(message, e);
            throw new NrgServiceException(message, e);
        }
    }

    /**
     * Set the system's {@link ScriptRunner script runners} to the submitted collection.
     *
     * @param runners The {@link ScriptRunner script runners} to be added to the system.
     */
    @Override
    public void setRunners(final Collection<Class<? extends ScriptRunner>> runners) {
        _runners.clear();
        for (final Class<? extends ScriptRunner> runner : runners) {
            // Sometimes a runner can't find its engine (missing dependency, etc.), so we need to throw it out.
            final ScriptRunner instance = createScriptRunnerInstance(runner);
            if (instance.getEngine() != null) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Adding runner for {} using engine {} version {}", instance.getLanguage(), instance.getEngine().get(ScriptEngine.ENGINE), instance.getEngine().get(ScriptEngine.ENGINE_VERSION));
                }
                addRunner(runner);
            } else if (_log.isInfoEnabled()) {
                _log.debug("Not adding the runner for {}, no engine found", instance.getLanguage());
            }
        }
    }

    @Override
    public boolean hasRunner(final String language) {
        return _runners.containsKey(language.toLowerCase());
    }

    @Override
    public List<String> getRunners() {
        return new ArrayList<>(_runners.keySet());
    }

    @Override
    public ScriptRunner getRunner(final String language) {
        if (!_runners.containsKey(language.toLowerCase())) {
            return null;
        }
        final Class<? extends ScriptRunner> clazz = _runners.get(language.toLowerCase());
        return createScriptRunnerInstance(clazz);
    }

    /**
     * Adds the submitted {@link ScriptRunner script runner} to the system.
     *
     * @param runner The {@link ScriptRunner script runner} to be added to the system.
     */
    @Override
    public void addRunner(final Class<? extends ScriptRunner> runner) {
        final String language = getLanguage(runner);
        _runners.put(language.toLowerCase(), runner);
    }

    /**
     * Adds the submitted {@link ScriptRunner script runners} to the system.
     *
     * @param runners The {@link ScriptRunner script runners} to be added to the system.
     */
    @Override
    public void addRunners(final Collection<Class<? extends ScriptRunner>> runners) {
        for (final Class<? extends ScriptRunner> runner : runners) {
            addRunner(runner);
        }
    }

    @NotNull
    private ScriptRunner createScriptRunnerInstance(final Class<? extends ScriptRunner> clazz) {
        try {
            final Constructor<? extends ScriptRunner> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Instantiation, "The runner class for " + getLanguage(clazz) + " has no default constructor.");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred instantiating the runner class for " + getLanguage(clazz) + " scripts.", e);
        }
    }

    private String getLanguage(final Class<? extends ScriptRunner> runner) {
        return runner.getAnnotation(Supports.class).value();
    }

    private void setScriptImpl(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String event, final String language) {
        if (StringUtils.isBlank(scriptId)) {
            throw new NrgServiceRuntimeException(NrgServiceError.InvalidScript, "You can not save a script with an empty script ID!");
        }
        if (StringUtils.isBlank(content)) {
            throw new NrgServiceRuntimeException(NrgServiceError.InvalidScript, "You can not save the script " + scriptId + " with an empty script ID!");
        }
        final Script script;
        if (_scriptService.hasScript(scriptId)) {
            script = _scriptService.getByScriptId(scriptId);
        } else {
            script = new Script();
            script.setScriptId(scriptId);
        }

        script.setDescription(StringUtils.isNotBlank(description) ? description : getDefaultScriptDescription(script));
        script.setLanguage(StringUtils.isNotBlank(language) ? language : ScriptRunner.DEFAULT_LANGUAGE);
        script.setContent(content);

        if (scope == null) {
            saveScript(script);
        } else {
            final String resolvedEvent = StringUtils.isNotBlank(event) ? event : ScriptTrigger.DEFAULT_EVENT;
            ScriptTrigger trigger = _triggerService.getByScopeEntityAndEvent(scope, entityId, resolvedEvent);
            if (trigger == null) {
                trigger = new ScriptTrigger();
                trigger.setTriggerId(_triggerService.getDefaultTriggerName(scriptId, scope, entityId, resolvedEvent));
            }
            trigger.setDescription(getDefaultTriggerDescription(scriptId, scope, entityId, resolvedEvent));
            trigger.setScriptId(scriptId);
            trigger.setAssociation(Scope.encode(scope, entityId));
            trigger.setEvent(getEvent(resolvedEvent));

            setScript(script, trigger);
        }
    }

    private Event getEvent(final String eventId) {
        return _eventService.hasEvent(eventId) ? _eventService.getByEventId(eventId) : _eventService.create(eventId, eventId);
    }

    private void saveScript(final Script script) {
        final Script existingScript = _scriptService.getByScriptId(script.getScriptId());
        if (existingScript == null) {
            _scriptService.create(script);
        } else {
            boolean isDirty = false;
            final String existingContent = existingScript.getContent();
            final String content = script.getContent();
            if (!StringUtils.equals(existingContent, content)) {
                existingScript.setContent(content);
                isDirty = true;
            }
            final String existingLanguage = existingScript.getLanguage();
            final String language = script.getLanguage();
            if (!StringUtils.equals(existingLanguage, language)) {
                existingScript.setLanguage(language);
                isDirty = true;
            }
            final String existingDescription = existingScript.getDescription();
            final String description = script.getDescription();
            if (!StringUtils.equals(existingDescription, description)) {
                existingScript.setDescription(description);
                isDirty = true;
            }
            if (isDirty) {
                _scriptService.update(existingScript);
            }
        }
    }

    private void saveTrigger(final ScriptTrigger trigger) {
        final ScriptTrigger existingTrigger = _triggerService.getByTriggerId(trigger.getTriggerId());
        if (existingTrigger == null) {
            _triggerService.create(trigger);
        } else {
            boolean isDirty = false;
            String existingDescription = existingTrigger.getDescription();
            String description = trigger.getDescription();
            if (!existingDescription.equals(description)) {
                existingTrigger.setDescription(description);
                isDirty = true;
            }
            if (isDirty) {
                _triggerService.update(existingTrigger);
            }
        }
    }

    private String formatScriptAndParameters(final Script script, final Map<String, Object> parameters) {
        final StringBuilder buffer = new StringBuilder("Script ID: [").append(script.getScriptId()).append("]");
        if (parameters != null) {
            buffer.append("Parameters:\n");
            for (final String key : parameters.keySet()) {
                buffer.append(" * ").append(key).append(": ").append(parameters.get(key).toString()).append("\n");
            }
        }
        return buffer.toString();
    }

    private String getDefaultScriptDescription(final Script script) {
        return "Default description: script ID " + script.getScriptId() + " configured to run with " + script.getLanguage();
    }

    private String getDefaultTriggerDescription(final String scriptId, final Scope scope, final String entityId, final String event) {
        return "Script trigger for script " + scriptId + ", scope " + scope.code() + (entityId != null ? ", entity ID: " + entityId : "") + ", event: " + event;
    }

    private static final Logger _log = LoggerFactory.getLogger(DefaultScriptRunnerService.class);

    @Inject
    private EventService _eventService;

    @Inject
    private ScriptService _scriptService;

    @Inject
    private ScriptTriggerService _triggerService;

    private final Map<String, Class<? extends ScriptRunner>> _runners = new HashMap<>();
    private List<String> _packages;
}
