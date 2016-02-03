/*
 * org.nrg.xnat.restlet.XNATComponent
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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
