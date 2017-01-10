/*
 * automation: org.nrg.automation.repositories.EventRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
