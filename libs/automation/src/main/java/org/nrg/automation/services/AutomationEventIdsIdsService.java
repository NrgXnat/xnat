/*
 * automation: org.nrg.automation.services.AutomationEventIdsIdsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The Interface AutomationEventIdsIdsService.
 */
public interface AutomationEventIdsIdsService {
	
	/**
	 * Save or update.
	 *
	 * @param e the e
	 */
	@Transactional
	void saveOrUpdate(AutomationEventIdsIds e);

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param eventId the event id
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, String eventId, boolean exactMatchExternalId);

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId);

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId);

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param exactMatchExternalId the exact match external id
	 * @param max_per_type the max_per_type
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId, int max_per_type);

}
