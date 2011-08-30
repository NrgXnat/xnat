/**
 * HibernateNotificationDispatcherService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.ChannelRendererNotFoundException;
import org.nrg.notify.exceptions.ChannelRendererProcessingException;
import org.nrg.notify.exceptions.InvalidChannelRendererException;
import org.nrg.notify.exceptions.NrgNotificationException;
import org.nrg.notify.exceptions.UnknownChannelRendererException;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.services.ChannelRendererService;
import org.nrg.notify.services.NotificationDispatcherService;
import org.springframework.stereotype.Service;

/**
 * The Class DefaultNotificationDispatcherServiceImpl.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>

 */
@Service
public class DefaultNotificationDispatcherService implements NotificationDispatcherService {

    /**
     * @throws UnknownChannelRendererException 
     * @throws InvalidChannelRendererException 
     * @throws ChannelRendererNotFoundException 
     * @throws ChannelRendererProcessingException 
     * @see NotificationDispatcherService#dispatch(Notification)
     */
    @Override
    public void dispatch(Notification notification) throws NrgNotificationException {
        for(Subscription subscription : notification.getDefinition().getSubscriptions()) {
            for (Channel channel : subscription.getChannels()) {
                String name = channel.getName();

                ChannelRenderer renderer;
                if (!_renderers.containsKey(name)) {
                    renderer = _rendererService.getRenderer(name);
                    _renderers.put(name, renderer);
                } else {
                    renderer = _renderers.get(name);
                }
                
                renderer.render(subscription, notification, channel.getFormat());
            }
        }
    }

    @Inject
    private ChannelRendererService _rendererService;
    private Map<String, ChannelRenderer> _renderers = new HashMap<String, ChannelRenderer>();
}
