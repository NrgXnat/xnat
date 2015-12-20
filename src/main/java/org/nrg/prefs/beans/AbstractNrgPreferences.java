package org.nrg.prefs.beans;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.NrgPrefsService;

abstract public class AbstractNrgPreferences implements NrgPreferences {

    protected AbstractNrgPreferences(final NrgPrefsService service, final String toolId, final EntityResolver<Preference> resolver) {
        this(service, toolId, Scope.Site, resolver);
    }

    protected AbstractNrgPreferences(final NrgPrefsService service, final String toolId, final Scope scope, final EntityResolver<Preference> resolver) {
        _service = service;
        _toolId = toolId;
        _scope = scope;
        _service.setEntityResolver(toolId, resolver);
        _resolver = resolver;
    }

    public EntityResolver<Preference> getResolver() {
        return _resolver;
    }

    protected Preference getPreference(final String preference) {
        return _service.getPreference(_toolId, preference);
    }

    protected Preference getPreference(final String preference, final String entityId) {
        return _service.getPreference(_toolId, preference, _scope, entityId);
    }

    protected Preference getPreference(final String preference, final Scope scope, final String entityId) {
        return _service.getPreference(_toolId, preference, scope, entityId);
    }

    protected void setPreference(final String preference, final String value) {
        _service.setPreferenceValue(_toolId, preference, value);
    }

    protected void setPreference(final String preference, final String value, final String entityId) {
        _service.setPreferenceValue(_toolId, preference, _scope, entityId, value);
    }

    protected void setPreference(final String preference, final String value, final Scope scope, final String entityId) {
        _service.setPreferenceValue(_toolId, preference, scope, entityId, value);
    }

    private final NrgPrefsService _service;
    private final String _toolId;
    private final Scope _scope;
    private final EntityResolver<Preference> _resolver;
}
