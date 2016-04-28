package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.event.entities.AutomationFilters;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AutomationFiltersService extends BaseHibernateService<AutomationFilters> {
	AutomationFilters getAutomationFilters(String externalId, String srcEventClass, String column, boolean exactMatchExternalId);
	List<AutomationFilters> getAutomationFilters(String externalId, String srcEventClass, boolean exactMatchExternalId);
	List<AutomationFilters> getAutomationFilters(String externalId, boolean exactMatchExternalId);
	void saveOrUpdate(AutomationFilters filters);
}
