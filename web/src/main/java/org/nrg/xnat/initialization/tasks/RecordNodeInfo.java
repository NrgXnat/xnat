/*
 * web: org.nrg.xnat.initialization.tasks.RecordNodeInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.framework.node.XnatNode;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * The Class RecordNodeInfo.
 */
@Component
public class RecordNodeInfo extends AbstractInitializingTask {
	
    /** The _xnat node. */
    private XnatNode _xnatNode;
	
	/** The _xnat node info service. */
	private XnatNodeInfoService _xnatNodeInfoService;

	/**
	 * Instantiates a new record node info.
	 *
	 * @param xnatNode the xnat node
	 * @param xnatNodeInfoService the xnat node info service
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Autowired
    public RecordNodeInfo(final XnatNode xnatNode, final XnatNodeInfoService xnatNodeInfoService) throws IOException {
        super();
    	_xnatNode = xnatNode;
    	_xnatNodeInfoService = xnatNodeInfoService;
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.initialization.tasks.AbstractInitializingTask#getTaskName()
     */
    @Override
    public String getTaskName() {
        return "Record node information";
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.initialization.tasks.AbstractInitializingTask#callImpl()
     */
    @Override
    protected void callImpl() throws InitializingTaskException {
    	// We're only recording node information if either this node is configured or we have another node that has recorded configuration.
    	// Otherwise we'll basically not perform node/task related functions.
    	boolean hasConfiguredNode = false;
       	for (final XnatNodeInfo nodeInfo : _xnatNodeInfoService.getAll()) {
       		if (!nodeInfo.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
       			hasConfiguredNode = true;
       			break;
       		}
       	}
       	if (hasConfiguredNode || !_xnatNode.getNodeId().equals(XnatNode.NODE_ID_NOT_CONFIGURED)) {
       		_xnatNodeInfoService.recordNodeInitialization();
       	}
    }

    ///** The Constant _log. */
    //private static final Logger _log              = LoggerFactory.getLogger(RecordNodeInfo.class);

}
