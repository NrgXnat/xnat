/*
 * config: org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.interfaces;

public interface SiteConfigurationPropertyChangedListener {
	
	void siteConfigurationPropertyChanged(String propertyName, String newPropertyValue);
}
