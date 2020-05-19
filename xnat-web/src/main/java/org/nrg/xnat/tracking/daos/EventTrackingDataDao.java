/*
 * web: org.nrg.xnat.daos.EventListenerDataDao
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.springframework.stereotype.Repository;

@Repository
public class EventTrackingDataDao extends AbstractHibernateDAO<EventTrackingData> {
}
