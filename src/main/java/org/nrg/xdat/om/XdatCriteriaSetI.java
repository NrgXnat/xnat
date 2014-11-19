/*
 * org.nrg.xdat.om.XdatCriteriaSetI
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
public interface XdatCriteriaSetI {

	public String getSchemaElementName();

	/**
	 * criteria
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaI
	 */
	public ArrayList getCriteria();

	/**
	 * Sets the value for criteria.
	 * @param v Value to Set.
	 */
	public void setCriteria(ItemI v) throws Exception;

	/**
	 * child_set
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaSetI
	 */
	public ArrayList getChildSet();

	/**
	 * Sets the value for child_set.
	 * @param v Value to Set.
	 */
	public void setChildSet(ItemI v) throws Exception;

	/**
	 * @return Returns the method.
	 */
	public String getMethod();

	/**
	 * Sets the value for method.
	 * @param v Value to Set.
	 */
	public void setMethod(String v);

	/**
	 * @return Returns the xdat_criteria_set_id.
	 */
	public Integer getXdatCriteriaSetId();

	/**
	 * Sets the value for xdat_criteria_set_id.
	 * @param v Value to Set.
	 */
	public void setXdatCriteriaSetId(Integer v);
}
