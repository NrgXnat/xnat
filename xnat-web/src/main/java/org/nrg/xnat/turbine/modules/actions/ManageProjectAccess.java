/*
 * web: org.nrg.xnat.turbine.modules.actions.ManageProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.nrg.xdat.security.helpers.Users.USERI_TO_USERNAME;

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
                final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USERS_TO_PROJECT + " (" + StringUtils.join(Lists.transform(added, USERI_TO_USERNAME), ", ") + ")"));
                Groups.addUsersToGroup(projectId + "_" + group, user, added, eventMeta);
                PersistentWorkflowUtils.complete(workflow, eventMeta);
            }

            final Set<String> removals = FluentIterable.from(current).filter(new Predicate<String>() {
                @Override
                public boolean apply(@Nullable final String member) {
                    return !updated.contains(member);
                }
            }).toSet();

            if (!removals.isEmpty()) {
                final List<UserI> removedUsers = new ArrayList<>();
                for (final String removal : removals) {
                    removedUsers.addAll(Users.getUsersByEmail(removal));
                }
                final String              removedUsernames = StringUtils.join(Lists.transform(removedUsers, USERI_TO_USERNAME), ", ");
                final PersistentWorkflowI removedWorkflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, newEventInstance(data, EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USERS_FROM_PROJECT + " (" + removedUsernames + ")"));
                Groups.removeUsersFromGroup(projectId + "_" + group, user, removedUsers, eventMeta);
                PersistentWorkflowUtils.complete(removedWorkflow, eventMeta);
            }
        }
        return added;
    }
    
    private void sendEmailsToNewMembers(final Context context, final UserI user, final XnatProjectdata project, final List<UserI> newMembers) throws Exception {
        final Set<String> emails = new HashSet<>(Lists.transform(newMembers, new Function<UserI, String>() {
            @Override
            public String apply(final UserI user) {
                return user.getEmail();
            }
        }));
        for (final String email : emails) {
            context.put("user", user);
            context.put("server", TurbineUtils.GetFullServerPath());
            context.put("siteLogoPath", XDAT.getSiteLogoPath());
            context.put("process", "Transfer to the archive.");
            context.put("system", TurbineUtils.GetSystemName());
            context.put("access_level", "collaborator");
            context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
            context.put("projectOM", project);
            ProcessAccessRequest.SendAccessApprovalEmail(context, email, user, TurbineUtils.GetSystemName() + " Access Granted for " + project.getName());
        }
    }
}
