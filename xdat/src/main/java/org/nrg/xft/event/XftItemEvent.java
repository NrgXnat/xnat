/*
 * core: org.nrg.xft.event.XftItemEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class XftItemEvent.
 */
@Getter
@Accessors(prefix = "_")
@Slf4j
public class XftItemEvent implements XftItemEventI {
    private static final long serialVersionUID = -9167640746375952254L;

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unused")
    public static class Builder {
        public Builder xsiType(final String xsiType) {
            if (StringUtils.isNotBlank(_xsiType)) {
                throw new RuntimeException("You can only set the XSI type along with the ID for a single-item event, but the " + _xsiType + " data type is already set for this builder.");
            }
            _xsiType = xsiType;
            return this;
        }

        public Builder id(final String id) {
            _ids.add(id);
            return this;
        }

        public Builder action(final String action) {
            _action = action;
            return this;
        }

        public Builder ids(final List<String> ids) {
            _ids.addAll(ids);
            return this;
        }

        public Builder item(final XFTItem item) {
            try {
                _items.add(item);
                xsiType(item.getXSIType());
                id(StringUtils.defaultIfBlank(item.getIDValue(), item.getPKValueString()));
                return this;
            } catch (XFTInitException | ElementNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public Builder items(final List<XFTItem> items) {
            items.forEach(this::item);
            return this;
        }

        public Builder element(final BaseElement element) {
            try {
                return xsiType(element.getXSIType()).id(StringUtils.defaultIfBlank(element.getStringProperty("ID"), element.getItem().getPKValueString()));
            } catch (ElementNotFoundException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Instantiation, "Submitted a BaseElement of type '" + element.getClass().getName() + "', which doesn't seem to have a property named ID. I don't know how to handle this element: " + e.ELEMENT, e);
            } catch (FieldNotFoundException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Instantiation, "Submitted a BaseElement of type '" + element.getClass().getName() + "', which doesn't seem to have a property named ID. Got a field not found exception: " + e.FIELD, e);
            }
        }

        public Builder elements(final List<BaseElement> elements) {
            elements.stream().map(element -> element == null ? null : element.getItem()).filter(Objects::nonNull).forEach(this::item);
            return this;
        }

        public Builder property(final String property, final Object value) {
            _properties.put(property, value);
            return this;
        }

        public Builder properties(final Map<String, ?> properties) {
            _properties.putAll(properties);
            return this;
        }

        public XftItemEvent build() {
            return new XftItemEvent(_xsiType, _ids, _action, _properties, _items);
        }

