/*
 * org.nrg.automation.services.AutomationEventIdsService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rherrick on 4/27/16.
 */
public interface AutomationEventIdsService {
	
	/**
	 * Save or update.
	 *
	 * @param e the e
	 */
	@Transactional
	void saveOrUpdate(AutomationEventIds e);

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId);

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId);

	/**
	 * Gets the event ids.
	 *
	 * @param eventData the event data
	 * @return the event ids
	 */
	@Transactional
	List<AutomationEventIds> getEventIds(AutomationEventImplementerI eventData);
}
