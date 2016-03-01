package org.nrg.automation.repositories;

import org.nrg.automation.entities.Event;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

/**
 * EventRepository class.
 */
@Repository
public class EventRepository extends AbstractHibernateDAO<Event> {
}
