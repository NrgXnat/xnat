/*
 * core: org.nrg.xdat.model.XdatFieldMappingI
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
public interface XdatFieldMappingI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * @return Returns the field.
	 */
	public String getField();

	/**
	 * Sets the value for field.
	 * @param v Value to Set.
	 */
	public void setField(String v);

	/**
	 * @return Returns the field_value.
	 */
	public String getFieldValue();

	/**
	 * Sets the value for field_value.
	 * @param v Value to Set.
	 */
	public void setFieldValue(String v);

	/**
	 * @return Returns the create_element.
	 */
	public Boolean getCreateElement();

	/**
	 * Sets the value for create_element.
	 * @param v Value to Set.
	 */
	public void setCreateElement(Object v);

	/**
	 * @return Returns the read_element.
	 */
	public Boolean getReadElement();

	/**
	 * Sets the value for read_element.
	 * @param v Value to Set.
	 */
	public void setReadElement(Object v);

	/**
	 * @return Returns the edit_element.
	 */
	public Boolean getEditElement();

	/**
	 * Sets the value for edit_element.
	 * @param v Value to Set.
	 */
	public void setEditElement(Object v);

	/**
	 * @return Returns the delete_element.
	 */
	public Boolean getDeleteElement();

	/**
	 * Sets the value for delete_element.
	 * @param v Value to Set.
	 */
	public void setDeleteElement(Object v);

	/**
	 * @return Returns the active_element.
	 */
	public Boolean getActiveElement();

	/**
	 * Sets the value for active_element.
	 * @param v Value to Set.
	 */
	public void setActiveElement(Object v);

	/**
	 * @return Returns the comparison_type.
	 */
	public String getComparisonType();

	/**
	 * Sets the value for comparison_type.
	 * @param v Value to Set.
	 */
	public void setComparisonType(String v);

	/**
	 * @return Returns the xdat_field_mapping_id.
	 */
	public Integer getXdatFieldMappingId();
}
