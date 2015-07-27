package org.nrg.automation.services;

import org.nrg.automation.entities.Event;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.springframework.transaction.annotation.Transactional;

public interface EventService extends BaseHibernateService<Event> {
    @Transactional
    boolean hasEvent(String eventId);

    @Transactional
    Event getByEventId(String eventId);
}
