/*
 * web: org.nrg.xnat.task.services.impl.HibernateXnatTaskInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.services.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.nrg.framework.node.XnatNode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.task.entities.XnatTaskInfo;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.nrg.xnat.task.dao.XnatTaskInfoDAO;
import org.nrg.xnat.task.services.XnatTaskInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateXnatTaskInfoService.
 */
@Service
public class HibernateXnatTaskInfoService extends AbstractHibernateEntityService<XnatTaskInfo, XnatTaskInfoDAO> implements XnatTaskInfoService {
	
	/** The _node info service. */
	XnatNodeInfoService _nodeInfoService;
	
	/**
	 * Instantiates a new hibernate xnat task info service.
	 *
	 * @param nodeInfoService the node info service
	 */
	@Autowired
	public HibernateXnatTaskInfoService(final XnatNodeInfoService nodeInfoService) {
		_nodeInfoService = nodeInfoService;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.task.services.XnatTaskInfoService#recordTaskRun(org.nrg.framework.node.XnatNode, java.lang.String)
	 */
	@Override
	@Transactional
	public void recordTaskRun(final XnatNode xnatNode, final String taskId) {
		final InetAddress localHost;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			_log.warn("WARNING:  Unable to obtain host information.  Cannot record task information", e);
			return;
		}
		final String localHostName = localHost.getHostName();
		final XnatNodeInfo xnatNodeInfo = _nodeInfoService.getXnatNodeInfoByNodeIdAndHostname(xnatNode.getNodeId(), localHostName);
		if (xnatNodeInfo == null) {
			_log.warn("WARNING:  No node information recorded for this node/host.  Cannot record task information ({}/{})", xnatNode.getNodeId(), localHostName);
			return;
		}
		
		XnatTaskInfo taskInfo = getDao().getXnatTaskInfoByTaskIdAndNode(taskId, xnatNodeInfo);
		final Date lastRun = new Date();
		if (taskInfo != null) {
			taskInfo.setXnatNodeInfo(xnatNodeInfo);
			taskInfo.setLastRun(lastRun);
		} else {
			taskInfo = new XnatTaskInfo(taskId, xnatNodeInfo, lastRun);
		}
		getDao().saveOrUpdate(taskInfo);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xnat.task.services.XnatTaskInfoService#getXnatTaskInfoListByTaskIdAndNode(java.lang.String)
	 */
	@Override
	@Transactional
	public List<XnatTaskInfo> getXnatTaskInfoListByTaskIdAndNode(final String taskId) {
		return getDao().getXnatTaskInfoListByTaskIdAndNode(taskId);
	}

    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(HibernateXnatTaskInfoService.class);

}
