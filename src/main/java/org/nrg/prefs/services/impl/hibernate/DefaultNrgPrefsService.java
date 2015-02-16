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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service
public class DefaultNrgPrefsService implements NrgPrefsService {
    /**
     * Creates a new {@link Tool preferences tool} instance.
     * @param id             The ID of the new tool.
     * @param name           The name of the new tool.
     * @param description    The description of the new tool.
     * @param preferences    The available preferences for the tool along with their default values.
     * @return The newly created tool instance.
     */
    @Transactional
    @Override
    public Tool createTool(final String id, final String name, final String description, final Map<String, String> preferences) {
        final Tool tool = new Tool(id, name, description, preferences);
        _toolService.create(tool);
        for (final String preference : preferences.keySet()) {
            setPreference(tool, preference, EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferences.get(preference));
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
        setPreferenceValue(toolId, preferenceName, EntityId.Default.getScope(), EntityId.Default.getEntityId(), value);
    }

    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) {
        final Tool tool = _toolService.getByToolId(toolId);
        if (!tool.getToolPreferences().containsKey(preferenceName)) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "The tool " + toolId + " doesn't support the preference " + preferenceName + ".");
        }
        setPreference(tool, preferenceName, scope, entityId, value);
    }

    private void setPreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId, final String value) {
        _preferenceService.setPreference(tool, preferenceName, scope, entityId, value);
    }

    @Override
    public Set<String> getToolIds() {
        return getToolMap().keySet();
    }

    @Override
    public Set<Tool> getTools() {
        return new HashSet<>(getToolMap().values());
    }

    @Override
    public Set<String> getToolPropertyNames(final String toolId) {
        return null;
    }

    @Override
    public Properties getToolProperties(final String toolId) {
        return null;
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

    private Map<String, Tool> getToolMap() {
        if (_tools.size() == 0) {
            initializeTools();
        }
        return _tools;
    }

    private EntityResolver getResolver(final String toolId) {
        return _resolvers.containsKey(toolId) ? _resolvers.get(toolId) : _resolvers.get("defaultResolver");
    }

    private synchronized void initializeTools() {
        final List<Tool> tools = _toolService.getAll();
        for (final Tool tool : tools) {
            _tools.put(tool.getToolId(), tool);
        }
    }

    @Inject
    private ToolService _toolService;
    @Inject
    private PreferenceService _preferenceService;
    @Inject
    private Map<String, EntityResolver> _resolvers;

    private final Map<String, Tool> _tools = new HashMap<>();
}
