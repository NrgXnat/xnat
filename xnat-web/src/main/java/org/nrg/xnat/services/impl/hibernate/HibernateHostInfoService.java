package org.nrg.xnat.services.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.daos.HostInfoDAO;
import org.nrg.xnat.entities.HostInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateHostInfoService extends AbstractHibernateEntityService<HostInfo, HostInfoDAO> {

	private static HibernateHostInfoService _instance;
	
	public HibernateHostInfoService() {
		_instance = this;
	}
	
	public static HibernateHostInfoService getService() {
	    if (_instance == null) {
	    	_instance = XDAT.getContextService().getBean(HibernateHostInfoService.class);
	    }
	    return _instance;
	}
	
    @Transactional
    public String getHostNumber() {
        return getDao().getHostNumber();
    }

    @Transactional
    public String getHostNumber(String hostName) {
        return getDao().getHostNumber(hostName);
    }

}
