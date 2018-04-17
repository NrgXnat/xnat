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
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Supports events that involve multiple {@link XFTItem} objects, as well as related object types through the {@link BaseElement} class.
 * Although this class supports multiple target objects, it still indicates only a single event occurrence: the specified {@link #getAction()}
 * applies to all of the objects referenced by this event.
 */
@Getter
@Builder(builderMethodName = "multiItemBuilder")
@EqualsAndHashCode(callSuper = false)
@Accessors(prefix = "_")
@Slf4j
public class MultiXftItemEvent extends XftItemEvent {
    public static class MultiXftItemEventBuilder {
        public MultiXftItemEventBuilder element(final BaseElement element) throws XFTInitException, ElementNotFoundException {
            final XFTItem item = element.getItem();
            items.add(item);
            typesAndIds.add(ImmutablePair.of(item.getXSIType(), item.getIDValue()));
            return this;
        }

        public MultiXftItemEventBuilder elements(final List<BaseElement> elements) {
            final List<XFTItem> items = Lists.newArrayList(Iterables.filter(Lists.transform(elements, BASEELEMENT_TO_XFTITEM_FUNCTION), Predicates.<XFTItem>notNull()));
            this.items.addAll(items);
            typesAndIds.addAll(Sets.newHashSet(Iterables.filter(Lists.transform(items, XFTITEM_TO_PAIR_FUNCTION), Predicates.<Pair<String, String>>notNull())));
            return this;
        }

        public MultiXftItemEventBuilder item(final XFTItem item) throws XFTInitException, ElementNotFoundException {
            items.add(item);
            typesAndIds.add(ImmutablePair.of(item.getXSIType(), item.getIDValue()));
            return this;
        }

        public MultiXftItemEventBuilder items(final List<XFTItem> items) {
            this.items.addAll(items);
            typesAndIds.addAll(Sets.newHashSet(Iterables.filter(Lists.transform(items, XFTITEM_TO_PAIR_FUNCTION), Predicates.<Pair<String, String>>notNull())));
            return this;
        }

        public MultiXftItemEventBuilder action(final String action) {
            this.action(action);
            return this;
        }

        @SuppressWarnings("unused")
        public MultiXftItemEventBuilder clearItems() {
            items.clear();
            return this;
        }
    }

    public MultiXftItemEvent(final List<Pair<String, String>> typesAndIds, final List<XFTItem> items) {
        super();

        // No care taken here with the input parameters: the default constructor above will throw an exception.
        _typesAndIds = typesAndIds;
        _items = items;
    }

    public MultiXftItemEvent(final List<Pair<String, String>> typesAndIds, final List<XFTItem> items, final String action) {
        super(action);
        _typesAndIds = typesAndIds != null && !typesAndIds.isEmpty() ? ImmutableList.copyOf(typesAndIds) : Collections.<Pair<String,String>>emptyList();
        _items = items != null && !items.isEmpty() ? ImmutableList.copyOf(items) : Collections.<XFTItem>emptyList();
    }

    @Override
    public String toString() {
        return getAction() + " on " + StringUtils.join(Lists.transform(getTypesAndIds(), PAIR_TO_STRING_FUNCTION), ", ");
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

    /**
     * Gets the XSI type and ID of each item associated with the event as a pair, where the XSI type is the left or key property and the ID is the right
     * or value property.
     */
    @Singular
    private final List<Pair<String, String>> _typesAndIds;

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
}
