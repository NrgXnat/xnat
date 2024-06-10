/*
 * web: org.nrg.xnat.turbine.modules.actions.ManageProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
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
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.utils.WorkflowUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
public class ManageProjectAccess extends SecureAction {
    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        final String          projectId = ((String) TurbineUtils.GetPassedParameter("project", data));
        final XnatProjectdata project   = XnatProjectdata.getXnatProjectdatasById(projectId, null, false);

        if (project == null) {
            error(new ItemNotFoundException("Could not find a project with the ID " + projectId), data);
            return;
        }

        final UserI user = XDAT.getUserDetails();

        if (user == null) {
            throw new NrgServiceRuntimeException("No user found for accessing this function, that's not OK.");
        }
        
        final PersistentWorkflowI workflow  = WorkflowUtils.buildOpenWorkflow(user, "xnat:projectData", project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS));
        final EventMetaI          eventMeta = workflow.buildEvent();

        if (!Permissions.canEdit(user, project)) {
            error(new InvalidPermissionException("User cannot modify project " + project.getId()), data);
            return;
        }

        final String accessibility = (String) TurbineUtils.GetPassedParameter("accessibility", data);
        if (StringUtils.isNotBlank(accessibility) && !StringUtils.equals(accessibility, project.getPublicAccessibility())) {
            Permissions.setDefaultAccessibility(project.getId(), accessibility, false, user, eventMeta);
        }

        final List<String> owners = XftStringUtils.CommaDelimitedStringToArrayList((String) TurbineUtils.GetPassedParameter("owners", data));
        final List<String> members = XftStringUtils.CommaDelimitedStringToArrayList((String) TurbineUtils.GetPassedParameter("members", data));
        final List<String> collaborators = XftStringUtils.CommaDelimitedStringToArrayList((String) TurbineUtils.GetPassedParameter("collaborators", data));

        final List<UserI> updatedOwners = updateGroupMembers(data, user, project, BaseXnatProjectdata.OWNER_GROUP, owners, eventMeta);
        final List<UserI> updatedMembers = updateGroupMembers(data, user, project, BaseXnatProjectdata.MEMBER_GROUP, members, eventMeta);
        final List<UserI> updatedCollaborators = updateGroupMembers(data, user, project, BaseXnatProjectdata.COLLABORATOR_GROUP, collaborators, eventMeta);


        if (StringUtils.equals("email", (String) TurbineUtils.GetPassedParameter("sendmail", data))) {
            sendEmailsToNewMembers(context, user, project, updatedOwners);
            sendEmailsToNewMembers(context, user, project, updatedMembers);
            sendEmailsToNewMembers(context, user, project, updatedCollaborators);
        }

        PersistentWorkflowUtils.complete(workflow, workflow.buildEvent());

        //UserGroupManager.Refresh();
        Users.clearCache(user);
        Groups.reloadGroupsForUser(user);

        redirectToReportScreen("XDATScreen_report_xnat_projectData.vm", project, data);
    }
    
    private List<UserI> updateGroupMembers(final RunData data, final UserI user, final XnatProjectdata project, final String group, final List<String> updated, final EventMetaI eventMeta) throws Exception {
        final List<UserI> added = new ArrayList<>();
        if (!updated.isEmpty()) {
            final List<String> current = project.getGroupMembers(group);
            for (final String newMember : updated) {
                if (!current.contains(newMember)) {
                    added.addAll(Users.getUsersByEmail(newMember));
                }
            }

            final String projectId = project.getId();
            if (!added.isEmpty()) {
                final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USERS_TO_PROJECT + " (" + added.stream().map(UserI::getUsername).collect(Collectors.joining(", ")) + ")"));
                Groups.addUsersToGroup(projectId + "_" + group, user, added, eventMeta);
                PersistentWorkflowUtils.complete(workflow, eventMeta);
            }

            final Set<String> removals = current.stream().filter(member -> !updated.contains(member)).collect(Collectors.toSet());
            if (!removals.isEmpty()) {
                final List<UserI> removedUsers = new ArrayList<>();
                for (final String removal : removals) {
                    removedUsers.addAll(Users.getUsersByEmail(removal));
                }
                final String              removedUsernames = removedUsers.stream().map(UserI::getUsername).collect(Collectors.joining(", "));
                final PersistentWorkflowI removedWorkflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USERS_FROM_PROJECT + " (" + removedUsernames + ")"));
                Groups.removeUsersFromGroup(projectId + "_" + group, user, removedUsers, eventMeta);
                PersistentWorkflowUtils.complete(removedWorkflow, eventMeta);
            }
        }
        return added;
    }
    
    private void sendEmailsToNewMembers(final Context context, final UserI user, final XnatProjectdata project, final List<UserI> newMembers) {
        newMembers.stream().map(UserI::getEmail).collect(Collectors.toSet()).forEach(email -> {
            try {
                String body = XDAT.getNotificationsPreferences().getEmailMessageProjectAccessApproval();
                body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, null);
                body = body.replaceAll("PROJECT_NAME", project.getName());
                body = body.replaceAll("RQ_ACCESS_LEVEL", "collaborator");

                final String respondAccessUrl = TurbineUtils.GetFullServerPath() +"/app/template/XDATScreen_report_xnat_projectData.vm/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/" + project.getId();

                String accessUrl = "<a href=\"" + respondAccessUrl + "\">" + respondAccessUrl + "</a>";

                body = body.replaceAll("ACCESS_URL", accessUrl);

                String subject = TurbineUtils.GetSystemName() + " Access Request for " + project.getName();

                String[] cc = new String[]{user.getEmail()};
                String[] to = new String[]{email};
                String[] bcc = new String[]{XDAT.getSiteConfigPreferences().getAdminEmail()};

                ProcessAccessRequest.SendAccessApprovalEmail(body, subject, to, cc, bcc);
            } catch (Exception e) {
                log.error("An error occurred trying to send a new member email to user {} at email {}", user.getUsername(), email, e);
            }
        });
    }
}
