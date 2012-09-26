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
