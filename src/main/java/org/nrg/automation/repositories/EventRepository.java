package org.nrg.automation.repositories;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.nrg.automation.entities.Event;
import org.nrg.automation.entities.Script;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * EventRepository class.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Repository
public class EventRepository extends AbstractHibernateDAO<Event> {
    /**
     *
     *
     * @return The current Hibernate session object if available.
     */
    @Override
    protected Session getSession() {
        return _session != null ? _session : super.getSession();
    }

    /**
     *
     *
     * @param session
     */
    public void setSession(final Session session) {
        _session = session;
    }

    public void clearSession() {
        _session = null;
    }

    private Session _session;
}
