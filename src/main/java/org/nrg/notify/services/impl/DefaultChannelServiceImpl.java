/**
 * DefaultChannelServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 25, 2011
 */
package org.nrg.notify.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.Channel;
import org.nrg.notify.daos.ChannelDAO;
import org.nrg.notify.services.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultChannelServiceImpl extends AbstractHibernateEntityService<Channel> implements ChannelService {

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

    private static final Log _log = LogFactory.getLog(DefaultChannelServiceImpl.class);
    
    @Autowired
    private ChannelDAO _dao;
}
