/*
 * notify: org.nrg.notify.services.impl.DefaultNotificationDispatcherService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services.impl;

import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Notification;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.*;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.services.ChannelRendererService;
import org.nrg.notify.services.NotificationDispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultNotificationDispatcherService implements NotificationDispatcherService {
    @Autowired
    public DefaultNotificationDispatcherService(final ChannelRendererService rendererService) {
        _rendererService = rendererService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void dispatch(Notification notification) throws NrgNotificationException {
        for (Subscription subscription : notification.getDefinition().getSubscriptions()) {
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

    private final ChannelRendererService _rendererService;
    private final Map<String, ChannelRenderer> _renderers = new HashMap<>();
}
