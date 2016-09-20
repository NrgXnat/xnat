/*
 * org.nrg.notify.services.impl.hibernate.HibernateDefinitionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services.impl.hibernate;

import org.hibernate.Hibernate;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.daos.DefinitionDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.services.DefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HibernateDefinitionService extends AbstractHibernateEntityService<Definition, DefinitionDAO> implements DefinitionService {

    /**
     * Retrieves all {@link Definition definitions} associated with the given category.
     * @param category The category for which to find all associated definitions. 
     * @return All {@link Definition definitions} associated with the given category.
     */
    @Override
    @Transactional
    public List<Definition> getDefinitionsForCategory(Category category) {
        if (_log.isDebugEnabled()) {
            _log.debug("Getting the definitions for category: [" + category.toString() + "]");
        }
        List<Definition> definitions = getDao().getDefinitionsForCategory(category);
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
        if (_log.isDebugEnabled()) {
            _log.debug("Getting the definition for category/entity: [" + category.toString() + ":" + entity + "]");
        }
        return getDao().getDefinitionForCategoryAndEntity(category, entity);
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateDefinitionService.class);
}
