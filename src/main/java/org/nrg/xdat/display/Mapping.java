/*
 * org.nrg.xdat.display.Mapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.display;

import java.util.ArrayList;

/**
 * @author Tim
 */
public class Mapping {
    private String tableName = "";
    private ArrayList columns = new ArrayList();

    /**
     * @return The columns in the mapping.
     */
    public ArrayList getColumns() {
        return columns;
    }

    /**
     * @return The table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param list A list of the columns in the mapping.
     */
    public void setColumns(ArrayList list) {
        columns = list;
    }

    /**
     * @param string The table name for the mapping.
     */
    public void setTableName(String string) {
        tableName = string;
    }

    @SuppressWarnings("unchecked")
    public void addColumn(MappingColumn c) {
        columns.add(c);
    }

}

