/*
 * org.nrg.notify.services.NotificationDispatcherService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services;

import org.nrg.notify.entities.Notification;
import org.nrg.notify.exceptions.*;

public interface NotificationDispatcherService {
    /**
     * Dispatches the submitted notification to the renderers requested by subscribers.
     *
     * @throws UnknownChannelRendererException    An unknown error occurred.
     * @throws InvalidChannelRendererException    The channel renderer is not specified correctly.
     * @throws ChannelRendererNotFoundException   The requested channel renderer wasn't found.
     * @throws ChannelRendererProcessingException An error occurred while rendering.
     * @throws NrgNotificationException           When an error occurs dispatching the notification.
     * @see NotificationDispatcherService#dispatch(Notification)
     */
    void dispatch(Notification notification) throws NrgNotificationException;
}
