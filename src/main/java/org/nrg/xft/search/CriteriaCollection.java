/*
 * org.nrg.xft.search.CriteriaCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xft.search;

import org.apache.log4j.Logger;
import org.nrg.xdat.search.DisplayCriteria;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Tim
 */
public class CriteriaCollection implements SQLClause {
    static org.apache.log4j.Logger logger = Logger.getLogger(CriteriaCollection.class);
    private ArrayList collection = new ArrayList();
    private String joinType = "AND";

    public String getElementName() {
        if (size() > 0) {
            return ((SQLClause) collection.get(0)).getElementName();
        } else {
            return "";
        }
    }

    public CriteriaCollection(String join) {
        joinType = join;
    }

    public String getSQLClause(QueryOrganizerI qo) throws Exception {
        String clause = "\n (";
        Iterator clauses = getClauses();
        int counter = 0;
        while (clauses.hasNext()) {
            SQLClause c = (SQLClause) clauses.next();
            if (counter++ != 0) {
                clause += " " + joinType + " ";
            }
            clause += c.getSQLClause(qo);
        }
        clause += ")";
        return clause;
    }


    @SuppressWarnings("unchecked")
    public ArrayList getSchemaFields() throws Exception {
        ArrayList al = new ArrayList();
        Iterator clauses = getClauses();
        while (clauses.hasNext()) {
            SQLClause c = (SQLClause) clauses.next();
            al.addAll(c.getSchemaFields());
        }
        return al;
    }

    public ArrayList<DisplayCriteria> getSubQueries() throws Exception {
        ArrayList<DisplayCriteria> al = new ArrayList<>();
        Iterator clauses = getClauses();
        while (clauses.hasNext()) {
            SQLClause c = (SQLClause) clauses.next();
            al.addAll(c.getSubQueries());
        }
        return al;
    }

    /**
     * Can be SearchCriteria or CriteriaCollection
     *
     * @return An iterator of the clauses.
     */
    public Iterator getClauses() {
        return collection.iterator();
    }

    public ArrayList toArrayList() {
        return this.collection;
    }

    public void addClause(String xmlPath, Object value) {
        try {
            SearchCriteria c = new SearchCriteria();
            c.setFieldWXMLPath(xmlPath);
            c.setValue(value);
            this.add(c);
        } catch (Exception e) {
            logger.error("", e);
        }
    }


    public void addClause(String xmlPath, String comparison, Object value) {
        try {
            SearchCriteria c = new SearchCriteria();
            c.setFieldWXMLPath(xmlPath);
            if (comparison.trim().equalsIgnoreCase("LIKE")) {
                comparison = " LIKE ";
                String temp = value.toString();
                if (temp.startsWith("'")) {
                    temp = temp.substring(1);
                }
                if (temp.endsWith("'")) {
                    temp = temp.substring(0, temp.length() - 1);
                }

                if (!temp.contains("%")) {
                    temp = "%" + temp + "%";
                }

                value = temp;

                //c.setOverrideFormatting(true);
            }
            c.setValue(value);
            c.setComparison_type(comparison);
            this.add(c);
        } catch (Exception e) {
            logger.error("", e);
        }
    }


    public void addClause(String xmlPath, String comparison, Object value, boolean overrideFormatting) {
        if (overrideFormatting) {
            try {
                SearchCriteria c = new SearchCriteria();
                c.setFieldWXMLPath(xmlPath);
                if (comparison.trim().equalsIgnoreCase("LIKE")) {
                    comparison = " LIKE ";
                }
                c.setOverrideFormatting(true);
                c.setValue(value);
                c.setComparison_type(comparison);
                this.add(c);
            } catch (Exception e) {
                logger.error("", e);
            }
        } else {
            addClause(xmlPath, comparison, value);
        }
    }

    /**
     * @param o The SQL clause to add.
     */
    @SuppressWarnings("unchecked")
    public void addClause(SQLClause o) {
        collection.add(o);
    }

    public void add(SQLClause o) {
        addClause(o);
    }

    public int size() {
        return numClauses();
    }

    public int numClauses() {
        int size = 0;
        for (final Object o : collection) {
            if (o instanceof CriteriaCollection) {
                size += ((CriteriaCollection) o).numClauses();
            } else {
                size++;
            }
        }
        return size;
    }

    public int numSchemaClauses() {
        int size = 0;
        for (final Object o : collection) {
            if (o instanceof CriteriaCollection) {
                size += ((CriteriaCollection) o).numSchemaClauses();
            } else {
                if (o instanceof SearchCriteria) {
                    size++;
                }
            }
        }
        return size;
    }

    public Iterator iterator() {
        return collection.iterator();
    }

    public void addCriteria(ArrayList list) {
        for (final Object aList : list) {
            SQLClause c = (SQLClause) aList;
            add(c);
        }
    }

    public void addCriteria(SQLClause clause) {
        add(clause);
    }

    /**
     * 'AND' or 'OR'
     *
     * @return The join type.
     */
    public String getJoinType() {
        return joinType;
    }

    /**
     * 'AND' or 'OR'
     *
     * @param joinType The join type to set.
     */
    @SuppressWarnings("unused")
    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public String toString() {
        try {
            return this.getSQLClause(null);
        } catch (Exception e) {
            return "error";
        }
    }
}

