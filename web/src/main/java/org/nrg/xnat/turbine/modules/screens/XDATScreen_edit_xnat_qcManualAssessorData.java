/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_qcManualAssessorData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatQcscandataI;
import org.nrg.xdat.om.XnatCtscandata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatMrqcscandata;
import org.nrg.xdat.om.XnatMrscandata;
import org.nrg.xdat.om.XnatOtherqcscandata;
import org.nrg.xdat.om.XnatPetqcscandata;
import org.nrg.xdat.om.XnatPetscandata;
import org.nrg.xdat.om.XnatQcmanualassessordata;
import org.nrg.xdat.om.XnatQcscandata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class XDATScreen_edit_xnat_qcManualAssessorData
		extends
		org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xnat_qcManualAssessorData {
	static Logger logger = Logger.getLogger(XDATScreen_edit_xnat_qcManualAssessorData.class);

	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xnat_qcManualAssessorData#getEmptyItem(org.apache.turbine.util.RunData)
	 */
	@Override
	public ItemI getEmptyItem(RunData data) throws Exception {
		final UserI user = TurbineUtils.getUser(data);
		final XnatQcmanualassessordata qcAccessor = new XnatQcmanualassessordata(XFTItem.NewItem(getElementName(), user));
		final String searchElement = TurbineUtils.GetSearchElement(data);
		if (!StringUtils.isEmpty(searchElement)) {
			final GenericWrapperElement se = GenericWrapperElement.GetElement(searchElement);
			if (se.instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)) {
				final String searchValue = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
				if (!StringUtils.isEmpty(searchValue)) {
					XnatImagesessiondata imageSession = new XnatImagesessiondata(TurbineUtils.GetItemBySearch(data));

					populateDetails(qcAccessor,imageSession,user,data);
				}
			}
		}

		return qcAccessor.getItem();
	}

	/**
	 * @param qcAccessor populate default qc assessor
	 * @param imageSession session for qc
	 * @param user for transaction
	 * @throws Exception passed out from XFT transactions
	 */
	private void populateDetails(final XnatQcmanualassessordata qcAccessor, final XnatImagesessiondata imageSession, final UserI user,final RunData data) throws Exception{
		qcAccessor.setImageSessionData(imageSession);

		// set defaults for new qc assessors
		if(StringUtils.isEmpty(qcAccessor.getImagesessionId())){
			qcAccessor.setImagesessionId(imageSession.getId());
		}

		if(StringUtils.isEmpty(qcAccessor.getId())){
			qcAccessor.setId(XnatExperimentdata.CreateNewID());
		}

		if(StringUtils.isEmpty(qcAccessor.getLabel())){
			qcAccessor.setLabel(imageSession.getLabel() + "_"+ Calendar.getInstance().getTimeInMillis());
		}


		if(StringUtils.isEmpty(qcAccessor.getProject())){
			qcAccessor.setProject(imageSession.getProject());
		}

		List<Object> types=Lists.newArrayList();
		if(TurbineUtils.HasPassedParameter("types", data)){
			//only show requested modalities
			types=Lists.newArrayList(Arrays.asList(TurbineUtils.GetPassedObjects("types", data)));
		}else if(qcAccessor.getScans_scan().size()>0){
			//show similar scans (by modality)
			for(final XnatQcscandataI scan: qcAccessor.getScans_scan()){
				if(!types.contains(scan.getXSIType())){
					types.add(scan.getXSIType());
				}
			}
		}

		for (XnatImagescandataI imageScan: imageSession.getScans_scan()){
			if(types.size()==0 || types.contains(imageScan.getXSIType())){
				XnatQcscandata scan= (XnatQcscandata)getQCScan(qcAccessor,imageScan.getId());
				if(scan==null){
					if (XnatPetscandata.SCHEMA_ELEMENT_NAME.equals(imageScan.getXSIType())) {
						scan = new XnatPetqcscandata(user);
					} else if (XnatMrscandata.SCHEMA_ELEMENT_NAME.equals(imageScan.getXSIType())) {
						scan = new XnatMrqcscandata(user);
					} else {
						scan = new XnatOtherqcscandata(user);
					}
					scan.setImagescanId(imageScan.getId());
					qcAccessor.setScans_scan(scan);
				}
			}
		}
	}

	/**
	 * @param qc assessment
	 * @param imageScanId id of scan to retrieve
	 * @return matched scan
	 */
	private XnatQcscandataI getQCScan(final XnatQcmanualassessordata qc, final String imageScanId){
		for(XnatQcscandataI s: qc.getScans_scan()){
			if(imageScanId.equals(s.getImagescanId())){
				return s;
			}
		}

		return null;
	}

	@Override
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data, context);
		final XnatQcmanualassessordata qcAccessor=(XnatQcmanualassessordata)context.get("om");

		if(qcAccessor.getImageSessionData()!=null){
			try {
				populateDetails(qcAccessor,qcAccessor.getImageSessionData(),TurbineUtils.getUser(data),data);
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}
}
