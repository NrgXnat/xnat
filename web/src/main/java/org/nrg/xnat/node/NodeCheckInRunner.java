/*
 * web: org.nrg.xnat.node.NodeCheckInRunner
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.node.XnatNode;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.nrg.xnat.task.AbstractXnatRunnable;

/**
 * The Class NodeCheckIn.
 */
@Slf4j
public class NodeCheckInRunner extends AbstractXnatRunnable {
    /**
     * Instantiates a new node check-in runner
     *
     * @param xnatNode        the xnat node
     * @param nodeInfoService the service
     */
    public NodeCheckInRunner(final XnatNode xnatNode, final XnatNodeInfoService nodeInfoService) {
        log.debug("Configuring this node to be {}", xnatNode.getNodeId());
        _nodeInfoService = nodeInfoService;
        _xnatNode = xnatNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runTask() {
        log.debug("Running the node check-in runner for node {}", _xnatNode.getNodeId());

        // We only want to run this we have one or more nodes with configuration, however we'd better continue to check in case
        // another node becomes configured, so we'll stop recording here rather than preventing the TriggerTask
        final boolean hasConfiguredNode = hasConfiguredNode();
        log.debug("{} a configured node ID", hasConfiguredNode ? "Found" : "Didn't find");
        if (hasConfiguredNode || !_xnatNode.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
            _nodeInfoService.recordNodeCheckIn();
        }
    }

    /**
     * Searches for any node that is <i>not</i> named {@link XnatNode#NODE_ID_NOT_CONFIGURED}.
     *
     * @return Returns true if any node with an actual node ID is found, false otherwise.
     */
    private boolean hasConfiguredNode() {
        for (final XnatNodeInfo nodeInfo : _nodeInfoService.getAll()) {
            log.debug("Checking node info for {}", nodeInfo.getNodeId());
            if (!nodeInfo.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
                return true;
            }
        }
        return false;
    }

    private final XnatNodeInfoService _nodeInfoService;
    private final XnatNode            _xnatNode;
}
