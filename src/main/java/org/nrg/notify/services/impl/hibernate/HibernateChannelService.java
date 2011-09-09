/**
 * HibernateChannelService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.ChannelDAO;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.services.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class HibernateChannelService extends AbstractHibernateEntityService<Channel> implements ChannelService {

    /**
     * A shortcut method for quickly creating a new channel.
     * @param name The name of the channel.
     * @param format The format supported by the channel.
     * @return The newly created channel object.
     * @see ChannelService#createChannel(String, String)
     */
    @Override
    @Transactional
    public Channel createChannel(String name, String format) {
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
        return getDao().getChannelByName(name);
    }

    /**
     * @return A new empty {@link Channel} object.
     * @see ChannelService#newEntity()
     */
    public Channel newEntity() {
        _log.debug("Creating a new channel object");
        return new Channel();
    }

    /**
     * @return
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected ChannelDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateChannelService.class);
    
    @Autowired
    private ChannelDAO _dao;
}
