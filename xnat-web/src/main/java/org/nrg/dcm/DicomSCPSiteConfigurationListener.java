/*
 * web: org.nrg.dcm.DicomSCPSiteConfigurationListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
