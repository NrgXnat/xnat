/*
 * core: org.nrg.xdat.om.XdatStoredSearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om;

import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 */
public interface XdatStoredSearchI {

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
    String getSchemaElementName();

    /**
     * @return Returns the root_element_name.
     */
    String getRootElementName();

    /**
     * Sets the value for root_element_name.
     *
     * @param v Value to Set.
     */
    void setRootElementName(String v);

    /**
     * search_field
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatSearchFieldI
     */
    ArrayList getSearchField();

    /**
     * Sets the value for search_field.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setSearchField(ItemI v) throws Exception;

    /**
     * search_where
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaSetI
     */
    ArrayList getSearchWhere();

    /**
     * Sets the value for search_where.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setSearchWhere(ItemI v) throws Exception;

    /**
     * @return Returns the sort_by/element_name.
     */
    String getSortBy_elementName();

    /**
     * Sets the value for sort_by/element_name.
     *
     * @param v Value to Set.
     */
    void setSortBy_elementName(String v);

    /**
     * @return Returns the sort_by/field_ID.
     */
    String getSortBy_fieldId();

    /**
     * Sets the value for sort_by/field_ID.
     *
     * @param v Value to Set.
     */
    void setSortBy_fieldId(String v);

    /**
     * allowed_user
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatStoredSearchAllowedUserI
     */
    ArrayList getAllowedUser();

    /**
     * Sets the value for allowed_user.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setAllowedUser(ItemI v) throws Exception;

    /**
     * @return Returns the ID.
     */
    String getId();

    /**
     * Sets the value for ID.
     *
     * @param v Value to Set.
     */
    void setId(String v);

    /**
     * @return Returns the description.
     */
    String getDescription();

    /**
     * Sets the value for description.
     *
     * @param v Value to Set.
     */
    void setDescription(String v);

    /**
     * @return Returns the layeredSequence.
     */
    @SuppressWarnings("unused")
    String getLayeredsequence();

    /**
     * Sets the value for layeredSequence.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setLayeredsequence(String v);

    /**
     * @return Returns the allow-diff-columns.
     */
    Boolean getAllowDiffColumns();

    /**
     * Sets the value for allow-diff-columns.
     *
     * @param v Value to Set.
     */
    void setAllowDiffColumns(Object v);

    /**
     * @return Returns the secure.
     */
    Boolean getSecure();

    /**
     * Sets the value for secure.
     *
     * @param v Value to Set.
     */
    void setSecure(Object v);

    /**
     * @return Returns the brief-description.
     */
    String getBriefDescription();

    /**
     * Sets the value for brief-description.
     *
     * @param v Value to Set.
     */
    void setBriefDescription(String v);
}
