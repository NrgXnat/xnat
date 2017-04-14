/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_download_sessions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class XDATScreen_download_sessions extends SecureScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        List<String> sessionList = null;
        String[] sessions = ((String[])TurbineUtils.GetPassedObjects("sessions",data));
        if (sessions == null) {
            // If the sessions aren't directly embedded in the data, check for a search element.
            String element = (String) TurbineUtils.GetPassedParameter("search_element", data);
            String field = (String) TurbineUtils.GetPassedParameter("search_field", data);
            String value = (String) TurbineUtils.GetPassedParameter("search_value", data);
            
            // TODO: For now, hard-limit this to MR sessions.
            if(StringUtils.isNotBlank(element) && StringUtils.isNotBlank(field) && StringUtils.isNotBlank(value)){
            	SchemaElement se=SchemaElement.GetElement(element);
            	if(se.getGenericXFTElement().instanceOf("xnat:imageSessionData")){
            		sessionList = new ArrayList<>();
                    sessionList.add(value);
            	}
            }

            // Add the targeted flag so that the page can display based on the single session-targeted action.
            context.put("targeted", true);
        }else{
            // Add the targeted flag so that the page can display based on the multiple session-targeted action.
            context.put("targeted", false);
            sessionList = Arrays.asList(sessions);
        }
             
        if (sessionList != null) {
            final StringBuilder sessionString = new StringBuilder();
            int counter = 0;
            for(String s : sessionList)
            {
            	if(s!=null){
                    if (counter++>0){
                        sessionString.append(",");
                    }
                    if(s.contains("'"))
                    {
                    	s= StringUtils.remove(s, '\'');
                    }
                    sessionString.append("'").append(s).append("'");
            	}
            }

            final UserI user = XDAT.getUserDetails();
            final String login = user.getLogin();

            final String project = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project", data));

            final String query;
            if (project == null) {
                query = "SELECT expt.id,COALESCE(expt.label,expt.id) AS IDS, modality, subj.id as subject" +
                        " FROM xnat_imageSessionData isd" +
                        " LEFT JOIN xnat_experimentData expt ON expt.id=isd.id" +
                        " LEFT JOIN xnat_subjectassessordata sa ON sa.id=expt.id" +
                        " LEFT JOIN xnat_subjectdata subj ON sa.subject_id=subj.id" +
                        " LEFT JOIN xnat_experimentData_share pp ON expt.id=pp.sharing_share_xnat_experimentda_id" +
                        " WHERE isd.ID IN (" + sessionString + ") ORDER BY IDS;";
            } else {
                if (!retrieveAllTags(user).contains(project)) {
                    Exception e = new Exception("Unknown project: " + project);
                    logger.error("", e);
                    this.error(e, data);
                    return;
                }

                query = "SELECT expt.id,COALESCE(pp.label,expt.label,expt.id) AS IDS, modality, subj.label as subject " +
                        " FROM xnat_imageSessionData isd" +
                        " LEFT JOIN xnat_experimentData expt ON expt.id=isd.id" +
                        " LEFT JOIN xnat_subjectassessordata sa ON sa.id=expt.id" +
                        " LEFT JOIN xnat_subjectdata subj ON sa.subject_id=subj.id" +
                        " LEFT JOIN xnat_experimentData_share pp ON expt.id=pp.sharing_share_xnat_experimentda_id AND pp.project='" + project + "'" +
                        " WHERE isd.ID IN (" + sessionString + ") ORDER BY IDS;";
            }

            XFTTable table = XFTTable.Execute(query, null, login);
            
            ArrayList<List> sessionSummary =table.toArrayListOfLists();
            
            context.put("sessionSummary", sessionSummary);
            
            //SELECT SCANS

            final String scansQuery = "SELECT type,COUNT(*) FROM xnat_imagescandata " +
                    " WHERE xnat_imagescandata.image_session_id IN (" + sessionString +") GROUP BY type ORDER BY type;";
            
            table = XFTTable.Execute(scansQuery, null, login);
            
            ArrayList<List> scans =table.toArrayListOfLists();
            context.put("scans", scans);
            
            //SELECT RECONS

            final String reconsQuery= "SELECT type,COUNT(*) FROM xnat_reconstructedimagedata " +
                    " WHERE xnat_reconstructedimagedata.image_session_id IN (" + sessionString +") GROUP BY type ORDER BY type;";
            
            table = XFTTable.Execute(reconsQuery, null, login);
            
            scans =table.toArrayListOfLists();
            context.put("recons", scans);
            
            //SELECT ASSESSORS

            final String assessorsQuery = "SELECT element_name,COUNT(*) " +
                    "FROM (SELECT xnat_imageassessordata_id, COUNT(*) FROM img_assessor_out_resource GROUP BY xnat_imageassessordata_id) img_ass_count  " +
                    "LEFT JOIN xnat_imageassessorData img_ass ON img_ass_count.xnat_imageassessordata_id=img_ass.id  " +
                    "LEFT JOIN xnat_experimentData expt ON img_ass.id=expt.id " +
                    "LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id  " +
                    " WHERE img_ass.imagesession_id IN (" + sessionString +") " +
                    "GROUP BY element_name ORDER BY element_name;";
            
            table = XFTTable.Execute(assessorsQuery, null, login);
            
            scans =table.toArrayListOfLists();
            
            final List<List<String>> assessors = new ArrayList<>();
            //noinspection unchecked
            for(final List<String> assessor : scans){
                final List<String> sub = new ArrayList<>();
                sub.addAll(assessor);
                sub.add(ElementSecurity.GetPluralDescription(assessor.get(0)));
                assessors.add(sub);
            }
            
            context.put("assessors", assessors);
            
            //SELECT SCAN_FORMATS

            final String scanFormatsQuery = "SELECT label,COUNT(*) FROM xnat_imagescandata JOIN xnat_abstractResource ON xnat_imagescandata.xnat_imagescandata_id=xnat_abstractResource.xnat_imagescandata_xnat_imagescandata_id " +
                    " WHERE xnat_imagescandata.image_session_id IN (" + sessionString +") GROUP BY label;";
            
            table = XFTTable.Execute(scanFormatsQuery, null, login);
            
            ArrayList<List> formats =table.toArrayListOfLists();
            context.put("scan_formats", formats);
            
            //SELECT RESOURCES

            final String resourceQuery = "SELECT label,COUNT(*) FROM xnat_experimentData_resource JOIN xnat_abstractResource ON xnat_experimentData_resource.xnat_abstractresource_xnat_abstractresource_id=xnat_abstractResource.xnat_abstractresource_id " +
                    " WHERE xnat_experimentData_resource.xnat_experimentdata_id IN (" + sessionString +") GROUP BY label;";
            
            table = XFTTable.Execute(resourceQuery, null, login);
            
            ArrayList<List> resources =table.toArrayListOfLists();
            context.put("resources", resources);
        }
    }
	public List<String> retrieveAllTags(final UserI user){
		try {
            //noinspection unchecked
            return (List<String>)(XFTTable.Execute("SELECT DISTINCT id from xnat_projectData;", user.getDBName(), user.getLogin()).convertColumnToArrayList("id"));
		} catch (SQLException | DBPoolException e) {
			logger.error("",e);
		}

        return Lists.newArrayList();
	}

}
