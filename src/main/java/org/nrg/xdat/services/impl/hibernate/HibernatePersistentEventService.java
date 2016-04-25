package org.nrg.xdat.services.impl.hibernate;

import java.util.List;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.PersistentEventDAO;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.entities.PersistentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernatePersistentEventService.
 */
@Service
public class HibernatePersistentEventService extends AbstractHibernateEntityService<PersistentEvent, PersistentEventDAO> {
	
	/**
	 * Save persistent event.
	 *
	 * @param e the e
	 */
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
	@Transactional
	public List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId) {
		return getDao().getDistinctValues(clazz,column,projectId);
	}
	
}
