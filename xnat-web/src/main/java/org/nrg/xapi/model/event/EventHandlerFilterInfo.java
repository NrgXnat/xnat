/*
 * web: org.nrg.xapi.model.event.EventHandlerFilterInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The Class EventClassInfo.
 */
@Schema(description = "Event class names and filterable fields.")
public class EventHandlerFilterInfo {
	
	String _defaultValue;
	
	List<String> _filterValues;
	
	Boolean _filterRequired;
	
	Boolean _includeValuesFromDatabase;

	
    @Schema(description = "Default selected value for event filter values")
    @JsonProperty("defaultValue")
    
	public String getDefaultValue() {
		return _defaultValue;
	}

	public void setDefaultValue(String _defaultValue) {
		this._defaultValue = _defaultValue;
	}

    @Schema(description = "Filter Values")
    @JsonProperty("filterValues")
	public List<String> getFilterValues() {
		return _filterValues;
	}

	public void setFilterValues(List<String> _filterValues) {
		this._filterValues = _filterValues;
	}

    @Schema(description = "Filter Required?")
    @JsonProperty("filterRequired")
	public Boolean getFilterRequired() {
		return _filterRequired;
	}

	public void setFilterRequired(Boolean _filterRequired) {
		this._filterRequired = _filterRequired;
	}

    @Schema(description = "Include Values From Database?")
    @JsonProperty("includeValuesFromDatabase")
	public Boolean getIncludeValuesFromDatabase() {
		return _includeValuesFromDatabase;
	}

	public void setIncludeValuesFromDatabase(Boolean _includeValuesFromDatabase) {
		this._includeValuesFromDatabase = _includeValuesFromDatabase;
	}

}
