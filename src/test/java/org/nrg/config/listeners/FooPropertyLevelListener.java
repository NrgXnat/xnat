/*
 * org.nrg.config.listeners.FooPropertyLevelListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.listeners;

import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;

public class FooPropertyLevelListener implements
		SiteConfigurationPropertyChangedListener {
	
	static {
		_invokedCount = 0;
	}
	
	@Override
	public void siteConfigurationPropertyChanged(String propertyName,
			String newPropertyValue) {
		++_invokedCount;
	}
	
	public static int getInvokedCount() {
		return _invokedCount;
	}

	private static int _invokedCount;
}
