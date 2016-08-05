package org.nrg.xapi.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.nrg.framework.event.EventClass;
import org.python.google.common.collect.Maps;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class EventClassInfo.
 */
@ApiModel(description = "Event class names and filterable fields.")
public class EventHandlerFilterInfo {
	
	String _defaultValue;
	
	List<String> _filterValues;
	
	Boolean _filterRequired;
	
	Boolean _includeValuesFromDatabase;

	
    @ApiModelProperty(value = "Default selected value for event filter values")
    @JsonProperty("defaultValue")
    
	public String getDefaultValue() {
		return _defaultValue;
	}

	public void setDefaultValue(String _defaultValue) {
		this._defaultValue = _defaultValue;
	}

    @ApiModelProperty(value = "Filter Values")
    @JsonProperty("filterValues")
	public List<String> getFilterValues() {
		return _filterValues;
	}

	public void setFilterValues(List<String> _filterValues) {
		this._filterValues = _filterValues;
	}

    @ApiModelProperty(value = "Filter Required?")
    @JsonProperty("filterRequired")
	public Boolean getFilterRequired() {
		return _filterRequired;
	}

	public void setFilterRequired(Boolean _filterRequired) {
		this._filterRequired = _filterRequired;
	}

    @ApiModelProperty(value = "Include Values From Database?")
    @JsonProperty("includeValuesFromDatabase")
	public Boolean getIncludeValuesFromDatabase() {
		return _includeValuesFromDatabase;
	}

	public void setIncludeValuesFromDatabase(Boolean _includeValuesFromDatabase) {
		this._includeValuesFromDatabase = _includeValuesFromDatabase;
	}

}
