/*
 * core: org.nrg.xft.search.CriteriaCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.search;

import com.google.common.collect.Lists;
import java.util.Collections;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.search.DisplayCriteria;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public List<SQLClause> getCriteriaCollection(){
        return collection;
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

    /***********************
     * Used for templatizing query paramaters for use in Prepared Statements
     * @param tracker
     * @return
     * @throws Exception
     */
    public SQLClause templatizeQuery(ValueTracker tracker) throws Exception {
        CriteriaCollection copy = new CriteriaCollection(this.joinType);
        for (SQLClause c: collection) {
            copy.collection.add(c.templatizeQuery(tracker));
        }
        return copy;
    }

    /*****************************
     * if this collection contains a SearchCriteria with a field path ending with 'suffix', then true, else false
     *
     * @param suffix
     * @return
     */
    public boolean containsXMLPathEndingWith(final String suffix){
        for(SQLClause clause: this.getCriteriaCollection()){
            if(clause instanceof SearchCriteria && ((SearchCriteria)clause).getXMLPath().endsWith(suffix)){
                return true;
            }
        }
        return false;
    }

    /*******************
     * if this collection contains a SearchCriteria with a field path ending with 'suffix' then the corresponding vaue is returned
     *
     * @param suffix
     * @return
     */
    @Nullable
    public Object getValueEndingWith(final String suffix){
        for(final SQLClause clause: this.getCriteriaCollection()) {
            if (clause instanceof SearchCriteria && ((SearchCriteria) clause).getXMLPath().endsWith(suffix)) {
                return ((SearchCriteria) clause).getValue();
            }
        }

        return null;
    }

    /*****************************
     * if this collection contains another sub-collection, then true, else false
     *
     * @return
     */
    public boolean containsNestedQuery(){
        for (final SQLClause c: collection) {
            if(c instanceof CriteriaCollection){
                return true;
            }
        }
        return false;
    }

}

