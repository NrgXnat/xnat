/*
 * notify: org.nrg.notify.services.impl.hibernate.HibernateChannelService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.ChannelDAO;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.services.ChannelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HibernateChannelService extends AbstractHibernateEntityService<Channel, ChannelDAO> implements ChannelService {
    /**
     * A shortcut method for quickly creating a new channel.
     *
     * @param name   The name of the channel.
     * @param format The format supported by the channel.
     *
     * @return The newly created channel object.
     *
     * @see ChannelService#createChannel(String, String)
     */
    @Override
    @Transactional
    public Channel createChannel(String name, String format) {
        log.debug("Creating a new channel: {}, format: {}", name, format);
        Channel channel = newEntity();
        channel.setName(name);
        channel.setFormat(format);
        getDao().create(channel);
        return channel;
    }

    /**
     * Retrieves the channel with the indicated name.
     *
     * @param name The name of the channel to retrieve.
     *
     * @return The indicated channel.
     */
    @Override
    @Transactional
    public Channel getChannel(String name) {
        log.debug("Getting channel by name: {}", name);
        return getDao().getChannelByName(name);
    }
}
