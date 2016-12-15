package org.nrg.xnat.node.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.node.entities.XnatNodeInfo;

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
	 * Gets the xnat node info by node id and hostname.
	 *
	 * @param nodeId the node id
	 * @param hostName the host name
	 * @return the xnat node info by node id and hostname
	 */
	XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(String nodeId, String hostName);

}
