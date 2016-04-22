package org.nrg.xdat.services.impl.hibernate;

import java.util.List;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.AutomationEventIdsDAO;
import org.nrg.xft.event.entities.AutomationEventIds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateAutomationEventIdsService extends AbstractHibernateEntityService<AutomationEventIds, AutomationEventIdsDAO> {
	
	@Transactional
	public void saveOrUpdate(AutomationEventIds e) {
		getDao().saveOrUpdate(e);
	}
	
	@Transactional
	public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, srcEventClass, exactMatchExternalId);
	}
	
	@Transactional
	public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
		return getDao().getEventIds(projectId, exactMatchExternalId);
	}
	
}
