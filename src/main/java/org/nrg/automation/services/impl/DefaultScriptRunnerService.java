package org.nrg.automation.services.impl;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.nrg.automation.runners.ScriptRunner;
import org.nrg.automation.services.ScriptProperty;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class DefaultScriptRunnerService implements ScriptRunnerService {

    public static final String TOOL_ID_SCRIPTS = "scripts";

    @Override
    public Properties getSiteScript(final String scriptId) {
        return getScopedScript(Scope.Site, null, scriptId, null);
    }

    @Override
    public Properties getSiteScript(final String scriptId, final String path) {
        return getScopedScript(Scope.Site, null, scriptId, path);
    }

    @Override
    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId) {
        return getScopedScript(scope, entityId, scriptId, null);
    }

    @Override
    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId, final String path) {
        validateScopeAndEntityId(scope, entityId);
        final Long parsed = StringUtils.isBlank(entityId) ? null : Long.valueOf(entityId);
        final Configuration configuration = _configService.getConfig(TOOL_ID_SCRIPTS, composite(scriptId, path), parsed);
        if (configuration == null) {
            throw new NrgServiceRuntimeException("Didn't find a script matching the identifier set: " + formatScriptIdSet(scope, entityId, scriptId, path));
        }
        Properties configProps = configuration.asProperties();
        Properties properties = new Properties();
        properties.setProperty(ScriptProperty.Scope.key(), scope.code());
        if (!StringUtils.isBlank(entityId)) {
            properties.setProperty(ScriptProperty.EntityId.key(), entityId);
        }
        properties.setProperty(ScriptProperty.ScriptId.key(), scriptId);
        if (!StringUtils.isBlank(path)) {
            properties.setProperty(ScriptProperty.Path.key(), path);
        }
        if (configProps.containsKey("xnatUser")) {
            properties.setProperty("xnatUser", configProps.getProperty("xnatUser"));
        }
        try {
            final Properties configDataProps = MAPPER.readValue(configProps.getProperty("contents"), Properties.class);
            properties.setProperty(ScriptProperty.Script.key(), configDataProps.getProperty(ScriptProperty.Script.key()));
            properties.setProperty(ScriptProperty.Language.key(), configDataProps.getProperty(ScriptProperty.Language.key()));
            properties.setProperty(ScriptProperty.LanguageVersion.key(), configDataProps.getProperty(ScriptProperty.LanguageVersion.key()));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("There was a weird error unmarshalling the config data", e);
        }
        return properties;
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String script) throws ConfigServiceException {
        setSiteScript(user, scriptId, null, script, null);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String path, final String script) throws ConfigServiceException {
        setSiteScript(user, scriptId, path, script, null);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String script, final Properties properties) throws ConfigServiceException {
        setSiteScript(user, scriptId, null, script, properties);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String path, final String script, Properties properties) throws ConfigServiceException {
        setScopedScript(user, Scope.Site, null, scriptId, path, script, properties);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String script) throws ConfigServiceException {
        setScopedScript(user, scope, entityId, scriptId, null, script, null);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script) throws ConfigServiceException {
        setScopedScript(user, scope, entityId, scriptId, path, script, null);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String script, final Properties properties) throws ConfigServiceException {
        setScopedScript(user, scope, entityId, scriptId, null, script, properties);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script, Properties properties) throws ConfigServiceException {
        if (properties == null) {
            properties = new Properties();
        }
        for (final String property : ScriptProperty.keys()) {
            final ScriptProperty key = ScriptProperty.get(property);
            if (!properties.contains(key)) {
                final String value = key.defaultValue();
                if (!StringUtils.isBlank(value)) {
                    properties.setProperty(key.key(), value);
                }
            }
        }
        validateScopeAndEntityId(scope, entityId);
        final Long parsed = StringUtils.isBlank(entityId) ? null : Long.valueOf(entityId);
        _configService.replaceConfig(user, "Updating script", TOOL_ID_SCRIPTS, composite(scriptId, path), true, wrapScript(script, properties), parsed);
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId) {
        return runScriptImpl(user, Scope.Site, null, scriptId, null, getInitializedParameters());
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId, Map<String, Object> parameters) {
        return runScriptImpl(user, Scope.Site, null, scriptId, null, parameters);
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId, final String path) {
        return runScriptImpl(user, Scope.Site, null, scriptId, path, getInitializedParameters());
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId, final String path, Map<String, Object> parameters) {
        return runScriptImpl(user, Scope.Site, null, scriptId, path, parameters);
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId) {
        return runScriptImpl(user, scope, entityId, scriptId, null, getInitializedParameters());
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, Map<String, Object> parameters) {
        return runScriptImpl(user, scope, entityId, scriptId, null, parameters);
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path) {
        return runScriptImpl(user, scope, entityId, scriptId, path, getInitializedParameters());
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, Map<String, Object> parameters) {
        return runScriptImpl(user, scope, entityId, scriptId, path, parameters);
    }

    @Override
    @Autowired
    public void setRunners(final List<ScriptRunner> runners) {
        _runners.clear();
        addRunners(runners);
    }

    @Override
    public boolean hasRunner(final String language, final String version) {
        return _runners.containsKey(language) && _runners.get(language).containsKey(version);
    }

    @Override
    public ScriptRunner getRunner(final String language, final String version) {
        if (_runners.containsKey(language)) {
            return _runners.get(language).get(version);
        }
        return null;
    }

    @Override
    public void addRunner(final ScriptRunner runner) {
        final String language = runner.getLanguage();
        if (_runners.containsKey(language)) {
            _runners.get(language).put(runner.getLanguageVersion(), runner);
        } else {
            _runners.put(language, new HashMap<String, ScriptRunner>());
            _runners.get(language).put(runner.getLanguageVersion(), runner);
        }
    }

    @Override
    public void addRunners(final List<ScriptRunner> runners) {
        for (final ScriptRunner runner : runners) {
            addRunner(runner);
        }
    }

    private Object runScriptImpl(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final Map<String, Object> parameters) {
        Properties properties = getScopedScript(scope, entityId, scriptId, path);
        final String language = properties.getProperty(ScriptProperty.Language.key());
        final String version = properties.getProperty(ScriptProperty.LanguageVersion.key());

        final ScriptRunner runner;

        // TODO: Need to have a way to do a fuzzy match of versions, so that version 2.3.5 and 2.3.6 can match, e.g. 2.3 or whatever.
        if (hasRunner(language, version)) {
            runner = getRunner(language, version);
        } else {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownScriptRunner, "There is no script runner that supports " + language + " version " + version + ".");
        }

        for (final String key : properties.stringPropertyNames()) {
            // Don't override properties that are passed in explicitly.
            if (!parameters.containsKey(key)) {
                parameters.put(key, properties.getProperty(key));
            }
        }
        if (entityId != null) {
            parameters.put("scope", scope.code());
            parameters.put("entityId", entityId);
        }

        // TODO: Add audit/log trail entries here, something more robust than simple logging.
        if (_log.isInfoEnabled()) {
            _log.info("User " + user + " is preparing to run script " + formatScriptIdSet(scope, entityId, scriptId, path, parameters));
        }

        final Object results = runner.run(parameters);
        if (_log.isDebugEnabled()) {
            _log.debug("Got the following results from running " + formatScriptIdSet(scope, entityId, scriptId, path, parameters));
            if (results == null) {
                _log.debug(" * Null results");
            } else {
                _log.debug(" * Object type: " + results.getClass());
                final String renderedResults = results.toString();
                _log.debug(" * Results: " + (renderedResults.length() > 64 ? renderedResults.substring(0, 63) + "..." : renderedResults));
            }
        }
        return results;
    }

    /**
     * Provides an opportunity to initialize default parameters with some values.
     *
     * @return An initialized parameters map.
     */
    private Map<String, Object> getInitializedParameters() {
        return new HashMap<String, Object>();
    }

    private void validateScopeAndEntityId(final Scope scope, final String entityId) {
        final boolean blankEntityId = StringUtils.isBlank(entityId);
        if (scope == Scope.Site) {
            if (!blankEntityId) {
                throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "You can not specify an entity ID for the site scope.");
            }
            return;
        }
        if (blankEntityId) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "You must specify a valid value for the entity ID for scope " + scope.code() + ".");
        }
        if (scope == Scope.Project) {
            try {
                Long.parseLong(entityId);
            } catch (NumberFormatException ignored) {
                throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "You must specify a valid value for the entity ID for scope " + scope.code() + ". \"" + entityId + " is not a valid long value.");
            }
        } else {
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "This service currently only supports site and project scopes.");
        }
    }

    private String formatScriptIdSet(final Scope scope, final String entityId, final String scriptId, final String path) {
        return formatScriptIdSet(scope, entityId, scriptId, path, null);
    }

    private String formatScriptIdSet(final Scope scope, final String entityId, final String scriptId, final String path, final Map<String, Object> parameters) {
        final StringBuilder buffer = new StringBuilder("Scope[").append(scope.toString()).append("]");
        if (!StringUtils.isBlank(entityId)) {
            buffer.append(".EntityID[").append(entityId).append("]");
        }
        buffer.append(".ScriptID[").append(scriptId).append("]");
        if (!StringUtils.isBlank(path)) {
            buffer.append(".Path[").append(path).append("]");
        }
        for (final String key : parameters.keySet()) {
            buffer.append(" * ").append(key).append(": ").append(parameters.get(key).toString()).append("\n");
        }
        return buffer.toString();
    }

    /**
     * Right now this is a very simplistic compositing method to convert scriptId and path into a path of the sort used
     * by the NRG configuration service. This does allow these to be separated and managed differently later to assist
     * in creating a script and variation mechanism.
     *
     * @param scriptId The ID of the script family.
     * @param path     The path of the specific script to retrieve.
     * @return The composite script identifier.
     */
    private String composite(final String scriptId, final String path) {
        return StringUtils.isBlank(path) ? scriptId : scriptId + "/" + path;
    }

    private ObjectWriter getWriter() {
        return MAPPER.writer().withDefaultPrettyPrinter();
    }

    private String wrapScript(final String script, final Properties properties) {
        try {
            properties.setProperty(ScriptProperty.Script.key(), script);
            return getWriter().writeValueAsString(properties);
        } catch (IOException ignored) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An I/O exception happened while serializing a properties map.");
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DefaultScriptRunnerService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ConfigService _configService;

    private final Map<String, Map<String, ScriptRunner>> _runners = new HashMap<String, Map<String, ScriptRunner>>();
}
