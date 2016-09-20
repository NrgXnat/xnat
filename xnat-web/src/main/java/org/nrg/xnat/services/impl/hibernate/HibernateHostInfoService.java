/*
 * web: org.nrg.xnat.services.impl.hibernate.HibernateHostInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * 
 */
package org.nrg.xnat.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.daos.HostInfoDAO;
import org.nrg.xnat.entities.HostInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateHostInfoService.
 */
@Service
public class HibernateHostInfoService extends AbstractHibernateEntityService<HostInfo, HostInfoDAO> {

	/** The _instance. */
	private static HibernateHostInfoService _instance;
	
	/**
	 * Instantiates a new hibernate host info service.
	 */
	public HibernateHostInfoService() {
		_instance = this;
	}
	
	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public static HibernateHostInfoService getService() {
	    if (_instance == null) {
	    	_instance = XDAT.getContextService().getBean(HibernateHostInfoService.class);
	    }
	    return _instance;
	}
	
    /**
     * Gets the host number.
     *
     * @return the host number
     */
    @Transactional
    public String getHostNumber() {
        return getDao().getHostNumber();
    }

    /**
     * Gets the host number.
     *
     * @param hostName the host name
     * @return the host number
     */
    @Transactional
    public String getHostNumber(String hostName) {
        return getDao().getHostNumber(hostName);
    }

}
