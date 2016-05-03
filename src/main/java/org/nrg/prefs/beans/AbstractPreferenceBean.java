package org.nrg.prefs.beans;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceBeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.base.Predicates.*;

public abstract class AbstractPreferenceBean implements PreferenceBean {

    /**
     * Default constructor initializes default preferences for the bean.
     */
    protected AbstractPreferenceBean() {
        _preferences.clear();
        _preferences.putAll(PreferenceBeanHelper.getPreferenceInfoMap(getClass()));
    }

    @PostConstruct
    public PreferenceBean initialize() {
        if (_service == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "The NrgPreferenceService instance must be configured and wired before this preference bean can be initialized.");
        }
        processDefaultPreferences();
        return this;
    }

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

    @Override
    public Set<String> getPreferenceKeys() {
        return _service.getToolPropertyNames(getToolId());
    }

    @Override
    public Properties getPreferencesAsProperties() {
        return _service.getToolProperties(getToolId());
    }

    @Override
    public final Class<? extends PreferenceEntityResolver> getResolver() {
        if (!_resolverInitialized) {
            if (getClass().isAnnotationPresent(NrgPreferenceBean.class)) {
                final Class<? extends PreferenceEntityResolver>[] resolvers = getClass().getAnnotation(NrgPreferenceBean.class).resolver();
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

    @Override
    public Preference get(final String key, final String... subkeys) throws UnknownToolId {
        return _service.getPreference(getToolId(), getNamespacedPropertyId(key, subkeys));
    }

    @Override
    public Preference get(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        return _service.getPreference(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId);
    }

    @Override
    public String getValue(final String key, final String... subkeys) throws UnknownToolId {
        return _service.getPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys));
    }

    @Override
    public String getValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        return _service.getPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId);
    }

    @Override
    public Boolean getBooleanValue(final String key, final String... subkeys) throws UnknownToolId {
        return getBooleanValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @Override
    public Boolean getBooleanValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public Integer getIntegerValue(final String key, final String... subkeys) throws UnknownToolId {
        return getIntegerValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @Override
    public Integer getIntegerValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    @Override
    public Long getLongValue(final String key, final String... subkeys) throws UnknownToolId {
        return getLongValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @Override
    public Long getLongValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Override
    public Float getFloatValue(final String key, final String... subkeys) throws UnknownToolId {
        return getFloatValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @Override
    public Float getFloatValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Float.parseFloat(value);
    }

    @Override
    public Double getDoubleValue(final String key, final String... subkeys) throws UnknownToolId {
        return getDoubleValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @Override
    public Double getDoubleValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (value == null) {
            return null;
        }
        return Double.parseDouble(value);
    }

    @Override
    public Date getDateValue(final String key, final String... subkeys) throws UnknownToolId {
        return getDateValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), key, subkeys);
    }

    @Override
    public Date getDateValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId {
        final String value = getValue(scope, entityId, key, subkeys);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        final Long date = Long.getLong(value);
        if (date == null) {
            _log.error("The value for the date preference {} is a non-blank but invalid value: {}. It should be stored as a long that can be translated into a date object.", getNamespacedPropertyId(key, subkeys), value);
            return null;
        }
        return new Date(date);
    }

    @Override
    public <T> Map<String, T> getMapValue(final String preferenceName) throws UnknownToolId {
        return getMapValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @Override
    public <T> Map<String, T> getMapValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final PreferenceInfo info = _preferences.get(preferenceName);
        @SuppressWarnings("unchecked") final MapType mapType = getTypeFactory().constructMapType((Class<? extends Map>) info.getValueType(), String.class, info.getItemType());
        try {
            final Map<String, Object> map = deserialize("{}", mapType);
            final Set<String> propertyNames = Sets.filter(_service.getToolPropertyNames(getToolId()), or(equalTo(preferenceName), containsPattern("^" + preferenceName + NAMESPACE_DELIMITER)));
            for (final String propertyName : propertyNames) {
                final String value = _service.getPreferenceValue(getToolId(), propertyName);
                @SuppressWarnings("unchecked") final Object item = deserialize(value, info.getItemType());
                map.put(getPreferenceSubkey(propertyName), item);
            }
            //noinspection unchecked
            return (Map<String, T>) map;
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @Override
    public <T> List<T> getListValue(final String preferenceName) throws UnknownToolId {
        return getListValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @Override
    public <T> List<T> getListValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final PreferenceInfo info = _preferences.get(preferenceName);
        @SuppressWarnings("unchecked") final CollectionType listType = getTypeFactory().constructCollectionType((Class<? extends List>) info.getValueType(), info.getItemType());
        try {
            if (BeanUtils.isSimpleValueType(info.getItemType())) {
                final String value = _service.getPreferenceValue(getToolId(), preferenceName);
                return deserialize(value, listType);
            } else {
                final List<T> list = deserialize("[]", listType);
                final Set<String> propertyNames = Sets.filter(_service.getToolPropertyNames(getToolId()), or(equalTo(preferenceName), containsPattern("^" + preferenceName + NAMESPACE_DELIMITER)));
                for (final String propertyName : propertyNames) {
                    final String value = _service.getPreferenceValue(getToolId(), propertyName);
                    @SuppressWarnings("unchecked") final T item = deserialize(value, (Class<? extends T>) info.getItemType());
                    list.add(item);
                }
                return list;
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @Override
    public <T> T[] getArrayValue(final String preferenceName) throws UnknownToolId {
        return getArrayValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    @Override
    public <T> T[] getArrayValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final List<T> list = getListValue(scope, entityId, preferenceName);
        return (T[]) list.toArray(new Object[list.size()]);
    }

    @Override
    public void create(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _service.create(getToolId(), getNamespacedPropertyId(key, subkeys), value);
    }

    @Override
    public void create(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _service.create(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId, value);
    }

    @Override
    public void set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys), value);
    }

    @Override
    public void set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId, value);
    }

    @Override
    public void setBooleanValue(final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        setBooleanValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public void setBooleanValue(final Scope scope, final String entityId, final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @Override
    public void setIntegerValue(final Integer value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        setIntegerValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public void setIntegerValue(final Scope scope, final String entityId, final Integer value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @Override
    public void setLongValue(final Long value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        setLongValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public void setLongValue(final Scope scope, final String entityId, final Long value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @Override
    public void setFloatValue(final Float value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        setFloatValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public void setFloatValue(final Scope scope, final String entityId, final Float value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @Override
    public void setDoubleValue(final Double value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        setDoubleValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public void setDoubleValue(final Scope scope, final String entityId, final Double value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, value.toString(), key, subkeys);
    }

    @Override
    public void setDateValue(final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        setDateValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @Override
    public void setDateValue(final Scope scope, final String entityId, final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, Long.toString(value.getTime()), key, subkeys);
    }

    @Override
    public <T> void setMapValue(final String preferenceName, Map<String, T> map) throws UnknownToolId, InvalidPreferenceName {
        setMapValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, map);
    }

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

    @Override
    public <T> void setListValue(final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName {
        setListValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, list);
    }

    @Override
    public <T> void setListValue(final Scope scope, final String entityId, final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, preferenceName, serialize(list));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @Override
    public <T> void setArrayValue(final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName {
        setArrayValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, array);
    }

    @Override
    public <T> void setArrayValue(final Scope scope, final String entityId, final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, preferenceName, serialize(array));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @Override
    public void delete(final String key, final String... subkeys) throws InvalidPreferenceName {
        _service.deletePreference(getToolId(), getNamespacedPropertyId(key, subkeys));
    }

    @Override
    public void delete(final Scope scope, final String entityId, final String key, final String... subkeys) throws InvalidPreferenceName {
        _service.deletePreference(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId);
    }

    @Override
    public Map<String, PreferenceInfo> getDefaultPreferences() {
        return _preferences;
    }

    protected <T> T deserialize(final String json, Class<? extends T> clazz) throws IOException {
        return getObjectMapper().readValue(json, clazz);
    }

    private <T> T deserialize(final String json, JavaType type) throws IOException {
        return getObjectMapper().readValue(json, type);
    }

    protected <T> String serialize(final T instance) throws IOException {
        return getObjectMapper().writeValueAsString(instance);
    }

    private static ObjectMapper getObjectMapper() {
        return _mapper;
    }

    private static TypeFactory getTypeFactory() {
        return _typeFactory;
    }

    private void processDefaultPreferences() {
        if (!_service.getToolIds().contains(getToolId())) {
            try {
                _service.createTool(this);
            } catch (InvalidPreferenceName invalidPreferenceName) {
                _log.error("Invalid preference name error", invalidPreferenceName);
            }
        }
        if (!_preferences.isEmpty()) {
            if (_log.isInfoEnabled()) {
                _log.info("Found {} default values to add to tool {}", _preferences.size(), getToolId());
            }
            for (final String preference : _preferences.keySet()) {
                final PreferenceInfo info = _preferences.get(preference);
                if (info != null) {
                    final String defaultValue = info.getDefaultValue();
                    try {
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
                                final List<?> list = deserialize(defaultValue, listType);
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
                                    if (!_service.hasPreference(getToolId(), propertyId)) {
                                        try {
                                            create(_mapper.writeValueAsString(item), propertyId);
                                        } catch (InvalidPreferenceName invalidPreferenceName) {
                                            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to create the " + propertyId + " preference for the " + getToolId() + " tool.");
                                        }
                                    }
                                }
                            } else {
                                if (!_service.hasPreference(getToolId(), info.getProperty())) {
                                    try {
                                        create(defaultValue, info.getProperty());
                                    } catch (InvalidPreferenceName invalidPreferenceName) {
                                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to create the " + info + " preference for the " + getToolId() + " tool.");
                                    }
                                }
                            }
                        } else if (isMap) {
                            @SuppressWarnings("unchecked") final MapType mapType = getTypeFactory().constructMapType((Class<? extends Map>) valueType, String.class, itemType);
                            final Map<String, ?> map = deserialize(defaultValue, mapType);
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
                                if (!_service.hasPreference(getToolId(), propertyId)) {
                                    try {
                                        create(_mapper.writeValueAsString(map.get(mapKey)), propertyId);
                                    } catch (InvalidPreferenceName invalidPreferenceName) {
                                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to create the " + propertyId + " preference for the " + getToolId() + " tool.");
                                    }
                                }
                            }
                        } else {
                            if (!_service.hasPreference(getToolId(), info.getProperty())) {
                                try {
                                    create(defaultValue, info.getProperty());
                                } catch (InvalidPreferenceName invalidPreferenceName) {
                                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Something went wrong trying to create the " + info + " preference for the " + getToolId() + " tool.");
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
    }

    protected String getNamespacedPropertyId(final String key, final String... names) {
        return Joiner.on(NAMESPACE_DELIMITER).join(Lists.asList(key, names));
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

    private static final TypeFactory _typeFactory = _mapper.getTypeFactory();

    @Inject
    private NrgPreferenceService _service;

    private final Map<String, PreferenceInfo> _preferences = new HashMap<>();
    private String _toolId;
    private boolean _resolverInitialized = false;
    private Class<? extends PreferenceEntityResolver> _resolver;
}
