/*
 * notify: org.nrg.notify.services.ChannelService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services;

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Channel;

public interface ChannelService extends BaseHibernateService<Channel> {
    @SuppressWarnings("unused")
    String SERVICE_NAME = "ChannelService";

    /**
     * A shortcut method for quickly creating a new channel.
     *
     * @param name   The name of the channel.
     * @param format The format supported by the channel.
     * @return The newly created channel object.
     * @throws NrgServiceException When an error occurs creating the channel.
     */
    Channel createChannel(String name, String format) throws NrgServiceException;

    /**
     * Retrieves the channel with the indicated name.
     *
     * @param name The name of the channel to retrieve.
     * @return The indicated channel.
     */
    Channel getChannel(String name);
}
