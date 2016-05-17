package org.nrg.automation.services;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PersistentEventService extends BaseHibernateService<PersistentEvent> {
	void savePersistentEvent(PersistentEvent e);
	List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId);
}
