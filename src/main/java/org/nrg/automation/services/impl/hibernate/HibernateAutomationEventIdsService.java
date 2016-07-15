package org.nrg.automation.services.impl.hibernate;

import java.util.List;

import org.nrg.automation.daos.AutomationEventIdsDAO;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateAutomationEventIdsService.
 */
@Service
public class HibernateAutomationEventIdsService extends AbstractHibernateEntityService<AutomationEventIds, AutomationEventIdsDAO> implements AutomationEventIdsService {
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsService#saveOrUpdate(org.nrg.automation.event.entities.AutomationEventIds)
	 */
	@Override
	@Transactional
	public void saveOrUpdate(AutomationEventIds e) {
		getDao().saveOrUpdate(e);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsService#getEventIds(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@Transactional
	public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, srcEventClass, exactMatchExternalId);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsService#getEventIds(java.lang.String, boolean)
	 */
	@Override
	@Transactional
	public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, exactMatchExternalId);
	}

	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsService#getEventIds(org.nrg.automation.event.AutomationEventImplementerI)
	 */
	@Override
	@Transactional
	public List<AutomationEventIds> getEventIds(AutomationEventImplementerI eventData) {
		return getDao().getEventIds(eventData.getExternalId(), eventData.getSrcEventClass(), true);
	}
}
