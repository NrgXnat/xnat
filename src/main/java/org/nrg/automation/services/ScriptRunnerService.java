package org.nrg.automation.services;

import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.NrgService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("unused")
public interface ScriptRunnerService extends NrgService {

    Script getScript(final String scriptId);

    Script getScript(final String scriptId, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap);

    void deleteScript(final String scriptId) throws NrgServiceException;

    List<Script> getScripts(final Scope scope, final String entityId);

    Script getScript(final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap);

    List<Script> getScripts();

    List<Script> getScripts(final String scriptId);

    void setScript(final String scriptId, final String content);

    void setScript(final String scriptId, final String content, final String description);

    void setScript(final String scriptId, final String content, final Scope scope, final String entityId);

    void setScript(final String scriptId, final String content, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);

    void setScript(final String scriptId, final String content, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language);

    void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId);

    void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);

    void setScript(final String scriptId, final String content, final String description, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters, final String language);

    void setScript(final Script script, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);

    void setScript(final Script script, final ScriptTrigger trigger);
    
    void setScript(final String scriptId, final Properties properties) throws NrgServiceException;

    ScriptOutput runScript(final Script script) throws NrgServiceException;

    ScriptOutput runScript(final Script script, Map<String, Object> parameters) throws NrgServiceException;

    ScriptOutput runScript(final Script script, final ScriptTrigger trigger) throws NrgServiceException;

    ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters) throws NrgServiceException;

    ScriptOutput runScript(final Script script, final ScriptTrigger trigger, final Map<String, Object> parameters, boolean exceptionOnError) throws NrgServiceException;

    void setRunners(final Collection<Class<? extends ScriptRunner>> runners);

    boolean hasRunner(final String language);

    List<String> getRunners();

    ScriptRunner getRunner(final String language);

    void addRunner(final Class<? extends ScriptRunner> runner);

    @SuppressWarnings("unused")
    void addRunners(final Collection<Class<? extends ScriptRunner>> runners);
}
