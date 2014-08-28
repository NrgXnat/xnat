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
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class DefaultScriptRunnerService implements ScriptRunnerService {

        public static final String TOOL_ID_SCRIPTS = "scripts";

    @Override
    public boolean hasSiteScript(final String scriptId) {
        return hasScriptImpl(Scope.Site, null, false, scriptId, null);
    }

    @Override
    public boolean hasSiteScript(final String scriptId, final String path) {
        return hasScriptImpl(Scope.Site, null, false, scriptId, path);
    }

    @Override
    public boolean hasScopedScript(final Scope scope, final String entityId, final String scriptId) {
        return hasScriptImpl(scope, entityId, false, scriptId, null);
    }

    @Override
    public boolean hasScopedScript(final Scope scope, final String entityId, final String scriptId, final String path) {
        return hasScriptImpl(scope, entityId, false, scriptId, path);
    }

    @Override
    public boolean hasScript(final Scope scope, final String entityId, final String scriptId) {
        return hasScriptImpl(scope, entityId, true, scriptId, null);
    }

    @Override
    public boolean hasScript(final Scope scope, final String entityId, final String scriptId, final String path) {
        return hasScriptImpl(scope, entityId, true, scriptId, path);
    }

    @Override
    public Properties getSiteScript(final String scriptId) {
        return getScriptImpl(Scope.Site, null, false, scriptId, null);
    }

    @Override
    public Properties getSiteScript(final String scriptId, final String path) {
        return getScriptImpl(Scope.Site, null, false, scriptId, path);
    }

    @Override
    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId) {
        return getScriptImpl(scope, entityId, false, scriptId, null);
    }

    @Override
    public Properties getScopedScript(final Scope scope, final String entityId, final String scriptId, final String path) {
        return getScriptImpl(scope, entityId, false, scriptId, path);
    }

    @Override
    public Properties getScript(final Scope scope, final String entityId, final String scriptId) {
        return getScriptImpl(scope, entityId, true, scriptId, null);
    }

    @Override
    public Properties getScript(final Scope scope, final String entityId, final String scriptId, final String path) {
        return getScriptImpl(scope, entityId, true, scriptId, path);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String script) throws ConfigServiceException {
        setScriptImpl(user, Scope.Site, null, scriptId, null, script, null);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String path, final String script) throws ConfigServiceException {
        setScriptImpl(user, Scope.Site, null, scriptId, path, script, null);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String script, final Properties properties) throws ConfigServiceException {
        setScriptImpl(user, Scope.Site, null, scriptId, null, script, properties);
    }

    @Override
    public void setSiteScript(final String user, final String scriptId, final String path, final String script, Properties properties) throws ConfigServiceException {
        setScriptImpl(user, Scope.Site, null, scriptId, path, script, properties);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String script) throws ConfigServiceException {
        setScriptImpl(user, scope, entityId, scriptId, null, script, null);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script) throws ConfigServiceException {
        setScriptImpl(user, scope, entityId, scriptId, path, script, null);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String script, final Properties properties) throws ConfigServiceException {
        setScriptImpl(user, scope, entityId, scriptId, null, script, properties);
    }

    @Override
    public void setScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script, Properties properties) throws ConfigServiceException {
        setScriptImpl(user, scope, entityId, scriptId, path, script, properties);
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId) {
        return runScriptImpl(user, Scope.Site, null, false, scriptId, null, getInitializedParameters());
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId, Map<String, Object> parameters) {
        return runScriptImpl(user, Scope.Site, null, false, scriptId, null, parameters);
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId, final String path) {
        return runScriptImpl(user, Scope.Site, null, false, scriptId, path, getInitializedParameters());
    }

    @Override
    public Object runSiteScript(final String user, final String scriptId, final String path, Map<String, Object> parameters) {
        return runScriptImpl(user, Scope.Site, null, false, scriptId, path, parameters);
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId) {
        return runScriptImpl(user, scope, entityId, false, scriptId, null, getInitializedParameters());
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, Map<String, Object> parameters) {
        return runScriptImpl(user, scope, entityId, false, scriptId, null, parameters);
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path) {
        return runScriptImpl(user, scope, entityId, false, scriptId, path, getInitializedParameters());
    }

    @Override
    public Object runScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, Map<String, Object> parameters) {
        return runScriptImpl(user, scope, entityId, false, scriptId, path, parameters);
    }

    /**
     * This attempts to run a script using the indicated scope and entity ID. If no script exists with that scope and
     * entity ID, it will fail over and try to run the script with the same script ID but with the next higher scope.
     * <p/>
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID. Full hierarchical fail-over has performance and relational concerns (e.g.
     * failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user     The user requesting to run the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param scriptId The ID of the script to run.
     * @return The results of the script execution.
     */
    @Override
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId) {
        return runScriptImpl(user, scope, entityId, true, scriptId, null, getInitializedParameters());
    }

    /**
     * This attempts to run a script using the indicated scope and entity ID, passing along the indicated parameters. If
     * no script exists with that scope and entity ID, it will fail over and try to run the script with the same script
     * ID but with the next higher scope.
     * <p/>
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID. Full hierarchical fail-over has performance and relational concerns (e.g.
     * failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user       The user requesting to run the script.
     * @param scope      The scope for the script.
     * @param entityId   The associated entity for the script.
     * @param scriptId   The ID of the script to run.
     * @param parameters The parameters to pass to the script.
     * @return The results of the script execution.
     */
    @Override
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId, final Map<String, Object> parameters) {
        return runScriptImpl(user, scope, entityId, true, scriptId, null, parameters);
    }

    /**
     * This attempts to run a script using the indicated scope and entity ID. If no script exists with that scope and
     * entity ID, it will fail over and try to run the script with the same script ID and path but with the next higher
     * scope.
     * <p/>
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID and path. Full hierarchical fail-over has performance and relational concerns
     * (e.g. failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user     The user requesting to run the script.
     * @param scope    The scope for the script.
     * @param entityId The associated entity for the script.
     * @param scriptId The ID of the script to run.
     * @param path     The path info of the script to run.
     * @return The results of the script execution.
     */
    @Override
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path) {
        return runScriptImpl(user, scope, entityId, true, scriptId, path, getInitializedParameters());
    }

    /**
     * This attempts to run a script using the indicated scope and entity ID, passing along the indicated parameters. If
     * no script exists with that scope and entity ID, it will fail over and try to run the script with the same script
     * ID and path but with the next higher scope.
     * <p/>
     * For now, this effectively means it will try the specific scope and entity ID, then fail over to a site-wide
     * script with the indicated script ID and path. Full hierarchical fail-over has performance and relational concerns
     * (e.g. failing over from subject to project requires retrieving the subject and then the project).
     *
     * @param user       The user requesting to run the script.
     * @param scope      The scope for the script.
     * @param entityId   The associated entity for the script.
     * @param scriptId   The ID of the script to run.
     * @param path       The path info of the script to run.
     * @param parameters The parameters to pass to the script.
     * @return The results of the script execution.
     */
    @Override
    public Object runScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final Map<String, Object> parameters) {
        return runScriptImpl(user, scope, entityId, true, scriptId, path, parameters);
    }

    @Override
    public void enableSiteScript(final String user, final String scriptId) throws NrgServiceException {
        toggleScriptImpl(user, true, Scope.Site, null, scriptId, null);
    }

    @Override
    public void enableSiteScript(final String user, final String scriptId, final String path) throws NrgServiceException {
        toggleScriptImpl(user, true, Scope.Site, null, scriptId, path);
    }

    @Override
    public void enableScopedScript(final String user, final Scope scope, final String entityId, final String scriptId) throws NrgServiceException {
        toggleScriptImpl(user, true, scope, entityId, scriptId, null);
    }

    @Override
    public void enableScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path) throws NrgServiceException {
        toggleScriptImpl(user, true, scope, entityId, scriptId, path);
    }

    @Override
    public void disableSiteScript(final String user, final String scriptId) throws NrgServiceException {
        toggleScriptImpl(user, false, Scope.Site, null, scriptId, null);
    }

    @Override
    public void disableSiteScript(final String user, final String scriptId, final String path) throws NrgServiceException {
        toggleScriptImpl(user, false, Scope.Site, null, scriptId, path);
    }

    @Override
    public void disableScopedScript(final String user, final Scope scope, final String entityId, final String scriptId) throws NrgServiceException {
        toggleScriptImpl(user, false, scope, entityId, scriptId, null);
    }

    @Override
    public void disableScopedScript(final String user, final Scope scope, final String entityId, final String scriptId, final String path) throws NrgServiceException {
        toggleScriptImpl(user, false, scope, entityId, scriptId, path);
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

    private boolean hasScriptImpl(final Scope scope, final String entityId, final boolean failover, final String scriptId, final String path) {
        try {
            Connection connection = _dataSource.getConnection();
            Statement statement = connection.createStatement();
            final StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM XHBM_CONFIGURATION WHERE tool = '");
            sql.append(TOOL_ID_SCRIPTS).append("' AND path = '").append(composite(scriptId, path)).append("' AND status = 'enabled' AND enabled = 't'");
            // TODO: Need way to figure out other scopes.
            if (scope == Scope.Site) {
                sql.append(" AND project IS NULL");
            } else {
                sql.append(" AND project = '").append(entityId).append("'");
            }
            ResultSet results = statement.executeQuery(sql.toString());
            while (results.next()) {
                int count = results.getInt("total");
                if (count > 0) {
                    return true;
                }
                if (failover && scope.failoverTo() != null) {
                    return hasScriptImpl(scope.failoverTo(), entityId, true, scriptId, path);
                }
            }
            return false;
        } catch (SQLException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred checking the database", e);
        }
    }

    private Properties getScriptImpl(final Scope scope, final String entityId, final boolean failover, final String scriptId, final String path) {
        final Object received = validateScopeAndEntityId(scope, entityId);

        // TODO: This is a problem here. The entity ID is associated with the base scope. We need to be able to resolve parent IDs when ascending the scope hierarchy.
        final Long parsed = scope == Scope.Site ? null : (Long) received;
        final Configuration configuration = scope == Scope.Site ?
                _configService.getConfig(TOOL_ID_SCRIPTS, composite(scriptId, path)) :
                _configService.getConfig(TOOL_ID_SCRIPTS, composite(scriptId, path), parsed);

        // If we didn't find an enabled configuration...
        if (configuration == null || configuration.getStatus().equals("disabled")) {
            // If failover is allowed and we have somewhere to fail over to...
            if (failover && scope.failoverTo() != null) {
                // Then fail over to that scope.
                return getScriptImpl(scope.failoverTo(), entityId, true, scriptId, path);
            } else {
                if (_log.isInfoEnabled()) {
                    _log.info("Didn't find a script matching the identifier set: " + formatScriptIdSet(scope, entityId, scriptId, path));
                }
                return null;
            }
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

    private void setScriptImpl(final String user, final Scope scope, final String entityId, final String scriptId, final String path, final String script, Properties properties) throws ConfigServiceException {
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
        final Object received = validateScopeAndEntityId(scope, entityId);
        final Long parsed = scope == Scope.Site ? null : (Long) received;
        _configService.replaceConfig(user, "Updating script", TOOL_ID_SCRIPTS, composite(scriptId, path), true, wrapScript(script, properties), parsed);
    }

    private Object runScriptImpl(final String user, final Scope scope, final String entityId, final boolean failover, final String scriptId, final String path, final Map<String, Object> parameters) {
        Properties properties = getScriptImpl(scope, entityId, failover, scriptId, path);

        if (properties == null) {
            return null;
        }

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
            parameters.put("scope", properties.getProperty("scope"));
            parameters.put("entityId", properties.getProperty("entityId"));
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

    private void toggleScriptImpl(final String user, final boolean enable, final Scope scope, final String entityId, final String scriptId, final String path) throws NrgServiceException {
        final Long projectId = scope == Scope.Site ? null : (Long) validateScopeAndEntityId(scope, entityId);
        if (!hasScriptImpl(scope, entityId, false, scriptId, path)) {
            throw new NrgServiceException(NrgServiceError.UnknownEntity, "Couldn't find the script indicated by " + formatScriptIdSet(scope, entityId, scriptId, path) + " to " + (enable ? "enable" : "disable") + " it.");
        }
        try {
            if (enable) {
                if (scope == Scope.Site) {
                    _configService.enable(user, "Enabling script", TOOL_ID_SCRIPTS, composite(scriptId, path));
                } else {
                    _configService.enable(user, "Enabling script", TOOL_ID_SCRIPTS, composite(scriptId, path), projectId);
                }
            } else {
                if (scope == Scope.Site) {
                    _configService.disable(user, "Enabling script", TOOL_ID_SCRIPTS, composite(scriptId, path));
                } else {
                    _configService.disable(user, "Enabling script", TOOL_ID_SCRIPTS, composite(scriptId, path), projectId);
                }
            }
        } catch (ConfigServiceException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred in the configuration service while trying to " + (enable ? "enable" : "disable") + " the script indicated by " + formatScriptIdSet(scope, entityId, scriptId, path), e);
        }
    }

    /**
     * Provides an opportunity to initialize default parameters with some values.
     *
     * @return An initialized parameters map.
     */
    private Map<String, Object> getInitializedParameters() {
        return new HashMap<String, Object>();
    }

    private Object validateScopeAndEntityId(final Scope scope, final String entityId) {
        final boolean blankEntityId = StringUtils.isBlank(entityId);
        if (scope == Scope.Site) {
            return null;
        }
        if (blankEntityId) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "You must specify a valid value for the entity ID for scope " + scope.code() + ".");
        }
        if (scope == Scope.Project) {
            try {
                return Long.parseLong(entityId);
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

    @Inject
    private ConfigService _configService;
    @Inject
    private DataSource _dataSource;

    private final Map<String, Map<String, ScriptRunner>> _runners = new HashMap<String, Map<String, ScriptRunner>>();
}
