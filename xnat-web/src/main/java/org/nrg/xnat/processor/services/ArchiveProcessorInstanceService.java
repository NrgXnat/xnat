/*
 * web: org.nrg.xnat.node.services.XnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.entities.ArchiveProcessorInstance;

import java.util.List;

/**
 * The Interface ArchiveProcessorInstanceService.
 */
public interface ArchiveProcessorInstanceService extends BaseHibernateService<ArchiveProcessorInstance> {
    List<ArchiveProcessorInstance> getAllSiteProcessors();
    List<ArchiveProcessorInstance> getAllEnabledSiteProcessors();
    List<ArchiveProcessorInstance> getAllEnabledSiteProcessorsInOrder();
    ArchiveProcessorInstance findSiteProcessorById(final int processorId);
}
