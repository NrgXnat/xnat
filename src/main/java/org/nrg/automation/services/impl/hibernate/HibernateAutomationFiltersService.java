package org.nrg.automation.services.impl.hibernate;

import java.util.List;

import org.nrg.automation.daos.AutomationFiltersDAO;
import org.nrg.automation.event.entities.AutomationFilters;
import org.nrg.automation.services.AutomationFiltersService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateAutomationFiltersService.
 */
@Service
public class HibernateAutomationFiltersService extends AbstractHibernateEntityService<AutomationFilters, AutomationFiltersDAO> implements AutomationFiltersService {
	
	/**
	 * Gets the automation filters.
	 *
	 * @param externalId the external id
	 * @param srcEventClass the src event class
	 * @param column the column
	 * @param exactMatchExternalId the exact match external id
	 * @return the automation filters
	 */
	@Override
	@Transactional
	public AutomationFilters getAutomationFilters(String externalId, String srcEventClass, String column, boolean exactMatchExternalId) {
		return getDao().getAutomationFilters(externalId, srcEventClass, column, exactMatchExternalId);
	}
	
	/**
	 * Gets the automation filters.
	 *
	 * @param externalId the external id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the automation filters
	 */
	@Override
	@Transactional
	public List<AutomationFilters> getAutomationFilters(String externalId, String srcEventClass, boolean exactMatchExternalId) {
		return getDao().getAutomationFilters(externalId, srcEventClass, exactMatchExternalId);
	}
	
	/**
	 * Gets the automation filters.
	 *
	 * @param externalId the external id
	 * @param exactMatchExternalId the exact match external id
	 * @return the automation filters
	 */
	@Override
	@Transactional
	public List<AutomationFilters> getAutomationFilters(String externalId, boolean exactMatchExternalId) {
		return getDao().getAutomationFilters(externalId, exactMatchExternalId);
	}

	/**
	 * Save or update.
	 *
	 * @param filters the filters
	 */
	@Override
	@Transactional
	public void saveOrUpdate(AutomationFilters filters) {
		if (filters.getClass()!=null && filters.getSrcEventClass()!=null && filters.getField()!=null) {
			getDao().saveOrUpdate(filters);
		}
	}
	
}
