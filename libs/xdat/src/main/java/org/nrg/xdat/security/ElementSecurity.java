/*
 * core: org.nrg.xdat.security.ElementSecurity
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import static org.nrg.xft.event.XftItemEventI.CREATE;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.beans.XnatDataModelBean;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.*;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.*;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.DataModelDefinition;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
@Slf4j
public class ElementSecurity extends ItemWrapper {
    public static final String SCHEMA_ELEMENT_NAME = "xdat:element_security";

    public ElementSecurity() {
    }

    /**
     * Retrieves the system's element security objects.
     *
     * @return The map of element security objects.
     *
     * @throws Exception When something goes wrong.
     */
    public static Map<String, ElementSecurity> GetElementSecurities() throws Exception {
        synchronized (lock) {
            if (elements.isEmpty()) {
                ArrayList al = DisplaySearch.SearchForItems(SchemaElement.GetElement(SCHEMA_ELEMENT_NAME), new CriteriaCollection("AND"));
                for (final Object anItem : al) {
                    ItemI           item = (ItemI) anItem;
                    ElementSecurity es   = new ElementSecurity(item);
                    es.getItem().internValues();
                    elements.put(es.getElementName(), es);
                }
            }
        }
        return ImmutableMap.copyOf(elements);
    }

    /**
     * Reviews the classpath for additional data model definitions.  If those definitions
     * contain references to new data types (that haven't been registered already), they
     * will be registered now.
     *
     * @return Returns true if any new data model definitions were found.
     *
     * @throws Exception When something goes wrong.
     */
    public static List<String> registerNewTypes() throws Exception {
        final Map<String, ElementSecurity> elements    = GetElementSecurities();
        final List<String>                 newTypes    = new ArrayList<>();
        final JdbcTemplate                 template    = XDAT.getContextService().getBean(JdbcTemplate.class);
        final XnatPluginBeanManager        beanManager = XDAT.getContextService().getBean(XnatPluginBeanManager.class);
        final DatabaseHelper               helper      = XDAT.getContextService().getBean(DatabaseHelper.class);

        for (final DataModelDefinition dataModelDefinition : XFTManager.discoverDataModelDefs()) {
            for (final String securedElements : dataModelDefinition.getSecuredElements()) {
                if (StringUtils.isNotBlank(securedElements) && !elements.containsKey(securedElements) && GenericWrapperElement.GetFieldForXMLPath(securedElements + "/project") != null) {
                    final ElementSecurity elementSecurity = ElementSecurity.newElementSecurity(securedElements);
                    elementSecurity.initExistingPermissions();
                    newTypes.add(elementSecurity.getElementName());
                }
            }
        }

        newTypes.addAll(checkForReferencedDataModels(template, beanManager));

        if (!newTypes.isEmpty()) {
            ElementSecurity.refresh();
        }

        return newTypes;
    }

    public static List<String> checkForReferencedDataModels(final JdbcTemplate template, final XnatPluginBeanManager beanManager) throws Exception {
        synchronized (checkForReferencedDataModels) {
            if (checkForReferencedDataModels.get()) {
                return Collections.emptyList();
            }
            try {
                final Set<String> securities = GetElementSecurities().keySet();
                return beanManager.getPluginBeans().values().stream().map(XnatPluginBean::getDataModelBeans).flatMap(Collection::stream).filter(bean -> isUnconfiguredSecureDataModel(securities, bean)).map(bean -> activateDataType(template, bean)).filter(Objects::nonNull).collect(Collectors.toList());
            } finally {
                checkForReferencedDataModels.set(true);
            }
        }
    }

    public String toString() {
        try {
            return getElementName();
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            log.error("", e);
        }
        return null;
    }

    public String getSchemaElementName() {
        return SCHEMA_ELEMENT_NAME;
    }

    /**
     *
     */
    public static void refresh() {
        synchronized (lock) {
            elements.clear();
            CacheManager.GetInstance().clearAll();
        }
    }

    /**
     * @param elementName The name of the element to retrieve.
     *
     * @return The requested element.
     *
     * @throws Exception When something goes wrong.
     */
    public static ElementSecurity GetElementSecurity(String elementName) throws Exception {
        return GetElementSecurities().get(elementName);
    }

    public static ElementSecurity newElementSecurity(String elementName) throws Exception {
        if (GetElementSecurity(elementName) != null) {
            return GetElementSecurity(elementName);
        }

        final UserI user = Users.getUser("admin");

        final XFTItem securityItem = XFTItem.NewItem("xdat:element_security", user);
        securityItem.setProperty("element_name", elementName);
        securityItem.setProperty("secondary_password", "0");
        securityItem.setProperty("secure_ip", "0");
        securityItem.setProperty("secure", "1");
        securityItem.setProperty("browse", "1");
        securityItem.setProperty("sequence", "2");
        securityItem.setProperty("quarantine", "0");
        securityItem.setProperty("pre_load", "0");
        securityItem.setProperty("searchable", "1");
        securityItem.setProperty("secure_read", "1");
        securityItem.setProperty("secure_edit", "1");
        securityItem.setProperty("secure_create", "1");
        securityItem.setProperty("secure_delete", "1");
        securityItem.setProperty("accessible", "1");

        securityItem.setProperty("element_security_set_element_se_xdat_security_id", TurbineUtils.GetSystemID());
        securityItem.setProperty("xdat:element_security/primary_security_fields/primary_security_field[0]/primary_security_field", elementName + "/sharing/share/project");
        securityItem.setProperty("xdat:element_security/primary_security_fields/primary_security_field[1]/primary_security_field", elementName + "/project");

        addElementAction(securityItem, 0, "xml", "View XML", "2", "View", "NULL");
        addElementAction(securityItem, 1, "edit", "Edit", "0", "NULL", "edit");
        addElementAction(securityItem, 2, "xml_file", "Download XML", "7", "Download", "NULL");
        addElementAction(securityItem, 3, "email_report", "Email", "8", "NULL", "NULL");

        SaveItemHelper.authorizedSave(securityItem, user, false, false, EventUtils.ADMIN_EVENT(user));

        ElementSecurity.refresh();
        return ElementSecurity.GetElementSecurity(elementName);
    }

    private static void addElementAction(XFTItem es, int index, String action_name, String displayName, String sequence, String grouping, String access) {
        try {
            es.setProperty("xdat:element_security/element_actions/element_action[" + index + "]/element_action_name", action_name);
            es.setProperty("xdat:element_security/element_actions/element_action[" + index + "]/display_name", displayName);
            es.setProperty("xdat:element_security/element_actions/element_action[" + index + "]/sequence", sequence);
            es.setProperty("xdat:element_security/element_actions/element_action[" + index + "]/grouping", grouping);
            es.setProperty("xdat:element_security/element_actions/element_action[" + index + "]/secureAccess", access);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Gets a list of the insecure elements in the system.
     *
     * @return A list of insecure elements.
     *
     * @throws Exception When something goes wrong.
     */
    public static ArrayList<ElementSecurity> GetInSecureElements() throws Exception {
        final ArrayList<ElementSecurity>  securityElements = new ArrayList<>();
        final Collection<ElementSecurity> values           = GetElementSecurities().values();
        for (final ElementSecurity value : values) {
            if (!value.isSecure()) {
                securityElements.add(value);
            }
        }
        return securityElements;
    }

    /**
     * Tests the specified element for element security configuration.
     *
     * @param elementName The name of the element to test.
     *
     * @return Returns whether element security has been defined for this element
     */
    public static boolean HasDefinedElementSecurity(String elementName) {
        try {
            return GetElementSecurity(elementName) != null;
        } catch (Exception e) {
            log.error("", e);
            return true;
        }
    }

    /**
     * @return Returns a list of all the secure elements
     *
     * @throws Exception When something goes wrong.
     */
    public static ArrayList<ElementSecurity> GetSecureElements() throws Exception {
        final ArrayList<ElementSecurity>  elements = new ArrayList<>();
        final Collection<ElementSecurity> values   = GetElementSecurities().values();
        for (final ElementSecurity value : values) {
            if (value.isSecure()) {
                elements.add(value);
            }
        }
        return elements;
    }

    public static ArrayList<String> GetSecurityElements() throws Exception {
        final ArrayList<String> elements = new ArrayList<>();
        for (final ElementSecurity element : GetSecureElements()) {
            for (final String fieldPath : element.getPrimarySecurityFields()) {
                final GenericWrapperField field = GenericWrapperElement.GetFieldForXMLPath(fieldPath);
                if (field != null && field.isReference()) {
                    if (!elements.contains(field.getReferenceElementName().getFullForeignType())) {
                        elements.add(field.getReferenceElementName().getFullForeignType());
                    }
                } // else {
                // if (! se.contains(f.getParentElement().getFullXMLName())) {
                //     se.add(f.getParentElement().getFullXMLName());
                // }
                // }
            }
        }
        return elements;
    }

    public static ArrayList<ElementSecurity> GetQuarantinedElements() throws Exception {
        ArrayList<ElementSecurity> al = new ArrayList<>();
        try {
            Collection<ElementSecurity> ess = GetElementSecurities().values();
            for (ElementSecurity es : ess) {
                if (es.getBooleanProperty(ViewManager.QUARANTINE) != null) {
                    al.add(es);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return al;
    }

    public static ArrayList<ElementSecurity> GetPreLoadElements() throws Exception {
        ArrayList<ElementSecurity> al = new ArrayList<>();
        try {
            Collection<ElementSecurity> ess = GetElementSecurities().values();
            for (ElementSecurity es : ess) {
                if (es.getBooleanProperty("pre_load") != null) {
                    al.add(es);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return al;
    }

    /**
     * Tests whether the specified element is secured.
     *
     * @param elementName The name of the element to test.
     *
     * @return Whether the element is in the system's list of secure elements
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean IsSecureElement(String elementName) throws Exception {
        for (final ElementSecurity elementSecurity : GetSecureElements()) {
            if (StringUtils.equalsIgnoreCase(elementSecurity.getElementName(), elementName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the specified element and action are secured.
     *
     * @param elementName The name of the element to test.
     * @param action      The name of the action to test.
     *
     * @return Whether the element is in the system's list of secure elements
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean IsSecureElement(String elementName, String action) throws Exception {
        for (final ElementSecurity elementSecurity : GetSecureElements()) {
            if (StringUtils.equalsIgnoreCase(elementSecurity.getElementName(), elementName)) {
                return elementSecurity.isSecure(action);
            }
        }
        return false;
    }

    /**
     * Tests whether the specified element is not secured.
     *
     * @param elementName The name of the element to test.
     *
     * @return Whether the element is not in the system's list of secure elements
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean IsInSecureElement(String elementName) throws Exception {
        for (final ElementSecurity elementSecurity : GetInSecureElements()) {
            if (StringUtils.equalsIgnoreCase(elementSecurity.getElementName(), elementName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all browseable elements from the list of element securities.
     *
     * @return A list of available browseable elements.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static ArrayList<ElementSecurity> GetBrowseableElements() throws Exception {
        return Lists.newArrayList(Collections2.filter(GetElementSecurities().values(), new Predicate<ElementSecurity>() {
            @Override
            public boolean apply(@Nullable final ElementSecurity elementSecurity) {
                return elementSecurity != null && elementSecurity.isBrowseable();
            }
        }));
    }

    /**
     * Tests whether the specified element is browseable.
     *
     * @param elementName The name of the element to test.
     *
     * @return Whether the element is browseable.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean IsBrowseableElement(String elementName) throws Exception {
        final ElementSecurity elementSecurity = GetElementSecurity(elementName);
        return elementSecurity != null && elementSecurity.isBrowseable();
    }

    /**
     * Tests whether the specified element is searchable.
     *
     * @param elementName The name of the element to test.
     *
     * @return Whether the element is searchable.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean IsSearchable(String elementName) throws Exception {
        final ElementSecurity elementSecurity = GetElementSecurity(elementName);
        return elementSecurity != null && elementSecurity.isSearchable();
    }

    /**
     * Tests whether the specified element has primary security fields.
     *
     * @param elementName The name of the element to test.
     *
     * @return Whether the element has primary security fields.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static boolean HasPrimarySecurityFields(String elementName) throws Exception {
        final ElementSecurity elementSecurity = GetElementSecurity(elementName);
        return elementSecurity != null && !elementSecurity.getPrimarySecurityFields().isEmpty();
    }

    /**
     * Creates a new element security entry for the specified {@link ItemI item}.
     *
     * @param item The item for which to create the security element.
     */
    public ElementSecurity(final ItemI item) {
        setItem(item);
        try {
            elementName = getElementName();

            for (final Object child : getChildItems(XFT.PREFIX + ":element_security.primary_security_fields.primary_security_field")) {
                final String primarySecurityField = (String) ((ItemI) child).getProperty("primary_security_field");
                if (StringUtils.isNotBlank(primarySecurityField)) {
                    addPrimarySecurityField(primarySecurityField);
                }
            }

            for (final ItemI child : getChildItemCollection(XFT.PREFIX + ":element_security.element_actions.element_action").getItems("xdat:element_action_type.sequence")) {
                final ElementAction ea = new ElementAction(child);
                ea.getItem().internValues();
                addElementAction(ea);
            }

            for (final ItemI child : getChildItemCollection(org.nrg.xft.XFT.PREFIX + ":element_security.listing_actions.listing_action").getItems("xdat:element_security_listing_action.sequence")) {
                final XdatElementSecurityListingAction ea = new XdatElementSecurityListingAction(child);
                ea.getItem().internValues();
                addListingAction(ea);
            }
        } catch (XFTInitException e) {
            log.error("An error occurred accessing XFT while trying to create a new element security entry for an item of type {}", item.getXSIType(), e);
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element {} while trying to create a new element security entry for an item of type {}", e.ELEMENT, item.getXSIType(), e);
        } catch (FieldNotFoundException e) {
            log.error("Couldn't find the field {} while trying to create a new element security entry for an item of type {}: {}", e.FIELD, item.getXSIType(), e.MESSAGE, e);
        } catch (Exception e) {
            log.error("An unexpected error occurred =while trying to create a new element security entry for an item of type {}", e);
        }
    }

    /**
     * Gets the name of the element for this security object.
     *
     * @return The element's name
     *
     * @throws XFTInitException         When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When one of the requested fields can't be found in the data object.
     */
    public String getElementName() throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        try {
            return (String) getProperty("element_name");
        } catch (FieldEmptyException e) {
            log.error("The field {} was empty while trying to get the element name: {}", e.FIELD, e.MESSAGE, e);
            return null;
        }
    }

    /**
     * Gets the sequence number for this element.
     *
     * @return The sequence for this element
     *
     * @throws XFTInitException         When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When one of the requested fields can't be found in the data object.
     */
    public Integer getSequence() throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        try {
            return (Integer) getProperty("sequence");
        } catch (FieldEmptyException e) {
            log.error("The field {} was empty while trying to get the element sequence: {}", e.FIELD, e.MESSAGE, e);
            return null;
        }
    }

    /**
     * Gets the {@link SchemaElement schema element} for this element.
     *
     * @return Returns the schema element.
     *
     * @throws Exception When something goes wrong.
     */
    public SchemaElement getSchemaElement() throws Exception {
        return SchemaElement.GetElement(getElementName());
    }

    /**
     * Indicates whether this element is secure.
     *
     * @return Returns whether element is secure
     */
    public boolean isSecure() {
        return testIntegerPropertyForBoolean("secure", false);
    }

    /**
     * @return Returns whether action is secure
     */
    public boolean isSecure(String action) {
        return isSecure() && testIntegerPropertyForBoolean("secure_" + action, true);
    }

    /**
     * @return Returns whether read is secure
     */
    public boolean isSecureRead() {
        return testIntegerPropertyForBoolean("secure_read", true);
    }

    /**
     * @return Returns whether edit is secure
     */
    public boolean isSecureEdit() {
        return testIntegerPropertyForBoolean("secure_edit", true);
    }

    /**
     * @return Returns whether creation is secure
     */
    public boolean isSecureCreate() {
        return testIntegerPropertyForBoolean("secure_create", true);
    }

    /**
     * @return Returns whether deletion is secure.
     */
    public boolean isSecureDelete() {
        return testIntegerPropertyForBoolean("secure_delete", true);
    }

    /**
     * @return Returns whether the element is browseable.
     */
    public boolean isBrowseable() {
        return testIntegerPropertyForBoolean("browse", false);
    }

    /**
     * @return Returns whether the element is searchable.
     */
    public boolean isSearchable() {
        return testIntegerPropertyForBoolean("searchable", false);
    }

    /**
     * @return Returns whether the password is secure.
     */
    public boolean isSecurePassword() {
        return testIntegerPropertyForBoolean("secure_password", false);
    }

    /**
     * @return Returns whether the IP is secure
     */
    public boolean isSecureIP() {
        return testIntegerPropertyForBoolean("secure_ip", false);
    }

    /**
     * Adds the specified value as a primary security field.
     *
     * @param field The field to add to the primary security fields.
     */
    public void addPrimarySecurityField(final String field) {
        primarySecurityFields.add(field);
    }

    /**
     * Gets the list of primary security fields.
     *
     * @return Returns the list of primary security fields
     */
    public List<String> getPrimarySecurityFields() {
        return primarySecurityFields;
    }

    /**
     * @return Returns the list of element actions.
     */
    public List<ElementAction> getElementActions() {
        return elementActions;
    }

    public class ElementActionGroup {
        public String                   group   = "";
        public ArrayList<ElementAction> actions = new ArrayList<>();

        public String getGroup() {
            return group;
        }

        public ArrayList<ElementAction> getActions() {
            return actions;
        }
    }

    public ArrayList<ElementActionGroup> getElementActionsByGroupings() {
        ArrayList<ElementActionGroup> al = new ArrayList<>();

        for (ElementAction ea : this.elementActions) {
            if (ea.getGrouping() != null && !ea.getGrouping().equals("")) {
                boolean matched = false;
                for (ElementActionGroup eag : al) {
                    if (eag.group.equals(ea.getGrouping())) {
                        eag.actions.add(ea);
                        matched = true;
                    }
                }

                if (!matched) {
                    ElementActionGroup eag = new ElementActionGroup();
                    eag.group = ea.getGrouping();
                    eag.actions.add(ea);
                    al.add(eag);
                }
            } else {
                ElementActionGroup eag = new ElementActionGroup();
                eag.actions.add(ea);
                al.add(eag);
            }
        }

        return al;
    }

    /**
     * Adds the submitted {@link ElementAction element action} to this security object.
     *
     * @param action The action to be added to this security object.
     */
    public void addElementAction(ElementAction action) {
        elementActions.add(action);
    }

    /**
     * @return Returns the list of listing actions
     */
    public List<XdatElementSecurityListingAction> getListingActions() {
        return listingActions;
    }

    /**
     * Returns the listing actions as JSON.
     *
     * @return JSON representation of the listing actions.
     */
    @SuppressWarnings("unused")
    public String getListingActionsJSON() {
        if (StringUtils.isBlank(listingActionsJson)) {
            listingActionsJson = "[" + StringUtils.join(Lists.transform(listingActions, new Function<XdatElementSecurityListingAction, String>() {
                @Override
                public String apply(final XdatElementSecurityListingAction action) {
                    return "{action: \"" + action.getElementActionName() + "\", display: \"" + action.getDisplayName() + "\"" +
                           (action.getPopup() != null ? ", popup: \"" + action.getPopup() + "\"" : "") +
                           (action.getSequence() != null ? ", sequence: " + action.getSequence() : "") +
                           (action.getImage() != null ? ", image: \"" + action.getImage() + "\"" : "") +
                           (action.getParameterstring() != null ? ", params: \"" + action.getParameterstring() + "\"" : "") +
                           "}";
                }
            }), ", ") + "]";
        }
        return listingActionsJson;
    }

    /**
     * Adds a listing action to the element security.
     *
     * @param listingAction The listing action to add.
     */
    public void addListingAction(XdatElementSecurityListingAction listingAction) {
        listingActions.add(listingAction);
        listingActionsJson = null;
    }

    public List<PermissionItem> getPermissionItemsForTag(String tag) throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        List<PermissionItem> tempItems = Lists.newArrayList();

        for (String fieldName : getPrimarySecurityFields()) {
            if (!fieldName.startsWith(this.getElementName())) {
                fieldName = this.getElementName() + XFT.PATH_SEPARATOR + fieldName;
            }

            PermissionItem pi = new PermissionItem();
            pi.setFullFieldName(fieldName);
            pi.setDisplayName(tag);
            pi.setValue(tag);
            tempItems.add(pi);
        }

        Collections.sort(tempItems, PermissionItem.GetComparator());
        return tempItems;
    }

    private List<PermissionItem> permissionItems = null;

    /**
     * @return Returns a list of the PermissionsItems for the user
     *
     * @throws Exception When something goes wrong.
     */
    public List<PermissionItem> getPermissionItems(final String login) throws Exception {
        if (permissionItems == null) {
            permissionItems = new ArrayList<>();

            for (final String fieldName : getPrimarySecurityFields()) {
                final String              qualifiedFieldName = fieldName.startsWith(getElementName()) ? fieldName : getElementName() + XFT.PATH_SEPARATOR + fieldName;
                final GenericWrapperField field              = GenericWrapperElement.GetFieldForXMLPath(qualifiedFieldName);
                if (field != null) {
                    final Hashtable hash;
                    if (field.isReference()) {
                        SchemaElementI se = SchemaElement.GetElement(field.getReferenceElementName().getFullForeignType());
                        hash = GetDistinctIdValuesFor(se.getFullXMLName(), "default", login);
                    } else {
                        hash = GetDistinctIdValuesFor(field.getParentElement().getFullXMLName(), qualifiedFieldName, login);
                    }

                    Enumeration enumer = hash.keys();
                    while (enumer.hasMoreElements()) {
                        final Object id      = enumer.nextElement();
                        final String display = (String) hash.get(id);

                        final PermissionItem permissionItem = new PermissionItem();
                        permissionItem.setFullFieldName(qualifiedFieldName);
                        permissionItem.setDisplayName(display);
                        permissionItem.setValue(id);
                        permissionItems.add(permissionItem);
                    }
                } else {
                    log.warn("Couldn't retrieve a field object for the fully qualified field name '{}'", qualifiedFieldName);
                }
            }

            Collections.sort(permissionItems, PermissionItem.GetComparator());
        }
        return permissionItems;
    }

    /**
     * @return Returns whether the element has a displayField
     *
     * @throws Exception When something goes wrong.
     */
    public boolean hasDisplayValueOption() throws Exception {
        SchemaElement  se           = SchemaElement.GetElement(elementName);
        ElementDisplay ed           = se.getDisplay();
        DisplayField   idField      = ed.getDisplayField(ed.getValueField());
        DisplayField   displayField = ed.getDisplayField(ed.getDisplayField());
        return idField != null && displayField != null;
    }

    /**
     * @param elementName The element name to retrieve ID values for.
     * @param fieldName   The field name to search on.
     * @param login       The user retrieving the ID values.
     *
     * @return Returns a hastable of the distinct id values for the supplied information
     *
     * @throws Exception When something goes wrong.
     */
    public static Hashtable GetDistinctIdValuesFor(final String elementName, final String fieldName, final String login) throws Exception {
        //Hashtable hash1 = (Hashtable)elementDistinctIds.get(elementName); REMOVED TO PREVENT CACHING
        Hashtable<String, Hashtable<String, String>> hash1 = new Hashtable<>();
        SchemaElement                                se    = SchemaElement.GetElement(elementName);
        ElementDisplay                               ed    = se.getDisplay();
//		if (hash1 == null)
//		{
//			hash1 = new Hashtable();
//			elementDistinctIds.put(elementName,hash1);
//		}

        Hashtable hash2 = hash1.get(fieldName);
        if (hash2 == null) {
            if (fieldName.equalsIgnoreCase("default")) {
                final Hashtable<String, String> newHash               = new Hashtable<>();
                boolean                         hasDefinedIdValuePair = false;
                if (ed != null) {
                    DisplayField idField      = ed.getDisplayField(ed.getValueField());
                    DisplayField displayField = ed.getDisplayField(ed.getDisplayField());
                    if (idField != null && displayField != null) {
                        hasDefinedIdValuePair = true;
                        String   query = "SELECT DISTINCT ON (" + idField.getId() + ") " + idField.getId() + "," + displayField.getId() + " FROM " + se.getDisplayTable();
                        XFTTable table = TableSearch.Execute(query, se.getDbName(), login);
                        table.resetRowCursor();
                        while (table.hasMoreRows()) {
                            Object[] row = table.nextRow();
                            newHash.put(row[0].toString(), row[1].toString());
                        }
                        hash1.put(fieldName, newHash);
                        hash2 = newHash;
                    }
                }

                if (!hasDefinedIdValuePair) {
                    final StringBuilder query = new StringBuilder("SELECT DISTINCT ON (");
                    final String cols = StringUtils.join(Lists.transform(se.getAllPrimaryKeys(), new Function<Object, String>() {
                        @Nullable
                        @Override
                        public String apply(final Object object) {
                            return ((SchemaField) object).getSQLName();
                        }
                    }), ", ");

                    query.append(cols).append(") ").append(cols).append(" FROM ").append(se.getSQLName());

                    final XFTTable table = TableSearch.Execute(query.toString(), se.getDbName(), login);
                    table.resetRowCursor();
                    while (table.hasMoreRows()) {
                        final Object[] row = table.nextRow();
                        newHash.put(row[0].toString(), row[0].toString());
                    }
                    hash1.put(fieldName, newHash);
                    hash2 = newHash;
                }

            } else {
                final Hashtable<String, String> newHash = new Hashtable<>();

                GenericWrapperField f  = GenericWrapperElement.GetFieldForXMLPath(fieldName);
                ArrayList           al = f.getPossibleValues();
                if (al.size() > 0) {
                    for (final Object o : al) {
                        String s = (String) o;
                        newHash.put(s, s);
                    }
                    hash1.put(fieldName, newHash);
                    hash2 = newHash;

                } else {
                    final String baseElement = f.getBaseElement();
                    if (StringUtils.isBlank(baseElement)) {
                        GenericWrapperElement parent = f.getParentElement().getGenericXFTElement();
                        final String          xmlPath;
                        if (se.getGenericXFTElement().instanceOf(parent.getFullXMLName())) {
                            xmlPath = f.getXMLPathString(se.getFullXMLName());
                        } else {
                            xmlPath = f.getXMLPathString(parent.getFullXMLName());
                        }
                        QueryOrganizer qo = new QueryOrganizer(se.getFullXMLName(), null, ViewManager.ALL);
                        qo.addField(xmlPath);
                        String query = qo.buildQuery();
                        String col   = qo.translateXMLPath(xmlPath);
                        query = "SELECT DISTINCT " + col + " FROM (" + query + ") SEARCH";
                        XFTTable table = TableSearch.Execute(query, se.getDbName(), login);

                        table.resetRowCursor();
                        while (table.hasMoreRows()) {
                            Object[] row = table.nextRow();
                            if (row[0] != null) {
                                newHash.put(row[0].toString(), row[0].toString());
                            }
                        }
                        hash1.put(fieldName, newHash);
                        hash2 = newHash;
                    } else {
                        GenericWrapperElement foreign    = GenericWrapperElement.GetElement(baseElement);
                        String                foreignCol = f.getBaseCol();
                        String                query      = "SELECT DISTINCT " + foreignCol + " FROM " + foreign.getSQLName();
                        XFTTable              table      = TableSearch.Execute(query, se.getDbName(), login);

                        table.resetRowCursor();
                        while (table.hasMoreRows()) {
                            Object[] row = table.nextRow();
                            if (row[0] != null) {
                                newHash.put(row[0].toString(), row[0].toString());
                            }
                        }
                        hash1.put(fieldName, newHash);
                        hash2 = newHash;
                    }
                }
//				}else{
//					Hashtable newHash = new Hashtable();
//					String query = "SELECT DISTINCT " + df.getId() + " FROM " + se.getDisplayTable();
//					XFTTable table = TableSearch.Execute(query,se.getDB());
//					table.resetRowCursor();
//					while (table.hasMoreRows())
//					{
//						Object[] row = table.nextRow();
//						newHash.put(row[0].toString(),row[0].toString());
//					}
//					hash1.put(fieldName,newHash);
//					hash2 = newHash;
//				}
            }
        }
        return hash2;

    }

    /**
     * @param item
     *
     * @return Returns a SecurityValues object containing a map of item membership
     *
     * @throws Exception When something goes wrong.
     */
    public static SecurityValues GetSecurityValues(ItemI item) throws InvalidItemException, Exception {
        ElementSecurity es = ElementSecurity.GetElementSecurity(item.getXSIType());
        if (es == null) {
            return null;
        } else {
            return es.getSecurityValues(item);
        }
    }

    /**
     * @param item
     *
     * @return Returns a SecurityValues object containing a map of item membership
     *
     * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
     */
    public SecurityValues getSecurityValues(ItemI item) throws FieldNotFoundException, InvalidItemException {
        SecurityValues sv = new SecurityValues();

        try {
            if (item.getXSIType().equalsIgnoreCase(this.getElementName())) {
                Iterator psfs = this.getPrimarySecurityFields().iterator();
                while (psfs.hasNext()) {
                    String s = (String) psfs.next();

                    Object temp = item.getItem().getProperty(s, true, null);

                    if (temp == null) {
                        GenericWrapperField f = null;
                        try {
                            f = GenericWrapperElement.GetFieldForXMLPath(s);
                            if (f != null) {
                                if (f.isReference()) {
                                    XFTReferenceI ref = f.getXFTReference();
                                    if (!ref.isManyToMany()) {
                                        XFTSuperiorReference sup  = (XFTSuperiorReference) ref;
                                        Iterator             iter = sup.getKeyRelations().iterator();
                                        while (iter.hasNext()) {
                                            XFTRelationSpecification spec       = (XFTRelationSpecification) iter.next();
                                            String                   newXMLPath = s.substring(0, s.lastIndexOf(XFT.PATH_SEPARATOR) + 1) + spec.getLocalCol();
                                            temp = item.getProperty(newXMLPath);
                                            if (temp != null) {
                                                break;
                                            } else {
                                                XFTItem dbVersion = item.getCurrentDBVersion(false);
                                                if (dbVersion != null) {
                                                    temp = dbVersion.getProperty(newXMLPath);
                                                    if (temp != null) {
                                                        break;
                                                    }
                                                } else {
                                                    throw new InvalidItemException(item);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (XFTInitException e) {
                            log.error("", e);
                        } catch (ElementNotFoundException e) {
                            log.error("", e);
                        } catch (FieldNotFoundException e) {
                            log.error("", e);
                        }
                    }

                    if (temp != null) {
                        if (!(temp instanceof ArrayList)) {
                            ArrayList al = new ArrayList();
                            al.add(temp);
                            temp = al;
                        }

                        String   valueString = "";
                        Iterator values      = ((ArrayList) temp).iterator();
                        while (values.hasNext()) {
                            Object o = values.next();

                            if (o instanceof XFTItem) {
                                XFTItem sub = (XFTItem) o;
                                if (sub.hasProperties()) {
                                    o = sub.getPK();

                                    if (o == null && sub.getGenericSchemaElement().hasUniqueIdentifiers()) {
                                        try {
                                            ItemCollection items = sub.getUniqueMatches(true);
                                            if (items.size() > 0) {
                                                XFTItem   match = (XFTItem) items.first();
                                                ArrayList al    = sub.getPkNames();
                                                Iterator  iter  = al.iterator();
                                                while (iter.hasNext()) {
                                                    String key = (String) iter.next();

                                                    sub.setProperty(key, match.getProperty(key));
                                                }

                                                o = sub.getPK();
                                            }
                                        } catch (DBPoolException e1) {
                                            log.error("", e1);
                                        } catch (SQLException e1) {
                                            log.error("", e1);
                                        } catch (Exception e1) {
                                            log.error("", e1);
                                        }
                                    }

                                    if (o == null) {
                                        if (XFT.VERBOSE) {
                                            System.out.println("\nUnknown value in " + s);
                                        }
                                        log.error("\nUnknown value in " + s);
                                        if (XFT.VERBOSE) {
                                            System.out.println(sub.toXML_String(false));
                                        }
                                        log.error(sub.toXML_String(false));
                                        if (XFT.VERBOSE) {
                                            System.out.println("This field is a pre-defined security field and must match a pre-defined value.");
                                        }
                                        log.error("This field is a pre-defined security field and must match a pre-defined value.");
                                        if (XFT.VERBOSE) {
                                            System.out.println("This value is not currently in the database.");
                                        }
                                        log.error("This value is not currently in the database.");
                                    }
                                }
                            }

                            if (o != null && StringUtils.isNotEmpty(o.toString())) {
                                if (valueString == "") {
                                    valueString = o.toString();
                                } else {
                                    valueString += "," + o.toString();
                                }
                            }
                        }
                        if (valueString != "") {
                            sv.getHash().put(s, valueString);
                        }
                    }
                }
            }
        } catch (XFTInitException e) {
            log.error("", e);
        } catch (ElementNotFoundException e) {
            log.error("", e);
        } catch (FieldNotFoundException e) {
            log.error("", e);
        }

        return sv;
    }

    /**
     * @return The absent elements.
     */
    public static ArrayList GetAbsentElements() {
        try {
            ArrayList al      = new ArrayList();
            Iterator  schemas = XFTManager.GetSchemas().iterator();
            while (schemas.hasNext()) {
                XFTSchema s        = (XFTSchema) schemas.next();
                Iterator  elements = s.getSortedElements().iterator();
                while (elements.hasNext()) {
                    XFTElement e = (XFTElement) elements.next();
                    if (e.getAddin().equalsIgnoreCase("")) {
                        if (!ElementSecurity.HasDefinedElementSecurity(e.getType().getFullLocalType())) {
                            String proper = XFTReferenceManager.GetProperName(e.getType().getFullLocalType());
                            if (proper != null) {
                                al.add(e.getType().getFullLocalType());
                            }
                        }
                    }
                }
            }

            return al;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    /**
     * @return A sorted List of Strings
     *
     * @throws Exception When something goes wrong.
     */
    public static ArrayList GetElementNames() throws Exception {
        ArrayList<String>           al  = new ArrayList<String>();
        Collection<ElementSecurity> ess = GetElementSecurities().values();
        for (ElementSecurity es : ess) {
            al.add(es.getElementName());
        }
        al.trimToSize();
        Collections.sort(al);
        return al;
    }

    /**
     * @return A sorted List of Strings
     *
     * @throws Exception When something goes wrong.
     */
    public static ArrayList GetNonXDATElementNames() throws Exception {
        ArrayList<String>           al  = new ArrayList<>();
        Collection<ElementSecurity> ess = GetElementSecurities().values();
        for (ElementSecurity es : ess) {
            if (!es.getElementName().startsWith("xdat")) {
                al.add(es.getElementName());
            }
        }
        al.trimToSize();
        Collections.sort(al);
        return al;
    }

    public static String GetFinalSecurityField(String s) {
        GenericWrapperField f;
        try {
            f = GenericWrapperElement.GetFieldForXMLPath(s);
            if (f != null) {
                if (f.isReference()) {
                    XFTReferenceI ref = f.getXFTReference();
                    if (!ref.isManyToMany()) {
                        XFTSuperiorReference sup = (XFTSuperiorReference) ref;
                        for (final XFTRelationSpecification spec : sup.getKeyRelations()) {
                            s = s.substring(0, s.lastIndexOf(XFT.PATH_SEPARATOR) + 1) + spec.getLocalCol();
                            break;
                        }
                    }
                }
            }
        } catch (XFTInitException e) {
            log.error("", e);
        } catch (ElementNotFoundException e) {
            log.error("", e);
        } catch (FieldNotFoundException e) {
            log.error("", e);
        }

        return s;
    }

    public String getSecurityFields() {
        if (primarySecurityFields.size() == 0) {
            return "";
        }
        if (primarySecurityFields.size() == 1) {
            return primarySecurityFields.get(0);
        }
        return StringUtils.join(primarySecurityFields, " ");
    }

    List<String> fields = null;

    public boolean hasField(String field) {
        if (fields == null) {
            try {
                fields = this.getSchemaElement().getAllDefinedFields();
            } catch (Exception e) {
                log.error("", e);
                return false;
            }
        }
        return fields.contains(field);
    }

    public void initPSF(String field, EventMetaI meta) {
        try {
            String   query = "SELECT primary_security_field FROM xdat_primary_security_field WHERE primary_security_field = '" + field + "';";
            XFTTable t     = XFTTable.Execute(query, getDBName(), null);
            if (t.size() == 0) {
                XdatPrimarySecurityField psf = new XdatPrimarySecurityField(this.getUser());
                psf.setPrimarySecurityField(field);
                psf.setProperty("xdat:primary_security_field/primary_security_fields_primary_element_name", this.getElementName());
                SaveItemHelper.authorizedSave(psf, this.getUser(), true, true, meta);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Initializes permissions on existing objects when new data types are added to the system. The SQL is loaded from the
     * META-INF/xnat/scripts/new-data-type-permissions.sql and META-INF/xnat/scripts/new-data-type-public-perms.sql
     * resources.
     */
    public void initExistingPermissions() {
        final String elementName;
        try {
            elementName = getElementName();
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            log.error("Got an error just trying to get the name of the element security object, bailing on the operation.", e);
            return;
        }

        final boolean result = getDatabaseHelper().callFunction("data_type_fns_create_new_permissions", new MapSqlParameterSource("elementName", elementName), Boolean.class);
        if (!result) {
            log.warn("Got result of false from data_type_fns_create_new_permissions function call. Please check other logs to determine if there's an error.");
        }

        try {
            updateElementAccessAndFieldMapMetaData();
            XDAT.triggerXftItemEvent(SCHEMA_ELEMENT_NAME, elementName, CREATE);
            log.info("Executing clear groups cache query: {}", CLEAR_GROUPS_ITEM_CACHE);
            XDAT.getNamedParameterJdbcTemplate().update(CLEAR_GROUPS_ITEM_CACHE, EmptySqlParameterSource.INSTANCE);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    static void updateElementAccessAndFieldMapMetaData() {
        try {
            DBAction.InsertMetaDatas(XdatElementAccess.SCHEMA_ELEMENT_NAME);
            DBAction.InsertMetaDatas(XdatFieldMappingSet.SCHEMA_ELEMENT_NAME);
            DBAction.InsertMetaDatas(XdatFieldMapping.SCHEMA_ELEMENT_NAME);
        } catch (Exception e2) {
            log.error("", e2);
        }
    }

    public Comparator getComparator() {
        return new ESComparator();
    }

    public class ESComparator implements Comparator<ElementSecurity> {
        public ESComparator() {
        }

        public int compare(final ElementSecurity elementSecurity1, final ElementSecurity elementSecurity2) {
            try {
                if (elementSecurity1 == null) {
                    if (elementSecurity2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (elementSecurity2 == null) {
                    return 1;
                }
                return StringUtils.compare(elementSecurity1.getElementName(), elementSecurity2.getElementName());
            } catch (XFTInitException e) {
                log.error("An error occurred accessing XFT while trying to compare element security entries for items of type {} and {}", elementSecurity1.getXSIType(), elementSecurity2.getXSIType(), e);
            } catch (ElementNotFoundException e) {
                log.error("Couldn't find the element {} while trying to compare element security entries for items of type {} and {}", e.ELEMENT, elementSecurity1.getXSIType(), elementSecurity2.getXSIType(), e);
            } catch (FieldNotFoundException e) {
                log.error("Couldn't find the field {} while trying to compare element security entries for items of type {} and {}: {}", e.FIELD, elementSecurity1.getXSIType(), elementSecurity2.getXSIType(), e.MESSAGE, e);
            }
            return 0;
        }
    }

    private Boolean _Accessible = null;

    public Boolean getAccessible() {
        try {
            if (_Accessible == null) {
                _Accessible = getBooleanProperty("accessible", true);
                return _Accessible;
            } else {
                return _Accessible;
            }
        } catch (Exception e) {
            log.error("An exception occurred", e);
            return null;
        }
    }


    //FIELD

    private String _Usage = null;

    /**
     * @return Returns the element_name.
     */
    public String getUsage() {
        try {
            if (_Usage == null) {
                _Usage = getStringProperty("usage");
                return _Usage;
            } else {
                return _Usage;
            }
        } catch (Exception e) {
            log.error("An exception occurred", e);
            return null;
        }
    }

    /**
     * Sets the value for element_name.
     *
     * @param v Value to Set.
     */
    public void setUsage(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/usage", v);
            _Usage = null;
        } catch (Exception e) {
            log.error("An exception occurred", e);
        }
    }

    public boolean matchesUsageEntry(String id) {
        String usage = getUsage();
        return usage != null && Arrays.asList(usage.split("[\\s]*,[\\s]*")).contains(id);
    }

    //FIELD

    private String _Singular = null;

    /**
     * @return Returns the element_name.
     */
    public String getSingular() {
        try {
            if (_Singular == null) {
                _Singular = getStringProperty("singular");
                return _Singular;
            } else {
                return _Singular;
            }
        } catch (Exception e) {
            log.error("An exception occurred", e);
            return null;
        }
    }

    /**
     * Sets the value for element_name.
     *
     * @param v Value to Set.
     */
    public void setSingular(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/singular", v);
            _Singular = null;
        } catch (Exception e) {
            log.error("An exception occurred", e);
        }
    }

    //FIELD

    private String _Plural = null;

    /**
     * @return Returns the element_name.
     */
    public String getPlural() {
        try {
            if (_Plural == null) {
                _Plural = getStringProperty("plural");
                return _Plural;
            } else {
                return _Plural;
            }
        } catch (Exception e) {
            log.error("An exception occurred", e);
            return null;
        }
    }

    /**
     * Sets the value for element_name.
     *
     * @param v Value to Set.
     */
    public void setPlural(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/plural", v);
            _Plural = null;
        } catch (Exception e) {
            log.error("An exception occurred", e);
        }
    }

    //FIELD

    private String _Code = null;

    /**
     * @return Returns the element_name.
     */
    public String getCode() {
        try {
            if (_Code == null) {
                _Code = getStringProperty("code");
                return _Code;
            } else {
                return _Code;
            }
        } catch (Exception e) {
            log.error("An exception occurred", e);
            return null;
        }
    }

    /**
     * Sets the value for element_name.
     *
     * @param v Value to Set.
     */
    public void setCode(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/code", v);
            _Code = null;
        } catch (Exception e) {
            log.error("An exception occurred", e);
        }
    }

    //FIELD

    private String _Category = null;

    /**
     * @return Returns the element_name.
     */
    public String getCategory() {
        try {
            if (_Category == null) {
                _Category = getStringProperty("category");
                return _Category;
            } else {
                return _Category;
            }
        } catch (Exception e) {
            log.error("An exception occurred", e);
            return null;
        }
    }

    /**
     * Sets the value for element_name.
     *
     * @param v Value to Set.
     */
    public void setCategory(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/category", v);
            _Category = null;
        } catch (Exception e) {
            log.error("An exception occurred", e);
        }
    }

    public String getSingularDescription() {
        if (getSingular() == null) {
            try {
                return getSchemaElement().getDisplay().getDescription();
            } catch (Throwable e) {
                log.error("", e);
                try {
                    return this.getElementName();
                } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e1) {
                    log.error("", e1);
                }
            }
        } else {
            return getSingular();
        }

        return null;
    }

    public String getPluralDescription() {
        if (getPlural() == null) {
            try {
                return getSchemaElement().getDisplay().getDescription();
            } catch (Throwable e) {
                log.error("", e);
                try {
                    return this.getElementName();
                } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e1) {
                    log.error("", e1);
                }
            }
        } else {
            return getPlural();
        }

        return null;
    }


    public static String GetSingularDescription(String elementName) {
        try {
            ElementSecurity es = GetElementSecurities().get(elementName);
            return (es != null && StringUtils.isNotBlank(es.getSingularDescription())) ? es.getSingularDescription() : GenericWrapperElement.GetElement(elementName).getProperName();
        } catch (Exception e) {
            log.error("", e);
        }

        return elementName;
    }

    public static String GetPluralDescription(String elementName) {
        try {
            ElementSecurity es = GetElementSecurities().get(elementName);
            if (es != null) {
                return es.getPluralDescription();
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return elementName;
    }

    public static String GetCode(String elementName) {
        try {
            ElementSecurity es = GetElementSecurities().get(elementName);
            if (es != null) {
                return es.getCode();
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return elementName;
    }

    /**
     * Gets the specified integer property and tests whether the value is 1 (true) or not 1 (false). If the property is null or an exception
     * occurs retrieving the property, the default value is returned.
     *
     * @param property     The property to test.
     * @param defaultValue The default value to return if the property value is null or an exception occurs.
     *
     * @return If the property exists and has a value, this method returns true if the value is 1, returns false if the value is not 1. If there is no value or an exception occurs, returns the default value.
     */
    private boolean testIntegerPropertyForBoolean(final String property, final boolean defaultValue) {
        try {
            return ObjectUtils.defaultIfNull((Integer) getProperty(property), defaultValue ? 1 : 0) == 1;
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element {} while trying to get the secure state for an item of type {}", e.ELEMENT, getItem().getXSIType(), e);
        } catch (FieldNotFoundException e) {
            log.error("Couldn't find the field {} while trying to get the secure state for an item of type {}: {}", e.FIELD, getItem().getXSIType(), e.MESSAGE, e);
        }
        return defaultValue;
    }

    private static boolean isUnconfiguredSecureDataModel(final Collection<String> securities, final XnatDataModelBean bean) {
        final String type = bean.getType();
        try {
            return !securities.contains(type) && bean.isSecured() && GenericWrapperElement.GetFieldForXMLPath(type + "/project") != null;
        } catch (XftItemException e) {
            log.error("An error occurred trying to get the field for XML path {}/project", type);
            return false;
        }
    }

    private static String activateDataType(final JdbcTemplate template, final XnatDataModelBean bean) {
        final String dataType = bean.getType();
        final String singular = StringUtils.defaultIfBlank(bean.getSingular(), "");
        final String plural   = StringUtils.defaultIfBlank(bean.getPlural(), "");
        final String code     = StringUtils.defaultIfBlank(bean.getCode(), "");

        try {
            final ElementSecurity elementSecurity = ElementSecurity.newElementSecurity(dataType);
            elementSecurity.initExistingPermissions();

            final List<String> sets = new ArrayList<>();
            if (StringUtils.isNotBlank(singular)) {
                elementSecurity.setSingular(singular);
                sets.add("singular = '" + singular + "'");
            }
            if (StringUtils.isNotBlank(plural)) {
                elementSecurity.setSingular(plural);
                sets.add("plural = '" + plural + "'");
            }
            if (StringUtils.isNotBlank(code)) {
                elementSecurity.setSingular(code);
                sets.add("code = '" + code + "'");
            }
            if (!sets.isEmpty()) {
                final String query   = "UPDATE xdat_element_security SET " + String.join(", ", sets) + " WHERE lower(element_name) = '" + dataType.toLowerCase() + "'";
                final int    results = template.update(query);
                log.info("Updated {} rows with the query: {}", results, query);
            }

            return elementSecurity.getElementName();
        } catch (Exception e) {
            log.error("An error occurred trying to activate the data type {}", dataType, e);
            return null;
        }
    }

    private static DatabaseHelper getDatabaseHelper() {
        if (_databaseHelper == null) {
            final ContextService context = XDAT.getContextService();
            _databaseHelper = new DatabaseHelper(context.getBean(DataSource.class), context.getBean(TransactionTemplate.class));
        }
        return _databaseHelper;
    }

    private static String initializeNewDataTypePermissionsSql(final String script) {
        final String target = "classpath:META-INF/xnat/scripts/" + script;
        try {
            return IOUtils.toString(BasicXnatResourceLocator.getResource(target).getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            log.error("Unable to load the resource \"{}\"", target, e);
            return "";
        }
    }

    private static final String CLEAR_GROUPS_ITEM_CACHE = "DELETE FROM xs_item_cache WHERE elementname = '" + Groups.getGroupDatatype() + "'";

    private static final Map<String, ElementSecurity> elements                     = new HashMap<>();
    private static final AtomicBoolean                checkForReferencedDataModels = new AtomicBoolean();
    private static final Object                       lock                         = new Object();

    private static DatabaseHelper _databaseHelper;

    private final List<String>                           primarySecurityFields = new ArrayList<>();
    private final List<ElementAction>                    elementActions        = new ArrayList<>();
    private final List<XdatElementSecurityListingAction> listingActions        = new ArrayList<>();

    private String elementName        = null;
    private String listingActionsJson = null;
}
