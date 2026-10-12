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
import org.nrg.xnat.archive.ImportScope;
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
        // The importer asks this for every received object at each of its processing locations, and each ask
        // was a Hibernate query: the single largest CPU cost of receiving an object. Within one import scope
        // (the objects of one association, one uploaded archive, one inbox request) the answer is read once
        // per location; the instances are fully loaded (every collection is eager) and read-only to the
        // importer, so sharing them within the scope is safe.
        return ImportScope.scoped("archive-processors:" + location, () -> getDao().getEnabledSiteArchiveProcessorsInOrderForLocation(location));
    }

    @Override
    public ArchiveProcessorInstance findSiteProcessorById(final long processorId) {
        return getDao().getSiteArchiveProcessorInstanceByProcessorId(processorId);
    }
}
