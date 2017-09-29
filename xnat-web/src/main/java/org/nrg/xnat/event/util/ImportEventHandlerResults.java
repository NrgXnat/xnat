package org.nrg.xnat.event.util;

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
	public JsonResults[] getEventHandlers() {
		return eventHandlers;
	}

	/**
	 * @param arr
	 *            the arr to set
	 */
	public void setEventHandlers(JsonResults[] eventHandlers) {
		this.eventHandlers = eventHandlers;
	}
}
