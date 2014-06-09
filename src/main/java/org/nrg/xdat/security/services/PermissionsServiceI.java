package org.nrg.xdat.security.services;

import java.util.List;
import java.util.Map;

import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;

public interface PermissionsServiceI {
	/**
	 * Get all active permission criteria for this user account (including group permissions, etc).
	 * @param user
	 * @param dataType
	 * @return
	 */
	public abstract List<PermissionCriteriaI> getPermissionsForUser(UserI user, String dataType);
	
	/**
	 * Get current XDAT criteria objects for current permission settings.  The XDAT criteria are used within the search engine to build long ugly WHERE clauses which limit the users access.  We'll want to refactor this if it isn't rewritten.
	 * 
	 * @param user
	 * @param rootElement
	 * @return
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	public CriteriaCollection getCriteriaForXDATRead(UserI user, SchemaElement rootElement) throws IllegalAccessException, Exception;
	
	/**
	 * Get current XFT criteria used when querying XFT items out of the database.
	 * 
	 * @param user
	 * @param rootElement
	 * @return
	 * @throws Exception
	 */
	public CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI rootElement) throws Exception;
	
	/**
	 * Can the user create an element based on a collection of key/value pairs {@link SecurityValues}.
	 * 
	 * This is similar to running canCreate(user, String, Object) for each row in the SecurityValues object.
	 * 
	 * @param user
	 * @param root
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public boolean canCreate(UserI user, SchemaElementI root, SecurityValues values) throws Exception;
	
	/**
	 * Can the user read an element based on a collection of key/value pairs {@link SecurityValues}.
	 * 
	 * This is similar to running canRead(user, String, Object) for each row in the SecurityValues object.
	 * 
	 * @param user
	 * @param root
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public boolean canRead(UserI user, SchemaElementI root, SecurityValues values) throws Exception;
	
	/**
	 * Can the user edit an element based on a collection of key/value pairs {@link SecurityValues}.
	 * 
	 * This is similar to running canEdit(user, String, Object) for each row in the SecurityValues object.
	 * 
	 * @param user
	 * @param root
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public boolean canEdit(UserI user, SchemaElementI root, SecurityValues values) throws Exception;
	
	/**
	 * Can the user activate an element based on a collection of key/value pairs {@link SecurityValues}.
	 * 
	 * This is similar to running canActivate(user, String, Object) for each row in the SecurityValues object.
	 * 
	 * @param user
	 * @param root
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public boolean canActivate(UserI user, SchemaElementI root, SecurityValues values) throws Exception;
	
	/**
	 * Can the user delete an element based on a collection of key/value pairs {@link SecurityValues}.
	 * 
	 * This is similar to running canDelete(user, String, Object) for each row in the SecurityValues object.
	 * 
	 * @param user
	 * @param root
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public boolean canDelete(UserI user, SchemaElementI root, SecurityValues values) throws Exception;

	/**
	 * Can the user read the specified item
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public boolean canRead(UserI user, ItemI item) throws InvalidItemException,Exception;
	
	/**
	 * Can the user edit the specified item
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public boolean canEdit(UserI user, ItemI item) throws InvalidItemException,Exception;
	
	/**
	 * Can the user create the specified item
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public boolean canCreate(UserI user, ItemI item) throws InvalidItemException,Exception;
	
	/**
	 * Can the user activate the specified item
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public boolean canActivate(UserI user, ItemI item) throws InvalidItemException,Exception;
	
	/**
	 * Can the user delete the specified item
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public boolean canDelete(UserI user, ItemI item) throws InvalidItemException,Exception;

	/**
	 * Can the user read any of the given elementName/xmlPath/action combination
	 * 
	 * @param user
	 * @param elementName
	 * @param xmlPath
	 * @param action
	 * @return
	 */
	public boolean canAny(UserI user, String elementName, String xmlPath, String action);
	
	/**
	 * Can the user read any of the given elementName/action combination
	 * 
	 * @param user
	 * @param elementName
	 * @param action
	 * @return
	 */
	public boolean canAny(UserI user, String elementName, String action);
	
	/**
	 * Can the user do the specified action for the String/Object pair
	 * 
	 * @param user
	 * @param xmlPath
	 * @param value
	 * @param action
	 * @return
	 * @throws Exception
	 */
	public boolean can(UserI user, String xmlPath, Object value, String action) throws Exception;
	
	/**
	 * Can the user do the specified action for the item
	 * 
	 * @param user
	 * @param item
	 * @param action
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public boolean can(UserI user, ItemI item,String action) throws InvalidItemException,Exception;
	
	/**
	 * Can the user read items for the String/Object pair
	 * 
	 * @param user
	 * @param xmlPath
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean canRead(UserI user, String xmlPath, Object value) throws Exception;
	
	/**
	 * Can the user edit items for the String/Object pair
	 * 
	 * @param user
	 * @param xmlPath
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean canEdit(UserI user, String xmlPath, Object value) throws Exception;
	
	/**
	 * Can the user create items for the String/Object pair
	 * 
	 * @param user
	 * @param xmlPath
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean canCreate(UserI user, String xmlPath, Object value) throws Exception;
	
	/**
	 * Can the user activate items for the String/Object pair
	 * 
	 * @param user
	 * @param xmlPath
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean canActivate(UserI user, String xmlPath, Object value) throws Exception;
	
	/**
	 * Can the user delete items for the String/Object pair
	 * 
	 * @param user
	 * @param xmlPath
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean canDelete(UserI user, String xmlPath, Object value) throws Exception;
	
	/**
	 * Can the user create/update this item and potentially all of its descendents
	 * 
	 * @param user
	 * @param item
	 * @param descend
	 * @return
	 * @throws InvalidItemException
	 * @throws Exception
	 */
	public String canStoreItem(UserI user, ItemI item,boolean descend) throws InvalidItemException,Exception;
	
	/**
	 * Review the passed item and remove any child items that this user doesn't have access to.
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws IllegalAccessException
	 * @throws org.nrg.xft.exception.MetaDataException
	 */
	public ItemI secureItem(UserI user, ItemI item) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException;

	/**
	 * Get the values that this user can do the specified action on for the given element/xmlpath combo
	 * 
	 * @param user
	 * @param elementName
	 * @param xmlPath
	 * @param action
	 * @return
	 */
	public List<Object> getAllowedValues(UserI user, String elementName, String xmlPath, String action) ;
	
	/**
	 * Get the xmlpath/value combos that this user can do the specified action on for the given element
	 * @param user
	 * @param elementName
	 * @param action
	 * @return
	 */
	public Map<String,Object> getAllowedValues(UserI user, String elementName, String action) ;

	/**
	 * initialize or update the permissions of the 'effected' user based on thee parameters
	 * 
	 * @param effected
	 * @param authenticated
	 * @param elementName
	 * @param psf
	 * @param value
	 * @param create
	 * @param read
	 * @param delete
	 * @param edit
	 * @param activate
	 * @param activateChanges
	 * @param ci
	 */
	public void setPermissions(UserI effected, UserI authenticated, String elementName,String psf,String value,Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,EventMetaI ci);
	
	/**
	 * Set the accessibility (public/protected/private) of the entity represented by the tag
	 * 
	 * @param tag
	 * @param accessibility
	 * @param forceInit
	 * @param authenticatedUser
	 * @param ci
	 * @return
	 * @throws Exception
	 */
	public boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, final UserI authenticatedUser, EventMetaI ci) throws Exception;

    
	/**
	 * Get all active permission criteria for this user group / data type combination.
	 * @param group
	 * @param dataType
	 * @return
	 */
	public List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType);

    
	/**
	 * Get all active permission criteria for this user group (organized by data type).
	 * @param group
	 * @param dataType
	 * @return
	 */
	public abstract Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group);

	/**
	 * Adds specified permissions for this group.
	 * @param group
	 * @param dataType
	 * @return
	 * @throws Exception 
	 */
	public abstract void setPermissionsForGroup(UserGroupI group, List<PermissionCriteriaI> criteria,EventMetaI meta, UserI authenticatedUser) throws Exception;

	/**
	 * Return an SQL statement that will return a list of this user's permissions
	 * @param user
	 * @return
	 */
	public abstract String getUserPermissionsSQL(UserI user);
	
}
