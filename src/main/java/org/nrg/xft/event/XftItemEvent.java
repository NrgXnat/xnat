/*
 * core: org.nrg.xft.event.XftItemEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.EventI;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;

/**
 * The Class XftItemEvent.
 */
@Slf4j
public class XftItemEvent implements EventI {
    /**
     * The Constant CREATE.
     */
    public final static String CREATE = "C";

    /**
     * The Constant READ.
     */
    public final static String READ = "R";

    /**
     * The Constant UPDATE.
     */
    public final static String UPDATE = "U";

    /**
     * The Constant DELETE.
     */
    public final static String DELETE = "D";

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

    /**
     * Gets the xsi type.
     *
     * @return the xsi type
     */
    public String getXsiType() {
        return _xsiType;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return _id;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction() {
        return _action;
    }

    /**
     * Gets the item. This method returns an Object rather than an XFTItem because the object may sometimes be
     * the OM version rather than the generic XFTItem (e.g. {@link XdatUsergroup}), as when initialized with the
     * {@link #XftItemEvent(BaseElement, String)} constructor.
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
    private final String _action;
    private final String _id;

    private Object _item;
}

