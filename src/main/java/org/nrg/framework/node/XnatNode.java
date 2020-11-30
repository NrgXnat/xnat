/*
 * framework: org.nrg.framework.node.XnatNode
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.node;

import lombok.Data;

/**
 * The Class XnatNode.
 */
@Data
public class XnatNode {
	public static final String NODE_ID_NOT_CONFIGURED = "UNCONFIGURED";
	
	/** The node id. */
	private final String nodeId;
}
