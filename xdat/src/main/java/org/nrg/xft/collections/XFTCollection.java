/*
 * core: org.nrg.xft.collections.XFTCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.identifier.Identifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Tim
 */
public abstract class XFTCollection {
    private final Map<String, Identifier> identifiers      = new HashMap<>();
    private       boolean                 allowReplacement = false;
    private boolean allowItemNotFoundError = true;

    @JsonIgnore
    protected Object getStoredItem(String id) {
        Object o = identifiers.get(id);
        if (o == null && allowItemNotFoundError) {
            return null;
        } else
            return identifiers.get(id);
    }

    protected void addStoredItem(Identifier o) {
        if (!allowReplacement) {
            if (!identifiers.containsKey(o.getId())) {
                identifiers.put(o.getId(), o);
            }
        } else {
            identifiers.put(o.getId(), o);
        }
    }

    protected void removeItemById(String id) {
        identifiers.remove(id);
    }

    protected Object getStoredItemWException(String id) throws ItemNotFoundException {
        Object o = identifiers.get(id);
        if (o == null && allowItemNotFoundError) {
            throw new ItemNotFoundException(id);
        } else
            return identifiers.get(id);
    }

    protected void addStoredItemWException(Identifier o) throws DuplicateKeyException {
        if (!allowReplacement) {
            if (identifiers.containsKey(o.getId())) {
                throw new DuplicateKeyException(o.getId());
            } else {
                identifiers.put(o.getId(), o);
            }
        } else {
            identifiers.put(o.getId(), o);
        }
    }

    @JsonIgnore
    protected Iterator<Identifier> getItemIterator() {
        return identifiers.values().iterator();
    }
    @JsonIgnore
    protected Map<String, Identifier> getItemHash() {
        return identifiers;
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
        return identifiers.size();
    }

    /**
     * The string representation of the collection.
     *
     * @return The string representation of the collection.
     */
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(identifiers.size()).append(" Items");
        Iterator<Identifier> iterator = this.getItemIterator();
        while (iterator.hasNext()) {
            buffer.append("\n").append(iterator.next().toString());
        }

        return buffer.toString();
    }
}

