/*
 * org.nrg.xdat.om.XdatFieldMappingSetI
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
public interface XdatFieldMappingSetI {

	public String getSchemaElementName();

	/**
	 * allow
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingI
	 */
	public ArrayList getAllow();

	/**
	 * Sets the value for allow.
	 * @param v Value to Set.
	 */
	public void setAllow(ItemI v) throws Exception;

	/**
	 * sub_set
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingSetI
	 */
	public ArrayList getSubSet();

	/**
	 * Sets the value for sub_set.
	 * @param v Value to Set.
	 */
	public void setSubSet(ItemI v) throws Exception;

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
	 * @return Returns the xdat_field_mapping_set_id.
	 */
	public Integer getXdatFieldMappingSetId();

	/**
	 * Sets the value for xdat_field_mapping_set_id.
	 * @param v Value to Set.
	 */
	public void setXdatFieldMappingSetId(Integer v);
}
