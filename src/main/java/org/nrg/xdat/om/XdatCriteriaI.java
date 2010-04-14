// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:53 CST 2007
 *
 */
package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatCriteriaI {

	public String getSchemaElementName();

	/**
	 * @return Returns the schema_field.
	 */
	public String getSchemaField();

	/**
	 * Sets the value for schema_field.
	 * @param v Value to Set.
	 */
	public void setSchemaField(String v);

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
	 * @return Returns the custom_search.
	 */
	public String getCustomSearch();

	/**
	 * Sets the value for custom_search.
	 * @param v Value to Set.
	 */
	public void setCustomSearch(String v);

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
	 * @return Returns the override_value_formatting.
	 */
	public Boolean getOverrideValueFormatting();

	/**
	 * Sets the value for override_value_formatting.
	 * @param v Value to Set.
	 */
	public void setOverrideValueFormatting(Object v);

	/**
	 * @return Returns the xdat_criteria_id.
	 */
	public Integer getXdatCriteriaId();

	/**
	 * Sets the value for xdat_criteria_id.
	 * @param v Value to Set.
	 */
	public void setXdatCriteriaId(Integer v);
}
