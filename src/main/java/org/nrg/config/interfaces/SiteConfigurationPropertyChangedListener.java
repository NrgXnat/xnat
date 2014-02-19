/*
 * org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/26/13 6:15 PM
 */
package org.nrg.config.interfaces;

public interface SiteConfigurationPropertyChangedListener {
	
	void siteConfigurationPropertyChanged(String propertyName, String newPropertyValue);
}
