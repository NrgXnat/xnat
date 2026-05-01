/*
 * core: org.nrg.xdat.collections.DisplayFieldWrapperCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.search.DisplayFieldWrapper;
import org.nrg.xft.collections.XFTCollection;
import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.sequence.SequenceComparator;

/**
 * @author Tim
 */
@SuppressWarnings("unchecked")
public class DisplayFieldWrapperCollection extends XFTCollection {

    public DisplayFieldWrapperCollection() {
        super();
        setAllowReplacement(true);
    }

    public DisplayFieldWrapper getDisplayField(String id) {
        return (DisplayFieldWrapper) this.getStoredItem(id);
    }

    public void addDisplayField(DisplayFieldWrapper df) {
        df.setSequence(this.getItemHash().size());
        this.addStoredItem(df);
    }

    public void addDisplayField(DisplayField df, String header, Object value) {
        DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
        if (header != null)
            dfw.setHeader(header);
        dfw.setValue(value);
        addDisplayField(dfw);
    }

    public void addDisplayField(DisplayField df, String header, Object value, Boolean visible) {
        DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
        if (header != null)
            dfw.setHeader(header);
        dfw.setValue(value);
        dfw.setVisible(visible);
        addDisplayField(dfw);
    }

    public void addDisplayField(DisplayField df) {
        DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
        dfw.setHeader(df.getHeader());
        addDisplayField(dfw);
    }

    public void addDisplayField(DisplayField df, Object value) {
        DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
        dfw.setHeader(df.getHeader());
        dfw.setValue(value);
        addDisplayField(dfw);
    }

    public void addDisplayFields(Collection coll) {
        for (final Object o : coll) {
            if (o instanceof DisplayFieldWrapper) {
                addDisplayField((DisplayFieldWrapper) o);
            } else if (o instanceof DisplayField) {
                addDisplayField((DisplayField) o);
            }
        }
    }

    /**
     * Gets the requested {@link DisplayFieldWrapper display field}.
     *
     * @param id The ID of the display field to retrieve.
     * @return The {@link DisplayFieldWrapper display field} requested if it exists, null otherwise.
     */
    @SuppressWarnings("unused")
    public DisplayFieldWrapper getDisplayFieldWException(String id) {
        try {
            return (DisplayFieldWrapper) this.getStoredItemWException(id);
        } catch (ItemNotFoundException e1) {
            return null;
        }
    }

    /**
     * Adds the submitted {@link DisplayFieldWrapper display field}. Any exceptions that occur will be thrown up.
     *
     * @param displayField The {@link DisplayFieldWrapper display field} to be added.
     */
    @SuppressWarnings("unused")
    public void addDisplayFieldWException(DisplayFieldWrapper displayField) {
        try {
            displayField.setSequence(this.getItemHash().size());
            this.addStoredItemWException(displayField);
        } catch (DuplicateKeyException ignored) {
        }
    }

    /**
     * Gets the available fields, sorted by sequence.
     *
     * @return The available fields in a list.
     */
    public ArrayList getSortedFields() {
        ArrayList al = new ArrayList();
        al.addAll(this.getItemHash().values());
        Collections.sort(al, SequenceComparator.SequenceComparator);
        return al;
    }

    /**
     * Gets the available visible fields, sorted by sequence.
     *
     * @return The available visible fields in a list.
     */
    public ArrayList<DisplayFieldReferenceI> getSortedVisibleFields() {
        ArrayList<DisplayFieldReferenceI> al = new ArrayList<>();
        Iterator iterator = getItemIterator();
        while (iterator.hasNext()) {
            DisplayFieldWrapper dfw = (DisplayFieldWrapper) iterator.next();
            if (dfw.getDf().isVisible()) {
                al.add(dfw);
            }
        }
        Collections.sort(al, SequenceComparator.SequenceComparator);
        return al;
    }
}
