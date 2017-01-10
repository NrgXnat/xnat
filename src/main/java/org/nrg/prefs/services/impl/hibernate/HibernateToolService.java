/*
 * prefs: org.nrg.prefs.services.impl.hibernate.HibernateToolService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.repositories.ToolRepository;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

@Service
public class HibernateToolService extends AbstractHibernateEntityService<Tool, ToolRepository> implements ToolService {
    @Transactional
    @Override
    public Tool getByToolId(final String toolId) {
        return getDao().findByUniqueProperty("toolId", toolId);
    }

    @Transactional
    @Override
    public Set<String> getToolIds() {
        return getDao().getToolIds();
    }
}
