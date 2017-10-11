/**
 * 
 */
package org.nrg.xnat.event.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class JsonResults.
 *
 * @author Atul
 */
public class JsonResults {
	private String id;

	/** The event. */
	private String event;

	/** The event class. */
	private String eventClass;

	/** The script id. */
	private String scriptId;

	/** The description. */
	private String description;

	/** The filters. */
	private HashMap<String, List<String>> filters;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * Sets the event.
	 *
	 * @param event
	 *            the new event
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * Gets the event class.
	 *
	 * @return the event class
	 */
	public String getEventClass() {
		return eventClass;
	}

	/**
	 * Sets the event class.
	 *
	 * @param eventClass
	 *            the new event class
	 */
	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	/**
	 * Gets the script id.
	 *
	 * @return the script id
	 */
	public String getScriptId() {
		return scriptId;
	}

	/**
	 * Sets the script id.
	 *
	 * @param scriptId
	 *            the new script id
	 */
	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the filters.
	 *
	 * @return the filters
	 */
	public Map<String, List<String>> getFilters() {
		return filters;
	}

	/**
	 * Sets the filters.
	 *
	 * @param filters
	 *            the filters
	 */
	public void setFilters(HashMap<String, List<String>> filters) {
		this.filters = filters;
	}
}
