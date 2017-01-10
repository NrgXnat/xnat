/*
 * web: org.nrg.xnat.task.dao.XnatTaskInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.task.entities.XnatTaskInfo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class XnatTaskInfoDAO.
 */
@Repository
public class XnatTaskInfoDAO extends AbstractHibernateDAO<XnatTaskInfo> {
	
	/**
	 * Gets the xnat task info by task id and node.
	 *
	 * @param taskId the task id
	 * @param xnatNodeInfo the xnat node info
	 * @return the xnat task info by task id and node
	 */
	@Transactional
	public XnatTaskInfo getXnatTaskInfoByTaskIdAndNode(final String taskId, final XnatNodeInfo xnatNodeInfo) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("taskId", taskId));
        criteria.add(Restrictions.eq("xnatNodeInfo", xnatNodeInfo));
        return (XnatTaskInfo)criteria.uniqueResult();
	}
	
	/**
	 * Gets the xnat task info list by task id and node.
	 *
	 * @param taskId the task id
	 * @return the xnat task info list by task id and node
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public List<XnatTaskInfo> getXnatTaskInfoListByTaskIdAndNode(final String taskId) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("taskId", taskId));
        return criteria.list();
	}

}
