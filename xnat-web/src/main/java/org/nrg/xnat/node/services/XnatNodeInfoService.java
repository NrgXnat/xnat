/*
 * web: org.nrg.xnat.node.services.XnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.node.entities.XnatNodeInfo;

import java.util.List;

/**
 * The Interface XnatNodeInfoService.
 */
public interface XnatNodeInfoService extends BaseHibernateService<XnatNodeInfo> {
	
	/**
	 * Record node info.
	 */
	void recordNodeInitialization();

	/**
	 * Record check in.
	 */
	void recordNodeCheckIn();
	
	/**
	 * Record node shutdown.
	 */
	void recordNodeShutdown();

	/**
	 * Gets the xnat node info list by node id.
	 *
	 * @param nodeId the node id
	 * @return the xnat node info list by node id
	 */
	List<XnatNodeInfo> getXnatNodeInfoByNodeId(final String nodeId);

	/**
	 * Gets the xnat node info by node id and hostname.
	 *
	 * @param nodeId the node id
	 * @param hostName the host name
	 * @return the xnat node info by node id and hostname
	 */
	XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(String nodeId, String hostName);

	/**
	 * Gets a list of all configured nodes.
	 *
	 * @return The list of configured node info objects.
	 */
	List<XnatNodeInfo> getAllXnatNodeInfos();

	/**
	 * Gets a list of the currently active nodes.
	 *
	 * @return The list of node info objects for active nodes.
	 */
	List<XnatNodeInfo> getActiveXnatNodeInfos();
}
