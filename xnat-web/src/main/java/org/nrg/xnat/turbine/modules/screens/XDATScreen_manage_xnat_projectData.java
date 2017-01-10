/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_manage_xnat_projectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.sql.SQLException;
import java.util.TreeMap;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

public class XDATScreen_manage_xnat_projectData  extends SecureReport {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_report_xnat_projectData.class);
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        XnatProjectdata project = (XnatProjectdata)om;
        UserI user = TurbineUtils.getUser(data);
        try {
            context.put("guest", project.getPublicAccessibility());

            PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, project.getXSIType(),project.getId(),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN,EventUtils.TYPE.WEB_FORM, "Re-initialized project permissions"));
                        
            try {
				if(project.initGroups()){
					PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
				}
			} catch (Exception e) {
            	PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
			}
            
            XFTTable table = XFTTable.Execute("select TRIM(email) AS email, lastname || ', ' || firstname AS user_name FROM xdat_user WHERE email IS NOT NULL ORDER BY LOWER(lastname);", project.getDBName(), user.getLogin());
            context.put("allUsers", table.convertToMap("user_name", "email",new TreeMap<Object, Object>()));
            
            context.put("pars",ProjectAccessRequest.RequestPARs(" proj_id='" + project.getId() + "' AND approved IS NULL", user));
            
            context.put("ownerEmails", project.getGroupMembers(BaseXnatProjectdata.OWNER_GROUP));
            context.put("membersEmails", project.getGroupMembers(BaseXnatProjectdata.MEMBER_GROUP));
            context.put("collaboratorEmails", project.getGroupMembers(BaseXnatProjectdata.COLLABORATOR_GROUP));

        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
