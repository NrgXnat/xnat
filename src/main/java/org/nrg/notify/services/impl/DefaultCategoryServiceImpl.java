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
import org.nrg.notify.api.Category;
import org.nrg.notify.daos.CategoryDAO;
import org.nrg.notify.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the {@link CategoryService} interface to provide default {@link Category category}
 * management functionality.
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class DefaultCategoryServiceImpl implements CategoryService {

    /**
     * @return A new empty {@link Category} object.
     * @see CategoryService#newCategory()
     */
    public Category newCategory() {
        return new Category();
    }

    /**
     * Adds the submitted {@link Category object} to the system.
     * @param category The category to be added to the system.
     * @see CategoryService#create(Category)
     */
    @Transactional
    public void create(Category category) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a new category: " + category.toString());
        }
        _categoryDAO.create(category);
    }

    /**
     * 
     * @see CategoryService#retrieveCategory(long)
     */
    @Transactional
    public Category retrieveCategory(long id) {
        if (_log.isDebugEnabled()) {
            _log.debug("Retrieving category for ID: " + id);
        }
        return _categoryDAO.retrieve(id);
    }

    /**
     * @see CategoryService#update(Category)
     * @author Rick Herrick <rick.herrick@wustl.edu>
     */
    @Transactional
    public void update(Category category) {
        if (_log.isDebugEnabled()) {
            _log.debug("Updating category for ID: " + category.getId());
        }
        _categoryDAO.update(category);
    }

    /**
     * @see CategoryService#deleteCategory(Category)
     * @author Rick Herrick <rick.herrick@wustl.edu>
     */
    @Transactional
    public void deleteCategory(Category category) {
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting category for ID: " + category.getId());
        }
    }

    /**
     * @see CategoryService#deleteCategory(long)
     * @author Rick Herrick <rick.herrick@wustl.edu>
     */
    @Transactional
    public void deleteCategory(long id) {
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting category for ID: " + id);
        }
        _categoryDAO.delete(_categoryDAO.retrieve(id));
    }

    private static final Log _log = LogFactory.getLog(DefaultCategoryServiceImpl.class);
    
    @Autowired
    private CategoryDAO _categoryDAO;
}
