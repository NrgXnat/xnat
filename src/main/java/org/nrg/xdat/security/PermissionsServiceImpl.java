/*
 * core: org.nrg.xdat.security.PermissionsServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unused", "DuplicateThrows"})
public class PermissionsServiceImpl implements PermissionsServiceI {
	@Override
    public List<PermissionCriteriaI> getPermissionsForUser(UserI user, String dataType){
        return ((XDATUser)user).getPermissionsByDataType(dataType);
    }
    
	@Override
	public CriteriaCollection getCriteriaForXDATRead(UserI user,	SchemaElement root) throws IllegalAccessException, Exception {

		if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), SecurityManager.READ)) {
            return null;
        } else {
        	List<PermissionCriteriaI> criteria =getPermissionsForUser(user, root.getFullXMLName());

            CriteriaCollection cc = new CriteriaCollection("OR");
            for(PermissionCriteriaI crit: criteria){
            	if(crit.getRead()){
            		cc.add(DisplayCriteria.buildCriteria(root,crit));
            	}
            }

            if (cc.numClauses() == 0) {
                return null;
            }

            return cc;
        }
	}

	@Override
	public CriteriaCollection getCriteriaForXFTRead(UserI user,	SchemaElementI root) throws Exception {
		if (!ElementSecurity.IsSecureElement(root.getFullXMLName(), SecurityManager.READ)) {
            return null;
        } else {
        	List<PermissionCriteriaI> criteria =getPermissionsForUser(user, root.getFullXMLName());

            CriteriaCollection cc = new CriteriaCollection("OR");
            for(PermissionCriteriaI crit: criteria){
            	if(crit.getRead()){
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
        if (ElementSecurity.IsInSecureElement(root.getFullXMLName())) {
            return true;
        } else {
        	List<PermissionCriteriaI> criteria =getPermissionsForUser(user, root.getFullXMLName());

            for(PermissionCriteriaI crit: criteria){
            	if(crit.canAccess(action, values)){
            		return true;
            	}
            }
        }

        return false;
    }

    private boolean securityCheckByXMLPath(UserI user, String action, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user,action, root, values);
    }

	@Override
    public boolean canCreate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user,SecurityManager.CREATE, root, values);
    }

	@Override
    public boolean canRead(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user,SecurityManager.READ, root, values);
    }

	@Override
    public boolean canEdit(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user,SecurityManager.EDIT, root, values);
    }

	@Override
    public boolean canActivate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user,SecurityManager.ACTIVATE, root, values);
    }

	@Override
    public boolean canDelete(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(user,SecurityManager.DELETE, root, values);
    }

    @Override
    public String canStoreItem(UserI user, ItemI item, boolean allowDataDeletion) throws InvalidItemException, Exception {
        String invalidItemName;
        try {
            if (!canCreate(user,item)) {
                return item.getXSIType();
            }

            if (allowDataDeletion) {
                //this should check items stored in db, rather then just local hash
                Iterator iter = item.getChildItems().iterator();
                while (iter.hasNext()) {
                    ItemI child = (ItemI) iter.next();
                    invalidItemName = canStoreItem(user, child, allowDataDeletion);
                    if (invalidItemName != null) {
                        return invalidItemName;
                    }
                }
            } else {
                Iterator iter = item.getChildItems().iterator();
                while (iter.hasNext()) {
                    ItemI child = (ItemI) iter.next();
                    invalidItemName = canStoreItem(user, child, allowDataDeletion);
                    if (invalidItemName != null) {
                        return invalidItemName;
                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        }

        return null;
    }
    
    @Override
    public ItemI secureItem(UserI user, ItemI item) throws IllegalAccessException, org.nrg.xft.exception.MetaDataException {
        try {
            //check readability
            boolean isOK = canRead(user, item);

            //check quarantine
            if (isOK) {
                // If this item has a metadata element (which stores active status) and the user can't activate this...
                if (item.getProperty("meta") != null && !canActivate(user, item)) {
                    // Then check to see if it's not active. You can't access inactive things.
                    if (!item.isActive()) {
                        isOK = false;
                        throw new IllegalAccessException("Access Denied: This data is in quarantine.");
                    }
                }
            }

            if (isOK) {
                ArrayList invalidItems = new ArrayList();

                Iterator iter = item.getChildItems().iterator();
                while (iter.hasNext()) {
                    ItemI child = (ItemI) iter.next();
                    boolean b = canRead(user, child);
                    if (b) {
                        secureChild(user, child);
                    } else {
                        invalidItems.add(child);
                    }
                }

                if (invalidItems.size() > 0) {
                    Iterator invalids = invalidItems.iterator();
                    while (invalids.hasNext()) {
                        XFTItem invalid = (XFTItem) invalids.next();
                        XFTItem parent = (XFTItem) item;
                        parent.removeItem(invalid);
                        item = parent;
                    }
                }
            } else {
                throw new IllegalAccessException("Access Denied: Current user does not have permission to read this data.");
            }
        } catch (InvalidItemException e) {
            logger.error("", e);
        } catch (MetaDataException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

        return item;
    }

    private ItemI secureChild(UserI user, ItemI item) throws Exception {
        List<ItemI> invalidItems = new ArrayList<ItemI>();

        for (final Object o : item.getChildItems()) {
            ItemI child = (ItemI) o;
            boolean b = canRead(user, child);

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
        return item;
    }

    
    @Override
    public boolean can(UserI user, ItemI item, String action) throws InvalidItemException, Exception {
        if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
            return false;
        }
        final String xsiType = item.getXSIType();
        if (!ElementSecurity.HasDefinedElementSecurity(xsiType)) {
            return true;
        } else if (ElementSecurity.IsInSecureElement(xsiType)) {
            return true;
        } else {
            final ElementSecurity elementSecurity = ElementSecurity.GetElementSecurity(xsiType);
            if (elementSecurity.isSecure(action)) {
                final SchemaElement schemaElement = SchemaElement.GetElement(xsiType);
                final SecurityValues securityValues = item.getItem().getSecurityValues();
                final boolean isOK = securityCheckByXMLPath(user, action, schemaElement, securityValues);
                if (!isOK) {
                    logger.info("User {} doesn't have permission to {} the schema element {} for XSI type {}. The security values are: {}.",
                                user.getUsername(),
                                action,
                                schemaElement.getFormattedName(),
                                xsiType,
                                Joiner.on(", ").withKeyValueSeparator(" : ").join(securityValues.getHash()));
                }
                return isOK;
            } else {
                return true;
            }
        }
    }
    
    @Override
    public boolean canRead(UserI user, ItemI item) throws InvalidItemException, Exception {
    	return can(user,item,SecurityManager.READ);
    }
    
    @Override
    public boolean canEdit(UserI user, ItemI item) throws InvalidItemException, Exception {
    	return can(user,item,SecurityManager.EDIT);
    }
    
    @Override
    public boolean canCreate(UserI user, ItemI item) throws Exception {
    	return can(user,item,SecurityManager.CREATE);
    }
    
    @Override
    public boolean canActivate(UserI user, ItemI item) throws InvalidItemException, Exception {
    	return can(user,item,SecurityManager.ACTIVATE);
    }
    
    @Override
    public boolean canDelete(UserI user, ItemI item) throws InvalidItemException, Exception {
    	return can(user,item,SecurityManager.DELETE);
    }

    
    @Override
    public boolean can(UserI user, String xmlPath, Object value, String action) throws Exception {
        if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
            return false;
        }
        String rootElement = XftStringUtils.GetRootElementName(xmlPath);
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(rootElement)) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(rootElement)) {
            isOK = true;
        } else {
            SecurityValues sv = new SecurityValues();
            sv.put(xmlPath, value.toString());
            if (securityCheckByXMLPath(user,action, SchemaElement.GetElement(rootElement), sv)) {
                isOK = true;
            } else {
                isOK = false;
            }
        }
        return isOK;
    }
    
    @Override
    public boolean canRead(UserI user, String xmlPath, Object value) throws Exception {
    	return can(user,xmlPath,value,SecurityManager.READ);
    }
    
    @Override
    public boolean canEdit(UserI user, String xmlPath, Object value) throws Exception {
    	return can(user,xmlPath,value,SecurityManager.EDIT);
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
        List<Object> values = getAllowedValues(user,elementName, xmlPath, action);
        return values != null && values.size() > 0;
    }


    @Override
    public List<Object> getAllowedValues(UserI user, String elementName, String xmlPath, String action) {
    	List allowedValues = Lists.newArrayList();
    	
    	try {
			SchemaElement root=SchemaElement.GetElement(elementName);

			if (ElementSecurity.IsSecureElement(root.getFullXMLName(), action)) {
	        	List<PermissionCriteriaI> criteria =getPermissionsForUser(user, root.getFullXMLName());

			    CriteriaCollection cc = new CriteriaCollection("OR");
			    for(PermissionCriteriaI crit: criteria){
			    	if(crit.getAction(action) && !allowedValues.contains(crit.getFieldValue())){
			    		allowedValues.add(crit.getFieldValue());
			    	}
			    }
			} else {
			    allowedValues = GenericWrapperElement.GetUniqueValuesForField(xmlPath);
			}
			
	        Collections.sort(allowedValues);
		} catch (Exception e) {
			logger.error("",e);
		}

        return allowedValues;
    }

    @Override
    public Map<String,Object> getAllowedValues(UserI user, String elementName, String action) {
    	Map<String,Object> allowedValues = Maps.newHashMap();
    	
    	try {
			SchemaElement root=SchemaElement.GetElement(elementName);

			if (ElementSecurity.IsSecureElement(root.getFullXMLName(), action)) {
	        	List<PermissionCriteriaI> criteria =getPermissionsForUser(user, root.getFullXMLName());

			    CriteriaCollection cc = new CriteriaCollection("OR");
			    for(PermissionCriteriaI crit: criteria){
			    	if(crit.getAction(action)){
			    		allowedValues.put(crit.getField(),crit.getFieldValue());
			    	}
			    }
			}
		} catch (Exception e) {
			logger.error("",e);
		}

        return allowedValues;
    }

	@Override
	public boolean canAny(UserI user, String elementName, String action) {
		if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
            return false;
        }
        // consider caching, but this should not hit the database on every call anyways.
        Map<String,Object> values = getAllowedValues(user,elementName, action);
        return (values != null && values.size() > 0);
	}

	@Override
	public void setPermissions(UserI effected, UserI authenticated,	String elementName, String psf, String value, Boolean create,Boolean read, Boolean delete, Boolean edit, Boolean activate,	boolean activateChanges, EventMetaI ci) {
        try {
            ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);

            XdatElementAccess ea = null;
            Iterator eams = ((XDATUser)effected).getElementAccess().iterator();
            while (eams.hasNext()) {
                XdatElementAccess temp = (XdatElementAccess) eams.next();
                if (temp.getElementName().equals(elementName)) {
                    ea = temp;
                    break;
                }
            }

            if (ea == null) {
                ea = new XdatElementAccess((UserI) authenticated);
                ea.setElementName(elementName);
                ea.setProperty("xdat_user_xdat_user_id", effected.getID());
            }

            XdatFieldMappingSet fms = null;
            ArrayList al = ea.getPermissions_allowSet();
            if (al.size() > 0) {
                fms = (XdatFieldMappingSet) ea.getPermissions_allowSet().get(0);
            } else {
                fms = new XdatFieldMappingSet((UserI) authenticated);
                fms.setMethod("OR");
                ea.setPermissions_allowSet(fms);
            }

            XdatFieldMapping fm = null;

            Iterator iter = fms.getAllow().iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o instanceof XdatFieldMapping) {
                    if (((XdatFieldMapping) o).getFieldValue().equals(value) && ((XdatFieldMapping) o).getField().equals(psf)) {
                        fm = (XdatFieldMapping) o;
                    }
                }
            }

            if (fm == null) {
                if (create || read || edit || delete || activate)
                    fm = new XdatFieldMapping((UserI) authenticated);
                else
                    return;
            } else if (!(create || read || edit || delete || activate)) {
                if (fms.getAllow().size() == 1) {
                    SaveItemHelper.authorizedDelete(fms.getItem(), authenticated,ci);
                    return;
                } else {
                    SaveItemHelper.authorizedDelete(fm.getItem(), authenticated,ci);
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
                    SaveItemHelper.authorizedSave(fm, authenticated, true, false, true, false,ci);
                    fm.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fm, authenticated, true, false, false, false,ci);
                }
            } else if (ea.getXdatElementAccessId() != null) {
                fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fms, authenticated, true, false, true, false,ci);
                    fms.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fms, authenticated, true, false, false, false,ci);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(ea, authenticated, true, false, true, false,ci);
                    ea.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(ea, authenticated, true, false, false, false,ci);
                }
                ((XDATUser)effected).setElementAccess(ea);
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (InvalidValueException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
	}

	@Override
	public boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, UserI authenticatedUser, EventMetaI ci) throws Exception {
		ArrayList<ElementSecurity> securedElements = ElementSecurity.GetSecureElements();

        UserI guest=Users.getGuest();

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
        
        ((XDATUser)authenticatedUser).resetCriteria();
        Users.getGuest(true);
        return true;
	}

	@Override
	public List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType) {
		return ((UserGroup)group).getPermissionsByDataType(dataType);
	}

	@Override
	public Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group) {
		return ((UserGroup)group).getAllPermissions();
	}

	@Override
	public void setPermissionsForGroup(UserGroupI group, List<PermissionCriteriaI> criteria, EventMetaI meta, UserI authenticatedUser) throws Exception {
		for(PermissionCriteriaI crit:criteria){
			((UserGroup)group).addPermission(crit.getElementName(), crit, authenticatedUser);
		}
	}

	@Override
	public String getUserPermissionsSQL(UserI user) {
		return String.format("SELECT xea.element_name, xfm.field, xfm.field_value FROM xdat_user u JOIN xdat_user_groupID map ON u.xdat_user_id=map.groups_groupid_xdat_user_xdat_user_id JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND read_element=1 AND field_value!=''and field !='' WHERE u.login='guest' UNION SELECT xea.element_name, xfm.field, xfm.field_value FROM xdat_user_groupID map JOIN xdat_userGroup gp ON map.groupid=gp.id JOIN xdat_element_access xea ON gp.xdat_usergroup_id=xea.xdat_usergroup_xdat_usergroup_id JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id=xfms.permissions_allow_set_xdat_elem_xdat_element_access_id JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND read_element=1 AND field_value!=''and field !='' WHERE map.groups_groupid_xdat_user_xdat_user_id=%s",user.getID());
	}

    private static final Logger logger = LoggerFactory.getLogger(PermissionsServiceImpl.class);
}
