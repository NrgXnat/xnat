// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:52 CST 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;

import java.util.*;

/**
 * @author XDAT
 *
 */
public interface XdatStoredSearchI {

	public String getSchemaElementName();

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
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatSearchFieldI
	 */
	public ArrayList getSearchField();

	/**
	 * Sets the value for search_field.
	 * @param v Value to Set.
	 */
	public void setSearchField(ItemI v) throws Exception;

	/**
	 * search_where
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaSetI
	 */
	public ArrayList getSearchWhere();

	/**
	 * Sets the value for search_where.
	 * @param v Value to Set.
	 */
	public void setSearchWhere(ItemI v) throws Exception;

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
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatStoredSearchAllowedUserI
	 */
	public ArrayList getAllowedUser();

	/**
	 * Sets the value for allowed_user.
	 * @param v Value to Set.
	 */
	public void setAllowedUser(ItemI v) throws Exception;

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
}
