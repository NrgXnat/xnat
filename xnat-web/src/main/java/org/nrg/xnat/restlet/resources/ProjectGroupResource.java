/*
 * web: org.nrg.xnat.restlet.resources.ProjectGroupResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.nrg.xdat.om.base.auto.AutoXdatUsergroup.SCHEMA_ELEMENT_NAME;
import static org.nrg.xft.event.XftItemEventI.UPDATE;
import static org.restlet.data.Method.DELETE;
import static org.restlet.data.Method.GET;

@Slf4j
public class ProjectGroupResource extends SecureResource {
    public ProjectGroupResource(Context context, Request request, Response response) throws ClientException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));

        _template = XDAT.getNamedParameterJdbcTemplate();
        _service = XDAT.getContextService().getBean(UserGroupServiceI.class);

        final String projectId = (String) getParameter(request, "PROJECT_ID");
        if (StringUtils.isBlank(projectId)) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify the PROJECT_ID parameter to call this function.");
        }

        _project = XnatProjectdata.getProjectByIDorAlias(projectId, getUser(), false);
        if (_project == null) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find the requested project: " + projectId);
        }

        // This could be the group name or ID.
        final String groupId     = StringUtils.defaultIfBlank((String) getParameter(request, "GROUP_ID"), getBodyVariable("xdat:userGroup/ID"));
        final String displayName = getBodyVariable("xdat:userGroup/displayName");

        // If they didn't specify a group name or ID and this isn't a get, we can't do anything.
        // Even when creating a new group there must be an ID.
        final Method method = request.getMethod();
        if (StringUtils.isBlank(groupId) && (method.equals(DELETE) || !method.equals(GET) && StringUtils.isBlank(displayName))) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify one of the GROUP_ID, xdat:userGroup/ID, or xdat:userGroup/displayName parameters to call this function.");
        }

        _group = findGroupByNameAndDisplayName(groupId, displayName);
        if (_group == null && DELETE.equals(method)) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find the requested project: " + projectId);
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void handleDelete() {
        if (PROTECTED_DISPLAY_NAMES.contains(_group.getDisplayname())) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return;
        }
        final UserI user = getUser();
        try {
            if (!UserHelper.getUserHelperService(user).canDelete(_project)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return;
            }

            final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, _project.getId(), _project.getId(), EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.TYPE.WEB_SERVICE, "Remove Group"));
            final EventMetaI          ci       = workflow.buildEvent();
            _service.deleteGroup(_group, user, ci);
            WorkflowUtils.complete(workflow, ci);
        } catch (InvalidItemException e) {
            log.error("An invalid item was detected", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (Exception e) {
            log.error("An unexpected exception occurred", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        returnDefaultRepresentation();
    }

    public void handlePost() {
        handlePut();
    }

    @Override
    public void handlePut() {
        try {
            final UserI user = getUser();
            if (!Permissions.canDelete(user, _project)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return;
            }
            final Map<String, Object> groupProperties = new HashMap<>();
            groupProperties.putAll(getQueryVariablesAsMap());
            groupProperties.putAll(getBodyVariableMap());

            final List<PermissionCriteriaI> newPermissions = new ArrayList<>();

            try {
                final UserGroupI working = _service.createGroup(groupProperties);

                //tag must be for this project
                if (!StringUtils.equals(_project.getId(), working.getTag())) {
                    working.setTag(_project.getId());
                }

                //display name is required
                if (StringUtils.isEmpty(working.getDisplayname())) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Display name is required.");
                    return;
                }
                //display name cannot contain underscore
                if (working.getDisplayname().contains("_")) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Display name cannot contain underscores.");
                    return;
                }

                //set ID to the standard value
                if (StringUtils.isEmpty(working.getId())) {
                    working.setId(_project.getId() + "_" + StringUtils.removeAll(working.getDisplayname(), "\\s?"));
                }

                final List<ElementSecurity> elements = ElementSecurity.GetSecureElements();

                for (final ElementSecurity element : elements) {
                    final List<String> permissionItems = element.getPrimarySecurityFields();
                    for (final String securityField : permissionItems) {
                        final PermissionCriteria criteria = new PermissionCriteria(element.getElementName());
                        criteria.setField(securityField);
                        criteria.setFieldValue(working.getTag());

                        final String elementId = element.getElementName() + "_" + securityField + "_" + working.getTag();
                        if (groupProperties.get(elementId + "_R") != null) {
                            criteria.setRead(true);
                        } else {
                            criteria.setRead(false);
                        }
                        if (groupProperties.get(elementId + "_E") != null && !StringUtils.equals(element.getElementName(), XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                            criteria.setRead(true);
                            criteria.setEdit(true);
                            criteria.setCreate(true);
                            criteria.setActivate(true);
                        } else {
                            criteria.setCreate(false);
                            criteria.setEdit(false);
                            criteria.setActivate(false);
                        }
                        if (groupProperties.get(elementId + "_D") != null && !StringUtils.equals(element.getElementName(), XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                            criteria.setRead(true);
                            criteria.setDelete(true);
                        } else {
                            criteria.setDelete(false);
                        }
                        criteria.setComparisonType("equals");

                        final boolean wasSet = StringUtils.equals((String) groupProperties.get(elementId + "_wasSet"), "1");

                        if (wasSet || criteria.getCreate() || criteria.getRead() || criteria.getEdit() || criteria.getDelete() || criteria.getActivate()) {
                            newPermissions.add(criteria);
                            //inherit project permissions to shared project permissions
                            if (StringUtils.equals(criteria.getField(), element.getElementName() + "/project") && (wasSet || criteria.getRead())) {
                                final PermissionCriteria share = new PermissionCriteria(element.getElementName());
                                share.setField(element.getElementName() + "/sharing/share/project");
                                share.setFieldValue(working.getTag());
                                share.setRead(criteria.getRead());
                                share.setComparisonType("equals");
                                newPermissions.add(share);
                            }
                        }
                    }
                }

                final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, SCHEMA_ELEMENT_NAME, working.getTag(), working.getId(), EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.WEB_SERVICE, (_group == null) ? "Added user group" : "Modified user group."));
                assert wrk != null;

                //need to pre-create the group if it doesn't already exist
                if (_group == null) {
                    _service.save(working, user, wrk.buildEvent());
                }

                Permissions.setPermissionsForGroup(working, newPermissions, wrk.buildEvent(), user);
                Groups.save(working, user, wrk.buildEvent());
                WorkflowUtils.complete(wrk, wrk.buildEvent());
                Groups.reloadGroupsForUser(user);

                if (groupProperties.containsKey("src")) {
                    getResponse().setStatus(Status.REDIRECTION_SEE_OTHER);
                    getResponse().redirectSeeOther(XDAT.getSiteConfigPreferences().getSiteUrl() + "/data/projects/" + working.getTag() + "?format=html");
                } else {
                    returnDefaultRepresentation();
                }
            } catch (Exception e) {
                logger.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    @Override
    public Representation represent(Variant variant) {
        if (_group != null) {
            //return a particular group
            return representItem(((UserGroup) _group).getUserGroupImpl().getItem(), overrideVariant(variant));
        }
        //return a list of groups
        final UserI user = getUser();
        if (!Permissions.canReadProject(user, _project.getId())) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        }

        final ProjectGroupRowMapper mapper = new ProjectGroupRowMapper();
        _template.query(QUERY_PROJECT_GROUPS, new MapSqlParameterSource("projectId", _project.getId()), mapper);

        final XFTTable table = new XFTTable();
        table.initTable(GROUP_LIST_HEADERS, mapper.getRows());

        final Hashtable<String, Object> params = new Hashtable<>();
        params.put("title", "Projects");
        params.put("totalRecords", table.size());

        return representTable(table, overrideVariant(variant), params);
    }

    private UserGroupI findGroupByNameAndDisplayName(final String groupName, final String displayName) {
        if (StringUtils.isAllBlank(groupName, displayName)) {
            return null;
        }
        if (StringUtils.isNotBlank(groupName)) {
            if (NumberUtils.isCreatable(groupName)) {
                final UserGroupI group = Groups.getGroupByPK(groupName);
                if (group != null) {
                    return group;
                }
            }
            final UserGroupI byName = Groups.getGroup(groupName);
            if (byName != null) {
                return byName;
            }
            final UserGroupI byProjectAndGroupName = Groups.getGroup(_project.getId() + "_" + groupName);
            if (byProjectAndGroupName != null) {
                return byProjectAndGroupName;
            }
            final UserGroupI byTagAndName = Groups.getGroupByTagAndName(_project.getId(), groupName);
            if (byTagAndName != null) {
                return byTagAndName;
            }
        }
        if (StringUtils.isNotBlank(displayName)) {
            final UserGroupI byTagAndName = Groups.getGroupByTagAndName(_project.getId(), displayName);
            if (byTagAndName != null) {
                return byTagAndName;
            }
            return Groups.getGroup(_project.getId() + "_" + displayName);
        }
        return null;
    }

    private static class ProjectGroupRowMapper implements RowCallbackHandler {
        @Override
        public void processRow(final ResultSet row) throws SQLException {
            final List<Object> items = new ArrayList<>();
            for (final String header : GROUP_LIST_HEADERS) {
                items.add(row.getObject(header));
            }
            _rows.add(items.toArray(new Object[0]));
        }

        ArrayList<Object[]> getRows() {
            return _rows;
        }

        private final ArrayList<Object[]> _rows = new ArrayList<>();
    }

    private static final ArrayList<String> GROUP_LIST_HEADERS      = new ArrayList<>(Arrays.asList("id", "displayname", "tag", "xdat_usergroup_id", "users"));
    private static final List<String>      PROTECTED_DISPLAY_NAMES = Arrays.asList("Owners", "Members", "Collaborators");
    private static final String            QUERY_PROJECT_GROUPS    = "SELECT ug.id, ug.displayname,ug.tag,ug.xdat_usergroup_id, COUNT(map.groups_groupid_xdat_user_xdat_user_id) AS users FROM xdat_userGroup ug LEFT JOIN xdat_user_groupid map ON ug.id=map.groupid WHERE tag = :projectId GROUP BY ug.id, ug.displayname,ug.tag,ug.xdat_usergroup_id  ORDER BY ug.displayname DESC";

    private final NamedParameterJdbcTemplate _template;
    private final XnatProjectdata            _project;
    private final UserGroupI                 _group;
    private final UserGroupServiceI          _service;
}
