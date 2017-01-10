/*
 * web: org.nrg.xnat.services.system.HostInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.system;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.entities.HostInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides information about the current host.
 */
public interface HostInfoService extends BaseHibernateService<HostInfo> {
    /**
     * Gets the host number of the current host.
     *
     * @return The host number.
     */
    String getHostNumber();

    /**
     * Gets the host number of the host with the indicated name.
     *
     * @param hostName The name of the host.
     * @return The host number of the indicated host.
     */
    String getHostNumber(String hostName);
}
