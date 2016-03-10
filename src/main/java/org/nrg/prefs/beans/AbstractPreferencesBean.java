package org.nrg.prefs.beans;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPreferencesBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPrefsService;
import org.reflections.Reflections;

import javax.inject.Inject;
import java.util.Set;

public abstract class AbstractPreferencesBean implements PreferencesBean {
    @Override
    public final String getToolId() {
        if (StringUtils.isBlank(_toolId)) {
            if (getClass().isAnnotationPresent(NrgPreferencesBean.class)) {
                _toolId = getClass().getAnnotation(NrgPreferencesBean.class).toolId();
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + getClass().getName() + " must be annotated with the NrgPreferencesBean annotation.");
            }
        }
        return _toolId;
    }

    public final Class<? extends PreferenceEntityResolver> getResolver() {
        if (!_resolverInited) {
            if (getClass().isAnnotationPresent(NrgPreferencesBean.class)) {
                final Class<? extends PreferenceEntityResolver>[] resolvers = getClass().getAnnotation(NrgPreferencesBean.class).resolver();
                if (resolvers.length == 0) {
                    _resolver = null;
                } else {
                    _resolver = resolvers[0];
                }
                _resolverInited = true;
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + getClass().getName() + " must be annotated with the NrgPreferencesBean annotation.");
            }
        }
        return _resolver;
    }

    @Override
    public Preference get(final String preference, final String entityId) throws UnknownToolId {
        return _service.getPreference(getToolId(), preference, Scope.Site, entityId);
    }

    @Override
    public Preference get(final String preference, final Scope scope, final String entityId) throws UnknownToolId {
        return _service.getPreference(getToolId(), preference, scope, entityId);
    }

    @Override
    public String getValue(final String preferenceName) throws UnknownToolId {
        return _service.getPreferenceValue(getToolId(), preferenceName);
    }

    @Override
    public String getValue(final String preferenceName, final String entityId) throws UnknownToolId {
        return _service.getPreferenceValue(getToolId(), preferenceName, Scope.Site, entityId);
    }

    @Override
    public String getValue(final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        return _service.getPreferenceValue(getToolId(), preferenceName, scope, entityId);
    }

    @Override
    public void set(final String preference, final String value) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), preference, value);
    }

    @Override
    public void set(final String preference, final String value, final String entityId) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), preference, Scope.Site, entityId, value);
    }

    @Override
    public void set(final String preference, final String value, final Scope scope, final String entityId) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), preference, scope, entityId, value);
    }

    @Override
    public void delete(final String preference) throws InvalidPreferenceName {
        _service.deletePreference(getToolId(), preference);
    }

    @Override
    public void delete(final String preference, final String entityId) throws InvalidPreferenceName {
        _service.deletePreference(getToolId(), preference, Scope.Site, entityId);
    }

    @Override
    public void delete(final String preference, final Scope scope, final String entityId) throws InvalidPreferenceName {
        _service.deletePreference(getToolId(), preference, scope, entityId);
    }

    @Inject
    private NrgPrefsService _service;

    private String _toolId;
    private boolean _resolverInited = false;
    private Class<? extends PreferenceEntityResolver> _resolver;
}
