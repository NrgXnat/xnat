/*
 * org.nrg.automation.services.impl.DefaultScriptRunnerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.automation.annotations.Supports;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

/**
 * The Class DefaultScriptRunnerService.
 */
@Service
public class DefaultScriptRunnerService implements ScriptRunnerService, InitializingBean {
    @Autowired
    public DefaultScriptRunnerService(final ScriptService scriptService, final ScriptTriggerService triggerService) {
        _scriptService = scriptService;
        _triggerService = triggerService;
    }

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
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScript(Scope, String, String,
     * String, Map)}.
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
     * This method should no longer be used now that we do not have the versions of scripts saved as separate scripts.
     *
     * @param scriptId the script id
     * @return the scripts
     */
    @Override
    public List<Script> getScripts(final String scriptId) {
        List<Script> scriptsList = new ArrayList<>();
        scriptsList.add(_scriptService.getByScriptId(scriptId));
        return scriptsList;
    }

    /**
     * Gets the script for the specified script ID that is also associated (via {@link ScriptTrigger trigger}) with the
     * indicated scope, entity ID, and event. If a script doesn't exist with that script ID and trigger association,
     * this method returns null. Note that this method does no checking of the scope, associated entity, or event, but
     * just returns the script. You can get @{link Script scripts} for particular scopes or events by calling {@link
     * ScriptRunnerService#getScripts(Scope, String)} or {@link ScriptRunnerService#getScript(Scope, String, String,
     * String, Map)}.
     *
     * @param scriptId     The ID of the script to locate.
     * @param scope        The scope for the script.
     * @param entityId     The associated entity for the script.
     * @param eventClass the event class
     * @param event        The event for the script.
     * @param filterMap the filter map
     * @return The {@link Script} object if a script with the indicated script ID and association is found, <b>null</b>
     * otherwise.
     */
    @Override
    @Transactional
    public Script getScript(final String scriptId, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap) {
        final List<ScriptTrigger> triggers = _triggerService.getByScopeEntityAndEvent(scope, entityId, eventClass, event);
        ScriptTrigger trigger = null;
        if (triggers != null) {
        	outerloop:
        	for (final ScriptTrigger currTrigger : triggers) {
        		final Map<String, List<String>> eventFiltersMap = currTrigger.getEventFiltersAsMap();
        		for (final Entry<String, List<String>> entry : eventFiltersMap.entrySet()) {
        			final String key = entry.getKey();
        			final List<String> value = entry.getValue();
        			if (value == null || value.isEmpty()) {
        				continue;
        			}
        			if (!filterMap.containsKey(key) || !value.contains(filterMap.get(key))) {
        				continue outerloop;
        			}
        		}
        		trigger = currTrigger;
        	}
        }
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
        if (triggers != null) {
        	for (final ScriptTrigger trigger : triggers) {
        		_triggerService.delete(trigger);
        	}
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
     * @param eventClass the event class
     * @param event    The event for the script.
     * @param filterMap the filter map
     * @return The associated {@link Script scripts} if any with the indicated associations is found, <b>null</b>
     * otherwise.
     */
    @Override
    public Script getScript(final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap) {
        final List<ScriptTrigger> triggers = _triggerService.getByScopeEntityAndEvent(scope, entityId, eventClass, event);
        ScriptTrigger trigger = null;
        if (triggers != null) {
        	outerloop:
        	for (final ScriptTrigger currTrigger : triggers) {
        		final Map<String, List<String>> eventFiltersMap = currTrigger.getEventFiltersAsMap();
        		for (final Entry<String, List<String>> entry : eventFiltersMap.entrySet()) {
        			final String key = entry.getKey();
        			final List<String> value = entry.getValue();
        			if (value == null || value.isEmpty()) {
        				continue;
        			}
        			if (!filterMap.containsKey(key) || !value.contains(filterMap.get(key))) {
        				continue outerloop;
        			}
  				}
        		trigger = currTrigger;
        	}
        }
        if (trigger == null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found no script triggers associated with scope {}, entity ID {}, event class {}, event {} and filters {}.", scope, entityId, eventClass, event, filterMap);
            }
            return null;
        }
        final Script script = _scriptService.getByScriptId(trigger.getScriptId());
        if (_log.isDebugEnabled()) {
            if (script == null) {
                _log.debug("Found no script associated with scope {}, entity ID {}, event class {}, event {}, and filters {}.", scope, entityId, eventClass, event, filterMap);
            } else {
                _log.debug("Found script {} associated with scope {}, entity ID {}, event class {}, event {}, and filters {}.", script.getScriptId(), scope, entityId, eventClass, event, filterMap);
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
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the scope, event, language, and language version arguments to default values. This is useful
     * for creating a site-wide script that can be run on demand.
     *
     * @param scriptId The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content  The content to set for the script.
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content) {
        setScriptImpl(scriptId, scriptLabel, content, null, Scope.Site, null, ScriptTrigger.DEFAULT_CLASS, ScriptTrigger.DEFAULT_EVENT, ScriptTrigger.DEFAULT_FILTERS, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the scope, event, language, and language version arguments to default values. This is useful
     * for creating a site-wide script that can be run on demand.
     *
     * @param scriptId    The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final String description) {
        setScriptImpl(scriptId, scriptLabel, content, description, Scope.Site, null, ScriptTrigger.DEFAULT_CLASS, ScriptTrigger.DEFAULT_EVENT, ScriptTrigger.DEFAULT_FILTERS, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the event, language, and language version arguments to default values.
     *
     * @param scriptId The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final Scope scope, final String entityId) {
        setScriptImpl(scriptId, scriptLabel, content, null, scope, entityId, ScriptTrigger.DEFAULT_CLASS, ScriptTrigger.DEFAULT_EVENT, ScriptTrigger.DEFAULT_FILTERS, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the language and language version arguments to default values.
     *
     * @param scriptId The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param eventClass the event class
     * @param event    The event for the script.
     * @param eventFilters the event filters
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters) {
        setScriptImpl(scriptId, scriptLabel, content, null, scope, entityId, eventClass, event, eventFilters, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the description to the default value.
     *
     * @param scriptId The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content  The content to set for the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param eventClass the event class
     * @param event    The event for the script.
     * @param eventFilters the event filters
     * @param language The script language for this script.
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language) {
        setScriptImpl(scriptId, scriptLabel, content, null, scope, entityId, eventClass, event, eventFilters, language);
    }

    /**
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the event, language, and language version arguments to default values.
     *
     * @param scriptId    The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId) {
        setScriptImpl(scriptId, scriptLabel, content, description, scope, entityId, ScriptTrigger.DEFAULT_CLASS, ScriptTrigger.DEFAULT_EVENT, ScriptTrigger.DEFAULT_FILTERS, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * A pared down version of {@link #setScriptImpl(String, String, String, String, Scope, String, String, String, Map,
     * String)} that sets the language and language version arguments to default values.
     *
     * @param scriptId    The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param eventClass the event class
     * @param event       The event for the script.
     * @param eventFilters the event filters
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters) {
        setScriptImpl(scriptId, scriptLabel, content, description, scope, entityId, eventClass, event, eventFilters, ScriptRunner.DEFAULT_LANGUAGE);
    }

    /**
     * Creates a script and trigger with the indicated attributes and saves them to the script repository. If objects
     * with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param scriptId    The ID of the script to set.
     * @param scriptLabel The Label of the script to set.
     * @param content     The content to set for the script.
     * @param description The description of the script.
     * @param scope       The scope for the script.
     * @param entityId    The associated entity for the script.
     * @param eventClass the event class
     * @param event       The event for the script.
     * @param eventFilters the event filters
     * @param language    The script language for this script.
     */
    @Override
    public void setScript(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language) {
        setScriptImpl(scriptId, scriptLabel, content, description, scope, entityId, eventClass, event, eventFilters, language);
    }

    /**
     * Takes the submitted script object and creates a trigger for it with the indicated scope, entity ID, and event. If
     * objects with the same unique constraints already exist, they will be retrieved then updated.
     *
     * @param script   The script object to set.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param eventClass the event class
     * @param event    The event for the script.
     * @param eventFilters the event filters
     */
    @Override
    public void setScript(final Script script, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>>eventFilters) {
        final String resolved = StringUtils.isBlank(event) ? ScriptTrigger.DEFAULT_EVENT : event;
        final String triggerName = _triggerService.getDefaultTriggerName(script.getScriptId(), scope, entityId, eventClass, resolved, eventFilters);
        final String triggerDescription = getDefaultTriggerDescription(script.getScriptId(), scope, entityId, resolved);
        final ScriptTrigger trigger = _triggerService.create(triggerName, triggerDescription, script.getScriptId(), Scope.encode(scope, entityId), eventClass, resolved, eventFilters);
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
     * @throws NrgServiceException the nrg service exception
     */
    @Override
    public void setScript(final String scriptId, final Properties properties) throws NrgServiceException {
    	// TODO:  THIS METHOD MAY NEED WORK.  The passing of ScriptTrigger.DEFAULT_FILTERS COULD be problematic.
        if (StringUtils.isBlank(scriptId)) {
            throw new NrgServiceException(NrgServiceError.Unknown, "You must specify the script ID to use this method.");
        }
        final String scriptLabel = properties.getProperty("scriptLabel");
        final String content = properties.getProperty("content");
        final String description = properties.getProperty("description");
        //final String scriptVersion = properties.getProperty("scriptVersion");
        final Scope scope = properties.containsKey("scope") ? Scope.getScope(properties.getProperty("scope")) : null;
        final String entityId = properties.getProperty("entityId");
        final String event = properties.getProperty("event", ScriptTrigger.DEFAULT_EVENT);
        final String eventClass = properties.getProperty("srcEventClass", ScriptTrigger.DEFAULT_CLASS);
        final String language = properties.getProperty("language", ScriptRunner.DEFAULT_LANGUAGE);
        setScript(scriptId, scriptLabel, content, description, scope, entityId, eventClass, event, ScriptTrigger.DEFAULT_FILTERS, language);
    }
    
    /**
     * This attempts to run the submitted script. Note that this method does no checking of the scope, associated
     * entity, or event, but just executes the script. You can get @{link Script scripts} for particular scopes by
     * calling the {@link #getScripts()}, {@link ScriptRunnerService#getScripts(Scope, String)}, or {@link
     * #getScript(Scope, String, String, String, Map)} methods.
     *
     * @param script The script to run.
     * @return The results of the script execution.
     * @throws NrgServiceException the nrg service exception
     */
    @Override
    public ScriptOutput runScript(final Script script) throws NrgServiceException {
        return runScript(script, null, new HashMap<String, Object>(), true);
    }

    /**
     * This attempts to run the submitted script, passing in the <b>parameters</b> map as parameters to the script. Note
     * that this method does no checking of the scope, associated entity, or event, but just executes the script. You
     * can get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScript(Scope, String, String, String, Map)} methods.
     *
     * @param script     The script to run.
     * @param parameters The parameters to pass to the script.
     * @return The results of the script execution.
     * @throws NrgServiceException the nrg service exception
     */
    @Override
    public ScriptOutput runScript(final Script script, final Map<String, Object> parameters) throws NrgServiceException {
        return runScript(script, null, parameters, true);
    }

    /**
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, into the script execution environment. You can get @{link Script scripts} for
     * particular scopes by calling the {@link #getScripts()}, {@link ScriptRunnerService#getScripts(Scope, String)}, or
     * {@link #getScript(Scope, String, String, String, Map)} methods.
     *
     * @param script  The script to run.
     * @param trigger The associated trigger for the script execution.
     * @return The results of the script execution.
     * @throws NrgServiceException the nrg service exception
     */
    @Override
    public ScriptOutput runScript(final Script script, final ScriptTrigger trigger) throws NrgServiceException {
        return runScript(script, trigger, null, true);
    }

    /**
     * This attempts to run the submitted script. This passes the details about the associated scope and event, derived
     * from the trigger parameter, as well as the submitted parameters, into the script execution environment. You can
     * get @{link Script scripts} for particular scopes by calling the {@link #getScripts()}, {@link
     * ScriptRunnerService#getScripts(Scope, String)}, or {@link #getScript(Scope, String, String, String, Map)} methods.
     *
     * @param script     The script to run.
     * @param trigger    The associated trigger for the script execution.
     * @param parameters The parameters to pass to the script.
     * @return The results of the script execution.
     * @throws NrgServiceException the nrg service exception
     */
    @Override
    public ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters) throws NrgServiceException {
        return runScript(script, trigger, parameters, true);
    	
    }
    
    /**
     * Run script.
     *
     * @param script the script
     * @param trigger the trigger
     * @param parameters the parameters
     * @param exceptionOnError the exception on error
     * @return the script output
     * @throws NrgServiceException the nrg service exception
     */
    @Override
    public ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters, boolean exceptionOnError) throws NrgServiceException {
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
            final ScriptOutput results = runner.run(parameters, exceptionOnError);
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

    /**
     * Checks for runner.
     *
     * @param language the language
     * @return true, if successful
     */
    @Override
    public boolean hasRunner(final String language) {
        return _runners.containsKey(language.toLowerCase());
    }

    /**
     * Gets the runners.
     *
     * @return the runners
     */
    @Override
    public List<String> getRunners() {
        return new ArrayList<>(_runners.keySet());
    }

    /**
     * Gets the runner.
     *
     * @param language the language
     * @return the runner
     */
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

    /**
     * Creates the script runner instance.
     *
     * @param clazz the clazz
     * @return the script runner
     */
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

    /**
     * Gets the language.
     *
     * @param runner the runner
     * @return the language
     */
    private String getLanguage(final Class<? extends ScriptRunner> runner) {
        return runner.getAnnotation(Supports.class).value();
    }

    /**
     * Sets the script impl.
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
    private void setScriptImpl(final String scriptId, final String scriptLabel, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language) {
        if (StringUtils.isBlank(scriptId)) {
            throw new NrgServiceRuntimeException(NrgServiceError.InvalidScript, "You can not save a script with an empty script ID!");
        }
        if (StringUtils.isBlank(scriptLabel)) {
            throw new NrgServiceRuntimeException(NrgServiceError.InvalidScript, "You can not save the script " + scriptId + " with an empty script label!");
        }
        if (StringUtils.isBlank(content)) {
            throw new NrgServiceRuntimeException(NrgServiceError.InvalidScript, "You can not save the script " + scriptId + " with an empty script content!");
        }
        final Script script = new Script();
        script.setScriptId(scriptId);
        script.setScriptLabel(scriptLabel);
        //script.setScriptVersion(StringUtils.isNotBlank(scriptVersion) ? scriptVersion : "1");
        script.setDescription(StringUtils.isNotBlank(description) ? description : getDefaultScriptDescription(script));
        script.setLanguage(StringUtils.isNotBlank(language) ? language : ScriptRunner.DEFAULT_LANGUAGE);
        script.setContent(content);
        if (scope == null) {
            saveScript(script);
        } else {
            final String resolvedEvent = StringUtils.isNotBlank(event) ? event : ScriptTrigger.DEFAULT_EVENT;
            final List<ScriptTrigger> triggers = _triggerService.getByScopeEntityAndEvent(scope, entityId, eventClass, resolvedEvent);
            ScriptTrigger trigger = null;
            if (triggers != null) {
            	for (final ScriptTrigger thisTrigger : triggers) {
            		if (eventFilters.equals(thisTrigger.getEventFilters())) {
            			trigger = thisTrigger;
            	    }
            	}
            }
            if (trigger == null) {
                trigger = new ScriptTrigger();
                trigger.setTriggerId(_triggerService.getDefaultTriggerName(scriptId, scope, entityId, eventClass, resolvedEvent, eventFilters));
            }
            trigger.setDescription(getDefaultTriggerDescription(scriptId, scope, entityId, resolvedEvent));
            trigger.setScriptId(scriptId);
            trigger.setAssociation(Scope.encode(scope, entityId));
            trigger.setEvent(resolvedEvent);

            setScript(script, trigger);
        }
    }

    /**
     * Save script.
     *
     * @param script the script
     */
    private void saveScript(final Script script) {
        final Script existingScript = _scriptService.getByScriptId(script.getScriptId());
        if (existingScript == null) {
            _scriptService.create(script);
        } else {
            boolean isDirty = false;
            final String existingScriptLabel = existingScript.getScriptLabel();
            final String scriptLabel = script.getScriptLabel();
            if (!StringUtils.equals(existingScriptLabel, scriptLabel)) {
                existingScript.setScriptLabel(scriptLabel);
                isDirty = true;
            }
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

    /**
     * Save trigger.
     *
     * @param trigger the trigger
     */
    private void saveTrigger(final ScriptTrigger trigger) {
        final ScriptTrigger existingTrigger = _triggerService.getByTriggerId(trigger.getTriggerId());
        if (existingTrigger == null) {
            _triggerService.create(trigger);
        } else {
            boolean isDirty = false;
            final String existingDescription = existingTrigger.getDescription();
            final String description = trigger.getDescription();
            if (!existingDescription.equals(description)) {
                existingTrigger.setDescription(description);
                isDirty = true;
            }
            if (isDirty) {
                _triggerService.update(existingTrigger);
            }
        }
    }

    /**
     * Format script and parameters.
     *
     * @param script the script
     * @param parameters the parameters
     * @return the string
     */
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

    /**
     * Gets the default script description.
     *
     * @param script the script
     * @return the default script description
     */
    private String getDefaultScriptDescription(final Script script) {
        return "Default description: script ID " + script.getScriptId() + " configured to run with " + script.getLanguage();
    }

    /**
     * Gets the default trigger description.
     *
     * @param scriptId the script id
     * @param scope the scope
     * @param entityId the entity id
     * @param event the event
     * @return the default trigger description
     */
    private String getDefaultTriggerDescription(final String scriptId, final Scope scope, final String entityId, final String event) {
        return "Script trigger for script " + scriptId + ", scope " + scope.code() + (entityId != null ? ", entity ID: " + entityId : "") + ", event: " + event;
    }

    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(DefaultScriptRunnerService.class);

    /** The _script service. */
    private final ScriptService _scriptService;

    /** The _trigger service. */
    private final ScriptTriggerService _triggerService;

    /** The _runners. */
    private final Map<String, Class<? extends ScriptRunner>> _runners = new HashMap<>();
    
    /** The _packages. */
    private List<String> _packages;
}
