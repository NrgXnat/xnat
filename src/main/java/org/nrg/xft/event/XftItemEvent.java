/*
 * core: org.nrg.xft.event.XftItemEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;

/**
 * The Class XftItemEvent.
 */
@Getter
@Accessors(prefix = "_")
@Slf4j
public class XftItemEvent implements XftItemEventI {
    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unused")
    public static class Builder {
        public Builder xsiType(final String xsiType) {
            if (_typeAndIds.size() > 1) {
                throw new RuntimeException("You can only set the XSI type along with the ID for a single-item event, but there are already items set for this builder.");
            }
            if (_typeAndIds.isEmpty()) {
                _typeAndIds.add(new ImmutablePair<String, String>(xsiType, null));
            } else {
                _typeAndIds.set(0, new ImmutablePair<>(xsiType, _typeAndIds.get(0).getValue()));
            }
            return this;
        }

        public Builder id(final String id) {
            if (_typeAndIds.size() > 1) {
                throw new RuntimeException("You can only set the ID along with the XSI type for a single-item event, but there are already items set for this builder.");
            }
            if (_typeAndIds.isEmpty()) {
                _typeAndIds.add(new ImmutablePair<String, String>(null, id));
            } else {
                _typeAndIds.set(0, new ImmutablePair<>(_typeAndIds.get(0).getKey(), id));
            }
            return this;
        }

        public Builder action(final String action) {
            _action = action;
            return this;
        }

        public Builder typeAndId(final String xsiType) {
            return typeAndId(ImmutablePair.of(xsiType, (String) null));
        }

        public Builder typeAndId(final String xsiType, final String id) {
            return typeAndId(ImmutablePair.of(xsiType, id));
        }

        public Builder typeAndId(final Pair<String, String> typeAndId) {
            _typeAndIds.add(typeAndId);
            return this;
        }

        public Builder typeAndIds(final List<Pair<String, String>> typeAndIds) {
            _typeAndIds.addAll(typeAndIds);
            return this;
        }

