/*
 * core: org.nrg.xdat.security.PermissionsServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.Reflection;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.nrg.xdat.security.PermissionCriteria.dumpCriteriaList;
import static org.nrg.xdat.security.SecurityManager.*;
import static org.nrg.xft.event.XftItemEvent.builder;
import static org.nrg.xft.event.XftItemEventI.UPDATE;

@SuppressWarnings({"unused", "DuplicateThrows"})
@Service
@Slf4j
public class PermissionsServiceImpl implements PermissionsServiceI {

    @Autowired
    public PermissionsServiceImpl(final DataTypeAwareEventService eventService, final NamedParameterJdbcTemplate template) {
        _eventService = eventService;
        _template = template;
    }

    @Autowired
    public void setGroupsAndPermissionsCache(final GroupsAndPermissionsCache cache) {
        _cache = cache;
    }

    @Override
    public List<PermissionCriteriaI> getPermissionsForUser(final UserI user, final String dataType) {
        return ImmutableList.copyOf(((XDATUser) user).getPermissionsByDataType(dataType));
    }

    @Override
    public List<PermissionCriteriaI> getPermissionsForUser(final String username, final String dataType) {
        return ImmutableList.copyOf(_cache.getPermissionCriteria(username, dataType));
    }

    @Override
    public CriteriaCollection getCriteriaForXDATRead(UserI user, SchemaElement root) throws IllegalAccessException, Exception {
        final String fullXMLName = root.getFullXMLName();
        if (!ElementSecurity.IsSecureElement(fullXMLName, READ)) {
            return null;
        }

        final boolean            isProjectData = StringUtils.equalsIgnoreCase("xnat:projectData", fullXMLName);
        final CriteriaCollection collection    = new CriteriaCollection("OR");
        for (final PermissionCriteriaI criteria : getPermissionsForUser(user, fullXMLName)) {
            if (isProjectData && log.isTraceEnabled()) {
                log.trace("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", criteria.getElementName(), criteria.getField(), criteria.getFieldValue(), criteria.getRead(), criteria.getActivate(), criteria.getEdit(), criteria.getCreate(), criteria.getDelete());
            }
            if (criteria.getRead()) {
                collection.add(DisplayCriteria.buildCriteria(root, criteria));
            }
        }

        if (collection.numClauses() == 0) {
            throw new IllegalAccessException("Invalid read privileges for " + root.getFullXMLName());
        }

        return collection;
    }

    @Override
    public CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI root) throws Exception {
        if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), READ)) {
            return null;
        }

        final CriteriaCollection collection = new CriteriaCollection("OR");
        for (PermissionCriteriaI criteria : getPermissionsForUser(user, root.getFullXMLName())) {
            if (criteria.getRead()) {
                collection.add(SearchCriteria.buildCriteria(criteria));
            }
        }

        if (collection.numClauses() == 0) {
            throw new IllegalAccessException("Invalid read privileges for " + root.getFullXMLName());
        }

        return collection;
    }

    @Override
    public boolean canCreate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, CREATE, root, values);
    }

    @Override
    public boolean canRead(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, READ, root, values);
    }

    @Override
    public boolean canEdit(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, EDIT, root, values);
    }

    @Override
    public boolean canActivate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, ACTIVATE, root, values);
    }

    @Override
    public boolean canDelete(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, DELETE, root, values);
    }

    @Override
    public String canStoreItem(UserI user, ItemI item, boolean allowDataDeletion) throws InvalidItemException, Exception {
        String invalidItemName;
        try {
            if (!canCreate(user, item)) {
                return item.getXSIType();
            }

            for (final Object object : item.getChildItems()) {
                ItemI child = (ItemI) object;
                invalidItemName = canStoreItem(user, child, allowDataDeletion);
                if (StringUtils.isNotBlank(invalidItemName)) {
                    return invalidItemName;
                }
            }
        } catch (XFTInitException e) {
            log.error("An error occurred initializing XFT", e);
        } catch (ElementNotFoundException e) {
            log.error("Did not find the requested element on the item", e);
        } catch (FieldNotFoundException e) {
            log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
        }
        return null;
    }

    @Override
    public ItemI secureItem(UserI user, ItemI item) throws IllegalAccessException, MetaDataException {
        final String xsiType = item.getXSIType();
        try {
            final String itemId = getItemIId(item);
            try {
                // Check readability
                if (!canRead(user, item)) {
                    final String message = String.format("User '%s' does not have read access to the %s instance with ID %s", user.getUsername(), xsiType, itemId);
                    log.error(message);
                    throw new IllegalAccessException("Access Denied: " + message);
                }

                // Check quarantine: if this item has a metadata element (which stores active status) and the user can't
                // activate this...
                if (item.getProperty("meta") != null && !canActivate(user, item)) {
                    // Then check to see if it's not active. You can't access inactive things.
                    if (!item.isActive()) {
                        final String message = String.format("The %s item with ID %s is in quarantine and the user %s does not have permission to activate this data type.", xsiType, itemId, user.getUsername());
                        log.error(message);
                        throw new IllegalAccessException("Access Denied: " + message);
                    }
                }

                final List<ItemI> invalidItems = new ArrayList<>();
                for (final Object object : item.getChildItems()) {
                    final ItemI   child   = (ItemI) object;
                    final boolean canRead = canRead(user, child);
                    if (canRead) {
                        secureChild(user, child);
                    } else {
                        invalidItems.add(child);
                    }
                }

                if (!invalidItems.isEmpty()) {
                    for (final Object invalidItem : invalidItems) {
                        XFTItem invalid = (XFTItem) invalidItem;
                        XFTItem parent  = (XFTItem) item;
                        parent.removeItem(invalid);
                        item = parent;
                    }
                }
            } catch (MetaDataException | IllegalAccessException e) {
                throw e;
            } catch (Exception e) {
                log.error("An error occurred trying to secure the item of type '{}' with ID '{}'", xsiType, itemId, e);
            }
        } catch (XFTInitException e) {
            log.error("An error occurred trying to access XFT when trying to get the element_name property from this ItemI object:\n{}", item, e);
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element of type {}: {}", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Couldn't find the field named {} for type {}: {}", e.FIELD, xsiType, e.MESSAGE);
        } catch (InvocationTargetException e) {
            log.error("An error occurred trying to call a method on the class '{}' to get the item ID", item.getClass().getName(), e);
        }

        return item;
    }

    @Override
    public boolean can(UserI user, ItemI item, String action) throws InvalidItemException, Exception {
        if (user == null || user.isGuest() && !action.equalsIgnoreCase(READ)) {
            return false;
        }
        final String xsiType = item.getXSIType();
        if (!ElementSecurity.HasDefinedElementSecurity(xsiType)) {
            return true;
        } else if (ElementSecurity.IsInSecureElement(xsiType)) {
            // The xdat:user type is "insecure", so you have to check explicitly if this is a user request. Only admins or self can read xdat:user.
            if (item instanceof XFTItem && StringUtils.equalsIgnoreCase(item.getXSIType(), "xdat:user")) {
                return Roles.isSiteAdmin(user) || StringUtils.equalsIgnoreCase(user.getUsername(), (String) item.getProperty("login"));
            }
            return true;
        } else {
            final ElementSecurity elementSecurity = ElementSecurity.GetElementSecurity(xsiType);
            if (elementSecurity.isSecure(action)) {
                final SchemaElement       schemaElement  = SchemaElement.GetElement(xsiType);
                final SecurityValues      securityValues = item.getItem().getSecurityValues();
                final Map<String, String> hash           = securityValues.getHash();
                if (hash.size() == 1 && hash.containsKey("xnat:projectData/ID")) {
                    return can(user.getUsername(), action, null, hash.get("xnat:projectData/ID"));
                }
                if (!securityCheckByXMLPath(user, action, schemaElement, securityValues)) {
                    log.info("User {} doesn't have permission to {} the schema element {} for XSI type {}. The security values are: {}.",
                             user.getUsername(),
                             action,
                             schemaElement.getFormattedName(),
                             xsiType,
                             securityValues.toString());
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean canRead(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, READ);
    }

    @Override
    public boolean canEdit(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, EDIT);
    }

    @Override
    public boolean canCreate(UserI user, ItemI item) throws Exception {
        return can(user, item, CREATE);
    }

    @Override
    public boolean canActivate(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, ACTIVATE);
    }

    @Override
    public boolean canDelete(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, DELETE);
    }

    @Override
    public boolean can(UserI user, String xmlPath, Object value, String action) throws Exception {
        if (user.isGuest() && !action.equalsIgnoreCase(READ)) {
            return false;
        }
        String rootElement = XftStringUtils.GetRootElementName(xmlPath);
        if (!ElementSecurity.HasDefinedElementSecurity(rootElement)) {
            return true;
        } else if (ElementSecurity.IsInSecureElement(rootElement)) {
            return true;
        } else {
            SecurityValues sv = new SecurityValues();
            sv.put(xmlPath, value.toString());
            return securityCheckByXMLPath(user, action, SchemaElement.GetElement(rootElement), sv);
        }
    }

    @Override
    public boolean canRead(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, READ);
    }

    @Override
    public boolean canEdit(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, EDIT);
    }

    @Override
    public boolean canCreate(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, CREATE);
    }

    @Override
    public boolean canActivate(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, ACTIVATE);
    }

    @Override
    public boolean canDelete(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, DELETE);
    }

    @Override
    public boolean canRead(final UserI user, final String entityId) {
        return can(user.getUsername(), READ, null, entityId);
    }

    @Override
    public boolean canEdit(final UserI user, final String entityId) {
        return can(user.getUsername(), EDIT, null, entityId);
    }

    @Override
    public boolean canCreate(final UserI user, final String entityId) {
        return can(user.getUsername(), CREATE, null, entityId);
    }

    @Override
    public boolean canDelete(final UserI user, final String entityId) {
        return can(user.getUsername(), DELETE, null, entityId);
    }

    @Override
    public boolean canActivate(final UserI user, final String entityId) {
        return can(user.getUsername(), ACTIVATE, null, entityId);
    }

    @Override
    public boolean canRead(final UserI user, final String project, final String entityId) {
        return can(user.getUsername(), READ, project, entityId);
    }

    @Override
    public boolean canEdit(final UserI user, final String project, final String entityId) {
        return can(user.getUsername(), EDIT, project, entityId);
    }

    @Override
    public boolean canCreate(final UserI user, final String project, final String entityId) {
        return can(user.getUsername(), CREATE, project, entityId);
    }

    @Override
    public boolean canDelete(final UserI user, final String project, final String entityId) {
        return can(user.getUsername(), DELETE, project, entityId);
    }

    @Override
    public boolean canActivate(final UserI user, final String project, final String entityId) {
        return can(user.getUsername(), ACTIVATE, project, entityId);
    }

    @Override
    public boolean canAny(UserI user, String elementName, String xmlPath, String action) {
        if (user.isGuest() && !action.equalsIgnoreCase(READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        return !getAllowedValues(user, elementName, xmlPath, action).isEmpty();
    }

    @Override
    public boolean canAny(UserI user, String elementName, String action) {
        if (user.isGuest() && !action.equalsIgnoreCase(READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        return !getAllowedValues(user, elementName, action).isEmpty();
    }

    @Override
    public boolean canAny(final String username, final String elementName, final String action) {
        if (isGuest(username) && !action.equalsIgnoreCase(READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        return !getAllowedValues(username, elementName, action).isEmpty();
    }

    @Override
    public List<Object> getAllowedValues(final UserI user, final String elementName, final String xmlPath, final String action) {
        return getAllowedValues(user.getUsername(), elementName, xmlPath, action);
    }

    @Override
    public List<Object> getAllowedValues(final String username, final String elementName, final String xmlPath, final String action) {
        //noinspection rawtypes
        final List allowedValues = new ArrayList();

        try {
            final String rootXmlName = SchemaElement.GetElement(elementName).getFullXMLName();
            if (ElementSecurity.IsSecureElement(rootXmlName, action)) {
                final List<PermissionCriteriaI> permissions = getPermissionsForUser(username, rootXmlName);
                for (final PermissionCriteriaI criteria : permissions) {
                    if (criteria.getAction(action) && !allowedValues.contains(criteria.getFieldValue())) {
                        //noinspection unchecked
                        allowedValues.add(criteria.getFieldValue());
                    }
                }
            } else {
                //noinspection unchecked
                allowedValues.addAll(GenericWrapperElement.GetUniqueValuesForField(xmlPath));
            }

            Collections.sort(allowedValues);
        } catch (Exception e) {
            log.error("An error occurred trying to get the allowed values for user '{}' action '{}' on data type '{}', XML path '{}'", username, action, elementName, xmlPath, e);
        }

        //noinspection unchecked
        return ImmutableList.copyOf(allowedValues);
    }

    @Override
    public Map<String, Object> getAllowedValues(UserI user, String elementName, String action) {
        return getAllowedValues(user.getUsername(), elementName, action);
    }

    @Override
    public Map<String, Object> getAllowedValues(final String username, String elementName, String action) {
        final Map<String, Object> allowedValues = Maps.newHashMap();

        try {
            final String rootXmlName = SchemaElement.GetElement(elementName).getFullXMLName();
            if (ElementSecurity.IsSecureElement(rootXmlName, action)) {
                final List<PermissionCriteriaI> permissions = getPermissionsForUser(username, rootXmlName);
                if (log.isInfoEnabled()) {
                    if (!permissions.isEmpty()) {
                        log.info("Found {} permissions for user {} to {} data type {}:\n    {}", permissions.size(), username, action, elementName, StringUtils.join(permissions, "\n    "));
                    } else {
                        log.info("Found no permissions for user {} to {} data type {}", username, action, elementName);
                    }
                }
                for (final PermissionCriteriaI criteria : permissions) {
                    if (criteria.getAction(action)) {
                        log.debug("User {} can {} data type {} according to criteria for {} {}", username, action, elementName, criteria.getField(), criteria.getFieldValue());
                        allowedValues.put(criteria.getField(), criteria.getFieldValue());
                    } else if (log.isDebugEnabled()) {
                        log.debug("User {} can not {} data type {} according to criteria for {} {}", username, action, elementName, criteria.getField(), criteria.getFieldValue());
                    }
                }
            }
        } catch (Exception e) {
            log.error("An error occurred trying to get the allowed values for user '{}' action '{}' on data type '{}'", username, action, elementName, e);
        }

        return ImmutableMap.copyOf(allowedValues);
    }

    @Override
    public void setPermissions(final UserI affected, final UserI authenticated, final String elementName, final String fieldName, final String fieldValue, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final EventMetaI ci) {
        setPermissionsInternal(true, affected, authenticated, elementName, fieldName, fieldValue, create, read, delete, edit, activate, activateChanges, ci);
    }

    @Override
    public boolean initializeDefaultAccessibility(final String tag, final String accessibility, final boolean forceInit, final UserI authenticatedUser, final EventMetaI ci) throws Exception {
        return setAccessibilityInternal(false, tag, accessibility, forceInit, authenticatedUser, ci);
    }

    @Override
    public boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, UserI authenticatedUser, EventMetaI ci) throws Exception {
        return setAccessibilityInternal(true, tag, accessibility, forceInit, authenticatedUser, ci);
    }

    @Override
    public List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType) {
        return ImmutableList.copyOf(group.getPermissionsByDataType(dataType));
    }

    @Override
    public Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group) {
        return ImmutableMap.copyOf(((UserGroup) group).getAllPermissions());
    }

    @Override
    public void setPermissionsForGroup(final UserGroupI group, final List<PermissionCriteriaI> criteria, final EventMetaI meta, final UserI authenticatedUser) throws Exception {
        for (final PermissionCriteriaI criterion : criteria) {
            ((UserGroup) group).addPermission(criterion.getElementName(), criterion, authenticatedUser);
        }
    }

    @Override
    public String getUserPermissionsSQL(final UserI user) {
        return String.format(QUERY_USER_READABLE_ELEMENTS, user.getUsername());
    }

    @Override
    public List<String> getUserReadableProjects(final UserI user) {
        return getUserReadableProjects(user.getUsername());
    }

    @Override
    public List<String> getUserReadableProjects(final String username) {
        return _cache.getProjectsForUser(username, READ);
    }

    @Override
    public List<String> getUserEditableProjects(final UserI user) {
        return getUserEditableProjects(user.getUsername());
    }

    @Override
    public List<String> getUserEditableProjects(final String username) {
        return _cache.getProjectsForUser(username, EDIT);
    }

    @Override
    public List<String> getUserOwnedProjects(final UserI user) {
        return getUserOwnedProjects(user.getUsername());
    }

    @Override
    public List<String> getUserOwnedProjects(final String username) {
        return _cache.getProjectsForUser(username, DELETE);
    }

    private boolean can(final @Nonnull String username, final @Nonnull String action, final @Nullable String projectId, final @Nonnull String entityId) {
        final boolean hasProjectId = StringUtils.isNotBlank(projectId);
        final String  query;
        switch (action) {
            case READ:
                query = hasProjectId ? QUERY_CAN_USER_READ_ID_IN_PROJECT : QUERY_CAN_USER_READ_ID;
                break;
            case EDIT:
                query = hasProjectId ? QUERY_CAN_USER_EDIT_ID_IN_PROJECT : QUERY_CAN_USER_EDIT_ID;
                break;
            case CREATE:
                query = hasProjectId ? QUERY_CAN_USER_CREATE_ID_IN_PROJECT : QUERY_CAN_USER_CREATE_ID;
                break;
            case DELETE:
                query = hasProjectId ? QUERY_CAN_USER_DELETE_ID_IN_PROJECT : QUERY_CAN_USER_DELETE_ID;
                break;
            case ACTIVATE:
                query = hasProjectId ? QUERY_CAN_USER_ACTIVE_ID_IN_PROJECT : QUERY_CAN_USER_ACTIVE_ID;
                break;
            default:
                throw new IllegalArgumentException("Action must be one of read, edit, create, delete, or active. Invalid: " + action);
        }
        final MapSqlParameterSource parameters = new MapSqlParameterSource("username", username).addValue("entityId", entityId);
        if (hasProjectId) {
            parameters.addValue("projectId", projectId);
        }
        return _template.queryForObject(query, parameters, Boolean.class);
    }

    private boolean securityCheck(final UserI user, final String action, final SchemaElementI root, final SecurityValues values) throws Exception {
        final String rootXmlName = root.getFullXMLName();
        if (ElementSecurity.IsInSecureElement(rootXmlName)) {
            return true;
        } else {
            final List<PermissionCriteriaI> criteria = getPermissionsForUser(user, rootXmlName);
            final String                    username = user.getUsername();
            if (criteria.size() == 0) {
                if (!user.isGuest()) {
                    if (log.isDebugEnabled()) {
                        // If debug is enabled, add exception to the logging to provide stack-trace info.
                        log.debug("No permission criteria found for user '{}' with action '{}' on the schema element '{}' and the following security values: {}.",
                                  username,
                                  action,
                                  rootXmlName,
                                  values.toString(),
                                  new Exception());
                    } else {
                        log.info("No permission criteria found for user '{}' with action '{}' on the schema element '{}' and the following security values: {}.",
                                 username,
                                 action,
                                 rootXmlName,
                                 values.toString());
                    }
                }
                return false;
            }

            if (log.isInfoEnabled()) {
                log.info("Checking user {} access to action {} with security values {}", username, action, values.toString());
            }

            if (Iterables.any(criteria, new Predicate<PermissionCriteriaI>() {
                @Override
                public boolean apply(final PermissionCriteriaI criterion) {
                    if (log.isInfoEnabled()) {
                        log.info(" * Testing against criterion {}", criterion.toString());
                    }
                    try {
                        if (criterion.canAccess(action, values)) {
                            if (log.isDebugEnabled()) {
                                log.debug("User {} has {} access on element {} with criterion {} and security values: {}", username, action, rootXmlName, criterion.toString(), values.toString());
                            }
                            return true;
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("User {} does not have {} access on element {} with criterion {} and security values: {}", username, action, rootXmlName, criterion.toString(), values.toString());
                        }
                    } catch (Exception e) {
                        log.error("An error occurred trying to check {} access for user {} with criterion {} and values: {}", action, username, criterion.toString(), values);
                    }
                    return false;
                }
            })) {
                return true;
            }

            // If we've reached here, the security check has failed so let's provide some information on the context but
            // only if this isn't the guest user and the log level is INFO or below...
            if (!user.isGuest() && log.isInfoEnabled()) {
                log.info("User {} not able to {} the schema element {} with the security values: {}. {}",
                         username,
                         action,
                         root.getFormattedName(),
                         values.toString(),
                         dumpCriteriaList(criteria));
            }
        }

        return false;
    }

    private boolean setAccessibilityInternal(final boolean triggerEvent, final String projectId, final String accessibility, final boolean forceInit, final UserI authenticatedUser, final EventMetaI ci) throws Exception {
        final List<ElementSecurity> securedElements = ElementSecurity.GetSecureElements();
        final UserI                 guest           = Users.getGuest();

        if (securedElements.isEmpty()) {
            log.error("Setting access level for project {} to {}, but there are no secured elements to set. Most likely bad things are going to happen.", projectId, accessibility);
        } else {
            log.info("Setting access level for project {} to {}, along with {} secured elements", projectId, accessibility, securedElements.size());
        }

        final int currentAccessibility = Permissions.checkProjectAccess(_template, projectId);
        if (StringUtils.equals("public", accessibility)) {
            if (currentAccessibility == 1) {
                return false;
            }
            final List<Integer> projectFieldMappingIds = _template.queryForList(QUERY_FIELD_MAPPING, new MapSqlParameterSource("field", "xnat:projectData/ID").addValue("projectId", projectId), Integer.class);
            if (projectFieldMappingIds.isEmpty()) {
                setPermissionsInternal(false, guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", projectId, false, true, false, false, true, true, ci);
            } else if (projectFieldMappingIds.size() == 1) {
                _template.update(QUERY_MAKE_FIELD_MAPPING_PUBLIC, new MapSqlParameterSource("fieldMappingId", projectFieldMappingIds.get(0)));
            } else {
                log.warn("Found case where there's more than one field mapping for guest access to project ID {}", projectId);
            }
            for (final ElementSecurity securedElement : securedElements) {
                final String elementName = securedElement.getElementName();
                log.debug("Preparing to set permissions for secured element '{}' while setting access level for project {} to {}", elementName, projectId, accessibility);
                if (securedElement.hasField(elementName + "/project")) {
                    log.debug("Setting permissions for secured element field '{}/project' while setting access level for project {} to {}", elementName, projectId, accessibility);
                    setPermissionsInternal(false, guest, authenticatedUser, elementName, elementName + "/project", projectId, false, true, false, false, true, true, ci);
                } else if (!StringUtils.equalsIgnoreCase("xnat:projectData", elementName)) {
                    log.warn("The secured element '{}' does not have the field '{}' while trying to set project {} accessibility to public", elementName, elementName + "/project", projectId);
                }
                if (securedElement.hasField(elementName + "/sharing/share/project")) {
                    log.debug("Setting permissions for secured element field '{}/sharing/share/project' while setting access level for project {} to {}", elementName, projectId, accessibility);
                    setPermissionsInternal(false, guest, authenticatedUser, elementName, elementName + "/sharing/share/project", projectId, false, true, false, false, false, true, ci);
                } else if (!StringUtils.equalsIgnoreCase("xnat:projectData", elementName)) {
                    log.warn("The secured element '{}' does not have the field '{}' while trying to set project {} accessibility to public", elementName, elementName + "/sharing/share/project", projectId);
                }
            }
        } else {
            // Main diff between protected and private is that the project ID is readable by guest in protected, so set that once and apply privileges.
            // Other than that, nothing else is readable by guest in protected or private.
            final boolean readableByGuest = StringUtils.equals("protected", accessibility);
            if (readableByGuest && currentAccessibility == 0 || !readableByGuest && currentAccessibility == -1) {
                return false;
            }
            log.debug("The project {} will {}be readable by guest users", projectId, readableByGuest ? "" : "not ");
            final List<Integer> projectFieldMappingIds = _template.queryForList(QUERY_FIELD_MAPPING, new MapSqlParameterSource("field", "xnat:projectData/ID").addValue("projectId", projectId), Integer.class);

            // If no mapping IDs were found but we need one for protected status, then create that one.
            if (readableByGuest) {
                if (projectFieldMappingIds.isEmpty()) {
                    setPermissionsInternal(false, guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", projectId, false, true, false, false, false, true, ci);
                } else if (projectFieldMappingIds.size() == 1) {
                    _template.update(QUERY_MAKE_FIELD_MAPPING_PROTECTED, new MapSqlParameterSource("fieldMappingId", projectFieldMappingIds.get(0)));
                } else {
                    log.warn("Found case where there's more than one field mapping for guest access to project ID {}", projectId);
                }
            } else if (!projectFieldMappingIds.isEmpty()) {
                // We found some mappings for guess access to project but this project isn't readable so they all gotta go.
                deleteFieldMappings(projectFieldMappingIds, authenticatedUser, ci);
            }
            final Set<Integer> dataTypeFieldMappingIds = new HashSet<>();
            for (final ElementSecurity securedElement : securedElements) {
                final String elementName = securedElement.getElementName();
                if (!StringUtils.equalsIgnoreCase("xnat:projectData", elementName)) {
                    log.debug("Preparing to set permissions for secured element '{}' while setting access level for project {} to {}", elementName, projectId, accessibility);
                    dataTypeFieldMappingIds.addAll(getFieldMappingIdsForDataType(projectId, accessibility, securedElement, elementName, elementName + "/project"));
                    dataTypeFieldMappingIds.addAll(getFieldMappingIdsForDataType(projectId, accessibility, securedElement, elementName, elementName + "/sharing/share/project"));
                }
            }
            deleteFieldMappings(dataTypeFieldMappingIds, authenticatedUser, ci);
        }

        ((XDATUser) authenticatedUser).resetCriteria();
        ((XDATUser) guest).resetCriteria();
        Users.getGuest(true);

        if (triggerEvent) {
            _eventService.triggerEvent(builder().xsiType("xnat:projectData").id(projectId).action(UPDATE).property("accessibility", accessibility).build());
        }

        return true;
    }

    private void deleteFieldMappings(final Collection<Integer> fieldMappingIds, final UserI authenticatedUser, final EventMetaI ci) throws Exception {
        for (final Integer fieldMappingId : fieldMappingIds) {
            final XdatFieldMapping mapping = XdatFieldMapping.getXdatFieldMappingsByXdatFieldMappingId(fieldMappingId, authenticatedUser, false);
            if (mapping != null) {
                log.debug("Deleting field mapping with ID {}", fieldMappingId);
                SaveItemHelper.authorizedDelete(mapping.getItem(), authenticatedUser, ci);
            } else {
                log.warn("Tried to retrieve field mapping with ID {} but didn't find anything", fieldMappingId);
            }
        }
    }

    private List<Integer> getFieldMappingIdsForDataType(final String projectId, final String accessibility, final ElementSecurity securedElement, final String elementName, final String field) {
        if (securedElement.hasField(field)) {
            return _template.queryForList(QUERY_FIELD_MAPPING, new MapSqlParameterSource("field", field).addValue("projectId", projectId), Integer.class);
        } else {
            log.warn("The secured element '{}' does not have the field '{}' while trying to set project {} accessibility to {}", elementName, field, projectId, accessibility);
            return Collections.emptyList();
        }
    }

    private void setPermissionsInternal(final boolean triggerEvent, final UserI affected, final UserI authenticated, final String elementName, final String fieldName, final String fieldValue, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final EventMetaI ci) {
        try {
            final boolean isAccessible = create || read || edit || delete || activate;
            if (!isAccessible) {
                final Integer fieldMappingId = _template.queryForObject(QUERY_FIELD_MAPPING, new MapSqlParameterSource("field", fieldName).addValue("projectId", fieldValue), Integer.class);
                deleteFieldMappings(Collections.singletonList(fieldMappingId), affected, ci);
                if (triggerEvent) {
                    _eventService.triggerEvent(builder().xsiType(XdatFieldMapping.SCHEMA_ELEMENT_NAME).id(fieldMappingId.toString()).action(XftItemEventI.DELETE).build());
                }
                return;
            }

            final XDATUser user            = (XDATUser) affected;
            final Long     elementAccessId = getUserElementAccess(user, elementName);

            final XdatElementAccess elementAccess;
            if (elementAccessId != null) {
                elementAccess = XdatElementAccess.getXdatElementAccesssByXdatElementAccessId(elementAccessId, user, true);
                if (elementAccess == null) {
                    throw new NrgServiceException(NrgServiceError.UnknownEntity, "Found the element access ID value '" + elementAccessId + "' but was not able to retrieve a corresponding XdatElementAccess object");
                }
            } else {
                elementAccess = new XdatElementAccess(authenticated);
                elementAccess.setElementName(elementName);
                elementAccess.setProperty("xdat_user_xdat_user_id", user.getID());
            }

            final XdatFieldMappingSet fieldMappingSet = elementAccess.getOrCreateFieldMappingSet(authenticated);
            final XdatFieldMapping    fieldMapping    = getFieldMapping(authenticated, fieldMappingSet, fieldName, fieldValue);

            if (fieldMapping == null) {
                return;
            }

            fieldMapping.setField(fieldName);
            fieldMapping.setFieldValue(fieldValue);
            fieldMapping.setCreateElement(create);
            fieldMapping.setReadElement(read);
            fieldMapping.setEditElement(edit);
            fieldMapping.setDeleteElement(delete);
            fieldMapping.setActiveElement(activate);
            fieldMapping.setComparisonType("equals");

            fieldMappingSet.setAllow(fieldMapping);

            if (fieldMappingSet.getXdatFieldMappingSetId() != null) {
                fieldMapping.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fieldMappingSet.getXdatFieldMappingSetId());

                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fieldMapping, authenticated, true, false, true, false, ci);
                    fieldMapping.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fieldMapping, authenticated, true, false, false, false, ci);
                }
            } else if (elementAccess.getXdatElementAccessId() != null) {
                fieldMappingSet.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", elementAccess.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fieldMappingSet, authenticated, true, false, true, false, ci);
                    fieldMappingSet.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fieldMappingSet, authenticated, true, false, false, false, ci);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(elementAccess, authenticated, true, false, true, false, ci);
                    elementAccess.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(elementAccess, authenticated, true, false, false, false, ci);
                }
                ((XDATUser) affected).setElementAccess(elementAccess);
            }

            if (triggerEvent) {
                _eventService.triggerEvent(builder().xsiType(elementName).id(fieldValue).action(UPDATE).build());
            }
        } catch (XFTInitException e) {
            log.error("An error occurred initializing XFT", e);
        } catch (ElementNotFoundException e) {
            log.error("Did not find the requested element on the item", e);
        } catch (FieldNotFoundException e) {
            log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
        } catch (InvalidValueException e) {
            log.error("Invalid value specified: {}", affected.getID(), e);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private Long getUserElementAccess(final UserI user, final String elementName) {
        try {
            return _template.queryForObject(QUERY_USER_ELEMENT_ACCESS, new MapSqlParameterSource("elementName", elementName).addValue("identifier", user.getUsername()), Long.class);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Found no xnat:projectData elements configured for user {}", user.getUsername());
            return null;
        }
    }

    private XdatFieldMapping getFieldMapping(final UserI user, final XdatFieldMappingSet fieldMappingSet, final String fieldName, final String fieldValue) {
        final Optional<XdatFieldMapping> optional = FluentIterable.from(fieldMappingSet.getAllow()).firstMatch(new Predicate<XdatFieldMapping>() {
            @Override
            public boolean apply(@Nullable final XdatFieldMapping fieldMapping) {
                return fieldMapping != null && StringUtils.equals(fieldValue, fieldMapping.getFieldValue()) && StringUtils.equals(fieldName, fieldMapping.getField());
            }
        });

        //noinspection UnstableApiUsage
        return optional.or(new Supplier<XdatFieldMapping>() {
            @Override
            public XdatFieldMapping get() {
                return new XdatFieldMapping(user);
            }
        });
    }

    private boolean securityCheckByXMLPath(UserI user, String action, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, action, root, values);
    }

    private String getItemIId(final ItemI item) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, IllegalAccessException, InvocationTargetException {
        final String itemId;
        if (item instanceof XFTItem) {
            itemId = ((XFTItem) item).getIDValue();
        } else if (item instanceof org.nrg.xdat.base.BaseElement) {
            itemId = ((org.nrg.xdat.base.BaseElement) item).getStringProperty("ID");
        } else {
            final Method getId = Reflection.getMatchingMethod(item.getClass(), "getId", new Object[0]);
            if (getId != null) {
                itemId = (String) getId.invoke(item);
            } else {
                final Method getID = Reflection.getMatchingMethod(item.getClass(), "getID", new Object[0]);
                if (getID != null) {
                    itemId = (String) getID.invoke(item);
                } else {
                    itemId = "Couldn't determine item's ID, attaching full XML\n" + item.toString();
                }
            }
        }
        return itemId;
    }

    private void secureChild(UserI user, ItemI item) throws Exception {
        List<ItemI> invalidItems = new ArrayList<>();

        for (final Object o : item.getChildItems()) {
            ItemI   child = (ItemI) o;
            boolean b     = canRead(user, child);

            if (b) {
                if (child.getProperty("meta") != null && !canActivate(user, child)) {
                    if (!child.isActive()) {
                        b = false;
                    }
                }
            }

            if (b) {
                secureChild(user, child);
            } else {
                invalidItems.add(child);
            }
        }

        if (invalidItems.size() > 0) {
            for (final ItemI invalid : invalidItems) {
                ((XFTItem) item).removeItem(invalid);
            }
        }
    }

    private boolean isGuest(final String username) {
        return _guest != null ? StringUtils.equalsIgnoreCase(_guest.getUsername(), username) : StringUtils.equalsIgnoreCase(GUEST_USERNAME, username);
    }

    private static final String GUEST_USERNAME                      = "guest";
    private static final String QUERY_USER_ELEMENT_ACCESS           = "SELECT  " +
                                                                      "  xdat_element_access_id  " +
                                                                      "FROM  " +
                                                                      "  xdat_element_access a  " +
                                                                      "    LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id  " +
                                                                      "    LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id  " +
                                                                      "WHERE  " +
                                                                      "  a.element_name = :elementName AND  " +
                                                                      "  (u.login = :identifier OR g.id = :identifier)";
    private static final String QUERY_USER_READABLE_ELEMENTS        = "SELECT " +
                                                                      "  a.element_name, " +
                                                                      "  m.field, " +
                                                                      "  m.field_value " +
                                                                      "FROM xdat_user u " +
                                                                      "  LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id " +
                                                                      "  LEFT JOIN xdat_usergroup g on i.groupid = g.id " +
                                                                      "  LEFT JOIN xdat_element_access a on (xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = a.xdat_user_xdat_user_id) " +
                                                                      "  LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                      "  LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                      "WHERE " +
                                                                      "  m.field_value != '*' AND " +
                                                                      "  m.read_element = 1 AND " +
                                                                      "  u.login IN ('guest', '%s')";
    private static final String QUERY_FIELD_MAPPING                 = "SELECT " +
                                                                      "  m.xdat_field_mapping_id AS fieldMappingId " +
                                                                      "FROM " +
                                                                      "  xdat_element_access a " +
                                                                      "    LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id " +
                                                                      "    LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                      "    LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                      "WHERE " +
                                                                      "  u.login = 'guest' AND " +
                                                                      "  m.field = :field AND " +
                                                                      "  m.field_value = :projectId";
    private static final String QUERY_FIELD_MAPPING_EXISTS          = "SELECT EXISTS(" + QUERY_FIELD_MAPPING + ")";
    private static final String QUERY_FIND_ORPHAN_ELEMENT_ACCESS    = "SELECT " +
                                                                      "  a.xdat_element_access_id " +
                                                                      "FROM " +
                                                                      "  xdat_element_access a " +
                                                                      "    LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                                      "WHERE " +
                                                                      "  s.permissions_allow_set_xdat_elem_xdat_element_access_id IS NULL";
    private static final String QUERY_FIND_ORPHAN_MAPPING_SETS      = "SELECT " +
                                                                      "  s.xdat_field_mapping_set_id " +
                                                                      "FROM " +
                                                                      "  xdat_field_mapping_set s " +
                                                                      "    LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                                      "WHERE " +
                                                                      "  m.xdat_field_mapping_set_xdat_field_mapping_set_id IS NULL";
    private static final String QUERY_MAKE_FIELD_MAPPING_PROTECTED  = "UPDATE " +
                                                                      "  xdat_field_mapping " +
                                                                      "SET " +
                                                                      "  create_element = 0, " +
                                                                      "  read_element = 1, " +
                                                                      "  edit_element = 0, " +
                                                                      "  delete_element = 0, " +
                                                                      "  active_element = 0 " +
                                                                      "WHERE " +
                                                                      "  xdat_field_mapping_id = :fieldMappingId";
    private static final String QUERY_MAKE_FIELD_MAPPING_PUBLIC     = "UPDATE " +
                                                                      "  xdat_field_mapping " +
                                                                      "SET " +
                                                                      "  create_element = 0, " +
                                                                      "  read_element = 1, " +
                                                                      "  edit_element = 0, " +
                                                                      "  delete_element = 0, " +
                                                                      "  active_element = 1 " +
                                                                      "WHERE " +
                                                                      "  xdat_field_mapping_id = :fieldMappingId";
    private static final String QUERY_GET_USER_PERMS_FOR_ID         = "SELECT * FROM data_type_fns_get_entity_permissions(:username, :entityId)";
    private static final String QUERY_CAN_USER_READ_ID              = "SELECT data_type_fns_can(:username, 'read', :entityId) AS can_read";
    private static final String QUERY_CAN_USER_EDIT_ID              = "SELECT data_type_fns_can(:username, 'edit', :entityId) AS can_edit";
    private static final String QUERY_CAN_USER_CREATE_ID            = "SELECT data_type_fns_can(:username, 'create', :entityId) AS can_create";
    private static final String QUERY_CAN_USER_DELETE_ID            = "SELECT data_type_fns_can(:username, 'delete', :entityId) AS can_delete";
    private static final String QUERY_CAN_USER_ACTIVE_ID            = "SELECT data_type_fns_can(:username, 'active', :entityId) AS can_active";
    private static final String QUERY_CAN_USER_READ_ID_IN_PROJECT   = "SELECT data_type_fns_can(:username, 'read', :entityId, :projectId) AS can_read";
    private static final String QUERY_CAN_USER_EDIT_ID_IN_PROJECT   = "SELECT data_type_fns_can(:username, 'edit', :entityId, :projectId) AS can_edit";
    private static final String QUERY_CAN_USER_CREATE_ID_IN_PROJECT = "SELECT data_type_fns_can(:username, 'create', :entityId, :projectId) AS can_create";
    private static final String QUERY_CAN_USER_DELETE_ID_IN_PROJECT = "SELECT data_type_fns_can(:username, 'delete', :entityId, :projectId) AS can_delete";
    private static final String QUERY_CAN_USER_ACTIVE_ID_IN_PROJECT = "SELECT data_type_fns_can(:username, 'active', :entityId, :projectId) AS can_active";

    private final DataTypeAwareEventService  _eventService;
    private final NamedParameterJdbcTemplate _template;

    private GroupsAndPermissionsCache _cache;
    private UserI                     _guest;
}
