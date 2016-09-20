/*
 * org.nrg.notify.daos.DefinitionDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.services.DefinitionService;
import org.springframework.stereotype.Repository;


@Repository
public class DefinitionDAO extends AbstractHibernateDAO<Definition> {

    /**
     * Retrieves all {@link Definition definitions} associated with the given category.
     * @param category The category for which to find all associated definitions. 
     * @return All {@link Definition definitions} associated with the given category.
     * @see DefinitionService#getDefinitionsForCategory(Category)
     */
    @SuppressWarnings("unchecked")
    public List<Definition> getDefinitionsForCategory(Category category) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("category", category));
        criteria.add(Restrictions.eq("enabled", true));
        return criteria.list();
    }

    /**
     * Returns a {@link Definition definition} matching the specified criteria.
     * @param category The category for which to find an associated definitions. 
     * @param entity The entity ID for which to find an associated definition.
     * @return The definition associated with the given category and entity.
     * @throws DuplicateDefinitionException When multiple definitions for the given scope, event, and entity association exist.
     */
    public Definition getDefinitionForCategoryAndEntity(Category category, long entity) throws DuplicateDefinitionException {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("category", category));
        criteria.add(Restrictions.eq("entity", entity));
        criteria.add(Restrictions.eq("enabled", true));

        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        
        if (list == null || list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            throw new DuplicateDefinitionException("Found " + list.size() + " definitions for the given criteria, scope [" + category.getScope() + "], event [" + category.getEvent() + "], entity [" + entity + "]");
        }

        return (Definition) list.get(0);
    }
}
