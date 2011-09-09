/**
 * HibernateCategoryService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl.hibernate;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.daos.CategoryDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.services.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implements the {@link CategoryService} interface to provide default {@link Category category}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class HibernateCategoryService extends AbstractHibernateEntityService<Category> implements CategoryService {

    /**
     * Finds a currently enabled {@link Category category} with the indicated {@link CategoryScope scope} and
     * event. If there is no currently enabled category that meets that criteria, this method returns <b>null</b>.
     * @param scope Indicates the category scope.
     * @param event Indicates the category event.
     * @return The matching category if it exists, otherwise <b>null</b>.
     * @see CategoryService#getCategoryByScopeAndEvent(CategoryScope, String)
     */
    @Override
    @Transactional
    public Category getCategoryByScopeAndEvent(CategoryScope scope, String event) {
        return getDao().getCategoryByScopeAndEvent(scope, event);
    }

    /**
     * @return A new empty {@link Category} object.
     * @see CategoryService#newCategory()
     */
    public Category newEntity() {
        _log.debug("Creating a new category object");
        return new Category();
    }

    /**
     * @return
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected CategoryDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateCategoryService.class);

    @Inject
    private CategoryDAO _dao;

}
