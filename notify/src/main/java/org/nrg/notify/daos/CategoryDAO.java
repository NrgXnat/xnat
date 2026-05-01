/*
 * notify: org.nrg.notify.daos.CategoryDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.entities.Category;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryDAO extends AbstractHibernateDAO<Category> {
    /**
     * Attempts to find an enabled category matching the submitted scope and event values.
     * If no matching category is found, this method returns <b>null</b>.
     *
     * @param scope The category scope.
     * @param event The category event.
     *
     * @return A matching category, if it exists.
     */
    public Category getCategoryByScopeAndEvent(CategoryScope scope, String event) {
        return findByUniqueProperties(parameters("scope", scope, "event", event));
    }
}
