/**
 * CategoryDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.entities.Category;
import org.springframework.stereotype.Repository;


@Repository
public class CategoryDAO extends AbstractHibernateDAO<Category> {

    /**
     * Attempts to find an enabled category matching the submitted scope and event values.
     * If no matching category is found, this method returns <b>null</b>.
     * @param scope The category scope.
     * @param event The category event.
     * @return A matching category, if it exists.
     */
    public Category getCategoryByScopeAndEvent(CategoryScope scope, String event) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("enabled", true));
        criteria.add(Restrictions.eq("scope", scope));
        criteria.add(Restrictions.eq("event", event));

        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (Category) list.get(0);
    }
}
