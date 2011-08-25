/**
 * DefaultDefinitionServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 24, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.Definition;
import org.nrg.notify.daos.DefinitionDAO;
import org.nrg.notify.services.DefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the {@link DefinitionService} interface to provide default {@link Definition Definition}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultDefinitionServiceImpl extends AbstractHibernateEntityService<Definition> implements DefinitionService {

    /**
     * @return A new empty {@link Definition} object.
     * @see DefinitionService#newDefinition()
     */
    public Definition newEntity() {
        _log.debug("Creating a new definition object");
        return new Definition();
    }

    /**
     * @return
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected DefinitionDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(DefaultDefinitionServiceImpl.class);

    @Autowired
    private DefinitionDAO _dao;
}
