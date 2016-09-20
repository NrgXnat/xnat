/*
 * org.nrg.notify.renderers.ChannelRenderer
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.renderers;

import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.ChannelRendererProcessingException;

public interface ChannelRenderer {
    abstract public void render(Subscription subscription, Notification notification) throws ChannelRendererProcessingException;
    abstract public void render(Subscription subscription, Notification notification, String format) throws ChannelRendererProcessingException;
}
