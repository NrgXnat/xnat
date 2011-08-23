/**
 * NotificationSubscriberProvider
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 23, 2011
 */
package org.nrg.mail.api;

public interface NotificationSubscriberProvider {
    public abstract String[] getSubscribers(NotificationType type);
}
