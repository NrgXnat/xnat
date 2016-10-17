/*
 * core: org.nrg.xdat.model.XdatStoredSearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Thu Mar 31 18:38:30 CDT 2016
 *
 */
package org.nrg.xdat.model;

import java.util.List;

/**
 * @author XDAT
 *
 */
public interface XdatStoredSearchI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * @return Returns the root_element_name.
	 */
	public String getRootElementName();

	/**
	 * Sets the value for root_element_name.
	 * @param v Value to Set.
	 */
	public void setRootElementName(String v);

	/**
	 * search_field
	 * @return Returns an List of org.nrg.xdat.model.XdatSearchFieldI
	 */
	public <A extends org.nrg.xdat.model.XdatSearchFieldI> List<A> getSearchField();

	/**
	 * search_field
	 */
	public <A extends org.nrg.xdat.model.XdatSearchFieldI> void addSearchField(A item) throws Exception;

	/**
	 * search_where
	 * @return Returns an List of org.nrg.xdat.model.XdatCriteriaSetI
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaSetI> List<A> getSearchWhere();

	/**
	 * search_where
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaSetI> void addSearchWhere(A item) throws Exception;

	/**
	 * @return Returns the sort_by/element_name.
	 */
	public String getSortBy_elementName();

	/**
	 * Sets the value for sort_by/element_name.
	 * @param v Value to Set.
	 */
	public void setSortBy_elementName(String v);

	/**
	 * @return Returns the sort_by/field_ID.
	 */
	public String getSortBy_fieldId();

	/**
	 * Sets the value for sort_by/field_ID.
	 * @param v Value to Set.
	 */
	public void setSortBy_fieldId(String v);

	/**
	 * allowed_user
	 * @return Returns an List of org.nrg.xdat.model.XdatStoredSearchAllowedUserI
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchAllowedUserI> List<A> getAllowedUser();

	/**
	 * allowed_user
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchAllowedUserI> void addAllowedUser(A item) throws Exception;

	/**
	 * allowed_groups/groupID
	 * @return Returns an List of org.nrg.xdat.model.XdatStoredSearchGroupidI
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchGroupidI> List<A> getAllowedGroups_groupid();

	/**
	 * allowed_groups/groupID
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchGroupidI> void addAllowedGroups_groupid(A item) throws Exception;

	/**
	 * @return Returns the ID.
	 */
	public String getId();

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v);

	/**
	 * @return Returns the description.
	 */
	public String getDescription();

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v);

	/**
	 * @return Returns the layeredSequence.
	 */
	public String getLayeredsequence();

	/**
	 * Sets the value for layeredSequence.
	 * @param v Value to Set.
	 */
	public void setLayeredsequence(String v);

	/**
	 * @return Returns the allow-diff-columns.
	 */
	public Boolean getAllowDiffColumns();

	/**
	 * Sets the value for allow-diff-columns.
	 * @param v Value to Set.
	 */
	public void setAllowDiffColumns(Object v);

	/**
	 * @return Returns the secure.
	 */
	public Boolean getSecure();

	/**
	 * Sets the value for secure.
	 * @param v Value to Set.
	 */
	public void setSecure(Object v);

	/**
	 * @return Returns the brief-description.
	 */
	public String getBriefDescription();

	/**
	 * Sets the value for brief-description.
	 * @param v Value to Set.
	 */
	public void setBriefDescription(String v);

	/**
	 * @return Returns the tag.
	 */
	public String getTag();

	/**
	 * Sets the value for tag.
	 * @param v Value to Set.
	 */
	public void setTag(String v);
}
