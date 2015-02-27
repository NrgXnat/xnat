package org.nrg.prefs.services.impl.hibernate;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPrefsService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service
public class DefaultNrgPrefsService implements NrgPrefsService {
    /**
     * Creates a {@link Tool tool} with no default properties or values set.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool toolName.
     * @param description    The readable description of the tool.
     * @return The object representing the persisted tool definition.
     */
    @Transactional
    @Override
    public Tool createTool(final String toolId, final String toolName, final String description) {
        return createTool(toolId, toolName, description, (Map<String, String>) null);
    }

    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool toolName.
     * @param description    The readable description of the tool.
     * @param defaults       The default properties and values for the tool.
     * @return The object representing the persisted tool definition.
     */
    @Transactional
    @Override
    public Tool createTool(final String toolId, final String toolName, final String description, final Properties defaults) {
        return createTool(toolId, toolName, description, convertPropertiesToMap(defaults));
    }

    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool toolName.
     * @param description    The readable description of the tool.
     * @param defaults       The default properties and values for the tool.
     * @return The object representing the persisted tool definition.
     */
    @Transactional
    @Override
    public Tool createTool(final String toolId, final String toolName, final String description, final Map<String, String> defaults) {
        if (_log.isDebugEnabled()) {
            _log.debug("Request to create a new tool received: {}", toolId);
        }
        final Tool tool = new Tool(toolId, toolName, description, defaults);
        _toolService.create(tool);
        if (_log.isDebugEnabled()) {
            _log.debug("New tool {} created with primary key ID: {} at {}", toolId, tool.getId(), tool.getCreated());
        }
        if (defaults != null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found {} default values to add to tool {}", defaults.size(), toolId);
            }
            for (final String preference : defaults.keySet()) {
                _preferenceService.setPreference(tool, preference, EntityId.Default.getScope(), EntityId.Default.getEntityId(), defaults.get(preference));
                if (_log.isDebugEnabled()) {
                    _log.debug(" * {}: {}", preference, defaults.get(preference));
                }
            }
        }
        return tool;
    }

    @Transactional
    @Override
    public Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        final EntityResolver resolver = getResolver(toolId);
        // TODO: In this case I know that this resolver will return a Preference, so I can just cast it. There needs to be a better way to manage this, though.
        return (Preference) resolver.resolve(new EntityId(scope, entityId), toolId, preferenceName);
    }

    @Transactional
    @Override
    public String getPreferenceValue(final String toolId, final String preferenceName) {
        return getPreferenceValue(toolId, preferenceName, EntityId.Default.getScope(), EntityId.Default.getEntityId());
    }

    @Transactional
    @Override
    public String getPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        final Preference preference = getPreference(toolId, preferenceName, scope, entityId);
        return preference != null ? preference.getValue() : null;
    }

    @Transactional
    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final String value) {
        setPreferenceValue(toolId, preferenceName, DEFAULT_SCOPE, DEFAULT_ENTITY_ID, value);
    }

    @Transactional
    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) {
        final Tool tool = _toolService.getByToolId(toolId);
        if (tool == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "Didn't find a tool with the corresponding tool ID: " + toolId);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Found the tool identified by tool ID: {}", toolId);
        }
        // If the preference doesn't already exist in the known preferences, add it.
        if (!tool.getToolPreferences().containsKey(preferenceName)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Adding preference {} to tool {} with default value of {}", preferenceName, toolId, value);
            }
            tool.getToolPreferences().put(preferenceName, value);
            _toolService.update(tool);
        }
        _preferenceService.setPreference(tool, preferenceName, scope, entityId, value);
    }

    @Override
    public Set<String> getToolIds() {
        return _toolService.getToolIds();
    }

    @Override
    public Set<Tool> getTools() {
        return new HashSet<>(_toolService.getAll());
    }

    @Override
    public Set<String> getToolPropertyNames(final String toolId) {
        return _preferenceService.getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID).stringPropertyNames();
    }

    @Override
    public Properties getToolProperties(final String toolId) {
        return _preferenceService.getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    @Override
    public String getPropertyValue(final String toolId, final String property) {
        return null;
    }

    @Override
    public String getPropertyValue(final String toolId, final String property, final EntityId entityId) {
        return null;
    }

    @Override
    public String getPropertyValue(final String toolId, final String property, final Scope scope, final String entityId) {
        return null;
    }

    @Override
    public void setEntityResolver(final String toolId, final EntityResolver resolver) {
        _resolvers.put(toolId, resolver);
    }

    private EntityResolver getResolver(final String toolId) {
        return _resolvers.containsKey(toolId) ? _resolvers.get(toolId) : _resolvers.get("defaultResolver");
    }

    private static Map<String,String> convertPropertiesToMap(final Properties properties) {
        Map<String, String> map = new HashMap<>();
        for (final String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return map;
    }

    @Inject
    private ToolService _toolService;
    @Inject
    private PreferenceService _preferenceService;
    @Inject
    private Map<String, EntityResolver> _resolvers;

    private static final Logger _log = LoggerFactory.getLogger(DefaultNrgPrefsService.class);
    private static final Scope DEFAULT_SCOPE = EntityId.Default.getScope();
    private static final String DEFAULT_ENTITY_ID = EntityId.Default.getEntityId();
}
