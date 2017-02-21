/*
 * prefs: org.nrg.prefs.repositories.ToolRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.repositories;

import com.google.common.collect.Sets;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.prefs.entities.Tool;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class ToolRepository extends AbstractHibernateDAO<Tool> {
    public Set<String> getToolIds() {
        final Criteria criteria = getSession().createCriteria(Tool.class);
        criteria.setProjection(Projections.distinct(Projections.property("toolId")));
        //noinspection unchecked
        return Sets.newHashSet(criteria.list());
    }

    public Tool findByToolId(final String toolId) {
        return findByUniqueProperty("toolId", toolId);
    }
}
