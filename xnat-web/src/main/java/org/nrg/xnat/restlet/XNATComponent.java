/*
 * web: org.nrg.xnat.restlet.XNATComponent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xnat.restlet;

import org.restlet.Component;

/**
 * @author tolsen01
 *
 */
public class XNATComponent extends Component {

	/**
	 * 
	 */
	public XNATComponent() {
		super();
		
		getHosts().addAll(XNATRestletFactory.buildVirtualHosts(this.getContext().createChildContext()));
	}
}
