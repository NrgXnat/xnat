/*
 * core: org.nrg.xft.collections.XFTCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.collections;

import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.identifier.Identifier;

/**
 * @author Tim
 */
@SuppressWarnings("unchecked")
public abstract class XFTCollection {
    private Hashtable coll = new Hashtable();
    private boolean allowReplacement = false;
    private boolean allowItemNotFoundError = true;

    protected Object getStoredItem(String id) {
        Object o = coll.get(id);
        if (o == null && allowItemNotFoundError) {
            return null;
        } else
            return coll.get(id);
    }

    protected void addStoredItem(Identifier o) {
        if (!allowReplacement) {
            if (!coll.containsKey(o.getId())) {
                coll.put(o.getId(), o);
            }
        } else {
            coll.put(o.getId(), o);
        }
    }

    protected Object getStoredItemWException(String id) throws ItemNotFoundException {
        Object o = coll.get(id);
        if (o == null && allowItemNotFoundError) {
            throw new ItemNotFoundException(id);
        } else
            return coll.get(id);
    }

    protected void addStoredItemWException(Identifier o) throws DuplicateKeyException {
        if (!allowReplacement) {
            if (coll.containsKey(o.getId())) {
                throw new DuplicateKeyException(o.getId());
            } else {
                coll.put(o.getId(), o);
            }
        } else {
            coll.put(o.getId(), o);
        }
    }

    protected Iterator getItemIterator() {
        return coll.values().iterator();
    }

    protected Hashtable getItemHash() {
        return coll;
    }

    /**
     * Indicates whether this collection allows replacing of items that already exist in the collection.
     *
     * @return True if replacements are allowed, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean allowReplacement() {
        return allowReplacement;
    }

    /**
     * Sets whether this collection allows replacing of items that already exist in the collection.
     *
     * @param allowReplacement Whether replacements should be allowed.
     */
    public void setAllowReplacement(boolean allowReplacement) {
        this.allowReplacement = allowReplacement;
    }

    /**
     * Indicates whether error exceptions should be thrown when a requested item is not found in the collection.
     *
     * @return True if {@link ItemNotFoundException} should be thrown when a requested item is not found in the
     * collection.
     */
    @SuppressWarnings("unused")
    public boolean allowItemNotFoundError() {
        return allowItemNotFoundError;
    }

    /**
     * Sets whether error exceptions should be thrown when a requested item is not found in the collection.
     *
     * @param allowItemNotFoundError Indicates whether an {@link ItemNotFoundException} should be thrown when a
     *                               requested item is not found in the collection.
     */
    @SuppressWarnings("unused")
    public void setAllowItemNotFoundError(boolean allowItemNotFoundError) {
        this.allowItemNotFoundError = allowItemNotFoundError;
    }

    /**
     * The number of items in the collection.
     *
     * @return The number of items in the collection.
     */
    public int size() {
        return coll.size();
    }

    /**
     * The string representation of the collection.
     *
     * @return The string representation of the collection.
     */
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(coll.size()).append(" Items");
        Iterator iterator = this.getItemIterator();
        while (iterator.hasNext()) {
            buffer.append("\n").append(iterator.next().toString());
        }

        return buffer.toString();
    }
}

