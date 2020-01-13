/*
 * core: org.nrg.xft.search.CriteriaCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.search;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.search.DisplayCriteria;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tim
 */
@Slf4j
public class CriteriaCollection implements SQLClause {
    private ArrayList<SQLClause>   collection = new ArrayList<>();
    private String                 joinType;

    public String getElementName() {
        if (size() > 0) {
            return collection.get(0).getElementName();
        } else {
            return "";
        }
    }

    public CriteriaCollection(String join) {
        joinType = join;
    }

    public String getSQLClause(QueryOrganizerI qo) throws Exception {
        StringBuilder clause  = new StringBuilder("\n (");
        Iterator      clauses = getClauses();
        int           counter = 0;
        while (clauses.hasNext()) {
            SQLClause c = (SQLClause) clauses.next();
            if (counter++ != 0) {
                clause.append(" ").append(joinType).append(" ");
            }
            clause.append(c.getSQLClause(qo));
        }
        clause.append(")");
        return clause.toString();
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

    public List<SQLClause> toList() {
        return collection;
    }

    public void addClause(String xmlPath, Object value) {
        try {
            SearchCriteria c = new SearchCriteria();
            c.setFieldWXMLPath(xmlPath);
            c.setValue(value);
            this.add(c);
        } catch (Exception e) {
            log.error("", e);
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
            log.error("", e);
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
                log.error("", e);
            }
        } else {
            addClause(xmlPath, comparison, value);
        }
    }

    /**
     * @param clause The SQL clause to add.
     */
    public void addClause(final SQLClause clause) {
        collection.add(clause);
    }

    public void add(final SQLClause clause) {
        addClause(clause);
    }

    public int size() {
        return numClauses();
    }

    public int numClauses() {
        int size = 0;
        for (final SQLClause clause : collection) {
            if (clause instanceof CriteriaCollection) {
                size += clause.numClauses();
            } else {
                size++;
            }
        }
        return size;
    }

    public int numSchemaClauses() {
        int size = 0;
        for (final SQLClause clause : collection) {
            if (clause instanceof CriteriaCollection) {
                size += ((CriteriaCollection) clause).numSchemaClauses();
            } else if (clause instanceof SearchCriteria) {
                size++;
            }
        }
        return size;
    }

    public Iterator<SQLClause> iterator() {
        return collection.iterator();
    }

    public void addCriteria(final ArrayList list) {
        for (final Object clause : list) {
            add((SQLClause) clause);
        }
    }

    public void addCriteria(final SQLClause clause) {
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
            return getSQLClause(null);
        } catch (Exception e) {
            return "error";
        }
    }
}

