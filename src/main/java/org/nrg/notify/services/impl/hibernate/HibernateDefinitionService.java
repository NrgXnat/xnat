/**
 * HibernateDefinitionService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.DefinitionDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.services.DefinitionService;
import org.nrg.notify.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class HibernateDefinitionService extends AbstractHibernateEntityService<Definition> implements DefinitionService {

    /**
     * Retrieves all {@link Definition definitions} associated with the given category.
     * @param category The category for which to find all associated definitions. 
     * @return All {@link Definition definitions} associated with the given category.
     * @see NotificationService#getDefinitionsForCategory(Category)
     */
    @Override
    @Transactional
    public List<Definition> getDefinitionsForCategory(Category category) {
        List<Definition> definitions = _dao.getDefinitionsForCategory(category);
        Hibernate.initialize(definitions);
        return definitions;
    }

    /**
     * Retrieves the {@link Definition definition} associated with the given {@link Category category}
     * and entity ID.
     * @param category The category associated with the definition.
     * @param entity The entity ID associated with the definition.
     * @return The {@link Definition definition} associated with the given {@link Category category} and entity ID.
     * @throws DuplicateDefinitionException When multiple definitions for the given scope, event, and entity association exist.
     * @see DefinitionService#getDefinitionForCategoryAndEntity(Category, long)
     */
    @Override
    @Transactional
    public Definition getDefinitionForCategoryAndEntity(Category category, long entity) throws DuplicateDefinitionException {
        return _dao.getDefinitionForCategoryAndEntity(category, entity);
    }

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

    private static final Log _log = LogFactory.getLog(HibernateDefinitionService.class);

    @Autowired
    private DefinitionDAO _dao;
}
