package org.nrg.prefs.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.prefs.entities.Tool;

public interface ToolService extends BaseHibernateService<Tool> {
    Tool getByToolId(String toolId);
}
