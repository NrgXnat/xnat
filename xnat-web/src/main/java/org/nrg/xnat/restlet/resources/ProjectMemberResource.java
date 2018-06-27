/*
 * web: org.nrg.xnat.restlet.resources.ProjectMemberResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.noelios.restlet.ext.servlet.ServletCall;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.velocity.VelocityContext;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.modules.actions.ProcessAccessRequest;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class ProjectMemberResource extends SecureResource {
    public ProjectMemberResource(Context context, Request request, Response response) throws NotFoundException, ClientException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        setModifiable(true);

        final UserI user = getUser();

        displayHiddenUsers = Boolean.parseBoolean((String) getParameter(request, "DISPLAY_HIDDEN_USERS"));
        projectId = getUrlEncodedParameter(request, "PROJECT_ID");
        project = StringUtils.isNotBlank(projectId) ? XnatProjectdata.getProjectByIDorAlias(projectId, user, false) : null;
        groupId = getUrlEncodedParameter(request, "GROUP_ID");
        group = findGroup();

        if (group == null) {
            log.error("Couldn't find a group for the group ID '{}'", groupId);
            throw new NotFoundException(projectId + ": " + groupId);
        }

        final Method  method = request.getMethod();
        final boolean isPut  = method.equals(Method.PUT);
        if ((isPut || method.equals(Method.DELETE) && project == null)) {
            throw isPut ? new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "You must specify a project and group for this call.") : new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a project and users to be deleted from the group for this call.");
        }


        final String       userIdParameter = (String) getParameter(request, "USER_ID");
        final List<String> userIds         = userIdParameter.contains(",") ? Arrays.asList(userIdParameter.split("\\s+,\\s+")) : Collections.singletonList(userIdParameter);
        for (final String userId : userIds) {
            try {
                final List<? extends UserI> groupMembers = findGroupMember(userId);
                if (groupMembers == null) {
                    unknown.add(userId);
                } else {
                    users.addAll(groupMembers);
                }
            } catch (UserNotFoundException e) {
                unknown.add(userId);
            } catch (Exception e) {
                log.error("An error occurred trying to retrieve group information for project ID '{}', group ID '{}' and user ID(s) '{}'", projectId, groupId, userIdParameter, e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
    }

    @Override
    public boolean allowPost() {
        return false;
    }

    @Override
    public void handleDelete() {
        final UserI user = getUser();
        try {
            final PersistentWorkflowI workflow  = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, XdatUsergroup.SCHEMA_ELEMENT_NAME, groupId, projectId, newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USERS_FROM_PROJECT));
            final EventMetaI          eventMeta = workflow.buildEvent();
            if (Permissions.canDelete(user, project)) {
                Groups.removeUsersFromGroup(groupId, user, users, eventMeta);
                WorkflowUtils.complete(workflow, eventMeta);
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                WorkflowUtils.fail(workflow, eventMeta);
            }
        } catch (Exception e) {
            log.error("An error occurred deleting the group {}", groupId, e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        returnDefaultRepresentation();
    }

    @Override
    public void handlePut() {
        final HttpServletRequest request = ServletCall.getRequest(getRequest());
        try {
            final UserI user = getUser();
            if (Permissions.canDelete(user, project)) {
                if (unknown.size() > 0) {
                    //NEW USER
                    try {
                        for (String uID : unknown) {
                            VelocityContext context = new VelocityContext();
                            context.put("user", user);
                            context.put("server", TurbineUtils.GetFullServerPath(request));
                            context.put("siteLogoPath", XDAT.getSiteLogoPath());
                            context.put("process", "Transfer to the archive.");
                            context.put("system", TurbineUtils.GetSystemName());
                            context.put("access_level", groupId);
                            context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
                            context.put("projectOM", project);
                            //SEND email to user
                            final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.INVITE_USER_TO_PROJECT + " (" + uID + ")"));
                            try {
                                ProjectAccessRequest.InviteUser(context, uID, user, user.getFirstname() + " " + user.getLastname() + " has invited you to join the " + project.getName() + " " + DisplayManager.GetInstance().getSingularDisplayNameForProject().toLowerCase() + ".");
                                WorkflowUtils.complete(wrk, wrk.buildEvent());
                            } catch (Exception e) {
                                WorkflowUtils.fail(wrk, wrk.buildEvent());
                                log.error("", e);
                            }
                        }
                    } catch (Throwable e) {
                        log.error("", e);
                    }
                }

                if (users.size() > 0) {
                    //CURRENT USER
                    final boolean sendmail = isQueryVariableTrue("sendemail");

                    for (final UserI newUser : users) {
                        if (newUser != null && newUser.getID().equals(Users.getGuest().getID())) {
                            getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
                        } else {
                            final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, Users.getUserDataType(), newUser.getID().toString(), project.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USER_TO_PROJECT));
                            final EventMetaI          eventMeta = workflow.buildEvent();

                            project.addGroupMember(group.getId(), newUser, user, WorkflowUtils.setStep(workflow, "Add " + newUser.getLogin()));
                            WorkflowUtils.complete(workflow, eventMeta);

                            if (sendmail) {
                                try {
                                    final VelocityContext context = new VelocityContext();
                                    context.put("user", user);
                                    context.put("server", TurbineUtils.GetFullServerPath(request));
                                    context.put("siteLogoPath", XDAT.getSiteLogoPath());
                                    context.put("process", "Transfer to the archive.");
                                    context.put("system", TurbineUtils.GetSystemName());
                                    context.put("access_level", group.getDisplayname());
                                    context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
                                    context.put("projectOM", project);
                                    ProcessAccessRequest.SendAccessApprovalEmail(context, newUser.getEmail(), user, TurbineUtils.GetSystemName() + " Access Granted for " + project.getName());
                                } catch (Throwable e) {
                                    log.error("", e);
                                }
                            }
                        }
                    }
                }
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
        } catch (InvalidItemException e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (ActionException e) {
            getResponse().setStatus(e.getStatus());
            return;
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        returnDefaultRepresentation();
    }

    @Override
    public Representation represent(Variant variant) {
        XFTTable table = null;
        if (project != null) {
            try {
                StringBuilder query = new StringBuilder("SELECT g.id AS \"GROUP_ID\", displayname,login,firstname,lastname,email FROM xdat_userGroup g RIGHT JOIN xdat_user_Groupid map ON g.id=map.groupid RIGHT JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id=u.xdat_user_id WHERE tag='").append(project.getId()).append("' ");
                if (!displayHiddenUsers) {
                    query.append(" and enabled = 1 ");
                }
                query.append(" ORDER BY g.id DESC;");
                final UserI user = getUser();
                table = XFTTable.Execute(query.toString(), user.getDBName(), user.getLogin());
            } catch (SQLException | DBPoolException e) {
                log.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }

        Hashtable<String, Object> params = new Hashtable<>();
        params.put("title", "Projects");

        MediaType mt = overrideVariant(variant);

        if (table != null) {
            params.put("totalRecords", table.size());
        }
        return representTable(table, mt, params);
    }

    private UserGroupI findGroup() {
        final UserGroupI groupById = Groups.getGroup(groupId);
        if (groupById != null) {
            return groupById;
        }
        final UserGroupI groupByProjectId = Groups.getGroup(projectId + "_" + groupId);
        if (groupByProjectId != null) {
            return groupByProjectId;
        }
        if (StringUtils.isNotBlank(projectId)) {
            try {
                for (final UserGroupI groupByTag : Groups.getGroupsByTag(projectId)) {
                    if (StringUtils.equals(groupId, groupByTag.getDisplayname())) {
                        return groupByTag;
                    }
                }
            } catch (Exception e) {
                log.error("An error occurred trying to retrieve groups by tag for the project ID {}", projectId, e);
            }
        }
        return null;
    }

    private List<? extends UserI> findGroupMember(final String userId) throws UserNotFoundException {
        try {
            if (NumberUtils.isParsable(userId)) {
                final Integer xdatUserId  = NumberUtils.createInteger(userId);
                final UserI   groupMember = Users.getUser(xdatUserId);
                if (groupMember != null) {
                    return Collections.singletonList(groupMember);
                }
            }

            final UserI groupMember = Users.getUser(userId);
            if (groupMember != null) {
                return Collections.singletonList(groupMember);
            }

            final List<? extends UserI> items = Users.getUsersByEmail(userId);
            if (!items.isEmpty()) {
                return items;
            }
            throw new UserNotFoundException(userId);
        } catch (UserInitException e) {
            log.error("An exception occurred trying to retrieve the user '{}'", userId, e);
        }
        return null;
    }

    private final String          projectId;
    private final XnatProjectdata project;
    private final String          groupId;
    private final UserGroupI      group;
    private final boolean         displayHiddenUsers;
    private final List<UserI>     users   = new ArrayList<>();
    private final List<String>    unknown = new ArrayList<>();
}
