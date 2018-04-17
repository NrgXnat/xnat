/*
 * core: org.nrg.xft.event.XftItemEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;

import static lombok.AccessLevel.PRIVATE;

/**
 * The Class XftItemEvent.
 */
@Data
@Builder
@AllArgsConstructor(access = PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class XftItemEvent implements XftItemEventI {
    public XftItemEvent build(final BaseElement item, final String action) throws ElementNotFoundException, FieldNotFoundException {
        return new XftItemEvent(item, action);
    }

    public XftItemEvent build(final XFTItem item, final String action) throws XFTInitException, ElementNotFoundException {
        return new XftItemEvent(item, action);
    }

    public static XftItemEvent build(final String xsiType, final String action) {
        return build(xsiType, null, action);
    }

    public static XftItemEvent build(final String xsiType, final String id, final String action) {
        return new XftItemEvent(xsiType, id, action);
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item The {@link XFTItem}
     */
    public XftItemEvent(final BaseElement item, final String action) throws ElementNotFoundException, FieldNotFoundException {
        _item = item;
        _xsiType = item.getXSIType();
        _id = item.getStringProperty("ID");
        _action = action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param item The {@link XFTItem}
     */
    public XftItemEvent(final XFTItem item, final String action) throws XFTInitException, ElementNotFoundException {
        _item = item;
        _xsiType = item.getXSIType();
        _id = item.getIDValue();
        _action = action;
    }

    /**
     * Instantiates a new XFTItem event.
     *
     * @param xsiType the xsi type
     * @param action  the action
     */
    public XftItemEvent(final String xsiType, final String action) {
        _xsiType = xsiType;
        _id = null;
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
        _xsiType = xsiType;
        _id = id;
        _action = action;
    }

    protected XftItemEvent(final String action) {
        _xsiType = null;
        _id = null;
        _action = action;
    }

    protected XftItemEvent() {
        throw new IllegalArgumentException("You must specify an action to construct an XFT item event.");
    }

    /**
     * Gets the item. This method returns an Object rather than an XFTItem because the object may sometimes be
     * the OM version rather than the generic XFTItem (e.g. {@link XdatUsergroup}), as when initialized with the
     * {@link #XftItemEvent(XFTItem, String)} constructor.
     *
     * @return the item
     */
    public Object getItem() {
        if (_item == null) {
            final String xmlPath = _xsiType + "/ID";
            try {
                _item = ItemSearch.GetItem(xmlPath, _id, Users.getAdminUser(), false);
            } catch (Exception e) {
                log.warn("An error occurred trying to retrieve the XFTItem for object via XMLPath {} with ID {}", xmlPath, _id, e);
            }
        }
        return _item;
    }

    @Override
    public String toString() {
        return _xsiType + " " + _id + " " + _action;
    }

    private final String _xsiType;
    private final String _id;
    private final String _action;

    private Object _item;
}
