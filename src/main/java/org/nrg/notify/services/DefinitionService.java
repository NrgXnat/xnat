/**
 * DefinitionService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.exceptions.DuplicateDefinitionException;


/**
 * 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface DefinitionService extends BaseHibernateService<Definition>  {
    public static String SERVICE_NAME = "DefinitionService";

    /**
     * Retrieves all {@link Definition definitions} associated with the given category.
     * @param category The category for which to find all associated definitions. 
     * @return All {@link Definition definitions} associated with the given category.
     */
    List<Definition> getDefinitionsForCategory(Category category);

    /**
     * Retrieves the {@link Definition definition} associated with the given {@link Category category}
     * and entity ID.
     * @param category The category associated with the definition.
     * @param entity The entity ID associated with the definition.
     * @return The {@link Definition definition} associated with the given {@link Category category} and entity ID.
     * @throws DuplicateDefinitionException When multiple definitions for the given scope, event, and entity association exist.
     */
    Definition getDefinitionForCategoryAndEntity(Category category, long entity) throws DuplicateDefinitionException;
}
