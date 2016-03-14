package org.nrg.prefs.beans;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPrefsService;
import org.reflections.ReflectionUtils;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.reflections.ReflectionUtils.withAnnotation;

public abstract class AbstractPreferencesBean implements PreferencesBean {
    @Override
    public final String getToolId() {
        if (StringUtils.isBlank(_toolId)) {
            if (getClass().isAnnotationPresent(NrgPreferenceBean.class)) {
                _toolId = getClass().getAnnotation(NrgPreferenceBean.class).toolId();
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + getClass().getName() + " must be annotated with the NrgPreferenceBean annotation.");
            }
        }
        return _toolId;
    }

    public final Class<? extends PreferenceEntityResolver> getResolver() {
        if (!_resolverInited) {
            if (getClass().isAnnotationPresent(NrgPreferenceBean.class)) {
                final Class<? extends PreferenceEntityResolver>[] resolvers = getClass().getAnnotation(NrgPreferenceBean.class).resolver();
                if (resolvers.length == 0) {
                    _resolver = null;
                } else {
                    _resolver = resolvers[0];
                }
                _resolverInited = true;
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + getClass().getName() + " must be annotated with the NrgPreferenceBean annotation.");
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

    @Override
    public Map<String, PreferenceInfo> getDefaultPreferences() {
        final Map<String, PreferenceInfo>                preferences = new HashMap<>();
        @SuppressWarnings("unchecked") final Set<Method> properties  = ReflectionUtils.getAllMethods(getClass(), withAnnotation(NrgPreference.class));
        for (final Method method : properties) {
            if (isGetter(method)) {
                final String   name         = propertize(method.getName(), "get");
                final String   defaultValue = method.getAnnotation(NrgPreference.class).defaultValue();
                final Class<?> type         = method.getReturnType();
                preferences.put(name, new PreferenceInfo(name, defaultValue, type));
            } else if (isSetter(method)) {
                final String   name         = propertize(method.getName(), "set");
                final String   defaultValue = method.getAnnotation(NrgPreference.class).defaultValue();
                final Class<?> type         = method.getParameterTypes()[0];
                preferences.put(name, new PreferenceInfo(name, defaultValue, type));
            }
        }
        return preferences;
    }

    private static String propertize(final String name, final String type) {
        return StringUtils.uncapitalize(name.replace(type, ""));
    }

    private static boolean isGetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) && PATTERN_GETTER.matcher(method.getName()).matches() && method.getParameterTypes().length == 0;
    }

    private static boolean isSetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) && PATTERN_SETTER.matcher(method.getName()).matches() && method.getParameterTypes().length == 1;
    }

    private static final Pattern PATTERN_GETTER = Pattern.compile("^get[A-Z][A-z]+");
    private static final     Pattern PATTERN_SETTER = Pattern.compile("^set[A-Z][A-z]+");

    @Inject
    private NrgPrefsService _service;

    private String _toolId;
    private boolean _resolverInited = false;
    private Class<? extends PreferenceEntityResolver> _resolver;
}