        public Builder item(final XFTItem item) {
            try {
                _items.add(item);
                _typeAndIds.add(ImmutablePair.of(item.getXSIType(), item.getIDValue()));
                return this;
            } catch (XFTInitException | ElementNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public Builder items(final List<XFTItem> items) {
            _items.addAll(items);
            _typeAndIds.addAll(Sets.newHashSet(Iterables.filter(Lists.transform(items, XFTITEM_TO_PAIR_FUNCTION), Predicates.<Pair<String, String>>notNull())));
            return this;
        }

        public Builder element(final BaseElement element) {
            return item(element.getItem());
        }

        public Builder elements(final List<BaseElement> elements) {
            return items(Lists.newArrayList(Iterables.filter(Lists.transform(elements, BASEELEMENT_TO_XFTITEM_FUNCTION), Predicates.<XFTItem>notNull())));
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
            if (StringUtils.isBlank(_action)) {
                throw new RuntimeException("You can't have an event without an action.");
            }

            final boolean hasTypeAndIds = !_typeAndIds.isEmpty();
            final boolean hasXsiType    = hasTypeAndIds && StringUtils.isNotBlank(_typeAndIds.get(0).getKey());
            final boolean hasId         = hasTypeAndIds && StringUtils.isNotBlank(_typeAndIds.get(0).getValue());

            if (!hasTypeAndIds) {
                throw new RuntimeException("You can't have an event without it having affected something.");
            }

            if (!hasXsiType) {
                throw new RuntimeException("You can only set the XSI type along with the ID for a single-item event, but there are already other items set for this builder.");
            }

            if (!hasId) {
                throw new RuntimeException("You must set the ID along with the XSI type for a single-item event, or there's no way to tell which object was affected.");
            }

            return new XftItemEvent(_typeAndIds, _action, _properties, _items);
        }

        private final List<Pair<String, String>> _typeAndIds = new ArrayList<>();
        private final List<XFTItem>              _items      = new ArrayList<>();
        private       String                     _action;
        private       Map<String, Object>        _properties = new HashMap<>();
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item The {@link XFTItem}
     */
    public XftItemEvent(final BaseElement item, final String action) throws ElementNotFoundException, FieldNotFoundException {
        this(Collections.<Pair<String, String>>singletonList(ImmutablePair.of(item.getXSIType(), item.getStringProperty("ID"))), action, null, Collections.singletonList(item.getItem()));
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item The {@link XFTItem}
     */
    public XftItemEvent(final XFTItem item, final String action) throws XFTInitException, ElementNotFoundException {
        this(Collections.<Pair<String, String>>singletonList(ImmutablePair.of(item.getXSIType(), item.getIDValue())), action, null, Collections.singletonList(item));
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param xsiType the xsi type
     * @param id      the id
     * @param action  the action
     */
    public XftItemEvent(final String xsiType, final String id, final String action) {
        this(Collections.<Pair<String, String>>singletonList(ImmutablePair.of(xsiType, id)), action, null, null);
    }

    public XftItemEvent(final List<Pair<String, String>> typeAndIds, final String action) {
        this(typeAndIds, action, null, null);
    }

    protected XftItemEvent(final List<Pair<String, String>> typeAndIds, final List<XFTItem> items, final String action) {
        this(typeAndIds, action, null, items);
    }

    protected XftItemEvent(final List<Pair<String, String>> typeAndIds, final String action, final Map<String, Object> properties, final List<XFTItem> items) {
        final boolean hasTypeAndIds = typeAndIds != null && !typeAndIds.isEmpty();
        final boolean hasXftItems = items != null && !items.isEmpty();
        if (!hasTypeAndIds && !hasXftItems) {
            throw new NrgServiceRuntimeException(ConfigurationError, "You must specify at least one XSI type and ID pair or XFTItem: an action must happen to something.");
        }
        _typeAndIds = hasTypeAndIds ? ImmutableList.copyOf(typeAndIds) : getTypeAndIdsFromXftItems(items);
        _action = action;
        _properties = ImmutableMap.copyOf(ObjectUtils.defaultIfNull(properties, Collections.<String, Object>emptyMap()));
        _items = new ArrayList<>(ObjectUtils.defaultIfNull(items, Collections.<XFTItem>emptyList()));
    }

    @Override
    public boolean isMultiItemEvent() {
        return _typeAndIds.size() > 1;
    }

    @Override
    public String getXsiType() {
        checkSingleItemState();
        return _typeAndIds.get(0).getKey();
    }

    @Override
    public String getId() {
        checkSingleItemState();
        return _typeAndIds.get(0).getValue();
    }

    /**
     * Gets the item. This method returns an Object rather than an XFTItem because the object may sometimes be
     * the OM version rather than the generic XFTItem (e.g. {@link XdatUsergroup}), as when initialized with the
     * {@link #XftItemEvent(XFTItem, String)} constructor.
     *
     * @return the item
     */
    @Override
    public XFTItem getItem() {
        checkSingleItemState();
        if (_items.isEmpty()) {
            final Pair<String, String> item    = _typeAndIds.get(0);
            final String               xsiType = item.getKey();
            final String               id      = item.getValue();
            final String               xmlPath = xsiType + "/ID";
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
        switch (_typeAndIds.size()) {
            case 0:
                return "Action '" + getAction() + "' on uninitialized item(s)";

            case 1:
                return "Action '" + getAction() + "' on " + PAIR_TO_STRING_FUNCTION.apply(_typeAndIds.get(0));

            default:
                return "Action '" + getAction() + "' on [" + StringUtils.join(Lists.transform(getTypeAndIds(), PAIR_TO_STRING_FUNCTION), ", ") + "]";
        }
    }

    private static final Function<BaseElement, XFTItem> BASEELEMENT_TO_XFTITEM_FUNCTION = new Function<BaseElement, XFTItem>() {
        @Nullable
        @Override
        public XFTItem apply(@Nullable final BaseElement element) {
            if (element == null) {
                return null;
            }
            return element.getItem();
        }
    };

    private static final Function<XFTItem, Pair<String, String>> XFTITEM_TO_PAIR_FUNCTION = new Function<XFTItem, Pair<String, String>>() {
        @Nullable
        @Override
        public Pair<String, String> apply(@Nullable final XFTItem item) {
            if (item == null) {
                return null;
            }
            try {
                return ImmutablePair.of(item.getXSIType(), item.getIDValue());
            } catch (XFTInitException e) {
                log.error("There was an error initializing or accessing XFT", e);
            } catch (ElementNotFoundException e) {
                log.error("Element '{}' not found", e.ELEMENT, e);
            }
            return null;
        }
    };

    private static final Function<Pair<String, String>, String> PAIR_TO_STRING_FUNCTION = new Function<Pair<String, String>, String>() {
        @Nullable
        @Override
        public String apply(@Nullable final Pair<String, String> pair) {
            if (pair == null) {
                return null;
            }
            return pair.toString("%1$s/ID=%2$s");
        }
    };

    @Nonnull
    private static List<Pair<String, String>> getTypeAndIdsFromXftItems(final List<XFTItem> items) {
        return Lists.newArrayList(Iterables.filter(Lists.transform(items, XFTITEM_TO_PAIR_FUNCTION), Predicates.<Pair<String,String>>notNull()));
    }

    private void checkSingleItemState() {
        if (_typeAndIds.isEmpty()) {
            throw new IllegalArgumentException("Can't get an item because there's no XSI type and ID specified.");
        }
        if (isMultiItemEvent()) {
            throw new IllegalArgumentException("Can't get an item because there are multiple XSI types and IDs specified. Call getItems() instead. You can check for multi-item events by calling isMultiItemEvent().");
        }
    }

    private final String _action;

    /**
     * Gets the XSI type and ID of each item associated with the event as a pair, where the XSI type is the left or key property and the ID is the right
     * or value property.
     */
    private final List<Pair<String, String>> _typeAndIds;

    /**
     * Gets the items. This method differs from the singular {@link XftItemEvent} in two ways: it returns a list of {@link XFTItem} objects rather than
     * a generic Java object. Even objects inserted into the event that subclass {@link BaseElement} are converted upon insertion. It also differs in that,
     * unlike the {@link XftItemEvent#getItem()} method, it does not create the item objects on demand if they were initially set through the XSI type and
     * object ID.
     *
     * @return Returns the list of items for the event.
     */
    @SuppressWarnings("JavaDoc")
    private final List<XFTItem> _items;

    /**
     * Contains the properties for event-specific data.
     */
    private final Map<String, Object> _properties;
}
