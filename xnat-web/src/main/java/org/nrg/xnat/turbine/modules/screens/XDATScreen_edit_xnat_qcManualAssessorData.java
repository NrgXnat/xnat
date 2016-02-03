/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_qcManualAssessorData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

import java.util.Calendar;

public class XDATScreen_edit_xnat_qcManualAssessorData
		extends
		org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xnat_qcManualAssessorData {

	@Override
	public ItemI getEmptyItem(RunData data) throws Exception {
		final UserI user = TurbineUtils.getUser(data);
		final XnatQcmanualassessordata qcAccessor = new XnatQcmanualassessordata(XFTItem.NewItem(getElementName(), user));
		final String search_element = TurbineUtils.GetSearchElement(data);
		if (!StringUtils.IsEmpty(search_element)) {
			final GenericWrapperElement se = GenericWrapperElement.GetElement(search_element);
			if (se.instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)) {
				final String search_value = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
				if (!StringUtils.IsEmpty(search_value)) {
					XnatImagesessiondata imageSession = new XnatImagesessiondata(TurbineUtils.GetItemBySearch(data));

					// set defaults for new qc assessors
					qcAccessor.setImagesessionId(search_value);
					qcAccessor.setId(XnatExperimentdata.CreateNewID());
					qcAccessor.setLabel(imageSession.getLabel() + "_"+ Calendar.getInstance().getTimeInMillis());
					qcAccessor.setProject(imageSession.getProject());
					
					for (XnatImagescandataI imageScan: imageSession.getScans_scan()){
						XnatQcscandata scan;
						if (XnatPetscandata.SCHEMA_ELEMENT_NAME.equals(imageScan.getXSIType())) {
							scan = new XnatPetqcscandata(user);
						} else if (XnatMrscandata.SCHEMA_ELEMENT_NAME.equals(imageScan.getXSIType())) {
							scan = new XnatMrqcscandata(user);
						} else {
							// do not create anything but PET and MR QCs for now (e.g. PET/CT, only do QC on PET)
							continue;
						}
						scan.setImagescanId(imageScan.getId());
						qcAccessor.setScans_scan(scan);
					}
				}
			}
		}

		return qcAccessor.getItem();
	}
}
