package org.nrg.xnat.event.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The Class ImportEventHandlerResults.
 */
public class ImportEventHandlerResults {
	
	/** The event handlers. */
	private JsonResults eventHandlers[];
	
	/** The is import. */
	private boolean isImport = false;

	/** The source project id. */
	private String sourceProjectId;

	/**
	 * @return the sourceProjectId
	 */
	public String getSourceProjectId() {
		return sourceProjectId;
	}

	/**
	 * @param sourceProjectId
	 *            the sourceProjectId to set
	 */
	public void setSourceProjectId(String sourceProjectId) {
		this.sourceProjectId = sourceProjectId;
	}

	/**
	 * @return the isImport
	 */
	public boolean isImport() {
		return isImport;
	}

	/**
	 * @param isImport
	 *            the isImport to set
	 */
	public void setImport(boolean isImport) {
		this.isImport = isImport;
	}

	/**
	 * @return the arr
	 */
	public JsonResults[] getJsonResults() {
		return eventHandlers;
	}

	/**
	 * @param arr
	 *            the arr to set
	 */
	public void setArr(JsonResults[] eventHandlers) {
		this.eventHandlers = eventHandlers;
	}

	/**
	 * Gets the object from JSON.
	 *
	 * @param jsonString the json string
	 * @return the object from JSON
	 */
	public ImportEventHandlerResults getObjectFromJSON(String jsonString) {
		final GsonBuilder builder = new GsonBuilder().serializeNulls();
		final Gson gson = builder.create();
		return gson.fromJson(jsonString, ImportEventHandlerResults.class);
	}
}
