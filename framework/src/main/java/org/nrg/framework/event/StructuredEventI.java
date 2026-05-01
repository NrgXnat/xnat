/*
 * framework: org.nrg.framework.event.StructuredEventI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.event;

/**
 * The Interface StructuredEventI.
 */
public interface StructuredEventI extends EventI {

	/**
	 * Gets the src stringified id.
	 *
	 * @return the src stringified id
	 */
	String getSrcStringifiedId();
	
	/**
	 * Sets the src event class.
	 *
	 * @param srcEventClass the new src event class
	 */
	void setSrcEventClass(String srcEventClass);

	/**
	 * Gets the src event class.
	 *
	 * @return the src event class
	 */
	String getSrcEventClass();
	
	/**
	 * Sets the event id.
	 *
	 * @param eventId the new event id
	 */
	void setEventId(String eventId);

	/**
	 * Gets the event id.
	 *
	 * @return the event id
	 */
	String getEventId();

	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	void setUserId(Integer userId);

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	Integer getUserId();

	/**
	 * Sets the external id.
	 *
	 * @param projectId the new external id
	 */
	void setExternalId(String projectId);

	/**
	 * Gets the external id.
	 *
	 * @return the external id
	 */
	String getExternalId();

	/**
	 * Sets the entity id.
	 *
	 * @param entityId the new entity id
	 */
	void setEntityId(String entityId);

	/**
	 * Gets the entity id.
	 *
	 * @return the entity id
	 */
	String getEntityId();

	/**
	 * Sets the entity type.
	 *
	 * @param entityType the new entity type
	 */
	void setEntityType(String entityType);

	/**
	 * Gets the entity type.
	 *
	 * @return the entity type
	 */
	String getEntityType();

}
