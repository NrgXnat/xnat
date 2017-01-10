/*
 * core: org.nrg.xdat.om.XdatUsergroupI
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
 *
 */
public interface XdatUsergroupI {

	String getSchemaElementName();

	/**
	 * element_access
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessI
	 */
	ArrayList getElementAccess();

	/**
	 * Sets the value for element_access.
	 * @param v Value to Set.
	 * @throws Exception When an error occurs.
	 */
	void setElementAccess(ItemI v) throws Exception;

	/**
	 * @return Returns the ID.
	 */
	String getId();

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	void setId(String v);

	/**
	 * @return Returns the displayName.
	 */
	String getDisplayname();

	/**
	 * Sets the value for displayName.
	 * @param v Value to Set.
	 */
	void setDisplayname(String v);

	/**
	 * @return Returns the xdat_userGroup_id.
	 */
	Integer getXdatUsergroupId();

	/**
	 * Sets the value for xdat_userGroup_id.
	 * @param v Value to Set.
	 */
    @SuppressWarnings("unused")
	void setXdatUsergroupId(Integer v);
}
