/*
 * web: org.nrg.xnat.daos.HostInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.daos;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.HostInfo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HostInfoDAO extends AbstractHibernateDAO<HostInfo> {
	 
	/**
	 * Gets the host number.
	 *
	 * @param hostName the host name
	 * @param setValue the set value
	 * @return the host number
	 */
	@Transactional
	public String getHostNumber(String hostName, boolean setValue) {
		Criteria criteria = getCriteriaForType();
		criteria.add(Restrictions.eq("hostName", hostName));
		if (criteria.list().size() == 0) {
			if (setValue) {
				HostInfo serverInfo = new HostInfo(hostName); 
				this.create(serverInfo);
				return getHostNumber(hostName,false);
			} else {
				return "";
			}
		} else {
			return String.format("%02d",((HostInfo)criteria.list().get(0)).getId());
		}
	}
	
	@Transactional
	public String getHostNumber(String hostName) {
		return getHostNumber(hostName,true);
	}
	
	@Transactional
	public String getHostNumber() {
		try {
			return getHostNumber(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			return "";
		}
	}

}
