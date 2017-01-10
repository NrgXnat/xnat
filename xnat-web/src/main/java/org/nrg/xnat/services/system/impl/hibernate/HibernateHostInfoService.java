/*
 * web: org.nrg.xnat.services.system.impl.hibernate.HibernateHostInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * 
 */
package org.nrg.xnat.services.system.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.daos.HostInfoDAO;
import org.nrg.xnat.entities.HostInfo;
import org.nrg.xnat.services.system.HostInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc}
 */
@Service
public class HibernateHostInfoService extends AbstractHibernateEntityService<HostInfo, HostInfoDAO> implements HostInfoService {
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String getHostNumber() {
        return getDao().getHostNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String getHostNumber(String hostName) {
        return getDao().getHostNumber(hostName);
    }
}
