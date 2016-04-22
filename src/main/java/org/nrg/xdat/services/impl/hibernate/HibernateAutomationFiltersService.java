package org.nrg.xdat.services.impl.hibernate;

import java.util.List;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.AutomationFiltersDAO;
import org.nrg.xft.event.entities.AutomationFilters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateAutomationFiltersService extends AbstractHibernateEntityService<AutomationFilters, AutomationFiltersDAO> {
	
	@Transactional
	public AutomationFilters getAutomationFilters(String externalId,String srcEventClass,String column, boolean exactMatchExternalId) {
		return getDao().getAutomationFilters(externalId, srcEventClass, column, exactMatchExternalId);
	}
	
	@Transactional
	public List<AutomationFilters> getAutomationFilters(String externalId,String srcEventClass, boolean exactMatchExternalId) {
		return getDao().getAutomationFilters(externalId, srcEventClass, exactMatchExternalId);
	}
	
	@Transactional
	public List<AutomationFilters> getAutomationFilters(String externalId, boolean exactMatchExternalId) {
		return getDao().getAutomationFilters(externalId, exactMatchExternalId);
	}

	@Transactional
	public void saveOrUpdate(AutomationFilters filters) {
		if (filters.getClass()!=null && filters.getSrcEventClass()!=null && filters.getField()!=null) {
			getDao().saveOrUpdate(filters);
		}
	}
	
}
