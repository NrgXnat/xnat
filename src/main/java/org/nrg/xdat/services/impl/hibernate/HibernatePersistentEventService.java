package org.nrg.xdat.services.impl.hibernate;

import java.util.List;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.PersistentEventDAO;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.entities.PersistentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernatePersistentEventService extends AbstractHibernateEntityService<PersistentEvent, PersistentEventDAO> {
	
	@Transactional
	public void savePersistentEvent(PersistentEvent e) {
		getDao().create(e);
	}
	
	@Transactional
	public List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId) {
		return getDao().getDistinctValues(clazz,column,projectId);
	}
	
}
