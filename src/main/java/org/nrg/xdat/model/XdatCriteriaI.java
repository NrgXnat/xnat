/*
 * core: org.nrg.xdat.model.XdatCriteriaI
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
public interface XdatCriteriaI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

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
}