        private final List<String>        _ids        = new ArrayList<>();
        private final List<XFTItem>       _items      = new ArrayList<>();
        private final Map<String, Object> _properties = new HashMap<>();
        private       String              _xsiType;
        private       String              _action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item   The {@link XFTItem item} affected by the event.
     * @param action The action that triggered this event.
     */
    public XftItemEvent(final BaseElement item, final String action) throws ElementNotFoundException, FieldNotFoundException {
        this(item.getXSIType(), Collections.singletonList(item.getStringProperty("ID")), action, null, Collections.singletonList(item.getItem()));
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item   The {@link XFTItem item} affected by the event.
     * @param action The action that triggered this event.
     */
    public XftItemEvent(final XFTItem item, final String action) throws XFTInitException, ElementNotFoundException {
        this(item.getXSIType(), Collections.singletonList(item.getIDValue()), action, null, Collections.singletonList(item));
    }

    /**
     * Instantiates a new event.
     *
     * @param xsiType The XSI type of the object or objects affected by the event.
     * @param id      The ID of the object affected by the event.
     * @param action  The action that triggered this event.
     */
    public XftItemEvent(final String xsiType, final String id, final String action) {
        this(xsiType, Collections.singletonList(id), action, null, null);
    }

    /**
     * Instantiates a new event.
     *
     * @param xsiType The XSI type of the object or objects affected by the event.
     * @param ids     A list of one or more IDs of the object or objects affected by the event.
     * @param action  The action that triggered this event.
     */
    public XftItemEvent(final String xsiType, final List<String> ids, final String action) {
        this(xsiType, ids, action, null, null);
    }

    protected XftItemEvent(final String xsiType, final String id, final String action, final Map<String, Object> properties, final XFTItem items) {
        this(xsiType, Collections.singletonList(id), action, properties, Collections.singletonList(items));
    }

    protected XftItemEvent(final String xsiType, List<String> ids, final List<XFTItem> items, final String action) {
        this(xsiType, ids, action, null, items);
    }

    protected XftItemEvent(final String xsiType, final List<String> ids, final String action, final Map<String, Object> properties, final List<XFTItem> items) {
        if (StringUtils.isBlank(action)) {
            throw new RuntimeException("You can't have an event without an action.");
        }

        final boolean hasXsiType  = StringUtils.isNotBlank(xsiType);
        final boolean hasIds      = !CollectionUtils.isEmpty(ids);
        final boolean hasXftItems = !CollectionUtils.isEmpty(items);

        if (!hasXsiType && !hasIds && !hasXftItems) {
            throw new RuntimeException("You can't have an event without it having affected something: you must specify at least one XSI type/ID, BaseElement, or XFTItem for an event..");
        }

        if (hasXftItems) {
            if (items.size() != ids.size()) {
                throw new IllegalArgumentException("You must specify either one or more BaseElement or XFTItem instances (all of the same XSI type) OR one XSI type and one or more IDs. This class doesn't support mixing them.");
            }
        } else {
            if (!hasXsiType) {
                throw new RuntimeException("You must set the XSI type along with the ID(s) for an event.");
            }
            if (!hasIds) {
                throw new RuntimeException("You must set at least one instance ID along with the XSI type, or there's no way to tell which object was affected.");
            }
        }

        _action = action;
        _properties = ImmutableMap.copyOf(ObjectUtils.getIfNull(properties, Collections::emptyMap));
        _items = new ArrayList<>(ObjectUtils.getIfNull(items, Collections::emptyList));
        if (hasXftItems) {
            final List<String> xsiTypes = _items.stream().map(XFTItem::getXSIType).distinct().collect(Collectors.toList());
            if (xsiTypes.size() > 1) {
                throw new IllegalArgumentException("You specified " + xsiTypes.size() + " items, which is fine, but there are multiple XSI types, which is not fine. XFT item events support multiple items but they must all be of the same type, while you specified items of type: " + String.join(", ", xsiTypes));
            }
            _xsiType = xsiTypes.get(0);
            _ids = _items.stream().map(item -> {
                try {
                    return item.getIDValue();
                } catch (XFTInitException | ElementNotFoundException e) {
                    log.error("An error occurred trying to get the ID of an object of type {}", item.getXSIType(), e);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            _xsiType = xsiType;
            _ids = ImmutableList.copyOf(ids);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMultiItemEvent() {
        return _ids.size() > 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        checkSingleItemState();
        return getIds().get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XFTItem getItem() {
        checkSingleItemState();
        if (_items.isEmpty()) {
            final String id      = getIds().get(0);
            final String xmlPath = getXsiType() + "/ID";
            try {
                _items.add(ItemSearch.GetItem(xmlPath, id, Users.getAdminUser(), false));
            } catch (Exception e) {
                log.warn("An error occurred trying to retrieve the XFTItem for object via XMLPath {} with ID {}", xmlPath, id, e);
            }
        }
        return _items.get(0);
    }

    @Override
    public String toString() {
        switch (_ids.size()) {
            case 0:
                return "Action '" + getAction() + "' on uninitialized item(s)";

            case 1:
                return "Action '" + getAction() + "' on " + String.format("%1$s/ID=%2$s", getXsiType(), getId());

            default:
                return "Action '" + getAction() + "' on [" + getIds().stream().map(id -> String.format("%1$s/ID=%2$s", getXsiType(), id)).collect(Collectors.joining(", ")) + "]";
        }
    }

    private void checkSingleItemState() {
        if (StringUtils.isBlank(_xsiType) || _ids.isEmpty()) {
            throw new IllegalArgumentException("Can't get an item because there's no XSI type and ID specified.");
        }
        if (isMultiItemEvent()) {
            throw new IllegalArgumentException("Can't get an item because there are multiple XSI types and IDs specified. Call getItems() instead. You can check for multi-item events by calling isMultiItemEvent().");
        }
    }

    private final String              _action;
    private final String              _xsiType;
    private final List<String>        _ids;
    private final List<XFTItem>       _items;
    private final Map<String, Object> _properties;
}
