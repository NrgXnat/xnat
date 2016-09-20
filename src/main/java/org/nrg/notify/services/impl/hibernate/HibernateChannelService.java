/*
 * org.nrg.notify.services.impl.hibernate.HibernateChannelService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services.impl.hibernate;

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.ChannelDAO;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.services.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class HibernateChannelService extends AbstractHibernateEntityService<Channel, ChannelDAO> implements ChannelService {

    /**
     * A shortcut method for quickly creating a new channel.
     * @param name The name of the channel.
     * @param format The format supported by the channel.
     * @return The newly created channel object.
     * @see ChannelService#createChannel(String, String)
     */
    @Override
    @Transactional
    public Channel createChannel(String name, String format) throws NrgServiceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a new channel: " + name + ", format: " + format);
        }
        Channel channel = newEntity();
        channel.setName(name);
        channel.setFormat(format);
        getDao().create(channel);
        return channel;
    }

    /**
     * Retrieves the channel with the indicated name.
     * @param name The name of the channel to retrieve.
     * @return The indicated channel.
     */
    @Override
    @Transactional
    public Channel getChannel(String name) {
        if (_log.isDebugEnabled()) {
            _log.debug("Getting channel by name: " + name);
        }
        return getDao().getChannelByName(name);
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateChannelService.class);
}
