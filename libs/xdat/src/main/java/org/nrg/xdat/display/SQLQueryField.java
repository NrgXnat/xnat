/*
 * core: org.nrg.xdat.display.SQLQueryField
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.display;

import java.util.ArrayList;
import java.util.List;

public class SQLQueryField extends DisplayField {
    private String                   subQuery       = "";
    private Object                   value          = "";
    private List<QueryMappingColumn> mappingColumns = new ArrayList<>();

    /**
     * @param ed The element display to set for the SQL query field.
     */
    public SQLQueryField(ElementDisplay ed) {
        super(ed);
    }


    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }


    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }


    /**
     * @return the subQuery
     */
    public String getSubQuery() {
        return subQuery;
    }

    /**
     * @param subQuery the subQuery to set
     */
    public void setSubQuery(String subQuery) {
        this.subQuery = subQuery;
    }

    @SuppressWarnings("unused")
    public void addMappingColumn(QueryMappingColumn mc) {
        this.mappingColumns.add(mc);
    }

    public void addMappingColumn(String schemaField, String queryField) {
        this.mappingColumns.add(new QueryMappingColumn(schemaField, queryField));
    }

    /**
     * @return the mappingColumns
     */
    public List<QueryMappingColumn> getMappingColumns() {
        return mappingColumns;
    }

    /**
     * @param mappingColumns the mappingColumns to set
     */
    @SuppressWarnings("unused")
    public void setMappingColumns(ArrayList<QueryMappingColumn> mappingColumns) {
        this.mappingColumns = mappingColumns;
    }


    public class QueryMappingColumn {
        private String schemaField = "";
        private String queryField  = "";

        public QueryMappingColumn(String s, String q) {
            schemaField = s;
            queryField = q;
        }

        /**
         * @return the queryField
         */
        public String getQueryField() {
            return queryField;
        }

        /**
         * @param queryField the queryField to set
         */
        public void setQueryField(String queryField) {
            this.queryField = queryField;
        }

        /**
         * @return the schemaField
         */
        public String getSchemaField() {
            return schemaField;
        }

        /**
         * @param schemaField the schemaField to set
         */
        public void setSchemaField(String schemaField) {
            this.schemaField = schemaField;
        }
    }
}
