/*
 * web: org.nrg.xnat.node.dao.XnatNodeInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class XnatNodeInfoDAO.
 */
@Repository
public class XnatNodeInfoDAO extends AbstractHibernateDAO<XnatNodeInfo> {
	
	/**
	 * Gets the xnat node info list by node id.
	 *
	 * @param nodeId the node id
	 * @return the xnat node info list by node id
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public List<XnatNodeInfo> getXnatNodeInfoListByNodeId(final String nodeId) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("nodeId", nodeId));
        return criteria.list();
	}
	
	/**
	 * Gets the xnat node info by node id and hostname.
	 *
	 * @param nodeId the node id
	 * @param hostName the host name
	 * @return the xnat node info by node id and hostname
	 */
	@Transactional
	public XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(final String nodeId, final String hostName) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("nodeId", nodeId));
        criteria.add(Restrictions.eq("hostName", hostName));
        return (XnatNodeInfo)criteria.uniqueResult();
	}

}
