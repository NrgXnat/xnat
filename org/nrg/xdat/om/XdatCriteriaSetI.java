// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:53 CST 2007
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
