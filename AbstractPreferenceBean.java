package org.nrg.prefs.beans;

import com.fasterxml.jackson.core.JsonParseException;
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
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Predicates.*;
import static org.reflections.ReflectionUtils.withAnnotation;

public abstract class AbstractPreferenceBean implements PreferenceBean {

    @Inject
    public void setService(final NrgPreferenceService service) {
        _service = service;
    }

    @Override
    public PreferenceBean initialize(final NrgPreferenceService service) {
        _service = service;
        getDefaultPreferences();
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
    public <T> Map<String, T> getMapValue(final String preferenceName) throws UnknownToolId {
        return getMapValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName);
    }

    @Override
    public <T> Map<String, T> getMapValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId {
        final PreferenceInfo info = _preferences.get(preferenceName);
        @SuppressWarnings("unchecked") final MapType mapType = getTypeFactory().constructMapType((Class<? extends Map>) info.getValueType(), String.class, info.getItemType());
        try {
            final Map<String, Object> map = deserialize("{}", mapType);
            final Set<String> propertyNames = Sets.filter(_service.getToolPropertyNames(getToolId()), or(equalTo(preferenceName), containsPattern("^" + preferenceName + "\\.")));
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
                final Set<String> propertyNames = Sets.filter(_service.getToolPropertyNames(getToolId()), or(equalTo(preferenceName), containsPattern("^" + preferenceName + "\\.")));
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
    public void set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys), value);
    }

    @Override
    public void set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        _service.setPreferenceValue(getToolId(), getNamespacedPropertyId(key, subkeys), scope, entityId, value);
    }

    @Override
    public <T> void setMapValue(final String preferenceName, Map<String, T> map) throws UnknownToolId {
        setMapValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, map);
    }

    @Override
    public <T> void setMapValue(final Scope scope, final String entityId, final String preferenceName, Map<String, T> map) throws UnknownToolId {
        for (final String key : map.keySet()) {
            final String id = getNamespacedPropertyId(preferenceName, key);
            try {
                set(scope, entityId, serialize(map.get(key)), id);
            } catch (IOException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
            } catch (InvalidPreferenceName e) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "An error occurred trying to set the preference: couldn't find the preference identified by the ID " + id);
            }
        }
    }

    @Override
    public <T> void setListValue(final String preferenceName, List<T> list) throws UnknownToolId {
        setListValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, list);
    }

    @Override
    public <T> void setListValue(final Scope scope, final String entityId, final String preferenceName, List<T> list) throws UnknownToolId {
    }

    @Override
    public <T> void setArrayValue(final String preferenceName, T[] array) throws UnknownToolId {
        setArrayValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, array);
    }

    @Override
    public <T> void setArrayValue(final Scope scope, final String entityId, final String preferenceName, T[] array) throws UnknownToolId {
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
        if (_preferences.isEmpty()) {
            initializeDefaultPreferences();
        }
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

    private ObjectMapper getObjectMapper() {
        return _mapper;
    }

    private TypeFactory getTypeFactory() {
        if (_typeFactory == null) {
            _typeFactory = getObjectMapper().getTypeFactory();
        }
        return _typeFactory;
    }

    private void initializeDefaultPreferences() {
        _preferences.clear();
        @SuppressWarnings("unchecked") final Set<Method> properties = ReflectionUtils.getAllMethods(getClass(), withAnnotation(NrgPreference.class));
        for (final Method method : properties) {
            final NrgPreference annotation = method.getAnnotation(NrgPreference.class);
            final String name;
            final Class<?> type;
            final Type genericType;
            if (isGetter(method)) {
                name = propertize(method.getName(), "get");
                type = method.getReturnType();
                genericType = method.getGenericReturnType();
            } else if (isSetter(method)) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can't annotate the " + method.getName() + "() method with " + parameterTypes.length + " parameters: it must have one and only one parameter.");
                }
                name = propertize(method.getName(), "set");
                type = parameterTypes[0];
                genericType = method.getGenericParameterTypes()[0];
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The" + method.getName() + "() method doesn't appear to be a getter or a setter, but is annotated anyway. Only getter and setter methods should be annotated.");
            }

            final boolean isArray = type.isArray();
            final boolean isList = List.class.isAssignableFrom(type);
            final boolean isMap = Map.class.isAssignableFrom(type);

            // If this is a list or a map, then the type should be the type of map and the generic type should be the
            // parameterized type. If they're equal, that means it's just a List or Map with no type set, which means we
            // can't determine what type of object is inside the list or map.
            if ((isList || isMap) && type.equals(genericType)) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method must use a parameterized " + (isList ? "list" : "map") + " type so that I can determine the type of preference in the collection.");
            }

            final PreferenceInfo info = new PreferenceInfo();
            info.setName(name);
            info.setValueType(type);
            info.setKey(annotation.key());
            info.setDefaultValue(annotation.defaultValue());

            if (isArray || isList) {
                final Class<?> itemType;
                try {
                    itemType = isArray ? type.getComponentType() : getClassesFromParameterizedType(genericType).get(0);
                } catch (NotParameterizedTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a list that is not parameterized, i.e. is just a List rather than List<String>. This is not currently supported.");
                } catch (NotConcreteTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a list that is parameterized to contain a parameterized type, e.g. List<List<String>>. This is not currently supported.");
                }
                if (!BeanUtils.isSimpleValueType(itemType) && StringUtils.isBlank(annotation.key())) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "When specifying an array or list of complex types as a preference setting, you must also specify the key property on the complex type to use to store the preference data, e.g. 'key=\"id\", where 'id' corresponds to a 'getId()' method on the complex type.");
                }
                info.setItemType(itemType);
            } else if (isMap) {
                final List<Class<?>> classes;
                try {
                    classes = getClassesFromParameterizedType(genericType);
                } catch (NotParameterizedTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a map that is not parameterized, i.e. is just a Map rather than Map<String, String>. This is not currently supported.");
                } catch (NotConcreteTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a map that is parameterized to contain a parameterized type, e.g. Map<String, List<String>>. This is not currently supported.");
                }
                final Class<?> keyType = classes.get(0);
                if (!keyType.equals(String.class)) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a map that uses a non-String key type. This is not currently supported.");
                }
                final Class<?> valueType = classes.get(1);
                info.setItemType(valueType);
            } else {
                info.setItemType(null);
            }
            _preferences.put(name, info);
        }
    }

    private void processDefaultPreferences() {
        if (!_preferences.isEmpty()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found {} default values to add to tool {}", _preferences.size(), getToolId());
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
                                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preference " + info.getName() + " specifies a key " + key + " that doesn't exist on the object type.");
                                }
                                for (final Object item : list) {
                                    final String keyValue = getter.invoke(item).toString();
                                    _service.setPreferenceValue(getToolId(), getNamespacedPropertyId(info.getName(), keyValue), getObjectMapper().writeValueAsString(item));
                                }
                            } else {
                                _service.setPreferenceValue(getToolId(), info.getName(), defaultValue);
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
                                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preference " + info.getName() + " specifies a key " + key + " that doesn't exist on the object type.");
                                }
                            }
                            for (final String mapKey : map.keySet()) {
                                _service.setPreferenceValue(getToolId(), getNamespacedPropertyId(info.getName(), mapKey), getObjectMapper().writeValueAsString(map.get(mapKey)));
                            }
                        } else {
                            _service.setPreferenceValue(getToolId(), info.getName(), defaultValue);
                        }
                    } catch (InvalidPreferenceName ignored) {
                        // This shouldn't happen: we're creating new preferences from the _preferences that define the list of acceptable preferences.
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

    private List<Class<?>> getClassesFromParameterizedType(final Type type) throws NotParameterizedTypeException, NotConcreteTypeException {
        if (!(type instanceof ParameterizedType)) {
            throw new NotParameterizedTypeException("The type " + type.toString() + " is not a parameterized type");
        }
        final List<Class<?>> classes = new ArrayList<>();
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        for (final Type subtype : parameterizedType.getActualTypeArguments()) {
            if (subtype instanceof ParameterizedType) {
                throw new NotParameterizedTypeException("The type " + type.toString() + " can not be a parameterized type");
            }
            classes.add((Class<?>) subtype);
        }
        return classes;
    }

    protected String getNamespacedPropertyId(final String key, final String... names) {
        return Joiner.on(".").join(Lists.asList(key, names));
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

    private static String getPreferenceSubkey(final String preferenceName) {
        if (StringUtils.isBlank(preferenceName)) {
            return "";
        }
        final String[] atoms = preferenceName.split("\\.", 2);
        return atoms.length == 1 ? "" : atoms[1];
    }

    private class NotParameterizedTypeException extends Throwable {
        NotParameterizedTypeException(final String message) {
            super(message);
        }
    }

    private class NotConcreteTypeException extends Throwable {
        public NotConcreteTypeException(final String message) {
            super(message);
        }
    }

    private static final Logger       _log         = LoggerFactory.getLogger(AbstractPreferenceBean.class);
    private static final Pattern PATTERN_GETTER = Pattern.compile("^get[A-Z][A-z]+");

    private static final Pattern PATTERN_SETTER = Pattern.compile("^set[A-Z][A-z]+");

    @Inject
    private ObjectMapper _mapper;

    private TypeFactory  _typeFactory;

    private NrgPreferenceService _service;

    private final Map<String, PreferenceInfo> _preferences = new HashMap<>();

    private String _toolId;
    private boolean _resolverInitialized = false;
    private Class<? extends PreferenceEntityResolver> _resolver;
}
