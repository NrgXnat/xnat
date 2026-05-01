/*
 * prefs: org.nrg.prefs.repositories.ToolRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.prefs.entities.Tool;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class ToolRepository extends AbstractHibernateDAO<Tool> {
    public Set<String> getToolIds() {
        return distinct(String.class, Tool.PROPERTY_TOOL_ID);
    }

    public Tool findByToolId(final String toolId) {
        return findByUniqueProperty(Tool.PROPERTY_TOOL_ID, toolId);
    }
}
