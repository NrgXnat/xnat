package org.nrg.prefs.beans;

import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.services.NrgPrefsService;

public class BaseNrgPreferences implements NrgPreferences {
    public BaseNrgPreferences(final NrgPrefsService service, final String toolId) {
        this(service, toolId, Scope.Site);
    }

    public BaseNrgPreferences(final NrgPrefsService service, final String toolId, final Scope scope) {
        _service = service;
        _toolId = toolId;
        _scope = scope;
    }

    public Preference get(final String preference) throws UnknownToolId {
        return _service.getPreference(_toolId, preference);
    }

    @Override
    public Preference get(final String preference, final String entityId) throws UnknownToolId {
        return _service.getPreference(_toolId, preference, _scope, entityId);
    }

    @Override
    public Preference get(final String preference, final Scope scope, final String entityId) throws UnknownToolId {
        return _service.getPreference(_toolId, preference, scope, entityId);
    }

    @Override
    public String getValue(final String preferenceName) throws UnknownToolId {
        return _service.getPreferenceValue(_toolId, preferenceName);
    }

    @Override
    public String getValue(final String preferenceName, final String entityId) throws UnknownToolId {
        return _service.getPreferenceValue(_toolId, preferenceName, _scope, entityId);
    }

    @Override
    public String getValue(final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        return _service.getPreferenceValue(_toolId, preferenceName, scope, entityId);
    }

    @Override
    public void set(final String preference, final String value) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(_toolId, preference, value);
    }

    @Override
    public void set(final String preference, final String value, final String entityId) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(_toolId, preference, _scope, entityId, value);
    }

    @Override
    public void set(final String preference, final String value, final Scope scope, final String entityId) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(_toolId, preference, scope, entityId, value);
    }

    @Override
    public void delete(final String preference) throws InvalidPreferenceName {
        _service.deletePreference(_toolId, preference);
    }

    @Override
    public void delete(final String preference, final String entityId) throws InvalidPreferenceName {
        _service.deletePreference(_toolId, preference, _scope, entityId);
    }

    @Override
    public void delete(final String preference, final Scope scope, final String entityId) throws InvalidPreferenceName {
        _service.deletePreference(_toolId, preference, scope, entityId);
    }

    private final NrgPrefsService _service;
    private final String _toolId;
    private final Scope _scope;
}
