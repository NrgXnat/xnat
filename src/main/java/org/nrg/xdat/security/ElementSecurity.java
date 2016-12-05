/*
 * core: org.nrg.xdat.security.ElementSecurity
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.framework.beans.XnatDataModelBean;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader;
import org.nrg.xft.*;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Tim
 */
@SuppressWarnings("serial")
public class ElementSecurity extends ItemWrapper {
    private static final String                             XDAT_ELEMENT_SECURITY = "xdat:element_security";
    static               Logger                             logger                = Logger.getLogger(ElementSecurity.class);
    public static final  String                             SCHEMA_ELEMENT_NAME   = XDAT_ELEMENT_SECURITY;
    public static        Hashtable<String, ElementSecurity> elements              = null;
    public static        Hashtable                          elementDistinctIds    = new Hashtable(); //Hashtable of Hashtables /level 1 key=elementName /level2 key=fieldName (or default if ref)/Level 3 key= pk value ,value=display name

    private String                                      elementName           = null;
    private ArrayList<String>                           primarySecurityFields = new ArrayList<>();
    private ArrayList<ElementAction>                    elementActions        = new ArrayList<>();
    private ArrayList<XdatElementSecurityListingAction> listingActions        = new ArrayList<>();

    private static final Object lock = new Object();

    public ElementSecurity() {
    }

    /**
     * Retrieves the system's element security objects.
     * @return The map of element security objects.
     * @throws Exception When something goes wrong.
     */
    public static Hashtable<String, ElementSecurity> GetElementSecurities() throws Exception {
        if (elements == null || elements.size() == 0) {
            synchronized (lock) {
                if (elements == null || elements.size() == 0) {
                    elements = new Hashtable<>();
                    ArrayList al = DisplaySearch.SearchForItems(SchemaElement.GetElement(XDAT_ELEMENT_SECURITY), new CriteriaCollection("AND"));
                    for (final Object anItem : al) {
                        ItemI item = (ItemI) anItem;
                        ElementSecurity es = new ElementSecurity(item);
                        es.getItem().internValues();
                        elements.put(es.getElementName(), es);
                    }
                }
            }
        }
        return elements;
    }

    /**
     * Reviews the classpath for additional data model definitions.  If those definitions
     * contain references to new data types (that haven't been registerd already), they
     * will be registered now.
     *
     * @return Returns true if any new data model definitions were found.
     * @throws Exception When something goes wrong.
     */
    public static boolean registerNewTypes() throws Exception {
        Map<String, ElementSecurity> elements = (Map<String, ElementSecurity>) GetElementSecurities().clone();

        boolean _new = false;
        UserI admin = AdminUtils.getAdminUser();
        assert admin != null;
        for (DataModelDefinition def : XFTManager.discoverDataModelDefs()) {
            for (String s : def.getSecuredElements()) {

                if ((StringUtils.isNotBlank(s)) && !elements.containsKey(s)) {
                    if (GenericWrapperElement.GetFieldForXMLPath(s + "/project") != null) {
                        ElementSecurity es = ElementSecurity.newElementSecurity(s);
                        es.initExistingPermissions(admin.getUsername());
                        _new = true;
                    }
                }
            }
        }

        for (final XnatPluginBean plugin : XnatPluginBean.getXnatPluginBeans().values()) {
            for (final XnatDataModelBean bean : plugin.getDataModelBeans()) {
                if (!elements.containsKey(bean.getType()) && bean.isSecured()) {
                    if (GenericWrapperElement.GetFieldForXMLPath(bean.getType() + "/project") != null) {
                        ElementSecurity es = ElementSecurity.newElementSecurity(bean.getType());
                        es.initExistingPermissions(admin.getUsername());

                        final boolean hasSingular = StringUtils.isNotBlank(bean.getSingular());
                        final boolean hasPlural = StringUtils.isNotBlank(bean.getPlural());
                        final boolean hasCode = StringUtils.isNotBlank(bean.getCode());
                        if (hasSingular || hasPlural || hasCode) {
                            es.setSingular(bean.getSingular());
                            es.setPlural(bean.getPlural());
                            es.setPlural(bean.getPlural());
                            final StringBuilder query = new StringBuilder("update xdat_element_security set ");
                            if (hasSingular) {
                                query.append("singular = '").append(bean.getSingular()).append("'");
                            }
                            if (hasSingular && hasPlural) {
                                query.append(", ");
                            }
                            if (hasPlural) {
                                query.append("plural = '").append(bean.getPlural()).append("'");
                            }
                            if (hasCode) {
                                if (hasSingular || hasPlural) {
                                    query.append(", ");
                                }
                                query.append("code = '").append(bean.getCode()).append("'");
                            }
                            query.append(" where element_name = '").append(bean.getType()).append("'");
                            final JdbcTemplate template = XDAT.getContextService().getBean(JdbcTemplate.class);
                            final int results = template.update(query.toString());
                            if (logger.isInfoEnabled()) {
                                logger.info("Updated " + results + " rows with the query: " + query.toString());
                            }
                        }
                        _new = true;
                    }
                }
            }
        }

        ElementSecurity.refresh();

        return _new;
    }

