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
import org.nrg.xnat.processor.dao.ArchiveProcessorInstanceDAO;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class HibernateArchiveProcessorInstanceService extends AbstractHibernateEntityService<ArchiveProcessorInstance, ArchiveProcessorInstanceDAO> implements ArchiveProcessorInstanceService {

    @Override
    @Transactional
    public List<ArchiveProcessorInstance> getAllSiteProcessors(){
        return _dao.getSiteArchiveProcessors();
    }

    @Override
    @Transactional
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessors(){
        return _dao.getEnabledSiteArchiveProcessors();
    }

    @Override
    @Transactional
    public List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsInOrder(){
        return _dao.getEnabledSiteArchiveProcessorsInOrder();
    }

    @Override
    @Transactional
    public ArchiveProcessorInstance findSiteProcessorById(final int processorId){
        return _dao.getSiteArchiveProcessorInstanceByProcessorId(processorId);
    }
    @Inject
    private ArchiveProcessorInstanceDAO _dao;

    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(HibernateArchiveProcessorInstanceService.class);

}
