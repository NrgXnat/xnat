/*
 * notify: org.nrg.notify.daos.NotificationDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Notification;
import org.springframework.stereotype.Repository;


@Repository
public class NotificationDAO extends AbstractHibernateDAO<Notification> {

}
