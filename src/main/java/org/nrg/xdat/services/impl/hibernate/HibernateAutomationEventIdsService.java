package org.nrg.xdat.services.impl.hibernate;

import java.util.List;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.AutomationEventIdsDAO;
import org.nrg.xdat.services.AutomationEventIdsService;
import org.nrg.xft.event.entities.AutomationEventIds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateAutomationEventIdsService.
 */
@Service
public class HibernateAutomationEventIdsService extends AbstractHibernateEntityService<AutomationEventIds, AutomationEventIdsDAO> implements AutomationEventIdsService {
	
	/**
	 * Save or update.
	 *
	 * @param e the e
	 */
	@Override
	@Transactional
	public void saveOrUpdate(AutomationEventIds e) {
		getDao().saveOrUpdate(e);
	}
	
	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Override
	@Transactional
	public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, srcEventClass, exactMatchExternalId);
	}
	
	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@Override
	@Transactional
	public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, exactMatchExternalId);
	}
}
