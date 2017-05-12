/*
 * prefs: org.nrg.prefs.beans.AbstractPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceBeanHelper;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.base.Predicates.*;
import static org.nrg.framework.utilities.Reflection.findAnnotationInClassHierarchy;
import static org.nrg.framework.utilities.Reflection.getGetter;

public abstract class AbstractPreferenceBean extends HashMap<String, Object> implements PreferenceBean {
    /**
     * Initializes default preferences for the bean and sets the preference service to be used for managing system
     * preferences.
     *
     * @param preferenceService The preference service to use for managing preference values.
     */
    protected AbstractPreferenceBean(final NrgPreferenceService preferenceService) {
        this(preferenceService, null);
    }

    /**
     * Initializes default preferences for the bean and sets the preference service to be used for managing system
     * preferences, as well as the configuration folder paths to be used when attempting to locate initialization
     * properties.
     *
     * @param preferenceService The preference service to use for managing preference values.
     * @param configFolderPaths The configuration folder paths to search for initialization properties.
     */
    protected AbstractPreferenceBean(final NrgPreferenceService preferenceService, final ConfigPaths configFolderPaths) {
        this(preferenceService, configFolderPaths, null);
    }

    /**
     * Initializes default preferences for the bean and sets the preference service to be used for managing system
     * preferences, as well as the configuration folder paths to be used when attempting to locate initialization
     * properties.
     *
     * @param preferenceService The preference service to use for managing preference values.
     * @param configFolderPaths The configuration folder paths to search for initialization properties.
     * @param initPrefs         The ordered properties to consider when initializing bean values.
     */
    protected AbstractPreferenceBean(final NrgPreferenceService preferenceService, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        _preferenceService = preferenceService;
        _configFolderPaths = configFolderPaths == null ? new ConfigPaths() : configFolderPaths;
        _initPrefs = initPrefs == null ? new OrderedProperties() : initPrefs;
        _preferences.putAll(PreferenceBeanHelper.getPreferenceInfoMap(getClass()));
        PreferenceBeanHelper.getAliases(_preferences.values(), _aliases, _aliasedPreferences);
    }

    @JsonIgnore
    @Autowired
    public void setNrgPreferenceService(final NrgPreferenceService service) {
        _preferenceService = service;
    }

    /**
     * Initializes the preference bean. Bean implementations can provide custom pre- and post-processing of
     * initialization
     * by overriding the default {@link #preProcessPreferences()} and {@link #postProcessPreferences()} methods
     * respectively.
     *
     * @return A reference to this instance of the preferences bean.
     */
    @JsonIgnore
    @PostConstruct
    public PreferenceBean initialize() {
        if (_preferenceService == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "The NrgPreferenceService instance must be configured and wired before this preference bean can be initialized.");
        }
        preProcessPreferences();
        processDefaultPreferences();
        postProcessPreferences();
        return this;
    }

    @Override
    public final String getToolId() {
        if (StringUtils.isBlank(_toolId)) {
            final NrgPreferenceBean annotation = getNrgPreferenceBean();
            if (annotation != null) {
                _toolId = annotation.toolId();
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + getClass().getName() + " must be annotated with the NrgPreferenceBean annotation.");
            }
        }
        return _toolId;
    }

    /**
     * Gets the keys for all the preferences.
     *
     * @return The set of preference keys.
     *
     * @deprecated Use the {@link #keySet()} method instead.
     */
    @Override
    @Deprecated
    public Set<String> getPreferenceKeys() {
        return keySet();
    }

    /**
     * Gets the preferences for the current implementation as a map.
     *
     * @return The preferences for the current implementation as a map.
     *
     * @deprecated Preference beans are now themselves maps. This method just returns the preference bean itself.
     */
    @Override
    @Deprecated
    public Map<String, Object> getPreferenceMap() {
        return this;
    }

    /**
     * Gets the preferences for the current implementation as a map, including only the specified keys.
     *
     * @param preferenceNames The preferences to return in the map.
     *
     * @return The preferences for the current implementation as a map.
     *
     * @deprecated Preference beans are now themselves maps. This method calls the {@link #getPreferences(Set)} method.
     * You can also call use streams or Guava methods to filter the bean itself as a map.
     */
    @Override
    @Deprecated
    public Map<String, Object> getPreferenceMap(final String... preferenceNames) {
        return getPreferences(new HashSet<>(Arrays.asList(preferenceNames)));
    }

    /**
     * Gets the preferences for the current implementation as a map, including only the specified keys.
     *
     * @param preferenceNames The preferences to return in the map.
     *
     * @return The preferences for the current implementation as a map.
     *
     * @deprecated Preference beans are now themselves maps. This method calls the {@link #getPreferences(Set)} method.
     * You can also call use streams or Guava methods to filter the bean itself as a map.
     */
    @Override
    @Deprecated
    public Map<String, Object> getPreferenceMap(final Set<String> preferenceNames) {
        return getPreferences(preferenceNames);
    }

    /**
     * Returns the preferences with the specified names.
     *
     * @param preferenceNames The names of the preferences to be retrieved.
     *
     * @return The requested preferences.
     */
    public Map<String, Object> getPreferences(final Set<String> preferenceNames) {
        return Maps.filterKeys(this, Predicates.in(preferenceNames));
    }

    @JsonIgnore
    @Override
    public Properties asProperties() {
        final Properties properties = new Properties();
        for (final String preference : keySet()) {
            final Object value = get(preference);
            properties.setProperty(preference, value != null ? value.toString() : "");
        }
        return properties;
    }

    @JsonIgnore
    @Override
    public final Class<? extends PreferenceEntityResolver> getResolver() {
        if (!_resolverInitialized) {
            final NrgPreferenceBean annotation = getNrgPreferenceBean();
            if (annotation != null) {
                final Class<? extends PreferenceEntityResolver>[] resolvers = annotation.resolver();
                if (resolvers.length == 0) {
                    _resolver = null;
                } else {
                    _resolver = resolvers[0];
                }
                _resolverInitialized = true;
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + getClass().getName() + " must be annotated with the NrgPreferenceBean annotation.");
            }
        }
        return _resolver;
    }

    @JsonIgnore
    @Override
    public Preference getPreference(final String key, final String... subkeys) throws UnknownToolId {
        return _preferenceService.getPreference(getToolId(), getNamespacedPropertyId(key, subkeys));
    }

    @JsonIgnore
    @Override
    public Preference getPreference(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        return _preferenceService.getPreference(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId);
    }

    @JsonIgnore
    @Override
    public String getValue(final String key, final String... subkeys) throws UnknownToolId {
        return _preferenceService.getPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys));
    }

    @JsonIgnore
    @Override
    public String getValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        return _preferenceService.getPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId);
    }

    @JsonIgnore
    @Override
    public Object getProperty(final String preference) throws UnknownToolId {
        return getProperty(preference, null);
    }

    @JsonIgnore
    @Override
    public Object getProperty(final String preference, final Object defaultValue) throws UnknownToolId {
        if (containsKey(preference)) {
            final Object value = getFromCache(preference);
            if (value != null) {
                _log.debug("Found cached value for preference {}, returning that: {}", preference, value.toString());
                return value;
            }
            _log.debug("Found entry for preference {}, but value was null, trying to retrieve via getter method", preference);
        }
        final Method method;
        if (_methods.containsKey(preference)) {
            method = _methods.get(preference);
        } else {
            method = getGetter(getClass(), AbstractPreferenceBean.class, preference, ReflectionUtils.withAnnotation(NrgPreference.class));
            if (method == null) {
                final Tool tool = _preferenceService.getTool(getToolId());
                if (tool.isStrict()) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "No such property on this preference object: " + preference);
                }
                final String returnValue = getValue(preference);
                if (StringUtils.isNotBlank(returnValue)) {
                    storeToCache(preference, returnValue);
                    return returnValue;
                }
                if (defaultValue == null) {
                    return null;
                }

                storeToCache(preference, defaultValue);
                return defaultValue;
            }
            _methods.put(preference, method);
        }
        try {
            final Object returnValue = method.invoke(this);
            if (returnValue != null) {
                storeToCache(preference, returnValue);
                return returnValue;
            }
            storeToCache(preference, defaultValue);
            return defaultValue;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to reference the " + preference + " setting on the " + getToolId() + " preference bean " + getClass().getName(), e);
        }
    }

    protected Object getPropertyNoCache(final String preference, final Object defaultValue) throws UnknownToolId {
        if (containsKey(preference)) {
            final Object value = getFromCache(preference);
            if (value != null) {
                _log.debug("Found cached value for preference {}, returning that: {}", preference, value.toString());
                return value;
            }
            _log.debug("Found entry for preference {}, but value was null, trying to retrieve via getter method", preference);
        }
        final Method method;
        if (_methods.containsKey(preference)) {
            method = _methods.get(preference);
        } else {
            method = getGetter(getClass(), AbstractPreferenceBean.class, preference, ReflectionUtils.withAnnotation(NrgPreference.class));
            if (method == null) {
                final Tool tool = _preferenceService.getTool(getToolId());
                if (tool.isStrict()) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "No such property on this preference object: " + preference);
                }
                final String returnValue = getValue(preference);
                if (StringUtils.isNotBlank(returnValue)) {
                    storeToCache(preference, returnValue);
                    return returnValue;
                }
                if (defaultValue == null) {
                    return null;
                }

                storeToCache(preference, defaultValue);
                return defaultValue;
            }
            _methods.put(preference, method);
        }
        try {
            final Object returnValue = method.invoke(this);
            if (returnValue != null) {
//                storeToCache(preference, returnValue);
                return returnValue;
            }
            return defaultValue;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to reference the " + preference + " setting on the " + getToolId() + " preference bean " + getClass().getName(), e);
        }
    }

    @JsonIgnore
    @Override
    public Object getProperty(final Scope scope, final String entityId, final String preference) throws UnknownToolId {
        throw new NotImplementedException("The entity-scoped value-by-reference method is not yet implemented.");
    }

    @JsonIgnore
    @Override
    public Object getProperty(final Scope scope, final String entityId, final String preference, final Object defaultValue) throws UnknownToolId {
        throw new NotImplementedException("The entity-scoped value-by-reference method is not yet implemented.");
    }

    @JsonIgnore
    @Override
    public Boolean getBooleanValue(final String key, final String... subkeys) throws UnknownToolId {
        return getBooleanValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public Boolean getBooleanValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @JsonIgnore
    @Override
    public Integer getIntegerValue(final String key, final String... subkeys) throws UnknownToolId {
        return getIntegerValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public Integer getIntegerValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    @JsonIgnore
    @Override
    public Long getLongValue(final String key, final String... subkeys) throws UnknownToolId {
        return getLongValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public Long getLongValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    @JsonIgnore
    @Override
    public Float getFloatValue(final String key, final String... subkeys) throws UnknownToolId {
        return getFloatValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public Float getFloatValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Float.parseFloat(value);
    }

    @JsonIgnore
    @Override
    public Double getDoubleValue(final String key, final String... subkeys) throws UnknownToolId {
        return getDoubleValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public Double getDoubleValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Double.parseDouble(value);
    }

    @JsonIgnore
    @Override
    public Date getDateValue(final String key, final String... subkeys) throws UnknownToolId {
        return getDateValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public Date getDateValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        final Long date = Long.parseLong(value);
        return new Date(date);
    }

    @JsonIgnore
    @Override
    public <T> Map<String, T> getMapValue(final String preferenceName) throws UnknownToolId {
        return getMapValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @JsonIgnore
    @Override
    public <T> Map<String, T> getMapValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final PreferenceInfo info = getPreferenceInfo(preferenceName);
        @SuppressWarnings("unchecked") final MapType mapType = getTypeFactory().constructMapType((Class<? extends Map>) info.getValueType(), String.class, info.getItemType());
        try {
            final Map<String, Object> map = deserialize("{}", mapType);
            final Set<String> propertyNames = Sets.filter(_preferenceService.getToolPropertyNames(getToolId()), or(equalTo(preferenceName), containsPattern("^" + preferenceName + NAMESPACE_DELIMITER)));
            for (final String propertyName : propertyNames) {
                final String value = _preferenceService.getPreferenceValue(getToolId(), propertyName);
                @SuppressWarnings("unchecked") final Object item = deserialize(value, info.getItemType());
                map.put(getPreferenceSubkey(propertyName), item);
            }
            //noinspection unchecked
            return (Map<String, T>) map;
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @JsonIgnore
    @Override
    public <T> List<T> getListValue(final String preferenceName) throws UnknownToolId {
        return getListValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @JsonIgnore
    @Override
    public <T> List<T> getListValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final PreferenceInfo info = getPreferenceInfo(preferenceName);
        @SuppressWarnings("unchecked") final CollectionType listType = getTypeFactory().constructCollectionType((Class<? extends List>) info.getValueType(), info.getItemType());
        try {
            if (BeanUtils.isSimpleValueType(info.getItemType())) {
                final String value = _preferenceService.getPreferenceValue(getToolId(), preferenceName);
                return deserialize(StringUtils.defaultIfBlank(value, "[]"), listType);
            } else {
                final List<T> list = deserialize("[]", listType);
                final Set<String> propertyNames = Sets.filter(_preferenceService.getToolPropertyNames(getToolId()), or(equalTo(preferenceName), containsPattern("^" + preferenceName + NAMESPACE_DELIMITER)));
                for (final String propertyName : propertyNames) {
                    final String value = _preferenceService.getPreferenceValue(getToolId(), propertyName);
                    @SuppressWarnings("unchecked") final T item = deserialize(value, (Class<? extends T>) info.getItemType());
                    list.add(item);
                }
                return list;
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @JsonIgnore
    @Override
    public <T> T[] getArrayValue(final String preferenceName) throws UnknownToolId {
        return getArrayValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    @JsonIgnore
    @Override
    public <T> T[] getArrayValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final List<T> list = getListValue(scope, entityId, preferenceName);
        return (T[]) list.toArray(new Object[list.size()]);
    }

    @JsonIgnore
    @Override
    public <T> T getObjectValue(final String preferenceName) throws UnknownToolId {
        return getObjectValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @JsonIgnore
    @Override
    public <T> T getObjectValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final PreferenceInfo preference = _preferences.get(preferenceName);
        //noinspection unchecked
        return (T) getObjectValue(scope, entityId, preferenceName, preference.getValueType());
    }

    @JsonIgnore
    @Override
    public <T> T getObjectValue(final Scope scope, final String entityId, final String preferenceName, final Class<T> clazz) throws UnknownToolId {
        final String value = getValue(scope, entityId, preferenceName);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return deserialize(value, clazz);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to deserialize the preference " + preferenceName + " to type " + clazz.getName(), e);
        }
    }

    @JsonIgnore
    @Override
    public void create(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _preferenceService.create(getToolId(), getNamespacedPropertyId(key, subkeys), value);
    }

    @JsonIgnore
    @Override
    public void create(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _preferenceService.create(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId, value);
    }

    @JsonIgnore
    @Override
    public String set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        return set(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public String set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        final String current = getValue(namespacedPropertyId);
        if (_preferences.containsKey(namespacedPropertyId)) {
            try {
                final Properties existing = _preferenceService.getToolProperties(getToolId(), Collections.singletonList(namespacedPropertyId));
                final Properties properties = convertValueForPreference(getPreferenceInfo(namespacedPropertyId), value);
                for (final String property : properties.stringPropertyNames()) {
                    _preferenceService.setPreferenceValue(getToolId(), property, scope, entityId, properties.getProperty(property));
                    if (existing.containsKey(property)) {
                        existing.remove(property);
                    }
                }
            } catch (IOException | IllegalAccessException | InvocationTargetException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to set the " + namespacedPropertyId + " preference setting.", e);
            }
        } else {
            _preferenceService.setPreferenceValue(getToolId(), namespacedPropertyId, scope, entityId, value);
        }
        // This caches primitive values that aren't sub-items in other preferences.
        if (subkeys.length == 0){
            String keyPrefix = key;
            if(key.indexOf(":")!=-1){
                keyPrefix = key.substring(0,key.indexOf(":"));
            }

            if(_preferences.containsKey(keyPrefix)){
                final Class<?> valueType = _preferences.get(keyPrefix).getValueType();
                if(isPrimative(valueType)){
                    storeToCache(key, value);
                }
            }
            else{
                if (_preferenceService.getTool(getToolId()).isStrict()) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "No such property on this preference object: " + namespacedPropertyId);
                }
                else{
                    storeToCache(key, value);
                }
            }
        }


        return current;
    }

    @JsonIgnore
    @Override
    public void setBooleanValue(final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Only set the value here if this is the primary value without subkeys. Value before subkeys should be set at List or Map setter.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            storeToCache(key, value);
        }
        setBooleanValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setBooleanValue(final Scope scope, final String entityId, final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setIntegerValue(final Integer value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Only set the value here if this is the primary value without subkeys. Value before subkeys should be set at List or Map setter.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            storeToCache(key, value);
        }
        setIntegerValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setIntegerValue(final Scope scope, final String entityId, final Integer value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setLongValue(final Long value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Only set the value here if this is the primary value without subkeys. Value before subkeys should be set at List or Map setter.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            storeToCache(key, value);
        }
        setLongValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setLongValue(final Scope scope, final String entityId, final Long value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setFloatValue(final Float value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Only set the value here if this is the primary value without subkeys. Value before subkeys should be set at List or Map setter.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            storeToCache(key, value);
        }
        setFloatValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setFloatValue(final Scope scope, final String entityId, final Float value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setDoubleValue(final Double value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Only set the value here if this is the primary value without subkeys. Value before subkeys should be set at List or Map setter.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            storeToCache(key, value);
        }
        setDoubleValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setDoubleValue(final Scope scope, final String entityId, final Double value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setDateValue(final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Only set the value here if this is the primary value without subkeys. Value before subkeys should be set at List or Map setter.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            storeToCache(key, value);
        }
        setDateValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setDateValue(final Scope scope, final String entityId, final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, Long.toString(value.getTime()), key, subkeys);
    }

    @JsonIgnore
    @Override
    public <T> void setObjectValue(final T value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        // Cheap map caching here is only for site-level settings.
        storeToCache(key, value);
        setObjectValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public <T> void setObjectValue(final Scope scope, final String entityId, T value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, serialize(value), key, subkeys);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @JsonIgnore
    @Override
    public <T> void setMapValue(final String preferenceName, Map<String, T> map) throws UnknownToolId, InvalidPreferenceName {
        // Cheap map caching here is only for site-level settings.
        storeToCache(preferenceName, map);
        setMapValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, map);
    }

    @JsonIgnore
    @Override
    public <T> void setMapValue(final Scope scope, final String entityId, final String preferenceName, Map<String, T> map) throws UnknownToolId, InvalidPreferenceName {
        for (final String key : map.keySet()) {
            final String id = getNamespacedPropertyId(preferenceName, key);
            try {
                set(scope, entityId, serialize(map.get(key)), id);
            } catch (IOException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
            }
        }
    }

    @JsonIgnore
    @Override
    public <T> void setListValue(final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName {
        // Cheap map caching here is only for site-level settings.
        storeToCache(preferenceName, list);
        setListValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, list);
    }

    @JsonIgnore
    @Override
    public <T> void setListValue(final Scope scope, final String entityId, final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, serialize(list), preferenceName);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @JsonIgnore
    @Override
    public <T> void setArrayValue(final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName {
        // Cheap map caching here is only for site-level settings.
        storeToCache(preferenceName, array);
        setArrayValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, array);
    }

    @JsonIgnore
    @Override
    public <T> void setArrayValue(final Scope scope, final String entityId, final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, serialize(array), preferenceName);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @JsonIgnore
    @Override
    public void delete(final String key, final String... subkeys) throws InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        _preferenceService.deletePreference(getToolId(), namespacedPropertyId);

        // Delete the value here if this is the primary value without subkeys. If subkeys present, refresh value.
        // Cheap map caching here is only for site-level settings.
        if (subkeys.length == 0) {
            removeFromCache(key);
        } else {
            final Object value = getProperty(key);
            if (value == null) {
                removeFromCache(key);
            } else {
                storeToCache(key, value);
            }
        }
    }

    @JsonIgnore
    @Override
    public void delete(final Scope scope, final String entityId, final String key, final String... subkeys) throws InvalidPreferenceName {
        _preferenceService.deletePreference(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId);
    }

    @JsonIgnore
    @Override
    public Map<String, PreferenceInfo> getDefaultPreferences() {
        return ImmutableMap.copyOf(_preferences);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return keySet().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        return key != null && keySet().contains(key.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(final Object value) {
        throw new NotImplementedException("Values are not searchable in preference bean maps.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final Object key) {
        if (key == null) {
            return null;
        }
        final String keyed = key.toString();
        return containsKey(keyed) ? getFromCache(keyed) : getProperty(keyed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(final String key, final Object value) {
        throw new NotImplementedException("Values can't be set in preference bean maps. Use the PreferenceBean methods instead.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(final Object key) {
        throw new NotImplementedException("Values can't be removed in preference bean maps. Use the PreferenceBean methods instead.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(@Nonnull final Map<? extends String, ?> map) {
        throw new NotImplementedException("Values can't be set in preference bean maps. Use the PreferenceBean methods instead.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<String> keySet() {
        final Set<String> primaryKeys = new TreeSet<>();
        final Set<String> rawKeys = _preferenceService.getToolPropertyNames(getToolId());
        for (final String rawKey : rawKeys) {
            primaryKeys.add(getPreferencePrimaryKey(rawKey));
        }
        return primaryKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<Object> values() {
        throw new NotImplementedException("Values are not searchable or retrievable in preference bean maps.");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        final Map<String, Object> map = Maps.newHashMap();
        for (final String key : keySet()) {
            map.put(key, getProperty(key));
        }
        return map.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!getClass().isInstance(other)) {
            return false;
        }
        final AbstractPreferenceBean otherBean = (AbstractPreferenceBean) other;
        final EqualsBuilder builder = new EqualsBuilder();
        for (final String preference : keySet()) {
            builder.append(getProperty(preference), otherBean.getProperty(preference));
        }
        return builder.isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        for (final String preference : keySet()) {
            builder.append(getProperty(preference));
        }
        return builder.hashCode();
    }

    /**
     * Provides pre-processing functionality for bean initialization.
     */
    protected void preProcessPreferences() {
        _log.debug("Performing default preference pre-processing.");
    }

    /**
     * Provides post-processing functionality for bean initialization.
     */
    protected void postProcessPreferences() {
        _log.debug("Performing default preference post-processing.");
    }

    protected boolean isInitFromConfig() {
        return _initFromConfig;
    }

    protected String getNamespacedPropertyId(final String key, final String... names) {
        return Joiner.on(NAMESPACE_DELIMITER).join(Lists.asList(resolveAlias(key), names));
    }

    protected PreferenceInfo getPreferenceInfo(final String preference) {
        return _preferences.get(resolveAlias(preference));
    }

    protected <T> String serialize(final T instance) throws IOException {
        return getObjectMapper().writeValueAsString(instance);
    }

    protected <T> T deserialize(final String json, Class<? extends T> clazz) throws IOException {
        return getObjectMapper().readValue(json, clazz);
    }

    private <T> T deserialize(final String json, JavaType type) throws IOException {
        return getObjectMapper().readValue(json, type);
    }

    private NrgPreferenceBean getNrgPreferenceBean() {
        if (_annotation == null) {
            _annotation = findAnnotationInClassHierarchy(getClass(), NrgPreferenceBean.class);
        }
        return _annotation;
    }

    private String resolveAlias(final String preferenceName) {
        return _aliases.containsKey(preferenceName) ? _aliases.get(preferenceName) : preferenceName;
    }

    private Object getFromCache(final String key) {
        return super.get(key);
    }

    private void storeToCache(final String key, final Object value) {
        super.put(key, value);
    }

    private void removeFromCache(final String key) {
        super.remove(key);
    }

    private void processDefaultPreferences() {
        final Tool tool;
        if (!_preferenceService.getToolIds().contains(getToolId())) {
            tool = _preferenceService.createTool(this);
        } else {
            tool = _preferenceService.getTool(getToolId());
        }

        final String toolId = tool.getToolId();
        final List<String> preferenceIds = Lists.newArrayList();
        final Properties initializationProperties = getInitializationProperties();
        initializationProperties.putAll(resolvePreferenceAliases(_initPrefs.getPropertiesForNamespace(toolId)));
        final Properties overrideProperties = cleanOverrides(_initPrefs.getProperties("prefs-override"));

        if (_initPrefs.getProperties("prefs-init").size() > 0 || overrideProperties.size() > 0) {
            _initFromConfig = true;
        }

        if (!_preferences.isEmpty()) {
            if (_log.isInfoEnabled()) {
                _log.info("Found {} default values to add to tool {}", _preferences.size(), getToolId());
            }
            for (final String preference : _preferences.keySet()) {
                final PreferenceInfo info = _preferences.get(preference);

                if (info != null) {
                    // We'll take, in order of precedence, the override value, the initialization value, then the default value.
                    final String overrideValue = getOverrideValue(overrideProperties, info);
                    final String initializationValue = getOverrideValue(initializationProperties, info);
                    final boolean hasOverrideValue = overrideValue != null;
                    final boolean hasInitializationValue = initializationValue != null;

                    final String defaultValue = hasOverrideValue
                                                ? overrideValue
                                                : (hasInitializationValue
                                                   ? initializationValue
                                                   : info.getDefaultValue());

                    if (_log.isDebugEnabled()) {
                        final String message = hasOverrideValue
                                               ? "Found preference override value for property {} with value {}." :
                                               (hasInitializationValue
                                                ? "Found initialization override value for property {} with value {}."
                                                : "Setting property {} to default value of {}.");
                        _log.debug(message, preference, defaultValue);
                    }

                    try {
                        final Properties properties = convertValueForPreference(info, defaultValue);
                        preferenceIds.add(info.getName());

                        Object tempO = null;
                        try{
                            tempO = this.getPropertyNoCache(preference,null);
                        }
                        catch(Throwable e){
                        }

                        for (final String property : properties.stringPropertyNames()) {
                            if (!_preferenceService.hasPreference(getToolId(), property)) {
                                // If we have this preference, but it's under an alias...
                                final String alias = findAliasedPreference(property);
                                if (alias != null) {
                                    // Migrate it from the alias to the primary name.
                                    migrateAliasedPreference(alias, overrideValue);
                                } else {
                                    try {
                                        if(!property.contains(":") || !(tempO!=null && HashMap.class.isAssignableFrom(tempO.getClass()) && ((HashMap)tempO).size()>0)){
                                            //Don't add the default version of this property is there is already a non-empty map of properties for it
                                            create(properties.getProperty(property), property);
                                        }
                                    } catch (InvalidPreferenceName invalidPreferenceName) {
                                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to create the " + info + " preference for the " + getToolId() + " tool.");
                                    }
                                }
                            } else if (hasOverrideValue && !StringUtils.equals(overrideValue, _preferenceService.getPreferenceValue(getToolId(), preference))) {
                                try {
                                    _preferenceService.setPreferenceValue(getToolId(), preference, overrideValue);
                                } catch (InvalidPreferenceName invalidPreferenceName) {
                                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to set the " + property + " value for the " + getToolId() + " tool to the override value " + overrideValue + ".");
                                }
                            }
                        }
                    } catch (JsonParseException e) {
                        final String message = "An error occurred parsing the JSON string: " + defaultValue;
                        _log.error(message);
                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, message, e);
                    } catch (JsonMappingException e) {
                        final String message = "An error occurred mapping the JSON string: " + defaultValue;
                        _log.error(message);
                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, message, e);
                    } catch (IOException e) {
                        final String message = "An unknown error occurred processing the JSON string: " + defaultValue;
                        _log.error(message);
                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, message, e);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "An error occurred invoking a method", e);
                    }
                    if (_log.isDebugEnabled()) {
                        _log.debug(" * {}: {}", preference, info);
                    }
                } else if (_log.isDebugEnabled()) {
                    _log.debug(" * {}: No default value specified", preference);
                }
            }
        }
        if (initializationProperties.size() > 0) {
            if (tool.isStrict()) {
                _log.warn("Extra initialization properties found, but tool {} is set to strict. The following preferences are being ignored: {}",
                          tool.getToolId(),
                          Joiner.on(", ").join(initializationProperties.stringPropertyNames()));
            } else {
                for (final String property : initializationProperties.stringPropertyNames()) {
                    if (!_preferenceService.hasPreference(getToolId(), property)) {
                        final String value = initializationProperties.getProperty(property);
                        _preferenceService.create(tool.getToolId(), property, value);
                        preferenceIds.add(property);
                        _log.info("Created a new preference entry from the initialization settings for tool {} with the name {} set to value {}.", tool.getToolId(), property, value);
                    }
                }
            }
        }
        if (overrideProperties.size() > 0) {
            if (tool.isStrict()) {
                _log.warn("Extra override properties found, but tool {} is set to strict. The following preferences are being ignored: {}",
                          tool.getToolId(),
                          Joiner.on(", ").join(overrideProperties.stringPropertyNames()));
            } else {
                for (final String property : overrideProperties.stringPropertyNames()) {
                    final String value = overrideProperties.getProperty(property);
                    if (!_preferenceService.hasPreference(getToolId(), property)) {
                        _preferenceService.create(tool.getToolId(), property, value);
                        preferenceIds.add(property);
                        _log.info("Created a new preference entry from the override settings for tool {} with the name {} set to value {}.", tool.getToolId(), property, value);
                    } else {
                        try {
                            if (!StringUtils.equals(_preferenceService.getPreferenceValue(tool.getToolId(), property), value)) {
                                _preferenceService.setPreferenceValue(tool.getToolId(), property, value);
                                preferenceIds.add(property);
                                _log.info("Updated a preference entry from the override settings for tool {} with the name {} set to value {}.", tool.getToolId(), property, value);
                            }
                        } catch (InvalidPreferenceName invalidPreferenceName) {
                            _log.error("Attempted to override the value for tool {} preference {} with the value {}, but something went wrong.", tool.getToolId(), property, value);
                        }
                    }
                }
            }
        }
        // This initializes the preference bean map to allow using that rather than reflected proxied calls to the
        // bean methods later, which is time consuming.
        for (final String preferenceId : preferenceIds) {
            getProperty(preferenceId);
        }
    }

    private Properties cleanOverrides(final Properties properties) {
        final Properties clean = new Properties();
        final int prefixSize = getToolId().length() + 1;
        for (final Map.Entry<?, ?> property : properties.entrySet()) {
            clean.setProperty(StringUtils.substring((String) property.getKey(), prefixSize), (String) property.getValue());
        }
        return clean;
    }

    private String getOverrideValue(final Properties overrideProperties, final PreferenceInfo info) {
        if (overrideProperties.containsKey(info.getName())) {
            return (String) overrideProperties.remove(info.getName());
        }
        for (final String alias : info.getAliases()) {
            if (overrideProperties.containsKey(alias)) {
                return (String) overrideProperties.remove(alias);
            }
        }
        return null;
    }

    private String findAliasedPreference(final String property) {
        if (!_aliasedPreferences.keySet().contains(property)) {
            return null;
        }
        for (final String alias : _aliasedPreferences.get(property)) {
            if (_preferenceService.hasPreference(getToolId(), alias)) {
                return alias;
            }
        }
        return null;
    }

    private void migrateAliasedPreference(final String alias, final String overrideValue) {
        if (_aliases.containsKey(alias)) {
            final String property = getPreferenceInfo(alias).getProperty();
            final Preference preference = _preferenceService.migrate(getToolId(), alias, property);
            final String value = preference.getValue();
            if (overrideValue != null && !StringUtils.equals(value, overrideValue)) {
                try {
                    _preferenceService.setPreferenceValue(getToolId(), property, overrideValue);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to override the value of the " + property + " preference for the " + getToolId() + " tool.");
                }
            }
        }
    }

    private Properties getInitializationProperties() {
        // Create the properties object.
        final Properties properties = new Properties();

        // Check for the tool-specific properties file.
        final String propertiesFile = StringUtils.isNotBlank(_annotation.properties()) ? _annotation.properties() : propertize(getClass().getName());
        final File file = _configFolderPaths.findFile(propertiesFile);
        if (file != null && file.exists() && file.isFile()) {
            try (final InputStream input = new FileInputStream(file)) {
                properties.load(input);
                return resolvePreferenceAliases(properties);
            } catch (FileNotFoundException ignored) {
                // Nothing to do here: we've already checked that the file exists.
            } catch (IOException e) {
                _log.warn("An error occurred attempting to read the file " + file.getAbsolutePath(), e);
            }
        }

        return properties;
    }

    private Properties resolvePreferenceAliases(final Properties properties) {
        for (final String property : properties.stringPropertyNames()) {
            if (_aliases.containsKey(property)) {
                properties.setProperty(getPreferenceInfo(property).getProperty(), properties.getProperty(property));
                properties.remove(property);
            }
        }
        return properties;
    }

    private String propertize(final String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        final StringBuilder buffer = new StringBuilder();
        for (int index = 0; index < name.length(); index++) {
            final char character = name.charAt(index);
            if (Character.isLetter(character)) {
                if (Character.isLowerCase(character)) {
                    buffer.append(character);
                } else {
                    if (buffer.length() > 0) {
                        buffer.append('-');
                    }
                    buffer.append(Character.toLowerCase(character));
                }
            }
        }
        return buffer.append(".properties").toString();
    }

    private Properties convertValueForPreference(final PreferenceInfo info, final String value) throws IOException, IllegalAccessException, InvocationTargetException {
        final Properties properties = new Properties();

        // TODO: For now creates a site-wide preference only.
        final Class<?> valueType = info.getValueType();
        final Class<?> itemType = info.getItemType();
        final String key = info.getKey();

        final boolean isArray = valueType.isArray();
        final boolean isList = List.class.isAssignableFrom(valueType);
        final boolean isMap = Map.class.isAssignableFrom(valueType);

        // For persistence purposes, we treat arrays and lists the same.
        if (isArray || isList) {
            @SuppressWarnings("unchecked") final CollectionType listType = getTypeFactory().constructCollectionType((Class<? extends List>) valueType, itemType);
            if (!BeanUtils.isSimpleValueType(itemType)) {
                if (StringUtils.isBlank(key)) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "When specifying an array or list of complex types as a preference setting, you must also specify the key property on the complex type to use to store the preference data, e.g. 'key=\"id\", where 'id' corresponds to a 'getId()' method on the complex type.");
                }
                final List<?> list = deserialize(StringUtils.defaultIfBlank(value, "[]"), listType);
                final String getterName = "get" + StringUtils.capitalize(key);
                final Method getter;
                try {
                    getter = itemType.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preference " + info + " specifies a key " + key + " that doesn't exist on the object type.");
                }
                for (final Object item : list) {
                    final String keyValue = getter.invoke(item).toString();
                    final String propertyId = getNamespacedPropertyId(info.getProperty(), keyValue);
                    properties.setProperty(propertyId, _mapper.writeValueAsString(item));
                }
            } else {
                properties.setProperty(info.getProperty(), value);
            }
        } else if (isMap) {
            @SuppressWarnings("unchecked") final MapType mapType = getTypeFactory().constructMapType((Class<? extends Map>) valueType, String.class, itemType);
            final Map<String, ?> map = deserialize(StringUtils.defaultIfBlank(value, "{}"), mapType);
            if (!BeanUtils.isSimpleValueType(itemType)) {
                if (StringUtils.isBlank(key)) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "When specifying a map of complex types as a preference setting, you must also specify the key property on the complex type to use to store the preference data, e.g. 'key=\"id\", where 'id' corresponds to a 'getId()' method on the complex type.");
                }
                final String getterName = "get" + StringUtils.capitalize(key);
                try {
                    itemType.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preference " + info + " specifies a key " + key + " that doesn't exist on the object type.");
                }
            }
            for (final String mapKey : map.keySet()) {
                final String propertyId = getNamespacedPropertyId(info.getProperty(), mapKey);
                properties.setProperty(propertyId, _mapper.writeValueAsString(map.get(mapKey)));
            }
        } else {
            properties.setProperty(info.getProperty(), value);
        }

        return properties;
    }

    private static ObjectMapper getObjectMapper() {
        return _mapper;
    }

    private static TypeFactory getTypeFactory() {
        return _typeFactory;
    }

    private static String getPreferencePrimaryKey(final String preferenceName) {
        if (StringUtils.isBlank(preferenceName)) {
            return "";
        }
        final String[] atoms = preferenceName.split(NAMESPACE_DELIMITER, 2);
        return atoms[0];
    }

    private static String getPreferenceSubkey(final String preferenceName) {
        if (StringUtils.isBlank(preferenceName)) {
            return "";
        }
        final String[] atoms = preferenceName.split(NAMESPACE_DELIMITER, 2);
        return atoms.length == 1 ? "" : atoms[1];
    }

    private static final Logger       _log    = LoggerFactory.getLogger(AbstractPreferenceBean.class);
    private static final ObjectMapper _mapper = new ObjectMapper() {{
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }};

    private static boolean isPrimative (Class<?> valueType){
        final String valName = valueType.getName();
        if(valueType.equals(String.class) || valueType.equals(Integer.class) || valueType.equals(Double.class) || valueType.equals(Float.class) || valueType.equals(Long.class) || valueType.equals(Boolean.class) ||
                valName.equals("boolean") || valName.equals("int") || valName.equals("long") || valName.equals("float") || valName.equals("double")) {
            return true;
        }
        else{
            return false;
        }
    }

    private static final TypeFactory _typeFactory = _mapper.getTypeFactory();

    private       NrgPreferenceService _preferenceService;
    private final ConfigPaths          _configFolderPaths;
    private final OrderedProperties    _initPrefs;

    private final Map<String, PreferenceInfo> _preferences        = new HashMap<>();
    private final Map<String, String>         _aliases            = new HashMap<>();
    private final Map<String, List<String>>   _aliasedPreferences = new HashMap<>();
    private final Map<String, Method>         _methods            = new HashMap<>();

    private boolean _resolverInitialized = false;
    private boolean _initFromConfig      = false;

    private NrgPreferenceBean                         _annotation;
    private String                                    _toolId;
    private Class<? extends PreferenceEntityResolver> _resolver;
}
