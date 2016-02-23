package org.nrg.xdat.security.helpers;

import org.apache.log4j.Logger;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Permissions {
    static Logger logger = Logger.getLogger(Permissions.class);


    private static PermissionsServiceI singleton = null;

    /**
     * Returns the currently configured permissions service
     * <p/>
     * You can customize the implementation returned by adding a new implementation to the org.nrg.xdat.security.user.custom package (or a diffently configured package).
     * <p/>
     * You can change the default implementation returned via the security.userManagementService.default configuration parameter
     *
     * @return
     */
    public static PermissionsServiceI getPermissionsService() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (singleton == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return singleton = contextService.getBean(PermissionsServiceI.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    // This is OK, we'll just create it from the indicated class.
                }
            }
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.permissionsService.package", "org.nrg.xdat.permissions.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (PermissionsServiceI.class.isAssignableFrom(clazz)) {
                            singleton = (PermissionsServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.error("", e);
            } catch (InstantiationException e) {
                logger.error("", e);
            } catch (IllegalAccessException e) {
                logger.error("", e);
            } catch (IOException e) {
                logger.error("", e);
            }

            //default to PermissionsServiceImpl implementation (unless a different default is configured)
            if (singleton == null) {
                try {
                    String className = XDAT.safeSiteConfigProperty("security.permissionsService.default", "org.nrg.xdat.security.PermissionsServiceImpl");
                    singleton = (PermissionsServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException e) {
                    logger.error("", e);
                } catch (InstantiationException e) {
                    logger.error("", e);
                } catch (IllegalAccessException e) {
                    logger.error("", e);
                }
            }
        }
        return singleton;
    }

    /**
     * Get current XDAT criteria objects for current permission settings.  The XDAT criteria are used within the search engine to build long ugly WHERE clauses which limit the users access.  We'll want to refactor this if it isn't rewritten.
     *
     * @param user
     * @param rootElement
     * @return
     * @throws IllegalAccessException
     * @throws Exception
     */
    public static CriteriaCollection getCriteriaForXDATRead(UserI user, SchemaElement rootElement) throws IllegalAccessException, Exception {
        return getPermissionsService().getCriteriaForXDATRead(user, rootElement);
    }

    /**
     * Get current XFT criteria used when querying XFT items out of the database.
     *
     * @param user
     * @param rootElement
     * @return
     * @throws Exception
     */
    public static CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI rootElement) throws Exception {
        return getPermissionsService().getCriteriaForXFTRead(user, rootElement);
    }

    /**
     * Can the user create an element based on a collection of key/value pairs {@link SecurityValues}.
     * <p/>
     * This is similar to running canCreate(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user
     * @param root
     * @param values
     * @return
     * @throws Exception
     */
    public static boolean canCreate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canCreate(user, root, values);
    }

    /**
     * Can the user read an element based on a collection of key/value pairs {@link SecurityValues}.
     * <p/>
     * This is similar to running canRead(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user
     * @param root
     * @param values
     * @return
     * @throws Exception
     */
    public static boolean canRead(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canRead(user, root, values);
    }

    /**
     * Can the user edit an element based on a collection of key/value pairs {@link SecurityValues}.
     * <p/>
     * This is similar to running canEdit(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user
     * @param root
     * @param values
     * @return
     * @throws Exception
     */
    public static boolean canEdit(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canEdit(user, root, values);
    }

    /**
     * Can the user activate an element based on a collection of key/value pairs {@link SecurityValues}.
     * <p/>
     * This is similar to running canActivate(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user
     * @param root
     * @param values
     * @return
     * @throws Exception
     */
    public static boolean canActivate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canActivate(user, root, values);
    }

    /**
     * Can the user delete an element based on a collection of key/value pairs {@link SecurityValues}.
     * <p/>
     * This is similar to running canDelete(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user
     * @param root
     * @param values
     * @return
     * @throws Exception
     */
    public static boolean canDelete(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canDelete(user, root, values);
    }

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
    public static boolean can(UserI user, ItemI item, String action) throws InvalidItemException, Exception {
        return getPermissionsService().can(user, item, action);
    }

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
    public static boolean can(UserI user, String xmlPath, Object value, String action) throws Exception {
        return getPermissionsService().can(user, xmlPath, value, action);
    }

    /**
     * Can the user read the specified item
     *
     * @param user
     * @param item
     * @return
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canRead(UserI user, ItemI item) throws InvalidItemException, Exception {
        return getPermissionsService().canRead(user, item);
    }

    /**
     * Can the user edit the specified item
     *
     * @param user
     * @param item
     * @return
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canEdit(UserI user, ItemI item) throws InvalidItemException, Exception {
        return getPermissionsService().canEdit(user, item);
    }

    /**
     * Can the user create the specified item
     *
     * @param user
     * @param item
     * @return
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canCreate(UserI user, ItemI item) throws InvalidItemException, Exception {
        return getPermissionsService().canCreate(user, item);
    }

    /**
     * Can the user activate the specified item
     *
     * @param user
     * @param item
     * @return
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canActivate(UserI user, ItemI item) throws InvalidItemException, Exception {
        return getPermissionsService().canActivate(user, item);
    }

    /**
     * Can the user delete the specified item
     *
     * @param user
     * @param item
     * @return
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canDelete(UserI user, ItemI item) throws InvalidItemException, Exception {
        return getPermissionsService().canDelete(user, item);
    }

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
    public static String canStoreItem(UserI user, ItemI item, boolean descend) throws InvalidItemException, Exception {
        return getPermissionsService().canStoreItem(user, item, descend);
    }

    /**
     * Review the passed item and remove any child items that this user doesn't have access to.
     *
     * @param user
     * @param item
     * @return
     * @throws IllegalAccessException
     * @throws org.nrg.xft.exception.MetaDataException
     */
    public static ItemI secureItem(UserI user, ItemI item) throws IllegalAccessException, org.nrg.xft.exception.MetaDataException {
        return getPermissionsService().secureItem(user, item);
    }

    /**
     * Can the user read items for the String/Object pair
     *
     * @param user       The user to test.
     * @param xmlPath    The property to test.
     * @param value      The value to test.
     * @return True or false.
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canRead(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canRead(user, xmlPath, value);
    }

    /**
     * Can the user edit items for the String/Object pair
     *
     * @param user       The user to test.
     * @param xmlPath    The property to test.
     * @param value      The value to test.
     * @return True or false.
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canEdit(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canEdit(user, xmlPath, value);
    }

    /**
     * Can the user create items for the String/Object pair
     *
     * @param user       The user to test.
     * @param xmlPath    The property to test.
     * @param value      The value to test.
     * @return True or false.
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canCreate(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canCreate(user, xmlPath, value);
    }

    /**
     * Can the user activate items for the String/Object pair
     *
     * @param user       The user to test.
     * @param xmlPath    The property to test.
     * @param value      The value to test.
     * @return True or false.
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canActivate(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canActivate(user, xmlPath, value);
    }

    /**
     * Can the user delete items for the String/Object pair
     *
     * @param user       The user to test.
     * @param xmlPath    The property to test.
     * @param value      The value to test.
     * @return True or false.
     * @throws InvalidItemException
     * @throws Exception
     */
    public static boolean canDelete(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canDelete(user, xmlPath, value);
    }

    /**
     * Can the user read any of the given elementName/xmlPath/action combination
     *
     * @param user
     * @param elementName
     * @param xmlPath
     * @param action
     * @return
     */
    public static boolean canAny(UserI user, String elementName, String xmlPath, String action) {
        return getPermissionsService().canAny(user, elementName, xmlPath, action);
    }

    /**
     * Can the user read any of the given elementName/action combination
     *
     * @param user
     * @param elementName
     * @param action
     * @return
     */
    public static boolean canAny(UserI user, String elementName, String action) {
        return getPermissionsService().canAny(user, elementName, action);
    }

    /**
     * Get current XDAT criteria objects for current permission settings.  The XDAT criteria are used within the search engine to build long ugly WHERE clauses which limit the users access.  We'll want to refactor this if it isn't rewritten.
     *
     * @param set
     * @param root
     * @param action
     * @return
     * @throws IllegalAccessException
     * @throws Exception
     */
    public static CriteriaCollection getXDATCriteria(PermissionSetI set, SchemaElement root, String action) throws Exception {
        final ElementSecurity es = root.getElementSecurity();

        final CriteriaCollection coll = new CriteriaCollection(set.getMethod());
        final List<String> checkedValues = new ArrayList<String>();

        for (PermissionCriteriaI c : set.getPermCriteria()) {
            checkedValues.add(c.getFieldValue().toString());

            boolean can = false;

            if (!can) {
                if (c.isActive()) {
                    can = c.getAction(action);
                }
            }

            if (can) {
                coll.addClause(DisplayCriteria.buildCriteria(root, c));
            }
        }

        for (PermissionSetI subset : set.getPermSets()) {
            final CriteriaCollection sub = getXDATCriteria(subset, root, action);
            coll.addClause(sub);
        }
        return coll;
    }

    /**
     * Get current XFT criteria used when querying XFT items out of the database.
     *
     * @param set
     * @param action
     * @return
     * @throws Exception
     */
    public static CriteriaCollection getXFTCriteria(PermissionSetI set, String action) throws Exception {
        final CriteriaCollection coll = new CriteriaCollection(set.getMethod());
        final List<String> checkedValues = new ArrayList<String>();

        for (PermissionCriteriaI c : set.getPermCriteria()) {
            checkedValues.add(c.getFieldValue().toString());
            boolean can = false;

            if (!can) {
                if (c.isActive()) {
                    can = c.getAction(action);
                }
            }

            if (can) {
                coll.addClause(SearchCriteria.buildCriteria(c));
            }
        }

        for (PermissionSetI subset : set.getPermSets()) {
            final CriteriaCollection sub = getXFTCriteria(subset, action);
            coll.addClause(sub);
        }
        return coll;
    }

    /**
     * Checks the security settings of the data type to see if the user can perform this query.
     * To check if users have been specifically allowed to see any objects of that type, use canReadAny.
     *
     * @param user
     * @param elementName
     * @return
     */
    public static boolean canQuery(final UserI user, final String elementName) {
        try {
            Authorizer.getInstance().authorizeRead(GenericWrapperElement.GetElement(elementName), user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks the security settings of the data type and the user's permissions to see if the user can access any items of this type.
     *
     * @param user
     * @param elementName
     * @return
     */
    public static boolean canReadAny(final UserI user, final String elementName) {
        try {
            Authorizer.getInstance().authorizeRead(GenericWrapperElement.GetElement(elementName), user);

            return Permissions.canAny(user, elementName, SecurityManager.READ);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the values that this user can do the specified action on for the given element/xmlpath combo
     *
     * @param user
     * @param elementName
     * @param xmlPath
     * @param action
     * @return
     */
    public static List<Object> getAllowedValues(UserI user, String elementName, String xmlPath, String action) {
        return getPermissionsService().getAllowedValues(user, elementName, xmlPath, action);
    }

    /**
     * Get the xmlpath/value combos that this user can do the specified action on for the given element
     *
     * @param user
     * @param elementName
     * @param action
     * @return
     */
    public static Map<String, Object> getAllowedValues(UserI user, String elementName, String action) {
        return getPermissionsService().getAllowedValues(user, elementName, action);
    }

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
    public static void setPermissions(UserI effected, UserI authenticated, String elementName, String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, EventMetaI ci) {
        getPermissionsService().setPermissions(effected, authenticated, elementName, psf, value, create, read, delete, edit, activate, activateChanges, ci);
    }

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
    public static boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, final UserI authenticatedUser, EventMetaI ci) throws Exception {
        return getPermissionsService().setDefaultAccessibility(tag, accessibility, forceInit, authenticatedUser, ci);
    }

    /**
     * Get all active permission criteria for this user account / data type combination (including group permissions, etc).
     *
     * @param user
     * @param dataType
     * @return
     */
    public static List<PermissionCriteriaI> getPermissionsForUser(UserI user, String dataType) {
        return getPermissionsService().getPermissionsForUser(user, dataType);
    }

    /**
     * Get all active permission criteria for this user group / data type combination.
     *
     * @param group
     * @param dataType
     * @return
     */
    public static List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType) {
        return getPermissionsService().getPermissionsForGroup(group, dataType);
    }

    /**
     * Get all active permission criteria for this user group (organized by data type).
     *
     * @param group
     * @return
     */
    public static Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group) {
        return getPermissionsService().getPermissionsForGroup(group);
    }

    /**
     * Adds/modifies specified permissions for this group.  However, nothing is saved to the database.
     * <p/>
     * Call Groups.save() to save the modifications.
     *
     * @param group
     * @param criteria
     * @param meta
     * @param authenticatedUser
     * @return
     * @throws Exception
     */
    public static void setPermissionsForGroup(UserGroupI group, List<PermissionCriteriaI> criteria, EventMetaI meta, UserI authenticatedUser) throws Exception {
        getPermissionsService().setPermissionsForGroup(group, criteria, meta, authenticatedUser);
    }

    /**
     * Return an SQL statement that will return a list of this user's permissions
     *
     * @param user
     * @return
     */
    public static String getUserPermissionsSQL(UserI user) {
        return getPermissionsService().getUserPermissionsSQL(user);
    }

}
