/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.transport.entities.ElementDisplayDB;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Tim Olsen
 *
 * DAO Object for management of Element Displays (Search Engine - Display docs)
 */
@SuppressWarnings("unchecked")
@Repository
public class ElementDisplayDAO extends AbstractHibernateDAO<ElementDisplayDB> {
    /**
     * Find stored ElementDisplay object by elementName
     * @param name
     * @param includeDisabled
     * @return
     */
    public ElementDisplayDB findByElementName(final String name, final boolean includeDisabled) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("elementName", name));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return (ElementDisplayDB) criteria.list().get(0);
    }
}
