// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:52 CST 2007
 *
 */
package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatFieldMappingI {

	public String getSchemaElementName();

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

	/**
	 * Sets the value for xdat_field_mapping_id.
	 * @param v Value to Set.
	 */
	public void setXdatFieldMappingId(Integer v);
}
