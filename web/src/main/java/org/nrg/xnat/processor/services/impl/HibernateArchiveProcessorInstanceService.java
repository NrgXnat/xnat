/*
 * web: org.nrg.xnat.node.services.impl.HibernateXnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.dao.ArchiveProcessorInstanceDAO;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HibernateArchiveProcessorInstanceService extends AbstractHibernateEntityService<ArchiveProcessorInstance, ArchiveProcessorInstanceDAO> implements ArchiveProcessorInstanceService {
    @Override
    public List<ArchiveProcessorInstance> getAllSiteProcessors() {
        return getDao().getSiteArchiveProcessors();
    }

    @Override
    public List<ArchiveProcessorInstance> getAllSiteProcessorsForClass(final String processorClass) {
        return getDao().getSiteArchiveProcessorsForClass(processorClass);
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessors() {
        return getDao().getEnabledSiteArchiveProcessors();
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsForAe(String aeAndPort) {
        return getDao().getEnabledSiteArchiveProcessorsForAe(aeAndPort);
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsInOrder() {
        return getDao().getEnabledSiteArchiveProcessorsInOrder();
    }

    @Override
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsInOrderForLocation(final String location) {
        return getDao().getEnabledSiteArchiveProcessorsInOrderForLocation(location);
    }

    @Override
    public ArchiveProcessorInstance findSiteProcessorById(final long processorId) {
        return getDao().getSiteArchiveProcessorInstanceByProcessorId(processorId);
    }
}
