/*
 * web: org.nrg.xnat.turbine.modules.actions.ProcessAccessRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.action.InvalidParamsException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.nrg.xnat.utils.WorkflowUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;

public class ProcessAccessRequest extends SecureAction {
    static Logger logger = Logger.getLogger(ProcessAccessRequest.class);

    public void doDenial(RunData data, Context context) throws Exception {
        Integer id = TurbineUtils.GetPassedInteger("id",data);
        UserI other = Users.getUser(id);

        String p = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
        
        if(p==null || p.contains("'")){
        	error(new InvalidParamsException("project", p), data);
        }
        
        
        UserI user = TurbineUtils.getUser(data);
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(p, null, false);

		assert project != null;
		final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, AutoXnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REJECT_PROJECT_REQUEST));
    	EventMetaI                c   =wrk.buildEvent();
    	WorkflowUtils.save(wrk, c);
        
        if (other != null) {
        	if(!Permissions.canDelete(user, project)){
        		error(new InvalidPermissionException("Invalid permissions"),data);
        		return;
        	}
        	
	        try {
			    
			    for (Map.Entry<String, UserGroupI> entry:Groups.getGroupsForUser(other).entrySet()){
			        if (entry.getValue().getTag().equals(project.getId())){
			        	Groups.removeUserFromGroup(other, user, entry.getValue().getId(), c);
			        }
			    }
			    
			    ProjectAccessRequest par = ProjectAccessRequest.RequestPARByUserProject(other.getID(),project.getId(), user);
			    par.setApproved(false);
			    par.save(user);
				
		        WorkflowUtils.complete(wrk, c);
			} catch (Exception e) {
				WorkflowUtils.fail(wrk, c);
			}
			    
		    context.put("user",user);
		    context.put("server",TurbineUtils.GetFullServerPath());
			context.put("siteLogoPath", XDAT.getSiteLogoPath());
		    context.put("system",TurbineUtils.GetSystemName());
		    context.put("admin_email",XDAT.getSiteConfigPreferences().getAdminEmail());
		    context.put("projectOM",project);
		    StringWriter sw = new StringWriter();
		    Template template =Velocity.getTemplate("/screens/RequestProjectAccessDenialEmail.vm");
		    template.merge(context,sw);
		    String message= sw.toString();

		    String from = XDAT.getSiteConfigPreferences().getAdminEmail();
		    String subject = TurbineUtils.GetSystemName() + " Access Request for " + project.getName() + " Denied";

		    try {
            	XDAT.getMailService().sendHtmlMessage(from, other.getEmail(), user.getEmail(), XDAT.getSiteConfigPreferences().getAdminEmail(), subject, message);
		    } catch (Exception e) {
		        logger.error("Unable to send denial email",e);
		    }
		}

        //data.setScreenTemplate("XDATScreen_manage_xnat_projectData.vm");
        //data.setScreenTemplate("/xnat_projectData/xnat_projectData_summary_management.vm");        
        TurbineUtils.SetSearchProperties(data, project);
        //data.getParameters().setString("topTab", "Access");
        this.redirectToReportScreen("XDATScreen_report_xnat_projectData.vm", project, data);
    }
    
    public void doApprove(RunData data, Context context) throws Exception {
        Integer id = TurbineUtils.GetPassedInteger("id",data);
        UserI user = TurbineUtils.getUser(data);
        UserI other = Users.getUser(id);

        String p = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
        String access_level = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("access_level",data));
        if (StringUtils.isEmpty(access_level)){
        	access_level="member";
        }else{
        	if(!(access_level.equalsIgnoreCase(BaseXnatProjectdata.MEMBER_GROUP) 
        			|| access_level.equalsIgnoreCase(BaseXnatProjectdata.OWNER_GROUP)
        			|| access_level.equalsIgnoreCase(BaseXnatProjectdata.COLLABORATOR_GROUP))){
        		error(new Exception("Unknown Access level:"+access_level), data);
        		return;
        	}
        }
        
        if(p==null || p.contains("'")){
        	error(new InvalidParamsException("project", p),data);
        	return;
        }
        
        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(p, null, false);


		assert project != null;
		final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, AutoXnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.APPROVE_PROJECT_REQUEST));
    	EventMetaI                c   =wrk.buildEvent();
    	WorkflowUtils.save(wrk, c);
        
        if (other != null) {
        	if(!Permissions.canDelete(user, project)){
        		error(new InvalidPermissionException("Invalid permissions"),data);
        		return;
        	}

			try {				
				for (Map.Entry<String, UserGroupI> entry:Groups.getGroupsForUser(other).entrySet()){
				    if (entry.getValue()!=null && entry.getValue().getTag()!=null && entry.getValue().getTag().equals(project.getId())){
				    	Groups.removeUserFromGroup(other, user, entry.getValue().getId(), c);
				    }
				}
				
				project.addGroupMember(project.getId() + "_" + access_level.toLowerCase(), other, user,c);
				
				ProjectAccessRequest par = ProjectAccessRequest.RequestPARByUserProject(other.getID(),project.getId(), user);
				par.setApproved(true);
				par.save(user);
				WorkflowUtils.complete(wrk, c);
			} catch (Exception e) {
				WorkflowUtils.fail(wrk, c);
				throw e;
			}
                        
            context.put("user",user);
            context.put("server",TurbineUtils.GetFullServerPath());
			context.put("siteLogoPath", XDAT.getSiteLogoPath());
            context.put("process","Transfer to the archive.");
            context.put("system",TurbineUtils.GetSystemName());
            context.put("access_level",access_level);
            context.put("admin_email",XDAT.getSiteConfigPreferences().getAdminEmail());
            context.put("projectOM",project);
            final ArrayList<String> ownerEmails = project.getOwnerEmails();
            String[] projectOwnerEmails = ownerEmails.toArray(new String[ownerEmails.size()]);
            SendAccessApprovalEmail(context,XDAT.getSiteConfigPreferences().getAdminEmail(),new String[]{other.getEmail()},projectOwnerEmails,new String[]{XDAT.getSiteConfigPreferences().getAdminEmail()},TurbineUtils.GetSystemName() + " Access Request for " + project.getName() + " Approved");
        }      
        //data.setScreenTemplate("XDATScreen_manage_xnat_projectData.vm");
        //data.setScreenTemplate("/xnat_projectData/xnat_projectData_summary_management.vm");
        TurbineUtils.SetSearchProperties(data, project);
        //data.getParameters().setString("topTab", "Access");
        this.redirectToReportScreen("XDATScreen_report_xnat_projectData.vm", project, data);
    }
    
    public static void SendAccessApprovalEmail(Context context, String otherEmail, UserI user, String subject) throws Exception {
	String admin = XDAT.getSiteConfigPreferences().getAdminEmail();
        SendAccessApprovalEmail(context, admin, new String[]{otherEmail}, new String[]{user.getEmail()}, new String[]{admin}, subject);
    }

    public static void SendAccessApprovalEmail(Context context, String from, String[] to, String[] cc, String[] bcc, String subject) throws Exception{
        StringWriter sw = new StringWriter();
        Template template =Velocity.getTemplate("/screens/RequestProjectAccessApprovalEmail.vm");
        template.merge(context,sw);
        String message= sw.toString();

		try {
			XDAT.getMailService().sendHtmlMessage(from, to, cc, bcc, subject, message);
		} catch (Exception exception) {
			logger.error("Send failed. Retrying by sending each email individually.", exception);
			int successfulSends = 0;
			for (String recipient : to) {
				try {
					XDAT.getMailService().sendHtmlMessage(from, new String[]{recipient}, null, bcc, subject, message);
					successfulSends++;
				} catch (Exception e) {
					logger.error("Unable to send mail to " + recipient + ".", e);
				}
			}
			if (successfulSends == 0) {
				logger.error("Unable to send mail", exception);
			}
		}
    }

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocitySecureAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void doPerform(RunData data, Context context) throws Exception {

    }

    
}
