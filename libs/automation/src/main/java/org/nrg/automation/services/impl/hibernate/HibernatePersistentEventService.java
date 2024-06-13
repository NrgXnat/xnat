/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernatePersistentEventService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl.hibernate;

import java.util.List;

import org.nrg.automation.daos.PersistentEventDAO;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.automation.services.PersistentEventService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernatePersistentEventService.
 */
@Service
public class HibernatePersistentEventService extends AbstractHibernateEntityService<PersistentEvent, PersistentEventDAO> implements PersistentEventService {
	
	/**
	 * Save persistent event.
	 *
	 * @param e the e
	 */
	@Override
	@Transactional
	public void savePersistentEvent(PersistentEvent e) {
		getDao().create(e);
	}
	
	/**
	 * Gets the distinct values.
	 *
	 * @param clazz the clazz
	 * @param column the column
	 * @param projectId the project id
	 * @return the distinct values
	 */
	@Override
	@Transactional
	public List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId) {
		return getDao().getDistinctValues(clazz,column,projectId);
	}
	
}
