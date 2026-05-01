/*
 * notify: org.nrg.notify.daos.DefinitionDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.services.DefinitionService;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DefinitionDAO extends AbstractHibernateDAO<Definition> {
    /**
     * Retrieves all {@link Definition definitions} associated with the given category.
     *
     * @param category The category for which to find all associated definitions.
     *
     * @return All {@link Definition definitions} associated with the given category.
     *
     * @see DefinitionService#getDefinitionsForCategory(Category)
     */
    public List<Definition> getDefinitionsForCategory(Category category) {
        return findByProperty("category", category);
    }

    /**
     * Returns a {@link Definition definition} matching the specified criteria.
     *
     * @param category The category for which to find an associated definitions.
     * @param entity   The entity ID for which to find an associated definition.
     *
     * @return The definition associated with the given category and entity.
     *
     * @throws DuplicateDefinitionException When multiple definitions for the given scope, event, and entity association exist.
     */
    public Definition getDefinitionForCategoryAndEntity(Category category, long entity) throws DuplicateDefinitionException {
        return findByUniqueProperties(parameters("category", category, "entity", entity));
    }
}
