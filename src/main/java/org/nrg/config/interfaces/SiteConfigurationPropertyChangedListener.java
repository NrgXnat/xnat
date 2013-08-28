/*
 * org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.interfaces;

public interface SiteConfigurationPropertyChangedListener {
	
	void siteConfigurationPropertyChanged(String propertyName, String newPropertyValue);
}
