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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.Reflection;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.PermissionCriteria.dumpCriteriaList;
import static org.nrg.xft.event.XftItemEvent.builder;
import static org.nrg.xft.event.XftItemEventI.DELETE;
import static org.nrg.xft.event.XftItemEventI.UPDATE;

@SuppressWarnings({"unused", "DuplicateThrows"})
@Service
@Slf4j
public class PermissionsServiceImpl implements PermissionsServiceI {
    @Autowired
    public PermissionsServiceImpl(final NrgEventService eventService) {
        _eventService = eventService;
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
        if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), SecurityManager.READ)) {
            return null;
        }

        final CriteriaCollection collection = new CriteriaCollection("OR");
        for (final PermissionCriteriaI criteria : getPermissionsForUser(user, root.getFullXMLName())) {
            if (criteria.getRead()) {
                collection.add(DisplayCriteria.buildCriteria(root, criteria));
            }
        }

        if (collection.numClauses() == 0) {
            return null;
        }

        return collection;
    }

    @Override
    public CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI root) throws Exception {
        if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), SecurityManager.READ)) {
            return null;
        }

        final CriteriaCollection collection = new CriteriaCollection("OR");
        for (PermissionCriteriaI criteria : getPermissionsForUser(user, root.getFullXMLName())) {
            if (criteria.getRead()) {
                collection.add(SearchCriteria.buildCriteria(criteria));
            }
        }

        if (collection.numClauses() == 0) {
            return null;
        }

        return collection;
    }

    @Override
    public boolean canCreate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, SecurityManager.CREATE, root, values);
    }

    @Override
    public boolean canRead(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, SecurityManager.READ, root, values);
    }

    @Override
    public boolean canEdit(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, SecurityManager.EDIT, root, values);
    }

    @Override
    public boolean canActivate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, SecurityManager.ACTIVATE, root, values);
    }

    @Override
    public boolean canDelete(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user, SecurityManager.DELETE, root, values);
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
        if (user == null || user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
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
                final SchemaElement  schemaElement  = SchemaElement.GetElement(xsiType);
                final SecurityValues securityValues = item.getItem().getSecurityValues();
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
        return can(user, item, SecurityManager.READ);
    }

    @Override
    public boolean canEdit(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, SecurityManager.EDIT);
    }

    @Override
    public boolean canCreate(UserI user, ItemI item) throws Exception {
        return can(user, item, SecurityManager.CREATE);
    }

    @Override
    public boolean canActivate(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, SecurityManager.ACTIVATE);
    }

    @Override
    public boolean canDelete(UserI user, ItemI item) throws InvalidItemException, Exception {
        return can(user, item, SecurityManager.DELETE);
    }

    @Override
    public boolean can(UserI user, String xmlPath, Object value, String action) throws Exception {
        if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
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
        return can(user, xmlPath, value, SecurityManager.READ);
    }

    @Override
    public boolean canEdit(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, SecurityManager.EDIT);
    }

    @Override
    public boolean canCreate(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, SecurityManager.CREATE);
    }

    @Override
    public boolean canActivate(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, SecurityManager.ACTIVATE);
    }

    @Override
    public boolean canDelete(UserI user, String xmlPath, Object value) throws Exception {
        return can(user, xmlPath, value, SecurityManager.DELETE);
    }

    @Override
    public boolean canAny(UserI user, String elementName, String xmlPath, String action) {
        if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        return !getAllowedValues(user, elementName, xmlPath, action).isEmpty();
    }

    @Override
    public boolean canAny(UserI user, String elementName, String action) {
        if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        return !getAllowedValues(user, elementName, action).isEmpty();
    }

    @Override
    public boolean canAny(final String username, final String elementName, final String action) {
        if (isGuest(username) && !action.equalsIgnoreCase(SecurityManager.READ)) {
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
        _eventService.triggerEvent(builder().xsiType(XdatUsergroup.SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
    }

    @Override
    public String getUserPermissionsSQL(final UserI user) {
        return String.format(QUERY_USER_READABLE_ELEMENTS, user.getUsername());
    }

    @Override
    public List<String> getUserReadableProjects(final UserI user) {
        return _cache.getProjectsForUser(user.getUsername(), SecurityManager.READ);
    }

    @Override
    public List<String> getUserEditableProjects(final UserI user) {
        return ((XDATUser) user).getAccessibleProjects();
    }

    @Override
    public List<String> getUserOwnedProjects(final UserI user) {
        return _cache.getProjectsForUser(user.getUsername(), SecurityManager.DELETE);
    }

    private boolean securityCheck(UserI user, String action, SchemaElementI root, SecurityValues values) throws Exception {
        final String rootXmlName = root.getFullXMLName();
        if (ElementSecurity.IsInSecureElement(rootXmlName)) {
            return true;
        } else {
            final List<PermissionCriteriaI> criteria = getPermissionsForUser(user, rootXmlName);
            final String                    username = user.getUsername();
            if (criteria.size() == 0) {
                if (!user.isGuest()) {
                    log.error("{}: No permission criteria found for user '{}' with action '{}' on the schema element '{}' and the following security values: {}.",
                              new Exception().getStackTrace()[0].toString(),
                              username,
                              action,
                              rootXmlName,
                              values.toString());
                }
                return false;
            }

            if (log.isInfoEnabled()) {
                log.info("Checking user {} access to action {} with security values {}", username, action, values.toString());
            }

            for (final PermissionCriteriaI criterion : criteria) {
                if (log.isInfoEnabled()) {
                    log.info(" * Testing against criterion {}", criterion.toString());
                }
                if (criterion.canAccess(action, values)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User {} has {} access on element {} with criterion {} and security values: {}", username, action, rootXmlName, criterion.toString(), values.toString());
                    }
                    return true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("User {} does not have {} access on element {} with criterion {} and security values: {}", username, action, rootXmlName, criterion.toString(), values.toString());
                    }
                }
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

    private boolean setAccessibilityInternal(final boolean triggerEvent, String tag, String accessibility, boolean forceInit, UserI authenticatedUser, EventMetaI ci) throws Exception {
        final List<ElementSecurity> securedElements = ElementSecurity.GetSecureElements();
        final UserI                 guest           = Users.getGuest();

        if (StringUtils.equals("public", accessibility)) {
            setPermissionsInternal(false, guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", tag, false, true, false, false, true, true, ci);
            for (final ElementSecurity securedElement : securedElements) {
                final String elementName = securedElement.getElementName();
                if (securedElement.hasField(elementName + "/project") && securedElement.hasField(elementName + "/sharing/share/project")) {
                    setPermissionsInternal(false, guest, authenticatedUser, elementName, elementName + "/project", tag, false, true, false, false, true, true, ci);
                    setPermissionsInternal(false, guest, authenticatedUser, elementName, elementName + "/sharing/share/project", tag, false, false, false, false, false, true, ci);
                }
            }
        } else {
            // Main diff between protected and private is that the project ID is readable by guest in protected, so set that once and apply privileges.
            // Other than that, nothing else is readable by guest in protected or private.
            final boolean readableByGuest = StringUtils.equals("protected", accessibility);
            setPermissionsInternal(false, guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", tag, false, readableByGuest, false, false, false, readableByGuest, ci);
            for (final ElementSecurity securedElement : securedElements) {
                final String elementName = securedElement.getElementName();
                if (securedElement.hasField(elementName + "/project") && securedElement.hasField(elementName + "/sharing/share/project")) {
                    setPermissionsInternal(false, guest, authenticatedUser, elementName, elementName + "/project", tag, false, false, false, false, false, true, ci);
                    setPermissionsInternal(false, guest, authenticatedUser, elementName, elementName + "/sharing/share/project", tag, false, false, false, false, false, true, ci);
                }
            }
        }

        ((XDATUser) authenticatedUser).resetCriteria();
        ((XDATUser) guest).resetCriteria();
        Users.getGuest(true);

        if (triggerEvent) {
            _eventService.triggerEvent(builder().typeAndId("xnat:projectData", tag).action(UPDATE).property("accessibility", accessibility).build());
        }

        return true;
    }

    private void setPermissionsInternal(final boolean triggerEvent, final UserI affected, final UserI authenticated, final String elementName, final String fieldName, final String fieldValue, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final EventMetaI ci) {
        try {
            final Optional<XdatElementAccess> optional = FluentIterable.from(((XDATUser) affected).getElementAccess()).firstMatch(new Predicate<XdatElementAccess>() {
                @Override
                public boolean apply(@Nullable final XdatElementAccess elementAccess) {
                    return elementAccess != null && StringUtils.equals(elementName, elementAccess.getElementName());
                }
            });

            final XdatElementAccess elementAccess;
            if (optional.isPresent()) {
                elementAccess = optional.get();
            } else {
                elementAccess = new XdatElementAccess(authenticated);
                elementAccess.setElementName(elementName);
                elementAccess.setProperty("xdat_user_xdat_user_id", affected.getID());
            }

            final boolean isAccessible = create || read || edit || delete || activate;

            final XdatFieldMappingSet fieldMappingSet = elementAccess.getOrCreateFieldMappingSet(authenticated);
            final XdatFieldMapping    fieldMapping    = getFieldMapping(authenticated, fieldMappingSet, fieldName, fieldValue, isAccessible);

            if (fieldMapping == null) {
                return;
            }

            if (!isAccessible) {
                final XFTItem item;
                if (fieldMappingSet.getAllow().size() == 1) {
                    item = fieldMappingSet.getItem();
                } else {
                    item = fieldMapping.getItem();
                }
                SaveItemHelper.authorizedDelete(item, authenticated, ci);
                if (triggerEvent) {
                    _eventService.triggerEvent(builder().item(item).action(DELETE).build());
                }
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
                _eventService.triggerEvent(builder().typeAndId(elementName, fieldValue).action(UPDATE).build());
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

    private XdatFieldMapping getFieldMapping(final UserI user, final XdatFieldMappingSet fieldMappingSet, final String fieldName, final String fieldValue, final boolean isAccessible) {
        final Optional<XdatFieldMapping> optional = FluentIterable.from(fieldMappingSet.getAllow()).firstMatch(new Predicate<XdatFieldMapping>() {
            @Override
            public boolean apply(@Nullable final XdatFieldMapping fieldMapping) {
                return fieldMapping != null && StringUtils.equals(fieldValue, fieldMapping.getFieldValue()) && StringUtils.equals(fieldName, fieldMapping.getField());
            }
        });

        if (optional.isPresent()) {
            return optional.get();
        }

        return isAccessible ? new XdatFieldMapping(user) : null;
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

    private static class PermissionsBean {
        PermissionsBean(final UserI affected, final UserI authenticated, final String elementName, final String fieldName, final String fieldValue, final Boolean create, final Boolean read, final Boolean delete, final Boolean edit, final Boolean activate, final boolean activateChanges, final EventMetaI ci) {
            _affected = affected;
            _authenticated = authenticated;
            _elementName = elementName;
            _fieldName = fieldName;
            _fieldValue = fieldValue;
            _create = create;
            _read = read;
            _delete = delete;
            _edit = edit;
            _activate = activate;
            _activateChanges = activateChanges;
            _ci = ci;
        }

        final UserI      _affected;
        final UserI      _authenticated;
        final String     _elementName;
        final String     _fieldName;
        final String     _fieldValue;
        final Boolean    _create;
        final Boolean    _read;
        final Boolean    _delete;
        final Boolean    _edit;
        final Boolean    _activate;
        final boolean    _activateChanges;
        final EventMetaI _ci;
    }

    private static final String QUERY_USER_READABLE_ELEMENTS = "SELECT " +
                                                               "  xea.element_name, " +
                                                               "  xfm.field, " +
                                                               "  xfm.field_value " +
                                                               "FROM xdat_user u " +
                                                               "  LEFT JOIN xdat_user_groupid map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
                                                               "  LEFT JOIN xdat_usergroup usergroup on map.groupid = usergroup.id " +
                                                               "  LEFT JOIN xdat_element_access xea on (usergroup.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = xea.xdat_user_xdat_user_id) " +
                                                               "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                               "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                               "WHERE " +
                                                               "  xfm.field_value != '*' AND " +
                                                               "  xfm.read_element = 1 AND " +
                                                               "  u.login IN ('guest', '%s')";
    private static final String GUEST_USERNAME               = "guest";

    private final NrgEventService            _eventService;

    private GroupsAndPermissionsCache _cache;
    private UserI                     _guest;
}
