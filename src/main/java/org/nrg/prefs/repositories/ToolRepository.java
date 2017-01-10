/*
 * prefs: org.nrg.prefs.repositories.ToolRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.repositories;

import org.hibernate.type.StandardBasicTypes;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.prefs.entities.Tool;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class ToolRepository extends AbstractHibernateDAO<Tool> {
    public Set<String> getToolIds() {
        @SuppressWarnings({"unchecked", "SqlDialectInspection", "SqlNoDataSourceInspection"})
        final List<String> results = getSession().createSQLQuery("select tool_id from xhbm_tool")
                .addScalar("tool_id", StandardBasicTypes.STRING)
                .list();
        final Set<String> toolIds = new HashSet<>();
        toolIds.addAll(results);
        return toolIds;
    }
}
