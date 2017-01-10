/*
 * web: org.nrg.xnat.node.NodeCheckInRunner
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node;

import org.nrg.framework.node.XnatNode;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;

/**
 * The Class NodeCheckIn.
 */
public class NodeCheckInRunner implements Runnable {
	
	/** The _service. */
	private final XnatNodeInfoService _nodeInfoService;
	
	/** The _xnat node. */
	private final XnatNode _xnatNode;
	
	/**
	 * Instantiates a new node check in.
	 *
	 * @param xnatNode the xnat node
	 * @param service the service
	 */
	public NodeCheckInRunner(final XnatNode xnatNode, final XnatNodeInfoService nodeInfoService) {
		_nodeInfoService = nodeInfoService;
		_xnatNode = xnatNode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
       	// We only want to run this we have one or more nodes with configuration, however we'd better continue to check in case
		// another node becomes configured, so we'll stop recording here rather than preventing the TriggerTask
    	boolean hasConfiguredNode = false;
       	for (final XnatNodeInfo nodeInfo : _nodeInfoService.getAll()) {
       		if (!nodeInfo.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
       			hasConfiguredNode = true;
       			break;
       		}
       	}
       	if (hasConfiguredNode || !_xnatNode.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
       		_nodeInfoService.recordNodeCheckIn();
       	}
	}

}
