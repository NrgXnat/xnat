/**
 * ChannelRenderer
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.renderers;

import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.ChannelRendererProcessingException;

public interface ChannelRenderer {
    abstract public void render(Subscription subscription, Notification notification) throws ChannelRendererProcessingException;
    abstract public void render(Subscription subscription, Notification notification, String format) throws ChannelRendererProcessingException;
}