    public String toString() {
        try {
            return this.getElementName();
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
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
            elements = null;
            elementDistinctIds = new Hashtable();
            //	    XdatStoredSearch.RefreshPreLoadedSearches();
            //	    UserCache.Clear();
            //        UserGroupManager.Refresh();
            //        guestPermissions= new Hashtable();
            //        guestLoaded=false;

            CacheManager.GetInstance().clearAll();
        }
    }

    /**
     * @param elementName    The name of the element to retrieve.
     * @return The requested element.
     * @throws Exception When something goes wrong. 
     */
    public static ElementSecurity GetElementSecurity(String elementName) throws Exception {
        return GetElementSecurities().get(elementName);
    }

    public static ElementSecurity newElementSecurity(String elementName) throws Exception {
        if (GetElementSecurity(elementName) != null) {
            return GetElementSecurity(elementName);
        }

        UserI user = Users.getUser("admin");

        XFTItem es = XFTItem.NewItem("xdat:element_security", user);
        es.setProperty("element_name", elementName);
        es.setProperty("secondary_password", "0");
        es.setProperty("secure_ip", "0");
        es.setProperty("secure", "1");
        es.setProperty("browse", "1");
        es.setProperty("sequence", "2");
        es.setProperty("quarantine", "0");
        es.setProperty("pre_load", "0");
        es.setProperty("searchable", "1");
        es.setProperty("secure_read", "1");
        es.setProperty("secure_edit", "1");
        es.setProperty("secure_create", "1");
        es.setProperty("secure_delete", "1");
        es.setProperty("accessible", "1");

        es.setProperty("xdat:element_security/primary_security_fields/primary_security_field[0]/primary_security_field", elementName + "/sharing/share/project");
        es.setProperty("xdat:element_security/primary_security_fields/primary_security_field[1]/primary_security_field", elementName + "/project");

        addElementAction(es, 0, "xml", "View XML", "2", "View", "NULL");
        addElementAction(es, 1, "edit", "Edit", "0", "NULL", "edit");
        addElementAction(es, 2, "xml_file", "Download XML", "7", "Download", "NULL");
        addElementAction(es, 3, "email_report", "Email", "8", "NULL", "NULL");

        SaveItemHelper.authorizedSave(es, user, false, false, EventUtils.ADMIN_EVENT(user));

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
            logger.error("", e);
        }
    }

    /**
     * Gets a list of the insecure elements in the system.
     * @return A list of insecure elements.
     * @throws Exception When something goes wrong. 
     */
    public static ArrayList<ElementSecurity> GetInSecureElements() throws Exception {
        ArrayList<ElementSecurity>  al  = new ArrayList<ElementSecurity>();
        Collection<ElementSecurity> ess = GetElementSecurities().values();
        for (ElementSecurity es : ess) {
            if (!es.isSecure()) {
                al.add(es);
            }
        }
        al.trimToSize();
        return al;
    }

    /**
     * @param elementName
     * @return Returns whether element security has been defined for this element
     */
    public static boolean HasDefinedElementSecurity(String elementName) {
        try {
            if (GetElementSecurity(elementName) != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("", e);
            return true;
        }
    }

    /**
     * @return Returns a list of all the secure elements
     * @throws Exception When something goes wrong. 
     */
    public static ArrayList<ElementSecurity> GetSecureElements() throws Exception {
        final ArrayList<ElementSecurity> al = new ArrayList<ElementSecurity>();

        final Collection<ElementSecurity> ess = GetElementSecurities().values();
        for (ElementSecurity es : ess) {
            if (es.isSecure()) {
                al.add(es);
            }
        }

        al.trimToSize();

        return al;
    }

    public static ArrayList<String> GetSecurityElements() throws Exception {
        ArrayList<String> se   = new ArrayList<String>();
        Iterator          iter = GetSecureElements().iterator();
        while (iter.hasNext()) {
            ElementSecurity es    = (ElementSecurity) iter.next();
            Iterator        iter2 = es.getPrimarySecurityFields().iterator();
            while (iter2.hasNext()) {
                String              psf = (String) iter2.next();
                GenericWrapperField f   = GenericWrapperElement.GetFieldForXMLPath(psf);
                if (f.isReference()) {
                    if (!se.contains(f.getReferenceElementName().getFullForeignType())) {
                        se.add(f.getReferenceElementName().getFullForeignType());
                    }
                } else {
//			        if (! se.contains(f.getParentElement().getFullXMLName()))
//			            se.add(f.getParentElement().getFullXMLName());
                }
            }
        }
        se.trimToSize();
        return se;
    }

    public static ArrayList<ElementSecurity> GetQuarantinedElements() throws Exception {
        ArrayList<ElementSecurity> al = new ArrayList<ElementSecurity>();
        try {
            Collection<ElementSecurity> ess = GetElementSecurities().values();
            for (ElementSecurity es : ess) {
                if (es.getBooleanProperty(ViewManager.QUARANTINE) != null) {
                    al.add(es);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        return al;
    }

    public static ArrayList<ElementSecurity> GetPreLoadElements() throws Exception {
        ArrayList<ElementSecurity> al = new ArrayList<ElementSecurity>();
        try {
            Collection<ElementSecurity> ess = GetElementSecurities().values();
            for (ElementSecurity es : ess) {
                if (es.getBooleanProperty("pre_load") != null) {
                    al.add(es);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        return al;
    }

    /**
     * @param elementName
     * @return Whether the element is in the system's list of secure elements
     * @throws Exception When something goes wrong. 
     */
    public static boolean IsSecureElement(String elementName) throws Exception {
        Iterator iter = GetSecureElements().iterator();
        while (iter.hasNext()) {
            ElementSecurity es = (ElementSecurity) iter.next();
            if (es.getElementName().equalsIgnoreCase(elementName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param elementName
     * @return Whether the element is in the system's list of secure elements and the supplied action is secure
     * @throws Exception When something goes wrong. 
     */
    public static boolean IsSecureElement(String elementName, String action) throws Exception {
        Iterator iter = GetSecureElements().iterator();
        while (iter.hasNext()) {
            ElementSecurity es = (ElementSecurity) iter.next();
            if (es.getElementName().equalsIgnoreCase(elementName)) {
                return es.isSecure(action);
            }
        }
        return false;
    }

    /**
     * @param elementName
     * @return Whether the element is in the system's list of insecure elements
     * @throws Exception When something goes wrong. 
     */
    public static boolean IsInSecureElement(String elementName) throws Exception {
        Iterator iter = GetInSecureElements().iterator();
        while (iter.hasNext()) {
            ElementSecurity es = (ElementSecurity) iter.next();
            if (es.getElementName().equalsIgnoreCase(elementName)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<ElementSecurity> GetBrowseableElements() throws Exception {
        ArrayList<ElementSecurity>  al  = new ArrayList<ElementSecurity>();
        Collection<ElementSecurity> ess = GetElementSecurities().values();
        for (ElementSecurity es : ess) {
            if (es.isBrowseable()) {
                al.add(es);
            }
        }

        return al;
    }

    /**
     * @param elementName
     * @return Whether element is browseable
     * @throws Exception When something goes wrong. 
     */
    public static boolean IsBrowseableElement(String elementName) throws Exception {
        ElementSecurity es = (ElementSecurity) GetElementSecurity(elementName);
        if (es != null) {
            return es.isBrowseable();
        } else {
            return false;
        }
    }

    /**
     * @param elementName
     * @return Whether element is searchable
     * @throws Exception When something goes wrong. 
     */
    public static boolean IsSearchable(String elementName) throws Exception {
        ElementSecurity es = (ElementSecurity) GetElementSecurity(elementName);

        if (es != null) {
            return es.isSearchable();
        } else {
            return false;
        }
    }

    /**
     * @param elementName
     * @return Whether element has primary security fields
     * @throws Exception When something goes wrong. 
     */
    public static boolean HasPrimarySecurityFields(String elementName) throws Exception {
        ElementSecurity es = (ElementSecurity) GetElementSecurity(elementName);
        if (es.getPrimarySecurityFields().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param i The item for which to create the security element.
     */
    public ElementSecurity(ItemI i) {
        this.setItem(i);
        try {
            this.elementName = getElementName();

            Iterator psfs = getChildItems(org.nrg.xft.XFT.PREFIX + ":element_security.primary_security_fields.primary_security_field").iterator();
            while (psfs.hasNext()) {
                ItemI  child = (ItemI) psfs.next();
                String s     = (String) child.getProperty("primary_security_field");
                if (s != null) {
                    this.addPrimarySecurityField(s);
                }
            }

            Iterator eas = getChildItemCollection(org.nrg.xft.XFT.PREFIX + ":element_security.element_actions.element_action").getItems("xdat:element_action_type.sequence").iterator();
            while (eas.hasNext()) {
                ItemI         child = (ItemI) eas.next();
                ElementAction ea    = new ElementAction(child);
                ea.getItem().internValues();
                this.addElementAction(ea);
            }

            eas = getChildItemCollection(org.nrg.xft.XFT.PREFIX + ":element_security.listing_actions.listing_action").getItems("xdat:element_security_listing_action.sequence").iterator();
            while (eas.hasNext()) {
                ItemI                            child = (ItemI) eas.next();
                XdatElementSecurityListingAction ea    = new XdatElementSecurityListingAction(child);
                ea.getItem().internValues();
                this.addListingAction(ea);
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    /**
     * @return The element's name
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
     */
    public String getElementName() throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        try {
            return (String) getProperty("element_name");
        } catch (FieldEmptyException e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * @return The sequence for this element
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
     */
    public Integer getSequence() throws XFTInitException, ElementNotFoundException, FieldNotFoundException {
        try {
            return (Integer) getProperty("sequence");
        } catch (FieldEmptyException e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * @return Returns the element
     * @throws Exception When something goes wrong. 
     */
    public SchemaElement getSchemaElement() throws Exception {
        return SchemaElement.GetElement(getElementName());
    }

    /**
     * @return Returns whether element is secure
     */
    public boolean isSecure() {
        try {
            Integer s = (Integer) getProperty("secure");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return Returns whether action is secure
     */
    public boolean isSecure(String action) {
        if (this.isSecure()) {
            String name = "secure_" + action;
            try {
                Integer s = (Integer) getProperty(name);
                if (s != null) {
                    if (s.intValue() == 1) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } catch (Exception e) {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * @return Returns whether read is secure
     */
    public boolean isSecureRead() {
        try {
            Integer s = (Integer) getProperty("secure_read");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @return Returns whether edit is secure
     */
    public boolean isSecureEdit() {
        try {
            Integer s = (Integer) getProperty("secure_edit");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @return Returns whether creation is secure
     */
    public boolean isSecureCreate() {
        try {
            Integer s = (Integer) getProperty("secure_create");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @return Returns whether deletion is secure.
     */
    public boolean isSecureDelete() {
        try {
            Integer s = (Integer) getProperty("secure_delete");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @return  Returns whether the element is browsable.
     */
    public boolean isBrowseable() {
        try {
            Integer s = (Integer) getProperty("browse");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * @return Returns whether the element is searchable.
     */
    public boolean isSearchable() {
        try {
            Integer s = (Integer) getProperty("searchable");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    /**
     * @return Returns whether the password is secure.
     */
    public boolean isSecurePassword() {
        try {
            Integer s = (Integer) getProperty("secure_password");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return Returns whether the IP is secure
     */
    public boolean isSecureIP() {
        try {
            Integer s = (Integer) getProperty("secure_ip");
            if (s != null) {
                if (s.intValue() == 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param s
     */
    public void addPrimarySecurityField(String s) {
        primarySecurityFields.add(s);
    }

    /**
     * @return Returns the list of primary security fields
     */
    public ArrayList<String> getPrimarySecurityFields() {
        return primarySecurityFields;
    }

    /**
     * @return Returns the list of element actions.
     */
    public ArrayList<ElementAction> getElementActions() {
        return elementActions;
    }

    public class ElementActionGroup {
        public String                   group   = "";
        public ArrayList<ElementAction> actions = new ArrayList<ElementAction>();

        public String getGroup() {
            return group;
        }

        public ArrayList<ElementAction> getActions() {
            return actions;
        }
    }

    public ArrayList<ElementActionGroup> getElementActionsByGroupings() {
        ArrayList<ElementActionGroup> al = new ArrayList<ElementActionGroup>();

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
     * @param ea
     */
    public void addElementAction(ElementAction ea) {
        elementActions.add(ea);
    }

    /**
     * @return Returns the list of listing actions
     */
    public ArrayList<XdatElementSecurityListingAction> getListingActions() {
        return listingActions;
    }

    String laJSON = null;

    public String getListingActionsJSON() {
        if (laJSON == null) {
            StringBuffer sb = new StringBuffer("[");
            int          c  = 0;
            for (XdatElementSecurityListingAction la : this.listingActions) {
                if (c++ > 0) {
                    sb.append(",");
                }
                sb.append("{");
                sb.append("action:\"").append(la.getElementActionName()).append("\"");
                sb.append(",display:\"").append(la.getDisplayName()).append("\"");
                if (la.getPopup() != null) {
                    sb.append(",popup:\"").append(la.getPopup()).append("\"");
                }
                if (la.getSequence() != null) {
                    sb.append(",sequence:").append(la.getSequence()).append("");
                }
                if (la.getImage() != null) {
                    sb.append(",image:\"").append(la.getImage()).append("\"");
                }
                if (la.getParameterstring() != null) {
                    sb.append(",params:\"").append(la.getParameterstring()).append("\"");
                }

                sb.append("}");
            }
            sb.append("]");
            laJSON = sb.toString();
        }
        return laJSON;
    }

    /**
     * @param ea
     */
    public void addListingAction(org.nrg.xdat.om.XdatElementSecurityListingAction ea) {
        listingActions.add(ea);
        laJSON = null;
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

    private ArrayList permissionItems = null;

    /**
     * @return Returns a list of the PermissionsItems for the user
     * @throws Exception When something goes wrong. 
     */
    public List<PermissionItem> getPermissionItems(String login) throws Exception {
        if (permissionItems == null) {
            permissionItems = new ArrayList();

            for (String fieldName : getPrimarySecurityFields()) {
                if (!fieldName.startsWith(this.getElementName())) {
                    fieldName = this.getElementName() + XFT.PATH_SEPARATOR + fieldName;
                }
                final GenericWrapperField field = GenericWrapperElement.GetFieldForXMLPath(fieldName);

                Hashtable hash = null;
                if (field.isReference()) {
                    SchemaElementI se = SchemaElement.GetElement(field.getReferenceElementName().getFullForeignType());
                    hash = GetDistinctIdValuesFor(se.getFullXMLName(), "default", login);
                } else {
                    hash = GetDistinctIdValuesFor(field.getParentElement().getFullXMLName(), fieldName, login);
                }

                Enumeration enumer = hash.keys();
                while (enumer.hasMoreElements()) {
                    Object         id      = enumer.nextElement();
                    String         display = (String) hash.get(id);
                    PermissionItem pi      = new PermissionItem();
                    pi.setFullFieldName(fieldName);
                    pi.setDisplayName(display);
                    pi.setValue(id);
                    permissionItems.add(pi);
                }
            }

            permissionItems.trimToSize();
            Collections.sort(permissionItems, PermissionItem.GetComparator());
        }
        return permissionItems;
    }

    /**
     * @return Returns whether the element has a displayField
     * @throws Exception When something goes wrong. 
     */
    public boolean hasDisplayValueOption() throws Exception {
        SchemaElement  se           = SchemaElement.GetElement(elementName);
        ElementDisplay ed           = se.getDisplay();
        DisplayField   idField      = ed.getDisplayField(ed.getValueField());
        DisplayField   displayField = ed.getDisplayField(ed.getDisplayField());
        if (idField != null && displayField != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param elementName
     * @param fieldName
     * @return Returns a hastable of the distinct id values for the supplied information
     * @throws Exception When something goes wrong. 
     */
    public static Hashtable GetDistinctIdValuesFor(String elementName, String fieldName, String login) throws Exception {
        //Hashtable hash1 = (Hashtable)elementDistinctIds.get(elementName); REMOVED TO PREVENT CACHING
        Hashtable<String, Hashtable<String, String>> hash1 = new Hashtable<String, Hashtable<String, String>>();
        SchemaElement                                se    = SchemaElement.GetElement(elementName);
        ElementDisplay                               ed    = se.getDisplay();
//		if (hash1 == null)
//		{
//			hash1 = new Hashtable();
//			elementDistinctIds.put(elementName,hash1);
//		}

        Hashtable hash2 = (Hashtable) hash1.get(fieldName);
        if (hash2 == null) {
            if (fieldName.equalsIgnoreCase("default")) {
                Hashtable newHash               = new Hashtable();
                boolean   hasDefinedIdValuePair = false;
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
                    Iterator iter = se.getAllPrimaryKeys().iterator();

                    String query = "SELECT DISTINCT ON (";
                    String cols  = "";

                    int counter = 0;
                    while (iter.hasNext()) {
                        SchemaField sf = (SchemaField) iter.next();
                        if (counter == 0) {
                            cols += sf.getSQLName();
                        } else {
                            cols += ", " + sf.getSQLName();
                        }
                    }

                    query += cols + ") " + cols + " FROM " + se.getSQLName();

                    XFTTable table = TableSearch.Execute(query, se.getDbName(), login);
                    table.resetRowCursor();
                    while (table.hasMoreRows()) {
                        Object[] row = table.nextRow();
                        newHash.put(row[0].toString(), row[0].toString());
                    }
                    hash1.put(fieldName, newHash);
                    hash2 = newHash;
                }

            } else {
                Hashtable newHash = new Hashtable();

                GenericWrapperField f  = GenericWrapperElement.GetFieldForXMLPath(fieldName);
                ArrayList           al = f.getPossibleValues();
                if (al.size() > 0) {
                    Iterator pvs = al.iterator();
                    while (pvs.hasNext()) {
                        String s = (String) pvs.next();
                        newHash.put(s, s);
                    }
                    hash1.put(fieldName, newHash);
                    hash2 = newHash;

                } else {
                    if (f.getBaseElement() == null || f.getBaseElement() == "") {
                        GenericWrapperElement parent  = f.getParentElement().getGenericXFTElement();
                        String                xmlPath = null;
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
                        String                foreignTable = f.getBaseElement();
                        GenericWrapperElement foreign      = GenericWrapperElement.GetElement(foreignTable);
                        String                foreignCol   = f.getBaseCol();
                        String                query        = "SELECT DISTINCT " + foreignCol + " FROM " + foreign.getSQLName();
                        XFTTable              table        = TableSearch.Execute(query, se.getDbName(), login);

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
     * @return Returns a SecurityValues object containing a map of item membership
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
     * @return Returns a SecurityValues object containing a map of item membership
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
                            logger.error("", e);
                        } catch (ElementNotFoundException e) {
                            logger.error("", e);
                        } catch (FieldNotFoundException e) {
                            logger.error("", e);
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
                                            logger.error("", e1);
                                        } catch (SQLException e1) {
                                            logger.error("", e1);
                                        } catch (Exception e1) {
                                            logger.error("", e1);
                                        }
                                    }

                                    if (o == null) {
                                        if (XFT.VERBOSE) {
                                            System.out.println("\nUnknown value in " + s);
                                        }
                                        logger.error("\nUnknown value in " + s);
                                        if (XFT.VERBOSE) {
                                            System.out.println(sub.toXML_String(false));
                                        }
                                        logger.error(sub.toXML_String(false));
                                        if (XFT.VERBOSE) {
                                            System.out.println("This field is a pre-defined security field and must match a pre-defined value.");
                                        }
                                        logger.error("This field is a pre-defined security field and must match a pre-defined value.");
                                        if (XFT.VERBOSE) {
                                            System.out.println("This value is not currently in the database.");
                                        }
                                        logger.error("This value is not currently in the database.");
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
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
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
            logger.error("", e);
        }
        return null;
    }


    /**
     * @return A sorted List of Strings
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
     * @throws Exception When something goes wrong. 
     */
    public static ArrayList GetNonXDATElementNames() throws Exception {
        ArrayList<String>           al  = new ArrayList<String>();
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
                            XFTRelationSpecification spec = (XFTRelationSpecification) iter.next();
                            s = s.substring(0, s.lastIndexOf(XFT.PATH_SEPARATOR) + 1) + spec.getLocalCol();
                            break;
                        }
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

        return s;
    }

    public String getSecurityFields() {
        if (this.primarySecurityFields.size() == 0) {
            return "";
        } else if (this.primarySecurityFields.size() == 1) {
            return this.primarySecurityFields.get(0).toString();
        } else {
            String   s    = "";
            Iterator iter = this.primarySecurityFields.iterator();
            while (iter.hasNext()) {
                s += " " + (String) iter.next();
            }
            return s;
        }
    }

    List<String> fields = null;

    public boolean hasField(String field) {
        if (fields == null) {
            try {
                fields = this.getSchemaElement().getAllDefinedFields();
            } catch (Exception e) {
                logger.error("", e);
                return false;
            }
        }
        return fields.contains(field);
    }

    public void initPSF(String field, EventMetaI meta) {
        try {
            String   query = "SELECT primary_security_field FROM xdat_primary_security_field WHERE primary_security_field = '" + field + "';";
            XFTTable t     = XFTTable.Execute(query, this.getDBName(), null);
            if (t.size() == 0) {
                XdatPrimarySecurityField psf = new XdatPrimarySecurityField(this.getUser());
                psf.setPrimarySecurityField(field);
                psf.setProperty("xdat:primary_security_field/primary_security_fields_primary_element_name", this.getElementName());
                SaveItemHelper.authorizedSave(psf, this.getUser(), true, true, meta);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    //method added to facilitate the adding of permissions once you've created a new data type.  It loads a vm to build the requisite SQL.
    public void initExistingPermissions(String userName) {
        InputStream is = null;
        try {
            is = CustomClasspathResourceLoader.getInputStream("/screens/new_dataType_permissions.vm");
            if (is != null) {
                final List<String> stmts   = Lists.newArrayList();
                final Scanner      scanner = new Scanner(is).useDelimiter(";");
                while (scanner.hasNext()) {
                    String query = scanner.next();
                    if (StringUtils.isNotBlank(query)) {
                        stmts.add(query.replace("$!element_name", this.getElementName()).replace("$element_name", this.getElementName()));
                    }
                }
                PoolDBUtils.ExecuteBatch(stmts, this.getDBName(), userName);
            }
        } catch (Exception ignore) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }

        try {
            DBAction.InsertMetaDatas(XdatElementAccess.SCHEMA_ELEMENT_NAME);
            DBAction.InsertMetaDatas(XdatFieldMappingSet.SCHEMA_ELEMENT_NAME);
            DBAction.InsertMetaDatas(XdatFieldMapping.SCHEMA_ELEMENT_NAME);
        } catch (Exception e2) {
            logger.error("", e2);
        }

        try {
    		final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
            eventService.triggerEvent(new XftItemEvent(Groups.getGroupDatatype(), XftItemEvent.UPDATE));
        } catch (NoSuchBeanDefinitionException e) {
            logger.info("Didn't find the NrgEventService instance, it probably hasn't been initialized yet.");
        } catch (Exception e1) {
            logger.error("", e1);
        }

        try {
            PoolDBUtils.ClearCache(this.getDBName(), userName, Groups.getGroupDatatype());
        } catch (Exception e) {
            logger.error("", e);
        }
    }
//    
//    public static Hashtable<String,Hashtable<String,GuestPermission>> guestPermissions = new Hashtable<String,Hashtable<String,GuestPermission>>();
//    public static boolean guestLoaded = false;
//    
//    public static boolean GetGuestPermission(String elementName, String action, Object value){
//        GuestPermission perm = null;
//        Hashtable<String,GuestPermission> permissions = guestPermissions.get(elementName);
//        if (permissions == null)
//        {
//            if (!guestLoaded)
//            {
//                guestLoaded=true;
//                String query ="SELECT element_name, create_element, read_element, edit_element, delete_element, active_element, field_value FROM xdat_element_access ea LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id=fm.xdat_field_mapping_set_xdat_field_mapping_set_id LEFT JOIN xdat_user u ON ea.xdat_user_xdat_user_id=u.xdat_user_id WHERE u.login='guest';";
//                try {
//                    XFTTable table = XFTTable.Execute(query, ((ElementSecurity)GetElementSecurities().entrySet()).getDBName(), null);
//                                        
//                    if (table.size()>0)
//                    {
//                        
//                        table.resetRowCursor();
//                        while(table.hasMoreRows()){
//                            Object[] row = table.nextRow();
//                            String name = (String)row[0];
//                            Integer create = (Integer)row[1];
//                            Integer read = (Integer)row[2];
//                            Integer edit = (Integer)row[3];
//                            Integer delete = (Integer)row[4];
//                            Integer activate = (Integer)row[5];
//                            String v = (String)row[6];
//                            
//                            GuestPermission temp = new ElementSecurity.GuestPermission(name,v,edit,read,delete,create,activate);
//                            
//                                                        
//                            if (guestPermissions.get(name)==null){
//                                Hashtable<String,GuestPermission> level1 = new Hashtable<String,GuestPermission>();
//                                guestPermissions.put(name, level1);
//                                level1.put(v, temp);
//                            }else{
//                                guestPermissions.get(name).put(v, temp);
//                            }
//                        }
//                    }
//                } catch (SQLException e) {
//                    logger.error("",e);
//                } catch (DBPoolException e) {
//                    logger.error("",e);
//                } catch (Exception e) {
//                    logger.error("",e);
//                }
//                
//                permissions=guestPermissions.get(elementName);
//            }            
//        }
//        
//        if (permissions !=null){
//            perm = permissions.get(value);
//        
//            if (perm!=null){
//                if (action.equals(SecurityManager.READ)){
//                    return perm.isRead();
//                }else if (action.equals(SecurityManager.EDIT)){
//                    return perm.isEdit();
//                }else if (action.equals(SecurityManager.DELETE)){
//                    return perm.isDelete();
//                }else if (action.equals(SecurityManager.CREATE)){
//                    return perm.isCreate();
//                }else if (action.equals(SecurityManager.ACTIVATE)){
//                    return perm.isActivate();
//                }
//            }
//        }
//        
//        
//        return false;
//    }
//    
//    public static class GuestPermission{
//        private String elementName = null;
//        private String value = null;
//        
//        private boolean edit = false;
//        private boolean read = false;
//        private boolean delete = false;
//        private boolean create = false;
//        private boolean activate = false;
//        
//        public GuestPermission(String en, String v, boolean e, boolean r, boolean d, boolean c, boolean a){
//            elementName=en;
//            value=v;
//            edit=e;
//            read=r;
//            delete=d;
//            create=c;
//            activate=a;
//        }
//        
//        public GuestPermission(String en, String v, Integer e, Integer r, Integer d, Integer c, Integer a){
//            elementName=en;
//            value=v;
//            if (e!=null && e.intValue()==1)
//                edit=true;
//            if (r!=null && r.intValue()==1)
//                read=true;
//            if (d!=null && d.intValue()==1)
//                delete=true;
//            if (c!=null && c.intValue()==1)
//                create=true;
//            if (a!=null && a.intValue()==1)
//                activate=true;
//        }
//
//        /**
//         * @return the activate
//         */
//        public boolean isActivate() {
//            return activate;
//        }
//
//        /**
//         * @param activate the activate to set
//         */
//        public void setActivate(boolean activate) {
//            this.activate = activate;
//        }
//
//        /**
//         * @return the create
//         */
//        public boolean isCreate() {
//            return create;
//        }
//
//        /**
//         * @param create the create to set
//         */
//        public void setCreate(boolean create) {
//            this.create = create;
//        }
//
//        /**
//         * @return the delete
//         */
//        public boolean isDelete() {
//            return delete;
//        }
//
//        /**
//         * @param delete the delete to set
//         */
//        public void setDelete(boolean delete) {
//            this.delete = delete;
//        }
//
//        /**
//         * @return the edit
//         */
//        public boolean isEdit() {
//            return edit;
//        }
//
//        /**
//         * @param edit the edit to set
//         */
//        public void setEdit(boolean edit) {
//            this.edit = edit;
//        }
//
//        /**
//         * @return the elementName
//         */
//        public String getElementName() {
//            return elementName;
//        }
//
//        /**
//         * @param elementName the elementName to set
//         */
//        public void setElementName(String elementName) {
//            this.elementName = elementName;
//        }
//
//        /**
//         * @return the read
//         */
//        public boolean isRead() {
//            return read;
//        }
//
//        /**
//         * @param read the read to set
//         */
//        public void setRead(boolean read) {
//            this.read = read;
//        }
//
//        /**
//         * @return the value
//         */
//        public String getValue() {
//            return value;
//        }
//
//        /**
//         * @param value the value to set
//         */
//        public void setValue(String value) {
//            this.value = value;
//        }
//        
//    }


    public Comparator getComparator() {
        return new ESComparator();
    }

    public class ESComparator implements Comparator {
        public ESComparator() {
        }

        public int compare(Object o1, Object o2) {
            try {
                ElementSecurity value1 = (ElementSecurity) (o1);
                ElementSecurity value2 = (ElementSecurity) (o2);

                if (value1 == null) {
                    if (value2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (value2 == null) {
                    return 1;
                }

                if (((Comparable) value1.getElementName()).equals((Comparable) value2.getElementName())) {
                    return ((Comparable) value1.getElementName()).compareTo((Comparable) value2.getElementName());
                } else {
                    Comparable i1      = (Comparable) value1.getElementName();
                    Comparable i2      = (Comparable) value2.getElementName();
                    int        _return = i1.compareTo(i2);
                    return _return;
                }
            } catch (XFTInitException e) {
                logger.error("", e);
                return 0;
            } catch (ElementNotFoundException e) {
                logger.error("", e);
                return 0;
            } catch (FieldNotFoundException e) {
                logger.error("", e);
                return 0;
            }
        }
    }


    //FIELD

    private Boolean _Accessible = null;

    public Boolean getAccessible() {
        try {
            if (_Accessible == null) {
                _Accessible = getBooleanProperty("accessible", true);
                return _Accessible;
            } else {
                return _Accessible;
            }
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    public boolean matchesUsageEntry(String id) {
        String usage = getUsage();

        if (usage != null) {
            return Arrays.asList(usage.split("[\\s]*,[\\s]*")).contains(id);
        }

        return false;
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
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
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    public String getSingularDescription() {
        if (getSingular() == null) {
            try {
                return getSchemaElement().getDisplay().getDescription();
            } catch (Throwable e) {
                logger.error("", e);
                try {
                    return this.getElementName();
                } catch (XFTInitException e1) {
                    logger.error("", e1);
                } catch (ElementNotFoundException e1) {
                    logger.error("", e1);
                } catch (FieldNotFoundException e1) {
                    logger.error("", e1);
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
                logger.error("", e);
                try {
                    return this.getElementName();
                } catch (XFTInitException e1) {
                    logger.error("", e1);
                } catch (ElementNotFoundException e1) {
                    logger.error("", e1);
                } catch (FieldNotFoundException e1) {
                    logger.error("", e1);
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
            logger.error("", e);
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
            logger.error("", e);
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
            logger.error("", e);
        }

        return elementName;
    }
}

