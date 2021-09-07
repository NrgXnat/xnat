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

        _displayHiddenUsers = Boolean.parseBoolean((String) getParameter(request, "DISPLAY_HIDDEN_USERS"));
        _projectId = getUrlEncodedParameter(request, "PROJECT_ID");
        _project = StringUtils.isNotBlank(_projectId) ? XnatProjectdata.getProjectByIDorAlias(_projectId, user, false) : null;

        final String groupId = getUrlEncodedParameter(request, "GROUP_ID");
        _group = findGroup(_projectId, groupId);

        if (_group == null) {
            log.error("Couldn't find a group for the group ID '{}'", groupId);
            throw new NotFoundException(_projectId + ": " + groupId);
        }

        _groupId = _group.getId();

        final Method method = request.getMethod();
        if ((method.equals(Method.PUT) || method.equals(Method.DELETE)) && _project == null) {
            throw method.equals(Method.PUT) ? new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "You must specify a project and group for this call.") : new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a project and users to be deleted from the group for this call.");
        }

        final String       userIdParameter = (String) getParameter(request, "USER_ID");
        final List<String> userIds         = userIdParameter.contains(",") ? Arrays.asList(userIdParameter.split("\\s+,\\s+")) : Collections.singletonList(userIdParameter);
        for (final String userId : userIds) {
            try {
                final List<? extends UserI> groupMembers = findGroupMember(userId);
                if (groupMembers == null) {
                    _unknown.add(userId);
                } else {
                    _users.addAll(groupMembers);
                }
            } catch (UserNotFoundException e) {
                _unknown.add(userId);
            } catch (Exception e) {
                log.error("An error occurred trying to retrieve group information for project ID '{}', group ID '{}' and user ID(s) '{}'", _projectId, _groupId, userIdParameter, e);
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
            final PersistentWorkflowI workflow  = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, XdatUsergroup.SCHEMA_ELEMENT_NAME, _groupId, _projectId, newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.REMOVE_USERS_FROM_PROJECT));
            final EventMetaI          eventMeta = workflow.buildEvent();
            if (Permissions.canDelete(user, _project)) {
                Groups.removeUsersFromGroup(_groupId, user, _users, eventMeta);
                WorkflowUtils.complete(workflow, eventMeta);
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                WorkflowUtils.fail(workflow, eventMeta);
            }
        } catch (Exception e) {
            log.error("An error occurred deleting the group {}", _groupId, e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        returnDefaultRepresentation();
    }

    @Override
    public void handlePut() {
        final HttpServletRequest request = ServletCall.getRequest(getRequest());
        try {
            final UserI user = getUser();
            if (Permissions.canDelete(user, _project)) {
                if (_unknown.size() > 0) {
                    //NEW USER
                    try {
                        for (String uID : _unknown) {
                            //SEND email to user
                            final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, _project.getId(), _project.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.INVITE_USER_TO_PROJECT + " (" + uID + ")"));
                            try {
                                ProjectAccessRequest.InviteUser(_project, uID, user, user.getFirstname() + " " + user.getLastname() + " has invited you to join the " + _project.getName() + " " + DisplayManager.GetInstance().getSingularDisplayNameForProject().toLowerCase() + ".", _groupId);
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

                if (_users.size() > 0) {
                    //CURRENT USER
                    final boolean sendmail = isQueryVariableTrue("sendemail");

                    for (final UserI newUser : _users) {
                        if (newUser.getID().equals(Users.getGuest().getID())) {
                            getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
                        } else {
                            final PersistentWorkflowI workflow  = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, Users.getUserDataType(), newUser.getID().toString(), _project.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.ADD_USER_TO_PROJECT));
                            final EventMetaI          eventMeta = workflow.buildEvent();

                            _project.addGroupMember(_groupId, newUser, user, WorkflowUtils.setStep(workflow, "Add " + newUser.getLogin()));
                            WorkflowUtils.complete(workflow, eventMeta);

                            if (sendmail) {
                                try {
                                    String body = XDAT.getNotificationsPreferences().getEmailMessageProjectAccessApproval();
                                    body = body.replaceAll("PROJECT_NAME", _project.getName());
                                    body = body.replaceAll("RQ_ACCESS_LEVEL", _group.getDisplayname());
                                    body = body.replaceAll("SITE_URL",TurbineUtils.GetFullServerPath());

                                    final String respondAccessUrl = TurbineUtils.GetFullServerPath() +"/app/template/XDATScreen_report_xnat_projectData.vm/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/" + _project.getId();

                                    String accessUrl = "<a href=\"" + respondAccessUrl + "\">" + respondAccessUrl + "</a>";

                                    body = body.replaceAll("ACCESS_URL", accessUrl);
                                    body = body.replaceAll("SITE_NAME",TurbineUtils.GetSystemName());

                                    String adminEmailLink = "<a href=\"mailto:" + XDAT.getSiteConfigPreferences().getAdminEmail() + "?subject=" + TurbineUtils.GetSystemName() + "Assistance\">" + TurbineUtils.GetSystemName() + " Management </a>";
                                    body = body.replaceAll("ADMIN_MAIL",adminEmailLink);

                                    String subject = TurbineUtils.GetSystemName() + " Access Request for " + _project.getName();

                                    String[] to = new String[]{newUser.getEmail()};
                                    String[] cc = new String[]{user.getEmail()};
                                    String[] bcc = new String[]{XDAT.getSiteConfigPreferences().getAdminEmail()};
                                    ProcessAccessRequest.SendAccessApprovalEmail(body, subject, to, cc, bcc);
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
        if (_project != null) {
            try {
                StringBuilder query = new StringBuilder("SELECT g.id AS \"GROUP_ID\", displayname,login,firstname,lastname,email FROM xdat_userGroup g RIGHT JOIN xdat_user_Groupid map ON g.id=map.groupid RIGHT JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id=u.xdat_user_id WHERE tag='").append(_project.getId()).append("' ");
                if (!_displayHiddenUsers) {
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

    private UserGroupI findGroup(final String projectId, final String groupId) {
        if (StringUtils.isBlank(groupId)) {
            return null;
        }
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

    private final String          _projectId;
    private final XnatProjectdata _project;
    private final String          _groupId;
    private final UserGroupI      _group;
    private final boolean         _displayHiddenUsers;
    private final List<UserI>     _users   = new ArrayList<>();
    private final List<String>    _unknown = new ArrayList<>();
}
