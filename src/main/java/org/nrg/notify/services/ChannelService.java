/**
 * ChannelService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 25, 2011
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.api.Channel;

/**
 * Provides the means for managing the various notification publication channels.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface ChannelService extends BaseHibernateService<Channel> {
    public static String SERVICE_NAME = "ChannelService";
}
