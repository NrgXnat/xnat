package org.nrg.xapi.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.nrg.xft.event.EventClass;
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
public class EventClassInfo {
	
	/** The _class name. */
	String _className;
	
	/** The _display name. */
	String _displayName;
	
	/** The _filterable fields. */
	final Map<String,List<String>> _filterableFields = Maps.newHashMap();
	
	/** The _event ids. */
	final List<String> _eventIds = Lists.newArrayList();
	
	/** The _include event ids from database. */
	boolean _includeEventIdsFromDatabase = true;

	/**
	 * Instantiates a new event class info.
	 *
	 * @param className the class name
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EventClassInfo(final String className) {
        this._className = className;
        try {
			Class clazz = Class.forName(className);
			if (clazz.isAnnotationPresent(EventClass.class)) {
				
				Annotation anno = AnnotationUtils.findAnnotation(clazz, EventClass.class);
				Object annoDisplayNameObj = AnnotationUtils.getValue(anno, "displayName");
				_displayName = (annoDisplayNameObj != null && annoDisplayNameObj instanceof String) ? annoDisplayNameObj.toString() : _className;
				
				Object annoEventIdsObj = AnnotationUtils.getValue(anno, "defaultEventIds");
				String[] annoEventIds = (annoEventIdsObj != null && annoEventIdsObj instanceof String[]) ? (String[])annoEventIdsObj : new String[] {};
				_eventIds.addAll(Arrays.asList(annoEventIds));
				
				Object annoIncludeValuesFromDatabase = AnnotationUtils.getValue(anno, "includeValuesFromDatabase");
				if (annoIncludeValuesFromDatabase != null && annoIncludeValuesFromDatabase instanceof Boolean) {
					_includeEventIdsFromDatabase = (boolean)annoIncludeValuesFromDatabase;
				}
				
			} else {
				_displayName = _className;
			}
		} catch (ClassNotFoundException e) {
			_displayName = _className;
		}
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    @ApiModelProperty(value = "Event class name (Should implement AutomationEventImplementerI")
    @JsonProperty("class")
    public String getClassName() {
        return _className;
    }
    
    /**
     * Gets the display name.
     *
     * @return the display name
     */
    @ApiModelProperty(value = "Display Name")
    @JsonProperty("displayName")
    public String getDisplayName() {
        return _displayName;
    }

    /**
     * Sets the class name.
     *
     * @param className the new class name
     */
    public void setClassName(String className) {
        _className = className;
    }

    /**
     * Gets the filterable fields map.
     *
     * @return the filterable fields map
     */
    @ApiModelProperty(value = "Map of Filterable fields.")
    @JsonProperty("filterableFields")
    public Map<String,List<String>> getFilterableFieldsMap() {
        return _filterableFields;
    }
    
    /**
     * Gets the event ids.
     *
     * @return the event ids
     */
    @ApiModelProperty(value = "List of event IDs.")
    @JsonProperty("eventIds")
    public List<String> getEventIds() {
        return _eventIds;
    }
	
    /**
     * Gets the include event ids from database.
     *
     * @return the include event ids from database
     */
    public boolean getIncludeEventIdsFromDatabase() {
		return _includeEventIdsFromDatabase;
	}

	/**
	 * Sets the include event ids from database.
	 *
	 * @param _includeEventIdsFromDatabase the new include event ids from database
	 */
	public void setIncludeEventIdsFromDatabase(boolean _includeEventIdsFromDatabase) {
		this._includeEventIdsFromDatabase = _includeEventIdsFromDatabase;
	}
    
}
