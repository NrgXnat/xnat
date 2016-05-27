package org.nrg.xdat.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.services.NrgEventService;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.XDAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class EventTriggeringAbstractPreferenceBean extends AbstractPreferenceBean {

    private void triggerEventIfChanging(final String namespacedPropertyId, final String oldValue, final String newValue){
        if(!StringUtils.equals(oldValue,newValue)) { //Check if value is being changed.
            _eventService.triggerEvent(new PreferenceEvent(namespacedPropertyId, newValue));
        }
    }

    @JsonIgnore
    @Override
    public void set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        final String namespacedPropertyId = getNamespacedPropertyId(key, subkeys);
        String oldValue = getValue(namespacedPropertyId);
        if (_preferences.containsKey(namespacedPropertyId)) {
            try {
                final Properties existing   = _service.getToolProperties(getToolId(), Collections.singletonList(namespacedPropertyId));
                final Properties properties = convertValueForPreference(_preferences.get(namespacedPropertyId), value);
                for (final String property : properties.stringPropertyNames()) {
                    _service.setPreferenceValue(getToolId(), property, scope, entityId, properties.getProperty(property));
                    if (existing.containsKey(property)) {
                        existing.remove(property);
                    }
                }
            } catch (IOException | IllegalAccessException | InvocationTargetException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to set the " + namespacedPropertyId + " preference setting.", e);
            }
        } else {
            _service.setPreferenceValue(getToolId(), namespacedPropertyId, scope, entityId, value);
        }
        triggerEventIfChanging(namespacedPropertyId, oldValue, value);
    }

    @JsonIgnore
    @Override
    public void setBooleanValue(final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
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
        setDateValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), value, key, subkeys);
    }

    @JsonIgnore
    @Override
    public void setDateValue(final Scope scope, final String entityId, final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName {
        set(scope, entityId, Long.toString(value.getTime()), key, subkeys);
    }

    @JsonIgnore
    @Override
    public <T> void setMapValue(final String preferenceName, Map<String, T> map) throws UnknownToolId, InvalidPreferenceName {
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
        setListValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, list);
    }

    @JsonIgnore
    @Override
    public <T> void setListValue(final Scope scope, final String entityId, final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, preferenceName, serialize(list));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    @JsonIgnore
    @Override
    public <T> void setArrayValue(final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName {
        setArrayValue(EntityId.Default.getScope(), EntityId.Default.getEntityId(), preferenceName, array);
    }

    @JsonIgnore
    @Override
    public <T> void setArrayValue(final Scope scope, final String entityId, final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName {
        try {
            set(scope, entityId, preferenceName, serialize(array));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred during serialization/deserialization", e);
        }
    }

    private static final Logger       _log    = LoggerFactory.getLogger(EventTriggeringAbstractPreferenceBean.class);
    private static final ObjectMapper _mapper = new ObjectMapper() {{
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }};

    @Inject
    private NrgPreferenceService _service;

    private final Map<String, PreferenceInfo> _preferences = new HashMap<>();

    @Lazy
    @Autowired
    private NrgEventService _eventService;
}
