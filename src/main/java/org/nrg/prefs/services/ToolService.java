package org.nrg.prefs.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.prefs.entities.Tool;

import java.util.Set;

public interface ToolService extends BaseHibernateService<Tool> {
    Tool getByToolId(final String toolId);
    Set<String> getToolIds();
}
