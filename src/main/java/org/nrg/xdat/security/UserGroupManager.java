/*
 * core: org.nrg.xdat.security.UserGroupManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import static lombok.AccessLevel.PRIVATE;
import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;
import static org.nrg.xdat.security.helpers.Groups.*;
import static org.nrg.xft.event.XftItemEventI.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class UserGroupManager implements UserGroupServiceI {
    @Autowired
    public UserGroupManager(final GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template, final DataTypeAwareEventService eventService, final DatabaseHelper helper) {
        _cache = cache;
        _template = template;
        _eventService = eventService;
        _helper = helper;
    }

    @SuppressWarnings("unused")
    public static List<String> formatIrregularProjectGroups(final List<Map<String, Object>> irregulars) {
        return irregulars.stream().map(mapping -> StringUtils.joinWith("\t", mapping.get("id"), mapping.get("xdat_field_mapping_id"), mapping.get("field"), mapping.get("mismatched_values"))).collect(Collectors.toList());
    }

    @Override
    public boolean exists(final String id) {
        return _template.queryForObject(QUERY_CHECK_GROUP_EXISTS, new MapSqlParameterSource("groupId", id), Boolean.class);
    }

    @Override
    public UserGroupI getGroup(final String groupId) {
        final UserGroup group = (UserGroup) getCache().get(groupId);
        if (group != null) {
            return group;
        }

        log.info("Couldn't find the group with ID {}", groupId);
        return null;
    }

    @Override
    public boolean isMember(UserI user, String grp) {
        return getGroupIdsForUser(user).contains(grp);
    }

    @Override
    public boolean isMember(final String username, final String groupId) throws UserNotFoundException {
        return getGroupIdsForUser(username).contains(groupId);
    }

    @Override
    public Map<String, UserGroupI> getGroupsForUser(final UserI user) {
        try {
            return getGroupsForUser(user.getUsername());
        } catch (UserNotFoundException ignored) {
            // We have the UserI object so we don't have to worry about user not found
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException {
        return getCache().getGroupsForUser(username);
    }

    @Override
    public UserGroupI getGroupForUserAndTag(final UserI user, final String tag) {
        try {
            return getGroupForUserAndTag(user.getUsername(), tag);
        } catch (UserNotFoundException e) {
            // We have the UserI object so we don't have to worry about user not found
            return null;
        }
    }

    @Override
    public UserGroupI getGroupForUserAndTag(final String username, final String tag) throws UserNotFoundException {
        return getCache().getGroupForUserAndTag(username, tag);
    }

    @Override
    public List<String> getGroupIdsForUser(final UserI user) {
        try {
            return getGroupIdsForUser(user.getUsername());
        } catch (UserNotFoundException e) {
            // We have the UserI object so we don't have to worry about user not found
            return null;
        }
    }

    @Override
    public List<String> getGroupIdsForUser(final String username) throws UserNotFoundException {
        return Lists.newArrayList(_cache.getGroupsForUser(username).keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserIdsForGroup(final XdatUsergroupI group) {
        return getUserIdsForGroup(group.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserIdsForGroup(final UserGroupI group) {
        return getUserIdsForGroup(group.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserIdsForGroup(final String groupId) {
        return _cache.getUserIdsForGroup(groupId);
    }

    @Override
    public void updateUserForGroup(UserI user, String groupId, UserGroupI group) {
        try {
            ((XDATUser) user).init();
        } catch (Exception e) {
            log.error("", e);
        }
        ((XDATUser) user).resetCriteria();
    }

    @Override
    public void removeUserFromGroup(final UserI user, final UserI currentUser, final String groupId, final EventMetaI eventMeta) {
        try {
            removeUserFromGroup((XDATUser) user, currentUser, groupId, eventMeta);
            XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, groupId, UPDATE, ImmutableMap.of(OPERATION, OPERATION_REMOVE_USERS, USERS, Collections.singleton(user.getUsername())));
        } catch (Exception e) {
            log.error("Tried and failed to remove the user '{}' from group '{}'", user.getUsername(), groupId, e);
        }
    }

    @Override
    public void removeUsersFromGroup(final String groupId, final UserI currentUser, final List<UserI> users, final EventMetaI eventMeta) {
        final List<String> failed = new ArrayList<>();
        final Set<String> usernames = users.stream().map(user -> {
            final String username = user.getUsername();
            try {
                removeUserFromGroup((XDATUser) user, currentUser, groupId, eventMeta);
                return username;
            } catch (Exception e) {
                log.error("An error occurred trying to remove the user '{}' from the group '{}'", username, groupId, e);
                failed.add(username);
                return null;
            }
        }).collect(Collectors.toList()).stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (!failed.isEmpty()) {
            log.error("Tried and failed to remove the following users from group '{}': {}", groupId, StringUtils.join(failed, ", "));
            usernames.removeAll(failed);
        }
        XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, groupId, UPDATE, ImmutableMap.of(OPERATION, OPERATION_REMOVE_USERS, USERS, usernames));
    }

    @Override
    public void reloadGroupForUser(final UserI user, final String groupId) {
        reloadGroupsForUser(user);
    }

    @Override
    public void reloadGroupsForUser(final UserI user) {
        ((XDATUser) user).refreshGroups();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public UserGroupI createGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String tag, UserI authenticatedUser) {
        return createGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, tag, authenticatedUser, Collections.emptyList());
    }

    @Override
    public UserGroupI createGroup(final String groupId, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String tag, UserI authenticatedUser, final List<UserI> users) {
        final XdatUsergroup group = new XdatUsergroup(authenticatedUser);

        //optimized version for expediency
        final PersistentWorkflowI workflow = getWorkflow(tag, authenticatedUser, group, "ADMIN", "Initialized permissions");
        assert workflow != null;
        final EventMetaI event = workflow.buildEvent();

        try {
            if (exists(groupId)) {
                throw new ResourceAlreadyExistsException(XdatUsergroup.SCHEMA_ELEMENT_NAME, "Group " + groupId + " already exists");
            }

            group.setId(groupId);
            group.setDisplayname(displayName);
            group.setTag(tag);

            SaveItemHelper.authorizedSave(group, authenticatedUser, false, false, event);

            final UserGroupI persisted = new UserGroup(groupId, _template);
            initPermissions(persisted, create, read, delete, edit, activate, ess, tag, authenticatedUser);
            ElementSecurity.updateElementAccessAndFieldMapMetaData();

            final Set<String> added = new HashSet<>();
            if (users != null) {
                for (final UserI user : users) {
                    if (!getGroupsForUser(user).containsKey(groupId)) {
                        addUserToGroup(persisted, user, authenticatedUser, event, false);
                        added.add(user.getUsername());
                    }
                }
            }
            if (users != null) {
                XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, groupId, CREATE, ImmutableMap.of(OPERATION, OPERATION_ADD_USERS, USERS, added));
            } else {
                XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, groupId, CREATE);
            }

            workflow.setId(persisted.getPK().toString());
            PersistentWorkflowUtils.complete(workflow, event);

            try {
                PoolDBUtils.ClearCache(null, authenticatedUser.getUsername(), XdatUsergroup.SCHEMA_ELEMENT_NAME);
            } catch (Exception e) {
                log.error("", e);
            }

            return getGroup(groupId);
        } catch (Exception e) {
            log.error("An error occurred while creating the group " + groupId, e);
            try {
                PersistentWorkflowUtils.fail(workflow, event);
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    @Override
    public UserGroupI createOrUpdateGroup(final String id, final String displayName, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final List<ElementSecurity> elementSecurities, final String value, final UserI authenticatedUser) throws Exception {
        return createOrUpdateGroup(id, displayName, create, read, delete, edit, activate, activateChanges, elementSecurities, value, authenticatedUser, Collections.emptyList());
    }

    @Override
    public UserGroupI createOrUpdateGroup(final String id, final String displayName, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final List<ElementSecurity> elementSecurities, final String tag, final UserI authenticatedUser, final List<UserI> users) throws Exception {
        //hijacking the code her to manually create a group if it is brand new.  Should make project creation much quicker.
        final Long matches = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(ID) FROM xdat_usergroup WHERE ID='" + id + "'", "COUNT", authenticatedUser.getDBName(), null);
        if (matches == 0) {
            //this means the group is new.  It doesn't need to be as thorough as an edit of an existing one would be.
            return createGroup(id, displayName, create, read, delete, edit, activate, activateChanges, elementSecurities, tag, authenticatedUser, users);
        }

        //this means the group previously existed, and this is an update rather than an init.
        //the logic here will be way more intrusive (and expensive)
        //it will end up checking every individual permission setting (using very inefficient code)
        final ArrayList<XdatUsergroup> groups = XdatUsergroup.getXdatUsergroupsByField(XdatUsergroup.SCHEMA_ELEMENT_NAME + ".ID", id, authenticatedUser, true);

        if (groups.isEmpty()) {
            throw new Exception("Count didn't match query results");
        }

        final XdatUsergroup       group    = groups.get(0);
        final PersistentWorkflowI workflow = getWorkflow(tag, authenticatedUser, group, group.getXdatUsergroupId().toString(), "Modified permissions");

        if (workflow == null) {
            return null;
        }

        final long start = Calendar.getInstance().getTimeInMillis();
        try {
            if (group.getDisplayname().equals("Owners")) {
                setPermissions(group, "xnat:projectData", "xnat:projectData/ID", tag, create, read, delete, edit, activate, activateChanges, authenticatedUser, false, workflow.buildEvent());
            } else {
                setPermissions(group, "xnat:projectData", "xnat:projectData/ID", tag, false, read, false, false, false, activateChanges, authenticatedUser, false, workflow.buildEvent());
            }

            for (final ElementSecurity elementSecurity : elementSecurities) {
                setPermissions(group, elementSecurity.getElementName(), elementSecurity.getElementName() + "/project", tag, create, read, delete, edit, activate, activateChanges, authenticatedUser, false, workflow.buildEvent());
                setPermissions(group, elementSecurity.getElementName(), elementSecurity.getElementName() + "/sharing/share/project", tag, false, true, false, false, true, true, authenticatedUser, false, workflow.buildEvent());
            }
        } catch (Exception e) {
            PersistentWorkflowUtils.fail(workflow, workflow.buildEvent());
            log.error("", e);
        }

        try {
            PersistentWorkflowUtils.complete(workflow, workflow.buildEvent());
            try {
                PoolDBUtils.ClearCache(null, authenticatedUser.getUsername(), XdatUsergroup.SCHEMA_ELEMENT_NAME);
            } catch (Exception e) {
                log.error("", e);
            }
            XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), UPDATE);
        } catch (Exception e1) {
            PersistentWorkflowUtils.fail(workflow, workflow.buildEvent());
            log.error("", e1);
        }

        log.debug("Updated group {} in {} ms", id, Calendar.getInstance().getTimeInMillis() - start);
        return new UserGroup(group, _template);
    }

    @Override
    public List<UserGroupI> createOrUpdateProjectGroups(final String projectId, final UserI user) {
        try {
            return _helper.callFunction("project_groups_create_groups_and_permissions", getFunctionParameterSource("projectId", projectId)).stream().map(group -> getGroup((String) group.get("group_id"))).collect(Collectors.toList());
        } finally {
            for (final String dataType : PROJECT_GROUP_DATA_TYPES) {
                DBAction.InsertMetaDatas(dataType);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getProjectGroupPermissions(final String projectId) {
        return _helper.callFunction("project_groups_get_groups_and_permissions", getFunctionParameterSource("projectId", projectId));
    }

    @Override
    public List<Map<String, Object>> findIrregularProjectGroups() {
        return _template.queryForList("SELECT tag, id, xdat_field_mapping_id, field, mismatched_values, mismatched_read_value, mismatched_edit_value, mismatched_create_value, mismatched_delete_value, mismatched_active_value FROM project_groups_find_irregular_settings", EmptySqlParameterSource.INSTANCE);
    }

    @Override
    public List<Integer> fixIrregularProjectGroups() {
        return _helper.getJdbcTemplate().queryForList("SELECT * FROM project_groups_fix_irregular_settings(TRUE)", Integer.class);
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public List<UserGroupI> getGroupsByTag(String tag) throws Exception {
        return ImmutableList.copyOf(_cache.getGroupsForTag(tag));
    }

    @Override
    public UserGroupI addUserToGroup(final String groupId, final UserI newUser, final UserI currentUser, final EventMetaI ci) throws Exception {
        final UserGroupI userGroup = getGroup(groupId);
        addUserToGroup(userGroup, currentUser, newUser, ci, true);
        return userGroup;
    }

    @Override
    public UserGroupI addUsersToGroup(final String groupId, final UserI currentUser, final List<UserI> users, final EventMetaI eventMeta) throws Exception {
        final UserGroupI userGroup = getGroup(groupId);
        for (final UserI user : users) {
            addUserToGroup(userGroup, currentUser, user, eventMeta, true);
        }
        return userGroup;
    }

    @Override
    public List<UserGroupI> getAllGroups() {
        return _template.query(ALL_GROUPS_QUERY, (resultSet, rowNum) -> getCache().get(resultSet.getString("id")));
    }

    @Override
    public String getGroupDatatype() {
        return XdatUsergroup.SCHEMA_ELEMENT_NAME;
    }

    @Override
    public void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception {
        for (UserGroupI g : getGroupsByTag(tag)) {
            deleteGroup(g, user, ci);
        }
    }

    @Override
    public UserGroupI createGroup(Map<String, ?> params) throws GroupFieldMappingException {
        try {
            final ItemI found = new PopulateItem(params, null, XdatUsergroup.SCHEMA_ELEMENT_NAME, true).getItem();
            return new UserGroup(new XdatUsergroup(found), _template);
        } catch (Exception e) {
            throw new GroupFieldMappingException(e);
        }
    }

    @Override
    public void deleteGroup(final UserGroupI group, final UserI user, final EventMetaI ci) {
        final CriteriaCollection criteria = new CriteriaCollection("AND");
        criteria.addClause(XdatUserGroupid.SCHEMA_ELEMENT_NAME + ".groupid", " = ", group.getId());

        for (final XdatUserGroupid gId : XdatUserGroupid.getXdatUserGroupidsByField(criteria, user, false)) {
            try {
                SaveItemHelper.authorizedDelete(gId.getItem(), user, ci);
            } catch (Throwable e) {
                log.error("", e);
            }
        }

        try {
            final XdatUsergroup tmp = XdatUsergroup.getXdatUsergroupsByXdatUsergroupId(group.getPK(), user, false);
            assert tmp != null;
            SaveItemHelper.authorizedDelete(tmp.getItem(), user, ci);
            XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), DELETE);
        } catch (Throwable e) {
            log.error("", e);
        }

        try {
            PoolDBUtils.ClearCache(null, user.getUsername(), XdatUsergroup.SCHEMA_ELEMENT_NAME);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public UserGroupI getGroupByPK(Object gID) {
        try {
            String id = (String) PoolDBUtils.ReturnStatisticQuery(String.format("SELECT id FROM xdat_usergroup WHERE xdat_userGroup_id=%1$s;", gID), "id", null, null);
            if (id != null) {
                return getGroup(id);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    @Override
    public UserGroupI getGroupByTagAndName(final String tag, final String displayName) {
        try {
            String id = (String) PoolDBUtils.ReturnStatisticQuery(String.format("SELECT id FROM xdat_usergroup WHERE tag='%1$s' AND displayname='%2$s';", tag, displayName), "id", null, null);
            if (id != null) {
                return getGroup(id);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    @Override
    public void save(UserGroupI group, UserI user, EventMetaI meta) throws Exception {
        final XdatUsergroup xdatGroup = ((UserGroup) group).getUserGroupImpl();
        if (xdatGroup == null) {
            return;
        }

        if (!Roles.isSiteAdmin(user)) {
            final String                  tag;
            final List<XdatElementAccess> access = xdatGroup.getElementAccess();
            // If there's no element access set up, we'll use the tag: this is a new group and access will be added.
            if (access.isEmpty()) {
                tag = xdatGroup.getTag();
            } else {
                // If access IS set up, extract from there rather than trusting the tag.
                String value = null;
                for (final XdatElementAccess ea : access) {
                    value = getPermissionValues(ea.getPermissions_allowSet().get(0)).get(0);
                    if (StringUtils.isNotBlank(value)) {
                        break;
                    }
                }
                tag = value;
            }

            validateGroupByTag(xdatGroup, tag);

            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            if (!userHelperService.isOwner(tag)) {
                throw new InvalidValueException();
            }
        }

        SaveItemHelper.authorizedSave(xdatGroup, user, false, true, meta);
        XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, xdatGroup.getId(), UPDATE);
        reloadGroupsForUser(user);
    }

    public void validateGroupByTag(XdatUsergroup tempGroup, String tag) throws InvalidValueException {
        //verify that the user isn't trying to gain access to other projects.
        for (XdatElementAccess ea : tempGroup.getElementAccess()) {
            for (XdatFieldMappingSet set : ea.getPermissions_allowSet()) {
                List<String> values = getPermissionValues(set);
                if (values.size() != 1) {
                    throw new InvalidValueException();
                }
                if (!StringUtils.equals(values.get(0), tag)) {
                    throw new InvalidValueException();
                }
            }
        }
    }

    /**
     * Removes the group-to-user mapping. This does <i>not</i> trigger an {@link XftItemEvent}!
     *
     * @param user        The user to remove from the group.
     * @param currentUser The user requesting the deletion operation.
     * @param groupId     The ID of the group from which the user should be removed.
     * @param eventMeta   The workflow event.
     *
     * @throws Exception When an error occurs.
     */
    private void removeUserFromGroup(final XDATUser user, final UserI currentUser, final String groupId, final EventMetaI eventMeta) throws Exception {
        for (final XdatUserGroupid map : user.getGroups_groupid()) {
            if (StringUtils.equals(map.getGroupid(), groupId)) {
                SaveItemHelper.authorizedDelete(map.getItem(), currentUser, eventMeta);
            }
        }
        user.resetCriteria();
    }

    private void addUserToGroup(final UserGroupI userGroup, final UserI currentUser, final UserI user, final EventMetaI eventMeta, final boolean triggerEvent) throws Exception {
        final String       groupId          = userGroup.getId();
        final String       groupTag         = userGroup.getTag();
        final List<String> groupIdsToRemove = new ArrayList<>();

        if (StringUtils.isNotBlank(groupTag)) {
            //remove from existing groups
            for (final UserGroupI existing : getGroupsForUser(user).values()) {
                final String existingId = existing.getId();
                if (StringUtils.equals(existing.getTag(), groupTag)) {
                    if (StringUtils.equals(existingId, groupId)) {
                        return;
                    }
                    if (isMember(user, existingId)) {
                        groupIdsToRemove.add(existingId);
                    }
                }
            }

            //find mapping object to delete
            if (!groupIdsToRemove.isEmpty()) {
                for (final String removedGroupId : groupIdsToRemove) {
                    removeUserFromGroup((XDATUser) user, currentUser, removedGroupId, eventMeta);
                }
            }
        }

        if (!_template.queryForObject(CONFIRM_QUERY, new MapSqlParameterSource("groupId", groupId).addValue("userId", user.getID()), Boolean.class)) {
            final XdatUserGroupid map = new XdatUserGroupid(currentUser);
            map.setProperty(map.getXSIType() + ".groups_groupid_xdat_user_xdat_user_id", user.getID());
            map.setGroupid(groupId);
            SaveItemHelper.authorizedSave(map, currentUser, false, false, eventMeta);
        }

        final Map<String, Object> properties = new HashMap<>();
        properties.put(OPERATION, OPERATION_ADD_USERS);
        properties.put(USERS, Collections.singleton(user.getUsername()));
        if (!groupIdsToRemove.isEmpty()) {
            properties.put(REMOVED, groupIdsToRemove);
        }
        if (triggerEvent) {
            _eventService.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, groupId, UPDATE, properties);
        }
    }

    private PersistentWorkflowI getWorkflow(final String value, final UserI authenticatedUser, final XdatUsergroup group, final String objectId, final String action) {
        try {
            return PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), objectId, value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, action));
        } catch (PersistentWorkflowUtils.JustificationAbsent | PersistentWorkflowUtils.ActionNameAbsent | PersistentWorkflowUtils.IDAbsent exception) {
            log.error("An error occurred trying to create a workflow object for the group '{}' and value '{}'", group.getId(), objectId);
            return null;
        }
    }

    private List<String> getPermissionValues(XdatFieldMappingSet set) {
        List<String> values = Lists.newArrayList();

        for (final XdatFieldMapping map : set.getAllow()) {
            if (!values.contains(map.getFieldValue())) {
                values.add(map.getFieldValue());
            }
        }

        for (XdatFieldMappingSet subset : set.getSubSet()) {
            values.addAll(getPermissionValues(subset));
        }

        return values;
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private boolean setPermissions(final XdatUsergroup usergroup, final String elementName, final String primarySecurityField, final String value, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final UserI user, final boolean includesModification, final EventMetaI eventMeta) {
        try {
            final XdatElementAccess elementAccess = usergroup.getElementAccess().stream().filter(elementAccess1 -> StringUtils.equals(elementName, elementAccess1.getElementName())).findFirst().orElseGet(() -> getNewElementAccess(user, elementName, usergroup.getXdatUsergroupId()));

            final XdatFieldMappingSet        fieldMappingSet = elementAccess.getOrCreateFieldMappingSet(user);
            final Optional<XdatFieldMapping> optional        = fieldMappingSet.getAllow().stream().filter(mapping -> mapping.getFieldValue().equals(value) && mapping.getField().equals(primarySecurityField)).findAny();

            final XdatFieldMapping fieldMapping;
            if (!optional.isPresent()) {
                if (!(create || read || edit || delete || activate)) {
                    return false;
                }
                fieldMapping = new XdatFieldMapping(user);
            } else if (!includesModification) {
                if (create || read || edit || delete || activate) {
                    return false;
                }
                SaveItemHelper.authorizedDelete(fieldMappingSet.getAllow().size() == 1 ? fieldMappingSet.getItem() : optional.get().getItem(), user, eventMeta);
                return true;
            } else {
                fieldMapping = optional.get();
            }

            fieldMapping.init(primarySecurityField, value, create, read, delete, edit, activate);
            fieldMappingSet.setAllow(fieldMapping);

            if (fieldMappingSet.getXdatFieldMappingSetId() != null) {
                fieldMapping.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fieldMappingSet.getXdatFieldMappingSetId());

                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fieldMapping, user, true, false, true, false, eventMeta);
                    fieldMapping.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(fieldMapping, user, true, false, false, false, eventMeta);
                }
            } else if (elementAccess.getXdatElementAccessId() != null) {
                fieldMappingSet.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", elementAccess.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fieldMappingSet, user, true, false, true, false, eventMeta);
                    fieldMappingSet.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(fieldMappingSet, user, true, false, false, false, eventMeta);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(elementAccess, user, true, false, true, false, eventMeta);
                    elementAccess.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(elementAccess, user, true, false, false, false, eventMeta);
                }
                usergroup.setElementAccess(elementAccess);
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return true;
    }

    @Nonnull
    private XdatElementAccess getNewElementAccess(final UserI user, final String elementName, final Integer xdatUsergroupId) {
        try {
            final XdatElementAccess elementAccess = new XdatElementAccess(user);
            elementAccess.setElementName(elementName);
            elementAccess.setProperty("xdat_usergroup_xdat_usergroup_id", xdatUsergroupId);
            return elementAccess;
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException | org.nrg.xft.exception.InvalidValueException e) {
            throw new RuntimeException(e);
        }
    }

    private void initPermissions(final UserGroupI group, final boolean create, final boolean read, final boolean delete, final boolean edit, final boolean activate, final List<ElementSecurity> elementSecurities, final String value, final UserI authenticatedUser) {
        try {
            if (checkVelocityInit() && Velocity.resourceExists("/screens/" + NEW_GROUP_PERMS_TEMPLATE)) {
                final VelocityContext context = new VelocityContext();
                context.put("group", group);
                context.put("create", create ? "1" : "0");
                context.put("read", read ? "1" : "0");
                context.put("delete", delete ? "1" : "0");
                context.put("edit", edit ? "1" : "0");
                context.put("activate", activate ? "1" : "0");
                context.put("elementSecurities", elementSecurities);
                context.put("value", value);
                context.put("user", authenticatedUser);

                final StringWriter writer   = new StringWriter();
                final Template     template = Velocity.getTemplate("/screens/" + NEW_GROUP_PERMS_TEMPLATE);
                template.merge(context, writer);

                final List<String> queries = XftStringUtils.DelimitedStringToArrayList(writer.toString(), ";");
                final File         sql     = new File("/data/xnat/cache/group-perms-" + group.getId() + ".sql");
                try (final FileWriter sqlOutput = new FileWriter(sql)) {
                    IOUtils.copy(new StringReader(StringUtils.join(queries, "\n")), sqlOutput);
                }
                PoolDBUtils.ExecuteBatch(queries, null, authenticatedUser.getUsername());
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean checkVelocityInit() {
        try {
            Velocity.resourceExists(NEW_GROUP_PERMS_TEMPLATE);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static final String QUERY_CHECK_GROUP_EXISTS = "SELECT EXISTS(SELECT TRUE FROM xdat_usergroup WHERE id = :groupId) AS exists";
    private static final String ALL_GROUPS_QUERY         = "SELECT xdat_usergroup_id, id, displayname, tag FROM xdat_usergroup";
    private static final String QUERY_GET_PRIMARY_KEY    = "SELECT xdat_usergroup_id FROM xdat_usergroup WHERE id = :groupId";
    private static final String CONFIRM_QUERY            = "SELECT EXISTS (SELECT 1 FROM xdat_user_groupid WHERE groupid = :groupId AND groups_groupid_xdat_user_xdat_user_id = :userId)";

    private static final String       NEW_GROUP_PERMS_TEMPLATE = "new_group_permissions.vm";
    private static final List<String> PROJECT_GROUP_DATA_TYPES = Arrays.asList(XdatUsergroup.SCHEMA_ELEMENT_NAME, XdatElementAccess.SCHEMA_ELEMENT_NAME, XdatFieldMappingSet.SCHEMA_ELEMENT_NAME, XdatFieldMapping.SCHEMA_ELEMENT_NAME);

    private final GroupsAndPermissionsCache  _cache;
    private final NamedParameterJdbcTemplate _template;
    private final DataTypeAwareEventService  _eventService;
    private final DatabaseHelper             _helper;
}
