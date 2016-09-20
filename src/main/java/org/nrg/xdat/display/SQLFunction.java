/*
 * core: org.nrg.xdat.display.SQLFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.display;

import java.util.Comparator;

/**
 * @author Tim
 */
public class SQLFunction {
    private String _name = "";
    private String _content = "";
    private int _sortOrder = 0;

    /**
     * Gets the name of the SQL function.
     *
     * @return The name of the SQL function.
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of the SQL function.
     *
     * @param name The name of the SQL function.
     */
    public void setName(final String name) {
        _name = name;
    }

    /**
     * Gets the content of the SQL function.
     *
     * @return The content of the SQL function.
     */
    public String getContent() {
        return _content;
    }

    /**
     * Gets the content of the SQL function.
     *
     * @param content The content of the SQL function.
     */
    public void setContent(final String content) {
        _content = content;
    }

    /**
     * Gets the sort order of the SQL function.
     *
     * @return The sort order of the SQL function.
     */
    public int getSortOrder() {
        return _sortOrder;
    }

    /**
     * Sets the sort order of the SQL function.
     *
     * @param sortOrder The sort order of the SQL function.
     */
    public void setSortOrder(final int sortOrder) {
        _sortOrder = sortOrder;
    }

    public final static Comparator<SQLFunction> SequenceComparator = new Comparator<SQLFunction>() {
        @Override
        public int compare(final SQLFunction mr1, final SQLFunction mr2) {
            try {
                int value1 = mr1.getSortOrder();
                int value2 = mr2.getSortOrder();

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
