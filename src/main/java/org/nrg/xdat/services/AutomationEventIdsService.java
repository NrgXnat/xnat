package org.nrg.xdat.services;

import org.nrg.xft.event.entities.AutomationEventIds;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rherrick on 4/27/16.
 */
public interface AutomationEventIdsService {
	@Transactional
	void saveOrUpdate(AutomationEventIds e);

	@Transactional
	List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId);

	@Transactional
	List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId);
}
