/*
 * core: org.nrg.xdat.security.PermissionsServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.accessibility.internal.resources.accessibility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
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
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.PermissionCriteria.dumpCriteriaList;

@SuppressWarnings({"unused", "DuplicateThrows"})
@Service
@Slf4j
public class PermissionsServiceImpl implements PermissionsServiceI {
    @Autowired
    public PermissionsServiceImpl(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    @Override
    public List<PermissionCriteriaI> getPermissionsForUser(UserI user, String dataType) {
        return ImmutableList.copyOf(((XDATUser) user).getPermissionsByDataType(dataType));
    }

    @Override
    public CriteriaCollection getCriteriaForXDATRead(UserI user, SchemaElement root) throws IllegalAccessException, Exception {

        if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), SecurityManager.READ)) {
            return null;
        } else {
            List<PermissionCriteriaI> criteria = getPermissionsForUser(user, root.getFullXMLName());

            CriteriaCollection cc = new CriteriaCollection("OR");
            for (PermissionCriteriaI crit : criteria) {
                if (crit.getRead()) {
                    cc.add(DisplayCriteria.buildCriteria(root, crit));
                }
            }

            if (cc.numClauses() == 0) {
                return null;
            }

            return cc;
        }
    }

    @Override
    public CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI root) throws Exception {
        if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), SecurityManager.READ)) {
            return null;
        } else {
            List<PermissionCriteriaI> criteria = getPermissionsForUser(user, root.getFullXMLName());

            CriteriaCollection cc = new CriteriaCollection("OR");
            for (PermissionCriteriaI crit : criteria) {
                if (crit.getRead()) {
                    cc.add(SearchCriteria.buildCriteria(crit));
                }
            }

            if (cc.numClauses() == 0) {
                return null;
            }

            return cc;
        }
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
                    log.error("{}: No permission criteria found for user {} with action {} on the schema element {} and the following security values: {}.",
                              (new Exception()).getStackTrace()[0].toString(),
                              username,
                              action,
                              root.getFormattedName(),
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
        try {
            // Check readability
            if (!canRead(user, item)) {
                final String itemId  = getItemIId(item);
                final String message = String.format("User '%s' does not have read access to the %s instance with ID %s", user.getUsername(), item.getXSIType(), itemId);
                log.error(message);
                throw new IllegalAccessException("Access Denied: " + message);
            }

            // Check quarantine: if this item has a metadata element (which stores active status) and the user can't
            // activate this...
            if (item.getProperty("meta") != null && !canActivate(user, item)) {
                // Then check to see if it's not active. You can't access inactive things.
                if (!item.isActive()) {
                    final String itemId  = getItemIId(item);
                    final String message = String.format("The %s item with ID %s is in quarantine and the user %s does not have permission to activate this data type.", item.getXSIType(), itemId, user.getUsername());
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

            if (invalidItems.size() > 0) {
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
            log.error("", e);
        }

        return item;
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
            if (item instanceof XFTItem && ((XFTItem) item).instanceOf("xdat:user")) {
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
        String  rootElement = XftStringUtils.GetRootElementName(xmlPath);
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
        List<Object> values = getAllowedValues(user, elementName, xmlPath, action);
        return values != null && values.size() > 0;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getAllowedValues(UserI user, String elementName, String xmlPath, String action) {
        final List allowedValues = new ArrayList();

        try {
            final String rootXmlName = SchemaElement.GetElement(elementName).getFullXMLName();
            if (ElementSecurity.IsSecureElement(rootXmlName, action)) {

                final List<PermissionCriteriaI> criteria = getPermissionsForUser(user, rootXmlName);

                CriteriaCollection cc = new CriteriaCollection("OR");
                for (PermissionCriteriaI crit : criteria) {
                    if (crit.getAction(action) && !allowedValues.contains(crit.getFieldValue())) {
                        allowedValues.add(crit.getFieldValue());
                    }
                }
            } else {
                allowedValues.addAll(GenericWrapperElement.GetUniqueValuesForField(xmlPath));
            }

            Collections.sort(allowedValues);
        } catch (Exception e) {
            log.error("", e);
        }

        return ImmutableList.copyOf(allowedValues);
    }

    @Override
    public Map<String, Object> getAllowedValues(UserI user, String elementName, String action) {
        final Map<String, Object> allowedValues = Maps.newHashMap();

        try {
            SchemaElement root = SchemaElement.GetElement(elementName);

            if (ElementSecurity.IsSecureElement(root.getFullXMLName(), action)) {
                List<PermissionCriteriaI> criteria = getPermissionsForUser(user, root.getFullXMLName());

                CriteriaCollection cc = new CriteriaCollection("OR");
                for (PermissionCriteriaI crit : criteria) {
                    if (crit.getAction(action)) {
                        allowedValues.put(crit.getField(), crit.getFieldValue());
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return ImmutableMap.copyOf(allowedValues);
    }

    @Override
    public boolean canAny(UserI user, String elementName, String action) {
        if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        Map<String, Object> values = getAllowedValues(user, elementName, action);
        return (values != null && values.size() > 0);
    }

    @Override
    public void setPermissions(UserI effected, UserI authenticated, String elementName, String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, EventMetaI ci) {
        try {
            ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);

            XdatElementAccess ea   = null;
            for (final XdatElementAccess temp : ((XDATUser) effected).getElementAccess()) {
                if (temp.getElementName().equals(elementName)) {
                    ea = temp;
                    break;
                }
            }

            if (ea == null) {
                ea = new XdatElementAccess(authenticated);
                ea.setElementName(elementName);
                ea.setProperty("xdat_user_xdat_user_id", effected.getID());
            }

            XdatFieldMappingSet fms;
            ArrayList           al  = ea.getPermissions_allowSet();
            if (al.size() > 0) {
                fms = ea.getPermissions_allowSet().get(0);
            } else {
                fms = new XdatFieldMappingSet(authenticated);
                fms.setMethod("OR");
                ea.setPermissions_allowSet(fms);
            }

            XdatFieldMapping fm = null;

            for (final XdatFieldMapping fieldMapping : fms.getAllow()) {
                if (fieldMapping.getFieldValue().equals(value) && fieldMapping.getField().equals(psf)) {
                    fm = fieldMapping;
                }
            }

            if (fm == null) {
                if (create || read || edit || delete || activate) {
                    fm = new XdatFieldMapping(authenticated);
                } else {
                    return;
                }
            } else if (!(create || read || edit || delete || activate)) {
                if (fms.getAllow().size() == 1) {
                    SaveItemHelper.authorizedDelete(fms.getItem(), authenticated, ci);
                    return;
                } else {
                    SaveItemHelper.authorizedDelete(fm.getItem(), authenticated, ci);
                    return;
                }
            }


            fm.setField(psf);
            fm.setFieldValue(value);

            fm.setCreateElement(create);
            fm.setReadElement(read);
            fm.setEditElement(edit);
            fm.setDeleteElement(delete);
            fm.setActiveElement(activate);
            fm.setComparisonType("equals");
            fms.setAllow(fm);

            if (fms.getXdatFieldMappingSetId() != null) {
                fm.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fms.getXdatFieldMappingSetId());

                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fm, authenticated, true, false, true, false, ci);
                    fm.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fm, authenticated, true, false, false, false, ci);
                }
            } else if (ea.getXdatElementAccessId() != null) {
                fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fms, authenticated, true, false, true, false, ci);
                    fms.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fms, authenticated, true, false, false, false, ci);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(ea, authenticated, true, false, true, false, ci);
                    ea.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(ea, authenticated, true, false, false, false, ci);
                }
                ((XDATUser) effected).setElementAccess(ea);
            }
        } catch (XFTInitException e) {
            log.error("An error occurred initializing XFT", e);
        } catch (ElementNotFoundException e) {
            log.error("Did not find the requested element on the item", e);
        } catch (FieldNotFoundException e) {
            log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
        } catch (InvalidValueException e) {
            log.error("Invalid value specified: {}", effected.getID(), e);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, UserI authenticatedUser, EventMetaI ci) throws Exception {
        ArrayList<ElementSecurity> securedElements = ElementSecurity.GetSecureElements();

        UserI guest = Users.getGuest();

        switch (accessibility) {
            case "public":
                Permissions.setPermissions(guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", tag, false, true, false, false, true, true, ci);

                for (ElementSecurity es : securedElements) {
                    if (es.hasField(es.getElementName() + "/project") && es.hasField(es.getElementName() + "/sharing/share/project")) {
                        Permissions.setPermissions(guest, authenticatedUser, es.getElementName(), es.getElementName() + "/project", tag, false, true, false, false, true, true, ci);
                        Permissions.setPermissions(guest, authenticatedUser, es.getElementName(), es.getElementName() + "/sharing/share/project", tag, false, false, false, false, false, true, ci);
                    }
                }
                break;
            case "protected":
                Permissions.setPermissions(guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", tag, false, true, false, false, false, true, ci);
                for (ElementSecurity es : securedElements) {
                    if (es.hasField(es.getElementName() + "/project") && es.hasField(es.getElementName() + "/sharing/share/project")) {
                        Permissions.setPermissions(guest, authenticatedUser, es.getElementName(), es.getElementName() + "/project", tag, false, false, false, false, false, true, ci);
                        Permissions.setPermissions(guest, authenticatedUser, es.getElementName(), es.getElementName() + "/sharing/share/project", tag, false, false, false, false, false, true, ci);
                    }
                }
                break;
            default: // "private"
                Permissions.setPermissions(guest, authenticatedUser, "xnat:projectData", "xnat:projectData/ID", tag, false, false, false, false, false, true, ci);
                for (ElementSecurity es : securedElements) {
                    if (es.hasField(es.getElementName() + "/project") && es.hasField(es.getElementName() + "/sharing/share/project")) {
                        Permissions.setPermissions(guest, authenticatedUser, es.getElementName(), es.getElementName() + "/project", tag, false, false, false, false, false, true, ci);
                        Permissions.setPermissions(guest, authenticatedUser, es.getElementName(), es.getElementName() + "/sharing/share/project", tag, false, false, false, false, false, true, ci);
                    }
                }
                break;
        }

        ((XDATUser) authenticatedUser).resetCriteria();
        Users.getGuest(true);
        return true;
    }

    @Override
    public List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType) {
        return ImmutableList.copyOf(((UserGroup) group).getPermissionsByDataType(dataType));
    }

    @Override
    public Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group) {
        return ImmutableMap.copyOf(((UserGroup) group).getAllPermissions());
    }

    @Override
    public void setPermissionsForGroup(UserGroupI group, List<PermissionCriteriaI> criteria, EventMetaI meta, UserI authenticatedUser) throws Exception {
        for (PermissionCriteriaI crit : criteria) {
            ((UserGroup) group).addPermission(crit.getElementName(), crit, authenticatedUser);
        }
    }

    @Override
    public String getUserPermissionsSQL(UserI user) {
        return String.format("SELECT xea.element_name, xfm.field, xfm.field_value FROM xdat_user u JOIN xdat_user_groupID map ON u.xdat_user_id=map.groups_groupid_xdat_user_xdat_user_id JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND read_element=1 AND field_value!=''and field !='' WHERE u.login='guest' UNION SELECT xea.element_name, xfm.field, xfm.field_value FROM xdat_user_groupID map JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND read_element=1 AND field_value!=''and field !='' WHERE map.groups_groupid_xdat_user_xdat_user_id=%s", user.getID());
    }

    @Override
    public List<String> getUserReadableProjects(final UserI user) {
        return _template.queryForList(QUERY_READABLE_PROJECTS, new MapSqlParameterSource("username", user.getUsername()), String.class);
    }

    @Override
    public List<String> getUserEditableProjects(final UserI user) {
        return _template.queryForList(QUERY_EDITABLE_PROJECTS, new MapSqlParameterSource("username", user.getUsername()), String.class);
    }

    @Override
    public List<String> getUserOwnedProjects(final UserI user) {
        return _template.queryForList(QUERY_OWNED_PROJECTS, new MapSqlParameterSource("username", user.getUsername()), String.class);
    }

    private static final String QUERY_USER_PROJECTS     = "SELECT DISTINCT xfm.field_value AS project " +
                                                          "FROM xdat_field_mapping xfm " +
                                                          "  LEFT JOIN xdat_field_mapping_set xfms ON xfm.xdat_field_mapping_set_xdat_field_mapping_set_id = xfms.xdat_field_mapping_set_id " +
                                                          "  LEFT JOIN xdat_element_access xea ON xfms.permissions_allow_set_xdat_elem_xdat_element_access_id = xea.xdat_element_access_id " +
                                                          "  LEFT JOIN xdat_usergroup usergroup ON xea.xdat_usergroup_xdat_usergroup_id = usergroup.xdat_usergroup_id " +
                                                          "  LEFT JOIN xdat_user_groupid groupid ON usergroup.id = groupid.groupid " +
                                                          "  LEFT JOIN xdat_user xu ON groupid.groups_groupid_xdat_user_xdat_user_id = xu.xdat_user_id " +
                                                          "WHERE " +
                                                          "  xu.login = :username " +
                                                          "  AND xea.element_name = 'xnat:projectData' " +
                                                          "  AND xfm.%s = 1 " +
                                                          "  AND xfm.comparison_type = 'equals' " +
                                                          "  AND xfm.field_value != '*' " +
                                                          "ORDER BY project";
    private static final String QUERY_OWNED_PROJECTS    = String.format(QUERY_USER_PROJECTS, "delete_element");
    private static final String QUERY_EDITABLE_PROJECTS = String.format(QUERY_USER_PROJECTS, "edit_element");
    private static final String QUERY_READABLE_PROJECTS = "SELECT DISTINCT xfm.field_value AS project " +
                                                          "FROM xdat_field_mapping xfm " +
                                                          "  LEFT JOIN xdat_field_mapping_set xfms ON xfm.xdat_field_mapping_set_xdat_field_mapping_set_id = xfms.xdat_field_mapping_set_id " +
                                                          "  LEFT JOIN xdat_element_access xea ON xfms.permissions_allow_set_xdat_elem_xdat_element_access_id = xea.xdat_element_access_id " +
                                                          "  LEFT JOIN xdat_user xu ON xea.xdat_user_xdat_user_id = xu.xdat_user_id " +
                                                          "  LEFT JOIN xdat_user_groupid xugid ON xu.xdat_user_id = xugid.groups_groupid_xdat_user_xdat_user_id " +
                                                          "  LEFT JOIN xdat_usergroup xug ON xugid.groupid = xug.id " +
                                                          "WHERE " +
                                                          "  xu.login = 'guest' " +
                                                          "  AND xea.element_name = 'xnat:projectData' " +
                                                          "  AND xfm.create_element = 0 " +
                                                          "  AND xfm.read_element = 1 " +
                                                          "  AND xfm.edit_element = 0 " +
                                                          "  AND xfm.delete_element = 0 " +
                                                          "  AND xfm.active_element = 0 " +
                                                          "  AND xfm.comparison_type = 'equals' " +
                                                          "UNION DISTINCT " + String.format(QUERY_USER_PROJECTS, "read_element");

    private final NamedParameterJdbcTemplate _template;
}
