package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.entities.PersistentEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PersistentEventService extends BaseHibernateService<PersistentEvent> {
	void savePersistentEvent(PersistentEvent e);
	List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId);
}
