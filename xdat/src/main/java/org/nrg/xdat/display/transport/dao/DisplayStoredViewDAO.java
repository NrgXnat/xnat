/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.display.transport.entities.DisplayStoredViewDB;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @author Tim Olsen
 *
 * DAO Object for management of DisplayStoredViews (sql views and functions configured for use by the search engine)
 */
@SuppressWarnings("unchecked")
@Repository
public class DisplayStoredViewDAO extends AbstractHibernateDAO<DisplayStoredViewDB> {
    /**
     * Find stored view/function by entity's name
     * @param name
     * @param includeDisabled
     * @return
     */
    public DisplayStoredViewDB findByName(final String name, final boolean includeDisabled) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("name", name));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return (DisplayStoredViewDB) criteria.list().get(0);
    }

    /**
     * Get the stored view/function objects, sorted by sourceType, sourceOrder
     * @return
     */
    public List<DisplayStoredViewDB> getSortedViews() {
        final Criteria criteria = getCriteriaForType();
        criteria.addOrder(Order.asc("sourceType"));
        criteria.addOrder(Order.asc("sortOrder"));
        if (criteria.list().size() == 0) {
            return null;
        }
        return criteria.list();
    }
}
