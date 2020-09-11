/*
 * web: org.nrg.xnat.tracking.daos.EventTrackingDataDao
 * XNAT http://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class EventTrackingDataDao extends AbstractHibernateDAO<EventTrackingData> {
    /**
     * Delete event tracking data last updated prior to expiration
     * @param expiration the expiration date
     */
    public void deleteEntriesLastUpdatedBefore(Date expiration) {
        getSession().createQuery("DELETE from EventTrackingData where timestamp < :expiration").setDate("expiration", expiration).executeUpdate();
    }
}
