/*
 * core: org.nrg.xdat.security.UserGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unused")
@Slf4j
public class UserGroup implements UserGroupI {
    /**
     * Creates a new user group bean from an {@link XdatUsergroup} object.
     *
     * @param group The group for initialization.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public UserGroup(final XdatUsergroup group) throws Exception {
        init(group);
    }

    /**
     * Creates a new user group bean from an {@link XdatUsergroup} object. This uses the JDBC template to retrieve
     * data from related objects from the database where possible. This provides some performance advantages over
     * retrieving related objects through the XFT application framework.
     *
     * @param group    The group for initialization.
     * @param template Template used for direct database queries.
     *
     * @throws Exception When something goes wrong.
     */
    public UserGroup(final XdatUsergroup group, final NamedParameterJdbcTemplate template) throws Exception {
        init(group, template);
    }

    /**
     * Creates a new user group bean based on the {@link XdatUsergroup#getId() group's ID}. Note that this ID is <i>not</i>
     * the primary key in the database, which is the {@link XdatUsergroup#getXdatUsergroupId()} method. This uses the JDBC
     * template to retrieve data from related objects from the database. This provides a performance advantage over retrieving
     * related objects through the XFT application framework.
     *
     * @param id       The ID of the group for initialization.
     * @param template Template used for direct database queries.
     */
    public UserGroup(final String id, final NamedParameterJdbcTemplate template) throws ItemNotFoundException {
        init(id, template);
    }

    @SuppressWarnings("unused")
    public UserGroup(final int xdatUsergroupId, final String id, final String displayName, final String tag) {
        pk = xdatUsergroupId;
        this.id = id;
        this.displayName = displayName;
        this.tag = tag;
    }

    /**
     * Creates an empty user group object.
     */
    public UserGroup() {

    }

    /**
     * Gets the {@link XdatUsergroup#getId() user group ID property}. Note that this is <i>not</i> the primary key in the
     * database for the group. For that, call the {@link #getPK()} method.
     *
     * @return Returns the {@link XdatUsergroup#getId() user group ID property}.
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Integer getPK() {
        return pk;
    }

    /**
     * Gets the {@link XdatUsergroup#getTag()} user group tag property}. This generally corresponds to the project with
     * which the group is associated.
     *
     * @return Returns the {@link XdatUsergroup#getTag() user group tag property}.
     */
    @Override
    public String getTag() {
        return tag;
    }

    /**
     * Gets the {@link XdatUsergroup#getDisplayname() user group display name property}.
     *
     * @return Returns the {@link XdatUsergroup#getDisplayname() user group display name property}.
     */
    @Override
    public String getDisplayname() {
        return displayName;
    }

    @Override
    public List<String> getUsernames() {
        return ImmutableList.copyOf(_usernames);
    }

    /**
     * ArrayList: 0:elementName 1:ArrayList of PermissionItems
     *
     * @return Returns a list of the lists of PermissionItems for each element
     *
     * @throws Exception When an error occurs.
     */
    @Override
    public List<List<Object>> getPermissionItems(final String username) throws Exception {
        if (!_permissionItemsByLogin.containsKey(username)) {
            final Map<String, ElementAccessManager> accessManagers = getAccessManagers();
            if (accessManagers == null) {
                log.warn("The element access managers are not initialized for the group '{}'. This prevents retrieval of permissions for data types. Returning an empty set of permission items.", getId());
                return Collections.emptyList();
            }

            log.debug("No cached permission items found for username '{}' and tag '{}', initializing", username, tag);

            final String tag = getTag();
            for (final ElementSecurity elementSecurity : ElementSecurity.GetSecureElements()) {
                final List<PermissionItem> permissionItems = elementSecurity.getPermissionItems(StringUtils.defaultIfBlank(tag, username));
                boolean                    isAuthenticated = true;
                boolean                    wasSet          = false;
                for (final PermissionItem permissionItem : permissionItems) {
                    final ElementAccessManager accessManager = accessManagers.get(elementSecurity.getElementName());
                    if (accessManager != null) {
                        final PermissionCriteriaI criteria = accessManager.getMatchingPermissions(permissionItem.getFullFieldName(), permissionItem.getValue());
                        if (criteria != null) {
                            permissionItem.set(criteria);
                        }
                    }
                    if (!permissionItem.isAuthenticated()) {
                        isAuthenticated = false;
                    }
                    if (permissionItem.wasSet()) {
                        wasSet = true;
                    }
                }

                final List<Object> elementManager = new ArrayList<>();
                elementManager.add(elementSecurity.getElementName());
                elementManager.add(permissionItems);
                elementManager.add(elementSecurity.getSchemaElement().getSQLName());
                elementManager.add((isAuthenticated) ? Boolean.TRUE : Boolean.FALSE);
                elementManager.add((wasSet) ? Boolean.TRUE : Boolean.FALSE);
                elementManager.add(elementSecurity);

                if (!permissionItems.isEmpty()) {
                    _permissionItemsByLogin.put(username, elementManager);
                    log.debug("Added {} permission items for the user '{}' for data type '{}'", permissionItems.size(), username, elementSecurity.getElementName());
                } else {
                    log.debug("No permission items found for the user '{}' and data type '{}'", username, elementSecurity.getElementName());
                }
            }
            log.debug("Added permission items for {} data types for the user '{}'", _permissionItemsByLogin.get(username).size(), username);
        }

        return ImmutableList.copyOf(_permissionItemsByLogin.get(username));
    }

    @Override
    public List<PermissionCriteriaI> getPermissionsByDataType(final String type) {
        if (!_permissionCriteriaByDataType.containsKey(type)) {
            _permissionCriteriaByDataType.clear();
            _permissionCriteriaByDataTypeAndField.clear();
            getAllPermissions();
        }

        return ImmutableList.copyOf(_permissionCriteriaByDataType.get(type));
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setPK(Integer pk) {
        this.pk = pk;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public void setDisplayname(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getId()).append("\n");
        sb.append(getTag()).append("\n");

        for (final ElementAccessManager eam : getAccessManagers().values()) {
            sb.append(eam.toString()).append("\n");
        }

        return sb.toString();
    }

    public List<String> getFeatures() {
        return ImmutableList.copyOf(_features);
    }

    public boolean hasFeature(final String feature) {
        return _features.contains(feature);
    }

    public void addFeature(final String feature) {
        _features.add(feature);
    }

    public void addFeatures(final String... features) {
        addFeatures(Arrays.asList(features));
    }

    public void addFeatures(final Collection<String> features) {
        _features.addAll(features);
    }

    public void removeFeature(final String feature) {
        _features.remove(feature);
    }

    public void removeFeatures(final String... features) {
        removeFeatures(Arrays.asList(features));
    }

    public void removeFeatures(final Collection<String> features) {
        _features.removeAll(features);
    }

    public void clearFeatures() {
        _features.clear();
    }

    public List<String> getBlockedFeatures() {
        return ImmutableList.copyOf(_blocked);
    }

    public boolean hasBlockedFeature(final String blockedFeature) {
        return _blocked.contains(blockedFeature);
    }

    public void addBlockedFeature(final String blockedFeature) {
        _blocked.add(blockedFeature);
    }

    public void addBlockedFeatures(final String... blockedFeatures) {
        addBlockedFeatures(Arrays.asList(blockedFeatures));
    }

    public void addBlockedFeatures(final Collection<String> blockedFeatures) {
        _blocked.addAll(blockedFeatures);
    }

    public void removeBlockedFeature(final String blockedFeature) {
        _blocked.remove(blockedFeature);
    }

    public void removeBlockedFeatures(final String... blockedFeatures) {
        removeBlockedFeatures(Arrays.asList(blockedFeatures));
    }

    public void removeBlockedFeatures(final Collection<String> blockedFeatures) {
        _blocked.removeAll(blockedFeatures);
    }

    public void clearBlockedFeatures() {
        _blocked.clear();
    }

    /**
     * Gets the {@link XdatUsergroup} instance with which this object is associated.
     *
     * @return The associated {@link XdatUsergroup}.
     */
    public XdatUsergroup getUserGroupImpl() {
        return ObjectUtils.defaultIfNull(xdatGroup, getSavedUserGroupImpl(Users.getAdminUser()));
    }

    public void refresh(final NamedParameterJdbcTemplate template) {
        initializeUsernames(template);
        initializeAccessManagers(template);
        initializeFeatures();
    }

    @SuppressWarnings("unused")
    public void initializeUsernames(final ItemI item) {
        log.warn("Can't currently get the list of usernames from an ItemI");
    }

    public void initializeUsernames(final NamedParameterJdbcTemplate template) {
        _usernames.clear();
        _usernames.addAll(template.queryForList(QUERY_GET_USERS_FOR_GROUP, new MapSqlParameterSource("groupId", id), String.class));
    }

    public void initializeAccessManagers(final ItemI item) throws Exception {
        _accessManagers.clear();
        final List<XFTItem> childItems = item.getChildItems("xdat:userGroup.element_access");
        if (childItems != null) {
            for (final ItemI childItem : childItems) {
                final ElementAccessManager elementAccessManager = new ElementAccessManager(childItem);
                _accessManagers.put(elementAccessManager.getElement(), elementAccessManager);
            }
        }
    }

    public void initializeAccessManagers(final NamedParameterJdbcTemplate template) {
        _accessManagers.clear();
        final Map<String, ElementAccessManager> accessManagers = ElementAccessManager.initialize(template, QUERY_ELEMENT_ACCESS, new MapSqlParameterSource("xdatUsergroupId", pk));
        _accessManagers.putAll(accessManagers);
    }

    public void initializeFeatures() {
        clearFeatures();
        clearBlockedFeatures();
        final List<GroupFeature> features = getGroupFeatureService().findFeaturesForGroup(getId());
        if (features != null) {
            for (final GroupFeature feature : features) {
                if (feature.isBlocked()) {
                    addFeature(feature.getFeature());
                } else if (feature.isOnByDefault()) {
                    addFeature(feature.getFeature());
                }
            }
        }
    }

    public void addPermission(final String elementName, final PermissionCriteriaI pc, final UserI authenticatedUser) throws Exception {
        final XdatUsergroup xdatGroup = getSavedUserGroupImpl(authenticatedUser);
        final Optional<XdatElementAccess> accessCandidate = Iterables.tryFind(xdatGroup.getElementAccess(), new Predicate<XdatElementAccess>() {
            @Override
            public boolean apply(@Nullable final XdatElementAccess access) {
                return access != null && access.getElementName().equals(elementName);
            }
        });

        final XdatElementAccess xea;
        if (accessCandidate.isPresent()) {
            xea = accessCandidate.get();
        } else {
            xea = new XdatElementAccess(authenticatedUser);
            xea.setElementName(elementName);
            xdatGroup.setElementAccess(xea);
        }

        final XdatFieldMappingSet       fieldMappingSet;
        final List<XdatFieldMappingSet> set = xea.getPermissions_allowSet();
        if (set.isEmpty()) {
            fieldMappingSet = new XdatFieldMappingSet(authenticatedUser);
            fieldMappingSet.setMethod("OR");
            xea.setPermissions_allowSet(fieldMappingSet);
        } else {
            fieldMappingSet = set.get(0);
        }

        final XdatFieldMapping xfm;
        final Optional<XdatFieldMapping> mappingCandidate = Iterables.tryFind(fieldMappingSet.getAllow(), new Predicate<XdatFieldMapping>() {
            @Override
            public boolean apply(@Nullable final XdatFieldMapping mapping) {
                return mapping != null && mapping.getField().equals(pc.getField()) && mapping.getFieldValue().equals(pc.getFieldValue());
            }
        });

        if (mappingCandidate.isPresent()) {
            xfm = mappingCandidate.get();
        } else {
            xfm = new XdatFieldMapping(authenticatedUser);
            xfm.setField(pc.getField());
            xfm.setFieldValue((String) pc.getFieldValue());
            fieldMappingSet.setAllow(xfm);
        }

        xfm.setCreateElement(pc.getCreate());
        xfm.setReadElement(pc.getRead());
        xfm.setEditElement(pc.getEdit());
        xfm.setDeleteElement(pc.getDelete());
        xfm.setActiveElement(pc.getActivate());
        xfm.setComparisonType("equals");
    }

    public Map<String, List<PermissionCriteriaI>> getAllPermissions() {
        if (_permissionCriteriaByDataType.isEmpty()) {
            final Collection<ElementAccessManager> values = getAccessManagers().values();
            for (final ElementAccessManager manager : values) {
                final List<PermissionSetI> permissionSets = manager.getPermissionSets();
                for (final PermissionSetI permissions : permissionSets) {
                    if (permissions.isActive()) {
                        final List<PermissionCriteriaI> allCriteria = permissions.getAllCriteria();
//                        final String formatted = StringUtils.join(Lists.transform(allCriteria, new Function<PermissionCriteriaI, String>() {
//                            @Override
//                            public String apply(final PermissionCriteriaI criteria) {
//                                return StringUtils.join(Arrays.asList("Field: " + criteria.getField(),
//                                                                      "Field value: " + criteria.getFieldValue(),
//                                                                      "Read: " + criteria.getRead(),
//                                                                      "Activate: " + criteria.getActivate(),
//                                                                      "Edit: " + criteria.getEdit(),
//                                                                      "Create: " + criteria.getCreate(),
//                                                                      "Delete: " + criteria.getDelete()), ", ");
//                            }
//                        }), "\n");
//                        ADHOC.debug("Group {}: found active permission set with {} criteria\n{}", getId(), allCriteria.size(), formatted);
                        addPermissionCriteria(allCriteria);
                    }
                }
            }
        }

        //noinspection UnstableApiUsage
        return Maps.transformValues(Multimaps.asMap(_permissionCriteriaByDataType), new Function<Collection<PermissionCriteriaI>, List<PermissionCriteriaI>>() {
            @Override
            public List<PermissionCriteriaI> apply(final Collection<PermissionCriteriaI> input) {
                return new ArrayList<>(input);
            }
        });
    }

    @SuppressWarnings("unused")
    public List<PermissionCriteriaI> getPermissionsByDataTypeAndField(String dataType, String field) {
        final String compositeKey = formatTypeAndField(dataType, field);
        if (!_permissionCriteriaByDataTypeAndField.containsKey(compositeKey)) {
            getPermissionsByDataType(dataType);
        }

        return _permissionCriteriaByDataTypeAndField.containsKey(compositeKey) ? ImmutableList.copyOf(_permissionCriteriaByDataTypeAndField.get(compositeKey)) : Collections.<PermissionCriteriaI>emptyList();
    }

    /**
     * Adds the list of criteria to the group's permission sets.
     *
     * <b>Note:</b> The addition of the criteria here is completely transitive and is not persisted to the database. This can be used
     * to populate a new instance of the group or to update a cached instance to reflect changes in permissions elsewhere.
     *
     * @param criteria The criteria to add to the group's permission sets.
     */
    public void addPermissionCriteria(final List<PermissionCriteriaI> criteria) {
        for (final PermissionCriteriaI criterion : criteria) {
            addPermissionCriteria(criterion);
        }
    }

    /**
     * Adds the criterion entry to the group's permission sets.
     *
     * <b>Note:</b> The addition of the criterion entry here is completely transitive and is not persisted to the database. This can
     * be used to populate a new instance of the group or to update a cached instance to reflect changes in permissions elsewhere.
     *
     * @param criterion The criterion to add to the group's permission sets.
     */
    public void addPermissionCriteria(final PermissionCriteriaI criterion) {
        final String elementName = criterion.getElementName();
        _permissionCriteriaByDataType.put(elementName, criterion);
        _permissionCriteriaByDataTypeAndField.put(formatTypeAndField(elementName, criterion.getField()), criterion);
        if (log.isTraceEnabled()) {
            log.trace("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", getTag(), getId(), elementName, criterion.getField(), criterion.getFieldValue(), criterion.getRead(), criterion.getActivate(), criterion.getEdit(), criterion.getCreate(), criterion.getDelete());
        }
    }

    /**
     * @return Returns a list of stored search objects
     */
    protected List<XdatStoredSearch> getStoredSearches() {
        if (_storedSearches.isEmpty()) {
            synchronized (_storedSearches) {
                try {
                    for (final XdatStoredSearch search : XdatStoredSearch.GetPreLoadedSearchesByAllowedGroup(id)) {
                        _storedSearches.put(search.getId(), search);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        return ImmutableList.copyOf(_storedSearches.values());
    }

    /**
     * Retrieves the stored search associated with the submitted ID.
     *
     * @param id The ID of the stored search to retrieve.
     *
     * @return Returns the stored search for the given ID
     */
    protected XdatStoredSearch getStoredSearch(String id) {
        if (_storedSearches.isEmpty()) {
            getStoredSearches();
        }
        return _storedSearches.get(id);
    }

    protected void replacePreLoadedSearch(final XdatStoredSearch search) {
        try {
            final String id = search.getStringProperty("ID");
            _storedSearches.put(id, search);
        } catch (ElementNotFoundException | FieldNotFoundException e) {
            log.error("", e);
        }
    }

    protected synchronized Map<String, ElementAccessManager> getAccessManagers() {
        if (_accessManagers.isEmpty()) {
            try {
                init(getUserGroupImpl());
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return _accessManagers;
    }

    private synchronized void init(final XdatUsergroup group) throws Exception {
        init(group, XDAT.getContextService().getBeanSafely(NamedParameterJdbcTemplate.class));
    }

    private synchronized void init(final XdatUsergroup group, final NamedParameterJdbcTemplate template) throws Exception {
        id = group.getId();
        pk = group.getXdatUsergroupId();
        displayName = group.getDisplayname();

        final ItemI item = group.getItem();

        if (item == null) {
            log.info("Tried to init() a user group that doesn't have an item set yet. This might mean it's being created.");
            return;
        }

        tag = item.getStringProperty("tag");

        if (template == null) {
            initializeUsernames(item);
            initializeAccessManagers(item);
        } else {
            initializeUsernames(template);
            initializeAccessManagers(template);
        }

        initializeFeatures();
    }

    private synchronized void init(final String id, final NamedParameterJdbcTemplate template) throws ItemNotFoundException {
        try {
            final Map<String, Object> parameters = template.queryForMap(QUERY_GROUP_ATTRIBUTES, new MapSqlParameterSource("id", id));

            this.id = id;
            pk = (Integer) parameters.get("xdat_usergroup_id");
            displayName = (String) parameters.get("displayname");
            tag = (String) parameters.get("tag");

            refresh(template);
        } catch (EmptyResultDataAccessException e) {
            log.info("Someone tried to retrieve a group with the ID {}, but that doesn't seem to exist.", id);
            throw new ItemNotFoundException(XdatUsergroup.SCHEMA_ELEMENT_NAME, id);
        }
    }

    private XdatUsergroup getSavedUserGroupImpl(final UserI user) {
        if (xdatGroup == null) {
            xdatGroup = ObjectUtils.defaultIfNull(XdatUsergroup.getXdatUsergroupsById(id, user, true), buildNewUserGroupImpl(user));
        }
        return xdatGroup;
    }

    private XdatUsergroup buildNewUserGroupImpl(final UserI user) {
        final XdatUsergroup group = new XdatUsergroup(user);
        group.setId(getId());
        group.setTag(getTag());
        group.setDisplayname(getDisplayname());
        return group;
    }

    private GroupFeatureService getGroupFeatureService() {
        if (_groupFeatureService == null) {
            _groupFeatureService = XDAT.getContextService().getBean(GroupFeatureService.class);
        }
        return _groupFeatureService;
    }

    private static String formatTypeAndField(final String type, final String field) {
        return type + "/" + field;
    }

    private static final String QUERY_GROUP_ATTRIBUTES    = "SELECT xdat_usergroup_id, displayname, tag FROM xdat_usergroup WHERE id = :id";
    private static final String QUERY_GET_USERS_FOR_GROUP = "SELECT DISTINCT " +
                                                            "  login " +
                                                            "FROM xdat_user u " +
                                                            "  LEFT JOIN xdat_user_groupid xug ON u.xdat_user_id = xug.groups_groupid_xdat_user_xdat_user_id " +
                                                            "  LEFT JOIN xdat_usergroup usergroup ON xug.groupid = usergroup.id " +
                                                            "WHERE usergroup.id = :groupId";
    private static final String QUERY_ELEMENT_ACCESS      = "SELECT " +
                                                            "  xea.element_name    AS element_name, " +
                                                            "  xeamd.status        AS active_status, " +
                                                            "  xfms.method         AS method, " +
                                                            "  xfm.field           AS field, " +
                                                            "  xfm.field_value     AS field_value, " +
                                                            "  xfm.comparison_type AS comparison_type, " +
                                                            "  xfm.read_element    AS read_element, " +
                                                            "  xfm.edit_element    AS edit_element, " +
                                                            "  xfm.create_element  AS create_element, " +
                                                            "  xfm.delete_element  AS delete_element, " +
                                                            "  xfm.active_element  AS active_element " +
                                                            "FROM xdat_usergroup usergroup " +
                                                            "  LEFT JOIN xdat_element_access xea ON usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                            "  LEFT JOIN xdat_element_access_meta_data xeamd ON xea.element_access_info = xeamd.meta_data_id " +
                                                            "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                            "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                            "WHERE " +
                                                            "  usergroup.xdat_usergroup_id = :xdatUsergroupId " +
                                                            "ORDER BY element_name, field";

    private              String              id          = null;
    private              Integer             pk          = null;
    private              String              tag         = null;
    private              String              displayName = null;
    private              XdatUsergroup       xdatGroup   = null;
    private              GroupFeatureService _groupFeatureService;

    private final Map<String, ElementAccessManager>     _accessManagers                       = new HashMap<>();
    private final Multimap<String, PermissionCriteriaI> _permissionCriteriaByDataType         = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, PermissionCriteriaI>create());
    private final Multimap<String, PermissionCriteriaI> _permissionCriteriaByDataTypeAndField = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, PermissionCriteriaI>create());
    private final Multimap<String, List<Object>>        _permissionItemsByLogin               = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, List<Object>>create());
    private final Map<String, XdatStoredSearch>         _storedSearches                       = new HashMap<>();
    private final Set<String>                           _usernames                            = new HashSet<>();
    private final Set<String>                           _features                             = new HashSet<>();
    private final Set<String>                           _blocked                              = new HashSet<>();
}
