/*
 * org.nrg.config.listeners.FooNamespaceLevelListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.config.listeners;

import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;

public class FooNamespaceLevelListener implements
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

    public static void resetInvokedCount() {
        _invokedCount = 0;
    }

    private static int _invokedCount;
}
