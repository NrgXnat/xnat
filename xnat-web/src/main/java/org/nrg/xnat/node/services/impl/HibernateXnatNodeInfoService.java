/*
 * web: org.nrg.xnat.node.services.impl.HibernateXnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;

import org.nrg.framework.node.XnatNode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.node.dao.XnatNodeInfoDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HibernateXnatNodeInfoService.
 */
@Service
public class HibernateXnatNodeInfoService extends AbstractHibernateEntityService<XnatNodeInfo, XnatNodeInfoDAO> implements XnatNodeInfoService {
	
	/** The _xnat node. */
	private final XnatNode _xnatNode;
	
	/** The _jdbc template. */
	private final JdbcTemplate _jdbcTemplate;

	/**
	 * Instantiates a new hibernate xnat node info service.
	 *
	 * @param xnatNode the xnat node
	 * @param jdbcTemplate the jdbc template
	 */
	@Autowired
	public HibernateXnatNodeInfoService(final XnatNode xnatNode, final JdbcTemplate jdbcTemplate) {
		_xnatNode = xnatNode;
		_jdbcTemplate = jdbcTemplate;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.node.services.XnatNodeInfoService#recordNodeInfo(org.nrg.xnat.node.XnatNode)
	 */
	@Override
	@Transactional
	public void recordNodeInitialization() {
		
		final InetAddress localHost;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			_log.warn("WARNING:  Unable to obtain host information.  Cannot record node information", e);
			return;
		}
		final String nodeId = _xnatNode.getNodeId();
		final List<XnatNodeInfo> nodeInfoList = getDao().getXnatNodeInfoListByNodeId(nodeId);
		final String localHostName = localHost.getHostName();
		final String localHostAddress = localHost.getHostAddress();
		final Date initialized = new Date(System.currentTimeMillis());
		for (final XnatNodeInfo nodeInfo : nodeInfoList) {
			if (nodeInfo.getHostName().equals(localHostName)) {
				nodeInfo.setLastIpAddress(localHostAddress);
				nodeInfo.setLastInitialized(initialized);
				nodeInfo.setIsActive(true);
				getDao().saveOrUpdate(nodeInfo);
				return;
			}
		}
		final XnatNodeInfo nodeInfo = new XnatNodeInfo(nodeId, localHostName, localHostAddress, initialized);
		getDao().saveOrUpdate(nodeInfo);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.node.services.XnatNodeInfoService#checkIn(org.nrg.framework.node.XnatNode)
	 */
	@Override
	@Transactional
	public void recordNodeCheckIn() {
		
		final InetAddress localHost;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			_log.warn("WARNING:  Unable to obtain host information.  Cannot record node information", e);
			return;
		}
		final String nodeId = _xnatNode.getNodeId();
		final List<XnatNodeInfo> nodeInfoList = getDao().getXnatNodeInfoListByNodeId(nodeId);
		final String localHostName = localHost.getHostName();
		final String localHostAddress = localHost.getHostAddress();
		final Date initialized = null;
		final Date checkIn = new Date(System.currentTimeMillis());
		for (final XnatNodeInfo nodeInfo : nodeInfoList) {
			if (nodeInfo.getHostName().equals(localHostName)) {
				nodeInfo.setLastIpAddress(localHostAddress);
				nodeInfo.setLastCheckIn(checkIn);
				nodeInfo.setIsActive(true);
				getDao().saveOrUpdate(nodeInfo);
				return;
			}
		}
		final XnatNodeInfo nodeInfo = new XnatNodeInfo(nodeId, localHostName, localHostAddress, initialized);
		nodeInfo.setLastCheckIn(checkIn);
		getDao().saveOrUpdate(nodeInfo);
		
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.node.services.XnatNodeInfoService#recordNodeShutdown()
	 */
	@Override
	@Transactional
	@PreDestroy
	public void recordNodeShutdown() {
		
		final InetAddress localHost;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			_log.warn("WARNING:  Unable to obtain host information.  Cannot record node information", e);
			return;
		}
		final String nodeId = _xnatNode.getNodeId();
		final List<XnatNodeInfo> nodeInfoList = getDao().getXnatNodeInfoListByNodeId(nodeId);
		final String localHostName = localHost.getHostName();
		for (final XnatNodeInfo nodeInfo : nodeInfoList) {
			if (nodeInfo.getHostName().equals(localHostName)) {
				// Use JDBC here.  The Hibernate session doen't seem to be available in the PreDestroy context.  
                _jdbcTemplate.update("UPDATE xhbm_xnat_node_info SET is_active = FALSE WHERE node_id=? and host_name=?", nodeId, localHostName);
				return;
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.node.services.XnatNodeInfoService#getXnatNodeInfoByNodeIdAndHostname(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional
	public XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(String nodeId, String hostName) {
		return getDao().getXnatNodeInfoByNodeIdAndHostname(nodeId, hostName);
	}

    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(HibernateXnatNodeInfoService.class);

}
