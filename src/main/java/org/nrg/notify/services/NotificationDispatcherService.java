/**
 * NotificationDispatcherService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.exceptions.NrgNotificationException;

public interface NotificationDispatcherService {
    public static String SERVICE_NAME = "NotificationDispatcherService";

    /**
     * Dispatches the {@link Notification notification} to the {@link Subscriber subscribers} associated
     * with the notification's {@link Definition definition}. 
     * @param notification The notification to be dispatched.
     * @throws NrgNotificationException 
     */
    abstract public void dispatch(Notification notification) throws NrgNotificationException;
}
