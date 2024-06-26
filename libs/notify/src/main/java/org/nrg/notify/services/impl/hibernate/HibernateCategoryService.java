/*
 * notify: org.nrg.notify.services.impl.hibernate.HibernateCategoryService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.daos.CategoryDAO;
import org.nrg.notify.entities.Category;
import org.nrg.notify.services.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateCategoryService extends AbstractHibernateEntityService<Category, CategoryDAO> implements CategoryService {

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
        if (_log.isDebugEnabled()) {
            _log.debug("Retrieving category by scope and event: [" + scope.toString() + ":" + event + "]");
        }
        return getDao().getCategoryByScopeAndEvent(scope, event);
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateCategoryService.class);
}
