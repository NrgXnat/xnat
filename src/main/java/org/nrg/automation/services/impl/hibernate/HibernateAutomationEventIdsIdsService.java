package org.nrg.automation.services.impl.hibernate;

import java.util.List;

import org.nrg.automation.daos.AutomationEventIdsIdsDAO;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateAutomationEventIdsIdsService.
 */
@Service
public class HibernateAutomationEventIdsIdsService extends AbstractHibernateEntityService<AutomationEventIdsIds, AutomationEventIdsIdsDAO> implements AutomationEventIdsIdsService {
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsIdsService#saveOrUpdate(org.nrg.automation.event.entities.AutomationEventIdsIds)
	 */
	@Override
	@Transactional
	public void saveOrUpdate(AutomationEventIdsIds e) {
		getDao().saveOrUpdate(e);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@Transactional
	public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, String eventId, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, srcEventClass, eventId, exactMatchExternalId);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@Transactional
	public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, srcEventClass, exactMatchExternalId);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, boolean)
	 */
	@Override
	@Transactional
	public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, exactMatchExternalId);
	}

	/* (non-Javadoc)
	 * @see org.nrg.automation.services.AutomationEventIdsIdsService#getEventIds(java.lang.String, boolean, int)
	 */
	@Override
	@Transactional
	public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId, int max_per_type) {
		return getDao().getEventIds(projectId, exactMatchExternalId, max_per_type);
	}
}
