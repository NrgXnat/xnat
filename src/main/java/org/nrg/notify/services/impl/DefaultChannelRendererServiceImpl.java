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
import org.nrg.notify.api.ChannelRenderer;
import org.nrg.notify.daos.ChannelRendererDAO;
import org.nrg.notify.services.ChannelRendererService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultChannelRendererServiceImpl extends AbstractHibernateEntityService<ChannelRenderer> implements ChannelRendererService {

    /**
     * @return A new empty {@link ChannelRenderer} object.
     * @see ChannelRendererService#newEntity()
     */
    public ChannelRenderer newEntity() {
        _log.debug("Creating a new channel renderer object");
        return new ChannelRenderer();
    }

    /**
     * @return
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected ChannelRendererDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(DefaultChannelRendererServiceImpl.class);
    
    @Autowired
    private ChannelRendererDAO _dao;
}
