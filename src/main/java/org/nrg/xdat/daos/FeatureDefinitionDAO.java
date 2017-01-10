/*
 * core: org.nrg.xdat.daos.FeatureDefinitionDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.FeatureDefinition;
import org.springframework.stereotype.Repository;

@Repository
public class FeatureDefinitionDAO extends AbstractHibernateDAO<FeatureDefinition>{

    public FeatureDefinition findByKey(String key) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("key", key));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        if (criteria.list().size() == 0) {
            return null;
        }
        return (FeatureDefinition) criteria.list().get(0);
    }

}
