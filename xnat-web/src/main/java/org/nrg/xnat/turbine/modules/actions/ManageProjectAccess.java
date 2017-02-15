/*
 * web: org.nrg.xnat.turbine.modules.actions.ManageProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ManageProjectAccess extends SecureAction {
    private static final Logger logger = LoggerFactory.getLogger(ManageProjectAccess.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {

        final String projectId = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project", data));
        final XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, null, false);
        if (project == null) {
            error(new ItemNotFoundException("Could not find a project with the ID " + projectId), data);
            return;
        }
        PersistentWorkflowI wrk = WorkflowUtils.buildOpenWorkflow(TurbineUtils.getUser(data), "xnat:projectData", project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS));
        EventMetaI c = wrk.buildEvent();
        UserI user = TurbineUtils.getUser(data);
        if (!Permissions.canEdit(user, project)) {
            error(new InvalidPermissionException("User cannot modify project " + project.getId()), data);
            return;
        }

        String accessibility = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("accessibility", data));
        Permissions.setDefaultAccessibility(project.getId(), accessibility, false, user, c);

        boolean sendmail = false;
        if (null != TurbineUtils.GetPassedParameter("sendmail", data) && TurbineUtils.GetPassedParameter("sendmail", data).equals("email"))
            sendmail = true;

        String collaborators = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("collaborators", data));
        String members = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("members", data));
        String owners = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("owners", data));

        List<String> ownersL = XftStringUtils.CommaDelimitedStringToArrayList(owners);
        List<String> membersL = XftStringUtils.CommaDelimitedStringToArrayList(members);
        List<String> collaboratorsL = XftStringUtils.CommaDelimitedStringToArrayList(collaborators);


        if (owners != null) {
            ArrayList<String> currentOwners = project.getGroupMembers(BaseXnatProjectdata.OWNER_GROUP);

            for (String newOwner : ownersL) {
                if (!currentOwners.contains(newOwner)) {

                    //ADD MEMBER
                    for (UserI newUOM : Users.getUsersByEmail(newOwner)) {
                        final PersistentWorkflowI wrk2 = PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), project.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USER_TO_PROJECT + " (" + newUOM.getLogin() + ")"));

                        project.addGroupMember(project.getId() + "_" + BaseXnatProjectdata.OWNER_GROUP, newUOM, TurbineUtils.getUser(data), c);
                        PersistentWorkflowUtils.save(wrk2, c);
                    }
                    if (sendmail) {
                        context.put("user", XDAT.getUserDetails());
                        context.put("server", TurbineUtils.GetFullServerPath());
                        context.put("siteLogoPath", XDAT.getSiteLogoPath());
                        context.put("process", "Transfer to the archive.");
                        context.put("system", TurbineUtils.GetSystemName());
                        context.put("access_level", "owner");
                        context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
                        context.put("projectOM", project);
                        org.nrg.xnat.turbine.modules.actions.ProcessAccessRequest.SendAccessApprovalEmail(context, newOwner, TurbineUtils.getUser(data), TurbineUtils.GetSystemName() + " Access Granted for " + project.getName());
                    }
                }
            }

            for (String newOwner : currentOwners) {
                if (!ownersL.contains(newOwner)) {
                    //REMOVE MEMBER//ADD MEMBER
                    for (UserI newUOM : Users.getUsersByEmail(newOwner)) {
                        project.removeGroupMember(project.getId() + "_" + BaseXnatProjectdata.OWNER_GROUP, newUOM, TurbineUtils.getUser(data), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USER_FROM_PROJECT + " (" + newUOM.getLogin() + ")"));
                    }
                }
            }
        }

        if (members != null) {
            ArrayList<String> currentMembers = project.getGroupMembers(BaseXnatProjectdata.MEMBER_GROUP);

            for (String newMember : membersL) {
                if (!currentMembers.contains(newMember)) {
                    //ADD MEMBER
                    for (UserI newU : Users.getUsersByEmail(newMember)) {
                        final PersistentWorkflowI wrk2 = PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), project.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USER_TO_PROJECT + " (" + newU.getLogin() + ")"));
                        project.addGroupMember(project.getId() + "_" + BaseXnatProjectdata.MEMBER_GROUP, newU, TurbineUtils.getUser(data), c);
                        PersistentWorkflowUtils.complete(wrk2, c);
                    }
                    if (sendmail) {
                        context.put("user", XDAT.getUserDetails());
                        context.put("server", TurbineUtils.GetFullServerPath());
                        context.put("siteLogoPath", XDAT.getSiteLogoPath());
                        context.put("process", "Transfer to the archive.");
                        context.put("system", TurbineUtils.GetSystemName());
                        context.put("access_level", "member");
                        context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
                        context.put("projectOM", project);
                        org.nrg.xnat.turbine.modules.actions.ProcessAccessRequest.SendAccessApprovalEmail(context, newMember, TurbineUtils.getUser(data), TurbineUtils.GetSystemName() + " Access Granted for " + project.getName());
                    }
                }
            }

            for (String newMember : currentMembers) {
                if (!membersL.contains(newMember)) {
                    //REMOVE MEMBER//ADD MEMBER
                    for (UserI newUOM : Users.getUsersByEmail(newMember)) {
                        project.removeGroupMember(project.getId() + "_" + BaseXnatProjectdata.MEMBER_GROUP, newUOM, TurbineUtils.getUser(data), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USER_FROM_PROJECT + " (" + newUOM.getLogin() + ")"));
                    }
                }
            }
        }

        if (collaborators != null) {
            ArrayList<String> currentMembers = project.getGroupMembers(BaseXnatProjectdata.COLLABORATOR_GROUP);

            for (String newMember : collaboratorsL) {
                if (!currentMembers.contains(newMember)) {
                    //ADD MEMBER
                    for (UserI newU : Users.getUsersByEmail(newMember)) {
                        final PersistentWorkflowI wrk2 = PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), project.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USER_TO_PROJECT + " (" + newU.getLogin() + ")"));
                        project.addGroupMember(project.getId() + "_" + BaseXnatProjectdata.COLLABORATOR_GROUP, newU, TurbineUtils.getUser(data), c);
                        PersistentWorkflowUtils.complete(wrk2, c);
                    }
                    if (sendmail) {
                        context.put("user", XDAT.getUserDetails());
                        context.put("server", TurbineUtils.GetFullServerPath());
                        context.put("siteLogoPath", XDAT.getSiteLogoPath());
                        context.put("process", "Transfer to the archive.");
                        context.put("system", TurbineUtils.GetSystemName());
                        context.put("access_level", "collaborator");
                        context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
                        context.put("projectOM", project);
                        org.nrg.xnat.turbine.modules.actions.ProcessAccessRequest.SendAccessApprovalEmail(context, newMember, TurbineUtils.getUser(data), TurbineUtils.GetSystemName() + " Access Granted for " + project.getName());
                    }
                }
            }

            for (String newMember : currentMembers) {
                if (!collaboratorsL.contains(newMember)) {
                    //REMOVE MEMBER
                    for (UserI newU : Users.getUsersByEmail(newMember)) {
                        project.removeGroupMember(project.getId() + "_" + BaseXnatProjectdata.COLLABORATOR_GROUP, newU, TurbineUtils.getUser(data), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USER_FROM_PROJECT + " (" + newU.getLogin() + ")"));
                    }
                }
            }
        }

        PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());

        //UserGroupManager.Refresh();
        Users.clearCache(user);
        Groups.reloadGroupsForUser(user);

        this.redirectToReportScreen("XDATScreen_report_xnat_projectData.vm", project, data);
    }


}
