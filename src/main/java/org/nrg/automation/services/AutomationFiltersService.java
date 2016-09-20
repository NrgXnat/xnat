/*
 * org.nrg.automation.services.AutomationFiltersService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.event.entities.AutomationFilters;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AutomationFiltersService extends BaseHibernateService<AutomationFilters> {
	AutomationFilters getAutomationFilters(String externalId, String srcEventClass, String column, boolean exactMatchExternalId);
	List<AutomationFilters> getAutomationFilters(String externalId, String srcEventClass, boolean exactMatchExternalId);
	List<AutomationFilters> getAutomationFilters(String externalId, boolean exactMatchExternalId);
	void saveOrUpdate(AutomationFilters filters);
}
