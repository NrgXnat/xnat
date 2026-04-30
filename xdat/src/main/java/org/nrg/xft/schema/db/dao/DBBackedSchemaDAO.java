/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xft.schema.db.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unchecked")
@Repository
public class DBBackedSchemaDAO extends AbstractHibernateDAO<DBBackedSchema> {
    public DBBackedSchema findByPath(String path, boolean includeDisabled) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("path", path));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return (DBBackedSchema) criteria.list().get(0);
    }

    public DBBackedSchema findByName(String name, boolean includeDisabled) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("name", name));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return (DBBackedSchema) criteria.list().get(0);
    }
}
