/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_scr_screeningAssessment
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.ScrScreeningassessment;
import org.nrg.xdat.om.ScrScreeningscandata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/**
 * @author XDAT
 *
 */
public class XDATScreen_edit_scr_screeningAssessment extends org.nrg.xnat.turbine.modules.screens.EditImageAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_scr_screeningAssessment.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "scr:screeningAssessment";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception
	{
		final UserI user = TurbineUtils.getUser(data);
		final ScrScreeningassessment screeningAssessment = new ScrScreeningassessment(XFTItem.NewItem(getElementName(), user));
		final String search_element = TurbineUtils.GetSearchElement(data);
		if (!StringUtils.isEmpty(search_element)) {
			final GenericWrapperElement se = GenericWrapperElement.GetElement(search_element);
			if (se.instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)) {
				final String search_value = data.getParameters().getString("search_value");
				if (!StringUtils.isEmpty(search_value)) {
					XnatImagesessiondata imageSession = new XnatImagesessiondata(TurbineUtils.GetItemBySearch(data));
					screeningAssessment.setImagesessionId(search_value);
					screeningAssessment.setProject(imageSession.getProject());
					screeningAssessment.setId(XnatExperimentdata.CreateNewID());
			    	//screeningAssessment.setLabel(generateLabel(imageSession.getLabel()));
				   	DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
					screeningAssessment.setDate(dateFormat.format(Calendar.getInstance().getTime()));
					
					for (XnatImagescandataI imageScan: imageSession.getScans_scan()){
						ScrScreeningscandata scan = new ScrScreeningscandata(user);
						scan.setImagescanId(imageScan.getId());
						screeningAssessment.setScans_scan(scan);
					}
				}
			}
		}

		return screeningAssessment.getItem();
		
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		
	
	}
	
	private static List<String> claimedIDs=new ArrayList<String>();
	
	private synchronized String generateLabel(String sessionLabel) throws Exception{
		sessionLabel = cleanupLabel(sessionLabel);
		String labelExtension = "_screen";
		String labelExtensionEscaped = "\\" + labelExtension;
		String column = "label";
		String tableName = "xnat_experimentData";
		int digits = 5;
		String temp_id=null;
		
		XFTTable table = org.nrg.xft.search.TableSearch.Execute("SELECT " + column + " FROM " + tableName + " WHERE " + column + " LIKE '" + sessionLabel + labelExtensionEscaped + "%';", null, null);
        ArrayList al =table.convertColumnToArrayList(column.toLowerCase());
        
        if (al.size()>0 || claimedIDs.size()>0){
            int count =al.size()+1;
            String full = org.apache.commons.lang.StringUtils.leftPad((new Integer(count)).toString(), digits, '0');
            temp_id = sessionLabel+labelExtension+ full;

            while (al.contains(temp_id) || claimedIDs.contains(temp_id)){
                count++;
                full =org.apache.commons.lang.StringUtils.leftPad((new Integer(count)).toString(), digits, '0');
                temp_id = sessionLabel+labelExtension+ full;
            }
            
            claimedIDs.add(temp_id);

            return temp_id;
        }else{
            int count =1;
            String full = org.apache.commons.lang.StringUtils.leftPad((new Integer(count)).toString(), digits, '0');
            temp_id = sessionLabel+labelExtension+ full;
            return temp_id;
        }
	}
	
	private static String cleanupLabel(String sessionLabel){
		sessionLabel = StringUtils.replace(sessionLabel, " ", "");
		sessionLabel = StringUtils.replace(sessionLabel, "-", "_");
		sessionLabel = StringUtils.replace(sessionLabel, "\"", "");
		sessionLabel = StringUtils.replace(sessionLabel, "'", "");
		sessionLabel = StringUtils.replace(sessionLabel, "^", "");
		return sessionLabel;
	}
}


