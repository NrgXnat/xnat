/*
 * core: org.nrg.xdat.om.XdatSearchFieldI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatSearchFieldI {

	public String getSchemaElementName();

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName();

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v);

	/**
	 * @return Returns the field_ID.
	 */
	public String getFieldId();

	/**
	 * Sets the value for field_ID.
	 * @param v Value to Set.
	 */
	public void setFieldId(String v);

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence();

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v);

	/**
	 * @return Returns the type.
	 */
	public String getType();

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v);

	/**
	 * @return Returns the header.
	 */
	public String getHeader();

	/**
	 * Sets the value for header.
	 * @param v Value to Set.
	 */
	public void setHeader(String v);

	/**
	 * @return Returns the xdat_search_field_id.
	 */
	public Integer getXdatSearchFieldId();

	/**
	 * Sets the value for xdat_search_field_id.
	 * @param v Value to Set.
	 */
	public void setXdatSearchFieldId(Integer v);
}
