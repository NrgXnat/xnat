package org.nrg.xnat.customforms.events;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class CustomFormEvent implements  CustomFormEventI {

    public static CustomFormEvent.Builder builder() {
        return new CustomFormEvent.Builder();
    }

    @SuppressWarnings("unused")
    public static class Builder {
        public CustomFormEvent.Builder xsiType(final String xsiType) {
            if (StringUtils.isNotBlank(_xsiType)) {
                throw new RuntimeException("You can only set the XSI type along with the UUIID for a single-item event, but the " + _xsiType + " data type is already set for this builder.");
            }
            _xsiType = xsiType;
            return this;
        }

        public CustomFormEvent.Builder uuid(final String uuid) {
            _uuid = uuid;
            return this;
        }

        public CustomFormEvent.Builder action(final String action) {
            _action = action;
            return this;
        }


        public CustomFormEvent.Builder property(final String property, final Object value) {
            _properties.put(property, value);
            return this;
        }

        public CustomFormEvent.Builder properties(final Map<String, ?> properties) {
            _properties.putAll(properties);
            return this;
        }

        public CustomFormEvent build() {
            return new CustomFormEvent(_xsiType, _uuid, _action, _properties);
        }

        private final Map<String, Object> _properties = new HashMap<>();
        private       String              _xsiType;
        private       String               _uuid;
        private       String             _action;
    }

    /**
     * Instantiates a new CustomFormEvent event.
     *
     * @param xsiType   The  affected by the event.
     * @param uuid      The UUID identifying the event.
     * @param action The action that triggered this event.
     */
    public CustomFormEvent(final String xsiType, final String uuid, final String action, final Map<String, Object> properties) {
        if (StringUtils.isBlank(action)) {
            throw new RuntimeException("You can't have an event without an action.");
        }

        _xsiType = xsiType;
        _uuid = uuid;
        _action = action;
        _properties = ImmutableMap.copyOf(ObjectUtils.getIfNull(properties, Collections::emptyMap));
    }


    @Override
    public String toString() {
        return "Action '" + getAction() + "' on " + String.format("%1$s/UUID=%2$s", getXsiType(), getUuid());
   }

    private final Map<String, Object> _properties;
    private       String              _xsiType;
    private       String                _uuid;
    private       String             _action;



}
