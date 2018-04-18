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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            if (!_typeAndIds.isEmpty()) {
                throw new RuntimeException("You can only set the XSI type along with the ID for a single-item event, but there are already items set for this builder.");
            }
            _xsiType = xsiType;
            return this;
        }

        public Builder id(final String id) {
            if (!_typeAndIds.isEmpty()) {
                throw new RuntimeException("You can only set the ID along with the XSI type for a single-item event, but there are already items set for this builder.");
            }
            _id = id;
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

        public XftItemEvent build() {
            if (StringUtils.isBlank(_action)) {
                throw new RuntimeException("You can't have an event without an action.");
            }

            final boolean hasTypeAndIds = !_typeAndIds.isEmpty();
            final boolean hasXsiType    = StringUtils.isNotBlank(_xsiType);
            final boolean hasId         = StringUtils.isNotBlank(_id);

            if (!hasTypeAndIds && !hasXsiType) {
                throw new RuntimeException("You can't have an event without it having affected something.");
            }

            if (hasTypeAndIds && hasXsiType) {
                throw new RuntimeException("You can only set the XSI type along with the ID for a single-item event, but there are already other items set for this builder.");
            }

            if (hasXsiType && !hasId) {
                throw new RuntimeException("You must set the ID along with the XSI type for a single-item event, or there's no way to tell which object was affected.");
            }

            if (hasXsiType) {
                _typeAndIds.add(ImmutablePair.of(_xsiType, _id));
            }

            return new XftItemEvent(_typeAndIds, _items, _action);
        }

        private final List<Pair<String, String>> _typeAndIds = new ArrayList<>();
        private final List<XFTItem>              _items      = new ArrayList<>();
        private       String                     _xsiType;
        private       String                     _id;
        private       String                     _action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item The {@link XFTItem}
     */
    public XftItemEvent(final BaseElement item, final String action) throws ElementNotFoundException, FieldNotFoundException {
        _items.add(item.getItem());
        _typeAndIds.add(ImmutablePair.of(item.getXSIType(), item.getStringProperty("ID")));
        _action = action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item The {@link XFTItem}
     */
    public XftItemEvent(final XFTItem item, final String action) throws XFTInitException, ElementNotFoundException {
        _items.add(item);
        _typeAndIds.add(ImmutablePair.of(item.getXSIType(), item.getIDValue()));
        _action = action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param xsiType the xsi type
     * @param action  the action
     */
    public XftItemEvent(final String xsiType, final String action) {
        _typeAndIds.add(ImmutablePair.of(xsiType, (String) null));
        _action = action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param xsiType the xsi type
     * @param id      the id
     * @param action  the action
     */
    public XftItemEvent(final String xsiType, final String id, final String action) {
        _typeAndIds.add(ImmutablePair.of(xsiType, id));
        _action = action;
    }

    public XftItemEvent(final List<Pair<String, String>> typeAndIds, final String action) {
        _typeAndIds.addAll(typeAndIds != null && !typeAndIds.isEmpty() ? ImmutableList.copyOf(typeAndIds) : Collections.<Pair<String, String>>emptyList());
        _action = action;
    }

    protected XftItemEvent(final List<Pair<String, String>> typeAndIds, final List<XFTItem> items, final String action) {
        _typeAndIds.addAll(typeAndIds != null && !typeAndIds.isEmpty() ? ImmutableList.copyOf(typeAndIds) : Collections.<Pair<String, String>>emptyList());
        _items.addAll(items != null && !items.isEmpty() ? ImmutableList.copyOf(items) : Collections.<XFTItem>emptyList());
        _action = action;
    }

    protected XftItemEvent() {
        throw new IllegalArgumentException("You must specify an action to construct an XFT item event.");
    }

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
                return "Action " + getAction() + " on uninitialized item(s)";

            case 1:
                final Pair<String, String> item = _typeAndIds.get(0);
                return item.getKey() + " " + item.getValue() + " " + _action;

            default:
                return getAction() + " on " + StringUtils.join(Lists.transform(getTypeAndIds(), PAIR_TO_STRING_FUNCTION), ", ");
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
    private final List<Pair<String, String>> _typeAndIds = new ArrayList<>();

    /**
     * Gets the items. This method differs from the singular {@link XftItemEvent} in two ways: it returns a list of {@link XFTItem} objects rather than
     * a generic Java object. Even objects inserted into the event that subclass {@link BaseElement} are converted upon insertion. It also differs in that,
     * unlike the {@link XftItemEvent#getItem()} method, it does not create the item objects on demand if they were initially set through the XSI type and
     * object ID.
     *
     * @return Returns the list of items for the event.
     */
    @SuppressWarnings("JavaDoc")
    private final List<XFTItem> _items = new ArrayList<>();
}
