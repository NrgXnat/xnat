/**
 * ChannelService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Channel;


/**
 * Provides the means for managing the various notification publication channels.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface ChannelService extends BaseHibernateService<Channel> {
    public static String SERVICE_NAME = "ChannelService";

    /**
     * A shortcut method for quickly creating a new channel.
     * @param name The name of the channel.
     * @param format The format supported by the channel.
     * @return The newly created channel object.
     */
    abstract public Channel createChannel(String name, String format);

    /**
     * Retrieves the channel with the indicated name.
     * @param name The name of the channel to retrieve.
     * @return The indicated channel.
     */
    abstract public Channel getChannel(String name);
}
