/*
 * web: org.nrg.dcm.scp.DicomSCPSiteConfigurationListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.xdat.XDAT;

@SuppressWarnings("unused")
public class DicomSCPSiteConfigurationListener implements SiteConfigurationPropertyChangedListener {
	@Override
	public void siteConfigurationPropertyChanged(String propertyName, String newPropertyValue) {
		try {
			XDAT.getContextService().getBean(DicomSCPManager.class).start();
		} catch (UnknownDicomHelperInstanceException | DicomNetworkException e) {
			throw new RuntimeException(e);
		}
	}
}
