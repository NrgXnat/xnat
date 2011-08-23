/**
 * NotificationDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 19, 2011
 */
package org.nrg.notify.daos;

import org.nrg.notify.api.Notification;
import org.springframework.stereotype.Repository;

/**
 * Implements the DAO class for the {@link Notification} entity type.
 * 
 * @see BaseDAO
 * @author rherrick
 */
@Repository
public class NotificationDAO extends BaseDAO<Notification> {
}
