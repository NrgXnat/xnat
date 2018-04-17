/*
 * core: org.nrg.xdat.security.UserGroupManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static lombok.AccessLevel.PRIVATE;

@Service
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class UserGroupManager implements UserGroupServiceI {
    @Autowired
    public UserGroupManager(final GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        _cache = cache;
        _template = template;
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
    public Map<String, UserGroupI> getGroupsForUser(final UserI user) {
        try {
            return getCache().getGroupsForUser(user.getUsername());
        } catch (UserNotFoundException ignored) {
            // We have the UserI object so we don't have to worry about user not found
            return Collections.emptyMap();
        }
    }

    @Override
    public UserGroupI getGroupForUserAndTag(final UserI user, final String tag) {
        try {
            return getCache().getGroupForUserAndTag(user.getUsername(), tag);
        } catch (UserNotFoundException ignored) {
            // We have the UserI object so we don't have to worry about user not found
            return null;
        }
    }

    @Override
    public List<String> getGroupIdsForUser(final UserI user) {
        return Lists.transform(((XDATUser) user).getGroups_groupid(), new Function<XdatUserGroupid, String>() {
            @Override
            public String apply(final XdatUserGroupid userGroupid) {
                return userGroupid.getGroupid();
            }
        });
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
    public void removeUserFromGroup(UserI user, UserI currentUser, String groupId, EventMetaI ci) throws Exception {
        for (XdatUserGroupid map : ((XDATUser) user).getGroups_groupid()) {
            if (StringUtils.equals(map.getGroupid(), groupId)) {
                SaveItemHelper.authorizedDelete(map.getItem(), currentUser, ci);
            }
        }
        ((XDATUser) user).resetCriteria();
    }

    @Override
    public void removeUsersFromGroup(final String groupId, final UserI currentUser, final List<UserI> users, final EventMetaI eventMeta) throws Exception {
        for (final UserI user : users) {
            for (final XdatUserGroupid map : ((XDATUser) user).getGroups_groupid()) {
                if (StringUtils.equals(map.getGroupid(), groupId)) {
                    SaveItemHelper.authorizedDelete(map.getItem(), currentUser, eventMeta);
                }
            }
            ((XDATUser) user).resetCriteria();
        }
    }

    @Override
    public void reloadGroupForUser(final UserI user, final String groupId) {
        ((XDATUser) user).refreshGroup(groupId);
    }

    @Override
    public void reloadGroupsForUser(final UserI user) {
        ((XDATUser) user).getGroups();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public UserGroupI createGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String value, UserI authenticatedUser) {
        XdatUsergroup       group;
        PersistentWorkflowI wrk = null;

        try {
            group = new XdatUsergroup(authenticatedUser);
            group.setId(id);
            group.setDisplayname(displayName);
            group.setTag(value);

            if (_template.queryForObject(QUERY_CHECK_GROUP_EXISTS, new MapSqlParameterSource("groupId", id), Boolean.class)) {
                throw new Exception("Group already exists");
            }
            //optimized version for expediency
            wrk = PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), "ADMIN", value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, "Initialized permissions"));
            assert wrk != null;

            SaveItemHelper.authorizedSave(group, authenticatedUser, false, false, wrk.buildEvent());

            final UserGroupI persisted = new UserGroup(_template.queryForObject(QUERY_GET_PRIMARY_KEY, new MapSqlParameterSource("groupId", id), Integer.class), id, displayName, value);

            initPermissions(persisted, create, read, delete, edit, activate, ess, value, authenticatedUser);

            wrk.setId(persisted.getPK().toString());

            PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());

            ElementSecurity.updateElementAccessAndFieldMapMetaData();

            XDAT.triggerEvent(XftItemEvent.build(Groups.getGroupDatatype(), id, XftItemEvent.UPDATE));

            try {
                PoolDBUtils.ClearCache(null, authenticatedUser.getUsername(), Groups.getGroupDatatype());
            } catch (Exception e) {
                log.error("", e);
            }

            return Groups.getGroup(id);
        } catch (Exception e) {
            log.error("", e);
            try {
                if (wrk != null) {
                    PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    @Override
    public UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String value, UserI authenticatedUser) throws Exception {
        //hijacking the code her to manually create a group if it is brand new.  Should make project creation much quicker.
        Long matches = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(ID) FROM xdat_usergroup WHERE ID='" + id + "'", "COUNT", authenticatedUser.getDBName(), null);
        if (matches == 0) {
            //this means the group is new.  It doesn't need to be as thorough as an edit of an existing one would be.
            return createGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, value, authenticatedUser);
        }

        PersistentWorkflowI wrk;
        XdatUsergroup       group;

        //this means the group previously existed, and this is an update rather than an init.
        //the logic here will be way more intrusive (and expensive)
        //it will end up checking every individual permission setting (using very inefficient code)
        final ArrayList<XdatUsergroup> groups   = XdatUsergroup.getXdatUsergroupsByField(XdatUsergroup.SCHEMA_ELEMENT_NAME + ".ID", id, authenticatedUser, true);
        boolean                        modified = false;

        if (groups.size() == 0) {
            throw new Exception("Count didn't match query results");
        } else {
            group = groups.get(0);
            wrk = PersistentWorkflowUtils.buildOpenWorkflow(authenticatedUser, group.getXSIType(), group.getXdatUsergroupId().toString(), value, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.TYPE.PROCESS, "Modified permissions"));
        }

        long start = Calendar.getInstance().getTimeInMillis();
        try {
            if (group.getDisplayname().equals("Owners")) {
                assert wrk != null;
                setPermissions(group, "xnat:projectData", "xnat:projectData/ID", value, create, read, delete, edit, activate, activateChanges, authenticatedUser, false, wrk.buildEvent());
            } else {
                assert wrk != null;
                setPermissions(group, "xnat:projectData", "xnat:projectData/ID", value, Boolean.FALSE, read, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, activateChanges, authenticatedUser, false, wrk.buildEvent());
            }

            for (final ElementSecurity es : ess) {
                if (setPermissions(group, es.getElementName(), es.getElementName() + "/project", value, create, read, delete, edit, activate, activateChanges, authenticatedUser, false, wrk.buildEvent())) {
                    modified = true;
                }

                if (setPermissions(group, es.getElementName(), es.getElementName() + "/sharing/share/project", value, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, authenticatedUser, false, wrk.buildEvent())) {
                    modified = true;
                }
            }
        } catch (Exception e) {
            if (wrk != null) {
                PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            }
            log.error("", e);
        }


        try {
            if (modified) {
                PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
                XDAT.triggerEvent(XftItemEvent.build(Groups.getGroupDatatype(), group.getId(), XftItemEvent.UPDATE));

                try {
                    PoolDBUtils.ClearCache(null, authenticatedUser.getUsername(), Groups.getGroupDatatype());
                } catch (Exception e) {
                    log.error("", e);
                }

            }
        } catch (Exception e1) {
            PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            log.error("", e1);
        }

        log.debug("UPDATE GROUP " + id + "... " + (Calendar.getInstance().getTimeInMillis() - start) + " ms");

        return new UserGroup(group);
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public List<UserGroupI> getGroupsByTag(String tag) throws Exception {
        return ImmutableList.copyOf(_cache.getGroupsForTag(tag));
    }

    @Override
    public UserGroupI addUserToGroup(final String groupId, final UserI newUser, final UserI currentUser, final EventMetaI ci) throws Exception {
        final UserGroupI userGroup = Groups.getGroup(groupId);
        addUserToGroup(userGroup, currentUser, newUser, ci);
        return userGroup;
    }

    @Override
    public UserGroupI addUsersToGroup(final String groupId, final UserI currentUser, final List<UserI> users, final EventMetaI eventMeta) throws Exception {
        final UserGroupI userGroup = Groups.getGroup(groupId);
        for (final UserI user : users) {
            addUserToGroup(userGroup, currentUser, user, eventMeta);
        }
        return userGroup;
    }

    @Override
    public List<UserGroupI> getAllGroups() {
        return _template.query(ALL_GROUPS_QUERY, new RowMapper<UserGroupI>() {
            @Override
            public UserGroupI mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
                final UserGroup group = new UserGroup();
                group.setPK(resultSet.getInt("xdat_usergroup_id"));
                group.setId(resultSet.getString("id"));
                group.setDisplayname(resultSet.getString("displayname"));
                group.setTag(resultSet.getString("tag"));
                return group;
            }
        });
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
            final PopulateItem populator = new PopulateItem(params, null, XdatUsergroup.SCHEMA_ELEMENT_NAME, true);
            final ItemI        found     = populator.getItem();
            return new UserGroup(new XdatUsergroup(found));
        } catch (Exception e) {
            throw new GroupFieldMappingException(e);
        }
    }

    @Override
    public void deleteGroup(UserGroupI group, UserI user, EventMetaI ci) {
        //DELETE user.groupId
        CriteriaCollection col = new CriteriaCollection("AND");
        col.addClause(XdatUserGroupid.SCHEMA_ELEMENT_NAME + ".groupid", " = ", group.getId());

        for (final XdatUserGroupid gId : XdatUserGroupid.getXdatUserGroupidsByField(col, user, false)) {
            try {
                SaveItemHelper.authorizedDelete(gId.getItem(), user, ci);
            } catch (Throwable e) {
                log.error("", e);
            }
        }

        try {
            XdatUsergroup tmp = XdatUsergroup.getXdatUsergroupsByXdatUsergroupId(group.getPK(), user, false);

            assert tmp != null;
            SaveItemHelper.authorizedDelete(tmp.getItem(), user, ci);
        } catch (Throwable e) {
            log.error("", e);
        }

        XDAT.triggerEvent(XftItemEvent.build(Groups.getGroupDatatype(), group.getId(), XftItemEvent.DELETE));

        try {
            PoolDBUtils.ClearCache(null, user.getUsername(), Groups.getGroupDatatype());
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
        if (((UserGroup) group).getUserGroupImpl() == null) {
            return;
        }

        XdatUsergroup xdatGroup = ((UserGroup) group).getUserGroupImpl();

        if (!Roles.isSiteAdmin(user)) {
            String firstValue = null;
            for (XdatElementAccess ea : xdatGroup.getElementAccess()) {
                List<String> values = getPermissionValues(ea.getPermissions_allowSet().get(0));
                firstValue = values.get(0);
                if (firstValue != null) {
                    break;
                }
            }

            validateGroupByTag(xdatGroup, firstValue);

            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            if (!userHelperService.isOwner(firstValue)) {
                throw new InvalidValueException();
            }
        }

        SaveItemHelper.authorizedSave(xdatGroup, user, false, true, meta);
        XDAT.triggerEvent(XftItemEvent.build(XdatUsergroup.SCHEMA_ELEMENT_NAME, xdatGroup.getId(), XftItemEvent.UPDATE));
        Groups.reloadGroupsForUser(user);
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

    private void addUserToGroup(final UserGroupI userGroup, final UserI currentUser, final UserI user, final EventMetaI eventMeta) throws Exception {
        final String groupId = userGroup.getId();
        if (userGroup.getTag() != null) {
            //remove from existing groups
            final List<String> groupIdsToRemove = new ArrayList<>();
            for (Map.Entry<String, UserGroupI> entry : Groups.getGroupsForUser(user).entrySet()) {
                if (entry.getValue().getTag() != null && entry.getValue().getTag().equals(userGroup.getTag())) {
                    final String userGroupId = entry.getValue().getId();
                    if (userGroupId.equals(groupId)) {
                        return;
                    }

                    //find mapping object to delete
                    if (Groups.isMember(user, userGroupId)) {
                        groupIdsToRemove.add(userGroupId);
                    }
                }
            }
            if (groupIdsToRemove.size() > 0) {
                for (final String removedGroupId : groupIdsToRemove) {
                    Groups.removeUserFromGroup(user, currentUser, removedGroupId, eventMeta);
                }
            }
        }

        if (!_template.queryForObject(CONFIRM_QUERY, new MapSqlParameterSource("groupId", groupId).addValue("userId", user.getID()), Boolean.class)) {
            final XdatUserGroupid map = new XdatUserGroupid(currentUser);
            map.setProperty(map.getXSIType() + ".groups_groupid_xdat_user_xdat_user_id", user.getID());
            map.setGroupid(groupId);
            SaveItemHelper.authorizedSave(map, currentUser, false, false, eventMeta);
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

    @SuppressWarnings("SameParameterValue")
    private boolean setPermissions(XdatUsergroup impl, String elementName, String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, UserI user, boolean includesModification, EventMetaI c) {
        try {

            XdatElementAccess ea = null;
            for (XdatElementAccess temp : impl.getElementAccess()) {
                if (temp.getElementName().equals(elementName)) {
                    ea = temp;
                    break;
                }
            }

            if (ea == null) {
                ea = new XdatElementAccess(user);
                ea.setElementName(elementName);
                ea.setProperty("xdat_usergroup_xdat_usergroup_id", impl.getXdatUsergroupId());
            }

            final XdatFieldMappingSet fms = ea.getOrCreateFieldMappingSet(user);

            XdatFieldMapping fm = null;

            for (final XdatFieldMapping mapping : fms.getAllow()) {
                if (mapping.getFieldValue().equals(value) && mapping.getField().equals(psf)) {
                    fm = mapping;
                    break;
                }
            }

            if (fm == null) {
                if (create || read || edit || delete || activate) {
                    fm = new XdatFieldMapping(user);
                } else {
                    return false;
                }
            } else if (!includesModification) {
                if (!(create || read || edit || delete || activate)) {
                    if (fms.getAllow().size() == 1) {
                        SaveItemHelper.authorizedDelete(fms.getItem(), user, c);
                        return true;
                    } else {
                        SaveItemHelper.authorizedDelete(fm.getItem(), user, c);
                        return true;
                    }
                }
                return false;
            }

            fm.init(psf, value, create, read, delete, edit, activate);

            fms.setAllow(fm);

            if (fms.getXdatFieldMappingSetId() != null) {
                fm.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fms.getXdatFieldMappingSetId());

                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fm, user, true, false, true, false, c);
                    fm.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(fm, user, true, false, false, false, c);
                }
            } else if (ea.getXdatElementAccessId() != null) {
                fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fms, user, true, false, true, false, c);
                    fms.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(fms, user, true, false, false, false, c);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(ea, user, true, false, true, false, c);
                    ea.activate(user);
                } else {
                    SaveItemHelper.authorizedSave(ea, user, true, false, false, false, c);
                }
                impl.setElementAccess(ea);
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return true;
    }

    private void initPermissions(UserGroupI group, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, List<ElementSecurity> ess, String value, UserI authenticatedUser) {
        String templateName = "new_group_permissions.vm";
        try {
            boolean velocityInit = false;

            try {
                Velocity.resourceExists(templateName);
                velocityInit = true;
            } catch (Exception ignored) {
            }

            if (velocityInit) {
                boolean exists = Velocity.resourceExists("/screens/" + templateName);
                if (exists) {
                    VelocityContext context = new VelocityContext();
                    context.put("group", group);
                    context.put("create", (create) ? "1" : "0");
                    context.put("read", (read) ? "1" : "0");
                    context.put("delete", (delete) ? "1" : "0");
                    context.put("edit", (edit) ? "1" : "0");
                    context.put("activate", (activate) ? "1" : "0");
                    context.put("ess", ess);
                    context.put("value", value);
                    context.put("user", authenticatedUser);
                    StringWriter sw       = new StringWriter();
                    Template     template = Velocity.getTemplate("/screens/" + templateName);
                    template.merge(context, sw);

                    PoolDBUtils.ExecuteBatch(XftStringUtils.DelimitedStringToArrayList(sw.toString(), ";"), null, authenticatedUser.getUsername());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static final String QUERY_CHECK_GROUP_EXISTS = "SELECT EXISTS(SELECT TRUE FROM xdat_usergroup WHERE id = :groupId) AS exists";
    private static final String ALL_GROUPS_QUERY         = "SELECT xdat_usergroup_id, id, displayname, tag FROM xdat_usergroup";
    private static final String QUERY_GET_PRIMARY_KEY    = "SELECT xdat_usergroup_id FROM xdat_usergroup WHERE id = :groupId";
    private static final String CONFIRM_QUERY            = "SELECT EXISTS (SELECT 1 FROM xdat_user_groupid WHERE groupid = :groupId AND groups_groupid_xdat_user_xdat_user_id = :userId)";

    private final GroupsAndPermissionsCache  _cache;
    private final NamedParameterJdbcTemplate _template;
}
