/*
 * core: org.nrg.xdat.model.XdatSearchFieldI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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
public interface XdatSearchFieldI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

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
	 * Sets the value for xdat:search_field/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) ;

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
	 * @return Returns the value.
	 */
	public String getValue();

	/**
	 * Sets the value for value.
	 * @param v Value to Set.
	 */
	public void setValue(String v);

	/**
	 * @return Returns the visible.
	 */
	public Boolean getVisible();

	/**
	 * Sets the value for visible.
	 * @param v Value to Set.
	 */
	public void setVisible(Object v);

	/**
	 * @return Returns the xdat_search_field_id.
	 */
	public Integer getXdatSearchFieldId();
}
