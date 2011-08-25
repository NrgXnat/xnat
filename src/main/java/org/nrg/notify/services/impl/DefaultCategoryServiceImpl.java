/**
 * DefaultCategoryServiceImpl
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
import org.nrg.notify.api.Category;
import org.nrg.notify.daos.CategoryDAO;
import org.nrg.notify.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the {@link CategoryService} interface to provide default {@link Category category}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultCategoryServiceImpl extends AbstractHibernateEntityService<Category> implements CategoryService {

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

    private static final Log _log = LogFactory.getLog(DefaultCategoryServiceImpl.class);

    @Autowired
    private CategoryDAO _dao;
}
