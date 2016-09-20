/*
 * org.nrg.mail.api.NotificationSubscriberProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.api;

public interface NotificationSubscriberProvider {
    public abstract String[] getSubscribers(NotificationType type);
}
