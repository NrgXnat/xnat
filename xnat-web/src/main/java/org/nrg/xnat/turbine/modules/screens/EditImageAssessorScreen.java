/*
 * org.nrg.xnat.turbine.modules.screens.EditImageAssessorScreen
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
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

public abstract class EditImageAssessorScreen extends EditScreenA {

	@Override
	public ItemI getEmptyItem(RunData data) throws Exception {
		final UserI user = TurbineUtils.getUser(data);
		final XnatImageassessordata assessor = (XnatImageassessordata)BaseElement.GetGeneratedItem(XFTItem.NewItem(getElementName(), user));
		final String search_element = TurbineUtils.GetSearchElement(data);
		if (!StringUtils.IsEmpty(search_element)) {
			final GenericWrapperElement se = GenericWrapperElement.GetElement(search_element);
			if (se.instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)) {
				final String search_value = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
				if (!StringUtils.IsEmpty(search_value)) {
					final XnatImagesessiondata imageSession = new XnatImagesessiondata(TurbineUtils.GetItemBySearch(data));

					// set defaults for new qc assessors
					assessor.setImagesessionId(search_value);
					assessor.setId(XnatExperimentdata.CreateNewID());
					assessor.setProject(imageSession.getProject());
				}
			}
		}

		return assessor.getItem();
	}


}
