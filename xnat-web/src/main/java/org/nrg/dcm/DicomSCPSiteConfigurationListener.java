/*
 * org.nrg.dcm.DicomSCPSiteConfigurationListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm;

import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;
import org.nrg.xdat.XDAT;

public class DicomSCPSiteConfigurationListener implements
		SiteConfigurationPropertyChangedListener {
	
	@Override
	public void siteConfigurationPropertyChanged(String propertyName,
			String newPropertyValue) {
        XDAT.getContextService().getBean(DicomSCPManager.class).startOrStopDicomSCPAsDictatedByConfiguration();
	}
}
