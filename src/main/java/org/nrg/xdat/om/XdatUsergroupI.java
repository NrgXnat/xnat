/*
 * org.nrg.xdat.om.XdatUsergroupI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatUsergroupI {

	public String getSchemaElementName();

	/**
	 * element_access
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessI
	 */
	public ArrayList getElementAccess();

	/**
	 * Sets the value for element_access.
	 * @param v Value to Set.
	 */
	public void setElementAccess(ItemI v) throws Exception;

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
	 * @return Returns the displayName.
	 */
	public String getDisplayname();

	/**
	 * Sets the value for displayName.
	 * @param v Value to Set.
	 */
	public void setDisplayname(String v);

	/**
	 * @return Returns the xdat_userGroup_id.
	 */
	public Integer getXdatUsergroupId();

	/**
	 * Sets the value for xdat_userGroup_id.
	 * @param v Value to Set.
	 */
	public void setXdatUsergroupId(Integer v);
}
