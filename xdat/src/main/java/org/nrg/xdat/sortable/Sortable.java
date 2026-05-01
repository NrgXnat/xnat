/*
 * core: org.nrg.xdat.sortable.Sortable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.sortable;

import java.util.Comparator;

/**
 * @author Tim
 */
public abstract class Sortable {
    private int sortOrder = 0;

    /**
     * @return The sort order of the sortable object.
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the sort order of the sortable object.
     *
     * @param sortOrder The sort order.
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public final static Comparator SequenceComparator = new Comparator() {
        public int compare(Object mr1, Object mr2) throws ClassCastException {
            try {
                int value1 = ((Sortable) mr1).getSortOrder();
                int value2 = ((Sortable) mr2).getSortOrder();

                if (value1 > value2) {
                    return 1;
                } else if (value1 < value2) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (Exception ex) {
                throw new ClassCastException("Error Comparing Sequence");
            }
        }
    };
}

